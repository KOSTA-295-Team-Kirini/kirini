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

// 페이지 로드시 키보드 데이터 불러오기
document.addEventListener('DOMContentLoaded', function() {
  loadKeyboardList();
});

// --- 목업 데이터 정의 시작 ---
const mockKeyboardData = [
  {
    keyboardId: 1,
    keyboardName: "커세어 K70 RGB PRO",
    keyboardImageUrl: "../img/keyboard1.png",
    tags: [{ tagName: "게이밍", isAdmin: true, voteCount: 150 }, { tagName: "기계식", isAdmin: false, voteCount: 95 }, { tagName: "풀배열", isAdmin: false, voteCount: 70 }],
    avgScore: 4.8,
    reviewCount: 120,
    keyboardDescription: "최고의 게이밍 경험을 위한 OPX 광적축 스위치를 탑재한 풀배열 기계식 키보드입니다. AXON 하이퍼 폴링 기술로 빠른 반응 속도를 자랑합니다.",
    keyboardSwitchType: "기계식 (광적축)",
    keyboardConnectType: "유선"
  },
  {
    keyboardId: 2,
    keyboardName: "로지텍 MX Keys S",
    keyboardImageUrl: "../img/keyboard2.png",
    tags: [{ tagName: "사무용", isAdmin: true, voteCount: 200 }, { tagName: "무선", isAdmin: false, voteCount: 180 }, { tagName: "펜타그래프", isAdmin: false, voteCount: 120 }],
    avgScore: 4.6,
    reviewCount: 250,
    keyboardDescription: "조용하고 편안한 타이핑을 제공하는 스마트 백라이트 탑재 무선 키보드입니다. 멀티 디바이스 연결 및 Flow 기능을 지원합니다.",
    keyboardSwitchType: "펜타그래프",
    keyboardConnectType: "무선 (블루투스, Logi Bolt)"
  },
  {
    keyboardId: 3,
    keyboardName: "키크론 K2 Pro (QMK/VIA)",
    keyboardImageUrl: "../img/keyboard3.png",
    tags: [{ tagName: "커스텀", isAdmin: false, voteCount: 110 }, { tagName: "기계식", isAdmin: false, voteCount: 90 }, { tagName: "무선", isAdmin: false, voteCount: 85 }, { tagName: "75%배열", isAdmin: false, voteCount: 60 }],
    avgScore: 4.7,
    reviewCount: 95,
    keyboardDescription: "QMK/VIA를 지원하여 완벽한 커스터마이징이 가능한 75% 배열의 유무선 기계식 키보드입니다. 핫스왑 기능을 제공합니다.",
    keyboardSwitchType: "기계식 (선택 가능)",
    keyboardConnectType: "유무선 (블루투스, C타입 케이블)"
  },
  {
    keyboardId: 4,
    keyboardName: "앱코 KN01 텐키리스",
    keyboardImageUrl: "../img/keyboard4.png",
    tags: [{ tagName: "가성비", isAdmin: false, voteCount: 130 }, { tagName: "기계식", isAdmin: false, voteCount: 100 }, { tagName: "텐키리스", isAdmin: false, voteCount: 80 }],
    avgScore: 4.3,
    reviewCount: 180,
    keyboardDescription: "뛰어난 가성비를 자랑하는 텐키리스 기계식 키보드입니다. 다양한 스위치 옵션과 레인보우 LED를 제공합니다.",
    keyboardSwitchType: "기계식 (선택 가능)",
    keyboardConnectType: "유선"
  },
  {
    keyboardId: 5,
    keyboardName: "레이저 Huntsman V2 Analog",
    keyboardImageUrl: "../img/keyboard5.png",
    tags: [{ tagName: "게이밍", isAdmin: true, voteCount: 160 }, { tagName: "광축", isAdmin: false, voteCount: 110 }, { tagName: "아날로그", isAdmin: false, voteCount: 90 }],
    avgScore: 4.9,
    reviewCount: 150,
    keyboardDescription: "레이저 아날로그 광학 스위치를 탑재하여 압력 감지 입력이 가능한 혁신적인 게이밍 키보드입니다. 더블샷 PBT 키캡을 사용했습니다.",
    keyboardSwitchType: "기계식 (아날로그 광축)",
    keyboardConnectType: "유선"
  },
  {
    keyboardId: 6,
    keyboardName: "한성컴퓨터 GK898B OfficeMaster",
    keyboardImageUrl: "../img/keyboard6.png",
    tags: [{ tagName: "사무용", isAdmin: true, voteCount: 170 }, { tagName: "무접점", isAdmin: false, voteCount: 140 }, { tagName: "무선", isAdmin: false, voteCount: 100 }],
    avgScore: 4.5,
    reviewCount: 220,
    keyboardDescription: "정전용량 무접점 스위치를 사용하여 부드럽고 정숙한 타건감을 제공하는 유무선 키보드입니다. 블루투스 5.0 및 2.4GHz 무선 연결을 지원합니다.",
    keyboardSwitchType: "무접점 (정전용량)",
    keyboardConnectType: "유무선"
  }
];
// --- 목업 데이터 정의 끝 ---

