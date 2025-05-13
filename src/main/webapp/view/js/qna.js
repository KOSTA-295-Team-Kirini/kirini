/* QNA 페이지 관련 자바스크립트 */
document.addEventListener('DOMContentLoaded', function() {
  // Q&A 상세 내용 표시 기능
  const qnaLinks = document.querySelectorAll('.qna-title a');
  
  // 모든 질문/내 질문 탭 기능
  const tabButtons = document.querySelectorAll('.qna-detail-buttons .left-buttons .btn');
  
  tabButtons.forEach(button => {
    button.addEventListener('click', function(e) {
      e.preventDefault();
      
      // 현재 활성화된 버튼의 active 클래스 제거
      tabButtons.forEach(btn => btn.classList.remove('active'));
      
      // 클릭한 버튼에 active 클래스 추가
      this.classList.add('active');
      
      // 실제 구현에서는 여기서 AJAX 요청 등을 통해 해당 탭의 데이터를 불러올 수 있습니다.
      console.log('탭 클릭:', this.textContent);
    });
  });
  
  qnaLinks.forEach(link => {
    link.addEventListener('click', function(e) {
      e.preventDefault();
      
      // 이미 열려있는 상세 내용 모두 닫기
      document.querySelectorAll('.qna-detail').forEach(detail => {
        detail.style.display = 'none';
      });
      
      // 실제 구현에서는 AJAX로 데이터를 가져오거나 모달을 사용할 수 있습니다.
      const targetId = this.getAttribute('href');
      const detailElement = document.querySelector(targetId);
      
      // 간단한 데모 구현
      if (detailElement) {
        // 부드러운 애니메이션 효과 추가
        detailElement.style.opacity = 0;
        detailElement.style.display = 'block';
        
        // 부드럽게 나타나는 효과
        setTimeout(() => {
          detailElement.style.transition = 'opacity 0.3s ease';
          detailElement.style.opacity = 1;
        }, 10);
          // 스크롤 이동
        detailElement.scrollIntoView({behavior: 'smooth', block: 'start'});
      }
    });
  });
  
  // Q&A 상세 내용 닫기 버튼 기능
  document.addEventListener('click', function(e) {
    if (e.target.classList.contains('qna-detail-close')) {
      // 닫기 버튼 클릭 시
      const detailElement = e.target.closest('.qna-detail');
      if (detailElement) {
        // 부드럽게 사라지는 효과
        detailElement.style.transition = 'opacity 0.3s ease';
        detailElement.style.opacity = 0;
        
        // 애니메이션 후 숨김 처리
        setTimeout(() => {
          detailElement.style.display = 'none';
        }, 300);
      }
    }
  });
  
  // 필터 버튼에 대한 이벤트 리스너
  const filterButtons = document.querySelectorAll('.qna-filter .filter-buttons .btn');
  filterButtons.forEach(button => {
    button.addEventListener('click', function() {
      // 이미 활성화된 버튼이면 무시
      if (this.classList.contains('active')) return;
      
      // 모든 필터 버튼에서 active 클래스 제거
      filterButtons.forEach(btn => btn.classList.remove('active'));
      
      // 클릭된 버튼에 active 클래스 추가
      this.classList.add('active');
      
      // 실제 구현에서는 여기서 필터링 로직 추가
      // 예: 버튼 텍스트에 따라 다른 종류의 질문 표시
      const filterType = this.textContent.trim();
      const qnaItems = document.querySelectorAll('.qna-item');
      
      qnaItems.forEach(item => {
        const statusElement = item.querySelector('.qna-status');
        const status = statusElement ? statusElement.textContent.trim() : '';
          switch(filterType) {
          case '모든 질문':
            item.style.display = 'block';
            break;
          case '내 질문':
            // 실제로는 사용자 ID 등으로 필터링
            item.style.display = 'block';  // 데모용
            break;
        }
      });
    });
  });
  
  // 검색 기능
  const searchButton = document.querySelector('.search-bar .btn');
  const searchInput = document.querySelector('.search-bar input');
  
  if (searchButton && searchInput) {
    searchButton.addEventListener('click', performSearch);
    searchInput.addEventListener('keypress', function(e) {
      if (e.key === 'Enter') {
        performSearch();
      }
    });
  }
  function performSearch() {
    const searchTerm = searchInput.value.trim().toLowerCase();
    
    // 검색어가 비어있을 때 알림 텍스트로 처리
    if (searchTerm === '') {
      showSearchNotification('검색어를 입력해주세요', 'warning');
      return;
    }
    
    const qnaItems = document.querySelectorAll('.qna-item');
    let foundResults = false;
    
    qnaItems.forEach(item => {
      const title = item.querySelector('.qna-title').textContent.toLowerCase();
      
      // 제목에 검색어가 포함되어 있으면 표시
      if (title.includes(searchTerm)) {
        item.style.display = 'block';
        // 검색 결과 하이라이트 (실제 구현에서는 더 복잡할 수 있음)
        item.style.borderColor = '#ff9800';
        setTimeout(() => {
          item.style.borderColor = '#f0f0f0';
        }, 2000);
        foundResults = true;
      } else {
        item.style.display = 'none';
      }
    });
    
    // 검색 결과가 없을 때 알림 표시
    if (!foundResults) {
      showSearchNotification('검색 결과가 없습니다', 'info');
    }
  }    // 질문하기 버튼 기능
  const askButton = document.querySelector('.ask-question');
  if (askButton) {
    askButton.addEventListener('click', function() {
      // 실제 구현에서는 모달이나 새 페이지로 이동
      alert('질문 작성 페이지로 이동합니다.');
    });
  }
  
  // 검색 알림 표시 함수
  function showSearchNotification(message, type) {
    // 이미 있는 알림 제거
    const existingNotification = document.querySelector('.search-notification');
    if (existingNotification) {
      existingNotification.remove();
    }
    
    // 알림 요소 생성
    const notification = document.createElement('div');
    notification.className = `search-notification ${type}`;
    notification.textContent = message;
    
    // 검색창 아래에 알림 추가
    const searchBar = document.querySelector('.search-bar');
    if (searchBar) {
      searchBar.parentNode.insertBefore(notification, searchBar.nextSibling);
      
      // 3초 후 자동으로 알림 숨기기
      setTimeout(() => {
        notification.style.opacity = '0';
        setTimeout(() => {
          notification.remove();
        }, 300);
      }, 3000);
    }
  }
});
