document.addEventListener('DOMContentLoaded', function() {
  // 이미 로그인한 경우 메인 페이지로 리다이렉션
  if (Auth.isLoggedIn()) {
    window.location.href = '/';
    return;
  }
  
  // 로그인 폼 이벤트 리스너 설정
  const loginForm = document.getElementById('login-form');
  if (loginForm) {
    loginForm.addEventListener('submit', handleLogin);
  }
  
  // 비밀번호 재설정 모달 이벤트 리스너 설정
  setupPasswordResetModal();
  
  // 소셜 로그인 설정 추가
  setupSocialLogin();
});

/**
 * 로그인 폼 제출 처리
 * @param {Event} e 이벤트 객체
 */
async function handleLogin(e) {
  e.preventDefault();
  
  // 입력 필드
  const usernameInput = document.getElementById('username');
  const passwordInput = document.getElementById('password');
  const rememberMeCheckbox = document.getElementById('remember-me');
  const submitButton = document.querySelector('#login-form button[type="submit"]');
  const errorMessageElement = document.getElementById('login-error-message');
  
  // 입력값 가져오기
  const username = usernameInput.value.trim();
  const password = passwordInput.value;
  const rememberMe = rememberMeCheckbox?.checked || false;
  
  // 입력 검증
  if (!username) {
    showError(errorMessageElement, '사용자명을 입력해주세요.');
    usernameInput.focus();
    return;
  }
  
  if (!password) {
    showError(errorMessageElement, '비밀번호를 입력해주세요.');
    passwordInput.focus();
    return;
  }
  
  // 로딩 상태 표시
  submitButton.disabled = true;
  submitButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 로그인 중...';
  
  try {
    // 로그인 요청
    const result = await Auth.login(username, password, rememberMe);
    
    // 로그인 성공
    hideError(errorMessageElement);
    
    // 리다이렉션
    const urlParams = new URLSearchParams(window.location.search);
    const returnUrl = urlParams.get('returnUrl') || '/';
    
    window.location.href = decodeURIComponent(returnUrl);
  } catch (error) {
    console.error('로그인 실패:', error);
    
    // 오류 메시지 표시
    let errorMessage = '로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요.';
    
    if (error.message === 'Invalid credentials') {
      errorMessage = '아이디 또는 비밀번호가 일치하지 않습니다.';
    } else if (error.message === 'Account disabled') {
      errorMessage = '비활성화된 계정입니다. 관리자에게 문의하세요.';
    }
    
    showError(errorMessageElement, errorMessage);
    
    // 비밀번호 필드 초기화
    passwordInput.value = '';
    passwordInput.focus();
  } finally {
    // 버튼 상태 복원
    submitButton.disabled = false;
    submitButton.innerHTML = '로그인';
  }
}

/**
 * 비밀번호 재설정 모달 설정
 */
function setupPasswordResetModal() {
  const resetForm = document.getElementById('password-reset-form');
  const resetEmailInput = document.getElementById('reset-email');
  const resetButton = document.querySelector('#password-reset-form button[type="submit"]');
  const resetErrorMessage = document.getElementById('reset-error-message');
  const resetSuccessMessage = document.getElementById('reset-success-message');
  
  if (resetForm) {
    resetForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      
      // 입력값 가져오기
      const email = resetEmailInput.value.trim();
      
      // 입력 검증
      if (!email) {
        showError(resetErrorMessage, '이메일을 입력해주세요.');
        resetEmailInput.focus();
        return;
      }
      
      if (!isValidEmail(email)) {
        showError(resetErrorMessage, '유효한 이메일 주소를 입력해주세요.');
        resetEmailInput.focus();
        return;
      }
      
      // 로딩 상태 표시
      resetButton.disabled = true;
      resetButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 처리 중...';
      
      try {
        // 비밀번호 재설정 요청
        await UserService.requestPasswordReset(email);
        
        // 성공 메시지 표시
        hideError(resetErrorMessage);
        showSuccess(resetSuccessMessage, '비밀번호 재설정 링크가 이메일로 전송되었습니다.');
        
        // 폼 초기화
        resetEmailInput.value = '';
      } catch (error) {
        console.error('비밀번호 재설정 요청 실패:', error);
        
        // 오류 메시지 표시
        hideSuccess(resetSuccessMessage);
        showError(resetErrorMessage, '비밀번호 재설정 요청 중 오류가 발생했습니다.');
      } finally {
        // 버튼 상태 복원
        resetButton.disabled = false;
        resetButton.innerHTML = '비밀번호 재설정 링크 받기';
      }
    });
  }
  
  // 모달이 닫힐 때 폼 초기화
  const resetModal = document.getElementById('password-reset-modal');
  if (resetModal) {
    resetModal.addEventListener('hidden.bs.modal', () => {
      if (resetForm) resetForm.reset();
      if (resetErrorMessage) hideError(resetErrorMessage);
      if (resetSuccessMessage) hideSuccess(resetSuccessMessage);
    });
  }
}

