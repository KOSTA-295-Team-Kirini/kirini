document.addEventListener('DOMContentLoaded', function () {
    // 권한 체크 및 리디렉션
    if (typeof Auth !== 'undefined') {
        // 관리자나 매니저가 아니면 홈페이지로 리디렉션
        if (!Auth.isManagerOrAdmin()) {
            window.location.href = '../pages/index.html';
            return;
        }

        // 첫 로딩시에 적절한 초기 탭 선택
        if (Auth.isManager() && !Auth.isAdmin()) {
            // 매니저의 경우 사용자 권한 탭을 기본 탭으로 설정
            const firstManagerTab = document.querySelector('.admin-tab.manager-admin-only');
            if (firstManagerTab) {
                document.querySelectorAll('.admin-tab').forEach(t => t.classList.remove('active'));
                firstManagerTab.classList.add('active');

                // 해당 섹션 활성화
                const tabId = firstManagerTab.dataset.tab;
                document.querySelectorAll('.admin-section').forEach(section => {
                    section.classList.remove('active');
                });
                document.getElementById(tabId).classList.add('active');
            }
        }
    }

    // 탭 전환 기능
    document.querySelectorAll('.admin-tab').forEach(tab => {
        tab.addEventListener('click', () => {
            // 활성화된 탭 변경
            document.querySelectorAll('.admin-tab').forEach(t => {
                t.classList.remove('active');
            });
            tab.classList.add('active');

            // 해당 섹션 표시
            const tabId = tab.dataset.tab;
            document.querySelectorAll('.admin-section').forEach(section => {
                section.classList.remove('active');
            });
            document.getElementById(tabId).classList.add('active');
        });
    });

    // 폼 초기화 버튼 기능
    document.querySelectorAll('.btn-secondary').forEach(btn => {
        if (btn.textContent === '초기화') {
            btn.addEventListener('click', function () {
                const form = this.closest('form');
                form.reset();
            });
        }
    });

    // 수정 버튼 클릭 시 이벤트 (예시)
    document.querySelectorAll('.admin-table .btn:not(.btn-danger):not(.btn-success):not(.btn-secondary)').forEach(btn => {
        btn.addEventListener('click', function () {
            if (btn.textContent === '수정') {
                const row = this.closest('tr');
                const formId = getFormIdForSection(this.closest('.admin-section').id);
                populateFormFromRow(formId, row);
            }
        });
    });

    // 탭 드래그 스크롤 기능
    const tabsContainer = document.querySelector('.admin-tabs');

    if (tabsContainer) {
        let isDown = false;
        let startX;
        let scrollLeft;

        // 마우스 이벤트
        tabsContainer.addEventListener('mousedown', (e) => {
            isDown = true;
            tabsContainer.style.cursor = 'grabbing';
            startX = e.pageX - tabsContainer.offsetLeft;
            scrollLeft = tabsContainer.scrollLeft;
        });

        tabsContainer.addEventListener('mouseleave', () => {
            isDown = false;
            tabsContainer.style.cursor = 'grab';
        });

        tabsContainer.addEventListener('mouseup', () => {
            isDown = false;
            tabsContainer.style.cursor = 'grab';
        });

        tabsContainer.addEventListener('mousemove', (e) => {
            if (!isDown) return;
            e.preventDefault();
            const x = e.pageX - tabsContainer.offsetLeft;
            const walk = (x - startX) * 2; // 스크롤 속도 조절 (숫자가 클수록 빠름)
            tabsContainer.scrollLeft = scrollLeft - walk;
        });

        // 터치 이벤트 (모바일 대응)
        tabsContainer.addEventListener('touchstart', (e) => {
            isDown = true;
            startX = e.touches[0].pageX - tabsContainer.offsetLeft;
            scrollLeft = tabsContainer.scrollLeft;
        }, {passive: false});

        tabsContainer.addEventListener('touchend', () => {
            isDown = false;
        });

        tabsContainer.addEventListener('touchcancel', () => {
            isDown = false;
        });

        tabsContainer.addEventListener('touchmove', (e) => {
            if (!isDown) return;
            e.preventDefault();
            const x = e.touches[0].pageX - tabsContainer.offsetLeft;
            const walk = (x - startX) * 2; // 스크롤 속도 조절
            tabsContainer.scrollLeft = scrollLeft - walk;
        }, {passive: false});

        // 초기 커서 스타일 설정
        tabsContainer.style.cursor = 'grab';
    }
});

// 섹션에 맞는 폼 ID 반환 함수
function getFormIdForSection(sectionId) {
    const formMap = {
        'terms': 'term-form',
        'keyboard-info': 'keyboard-form',
        'categories': 'category-form',
        'tags': 'tag-form'
    };
    return formMap[sectionId] || null;
}

// 테이블 행의 정보로 폼 채우기 (예시)
function populateFormFromRow(formId, row) {
    if (!formId) return;

    const form = document.getElementById(formId);
    const cells = row.querySelectorAll('td');

    if (formId === 'term-form') {
        form.querySelector('#term-name').value = cells[0].textContent;
        form.querySelector('#term-category').value = cells[1].textContent;
    } else if (formId === 'keyboard-form') {
        form.querySelector('#keyboard-name').value = cells[0].textContent;
        form.querySelector('#keyboard-brand').value = cells[1].textContent;
    } else if (formId === 'category-form') {
        form.querySelector('#category-name').value = cells[0].textContent;
        form.querySelector('#category-description').value = cells[1].textContent;
    } else if (formId === 'tag-form') {
        form.querySelector('#tag-name').value = cells[0].textContent;
        form.querySelector('#tag-category').value = cells[1].textContent;
    }

    // 폼으로 스크롤
    form.scrollIntoView({behavior: 'smooth'});
}
