/**
 * 키보드 용어 사전 관리 기능
 * Kirini 프로젝트 - 기계식 키보드 용어 가이드
 */

document.addEventListener('DOMContentLoaded', function() {
  // 용어 사전 검색 및 필터 기능 초기화
  initializeGlossarySearch();
  
  // 용어 사전 로드
  loadGlossary();
});

/**
 * 용어 사전 검색 기능 초기화
 */
function initializeGlossarySearch() {
  const searchInput = document.getElementById('glossary-search');
  const categorySelect = document.getElementById('category-filter');
  
  // 검색 이벤트
  if (searchInput) {
    searchInput.addEventListener('input', debounce(function() {
      const query = searchInput.value.trim();
      const category = categorySelect ? categorySelect.value : 'all';
      
      if (query.length > 1) {
        searchTerms(query, category);
      } else if (query.length === 0) {
        // 검색어가 없으면 전체 목록 표시
        loadGlossary(category !== 'all' ? { category } : {});
      }
    }, 300)); // 300ms 디바운스
  }
  
  // 카테고리 필터 변경 이벤트
  if (categorySelect) {
    categorySelect.addEventListener('change', function() {
      const category = categorySelect.value;
      const query = searchInput ? searchInput.value.trim() : '';
      
      if (query.length > 1) {
        searchTerms(query, category);
      } else {
        loadGlossary(category !== 'all' ? { category } : {});
      }
    });
  }
  
  // 카테고리 옵션 로드
  loadCategories();
}

/**
 * 용어 사전 카테고리 불러오기
 */
async function loadCategories() {
  const categorySelect = document.getElementById('category-filter');
  if (!categorySelect) return;
  
  try {
    const response = await GlossaryService.getCategories();
    
    if (response && response.data && response.data.length > 0) {
      // 기본 '전체' 옵션은 유지
      const defaultOption = categorySelect.querySelector('option[value="all"]');
      categorySelect.innerHTML = '';
      
      if (defaultOption) {
        categorySelect.appendChild(defaultOption);
      } else {
        const allOption = document.createElement('option');
        allOption.value = 'all';
        allOption.textContent = '전체 카테고리';
        categorySelect.appendChild(allOption);
      }
      
      // 서버에서 받은 카테고리 추가
      response.data.forEach(category => {
        const option = document.createElement('option');
        option.value = category.id || category.name;
        option.textContent = category.name;
        categorySelect.appendChild(option);
      });
    }
  } catch (error) {
    console.error('카테고리 목록 불러오기 실패:', error);
  }
}

/**
 * 용어 사전 전체 목록 불러오기
 * @param {Object} params - 필터링 파라미터
 */
async function loadGlossary(params = {}) {
  const glossaryContainer = document.getElementById('glossary-content');
  if (!glossaryContainer) return;
  
  // 로딩 표시
  glossaryContainer.innerHTML = '<div class="loading">용어사전을 불러오는 중...</div>';
  
  try {
    const response = await GlossaryService.getTerms(params);
    
    if (response && response.data && response.data.length > 0) {
      displayGlossaryTerms(response.data);
    } else {
      glossaryContainer.innerHTML = '<div class="no-data">등록된 용어가 없습니다.</div>';
    }
  } catch (error) {
    console.error('용어사전 불러오기 실패:', error);
    glossaryContainer.innerHTML = '<div class="error">용어사전을 불러오는 중 오류가 발생했습니다.</div>';
  }
}

/**
 * 용어 검색
 * @param {string} query - 검색어
 * @param {string} category - 카테고리
 */
async function searchTerms(query, category = 'all') {
  const glossaryContainer = document.getElementById('glossary-content');
  if (!glossaryContainer) return;
  
  // 로딩 표시
  glossaryContainer.innerHTML = '<div class="loading">검색 중...</div>';
  
  try {
    const filters = category !== 'all' ? { category } : {};
    const response = await GlossaryService.searchTerms(query, filters);
    
    if (response && response.data && response.data.length > 0) {
      displayGlossaryTerms(response.data);
    } else {
      glossaryContainer.innerHTML = '<div class="no-results">검색 결과가 없습니다.</div>';
    }
  } catch (error) {
    console.error('용어 검색 실패:', error);
    glossaryContainer.innerHTML = '<div class="error">검색 중 오류가 발생했습니다.</div>';
  }
}

/**
 * 용어 목록 화면에 표시
 * @param {Array} terms - 용어 목록
 */
function displayGlossaryTerms(terms) {
  const glossaryContainer = document.getElementById('glossary-content');
  if (!glossaryContainer) return;
  
  // 용어 컨테이너 초기화
  glossaryContainer.innerHTML = '';
  
  // 알파벳순으로 정렬 
  terms.sort((a, b) => {
    const termA = a.term.toLowerCase();
    const termB = b.term.toLowerCase();
    return termA.localeCompare(termB);
  });
  
  // 알파벳 섹션별로 그룹화
  const groupedTerms = {};
  
  terms.forEach(term => {
    const firstChar = getFirstChar(term.term);
    if (!groupedTerms[firstChar]) {
      groupedTerms[firstChar] = [];
    }
    groupedTerms[firstChar].push(term);
  });
  
  // 그룹별로 표시
  Object.keys(groupedTerms).sort().forEach(char => {
    const sectionElement = document.createElement('div');
    sectionElement.className = 'glossary-section';
    sectionElement.innerHTML = `
      <h2 class="glossary-section-title">${char}</h2>
      <div class="glossary-items"></div>
    `;
    
    const itemsContainer = sectionElement.querySelector('.glossary-items');
    
    groupedTerms[char].forEach(term => {
      const termElement = document.createElement('div');
      termElement.className = 'glossary-item';
      termElement.innerHTML = `
        <h3 class="glossary-term">${term.term}</h3>
        <div class="glossary-description">${term.description}</div>
        ${term.category ? `<div class="glossary-category">${term.category}</div>` : ''}
      `;
      
      itemsContainer.appendChild(termElement);
    });
    
    glossaryContainer.appendChild(sectionElement);
  });
}

/**
 * 문자열의 첫 글자(알파벳 또는 한글) 반환
 * @param {string} str - 단어
 * @returns {string} 첫 글자
 */
function getFirstChar(str) {
  if (!str) return '#';
  
  const char = str.charAt(0).toUpperCase();
  
  // 영문자인 경우
  if (/[A-Z]/.test(char)) {
    return char;
  }
  
  // 한글인 경우
  if (/[가-힣]/.test(char)) {
    // 초성 추출 (간단한 구현)
    const consonants = ['ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'];
    const code = char.charCodeAt(0) - 44032;
    
    if (code > -1 && code < 11172) {
      const consonantIndex = Math.floor(code / 588);
      return consonants[consonantIndex] || char;
    }
    return char;
  }
  
  // 그 외의 경우
  return '#';
}

/**
 * 디바운스 유틸리티 함수
 * @param {Function} func - 실행할 함수
 * @param {number} delay - 지연시간 (ms)
 * @returns {Function} 디바운스된 함수
 */
function debounce(func, delay) {
  let timer;
  return function() {
    const context = this;
    const args = arguments;
    clearTimeout(timer);
    timer = setTimeout(() => func.apply(context, args), delay);
  };
}
