// 게시판 스크립트
document.addEventListener('DOMContentLoaded', function() {
  // 게시판 탭 초기화
  setupBoardTabs();
  
  // 게시판 목록 로드
  loadBoardLists();
  
  // 게시글 상세 보기 이벤트 설정
  setupPostDetailEvents();
  
  // 게시글 작성 폼 이벤트 설정
  setupPostFormEvents();
  
  // 검색 기능 설정
  setupSearchEvents();
  
  // 댓글 기능 설정
  setupCommentEvents();
});

/**
 * 게시판 탭 설정
 */
function setupBoardTabs() {
  const boardLinks = document.querySelectorAll('.board-nav a');
  const boardContents = document.querySelectorAll('.board-content');
  const boardBottomContainer = document.getElementById('board-bottom-container');
  const postDetailView = document.getElementById('post-detail-view');
  
  // 초기 활성 탭 ID 설정
  let previouslyActiveBoardId = 'free-board'; // 기본값 설정
  
  const initialActiveTabLink = document.querySelector('.board-nav a.active');
  if (initialActiveTabLink) {
    const boardName = initialActiveTabLink.dataset.board;
    if (boardName) {
      previouslyActiveBoardId = boardName + '-board';
    }
  }
  
  // 모든 게시판 콘텐츠 숨기기 (상세보기는 별도 제어)
  function hideBoardLists() {
    boardContents.forEach(content => {
      content.style.display = 'none';
    });
    
    if (boardBottomContainer) {
      boardBottomContainer.style.display = 'none';
    }
  }
  
  // 특정 게시판 목록 및 하단부 보여주기
  function showBoardList(boardIdToShow) {
    hideBoardLists(); // 일단 모든 목록 숨김
    
    if (postDetailView) {
      postDetailView.style.display = 'none'; // 상세 보기도 숨김
    }
    
    const boardToShow = document.getElementById(boardIdToShow);
    if (boardToShow) {
      boardToShow.style.display = 'block';
    }
    
    if (boardBottomContainer) {
      boardBottomContainer.style.display = 'block';
    }
    
    // URL 업데이트
    const boardType = boardIdToShow.replace('-board', '');
    const url = new URL(window.location);
    url.searchParams.set('board', boardType);
    window.history.pushState({}, '', url);
    
    // 게시글 목록 로드
    loadPosts(boardType);
  }
  
  // 탭 클릭 이벤트
  boardLinks.forEach(link => {
    link.addEventListener('click', function(e) {
      e.preventDefault();
      
      // 모든 탭에서 active 클래스 제거
      boardLinks.forEach(l => l.classList.remove('active'));
      
      // 클릭한 탭에 active 클래스 추가
      this.classList.add('active');
      
      // 게시판 이름 가져오기
      const boardName = this.dataset.board;
      if (boardName) {
        const boardId = boardName + '-board';
        showBoardList(boardId);
        previouslyActiveBoardId = boardId;
      }
    });
  });
  
  // 초기 활성 탭 표시
  showBoardList(previouslyActiveBoardId);
}

/**
 * 게시판 목록 로드
 */
function loadBoardLists() {
  // URL에서 게시판 타입 확인
  const urlParams = new URLSearchParams(window.location.search);
  const boardType = urlParams.get('board');
  
  if (boardType) {
    // 해당 게시판 탭 활성화
    const boardTab = document.querySelector(`.board-nav a[data-board="${boardType}"]`);
    if (boardTab) {
      boardTab.click();
    }
  }
  
  // URL에 게시글 ID가 있으면 상세 페이지 바로 열기
  const postId = urlParams.get('postId');
  if (postId && boardType) {
    loadPostDetail(boardType, postId);
  }
}

/**
 * 게시글 목록 로드
 * @param {string} boardType 게시판 타입
 * @param {Object} params 검색 및 페이징 파라미터
 */
async function loadPosts(boardType, params = {}) {
  // 게시판 콘텐츠 컨테이너 찾기
  const boardContainer = document.getElementById(`${boardType}-board`);
  if (!boardContainer) return;
  
  // 게시글 목록 컨테이너
  const postsContainer = boardContainer.querySelector('.post-list');
  if (!postsContainer) return;
  
  // 로딩 상태 표시
  postsContainer.innerHTML = `
    <div class="loading-indicator">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">로딩 중...</span>
      </div>
      <p>게시글을 불러오는 중입니다...</p>
    </div>
  `;
  
  try {
    // URL 매개변수 가져오기
    const urlParams = new URLSearchParams(window.location.search);
    const urlSearch = urlParams.get('search');
    const urlCategory = urlParams.get('category');
    const urlSort = urlParams.get('sort');
    const urlPage = urlParams.get('page');
    
    // URL 매개변수로 파라미터 설정
    if (urlSearch) {
      const searchInput = document.querySelector(`#${boardType}-search-input`);
      if (searchInput) {
        searchInput.value = urlSearch;
      }
      params.search = urlSearch;
    }
    
    if (urlCategory) {
      params.category = urlCategory;
      // 카테고리 필터 UI 업데이트
      updateCategoryFilterUI(boardType, urlCategory);
    }
    
    if (urlSort) {
      params.sort = urlSort;
      // 정렬 선택 UI 업데이트
      const sortSelect = document.querySelector(`#${boardType}-sort-select`);
      if (sortSelect) {
        sortSelect.value = urlSort;
      }
    }
    
    if (urlPage) {
      params.page = parseInt(urlPage);
    }
    
    // 캐시 키 생성
    const cacheKey = `board_posts_${boardType}_${JSON.stringify(params)}`;
    const cachedData = sessionStorage.getItem(cacheKey);
    
    let result;
    
    // 캐시된 데이터가 있으면 사용 (20초 내)
    if (cachedData) {
      const parsedCache = JSON.parse(cachedData);
      const cacheAge = Date.now() - parsedCache.timestamp;
      
      if (cacheAge < 20000) { // 20초
        console.log('캐시된 게시글 목록 사용');
        result = parsedCache.data;
      }
    }
    
    // 캐시가 없거나 오래된 경우 API 호출
    if (!result) {
      console.log('게시글 목록 API 호출');
      result = await BoardService.getPosts(boardType, params);
      
      // 결과 캐싱
      sessionStorage.setItem(cacheKey, JSON.stringify({
        timestamp: Date.now(),
        data: result
      }));
    }
    
    // 게시글 목록 렌더링
    renderPosts(boardType, result.posts || []);
    
    // 페이지네이션 업데이트
    updatePagination(boardType, result.pagination);
    
    // 새 글 작성 버튼 상태 업데이트
    updateWriteButtonState(boardType);
  } catch (error) {
    console.error(`${boardType} 게시글 로드 오류:`, error);
    
    // 오류 메시지 세분화
    let errorMessage = '게시글을 불러오는 중 오류가 발생했습니다.';
    if (error.message.includes('network') || error.message.includes('timeout')) {
      errorMessage = '네트워크 연결을 확인하고 다시 시도해주세요.';
    }
    
    postsContainer.innerHTML = `
      <div class="error-message">
        ${errorMessage}
        <button class="btn btn-sm btn-outline-primary mt-2 retry-board-load">다시 시도</button>
      </div>
    `;
    
    // 재시도 버튼 이벤트 설정
    const retryButton = postsContainer.querySelector('.retry-board-load');
    if (retryButton) {
      retryButton.addEventListener('click', () => {
        loadPosts(boardType, params);
      });
    }
  }
}

