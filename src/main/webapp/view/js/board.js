// 게시판 탭 전환 기능
document.addEventListener('DOMContentLoaded', function() {
  const boardLinks = document.querySelectorAll('.board-nav a');
  const boardContents = document.querySelectorAll('.board-content');
  
  const boardBottomContainer = document.getElementById('board-bottom-container');
  
  boardLinks.forEach(link => {
    link.addEventListener('click', function(e) {
      e.preventDefault();
      
      // 모든 탭 비활성화
      boardLinks.forEach(l => l.classList.remove('active'));
      boardContents.forEach(c => c.style.display = 'none');
      
      // 선택한 탭 활성화
      this.classList.add('active');
      const boardId = this.getAttribute('data-board');
      document.getElementById(boardId + '-board').style.display = 'block';
      
      // 익명 게시판일 경우 페이지네이션과 검색 숨기기
      if (boardId === 'anonymous') {
        boardBottomContainer.style.display = 'none';
      } else {
        boardBottomContainer.style.display = 'block';
      }
    });
  });
  
  // 초기 로드 시 현재 활성화된 탭 확인
  const activeTab = document.querySelector('.board-nav a.active');
  if (activeTab && activeTab.getAttribute('data-board') === 'anonymous') {
    boardBottomContainer.style.display = 'none';
  }
  
  // 글쓰기 모달 기능
  const writeBtn = document.getElementById('write-btn');
  const writeModal = document.getElementById('write-modal');
  const closeButtons = document.querySelectorAll('.close');
  
  writeBtn.addEventListener('click', function() {
    writeModal.style.display = 'block';
  });
  
  closeButtons.forEach(btn => {
    btn.addEventListener('click', function() {
      writeModal.style.display = 'none';
      document.getElementById('post-detail-modal').style.display = 'none';
    });
  });
  
  // 한줄 게시판 게시글 클릭 시 모달 표시
  const onelinePosts = document.querySelectorAll('.oneline-post');
  const postDetailModal = document.getElementById('post-detail-modal');
  
  onelinePosts.forEach(post => {
    post.addEventListener('click', function() {
      const postId = this.getAttribute('data-post-id');
      // 실제로는 postId를 기반으로 서버에서 데이터를 가져와야 함
      // 여기서는 예시로 고정된 데이터 표시
      postDetailModal.style.display = 'block';
    });
  });
  
  // 모달 외부 클릭 시 닫기
  window.addEventListener('click', function(e) {
    if (e.target === writeModal) {
      writeModal.style.display = 'none';
    }
    if (e.target === postDetailModal) {
      postDetailModal.style.display = 'none';
    }
  });
  
  // 게시판 탭 기능
  const boardNav = document.querySelectorAll('.board-nav a');
  boardNav.forEach(tab => {
    tab.addEventListener('click', function(e) {
      e.preventDefault();
      
      boardNav.forEach(item => item.classList.remove('active'));
      this.classList.add('active');
      
      const board = this.getAttribute('data-board');
      
      boardContents.forEach(content => {
        content.style.display = 'none';
      });
      
      document.getElementById(`${board}-board`).style.display = 'block';
    });
  });
  
  // 모달 관련 기능
  const closeBtn = document.querySelector('.close');
  const cancelBtn = document.querySelector('.cancel');
  const submitBtn = document.getElementById('submit-post');
  
  function closeModal() {
    writeModal.style.display = 'none';
  }
  
  closeBtn.addEventListener('click', closeModal);
  cancelBtn.addEventListener('click', closeModal);
  
  window.addEventListener('click', function(e) {
    if (e.target == writeModal) {
      closeModal();
    }
  });
  
  submitBtn.addEventListener('click', function() {
    // 실제 구현에서는 AJAX로 서버에 데이터 전송
    alert('게시글이 등록되었습니다.');
    closeModal();
  });
});
