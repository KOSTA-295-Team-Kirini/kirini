document.addEventListener('DOMContentLoaded', function() {
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
      
      // 탭에 따른 데이터 로드
      if (tabId === 'users') {
        loadUserPenaltyList();
      } else if (tabId === 'reports') {
        loadReportList();
      }
    });
  });

  // 폼 초기화 버튼 기능
  document.querySelectorAll('.btn-secondary').forEach(btn => {
    if (btn.textContent === '초기화') {
      btn.addEventListener('click', function() {
        const form = this.closest('form');
        form.reset();
      });
    }
  });

  // 수정 버튼 클릭 시 이벤트 (예시)
  document.querySelectorAll('.admin-table .btn:not(.btn-danger):not(.btn-success):not(.btn-secondary)').forEach(btn => {
    btn.addEventListener('click', function() {
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
    }, { passive: false });

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
    }, { passive: false });
    
    // 초기 커서 스타일 설정
    tabsContainer.style.cursor = 'grab';
  }
  
  // 초기 로딩 시 현재 활성화된 탭에 따라 데이터 로드
  const activeTab = document.querySelector('.admin-tab.active');
  if (activeTab) {
    const tabId = activeTab.dataset.tab;
    if (tabId === 'users') {
      loadUserPenaltyList();
    } else if (tabId === 'reports') {
      loadReportList();
    }
  }
  
  // 불량 이용자 검색 처리
  const userSearchForm = document.querySelector('#users .search-container');
  if (userSearchForm) {
    const searchButton = userSearchForm.querySelector('button');
    const searchInput = userSearchForm.querySelector('input');
    
    // 검색 버튼 클릭 이벤트
    searchButton.addEventListener('click', function() {
      const searchTerm = searchInput.value.trim();
      searchUsers(searchTerm);
    });
    
    // 엔터 키 입력 이벤트
    searchInput.addEventListener('keypress', function(e) {
      if (e.key === 'Enter') {
        e.preventDefault(); // 기본 동작 방지
        const searchTerm = searchInput.value.trim();
        searchUsers(searchTerm);
      }
    });
  }
  
  // 신고 내역 검색 처리
  const reportSearchForm = document.querySelector('#reports .search-container');
  if (reportSearchForm) {
    const searchButton = reportSearchForm.querySelector('button');
    const searchInput = reportSearchForm.querySelector('input');
    
    // 검색 버튼 클릭 이벤트
    searchButton.addEventListener('click', function() {
      const searchTerm = searchInput.value.trim();
      searchReports(searchTerm);
    });
    
    // 엔터 키 입력 이벤트
    searchInput.addEventListener('keypress', function(e) {
      if (e.key === 'Enter') {
        e.preventDefault(); // 기본 동작 방지
        const searchTerm = searchInput.value.trim();
        searchReports(searchTerm);
      }
    });
  }
  
  // 불량 이용자 액션 버튼 이벤트 위임
  const usersSection = document.getElementById('users');
  if (usersSection) {
    usersSection.addEventListener('click', function(event) {
      const target = event.target;
      if (target.tagName === 'BUTTON') {
        const row = target.closest('tr');
        if (row) {
          const userId = row.dataset.userId;
          if (target.textContent === '경고') {
            warnUser(userId);
          } else if (target.textContent === '차단') {
            blockUser(userId);
          }
        }
      }
    });
  }
  
  // 신고 내역 액션 버튼 이벤트 위임
  const reportsSection = document.getElementById('reports');
  if (reportsSection) {
    reportsSection.addEventListener('click', function(event) {
      const target = event.target;
      if (target.tagName === 'BUTTON') {
        const row = target.closest('tr');
        if (row) {
          const reportId = row.dataset.reportId;
          if (target.textContent === '상세보기') {
            viewReportDetail(reportId);
          } else if (target.textContent === '삭제') {
            deleteReportedContent(reportId);
          } else if (target.textContent === '무시') {
            ignoreReport(reportId);
          }
        }
      }
    });
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
  form.scrollIntoView({ behavior: 'smooth' });
}

