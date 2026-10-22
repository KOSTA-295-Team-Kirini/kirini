// ApiClient 객체 참조
const apiClient = window.ApiClient;
const userService = window.UserService;

document.addEventListener("DOMContentLoaded", function () {
  console.log("마이페이지 JavaScript 로드됨");

  // 탭 전환 기능
  document.querySelectorAll(".mypage-tab").forEach((tab) => {
    tab.addEventListener("click", () => {
      // 활성화된 탭 변경
      document.querySelectorAll(".mypage-tab").forEach((t) => {
        t.classList.remove("active");
      });
      tab.classList.add("active");

      // 해당 섹션 표시
      const tabId = tab.dataset.tab;
      document.querySelectorAll(".mypage-section").forEach((section) => {
        section.classList.remove("active");
      });
      document.getElementById(tabId).classList.add("active");

      // 해당 탭의 데이터 로드
      console.log(`${tabId} 탭 클릭됨`);
      loadTabData(tabId);
    });
  });

  // 꾸미기 아이템 선택 기능
  // 이벤트 위임 방식으로 변경 (동적 생성 요소에도 적용되도록)
  document
    .querySelector(".mypage-content")
    .addEventListener("click", function (e) {
      const item = e.target.closest(".customize-item");
      if (item) {
        const parent = item.parentElement;
        parent.querySelectorAll(".customize-item").forEach((i) => {
          i.classList.remove("active");
        });
        item.classList.add("active");
        console.log(`꾸미기 아이템 선택: ${item.dataset.itemId}`);
      }
    });

  // 프로필 수정 폼 제출 이벤트
  const profileForm = document.querySelector("#profile form");
  if (profileForm) {
    profileForm.addEventListener("submit", function (e) {
      e.preventDefault();
      updateProfile();
    });
  }

  // 회원 탈퇴 버튼 이벤트
  const deleteAccountBtn = document.querySelector(".btn-danger");
  if (deleteAccountBtn) {
    deleteAccountBtn.addEventListener("click", function () {
      if (confirm("정말로 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.")) {
        deleteAccount();
      }
    });
  }

  // 꾸미기 변경사항 저장 버튼 이벤트
  const saveCustomizeBtn = document.querySelector("#customize .btn");
  if (saveCustomizeBtn) {
    saveCustomizeBtn.addEventListener("click", function () {
      saveCustomization();
    });
  }

  // 초기 데이터 로드 (프로필 탭)
  loadTabData("profile");
});

/**
 * 탭 데이터 로드 함수
 * @param {string} tabId - 탭 ID (profile, scraps, posts, ratings, points, customize)
 * @param {number} page - 페이지 번호 (기본값: 1)
 */
async function loadTabData(tabId, page = 1) {
  console.log(`${tabId} 탭 데이터 로드 시작 (페이지: ${page})`);

  // 로딩 표시 시작
  showLoading(tabId);

  try {
    const response = await apiClient.get(
      `/mypage/api?endpoint=${tabId}&page=${page}`
    );
    console.log(`${tabId} 데이터 응답:`, response);

    // 응답이 없거나 invalid한 경우 처리
    if (!response) {
      throw new Error("서버로부터 응답을 받지 못했습니다.");
    }

    // 데이터 렌더링 (탭별로 다른 함수 호출)
    switch (tabId) {
      case "profile":
        renderProfile(response);
        break;
      case "scraps":
        renderScraps(response);
        break;
      case "posts":
        renderPosts(response);
        break;
      case "ratings":
        renderRatings(response);
        break;
      case "points":
        renderPoints(response);
        break;
      case "customize":
        renderCustomize(response);
        break;
    }

    // 로딩 표시 종료
    hideLoading(tabId);
  } catch (error) {
    console.error(`${tabId} 데이터 로드 중 오류 발생:`, error);
    // 오류 메시지 표시
    showError(tabId, "데이터 로드 중 오류가 발생했습니다.");
    // 로딩 표시 종료
    hideLoading(tabId);
  }
}

/**
 * 프로필 데이터 렌더링
 * @param {Object} data - 프로필 데이터
 */
