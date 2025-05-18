// 키보드 상세 페이지 스크립트
document.addEventListener('DOMContentLoaded', () => {
  // URL에서 키보드 ID 가져오기
  const urlParams = new URLSearchParams(window.location.search);
  const keyboardId = urlParams.get('id');
  
  if (!keyboardId) {
    showErrorMessage('키보드 ID가 지정되지 않았습니다.');
    return;
  }
  
  // 키보드 상세 데이터 로드
  loadKeyboardDetail(keyboardId);
  
  // 리뷰 관련 이벤트 리스너 설정
  setupReviewListeners(keyboardId);
  
  // 탭 전환 이벤트 처리
  setupTabEvents();
  
  // 이미지 갤러리 설정
  setupImageGalleryEvents();
  
  // 이미지 줌 기능 설정
  setupImageZoom();
});

/**
 * 키보드 상세 정보 로드
 * @param {number} keyboardId 키보드 ID
 */
async function loadKeyboardDetail(keyboardId) {
  try {
    // 로딩 상태 표시
    showLoadingState();
    
    // 캐시 키 생성
    const cacheKey = `keyboard_detail_${keyboardId}`;
    const cachedData = sessionStorage.getItem(cacheKey);
    
    let keyboardData;
    
    // 캐시된 데이터가 있으면 사용 (1분 내)
    if (cachedData) {
      const parsedCache = JSON.parse(cachedData);
      const cacheAge = Date.now() - parsedCache.timestamp;
      
      if (cacheAge < 60000) { // 1분
        keyboardData = parsedCache.data;
        console.log('캐시된 키보드 상세 정보 사용');
      }
    }
    
    // 캐시가 없거나 오래된 경우 API 호출
    if (!keyboardData) {
      console.log('키보드 상세 정보 API 호출');
      keyboardData = await KeyboardService.getKeyboardDetail(keyboardId);
      
      // 유효한 데이터인지 확인
      if (!keyboardData || !keyboardData.id) {
        throw new Error('유효하지 않은 키보드 데이터');
      }
      
      // 결과 캐싱
      sessionStorage.setItem(cacheKey, JSON.stringify({
        timestamp: Date.now(),
        data: keyboardData
      }));
    }
    
    // 데이터 렌더링
    renderKeyboardDetail(keyboardData);
    
    // 리뷰 로드
    loadKeyboardReviews(keyboardId);
    
    // 로딩 상태 제거
    hideLoadingState();
    
    // 방문 이력 기록 (최근 본 키보드)
    recordRecentKeyboard(keyboardData);
  } catch (error) {
    console.error('키보드 상세 정보 로딩 오류:', error);
    
    // 네트워크 오류인지 확인
    if (error.message.includes('network') || error.message.includes('fetch')) {
      showErrorMessage('네트워크 연결을 확인해주세요.', true);
    } else if (error.message.includes('유효하지 않은 키보드')) {
      showErrorMessage('요청한 키보드를 찾을 수 없습니다.');
    } else {
      showErrorMessage('키보드 정보를 불러오는데 문제가 발생했습니다.', true);
    }
  }
}

/**
 * 키보드 상세 정보 렌더링
 * @param {Object} keyboard 키보드 데이터
 */
function renderKeyboardDetail(keyboard) {
  // 메인 정보 업데이트
  document.querySelector('.keyboard-title').textContent = keyboard.name || '키보드 이름';
  document.querySelector('.keyboard-price').textContent = keyboard.price ? `₩${formatPrice(keyboard.price)}` : '가격 정보 없음';
  
  // 별점 표시
  const ratingElement = document.querySelector('.keyboard-rate .stars');
  if (ratingElement) {
    updateRatingDisplay(keyboard.rating || 0, keyboard.reviewCount || 0);
  }
  
  // 메인 이미지 설정
  const mainImage = document.getElementById('mainImage');
  if (mainImage) {
    mainImage.src = keyboard.imageUrl || '../img/keyboard1.png';
    mainImage.alt = keyboard.name;
    
    // 이미지 로딩 애니메이션
    mainImage.style.opacity = 0;
    mainImage.onload = function() {
      setTimeout(() => {
        mainImage.style.transition = 'opacity 0.5s ease';
        mainImage.style.opacity = 1;
      }, 100);
    };
  }
  
  // 태그 목록 업데이트
  updateTags(keyboard.tags || []);
  
  // 관련 키보드 업데이트
  updateRelatedKeyboards(keyboard.relatedKeyboards || []);
  
  // 메타데이터 업데이트 (페이지 제목 등)
  document.title = `${keyboard.name || '키보드 상세 정보'} - KIRINI`;
}

