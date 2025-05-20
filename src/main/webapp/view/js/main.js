/**
 * KIRINI 웹사이트 메인 JavaScript
 * 키보드 입문자를 위한 웹사이트의 상호작용 기능 구현
 */

// DOM이 완전히 로드된 후 실행
document.addEventListener("DOMContentLoaded", function () {
  initializeNavHighlight();
  initializeSearchFunctions();
  initializeMobileMenu();
  initializeDictionarySearch();
});

// 용어집 검색 결과 동적 표시 기능 (dictionary.html에서만 동작)
function initializeDictionarySearch() {
  if (!window.location.pathname.includes("dictionary.html")) return;
  const input = document.getElementById("search-input");
  const results = document.getElementById("search-results");

  // 용어 데이터 (추후 확장 가능)
  const terms = [
    // 스위치 관련 용어
    {
      name: "축",
      desc: "키보드 스위치의 전반적인 종류와 개념",
      link: "../axis.html",
    },
    {
      name: "적축",
      desc: "리니어 타입, 부드럽고 조용한 키감",
      link: "../axis_red.html",
    },
    {
      name: "갈축",
      desc: "넌클릭 타입, 약간의 구분감과 적당한 키압",
      link: "../axis_brown.html",
    },
    {
      name: "청축",
      desc: "클릭 타입, 뚜렷한 클릭음과 구분감",
      link: "../axis_blue.html",
    },
    {
      name: "은축",
      desc: "가벼운 키압의 리니어 타입, 빠른 연타에 적합",
      link: "../axis_silver.html",
    },
    {
      name: "흑축",
      desc: "무거운 키압의 리니어 타입, 오타 방지에 적합",
      link: "../axis_black.html",
    },
    {
      name: "스위치",
      desc: "키보드의 각 키 아래에 위치하는 기계식 소자",
      link: "../switch.html",
    },

    // 키캡 관련 용어
    {
      name: "키캡",
      desc: "키보드의 각 키 위에 장착되는 플라스틱 덕게",
      link: "../keycap.html",
    },
    {
      name: "PBT 키캡",
      desc: "내구성이 좋고 오래 사용해도 광이 나지 않는 키캡 소재",
      link: "../keycap_pbt.html",
    },
    {
      name: "ABS 키캡",
      desc: "일반적인 키캡 소재, 사용할수록 광이 나는 특성",
      link: "../keycap_abs.html",
    },
    {
      name: "더블샷 키캡",
      desc: "두 종류의 플라스틱을 사용해 제작된 키캡, 물리적 내구성 좋음",
      link: "../keycap_doubleshot.html",
    },
    {
      name: "다이서브 키캡",
      desc: "플라스틱에 색을 입혀 제작한 키캡, 색상이 어둡고 선명함",
      link: "../keycap_dyesub.html",
    },

    // 키보드 형태 관련 용어
    {
      name: "풀사이즈 키보드",
      desc: "104/108키 구성의 전체 키보드",
      link: "../keyboard_fullsize.html",
    },
    {
      name: "TKL",
      desc: "텐키리스, 숫자패드가 없는 87/88키 구성의 키보드",
      link: "../keyboard_tkl.html",
    },
    {
      name: "75%",
      desc: "숫자패드가 없고 키 간격이 좀 더 조밀한 84키 구성",
      link: "../keyboard_75.html",
    },
    {
      name: "65%",
      desc: "화살표와 페이지 이동 키를 유지한 65~68키 구성",
      link: "../keyboard_65.html",
    },
    {
      name: "60%",
      desc: "최소한의 키만 유지한 60~61키 구성",
      link: "../keyboard_60.html",
    },

    // 기타 키보드 관련 용어
    {
      name: "스타빗라이저",
      desc: "긴 키의 안정성을 높이는 기계장치",
      link: "../stabilizer.html",
    },
    {
      name: "소리흡수재",
      desc: "키보드 소음을 줄이는 내부 장착 소재",
      link: "../dampener.html",
    },
    {
      name: "핫스왑",
      desc: "키보드 바로 사용 가능한 PCB 및 스위치 구조",
      link: "../hotswap.html",
    },
    {
      name: "윈도우 키",
      desc: "Windows 로고가 있는 키, 윈도우 운영체제에서 사용",
      link: "../windows_key.html",
    },
    {
      name: "ANSI 배열",
      desc: "미국 표준 키보드 배열, 일자 모양 엔터키 특징",
      link: "../ansi_layout.html",
    },
    {
      name: "ISO 배열",
      desc: "유럽 표준 키보드 배열, 7자 모양 엔터키 특징",
      link: "../iso_layout.html",
    },
  ];

  function renderResults(filtered) {
    results.innerHTML = "";
    if (filtered.length === 0) {
      // 검색 결과가 없을 때 더 자세한 메시지 표시
      results.innerHTML = `
        <li style="color:#888; margin:20px 0; line-height:1.6;">
          <div style="font-size:1.1em; margin-bottom:10px;">검색 결과가 없습니다.</div>
          <div>다른 키워드를 사용해보세요. 예시: 축, 적축, 갈축, 청축 등</div>
        </li>
        <li style="margin-top:30px;">
          <div style="font-weight:bold; color:#ff7043; margin-bottom:10px;">인기 검색어</div>
          <div>
            <a href="../keyboard_terms/axis.html" style="display:inline-block; margin:5px 10px 5px 0; padding:5px 12px; background:#fff3e0; border-radius:4px; text-decoration:none; color:#ff7043;">축</a>
            <a href="../keyboard_terms/axis_red.html" style="display:inline-block; margin:5px 10px 5px 0; padding:5px 12px; background:#fff3e0; border-radius:4px; text-decoration:none; color:#ff7043;">적축</a>
            <a href="../keyboard_terms/axis_brown.html" style="display:inline-block; margin:5px 10px 5px 0; padding:5px 12px; background:#fff3e0; border-radius:4px; text-decoration:none; color:#ff7043;">갈축</a>
          </div>
        </li>
      `;
      return;
    }
    filtered.forEach((term) => {
      const li = document.createElement("li");
      li.style.margin = "18px 0";
      li.innerHTML = `<a href="${term.link}" style="font-weight:bold; font-size:1.13em;">${term.name}</a><br><span style='font-size:0.97em;color:#666;'>${term.desc}</span>`;
      results.appendChild(li);
    });
  }

  input.addEventListener("input", function () {
    const keyword = input.value.trim().toLowerCase();
    if (keyword === "") {
      // 검색어가 없을 때 초기 인기 검색어 표시
      results.innerHTML = `
        <li style="margin-top:30px;">
          <div style="font-weight:bold; color:#ff7043; margin-bottom:10px;">인기 검색어</div>
          <div>
            <a href="../keyboard_terms/axis.html" style="display:inline-block; margin:5px 10px 5px 0; padding:5px 12px; background:#fff3e0; border-radius:4px; text-decoration:none; color:#ff7043;">축</a>
            <a href="../keyboard_terms/axis_red.html" style="display:inline-block; margin:5px 10px 5px 0; padding:5px 12px; background:#fff3e0; border-radius:4px; text-decoration:none; color:#ff7043;">적축</a>
            <a href="../keyboard_terms/axis_brown.html" style="display:inline-block; margin:5px 10px 5px 0; padding:5px 12px; background:#fff3e0; border-radius:4px; text-decoration:none; color:#ff7043;">갈축</a>
          </div>
        </li>
      `;
      return;
    }
    const filtered = terms.filter(
      (term) =>
        term.name.toLowerCase().includes(keyword) ||
        term.desc.toLowerCase().includes(keyword)
    );
    renderResults(filtered);
  });

  // 첫 진입시 자동 포커스
  input.focus();
}