/**
 * 게시글 목록 렌더링
 * @param {string} boardType 게시판 타입
 * @param {Array} posts 게시글 배열
 */
function renderPosts(boardType, posts) {
  const boardContainer = document.getElementById(`${boardType}-board`);
  if (!boardContainer) return;
  
  const postsContainer = boardContainer.querySelector('.post-list');
  if (!postsContainer) return;
  
  // 게시글이 없는 경우
  if (!posts || posts.length === 0) {
    postsContainer.innerHTML = `
      <div class="no-posts">
        <p>등록된 게시글이 없습니다.</p>
      </div>
    `;
    return;
  }
  
  // 게시판 유형에 따른 테이블 헤더
  let tableHeader = '';
  if (boardType === 'free' || boardType === 'qna') {
    tableHeader = `
      <thead>
        <tr>
          <th class="post-num">번호</th>
          <th class="post-title">제목</th>
          <th class="post-author">작성자</th>
          <th class="post-date">작성일</th>
          <th class="post-view">조회</th>
        </tr>
      </thead>
    `;
  } else if (boardType === 'news') {
    tableHeader = `
      <thead>
        <tr>
          <th class="post-num">번호</th>
          <th class="post-title">뉴스 제목</th>
          <th class="post-source">출처</th>
          <th class="post-date">작성일</th>
          <th class="post-view">조회</th>
        </tr>
      </thead>
    `;
  }
  
  // 게시글 목록 생성
  let tableRows = '';
  posts.forEach(post => {
    let row = '';
    if (boardType === 'free' || boardType === 'qna') {
      // 댓글 수 표시
      const commentCount = post.commentCount ? `<span class="comment-count">[${post.commentCount}]</span>` : '';
      
      // 답변 완료 표시 (qna 게시판)
      const answerBadge = boardType === 'qna' && post.answered ? '<span class="answer-badge">답변완료</span>' : '';
      
      row = `
        <tr data-post-id="${post.id}" data-board-type="${boardType}">
          <td class="post-num">${post.id}</td>
          <td class="post-title">
            <a href="#" class="post-link">
              ${post.title} ${commentCount} ${answerBadge}
            </a>
          </td>
          <td class="post-author">${post.author}</td>
          <td class="post-date">${formatDate(post.createdAt)}</td>
          <td class="post-view">${post.viewCount || 0}</td>
        </tr>
      `;
    } else if (boardType === 'news') {
      row = `
        <tr data-post-id="${post.id}" data-board-type="${boardType}">
          <td class="post-num">${post.id}</td>
          <td class="post-title">
            <a href="#" class="post-link">
              ${post.title} ${post.commentCount ? `<span class="comment-count">[${post.commentCount}]</span>` : ''}
            </a>
          </td>
          <td class="post-source">${post.source || '-'}</td>
          <td class="post-date">${formatDate(post.createdAt)}</td>
          <td class="post-view">${post.viewCount || 0}</td>
        </tr>
      `;
    }
    
    tableRows += row;
  });
  
  // 테이블 생성
  postsContainer.innerHTML = `
    <table class="post-table">
      ${tableHeader}
      <tbody>${tableRows}</tbody>
    </table>
  `;
  
  // 게시글 클릭 이벤트 설정
  postsContainer.querySelectorAll('.post-link').forEach(link => {
    link.addEventListener('click', function(e) {
      e.preventDefault();
      
      const postRow = this.closest('tr');
      const postId = postRow.dataset.postId;
      const boardType = postRow.dataset.boardType;
      
      loadPostDetail(boardType, postId);
    });
  });
}

/**
 * 게시글 상세 정보 로드
 * @param {string} boardType 게시판 타입
 * @param {number} postId 게시글 ID
 */