/**
 * 키보드 리뷰 로드
 * @param {number} keyboardId 키보드 ID
 * @param {Object} params 페이징 및 정렬 파라미터
 */
async function loadKeyboardReviews(keyboardId, params = {}) {
  try {
    // 리뷰 컨테이너
    const reviewsContainer = document.querySelector('.reviews-container');
    if (!reviewsContainer) return;
    
    // 리뷰 로딩 상태 표시
    reviewsContainer.innerHTML = '<div class="loading-reviews">리뷰를 불러오는 중입니다...</div>';
    
    // 캐시 키 생성
    const cacheKey = `keyboard_reviews_${keyboardId}_${JSON.stringify(params)}`;
    const cachedData = sessionStorage.getItem(cacheKey);
    
    let reviewData;
    
    // 캐시된 데이터가 있으면 사용 (30초 내)
    if (cachedData) {
      const parsedCache = JSON.parse(cachedData);
      const cacheAge = Date.now() - parsedCache.timestamp;
      
      if (cacheAge < 30000) { // 30초
        reviewData = parsedCache.data;
        console.log('캐시된 리뷰 데이터 사용');
      }
    }
    
    // 캐시가 없거나 오래된 경우 API 호출
    if (!reviewData) {
      console.log('리뷰 데이터 API 호출');
      reviewData = await KeyboardService.getKeyboardReviews(keyboardId, params);
      
      // 결과 캐싱
      sessionStorage.setItem(cacheKey, JSON.stringify({
        timestamp: Date.now(),
        data: reviewData
      }));
    }
    
    // 리뷰 통계 업데이트
    updateReviewStats(reviewData.stats || {});
    
    // 리뷰 목록 렌더링
    renderReviews(reviewData.reviews || []);
    
    // 페이지네이션 업데이트
    updateReviewPagination(reviewData.pagination);
    
    // 리뷰 작성 폼 업데이트
    updateReviewForm(keyboardId);
  } catch (error) {
    console.error('키보드 리뷰 로딩 오류:', error);
    const reviewsContainer = document.querySelector('.reviews-container');
    if (reviewsContainer) {
      reviewsContainer.innerHTML = `
        <div class="error-message">
          리뷰를 불러오는데 문제가 발생했습니다.
          <button class="btn btn-sm btn-outline-primary retry-reviews mt-2">다시 시도</button>
        </div>
      `;
      
      // 리뷰 로드 재시도 버튼
      reviewsContainer.querySelector('.retry-reviews').addEventListener('click', () => {
        loadKeyboardReviews(keyboardId, params);
      });
    }
  }
}

/**
 * 별점 표시 업데이트
 * @param {number} rating 평점 (0-5)
 * @param {number} reviewCount 리뷰 수
 */
function updateRatingDisplay(rating = 0, reviewCount = 0) {
  const ratingElement = document.querySelector('.keyboard-rating');
  if (!ratingElement) return;
  
  const starsElement = document.querySelector('.rating-stars');
  const countElement = document.querySelector('.rating-count');
  
  // 별점 HTML 생성
  starsElement.innerHTML = generateStarRating(rating);
  
  // 리뷰 수 표시
  countElement.textContent = `(${rating.toFixed(1)}/5, 리뷰 ${reviewCount}개)`;
}

/**
 * 별점 HTML 생성
 * @param {number} rating 평점 (0-5)
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
 * 이미지 갤러리 설정
 * @param {Array} images 이미지 URL 배열
 */
function setupImageGallery(images) {
  const galleryContainer = document.querySelector('.keyboard-gallery-thumbnails');
  if (!galleryContainer) return;
  
  // 기본 이미지가 없으면 첫 번째 이미지를 추가
  if (images.length === 0) {
    images.push('/view/img/keyboard-placeholder.jpg');
  }
  
  // 썸네일 생성
  galleryContainer.innerHTML = images.map((imageUrl, index) => `
    <div class="gallery-thumbnail ${index === 0 ? 'active' : ''}">
      <img src="${imageUrl}" alt="키보드 이미지 ${index + 1}" data-index="${index}">
    </div>
  `).join('');
  
  // 메인 이미지 설정
  const mainImage = document.querySelector('.keyboard-main-image');
  if (mainImage && images.length > 0) {
    mainImage.src = images[0];
  }
}

/**
 * 이미지 갤러리 이벤트 설정
 */