/**
 * 현재 페이지에 해당하는 네비게이션 항목에 하이라이트 표시
 */
function initializeNavHighlight() {
  const currentPath = window.location.pathname;
  const navLinks = document.querySelectorAll("nav a");

  navLinks.forEach((link) => {
    const linkPath = link.getAttribute("href");
    if (
      currentPath.includes(linkPath) &&
      linkPath !== "../index.html" &&
      linkPath !== "index.html"
    ) {
      link.classList.add("active");
    }
  });
}

/**
 * 검색 기능 초기화
 */
function initializeSearchFunctions() {
  const searchBars = document.querySelectorAll(".search-bar");

  searchBars.forEach((searchBar) => {
    const searchInput = searchBar.querySelector("input");
    const searchButton = searchBar.querySelector("button");

    if (searchInput && searchButton) {
      searchButton.addEventListener("click", () => {
        const query = searchInput.value.trim();
        if (query.length > 0) {
          handleSearch(query);
        }
      });

      searchInput.addEventListener("keypress", (e) => {
        if (e.key === "Enter") {
          const query = searchInput.value.trim();
          if (query.length > 0) {
            handleSearch(query);
          }
        }
      });
    }
  });
}

/**
 * 검색 처리 함수
 * @param {string} query - 검색어
 */
