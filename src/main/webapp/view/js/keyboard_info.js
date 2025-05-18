// 키보드 정보 및 목록 관련 JavaScript 파일
document.addEventListener('DOMContentLoaded', () => {
  // 키보드 목록 로드
  loadKeyboards();
  
  // 필터 버튼 이벤트 리스너 설정
  setupFilterButtons();
  
  // 태그 클릭 이벤트 설정
  setupTagClickEvents();
  
  // 정렬 컨트롤 설정
  setupSortControls();
  
  // 검색 기능 설정
  setupSearchControls();
});

/**
 * API를 통해 키보드 목록 로드
 * @param {Object} params 검색 및 필터링 매개변수
 */
async function loadKeyboards(params = {}) {
  try {
    // 로딩 상태 표시
    showLoadingState();
    
    // 캐싱 적용 (같은 파라미터로 최근 30초 이내 조회시 캐시 사용)
    const cacheKey = `keyboard_list_${JSON.stringify(params)}`;
    const cachedData = sessionStorage.getItem(cacheKey);
    
    let keyboardData;
    
    // 캐시 데이터가 있고 30초 이내에 저장된 것이면 사용
    if (cachedData) {
      const parsedCache = JSON.parse(cachedData);
      const cacheAge = Date.now() - parsedCache.timestamp;
      
      if (cacheAge < 30000) { // 30초
        keyboardData = parsedCache.data;
        console.log('캐시된 키보드 목록 사용');
      }
    }
    
    // 캐시가 없거나 오래된 경우 API 호출
    if (!keyboardData) {
      console.log('키보드 목록 API 호출');
      keyboardData = await KeyboardService.getKeyboards(params);
      
      // 결과 캐싱
      sessionStorage.setItem(cacheKey, JSON.stringify({
        timestamp: Date.now(),
        data: keyboardData
      }));
    }
      // 데이터 검증
    if (!keyboardData || !Array.isArray(keyboardData.keyboards)) {
      throw new Error('API에서 유효한 키보드 데이터를 반환하지 않았습니다.');
    }
    
    // 키보드 목록 렌더링
    renderKeyboardList(keyboardData.keyboards || []);
    
    // 페이지네이션 업데이트
    updatePagination(keyboardData.pagination);
    
    // 카테고리 필터 업데이트 (서버에서 받은 카테고리 목록이 있으면)
    if (keyboardData.categories && Array.isArray(keyboardData.categories)) {
      updateCategoryFilters(keyboardData.categories);
    }
    
    // 로딩 상태 제거
    hideLoadingState();
  } catch (error) {
    console.error('키보드 목록 로딩 오류:', error);
    
    // 오류 메시지 세분화
    let errorMessage = '키보드 목록을 불러오는데 문제가 발생했습니다.';
    if (error.message.includes('네트워크')) {
      errorMessage = '네트워크 연결을 확인해주세요.';
    } else if (error.message.includes('시간 초과')) {
      errorMessage = '서버 응답 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.';
    }
    
    showErrorState(errorMessage, true);
  }
}

/**
 * 키보드 목록 렌더링
 * @param {Array} keyboards 키보드 데이터 배열
 */
function renderKeyboardList(keyboards) {
  const keyboardContainer = document.querySelector('.keyboard-listing');
  
  // 컨테이너가 없으면 종료
  if (!keyboardContainer) return;
  
  // 이전 내용 삭제
  keyboardContainer.innerHTML = '';
  
  // 키보드 목록이 비어있을 때
  if (keyboards.length === 0) {
    keyboardContainer.innerHTML = `
      <div class="no-results">
        <p>검색 결과가 없습니다.</p>
        <button class="btn btn-primary reset-filters">모든 필터 초기화</button>
      </div>
    `;
    
    // 필터 초기화 버튼 이벤트
    document.querySelector('.reset-filters')?.addEventListener('click', resetAllFilters);
    return;
  }
  
  // 키보드 카드 생성 및 추가
  keyboards.forEach(keyboard => {
    const keyboardCard = createKeyboardCard(keyboard);
    keyboardContainer.appendChild(keyboardCard);
  });
  
  // 태그 클릭 이벤트 재설정
  setupTagClickEvents();
}

/**
 * 키보드 카드 요소 생성
 * @param {Object} keyboard 키보드 데이터
 * @returns {HTMLElement} 키보드 카드 DOM 요소
 */
