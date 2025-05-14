// 회원가입 페이지 스크립트
document.addEventListener('DOMContentLoaded', function() {
  // 회원가입 페이지에서는 항상 비로그인 상태(GUEST)로 가정하고 UI를 설정
  if (typeof Auth !== 'undefined') {
    // 명시적으로 로컬 스토리지 상태를 체크하여 권한 설정 강제 적용
    // 이미 로그인된 상태라면 홈으로 리다이렉트
    if (Auth.isLoggedIn()) {
      // 이미 로그인된 사용자라면 홈페이지로 리다이렉션
      window.location.href = '../pages/index.html';
      return;
    } else {
      // 강제로 Auth 모듈에 GUEST 권한 적용 (CSS 표시용)
      Auth.applyRoleVisibility();
      console.log("회원가입 페이지: GUEST 권한 적용");
    }
  }

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
    } else {
      emailError.style.display = 'block';
    }
    
    // 이메일이 변경되면 중복확인 버튼 재활성화
    emailCheckBtn.textContent = '중복확인';
    emailCheckBtn.disabled = false;
    emailCheckBtn.classList.remove('completed');
    checkFormValidity();
  });

  // 이메일 중복 확인
  emailCheckBtn.addEventListener('click', function() {
    const email = emailInput.value;
    if (validateEmail(email)) {
      // 가상의 서버 응답을 시뮬레이션
      setTimeout(() => {
        this.textContent = '확인완료';
        this.classList.add('completed'); // 클래스로 스타일 변경
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
    } else {
      nicknameError.style.display = 'block';
    }
    
    // 닉네임이 변경되면 중복확인 버튼 재활성화
    nicknameCheckBtn.textContent = '중복확인';
    nicknameCheckBtn.disabled = false;
    nicknameCheckBtn.classList.remove('completed');
    checkFormValidity();
  });

  // 닉네임 중복 확인
  nicknameCheckBtn.addEventListener('click', function() {
    const nickname = nicknameInput.value;
    if (nickname.length >= 2 && nickname.length <= 10) {
      // 가상의 서버 응답을 시뮬레이션
      setTimeout(() => {
        this.textContent = '확인완료';
        this.classList.add('completed'); // 클래스로 스타일 변경
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
      // 어떤 유효성 검사를 통과하지 못했는지 확인
      if (this.value.length < 8) {
        passwordError.textContent = "비밀번호는 최소 8자 이상이어야 합니다.";
      } else if (!/(?=.*[A-Za-z])/.test(this.value)) {
        passwordError.textContent = "비밀번호에 영문자가 포함되어야 합니다.";
      } else if (!/(?=.*\d)/.test(this.value)) {
        passwordError.textContent = "비밀번호에 숫자가 포함되어야 합니다.";
      } else if (!/(?=.*[!@#$%^&*])/.test(this.value)) {
        passwordError.textContent = "비밀번호에 특수문자(!@#$%^&*)가 포함되어야 합니다.";
      } else {
        passwordError.textContent = "비밀번호 형식이 올바르지 않습니다.";
      }
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
  }  // 비밀번호 표시/숨김 토글 기능
  const passwordToggle = document.getElementById('password-toggle');
  const passwordConfirmToggle = document.getElementById('password-confirm-toggle');
  
  passwordToggle.addEventListener('click', function() {
    const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
    passwordInput.setAttribute('type', type);
    
    // 아이콘 상태 변경
    if (type === 'password') {
      this.querySelector('.eye-icon').classList.remove('eye-slash');
      this.querySelector('.eye-icon').classList.remove('eye-active');
      this.classList.remove('active'); // 토글 버튼 비활성화
    } else {
      this.querySelector('.eye-icon').classList.add('eye-slash');
      this.querySelector('.eye-icon').classList.add('eye-active');
      this.classList.add('active'); // 토글 버튼 활성화
    }
  });
  
  passwordConfirmToggle.addEventListener('click', function() {
    const type = passwordConfirmInput.getAttribute('type') === 'password' ? 'text' : 'password';
    passwordConfirmInput.setAttribute('type', type);
    
    // 아이콘 상태 변경
    if (type === 'password') {
      this.querySelector('.eye-icon').classList.remove('eye-slash');
      this.querySelector('.eye-icon').classList.remove('eye-active');
      this.classList.remove('active'); // 토글 버튼 비활성화
    } else {
      this.querySelector('.eye-icon').classList.add('eye-slash');
      this.querySelector('.eye-icon').classList.add('eye-active');
      this.classList.add('active'); // 토글 버튼 활성화
    }
  });
});