function handleSearch(query) {
  // 현재 페이지의 경로를 확인하여 어떤 종류의 검색을 수행할지 결정
  const currentPath = window.location.pathname;
  let searchType = "general";

  if (currentPath.includes("dictionary")) {
    searchType = "dictionary";
  } else if (currentPath.includes("database")) {
    searchType = "keyboard";
  } else if (currentPath.includes("qna")) {
    searchType = "qna";
  } else if (currentPath.includes("board")) {
    searchType = "board";
  }

  // 실제 구현에서는 서버로 AJAX 요청을 보내 검색 결과를 가져옴
  console.log(`Searching for "${query}" in ${searchType} section`);

  // 간단한 데모 알림
  alert(`"${query}"에 대한 검색을 시작합니다. (${searchType} 검색)`);

  // 현재는 페이지 새로고침으로 시뮬레이션
  // 실제 구현에서는 AJAX로 결과를 가져와 동적으로 표시
  window.location.href = `?search=${encodeURIComponent(
    query
  )}&type=${searchType}`;
}

/**
 * 모바일 메뉴 기능 초기화
 */
function initializeMobileMenu() {
  // 모바일 메뉴 토글 버튼이 있는지 확인
  const menuToggle = document.querySelector(".mobile-menu-toggle");

  if (menuToggle) {
    const nav = document.querySelector("nav");

    menuToggle.addEventListener("click", () => {
      nav.classList.toggle("mobile-active");
      menuToggle.classList.toggle("active");
    });
  }
}

/**
 * 쿠키를 설정하는 함수
 * @param {string} name - 쿠키 이름
 * @param {string} value - 쿠키 값
 * @param {number} days - 쿠키 유효 기간(일)
 */
function setCookie(name, value, days) {
  let expires = "";
  if (days) {
    const date = new Date();
    date.setTime(date.getTime() + days * 24 * 60 * 60 * 1000);
    expires = "; expires=" + date.toUTCString();
  }
  document.cookie =
    name + "=" + encodeURIComponent(value) + expires + "; path=/";
}

/**
 * 쿠키를 가져오는 함수
 * @param {string} name - 쿠키 이름
 * @return {string|null} 쿠키 값 또는 null
 */
function getCookie(name) {
  const nameEQ = name + "=";
  const ca = document.cookie.split(";");
  for (let i = 0; i < ca.length; i++) {
    let c = ca[i];
    while (c.charAt(0) === " ") c = c.substring(1, c.length);
    if (c.indexOf(nameEQ) === 0) {
      return decodeURIComponent(c.substring(nameEQ.length, c.length));
    }
  }
  return null;
}