function setupImageGalleryEvents() {
  // 갤러리 컨테이너
  const galleryContainer = document.querySelector('.keyboard-gallery-thumbnails');
  if (!galleryContainer) return;
  
  // 썸네일 클릭 이벤트
  galleryContainer.addEventListener('click', (e) => {
    const thumbnail = e.target.closest('.gallery-thumbnail img');
    if (!thumbnail) return;
    
    // 활성 썸네일 변경
    document.querySelectorAll('.gallery-thumbnail').forEach(thumb => {
      thumb.classList.remove('active');
    });
    thumbnail.parentElement.classList.add('active');
    
    // 메인 이미지 업데이트
    const mainImage = document.querySelector('.keyboard-main-image');
    if (mainImage) {
      mainImage.src = thumbnail.src;
    }
  });
  
  // 이미지 확대 기능
  const mainImage = document.querySelector('.keyboard-main-image');
  if (mainImage) {
    mainImage.addEventListener('click', () => {
      // 이미지 확대 모달 표시
      showImageModal(mainImage.src);
    });
  }
}

/**
 * 이미지 확대 모달 표시
 * @param {string} imageUrl 이미지 URL
 */
function showImageModal(imageUrl) {
  // 기존 모달 제거
  const existingModal = document.querySelector('.image-modal');
  if (existingModal) {
    existingModal.remove();
  }
  
  // 모달 생성
  const modal = document.createElement('div');
  modal.className = 'image-modal';
  modal.innerHTML = `
    <div class="modal-content">
      <span class="close-modal">&times;</span>
      <img src="${imageUrl}" alt="확대 이미지">
    </div>
  `;
  
  // 모달 닫기 이벤트
  modal.querySelector('.close-modal').addEventListener('click', () => {
    modal.remove();
  });
  
  // 모달 외부 클릭 시 닫기
  modal.addEventListener('click', (e) => {
    if (e.target === modal) {
      modal.remove();
    }
  });
  
  // 문서에 모달 추가
  document.body.appendChild(modal);
}

/**
 * 키보드 사양 테이블 업데이트
 * @param {Object} specs 키보드 사양 객체
 */
function updateSpecsTable(specs) {
  const specsTable = document.querySelector('.keyboard-specs-table tbody');
  if (!specsTable) return;
  
  // 테이블 내용 생성
  let tableContent = '';
  
  // 사양 정보 순회
  for (const [key, value] of Object.entries(specs)) {
    tableContent += `
      <tr>
        <th>${formatSpecName(key)}</th>
        <td>${value}</td>
      </tr>
    `;
  }
  
  // 내용이 없으면 기본 메시지 표시
  if (!tableContent) {
    tableContent = `
      <tr>
        <td colspan="2" class="no-specs">사양 정보가 없습니다.</td>
      </tr>
    `;
  }
  
  specsTable.innerHTML = tableContent;
}

/**
 * 사양 이름 포맷팅
 * @param {string} specName 사양 키 이름
 * @returns {string} 포맷팅된 사양 이름
 */
function formatSpecName(specName) {
  // 카멜케이스나 스네이크케이스를 적절히 변환
  return specName
    .replace(/_/g, ' ')
    .replace(/([A-Z])/g, ' $1')
    .replace(/^./, str => str.toUpperCase())
    .trim();
}

/**
 * 태그 목록 업데이트
 * @param {Array} tags 태그 배열
 */
function updateTags(tags) {
  const tagContainer = document.querySelector('.keyboard-tags');
  if (!tagContainer) return;
  
  // 태그가 없으면 컨테이너 숨김
  if (!tags || tags.length === 0) {
    tagContainer.style.display = 'none';
    return;
  }
  
  // 태그 HTML 생성
  tagContainer.innerHTML = tags.map(tag => 
    `<span class="keyboard-tag" data-tag="${tag}">${tag}</span>`
  ).join('');
  
  // 태그 클릭 이벤트
  tagContainer.querySelectorAll('.keyboard-tag').forEach(tagElement => {
    tagElement.addEventListener('click', () => {
      // 태그 검색 페이지로 이동
      window.location.href = `/view/keyboard/list.html?tag=${encodeURIComponent(tagElement.dataset.tag)}`;
    });
  });
  
  // 컨테이너 표시
  tagContainer.style.display = '';
}

/**
 * 관련 키보드 업데이트
 * @param {Array} relatedKeyboards 관련 키보드 배열
 */
function updateRelatedKeyboards(relatedKeyboards) {
  const relatedContainer = document.querySelector('.related-keyboards');
  if (!relatedContainer) return;
  
  // 관련 키보드가 없으면 섹션 숨김
  if (!relatedKeyboards || relatedKeyboards.length === 0) {
    const relatedSection = document.querySelector('.related-keyboards-section');
    if (relatedSection) {
      relatedSection.style.display = 'none';
    }
    return;
  }
  
  // 관련 키보드 HTML 생성
  relatedContainer.innerHTML = relatedKeyboards.map(keyboard => `
    <div class="related-keyboard-card">
      <a href="/view/keyboard/detail.html?id=${keyboard.id}">
        <div class="card">
          <div class="card-image">
            <img src="${keyboard.imageUrl || '/view/img/keyboard-placeholder.jpg'}" alt="${keyboard.name}">
          </div>
          <div class="card-content">
            <h4 class="card-title">${keyboard.name}</h4>
            <div class="card-price">₩${formatPrice(keyboard.price)}</div>
            <div class="card-rating">
              ${generateStarRating(keyboard.rating || 0)}
            </div>
          </div>
        </div>
      </a>
    </div>
  `).join('');
  
  // 섹션 표시
  const relatedSection = document.querySelector('.related-keyboards-section');
  if (relatedSection) {
    relatedSection.style.display = '';
  }
}

