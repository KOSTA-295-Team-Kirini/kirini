// URL에서 키보드 ID 가져오기
const urlParams = new URLSearchParams(window.location.search);
const keyboardId = urlParams.get('id');

// 페이지 로드시 키보드 정보 가져오기
document.addEventListener('DOMContentLoaded', function () {
    if (keyboardId) {
        loadKeyboardData();
        loadKeyboardReviews();
    } else {
        alert('키보드 정보를 찾을 수 없습니다.');
        window.location.href = 'keyboard_info.html';
    }
});

// 키보드 상세 정보 로드 (API 사용)
async function loadKeyboardData() {
    try {
        // 로딩 상태 표시
        const contentArea = document.querySelector('.keyboard-detail-content');
        if (contentArea) {
            contentArea.innerHTML = '<div class="loading-indicator"><div class="spinner"></div><p>키보드 정보를 불러오는 중입니다...</p></div>';
        }

        // API를 통해 키보드 정보 가져오기 (.do 접미사 사용)
        const keyboard = await KeyboardService.getKeyboardDetails(keyboardId);

        // 상세 정보 화면에 표시
        document.getElementById('keyboardName').textContent = keyboard.keyboardName;
        document.getElementById('keyboardPrice').textContent = `${keyboard.keyboardPrice.toLocaleString()}원`;

        // 별점 표시
        const avgScore = keyboard.avgScore || 0;
        const fullStars = Math.floor(avgScore);
        const halfStar = avgScore - fullStars >= 0.5;

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

        document.querySelector('.stars').textContent = starsText;
        document.querySelector('.rate-count').textContent = `(${avgScore.toFixed(1)}/5, 리뷰 ${keyboard.reviewCount}개)`;

        // 이미지 설정
        if (keyboard.keyboardImageUrl) {
            document.getElementById('mainImage').src = keyboard.keyboardImageUrl;
        } else {
            document.getElementById('mainImage').src = '../img/keyboard_default.jpg';
        }
        document.getElementById('mainImage').onerror = function () {
            this.src = `https://via.placeholder.com/500x300?text=${encodeURIComponent(keyboard.keyboardName)}`;
        };

        // 이미지 갤러리 생성
        if (keyboard.images && keyboard.images.length > 0) {
            const galleryContainer = document.querySelector('.image-gallery');
            if (galleryContainer) {
                galleryContainer.innerHTML = '';

                keyboard.images.forEach(imageUrl => {
                    const imgElement = document.createElement('img');
                    imgElement.className = 'gallery-image';
                    imgElement.src = imageUrl;
                    imgElement.alt = keyboard.keyboardName;
                    imgElement.onclick = function () {
                        changeImage(this, imageUrl);
                    };

                    galleryContainer.appendChild(imgElement);
                });

                // 첫 번째 이미지 활성화
                if (galleryContainer.firstChild) {
                    galleryContainer.firstChild.classList.add('active');
                }
            }
        }

        // 키보드 설명 표시
        if (document.getElementById('keyboard-description')) {
            document.getElementById('keyboard-description').textContent = keyboard.keyboardDescription;
        }

        // 사양 정보 표시
        if (keyboard.specifications && document.getElementById('keyboard-specs')) {
            const specsContainer = document.getElementById('keyboard-specs');
            specsContainer.innerHTML = '';

            // 사양 테이블 생성
            const table = document.createElement('table');
            table.className = 'specs-table';

            Object.entries(keyboard.specifications).forEach(([key, value]) => {
                const row = document.createElement('tr');

                const keyCell = document.createElement('th');
                keyCell.textContent = key;

                const valueCell = document.createElement('td');
                valueCell.textContent = value;

                row.appendChild(keyCell);
                row.appendChild(valueCell);
                table.appendChild(row);
            });

            specsContainer.appendChild(table);
        }

        // 태그 표시
        if (keyboard.tags && keyboard.tags.length > 0) {
            const tagsContainer = document.querySelector('.keyboard-tags');

            // 기존 태그 삭제 (태그 추가 버튼 제외)
            const addButton = tagsContainer.querySelector('.keyboard-tag-add');
            while (tagsContainer.firstChild && tagsContainer.firstChild !== addButton) {
                tagsContainer.removeChild(tagsContainer.firstChild);
            }

            // 새 태그 추가
            keyboard.tags.forEach(tag => {
                const tagElement = document.createElement('span');
                tagElement.className = 'keyboard-tag ' + (tag.isAdmin ? 'tag-admin' : 'tag-user');
                tagElement.setAttribute('data-tag', tag.tagName);

                const tagText = document.createTextNode(tag.tagName);
                tagElement.appendChild(tagText);

                if (!tag.isAdmin) {
                    const tagCount = document.createElement('span');
                    tagCount.className = 'tag-count';
                    tagCount.textContent = tag.voteCount;
                    tagElement.appendChild(tagCount);
                }

                tagsContainer.insertBefore(tagElement, addButton);
            });
        }

        // 현재 키보드 이름을 태그 모달에 설정
        document.getElementById('currentKeyboard').textContent = keyboard.keyboardName;

        // 리뷰 불러오기
        loadReviews(keyboardId);

        // 관련 키보드 불러오기
        loadRelatedKeyboards(keyboardId);

        // 로딩 상태 제거
        if (contentArea) {
            contentArea.querySelector('.loading-indicator')?.remove();
        }

    } catch (error) {
        console.error('키보드 정보를 불러오는데 실패했습니다:', error);

        // 오류 메시지 표시
        const contentArea = document.querySelector('.keyboard-detail-content');
        if (contentArea) {
            contentArea.innerHTML = '<div class="error-message"><p>키보드 정보를 불러오는데 실패했습니다. <button id="retry-load">다시 시도</button></p></div>';

            // 다시 시도 버튼
            document.getElementById('retry-load')?.addEventListener('click', () => {
                loadKeyboardData();
            });
        }
    }
}

