/**
 * 키보드 리뷰 관련 스크립트
 */
document.addEventListener('DOMContentLoaded', () => {
  // 리뷰 기능 초기화
  initializeReviewFeatures();
});

/**
 * 리뷰 기능 초기화
 */
function initializeReviewFeatures() {
  // 별점 선택 이벤트
  setupRatingInput();
  
  // 리뷰 필터 기능
  setupReviewFilters();
  
  // 리뷰 정렬 기능
  setupReviewSort();
  
  // 리뷰 폼 문자 수 계산
  setupCharacterCount();
  
  // 리뷰 태그 선택 기능
  setupTagSelection();
  
  // 이미지 업로드 미리보기
  setupImageUpload();
  
  // 리뷰 도움됨/신고 버튼
  setupReviewActions();
  
  // 리뷰 폼 제출
  setupReviewForm();
  
  // 페이지네이션 이벤트
  setupPagination();
  
  // 애니메이션 효과
  setupReviewAnimations();
}

/**
 * 별점 선택 기능 설정
 */
function setupRatingInput() {
  const ratingInputs = document.querySelectorAll('.rating-stars input');
  const ratingText = document.querySelector('.rating-selected-text');
  
  if (!ratingInputs.length || !ratingText) return;
  
  const ratingLabels = {
    5: '탁월함 (5점)',
    4: '매우 좋음 (4점)',
    3: '좋음 (3점)',
    2: '평범함 (2점)',
    1: '실망스러움 (1점)'
  };
  
  ratingInputs.forEach(input => {
    input.addEventListener('change', () => {
      const value = input.value;
      ratingText.textContent = ratingLabels[value] || '평점을 선택하세요';
      
      // 별점 선택 시 효과
      ratingText.style.opacity = 0;
      setTimeout(() => {
        ratingText.style.opacity = 1;
      }, 100);
      
      // 제출 버튼 활성화
      document.querySelector('.review-submit-btn').disabled = false;
    });
  });
}

/**
 * 리뷰 필터 기능 설정
 */
function setupReviewFilters() {
  const filterButtons = document.querySelectorAll('.review-filter-button');
  const reviewItems = document.querySelectorAll('.review-item');
  
  if (!filterButtons.length || !reviewItems.length) return;
  
  filterButtons.forEach(button => {
    button.addEventListener('click', () => {
      // 활성화된 버튼 변경
      document.querySelector('.review-filter-button.active').classList.remove('active');
      button.classList.add('active');
      
      const filter = button.getAttribute('data-filter');
      
      // 리뷰 필터링
      reviewItems.forEach(item => {
        const rating = parseInt(item.getAttribute('data-rating'));
        
        if (filter === 'all' ||
            (filter === 'positive' && rating >= 4) ||
            (filter === 'critical' && rating <= 3)) {
          // 리뷰 표시 애니메이션
          item.style.display = 'block';
          setTimeout(() => {
            item.style.opacity = 1;
            item.style.transform = 'translateY(0)';
          }, 50);
        } else {
          // 리뷰 숨김 애니메이션
          item.style.opacity = 0;
          item.style.transform = 'translateY(10px)';
          setTimeout(() => {
            item.style.display = 'none';
          }, 300);
        }
      });
    });
  });
}

/**
 * 리뷰 정렬 기능 설정
 */
function setupReviewSort() {
  const sortSelect = document.getElementById('reviewSort');
  const reviewsContainer = document.querySelector('.reviews-container');
  const reviewItems = Array.from(document.querySelectorAll('.review-item'));
  
  if (!sortSelect || !reviewsContainer || !reviewItems.length) return;
  
  sortSelect.addEventListener('change', () => {
    const sortBy = sortSelect.value;
    
    // 현재 표시된 리뷰들에 페이드 아웃 애니메이션
    reviewItems.forEach(item => {
      item.style.opacity = 0;
      item.style.transform = 'translateY(10px)';
    });
    
    // 정렬 후 새 순서로 요소 배치
    setTimeout(() => {
      const sortedItems = sortReviews(reviewItems, sortBy);
      
      // DOM에서 리뷰 제거
      reviewItems.forEach(item => {
        reviewsContainer.removeChild(item);
      });
      
      // 정렬된 리뷰 추가
      sortedItems.forEach(item => {
        reviewsContainer.appendChild(item);
      });
      
      // 페이드 인 애니메이션
      setTimeout(() => {
        sortedItems.forEach((item, index) => {
          setTimeout(() => {
            item.style.opacity = 1;
            item.style.transform = 'translateY(0)';
          }, index * 50);
        });
      }, 50);
    }, 300);
  });
}