// 한국어 날짜 문자열을 Date 객체로 변환하는 함수
function parseKoreanDate(dateStr) {
  console.log('[admin.js] 한국어 날짜 파싱 시도:', dateStr);
  if (!dateStr) return null;
  
  // "5월 20, 2025" 같은 한국어 날짜 형식 파싱
  const monthNames = {
    '1월': 0, '2월': 1, '3월': 2, '4월': 3, '5월': 4, '6월': 5,
    '7월': 6, '8월': 7, '9월': 8, '10월': 9, '11월': 10, '12월': 11
  };
  
  try {
    // 정규식으로 월, 일, 년도 추출
    const match = dateStr.match(/(\d+월)\s+(\d+),\s+(\d+)/);
    if (match) {
      const month = monthNames[match[1]];
      const day = parseInt(match[2]);
      const year = parseInt(match[3]);
      
      if (!isNaN(month) && !isNaN(day) && !isNaN(year)) {
        const date = new Date(year, month, day);
        console.log(`[admin.js] 파싱 성공: ${month+1}월 ${day}일, ${year}년 => ${date.toISOString()}`);
        return date;
      }
    }
    
    // 다른 방식 시도 - 직접 문자열 분리
    const parts = dateStr.split(', ');
    if (parts.length === 2) {
      const year = parseInt(parts[1]);
      const monthDayParts = parts[0].split(' ');
      if (monthDayParts.length === 2) {
        const month = monthNames[monthDayParts[0]];
        const day = parseInt(monthDayParts[1]);
        
        if (!isNaN(month) && !isNaN(day) && !isNaN(year)) {
          const date = new Date(year, month, day);
          console.log(`[admin.js] 파싱 성공 (방법2): ${month+1}월 ${day}일, ${year}년 => ${date.toISOString()}`);
          return date;
        }
      }
    }
    
    console.warn('[admin.js] 한국어 날짜 파싱 실패:', dateStr);
    return null;
  } catch (error) {
    console.error('[admin.js] 날짜 파싱 중 오류 발생:', error);
    return null;
  }
}