async function loadPostDetail(boardType, postId) {
  // 상세 보기 컨테이너
  const postDetailView = document.getElementById('post-detail-view');
  if (!postDetailView) return;
  
  // 상세 정보 요소
  const detailTitle = document.getElementById('detail-title');
  const detailAuthorContainer = document.getElementById('detail-author-container');
  const detailAuthor = document.getElementById('detail-author');
  const detailDateContainer = document.getElementById('detail-date-container');
  const detailDate = document.getElementById('detail-date');
  const detailContent = document.getElementById('detail-content');
  const detailCommentList = document.getElementById('detail-comment-list');
  
  // 로딩 상태 표시
  postDetailView.style.display = 'block';
  detailContent.innerHTML = `
    <div class="loading-indicator">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">로딩 중...</span>
      </div>
      <p>게시글을 불러오는 중입니다...</p>
    </div>
  `;
  
  try {
    // URL 업데이트
    const url = new URL(window.location);
    url.searchParams.set('board', boardType);
    url.searchParams.set('postId', postId);
    window.history.pushState({}, '', url);
      // 캐시 키 생성
    const cacheKey = `board_post_detail_${boardType}_${postId}`;
    const cachedData = sessionStorage.getItem(cacheKey);
    
    let post;
    
    // 캐시된 데이터가 있으면 사용 (30초 내)
    if (cachedData) {
      const parsedCache = JSON.parse(cachedData);
      const cacheAge = Date.now() - parsedCache.timestamp;
      
      if (cacheAge < 30000) { // 30초
        console.log('캐시된 게시글 상세 정보 사용');
        post = parsedCache.data;
      }
    }
    
    // 캐시가 없거나 오래된 경우 API 호출
    if (!post) {
      console.log('게시글 상세 정보 API 호출');
      post = await BoardService.getPostDetail(boardType, postId);
      
      // 결과 캐싱
      sessionStorage.setItem(cacheKey, JSON.stringify({
        timestamp: Date.now(),
        data: post
      }));
    }
    
    // 상세 정보 업데이트
    detailTitle.textContent = post.title || '';
    
    if (detailAuthorContainer && detailAuthor) {
      if (post.author) {
        detailAuthorContainer.style.display = '';
        detailAuthor.textContent = post.author;
        
        // 작성자의 등급이나 역할에 따른 뱃지 표시
        if (post.authorRole) {
          const roleBadge = document.createElement('span');
          roleBadge.className = `role-badge role-${post.authorRole.toLowerCase()}`;
          roleBadge.textContent = getRoleBadgeText(post.authorRole);
          detailAuthor.appendChild(roleBadge);
        }
      } else {
        detailAuthorContainer.style.display = 'none';
      }
    }
    
    if (detailDateContainer && detailDate) {
      if (post.createdAt) {
        detailDateContainer.style.display = '';
        detailDate.textContent = formatDate(post.createdAt, true);
        
        // 수정일이 있으면 추가 표시
        if (post.updatedAt && post.updatedAt !== post.createdAt) {
          const updatedSpan = document.createElement('span');
          updatedSpan.className = 'updated-time';
          updatedSpan.textContent = ` (${formatDate(post.updatedAt, false)}에 수정됨)`;
          detailDate.appendChild(updatedSpan);
        }
      } else {
        detailDateContainer.style.display = 'none';
      }
    }
    
    // 게시글 내용 업데이트 (HTML 허용 - XSS 방지는 서버에서 처리됨을 가정)
    if (detailContent) {
      detailContent.innerHTML = post.content || '';
      
      // 첨부파일이 있으면 표시
      if (post.attachments && post.attachments.length > 0) {
        const attachmentsList = document.createElement('div');
        attachmentsList.className = 'post-attachments';
        
        const attachmentsTitle = document.createElement('h5');
        attachmentsTitle.textContent = '첨부파일';
        attachmentsList.appendChild(attachmentsTitle);
        
        const attachmentUl = document.createElement('ul');
        post.attachments.forEach(attachment => {
          const li = document.createElement('li');
          const link = document.createElement('a');
          link.href = attachment.url;
          link.textContent = attachment.filename;
          link.target = '_blank';
          li.appendChild(link);
          attachmentUl.appendChild(li);
        });
        
        attachmentsList.appendChild(attachmentUl);
        detailContent.appendChild(attachmentsList);
      }
    }
    
    // 댓글 로드
    if (detailCommentList) {
      loadComments(boardType, postId);
    }
    
    // 게시판 목록 숨기기
    document.querySelectorAll('.board-content').forEach(content => {
      content.style.display = 'none';
    });
    
    const boardBottomContainer = document.getElementById('board-bottom-container');
    if (boardBottomContainer) {
      boardBottomContainer.style.display = 'none';
    }
    
    // QnA 답변 표시 (qna 게시판인 경우)
    if (boardType === 'qna' && post.answer) {
      const answerContainer = document.getElementById('qna-answer-container');
      const answerContent = document.getElementById('qna-answer-content');
      const answerDate = document.getElementById('qna-answer-date');
      
      if (answerContainer && answerContent) {
        answerContainer.style.display = 'block';
        answerContent.innerHTML = post.answer.content || '';
        
        if (answerDate && post.answer.createdAt) {
          answerDate.textContent = formatDate(post.answer.createdAt, true);
        }
      }
    }
  } catch (error) {
    // 게시글 상세 조회 오류 처리
    handlePostDetailError(detailContent, error, boardType, postId);
  }
}

/**
 * 사용자 역할에 따른 뱃지 텍스트 반환
 * @param {string} role 사용자 역할
 * @returns {string} 뱃지 텍스트
 */
function getRoleBadgeText(role) {
  switch(role.toUpperCase()) {
    case 'ADMIN':
      return '관리자';
    case 'MANAGER':
      return '운영자';
    case 'VERIFIED':
      return '인증회원';
    case 'USER':
      return '회원';
    default:
      return role;
  }
}

/**
 * 게시글 상세 조회 오류 처리
 * @param {HTMLElement} container 오류 표시 컨테이너
 * @param {Error} error 발생한 오류
 * @param {string} boardType 게시판 타입
 * @param {number} postId 게시글 ID
 */
function handlePostDetailError(container, error, boardType, postId) {
  console.error('게시글 상세 정보 로드 오류:', error);
  
  let errorMessage = '게시글을 불러오는 중 오류가 발생했습니다.';
  
  // 오류 유형에 따른 메시지
  if (error.message.includes('not found') || error.message.includes('404')) {
    errorMessage = '존재하지 않는 게시글이거나 삭제되었습니다.';
  } else if (error.message.includes('permission') || error.message.includes('403')) {
    errorMessage = '이 게시글을 볼 수 있는 권한이 없습니다.';
  } else if (error.message.includes('network') || error.message.includes('timeout')) {
    errorMessage = '네트워크 연결을 확인해주세요.';
  }
  
  container.innerHTML = `
    <div class="error-message alert alert-danger">
      <p>${errorMessage}</p>
      <div class="mt-3">
        <button class="btn btn-sm btn-outline-primary retry-post-load">다시 시도</button>
        <button class="btn btn-sm btn-outline-secondary return-to-list">목록으로 돌아가기</button>
      </div>
    </div>
  `;
  
  // 재시도 버튼 이벤트
  container.querySelector('.retry-post-load')?.addEventListener('click', () => {
    loadPostDetail(boardType, postId);
  });
  
  // 목록으로 돌아가기 버튼 이벤트
  container.querySelector('.return-to-list')?.addEventListener('click', () => {
    // 상세 보기 숨기기
    document.getElementById('post-detail-view').style.display = 'none';
    
    // URL에서 postId 파라미터 제거
    const url = new URL(window.location);
    url.searchParams.delete('postId');
    window.history.pushState({}, '', url);
    
    // 현재 활성화된 게시판 탭 찾기
    const activeTab = document.querySelector('.board-nav a.active');
    if (activeTab) {
      activeTab.click();
    }
  });
}