// 키보드 리뷰 불러오기
async function loadReviews(keyboardId) {
    try {
        const reviewsContainer = document.getElementById('keyboard-reviews');
        if (!reviewsContainer) return;

        reviewsContainer.innerHTML = '<div class="loading-indicator"><div class="spinner"></div><p>리뷰를 불러오는 중입니다...</p></div>';

        // API를 통해 리뷰 목록 가져오기 (.do 접미사 사용)
        const response = await ReviewService.getReviews(keyboardId, {
            page: 1,
            size: 10,
            sort: 'newest'
        });

        if (!response || !response.reviews) {
            throw new Error('리뷰를 불러오는데 실패했습니다.');
        }

        const reviews = response.reviews;

        // 리뷰 목록 표시
        reviewsContainer.innerHTML = '';

        if (reviews.length === 0) {
            reviewsContainer.innerHTML = `
        <div class="no-reviews">
          <p>아직 리뷰가 없습니다. 첫 리뷰를 작성해보세요!</p>
          <button class="btn write-review-btn">리뷰 작성하기</button>
        </div>
      `;

            reviewsContainer.querySelector('.write-review-btn')?.addEventListener('click', openReviewForm);
            return;
        }

        // 리뷰 컨테이너 생성
        const reviewsList = document.createElement('div');
        reviewsList.className = 'reviews-list';

        reviews.forEach(review => {
            const reviewElement = document.createElement('div');
            reviewElement.className = 'review-item';

            // 별점 생성
            let starsText = '';
            for (let i = 0; i < 5; i++) {
                starsText += i < review.score ? '★' : '☆';
            }

            reviewElement.innerHTML = `
        <div class="review-header">
          <div class="review-author">${review.authorName}</div>
          <div class="review-date">${new Date(review.createdAt).toLocaleDateString()}</div>
        </div>
        <div class="review-rating">
          <span class="stars">${starsText}</span>
          <span class="score">${review.score}/5</span>
        </div>
        <div class="review-content">${review.content}</div>
        <div class="review-footer">
          <button class="review-helpful" data-review-id="${review.id}">
            도움됨 <span class="helpful-count">${review.helpfulCount || 0}</span>
          </button>
        </div>
      `;

            reviewsList.appendChild(reviewElement);
        });

        reviewsContainer.appendChild(reviewsList);

        // 리뷰 하단 페이지네이션 및 작성 버튼
        const reviewsFooter = document.createElement('div');
        reviewsFooter.className = 'reviews-footer';
        reviewsFooter.innerHTML = `
      <button class="btn write-review-btn">리뷰 작성하기</button>
    `;

        reviewsContainer.appendChild(reviewsFooter);

        // 리뷰 작성 버튼 이벤트
        reviewsContainer.querySelector('.write-review-btn')?.addEventListener('click', openReviewForm);

        // 리뷰 도움됨 버튼 이벤트
        reviewsContainer.querySelectorAll('.review-helpful').forEach(button => {
            button.addEventListener('click', async function () {
                const reviewId = this.dataset.reviewId;
                try {
                    await ReviewService.rateReviewHelpfulness(reviewId, true);

                    const countElement = this.querySelector('.helpful-count');
                    if (countElement) {
                        const currentCount = parseInt(countElement.textContent);
                        countElement.textContent = (currentCount + 1).toString();
                    }

                    this.disabled = true;
                    this.classList.add('clicked');
                } catch (error) {
                    console.error('리뷰 평가 중 오류 발생:', error);
                }
            });
        });

    } catch (error) {
        console.error('리뷰를 불러오는 중 오류 발생:', error);

        const reviewsContainer = document.getElementById('keyboard-reviews');
        if (reviewsContainer) {
            reviewsContainer.innerHTML = '<div class="error-message"><p>리뷰를 불러오는데 실패했습니다.</p></div>';
        }
    }
}

