// 회원가입 페이지 스크립트
document.addEventListener('DOMContentLoaded', function() {
  // 이미 로그인한 경우 메인 페이지로 리다이렉션
  if (Auth.isLoggedIn()) {
    window.location.href = '/';
    return;
  }
  
  // 회원가입 폼 이벤트 리스너 설정
  const signupForm = document.getElementById('signup-form');
  if (signupForm) {
    setupFormValidation(signupForm);
    signupForm.addEventListener('submit', handleSignup);
  }
});

/**
 * 폼 유효성 검증 설정
 * @param {HTMLFormElement} form 폼 엘리먼트
 */
function setupFormValidation(form) {
  const usernameInput = form.querySelector('#username');
  const emailInput = form.querySelector('#email');
  const passwordInput = form.querySelector('#password');
  const confirmPasswordInput = form.querySelector('#confirm-password');
  const usernameError = form.querySelector('#username-error');
  const emailError = form.querySelector('#email-error');
  const passwordError = form.querySelector('#password-error');
  const confirmPasswordError = form.querySelector('#confirm-password-error');
  const passwordStrengthMeter = form.querySelector('.password-strength-meter');
  const passwordStrengthText = form.querySelector('.password-strength-text');
  
  // 사용자명 유효성 검사
  if (usernameInput && usernameError) {
    usernameInput.addEventListener('blur', async () => {
      const username = usernameInput.value.trim();
      
      if (!username) {
        showError(usernameError, '사용자명을 입력해주세요.');
        return;
      }
      
      if (username.length < 4) {
        showError(usernameError, '사용자명은 최소 4자 이상이어야 합니다.');
        return;
      }
      
      if (!/^[a-zA-Z0-9_]+$/.test(username)) {
        showError(usernameError, '사용자명은 영문자, 숫자, 밑줄(_)만 포함할 수 있습니다.');
        return;
      }
      
      try {
        // 사용자명 중복 확인 (API 요청)
        const response = await fetch(`/api/users/check-username.do?username=${encodeURIComponent(username)}`);
        const result = await response.json();
        
        if (!result.available) {
          showError(usernameError, '이미 사용 중인 사용자명입니다.');
          return;
        }
        
        hideError(usernameError);
      } catch (error) {
        console.error('사용자명 확인 실패:', error);
      }
    });
  }
  
  // 이메일 유효성 검사
  if (emailInput && emailError) {
    emailInput.addEventListener('blur', async () => {
      const email = emailInput.value.trim();
      
      if (!email) {
        showError(emailError, '이메일을 입력해주세요.');
        return;
      }
      
      if (!isValidEmail(email)) {
        showError(emailError, '유효한 이메일 주소를 입력해주세요.');
        return;
      }
      
      try {
        // 이메일 중복 확인 (API 요청)
        const response = await fetch(`/api/users/check-email.do?email=${encodeURIComponent(email)}`);
        const result = await response.json();
        
        if (!result.available) {
          showError(emailError, '이미 사용 중인 이메일입니다.');
          return;
        }
        
        hideError(emailError);
      } catch (error) {
        console.error('이메일 확인 실패:', error);
      }
    });
  }
  
  // 비밀번호 강도 체크
  if (passwordInput && passwordStrengthMeter && passwordStrengthText) {
    passwordInput.addEventListener('input', () => {
      const password = passwordInput.value;
      const strength = checkPasswordStrength(password);
      
      // 강도 미터 업데이트
      passwordStrengthMeter.className = 'password-strength-meter';
      passwordStrengthMeter.classList.add(`strength-${strength.level}`);
      
      // 강도 텍스트 업데이트
      passwordStrengthText.textContent = strength.message;
      passwordStrengthText.className = 'password-strength-text';
      passwordStrengthText.classList.add(`text-${strength.level}`);
      
      // 비밀번호 유효성 검사
      if (password && password.length < 8) {
        showError(passwordError, '비밀번호는 최소 8자 이상이어야 합니다.');
      } else if (strength.level === 'weak') {
        showError(passwordError, '더 강력한 비밀번호를 사용하세요.');
      } else {
        hideError(passwordError);
      }
    });
  }
  
  // 비밀번호 확인 검사
  if (confirmPasswordInput && confirmPasswordError) {
    confirmPasswordInput.addEventListener('input', () => {
      const password = passwordInput.value;
      const confirmPassword = confirmPasswordInput.value;
      
      if (password !== confirmPassword) {
        showError(confirmPasswordError, '비밀번호가 일치하지 않습니다.');
      } else {
        hideError(confirmPasswordError);
      }
    });
  }
}

/**
 * 회원가입 폼 제출 처리
 * @param {Event} e 이벤트 객체
 */