// 키보드 데이터 로드 함수
async function loadKeyboardList(filterKeyword = null) {
  try {
    // 로딩 상태 표시
    const keyboardGrid = document.querySelector('.keyboard-grid');
    if (!keyboardGrid) return;
    
    keyboardGrid.innerHTML = '<div class="loading-indicator"><div class="spinner"></div><p>키보드 정보를 불러오는 중입니다...</p></div>';
    
    // API에서 키보드 목록 가져오기 (.do 접미사 사용)
    // const response = await KeyboardService.getKeyboards({
    //   page: 1,
    //   size: 20,
    //   sort: 'popular'
    // });
    
    // if (!response || !response.keyboardList) {
    //   console.error('키보드 목록을 불러오는데 실패했습니다.');
    //   keyboardGrid.innerHTML = '<div class="error-message">키보드 정보를 불러오는데 실패했습니다. 새로고침을 시도해주세요.</div>';
    //   return;
    // }
    
    // const keyboardList = response.keyboardList;

    let keyboardList = mockKeyboardData;

    if (filterKeyword) {
      const lowerCaseKeyword = filterKeyword.toLowerCase();
      keyboardList = mockKeyboardData.filter(keyboard => {
        return (
          keyboard.keyboardName.toLowerCase().includes(lowerCaseKeyword) ||
          (keyboard.keyboardDescription && keyboard.keyboardDescription.toLowerCase().includes(lowerCaseKeyword)) ||
          (keyboard.tags && keyboard.tags.some(tag => tag.tagName.toLowerCase().includes(lowerCaseKeyword)))
        );
      });
    }
    
    // 기존 키보드 카드 삭제
    keyboardGrid.innerHTML = '';
    
    if (keyboardList.length === 0) {
      keyboardGrid.innerHTML = '<div class="empty-state">표시할 키보드 정보가 없습니다.</div>';
      return;
    }
    
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
    setupFilterButtons(); // 필터 버튼 이벤트도 여기서 설정하거나, DOMContentLoaded에서 한 번만 호출하도록 수정
    
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
    // if (!keyword) return; // 키워드가 없으면 전체 목록을 보여주도록 변경 가능

    loadKeyboardList(keyword); // 목업 데이터 기반 검색으로 변경
    
    // 검색 결과 메시지 (선택적)
    // const keyboardGrid = document.querySelector('.keyboard-grid');
    // const numResults = keyboardGrid.querySelectorAll('.keyboard-card').length;
    // if (keyword && numResults > 0) {
    //   alert(`'${keyword}' 검색 결과: ${numResults}개의 키보드를 찾았습니다.`);
    // } else if (keyword && numResults === 0) {
    //   // loadKeyboardList에서 이미 처리됨
    // }
    
  });
}

// 페이지 초기화
document.addEventListener('DOMContentLoaded', () => {
  loadKeyboardList(); // 초기 로드 시 필터 버튼 이벤트도 내부에서 설정됨
  // setupFilterButtons(); // loadKeyboardList 내부에서 호출되므로 중복 제거 가능
  setupSearch();
});
