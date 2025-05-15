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
  const detailCommentList = document.getElementById('detail-comment-list');
  const detailCommentInput = document.getElementById('detail-comment-input');
  // const detailCommentSubmitBtn = document.getElementById('detail-comment-submit'); // 필요시 주석 해제

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
      }

      // 상세 보기 표시
      postDetailView.style.display = 'block';

      // 현재 활성화된 게시판 목록과 페이지네이션은 그대로 유지 (숨기지 않음)
      // HTML 구조상 post-detail-view가 목록 위에 오도록 배치했으므로,
      // 목록을 숨길 필요 없이 post-detail-view만 block으로 만들면 됨.

    // 임시 데이터로 상세 보기 채우기
      detailTitle.textContent = `게시글 제목 (ID: ${postId} - ${boardType === 'news-board' ? '키보드 소식' : '자유게시판'})`;
      
      const tempAuthor = "임시사용자";
      const tempDate = "2025-05-15";
      const tempViews = Math.floor(Math.random() * 100) + 1;  // 1-100 랜덤 조회수
      const tempLikes = Math.floor(Math.random() * 20);       // 0-19 랜덤 추천수

      // 작성자 정보 채우기
      detailAuthor.textContent = tempAuthor;
      
      // 작성일 정보 채우기
      detailDate.textContent = tempDate;
      
      // 조회수 정보 채우기
      document.getElementById('detail-views').textContent = tempViews;
      
      // 추천수 정보 채우기
      document.getElementById('detail-likes').textContent = tempLikes;
      document.getElementById('post-like-count-display').textContent = tempLikes;
      
      let tempContentHTML = '';
      if (boardType === 'news-board') {
        tempContentHTML = `
          <p>✨ 키보드 소식 게시판(ID: ${postId})의 상세 내용이다냥! ✨</p>
          <p>이곳은 주로 새로운 키보드 출시 정보, 이벤트, 관련 뉴스 등을 다루는 곳이다냥. 냐옹~ ⌨️💨</p>
          <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.</p>
        `;
      } else if (boardType === 'free-board') {
        tempContentHTML = `
          <p>🎈 자유게시판(ID: ${postId})의 상세 내용이다냥! 🎈</p>
          <p>여기는 자유롭게 이야기를 나누는 공간이다냥! 어떤 주제든 환영이다냥~ 😻</p>
          <p>Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo.</p>
        `;
      } else {
        tempContentHTML = `<p>알 수 없는 게시판의 내용이다냥 (ID: ${postId})</p>`;
      }
      detailContent.innerHTML = tempContentHTML;

      detailCommentCount.textContent = "2"; // 임시 댓글 수
      detailCommentList.innerHTML = `
        <div class="comment">
          <div class="comment-meta"><span>댓글 작성자 1</span><span>2025-05-15</span></div>
          <div class="comment-content"><p>첫 번째 댓글입니다냥!</p></div>
        </div>
        <div class="comment">
          <div class="comment-meta"><span>댓글 작성자 2</span><span>2025-05-16</span></div>
          <div class="comment-content"><p>두 번째 댓글이다옹~</p></div>
        </div>
      `;
      detailCommentInput.value = '';
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
});