function createKeyboardCard(keyboard) {
  // 태그 HTML 생성
  const tagsHtml = keyboard.tags?.map(tag => 
    `<span class="keyboard-tag" data-tag="${tag}">${tag}</span>`
  ).join('') || '';
  
  // 카드 컨테이너 생성
  const cardElement = document.createElement('div');
  cardElement.className = 'keyboard-card';
  cardElement.dataset.categories = (keyboard.categories || []).join(' ');
  cardElement.dataset.keyboardId = keyboard.id;
  
  // 카드 내용 설정
  cardElement.innerHTML = `
    <div class="card">
      <div class="card-image">
        <img src="${keyboard.imageUrl || '/view/img/keyboard-placeholder.jpg'}" alt="${keyboard.name}">
      </div>
      <div class="card-content">
        <h3 class="card-title">${keyboard.name}</h3>
        <div class="card-price">₩${formatPrice(keyboard.price)}</div>
        <div class="card-rating">
          ${generateStarRating(keyboard.rating || 0)}
          <span class="rating-count">(${keyboard.reviewCount || 0})</span>
        </div>
        <p class="card-description">${keyboard.description || ''}</p>
        <div class="card-tags">
          ${tagsHtml}
        </div>
      </div>
      <div class="card-actions">
        <a href="/view/keyboard/detail.html?id=${keyboard.id}" class="btn btn-primary">자세히 보기</a>
      </div>
    </div>
  `;
  
  // 카드 클릭 이벤트 (상세 페이지로 이동)
  cardElement.querySelector('.card').addEventListener('click', function(e) {
    // 태그나 버튼 클릭은 별도 처리
    if (e.target.classList.contains('keyboard-tag') || e.target.classList.contains('btn')) {
      return;
    }
    window.location.href = `/view/keyboard/detail.html?id=${keyboard.id}`;
  });
  
  return cardElement;
}

/**
 * 별점 HTML 생성
 * @param {number} rating 별점 (0-5)
 * @returns {string} 별점 HTML
 */
function generateStarRating(rating) {
  const fullStars = Math.floor(rating);
  const hasHalfStar = rating % 1 >= 0.5;
  const emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);
  
  let starsHtml = '';
  
  // 꽉 찬 별
  for (let i = 0; i < fullStars; i++) {
    starsHtml += '<i class="fas fa-star"></i>';
  }
  
  // 반 별
  if (hasHalfStar) {
    starsHtml += '<i class="fas fa-star-half-alt"></i>';
  }
  
  // 빈 별
  for (let i = 0; i < emptyStars; i++) {
    starsHtml += '<i class="far fa-star"></i>';
  }
  
  return starsHtml;
}

/**
 * 가격 포맷팅 (1000단위 콤마)
 * @param {number} price 가격
 * @returns {string} 포맷된 가격
 */
function formatPrice(price) {
  return price?.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',') || '0';
}

/**
 * 필터 버튼 이벤트 리스너 설정
 */
function setupFilterButtons() {
  document.querySelectorAll('.filter-btn').forEach(button => {
    button.addEventListener('click', () => {
      // 활성화된 버튼 상태 변경
      document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.classList.remove('active');
      });
      button.classList.add('active');
      
      const filterValue = button.dataset.filter;
      
      // 현재 URL 매개변수 가져오기
      const urlParams = new URLSearchParams(window.location.search);
      
      // 카테고리 필터 업데이트
      if (filterValue === 'all') {
        urlParams.delete('category');
      } else {
        urlParams.set('category', filterValue);
      }
      
      // 첫 페이지로 돌아가기
      urlParams.delete('page');
      
      // URL 및 키보드 목록 업데이트
      updateUrlAndLoadKeyboards(urlParams);
    });
  });
}

/**
 * 태그 클릭 이벤트 설정
 */
function setupTagClickEvents() {
  document.querySelectorAll('.keyboard-tag').forEach(tag => {
    tag.addEventListener('click', () => {
      const tagName = tag.dataset.tag;
      if (!tagName) return;
      
      // 현재 URL 매개변수 가져오기
      const urlParams = new URLSearchParams(window.location.search);
      
      // 태그 필터 설정
      urlParams.set('tag', tagName);
      
      // 첫 페이지로 돌아가기
      urlParams.delete('page');
      
      // URL 및 키보드 목록 업데이트
      updateUrlAndLoadKeyboards(urlParams);
    });
  });
}

/**
 * 정렬 컨트롤 설정
 */