/**
 * 게시판 글 작성 기능
 * @param {string} boardType - 게시판 유형 (news, free, anonymous)
 * @param {string} title - 글 제목
 * @param {string} content - 글 내용
 * @param {File|null} file - 첨부 파일
 */
async function submitPost(boardType, title, content, file) {
  // 로깅
  console.log("게시글 등록:", { boardType, title, content, file });

  // 간단한 유효성 검사
  if (!title || title.trim().length === 0) {
    alert("제목을 입력해주세요.");
    return false;
  }

  if (!content || content.trim().length === 0) {
    alert("내용을 입력해주세요.");
    return false;
  }

  try {
    // BoardService를 이용하여 서버에 게시글 제출
    const boardTypeMapping = {
      news: "news",
      free: "freeboard",
      anonymous: "anonymous",
    };

    // API 경로 생성
    const mappedType = boardTypeMapping[boardType] || boardType; // FormData 객체를 생성하여 데이터 추가
    const formData = new FormData();

    // 게시판 타입에 따라 파라미터 이름 설정
    if (boardType === "news") {
      // FormData 대신 일반 객체를 사용해 JSON으로 전송
      const newsData = {
        newsTitle: title,
        newsContents: content,
        userId: 1,
      };

      console.log("뉴스 게시글 데이터:", newsData);

      // API 호출 (BoardService 사용 또는 ApiClient 직접 사용)
      let response;
      if (window.BoardService) {
        response = await window.BoardService.createPost(mappedType, newsData);
      } else {
        response = await window.ApiClient.postJson(
          `/${mappedType}/create`,
          newsData,
          true
        );
      }

      if (
        response &&
        (response.status === "success" || response.success === true)
      ) {
        alert("게시글이 성공적으로 등록되었습니다.");

        // 게시판 데이터 다시 로드
        if (window.loadBoardData) {
          loadBoardData(boardType);
        }

        // 페이지 새로고침 추가
        setTimeout(() => {
          window.location.reload();
        }, 500); // 0.5초 후 새로고침(알림 확인 시간)

        return true;
      } else {
        const errorMsg = response?.message || "게시글 등록에 실패했습니다.";
        alert(errorMsg);
        console.error("게시글 등록 실패:", response);
        return false;
      }
    } else if (boardType === "free") {
      formData.append("freeboardTitle", title);
      formData.append("freeboardContents", content);
      formData.append("userId", "1");

      if (file) {
        formData.append("file", file);
      }

      // API 호출 (BoardService 사용 또는 ApiClient 직접 사용)
      let response;
      if (window.BoardService) {
        response = await window.BoardService.createPost(mappedType, formData);
      } else {
        response = await window.ApiClient.postFormData(
          `/${mappedType}/create`,
          formData,
          true
        );
      }

      if (
        response &&
        (response.status === "success" || response.success === true)
      ) {
        alert("게시글이 성공적으로 등록되었습니다.");

        // 게시판 데이터 다시 로드
        if (window.loadBoardData) {
          loadBoardData(boardType);
        }

        return true;
      } else {
        const errorMsg = response?.message || "게시글 등록에 실패했습니다.";
        alert(errorMsg);
        console.error("게시글 등록 실패:", response);
        return false;
      }
    } else {
      formData.append("title", title);
      formData.append("content", content);
      formData.append("userId", "1");

      if (file) {
        formData.append("file", file);
      }

      // API 호출 (BoardService 사용 또는 ApiClient 직접 사용)
      let response;
      if (window.BoardService) {
        response = await window.BoardService.createPost(mappedType, formData);
      } else {
        response = await window.ApiClient.postFormData(
          `/${mappedType}/create`,
          formData,
          true
        );
      }

      if (
        response &&
        (response.status === "success" || response.success === true)
      ) {
        alert("게시글이 성공적으로 등록되었습니다.");

        // 게시판 데이터 다시 로드
        if (window.loadBoardData) {
          loadBoardData(boardType);
        }

        return true;
      } else {
        const errorMsg = response?.message || "게시글 등록에 실패했습니다.";
        alert(errorMsg);
        console.error("게시글 등록 실패:", response);
        return false;
      }
    }

    // 이 부분은 더 이상 필요 없음
    /*
    if (file) {
      formData.append("file", file);
    }

    // API 호출 (BoardService 사용 또는 ApiClient 직접 사용)
    let response;
    if (window.BoardService) {
      response = await window.BoardService.createPost(mappedType, formData);
    } else {
      response = await window.ApiClient.postFormData(
        `/${mappedType}/create`,
        formData,
        true
      );
    }

    if (
      response &&
      (response.status === "success" || response.success === true)
    ) {
      alert("게시글이 성공적으로 등록되었습니다.");

      // 게시판 데이터 다시 로드
      if (window.loadBoardData) {
        loadBoardData(boardType);
      }

      return true;
    } else {
      const errorMsg = response?.message || "게시글 등록에 실패했습니다.";
      alert(errorMsg);
      console.error("게시글 등록 실패:", response);
      return false;
    }
    */
  } catch (error) {
    console.error("게시글 등록 중 오류 발생:", error);
    alert("게시글 등록 중 오류가 발생했습니다.");
    return false;
  }
}

