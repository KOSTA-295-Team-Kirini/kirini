// 필터 기능
document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.filter-btn').forEach(button => {
    button.addEventListener('click', () => {
      // 활성화된 버튼 상태 변경
      document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.classList.remove('active');
      });
      button.classList.add('active');
      
      const filterValue = button.dataset.filter;
      
      // 키보드 카드 필터링
      document.querySelectorAll('.keyboard-card').forEach(card => {
        const categories = card.dataset.categories.split(' ');
        
        if (filterValue === 'all' || categories.includes(filterValue)) {
          card.style.display = '';
        } else {
          card.style.display = 'none';
        }
      });
    });
  });

  // 태그 클릭 시 검색 기능
  document.querySelectorAll('.keyboard-tag').forEach(tag => {
    tag.addEventListener('click', () => {
      const tagName = tag.dataset.tag;
      if (!tagName) return;
      
      // 모든 필터 버튼 비활성화
      document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.classList.remove('active');
      });
      
      // 태그에 맞는 키보드 표시
      document.querySelectorAll('.keyboard-card').forEach(card => {
        let hasTag = false;
        
        // 카드 내의 모든 태그를 검사
        card.querySelectorAll('.keyboard-tag').forEach(cardTag => {
          if (cardTag.dataset.tag === tagName) {
            hasTag = true;
          }
        });
        
        card.style.display = hasTag ? '' : 'none';
      });
      
      // 스크롤을 페이지 상단으로 이동
      window.scrollTo({
        top: document.querySelector('.filter-container').offsetTop - 20,
        behavior: 'smooth'
      });
    });
  });
});

// 키보드 데이터 로드 함수
async function loadKeyboardList() {
  try {
    // API에서 키보드 목록 가져오기
    const response = await API.keyboard.getList();
    
    if (!response || !response.keyboardList) {
      console.error('키보드 목록을 불러오는데 실패했습니다.');
      return;
    }
    
    const keyboardList = response.keyboardList;
    const keyboardGrid = document.querySelector('.keyboard-grid');
    
    // 기존 키보드 카드 삭제
    keyboardGrid.innerHTML = '';
    
    // 키보드 카드 생성 및 추가
    keyboardList.forEach(keyboard => {
      // 카테고리 문자열 생성
      const categoriesStr = getCategoriesString(keyboard);
      
      // 태그 HTML 생성
      const tagsHtml = getTagsHtml(keyboard.tags);
      
      // 별점 표시 텍스트 생성
      const starsText = getStarsText(keyboard.avgScore);
      
      // 이미지 URL 설정
      const imageUrl = keyboard.keyboardImageUrl || '../img/keyboard_default.jpg';
      const placeholderUrl = `https://via.placeholder.com/400x200?text=${encodeURIComponent(keyboard.keyboardName)}`;
      
      // 키보드 카드 HTML
      const cardHtml = `
        <div class="keyboard-card" data-categories="${categoriesStr}">
          <img src="${imageUrl}" alt="${keyboard.keyboardName}" class="keyboard-image" 
               onerror="this.src='${placeholderUrl}'">
          <div class="keyboard-content">
            <h3 class="keyboard-title">${keyboard.keyboardName}</h3>
            <div class="keyboard-tags">
              ${tagsHtml}
            </div>
            <div class="keyboard-rating">
              <span class="stars">${starsText}</span>
              <span class="rating-count">(${keyboard.avgScore ? keyboard.avgScore.toFixed(1) : '0.0'}/5, ${keyboard.reviewCount || 0}개 리뷰)</span>
            </div>
            <p class="keyboard-desc">${keyboard.keyboardDescription || ''}</p>
            <div class="keyboard-actions">
              <a href="keyboard_detail.html?id=${keyboard.keyboardId}" class="keyboard-detail-btn">상세정보 보기</a>
            </div>
          </div>
        </div>
      `;
      
      // 카드 추가
      keyboardGrid.insertAdjacentHTML('beforeend', cardHtml);
    });
    
    // 태그 클릭 이벤트 다시 설정
    setupTagClickEvents();
    
  } catch (error) {
    console.error('키보드 목록을 불러오는데 실패했습니다:', error);
  }
}

// 카테고리 문자열 생성 (필터링용)
function getCategoriesString(keyboard) {
  const categories = [];
  
  // 대표 카테고리 추가 (샘플)
  if (keyboard.keyboardSwitchType && keyboard.keyboardSwitchType.includes('기계식')) {
    categories.push('mechanical');
  }
  
  if (keyboard.keyboardConnectType && keyboard.keyboardConnectType.includes('무선')) {
    categories.push('wireless');
  }
  
  // 태그 기반 카테고리 추가
  if (keyboard.tags) {
    keyboard.tags.forEach(tag => {
      if (tag === '게이밍' || tag.tagName === '게이밍') {
        categories.push('gaming');
      } else if (tag === '사무용' || tag.tagName === '사무용') {
        categories.push('office');
      } else if (tag === '커스텀' || tag.tagName === '커스텀') {
        categories.push('custom');
      }
    });
  }
  
  // 모든 키보드는 'all' 카테고리에 포함
  categories.push('all');
  
  return categories.join(' ');
}

// 태그 HTML 생성
function getTagsHtml(tags) {
  if (!tags || !Array.isArray(tags) || tags.length === 0) {
    return '';
  }
  
  return tags.map(tag => {
    const tagName = typeof tag === 'string' ? tag : (tag.tagName || '');
    const isAdmin = typeof tag === 'object' && tag.isAdmin;
    const tagClass = isAdmin ? 'tag-admin' : 'tag-user';
    const count = typeof tag === 'object' && tag.voteCount ? `<span class="tag-count">${tag.voteCount}</span>` : '';
    
    return `<span class="keyboard-tag ${tagClass}" data-tag="${tagName}">${tagName}${count}</span>`;
  }).join('');
}

