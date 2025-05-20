// HTML 컴포넌트 로드 관리 스크립트

// 이클립스와 일반 환경에서 모두 작동하도록 경로 설정 (즉시 실행)
(function() {
  // base 태그 대신 전역 변수로 환경 정보 저장 
  // (base 태그는 다른 스크립트의 상대 경로에 영향을 줄 수 있음)
  window.KIRINI_ENV = {
    isEclipse: window.location.pathname.includes('/kirini/'),
    basePath: window.location.pathname.includes('/kirini/') ? '/kirini/view/' : ''
  };
  
  console.log('환경 설정:', window.KIRINI_ENV);
})();

// 헤더 초기화 및 UI 처리 (기존 header.js의 내용)
const Header = {
  // 헤더 UI 처리
  initialize: function() {
    console.log('헤더 초기화 시작 (components.js에서 호출)');
    
    // 로그인 상태에 따른 UI 업데이트
    this.updateAuthUI();
    
    // 로그아웃 버튼 이벤트 설정
    this.setupLogoutButton();
    
    console.log('헤더 초기화 완료 (components.js에서 호출)');
  },
  
  // 권한에 따른 UI 업데이트
  updateAuthUI: function() {
    if (typeof Auth === 'undefined') {
      console.error('Auth 객체를 찾을 수 없습니다. auth.js가 로드되었는지 확인하세요.');
      return;
    }
    
    // Auth 객체의 UI 처리 기능 사용
    Auth.applyRoleVisibility();
    
    // 사용자 이름 업데이트
    this.updateUserInfo();
  },
  
  // 사용자 정보 업데이트
  updateUserInfo: function() {
    console.log('사용자 정보 업데이트 시작');
    
    const userName = localStorage.getItem('userName') || sessionStorage.getItem('userName') || '사용자';
    console.log('로드된 사용자 이름:', userName);
    
    let currentRole = 'GUEST';
    if (typeof Auth !== 'undefined') {
      currentRole = Auth.getCurrentRole();
    } else {
      const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true' || sessionStorage.getItem('isLoggedIn') === 'true';
      if (isLoggedIn) {
        currentRole = localStorage.getItem('userRole') || sessionStorage.getItem('userRole') || 'USER';
      }
    }
    
    const usernameDisplay = document.getElementById('username-display');
    if (usernameDisplay) {
      usernameDisplay.textContent = userName;
      console.log('기본 사용자 이름 표시 완료');
    }
    
    if (currentRole === 'USER') {
      const usernameDisplayUser = document.getElementById('username-display-user');
      if (usernameDisplayUser) {
        usernameDisplayUser.textContent = userName;
        usernameDisplayUser.parentElement.classList.add('loaded');
        console.log('일반 사용자 이름 표시 완료');
      }
    } else if (currentRole === 'MANAGER') {
      const usernameDisplayManager = document.getElementById('username-display-manager');
      if (usernameDisplayManager) {
        usernameDisplayManager.textContent = userName;
        usernameDisplayManager.parentElement.classList.add('loaded');
        console.log('매니저 이름 표시 완료');
      }
    } else if (currentRole === 'ADMIN') {
      const usernameDisplayAdmin = document.getElementById('username-display-admin');
      if (usernameDisplayAdmin) {
        usernameDisplayAdmin.textContent = userName;
        usernameDisplayAdmin.parentElement.classList.add('loaded');
        console.log('관리자 이름 표시 완료');
      }
    }
  },
  // 로그아웃 버튼 이벤트 설정
  setupLogoutButton: function() {
    if (typeof Auth === 'undefined' || !Auth.logout) {
      console.error('Auth 객체를 찾을 수 없습니다. 로그아웃 기능을 사용할 수 없습니다.');
      return;
    }
    
    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) {
      logoutBtn.addEventListener('click', function() {
        Auth.logout();
      });
      console.log('로그아웃 버튼 이벤트 설정 완료');
    }
  }
};

// 외부에서 헤더 함수 접근 가능하도록 전역 노출
window.Header = Header;

// HTML 컴포넌트 로드 함수
document.addEventListener('DOMContentLoaded', function() {  // 현재 페이지의 경로를 확인합니다
  const currentPath = window.location.pathname;
  console.log('현재 경로:', currentPath);
  
  // 헤더 플레이스홀더 엘리먼트를 가져옵니다
  const headerPlaceholder = document.getElementById('header-placeholder');
  
  // 푸터 플레이스홀더 엘리먼트를 가져옵니다
  const footerPlaceholder = document.getElementById('footer-placeholder');
  
  // 상대 경로를 결정합니다
  let relativePath = '';
  
  // 이클립스 환경인지 확인 (전역 변수 사용)
  const isEclipse = window.KIRINI_ENV && window.KIRINI_ENV.isEclipse;
  
  if (isEclipse) {
    // 이클립스 환경일 경우
    relativePath = '';  // 기본값
    
    // 하위 폴더에 있을 경우 상대 경로 조정
    if (currentPath.includes('/keyboard_terms/') || currentPath.includes('/pages/')) {
      console.log('이클립스 환경: 하위 폴더 감지');
      relativePath = '../';
    }
  } else {
    // 일반 환경일 경우
    const isInSubFolder = currentPath.includes('/pages/') || currentPath.includes('/keyboard_terms/');
    relativePath = isInSubFolder ? '../' : '';
  }
  
  console.log('사용할 상대 경로:', relativePath);
  
  // 헤더 로드
  if (headerPlaceholder) {
    // 헤더 컴포넌트 로드
    const headerFile = 'components/header.html';      
    fetch(`${relativePath}${headerFile}`)
      .then(response => response.text())
      .then(data => {
        headerPlaceholder.innerHTML = data;
        // 헤더 HTML이 로드된 후 Header 객체가 존재하고, Auth 객체도 존재한다면 초기화 실행
        if (typeof Header !== 'undefined' && Header.initialize) {
          // 회원가입 페이지가 아닌 경우에만 일반 초기화 실행
          const isSignupPage = window.location.pathname.toLowerCase().includes('/signup.html');
          if (!isSignupPage) {
            Header.initialize();
          } else {
            // 회원가입 페이지 특별 처리
            document.body.classList.add('signup-page');
            // 모든 auth-section 비활성화
            document.querySelectorAll('.auth-section').forEach(section => {
              section.classList.remove('active');
              section.classList.add('element-hidden'); // element-hidden 클래스 사용
            });
            // guest-section만 활성화
            const guestSection = document.querySelector('.guest-section');
            if (guestSection) {
              guestSection.classList.add('active');
              guestSection.classList.remove('element-hidden'); // element-hidden 클래스 제거
            }
            // 로그인 관련 요소들 중 guest-only만 표시
            document.querySelectorAll('.guest-only').forEach(el => {
                el.classList.remove('element-hidden');
            });
            document.querySelectorAll('.logged-in-only, .user-only, .user-only-not-admin, .admin-only, .manager-only, .manager-admin-only').forEach(el => {
                el.classList.add('element-hidden');
            });
            console.log("회원가입 페이지: components.js에서 헤더 로드 후 GUEST 모드 강제 적용");
          }
        }
      })
      .catch(error => {
        console.error('헤더 로딩 오류:', error);
      });
  }
  
  // 푸터 로드
  if (footerPlaceholder) {
    // 헤더와 동일한 패턴으로 일관성 유지
    const footerFile = 'components/footer.html';
    fetch(`${relativePath}${footerFile}`)
      .then(response => response.text())
      .then(data => {
        footerPlaceholder.innerHTML = data;
      })
      .catch(error => {
        console.error('푸터 로딩 오류:', error);
      });
  }
});
