// URL에서 키보드 ID 가져오기
const urlParams = new URLSearchParams(window.location.search);
const keyboardId = urlParams.get('id');

// 키보드 ID에 따라 데이터 로드 (실제로는 서버에서 데이터를 가져올 것)
function loadKeyboardData() {
  // 여기서는 간단한 예시로 ID에 따라 다른 데이터 표시
  if (keyboardId === '1') {
    // 이미 기본 데이터가 설정되어 있음 (스틸시리즈 에이펙스 프로)
  } else if (keyboardId === '2') {
    document.getElementById('keyboardName').textContent = '로지텍 MX Keys';
    document.getElementById('keyboardPrice').textContent = '129,000원';
    document.querySelector('.stars').textContent = '★★★★★';
    document.querySelector('.rate-count').textContent = '(4.9/5, 리뷰 329개)';
    // 이미지도 변경 (실제 구현 시 이미지 경로 설정)
    document.getElementById('mainImage').src = '../img/keyboard2.jpg';
    document.getElementById('mainImage').onerror = function() {
      this.src = 'https://via.placeholder.com/500x300?text=로지텍+MX+Keys';
    };
  } else if (keyboardId === '3') {
    document.getElementById('keyboardName').textContent = 'GMMK Pro';
    document.getElementById('keyboardPrice').textContent = '249,000원';
    document.querySelector('.stars').textContent = '★★★★☆';
    document.querySelector('.rate-count').textContent = '(4.5/5, 리뷰 275개)';
    document.getElementById('mainImage').src = '../img/keyboard3.jpg';
    document.getElementById('mainImage').onerror = function() {
      this.src = 'https://via.placeholder.com/500x300?text=GMMK+Pro';
    };
  } else if (keyboardId === '4') {
    document.getElementById('keyboardName').textContent = '레이저 블랙위도우 V3 Pro';
    document.getElementById('keyboardPrice').textContent = '279,000원';
    document.querySelector('.stars').textContent = '★★★★☆';
    document.querySelector('.rate-count').textContent = '(4.6/5, 리뷰 192개)';
    document.getElementById('mainImage').src = '../img/keyboard4.jpg';
    document.getElementById('mainImage').onerror = function() {
      this.src = 'https://via.placeholder.com/500x300?text=레이저+블랙위도우';
    };
  } else if (keyboardId === '5') {
    document.getElementById('keyboardName').textContent = '레오폴드 FC900R';
    document.getElementById('keyboardPrice').textContent = '149,000원';
    document.querySelector('.stars').textContent = '★★★★★';
    document.querySelector('.rate-count').textContent = '(4.8/5, 리뷰 412개)';
    document.getElementById('mainImage').src = '../img/keyboard5.jpg';
    document.getElementById('mainImage').onerror = function() {
      this.src = 'https://via.placeholder.com/500x300?text=레오폴드+FC900R';
    };
  } else if (keyboardId === '6') {
    document.getElementById('keyboardName').textContent = 'ToFu60 DIY 키트';
    document.getElementById('keyboardPrice').textContent = '189,000원';
    document.querySelector('.stars').textContent = '★★★★☆';
    document.querySelector('.rate-count').textContent = '(4.4/5, 리뷰 157개)';
    document.getElementById('mainImage').src = '../img/keyboard6.jpg';
    document.getElementById('mainImage').onerror = function() {
      this.src = 'https://via.placeholder.com/500x300?text=ToFu60+DIY';
    };
  }
  
  // 현재 키보드 이름을 태그 모달에 설정
  document.getElementById('currentKeyboard').textContent = document.getElementById('keyboardName').textContent;
}

// 이미지 갤러리 기능
function changeImage(element, imageUrl) {
  document.getElementById('mainImage').src = imageUrl;
  document.querySelectorAll('.gallery-image').forEach(img => {
    img.classList.remove('active');
  });
  element.classList.add('active');
}

// 탭 전환 기능
function initTabs() {
  document.querySelectorAll('.tab-btn').forEach(button => {
    button.addEventListener('click', () => {
      // 모든 탭 버튼에서 active 클래스 제거
      document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
      });
      // 클릭한 탭 버튼에 active 클래스 추가
      button.classList.add('active');
      
      // 모든 탭 콘텐츠에서 active 클래스 제거
      document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.remove('active');
      });
      
      // 선택한 탭의 콘텐츠에 active 클래스 추가
      const tabId = button.getAttribute('data-tab');
      document.getElementById('tab-' + tabId).classList.add('active');
    });
  });
}

// 태그 모달 관련 함수
function openTagModal(keyboardName) {
  document.getElementById('currentKeyboard').textContent = keyboardName;
  document.getElementById('tagModal').classList.add('active');
}

function closeTagModal() {
  document.getElementById('tagModal').classList.remove('active');
}

// 추천 태그 추가 기능
function addRecommendedTag(tagName) {
  const tagsContainer = document.querySelector('.keyboard-tags');
  const tagExists = Array.from(tagsContainer.querySelectorAll('.keyboard-tag')).some(
    tag => tag.textContent.trim().replace(/\d+/g, '').includes(tagName)
  );
  
  if (tagExists) {
    alert('이미 추가된 태그입니다.');
    return;
  }
  
  // 새로운 태그 요소 생성
  const newTag = document.createElement('span');
  newTag.className = 'keyboard-tag tag-user';
  newTag.setAttribute('data-tag', tagName);
  newTag.innerHTML = tagName + '<span class="tag-count">1</span>';
  
  // 태그 추가 버튼 앞에 삽입
  const addButton = tagsContainer.querySelector('.keyboard-tag-add');
  tagsContainer.insertBefore(newTag, addButton);
  
  // 알림 표시 후 모달 닫기
  alert(`'${tagName}' 태그가 추가되었습니다.`);
  closeTagModal();
}

