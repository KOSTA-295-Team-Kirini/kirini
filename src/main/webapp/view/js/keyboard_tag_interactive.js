/**
 * 키보드 태그 관련 인터랙티브 기능 스크립트
 */
document.addEventListener('DOMContentLoaded', () => {
  // 태그 기능 초기화
  initializeTagFeatures();
});

/**
 * 태그 기능 초기화
 */
function initializeTagFeatures() {
  // 태그 투표 기능
  setupTagVoting();
  
  // 태그 모달 기능
  setupTagModal();
  
  // 태그 폼 제출
  setupTagForm();
  
  // 태그 애니메이션
  setupTagAnimations();
}

/**
 * 태그 투표 기능 설정
 */
function setupTagVoting() {
  const tagVoteButtons = document.querySelectorAll('.tag-vote-btn');
  
  if (!tagVoteButtons.length) return;
  
  tagVoteButtons.forEach(button => {
    button.addEventListener('click', function() {
      const tagName = this.closest('.tag-list-item').querySelector('.tag-list-name').textContent.trim();
      const voteType = this.classList.contains('upvote') ? 'up' : 'down';
      const voteCountElement = this.closest('.tag-actions').querySelector('.vote-count');
      const voteCount = parseInt(voteCountElement.textContent);
      
      // 이미 투표한 태그인지 확인
      const votedTags = JSON.parse(localStorage.getItem('votedTags') || '{}');
      const keyboardId = getKeyboardIdFromUrl();
      const tagKey = `${keyboardId}_${tagName}`;
      
      // 투표 취소 또는 변경
      if (votedTags[tagKey]) {
        if (votedTags[tagKey] === voteType) {
          // 같은 투표 취소
          delete votedTags[tagKey];
          
          // 투표 수 감소
          voteCountElement.textContent = voteCount - 1;
          button.classList.remove('voted');
        } else {
          // 다른 투표로 변경
          votedTags[tagKey] = voteType;
          
          // 반대 버튼 비활성화
          const oppositeButton = this.closest('.tag-actions').querySelector(
            voteType === 'up' ? '.downvote' : '.upvote'
          );
          oppositeButton.classList.remove('voted');
          
          // 현재 버튼 활성화
          button.classList.add('voted');
          
          // 반영될 투표 수 계산 (+2: 기존 -1, 새로 +1)
          voteCountElement.textContent = voteType === 'up' ? 
            voteCount + 2 : voteCount - 2;
        }
      } else {
        // 새 투표
        votedTags[tagKey] = voteType;
        
        // 투표 수 변경
        voteCountElement.textContent = voteType === 'up' ? 
          voteCount + 1 : voteCount - 1;
          
        // 버튼 활성화
        button.classList.add('voted');
      }
      
      // 로컬 스토리지 업데이트
      localStorage.setItem('votedTags', JSON.stringify(votedTags));
      
      // 투표 애니메이션
      voteCountElement.style.transform = 'scale(1.3)';
      voteCountElement.style.color = voteType === 'up' ? '#4caf50' : '#f44336';
      
      setTimeout(() => {
        voteCountElement.style.transform = 'scale(1)';
        voteCountElement.style.color = '';
      }, 300);
      
      // API 호출 (실제 서버 투표)
      // updateTagVote(keyboardId, tagName, voteType);
    });
  });
  
  // 이미 투표한 태그 표시
  loadVotedTags();
}

/**
 * 이미 투표한 태그 로드 및 표시
 */
function loadVotedTags() {
  const votedTags = JSON.parse(localStorage.getItem('votedTags') || '{}');
  const keyboardId = getKeyboardIdFromUrl();
  
  // 태그 목록 순회
  document.querySelectorAll('.tag-list-item').forEach(tagItem => {
    const tagName = tagItem.querySelector('.tag-list-name').textContent.trim();
    const tagKey = `${keyboardId}_${tagName}`;
    
    // 투표한 태그인 경우 버튼 활성화
    if (votedTags[tagKey]) {
      const voteType = votedTags[tagKey];
      const voteButton = tagItem.querySelector(
        voteType === 'up' ? '.upvote' : '.downvote'
      );
      
      if (voteButton) {
        voteButton.classList.add('voted');
      }
    }
  });
}

/**
 * URL에서 키보드 ID 가져오기
 */
function getKeyboardIdFromUrl() {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get('id') || 'unknown';
}

/**
 * 태그 모달 기능 설정
 */