function renderProfile(data) {
  console.log("프로필 데이터 렌더링:", data);

  // 프로필 기본 정보 표시
  const profileName = document.querySelector(".profile-details h3");
  const profileEmail = document.querySelector(".profile-details p");

  if (profileName) profileName.textContent = `${data.userName}님`;
  if (profileEmail) profileEmail.textContent = data.userEmail;

  // 폼 필드에 값 설정
  document.getElementById("username").value = data.userName || "";
  document.getElementById("email").value = data.userEmail || "";
  document.getElementById("bio").value = data.userIntroduce || "";

  // 비밀번호 필드 초기화
  document.getElementById("password").value = "";
  document.getElementById("password-confirm").value = "";
}

/**
 * 스크랩 데이터 렌더링
 * @param {Object} data - 스크랩 데이터 (items: 키보드 배열, pagination: 페이징 정보)
 */
function renderScraps(data) {
  console.log("스크랩 데이터 렌더링:", data);

  const container = document.querySelector("#scraps .keyboard-grid");

  // 컨테이너가 없으면 중단
  if (!container) {
    console.error("스크랩 컨테이너를 찾을 수 없습니다.");
    return;
  }

  // 컨테이너 내용 비우기
  container.innerHTML = "";

  // 데이터가 없는 경우
  if (!data.items || data.items.length === 0) {
    container.innerHTML = '<p class="no-data">스크랩한 키보드가 없습니다.</p>';
    return;
  }

  // 각 키보드 아이템 렌더링
  data.items.forEach((keyboard) => {
    const keyboardCard = document.createElement("div");
    keyboardCard.className = "keyboard-card";
    keyboardCard.innerHTML = `
      <img src="${
        keyboard.imageUrl || "../img/keyboard-placeholder.jpg"
      }" alt="${escapeHtml(keyboard.name)}"
           class="keyboard-image" onerror="this.src='https://via.placeholder.com/400x200?text=키보드+이미지'">
      <div class="keyboard-content">
        <h3>${escapeHtml(keyboard.name)}</h3>
        <p>${escapeHtml(keyboard.description || "설명 없음")}</p>
        <div class="keyboard-tags">
          ${
            keyboard.tags
              ? keyboard.tags
                  .map((tag) => `<span class="tag">${escapeHtml(tag)}</span>`)
                  .join("")
              : ""
          }
        </div>
        <div class="keyboard-actions">
          <a href="/keyboard/detail.do?id=${
            keyboard.id
          }" class="btn btn-small">상세보기</a>
        </div>
      </div>
    `;
    container.appendChild(keyboardCard);
  });

  // 페이징 컨트롤 생성
  renderPagination("scraps", data.pagination);
}

/**
 * 내가 쓴 글 데이터 렌더링
 * @param {Object} data - 글 데이터 (items: 게시글 배열, pagination: 페이징 정보)
 */
function renderPosts(data) {
  console.log("내가 쓴 글 데이터 렌더링:", data);

  const container = document.querySelector("#posts .item-list");

  if (!container) {
    console.error("게시글 컨테이너를 찾을 수 없습니다.");
    return;
  }

  // 컨테이너 내용 비우기
  container.innerHTML = "";

  // 데이터가 없는 경우
  if (!data.items || data.items.length === 0) {
    container.innerHTML = '<p class="no-data">작성한 게시글이 없습니다.</p>';
    return;
  }

  // 각 게시글 아이템 렌더링
  data.items.forEach((post) => {
    const postItem = document.createElement("li");
    postItem.className = "item-card";

    // 게시판 종류에 따른 URL 생성
    const boardUrl = getBoardUrl(post.boardType, post.postId);

    postItem.innerHTML = `
      <div class="item-info">
        <h3><a href="${escapeHtml(boardUrl)}">${escapeHtml(post.title)}</a></h3>
        <p class="item-meta">
          <span>${escapeHtml(post.boardName || "게시판")}</span> |
          <span>조회 ${post.viewCount}</span> | 
          <span>${formatDate(post.createdAt)}</span>
        </p>
      </div>
      <div class="item-actions">
        <a href="${boardUrl}" class="btn btn-small">보기</a>
      </div>
    `;
    container.appendChild(postItem);
  });

  // 페이징 컨트롤 생성
  renderPagination("posts", data.pagination);
}

