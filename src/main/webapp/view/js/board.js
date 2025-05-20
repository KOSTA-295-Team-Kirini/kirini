// 게시판 탭 전환 기능
document.addEventListener("DOMContentLoaded", function () {
  // 바로 게시판 초기화 함수 실행
  initializeBoardFunctions();

  function initializeBoardFunctions() {
    // 변수 초기화 시 조회수와 추천수 요소도 추가
    const boardLinks = document.querySelectorAll(".board-nav a");
    const boardContents = document.querySelectorAll(".board-content");
    const boardBottomContainer = document.getElementById(
      "board-bottom-container"
    );
    const postDetailView = document.getElementById("post-detail-view");
    const postDetailCloseBtn = document.querySelector(
      "#post-detail-view .post-detail-close"
    );
    const detailTitle = document.getElementById("detail-title");
    const detailAuthorContainer = document.getElementById(
      "detail-author-container"
    );
    const detailAuthor = document.getElementById("detail-author");
    const detailDateContainer = document.getElementById(
      "detail-date-container"
    );
    const detailDate = document.getElementById("detail-date");
    const detailViews = document.getElementById("detail-views");
    const detailLikes = document.getElementById("detail-likes");
    const detailContent = document.getElementById("detail-content");
    const detailCommentCount = document.getElementById("detail-comment-count");
    const detailCommentList = document.getElementById("detail-comment-list");
    const detailCommentInput = document.getElementById("detail-comment-input");
    const detailCommentSubmitBtn = document.getElementById(
      "detail-comment-submit"
    );

    let previouslyActiveBoardId = "news-board"; // 기본값 설정 또는 첫번째 활성 탭으로 초기화

    // 초기 활성 탭 ID 설정
    const initialActiveTabLink = document.querySelector(".board-nav a.active");
    if (initialActiveTabLink) {
      const boardName = initialActiveTabLink.dataset.board;
      if (boardName) {
        previouslyActiveBoardId = boardName + "-board";
      }
    }

    // 모든 게시판 콘텐츠 숨기기 (상세보기는 별도 제어)
    function hideBoardLists() {
      boardContents.forEach((content) => {
        if (
          content.id !== "anonymous-board" ||
          !content.classList.contains("active")
        ) {
          // 익명게시판이 활성화된 상태가 아니면 숨김
          content.style.display = "none";
        }
      });
      if (boardBottomContainer) {
        boardBottomContainer.style.display = "none";
      }
    }

    // 특정 게시판 목록 및 하단부 보여주기
    function showBoardList(boardIdToShow) {
      hideBoardLists(); // 일단 모든 목록 숨김
      postDetailView.style.display = "none"; // 상세 보기도 숨김

      const boardToShow = document.getElementById(boardIdToShow);
      if (boardToShow) {
        boardToShow.style.display = "block";
      }

      if (boardIdToShow !== "anonymous-board" && boardBottomContainer) {
        boardBottomContainer.style.display = "block"; // 익명게시판 아니면 하단부 표시
      } else if (boardBottomContainer) {
        boardBottomContainer.style.display = "none"; // 익명게시판이면 하단부 숨김
      }

      // 네비게이션 탭 활성화
      boardLinks.forEach((link) => {
        if (link.dataset.board === boardIdToShow.replace("-board", "")) {
          link.classList.add("active");
        } else {
          link.classList.remove("active");
        }
      });
      previouslyActiveBoardId = boardIdToShow; // 현재 활성화된 게시판 ID 업데이트
    }

    // 탭 클릭 이벤트
    boardLinks.forEach((link) => {
      link.addEventListener("click", function (e) {
        e.preventDefault();
        const boardId = this.dataset.board + "-board";
        showBoardList(boardId);
      });
    });

    // 초기 로드 시: 기본으로 'news-board' 또는 활성화된 탭의 내용을 보여줌
    showBoardList(previouslyActiveBoardId);

    // 게시판 컨테이너에 이벤트 위임 등록 (한 번만)
    // board-wrapper 또는 특정 게시판 컨텐츠에 이벤트 위임
    const boardWrapper = document.querySelector(".board-wrapper");
    if (boardWrapper) {
      // 이벤트 위임에 debounce 적용하여 중복 클릭 방지
      const debounceTimeout = 300; // 300ms 동안 중복 클릭 방지
      let clickTimeout = null;
      let lastClickedPostId = null;

      boardWrapper.addEventListener("click", function (e) {
        // 클릭된 요소가 clickable-row 또는 그 하위 요소인지 확인
        const clickableRow = e.target.closest(".clickable-row");
        if (!clickableRow) return; // clickable-row가 아니면 무시

        // 데이터 속성에서 정보 가져오기
        const postId = clickableRow.dataset.postId;
        const boardType =
          clickableRow.dataset.boardType ||
          clickableRow.closest(".board-content")?.id ||
          "unknown-board";

        // 동일한 게시글 클릭 중복 방지
        if (postId === lastClickedPostId && clickTimeout) {
          console.log(
            `중복 클릭 방지: ${debounceTimeout}ms 이내에 동일한 게시글 ID=${postId} 클릭됨`
          );
          return;
        }

        // 이전 타임아웃 있으면 클리어
        if (clickTimeout) {
          clearTimeout(clickTimeout);
        }

        // 현재 클릭 정보 저장
        lastClickedPostId = postId;

        // 게시물 상세 정보 로드
        if (postId) {
          console.log(`게시글 클릭: ID=${postId}, 게시판=${boardType}`);
          loadPostDetails(postId, boardType);
          postDetailView.style.display = "block";

          // 일정 시간 후 클릭 상태 초기화
          clickTimeout = setTimeout(() => {
            lastClickedPostId = null;
            clickTimeout = null;
          }, debounceTimeout);
        }
      });
      console.log("게시판 컨테이너에 이벤트 위임 등록 완료");
    }

    // 모든 게시판 테이블에 클릭 이벤트 등록
    attachPostClickEvents();

    // 게시글 상세 보기 닫기 버튼 클릭 시
    if (postDetailCloseBtn) {
      postDetailCloseBtn.addEventListener("click", function () {
        postDetailView.style.display = "none"; // 상세 보기만 숨김
        // previouslyActiveBoardId에 해당하는 게시판 목록은 이미 보여지고 있어야 함.
        // 네비게이션 탭 활성화는 showBoardList에서 처리하므로, 여기서는 특별히 할 필요 없음.
        // 만약 상세보기를 닫았을 때 특정 목록을 강제로 다시 로드해야 한다면 showBoardList(previouslyActiveBoardId) 호출.
        // 현재 로직에서는 상세보기를 열 때 목록을 숨기지 않으므로, 닫을 때도 목록은 그대로 있음.
      });
    }

    // 글쓰기 모달 관련 (기존 코드 유지 또는 필요시 수정)
    const writeBtn = document.getElementById("write-btn");
    const writeModal = document.getElementById("write-modal");
    const modalCloseButtons = document.querySelectorAll(".modal .close"); // 모든 모달 닫기 버튼

    if (writeBtn) {
      writeBtn.addEventListener("click", function () {
        if (writeModal) writeModal.style.display = "block";
      });
    }

    modalCloseButtons.forEach((btn) => {
      btn.addEventListener("click", function () {
        const modalToClose = this.closest(".modal");
        if (modalToClose) modalToClose.style.display = "none";
      });
    });

    window.addEventListener("click", function (e) {
      if (e.target === writeModal) {
        if (writeModal) writeModal.style.display = "none";
      }
      // 익명게시판 상세 모달 외부 클릭 닫기 (필요시 추가)
      // const postDetailModal = document.getElementById('post-detail-modal');
      // if (e.target === postDetailModal) {
      //   if (postDetailModal) postDetailModal.style.display = 'none';
      // }
    });

    // 익명 게시판(한줄 게시판) 게시글 클릭 시 모달 표시 (기존 로직 유지)
    const onelinePosts = document.querySelectorAll(".oneline-post");
    const postDetailModal = document.getElementById("post-detail-modal"); // 익명게시판용 모달

    onelinePosts.forEach((post) => {
      post.addEventListener("click", function () {
        // 익명 게시판은 별도의 모달을 사용하므로, postDetailView와 로직이 겹치지 않도록 주의
        if (postDetailModal) {
          // 여기에 익명 게시판 상세 내용 채우는 로직 추가 (필요시)
          // 예: const anonDetailTitle = postDetailModal.querySelector(...);
          postDetailModal.style.display = "block";
        }
      });
    });
    // 추천 버튼 기능 추가
    const postLikeButton = document.getElementById("post-like-button");
    if (postLikeButton) {
      postLikeButton.addEventListener("click", function () {
        // 추천 버튼 클릭 시 효과 추가
        this.classList.add("liked");
        // 현재 추천 수 가져오기
        const likeCountDisplay = document.getElementById(
          "post-like-count-display"
        );
        // 초기 렌더링 시점에 detail-likes의 값으로 post-like-count-display를 설정했으므로,
        // currentLikes는 post-like-count-display에서 가져오는 것이 일관적이다냥.
        let currentLikes = parseInt(likeCountDisplay.textContent);
        if (isNaN(currentLikes)) {
          // 혹시 숫자가 아니라면 0으로 초기화한다냥
          currentLikes = 0;
        }
        // 추천 수 증가
        const newLikes = currentLikes + 1;
        likeCountDisplay.textContent = newLikes;

        // 실제 서버에 추천 요청 보내기
        recommendPost();

        // 중복 클릭 방지를 위해 버튼 비활성화
        this.disabled = true;
      });

      // 추천 요청 함수
      async function recommendPost() {
        try {
          // 게시글 ID와 게시판 타입 가져오기
          const postId = detailTitle.dataset.postId;
          const boardType = detailTitle.dataset.boardType;

          if (!postId || !boardType) {
            console.error("게시글 정보를 찾을 수 없습니다.");
            return false;
          }

          // 게시판 타입에서 "-board" 제거
          const apiType = boardType.replace("-board", "");

          // 추천 API 호출
          const result = await BoardService.reactToPost(
            apiType,
            postId,
            "like"
          );

          // 응답 확인 로직
          if (
            result &&
            (result.success === true || result.status === "success")
          ) {
            console.log("게시글 추천 성공");

            // 서버에서 반환한 추천수가 있으면 이를 사용하여 UI 업데이트
            if (result.likeCount !== undefined) {
              const likeCountDisplay = document.getElementById(
                "post-like-count-display"
              );
              likeCountDisplay.textContent = result.likeCount;

              // 상세 페이지의 추천수도 함께 업데이트
              const detailLikes = document.getElementById("detail-likes");
              if (detailLikes) {
                detailLikes.textContent = result.likeCount;
              }
            }

            return true;
          } else {
            console.error(
              "게시글 추천 실패:",
              result?.message || "알 수 없는 오류"
            );

            // 이미 추천한 경우 등의 메시지가 있으면 표시
            if (result && result.message) {
              alert(result.message);
            }

            // 실패 시 UI 롤백
            const likeCountDisplay = document.getElementById(
              "post-like-count-display"
            );
            const currentLikes = parseInt(likeCountDisplay.textContent);
            likeCountDisplay.textContent = currentLikes - 1;
            postLikeButton.disabled = false;
            postLikeButton.classList.remove("liked");
            return false;
          }
        } catch (error) {
          console.error(`게시글 추천 중 오류 발생:`, error);
          // 오류 발생 시 UI 롤백
          const likeCountDisplay = document.getElementById(
            "post-like-count-display"
          );
          const currentLikes = parseInt(likeCountDisplay.textContent);
          likeCountDisplay.textContent = currentLikes - 1;
          postLikeButton.disabled = false;
          postLikeButton.classList.remove("liked");
        }

        return false;
      }
    }

    // 페이지 초기화 시 데이터 로드
    initBoardData();

    // 화면 초기화 시 게시판 목록 로드
    loadBoardPosts("news");

    // 날짜 포맷 함수
    function formatDate(dateInput) {
      // 빈 값 또는 유효하지 않은 데이터인 경우 현재 날짜 반환
      if (!dateInput) {
        const now = new Date();
        return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(
          2,
          "0"
        )}-${String(now.getDate()).padStart(2, "0")}`;
      }

      let date;

      try {
        // 이미 Date 객체인 경우
        if (dateInput instanceof Date) {
          date = dateInput;
        }
        // ISO 형식 문자열인 경우 ("2025-04-15T12:30:45")
        else if (typeof dateInput === "string" && dateInput.includes("T")) {
          date = new Date(dateInput);
        }
        // 서버에서 받아온 형식인 경우 ("2025-04-15 12:30:45")
        else if (typeof dateInput === "string" && dateInput.includes(" ")) {
          const [datePart, timePart] = dateInput.split(" ");
          const [year, month, day] = datePart.split("-");
          const [hour, minute, second] = timePart
            ? timePart.split(":")
            : [0, 0, 0];
          date = new Date(year, month - 1, day, hour, minute, second);
        }
        // 기타 문자열 형식
        else {
          date = new Date(dateInput);
        }
      } catch (e) {
        console.error("날짜 형식 변환 오류:", e);
        date = new Date();
      }

      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
    }

    // 게시물 클릭 이벤트 연결 함수
    function attachPostClickEvents() {
      console.log("게시글 클릭 이벤트 등록 중...");

      // 모든 클릭 가능한 행 선택
      const clickableRows = document.querySelectorAll(
        ".board-table .clickable-row"
      );

      console.log(
        `총 ${clickableRows.length}개의 게시글 행에 이벤트 등록 시작`
      );

      // 각 게시글 행에 직접 클릭 이벤트 추가
      clickableRows.forEach((row) => {
        // 이벤트 중복 방지를 위해 클론 노드 생성은 제거하고 직접 이벤트 등록
        row.addEventListener("click", function (e) {
          const postId = this.dataset.postId;
          const boardType =
            this.dataset.boardType ||
            this.closest(".board-content")?.id ||
            "unknown-board";

          console.log(`게시글 직접 클릭: ID=${postId}, 게시판=${boardType}`);

          if (postId) {
            // 게시글 상세 페이지 표시
            loadPostDetails(postId, boardType);
            const postDetailView = document.getElementById("post-detail-view");
            if (postDetailView) {
              postDetailView.style.display = "block";
            }
          }
        });
      });

      console.log(`${clickableRows.length}개의 게시글에 클릭 이벤트 등록 완료`);
    }

    // 게시판 API를 통해 게시물 목록 로드
    async function loadBoardData(boardType, page = 1, size = 10) {
      try {
        // API를 통해 게시물 목록 가져오기 (백엔드 URL 패턴에 맞게)
        const response = await BoardService.getPosts(boardType, {
          page: page,
          size: size,
          sort: "latest",
        }); // 백엔드 응답 구조에 맞게 처리
        // NewsController에서는 newsList 필드로 반환
        // FreeboardController에서는 freeboardList 필드로 반환
        const posts =
          response.newsList || response.freeboardList || response.posts || [];
        if (!posts || posts.length === 0) {
          console.error(
            `${boardType} 게시물 목록이 비어있거나 응답 형식이 올바르지 않습니다.`
          );
          return;
        }

        // 선택된 게시판의 테이블 찾기
        const boardContainer = document.getElementById(`${boardType}-board`);
        if (!boardContainer) return;

        const postsTable = boardContainer.querySelector(".board-table tbody");
        if (!postsTable) return;

        // 테이블 내용 비우기
        postsTable.innerHTML = "";

        // 각 게시물 정보 표시
        posts.forEach((post, index) => {
          const formattedDate = formatDate(
            post.newsWritetime || post.createdAt || new Date()
          );
          const commentCount = post.commentCount || 0;
          const commentDisplay =
            commentCount > 0
              ? ` <span class="comment-count">[${commentCount}]</span>`
              : "";

          // 새 행 생성
          const row = document.createElement("tr");
          row.className = "clickable-row";
          row.dataset.postId = post.newsId || post.id;
          row.dataset.boardType = boardType; // 게시판 타입도 데이터 속성으로 저장
          row.innerHTML = `
            <td>${post.newsId || post.id}</td>
            <td><a href="javascript:void(0)">${
              post.newsTitle || post.title
            }</a>${commentDisplay}</td>
            <td>${post.userName || post.author || "익명"}</td>
            <td>${formattedDate}</td>
            <td>${post.newsRead || post.views || 0}</td>
          `;

          postsTable.appendChild(row);
        });
      } catch (error) {
        console.error(`게시물 목록 로딩 중 오류 발생:`, error);
      }
    }

    // 게시물 상세 정보 로드 (boardType 형식: 'news' 또는 'free')
    async function loadPostDetail(boardType, postId) {
      try {
        console.log(
          `loadPostDetail 호출: boardType=${boardType}, postId=${postId}, 시간=${new Date().toISOString()}`
        );

        // boardType에 "-board" 접미사 추가 (loadPostDetails에서 사용하는 형식으로 변환)
        const boardTypeWithSuffix = boardType.endsWith("-board")
          ? boardType
          : `${boardType}-board`;

        // 상세 정보 로드 함수 호출 (세션 기반 조회수 제어 기능 포함)
        return loadPostDetails(postId, boardTypeWithSuffix);
      } catch (error) {
        console.error(`게시물 상세 정보 로딩 중 오류 발생:`, error);
      }
    }

    // 게시물 댓글 로드
    async function loadPostComments(boardType, postId) {
      try {
        // boardType에서 -board 접미사 제거 (API 호출용)
        const apiType = boardType.replace("-board", "");
        console.log(
          `loadPostComments 호출: boardType=${boardType}, apiType=${apiType}, postId=${postId}`
        );

        // 정수 변환 확인
        const numericPostId = parseInt(postId);
        if (isNaN(numericPostId)) {
          console.error(`유효하지 않은 게시글 ID: ${postId}`);
          detailCommentList.innerHTML =
            '<div class="error">유효하지 않은 게시글 ID입니다.</div>';
          return;
        }

        // 중복 코드 제거를 위해 loadComments 함수 호출
        return loadComments(apiType, numericPostId);
      } catch (error) {
        console.error("댓글 로드 오류:", error);
        detailCommentList.innerHTML =
          '<div class="error">댓글을 불러오는 중 오류가 발생했습니다.</div>';
      }
    }

    /**
     * 게시판 목록 로드
     * @param {string} boardType 게시판 타입 (news, free 등)
     * @param {Object} params 페이징 및 정렬 옵션
     */
    async function loadBoardPosts(boardType, params = {}) {
      try {
        console.log(`loadBoardPosts 호출: boardType=${boardType}`);
        const boardContainer = document.getElementById(`${boardType}-board`);
        if (!boardContainer) {
          console.error(
            `게시판 컨테이너를 찾을 수 없습니다: ${boardType}-board`
          );
          return;
        }

        const postsTable = boardContainer.querySelector(".board-table tbody");
        if (!postsTable) {
          console.error(
            `게시판 테이블을 찾을 수 없습니다: ${boardType}-board .board-table tbody`
          );
          return;
        }

        // 로딩 표시 추가
        postsTable.innerHTML =
          '<tr><td colspan="5" class="loading">게시글을 불러오는 중...</td></tr>';

        // 게시글 목록 로드 (Auth 체크 없이 모든 사용자가 볼 수 있게)
        const response = await BoardService.getPosts(boardType, params);
        console.log("게시판 API 응답:", response);

        // 응답 구조 처리
        // 가능한 응답 구조:
        // 1. 게시글 배열: [...]
        // 2. {newsList: [...]} (news 게시판용)
        // 3. {freeboardList: [...]} (freeboard 게시판용)
        // 4. {posts: [...]} (일반적인 형식)
        let posts = [];

        if (Array.isArray(response)) {
          // 응답이 바로 배열인 경우
          posts = response;
        } else if (response?.newsList) {
          // {newsList: [...]} 형태인 경우
          posts = response.newsList;
        } else if (response?.freeboardList) {
          // {freeboardList: [...]} 형태인 경우
          posts = response.freeboardList;
        } else if (response?.posts) {
          // {posts: [...]} 형태인 경우
          posts = response.posts;
        } else if (typeof response === "object") {
          // 기타 다른 형태의 객체 응답인 경우, 배열처럼 사용 가능한 값 찾기
          const possiblePostArrays = Object.values(response).filter((val) =>
            Array.isArray(val)
          );
          if (possiblePostArrays.length > 0) {
            // 첫 번째 발견된 배열 사용
            posts = possiblePostArrays[0];
          }
        }

        console.log("처리된 게시글 배열:", posts);

        if (posts && posts.length > 0) {
          postsTable.innerHTML = "";
          posts.forEach((post, index) => {
            // 게시물 데이터 표준화
            const postData = {
              id: post.newsId || post.freeboardId || post.id || index + 1,
              title:
                post.newsTitle ||
                post.freeboardTitle ||
                post.title ||
                "제목 없음",
              author: post.userName || post.author || "익명",
              createdAt:
                post.newsWritetime ||
                post.freeboardWritetime ||
                post.createdAt ||
                new Date(),
              views: post.newsRead || post.freeboardRead || post.views || 0,
              commentCount: post.commentCount || 0,
              hasFile: post.hasFile || false,
            };

            const formattedDate = formatDate(postData.createdAt);
            const commentDisplay =
              postData.commentCount > 0
                ? ` <span class="comment-count">[${postData.commentCount}]</span>`
                : "";
            const fileDisplay = postData.hasFile
              ? ' <span class="file-icon">📎</span>'
              : "";

            const row = document.createElement("tr");
            row.className = "clickable-row";
            row.dataset.postId = postData.id;
            row.dataset.boardType = boardType; // 게시판 타입도 데이터 속성으로 저장
            row.innerHTML = `
              <td>${postData.id}</td>
              <td><a href="javascript:void(0)">${postData.title}</a>${commentDisplay}${fileDisplay}</td>
              <td>${postData.author}</td>
              <td>${formattedDate}</td>
              <td>${postData.views}</td>
            `;

            postsTable.appendChild(row);
          });

          // 게시글 목록이 로드된 후 클릭 이벤트 다시 등록
          attachPostClickEvents();
        } else {
          postsTable.innerHTML =
            '<tr><td colspan="5">게시글이 없습니다.</td></tr>';
        }
      } catch (error) {
        console.error(`${boardType} 게시글 목록 로드 오류:`, error);
        const boardContainer = document.getElementById(`${boardType}-board`);
        if (boardContainer) {
          const postsTable = boardContainer.querySelector(".board-table tbody");
          if (postsTable) {
            postsTable.innerHTML =
              '<tr><td colspan="5" class="error">게시글을 불러오는 중 오류가 발생했습니다.</td></tr>';
          }
        }
      }
    }

    /**
     * 게시글 상세 정보 로드 (조회수 한 번만 증가)
     * @param {string} postId 게시글 ID
     * @param {string} boardType 게시판 타입 (news-board, free-board 등)
     */
    async function loadPostDetails(postId, boardType) {
      try {
        const startTime = Date.now();

        // 중복 호출 방지를 위한 임시 플래그 - 고유한 키를 생성해서 현재 요청 추적
        const requestKey = `requesting_${boardType}_${postId}`;

        // 이미 요청 중인지 확인
        if (sessionStorage.getItem(requestKey)) {
          console.log(
            `[${new Date().toISOString()}] 중복 요청 감지! 이미 ${boardType}-${postId}의 상세 정보를 요청 중입니다.`
          );
          return;
        }

        // 요청 시작 표시
        sessionStorage.setItem(requestKey, "true");

        console.log(
          `[${new Date().toISOString()}] loadPostDetails 시작: postId=${postId}, boardType=${boardType}`
        );

        // boardType에서 board- 접미사 제거하고 API 경로 생성
        const apiType = boardType.replace("-board", "");
        console.log(`API 호출 타입: ${apiType}`);

        // 세션 스토리지를 사용하여 이미 본 게시글 추적
        const viewedPostKey = `viewed_post_${apiType}_${postId}`;
        const hasViewedPost = sessionStorage.getItem(viewedPostKey);

        // 이미 본 게시글인지 확인하여 조회수 증가 여부 결정
        const shouldIncreaseReadCount = !hasViewedPost;

        console.log(
          `게시글 조회 상태: ${viewedPostKey}=${hasViewedPost}, 조회수 증가 여부: ${shouldIncreaseReadCount}`
        );

        try {
          // 게시글 정보 및 댓글 정보를 한 번에 가져오기 (처음 보는 경우에만 조회수 증가)
          const response = await BoardService.getPost(
            apiType,
            postId,
            shouldIncreaseReadCount
          );

          // 조회 기록 저장 (게시글 내용을 성공적으로 가져온 경우에만)
          if (
            response &&
            (response.news || response.post || response.status !== "error")
          ) {
            sessionStorage.setItem(viewedPostKey, "true");
            console.log(`게시글 조회 기록 저장: ${viewedPostKey}=true`);
          }

          console.log("게시글 응답:", response);
          // 응답 데이터 구조 자세히 출력
          if (response && (response.news || response.post)) {
            const post = response.news || response.post || response;
            console.log("게시글 구조:", Object.keys(post));
            console.log(
              "조회수 필드:",
              post.newsRead,
              post.freeboardRead,
              post.views
            );
            console.log(
              "추천수 필드:",
              post.newsRecommend,
              post.freeboardRecommend,
              post.likes
            );
            console.log(
              "내용 필드:",
              post.newsContents,
              post.freeboardContents,
              post.content
            );
          }

          if (response && (response.news || response.post)) {
            const post = response.news || response.post || response;
            // 서버 응답에서 댓글 정보 추출
            const comments = response.comments || [];

            // 게시글 정보 표시
            if (detailTitle) {
              detailTitle.textContent = post.newsTitle || post.title;
              detailTitle.dataset.postId = post.newsId || post.id;
              detailTitle.dataset.boardType = boardType;
            }

            // 작성자 정보 표시
            if (detailAuthor) {
              detailAuthor.textContent = post.userName || post.author || "익명";
            }

            // 날짜 정보 표시
            if (detailDate) {
              const postDate = post.newsDate || post.date || post.createdAt;
              const formattedDate = formatDate(postDate);
              detailDate.textContent = formattedDate;
            }

            // 조회수 정보 표시
            if (detailViews) {
              const views =
                post.newsRead !== undefined
                  ? post.newsRead
                  : post.freeboardRead !== undefined
                  ? post.freeboardRead
                  : post.views !== undefined
                  ? post.views
                  : 0;
              detailViews.textContent = views;
            }

            // 추천수 정보 표시
            if (detailLikes) {
              const likes =
                post.newsRecommend !== undefined
                  ? post.newsRecommend
                  : post.freeboardRecommend !== undefined
                  ? post.freeboardRecommend
                  : post.likes !== undefined
                  ? post.likes
                  : 0;
              detailLikes.textContent = likes;

              // 추천 버튼의 카운터도 함께 업데이트
              const likeCountDisplay = document.getElementById(
                "post-like-count-display"
              );
              if (likeCountDisplay) {
                likeCountDisplay.textContent = likes;
              }
            }

            // 게시글 내용 표시
            if (detailContent) {
              const content =
                post.newsContents !== undefined
                  ? post.newsContents
                  : post.freeboardContents !== undefined
                  ? post.freeboardContents
                  : post.content !== undefined
                  ? post.content
                  : "";
              detailContent.innerHTML = content;
            }

            // 댓글 수 표시
            if (detailCommentCount) {
              const commentCount = comments ? comments.length : 0;
              detailCommentCount.textContent =
                commentCount > 0 ? `${commentCount}개의 댓글` : "댓글 없음";
            }

            // 댓글 표시
            displayComments(comments);

            // 댓글이 없는 경우에도 댓글 목록을 서버에서 다시 한번 요청
            if (!comments || comments.length === 0) {
              // 추가 댓글 로드 (조회수 증가 없이)
              const commentsResponse = await loadPostComments(
                boardType,
                postId
              );
              if (
                commentsResponse &&
                commentsResponse.comments &&
                commentsResponse.comments.length > 0
              ) {
                displayComments(commentsResponse.comments);

                // 댓글 수 업데이트
                if (detailCommentCount) {
                  const commentCount = commentsResponse.comments.length;
                  detailCommentCount.textContent =
                    commentCount > 0 ? `${commentCount}개의 댓글` : "댓글 없음";
                }
              }
            }

            // 삭제 버튼 표시 여부 설정
            const postDeleteButton =
              document.getElementById("post-delete-button");
            if (postDeleteButton) {
              // 서버에서 받은 사용자 정보와 게시글 작성자 비교 로직을 여기에 추가
              // 임시로 항상 보이게 설정 (실제로는 권한 체크 필요)
              postDeleteButton.style.display = "block";
            }
          }
        } finally {
          // 요청 완료 표시 (성공 또는 실패와 관계없이)
          sessionStorage.removeItem(requestKey);
          console.log(
            `[${new Date().toISOString()}] loadPostDetails 완료: postId=${postId}, boardType=${boardType}, 소요시간=${
              Date.now() - startTime
            }ms`
          );
        }
      } catch (error) {
        console.error("게시글 상세 정보 로드 오류:", error);
      }
    }

    // 댓글만 표시하는 함수 분리
    function displayComments(comments) {
      // 댓글 표시 로직
      if (detailCommentList) {
        detailCommentList.innerHTML = "";

        if (!comments || comments.length === 0) {
          detailCommentList.innerHTML =
            '<p class="no-comments">아직 댓글이 없습니다. 첫 댓글을 작성해보세요!</p>';
          return;
        }

        // Array.isArray로 comments가 정말 배열인지 확인
        if (!Array.isArray(comments)) {
          console.error("댓글 데이터가 배열 형식이 아닙니다.", comments);
          detailCommentList.innerHTML =
            '<div class="error">댓글을 불러오는 중 오류가 발생했습니다.</div>';
          return;
        }

        // 댓글 목록 생성
        comments.forEach((comment) => {
          // 댓글 데이터 표준화
          const commentData = {
            author: comment.userName || comment.author || "익명",
            createdAt:
              comment.newsCommentWritetime ||
              comment.freeboardCommentWritetime ||
              comment.createdAt ||
              new Date(),
            content:
              comment.newsCommentContents ||
              comment.freeboardCommentContents ||
              comment.content ||
              "내용 없음",
          };

          const commentElement = document.createElement("div");
          commentElement.className = "comment";
          commentElement.innerHTML = `
            <div class="comment-meta">
              <span>${commentData.author}</span>
              <span>${formatDate(commentData.createdAt)}</span>
            </div>
            <div class="comment-content">
              <p>${commentData.content}</p>
            </div>
          `;
          detailCommentList.appendChild(commentElement);
        });
      }
    }

    // board.js - 게시글 삭제 버튼 이벤트 리스너
    const postDeleteButton = document.getElementById("post-delete-button");
    if (postDeleteButton) {
      postDeleteButton.addEventListener("click", async function () {
        // 삭제 확인
        if (!confirm("정말 이 게시글을 삭제하시겠습니까?")) {
          return;
        }

        const postId = detailTitle.dataset.postId;
        const boardType = detailTitle.dataset.boardType;

        if (!postId || !boardType) {
          alert("게시글 정보를 찾을 수 없습니다.");
          return;
        }

        // 게시판 타입에서 "-board" 제거
        const apiType = boardType.replace("-board", "");

        try {
          // 삭제 API 호출
          const result = await BoardService.deletePost(apiType, postId);

          if (result && (result.success || result.status === "success")) {
            alert("게시글이 삭제되었습니다.");

            // 게시글 목록 다시 로드
            loadBoardPosts(apiType);

            // 상세 보기 닫기
            postDetailView.style.display = "none";
          } else {
            const errorMsg =
              result && result.message
                ? result.message
                : "게시글 삭제에 실패했습니다.";
            alert(errorMsg);
          }
        } catch (error) {
          console.error("게시글 삭제 오류:", error);
          alert("게시글을 삭제하는 중 오류가 발생했습니다.");
        }
      });
    }

    /**
     * 댓글 목록 로드
     * @param {string} boardType 게시판 타입 (news, free 등)
     * @param {string} postId 게시글 ID
     */
    async function loadComments(boardType, postId) {
      try {
        console.log(
          `loadComments 호출: boardType=${boardType}, postId=${postId}`
        );

        // postId 값 확인 및 정수 변환
        if (!postId || isNaN(parseInt(postId))) {
          console.error(`유효하지 않은 게시글 ID: ${postId}`);
          detailCommentList.innerHTML =
            '<div class="error">유효하지 않은 게시글 ID입니다.</div>';
          return;
        }

        // 정수로 변환된 postId 사용
        const numericPostId = parseInt(postId);

        // 인증 없이도 댓글을 볼 수 있게 수정
        const response = await BoardService.getComments(
          boardType,
          numericPostId
        );
        console.log("댓글 응답 원본:", response);

        // 응답이 없거나 오류 응답인 경우 처리
        if (!response || response.status === "error") {
          console.error(
            `댓글 정보를 불러오는데 실패했습니다: ${
              response?.message || "알 수 없는 오류"
            }`
          );
          detailCommentList.innerHTML =
            '<div class="error">댓글을 불러오는 중 오류가 발생했습니다.</div>';
          return;
        }

        // 댓글 응답 구조 처리
        // 가능한 응답 구조:
        // 1. {comments: [...]}
        // 2. [...] (댓글 배열 직접 반환)
        // 3. {newsComments: [...]} (news 게시판용)
        // 4. {freeboardComments: [...]} (freeboard 게시판용)
        let comments = [];

        if (Array.isArray(response)) {
          // 응답이 바로 배열인 경우
          comments = response;
        } else if (response?.comments) {
          // {comments: [...]} 형태인 경우
          comments = response.comments;
        } else if (response?.newsComments) {
          // {newsComments: [...]} 형태인 경우
          comments = response.newsComments;
        } else if (response?.freeboardComments) {
          // {freeboardComments: [...]} 형태인 경우
          comments = response.freeboardComments;
        } else if (typeof response === "object") {
          // 기타 다른 형태의 객체 응답인 경우, 배열처럼 사용 가능한 값 찾기
          // newsList는 댓글이 아닌 게시글 목록이므로 제외
          const possibleCommentArrays = Object.entries(response)
            .filter(
              ([key, val]) =>
                Array.isArray(val) &&
                key !== "newsList" &&
                key !== "freeboardList" &&
                key !== "notificationList"
            )
            .map(([_, val]) => val);

          if (possibleCommentArrays.length > 0) {
            // 첫 번째 발견된 배열 사용
            comments = possibleCommentArrays[0];
          }
        }

        console.log("loadComments - 처리된 댓글 배열:", comments);

        // 댓글 카운트 요소 확인
        if (detailCommentCount) {
          detailCommentCount.textContent = comments.length || 0;
        }

        // 댓글 목록 요소 확인
        if (!detailCommentList) {
          console.error("댓글 목록 표시 영역을 찾을 수 없습니다.");
          return;
        }

        // 댓글 목록 생성
        detailCommentList.innerHTML = "";

        if (comments.length === 0) {
          detailCommentList.innerHTML =
            '<p class="no-comments">아직 댓글이 없습니다. 첫 댓글을 작성해보세요!</p>';
          return;
        }

        // Array.isArray로 comments가 정말 배열인지 확인
        if (!Array.isArray(comments)) {
          console.error("댓글 데이터가 배열 형식이 아닙니다.", comments);
          detailCommentList.innerHTML =
            '<div class="error">댓글을 불러오는 중 오류가 발생했습니다.</div>';
          return;
        }

        comments.forEach((comment) => {
          // 댓글 데이터 표준화
          const commentData = {
            author: comment.userName || comment.author || "익명",
            createdAt:
              comment.newsCommentWritetime ||
              comment.freeboardCommentWritetime ||
              comment.createdAt ||
              new Date(),
            content:
              comment.newsCommentContents ||
              comment.freeboardCommentContents ||
              comment.content ||
              "내용 없음",
          };

          const commentElement = document.createElement("div");
          commentElement.className = "comment";
          commentElement.innerHTML = `
            <div class="comment-meta">
              <span>${commentData.author}</span>
              <span>${formatDate(commentData.createdAt)}</span>
            </div>
            <div class="comment-content">
              <p>${commentData.content}</p>
            </div>
          `;
          detailCommentList.appendChild(commentElement);
        });
      } catch (error) {
        console.error("댓글 로드 오류:", error);
        detailCommentList.innerHTML =
          '<div class="error">댓글을 불러오는 중 오류가 발생했습니다.</div>';
      }
    }

    // 댓글 제출 버튼 클릭 이벤트
    if (detailCommentSubmitBtn) {
      detailCommentSubmitBtn.addEventListener("click", submitComment);
    }

    /**
     * 댓글 제출 처리
     */
    async function submitComment() {
      const commentInput = document.getElementById("detail-comment-input");
      if (!commentInput || !commentInput.value.trim()) {
        alert("댓글 내용을 입력해주세요.");
        return;
      }

      try {
        const postId = detailTitle.dataset.postId;
        const boardType = detailTitle.dataset.boardType;

        if (!postId || !boardType) {
          alert("게시글 정보를 찾을 수 없습니다.");
          return;
        }

        // boardType에서 board- 접미사 제거
        const apiType = boardType.replace("-board", "");

        console.log(
          `댓글 작성 시도: 게시판=${apiType}, ID=${postId}, 내용=${commentInput.value.trim()}`
        );

        // 댓글 작성 API 호출
        const result = await BoardService.createComment(
          apiType,
          postId,
          commentInput.value.trim()
        );

        console.log("댓글 작성 응답:", result);

        // 응답 구조 확인 및 처리
        if (result && (result.success || result.status === "success")) {
          // 댓글 작성 성공
          commentInput.value = ""; // 입력창 비우기

          // 댓글 목록 갱신
          loadComments(apiType, postId);

          // 성공 알림
          alert("댓글이 등록되었습니다.");
        } else {
          const errorMsg =
            result && result.message
              ? result.message
              : "댓글 작성에 실패했습니다.";
          alert(errorMsg);
        }
      } catch (error) {
        console.error("댓글 제출 오류:", error);
        alert("댓글을 작성하는 중 오류가 발생했습니다.");
      }
    }

    // 초기 데이터 로드 함수
    function initBoardData() {
      // 기본적으로 뉴스 게시판 로드
      loadBoardData("news");

      // 이전에 활성화된 탭이 있다면 그 탭의 게시물도 로드
      if (previouslyActiveBoardId !== "news-board") {
        const boardType = previouslyActiveBoardId.replace("-board", "");
        loadBoardData(boardType);
      }
    }
  }
});