function setupSortControls() {
  const sortSelect = document.querySelector('#sort-select');
  if (sortSelect) {
    // 초기 값 설정
    const urlParams = new URLSearchParams(window.location.search);
    const currentSort = urlParams.get('sort') || 'popular';
    sortSelect.value = currentSort;
    
    // 정렬 변경 이벤트
    sortSelect.addEventListener('change', () => {
      const urlParams = new URLSearchParams(window.location.search);
      urlParams.set('sort', sortSelect.value);
      
      // URL 및 키보드 목록 업데이트
      updateUrlAndLoadKeyboards(urlParams);
    });
  }
}

/**
 * 검색 컨트롤 설정
 */
function setupSearchControls() {
  const searchForm = document.querySelector('#keyboard-search-form');
  const searchInput = document.querySelector('#keyboard-search-input');
  
  if (searchForm && searchInput) {
    // 초기 값 설정
    const urlParams = new URLSearchParams(window.location.search);
    const currentSearch = urlParams.get('q');
    if (currentSearch) {
      searchInput.value = currentSearch;
    }
    
    // 검색 제출 이벤트
    searchForm.addEventListener('submit', (e) => {
      e.preventDefault();
      
      const urlParams = new URLSearchParams(window.location.search);
      const searchValue = searchInput.value.trim();
      
      if (searchValue) {
        urlParams.set('q', searchValue);
      } else {
        urlParams.delete('q');
      }
      
      // 첫 페이지로 돌아가기
      urlParams.delete('page');
      
      // URL 및 키보드 목록 업데이트
      updateUrlAndLoadKeyboards(urlParams);
    });
    
    // 검색어 지우기 버튼
    const clearSearchBtn = document.querySelector('.clear-search');
    if (clearSearchBtn) {
      clearSearchBtn.addEventListener('click', () => {
        searchInput.value = '';
        
        const urlParams = new URLSearchParams(window.location.search);
        urlParams.delete('q');
        
        // URL 및 키보드 목록 업데이트
        updateUrlAndLoadKeyboards(urlParams);
      });
    }
  }
}

/**
 * 가격 필터 설정
 */
function setupPriceFilter() {
  const priceForm = document.querySelector('#price-filter-form');
  const minInput = document.querySelector('#price-min');
  const maxInput = document.querySelector('#price-max');
  
  if (priceForm && minInput && maxInput) {
    // 초기 값 설정
    const urlParams = new URLSearchParams(window.location.search);
    const currentMin = urlParams.get('minPrice');
    const currentMax = urlParams.get('maxPrice');
    
    if (currentMin) minInput.value = currentMin;
    if (currentMax) maxInput.value = currentMax;
    
    // 가격 필터 제출 이벤트
    priceForm.addEventListener('submit', (e) => {
      e.preventDefault();
      
      const urlParams = new URLSearchParams(window.location.search);
      const minPrice = minInput.value.trim();
      const maxPrice = maxInput.value.trim();
      
      if (minPrice) {
        urlParams.set('minPrice', minPrice);
      } else {
        urlParams.delete('minPrice');
      }
      
      if (maxPrice) {
        urlParams.set('maxPrice', maxPrice);
      } else {
        urlParams.delete('maxPrice');
      }
      
      // 첫 페이지로 돌아가기
      urlParams.delete('page');
      
      // URL 및 키보드 목록 업데이트
      updateUrlAndLoadKeyboards(urlParams);
    });
  }
}

/**
 * URL 업데이트 및 키보드 목록 다시 로드
 * @param {URLSearchParams} urlParams URL 매개변수
 */
function updateUrlAndLoadKeyboards(urlParams) {
  // 브라우저 URL 업데이트
  const newUrl = `${window.location.pathname}?${urlParams.toString()}`;
  window.history.pushState({ path: newUrl }, '', newUrl);
  
  // URL 매개변수를 객체로 변환
  const params = {};
  for (const [key, value] of urlParams.entries()) {
    params[key] = value;
  }
  
  // 키보드 목록 다시 로드
  loadKeyboards(params);
}

/**
 * 로딩 상태 표시
 */
function showLoadingState() {
  // 기존 오류 메시지 제거
  hideErrorState();
  
  const container = document.querySelector('.keyboard-listing');
  if (!container) return;
  
  // 로딩 인디케이터 추가
  const loadingElement = document.createElement('div');
  loadingElement.className = 'loading-indicator';
  loadingElement.innerHTML = `
    <div class="spinner-border text-primary" role="status">
      <span class="visually-hidden">로딩 중...</span>
    </div>
    <p>키보드 목록을 불러오는 중입니다...</p>
  `;
  
  container.innerHTML = '';
  container.appendChild(loadingElement);
}

/**
 * 로딩 상태 제거
 */
