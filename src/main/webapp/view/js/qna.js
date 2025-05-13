/* QNA 페이지 관련 자바스크립트 */
document.addEventListener('DOMContentLoaded', function() {
  // Q&A 상세 내용 표시 기능
  const qnaLinks = document.querySelectorAll('.qna-title a');
  
  // 모든 질문/내 질문 탭 기능
  const tabButtons = document.querySelectorAll('.qna-detail-buttons .left-buttons .btn');
  // 글쓰기 모달 기능
  const writeBtn = document.getElementById('write-btn');
  const writeModal = document.getElementById('write-modal');
  const closeButtons = document.querySelectorAll('.close');
  const submitBtn = document.getElementById('submit-post');
  
  // 글쓰기 버튼 클릭 시 모달 표시
  writeBtn.addEventListener('click', function() {
    writeModal.style.display = 'block';
  });
  
  // 닫기 버튼 클릭 시 모달 닫기
  closeButtons.forEach(btn => {
    btn.addEventListener('click', function() {
      writeModal.style.display = 'none';
    });
  });
  
  // 질문 등록 버튼 클릭 시 처리
  if (submitBtn) {
    submitBtn.addEventListener('click', function() {
      // 입력 필드에서 값 가져오기
      const title = document.getElementById('post-title').value.trim();
      const content = document.getElementById('post-content').value.trim();
      const fileInput = document.getElementById('post-file');
      
      // 입력 값 검증
      if (!title) {
        showNotification('제목을 입력해주세요.', 'error');
        return;
      }
      
      if (!content) {
        showNotification('내용을 입력해주세요.', 'error');
        return;
      }
      
      // 로딩 표시 (버튼 비활성화 및 텍스트 변경)
      submitBtn.disabled = true;
      submitBtn.textContent = '등록 중...';
      submitBtn.classList.add('loading');
      
      // FormData 객체 생성
      const formData = new FormData();
      formData.append('title', title);
      formData.append('content', content);
      
      // 파일이 첨부된 경우 추가
      if (fileInput.files.length > 0) {
        formData.append('file', fileInput.files[0]);
      }
      
      // 서버로 데이터 전송
      fetch('/qna/create', {
        method: 'POST',
        body: formData
      })
      .then(response => {
        if (!response.ok) {
          throw new Error('서버 응답 오류');
        }
        return response.json();
      })
      .then(data => {
        // 성공 처리
        showNotification('질문이 성공적으로 등록되었습니다!', 'success');
        
        // 입력 필드 초기화
        document.getElementById('post-title').value = '';
        document.getElementById('post-content').value = '';
        document.getElementById('post-file').value = '';
        
        // 모달 닫기
        writeModal.style.display = 'none';
        
        // 0.5초 후에 페이지 새로고침 (사용자에게 성공 메시지를 보여주기 위한 지연)
        setTimeout(() => {
          location.reload();
        }, 500);
      })
      .catch(error => {
        console.error('Error:', error);
        showNotification('질문 등록 중 오류가 발생했습니다. 다시 시도해주세요.', 'error');
      })
      .finally(() => {
        // 버튼 상태 복원
        submitBtn.disabled = false;
        submitBtn.textContent = '질문 등록하기';
        submitBtn.classList.remove('loading');
      });
    });
  }
  
  // 모달 외부 클릭 시 닫기
  window.addEventListener('click', function(e) {
    if (e.target === writeModal) {
      writeModal.style.display = 'none';
    }
  });
  
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
  }  // 질문하기 버튼 기능은 제거 (모달로 대체됨)
  
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

  // 알림 표시 함수 (등록, 에러 등의 알림에 사용)
  function showNotification(message, type) {
    // 이미 있는 알림 제거
    const existingNotification = document.querySelector('.notification');
    if (existingNotification) {
      existingNotification.remove();
    }
    
    // 알림 요소 생성
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    
    // 알림 스타일 설정
    notification.style.position = 'fixed';
    notification.style.top = '20px';
    notification.style.right = '20px';
    notification.style.padding = '15px 25px';
    notification.style.borderRadius = '5px';
    notification.style.zIndex = '9999';
    notification.style.opacity = '0';
    notification.style.transition = 'opacity 0.3s ease';
    notification.style.boxShadow = '0 3px 10px rgba(0,0,0,0.2)';
    
    // 타입에 따른 스타일 설정
    switch (type) {
      case 'success':
        notification.style.backgroundColor = '#2196F3';
        notification.style.color = 'white';
        break;
      case 'error':
        notification.style.backgroundColor = '#F44336';
        notification.style.color = 'white';
        break;
      case 'warning':
        notification.style.backgroundColor = '#FF9800';
        notification.style.color = 'white';
        break;
      default:
        notification.style.backgroundColor = '#4CAF50';
        notification.style.color = 'white';
    }
    
    // 문서에 알림 추가
    document.body.appendChild(notification);
    
    // 알림 표시 애니메이션
    setTimeout(() => {
      notification.style.opacity = '1';
    }, 10);
    
    // 3초 후 자동으로 알림 숨기기
    setTimeout(() => {
      notification.style.opacity = '0';
      setTimeout(() => {
        notification.remove();
      }, 300);
    }, 3000);
  }
});
