/**
 * 인증 및 권한 관리 모듈
 */
const Auth = {
  // 사용자 역할 정의
  ROLES: {
    GUEST: 'GUEST',
    USER: 'USER',
    MANAGER: 'MANAGER',
    ADMIN: 'ADMIN'
  },
  
  /**
   * 로그인 상태 확인
   * @returns {boolean} 로그인 여부
   */
  isLoggedIn: function() {
    return ApiClient.getAuthToken() !== null;
  },
  
  /**
   * 인증 토큰 가져오기
   * @returns {string|null} 인증 토큰
   */
  getToken: function() {
    return ApiClient.getAuthToken();
  },
  
  /**
   * 현재 사용자 정보 가져오기
   * @returns {Promise<Object>} 사용자 정보 객체
   */
  async getCurrentUser() {
    if (!this.isLoggedIn()) {
      return null;
    }
    
    try {
      // 세션 스토리지에 사용자 정보가 있으면 사용
      const cachedUserInfo = sessionStorage.getItem('currentUser');
      if (cachedUserInfo) {
        return JSON.parse(cachedUserInfo);
      }
      
      // API에서 사용자 정보 가져오기
      const userInfo = await UserService.getProfile();
      
      // 세션 스토리지에 정보 캐싱
      sessionStorage.setItem('currentUser', JSON.stringify(userInfo));
      
      return userInfo;
    } catch (error) {
      console.error('사용자 정보 로드 실패:', error);
      return null;
    }
  },
  
  /**
   * 현재 사용자 역할 확인
   * @returns {string} 사용자 역할
   */
  getCurrentRole: async function() {
    if (!this.isLoggedIn()) {
      return this.ROLES.GUEST;
    }
    
    // 세션 스토리지에서 역할 확인
    const cachedRole = sessionStorage.getItem('userRole');
    if (cachedRole) {
      return cachedRole;
    }
    
    try {
      // 사용자 정보에서 역할 가져오기
      const userInfo = await this.getCurrentUser();
      const role = userInfo?.role || this.ROLES.USER;
      
      // 세션 스토리지에 역할 저장
      sessionStorage.setItem('userRole', role);
      
      return role;
    } catch (error) {
      console.error('역할 정보 로드 실패:', error);
      return this.ROLES.USER;
    }
  },
  
  /**
   * 로그인 처리
   * @param {string} username 사용자명
   * @param {string} password 비밀번호
   * @param {boolean} rememberMe 자동 로그인 여부
   * @returns {Promise<Object>} 로그인 결과
   */
  async login(username, password, rememberMe = false) {
    try {
      const result = await UserService.login(username, password);
      
      // 세션 스토리지에 사용자 정보 저장
      if (result.user) {
        sessionStorage.setItem('currentUser', JSON.stringify(result.user));
        sessionStorage.setItem('userRole', result.user.role || this.ROLES.USER);
      }
      
      // 로그인 성공 이벤트 발생
      this.dispatchAuthEvent('login');
      
      return result;
    } catch (error) {
      console.error('로그인 실패:', error);
      throw error;
    }
  },
  
  /**
   * 로그아웃 처리
   * @returns {Promise<void>}
   */
  async logout() {
    try {
      // API 로그아웃 요청
      await UserService.logout();
    } catch (error) {
      console.warn('로그아웃 요청 실패:', error);
    } finally {
      // 로컬 스토리지의 인증 정보 삭제
      ApiClient.removeAuthToken();
      
      // 세션 스토리지의 사용자 정보 삭제
      sessionStorage.removeItem('currentUser');
      sessionStorage.removeItem('userRole');
      
      // 로그아웃 이벤트 발생
      this.dispatchAuthEvent('logout');
    }
  },
  
  /**
   * 회원가입 처리
   * @param {Object} userData 사용자 등록 데이터
   * @returns {Promise<Object>} 등록 결과
   */
  async register(userData) {
    try {
      return await UserService.register(userData);
    } catch (error) {
      console.error('회원가입 실패:', error);
      throw error;
    }
  },
  
  // 권한 확인 메소드
  async isGuest() { 
    const role = await this.getCurrentRole(); 
    return role === this.ROLES.GUEST; 
  },
  
  async isUser() { 
    return this.isLoggedIn(); // 로그인한 모든 사용자
  },
  
  async isRegularUser() { 
    const role = await this.getCurrentRole(); 
    return role === this.ROLES.USER; // 일반 사용자(매니저, 관리자 제외)
  },
  
  async isManager() { 
    const role = await this.getCurrentRole(); 
    return role === this.ROLES.MANAGER; 
  },
  
  async isAdmin() { 
    const role = await this.getCurrentRole(); 
    return role === this.ROLES.ADMIN; 
  },
  
  async isManagerOrAdmin() { 
    const role = await this.getCurrentRole();
    return role === this.ROLES.MANAGER || role === this.ROLES.ADMIN; 
  },
  
  /**
   * UI 요소 권한 관리
   * @param {Function} callback - 권한 변동 시 호출할 콜백 함수
   */
  async applyRoleVisibility(callback) {
    const role = await this.getCurrentRole();
    console.log('권한 적용:', role);
    
    // 모든 권한 섹션 비활성화 (클래스 제거)
    document.querySelectorAll('.auth-section').forEach(section => {
      section.classList.remove('active');
    });

    // 모든 역할별 개별 요소 기본적으로 숨김
    const allRoleSpecificElements = document.querySelectorAll(
      '.guest-only, .logged-in-only, .user-only, .user-only-not-admin, .admin-only, .manager-only, .manager-admin-only'
    );
    allRoleSpecificElements.forEach(el => {
      el.classList.add('element-hidden');
    });
    
    // 현재 사용자 역할에 따라 UI 요소 표시
    if (role === this.ROLES.GUEST) {
      // 비로그인 사용자
      document.querySelectorAll('.auth-section-guest').forEach(section => {
        section.classList.add('active');
      });
      document.querySelectorAll('.guest-only').forEach(el => {
        el.classList.remove('element-hidden');
      });
    } else {
      // 로그인한 사용자
      document.querySelectorAll('.auth-section-user').forEach(section => {
        section.classList.add('active');
      });
      document.querySelectorAll('.logged-in-only').forEach(el => {
        el.classList.remove('element-hidden');
      });
      
      // 역할별 추가 권한
      if (role === this.ROLES.USER) {
        document.querySelectorAll('.user-only, .user-only-not-admin').forEach(el => {
          el.classList.remove('element-hidden');
        });
      } else if (role === this.ROLES.MANAGER) {
        document.querySelectorAll('.manager-only, .manager-admin-only').forEach(el => {
          el.classList.remove('element-hidden');
        });
      } else if (role === this.ROLES.ADMIN) {
        document.querySelectorAll('.admin-only, .manager-admin-only').forEach(el => {
          el.classList.remove('element-hidden');
        });
      }
    }
    
    // 콜백 함수 호출 (UI 업데이트 등)
    if (typeof callback === 'function') {
      callback(role);
    }
    
    return role;
  },
  
  /**
   * 인증 상태 이벤트 발생
   * @param {string} eventType 이벤트 타입 ('login' | 'logout')
   */
  dispatchAuthEvent(eventType) {
    const event = new CustomEvent('auth:' + eventType, {
      detail: { timestamp: new Date().getTime() },
      bubbles: true
    });
    document.dispatchEvent(event);
  },
  
  /**
   * 보호된 페이지 확인 및 리다이렉션
   * @param {Object} options - 옵션 객체
   * @param {string} options.loginUrl - 로그인 페이지 URL
   * @param {string} options.errorUrl - 권한 오류 페이지 URL
   * @param {Array} options.allowedRoles - 허용된 역할 배열
   * @returns {Promise<boolean>} 접근 가능 여부
   */
  async checkProtectedPage(options = {}) {
    const defaultOptions = {
      loginUrl: '/view/login.html',
      errorUrl: '/view/error.html?code=403',
      allowedRoles: null // null이면 모든 로그인 사용자 허용
    };
    
    const opts = { ...defaultOptions, ...options };
    
    // 로그인 여부 확인
    if (!this.isLoggedIn()) {
      // 현재 URL 저장 (로그인 후 리다이렉션 용)
      const returnUrl = encodeURIComponent(window.location.href);
      window.location.href = `${opts.loginUrl}?returnUrl=${returnUrl}`;
      return false;
    }
    
    // 특정 역할만 허용하는 경우
    if (opts.allowedRoles && Array.isArray(opts.allowedRoles)) {
      const currentRole = await this.getCurrentRole();
      
      if (!opts.allowedRoles.includes(currentRole)) {
        window.location.href = opts.errorUrl;
        return false;
      }
    }
    
    return true;
  },
  
  /**
   * 인증 초기화
   */
  init: function() {
    // 로그인 및 로그아웃 이벤트 리스너 설정
    document.addEventListener('auth:login', () => {
      this.applyRoleVisibility();
    });
    
    document.addEventListener('auth:logout', () => {
      this.applyRoleVisibility();
    });
    
    // 페이지 로드 시 권한 확인 및 UI 업데이트
    window.addEventListener('DOMContentLoaded', () => {
      this.applyRoleVisibility();
    });
  }
};

// 인증 모듈 초기화 및 전역 노출
Auth.init();
window.Auth = Auth;