/**
 * 리뷰 정렬 함수
 */
function sortReviews(reviews, sortBy) {
  return [...reviews].sort((a, b) => {
    if (sortBy === 'recent') {
      const dateA = new Date(a.querySelector('.review-date').textContent.replace(/\./g, '-'));
      const dateB = new Date(b.querySelector('.review-date').textContent.replace(/\./g, '-'));
      return dateB - dateA;
    } 
    else if (sortBy === 'helpful') {
      const helpfulA = parseInt(a.querySelector('.helpful-count').textContent);
      const helpfulB = parseInt(b.querySelector('.helpful-count').textContent);
      return helpfulB - helpfulA;
    }
    else if (sortBy === 'highRating') {
      const ratingA = parseInt(a.getAttribute('data-rating'));
      const ratingB = parseInt(b.getAttribute('data-rating'));
      return ratingB - ratingA;
    }
    else if (sortBy === 'lowRating') {
      const ratingA = parseInt(a.getAttribute('data-rating'));
      const ratingB = parseInt(b.getAttribute('data-rating'));
      return ratingA - ratingB;
    }
    
    return 0;
  });
}

/**
 * 문자 수 계산 기능 설정
 */
function setupCharacterCount() {
  const textInputs = document.querySelectorAll('input[type="text"], textarea');
  
  textInputs.forEach(input => {
    const maxLength = input.getAttribute('maxlength');
    if (!maxLength) return;
    
    const counter = input.nextElementSibling?.querySelector('span');
    if (!counter) return;
    
    input.addEventListener('input', () => {
      counter.textContent = input.value.length;
      
      // 90% 이상 작성 시 색상 변경
      if (input.value.length > maxLength * 0.9) {
        counter.style.color = '#f44336';
      } else {
        counter.style.color = '';
      }
    });
  });
}

/**
 * 태그 선택 기능 설정
 */
function setupTagSelection() {
  const tagOptions = document.querySelectorAll('.review-tag-option');
  const selectedTagsContainer = document.getElementById('selectedTags');
  
  if (!tagOptions.length || !selectedTagsContainer) return;
  
  const selectedTags = new Set();
  
  tagOptions.forEach(tag => {
    tag.addEventListener('click', () => {
      const tagText = tag.getAttribute('data-tag');
      
      // 최대 5개 태그만 선택 가능
      if (selectedTags.has(tagText)) {
        // 선택 해제
        selectedTags.delete(tagText);
        tag.classList.remove('selected');
        
        // 선택된 태그 목록에서 제거
        const tagElement = selectedTagsContainer.querySelector(`[data-tag="${tagText}"]`);
        if (tagElement) {
          tagElement.classList.add('removing');
          setTimeout(() => {
            selectedTagsContainer.removeChild(tagElement);
          }, 300);
        }
      } 
      else if (selectedTags.size < 5) {
        // 태그 선택
        selectedTags.add(tagText);
        tag.classList.add('selected');
        
        // 선택된 태그 목록에 추가
        const tagElement = document.createElement('span');
        tagElement.className = 'selected-tag';
        tagElement.setAttribute('data-tag', tagText);
        tagElement.innerHTML = `${tagText} <span class="remove-tag">&times;</span>`;
        
        // 제거 버튼 이벤트
        tagElement.querySelector('.remove-tag').addEventListener('click', () => {
          selectedTags.delete(tagText);
          tag.classList.remove('selected');
          tagElement.classList.add('removing');
          
          setTimeout(() => {
            selectedTagsContainer.removeChild(tagElement);
          }, 300);
        });
        
        selectedTagsContainer.appendChild(tagElement);
        
        // 추가 애니메이션
        tagElement.style.opacity = 0;
        tagElement.style.transform = 'scale(0.8)';
        
        setTimeout(() => {
          tagElement.style.opacity = 1;
          tagElement.style.transform = 'scale(1)';
        }, 10);
      } 
      else {
        // 최대 개수 초과 알림
        alert('태그는 최대 5개까지 선택할 수 있습니다.');
      }
    });
  });
}

