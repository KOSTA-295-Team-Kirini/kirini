document.addEventListener("DOMContentLoaded", function () {
  document
    .getElementById("login-form")
    .addEventListener("submit", function (e) {
      e.preventDefault();

      const email = document.getElementById("email").value;
      const password = document.getElementById("password").value;

      // 여기에 실제 로그인 로직을 구현할 수 있습니다
      // 예시: 유효성 검사
      if (!validateEmail(email)) {
        alert("올바른 이메일 형식이 아닙니다.");
        return;
      }

      if (password.length < 6) {
        alert("비밀번호는 최소 6자 이상이어야 합니다.");
        return;
      }

      // 로그인 상태 표시
      const loginButton = document.querySelector(
        '#login-form button[type="submit"]'
      );
      const originalText = loginButton.textContent;
      loginButton.disabled = true;
      loginButton.textContent = "로그인 중...";

      // 로그인 요청 전송
      const formData = new URLSearchParams();
      formData.append("email", email);
      formData.append("password", password);

      const rememberMe = document.getElementById("remember-me");
      if (rememberMe && rememberMe.checked) {
        formData.append("remember-me", "on");
      }

      fetch("/kirini/login.do", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: formData,
      })
        .then((response) => {
          console.log("응답 상태:", response.status);
          if (!response.ok) {
            throw new Error("서버 응답 오류");
          }
          return response.json();
        })
        .then((data) => {
          console.log("로그인 응답:", data);

          if (data.success) {
            // 로그인 성공 처리
            alert(data.message || "로그인 성공");
            window.location.href = data.redirect || "/kirini/view/index.html";
          } else {
            // 로그인 실패 처리
            alert(data.message || "이메일 또는 비밀번호가 올바르지 않습니다.");
            loginButton.disabled = false;
            loginButton.textContent = originalText;
          }
        })
        .catch((error) => {
          console.error("로그인 오류:", error);
          alert("서버 연결에 실패했습니다.");
          loginButton.disabled = false;
          loginButton.textContent = originalText;
        });
    });
});

function validateEmail(email) {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return re.test(email);
}