// 태그 추천/비추천 기능
function voteTag(tagName, voteType) {
  // 해당 태그의 투표 카운트 요소 찾기
  const tagId = 'vote-' + tagName.replace(/[^a-zA-Z0-9]/g, '').toLowerCase();
  const voteCountElement = document.getElementById(tagId);
  
  if (!voteCountElement) {
    console.error('투표 카운트 요소를 찾을 수 없습니다:', tagId);
    return;
  }
  
  // 현재 투표 수 가져오기
  let voteCount = parseInt(voteCountElement.textContent);
  
  // 이미 투표했는지 체크 (실제 구현에서는 서버에 저장)
  const storageKey = `voted-${tagName}`;
  const alreadyVoted = localStorage.getItem(storageKey);
  
  if (alreadyVoted === voteType) {
    alert('이미 이 태그에 투표하셨습니다.');
    return;
  }
  
  // 이전에 반대로 투표한 경우, 이전 투표 취소하고 새로운 투표 반영
  if (alreadyVoted) {
    if (alreadyVoted === 'up' && voteType === 'down') {
      voteCount -= 2; // 추천에서 비추천으로 변경
    } else if (alreadyVoted === 'down' && voteType === 'up') {
      voteCount += 2; // 비추천에서 추천으로 변경
    }
  } else {
    // 첫 투표인 경우
    if (voteType === 'up') {
      voteCount += 1;
    } else {
      voteCount -= 1;
    }
  }
  
  // UI 업데이트
  voteCountElement.textContent = voteCount;
  
  // 키보드 상세 영역의 태그에도 업데이트
  updateMainTagCount(tagName, voteCount);
  
  // 태그 투표 상태 저장 (실제 구현에서는 서버로 전송)
  localStorage.setItem(storageKey, voteType);
  
  // 투표 버튼 스타일 업데이트
  updateVoteButtonStyles(tagName, voteType);
  
  alert(`'${tagName}' 태그에 ${voteType === 'up' ? '추천' : '비추천'}하셨습니다.`);
}

// 메인 태그 영역의 태그 카운트 업데이트
function updateMainTagCount(tagName, count) {
  const mainTags = document.querySelectorAll('.keyboard-tags .keyboard-tag');
  for (const tag of mainTags) {
    if (tag.getAttribute('data-tag') === tagName) {
      let countElement = tag.querySelector('.tag-count');
      if (!countElement) {
        // 관리자 태그에는 카운트가 없을 수 있음 - 추가
        countElement = document.createElement('span');
        countElement.className = 'tag-count';
        tag.appendChild(countElement);
        
        // 관리자 태그를 사용자 태그로 변경
        tag.classList.remove('tag-admin');
        tag.classList.add('tag-user');
      }
      countElement.textContent = Math.max(0, count); // 음수 방지
      break;
    }
  }
}

// 투표 버튼 스타일 업데이트
function updateVoteButtonStyles(tagName, voteType) {
  const tagItems = document.querySelectorAll('.tag-list-item');
  
  for (const item of tagItems) {
    const tagNameElement = item.querySelector('.tag-list-name');
    if (tagNameElement && tagNameElement.textContent.trim() === tagName) {
      const upvoteBtn = item.querySelector('.upvote');
      const downvoteBtn = item.querySelector('.downvote');
      
      // 모든 투표 버튼 초기화
      upvoteBtn.classList.remove('active');
      downvoteBtn.classList.remove('active');
      
      // 현재 투표에 따라 스타일 적용
      if (voteType === 'up') {
        upvoteBtn.classList.add('active');
      } else {
        downvoteBtn.classList.add('active');
      }
    }
  }
}

// 태그 클릭 이벤트 설정
function initTagClickEvents() {
  document.addEventListener('click', function(e) {
    const target = e.target;
    if (target.classList.contains('keyboard-tag')) {
      const tagName = target.getAttribute('data-tag');
      if (tagName) {
        alert(`'${tagName}' 태그로 관련 키보드를 검색합니다.`);
        // 여기에 태그 검색 기능을 구현할 수 있습니다.
      }
    }
  });
}

// 태그 폼 제출 핸들러 설정
function initTagForm() {
  const tagForm = document.getElementById('tagForm');
  if (tagForm) {
    tagForm.addEventListener('submit', function(e) {
      e.preventDefault();
      
      const tagName = document.getElementById('tagName').value;
      const tagReason = document.getElementById('tagReason').value;
      
      if (tagName && tagReason) {
        alert("'" + tagName + "' 태그 신청이 접수되었습니다. \n관리자 검토 후 추가될 예정입니다.");
        this.reset();
        closeTagModal();
      }
    });
  }
}

// 페이지 초기화 함수
function initKeyboardDetail() {
  loadKeyboardData();
  initTabs();
  initTagClickEvents();
  initTagForm();
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', initKeyboardDetail);