function setupTagModal() {
  // 태그 설정 버튼 이벤트
  const tagAddButton = document.querySelector('.keyboard-tag-add');
  const tagModal = document.getElementById('tagModal');
  
  if (!tagAddButton || !tagModal) return;
  
  tagAddButton.addEventListener('click', function() {
    // 키보드 이름 설정
    const keyboardName = document.querySelector('.keyboard-title').textContent;
    document.getElementById('currentKeyboard').textContent = keyboardName;
    
    // 모달 표시
    tagModal.classList.add('active');
    document.body.classList.add('modal-open');
    
    // 모달 애니메이션
    setTimeout(() => {
      tagModal.querySelector('.tag-modal-content').style.transform = 'translateY(0)';
      tagModal.querySelector('.tag-modal-content').style.opacity = 1;
    }, 10);
  });
  
  // 닫기 버튼 이벤트
  const closeButton = tagModal.querySelector('.close-tag-modal');
  if (closeButton) {
    closeButton.addEventListener('click', closeTagModal);
  }
  
  // 모달 외부 클릭 시 닫기
  tagModal.addEventListener('click', function(e) {
    if (e.target === this) {
      closeTagModal();
    }
  });
  
  // ESC 키로 닫기
  document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape' && tagModal.classList.contains('active')) {
      closeTagModal();
    }
  });
}

/**
 * 태그 모달 닫기 함수
 */
function closeTagModal() {
  const tagModal = document.getElementById('tagModal');
  
  if (!tagModal) return;
  
  // 모달 애니메이션
  tagModal.querySelector('.tag-modal-content').style.transform = 'translateY(-20px)';
  tagModal.querySelector('.tag-modal-content').style.opacity = 0;
  
  setTimeout(() => {
    tagModal.classList.remove('active');
    document.body.classList.remove('modal-open');
    
    // 스타일 초기화
    setTimeout(() => {
      tagModal.querySelector('.tag-modal-content').style.transform = 'translateY(-20px)';
    }, 100);
  }, 300);
}

/**
 * 태그 폼 제출 설정
 */
function setupTagForm() {
  const tagForm = document.getElementById('tagForm');
  
  if (!tagForm) return;
  
  // 폼 제출 이벤트
  tagForm.addEventListener('submit', function(e) {
    e.preventDefault();
    
    const tagName = document.getElementById('tagName').value.trim();
    const tagReason = document.getElementById('tagReason').value.trim();
    
    if (!tagName) {
      alert('태그명을 입력해주세요.');
      document.getElementById('tagName').focus();
      return;
    }
    
    if (!tagReason) {
      alert('신청 사유를 입력해주세요.');
      document.getElementById('tagReason').focus();
      return;
    }
    
    // 키보드 ID 가져오기
    const keyboardId = getKeyboardIdFromUrl();
    
    // 태그 데이터 구성
    const tagData = {
      keyboardId,
      tagName,
      reason: tagReason
    };
    
    console.log('태그 신청 데이터:', tagData);
    
    // API 호출 (태그 신청)
    // submitNewTag(tagData)
    //   .then(response => {
    //     alert('태그가 신청되었습니다. 관리자 승인 후 표시됩니다.');
    //     closeTagModal();
    //   })
    //   .catch(error => {
    //     alert('태그 신청에 실패했습니다.');
    //     console.error(error);
    //   });
    
    // 현재는 임시로 알림만 표시
    alert('태그가 신청되었습니다. 관리자 승인 후 표시됩니다.');
    
    // 폼 초기화
    tagForm.reset();
    
    // 모달 닫기
    closeTagModal();
  });
}

/**
 * 태그 애니메이션 설정
 */
function setupTagAnimations() {
  // 메인 페이지의 태그 애니메이션
  const tags = document.querySelectorAll('.keyboard-tag');
  
  tags.forEach((tag, index) => {
    // 순차적으로 페이드인
    tag.style.opacity = 0;
    tag.style.transform = 'translateY(10px)';
    
    setTimeout(() => {
      tag.style.transition = 'all 0.3s ease';
      tag.style.opacity = 1;
      tag.style.transform = 'translateY(0)';
    }, 100 + index * 100);
    
    // 클릭 이벤트 (태그 필터링)
    tag.addEventListener('click', function() {
      const tagType = this.classList.contains('tag-admin') ? 'admin' : 'user';
      const tagText = this.getAttribute('data-tag');
      
      // 클릭 효과
      this.style.transform = 'scale(1.1)';
      setTimeout(() => {
        this.style.transform = 'scale(1)';
      }, 200);
      
      console.log(`태그 클릭: ${tagText} (${tagType})`);
      
      // 태그 검색 페이지로 이동 또는 API 호출
      // window.location.href = `/view/pages/keyboard_info.html?tag=${encodeURIComponent(tagText)}`;
    });
  });
}
