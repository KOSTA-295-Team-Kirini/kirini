document.addEventListener('DOMContentLoaded', function() {
  // 탭 전환 기능
  document.querySelectorAll('.mypage-tab').forEach(tab => {
    tab.addEventListener('click', () => {
      // 활성화된 탭 변경
      document.querySelectorAll('.mypage-tab').forEach(t => {
        t.classList.remove('active');
      });
      tab.classList.add('active');
      
      // 해당 섹션 표시
      const tabId = tab.dataset.tab;
      document.querySelectorAll('.mypage-section').forEach(section => {
        section.classList.remove('active');
      });
      document.getElementById(tabId).classList.add('active');
    });
  });

  // 꾸미기 아이템 선택 기능
  document.querySelectorAll('.customize-item').forEach(item => {
    item.addEventListener('click', () => {
      const parent = item.parentElement;
      parent.querySelectorAll('.customize-item').forEach(i => {
        i.classList.remove('active');
      });
      item.classList.add('active');
    });
  });
});
