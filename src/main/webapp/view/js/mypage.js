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
    
  // 사용자 프로필 정보 로드 및 표시 함수
  async function loadUserProfile() {
    try {
      // UserService를 사용하여 현재 사용자 프로필 정보 요청 (인증된 사용자 기준)
      const userProfile = await UserService.getProfile(); // userId를 생략하면 현재 사용자 프로필을 가져옴

      if (userProfile && userProfile.success) {
        const userData = userProfile.data; // 실제 데이터 객체 키에 따라 수정 (예: userProfile.user)

        // 프로필 정보 표시 영역 업데이트
        const usernameDisplay = document.getElementById('profile-username-display');
        const emailDisplay = document.getElementById('profile-email-display');
        const postCountDisplay = document.getElementById('profile-post-count');
        const scrapCountDisplay = document.getElementById('profile-scrap-count');
        const pointCountDisplay = document.getElementById('profile-point-count');

        if (usernameDisplay) usernameDisplay.textContent = userData.name ? `${userData.name}님` : '이름 정보 없음';
        if (emailDisplay) emailDisplay.textContent = userData.email || '이메일 정보 없음';
        // 아래 통계 수치는 userData 객체에 해당 필드가 있어야 합니다. (예: userData.postCount)
        // 실제 필드명으로 수정해주세요.
        if (postCountDisplay) postCountDisplay.textContent = userData.postCount !== undefined ? userData.postCount : '-'; 
        if (scrapCountDisplay) scrapCountDisplay.textContent = userData.scrapCount !== undefined ? userData.scrapCount : '-';
        if (pointCountDisplay) pointCountDisplay.textContent = userData.points !== undefined ? userData.points.toLocaleString() : '-';

        // 내 정보 수정 폼 필드 채우기
        const usernameInput = document.getElementById('username');
        const emailInput = document.getElementById('email');
        const bioInput = document.getElementById('bio');

        if (usernameInput) usernameInput.value = userData.name || '';
        if (emailInput) emailInput.value = userData.email || '';
        if (bioInput) bioInput.value = userData.bio || '';

      } else {
        console.error('프로필 정보 로드 실패:', userProfile ? userProfile.message : '알 수 없는 오류');
        alert('프로필 정보를 불러오는데 실패했습니다. 다시 시도해주세요.');
        // 로그인 페이지로 리디렉션 또는 다른 오류 처리
        // window.location.href = '/login.html'; 
      }
    } catch (error) {
      console.error('프로필 정보 로드 중 오류 발생:', error);
      alert('프로필 정보를 불러오는 중 오류가 발생했습니다.');
    }
  }

  // 페이지 로드 시 사용자 프로필 정보 로드
  loadUserProfile();

  // 내 정보 수정 기능
  const profileForm = document.querySelector('#profile form');
  if (profileForm) {
    profileForm.addEventListener('submit', async (event) => {
      event.preventDefault(); // 폼 기본 제출 동작 방지

      const usernameInput = document.getElementById('username');
      const emailInput = document.getElementById('email');
      const passwordInput = document.getElementById('password');
      const passwordConfirmInput = document.getElementById('password-confirm');
      const bioInput = document.getElementById('bio');

      const username = usernameInput ? usernameInput.value : '';
      const email = emailInput ? emailInput.value : '';
      const password = passwordInput ? passwordInput.value : '';
      const passwordConfirm = passwordConfirmInput ? passwordConfirmInput.value : '';
      const bio = bioInput ? bioInput.value : '';

      if (password && password !== passwordConfirm) {
        alert('새 비밀번호와 비밀번호 확인이 일치하지 않습니다.');
        return;
      }

      const profileData = {
        name: username, // 서버에서 받는 필드명에 맞춰야 할 수 있습니다. (예: name 또는 username)
        email,
        bio
      };

      // 비밀번호 필드가 채워져 있고, 일치하는 경우에만 profileData에 추가
      if (password) {
        // UserService.updateProfile은 전체 프로필을 업데이트하므로,
        // 비밀번호 변경은 별도의 API(UserService.changePassword)를 사용하거나
        // updateProfile API가 비밀번호 필드를 선택적으로 받도록 해야 합니다.
        // 여기서는 profileData에 password를 포함시켜서 보내고,
        // 서버 API가 이를 처리한다고 가정합니다.
        // 만약 비밀번호 변경 API가 분리되어 있다면, 별도 UI와 로직이 필요합니다.
        profileData.newPassword = password; // 서버에서 받는 필드명에 맞춰야 합니다.
      }

      try {
        // UserService를 사용하여 프로필 업데이트 요청
        const result = await UserService.updateProfile(profileData);
        
        // api-client.js의 UserService.updateProfile는 이미 JSON 파싱을 처리하고,
        // 성공/실패에 따라 resolve/reject를 반환하거나, 특정 구조의 객체를 반환할 것으로 예상됩니다.
        // 실제 UserService.updateProfile의 반환 값 구조에 맞춰 처리해야 합니다.
        // 예를 들어, 성공 시 { success: true, message: "..." } 형태를 반환한다고 가정합니다.

        if (result && (result.success || result.status === 'success')) { // 실제 응답 구조에 따라 조건 변경
          alert(result.message || '정보가 성공적으로 수정되었습니다.');
          // 필요하다면 페이지를 새로고침하거나 UI를 업데이트합니다.
          // window.location.reload(); 
        } else {
          // 실패 메시지가 result 객체 안에 있을 경우
          alert('정보 수정에 실패했습니다.\\n' + (result.message || '알 수 없는 오류가 발생했습니다.'));
        }
      } catch (error) {
        console.error('정보 수정 중 오류 발생:', error);
        // error 객체에 서버에서 보낸 메시지가 포함되어 있을 수 있습니다.
        alert('정보 수정 중 오류가 발생했습니다.\\n' + (error.message || ''));
      }
    });
  }

  // 스크랩 정보 로드 함수
  async function loadScrappedKeyboards() {
    const scrapKeyboardGrid = document.getElementById('scrap-keyboard-grid');
    if (!scrapKeyboardGrid) return;

    try {
      // ApiClient를 사용하여 스크랩 목록을 가져오는 API 호출 (엔드포인트는 확인 필요)
      // api-client.js에 스크랩 전용 함수가 없다면, ApiClient.get을 직접 사용할 수 있습니다.
      // UserService.getProfile() 등이 스크랩 정보를 포함할 수도 있습니다.
      // 여기서는 기존 엔드포인트를 ApiClient.get으로 호출하는 예시를 보여줍니다.
      // 실제 엔드포인트는 /user/scraps.do 또는 유사한 형태일 가능성이 높습니다.
      const result = await ApiClient.get('/user/scraps.do'); // 또는 '/mypage.do?api=true&endpoint=scraps'가 맞다면 그대로 사용

      // ApiClient.get은 이미 JSON 파싱을 처리합니다.
      // 응답 구조가 { items: [...] } 형태라고 가정합니다.
      scrapKeyboardGrid.innerHTML = ''; // 기존 내용 초기화

      const scraps = result.items || result; // 실제 응답 구조에 따라 scraps 배열을 가져옵니다.

      if (!scraps || scraps.length === 0) {
        scrapKeyboardGrid.innerHTML = '<p>스크랩한 키보드가 아직 없습니다. 첫 스크랩을 해보세요!</p>';
        return;
      }

      scraps.forEach(keyboard => {
        const card = document.createElement('div');
        card.classList.add('keyboard-card');
        card.innerHTML = `
          <img src="${keyboard.imagePath || keyboard.image || '../img/default_keyboard.png'}" alt="${keyboard.name}" class="keyboard-image" onerror="this.src='https://via.placeholder.com/400x200?text=키보드+이미지'">            <div class="keyboard-content">
            <h3 class="keyboard-title">${keyboard.name}</h3>
            <div class="keyboard-price">${keyboard.price ? (typeof keyboard.price === 'number' ? keyboard.price.toLocaleString() : keyboard.price) + '원' : '가격 정보 없음'}</div>
            <button class="btn" style="margin-top: 0.5rem; padding: 0.5rem;" onclick="location.href='/keyboard.do?action=view&id=${keyboard.id || keyboard.keyboardId}'">상세보기</button>
          </div>
        `;
        scrapKeyboardGrid.appendChild(card);
      });
    } catch (error) {
      console.error('스크랩 정보 로드 중 오류 발생:', error);
      scrapKeyboardGrid.innerHTML = '<p>스크랩 정보 로드 중 오류가 발생했습니다.</p>';
    }
  }

  // 스크랩 탭이 활성화될 때 또는 페이지 로드 시 스크랩 정보 로드
  const scrapsTab = document.querySelector('.mypage-tab[data-tab="scraps"]');
  if (scrapsTab && scrapsTab.classList.contains('active')) {
    loadScrappedKeyboards();
  }
  if(scrapsTab){
    scrapsTab.addEventListener('click', () => {
        if(document.getElementById('scraps').classList.contains('active')){
            loadScrappedKeyboards();
        }
    });
  }
  // 페이지 최초 로드 시 프로필 탭이 기본 활성화 상태라면, 그때는 스크랩 정보를 미리 로드하지 않아도 됨.
  // 만약 스크랩 탭이 기본 활성화라면 위에서 처리됨.
  // 사용자가 스크랩 탭을 클릭했을 때 로드하도록 이벤트 리스너는 유지.
});
