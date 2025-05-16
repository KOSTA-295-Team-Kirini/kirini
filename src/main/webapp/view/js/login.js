document.addEventListener('DOMContentLoaded', function() {
  // 개발 모드 토글 기능 삭제
  
  document.getElementById('login-form').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const email = emailInput.value;
    const password = passwordInput.value;
    
    try {
      const response = await fetch('/user/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password }),
      });

      const result = await response.json();

      if (response.ok && result.status === 'success') {
        const userName = result.data.userName;
        const role = result.data.role;
        const rememberMe = document.getElementById('remember-me').checked;

        if (typeof Auth !== 'undefined') {
          Auth.setLoginStatus(userName, role, rememberMe); // Auth.js에 맞게 함수 호출
        } else {
          // Auth 객체가 없을 경우를 대비한 폴백 로직 (기존 방식 유지)
          const storage = rememberMe ? localStorage : sessionStorage;
          storage.setItem('isLoggedIn', 'true');
          storage.setItem('userName', userName);
          storage.setItem('userRole', role);
        }
        
        alert((role === 'ADMIN' ? '관리자' : role === 'MANAGER' ? '매니저' : '일반 회원') + '님, 환영합니다! 로그인되었습니다.');
        window.location.href = '../pages/index.html';
      } else {
        alert(result.message || '이메일 또는 비밀번호가 일치하지 않습니다. 다시 시도해주세요.');
        passwordInput.focus();
      }
    } catch (error) {
      console.error('로그인 요청 중 오류 발생:', error);
      alert('로그인 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
    }
  });
});
