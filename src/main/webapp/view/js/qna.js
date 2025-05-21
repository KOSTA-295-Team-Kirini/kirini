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
  
  // 답변 등록 버튼 기능 추가
  document.addEventListener('click', function(e) {
    if (e.target.classList.contains('answer-submit-btn')) {
      const answerForm = e.target.closest('.answer-form');
      const textarea = answerForm.querySelector('textarea');
      const answerContent = textarea.value.trim();
      const questionId = answerForm.getAttribute('data-question-id');
      
      // 답변 내용 검증
      if (!answerContent) {
        showNotification('답변 내용을 입력해주세요.', 'error');
        return;
      }
      
      // 로딩 표시 (버튼 비활성화 및 텍스트 변경)
      e.target.disabled = true;
      e.target.textContent = '등록 중...';
      e.target.classList.add('loading');
      
      // FormData 객체 생성
      const formData = new FormData();
      formData.append('content', answerContent);
      formData.append('questionId', questionId || '0'); // 질문 ID가 없으면 기본값 0 사용
      
      try {
        showNotification('답변이 성공적으로 등록되었습니다!', 'success');
        addNewAnswerToList(answerContent, questionId);
        textarea.value = '';
      } catch (error) {
        console.error('Mock Error:', error);
        showNotification('답변 등록 중 오류가 발생했습니다. 다시 시도해주세요.', 'error');
      } finally {
        // 버튼 상태 복원
        e.target.disabled = false;
        e.target.textContent = '답변 등록';
        e.target.classList.remove('loading');
      }
    }
  });
  
  // 새로운 답변을 화면에 추가하는 함수
  function addNewAnswerToList(content, questionId) {
    const answersContainer = document.querySelector(`#qna-${questionId} .qna-detail-answers`);
    
    if (!answersContainer) {
      // 질문 ID를 찾을 수 없는 경우 (단순히 현재 열린 상세 화면을 찾음)
      const openedDetail = document.querySelector('.qna-detail[style*="display: block"]');
      if (openedDetail) {
        const answersSection = openedDetail.querySelector('.qna-detail-answers');
        
        if (answersSection) {
          // 현재 사용자 정보 (실제 구현에서는 로그인한 사용자 정보를 사용)
          const username = '사용자';
          const today = new Date();
          const dateString = today.getFullYear() + '-' + 
                           String(today.getMonth() + 1).padStart(2, '0') + '-' + 
                           String(today.getDate()).padStart(2, '0');
          
          // 새 답변 요소 생성
          const newAnswer = document.createElement('div');
          newAnswer.className = 'answer';
          newAnswer.innerHTML = `
            <div class="answer-meta">
              <span>작성자: ${username}</span>
              <span>작성일: ${dateString}</span>
            </div>
            <div class="answer-content">
              <p>${content}</p>
            </div>
          `;
          
          // 답변 폼 앞에 새 답변 삽입
          const answerForm = answersSection.querySelector('.answer-form');
          answersSection.insertBefore(newAnswer, answerForm);
          
          // 답변 수 업데이트
          const answersTitle = answersSection.querySelector('h3');
          if (answersTitle) {
            const currentCount = parseInt(answersTitle.textContent.match(/\d+/) || 0);
            answersTitle.textContent = `답변 (${currentCount + 1})`;
          }
        }
      }
    }
  }
  
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
        // 서버로 데이터 전송 - QnaService 사용
      // QnaService.createQuestion(formData)
      // .then(response => {
      //   // QnaService는 이미 응답 처리를 수행했으므로 바로 데이터에 접근 가능
      // })
      // .then(data => {
      //   // 성공 처리
      //   showNotification('질문이 성공적으로 등록되었습니다!', 'success');
      //   
      //   // 입력 필드 초기화
      //   document.getElementById('post-title').value = '';
      //   document.getElementById('post-content').value = '';
      //   document.getElementById('post-file').value = '';
      //   
      //   // 모달 닫기
      //   writeModal.style.display = 'none';
      //   
      //   // 0.5초 후에 페이지 새로고침 (사용자에게 성공 메시지를 보여주기 위한 지연)
      //   setTimeout(() => {
      //     location.reload();
      //   }, 500);
      // })
      // .catch(error => {
      //   console.error('Error:', error);
      //   showNotification('질문 등록 중 오류가 발생했습니다. 다시 시도해주세요.', 'error');
      // })
      // .finally(() => {
      //   // 버튼 상태 복원
      //   submitBtn.disabled = false;
      //   submitBtn.textContent = '질문 등록하기';
      //   submitBtn.classList.remove('loading');
      // });

      // 목업 로직: 서버 호출 없이 진행
      try {
        showNotification('질문이 성공적으로 등록되었습니다!', 'success');
        
        document.getElementById('post-title').value = '';
        document.getElementById('post-content').value = '';
        document.getElementById('post-file').value = '';
        
        writeModal.style.display = 'none';
        
        // 목업 환경에서는 페이지 새로고침 시 동적으로 추가된 내용이 사라집니다.
        // 실제 서비스에서는 서버에서 데이터를 가져와 목록을 갱신합니다.
        setTimeout(() => {
          // location.reload(); // 목업에서는 이 부분을 주석 처리거나, 
                               // 새로운 질문을 목록에 동적으로 추가하는 로직으로 변경할 수 있습니다.
                               // 현재는 사용자의 요청에 따라 최소한으로 변경합니다.
          console.log('질문 등록 완료 (목업). 페이지 새로고침은 주석 처리됨.');
        }, 500);
      } catch (error) {
        console.error('Mock Error:', error);
        showNotification('질문 등록 중 오류가 발생했습니다. 다시 시도해주세요.', 'error');
      } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = '질문 등록하기';
        submitBtn.classList.remove('loading');
      }
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
  // 추천 버튼 기능 추가
  const likeButtons = document.querySelectorAll('.post-like-button');
  likeButtons.forEach(button => {
    button.addEventListener('click', function() {
      // 추천 버튼 클릭 시 효과 추가
      this.classList.add('liked');
      
      // 현재 추천 수 가져오기
      const likeCountSpan = this.querySelector('span');
      const currentLikes = parseInt(likeCountSpan.textContent);
      
      // 추천 수 증가시키기
      likeCountSpan.textContent = currentLikes + 1;
      
      // 메타 정보의 추천수도 업데이트
      const detailMeta = this.closest('.qna-detail').querySelector('.post-detail-likes-count');
      if (detailMeta) {
        detailMeta.textContent = currentLikes + 1;
      }
      
      // 잠시 후 버튼 상태 변경
      setTimeout(() => {
        this.innerHTML = '<i>✓</i> 추천완료 (' + (currentLikes + 1) + ')';
        this.disabled = true;
      }, 300);
      
    });
  });
  
  // 목업 QnA 데이터
  const mockQnaData = [
    {
      id: '1',
      title: '기계식 키보드 입문, 어떤 축이 좋을까요?',
      author: '키린이123',
      createdAt: '2025-05-20',
      views: 152,
      content: '안녕하세요! 최근 기계식 키보드에 관심이 생겨 입문해보려고 합니다. 주로 게임(롤, 배그)과 문서 작업을 하는데, 어떤 축(스위치)을 선택해야 할지 고민입니다. 소음이 너무 크지 않으면서 타건감이 좋은 축을 추천해주세요! 예산은 10~15만원 사이입니다.',
      answerCount: 2,
      answers: [
        { id: 'ans1-1', author: '기키고수', createdAt: '2025-05-20', content: '문서 작업과 게임을 겸하신다면 갈축이나 저소음 적축을 추천드립니다. 갈축은 구분감이 있어 타건의 재미가 있고, 저소음 적축은 조용하면서도 부드러운 타건감을 제공합니다. 해당 예산으로 레오폴드, 바밀로, 덱 같은 브랜드 제품을 알아보시면 좋을 것 같아요.' },
        { id: 'ans1-2', author: '겜돌이77', createdAt: '2025-05-21', content: '배그 하시면 반응속도 빠른 은축도 고려해보세요! 다만, 은축은 입력 지점이 짧아서 오타가 좀 날 수 있습니다. 타건샵에서 직접 쳐보시는 걸 추천해요.' }
      ]
    },
    {
      id: '2',
      title: '커스텀 키보드 윤활, 꼭 해야 하나요?',
      author: '궁금해요',
      createdAt: '2025-05-18',
      views: 88,
      content: '커스텀 키보드를 맞추려고 하는데, 스위치 윤활이 필수인지 궁금합니다. 윤활을 하면 어떤 점이 좋아지고, 안 하면 어떤 단점이 있나요? 초보자도 쉽게 할 수 있는 윤활 방법이 있다면 알려주세요.',
      answerCount: 1,
      answers: [
        { id: 'ans2-1', author: '윤활장인', createdAt: '2025-05-19', content: '필수는 아니지만, 윤활을 하면 스위치의 서걱임이나 스프링 소음을 줄여주고, 타건감을 더 부드럽고 정갈하게 만들어줍니다. 초보자분들은 붓윤활보다는 스프레이 윤활이 간편할 수 있지만, 효과는 붓윤활이 더 좋습니다. 유튜브에 관련 영상 많으니 참고해보세요!' }
      ]
    },
    {
      id: '3',
      title: '무접점 키보드, 기계식과 차이점이 뭔가요?',
      author: '알고싶다',
      createdAt: '2025-05-15',
      views: 230,
      content: '무접점 키보드가 좋다는 얘기를 많이 들었는데, 정확히 기계식 키보드와 어떤 차이가 있는지 궁금합니다. 타건감, 소음, 내구성 등 여러 측면에서 비교 설명해주시면 감사하겠습니다!',
      answerCount: 3,
      answers: [
        { id: 'ans3-1', author: '토프레매니아', createdAt: '2025-05-16', content: '가장 큰 차이는 입력 방식입니다. 기계식은 물리적인 접점으로 입력되지만, 무접점은 정전용량 변화를 감지하여 입력됩니다. 그래서 무접점이 내구성이 더 좋고, 특유의 보글보글하거나 초콜릿 부러뜨리는 듯한 타건감을 가지고 있죠. 대표적으로 리얼포스, 한성무접점 등이 있습니다.' },
        { id: 'ans3-2', author: '앱코사랑', createdAt: '2025-05-16', content: '요즘은 앱코나 COX에서도 가성비 좋은 무접점 키보드가 많이 나와요. 타건감은 개인 취향이 많이 타니, 직접 타건해보시는 게 제일 좋습니다.' },
        { id: 'ans3-3', author: '키보드연구원', createdAt: '2025-05-17', content: '소음 면에서는 무접점이 기계식보다 전반적으로 조용한 편입니다. 물론 기계식도 저소음 축을 사용하면 조용하지만, 무접점 특유의 정숙함이 있죠.' }
      ]
    },
    {
      id: '4',
      title: '키캡놀이 하고 싶은데, 키캡 호환성 어떻게 확인하나요?',
      author: '뉴비개발자',
      createdAt: '2025-05-12',
      views: 75,
      content: '제 키보드에 예쁜 키캡을 끼워주고 싶은데, 아무 키캡이나 다 호환되는 건 아니라고 들었습니다. 어떤 기준으로 호환성을 확인해야 하는지, 주로 사용되는 키캡 프로파일(체리, OEM 등)은 어떤 것들이 있는지 알려주세요.',
      answerCount: 1,
      answers: [
        { id: 'ans4-1', author: '키캡컬렉터', createdAt: '2025-05-13', content: '가장 중요한 건 스위치 종류(MX 호환인지)와 키보드 배열(표준 배열인지, 특수 배열인지)입니다. 스테빌라이저 형태(체리식, 마제식)도 확인해야 하고요. 키캡 프로파일은 높이나 모양에 따라 타건감에 영향을 주니, 선호하는 프로파일을 찾아보시는 것도 좋습니다. 보통 상품 상세페이지에 호환 정보가 나와있으니 잘 확인해보세요.' }
      ]
    },
    {
      id: '5',
      title: '노트북에 연결할 휴대용 기계식 키보드 추천해주세요!',
      author: '맥북유저',
      createdAt: '2025-05-10',
      views: 110,
      content: '카페나 외부에서 노트북으로 작업할 때 사용할 휴대성 좋은 기계식 키보드를 찾고 있습니다. 블루투스 연결이 가능하고, 텐키리스나 미니 배열이면 좋겠습니다. 추천 부탁드려요!',
      answerCount: 0,
      answers: []
    }
  ];

  // 페이지 로드 시 QnA 목록 불러오기
  loadQnaList();

  /**
   * QnA 목록 불러오기
   * @param {Object} params - 페이징 및 필터링 파라미터 (목업에서는 사용 안 함)
   */
  async function loadQnaList(params = {}) {
    try {
      // 로딩 표시
      const qnaListContainer = document.querySelector('.qna-list');
      if (qnaListContainer) {
        qnaListContainer.innerHTML = '<div class="loading">질문 목록을 불러오는 중...</div>';
      }

      // API 호출하여 QnA 목록 가져오기 -> 목업 데이터 사용으로 변경
      // const response = await BoardService.getPosts('qna', params);
      const response = { data: mockQnaData }; // 목업 데이터 사용
      
      // 데이터를 불러오는 데 시간이 걸리는 것처럼 보이게 하기 위한 임시 지연 (0.5초)
      await new Promise(resolve => setTimeout(resolve, 500));

      if (response && response.data && response.data.length > 0) {
        // 컨테이너 초기화
        qnaListContainer.innerHTML = '';
        
        // QnA 목록 렌더링
        response.data.forEach(qna => {
          const qnaElement = document.createElement('div');
          qnaElement.className = 'qna-item';
          qnaElement.setAttribute('id', `qna-${qna.id}`);
          
          // 답변 수 계산
          const answerCount = qna.answerCount || 0;
          const statusClass = answerCount > 0 ? 'status-answered' : 'status-pending';
          const statusText = answerCount > 0 ? '답변완료' : '미답변';
          
          qnaElement.innerHTML = `
            <div class="qna-header">
              <div class="qna-status ${statusClass}">${statusText}</div>
              <h3 class="qna-title">
                <a href="#" data-qna-id="${qna.id}">${qna.title}</a>
              </h3>
            </div>
            <div class="qna-meta">
              <span>작성자: ${qna.author || '익명'}</span>
              <span>작성일: ${qna.createdAt || '-'}</span>
              <span>조회수: ${qna.views || 0}</span>
            </div>
            <div class="qna-detail" style="display: none;">
              <div class="qna-detail-content">${qna.content || ''}</div>
              <div class="qna-detail-answers">
                <!-- 답변은 클릭 시 동적으로 로드됨 -->
              </div>
              <div class="answer-form" data-question-id="${qna.id}">
                <textarea placeholder="답변을 입력하세요..."></textarea>
                <button class="answer-submit-btn">답변 등록</button>
              </div>
            </div>
          `;
          
          qnaListContainer.appendChild(qnaElement);
          
          // 제목 클릭 시 상세 내용 토글 및 답변 로드
          const titleLink = qnaElement.querySelector('.qna-title a');
          if (titleLink) {
            titleLink.addEventListener('click', function(e) {
              e.preventDefault();
              const qnaId = this.getAttribute('data-qna-id');
              toggleQnaDetail(qnaId);
              loadAnswers(qnaId);
            });
          }
        });
      } else {
        qnaListContainer.innerHTML = '<div class="no-data">등록된 질문이 없습니다.</div>';
      }
    } catch (error) {
      console.error('QnA 목록 로드 오류:', error);
      const qnaListContainer = document.querySelector('.qna-list');
      if (qnaListContainer) {
        qnaListContainer.innerHTML = '<div class="error">질문 목록을 불러오는 중 오류가 발생했습니다.</div>';
      }
    }
  }

  /**
   * QnA 상세 내용 토글
   * @param {string} qnaId - QnA ID
   */
  function toggleQnaDetail(qnaId) {
    const qnaItem = document.getElementById(`qna-${qnaId}`);
    if (qnaItem) {
      const detailSection = qnaItem.querySelector('.qna-detail');
      if (detailSection) {
        const isVisible = detailSection.style.display === 'block';
        detailSection.style.display = isVisible ? 'none' : 'block';
        
        // 처음 열릴 때만 조회수 증가 API 호출 (중복 증가 방지)
        if (!isVisible && !detailSection.classList.contains('viewed')) {
          detailSection.classList.add('viewed');
          incrementViewCount(qnaId);
        }
      }
    }
  }
  
  /**
   * QnA 조회수 증가
   * @param {string} qnaId - QnA ID
   */
  async function incrementViewCount(qnaId) {
    try {
      // await BoardService.post(`/qna/view.do`, { id: qnaId });
      console.log(`Mock: Increment view count for QnA ID: ${qnaId}`);
      // 목업 환경에서는 실제 조회수를 업데이트하지 않거나, 로컬 mockQnaData를 직접 수정할 수 있습니다.
      // 예를 들어, mockQnaData에서 해당 id의 views를 1 증가시키는 로직 추가 가능
      const qnaItem = mockQnaData.find(q => q.id === qnaId);
      if (qnaItem) {
        qnaItem.views = (qnaItem.views || 0) + 1;
        // 화면에 표시된 조회수도 업데이트 (선택 사항)
        const viewCountElement = document.querySelector(`#qna-${qnaId} .qna-meta span:nth-child(3)`);
        if (viewCountElement) {
          viewCountElement.textContent = `조회수: ${qnaItem.views}`;
        }
      }
    } catch (error) {
      console.error('조회수 증가 오류 (목업):', error);
      // 조회수 증가 실패해도 사용자 경험에 큰 영향이 없으므로 오류 표시하지 않음
    }
  }

  /**
   * QnA 답변 목록 불러오기
   * @param {string} qnaId - QnA ID
   */
  async function loadAnswers(qnaId) {
    try {
      const answersContainer = document.querySelector(`#qna-${qnaId} .qna-detail-answers`);
      if (!answersContainer) return;
      
      // 이미 로드된 경우 스킵
      if (answersContainer.getAttribute('data-loaded') === 'true') return;
      
      // 로딩 표시
      answersContainer.innerHTML = '<div class="loading">답변을 불러오는 중...</div>';
      
      // API 호출하여 답변 목록 가져오기 -> 목업 데이터 사용으로 변경
      // const response = await BoardService.get(`/answer/list.do`, { questionId: qnaId });
      const qnaItem = mockQnaData.find(q => q.id === qnaId);
      const response = { data: qnaItem ? qnaItem.answers : [] }; // 해당 질문의 목업 답변 사용

      // 데이터를 불러오는 데 시간이 걸리는 것처럼 보이게 하기 위한 임시 지연 (0.3초)
      await new Promise(resolve => setTimeout(resolve, 300));

      if (response && response.data && response.data.length > 0) {
        // 컨테이너 초기화
        answersContainer.innerHTML = '';
        
        // 답변 목록 렌더링
        response.data.forEach(answer => {
          const answerElement = document.createElement('div');
          answerElement.className = 'answer';
          answerElement.innerHTML = `
            <div class="answer-meta">
              <span>작성자: ${answer.author || '익명'}</span>
              <span>작성일: ${answer.createdAt || '-'}</span>
            </div>
            <div class="answer-content">
              <p>${answer.content || ''}</p>
            </div>
          `;
          
          answersContainer.appendChild(answerElement);
        });
        
        // 로드 완료 표시
        answersContainer.setAttribute('data-loaded', 'true');
      } else {
        answersContainer.innerHTML = '<div class="no-answers">등록된 답변이 없습니다.</div>';
      }
    } catch (error) {
      console.error('답변 목록 로드 오류:', error);
      const answersContainer = document.querySelector(`#qna-${qnaId} .qna-detail-answers`);
      if (answersContainer) {
        answersContainer.innerHTML = '<div class="error">답변을 불러오는 중 오류가 발생했습니다.</div>';
      }
    }
  }
});