/**
 * 이미지 업로드 미리보기 설정
 */
function setupImageUpload() {
  const imageInput = document.getElementById('reviewImages');
  const previewContainer = document.querySelector('.image-preview-container');
  
  if (!imageInput || !previewContainer) return;
  
  const maxImages = 3;
  const uploadedFiles = [];
  
  imageInput.addEventListener('change', () => {
    // 기존 미리보기 제거
    previewContainer.innerHTML = '';
    
    // 파일 제한 (3개)
    const selectedFiles = Array.from(imageInput.files).slice(0, maxImages);
    
    if (selectedFiles.length > maxImages) {
      alert(`이미지는 최대 ${maxImages}장까지 첨부할 수 있습니다.`);
    }
    
    // 미리보기 생성
    selectedFiles.forEach((file, index) => {
      // 파일 크기 체크 (5MB)
      if (file.size > 5 * 1024 * 1024) {
        alert(`${file.name} 파일이 5MB를 초과합니다.`);
        return;
      }
      
      // 파일 타입 체크
      if (!file.type.startsWith('image/')) {
        alert(`${file.name}은(는) 이미지 파일이 아닙니다.`);
        return;
      }
      
      uploadedFiles.push(file);
      
      const reader = new FileReader();
      
      reader.onload = function(e) {
        const previewDiv = document.createElement('div');
        previewDiv.className = 'image-preview';
        previewDiv.innerHTML = `
          <img src="${e.target.result}" alt="미리보기">
          <div class="remove-image" data-index="${index}">&times;</div>
        `;
        
        previewContainer.appendChild(previewDiv);
        
        // 삭제 버튼 이벤트
        previewDiv.querySelector('.remove-image').addEventListener('click', function() {
          const removeIndex = this.getAttribute('data-index');
          uploadedFiles.splice(removeIndex, 1);
          
          // 애니메이션 후 제거
          previewDiv.style.transform = 'scale(0.8)';
          previewDiv.style.opacity = 0;
          
          setTimeout(() => {
            previewContainer.removeChild(previewDiv);
          }, 300);
        });
        
        // 추가 애니메이션
        previewDiv.style.opacity = 0;
        previewDiv.style.transform = 'scale(0.8)';
        
        setTimeout(() => {
          previewDiv.style.opacity = 1;
          previewDiv.style.transform = 'scale(1)';
        }, 10 + index * 100);
      };
      
      reader.readAsDataURL(file);
    });
  });
}

/**
 * 리뷰 추천/신고 기능 설정
 */