/**
 * 무한 스크롤 기능 초기화
 * @param {string} containerSelector - 컨테이너 요소 선택자
 * @param {string} itemSelector - 아이템 요소 선택자
 * @param {Function} loadMoreFunction - 더 많은 항목을 로드하는 함수
 */
function initializeInfiniteScroll(
  containerSelector,
  itemSelector,
  loadMoreFunction
) {
  const container = document.querySelector(containerSelector);
  if (!container) return;

  let loading = false;
  let page = 1;

  window.addEventListener("scroll", () => {
    if (loading) return;

    const lastItem = document.querySelector(`${itemSelector}:last-child`);
    if (!lastItem) return;

    const lastItemOffset = lastItem.offsetTop + lastItem.clientHeight;
    const pageOffset = window.pageYOffset + window.innerHeight;

    if (pageOffset > lastItemOffset - 200) {
      loading = true;
      page++;

      loadMoreFunction(page)
        .then(() => {
          loading = false;
        })
        .catch(() => {
          loading = false;
        });
    }
  });
}

/**
 * 다크모드 토글 기능
 */
function toggleDarkMode() {
  const body = document.body;
  body.classList.toggle("dark-mode");

  const isDarkMode = body.classList.contains("dark-mode");
  setCookie("darkMode", isDarkMode ? "1" : "0", 365);
}

// 사용자가 이미 다크모드를 설정했는지 확인
function checkDarkModePreference() {
  const darkModeCookie = getCookie("darkMode");
  if (darkModeCookie === "1") {
    document.body.classList.add("dark-mode");
  }
}

// 페이지 로드 시 다크모드 설정 확인
checkDarkModePreference();

// 특정 게시판 페이지에서만 사용되는 기능
if (window.location.pathname.includes("board.html")) {
  document.addEventListener("DOMContentLoaded", function () {
    const submitBtn = document.getElementById("submit-post");
    if (submitBtn) {
      submitBtn.addEventListener("click", async () => {
        const boardSelect = document.getElementById("board-select");
        const titleInput = document.getElementById("post-title");
        const contentInput = document.getElementById("post-content");
        const fileInput = document.getElementById("post-file");

        if (boardSelect && titleInput && contentInput) {
          const boardType = boardSelect.value;
          const title = titleInput.value;
          const content = contentInput.value;
          const file = fileInput ? fileInput.files[0] : null;

          const result = await submitPost(boardType, title, content, file);
          if (result) {
            // 성공적으로 등록되면 모달 닫기
            const modal = document.getElementById("write-modal");
            if (modal) modal.style.display = "none";

            // 폼 초기화
            titleInput.value = "";
            contentInput.value = "";
            if (fileInput) fileInput.value = "";
          }
        }
      });
    }
  });
}