// 불량 이용자 목록 로드
async function loadUserPenaltyList() {
  console.log('[admin.js] 불량 이용자 목록 로드 함수 시작');
  try {
    // 로딩 표시 추가
    const usersSection = document.getElementById('users');
    const tableBody = usersSection.querySelector('.admin-table tbody');
    tableBody.innerHTML = '<tr><td colspan="7" class="text-center">불량 이용자 목록을 불러오는 중...</td></tr>';
    
    // API 호출
    console.log('[admin.js] 불량 이용자 목록 API 요청 시작: /admin/user/penalty');
    const response = await fetch('/admin/user/penalty');
    if (!response.ok) {
      console.error('[admin.js] API 응답 오류:', response.status, response.statusText);
      throw new Error('서버 오류가 발생했습니다.');
    }
    
    console.log('[admin.js] API 응답 성공, JSON 파싱 시작');
    const data = await response.json();
    console.log('[admin.js] 받은 데이터:', data);
    
    // 테이블 내용 갱신
    if (data && data.length > 0) {
      console.log('[admin.js] 불량 이용자 데이터 수:', data.length);
      tableBody.innerHTML = '';
      data.forEach((penalty, index) => {
        console.log(`[admin.js] 항목 #${index} 처리 중:`, penalty);
        const row = document.createElement('tr');
        row.dataset.userId = penalty.userUid;
        row.dataset.penaltyId = penalty.penaltyUid;
        
        // 날짜 처리 - 한국어 날짜 형식을 파싱
        let startDate = '알 수 없음';
        let endDate = '알 수 없음';
        let penaltyDays = penalty.penaltyDays || '0';
        
        try {
          // 새로운 한국어 날짜 파싱 함수 사용
          if (penalty.penaltyStartDate) {
            const parsedStartDate = parseKoreanDate(penalty.penaltyStartDate);
            if (parsedStartDate) {
              startDate = parsedStartDate.toLocaleDateString();
              console.log(`[admin.js] 시작일 변환 성공: ${penalty.penaltyStartDate} -> ${startDate}`);
            } else {
              startDate = penalty.penaltyStartDate; // 파싱 실패시 원본 표시
              console.warn(`[admin.js] 시작일 변환 실패, 원본 사용: ${startDate}`);
            }
          } else {
            console.warn('[admin.js] 시작일 데이터 없음');
          }
          
          if (penalty.penaltyEndDate) {
            const parsedEndDate = parseKoreanDate(penalty.penaltyEndDate);
            if (parsedEndDate) {
              endDate = parsedEndDate.toLocaleDateString();
              console.log(`[admin.js] 종료일 변환 성공: ${penalty.penaltyEndDate} -> ${endDate}`);
            } else {
              endDate = penalty.penaltyEndDate; // 파싱 실패시 원본 표시
              console.warn(`[admin.js] 종료일 변환 실패, 원본 사용: ${endDate}`);
            }
          } else {
            console.warn('[admin.js] 종료일 데이터 없음');
          }
          
          console.log(`[admin.js] 차단일수: ${penaltyDays}`);
          
        } catch (dateError) {
          console.error('[admin.js] 날짜 변환 오류:', dateError);
        }
        
        const status = penalty.penaltyStatus === 'active' ? 'status-penalized' : 'status-inactive';
        
        row.innerHTML = `
          <td>${penalty.userEmail || '-'}</td>
          <td>${penalty.username || '알 수 없음'}</td>
          <td>${startDate}</td>
          <td>${endDate}</td>
          <td>${penaltyDays}</td>
          <td><span class="status ${status}">${penalty.penaltyStatus || '알 수 없음'}</span></td>
          <td>
            <div class="action-buttons">
              <button class="btn btn-secondary">차단 일수 수정</button>
              <button class="btn btn-success">차단해제</button>
            </div>
          </td>
        `;
        tableBody.appendChild(row);
      });
    } else {
      console.log('[admin.js] 불량 이용자 데이터 없음');
      tableBody.innerHTML = '<tr><td colspan="7" class="text-center">불량 이용자 내역이 없습니다.</td></tr>';
    }
  } catch (error) {
    console.error('[admin.js] 불량 이용자 목록 로드 실패:', error);
    const usersSection = document.getElementById('users');
    const tableBody = usersSection.querySelector('.admin-table tbody');
    tableBody.innerHTML = '<tr><td colspan="7" class="text-center">불량 이용자 목록을 불러오는 데 실패했습니다.</td></tr>';
  }
}

// 신고 내역 목록 로드
async function loadReportList() {
  try {
    // 로딩 표시 추가
    const reportsSection = document.getElementById('reports');
    const tableBody = reportsSection.querySelector('.admin-table tbody');
    tableBody.innerHTML = '<tr><td colspan="6" class="text-center">신고 내역을 불러오는 중...</td></tr>';
    
    // API 호출
    const response = await fetch('/admin/report');
    if (!response.ok) {
      throw new Error('서버 오류가 발생했습니다.');
    }
    
    const data = await response.json();
    
    // 테이블 내용 갱신
    if (data && data.length > 0) {
      tableBody.innerHTML = '';
      data.forEach(report => {
        const row = document.createElement('tr');
        row.dataset.reportId = report.reportUid;
        row.dataset.targetUserId = report.targetUserUid;
        
        const formattedDate = new Date(report.reportCreatetime).toLocaleDateString();
        const targetType = getTargetTypeDisplay(report.reportTargetType);
        
        row.innerHTML = `
          <td><a href="#" class="report-link">${getReportTargetDisplay(report)}</a></td>
          <td>${targetType}</td>
          <td>${report.reporterUsername || '알 수 없음'}</td>
          <td>${report.reportReason || '-'}</td>
          <td>${formattedDate}</td>
          <td>
            <div class="action-buttons">
              <button class="btn">상세보기</button>
              <button class="btn btn-danger">삭제</button>
              <button class="btn btn-secondary">무시</button>
            </div>
          </td>
        `;
        tableBody.appendChild(row);
      });
    } else {
      tableBody.innerHTML = '<tr><td colspan="6" class="text-center">신고 내역이 없습니다.</td></tr>';
    }
  } catch (error) {
    console.error('신고 내역 목록 로드 실패:', error);
    showError('신고 내역을 불러오는 데 실패했습니다.');
  }
}

