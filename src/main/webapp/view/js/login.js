document.addEventListener('DOMContentLoaded', function() {
  document.getElementById('login-form').addEventListener('submit', function(e) {
    e.preventDefault();
    
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    
    // 여기에 실제 로그인 로직을 구현할 수 있습니다
    // 예시: 유효성 검사
    if (!validateEmail(email)) {
      alert('올바른 이메일 형식이 아닙니다.');
      return;
    }
    
    if (password.length < 6) {
      alert('비밀번호는 최소 6자 이상이어야 합니다.');
      return;
    }
    
    // 로그인 요청 처리 (실제로는 서버로 요청을 보내야 함)
    console.log('로그인 시도:', { email, password: '******' });
    alert('로그인 기능은 현재 개발 중입니다.');
    
    // 성공 시 메인 페이지로 리디렉션 (예시)
    // window.location.href = '../index.html';
  });
});

function validateEmail(email) {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return re.test(email);
}
