/**
 * KIRINI 웹사이트 메인 JavaScript
 * 키보드 입문자를 위한 웹사이트의 상호작용 기능 구현
 */

// DOM이 완전히 로드된 후 실행
document.addEventListener('DOMContentLoaded', function() {
  initializeNavHighlight();
  initializeSearchFunctions();
  initializeMobileMenu();
});

/**
 * 현재 페이지에 해당하는 네비게이션 항목에 하이라이트 표시
 */
function initializeNavHighlight() {
  const currentPath = window.location.pathname;
  const navLinks = document.querySelectorAll('nav a');
  
  navLinks.forEach(link => {
    const linkPath = link.getAttribute('href');
    if (currentPath.includes(linkPath) && linkPath !== '../index.html' && linkPath !== 'index.html') {
      link.classList.add('active');
    }
  });
}

/**
 * 검색 기능 초기화
 */
function initializeSearchFunctions() {
  const searchBars = document.querySelectorAll('.search-bar');
  
  searchBars.forEach(searchBar => {
    const searchInput = searchBar.querySelector('input');
    const searchButton = searchBar.querySelector('button');
    
    if (searchInput && searchButton) {
      searchButton.addEventListener('click', () => {
        const query = searchInput.value.trim();
        if (query.length > 0) {
          handleSearch(query);
        }
      });
      
      searchInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
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
  let searchType = 'general';
  
  if (currentPath.includes('dictionary')) {
    searchType = 'dictionary';
  } else if (currentPath.includes('database')) {
    searchType = 'keyboard';
  } else if (currentPath.includes('qna')) {
    searchType = 'qna';
  } else if (currentPath.includes('board')) {
    searchType = 'board';
  }
  
  // 실제 구현에서는 서버로 AJAX 요청을 보내 검색 결과를 가져옴
  console.log(`Searching for "${query}" in ${searchType} section`);
  
  // 간단한 데모 알림
  alert(`"${query}"에 대한 검색을 시작합니다. (${searchType} 검색)`);
  
  // 현재는 페이지 새로고침으로 시뮬레이션
  // 실제 구현에서는 AJAX로 결과를 가져와 동적으로 표시
  window.location.href = `?search=${encodeURIComponent(query)}&type=${searchType}`;
}

/**
 * 모바일 메뉴 기능 초기화
 */
function initializeMobileMenu() {
  // 모바일 메뉴 토글 버튼이 있는지 확인
  const menuToggle = document.querySelector('.mobile-menu-toggle');
  
  if (menuToggle) {
    const nav = document.querySelector('nav');
    
    menuToggle.addEventListener('click', () => {
      nav.classList.toggle('mobile-active');
      menuToggle.classList.toggle('active');
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
  let expires = '';
  if (days) {
    const date = new Date();
    date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
    expires = '; expires=' + date.toUTCString();
  }
  document.cookie = name + '=' + encodeURIComponent(value) + expires + '; path=/';
}

/**
 * 쿠키를 가져오는 함수
 * @param {string} name - 쿠키 이름
 * @return {string|null} 쿠키 값 또는 null
 */
function getCookie(name) {
  const nameEQ = name + '=';
  const ca = document.cookie.split(';');
  for (let i = 0; i < ca.length; i++) {
    let c = ca[i];
    while (c.charAt(0) === ' ') c = c.substring(1, c.length);
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
function submitPost(boardType, title, content, file) {
  // 실제 구현에서는 서버로 AJAX 요청을 보내 데이터 저장
  console.log('게시글 등록:', { boardType, title, content, file });
  
  // 간단한 유효성 검사
  if (!title || title.trim().length === 0) {
    alert('제목을 입력해주세요.');
    return false;
  }
  
  if (!content || content.trim().length === 0) {
    alert('내용을 입력해주세요.');
    return false;
  }
  
  // 현재는 간단한 알림으로 시뮬레이션
  alert('게시글이 등록되었습니다.');
  return true;
}

/**
 * 무한 스크롤 기능 초기화
 * @param {string} containerSelector - 컨테이너 요소 선택자
 * @param {string} itemSelector - 아이템 요소 선택자
 * @param {Function} loadMoreFunction - 더 많은 항목을 로드하는 함수
 */
function initializeInfiniteScroll(containerSelector, itemSelector, loadMoreFunction) {
  const container = document.querySelector(containerSelector);
  if (!container) return;
  
  let loading = false;
  let page = 1;
  
  window.addEventListener('scroll', () => {
    if (loading) return;
    
    const lastItem = document.querySelector(`${itemSelector}:last-child`);
    if (!lastItem) return;
    
    const lastItemOffset = lastItem.offsetTop + lastItem.clientHeight;
    const pageOffset = window.pageYOffset + window.innerHeight;
    
    if (pageOffset > lastItemOffset - 200) {
      loading = true;
      page++;
      
      loadMoreFunction(page).then(() => {
        loading = false;
      }).catch(() => {
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
  body.classList.toggle('dark-mode');
  
  const isDarkMode = body.classList.contains('dark-mode');
  setCookie('darkMode', isDarkMode ? '1' : '0', 365);
}

// 사용자가 이미 다크모드를 설정했는지 확인
function checkDarkModePreference() {
  const darkModeCookie = getCookie('darkMode');
  if (darkModeCookie === '1') {
    document.body.classList.add('dark-mode');
  }
}

// 페이지 로드 시 다크모드 설정 확인
checkDarkModePreference();

// 특정 게시판 페이지에서만 사용되는 기능
if (window.location.pathname.includes('board.html')) {
  document.addEventListener('DOMContentLoaded', function() {
    const submitBtn = document.getElementById('submit-post');
    if (submitBtn) {
      submitBtn.addEventListener('click', () => {
        const boardSelect = document.getElementById('board-select');
        const titleInput = document.getElementById('post-title');
        const contentInput = document.getElementById('post-content');
        const fileInput = document.getElementById('post-file');
        
        if (boardSelect && titleInput && contentInput) {
          const boardType = boardSelect.value;
          const title = titleInput.value;
          const content = contentInput.value;
          const file = fileInput ? fileInput.files[0] : null;
          
          if (submitPost(boardType, title, content, file)) {
            // 성공적으로 등록되면 모달 닫기
            const modal = document.getElementById('write-modal');
            if (modal) modal.style.display = 'none';
            
            // 폼 초기화
            titleInput.value = '';
            contentInput.value = '';
            if (fileInput) fileInput.value = '';
          }
        }
      });
    }
  });
}