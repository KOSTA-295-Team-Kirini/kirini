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