// 신고 대상 타입 표시 문자열 반환
function getTargetTypeDisplay(targetType) {
  const types = {
    'post': '게시글',
    'comment': '댓글',
    'user': '사용자',
    'chat': '채팅'
  };
  return types[targetType] || '기타';
}

// 신고 대상 표시 문자열 반환
function getReportTargetDisplay(report) {
  const type = report.reportTargetType;
  const targetUsername = report.targetUsername || '알 수 없음';
  
  if (type === 'post') {
    return `게시글: ${report.targetContent || '내용 없음'}`;
  } else if (type === 'comment') {
    return `댓글: "${report.targetContent || '내용 없음'}"`;
  } else if (type === 'user') {
    return `사용자: ${targetUsername}`;
  } else if (type === 'chat') {
    return `채팅: "${report.targetContent || '내용 없음'}"`;
  }
  
  return '알 수 없는 대상';
}

// 불량 이용자 검색
async function searchUsers(searchTerm) {
  try {
    if (!searchTerm) {
      loadUserPenaltyList(); // 검색어가 없으면 전체 목록 로드
      return;
    }
    
    console.log('[admin.js] 불량 이용자 검색 시작, 검색어:', searchTerm);
    
    // 로딩 표시 추가
    const usersSection = document.getElementById('users');
    const tableBody = usersSection.querySelector('.admin-table tbody');
    tableBody.innerHTML = '<tr><td colspan="7" class="text-center">검색 결과를 불러오는 중...</td></tr>';    // API 호출 
    console.log(`[admin.js] 검색 API 요청: /admin/user/penalty?action=search&keyword=${encodeURIComponent(searchTerm)}`);
    const response = await fetch(`/admin/user/penalty?action=search&keyword=${encodeURIComponent(searchTerm)}`);
    if (!response.ok) {
      console.error('[admin.js] 검색 API 응답 오류:', response.status, response.statusText);
      throw new Error('서버 오류가 발생했습니다.');
    }    // response.json() 대신에 text()로 받아서 JSON으로 파싱
    const rawResponse = await response.text();
    console.log('[admin.js] 원본 응답:', rawResponse);
    
    // JSON으로 변환
    const data = JSON.parse(rawResponse);
    console.log('[admin.js] 검색 결과 수:', data.length);
    console.log('[admin.js] 검색 결과 데이터:', data);
    
    // 테이블 내용 갱신
    if (data && data.length > 0) {
      console.log('[admin.js] 검색 결과 수:', data.length);
      tableBody.innerHTML = '';
      data.forEach((penalty, index) => {
        console.log(`[admin.js] 검색 결과 항목 #${index} 처리 중:`, penalty);
        const row = document.createElement('tr');
        row.dataset.userId = penalty.userUid;
        row.dataset.penaltyId = penalty.penaltyUid;
        
        // 날짜 처리 - 한국어 날짜 형식을 파싱
        let startDate = '알 수 없음';
        let endDate = '알 수 없음';
        let penaltyDays = penalty.penaltyDays || '0';
        
        try {
          // 새로운 한국어 날짜 파싱 함수 사용
          if (penalty.penaltyStartDate) {
            const parsedStartDate = parseKoreanDate(penalty.penaltyStartDate);
            if (parsedStartDate) {
              startDate = parsedStartDate.toLocaleDateString();
            } else {
              startDate = penalty.penaltyStartDate; // 파싱 실패시 원본 표시
            }
          }
          
          if (penalty.penaltyEndDate) {
            const parsedEndDate = parseKoreanDate(penalty.penaltyEndDate);
            if (parsedEndDate) {
              endDate = parsedEndDate.toLocaleDateString();
            } else {
              endDate = penalty.penaltyEndDate; // 파싱 실패시 원본 표시
            }
          }
        } catch (dateError) {
          console.error('[admin.js] 검색 결과 날짜 변환 오류:', dateError);
        }
        
        const status = penalty.penaltyStatus === 'active' ? 'status-penalized' : 'status-inactive';
        
        row.innerHTML = `
          <td>${penalty.userEmail || '-'}</td>
          <td>${penalty.username || '알 수 없음'}</td>
          <td>${startDate}</td>
          <td>${endDate}</td>
          <td>${penaltyDays}</td>
          <td><span class="status ${status}">${penalty.penaltyStatus || '알 수 없음'}</span></td>
          <td>
            <div class="action-buttons">
              <button class="btn btn-secondary">차단 일수 수정</button>
              <button class="btn btn-success">차단해제</button>
            </div>
          </td>
        `;
        tableBody.appendChild(row);
      });
    } else {
      tableBody.innerHTML = '<tr><td colspan="7" class="text-center">검색 결과가 없습니다.</td></tr>';
    }
  } catch (error) {
    console.error('사용자 검색 실패:', error);
    showError('사용자 검색에 실패했습니다.');
  }
}