/**
 * 별점 내역 데이터 렌더링
 * @param {Object} data - 별점 데이터 (items: 별점 배열, pagination: 페이징 정보)
 */
function renderRatings(data) {
  console.log("별점 내역 데이터 렌더링:", data);

  const container = document.querySelector("#ratings .rating-list");

  if (!container) {
    console.error("별점 컨테이너를 찾을 수 없습니다.");
    return;
  }

  // 컨테이너 내용 비우기
  container.innerHTML = "";

  // 데이터가 없는 경우
  if (!data.items || data.items.length === 0) {
    container.innerHTML = '<p class="no-data">별점 평가 내역이 없습니다.</p>';
    return;
  }

  // 각 별점 아이템 렌더링
  data.items.forEach((rating) => {
    const ratingItem = document.createElement("div");
    ratingItem.className = "rating-item";

    // 별점 표시 (★☆)
    const stars = getStarRating(rating.score);

    ratingItem.innerHTML = `
      <div class="rating-info">
        <h3><a href="/keyboard/detail.do?id=${rating.keyboardId}">${escapeHtml(
      rating.keyboardName
    )}</a></h3>
        <p>${escapeHtml(rating.comment || "평가 코멘트 없음")}</p>
        <p class="rating-date">${formatDate(rating.ratedAt)}</p>
      </div>
      <div class="rating-stars">${stars}</div>
    `;
    container.appendChild(ratingItem);
  });

  // 페이징 컨트롤 생성
  renderPagination("ratings", data.pagination);
}

/**
 * 활동 포인트 데이터 렌더링
 * @param {Object} data - 포인트 데이터 (currentPoints: 현재 포인트, history: {items: 포인트 내역 배열, pagination: 페이징 정보})
 */
function renderPoints(data) {
  console.log("활동 포인트 데이터 렌더링:", data);

  // 현재 포인트 표시
  const pointValue = document.querySelector(".point-value");
  if (pointValue) {
    pointValue.textContent = data.currentPoints.toLocaleString();
  }

  // 포인트 내역 컨테이너
  const container = document.querySelector(".point-history");

  if (!container) {
    console.error("포인트 내역 컨테이너를 찾을 수 없습니다.");
    return;
  }

  // 컨테이너 내용 비우기
  container.innerHTML = "";

  // 데이터가 없는 경우
  if (!data.history || !data.history.items || data.history.items.length === 0) {
    container.innerHTML = '<p class="no-data">포인트 내역이 없습니다.</p>';
    return;
  }

  // 각 포인트 내역 아이템 렌더링
  data.history.items.forEach((item) => {
    const pointItem = document.createElement("div");
    pointItem.className = "point-item";

    // 포인트 증감에 따른 클래스 설정
    const changeClass = item.pointAmount > 0 ? "plus" : "minus";
    const pointText =
      item.pointAmount > 0 ? `+${item.pointAmount}` : `${item.pointAmount}`;

    pointItem.innerHTML = `
      <div class="point-desc">${escapeHtml(item.description)}</div>
      <div class="point-change ${changeClass}">${pointText}</div>
      <div class="point-date">${formatDate(item.createdAt)}</div>
    `;
    container.appendChild(pointItem);
  });

  // 페이징 컨트롤 생성
  renderPagination("points", data.history.pagination);
}

/**
 * 꾸미기 데이터 렌더링
 * @param {Object} data - 꾸미기 데이터 (items: 아이템 배열, userSettings: 사용자 설정)
 */
function renderCustomize(data) {
  console.log("꾸미기 데이터 렌더링:", data);

  // 아이콘 영역 렌더링
  renderCustomizeSection(
    "icons",
    data.items.icons,
    data.userSettings.selectedIconId
  );

  // 테마 영역 렌더링
  renderCustomizeSection(
    "themes",
    data.items.themes,
    data.userSettings.selectedThemeId
  );
}

/**
 * 꾸미기 섹션 렌더링 (아이콘 또는 테마)
 * @param {string} type - 섹션 타입 ('icons' 또는 'themes')
 * @param {Array} items - 아이템 배열
 * @param {string} selectedId - 현재 선택된 아이템 ID
 */