function setupReviewActions() {
  // 도움됨 버튼 이벤트
  const helpfulButtons = document.querySelectorAll('.helpful-btn');
  
  helpfulButtons.forEach(button => {
    button.addEventListener('click', function() {
      const reviewId = this.getAttribute('data-id');
      const countElement = this.querySelector('.helpful-count');
      const count = parseInt(countElement.textContent);
      
      if (this.classList.contains('active')) {
        // 추천 취소
        this.classList.remove('active');
        countElement.textContent = count - 1;
        
        // 효과
        countElement.style.transform = 'scale(0.8)';
        setTimeout(() => {
          countElement.style.transform = 'scale(1)';
        }, 200);
        
        // localStorage에서 제거
        const helpfulReviews = JSON.parse(localStorage.getItem('helpfulReviews') || '[]');
        const newHelpfulReviews = helpfulReviews.filter(id => id !== reviewId);
        localStorage.setItem('helpfulReviews', JSON.stringify(newHelpfulReviews));
      } else {
        // 추천
        this.classList.add('active');
        countElement.textContent = count + 1;
        
        // 효과
        countElement.style.transform = 'scale(1.2)';
        setTimeout(() => {
          countElement.style.transform = 'scale(1)';
        }, 200);
        
        // localStorage에 저장
        const helpfulReviews = JSON.parse(localStorage.getItem('helpfulReviews') || '[]');
        helpfulReviews.push(reviewId);
        localStorage.setItem('helpfulReviews', JSON.stringify(helpfulReviews));
      }
      
      // 서버에 추천 정보 전송 (API 요청)
      // updateReviewHelpful(reviewId, this.classList.contains('active'));
      
      // 로그인 여부 확인 (현재는 모든 사용자 허용)
      // if (!isLoggedIn()) {
      //   alert('로그인이 필요한 기능입니다.');
      //   return;
      // }
    });
  });
  
  // 신고 버튼 이벤트
  const reportButtons = document.querySelectorAll('.report-btn');
  
  reportButtons.forEach(button => {
    button.addEventListener('click', function() {
      const reviewId = this.getAttribute('data-id');
      
      // 신고 모달 열기 (모달 구현 필요)
      alert(`리뷰 신고 기능은 현재 개발 중입니다. (리뷰 ID: ${reviewId})`);
      
      // 로그인 여부 확인 (현재는 모든 사용자 허용)
      // if (!isLoggedIn()) {
      //   alert('로그인이 필요한 기능입니다.');
      //   return;
      // }
    });
  });
}

/**
 * 리뷰 폼 제출 설정
 */
function setupReviewForm() {
  const reviewForm = document.getElementById('reviewForm');
  
  if (!reviewForm) return;
  
  // 폼 제출 이벤트
  reviewForm.addEventListener('submit', function(e) {
    e.preventDefault();
    
    // 별점 필수 체크
    const rating = document.querySelector('input[name="rating"]:checked');
    if (!rating) {
      alert('별점을 선택해주세요.');
      return;
    }
    
    // 제목 필수 체크
    const title = document.getElementById('reviewTitle').value.trim();
    if (!title) {
      alert('리뷰 제목을 입력해주세요.');
      document.getElementById('reviewTitle').focus();
      return;
    }
    
    // 내용 필수 체크
    const content = document.getElementById('reviewContent').value.trim();
    if (!content) {
      alert('리뷰 내용을 입력해주세요.');
      document.getElementById('reviewContent').focus();
      return;
    }
    
    // 태그 수집
    const selectedTags = Array.from(document.querySelectorAll('.selected-tag')).map(tag => 
      tag.getAttribute('data-tag')
    );
    
    // 이미지 수집
    const uploadedImages = document.querySelectorAll('.image-preview img');
    const images = Array.from(uploadedImages).map(img => img.src);
    
    // 리뷰 데이터 구성
    const reviewData = {
      rating: rating.value,
      title,
      content,
      tags: selectedTags,
      images
    };
    
    console.log('리뷰 제출 데이터:', reviewData);
    
    // API 호출 (리뷰 등록)
    // submitReview(reviewData)
    //   .then(response => {
    //     alert('리뷰가 등록되었습니다.');
    //     window.location.reload();
    //   })
    //   .catch(error => {
    //     alert('리뷰 등록에 실패했습니다.');
    //     console.error(error);
    //   });
    
    // 현재는 임시로 알림만 표시
    alert('리뷰 등록 기능은 현재 개발 중입니다.');
    
    // 폼 초기화 (개발 중이므로 초기화하지 않음)
    // reviewForm.reset();
  });
  
  // 취소 버튼
  const cancelButton = document.querySelector('.review-cancel-btn');
  if (cancelButton) {
    cancelButton.addEventListener('click', () => {
      if (confirm('작성 중인 리뷰를 취소하시겠습니까?')) {
        reviewForm.reset();
        
        // 별점 초기화
        document.querySelector('.rating-selected-text').textContent = '평점을 선택하세요';
        
        // 이미지 미리보기 초기화
        document.querySelector('.image-preview-container').innerHTML = '';
        
        // 태그 초기화
        document.getElementById('selectedTags').innerHTML = '';
        document.querySelectorAll('.review-tag-option.selected').forEach(tag => {
          tag.classList.remove('selected');
        });
        
        // 스크롤
        window.scrollTo({
          top: document.querySelector('.review-filter').offsetTop - 100,
          behavior: 'smooth'
        });
      }
    });
  }
}

