/**
 * AJAX API 유틸리티
 * 통합 AjaxController와 통신하기 위한 함수들을 제공합니다.
 */

// 기본 API URL
const API_URL = "/api";

/**
 * GET 요청 보내기
 * @param {string} action - 요청 작업 (예: 'getGuides', 'searchGuides')
 * @param {Object} params - 요청 파라미터 (예: {keyword: '검색어'})
 * @returns {Promise} - 응답 Promise
 */
function apiGet(action, params = {}) {
  // URL 파라미터 생성
  const queryParams = new URLSearchParams({
    action,
    ...params,
  }).toString();

  // GET 요청 보내기
  return fetch(`${API_URL}?${queryParams}`)
    .then((response) => {
      if (!response.ok) {
        throw new Error("서버 응답 오류: " + response.status);
      }
      return response.json();
    })
    .then((data) => {
      if (!data.success) {
        throw new Error(data.message || "알 수 없는 오류");
      }
      return data.data;
    });
}

/**
 * POST 요청 보내기
 * @param {string} action - 요청 작업 (예: 'postChat', 'addKeyboardScore')
 * @param {Object} data - 요청 데이터
 * @returns {Promise} - 응답 Promise
 */
function apiPost(action, data = {}) {
  // FormData 객체 생성
  const formData = new FormData();
  formData.append("action", action);

  // 데이터 추가
  for (const key in data) {
    if (data.hasOwnProperty(key)) {
      formData.append(key, data[key]);
    }
  }

  // POST 요청 보내기
  return fetch(API_URL, {
    method: "POST",
    body: formData,
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error("서버 응답 오류: " + response.status);
      }
      return response.json();
    })
    .then((data) => {
      if (!data.success) {
        throw new Error(data.message || "알 수 없는 오류");
      }
      return data.data;
    });
}

/**
 * 키보드 용어집 전체 목록 가져오기
 * @returns {Promise} - 용어집 목록 Promise
 */
function getAllGuides() {
  return apiGet("getGuides");
}

/**
 * 키보드 용어집 검색
 * @param {string} keyword - 검색 키워드
 * @returns {Promise} - 검색 결과 Promise
 */
function searchGuides(keyword) {
  return apiGet("searchGuides", { keyword });
}

/**
 * 키보드 용어 상세 정보 가져오기
 * @param {number} id - 용어 ID
 * @returns {Promise} - 용어 상세 정보 Promise
 */
function getGuideDetail(id) {
  return apiGet("getGuideDetail", { id });
}

/**
 * 키보드 정보 목록 가져오기
 * @param {number} page - 페이지 번호
 * @returns {Promise} - 키보드 목록 Promise
 */
function getKeyboardInfos(page = 1) {
  return apiGet("getKeyboardInfos", { page });
}

/**
 * 최근 채팅 메시지 가져오기
 * @param {number} limit - 가져올 메시지 수
 * @returns {Promise} - 채팅 메시지 목록 Promise
 */
function getRecentChats(limit = 20) {
  return apiGet("getRecentChats", { limit });
}
