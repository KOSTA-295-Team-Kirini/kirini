// 회원가입 페이지 스크립트
document.addEventListener("DOMContentLoaded", function () {
    // 회원가입 페이지에서는 항상 비로그인 상태(GUEST)로 가정하고 UI를 설정
    if (typeof Auth !== "undefined") {
        // 명시적으로 로컬 스토리지 상태를 체크하여 권한 설정 강제 적용
        // 이미 로그인된 상태라면 홈으로 리다이렉트
        if (Auth.isLoggedIn()) {
            // 이미 로그인된 사용자라면 홈페이지로 리다이렉션
            window.location.href = "../pages/index.html";
            return;
        } else {
            // 강제로 Auth 모듈에 GUEST 권한 적용 (CSS 표시용)
            Auth.applyRoleVisibility();
            console.log("회원가입 페이지: GUEST 권한 적용");
        }
    }

    const signupForm = document.getElementById("signup-form");
    const emailInput = document.getElementById("email");
    const nicknameInput = document.getElementById("nickname");
    const passwordInput = document.getElementById("password");
    const passwordConfirmInput = document.getElementById("password-confirm");
    const emailCheckBtn = document.getElementById("email-check");
    const nicknameCheckBtn = document.getElementById("nickname-check");
    const signupButton = document.getElementById("signup-button");
    const agreeCheckbox = document.getElementById("agree");

    const emailError = document.getElementById("email-error");
    const nicknameError = document.getElementById("nickname-error");
    const passwordError = document.getElementById("password-error");
    const passwordConfirmError = document.getElementById(
        "password-confirm-error"
    );

    let isEmailCheckedAndValid = false;
    let isNicknameCheckedAndValid = false;

    // 이메일 유효성 검사 및 중복 확인 버튼 상태 초기화
    emailInput.addEventListener("input", function () {
        const isValid = validateEmail(this.value);
        if (isValid) {
            emailError.style.display = "none";
        } else {
            emailError.style.display = "block";
        }
        // 이메일이 변경되면 중복확인 버튼 재활성화 및 상태 초기화
        isEmailCheckedAndValid = false;
        emailCheckBtn.textContent = "중복확인";
        emailCheckBtn.disabled = false;
        emailCheckBtn.classList.remove("completed");
        checkFormValidity();
    });

    // 이메일 중복 확인
    emailCheckBtn.addEventListener("click", async function () {
        const email = emailInput.value;
        if (!validateEmail(email)) {
            emailError.textContent = "올바른 이메일 형식이 아닙니다.";
            emailError.style.display = "block";
            return;
        }
        emailError.style.display = "none";
        try {
            const result = await UserService.checkEmailDuplicate(email);
            if (result.isAvailable) {
                emailError.style.display = "none";
                emailCheckBtn.textContent = "확인완료";
                emailCheckBtn.disabled = true;
                emailCheckBtn.classList.add("completed");
                isEmailCheckedAndValid = true;
            } else {
                emailError.textContent =
                    result.message || "이미 사용 중인 이메일입니다.";
                emailError.style.display = "block";
                isEmailCheckedAndValid = false;
            }
        } catch (error) {
            console.error("이메일 중복 확인 중 오류 발생:", error);
            emailError.textContent = "오류가 발생했습니다. 다시 시도해주세요.";
            emailError.style.display = "block";
            isEmailCheckedAndValid = false;
        }
        checkFormValidity();
    });

    // 닉네임 유효성 검사 및 중복 확인 버튼 상태 초기화
    nicknameInput.addEventListener("input", function () {
        const isValid = this.value.length >= 2 && this.value.length <= 10;
        if (isValid) {
            nicknameError.style.display = "none";
        } else {
            nicknameError.style.display = "block";
        }
        // 닉네임이 변경되면 중복확인 버튼 재활성화 및 상태 초기화
        isNicknameCheckedAndValid = false;
        nicknameCheckBtn.textContent = "중복확인";
        nicknameCheckBtn.disabled = false;
        nicknameCheckBtn.classList.remove("completed");
        checkFormValidity();
    });

    // 닉네임 중복 확인
    nicknameCheckBtn.addEventListener("click", async function () {
        const nickname = nicknameInput.value;
        if (!(nickname.length >= 2 && nickname.length <= 10)) {
            nicknameError.textContent = "닉네임은 2-10자 이내여야 합니다.";
            nicknameError.style.display = "block";
            return;
        }
        nicknameError.style.display = "none";
        try {
            const result = await UserService.checkNicknameDuplicate(nickname);
            if (result.isAvailable) {
                nicknameError.style.display = "none";
                nicknameCheckBtn.textContent = "확인완료";
                nicknameCheckBtn.disabled = true;
                nicknameCheckBtn.classList.add("completed");
                isNicknameCheckedAndValid = true;
            } else {
                nicknameError.textContent =
                    result.message || "이미 사용 중인 닉네임입니다.";
                nicknameError.style.display = "block";
                isNicknameCheckedAndValid = false;
            }
        } catch (error) {
            console.error("닉네임 중복 확인 중 오류 발생:", error);
            nicknameError.textContent = "오류가 발생했습니다. 다시 시도해주세요.";
            nicknameError.style.display = "block";
            isNicknameCheckedAndValid = false;
        }
        checkFormValidity();
    });

    // 비밀번호 유효성 검사
    passwordInput.addEventListener("input", function () {
        const isValid = validatePassword(this.value);
        if (isValid) {
            passwordError.style.display = "none";
        } else {
            passwordError.style.display = "block";
        }
        // 비밀번호 확인 일치 여부도 체크
        const isMatch = this.value === passwordConfirmInput.value;
        if (isMatch || passwordConfirmInput.value === "") {
            passwordConfirmError.style.display = "none";
        } else {
            passwordConfirmError.style.display = "block";
        }
        checkFormValidity();
    });

    // 비밀번호 확인 일치 검사
    passwordConfirmInput.addEventListener("input", function () {
        const isMatch = this.value === passwordInput.value;
        if (isMatch) {
            passwordConfirmError.style.display = "none";
        } else {
            passwordConfirmError.style.display = "block";
        }
        checkFormValidity();
    });

    // 약관 동의 체크박스
    agreeCheckbox.addEventListener("change", checkFormValidity);

    // 폼 제출 이벤트
    signupForm.addEventListener("submit", async function (e) {
        e.preventDefault();
        if (signupButton.disabled) {
            alert("입력값을 모두 정확히 입력하고, 약관에 동의해주세요.");
            return;
        }

        const email = emailInput.value;
        const nickname = nicknameInput.value;
        const password = passwordInput.value;
        try {
            const response = await fetch("/signup.do", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({email, nickname, password}),
            });

            const result = await response.json();

            if (result.success === true) {
                alert("회원가입이 완료되었습니다! 로그인 페이지로 이동합니다.");
                window.location.href = "login.html"; // 회원가입 성공 시 로그인 페이지로 이동
            } else {
                alert(result.message || "회원가입에 실패했습니다. 다시 시도해주세요.");
            }
        } catch (error) {
            console.error("회원가입 요청 중 오류 발생:", error);
            alert("회원가입 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    });

    // 이메일 유효성 검사 함수
    function validateEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    }

    // 비밀번호 유효성 검사 함수 (영문, 숫자, 특수문자 조합 8자 이상)
    function validatePassword(password) {
        // 실제 프로젝트의 비밀번호 정책에 맞게 수정할 수 있다냥!
        const re = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*])[A-Za-z\d!@#$%^&*]{8,}$/;
        return re.test(password);
    }

    // 폼 유효성 검증 함수
    function checkFormValidity() {
        const isEmailValid = validateEmail(emailInput.value);
        const isNicknameValid =
            nicknameInput.value.length >= 2 && nicknameInput.value.length <= 10;
        const isPasswordValid = validatePassword(passwordInput.value);
        const isPasswordConfirmValid =
            passwordInput.value === passwordConfirmInput.value;
        const isAgreed = agreeCheckbox.checked;

        // 모든 조건이 충족되어야 회원가입 버튼 활성화
        if (
            isEmailValid &&
            isEmailCheckedAndValid && // 이메일 중복 확인 완료 여부
            isNicknameValid &&
            isNicknameCheckedAndValid && // 닉네임 중복 확인 완료 여부
            isPasswordValid &&
            isPasswordConfirmValid &&
            isAgreed
        ) {
            signupButton.disabled = false;
        } else {
            signupButton.disabled = true;
        }
    }

    // 비밀번호 보이기/숨기기 토글 기능 (기존 코드 유지)
    const passwordToggle = document.getElementById("password-toggle");
    const passwordConfirmToggle = document.getElementById(
        "password-confirm-toggle"
    );

    if (passwordToggle) {
        passwordToggle.addEventListener("click", function () {
            const type =
                passwordInput.getAttribute("type") === "password" ? "text" : "password";
            passwordInput.setAttribute("type", type);
            // 아이콘 변경 로직 (필요시 추가)
            this.classList.toggle("active"); // 예시: active 클래스로 아이콘 상태 변경
        });
    }

    if (passwordConfirmToggle) {
        passwordConfirmToggle.addEventListener("click", function () {
            const type =
                passwordConfirmInput.getAttribute("type") === "password"
                    ? "text"
                    : "password";
            passwordConfirmInput.setAttribute("type", type);
            // 아이콘 변경 로직 (필요시 추가)
            this.classList.toggle("active"); // 예시: active 클래스로 아이콘 상태 변경
        });
    }
});
