// 글로벌 로그아웃 함수 정의 - 어디서나 접근 가능
function handleLogout() {
  console.log('글로벌 로그아웃 함수 호출');
  
  // Auth 객체를 사용하거나 직접 로그아웃 처리
  if (typeof Auth !== 'undefined' && Auth.logout) {
    Auth.logout();
  } else {
    // 로그인 상태 및 사용자 역할 제거
    localStorage.removeItem('isLoggedIn');
    localStorage.removeItem('userName');
    localStorage.removeItem('userRole');
    sessionStorage.removeItem('isLoggedIn');
    sessionStorage.removeItem('userName');
    sessionStorage.removeItem('userRole');
    
    // 로그아웃 메시지
    alert('로그아웃 되었습니다.');
    
    // 메인 페이지로 리디렉션
    window.location.href = '../pages/index.html';
  }
}

// HTML 컴포넌트 로드 함수
document.addEventListener('DOMContentLoaded', function() {
  // 현재 페이지의 경로를 확인합니다
  const currentPath = window.location.pathname;
  
  // 헤더 플레이스홀더 엘리먼트를 가져옵니다
  const headerPlaceholder = document.getElementById('header-placeholder');
  
  // 푸터 플레이스홀더 엘리먼트를 가져옵니다
  const footerPlaceholder = document.getElementById('footer-placeholder');
  
  // 상대 경로를 결정합니다 (현재 경로가 /pages/로 시작하면 상위 디렉토리로 이동해야 함)
  const relativePath = currentPath.includes('/pages/') ? '../' : '';
  
  // 관리자 페이지인지 확인합니다 (URL에 admin.html이 포함되어 있는지)
  const isAdminPage = currentPath.includes('admin.html');
    if (headerPlaceholder) {
    // 모든 경우에 header.html 로드
    // 권한에 따라 보이는 부분은 auth_roles.js에서 처리
    const headerFile = 'components/header.html';      fetch(`${relativePath}${headerFile}`)
      .then(response => response.text())
      .then(data => {
        headerPlaceholder.innerHTML = data;
        // 헤더가 로드된 후 권한 기반 UI 업데이트 적용
        if (typeof Auth !== 'undefined') {
          Auth.applyRoleVisibility();
        } else if (typeof AuthRoles !== 'undefined') {
          // 이전 버전 호환성 유지
          AuthRoles.applyRoleVisibility();
        } else {
          console.error('Auth is not defined. Make sure auth.js is loaded.');
        }
        
        // 헤더가 완전히 로드된 후 사용자 정보 즉시 표시
        updateUserInfo();
      })
      .catch(error => {
        console.error('Error loading header:', error);
      });
  }
    if (footerPlaceholder) {
    fetch(`${relativePath}components/footer.html`)
      .then(response => response.text())
      .then(data => {
        footerPlaceholder.innerHTML = data;
      })
      .catch(error => {
        console.error('Error loading footer:', error);
      });
  }  // 로그아웃 처리 함수 - 컴포넌트 내부용 
  const internalHandleLogout = function() {
    // 글로벌 함수 호출
    handleLogout();
  }

  // 사용자 정보 업데이트 함수
  function updateUserInfo() {
    console.log('사용자 정보 업데이트 시작');
    
    // localStorage 또는 sessionStorage에서 사용자 이름 가져오기
    const userName = localStorage.getItem('userName') || sessionStorage.getItem('userName') || '사용자';
    console.log('로드된 사용자 이름:', userName);
    
    // 일반 사용자 이름 표시하기
    const usernameDisplayUser = document.getElementById('username-display-user');
    if (usernameDisplayUser) {
      usernameDisplayUser.textContent = userName;
      // 부모 요소에 loaded 클래스 추가
      usernameDisplayUser.parentElement.classList.add('loaded');
      console.log('일반 사용자 이름 표시 완료');
    }
    
    // 매니저 사용자 이름 표시하기
    const usernameDisplayManager = document.getElementById('username-display-manager');
    if (usernameDisplayManager) {
      usernameDisplayManager.textContent = userName;
      // 부모 요소에 loaded 클래스 추가
      usernameDisplayManager.parentElement.classList.add('loaded');
      console.log('매니저 이름 표시 완료');
    }
    
    // 관리자 사용자 이름 표시하기
    const usernameDisplayAdmin = document.getElementById('username-display-admin');
    if (usernameDisplayAdmin) {
      usernameDisplayAdmin.textContent = userName;
      // 부모 요소에 loaded 클래스 추가
      usernameDisplayAdmin.parentElement.classList.add('loaded');
      console.log('관리자 이름 표시 완료');
    }
    
    // 로그아웃 버튼 이벤트 리스너 설정
    setupLogoutButtons();
  }
  
  // 로그아웃 버튼 이벤트 리스너 설정 함수
  function setupLogoutButtons() {    // 로그아웃 버튼 세팅 - 이미 HTML에 onclick 속성이 추가되어 있지만, 이벤트 리스너도 추가
    // 일반 사용자 로그아웃 버튼 기능 추가
    const logoutBtnUser = document.getElementById('logout-btn-user');
    if (logoutBtnUser) {
      // 직접 글로벌 함수 할당
      logoutBtnUser.onclick = handleLogout;
      console.log('일반 사용자 로그아웃 버튼 설정 완료');
    }
    
    // 매니저 로그아웃 버튼 기능 추가
    const logoutBtnManager = document.getElementById('logout-btn-manager');
    if (logoutBtnManager) {
      logoutBtnManager.onclick = handleLogout;
      console.log('매니저 로그아웃 버튼 설정 완료');
    }
    
    // 관리자 로그아웃 버튼 기능 추가
    const logoutBtnAdmin = document.getElementById('logout-btn-admin');
    if (logoutBtnAdmin) {
      logoutBtnAdmin.onclick = handleLogout;
      console.log('관리자 로그아웃 버튼 설정 완료');
    }
    
    // 이전 버전 호환성을 위한 기존 로그아웃 버튼 처리
    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) {
      logoutBtn.onclick = handleLogout;
      console.log('기존 로그아웃 버튼 설정 완료');
    }
  }
  
  // 혹시 모를 문제를 대비한 백업 타이머 - 헤더 로드와 관계없이 일정 시간 후 실행
  setTimeout(() => {
    updateUserInfo();
    console.log('백업 타이머로 사용자 정보 업데이트');
  }, 500);
});
