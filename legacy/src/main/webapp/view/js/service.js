// 탭 전환 기능
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.service-tab').forEach(tab => {
        tab.addEventListener('click', () => {
            // 활성화된 탭 변경
            document.querySelectorAll('.service-tab').forEach(t => {
                t.classList.remove('active');
            });
            tab.classList.add('active');

            // 해당 섹션 표시
            const tabId = tab.dataset.tab;
            document.querySelectorAll('.service-section').forEach(section => {
                section.classList.remove('active');
            });
            document.getElementById(tabId).classList.add('active');
        });
    });

    // FAQ 아코디언 기능
    document.querySelectorAll('.faq-question').forEach(question => {
        question.addEventListener('click', () => {
            const answer = question.nextElementSibling;

            // 현재 질문 토글
            question.classList.toggle('active');
            answer.classList.toggle('active');

            // 다른 열린 FAQ 닫기 (한 번에 하나만 열리도록)
            document.querySelectorAll('.faq-question.active').forEach(activeQuestion => {
                if (activeQuestion !== question) {
                    activeQuestion.classList.remove('active');
                    activeQuestion.nextElementSibling.classList.remove('active');
                }
            });
        });
    });

    // 문의하기 폼 제출
    document.getElementById('inquiry-form').addEventListener('submit', function (e) {
        e.preventDefault();

        // 여기에 폼 제출 로직 구현
        alert('문의가 접수되었습니다. 빠른 시일 내에 답변 드리겠습니다.');
        this.reset();
    });

    // 공지사항 클릭 이벤트 (상세 내용 보기 기능 예시)
    document.querySelectorAll('.notice-title').forEach(title => {
        title.addEventListener('click', () => {
            // 여기에 공지사항 상세 보기 로직 구현
            alert('공지사항 상세 내용 페이지로 이동합니다.');
        });
    });
});