/**
 * 게시글 상세 보기 관련 이벤트 설정
 */
function setupPostDetailEvents() {
  // 상세 보기 닫기 버튼
  const postDetailCloseBtn = document.querySelector('#post-detail-view .post-detail-close');
  if (postDetailCloseBtn) {
    postDetailCloseBtn.addEventListener('click', function() {
      // 상세 보기 숨김
      document.getElementById('post-detail-view').style.display = 'none';
      
      // URL에서 postId 파라미터 제거
      const url = new URL(window.location);
      url.searchParams.delete('postId');
      window.history.pushState({}, '', url);
      
      // 현재 활성화된 게시판 탭 찾기
      const activeTab = document.querySelector('.board-nav a.active');
      if (activeTab) {
        activeTab.click();
      }
    });
  }
  
  // 뒤로가기 이벤트
  window.addEventListener('popstate', function() {
    const urlParams = new URLSearchParams(window.location.search);
    const boardType = urlParams.get('board');
    const postId = urlParams.get('postId');
    
    if (boardType) {
      // 게시판 탭 활성화
      const boardTab = document.querySelector(`.board-nav a[data-board="${boardType}"]`);
      if (boardTab) {
        boardTab.click();
        
        // 게시글 ID가 있으면 상세 페이지 열기
        if (postId) {
          loadPostDetail(boardType, postId);
        }
      }
    }
  });
  
  // 게시글 삭제 버튼
  const deletePostBtn = document.getElementById('delete-post-btn');
  if (deletePostBtn) {
    deletePostBtn.addEventListener('click', async function() {
      // 로그인 확인
      if (!Auth.isLoggedIn()) {
        alert('로그인이 필요합니다.');
        return;
      }
      
      const urlParams = new URLSearchParams(window.location.search);
      const boardType = urlParams.get('board');
      const postId = urlParams.get('postId');
      
      if (!boardType || !postId) return;
      
      if (confirm('정말 게시글을 삭제하시겠습니까?')) {
        try {
          await BoardService.deletePost(boardType, postId);
          alert('게시글이 삭제되었습니다.');
          
          // 게시글 목록으로 이동
          const url = new URL(window.location);
          url.searchParams.delete('postId');
          window.history.pushState({}, '', url);
          
          // 현재 게시판 다시 로드
          const activeTab = document.querySelector('.board-nav a.active');
          if (activeTab) {
            activeTab.click();
          }
        } catch (error) {
          console.error('게시글 삭제 오류:', error);
          alert('게시글 삭제 중 오류가 발생했습니다.');
        }
      }
    });
  }
  
  // 게시글 수정 버튼
  const editPostBtn = document.getElementById('edit-post-btn');
  if (editPostBtn) {
    editPostBtn.addEventListener('click', function() {
      // 로그인 확인
      if (!Auth.isLoggedIn()) {
        alert('로그인이 필요합니다.');
        return;
      }
      
      const urlParams = new URLSearchParams(window.location.search);
      const boardType = urlParams.get('board');
      const postId = urlParams.get('postId');
      
      if (!boardType || !postId) return;
      
      // 수정 페이지로 이동
      window.location.href = `/view/board/write.html?board=${boardType}&edit=${postId}`;
    });
  }
}

/**
 * 게시글 작성 폼 이벤트 설정
 */
function setupPostFormEvents() {
  const writeForm = document.getElementById('post-write-form');
  if (!writeForm) return;
  
  // URL에서 게시판 타입 및 수정 모드 확인
  const urlParams = new URLSearchParams(window.location.search);
  const boardType = urlParams.get('board');
  const editPostId = urlParams.get('edit');
  
  // 게시판 타입 설정
  if (boardType) {
    const boardTypeSelect = document.getElementById('board-type');
    if (boardTypeSelect) {
      boardTypeSelect.value = boardType;
    }
  }
  
  // 수정 모드인 경우 게시글 내용 불러오기
  if (editPostId) {
    loadPostForEditing(boardType, editPostId);
  }
  
  // 폼 제출 이벤트
  writeForm.addEventListener('submit', async function(e) {
    e.preventDefault();
    
    // 로그인 확인
    if (!Auth.isLoggedIn()) {
      alert('로그인이 필요합니다.');
      return;
    }
    
    // 입력값 검증
    const form = e.target;
    const selectedBoardType = form.boardType.value;
    const title = form.title.value.trim();
    const content = form.content.value.trim();
    
    if (!title) {
      alert('제목을 입력해주세요.');
      form.title.focus();
      return;
    }
    
    if (!content) {
      alert('내용을 입력해주세요.');
      form.content.focus();
      return;
    }
    
    // 게시글 데이터
    const postData = {
      title,
      content
    };
    
    try {
      // 로딩 상태 표시
      const submitButton = form.querySelector('button[type="submit"]');
      submitButton.disabled = true;
      submitButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 처리 중...';
      
      let result;
      
      if (editPostId) {
        // 게시글 수정
        result = await BoardService.updatePost(selectedBoardType, editPostId, postData);
        alert('게시글이 수정되었습니다.');
      } else {
        // 게시글 작성
        result = await BoardService.createPost(selectedBoardType, postData);
        alert('게시글이 등록되었습니다.');
      }
      
      // 게시글 상세 페이지로 이동
      window.location.href = `/view/board/index.html?board=${selectedBoardType}&postId=${editPostId || result.postId}`;
    } catch (error) {
      console.error('게시글 저장 오류:', error);
      alert('게시글 저장 중 오류가 발생했습니다.');
    } finally {
      // 버튼 상태 복원
      const submitButton = form.querySelector('button[type="submit"]');
      submitButton.disabled = false;
      submitButton.innerHTML = editPostId ? '수정하기' : '등록하기';
    }
  });
}