/**
 * 공유 링크 업데이트
 * @param {Object} keyboard 키보드 데이터
 */
function updateSharingLinks(keyboard) {
  const shareButtons = document.querySelectorAll('.share-button');
  if (!shareButtons.length) return;
  
  // 현재 페이지 URL
  const pageUrl = window.location.href;
  // 공유 제목
  const shareTitle = `${keyboard.name} - 키리니에서 확인하세요`;
  
  // SNS 공유 링크 설정
  shareButtons.forEach(button => {
    const platform = button.dataset.platform;
    if (!platform) return;
    
    switch (platform) {
      case 'facebook':
        button.href = `https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(pageUrl)}`;
        break;
      case 'twitter':
        button.href = `https://twitter.com/intent/tweet?url=${encodeURIComponent(pageUrl)}&text=${encodeURIComponent(shareTitle)}`;
        break;
      case 'kakaotalk':
        // 카카오톡 API 초기화 및 공유 기능은 별도 처리 필요
        break;
    }
  });
  
  // 클립보드 복사 버튼 이벤트
  const copyButton = document.querySelector('.share-button[data-platform="clipboard"]');
  if (copyButton) {
    copyButton.addEventListener('click', (e) => {
      e.preventDefault();
      
      // 클립보드에 URL 복사
      navigator.clipboard.writeText(pageUrl)
        .then(() => {
          alert('URL이 클립보드에 복사되었습니다.');
        })
        .catch(() => {
          alert('클립보드 복사에 실패했습니다.');
        });
    });
  }
}

/**
 * 리뷰 통계 업데이트
 * @param {Object} stats 리뷰 통계 데이터
 */
function updateReviewStats(stats) {
  const statsContainer = document.querySelector('.review-stats');
  if (!statsContainer) return;
  
  // 평균 평점
  const avgRating = stats.avgRating || 0;
  statsContainer.querySelector('.avg-rating-value').textContent = avgRating.toFixed(1);
  statsContainer.querySelector('.avg-rating-stars').innerHTML = generateStarRating(avgRating);
  
  // 평점 분포
  const distributionContainer = document.querySelector('.rating-distribution');
  if (distributionContainer && stats.distribution) {
    // 5점부터 1점까지 역순으로 표시
    for (let i = 5; i >= 1; i--) {
      const percentage = (stats.distribution[i] || 0) * 100;
      
      const barElement = distributionContainer.querySelector(`.rating-bar-${i} .bar-fill`);
      if (barElement) {
        barElement.style.width = `${percentage}%`;
      }
      
      const percentElement = distributionContainer.querySelector(`.rating-bar-${i} .bar-percent`);
      if (percentElement) {
        percentElement.textContent = `${Math.round(percentage)}%`;
      }
    }
  }
}

/**
 * 리뷰 목록 렌더링
 * @param {Array} reviews 리뷰 배열
 */