/**
 * 페이지네이션 설정
 */
function setupPagination() {
  const pageButtons = document.querySelectorAll('.page-btn');
  
  if (!pageButtons.length) return;
  
  pageButtons.forEach(button => {
    button.addEventListener('click', function() {
      if (this.classList.contains('active')) return;
      
      // 활성화된 버튼 변경
      document.querySelector('.page-btn.active')?.classList.remove('active');
      this.classList.add('active');
      
      // 현재는 실제 페이지네이션이 아닌 시각적 효과만 적용
      const reviewsContainer = document.querySelector('.reviews-container');
      
      // 페이드 아웃
      reviewsContainer.style.opacity = 0;
      reviewsContainer.style.transform = 'translateY(20px)';
      
      // 페이지 전환 효과 후 다시 원래 리뷰 표시
      setTimeout(() => {
        reviewsContainer.style.opacity = 1;
        reviewsContainer.style.transform = 'translateY(0)';
      }, 300);
      
      // 페이지 상단으로 스크롤
      window.scrollTo({
        top: document.querySelector('.review-stats').offsetTop - 20,
        behavior: 'smooth'
      });
      
      // API 호출 (페이지에 맞는 리뷰 목록 가져오기)
      // const page = this.textContent;
      // loadReviews(page)
    });
  });
}

/**
 * 리뷰 애니메이션 설정
 */
function setupReviewAnimations() {
  // 리뷰 아이템들에 순차적으로 애니메이션 적용
  const reviewItems = document.querySelectorAll('.review-item');
  
  reviewItems.forEach((item, index) => {
    item.style.opacity = 0;
    item.style.transform = 'translateY(20px)';
    
    setTimeout(() => {
      item.style.opacity = 1;
      item.style.transform = 'translateY(0)';
    }, 100 + index * 100);
  });
  
  // 리뷰 이미지 클릭 시 확대
  setupImageLightbox();
}

/**
 * 이미지 라이트박스 설정
 */
function setupImageLightbox() {
  const reviewImages = document.querySelectorAll('.review-image');
  
  reviewImages.forEach(image => {
    image.addEventListener('click', function() {
      // 라이트박스 생성
      const lightbox = document.createElement('div');
      lightbox.className = 'review-lightbox';
      
      const lightboxContent = document.createElement('div');
      lightboxContent.className = 'review-lightbox-content';
      
      const img = document.createElement('img');
      img.src = this.src;
      
      const closeBtn = document.createElement('div');
      closeBtn.className = 'review-lightbox-close';
      closeBtn.innerHTML = '&times;';
      
      lightboxContent.appendChild(img);
      lightboxContent.appendChild(closeBtn);
      lightbox.appendChild(lightboxContent);
      
      document.body.appendChild(lightbox);
      
      // 애니메이션
      setTimeout(() => {
        lightbox.style.opacity = 1;
        lightboxContent.style.transform = 'translateY(0)';
      }, 10);
      
      // 닫기 이벤트
      closeBtn.addEventListener('click', closeLightbox);
      lightbox.addEventListener('click', function(e) {
        if (e.target === this) {
          closeLightbox();
        }
      });
      
      function closeLightbox() {
        lightbox.style.opacity = 0;
        lightboxContent.style.transform = 'translateY(20px)';
        
        setTimeout(() => {
          document.body.removeChild(lightbox);
        }, 300);
      }
    });
  });
}