/**
 * 이메일 주소 유효성 검사
 * @param {string} email 이메일 주소
 * @returns {boolean} 유효 여부
 */
function isValidEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

/**
 * 오류 메시지 표시
 * @param {HTMLElement} element 메시지 엘리먼트
 * @param {string} message 오류 메시지
 */
function showError(element, message) {
  if (element) {
    element.textContent = message;
    element.style.display = 'block';
  }
}

/**
 * 오류 메시지 숨김
 * @param {HTMLElement} element 메시지 엘리먼트
 */
function hideError(element) {
  if (element) {
    element.textContent = '';
    element.style.display = 'none';
  }
}

/**
 * 성공 메시지 표시
 * @param {HTMLElement} element 메시지 엘리먼트
 * @param {string} message 성공 메시지
 */
function showSuccess(element, message) {
  if (element) {
    element.textContent = message;
    element.style.display = 'block';
  }
}

/**
 * 성공 메시지 숨김
 * @param {HTMLElement} element 메시지 엘리먼트
 */
function hideSuccess(element) {
  if (element) {
    element.textContent = '';
    element.style.display = 'none';
  }
}

/**
 * 소셜 로그인 설정
 */
function setupSocialLogin() {
  const socialButtons = document.querySelectorAll('.social-login-btn');
  
  socialButtons.forEach(button => {
    button.addEventListener('click', function(e) {
      e.preventDefault();
      
      const provider = this.dataset.provider;
      const errorMessageElement = document.getElementById('login-error-message');
      
      if (!provider) return;
      
      // 로그인 처리 중 상태 표시
      this.disabled = true;
      const originalText = this.innerHTML;
      this.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 처리 중...';
      
      // 리다이렉션 URL (현재 URL에서 returnUrl 파라미터 유지)
      const urlParams = new URLSearchParams(window.location.search);
      const returnUrl = urlParams.get('returnUrl') || '/';
      
      // 소셜 로그인 팝업 설정
      const width = 600;
      const height = 600;
      const left = window.screenX + (window.outerWidth - width) / 2;
      const top = window.screenY + (window.outerHeight - height) / 2;
      
      // OAuth 요청 상태 생성 (CSRF 방지)
      const state = Math.random().toString(36).substring(2);
      sessionStorage.setItem('oauth_state', state);
      sessionStorage.setItem('oauth_return_url', returnUrl);
      
      // 팝업 URL 생성
      let authUrl;
      switch (provider) {
        case 'google':
          authUrl = `/auth/google?state=${state}`;
          break;
        case 'naver':
          authUrl = `/auth/naver?state=${state}`;
          break;
        case 'kakao':
          authUrl = `/auth/kakao?state=${state}`;
          break;
        default:
          showError(errorMessageElement, '지원하지 않는 로그인 방식입니다.');
          this.disabled = false;
          this.innerHTML = originalText;
          return;
      }
      
      // 팝업 열기
      const popup = window.open(
        authUrl,
        `${provider}Auth`,
        `width=${width},height=${height},left=${left},top=${top},resizable=yes,scrollbars=yes`
      );
      
      // 팝업 닫힘 감지
      const checkPopupClosed = setInterval(() => {
        if (!popup || popup.closed) {
          clearInterval(checkPopupClosed);
          
          // 버튼 상태 복원
          this.disabled = false;
          this.innerHTML = originalText;
          
          // 로그인 처리 확인
          const authResult = sessionStorage.getItem('oauth_result');
          if (authResult === 'success') {
            // 로그인 성공 처리
            sessionStorage.removeItem('oauth_result');
            window.location.href = decodeURIComponent(returnUrl);
          }
        }
      }, 500);
    });
  });
}

// 소셜 로그인 콜백 처리 (메인 윈도우에서 호출)
window.handleSocialLoginSuccess = function(userData) {
  if (!userData) return false;
  
  try {
    // 인증 정보 저장
    ApiClient.setAuthToken(userData.accessToken);
    if (userData.refreshToken) {
      ApiClient.setRefreshToken(userData.refreshToken);
    }
    
    // 사용자 정보 세션 저장
    sessionStorage.setItem('currentUser', JSON.stringify(userData.user));
    sessionStorage.setItem('userRole', userData.user.role || 'USER');
    sessionStorage.setItem('oauth_result', 'success');
    
    // 인증 이벤트 발생
    document.dispatchEvent(new CustomEvent('auth:login', {
      detail: { timestamp: new Date().getTime() },
      bubbles: true
    }));
    
    return true;
  } catch (error) {
    console.error('소셜 로그인 처리 오류:', error);
    return false;
  }
};