// 별점 텍스트 생성
function getStarsText(score) {
  if (!score) return '☆☆☆☆☆';
  
  const fullStars = Math.floor(score);
  const halfStar = score - fullStars >= 0.5;
  
  let starsText = '';
  for (let i = 0; i < fullStars; i++) {
    starsText += '★';
  }
  if (halfStar) {
    starsText += '☆';
  }
  for (let i = 0; i < 5 - fullStars - (halfStar ? 1 : 0); i++) {
    starsText += '☆';
  }
  
  return starsText;
}

// 태그 클릭 이벤트 설정
function setupTagClickEvents() {
  document.querySelectorAll('.keyboard-tag').forEach(tag => {
    tag.addEventListener('click', () => {
      const tagName = tag.dataset.tag;
      if (!tagName) return;
      
      // 모든 필터 버튼 비활성화
      document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.classList.remove('active');
      });
      
      // 태그에 맞는 키보드 표시
      document.querySelectorAll('.keyboard-card').forEach(card => {
        let hasTag = false;
        
        // 카드 내의 모든 태그를 검사
        card.querySelectorAll('.keyboard-tag').forEach(cardTag => {
          if (cardTag.dataset.tag === tagName) {
            hasTag = true;
          }
        });
        
        card.style.display = hasTag ? '' : 'none';
      });
      
      // 스크롤을 페이지 상단으로 이동
      window.scrollTo({
        top: document.querySelector('.filter-container').offsetTop - 20,
        behavior: 'smooth'
      });
    });
  });
}

// 필터 버튼 기능
function setupFilterButtons() {
  document.querySelectorAll('.filter-btn').forEach(button => {
    button.addEventListener('click', () => {
      // 활성화된 버튼 상태 변경
      document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.classList.remove('active');
      });
      button.classList.add('active');
      
      const filterValue = button.dataset.filter;
      
      // 키보드 카드 필터링
      document.querySelectorAll('.keyboard-card').forEach(card => {
        const categories = card.dataset.categories ? card.dataset.categories.split(' ') : [];
        
        if (filterValue === 'all' || categories.includes(filterValue)) {
          card.style.display = '';
        } else {
          card.style.display = 'none';
        }
      });
    });
  });
}

// 검색 기능 설정
function setupSearch() {
  const searchForm = document.querySelector('.search-form');
  if (!searchForm) return;
  
  searchForm.addEventListener('submit', async function(e) {
    e.preventDefault();
    
    const searchInput = document.querySelector('#search-input');
    if (!searchInput) return;
    
    const keyword = searchInput.value.trim();
    if (!keyword) return;
    
    try {
      // API를 통한 검색
      const response = await API.keyboard.search({ keyword });
      
      if (!response || !response.keyboardList) {
        alert('검색 중 오류가 발생했습니다.');
        return;
      }
      
      const keyboardGrid = document.querySelector('.keyboard-grid');
      
      // 결과가 없는 경우
      if (response.keyboardList.length === 0) {
        keyboardGrid.innerHTML = `
          <div class="no-results">
            <p>'${keyword}' 검색 결과가 없습니다.</p>
          </div>
        `;
        return;
      }
      
      // 기존 키보드 카드 삭제
      keyboardGrid.innerHTML = '';
      
      // 검색 결과 표시
      response.keyboardList.forEach(keyboard => {
        // 기존 loadKeyboardList와 동일한 방식으로 카드 생성
        const categoriesStr = getCategoriesString(keyboard);
        const tagsHtml = getTagsHtml(keyboard.tags);
        const starsText = getStarsText(keyboard.avgScore);
        const imageUrl = keyboard.keyboardImageUrl || '../img/keyboard_default.jpg';
        const placeholderUrl = `https://via.placeholder.com/400x200?text=${encodeURIComponent(keyboard.keyboardName)}`;
        
        const cardHtml = `
          <div class="keyboard-card" data-categories="${categoriesStr}">
            <img src="${imageUrl}" alt="${keyboard.keyboardName}" class="keyboard-image" 
                 onerror="this.src='${placeholderUrl}'">
            <div class="keyboard-content">
              <h3 class="keyboard-title">${keyboard.keyboardName}</h3>
              <div class="keyboard-tags">
                ${tagsHtml}
              </div>
              <div class="keyboard-rating">
                <span class="stars">${starsText}</span>
                <span class="rating-count">(${keyboard.avgScore ? keyboard.avgScore.toFixed(1) : '0.0'}/5, ${keyboard.reviewCount || 0}개 리뷰)</span>
              </div>
              <p class="keyboard-desc">${keyboard.keyboardDescription || ''}</p>
              <div class="keyboard-actions">
                <a href="keyboard_detail.html?id=${keyboard.keyboardId}" class="keyboard-detail-btn">상세정보 보기</a>
              </div>
            </div>
          </div>
        `;
        
        keyboardGrid.insertAdjacentHTML('beforeend', cardHtml);
      });
      
      // 태그 클릭 이벤트 다시 설정
      setupTagClickEvents();
      
      // 검색 결과 메시지 표시
      alert(`'${keyword}' 검색 결과: ${response.keyboardList.length}개의 키보드를 찾았습니다.`);
      
    } catch (error) {
      console.error('검색 중 오류 발생:', error);
      alert('검색 중 오류가 발생했습니다.');
    }
  });
}

// 페이지 초기화
document.addEventListener('DOMContentLoaded', () => {
  loadKeyboardList();
  setupFilterButtons();
  setupSearch();
});
