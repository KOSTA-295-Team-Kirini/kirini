// 폼 요소들 가져오기
document.addEventListener("DOMContentLoaded", function () {
  const form = document.getElementById("signup-form");
  const emailInput = document.getElementById("email");
  const nicknameInput = document.getElementById("nickname");
  const passwordInput = document.getElementById("password");
  const passwordConfirmInput = document.getElementById("password-confirm");
  const agreeCheckbox = document.getElementById("agree");
  const signupButton = document.getElementById("signup-button");
  const emailCheckButton = document.getElementById("email-check");
  const nicknameCheckButton = document.getElementById("nickname-check");

  // 초기 데이터셋 값 설정
  emailInput.dataset.lastValue = "";
  nicknameInput.dataset.lastValue = "";

  // 에러 메시지 요소들
  const emailError = document.getElementById("email-error");
  const nicknameError = document.getElementById("nickname-error");
  const passwordError = document.getElementById("password-error");
  const passwordConfirmError = document.getElementById(
    "password-confirm-error"
  );

  // 유효성 검사 상태
  let validationState = {
    email: false,
    emailDuplication: false,
    nickname: false,
    nicknameDuplication: false,
    password: false,
    passwordConfirm: false,
    agree: false,
  };

  // 폼 입력값 변경시 유효성 검사
  emailInput.addEventListener("input", validateEmail);
  nicknameInput.addEventListener("input", validateNickname);
  passwordInput.addEventListener("input", validatePassword);
  passwordConfirmInput.addEventListener("input", validatePasswordConfirm);
  agreeCheckbox.addEventListener("change", validateAgree);

  // 중복확인 버튼 이벤트
  emailCheckButton.addEventListener("click", checkEmailDuplication);
  nicknameCheckButton.addEventListener("click", checkNicknameDuplication);

  // 폼 제출 이벤트 핸들러
  form.addEventListener("submit", function (e) {
    e.preventDefault(); // 기본 제출 동작 방지

    console.log("폼 제출 시도");

    // FormData 객체 생성
    const formData = new FormData(this);

    // 디버깅: FormData 내용 확인
    console.log("FormData 내용:");
    for (let pair of formData.entries()) {
      console.log(pair[0] + ": " + pair[1]);
    }

    // 서버로 데이터 전송
    fetch("/kirini/signup", {
      method: "POST",
      body: formData,
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error("서버 응답 오류");
        }
        return response.json();
      })
      .then((data) => {
        console.log("서버 응답:", data);
        if (data.success) {
          alert(data.message || "회원가입이 완료되었습니다.");
          window.location.href = data.redirect || "login.html";
        } else {
          alert(data.message || "회원가입에 실패했습니다.");
        }
      })
      .catch((error) => {
        console.error("API 오류:", error);
        alert("서버 연결에 실패했습니다.");
      });
  });

  // 이메일 유효성 검사
  function validateEmail() {
    const email = emailInput.value.trim();
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const isValid = re.test(email);

    if (!isValid) {
      emailError.style.display = "block";
      emailError.textContent = "올바른 이메일 형식이 아닙니다.";
      validationState.email = false;
    } else {
      emailError.style.display = "none";
      validationState.email = true;
    }

    // 이메일이 변경되면 중복확인 상태 초기화
    if (emailInput.dataset.lastValue !== email) {
      validationState.emailDuplication = false;
      emailInput.dataset.lastValue = email;
      // 시각적 표시 추가
      emailCheckButton.style.backgroundColor = "#0078d7";
      emailCheckButton.textContent = "중복확인";
    }

    updateSignupButton();
    return isValid;
  }
  // 닉네임 유효성 검사
  function validateNickname() {
    const nickname = nicknameInput.value.trim();
    const isValid = nickname.length >= 2 && nickname.length <= 10;

    if (!isValid) {
      nicknameError.style.display = "block";
      validationState.nickname = false;
    } else {
      nicknameError.style.display = "none";
      validationState.nickname = true;
    }

    // 닉네임이 변경되면 중복확인 상태 초기화
    if (nicknameInput.dataset.lastValue !== nickname) {
      validationState.nicknameDuplication = false;
      nicknameInput.dataset.lastValue = nickname;
      // 시각적 표시 추가
      nicknameCheckButton.style.backgroundColor = "#0078d7";
      nicknameCheckButton.textContent = "중복확인";
    }

    updateSignupButton();
    return isValid;
  }

  // 비밀번호 유효성 검사
  function validatePassword() {
    const password = passwordInput.value;
    // 최소 8자, 하나 이상의 문자, 하나의 숫자, 하나의 특수 문자
    const isValid =
      /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/.test(
        password
      );

    if (!isValid) {
      passwordError.style.display = "block";
      validationState.password = false;
    } else {
      passwordError.style.display = "none";
      validationState.password = true;
    }

    // 비밀번호 확인도 재검증
    if (passwordConfirmInput.value) {
      validatePasswordConfirm();
    }

    updateSignupButton();
    return isValid;
  }

  // 비밀번호 확인 검사
  function validatePasswordConfirm() {
    const password = passwordInput.value;
    const confirmPassword = passwordConfirmInput.value;
    const isValid = password === confirmPassword && confirmPassword.length > 0;

    if (!isValid) {
      passwordConfirmError.style.display = "block";
      validationState.passwordConfirm = false;
    } else {
      passwordConfirmError.style.display = "none";
      validationState.passwordConfirm = true;
    }

    updateSignupButton();
    return isValid;
  }

  // 이용약관 동의 검사
  function validateAgree() {
    const isChecked = agreeCheckbox.checked;
    validationState.agree = isChecked;
    updateSignupButton();
    return isChecked;
  }
  // 이메일 중복 확인
  function checkEmailDuplication() {
    if (!validateEmail()) {
      alert("올바른 이메일 형식으로 입력해주세요.");
      return;
    }

    // 서버에 중복 확인 요청
    fetch(
      `/kirini/signup?action=checkEmail&email=${encodeURIComponent(
        emailInput.value
      )}`
    )
      .then((response) => {
        if (!response.ok) throw new Error("서버 응답 오류");
        return response.json();
      })
      .then((data) => {
        console.log("이메일 중복 확인 응답:", data);

        if (data.isDuplicate) {
          emailError.style.display = "block";
          emailError.textContent = "이미 사용 중인 이메일입니다.";
          validationState.emailDuplication = false;
        } else {
          alert("사용 가능한 이메일입니다.");
          validationState.emailDuplication = true;
          emailCheckButton.style.backgroundColor = "#28a745";
          emailCheckButton.textContent = "확인완료";
        }

        updateSignupButton();
      })
      .catch((error) => {
        console.error("API 오류:", error);
        alert("서버 연결에 실패했습니다.");
        validationState.emailDuplication = false;
        updateSignupButton();
      });
  }
  // 닉네임 중복 확인
  function checkNicknameDuplication() {
    if (!validateNickname()) {
      alert("닉네임은 2-10자 이내여야 합니다.");
      return;
    }

    // 서버에 중복 확인 요청
    fetch(
      `/kirini/signup?action=checkNickname&nickname=${encodeURIComponent(
        nicknameInput.value
      )}`
    )
      .then((response) => {
        if (!response.ok) throw new Error("서버 응답 오류");
        return response.json();
      })
      .then((data) => {
        console.log("닉네임 중복 확인 응답:", data);

        if (data.isDuplicate) {
          nicknameError.style.display = "block";
          nicknameError.textContent = "이미 사용 중인 닉네임입니다.";
          validationState.nicknameDuplication = false;
        } else {
          alert("사용 가능한 닉네임입니다.");
          validationState.nicknameDuplication = true;
          nicknameCheckButton.style.backgroundColor = "#28a745";
          nicknameCheckButton.textContent = "확인완료";
        }

        updateSignupButton();
      })
      .catch((error) => {
        console.error("API 오류:", error);
        alert("서버 연결에 실패했습니다.");
        validationState.nicknameDuplication = false;
        updateSignupButton();
      });
  }

  // 회원가입 버튼 활성화/비활성화 업데이트
  function updateSignupButton() {
    if (isFormValid()) {
      signupButton.disabled = false;
    } else {
      signupButton.disabled = true;
    }
  }

  // 모든 유효성 검사가 통과했는지 확인
  function isFormValid() {
    return (
      validationState.email &&
      validationState.emailDuplication &&
      validationState.nickname &&
      validationState.nicknameDuplication &&
      validationState.password &&
      validationState.passwordConfirm &&
      validationState.agree
    );
  }
});