function renderReviews(reviews) {
  const reviewsContainer = document.querySelector('.reviews-list');
  if (!reviewsContainer) return;
  
  // 리뷰가 없을 경우
  if (!reviews || reviews.length === 0) {
    reviewsContainer.innerHTML = `
      <div class="no-reviews">
        <p>아직 리뷰가 없습니다.</p>
        <p>첫 번째 리뷰를 작성해보세요!</p>
      </div>
    `;
    return;
  }
  
  // 리뷰 목록 생성
  reviewsContainer.innerHTML = reviews.map(review => `
    <div class="review-item" data-review-id="${review.id}">
      <div class="review-header">
        <div class="reviewer-info">
          <span class="reviewer-name">${review.userName}</span>
          <span class="review-date">${formatDate(review.createdAt)}</span>
        </div>
        <div class="review-rating">
          ${generateStarRating(review.rating)}
        </div>
      </div>
      <div class="review-content">
        <div class="review-text">${review.content}</div>
        ${review.images && review.images.length ? `
          <div class="review-images">
            ${review.images.map(image => `
              <div class="review-image">
                <img src="${image}" alt="리뷰 이미지" class="review-img-thumbnail">
              </div>
            `).join('')}
          </div>
        ` : ''}
      </div>
      <div class="review-footer">
        <button class="btn-helpful" data-review-id="${review.id}">
          <i class="far fa-thumbs-up"></i>
          도움됨
          <span class="helpful-count">${review.helpfulCount || 0}</span>
        </button>
        <button class="btn-report" data-review-id="${review.id}">
          <i class="far fa-flag"></i>
          신고
        </button>
      </div>
    </div>
  `).join('');
  
  // 리뷰 이미지 클릭 이벤트 (확대)
  reviewsContainer.querySelectorAll('.review-img-thumbnail').forEach(img => {
    img.addEventListener('click', () => {
      showImageModal(img.src);
    });
  });
  
  // 도움됨 버튼 클릭 이벤트
  reviewsContainer.querySelectorAll('.btn-helpful').forEach(button => {
    button.addEventListener('click', async () => {
      if (!ApiClient.getAuthToken()) {
        alert('로그인이 필요합니다.');
        return;
      }
      
      const reviewId = button.dataset.reviewId;
      try {
        await KeyboardService.markReviewHelpful(reviewId);
        
        // 카운트 증가
        const countElement = button.querySelector('.helpful-count');
        const currentCount = parseInt(countElement.textContent) || 0;
        countElement.textContent = currentCount + 1;
        
        // 버튼 비활성화
        button.classList.add('active');
        button.disabled = true;
      } catch (error) {
        console.error('리뷰에 도움됨 표시 실패:', error);
        alert('이미 도움됨으로 표시하셨거나 오류가 발생했습니다.');
      }
    });
  });
  
  // 신고 버튼 클릭 이벤트
  reviewsContainer.querySelectorAll('.btn-report').forEach(button => {
    button.addEventListener('click', () => {
      if (!ApiClient.getAuthToken()) {
        alert('로그인이 필요합니다.');
        return;
      }
      
      const reviewId = button.dataset.reviewId;
      const reason = prompt('신고 사유를 입력해주세요:');
      
      if (reason) {
        KeyboardService.reportReview(reviewId, reason)
          .then(() => {
            alert('신고가 접수되었습니다.');
          })
          .catch(error => {
            console.error('리뷰 신고 실패:', error);
            alert('신고 처리 중 오류가 발생했습니다.');
          });
      }
    });
  });
}

/**
 * 리뷰 페이지네이션 업데이트
 * @param {Object} pagination 페이징 정보
 */
function updateReviewPagination(pagination) {
  const paginationContainer = document.querySelector('.review-pagination');
  if (!paginationContainer || !pagination) return;
  
  const { currentPage, totalPages } = pagination;
  
  // 페이지 링크 생성
  let paginationHTML = `
    <nav aria-label="리뷰 페이지 네비게이션">
      <ul class="pagination">
        <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
          <a class="page-link" href="#reviews" data-page="${currentPage - 1}" aria-label="이전">
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
        <a class="page-link" href="#reviews" data-page="${i}">${i}</a>
      </li>
    `;
  }
  
  paginationHTML += `
        <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
          <a class="page-link" href="#reviews" data-page="${currentPage + 1}" aria-label="다음">
            <span aria-hidden="true">&raquo;</span>
          </a>
        </li>
      </ul>
    </nav>
  `;
  
  paginationContainer.innerHTML = paginationHTML;
  
  // 페이지 링크 이벤트 설정
  document.querySelectorAll('.review-pagination .page-link').forEach(link => {
    link.addEventListener('click', (e) => {
      e.preventDefault();
      
      const page = parseInt(link.dataset.page);
      if (isNaN(page) || page < 1 || page > totalPages) return;
      
      // 리뷰 목록 새로 로드
      const urlParams = new URLSearchParams(window.location.search);
      const keyboardId = urlParams.get('id');
      
      loadKeyboardReviews(keyboardId, { page });
      
      // 리뷰 섹션으로 스크롤
      const reviewsTab = document.querySelector('a[href="#reviews"]');
      if (reviewsTab) {
        reviewsTab.click();
      }
    });
  });
}

/**
 * 리뷰 작성 폼 업데이트
 * @param {number} keyboardId 키보드 ID
 */
