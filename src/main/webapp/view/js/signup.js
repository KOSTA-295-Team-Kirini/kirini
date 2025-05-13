// 회원가입 페이지 스크립트
document.addEventListener('DOMContentLoaded', function() {
  const signupForm = document.getElementById('signup-form');
  const emailInput = document.getElementById('email');
  const nicknameInput = document.getElementById('nickname');
  const passwordInput = document.getElementById('password');
  const passwordConfirmInput = document.getElementById('password-confirm');
  const emailCheckBtn = document.getElementById('email-check');
  const nicknameCheckBtn = document.getElementById('nickname-check');
  const signupButton = document.getElementById('signup-button');
  const agreeCheckbox = document.getElementById('agree');

  const emailError = document.getElementById('email-error');
  const nicknameError = document.getElementById('nickname-error');
  const passwordError = document.getElementById('password-error');
  const passwordConfirmError = document.getElementById('password-confirm-error');

  // 이메일 유효성 검사
  emailInput.addEventListener('input', function() {
    const isValid = validateEmail(this.value);
    if (isValid) {
      emailError.style.display = 'none';
      // 파란색으로 변하는 과정 제거 - 항상 같은 색상 유지
    } else {
      emailError.style.display = 'block';
    }
    checkFormValidity();
  });

  // 이메일 중복 확인
  emailCheckBtn.addEventListener('click', function() {
    const email = emailInput.value;
    if (validateEmail(email)) {
      // 가상의 서버 응답을 시뮬레이션
      setTimeout(() => {
        this.textContent = '확인완료';
        this.style.backgroundColor = '#4CAF50';
        this.disabled = true; // 버튼 비활성화
        checkFormValidity();
      }, 1000);
    }
  });

  // 닉네임 유효성 검사
  nicknameInput.addEventListener('input', function() {
    const isValid = this.value.length >= 2 && this.value.length <= 10;
    if (isValid) {
      nicknameError.style.display = 'none';
      // 파란색으로 변하는 과정 제거 - 항상 같은 색상 유지
    } else {
      nicknameError.style.display = 'block';
    }
    checkFormValidity();
  });

  // 닉네임 중복 확인
  nicknameCheckBtn.addEventListener('click', function() {
    const nickname = nicknameInput.value;
    if (nickname.length >= 2 && nickname.length <= 10) {
      // 가상의 서버 응답을 시뮬레이션
      setTimeout(() => {
        this.textContent = '확인완료';
        this.style.backgroundColor = '#4CAF50';
        this.disabled = true; // 버튼 비활성화
        checkFormValidity();
      }, 1000);
    }
  });

  // 비밀번호 유효성 검사
  passwordInput.addEventListener('input', function() {
    const isValid = validatePassword(this.value);
    if (isValid) {
      passwordError.style.display = 'none';
    } else {
      passwordError.style.display = 'block';
    }
    // 비밀번호 확인 일치 여부도 체크
    const isMatch = this.value === passwordConfirmInput.value;
    if (isMatch) {
      passwordConfirmError.style.display = 'none';
    } else {
      passwordConfirmError.style.display = 'block';
    }
    checkFormValidity();
  });

  // 비밀번호 확인 일치 검사
  passwordConfirmInput.addEventListener('input', function() {
    const isMatch = this.value === passwordInput.value;
    if (isMatch) {
      passwordConfirmError.style.display = 'none';
    } else {
      passwordConfirmError.style.display = 'block';
    }
    checkFormValidity();
  });

  // 약관 동의 체크박스
  agreeCheckbox.addEventListener('change', checkFormValidity);

  // 폼 제출 이벤트
  signupForm.addEventListener('submit', function(e) {
    e.preventDefault();
    if (signupButton.disabled) return;
    
    alert('회원가입이 완료되었습니다!');
    // 실제 서비스에서는 서버로 데이터를 전송하고 회원가입 처리를 진행합니다.
    window.location.href = 'login.html';
  });

  // 이메일 유효성 검사 함수
  function validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
  }

  // 비밀번호 유효성 검사 함수 (영문, 숫자, 특수문자 조합 8자 이상)
  function validatePassword(password) {
    const re = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*])[A-Za-z\d!@#$%^&*]{8,}$/;
    return re.test(password);
  }

  // 폼 유효성 검증 함수
  function checkFormValidity() {
    const isEmailValid = validateEmail(emailInput.value) && emailCheckBtn.textContent === '확인완료';
    const isNicknameValid = nicknameInput.value.length >= 2 && nicknameInput.value.length <= 10 && nicknameCheckBtn.textContent === '확인완료';
    const isPasswordValid = validatePassword(passwordInput.value);
    const isPasswordMatch = passwordInput.value === passwordConfirmInput.value;
    const isAgreed = agreeCheckbox.checked;
    
    signupButton.disabled = !(isEmailValid && isNicknameValid && isPasswordValid && isPasswordMatch && isAgreed);
  }
});