function renderCustomizeSection(type, items, selectedId) {
  // 컨테이너 선택 (아이콘 또는 테마)
  const container = document.querySelector(
    type === "icons"
      ? "#customize .item-customize:nth-child(1) .customize-options"
      : "#customize .item-customize:nth-child(2) .customize-options"
  );

  if (!container) {
    console.error(`${type} 컨테이너를 찾을 수 없습니다.`);
    return;
  }

  // 컨테이너 내용 비우기
  container.innerHTML = "";

  // 데이터가 없는 경우
  if (!items || items.length === 0) {
    container.innerHTML =
      '<p class="no-data">사용 가능한 아이템이 없습니다.</p>';
    return;
  }

  // 각 아이템 렌더링
  items.forEach((item) => {
    const customizeItem = document.createElement("div");
    customizeItem.className = "customize-item";
    customizeItem.dataset.itemId = item.id;

    // 현재 선택된 아이템인 경우 active 클래스 추가
    if (item.id === selectedId) {
      customizeItem.classList.add("active");
    }

    // 아이템 타입에 따라 내용 다르게 구성
    if (type === "icons") {
      customizeItem.innerHTML = `
        <div class="icon-preview">${item.iconHtml || "👤"}</div>
        <p>${escapeHtml(item.name)}</p>
        <p class="item-cost">${
          item.cost > 0 ? `${item.cost} 포인트` : "기본"
        }</p>
      `;
    } else {
      customizeItem.innerHTML = `
        <div class="theme-preview" style="background-color: ${
          item.previewColor || "#f0f0f0"
        }"></div>
        <p>${escapeHtml(item.name)}</p>
        <p class="item-cost">${
          item.cost > 0 ? `${item.cost} 포인트` : "기본"
        }</p>
      `;
    }

    container.appendChild(customizeItem);
  });
}

/**
 * 페이징 컨트롤 렌더링
 * @param {string} tabId - 탭 ID
 * @param {Object} pagination - 페이징 정보 (currentPage, pageSize, totalItems, totalPages)
 */
function renderPagination(tabId, pagination) {
  console.log(`${tabId} 페이징 렌더링:`, pagination);

  // 페이징 컨트롤 컨테이너
  let container = document.querySelector(`#${tabId} .pagination-controls`);

  // 컨테이너가 없으면 생성
  if (!container) {
    container = document.createElement("div");
    container.className = "pagination-controls";
    document.getElementById(tabId).appendChild(container);
  }

  // 컨테이너 내용 비우기
  container.innerHTML = "";

  // 페이징이 필요 없는 경우 중단
  if (!pagination || pagination.totalPages <= 1) {
    return;
  }

  // 페이징 UI 생성
  const currentPage = pagination.currentPage;
  const totalPages = pagination.totalPages;

  // 이전 페이지 버튼
  const prevBtn = document.createElement("button");
  prevBtn.className = "pagination-btn prev";
  prevBtn.textContent = "이전";
  prevBtn.disabled = currentPage <= 1;
  prevBtn.addEventListener("click", () => loadTabData(tabId, currentPage - 1));
  container.appendChild(prevBtn);

  // 페이지 번호 버튼들
  const startPage = Math.max(1, currentPage - 2);
  const endPage = Math.min(totalPages, startPage + 4);

  for (let i = startPage; i <= endPage; i++) {
    const pageBtn = document.createElement("button");
    pageBtn.className = "pagination-btn page-num";
    pageBtn.textContent = i;

    if (i === currentPage) {
      pageBtn.classList.add("active");
    } else {
      pageBtn.addEventListener("click", () => loadTabData(tabId, i));
    }

    container.appendChild(pageBtn);
  }

  // 다음 페이지 버튼
  const nextBtn = document.createElement("button");
  nextBtn.className = "pagination-btn next";
  nextBtn.textContent = "다음";
  nextBtn.disabled = currentPage >= totalPages;
  nextBtn.addEventListener("click", () => loadTabData(tabId, currentPage + 1));
  container.appendChild(nextBtn);
}

/**
 * 프로필 정보 업데이트
 */
