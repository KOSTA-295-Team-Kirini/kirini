/**
 * KIRINI 웹사이트 용어집 JavaScript
 * dictionary.html 페이지의 검색 및 동적 컨텐츠 표시 기능 담당
 */

document.addEventListener('DOMContentLoaded', function() {
  initializeDictionarySearch();
});

// 용어집 검색 결과 동적 표시 기능
function initializeDictionarySearch() {
  if (!window.location.pathname.includes('dictionary.html')) {
    return; // dictionary.html 페이지가 아니면 실행하지 않음
  }

  const input = document.getElementById('search-input');
  const results = document.getElementById('search-results');
  const searchButton = document.querySelector('.search-container button'); // 검색 버튼
  
  // 이클립스 환경인지 확인 (이제 전역 변수 사용)
  const isEclipse = window.KIRINI_ENV ? window.KIRINI_ENV.isEclipse : window.location.pathname.includes('/kirini/');
  
  // API URL 설정
  const baseApiUrl = isEclipse ? '/kirini/guide' : '/guide';

  // 모든 용어를 저장할 변수
  let terms = [];
  
  // API 연결 상태를 추적하는 변수
  let apiConnectionState = 'loading'; // 'loading', 'success', 'error'

  // 페이지 로드 시 초기 데이터 가져오기
  fetchAllTerms();

  /**
   * API에서 모든 용어 가져오기
   */
  function fetchAllTerms() {
    // 로딩 상태 표시
    displayLoadingIndicator();
    apiConnectionState = 'loading';
    
    fetch(baseApiUrl)
      .then(response => {
        if (!response.ok) {
          throw new Error('API 응답 오류: ' + response.status);
        }
        return response.json();
      })
      .then(data => {
        apiConnectionState = 'success';
        
        if (data && data.length > 0) {
          // API에서 가져온 데이터 가공
          terms = data.map(term => ({
            id: term.guideId,
            name: term.term || '', 
            desc: term.description || '', 
            url: term.url, 
            link: mapTermToUrl(term)
          }));
          
          // 초기 인기 검색어 표시
          displayInitialPopularTerms();
        } else {
          // 데이터가 비어있을 때 처리
          displayEmptyDatabaseMessage();
        }
      })
      .catch(error => {
        apiConnectionState = 'error';
        displayConnectionError();
      });
  }
  
  /**
   * 로딩 표시기 보여주기
   */
  function displayLoadingIndicator() {
    results.innerHTML = '<li class="loading-message">검색 데이터를 불러오는 중입니다...</li>';
  }
  
  /**
   * DB 연결 오류 표시
   */
  function displayConnectionError() {
    results.innerHTML = `
      <li class="error-message">
        <p class="error-title">데이터 연결에 실패했습니다</p>
        <p>잠시 후 다시 시도해주시거나, 문제가 지속되면 관리자에게 문의해주세요.</p>
        <button onclick="location.reload()" class="refresh-btn">새로고침</button>
      </li>`;
  }
  
  /**
   * 데이터베이스가 비어있을 때 표시
   */
  function displayEmptyDatabaseMessage() {
    results.innerHTML = `
      <li class="empty-message">
        <p>용어 데이터가 아직 등록되지 않았습니다.</p>
        <p>관리자가 곧 유용한 정보들을 채워넣을 예정입니다! 조금만 기다려달라냥!</p>
      </li>`;
  }
  
  /**
   * 용어 객체를 URL로 매핑하는 함수
   */
  function mapTermToUrl(term) {
    // DB에 저장된 URL이 있는 경우 사용
    if (term.url && term.url.trim() !== '') {
      // DB URL이 상대 경로인지 확인
      if (term.url.startsWith('../') || term.url.startsWith('./')) {
        return term.url;
      } else if (term.url.startsWith('/')) {
        // 절대 경로인 경우 그대로 사용 (컨텍스트 경로는 renderResults에서 처리)
        return term.url;
      } else {
        // 상대 경로로 간주하고 기본 폴더 구조에 맞게 수정
        return `../keyboard_terms/${term.url}`;
      }
    }
    
    // URL 정보가 없는 경우 용어 이름으로 파일명 생성
    if (term && typeof term.term === 'string' && term.term.trim() !== '') {
      const termName = term.term.toLowerCase().replace(/ /g, '_');
      return `../keyboard_terms/${termName}.html`;
    }
    
    // term.term이 없거나 유효하지 않은 경우
    return ''; // 빈 문자열 반환
  }
  
  function renderResults(filteredTerms) {
    results.innerHTML = ''; // 기존 결과 초기화

    if (!filteredTerms || filteredTerms.length === 0) {
      const li = document.createElement('li');
      li.classList.add('no-results');
      li.innerHTML = `
        <p class="no-results-title">검색 결과가 없습니다.</p>
        <p>다른 키워드로 검색해 보세요.</p>
        <button onclick="document.getElementById('search-input').value=''; document.getElementById('search-input').focus()" class="clear-search-btn">
          검색어 지우기
        </button>`;
      results.appendChild(li);
      return;
    }

    filteredTerms.forEach(term => {
      const li = document.createElement('li');
      const link = document.createElement('a');
      
      // 이클립스 환경인지 확인하고 경로 조정
      let finalLinkHref = term.link; // 최종 링크 경로 변수
      if (isEclipse) {
        // 이클립스 환경에서는 /kirini/view/로 시작하는 절대 경로 사용
        if (term.link && term.link.startsWith('../')) {
          finalLinkHref = term.link.replace('../', '/kirini/view/');
        }
      }
      
      link.href = finalLinkHref; // 최종적으로 계산된 링크 할당
      link.classList.add('term-link');
      link.textContent = term.name;
      
      // 링크에 이벤트 리스너 추가 - 링크 유효성 확인용 (개발용)
      link.addEventListener('click', function(e) {
        if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
          console.log('용어집 링크 클릭:', this.href);
        }
      });

      const desc = document.createElement('p');
      desc.textContent = term.desc;

      li.appendChild(link);
      li.appendChild(desc);
      results.appendChild(li);
    });
  }

  // input 이벤트 리스너 (실시간 검색)
  input.addEventListener('input', performSearch);

  // 검색 버튼 클릭 이벤트 리스너
  if (searchButton) {
    searchButton.addEventListener('click', performSearch);
  }
  
  // 엔터 키 입력 시 검색 실행
  input.addEventListener('keypress', function(event) {
    if (event.key === 'Enter') {
      performSearch();
    }
  });

  // 초기 인기 검색어 표시 (예시 - 필요에 따라 수정/확장)
  function displayInitialPopularTerms() {
    // 인기 검색어 데이터 - 용어 데이터에서 실제 존재하는 항목만 선택
    let popularTermsData = [];
    
    // terms 배열에서 특정 용어들을 찾아 인기 검색어로 설정
    const popularTermNames = ['축', '적축', '갈축', '청축', '키캡', '스위치', '핫스왑'];
    
    // 실제 존재하는 용어만 인기 검색어로 표시
    popularTermsData = popularTermNames
      .map(name => {
        const term = terms.find(t => t.name === name);
        return term ? { name: term.name, link: term.link } : null;
      })
      .filter(term => term !== null)
      .slice(0, 5); // 최대 5개만 표시
    
    // 인기 검색어가 없으면 기본값 사용
    if (popularTermsData.length === 0) {
      popularTermsData = [
        { name: '축', link: '../keyboard_terms/axis.html' },
        { name: '적축', link: '../keyboard_terms/axis_red.html' },
        { name: '갈축', link: '../keyboard_terms/axis_brown.html' }
      ];
    }
    
    results.innerHTML = ''; // 기존 내용 지우기
    const li = document.createElement('li');
    const popularTitleDiv = document.createElement('div');
    popularTitleDiv.className = 'popular-term';
    popularTitleDiv.textContent = '추천 검색어';
    li.appendChild(popularTitleDiv);

    const linksDiv = document.createElement('div');
    popularTermsData.forEach(term => {
      const link = document.createElement('a');
      // 이클립스 환경인지 확인하고 경로 조정
      if (isEclipse) {
        // 이클립스 환경에서는 /kirini/view/로 시작하는 절대 경로 사용
        let linkPath = term.link;
        if (linkPath.startsWith('../')) {
          linkPath = linkPath.replace('../', '/kirini/view/');
        }
        link.href = linkPath;
      } else {
        // 일반 환경에서는 원래 상대 경로 사용
        link.href = term.link;
      }
      link.className = 'term-link';
      link.textContent = term.name;
      linksDiv.appendChild(link);
    });
    li.appendChild(linksDiv);
    results.appendChild(li);
    
    // 첫 진입시 자동 포커스
    if (input) {
      input.focus();
    }
  }

  // 첫 진입시 자동 포커스 - 외부에 있던 코드를 이곳으로 이동
  if (input) {
    input.focus();
  }

  // performSearch 함수를 initializeDictionarySearch 스코프 내부로 이동시키고,
  // 중복되는 searchInput, searchButton, searchResults 변수 선언을 제거합니다.
  async function performSearch() {
    const keyword = input.value.trim(); 
    if (keyword === '') {
      displayInitialPopularTerms();
      return;
    }

    // API 연결 상태가 로딩 중이면 대기 메시지 표시
    if (apiConnectionState === 'loading') {
      results.innerHTML = '<li class="loading-message">검색 데이터를 불러오는 중입니다...</li>';
      return;
    }

    try {
      const response = await fetch(`${baseApiUrl}?action=search&keyword=${encodeURIComponent(keyword)}`);
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const guides = await response.json();

      // 서버에서 온 전체 결과에서 클라이언트 측에서 한번 더 필터링 (서버 측 문제 임시 대응)
      const filteredGuides = guides.filter(guide => {
        const term = guide.term ? guide.term.toLowerCase() : '';
        const description = guide.description ? guide.description.toLowerCase() : '';
        const lowerKeyword = keyword.toLowerCase();
        
        // 용어명이나 설명에 키워드가 포함되어 있는지 확인
        return term.includes(lowerKeyword) || description.includes(lowerKeyword);
      });

      // 검색 결과에 링크 추가
      const guidesWithLinks = filteredGuides.map(guide => ({
        ...guide, // 기존 guide 객체의 모든 속성을 복사한다
        id: guide.guideId, // id도 명시적으로 매핑해준다 (renderResults에서 사용)
        name: guide.term || '', // 이름도 매핑해준다
        desc: guide.description || '', // 설명도 매핑해준다
        link: mapTermToUrl(guide) // mapTermToUrl 함수를 사용해서 link 속성을 추가한다
      }));

      // 결과 렌더링
      renderResults(guidesWithLinks); 

    } catch (error) {
      results.innerHTML = '<li>검색 중 오류가 발생했습니다.</li>'; 
    }
  }

  // 컨텍스트 경로를 동적으로 가져오는 함수
  function getContextPath() {
    // window.KIRINI_ENV.isEclipse를 직접 사용하는 대신 isEclipse 변수를 사용합니다.
    if (isEclipse) {
      return '/kirini';
    }
    return ''; // 컨텍스트 경로가 없는 경우 (예: 루트에서 직접 실행)
  }
}
