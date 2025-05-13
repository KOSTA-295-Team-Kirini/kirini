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
    
    // 임시 로그인 처리 (실제로는 서버 응답에 따라 처리해야 함)
    // 이메일에서 사용자 이름 추출 (@ 앞부분)
    const userName = email.split('@')[0];
    
    // 로그인 상태 유지 체크 여부 확인
    const rememberMe = document.getElementById('remember-me').checked;
    
    // 역할 설정 (테스트용)
    const role = email.toLowerCase().includes('admin') ? 'ADMIN' : 'USER';
      // Auth 객체가 있으면 사용, 없으면 직접 저장
    if (typeof Auth !== 'undefined') {
      Auth.setRole(role);
      
      // 사용자 이름은 Auth에서 직접 관리하지 않으므로 별도 저장
      const storage = rememberMe ? localStorage : sessionStorage;
      storage.setItem('userName', userName);
    } else {
      // 기존 방식으로 로그인 정보 저장 (Auth 객체 없을 경우 호환성 유지)
      if (rememberMe) {
        // 로컬 스토리지에 저장 (브라우저 닫아도 유지됨)
        localStorage.setItem('isLoggedIn', 'true');
        localStorage.setItem('userName', userName);
        localStorage.setItem('userRole', role);
      } else {
        // 세션 스토리지에 저장 (브라우저 닫으면 사라짐)
        sessionStorage.setItem('isLoggedIn', 'true');
        sessionStorage.setItem('userName', userName);
        sessionStorage.setItem('userRole', role);
      }
    }
    
    alert(`${role === 'ADMIN' ? '관리자' : '사용자'}로 로그인되었습니다.`);
    
    // 성공 시 메인 페이지로 리디렉션
    window.location.href = '../pages/index.html';
  });
});

function validateEmail(email) {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return re.test(email);
}