/**
 * 수정할 게시글 정보 불러오기
 * @param {string} boardType 게시판 타입
 * @param {number} postId 게시글 ID
 */
async function loadPostForEditing(boardType, postId) {
  try {
    // 게시글 상세 정보 가져오기
    const post = await BoardService.getPostDetail(boardType, postId);
    
    // 폼 필드 업데이트
    const titleInput = document.getElementById('post-title');
    const contentTextarea = document.getElementById('post-content');
    
    if (titleInput) {
      titleInput.value = post.title || '';
    }
    
    if (contentTextarea) {
      contentTextarea.value = post.content || '';
    }
    
    // 폼 제목 변경
    const formTitle = document.querySelector('.write-form-title');
    if (formTitle) {
      formTitle.textContent = '게시글 수정';
    }
    
    // 버튼 텍스트 변경
    const submitButton = document.querySelector('#post-write-form button[type="submit"]');
    if (submitButton) {
      submitButton.textContent = '수정하기';
    }
  } catch (error) {
    console.error('게시글 정보 로드 오류:', error);
    alert('게시글 정보를 불러오는데 실패했습니다.');
    
    // 작성 페이지로 이동
    window.location.href = `/view/board/write.html?board=${boardType}`;
  }
}

/**
 * 댓글 목록 로드
 * @param {string} boardType 게시판 타입
 * @param {number} postId 게시글 ID
 * @param {Object} params 페이징 파라미터
 */
async function loadComments(boardType, postId, params = {}) {
  // 댓글 목록 컨테이너
  const commentList = document.getElementById('detail-comment-list');
  const commentCount = document.getElementById('detail-comment-count');
  
  if (!commentList) return;
  
  // 로딩 상태 표시
  commentList.innerHTML = `
    <div class="loading-indicator">
      <div class="spinner-border spinner-border-sm" role="status">
        <span class="visually-hidden">로딩 중...</span>
      </div>
      <span>댓글을 불러오는 중입니다...</span>
    </div>
  `;
  
  try {
    // 캐시 키 생성
    const cacheKey = `board_comments_${boardType}_${postId}_${JSON.stringify(params)}`;
    const cachedData = sessionStorage.getItem(cacheKey);
    
    let result;
    
    // 캐시된 데이터가 있으면 사용 (10초 내)
    if (cachedData) {
      const parsedCache = JSON.parse(cachedData);
      const cacheAge = Date.now() - parsedCache.timestamp;
      
      if (cacheAge < 10000) { // 10초
        console.log('캐시된 댓글 목록 사용');
        result = parsedCache.data;
      }
    }
    
    // 캐시가 없거나 오래된 경우 API 호출
    if (!result) {
      console.log('댓글 목록 API 호출');
      result = await BoardService.getComments(boardType, postId, params);
      
      // 결과 캐싱
      sessionStorage.setItem(cacheKey, JSON.stringify({
        timestamp: Date.now(),
        data: result
      }));
    }
    
    // 댓글 수 업데이트
    if (commentCount) {
      commentCount.textContent = result.totalCount || 0;
    }
    
    // 댓글 작성 폼 상태 갱신
    updateCommentForm(boardType, postId);
    
    // 댓글 목록 렌더링
    renderComments(boardType, postId, result.comments || []);
    
    // 댓글 페이지네이션 업데이트
    updateCommentPagination(boardType, postId, result.pagination);
  } catch (error) {
    console.error('댓글 로드 오류:', error);
    let errorMessage = '댓글을 불러오는 중 오류가 발생했습니다.';
    
    if (error.message.includes('network') || error.message.includes('timeout')) {
      errorMessage = '네트워크 연결을 확인해주세요.';
    }
    
    commentList.innerHTML = `
      <div class="error-message">
        ${errorMessage}
        <button class="btn btn-sm btn-outline-primary mt-2 retry-comments-load">다시 시도</button>
      </div>
    `;
    
    // 재시도 버튼 이벤트
    const retryButton = commentList.querySelector('.retry-comments-load');
    if (retryButton) {
      retryButton.addEventListener('click', () => {
        loadComments(boardType, postId, params);
      });
    }
  }
}

/**
 * 댓글 목록 렌더링
 * @param {string} boardType 게시판 타입
 * @param {number} postId 게시글 ID
 * @param {Array} comments 댓글 배열
 */