// 키보드 리뷰 로드
async function loadKeyboardReviews() {
    try {
        const reviewsContainer = document.getElementById('reviews-container');
        if (!reviewsContainer) return;

        // 로딩 표시
        reviewsContainer.innerHTML = '<div class="loading-indicator"><div class="spinner"></div><p>리뷰를 불러오는 중입니다...</p></div>';

        // 리뷰 가져오기
        const reviews = await ReviewService.getReviews(keyboardId);

        if (!reviews || !reviews.data || reviews.data.length === 0) {
            reviewsContainer.innerHTML = '<div class="no-reviews">아직 리뷰가 없습니다. 첫 리뷰를 작성해보세요!</div>';
            return;
        }

        // 리뷰 목록 표시
        reviewsContainer.innerHTML = '';
        reviews.data.forEach(review => {
            const reviewElement = document.createElement('div');
            reviewElement.className = 'review-item';

            // 별점 텍스트 생성
            const score = review.score || 0;
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

            reviewElement.innerHTML = `
        <div class="review-header">
          <div class="reviewer-info">
            <span class="reviewer-name">${review.authorName || '익명'}</span>
            <span class="review-date">${review.createdAt || '-'}</span>
          </div>
          <div class="review-rating">
            <span class="stars">${starsText}</span>
            <span class="score">${score.toFixed(1)}/5</span>
          </div>
        </div>
        <div class="review-content">
          <p>${review.content || ''}</p>
        </div>
        <div class="review-footer">
          <div class="helpful-section">
            <span>이 리뷰가 도움이 되었나요?</span>
            <button class="helpful-btn" data-review-id="${review.id}" data-helpful="true">
              <i class="icon-thumbs-up"></i> 예 (${review.helpfulCount || 0})
            </button>
            <button class="helpful-btn" data-review-id="${review.id}" data-helpful="false">
              <i class="icon-thumbs-down"></i> 아니오 (${review.unhelpfulCount || 0})
            </button>
          </div>
        </div>
      `;

            reviewsContainer.appendChild(reviewElement);
        });

        // 도움됨/안됨 버튼 이벤트 추가
        setupHelpfulButtons();
    } catch (error) {
        console.error('리뷰 로드 오류:', error);
        const reviewsContainer = document.getElementById('reviews-container');
        if (reviewsContainer) {
            reviewsContainer.innerHTML = '<div class="error">리뷰를 불러오는 중 오류가 발생했습니다.</div>';
        }
    }
}

// 도움됨/안됨 버튼 이벤트 설정
function setupHelpfulButtons() {
    document.querySelectorAll('.helpful-btn').forEach(button => {
        button.addEventListener('click', async function () {
            const reviewId = this.getAttribute('data-review-id');
            const isHelpful = this.getAttribute('data-helpful') === 'true';

            if (!reviewId) return;

            try {
                await ReviewService.rateReviewHelpfulness(reviewId, isHelpful);

                // 버튼의 카운트 업데이트 (간소화 버전)
                const currentCount = parseInt(this.textContent.match(/\d+/) || '0');
                const newCount = currentCount + 1;

                if (isHelpful) {
                    this.innerHTML = `<i class="icon-thumbs-up"></i> 예 (${newCount})`;
                } else {
                    this.innerHTML = `<i class="icon-thumbs-down"></i> 아니오 (${newCount})`;
                }

                // 클릭 후 버튼 비활성화
                this.disabled = true;
                this.classList.add('voted');
            } catch (error) {
                console.error('리뷰 평가 오류:', error);
                alert('리뷰 평가 중 오류가 발생했습니다.');
            }
        });
    });
}

