/**
 * KIRINI 권한 관리 및 인증 모듈
 * auth_check.js와 auth_roles.js를 통합한 파일
 */

// Auth 객체 정의 - 인증 및 권한 관리를 위한 통합 객체
const Auth = {
  // 사용자 역할 상수
  ROLES: {
    GUEST: 'GUEST',
    USER: 'USER',
    MANAGER: 'MANAGER',
    ADMIN: 'ADMIN'
  },
  
  // 로그인 상태 확인
  isLoggedIn: function() {
    return localStorage.getItem('isLoggedIn') === 'true' || 
           sessionStorage.getItem('isLoggedIn') === 'true';
  },
  
  // 현재 사용자 역할 가져오기
  getCurrentRole: function() {
    if (!this.isLoggedIn()) {
      return this.ROLES.GUEST;
    }
    
    // localStorage 또는 sessionStorage에서 역할 확인
    const role = localStorage.getItem('userRole') || 
                sessionStorage.getItem('userRole') || 
                this.ROLES.USER;
    
    return role;
  },
    // 관리자 여부 확인
  isAdmin: function() {
    return this.getCurrentRole() === this.ROLES.ADMIN;
  },
  
  // 매니저 여부 확인
  isManager: function() {
    return this.getCurrentRole() === this.ROLES.MANAGER;
  },
  
  // 매니저 또는 관리자 여부 확인
  isManagerOrAdmin: function() {
    const role = this.getCurrentRole();
    return role === this.ROLES.MANAGER || role === this.ROLES.ADMIN;
  },
  
  // 로그인 사용자 여부 확인
  isUser: function() {
    const role = this.getCurrentRole();
    return role === this.ROLES.USER || role === this.ROLES.MANAGER || role === this.ROLES.ADMIN;
  },
  
  // 일반 사용자 여부 확인 (관리자, 매니저 제외)
  isRegularUser: function() {
    return this.getCurrentRole() === this.ROLES.USER;
  },
  
  // 게스트(비로그인) 여부 확인
  isGuest: function() {
    return this.getCurrentRole() === this.ROLES.GUEST;
  },  // 특정 요소를 권한에 따라 표시/숨김 처리
  applyRoleVisibility: function() {
    // 권한별 섹션 처리 - 모든 섹션 비활성화 후 현재 권한에 맞는 섹션만 활성화
    document.querySelectorAll('.auth-section').forEach(section => {
      section.classList.remove('active');
      section.style.display = 'none';
    });
    
    // 현재 권한에 맞는 섹션 활성화
    if (this.isAdmin()) {
      const adminSection = document.querySelector('.admin-section');
      if (adminSection) {
        adminSection.classList.add('active');
        adminSection.style.display = '';
      }
    } else if (this.isManager()) {
      const managerSection = document.querySelector('.manager-section');
      if (managerSection) {
        managerSection.classList.add('active');
        managerSection.style.display = '';
      }
    } else if (this.isRegularUser()) {
      const userSection = document.querySelector('.user-section');
      if (userSection) {
        userSection.classList.add('active');
        userSection.style.display = '';
      }
    } else {
      // 게스트인 경우
      const guestSection = document.querySelector('.guest-section');
      if (guestSection) {
        guestSection.classList.add('active');
        guestSection.style.display = '';
      }
    }
    
    // 개별 요소 처리 (기존 방식 유지 - 하위 호환성)
    // 관리자 전용 요소
    document.querySelectorAll('.admin-only').forEach(element => {
      element.style.display = this.isAdmin() ? '' : 'none';
    });
    
    // 매니저 전용 요소
    document.querySelectorAll('.manager-only').forEach(element => {
      element.style.display = this.isManager() ? '' : 'none';
    });
    
    // 매니저 또는 관리자 요소 (권한 관리자)
    document.querySelectorAll('.manager-admin-only').forEach(element => {
      element.style.display = this.isManagerOrAdmin() ? '' : 'none';
    });
    
    // 로그인 사용자 전용 요소 (일반 사용자, 매니저, 관리자 포함)
    document.querySelectorAll('.user-only').forEach(element => {
      element.style.display = this.isUser() ? '' : 'none';
    });
    
    // 일반 사용자 전용 요소 (관리자, 매니저 제외)
    document.querySelectorAll('.user-only-not-admin').forEach(element => {
      element.style.display = this.isRegularUser() ? '' : 'none';
    });
    
    // 비로그인 사용자 전용 요소
    document.querySelectorAll('.guest-only').forEach(element => {
      element.style.display = this.isGuest() ? '' : 'none';
    });
  },
  
  // 역할 설정 (로그인 처리 시 사용)
  setRole: function(role) {
    const storage = document.getElementById('remember-me')?.checked 
      ? localStorage 
      : sessionStorage;
    
    storage.setItem('userRole', role);
    storage.setItem('isLoggedIn', 'true');
  },
  
  // 로그아웃 처리
  logout: function() {
    // 로컬 스토리지와 세션 스토리지 모두에서 로그인 정보 제거
    localStorage.removeItem('isLoggedIn');
    localStorage.removeItem('userName');
    localStorage.removeItem('userRole');
    
    sessionStorage.removeItem('isLoggedIn');
    sessionStorage.removeItem('userName');
    sessionStorage.removeItem('userRole');
    
    // 홈페이지로 리디렉션
    window.location.href = '../pages/index.html';
  }
};

// 페이지 로드 시 권한에 따른 UI 업데이트 적용
document.addEventListener('DOMContentLoaded', function() {
  Auth.applyRoleVisibility();
  
  // 로그아웃 버튼 이벤트 리스너 등록
  const logoutBtn = document.getElementById('logout-btn');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', function() {
      Auth.logout();
    });
  }
});

// 기존 코드와의 호환성 유지
// 1. AuthRoles 객체 호환성
window.AuthRoles = Auth;

// 2. window.kirini.auth 호환성
window.kirini = window.kirini || {};
window.kirini.auth = {
  isLoggedIn: Auth.isLoggedIn()
};

// 개발 편의를 위한 전역 노출
window.Auth = Auth;