function renderComments(boardType, postId, comments) {
  const commentList = document.getElementById('detail-comment-list');
  if (!commentList) return;
  
  // 댓글이 없는 경우
  if (!comments || comments.length === 0) {
    commentList.innerHTML = `
      <div class="no-comments">
        <p>댓글이 없습니다.</p>
      </div>
    `;
    return;
  }
  
  // 댓글 목록 HTML 생성
  let commentsHtml = '';
  
  comments.forEach(comment => {
    // 현재 사용자인지 확인 (댓글 작성자)
    const isCurrentUser = Auth.isLoggedIn() && comment.isAuthor;
    
    // 댓글 HTML 생성
    commentsHtml += `
      <div class="comment-item" data-comment-id="${comment.id}">
        <div class="comment-header">
          <span class="comment-author">${comment.author}</span>
          <span class="comment-date">${formatDate(comment.createdAt)}</span>
        </div>
        <div class="comment-content">${comment.content}</div>
        <div class="comment-actions">
          ${isCurrentUser ? `
            <button class="btn btn-sm btn-edit-comment" data-comment-id="${comment.id}">수정</button>
            <button class="btn btn-sm btn-delete-comment" data-comment-id="${comment.id}">삭제</button>
          ` : ''}

          <button class="btn btn-sm btn-reply-comment" data-comment-id="${comment.id}">답글</button>
        </div>
        
        ${comment.replies && comment.replies.length > 0 ? `
          <div class="comment-replies">
            ${comment.replies.map(reply => `
              <div class="reply-item" data-reply-id="${reply.id}">
                <div class="reply-header">
                  <span class="reply-author">${reply.author}</span>
                  <span class="reply-date">${formatDate(reply.createdAt)}</span>
                </div>
                <div class="reply-content">${reply.content}</div>
                <div class="reply-actions">
                  ${Auth.isLoggedIn() && reply.isAuthor ? `
                    <button class="btn btn-sm btn-edit-reply" data-reply-id="${reply.id}">수정</button>
                    <button class="btn btn-sm btn-delete-reply" data-reply-id="${reply.id}">삭제</button>
                  ` : ''}
                </div>
              </div>
            `).join('')}

          </div>
        ` : ''}

        <div class="reply-form-container" id="reply-form-${comment.id}" style="display: none;">
          <form class="reply-form" data-comment-id="${comment.id}">
            <textarea class="form-control reply-input" placeholder="답글 내용을 입력하세요." required></textarea>
            <div class="reply-form-buttons">
              <button type="submit" class="btn btn-primary btn-sm">등록</button>
              <button type="button" class="btn btn-secondary btn-sm btn-cancel-reply">취소</button>
            </div>
          </form>
        </div>
      </div>
    `;
  });
  
  // 댓글 목록 업데이트
  commentList.innerHTML = commentsHtml;
  
  // 댓글 이벤트 설정
  setupCommentItemEvents(boardType, postId);
}

/**
 * 댓글 항목 이벤트 설정
 * @param {string} boardType 게시판 타입
 * @param {number} postId 게시글 ID
 */
function setupCommentItemEvents(boardType, postId) {
  // 댓글 수정 버튼
  document.querySelectorAll('.btn-edit-comment').forEach(button => {
    button.addEventListener('click', function() {
      const commentId = this.dataset.commentId;
      const commentItem = document.querySelector(`.comment-item[data-comment-id="${commentId}"]`);
      const commentContent = commentItem.querySelector('.comment-content');
      
      // 현재 내용 가져오기
      const currentContent = commentContent.textContent;
      
      // 수정 폼으로 변경
      commentContent.innerHTML = `
        <form class="edit-comment-form" data-comment-id="${commentId}">
          <textarea class="form-control comment-edit-input" required>${currentContent}</textarea>
          <div class="edit-form-buttons">
            <button type="submit" class="btn btn-primary btn-sm">수정</button>
            <button type="button" class="btn btn-secondary btn-sm btn-cancel-edit">취소</button>
          </div>
        </form>
      `;
      
      // 수정 취소 버튼
      commentItem.querySelector('.btn-cancel-edit').addEventListener('click', function() {
        commentContent.textContent = currentContent;
      });
      
      // 수정 폼 제출
      commentItem.querySelector('.edit-comment-form').addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const updatedContent = this.querySelector('.comment-edit-input').value.trim();
        if (!updatedContent) return;
        
        try {
          await BoardService.updateComment(boardType, postId, commentId, { content: updatedContent });
          
          // 댓글 내용 업데이트
          commentContent.textContent = updatedContent;
        } catch (error) {
          console.error('댓글 수정 오류:', error);
          alert('댓글 수정 중 오류가 발생했습니다.');
        }
      });
    });
  });
  
  // 댓글 삭제 버튼
  document.querySelectorAll('.btn-delete-comment').forEach(button => {
    button.addEventListener('click', async function() {
      if (!confirm('댓글을 삭제하시겠습니까?')) return;
      
      const commentId = this.dataset.commentId;
      
      try {
        await BoardService.deleteComment(boardType, postId, commentId);
        
        // 댓글 목록 다시 로드
        loadComments(boardType, postId);
      } catch (error) {
        console.error('댓글 삭제 오류:', error);
        alert('댓글 삭제 중 오류가 발생했습니다.');
      }
    });
  });
  
  // 답글 버튼
  document.querySelectorAll('.btn-reply-comment').forEach(button => {
    button.addEventListener('click', function() {
      // 로그인 확인
      if (!Auth.isLoggedIn()) {
        alert('로그인이 필요합니다.');
        return;
      }
      
      const commentId = this.dataset.commentId;
      const replyFormContainer = document.getElementById(`reply-form-${commentId}`);
      
      // 다른 답글 폼 모두 닫기
      document.querySelectorAll('.reply-form-container').forEach(container => {
        if (container.id !== `reply-form-${commentId}`) {
          container.style.display = 'none';
        }
      });
      
      // 답글 폼 토글
      replyFormContainer.style.display = replyFormContainer.style.display === 'none' ? 'block' : 'none';
    });
  });
  
  // 답글 취소 버튼
  document.querySelectorAll('.btn-cancel-reply').forEach(button => {
    button.addEventListener('click', function() {
      const replyForm = this.closest('.reply-form-container');
      replyForm.style.display = 'none';
    });
  });
  
  // 답글 폼 제출
  document.querySelectorAll('.reply-form').forEach(form => {
    form.addEventListener('submit', async function(e) {
      e.preventDefault();
      
      const commentId = this.dataset.commentId;
      const replyInput = this.querySelector('.reply-input');
      const content = replyInput.value.trim();
      
      if (!content) return;
      
      try {
        // 답글 제출
        await BoardService.addReply(boardType, postId, commentId, { content });
        
        // 답글 폼 초기화 및 숨김
        replyInput.value = '';
        this.closest('.reply-form-container').style.display = 'none';
        
        // 댓글 목록 다시 로드
        loadComments(boardType, postId);
      } catch (error) {
        console.error('답글 등록 오류:', error);
        alert('답글 등록 중 오류가 발생했습니다.');
      }
    });
  });
  
  // 답글 수정/삭제 버튼 이벤트도 비슷하게 구현...
}

/**
 * 댓글 기능 설정
 */
