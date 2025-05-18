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
  
  // 필터 패널 설정
  setupFilterPanel();
  
  // 맨 위로 버튼 설정
  setupBackToTopButton();
});

/**
 * API를 통해 키보드 목록 로드
 * @param {Object} params 검색 및 필터링 매개변수
 */
async function loadKeyboards(params = {}) {
  try {
    // 스켈레톤 로딩 상태 표시
    showSkeletonLoading();
    
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
      // 인위적인 지연으로 스켈레톤 효과를 더 잘 보여줌 (실제 환경에서는 제거)
      await new Promise(resolve => setTimeout(resolve, 800));
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
  const keyboardContainer = document.querySelector('.keyboard-grid');
  
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
  
  // 키보드 카드 생성 및 추가 (지연된 페이드인 효과 적용)
  keyboards.forEach((keyboard, index) => {
    setTimeout(() => {
      const keyboardCard = createKeyboardCard(keyboard);
      keyboardCard.classList.add('fade-in');
      keyboardContainer.appendChild(keyboardCard);
    }, index * 50); // 카드당 50ms 간격으로 표시
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
    <img src="${keyboard.imageUrl || '/view/img/keyboard-placeholder.jpg'}" 
         alt="${keyboard.name}" 
         class="keyboard-image loading"
         onload="this.classList.remove('loading'); this.classList.add('loaded')">    <div class="keyboard-content">
      <h3 class="keyboard-title">${keyboard.name}</h3>
      <div class="keyboard-tags">
        ${tagsHtml}
      </div>
      <div class="keyboard-specs">
        <div class="keyboard-price">₩${formatPrice(keyboard.price)}</div>
        <div class="keyboard-rating">
          ${generateStarRating(keyboard.rating || 0)}
          <span class="rating-count">(${keyboard.reviewCount || 0})</span>
        </div>
        <p class="keyboard-description">${keyboard.description || '매력적인 타이핑 경험을 선사하는 고품질 키보드'}</p>
      </div>
      <a href="/view/keyboard/detail.html?id=${keyboard.id}" class="keyboard-action-btn">자세히 보기</a>
    </div>
  `;
    // 카드 클릭 이벤트 (상세 페이지로 이동)
  cardElement.addEventListener('click', function(e) {
    // 태그나 버튼 클릭은 별도 처리
    if (e.target.classList.contains('keyboard-tag') || e.target.classList.contains('keyboard-action-btn')) {
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
 * 스켈레톤 로딩 표시
 * @param {number} count 표시할 스켈레톤 카드 수
 */
function showSkeletonLoading(count = 8) {
  // 기존 오류 메시지 제거
  hideErrorState();
  
  const container = document.querySelector('.keyboard-grid');
  if (!container) return;
  
  // 기존 내용 비우기
  container.innerHTML = '';
  
  // 스켈레톤 로딩 카드 추가
  for (let i = 0; i < count; i++) {
    const skeletonCard = document.createElement('div');
    skeletonCard.className = 'keyboard-card keyboard-skeleton';
    skeletonCard.innerHTML = `
      <div class="skeleton keyboard-skeleton-image"></div>
      <div class="keyboard-skeleton-content">
        <div class="skeleton keyboard-skeleton-title"></div>
        <div class="keyboard-skeleton-tags">
          <div class="skeleton keyboard-skeleton-tag"></div>
          <div class="skeleton keyboard-skeleton-tag"></div>
          <div class="skeleton keyboard-skeleton-tag"></div>
        </div>
        <div class="keyboard-skeleton-specs">
          <div class="skeleton keyboard-skeleton-spec"></div>
          <div class="skeleton keyboard-skeleton-spec"></div>
          <div class="skeleton keyboard-skeleton-spec"></div>
        </div>
      </div>
    `;
    container.appendChild(skeletonCard);
  }
}

/**
 * 로딩 상태 표시
 */
function showLoadingState() {
  showSkeletonLoading();
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
 * 필터 패널 토글 기능 설정
 */
function setupFilterPanel() {
  const filterToggleBtn = document.getElementById('filter-toggle');
  const filterPanel = document.getElementById('filter-panel');
  
  if (!filterToggleBtn || !filterPanel) return;
  
  // 필터 패널 토글 기능
  filterToggleBtn.addEventListener('click', function() {
    const isActive = filterPanel.classList.contains('active');
    
    // 애니메이션과 함께 토글
    if (isActive) {
      filterPanel.classList.remove('active');
      filterToggleBtn.classList.remove('active');
    } else {
      filterPanel.classList.add('active');
      filterToggleBtn.classList.add('active');
    }
  });
  
  // 패널 외부 클릭 시 패널 닫기
  document.addEventListener('click', function(e) {
    if (!filterPanel.contains(e.target) && !filterToggleBtn.contains(e.target) && filterPanel.classList.contains('active')) {
      filterPanel.classList.remove('active');
      filterToggleBtn.classList.remove('active');
    }
  });
  
  // 필터 칩 클릭 이벤트
  const filterChips = document.querySelectorAll('.filter-chip');
  filterChips.forEach(chip => {
    chip.addEventListener('click', function() {
      // 카테고리 필터인 경우 다른 카테고리 필터 비활성화
      if (this.dataset.filter === 'all') {
        filterChips.forEach(c => c.classList.remove('active'));
        this.classList.add('active');
      } else {
        // 전체보기 필터 비활성화
        document.querySelector('.filter-chip[data-filter="all"]').classList.remove('active');
        
        // 현재 칩 토글
        this.classList.toggle('active');
      }
      
      // 활성화된 필터가 없으면 전체보기 활성화
      const activeFilters = document.querySelectorAll('.filter-chip.active');
      if (activeFilters.length === 0) {
        document.querySelector('.filter-chip[data-filter="all"]').classList.add('active');
      }
      
      // 활성화된 필터 표시 업데이트
      updateActiveFiltersDisplay();
    });
  });
  
  // 평점 칩 클릭 이벤트
  const ratingChips = document.querySelectorAll('.rating-chip');
  ratingChips.forEach(chip => {
    chip.addEventListener('click', function() {
      ratingChips.forEach(c => c.classList.remove('active'));
      this.classList.toggle('active');
      updateActiveFiltersDisplay();
    });
  });
  
  // 가격 범위 슬라이더 설정
  setupPriceRangeSlider();
  
  // 필터 적용 버튼
  document.querySelector('.btn-apply-filters').addEventListener('click', function() {
    applyFilters();
    // 필터 패널 닫기
    filterPanel.classList.remove('active');
    filterToggleBtn.classList.remove('active');
  });
  
  // 필터 초기화 버튼
  document.querySelector('.btn-reset-filters').addEventListener('click', resetAllFilters);
  
  // 정렬 옵션 변경 이벤트
  document.getElementById('sort-select').addEventListener('change', function() {
    applyFilters();
  });
}

/**
 * 가격 범위 슬라이더 설정
 */
function setupPriceRangeSlider() {
  const rangeMin = document.getElementById('range-min');
  const rangeMax = document.getElementById('range-max');
  const inputMin = document.getElementById('price-min');
  const inputMax = document.getElementById('price-max');
  const sliderTrack = document.querySelector('.slider-track');
  
  if (!rangeMin || !rangeMax || !inputMin || !inputMax || !sliderTrack) return;
  
  // 슬라이더 트랙 업데이트 함수
  function updateSliderTrack() {
    const percent1 = (rangeMin.value / rangeMin.max) * 100;
    const percent2 = (rangeMax.value / rangeMax.max) * 100;
    
    sliderTrack.style.background = `linear-gradient(to right, #e0e0e0 ${percent1}%, #ff7043 ${percent1}%, #ff7043 ${percent2}%, #e0e0e0 ${percent2}%)`;
  }
  
  // 초기 슬라이더 트랙 업데이트
  updateSliderTrack();
  
  // 레인지 및 입력 필드 이벤트
  rangeMin.addEventListener('input', function() {
    // 최소값이 최대값을 넘지 않도록
    if (parseInt(rangeMin.value) > parseInt(rangeMax.value) - 10000) {
      rangeMin.value = parseInt(rangeMax.value) - 10000;
    }
    
    inputMin.value = rangeMin.value;
    updateSliderTrack();
    updateActiveFiltersDisplay();
  });
  
  rangeMax.addEventListener('input', function() {
    // 최대값이 최소값보다 작지 않도록
    if (parseInt(rangeMax.value) < parseInt(rangeMin.value) + 10000) {
      rangeMax.value = parseInt(rangeMin.value) + 10000;
    }
    
    inputMax.value = rangeMax.value;
    updateSliderTrack();
    updateActiveFiltersDisplay();
  });
  
  inputMin.addEventListener('input', function() {
    // 값이 범위 내에 있는지 확인
    let value = parseInt(inputMin.value);
    if (isNaN(value)) value = 0;
    if (value < 0) value = 0;
    if (value > parseInt(rangeMax.value) - 10000) value = parseInt(rangeMax.value) - 10000;
    
    rangeMin.value = value;
    inputMin.value = value;
    updateSliderTrack();
    updateActiveFiltersDisplay();
  });
  
  inputMax.addEventListener('input', function() {
    // 값이 범위 내에 있는지 확인
    let value = parseInt(inputMax.value);
    if (isNaN(value)) value = 500000;
    if (value > 500000) value = 500000;
    if (value < parseInt(rangeMin.value) + 10000) value = parseInt(rangeMin.value) + 10000;
    
    rangeMax.value = value;
    inputMax.value = value;
    updateSliderTrack();
    updateActiveFiltersDisplay();
  });
}

/**
 * 활성화된 필터 표시 업데이트
 */
function updateActiveFiltersDisplay() {
  const activeFiltersContainer = document.getElementById('active-filters');
  if (!activeFiltersContainer) return;
  
  activeFiltersContainer.innerHTML = '';
  
  // 카테고리 필터
  const activeCategories = Array.from(document.querySelectorAll('.filter-chip.active')).filter(chip => chip.dataset.filter !== 'all');
  activeCategories.forEach(category => {
    addActiveFilterChip(category.textContent, 'category', category.dataset.filter);
  });
  
  // 평점 필터
  const activeRating = document.querySelector('.rating-chip.active');
  if (activeRating) {
    addActiveFilterChip(activeRating.textContent, 'rating', activeRating.dataset.rating);
  }
  
  // 가격 범위 필터
  const minPrice = document.getElementById('price-min').value;
  const maxPrice = document.getElementById('price-max').value;
  const defaultMin = 0;
  const defaultMax = 500000;
  
  if (minPrice != defaultMin || maxPrice != defaultMax) {
    const formattedMin = formatPrice(minPrice);
    const formattedMax = formatPrice(maxPrice);
    addActiveFilterChip(`₩${formattedMin} ~ ₩${formattedMax}`, 'price', `${minPrice}-${maxPrice}`);
  }
  
  // 정렬 옵션
  const sortSelect = document.getElementById('sort-select');
  if (sortSelect && sortSelect.value !== 'popular') {
    const sortText = sortSelect.options[sortSelect.selectedIndex].text;
    addActiveFilterChip(sortText, 'sort', sortSelect.value);
  }
}

/**
 * 활성화된 필터 칩 추가
 */
function addActiveFilterChip(text, type, value) {
  const activeFiltersContainer = document.getElementById('active-filters');
  if (!activeFiltersContainer) return;
  
  const chipElement = document.createElement('div');
  chipElement.className = 'active-filter';
  chipElement.dataset.type = type;
  chipElement.dataset.value = value;
  chipElement.innerHTML = `
    ${text}
    <button class="active-filter-remove" aria-label="필터 제거">×</button>
  `;
  
  // 칩 제거 이벤트
  chipElement.querySelector('.active-filter-remove').addEventListener('click', function() {
    // 필터 유형에 따른 처리
    switch (type) {
      case 'category':
        // 카테고리 필터 비활성화
        document.querySelector(`.filter-chip[data-filter="${value}"]`).classList.remove('active');
        // 활성 필터가 없으면 '전체보기' 활성화
        if (document.querySelectorAll('.filter-chip.active').length === 0) {
          document.querySelector('.filter-chip[data-filter="all"]').classList.add('active');
        }
        break;
        
      case 'rating':
        // 평점 필터 비활성화
        document.querySelector(`.rating-chip[data-rating="${value}"]`).classList.remove('active');
        break;
        
      case 'price':
        // 가격 범위 초기화
        const [min, max] = value.split('-');
        document.getElementById('price-min').value = 0;
        document.getElementById('price-max').value = 500000;
        document.getElementById('range-min').value = 0;
        document.getElementById('range-max').value = 500000;
        
        // 슬라이더 트랙 업데이트
        const sliderTrack = document.querySelector('.slider-track');
        if (sliderTrack) {
          sliderTrack.style.background = 'linear-gradient(to right, #e0e0e0 0%, #ff7043 0%, #ff7043 100%, #e0e0e0 100%)';
        }
        break;
        
      case 'sort':
        // 정렬 옵션 초기화
        document.getElementById('sort-select').value = 'popular';
        break;
    }
    
    // 필터 칩 제거 및 필터 적용
    chipElement.remove();
    applyFilters();
  });
  
  activeFiltersContainer.appendChild(chipElement);
}

/**
 * 모든 필터 초기화
 */
function resetAllFilters() {
  // 카테고리 필터 초기화
  document.querySelectorAll('.filter-chip').forEach(chip => chip.classList.remove('active'));
  document.querySelector('.filter-chip[data-filter="all"]').classList.add('active');
  
  // 평점 필터 초기화
  document.querySelectorAll('.rating-chip').forEach(chip => chip.classList.remove('active'));
  
  // 가격 범위 초기화
  document.getElementById('price-min').value = 0;
  document.getElementById('price-max').value = 500000;
  document.getElementById('range-min').value = 0;
  document.getElementById('range-max').value = 500000;
  
  // 슬라이더 트랙 초기화
  const sliderTrack = document.querySelector('.slider-track');
  if (sliderTrack) {
    sliderTrack.style.background = 'linear-gradient(to right, #e0e0e0 0%, #ff7043 0%, #ff7043 100%, #e0e0e0 100%)';
  }
  
  // 정렬 옵션 초기화
  document.getElementById('sort-select').value = 'popular';
  
  // 활성화된 필터 표시 초기화
  document.getElementById('active-filters').innerHTML = '';
  
  // 필터 적용
  applyFilters();
}

/**
 * 필터 적용
 */
function applyFilters() {
  // 선택된 카테고리 가져오기
  const selectedCategories = Array.from(document.querySelectorAll('.filter-chip.active'))
    .filter(chip => chip.dataset.filter !== 'all')
    .map(chip => chip.dataset.filter);
  
  // 선택된 평점 가져오기
  const ratingChip = document.querySelector('.rating-chip.active');
  const selectedRating = ratingChip ? parseInt(ratingChip.dataset.rating) : 0;
  
  // 가격 범위 가져오기
  const minPrice = parseInt(document.getElementById('price-min').value);
  const maxPrice = parseInt(document.getElementById('price-max').value);
  
  // 정렬 옵션 가져오기
  const sortBy = document.getElementById('sort-select').value;
  
  // 필터링 파라미터 구성
  const filterParams = {
    categories: selectedCategories,
    minPrice: minPrice,
    maxPrice: maxPrice,
    minRating: selectedRating,
    sortBy: sortBy
  };
  
  // 키보드 목록 로드 (실제 API 연동 시 사용할 파라미터)
  console.log('필터 적용:', filterParams);
  loadKeyboards(filterParams);
  
  // 활성화된 필터 표시 업데이트
  updateActiveFiltersDisplay();
  
  // TODO: API가 완성되면 필터링된 키보드 목록을 가져오는 코드로 대체
  filterKeyboardCards(filterParams);
}

/**
 * 키보드 카드 클라이언트 측 필터링 (API 구현 전 임시 기능)
 */
function filterKeyboardCards(filterParams) {
  const cards = document.querySelectorAll('.keyboard-card');
  
  cards.forEach(card => {
    let visible = true;
    
    // 카테고리 필터링
    if (filterParams.categories.length > 0) {
      const cardCategories = card.dataset.categories?.split(' ') || [];
      const hasMatchingCategory = filterParams.categories.some(category => 
        cardCategories.includes(category)
      );
      
      if (!hasMatchingCategory) {
        visible = false;
      }
    }
    
    // 가격 필터링 (임시 데이터 사용)
    const priceText = card.querySelector('.keyboard-price')?.textContent || '';
    const priceMatch = priceText.match(/[\d,]+/);
    if (priceMatch) {
      const price = parseInt(priceMatch[0].replace(/,/g, ''));
      if (price < filterParams.minPrice || price > filterParams.maxPrice) {
        visible = false;
      }
    }
    
    // 카드 표시/숨김
    card.style.display = visible ? '' : 'none';
  });
}

/**
 * 맨 위로 버튼 기능 설정
 */
function setupBackToTopButton() {
  const backToTopBtn = document.getElementById('back-to-top');
  
  if (!backToTopBtn) return;
  
  // 스크롤 이벤트
  window.addEventListener('scroll', function() {
    // 스크롤 위치에 따라 버튼 표시/숨김
    if (window.pageYOffset > 300) {
      backToTopBtn.classList.add('visible');
    } else {
      backToTopBtn.classList.remove('visible');
    }
  });
  
  // 버튼 클릭 이벤트
  backToTopBtn.addEventListener('click', function() {
    // 부드럽게 맨 위로 스크롤
    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    });
  });
}

// 문서 로드 시 맨 위로 버튼 설정
document.addEventListener('DOMContentLoaded', () => {
  // 기존 이벤트 설정
  // ...existing code...
  
  // 필터 패널 설정
  setupFilterPanel();
  
  // 맨 위로 버튼 설정
  setupBackToTopButton();
});