// 신고 내역 검색
async function searchReports(searchTerm) {
  try {
    if (!searchTerm) {
      loadReportList(); // 검색어가 없으면 전체 목록 로드
      return;
    }
    
    // 로딩 표시 추가
    const reportsSection = document.getElementById('reports');
    const tableBody = reportsSection.querySelector('.admin-table tbody');
    tableBody.innerHTML = '<tr><td colspan="6" class="text-center">검색 결과를 불러오는 중...</td></tr>';
    
    // API 호출
    const response = await fetch(`/admin/report/search?term=${encodeURIComponent(searchTerm)}`);
    if (!response.ok) {
      throw new Error('서버 오류가 발생했습니다.');
    }
    
    const data = await response.json();
    
    // 테이블 내용 갱신
    if (data && data.length > 0) {
      tableBody.innerHTML = '';
      data.forEach(report => {
        // 위의 loadReportList와 동일한 로직
        const row = document.createElement('tr');
        row.dataset.reportId = report.reportUid;
        row.dataset.targetUserId = report.targetUserUid;
        
        const formattedDate = new Date(report.reportCreatetime).toLocaleDateString();
        const targetType = getTargetTypeDisplay(report.reportTargetType);
        
        row.innerHTML = `
          <td><a href="#" class="report-link">${getReportTargetDisplay(report)}</a></td>
          <td>${targetType}</td>
          <td>${report.reporterUsername || '알 수 없음'}</td>
          <td>${report.reportReason || '-'}</td>
          <td>${formattedDate}</td>
          <td>
            <div class="action-buttons">
              <button class="btn">상세보기</button>
              <button class="btn btn-danger">삭제</button>
              <button class="btn btn-secondary">무시</button>
            </div>
          </td>
        `;
        tableBody.appendChild(row);
      });
    } else {
      tableBody.innerHTML = '<tr><td colspan="6" class="text-center">검색 결과가 없습니다.</td></tr>';
    }
  } catch (error) {
    console.error('신고 내역 검색 실패:', error);
    showError('신고 내역 검색에 실패했습니다.');
  }
}