function setupCommentEvents() {
  // 댓글 작성 폼
  const commentForm = document.getElementById('comment-form');
  if (commentForm) {
    commentForm.addEventListener('submit', async function(e) {
      e.preventDefault();
      
      // 로그인 확인
      if (!Auth.isLoggedIn()) {
        alert('로그인이 필요합니다.');
        return;
      }
      
      // URL에서 게시판 타입 및 게시글 ID 가져오기
      const urlParams = new URLSearchParams(window.location.search);
      const boardType = urlParams.get('board');
      const postId = urlParams.get('postId');
      
      if (!boardType || !postId) return;
      
      // 댓글 내용
      const commentInput = document.getElementById('detail-comment-input');
      const content = commentInput.value.trim();
      
      if (!content) return;
      
      try {
        // 댓글 등록
        await BoardService.addComment(boardType, postId, { content });
        
        // 폼 초기화
        commentInput.value = '';
        
        // 댓글 목록 다시 로드
        loadComments(boardType, postId);
      } catch (error) {
        console.error('댓글 등록 오류:', error);
        alert('댓글 등록 중 오류가 발생했습니다.');
      }
    });
  }
}

/**
 * 검색 기능 설정
 */
function setupSearchEvents() {
  // 각 게시판의 검색 폼
  document.querySelectorAll('.board-search-form').forEach(form => {
    form.addEventListener('submit', function(e) {
      e.preventDefault();
      
      const boardType = this.dataset.boardType;
      const searchInput = this.querySelector('.board-search-input');
      const searchQuery = searchInput.value.trim();
      
      // URL 업데이트
      const url = new URL(window.location);
      
      if (searchQuery) {
        url.searchParams.set('search', searchQuery);
      } else {
        url.searchParams.delete('search');
      }
      
      // 페이지 초기화
      url.searchParams.delete('page');
      
      window.history.pushState({}, '', url);
      
      // 게시글 목록 다시 로드
      loadPosts(boardType, { search: searchQuery });
    });
  });
  
  // 검색 초기화 버튼
  document.querySelectorAll('.board-search-reset').forEach(button => {
    button.addEventListener('click', function() {
      const boardType = this.dataset.boardType;
      const searchInput = document.querySelector(`#${boardType}-search-input`);
      
      if (searchInput) {
        searchInput.value = '';
      }
      
      // URL 업데이트
      const url = new URL(window.location);
      url.searchParams.delete('search');
      url.searchParams.delete('page');
      
      window.history.pushState({}, '', url);
      
      // 게시글 목록 다시 로드
      loadPosts(boardType);
    });
  });
}

/**
 * 페이지네이션 업데이트
 * @param {string} boardType 게시판 타입
 * @param {Object} pagination 페이징 정보
 */
function updatePagination(boardType, pagination) {
  const paginationContainer = document.querySelector(`#${boardType}-board .pagination-container`);
  if (!paginationContainer || !pagination) return;
  
  const { currentPage, totalPages } = pagination;
  
  // 페이지 링크 생성
  let paginationHTML = `
    <nav aria-label="페이지 네비게이션">
      <ul class="pagination">
        <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
          <a class="page-link" href="#" data-page="${currentPage - 1}" aria-label="이전">
            <span aria-hidden="true">&laquo;</span>
          </a>
        </li>
  `;
  
  // 페이지 번호 표시 (최대 5개)
  const startPage = Math.max(1, currentPage - 2);
  const endPage = Math.min(totalPages, startPage + 4);
  
  for (let i = startPage; i <= endPage; i++) {
    paginationHTML += `
      <li class="page-item ${i === currentPage ? 'active' : ''}">
        <a class="page-link" href="#" data-page="${i}">${i}</a>
      </li>
    `;
  }
  
  paginationHTML += `
        <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
          <a class="page-link" href="#" data-page="${currentPage + 1}" aria-label="다음">
            <span aria-hidden="true">&raquo;</span>
          </a>
        </li>
      </ul>
    </nav>
  `;
  
  paginationContainer.innerHTML = paginationHTML;
  
  // 페이지 링크 이벤트 설정
  paginationContainer.querySelectorAll('.page-link').forEach(link => {
    link.addEventListener('click', function(e) {
      e.preventDefault();
      
      const page = parseInt(this.dataset.page);
      if (isNaN(page) || page < 1 || page > totalPages) return;
      
      // URL에 현재 검색어 유지
      const urlParams = new URLSearchParams(window.location.search);
      const searchQuery = urlParams.get('search');
      
      // 페이지 번호 업데이트
      urlParams.set('page', page);
      window.history.pushState({}, '', `?${urlParams.toString()}`);
      
      // 게시글 목록 로드
      loadPosts(boardType, { search: searchQuery, page });
      
      // 페이지 상단으로 스크롤
      window.scrollTo({ top: 0, behavior: 'smooth' });
    });
  });
}

/**
 * 댓글 페이지네이션 업데이트
 * @param {string} boardType 게시판 타입
 * @param {number} postId 게시글 ID
 * @param {Object} pagination 페이징 정보
 */
function updateCommentPagination(boardType, postId, pagination) {
  const paginationContainer = document.querySelector('#comment-pagination');
  if (!paginationContainer || !pagination) return;
  
  const { currentPage, totalPages } = pagination;
  if (totalPages <= 1) {
    paginationContainer.innerHTML = '';
    return;
  }
  
  // 페이지 링크 생성
  let paginationHTML = `
    <nav aria-label="댓글 페이지 네비게이션">
      <ul class="pagination pagination-sm">
  `;
  
  // 이전 버튼
  if (currentPage > 1) {
    paginationHTML += `
      <li class="page-item">
        <a class="page-link" href="#comments" data-page="${currentPage - 1}" aria-label="이전">
          <span aria-hidden="true">&laquo;</span>
        </a>
      </li>
    `;
  }
  
  // 페이지 번호 표시 (최대 5개)
  const startPage = Math.max(1, currentPage - 2);
  const endPage = Math.min(totalPages, startPage + 4);
  
  for (let i = startPage; i <= endPage; i++) {
    paginationHTML += `
      <li class="page-item ${i === currentPage ? 'active' : ''}">
        <a class="page-link" href="#comments" data-page="${i}">${i}</a>
      </li>
    `;
  }
  
  // 다음 버튼
  if (currentPage < totalPages) {
    paginationHTML += `
      <li class="page-item">
        <a class="page-link" href="#comments" data-page="${currentPage + 1}" aria-label="다음">
          <span aria-hidden="true">&raquo;</span>
        </a>
      </li>
    `;
  }
  
  paginationHTML += `
      </ul>
    </nav>
  `;
  
  paginationContainer.innerHTML = paginationHTML;
  
  // 페이지 링크 이벤트 설정
  paginationContainer.querySelectorAll('.page-link').forEach(link => {
    link.addEventListener('click', function(e) {
      e.preventDefault();
      
      const page = parseInt(this.dataset.page);
      if (isNaN(page) || page < 1 || page > totalPages) return;
      
      // 댓글 목록 다시 로드
      loadComments(boardType, postId, { page });
    });
  });
}