async function updateProfile() {
  console.log("프로필 정보 업데이트 시작");

  // 폼 데이터 가져오기
  const username = document.getElementById("username").value;
  const email = document.getElementById("email").value;
  const password = document.getElementById("password").value;
  const passwordConfirm = document.getElementById("password-confirm").value;
  const bio = document.getElementById("bio").value;

  // 유효성 검사
  if (!username.trim()) {
    alert("이름을 입력해주세요.");
    return;
  }

  if (!email.trim() || !email.includes("@")) {
    alert("유효한 이메일 주소를 입력해주세요.");
    return;
  }

  // 비밀번호 변경 시 검증
  if (password) {
    if (password !== passwordConfirm) {
      alert("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
      return;
    }

    if (password.length < 8) {
      alert("비밀번호는 8자 이상이어야 합니다.");
      return;
    }
  }
  // 프로필 데이터 객체 생성
  const profileData = {
    username,
    email,
    bio,
  };

  // 비밀번호가 입력된 경우에만 추가
  if (password) {
    profileData.password = password;
    profileData.passwordConfirm = passwordConfirm;
  }

  try {
    // 로딩 표시
    showLoading("profile");

    // API 요청
    const response = await apiClient.postJson(
      "/mypage/api?endpoint=updateProfile",
      profileData
    );
    console.log("프로필 업데이트 응답:", response);

    if (response && response.success) {
      alert("프로필 정보가 성공적으로 업데이트되었습니다.");
      // 프로필 데이터 다시 로드
      loadTabData("profile");
    } else {
      alert(
        `프로필 업데이트 실패: ${
          response && response.message
            ? response.message
            : "알 수 없는 오류가 발생했습니다."
        }`
      );
    }
  } catch (error) {
    console.error("프로필 업데이트 중 오류 발생:", error);
    alert("프로필 정보 업데이트 중 오류가 발생했습니다.");
  } finally {
    // 로딩 표시 해제
    hideLoading("profile");
  }
}

/**
 * 꾸미기 설정 저장
 */
async function saveCustomization() {
  console.log("꾸미기 설정 저장 시작");

  // 선택된 아이템 ID 가져오기
  const selectedIconItem = document.querySelector(
    "#customize .item-customize:nth-child(1) .customize-item.active"
  );
  const selectedThemeItem = document.querySelector(
    "#customize .item-customize:nth-child(2) .customize-item.active"
  );

  if (!selectedIconItem || !selectedThemeItem) {
    alert("아이콘과 테마를 모두 선택해주세요.");
    return;
  }

  const selectedIconId = selectedIconItem.dataset.itemId;
  const selectedThemeId = selectedThemeItem.dataset.itemId;

  console.log(
    `선택된 아이콘 ID: ${selectedIconId}, 테마 ID: ${selectedThemeId}`
  );

  try {
    // 로딩 표시
    showLoading("customize");

    // API 요청
    const response = await apiClient.postJson(
      "/mypage/api?endpoint=saveCustomization",
      {
        selectedIconId,
        selectedThemeId,
      }
    );

    console.log("꾸미기 설정 저장 응답:", response);

    if (response.success) {
      alert("꾸미기 설정이 저장되었습니다.");
    } else {
      alert(
        `꾸미기 설정 저장 실패: ${
          response.message || "알 수 없는 오류가 발생했습니다."
        }`
      );
    }
  } catch (error) {
    console.error("꾸미기 설정 저장 중 오류 발생:", error);
    alert("꾸미기 설정 저장 중 오류가 발생했습니다.");
  } finally {
    // 로딩 표시 해제
    hideLoading("customize");
  }
}

/**
 * 회원 탈퇴 요청
 */
async function deleteAccount() {
  console.log("회원 탈퇴 요청 시작");

  // 비밀번호 확인
  const confirmPassword = prompt("계정 삭제를 위해 비밀번호를 입력해주세요.");

  if (!confirmPassword) {
    return; // 취소함
  }

  try {
    // 로딩 표시
    showLoading("profile"); // API 요청
    const response = await apiClient.postJson(
      "/mypage/api?endpoint=deleteAccount",
      {
        confirmPassword,
      }
    );

    console.log("회원 탈퇴 응답:", response);

    if (response && response.success) {
      alert("계정이 성공적으로 삭제되었습니다.");
      // 로그아웃 처리 후 메인 페이지로 이동
      if (window.UserService && window.UserService.logout) {
        window.UserService.logout();
      } else {
        // UserService가 없는 경우 직접 로그아웃 처리
        ApiClient.clearAuthToken();
      }
      window.location.href = "/";
    } else {
      alert(
        `계정 삭제 실패: ${
          response && response.message
            ? response.message
            : "알 수 없는 오류가 발생했습니다."
        }`
      );
    }
  } catch (error) {
    console.error("회원 탈퇴 중 오류 발생:", error);
    alert("회원 탈퇴 처리 중 오류가 발생했습니다.");
  } finally {
    // 로딩 표시 해제
    hideLoading("profile");
  }
}

/**
 * 로딩 인디케이터 표시
 * @param {string} tabId - 탭 ID
 */
function showLoading(tabId) {
  // 기존 로딩 인디케이터가 있는지 확인
  let loadingEl = document.querySelector(`#${tabId} .loading-indicator`);

  // 없으면 생성
  if (!loadingEl) {
    loadingEl = document.createElement("div");
    loadingEl.className = "loading-indicator";
    loadingEl.innerHTML = "<p>로딩 중...</p>";

    // 탭 컨텐츠 영역의 상단에 추가
    const tabSection = document.getElementById(tabId);
    if (tabSection) {
      tabSection.insertBefore(loadingEl, tabSection.firstChild);
    }
  }

  // 표시
  loadingEl.style.display = "block";
}

/**
 * 로딩 인디케이터 숨기기
 * @param {string} tabId - 탭 ID
 */
function hideLoading(tabId) {
  const loadingEl = document.querySelector(`#${tabId} .loading-indicator`);
  if (loadingEl) {
    loadingEl.style.display = "none";
  }
}

/**
 * 오류 메시지 표시
 * @param {string} tabId - 탭 ID
 * @param {string} message - 오류 메시지
 */
function showError(tabId, message) {
  // 기존 오류 메시지가 있으면 제거
  const existingError = document.querySelector(`#${tabId} .error-message`);
  if (existingError) {
    existingError.remove();
  }

  // 새 오류 메시지 요소 생성
  const errorEl = document.createElement("div");
  errorEl.className = "error-message";
  errorEl.textContent = message;

  // 탭 컨텐츠 영역에 추가
  const tabSection = document.getElementById(tabId);
  if (tabSection) {
    tabSection.insertBefore(errorEl, tabSection.firstChild);
  }

  // 5초 후 자동으로 사라지게 설정
  setTimeout(() => {
    errorEl.style.opacity = "0";
    setTimeout(() => errorEl.remove(), 500);
  }, 5000);
}

/**
 * 별점을 ★☆ 형태로 변환
 * @param {number} score - 별점 (1-5)
 * @returns {string} 별 문자열
 */
function getStarRating(score) {
  const roundedScore = Math.round(score);
  const fullStars = "★".repeat(roundedScore);
  const emptyStars = "☆".repeat(5 - roundedScore);
  return fullStars + emptyStars;
}

/**
 * 게시판 타입에 따른 URL 생성
 * @param {string} boardType - 게시판 타입
 * @param {number} postId - 게시글 ID
 * @returns {string} 게시글 URL
 */
function getBoardUrl(boardType, postId) {
  switch (boardType) {
    case "free":
      return `/freeboard/view.do?postId=${postId}`;
    case "news":
      return `/news/view.do?postId=${postId}`;
    case "qna":
      return `/qna/view.do?postId=${postId}`;
    default:
      return `/board/view.do?type=${boardType}&postId=${postId}`;
  }
}

/**
 * 날짜 포맷팅
 * @param {string} dateString - 날짜 문자열
 * @returns {string} 포맷된 날짜
 */
function formatDate(dateString) {
  if (!dateString) return "";

  const date = new Date(dateString);

  // 유효한 날짜인지 확인
  if (isNaN(date.getTime())) {
    return dateString;
  }

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");

  return `${year}-${month}-${day}`;
}