// 사용자 경고
async function warnUser(userId) {
  try {
    if (!userId) return;
    
    if (!confirm('해당 이용자에게 경고를 보내시겠습니까?')) {
      return;
    }
    
    // 경고 사유 입력 받기
    const reason = prompt('경고 사유를 입력해주세요:');
    if (!reason) return;
    
    const currentUser = Auth.getCurrentUser();
    if (!currentUser || !currentUser.userUid) {
      throw new Error('관리자 정보를 찾을 수 없습니다.');
    }
    
    // API 호출
    const response = await fetch('/admin/user/penalty/warn', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        userId: userId,
        reason: reason,
        adminId: currentUser.userUid
      })
    });
    
    if (!response.ok) {
      throw new Error('서버 오류가 발생했습니다.');
    }
    
    const data = await response.json();
    if (data.success) {
      showSuccess('경고가 성공적으로 전송되었습니다.');
      loadUserPenaltyList(); // 목록 새로고침
    } else {
      showError(data.message || '경고 처리에 실패했습니다.');
    }
  } catch (error) {
    console.error('사용자 경고 실패:', error);
    showError('사용자 경고에 실패했습니다.');
  }
}

// 사용자 차단
async function blockUser(userId) {
  try {
    if (!userId) return;
    
    if (!confirm('해당 이용자를 차단하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
      return;
    }
    
    // 차단 기간 선택
    const durations = ['1일', '3일', '7일', '30일', '영구'];
    let selectedDuration = prompt(`차단 기간을 입력하세요 (${durations.join(', ')}):`, '7일');
    
    if (!selectedDuration || !durations.includes(selectedDuration)) {
      showError('유효하지 않은 차단 기간입니다.');
      return;
    }
    
    // 차단 사유 입력 받기
    const reason = prompt('차단 사유를 입력해주세요:');
    if (!reason) return;
    
    const currentUser = Auth.getCurrentUser();
    if (!currentUser || !currentUser.userUid) {
      throw new Error('관리자 정보를 찾을 수 없습니다.');
    }
    
    // API 호출
    const response = await fetch('/admin/report/penalty', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        userId: userId,
        reason: reason,
        duration: selectedDuration,
        adminId: currentUser.userUid,
        reportId: 0 // 신고와 관계없는 직접 차단의 경우
      })
    });
    
    if (!response.ok) {
      throw new Error('서버 오류가 발생했습니다.');
    }
    
    const data = await response.json();
    if (data.success) {
      showSuccess('사용자가 성공적으로 차단되었습니다.');
      loadUserPenaltyList(); // 목록 새로고침
    } else {
      showError(data.message || '사용자 차단에 실패했습니다.');
    }
  } catch (error) {
    console.error('사용자 차단 실패:', error);
    showError('사용자 차단에 실패했습니다.');
  }
}

// 신고 상세 보기
function viewReportDetail(reportId) {
  if (!reportId) return;
  
  // 해당 행 찾기
  const row = document.querySelector(`tr[data-report-id="${reportId}"]`);
  if (!row) return;
  
  // 행의 데이터 가져와서 모달 또는 상세 페이지 표시
  const targetUserId = row.dataset.targetUserId;
  const reportTarget = row.querySelector('td:first-child').textContent;
  const reportCategory = row.querySelector('td:nth-child(2)').textContent;
  const reporter = row.querySelector('td:nth-child(3)').textContent;
  const reason = row.querySelector('td:nth-child(4)').textContent;
  const date = row.querySelector('td:nth-child(5)').textContent;
  
  // 모달 생성 및 표시
  const modal = document.createElement('div');
  modal.className = 'modal';
  modal.innerHTML = `
    <div class="modal-content">
      <span class="close">&times;</span>
      <h2>신고 상세 정보</h2>
      <p><strong>신고 ID:</strong> ${reportId}</p>
      <p><strong>신고 대상:</strong> ${reportTarget}</p>
      <p><strong>카테고리:</strong> ${reportCategory}</p>
      <p><strong>신고자:</strong> ${reporter}</p>
      <p><strong>신고 사유:</strong> ${reason}</p>
      <p><strong>신고일:</strong> ${date}</p>
      <div class="modal-actions">
        <button class="btn btn-danger" id="penalize-button">제재 처리</button>
        <button class="btn btn-secondary" id="ignore-button">무시하기</button>
      </div>
    </div>
  `;
  
  document.body.appendChild(modal);
  modal.style.display = 'block';
  
  // 모달 닫기 버튼
  const closeButton = modal.querySelector('.close');
  closeButton.addEventListener('click', () => {
    modal.remove();
  });
  
  // 제재 처리 버튼 이벤트
  const penalizeButton = modal.querySelector('#penalize-button');
  penalizeButton.addEventListener('click', () => {
    applyPenalty(reportId, targetUserId);
    modal.remove();
  });
  
  // 무시하기 버튼 이벤트
  const ignoreButton = modal.querySelector('#ignore-button');
  ignoreButton.addEventListener('click', () => {
    ignoreReport(reportId);
    modal.remove();
  });
}