function updateReviewForm(keyboardId) {
  const reviewForm = document.querySelector('#review-form');
  if (!reviewForm) return;
  
  // 로그인 상태 확인
  if (!Auth.isLoggedIn()) {
    // 로그인하지 않은 경우 로그인 안내 표시
    reviewForm.innerHTML = `
      <div class="login-required-message">
        <p>리뷰를 작성하려면 <a href="/view/login.html?returnUrl=${encodeURIComponent(window.location.href)}">로그인</a>이 필요합니다.</p>
      </div>
    `;
    return;
  }
  
  // 이미 리뷰 작성 폼이 있으면 업데이트하지 않음
  if (reviewForm.querySelector('textarea')) return;
  
  // 리뷰 작성 폼 생성
  reviewForm.innerHTML = `
    <div class="form-group mb-3">
      <label for="review-rating">평점</label>
      <div class="rating-input">
        <div class="rating-stars">
          <i class="far fa-star" data-rating="1"></i>
          <i class="far fa-star" data-rating="2"></i>
          <i class="far fa-star" data-rating="3"></i>
          <i class="far fa-star" data-rating="4"></i>
          <i class="far fa-star" data-rating="5"></i>
        </div>
        <input type="hidden" id="review-rating" name="rating" value="0">
      </div>
    </div>
    <div class="form-group mb-3">
      <label for="review-title">제목</label>
      <input type="text" id="review-title" class="form-control" placeholder="리뷰 제목을 입력하세요">
    </div>
    <div class="form-group mb-3">
      <label for="review-content">내용</label>
      <textarea id="review-content" class="form-control" rows="5" placeholder="리뷰 내용을 입력하세요"></textarea>
    </div>
    <div class="form-group mb-3">
      <label for="review-pros">장점</label>
      <textarea id="review-pros" class="form-control" rows="2" placeholder="이 키보드의 장점을 입력하세요"></textarea>
    </div>
    <div class="form-group mb-3">
      <label for="review-cons">단점</label>
      <textarea id="review-cons" class="form-control" rows="2" placeholder="이 키보드의 단점을 입력하세요"></textarea>
    </div>
    <div class="form-group text-end">
      <button type="submit" class="btn btn-primary">리뷰 등록</button>
    </div>
  `;
  
  // 별점 선택 이벤트
  const stars = reviewForm.querySelectorAll('.rating-stars i');
  const ratingInput = reviewForm.querySelector('#review-rating');
  
  stars.forEach(star => {
    star.addEventListener('click', function() {
      const rating = parseInt(this.dataset.rating);
      
      // 별점 입력값 설정
      ratingInput.value = rating;
      
      // 별점 UI 업데이트
      stars.forEach((s, index) => {
        if (index < rating) {
          s.className = 'fas fa-star';
        } else {
          s.className = 'far fa-star';
        }
      });
    });
    
    // 마우스 호버 효과
    star.addEventListener('mouseenter', function() {
      const rating = parseInt(this.dataset.rating);
      
      stars.forEach((s, index) => {
        if (index < rating) {
          s.className = 'fas fa-star hover';
        }
      });
    });
    
    star.addEventListener('mouseleave', function() {
      const currentRating = parseInt(ratingInput.value);
      
      stars.forEach((s, index) => {
        if (index < currentRating) {
          s.className = 'fas fa-star';
        } else {
          s.className = 'far fa-star';
        }
      });
    });
  });
  
  // 리뷰 폼 제출 이벤트
  reviewForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const submitButton = reviewForm.querySelector('button[type="submit"]');
    const rating = parseInt(ratingInput.value);
    const title = reviewForm.querySelector('#review-title').value.trim();
    const content = reviewForm.querySelector('#review-content').value.trim();
    const pros = reviewForm.querySelector('#review-pros').value.trim();
    const cons = reviewForm.querySelector('#review-cons').value.trim();
    
    // 유효성 검사
    if (rating === 0) {
      alert('별점을 선택해주세요.');
      return;
    }
    
    if (!title) {
      alert('리뷰 제목을 입력해주세요.');
      reviewForm.querySelector('#review-title').focus();
      return;
    }
    
    if (!content) {
      alert('리뷰 내용을 입력해주세요.');
      reviewForm.querySelector('#review-content').focus();
      return;
    }
    
    // 버튼 비활성화
    submitButton.disabled = true;
    submitButton.innerHTML = '<span class="spinner-border spinner-border-sm"></span> 처리 중...';
    
    try {
      // 리뷰 데이터 구성
      const reviewData = {
        rating,
        title,
        content,
        pros,
        cons
      };
      
      // API 호출
      await KeyboardService.addReview(keyboardId, reviewData);
      
      // 성공 메시지
      alert('리뷰가 등록되었습니다.');
      
      // 캐시 삭제 (리뷰 목록 갱신 위함)
      const cacheKeyPattern = `keyboard_reviews_${keyboardId}`;
      Object.keys(sessionStorage).forEach(key => {
        if (key.startsWith(cacheKeyPattern)) {
          sessionStorage.removeItem(key);
        }
      });
      
      // 리뷰 목록 새로고침
      loadKeyboardReviews(keyboardId);
      
      // 폼 초기화
      reviewForm.reset();
      ratingInput.value = 0;
      stars.forEach(s => {
        s.className = 'far fa-star';
      });
    } catch (error) {
      console.error('리뷰 등록 실패:', error);
      alert('리뷰 등록에 실패했습니다. 다시 시도해주세요.');
    } finally {
      // 버튼 상태 복원
      submitButton.disabled = false;
      submitButton.innerHTML = '리뷰 등록';
    }
  });
}