function hideLoadingState() {
  const loadingIndicator = document.querySelector('.loading-indicator');
  if (loadingIndicator) {
    loadingIndicator.remove();
  }
}

/**
 * 오류 상태 표시
 * @param {string} message 오류 메시지
 * @param {boolean} showRetry 재시도 버튼 표시 여부
 */
function showErrorState(message, showRetry = false) {
  hideLoadingState();
  
  const container = document.querySelector('.keyboard-listing');
  if (!container) return;
  
  // 오류 메시지 추가
  const errorElement = document.createElement('div');
  errorElement.className = 'error-message alert alert-danger';
  
  let errorHTML = message;
  
  // 재시도 버튼 추가
  if (showRetry) {
    errorHTML += `
      <div class="mt-3">
        <button class="btn btn-outline-primary retry-button">다시 시도</button>
      </div>
    `;
  }
  
  errorElement.innerHTML = errorHTML;
  container.innerHTML = '';
  container.appendChild(errorElement);
  
  // 재시도 버튼 이벤트 설정
  if (showRetry) {
    container.querySelector('.retry-button').addEventListener('click', () => {
      // 현재 URL 파라미터로 다시 로드
      const urlParams = new URLSearchParams(window.location.search);
      const params = {};
      for (const [key, value] of urlParams.entries()) {
        params[key] = value;
      }
      loadKeyboards(params);
    });
  }
}

/**
 * 페이지네이션 업데이트
 * @param {Object} pagination 페이징 정보
 */
function updatePagination(pagination) {
  const paginationContainer = document.querySelector('.pagination-container');
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
  document.querySelectorAll('.page-link').forEach(link => {
    link.addEventListener('click', (e) => {
      e.preventDefault();
      
      const page = parseInt(link.dataset.page);
      if (isNaN(page) || page < 1 || page > totalPages) return;
      
      const urlParams = new URLSearchParams(window.location.search);
      urlParams.set('page', page);
      
      // URL 및 키보드 목록 업데이트
      updateUrlAndLoadKeyboards(urlParams);
      
      // 페이지 상단으로 스크롤
      window.scrollTo({ top: 0, behavior: 'smooth' });
    });
  });
}

/**
 * 카테고리 필터 동적 업데이트
 * @param {Array} categories 서버에서 받은 카테고리 목록
 */
function updateCategoryFilters(categories) {
  const filterContainer = document.querySelector('.category-filter');
  if (!filterContainer) return;
  
  // 기존 '전체' 버튼 남기기
  const allFilter = filterContainer.querySelector('[data-filter="all"]');
  
  // 새로운 버튼 HTML 생성
  let filterButtonsHTML = allFilter ? allFilter.outerHTML : '<button class="filter-btn active" data-filter="all">전체</button>';
  
  // 각 카테고리별 버튼 생성
  categories.forEach(category => {
    filterButtonsHTML += `
      <button class="filter-btn" data-filter="${category.id}">${category.name}</button>
    `;
  });
  
  // 필터 컨테이너 업데이트
  filterContainer.innerHTML = filterButtonsHTML;
  
  // 이벤트 리스너 다시 설정
  setupFilterButtons();
}

/**
 * 모든 필터 초기화
 */
function resetAllFilters() {
  // URL의 검색 파라미터만 제거
  const newUrl = window.location.pathname;
  window.history.pushState({ path: newUrl }, '', newUrl);
  
  // 활성화된 필터 버튼 초기화
  document.querySelectorAll('.filter-btn').forEach(btn => {
    btn.classList.remove('active');
  });
  
  // '전체' 버튼 활성화
  const allButton = document.querySelector('.filter-btn[data-filter="all"]');
  if (allButton) {
    allButton.classList.add('active');
  }
  
  // 검색 필드 초기화
  const searchInput = document.querySelector('#keyboard-search-input');
  if (searchInput) {
    searchInput.value = '';
  }
  
  // 가격 필터 초기화
  const minInput = document.querySelector('#price-min');
  const maxInput = document.querySelector('#price-max');
  if (minInput) minInput.value = '';
  if (maxInput) maxInput.value = '';
  
  // 정렬 기본값으로 초기화
  const sortSelect = document.querySelector('#sort-select');
  if (sortSelect) {
    sortSelect.value = 'popular';
  }
  
  // 키보드 목록 다시 로드 (필터 없이)
  loadKeyboards();
}

// 페이지 로드 완료 시 가격 필터 설정
document.addEventListener('DOMContentLoaded', () => {
  setupPriceFilter();
});