/**
 * 날짜 포맷팅
 * @param {string} dateString 날짜 문자열
 * @param {boolean} includeTime 시간 포함 여부
 * @returns {string} 포맷팅된 날짜 문자열
 */
function formatDate(dateString, includeTime = false) {
  const date = new Date(dateString);
  const now = new Date();
  const diff = now - date;
  
  // 오늘 날짜인 경우
  if (diff < 86400000) { // 24시간 이내
    if (includeTime) {
      const hours = date.getHours().toString().padStart(2, '0');
      const minutes = date.getMinutes().toString().padStart(2, '0');
      return `오늘 ${hours}:${minutes}`;
    }
    return '오늘';
  }
  
  // 어제 날짜인 경우
  if (diff < 172800000) { // 48시간 이내
    if (includeTime) {
      const hours = date.getHours().toString().padStart(2, '0');
      const minutes = date.getMinutes().toString().padStart(2, '0');
      return `어제 ${hours}:${minutes}`;
    }
    return '어제';
  }
  
  // 올해 날짜인 경우
  if (date.getFullYear() === now.getFullYear()) {
    if (includeTime) {
      const month = (date.getMonth() + 1).toString().padStart(2, '0');
      const day = date.getDate().toString().padStart(2, '0');
      const hours = date.getHours().toString().padStart(2, '0');
      const minutes = date.getMinutes().toString().padStart(2, '0');
      return `${month}.${day} ${hours}:${minutes}`;
    }
    
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${month}.${day}`;
  }
  
  // 이전 년도 날짜
  if (includeTime) {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${year}.${month}.${day} ${hours}:${minutes}`;
  }
  
  const year = date.getFullYear();
  const month = (date.getMonth() + 1).toString().padStart(2, '0');
  const day = date.getDate().toString().padStart(2, '0');
  return `${year}.${month}.${day}`;
}

/**
 * 카테고리 필터 UI 업데이트
 * @param {string} boardType 게시판 타입
 * @param {string} category 선택된 카테고리
 */
function updateCategoryFilterUI(boardType, category) {
  const categoryFilter = document.querySelector(`#${boardType}-board .category-filter`);
  if (!categoryFilter) return;
  
  // 모든 카테고리 버튼 비활성화
  categoryFilter.querySelectorAll('.category-btn').forEach(btn => {
    btn.classList.remove('active');
  });
  
  // 선택된 카테고리 활성화
  const selectedBtn = categoryFilter.querySelector(`.category-btn[data-category="${category}"]`);
  if (selectedBtn) {
    selectedBtn.classList.add('active');
  }
}

/**
 * 댓글 작성 폼 상태 갱신
 * @param {string} boardType 게시판 타입
 * @param {number} postId 게시글 ID
 */
function updateCommentForm(boardType, postId) {
  const commentForm = document.getElementById('comment-form');
  const commentInput = document.getElementById('detail-comment-input');
  const submitButton = commentForm?.querySelector('button[type="submit"]');
  
  // 로그인 상태에 따른 처리
  if (Auth.isLoggedIn()) {
    if (commentInput) {
      commentInput.disabled = false;
      commentInput.placeholder = '댓글을 작성하세요...';
    }
    
    if (submitButton) {
      submitButton.disabled = false;
    }
    
    // 로그인 사용자 정보로 작성자명 표시
    Auth.getCurrentUser().then(user => {
      const userDisplayElement = document.getElementById('comment-user-display');
      if (userDisplayElement && user) {
        userDisplayElement.textContent = user.nickname || user.username;
        userDisplayElement.style.display = 'block';
      }
    });
  } else {
    // 비로그인 상태
    if (commentInput) {
      commentInput.disabled = true;
      commentInput.placeholder = '댓글을 작성하려면 로그인이 필요합니다.';
    }
    
    if (submitButton) {
      submitButton.disabled = true;
    }
    
    // 로그인 버튼 표시
    const loginButtonContainer = document.getElementById('comment-login-container');
    if (loginButtonContainer) {
      loginButtonContainer.style.display = 'block';
      
      const loginButton = loginButtonContainer.querySelector('.comment-login-btn');
      if (loginButton) {
        loginButton.addEventListener('click', () => {
          window.location.href = `/view/login.html?returnUrl=${encodeURIComponent(window.location.href)}`;
        });
      }
    }
  }
  
  // 게시판별 댓글 정책 표시
  updateCommentPolicy(boardType);
}

/**
 * 게시판별 댓글 정책 설정
 * @param {string} boardType 게시판 타입
 */
function updateCommentPolicy(boardType) {
  const policyElement = document.getElementById('comment-policy');
  if (!policyElement) return;
  
  let policyText = '';
  
  // 게시판 타입에 따른 정책 메시지
  switch (boardType) {
    case 'free':
      policyText = '자유게시판의 댓글은 등록 후 10분 이내에 수정/삭제가 가능합니다.';
      break;
    case 'qna':
      policyText = 'QnA 게시판의 댓글은 질문자와 답변 작성자만 삭제할 수 있습니다.';
      break;
    case 'news':
      policyText = '뉴스 게시판의 댓글은 운영원칙에 의해 관리됩니다. 욕설, 비방 등의 내용은 삭제될 수 있습니다.';
      break;
    default:
      policyText = '댓글 작성 시 커뮤니티 가이드라인을 준수해주세요.';
  }
  
  policyElement.textContent = policyText;
  policyElement.style.display = 'block';
}