/**
 * 탭 전환 이벤트 설정
 */
function setupTabEvents() {
  const tabButtons = document.querySelectorAll('.tab-btn');
  const tabContents = document.querySelectorAll('.tab-content');
  
  // 탭 인디케이터 요소 생성
  const tabButtonContainer = document.querySelector('.tab-buttons');
  const tabIndicator = document.createElement('div');
  tabIndicator.className = 'tab-indicator';
  tabButtonContainer.appendChild(tabIndicator);
  
  // 초기 활성 탭 설정
  const initialActiveTab = document.querySelector('.tab-btn.active');
  if (initialActiveTab) {
    updateTabIndicator(initialActiveTab);
  }
  
  tabButtons.forEach((button) => {
    button.addEventListener('click', () => {
      const tabId = button.getAttribute('data-tab');
      
      // 모든 탭 버튼에서 active 클래스 제거
      tabButtons.forEach((btn) => btn.classList.remove('active'));
      
      // 모든 탭 콘텐츠에서 active 클래스 제거
      tabContents.forEach((content) => content.classList.remove('active'));
      
      // 클릭한 탭 버튼과 연결된 콘텐츠에 active 클래스 추가
      button.classList.add('active');
      document.getElementById('tab-' + tabId).classList.add('active');
      
      // 탭 인디케이터 업데이트
      updateTabIndicator(button);
    });
  });
}

/**
 * 탭 인디케이터 위치 업데이트
 * @param {HTMLElement} activeTab 활성 탭 요소
 */
function updateTabIndicator(activeTab) {
  const tabIndicator = document.querySelector('.tab-indicator');
  if (!tabIndicator) return;
  
  const tabLeft = activeTab.offsetLeft;
  const tabWidth = activeTab.offsetWidth;
  
  tabIndicator.style.width = `${tabWidth}px`;
  tabIndicator.style.left = `${tabLeft}px`;
}

/**
 * 이미지 갤러리 이벤트 설정
 */
function setupImageGalleryEvents() {
  const galleryImages = document.querySelectorAll('.gallery-image');
  const mainImage = document.getElementById('mainImage');
  
  if (!mainImage) return;
  
  galleryImages.forEach((image) => {
    image.addEventListener('click', function() {
      // 메인 이미지 변경
      const newSrc = this.getAttribute('src');
      
      // 현재 활성화된 이미지에서 active 클래스 제거
      document.querySelectorAll('.gallery-image.active').forEach((img) => {
        img.classList.remove('active');
      });
      
      // 클릭한 이미지에 active 클래스 추가
      this.classList.add('active');
      
      // 메인 이미지 페이드 아웃 후 변경 후 페이드 인
      mainImage.style.opacity = 0;
      
      setTimeout(() => {
        mainImage.src = newSrc;
        mainImage.onload = function() {
          mainImage.style.opacity = 1;
        };
      }, 300);
    });
  });
}

/**
 * 이미지 줌 기능 설정
 */
function setupImageZoom() {
  const mainImage = document.getElementById('mainImage');
  
  if (!mainImage) return;
  
  mainImage.addEventListener('click', function() {
    this.classList.toggle('zoomed');
    
    if (this.classList.contains('zoomed')) {
      // 이미지 확대 시 드래그로 이미지 이동 가능하게 설정
      let isDragging = false;
      let startX, startY, startLeft, startTop;
      
      const imageContainer = this.parentElement;
      
      this.addEventListener('mousedown', startDrag);
      document.addEventListener('mousemove', drag);
      document.addEventListener('mouseup', endDrag);
      
      // 터치 이벤트 지원
      this.addEventListener('touchstart', startDrag);
      document.addEventListener('touchmove', drag);
      document.addEventListener('touchend', endDrag);
      
      function startDrag(e) {
        if (!mainImage.classList.contains('zoomed')) return;
        
        isDragging = true;
        e.preventDefault();
        
        if (e.type === 'touchstart') {
          startX = e.touches[0].clientX;
          startY = e.touches[0].clientY;
        } else {
          startX = e.clientX;
          startY = e.clientY;
        }
        
        startLeft = parseInt(mainImage.style.left || 0);
        startTop = parseInt(mainImage.style.top || 0);
        
        // 커서 스타일 변경
        mainImage.style.cursor = 'grabbing';
      }
      
      function drag(e) {
        if (!isDragging) return;
        
        let clientX, clientY;
        if (e.type === 'touchmove') {
          clientX = e.touches[0].clientX;
          clientY = e.touches[0].clientY;
          e.preventDefault(); // 스크롤 방지
        } else {
          clientX = e.clientX;
          clientY = e.clientY;
        }
        
        const deltaX = clientX - startX;
        const deltaY = clientY - startY;
        
        mainImage.style.left = `${startLeft + deltaX}px`;
        mainImage.style.top = `${startTop + deltaY}px`;
      }
      
      function endDrag() {
        isDragging = false;
        mainImage.style.cursor = 'zoom-out';
      }
    } else {
      // 확대 해제 시 위치 초기화
      this.style.left = '0';
      this.style.top = '0';
    }
  });
}

