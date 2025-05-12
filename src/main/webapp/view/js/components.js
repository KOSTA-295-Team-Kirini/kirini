// HTML 컴포넌트 로드 함수
document.addEventListener('DOMContentLoaded', function() {
  // 현재 페이지의 경로를 확인합니다
  const currentPath = window.location.pathname;
  
  // 헤더 플레이스홀더 엘리먼트를 가져옵니다
  const headerPlaceholder = document.getElementById('header-placeholder');
  
  // 푸터 플레이스홀더 엘리먼트를 가져옵니다
  const footerPlaceholder = document.getElementById('footer-placeholder');
  
  // 상대 경로를 결정합니다 (현재 경로가 /pages/로 시작하면 상위 디렉토리로 이동해야 함)
  const relativePath = currentPath.includes('/pages/') ? '../' : '';
  
  // 관리자 페이지인지 확인합니다 (URL에 admin.html이 포함되어 있는지)
  const isAdminPage = currentPath.includes('admin.html');
  
  if (headerPlaceholder) {
    // 관리자 페이지라면 admin_header.html을 로드, 그렇지 않으면 일반 header.html 로드
    const headerFile = isAdminPage ? 'components/admin_header.html' : 'components/header.html';
    
    fetch(`${relativePath}${headerFile}`)
      .then(response => response.text())
      .then(data => {
        headerPlaceholder.innerHTML = data;
      })
      .catch(error => {
        console.error('Error loading header:', error);
      });
  }
  
  if (footerPlaceholder) {
    fetch(`${relativePath}components/footer.html`)
      .then(response => response.text())
      .then(data => {
        footerPlaceholder.innerHTML = data;
      })
      .catch(error => {
        console.error('Error loading footer:', error);
      });
  }
});