// 관련 키보드 불러오기
async function loadRelatedKeyboards(keyboardId) {
    try {
        const relatedContainer = document.getElementById('related-keyboards');
        if (!relatedContainer) return;

        relatedContainer.innerHTML = '<div class="loading-indicator"><div class="spinner"></div><p>관련 키보드를 불러오는 중입니다...</p></div>';

        // API를 통해 관련 키보드 가져오기 (.do 접미사 사용)
        const response = await KeyboardService.getRelatedKeyboards(keyboardId);

        if (!response || !response.keyboards) {
            throw new Error('관련 키보드를 불러오는데 실패했습니다.');
        }

        const relatedKeyboards = response.keyboards;

        // 관련 키보드 표시
        relatedContainer.innerHTML = '';

        if (relatedKeyboards.length === 0) {
            relatedContainer.innerHTML = '<p>관련 키보드가 없습니다.</p>';
            return;
        }

        // 관련 키보드 그리드 생성
        const relatedGrid = document.createElement('div');
        relatedGrid.className = 'related-keyboards-grid';

        relatedKeyboards.forEach(keyboard => {
            const keyboardElement = document.createElement('div');
            keyboardElement.className = 'related-keyboard-item';

            keyboardElement.innerHTML = `
        <a href="keyboard_detail.html?id=${keyboard.id}">
          <img src="${keyboard.imageUrl || '../img/keyboard_default.jpg'}" alt="${keyboard.name}" 
               onerror="this.src='https://via.placeholder.com/200x100?text=${encodeURIComponent(keyboard.name)}'">
          <div class="related-keyboard-info">
            <h4>${keyboard.name}</h4>
            <div class="price">${keyboard.price.toLocaleString()}원</div>
          </div>
        </a>
      `;

            relatedGrid.appendChild(keyboardElement);
        });

        relatedContainer.appendChild(relatedGrid);

    } catch (error) {
        console.error('관련 키보드를 불러오는 중 오류 발생:', error);

        const relatedContainer = document.getElementById('related-keyboards');
        if (relatedContainer) {
            relatedContainer.innerHTML = '<div class="error-message"><p>관련 키보드를 불러오는데 실패했습니다.</p></div>';
        }
    }
}