/**
 * 로딩 상태 표시
 */
function showLoadingState() {
  const container = document.querySelector('.keyboard-detail-container');
  if (!container) return;
  
  // 로딩 인디케이터 추가
  const loadingElement = document.createElement('div');
  loadingElement.className = 'loading-indicator';
  loadingElement.innerHTML = `
    <div class="spinner-border text-primary" role="status">
      <span class="visually-hidden">로딩 중...</span>
    </div>
    <p>키보드 정보를 불러오는 중입니다...</p>
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
 * 최근 본 키보드 기록
 * @param {Object} keyboard 키보드 데이터
 */
function recordRecentKeyboard(keyboard) {
  try {
    // 필요한 데이터만 저장
    const keyboardInfo = {
      id: keyboard.id,
      name: keyboard.name,
      brand: keyboard.brand,
      imageUrl: keyboard.imageUrl || keyboard.images?.[0],
      price: keyboard.price,
      timestamp: Date.now()
    };
    
    // 로컬 스토리지에서 기존 기록 가져오기
    const recentItems = JSON.parse(localStorage.getItem('recent_keyboards') || '[]');
    
    // 이미 있는 항목이면 제거
    const existingIndex = recentItems.findIndex(item => item.id === keyboardInfo.id);
    if (existingIndex !== -1) {
      recentItems.splice(existingIndex, 1);
    }
    
    // 배열 맨 앞에 추가
    recentItems.unshift(keyboardInfo);
    
    // 최대 10개만 유지
    const updatedList = recentItems.slice(0, 10);
    
    // 로컬 스토리지에 저장
    localStorage.setItem('recent_keyboards', JSON.stringify(updatedList));
  } catch (error) {
    console.error('최근 본 키보드 기록 오류:', error);
    // 비치명적 오류이므로 사용자에게 알리지 않음
  }
}

/**
 * 오류 메시지 표시
 * @param {string} message 오류 메시지
 * @param {boolean} showRetry 재시도 버튼 표시 여부
 */
function showErrorMessage(message, showRetry = false) {
  const container = document.querySelector('.keyboard-detail-container');
  if (!container) return;
  
  // 로딩 인디케이터 제거
  hideLoadingState();
  
  // 오류 메시지 추가
  const errorElement = document.createElement('div');
  errorElement.className = 'error-message alert alert-danger';
  
  let errorHTML = message;
  
  // 재시도 버튼 추가
  if (showRetry) {
    errorHTML += `
      <div class="mt-3">
        <button class="btn btn-outline-primary retry-button">다시 시도</button>
        <button class="btn btn-link return-to-list">목록으로 돌아가기</button>
      </div>
    `;
  } else {
    errorHTML += `
      <div class="mt-3">
        <button class="btn btn-link return-to-list">목록으로 돌아가기</button>
      </div>
    `;
  }
  
  errorElement.innerHTML = errorHTML;
  container.innerHTML = '';
  container.appendChild(errorElement);
  
  // 재시도 버튼 이벤트 설정
  if (showRetry) {
    container.querySelector('.retry-button').addEventListener('click', () => {
      const urlParams = new URLSearchParams(window.location.search);
      const keyboardId = urlParams.get('id');
      loadKeyboardDetail(keyboardId);
    });
  }
  
  // 목록으로 돌아가기 버튼
  container.querySelector('.return-to-list').addEventListener('click', () => {
    window.location.href = '/view/keyboard_info.html';
  });
}

/**
 * 날짜 포맷팅
 * @param {string} dateString 날짜 문자열
 * @returns {string} 포맷된 날짜 문자열
 */
function formatDate(dateString) {
  const date = new Date(dateString);
  return `${date.getFullYear()}년 ${date.getMonth() + 1}월 ${date.getDate()}일`;
}

/**
 * 가격 포맷팅 (1000단위 콤마)
 * @param {number} price 가격
 * @returns {string} 포맷된 가격
 */
function formatPrice(price) {
  return price?.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',') || '0';
}