async function handleSignup(e) {
  e.preventDefault();
  
  // 폼 데이터 가져오기
  const form = e.target;
  const username = form.username.value.trim();
  const email = form.email.value.trim();
  const password = form.password.value;
  const confirmPassword = form.confirmPassword.value;
  const nickname = form.nickname?.value.trim() || username;
  const agreeTerms = form.agreeTerms?.checked || false;
  
  // 제출 버튼 및 오류 메시지 요소
  const submitButton = form.querySelector('button[type="submit"]');
  const formErrorMessage = document.getElementById('form-error-message');
  
  // 폼 유효성 검사
  if (!username || !email || !password || !confirmPassword) {
    showError(formErrorMessage, '모든 필수 항목을 입력해주세요.');
    return;
  }
  
  if (!agreeTerms) {
    showError(formErrorMessage, '서비스 약관에 동의해주세요.');
    return;
  }
  
  if (password !== confirmPassword) {
    showError(formErrorMessage, '비밀번호가 일치하지 않습니다.');
    return;
  }
  
  // 비밀번호 강도 검사
  const strength = checkPasswordStrength(password);
  if (strength.level === 'weak') {
    showError(formErrorMessage, '비밀번호가 너무 약합니다. 더 강력한 비밀번호를 사용하세요.');
    return;
  }
  
  // 로딩 상태 표시
  submitButton.disabled = true;
  submitButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 처리 중...';
    // 성공 메시지 컨테이너
  const successContainer = document.querySelector('.signup-success');
  const formContainer = document.querySelector('.signup-form-container');
  
  try {
    // 회원가입 요청 데이터
    const userData = {
      username,
      email,
      password,
      confirmPassword, // 백엔드 검증용
      nickname,
      agreeTerms // 약관 동의 여부
    };
    
    // CSRF 토큰 추가 (보안 강화)
    const csrfToken = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content');
    if (csrfToken) {
      userData.csrfToken = csrfToken;
    }
    
    // 회원가입 API 요청
    const result = await Auth.register(userData);
    
    // 회원가입 성공
    hideError(formErrorMessage);
    
    // 폼 숨기고 성공 메시지 표시
    if (formContainer && successContainer) {
      formContainer.style.display = 'none';
      successContainer.style.display = 'block';
      
      // 성공 메시지 설정
      const messageElement = successContainer.querySelector('.success-message');
      if (messageElement) {
        messageElement.innerHTML = `
          <h3>회원가입이 완료되었습니다!</h3>
          <p>환영합니다, <strong>${nickname || username}</strong>님!</p>
          <p>가입하신 이메일 <strong>${email}</strong>로 인증 메일이 발송되었습니다.</p>
          <p>이메일 인증 후 모든 서비스를 이용하실 수 있습니다.</p>
        `;
      }
      
      // 자동 로그인 전환
      if (result.autoLogin) {
        const redirectTimer = successContainer.querySelector('.redirect-timer');
        if (redirectTimer) {
          let timeLeft = 5;
          redirectTimer.textContent = timeLeft;
          
          // 카운트다운 후 로그인 페이지로 이동
          const interval = setInterval(() => {
            timeLeft--;
            if (redirectTimer) redirectTimer.textContent = timeLeft;
            
            if (timeLeft <= 0) {
              clearInterval(interval);
              window.location.href = '/view/login.html';
            }
          }, 1000);
        } else {
          // 타이머 요소 없으면 5초 후 이동
          setTimeout(() => {
            window.location.href = '/view/login.html';
          }, 5000);
        }
      }
    } else {
      // 성공 컨테이너가 없는 경우 기존 방식으로 처리
      alert('회원가입이 완료되었습니다. 로그인 페이지로 이동합니다.');
      window.location.href = '/view/login.html';
    }
  } catch (error) {
    console.error('회원가입 실패:', error);
    
    // 오류 메시지 표시
    let errorMessage = '회원가입 중 문제가 발생했습니다. 다시 시도해주세요.';
    
    // 에러 메시지 세분화
    if (error.message === 'Username already exists') {
      errorMessage = '이미 사용 중인 사용자명입니다.';
    } else if (error.message === 'Email already exists') {
      errorMessage = '이미 사용 중인 이메일입니다.';
    } else if (error.message === 'Invalid password format') {
      errorMessage = '유효하지 않은 비밀번호 형식입니다. 영문, 숫자, 특수문자를 포함하여 8자 이상으로 설정해주세요.';
    } else if (error.message === 'Invalid email format') {
      errorMessage = '유효하지 않은 이메일 형식입니다.';
    } else if (error.message.includes('network') || error.message.includes('timeout')) {
      errorMessage = '네트워크 오류가 발생했습니다. 인터넷 연결을 확인하고 다시 시도해주세요.';
    }
    
    showError(formErrorMessage, errorMessage);
  } finally {
    // 버튼 상태 복원
    submitButton.disabled = false;
    submitButton.innerHTML = '회원가입';
  }
}

/**
 * 비밀번호 강도 확인
 * @param {string} password 비밀번호
 * @returns {Object} 강도 정보
 */
function checkPasswordStrength(password) {
  if (!password) {
    return { level: 'empty', message: '비밀번호를 입력하세요' };
  }
  
  const length = password.length;
  const hasLowerCase = /[a-z]/.test(password);
  const hasUpperCase = /[A-Z]/.test(password);
  const hasNumbers = /\d/.test(password);
  const hasSpecialChars = /[!@#$%^&*(),.?":{}|<>]/.test(password);
  
  // 점수 계산
  let score = 0;
  if (length >= 8) score += 1;
  if (length >= 12) score += 1;
  if (hasLowerCase) score += 1;
  if (hasUpperCase) score += 1;
  if (hasNumbers) score += 1;
  if (hasSpecialChars) score += 2;
  
  // 강도 수준 결정
  if (score >= 6) {
    return { level: 'strong', message: '강력함' };
  } else if (score >= 4) {
    return { level: 'medium', message: '보통' };
  } else {
    return { level: 'weak', message: '약함' };
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