// 리뷰 작성 폼 열기
function openReviewForm() {
    // 리뷰 작성 모달 표시 로직
    const reviewModal = document.getElementById('review-modal');
    if (reviewModal) {
        reviewModal.style.display = 'block';
    }
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
async function addRecommendedTag(tagName) {
    const tagsContainer = document.querySelector('.keyboard-tags');
    const tagExists = Array.from(tagsContainer.querySelectorAll('.keyboard-tag')).some(
        tag => tag.textContent.trim().replace(/\d+/g, '').includes(tagName)
    );

    if (tagExists) {
        alert('이미 추가된 태그입니다.');
        return;
    }

    try {
        // API를 통해 태그 제안
        const response = await KeyboardService.suggestTag(keyboardId, tagName);

        if (response && response.success) {
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
        } else {
            alert(response?.message || '태그 추가 중 오류가 발생했습니다.');
        }
    } catch (error) {
        console.error('태그 추가 중 오류 발생:', error);
        alert('태그 추가 중 오류가 발생했습니다. 로그인이 필요할 수 있습니다.');
    }
}

// 태그 추천/비추천 기능
async function voteTag(tagName, voteType) {
    // 해당 태그의 투표 카운트 요소 찾기
    const tagId = 'vote-' + tagName.replace(/[^a-zA-Z0-9]/g, '').toLowerCase();
    const voteCountElement = document.getElementById(tagId);

    if (!voteCountElement) {
        console.error('투표 카운트 요소를 찾을 수 없습니다:', tagId);
        return;
    }

    try {
        // API를 통해 태그 투표
        const response = await KeyboardService.voteTag(keyboardId, tagId, voteType);

        if (response.success) {
            // 현재 투표 수 가져오기
            let voteCount = response.newCount || parseInt(voteCountElement.textContent);

            // UI 업데이트
            voteCountElement.textContent = voteCount;

            // 키보드 상세 영역의 태그에도 업데이트
            updateMainTagCount(tagName, voteCount);

            // 투표 버튼 스타일 업데이트
            updateVoteButtonStyles(tagName, voteType);

            alert(`'${tagName}' 태그에 ${voteType === 'up' ? '추천' : '비추천'}하셨습니다.`);
        } else {
            alert(response.message || '태그 투표 처리 중 오류가 발생했습니다.');
        }
    } catch (error) {
        console.error('태그 투표 처리 중 오류 발생:', error);
        alert('태그 투표 처리 중 오류가 발생했습니다. 로그인이 필요할 수 있습니다.');
    }
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
    document.addEventListener('click', function (e) {
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
        tagForm.addEventListener('submit', async function (e) {
            e.preventDefault();

            const tagName = document.getElementById('tagName').value;
            const tagReason = document.getElementById('tagReason').value;

            if (tagName && tagReason) {
                try {
                    // API 호출을 위해 suggestTag 함수에 tagReason도 넘길 수 있게 API 확장이 필요
                    const response = await KeyboardService.suggestTag(keyboardId, tagName, tagReason);

                    if (response && response.success) {
                        alert("'" + tagName + "' 태그 신청이 접수되었습니다. \n관리자 검토 후 추가될 예정입니다.");
                        this.reset();
                        closeTagModal();
                    } else {
                        alert(response?.message || '태그 신청 중 오류가 발생했습니다.');
                    }
                } catch (error) {
                    console.error('태그 신청 중 오류 발생:', error);
                    alert('태그 신청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
                }
            } else {
                alert('태그 이름과 추천 이유를 모두 입력해주세요.');
            }
        });
    }
}

// 별점 입력 폼 초기화
function initRatingForm() {
    // 리뷰 탭 컨텐츠 찾기
    const reviewsTab = document.getElementById('tab-reviews');
    if (!reviewsTab) return;

    // 리뷰 폼이 이미 있는지 확인
    let ratingFormContainer = reviewsTab.querySelector('.rating-form-container');
    if (ratingFormContainer) return;

    // 리뷰 폼 추가
    const formHtml = `
    <div class="rating-form-container">
      <h3>리뷰 작성하기</h3>
      <form id="ratingForm" class="review-form">
        <div class="rating-stars">
          <span>별점 선택:</span>
          <div class="star-input">
            <i class="star" data-value="1">★</i>
            <i class="star" data-value="2">★</i>
            <i class="star" data-value="3">★</i>
            <i class="star" data-value="4">★</i>
            <i class="star" data-value="5">★</i>
            <input type="hidden" id="ratingValue" name="ratingValue" value="0">
          </div>
        </div>
        <div class="form-group">
          <label for="reviewContent">한줄평</label>
          <textarea id="reviewContent" name="reviewContent" rows="3" placeholder="이 키보드에 대한 의견을 작성해주세요" required></textarea>
        </div>
        <button type="submit" class="submit-review-btn">리뷰 등록</button>
      </form>
    </div>
  `;

    // 리뷰 폼 추가
    reviewsTab.insertAdjacentHTML('afterbegin', formHtml);

    // 별점 선택 기능 추가
    const stars = reviewsTab.querySelectorAll('.star');
    stars.forEach(star => {
        star.addEventListener('click', function () {
            const value = parseInt(this.getAttribute('data-value'));
            document.getElementById('ratingValue').value = value;

            // 별점 표시
            stars.forEach(s => {
                const starValue = parseInt(s.getAttribute('data-value'));
                s.classList.toggle('active', starValue <= value);
            });
        });

        // 마우스 오버 효과
        star.addEventListener('mouseover', function () {
            const value = parseInt(this.getAttribute('data-value'));

            stars.forEach(s => {
                const starValue = parseInt(s.getAttribute('data-value'));
                s.classList.toggle('hover', starValue <= value);
            });
        });
    });

    // 마우스가 나가면 원래 선택한 별점으로 돌아가기
    const starInput = reviewsTab.querySelector('.star-input');
    starInput.addEventListener('mouseout', function () {
        const value = parseInt(document.getElementById('ratingValue').value);

        stars.forEach(s => {
            s.classList.remove('hover');
            const starValue = parseInt(s.getAttribute('data-value'));
            s.classList.toggle('active', starValue <= value);
        });
    });

    // 폼 제출 처리
    const reviewForm = document.getElementById('ratingForm');
    reviewForm.addEventListener('submit', async function (e) {
        e.preventDefault();

        const scoreValue = parseInt(document.getElementById('ratingValue').value);
        const review = document.getElementById('reviewContent').value;

        if (scoreValue === 0) {
            alert('별점을 선택해주세요.');
            return;
        }

        try {
            // API 호출하여 별점 등록
            const response = await KeyboardService.rateKeyboard(keyboardId, scoreValue, review);

            if (response && response.success) {
                alert('리뷰가 성공적으로 등록되었습니다.');
                // 페이지 새로고침하여 업데이트된 리뷰 목록 보기
                location.reload();
            } else {
                alert(response?.message || '리뷰 등록 중 오류가 발생했습니다.');
            }
        } catch (error) {
            console.error('리뷰 등록 중 오류 발생:', error);
            alert('리뷰 등록 중 오류가 발생했습니다. 로그인이 필요할 수 있습니다.');
        }
    });
}

// 페이지 초기화 함수
function initKeyboardDetail() {
    loadKeyboardData();
    initTabs();
    initTagClickEvents();
    initTagForm();
    initRatingForm();
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', initKeyboardDetail);
