// 게시판 탭 전환 기능
document.addEventListener('DOMContentLoaded', function() {
  const boardLinks = document.querySelectorAll('.board-nav a');
  const boardContents = document.querySelectorAll('.board-content');
  const boardBottomContainer = document.getElementById('board-bottom-container');
  const postDetailView = document.getElementById('post-detail-view');
  const postDetailCloseBtn = document.querySelector('#post-detail-view .post-detail-close');
  const detailTitle = document.getElementById('detail-title');
  const detailAuthorContainer = document.getElementById('detail-author-container');
  const detailAuthor = document.getElementById('detail-author');
  const detailDateContainer = document.getElementById('detail-date-container');
  const detailDate = document.getElementById('detail-date');
  const detailContent = document.getElementById('detail-content');
  const detailCommentCount = document.getElementById('detail-comment-count');
  const detailCommentList = document.getElementById('detail-comment-list');  const detailCommentInput = document.getElementById('detail-comment-input');
  const detailCommentSubmitBtn = document.getElementById('detail-comment-submit');

  let previouslyActiveBoardId = 'news-board'; // 기본값 설정 또는 첫번째 활성 탭으로 초기화

  // 초기 활성 탭 ID 설정
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
      if (content.id !== 'anonymous-board' || !content.classList.contains('active')) { // 익명게시판이 활성화된 상태가 아니면 숨김
          content.style.display = 'none';
      }
    });
    if (boardBottomContainer) {
      boardBottomContainer.style.display = 'none';
    }
  }

  // 특정 게시판 목록 및 하단부 보여주기
  function showBoardList(boardIdToShow) {
    hideBoardLists(); // 일단 모든 목록 숨김
    postDetailView.style.display = 'none'; // 상세 보기도 숨김

    const boardToShow = document.getElementById(boardIdToShow);
    if (boardToShow) {
      boardToShow.style.display = 'block';
    }

    if (boardIdToShow !== 'anonymous-board' && boardBottomContainer) {
      boardBottomContainer.style.display = 'block'; // 익명게시판 아니면 하단부 표시
    } else if (boardBottomContainer) {
      boardBottomContainer.style.display = 'none'; // 익명게시판이면 하단부 숨김
    }

    // 네비게이션 탭 활성화
    boardLinks.forEach(link => {
      if (link.dataset.board === boardIdToShow.replace('-board', '')) {
        link.classList.add('active');
      } else {
        link.classList.remove('active');
      }
    });
    previouslyActiveBoardId = boardIdToShow; // 현재 활성화된 게시판 ID 업데이트
  }

  // 탭 클릭 이벤트
  boardLinks.forEach(link => {
    link.addEventListener('click', function(e) {
      e.preventDefault();
      const boardId = this.dataset.board + '-board';
      showBoardList(boardId);
    });
  });

  // 초기 로드 시: 기본으로 'news-board' 또는 활성화된 탭의 내용을 보여줌
  showBoardList(previouslyActiveBoardId);


  // '키보드 소식' 및 '자유게시판' 게시글 클릭 시 상세 보기 표시
  const clickableRows = document.querySelectorAll('.board-table .clickable-row');
  clickableRows.forEach(row => {
    row.addEventListener('click', function() {
      const postId = this.dataset.postId;
      const parentBoardElement = this.closest('.board-content');
      let boardType = 'unknown-board';

      if (parentBoardElement && parentBoardElement.id) {
        boardType = parentBoardElement.id; // 'news-board' 또는 'free-board'
         // previouslyActiveBoardId는 현재 보고 있는 목록의 ID여야 함.
         // 상세보기를 열 때는 previouslyActiveBoardId를 변경하지 않음.
         // 탭 전환 시에만 previouslyActiveBoardId가 변경됨.
      } else {
        // closest로 못찾는 경우(구조 변경 등) 대비, 현재 활성화된 탭에서 유추
        const activeNav = document.querySelector('.board-nav a.active');
        if (activeNav && activeNav.dataset.board) {
            boardType = activeNav.dataset.board + '-board';
        }
      }      // 상세 보기 표시 및 데이터 가져오기
      loadPostDetails(postId, boardType);
      
      // 현재 활성화된 게시판 목록과 페이지네이션은 그대로 유지 (숨기지 않음)
      // HTML 구조상 post-detail-view가 목록 위에 오도록 배치했으므로,
      // 목록을 숨길 필요 없이 post-detail-view만 block으로 만들면 됨.
      
      postDetailView.style.display = 'block';
    });
  });

  // 게시글 상세 보기 닫기 버튼 클릭 시
  if (postDetailCloseBtn) {
    postDetailCloseBtn.addEventListener('click', function() {
      postDetailView.style.display = 'none'; // 상세 보기만 숨김
      // previouslyActiveBoardId에 해당하는 게시판 목록은 이미 보여지고 있어야 함.
      // 네비게이션 탭 활성화는 showBoardList에서 처리하므로, 여기서는 특별히 할 필요 없음.
      // 만약 상세보기를 닫았을 때 특정 목록을 강제로 다시 로드해야 한다면 showBoardList(previouslyActiveBoardId) 호출.
      // 현재 로직에서는 상세보기를 열 때 목록을 숨기지 않으므로, 닫을 때도 목록은 그대로 있음.
    });
  }

  // 글쓰기 모달 관련 (기존 코드 유지 또는 필요시 수정)
  const writeBtn = document.getElementById('write-btn');
  const writeModal = document.getElementById('write-modal');
  const modalCloseButtons = document.querySelectorAll('.modal .close'); // 모든 모달 닫기 버튼

  if (writeBtn) {
    writeBtn.addEventListener('click', function() {
      if (writeModal) writeModal.style.display = 'block';
    });
  }

  modalCloseButtons.forEach(btn => {
    btn.addEventListener('click', function() {
      const modalToClose = this.closest('.modal');
      if (modalToClose) modalToClose.style.display = 'none';
    });
  });

  window.addEventListener('click', function(e) {
    if (e.target === writeModal) {
      if (writeModal) writeModal.style.display = 'none';
    }
    // 익명게시판 상세 모달 외부 클릭 닫기 (필요시 추가)
    // const postDetailModal = document.getElementById('post-detail-modal');
    // if (e.target === postDetailModal) {
    //   if (postDetailModal) postDetailModal.style.display = 'none';
    // }
  });

  // 익명 게시판(한줄 게시판) 게시글 클릭 시 모달 표시 (기존 로직 유지)
  const onelinePosts = document.querySelectorAll('.oneline-post');
  const postDetailModal = document.getElementById('post-detail-modal'); // 익명게시판용 모달

  onelinePosts.forEach(post => {
    post.addEventListener('click', function() {
      // 익명 게시판은 별도의 모달을 사용하므로, postDetailView와 로직이 겹치지 않도록 주의
      if (postDetailModal) {
        // 여기에 익명 게시판 상세 내용 채우는 로직 추가 (필요시)
        // 예: const anonDetailTitle = postDetailModal.querySelector(...);
        postDetailModal.style.display = 'block';
      }
    });
  });
  // 추천 버튼 기능 추가
  const postLikeButton = document.getElementById('post-like-button');
  if (postLikeButton) {
    postLikeButton.addEventListener('click', function() {
      // 추천 버튼 클릭 시 효과 추가
      this.classList.add('liked');
        // 현재 추천 수 가져오기
      const likeCountDisplay = document.getElementById('post-like-count-display');
      // 초기 렌더링 시점에 detail-likes의 값으로 post-like-count-display를 설정했으므로,
      // currentLikes는 post-like-count-display에서 가져오는 것이 일관적이다냥.
      let currentLikes = parseInt(likeCountDisplay.textContent);
      if (isNaN(currentLikes)) { // 혹시 숫자가 아니라면 0으로 초기화한다냥
        currentLikes = 0;
      }
        // 추천 수 증가시키기
      likeCountDisplay.textContent = currentLikes + 1;
      // 상세 보기 영역의 추천수도 업데이트 (만약 있다면)
      const detailLikesElement = document.getElementById('detail-likes');
      if (detailLikesElement) {
        detailLikesElement.textContent = currentLikes + 1;
      }
      
      // 잠시 후 버튼 상태 변경
      setTimeout(() => {
        this.innerHTML = '<i>✓</i> 추천완료 (<span>' + (currentLikes + 1) + '</span>)';
        this.disabled = true;
      }, 300);
      
      // 실제 구현에서는 여기서 서버로 추천 데이터 전송
      // fetch('/post/like', {
      //   method: 'POST',
      //   body: JSON.stringify({ postId: currentPostId }),
      //   headers: { 'Content-Type': 'application/json' }
      // });
    });
  }
    // 모달 내 추천 버튼에도 같은 기능 부여
  const modalLikeButtons = document.querySelectorAll('.modal .post-like-button');
  modalLikeButtons.forEach(button => {
    button.addEventListener('click', function() {
      // 추천 버튼 클릭 시 효과 추가
      this.classList.add('liked');
      
      // 현재 추천 수 가져오기 (모달 내에서)
      const likeCountSpan = this.querySelector('span');
      let currentLikes = parseInt(likeCountSpan.textContent);
      if (isNaN(currentLikes)) {
        currentLikes = 0;
      }
        // 추천 수 증가시키기
      likeCountSpan.textContent = currentLikes + 1;
        // 잠시 후 버튼 상태 변경
      setTimeout(() => {
        this.innerHTML = '<i>✓</i> 추천완료 (<span>' + (currentLikes + 1) + '</span>)';
        this.disabled = true;
      }, 300);
    });
  });
  
  // 게시판 API를 통해 게시물 목록 로드
  async function loadBoardData(boardType, page = 1, size = 10) {
    try {
      // API를 통해 게시물 목록 가져오기 (.do 접미사 사용)
      const response = await BoardService.getPosts(boardType, {
        page: page,
        size: size,
        sort: 'latest'
      });
      
      if (!response || !response.posts) {
        console.error(`${boardType} 게시판의 게시물을 불러오는데 실패했습니다.`);
        return;
      }
      
      const posts = response.posts;
      const boardTable = document.querySelector(`#${boardType}-board .board-table tbody`);
      
      if (!boardTable) {
        console.error(`${boardType} 게시판의 테이블을 찾을 수 없습니다.`);
        return;
      }
      
      // 기존 데이터 삭제
      boardTable.innerHTML = '';
      
      // 게시물 목록 생성
      posts.forEach(post => {
        const row = document.createElement('tr');
        row.className = 'clickable-row';
        row.dataset.postId = post.id;
        
        row.innerHTML = `
          <td class="post-id">${post.id}</td>
          <td class="post-title">${post.title} ${post.commentCount > 0 ? `<span class="comment-count">[${post.commentCount}]</span>` : ''}</td>
          <td class="post-author">${post.author}</td>
          <td class="post-date">${formatDate(post.createdAt)}</td>
          <td class="post-views">${post.views}</td>
          <td class="post-likes">${post.likes}</td>
        `;
        
        boardTable.appendChild(row);
      });
      
      // 게시물 클릭 이벤트 다시 연결
      attachPostClickEvents();
      
    } catch (error) {
      console.error(`게시물 목록 로딩 중 오류 발생:`, error);
    }
  }
  
  // 게시물 상세 정보 로드
  async function loadPostDetail(boardType, postId) {
    try {
      // API를 통해 게시물 상세 정보 가져오기 (.do 접미사 사용)
      const post = await BoardService.getPost(boardType, postId);
      
      if (!post) {
        console.error(`게시물 정보를 불러오는데 실패했습니다.`);
        return;
      }
      
      // 상세 정보 표시
      detailTitle.textContent = post.title;
      detailAuthor.textContent = post.author;
      detailDate.textContent = formatDate(post.createdAt);
      document.getElementById('detail-views').textContent = post.views;
      document.getElementById('detail-likes').textContent = post.likes;
      document.getElementById('post-like-count-display').textContent = post.likes;
      detailContent.innerHTML = post.content;
      
      // 댓글 불러오기
      loadPostComments(boardType, postId);
      
    } catch (error) {
      console.error(`게시물 상세 정보 로딩 중 오류 발생:`, error);
    }
  }
  
  // 게시물 댓글 로드
  async function loadPostComments(boardType, postId) {
    try {
      // API를 통해 댓글 목록 가져오기 (.do 접미사 사용)
      const response = await BoardService.getComments(boardType, postId);
      
      if (!response || !response.comments) {
        console.error(`댓글을 불러오는데 실패했습니다.`);
        return;
      }
      
      const comments = response.comments;
      
      // 댓글 수 업데이트
      detailCommentCount.textContent = comments.length;
      
      // 댓글 목록 생성
      detailCommentList.innerHTML = '';
      
      if (comments.length === 0) {
        detailCommentList.innerHTML = '<p class="no-comments">아직 댓글이 없습니다. 첫 댓글을 작성해보세요!</p>';
        return;
      }
      
      comments.forEach(comment => {
        const commentElement = document.createElement('div');
        commentElement.className = 'comment-item';
        
        commentElement.innerHTML = `
          <div class="comment-header">
            <span class="comment-author">${comment.author}</span>
            <span class="comment-date">${formatDate(comment.createdAt)}</span>
          </div>
          <div class="comment-content">${comment.content}</div>
        `;
        
        detailCommentList.appendChild(commentElement);
      });
      
    } catch (error) {
      console.error(`댓글 로딩 중 오류 발생:`, error);
    }
  }
  
  // 날짜 포맷 함수
  function formatDate(dateString) {
    const date = new Date(dateString);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  }
  
  // 게시물 클릭 이벤트 연결 함수
  function attachPostClickEvents() {
    const clickableRows = document.querySelectorAll('.board-table .clickable-row');
    clickableRows.forEach(row => {
      row.addEventListener('click', function() {
        const postId = this.dataset.postId;
        const parentBoardElement = this.closest('.board-content');
        let boardType = '';
        
        if (parentBoardElement && parentBoardElement.id) {
          boardType = parentBoardElement.id.replace('-board', '');
        } else {
          const activeNav = document.querySelector('.board-nav a.active');
          if (activeNav && activeNav.dataset.board) {
            boardType = activeNav.dataset.board;
          }
        }
        
        // 게시물 상세 정보 로드
        if (boardType && postId) {
          loadPostDetail(boardType, postId);
          postDetailView.style.display = 'block';
        }
      });
    });
  }

  // 초기화 시 각 게시판의 데이터 로드
  function initBoardData() {
    // 키보드 소식 게시판 (news) 데이터 로드
    loadBoardData('news');
    
    // 자유게시판 (free) 데이터 로드
    loadBoardData('free');
    
    // 익명게시판의 경우 필요시 구현 (비동기 로드 또는 탭 클릭 시 로드)
  }
  
  // 탭 클릭 시 해당 게시판 데이터 다시 로드 (선택적)
  boardLinks.forEach(link => {
    const originalClickHandler = link.onclick;
    
    link.onclick = function(e) {
      if (originalClickHandler) {
        originalClickHandler.call(this, e);
      }
      
      const boardType = this.dataset.board;
      if (boardType && boardType !== 'anonymous') {
        loadBoardData(boardType);
      }
      
      return false;
    };
  });
  
  // 댓글 작성 기능
  if (detailCommentInput) {
    const commentForm = detailCommentInput.closest('form');
    if (commentForm) {
      commentForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const commentText = detailCommentInput.value.trim();
        if (!commentText) return;
        
        try {
          const activeNav = document.querySelector('.board-nav a.active');
          if (!activeNav || !activeNav.dataset.board) return;
          
          const boardType = activeNav.dataset.board;
          const postId = document.querySelector('.clickable-row.active')?.dataset.postId;
          
          if (!postId) return;
          
          // API를 통해 댓글 작성 (.do 접미사 사용)
          await BoardService.createComment(boardType, postId, commentText);
          
          // 댓글 작성 후 댓글 목록 다시 로드
          loadPostComments(boardType, postId);
          
          // 입력 필드 초기화
          detailCommentInput.value = '';
          
        } catch (error) {
          console.error(`댓글 작성 중 오류 발생:`, error);
          alert('댓글 작성에 실패했습니다. 다시 시도해주세요.');
        }
      });
    }
  }
  
  // 게시글 추천 기능
  if (postLikeButton) {
    const originalClickHandler = postLikeButton.onclick;
    
    postLikeButton.onclick = async function(e) {
      if (originalClickHandler) {
        originalClickHandler.call(this, e);
      }
      
      try {
        const activeNav = document.querySelector('.board-nav a.active');
        if (!activeNav || !activeNav.dataset.board) return;
        
        const boardType = activeNav.dataset.board;
        const postId = document.querySelector('.clickable-row.active')?.dataset.postId;
        
        if (!postId) return;
        
        // API를 통해 게시글 추천 (.do 접미사 사용)
        await BoardService.reactToPost(boardType, postId, 'like');
        
      } catch (error) {
        console.error(`게시글 추천 중 오류 발생:`, error);
      }
      
      return false;
    };
  }
  
  // 페이지 초기화 시 데이터 로드
  initBoardData();
  
  // 화면 초기화 시 게시판 목록 로드
  loadBoardPosts('news');
  
  /**
   * 게시글 상세 정보 로드
   * @param {string} postId 게시글 ID
   * @param {string} boardType 게시판 타입 (news-board, free-board 등)
   */
  async function loadPostDetails(postId, boardType) {
    try {
      // boardType에서 board- 접미사 제거하고 API 경로 생성 
      const apiType = boardType.replace('-board', '');
      const post = await BoardService.getPost(apiType, postId);
        if (post) {
        // 게시글 정보 표시
        detailTitle.textContent = post.title;
        // 게시글 ID와 게시판 타입 저장 (댓글 작성에 필요)
        detailTitle.dataset.postId = postId;
        detailTitle.dataset.boardType = boardType;
        
        detailAuthor.textContent = post.author || '익명';
        detailDate.textContent = post.createdAt || new Date().toLocaleDateString();
        detailContent.innerHTML = post.content || '';
        
        // 조회수 표시
        if (document.getElementById('detail-views')) {
          document.getElementById('detail-views').textContent = post.views || 0;
        }
        
        // 추천수 표시
        if (document.getElementById('detail-likes')) {
          document.getElementById('detail-likes').textContent = post.likes || 0;
        }
        if (document.getElementById('post-like-count-display')) {
          document.getElementById('post-like-count-display').textContent = post.likes || 0;
        }
        
        // 댓글 로드
        loadComments(apiType, postId);
      } else {
        alert('게시글을 불러올 수 없습니다.');
        postDetailView.style.display = 'none';
      }
    } catch (error) {
      console.error('게시글 상세 정보 로드 오류:', error);
      alert('게시글을 불러오는 중 오류가 발생했습니다.');
    }
  }
  
  /**
   * 댓글 목록 로드
   * @param {string} boardType 게시판 타입 (news, free 등)
   * @param {string} postId 게시글 ID
   */
  async function loadComments(boardType, postId) {
    try {
      const comments = await BoardService.getComments(boardType, postId);
      
      if (comments && comments.length > 0) {
        detailCommentCount.textContent = comments.length.toString();
        
        // 댓글 목록 생성
        detailCommentList.innerHTML = '';
        comments.forEach(comment => {
          const commentElement = document.createElement('div');
          commentElement.className = 'comment';
          commentElement.innerHTML = `
            <div class="comment-meta">
              <span>${comment.author || '익명'}</span>
              <span>${comment.createdAt || new Date().toLocaleDateString()}</span>
            </div>
            <div class="comment-content">
              <p>${comment.content}</p>
            </div>
          `;
          detailCommentList.appendChild(commentElement);
        });
      } else {
        detailCommentCount.textContent = '0';
        detailCommentList.innerHTML = '<div class="no-comments">댓글이 없습니다.</div>';
      }
    } catch (error) {
      console.error('댓글 로드 오류:', error);
      detailCommentList.innerHTML = '<div class="error">댓글을 불러오는 중 오류가 발생했습니다.</div>';
    }
  }
  
  /**
   * 게시판 목록 로드
   * @param {string} boardType 게시판 타입 (news, free 등)
   * @param {Object} params 페이징 및 정렬 옵션
   */
  async function loadBoardPosts(boardType, params = {}) {
    try {
      const boardContainer = document.getElementById(`${boardType}-board`);
      if (!boardContainer) return;
      
      const postsTable = boardContainer.querySelector('.board-table tbody');
      if (!postsTable) return;
      
      // 로딩 표시 추가
      postsTable.innerHTML = '<tr><td colspan="5" class="loading">게시글을 불러오는 중...</td></tr>';
      
      // 게시글 목록 로드
      const posts = await BoardService.getPosts(boardType, params);
      
      if (posts && posts.length > 0) {
        postsTable.innerHTML = '';
        posts.forEach(post => {
          const row = document.createElement('tr');
          row.className = 'clickable-row';
          row.dataset.postId = post.id;
          
          row.innerHTML = `
            <td>${post.id}</td>
            <td class="post-title">${post.title}</td>
            <td>${post.author || '익명'}</td>
            <td>${post.createdAt || '-'}</td>
            <td>${post.views || 0}</td>
          `;
          
          // 클릭 이벤트 추가
          row.addEventListener('click', function() {
            const postId = this.dataset.postId;
            loadPostDetails(postId, `${boardType}-board`);
            postDetailView.style.display = 'block';
          });
          
          postsTable.appendChild(row);
        });
      } else {
        postsTable.innerHTML = '<tr><td colspan="5">게시글이 없습니다.</td></tr>';
      }
    } catch (error) {
      console.error(`${boardType} 게시글 목록 로드 오류:`, error);
      const boardContainer = document.getElementById(`${boardType}-board`);
      if (boardContainer) {
        const postsTable = boardContainer.querySelector('.board-table tbody');
        if (postsTable) {
          postsTable.innerHTML = '<tr><td colspan="5" class="error">게시글을 불러오는 중 오류가 발생했습니다.</td></tr>';
        }
      }
    }
  }
  
  // 댓글 제출 버튼 클릭 이벤트
  if (detailCommentSubmitBtn) {
    detailCommentSubmitBtn.addEventListener('click', submitComment);
  }
  
  /**
   * 댓글 제출 처리
   */
  async function submitComment() {
    const commentContent = detailCommentInput.value.trim();
    if (!commentContent) {
      alert('댓글 내용을 입력해주세요.');
      return;
    }
    
    // 현재 보고 있는 게시글의 ID와 게시판 타입 가져오기
    const postId = detailTitle.dataset.postId;
    const boardType = detailTitle.dataset.boardType;
    
    if (!postId || !boardType) {
      alert('게시글 정보를 가져올 수 없습니다.');
      return;
    }
    
    try {
      // 게시판 타입에서 '-board' 접미사 제거
      const apiType = boardType.replace('-board', '');
      
      // 댓글 작성 API 호출
      await BoardService.createComment(apiType, postId, commentContent);
      
      // 댓글 입력창 초기화
      detailCommentInput.value = '';
      
      // 댓글 목록 새로고침
      loadComments(apiType, postId);
      
      alert('댓글이 등록되었습니다.');
    } catch (error) {
      console.error('댓글 작성 오류:', error);
      alert('댓글 작성 중 오류가 발생했습니다.');
    }
  }
});