// 신고된 컨텐츠 삭제
async function deleteReportedContent(reportId) {
  try {
    if (!reportId) return;
    
    if (!confirm('신고된 컨텐츠를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
      return;
    }
    
    // API 호출
    const response = await fetch('/admin/report/delete-content', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        reportId: reportId
      })
    });
    
    if (!response.ok) {
      throw new Error('서버 오류가 발생했습니다.');
    }
    
    const data = await response.json();
    if (data.success) {
      showSuccess('컨텐츠가 성공적으로 삭제되었습니다.');
      loadReportList(); // 목록 새로고침
    } else {
      showError(data.message || '컨텐츠 삭제에 실패했습니다.');
    }
  } catch (error) {
    console.error('컨텐츠 삭제 실패:', error);
    showError('컨텐츠 삭제에 실패했습니다.');
  }
}

// 신고 무시
async function ignoreReport(reportId) {
  try {
    if (!reportId) return;
    
    if (!confirm('이 신고를 무시하시겠습니까?')) {
      return;
    }
    
    // API 호출
    const response = await fetch('/admin/report/update', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        reportId: reportId,
        status: '무시됨'
      })
    });
    
    if (!response.ok) {
      throw new Error('서버 오류가 발생했습니다.');
    }
    
    const data = await response.json();
    if (data.success) {
      showSuccess('신고가 무시 처리되었습니다.');
      loadReportList(); // 목록 새로고침
    } else {
      showError(data.message || '신고 처리에 실패했습니다.');
    }
  } catch (error) {
    console.error('신고 무시 처리 실패:', error);
    showError('신고 무시 처리에 실패했습니다.');
  }
}

// 제재 적용 (패널티 부여)
async function applyPenalty(reportId, userId) {
  try {
    if (!reportId || !userId) return;
    
    // 차단 기간 선택
    const durations = ['1일', '3일', '7일', '30일', '영구'];
    let selectedDuration = prompt(`차단 기간을 입력하세요 (${durations.join(', ')}):`, '7일');
    
    if (!selectedDuration || !durations.includes(selectedDuration)) {
      showError('유효하지 않은 차단 기간입니다.');
      return;
    }
    
    // 차단 사유 입력 받기
    const reason = prompt('제재 사유를 입력해주세요:');
    if (!reason) return;
    
    const currentUser = Auth.getCurrentUser();
    if (!currentUser || !currentUser.userUid) {
      throw new Error('관리자 정보를 찾을 수 없습니다.');
    }
    
    // API 호출
    const response = await fetch('/admin/report/penalty', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        reportId: reportId,
        userId: userId,
        reason: reason,
        duration: selectedDuration,
        adminId: currentUser.userUid
      })
    });
    
    if (!response.ok) {
      throw new Error('서버 오류가 발생했습니다.');
    }
    
    const data = await response.json();
    if (data.success) {
      showSuccess('제재가 성공적으로 적용되었습니다.');
      loadReportList(); // 목록 새로고침
    } else {
      showError(data.message || '제재 적용에 실패했습니다.');
    }
  } catch (error) {
    console.error('제재 적용 실패:', error);
    showError('제재 적용에 실패했습니다.');
  }
}

// 성공 메시지 표시
function showSuccess(message) {
  alert(message); // 실제로는 토스트나 알림창으로 대체할 수 있음
}

// 에러 메시지 표시
function showError(message) {
  alert('오류: ' + message); // 실제로는 토스트나 알림창으로 대체할 수 있음
}
