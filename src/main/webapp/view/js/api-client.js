/**
 * Kirini API Client
 * 
 * 모든 API 요청을 처리하기 위한 재사용 가능한 클라이언트
 * Spring MVC/Struts에 맞게 .do 접미사 사용
 */

// 기본 API 구성
const API_CONFIG = {
  baseUrl: '', // 상대 경로 사용 (같은 도메인)
  defaultHeaders: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
};

// 토큰 스토리지 키
const TOKEN_STORAGE_KEY = 'kirini_auth_token';

/**
 * API 클라이언트 클래스
 */
class ApiClient {
  /**
   * 인증 토큰 가져오기
   * @returns {string|null} 저장된 토큰
   */
  static getAuthToken() {
    return localStorage.getItem(TOKEN_STORAGE_KEY);
  }

  /**
   * 인증 토큰 저장
   * @param {string} token 저장할 토큰
   */
  static setAuthToken(token) {
    if (token) {
      localStorage.setItem(TOKEN_STORAGE_KEY, token);
    } else {
      localStorage.removeItem(TOKEN_STORAGE_KEY);
    }
  }

  /**
   * 인증 토큰 삭제 (로그아웃)
   */
  static removeAuthToken() {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
  }
  
  /**
   * API 요청 처리를 위한 기본 fetch 래퍼 함수
   * @param {string} endpoint - API 엔드포인트 경로
   * @param {Object} options - fetch 옵션
   * @returns {Promise} - API 응답 Promise
   */
  static async fetch(endpoint, options = {}) {
    // .do 접미사 추가 (이미 있는 경우 제외)
    let url = endpoint;
    if (!url.endsWith('.do')) {
      url = `${url}.do`;
    }
    
    // 기본 헤더와 사용자 제공 헤더 병합
    const headers = {
      ...API_CONFIG.defaultHeaders,
      ...(options.headers || {})
    };
    
    // 인증 토큰이 있는 경우 Authorization 헤더 추가
    const token = this.getAuthToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    
    // fetch 옵션 구성
    const fetchOptions = {
      ...options,
      headers
    };
    
    try {
      // API 요청 실행
      const response = await fetch(url, fetchOptions);
      
      // HTTP 에러 처리
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw {
          status: response.status,
          statusText: response.statusText,
          data: errorData,
          message: errorData.message || `HTTP error ${response.status}: ${response.statusText}`
        };
      }
      
      // 응답 데이터가 없는 경우 (204 No Content)
      if (response.status === 204) {
        return { status: 'success' };
      }
      
      // JSON 응답 파싱 및 반환
      const data = await response.json();
      return data;
    } catch (error) {
      // 네트워크 오류나 JSON 파싱 오류 처리
      console.error('API 요청 오류:', error);
      throw error;
    }
  }
  
  /**
   * GET 요청 헬퍼 함수
   * @param {string} endpoint - API 엔드포인트
   * @param {Object} options - 추가 fetch 옵션
   * @returns {Promise} - API 응답
   */
  static async get(endpoint, options = {}) {
    return this.fetch(endpoint, { 
      ...options, 
      method: 'GET' 
    });
  }
  
  /**
   * POST 요청 헬퍼 함수
   * @param {string} endpoint - API 엔드포인트
   * @param {Object} data - 요청 본문 데이터
   * @param {Object} options - 추가 fetch 옵션
   * @returns {Promise} - API 응답
   */
  static async post(endpoint, data, options = {}) {
    return this.fetch(endpoint, {
      ...options,
      method: 'POST',
      body: JSON.stringify(data)
    });
  }
  
  /**
   * PUT 요청 헬퍼 함수
   * @param {string} endpoint - API 엔드포인트
   * @param {Object} data - 요청 본문 데이터
   * @param {Object} options - 추가 fetch 옵션
   * @returns {Promise} - API 응답
   */
  static async put(endpoint, data, options = {}) {
    return this.fetch(endpoint, {
      ...options,
      method: 'PUT',
      body: JSON.stringify(data)
    });
  }
  
  /**
   * DELETE 요청 헬퍼 함수
   * @param {string} endpoint - API 엔드포인트
   * @param {Object} options - 추가 fetch 옵션
   * @returns {Promise} - API 응답
   */
  static async delete(endpoint, options = {}) {
    return this.fetch(endpoint, {
      ...options,
      method: 'DELETE'
    });
  }
}

/**
 * 사용자 관련 API 서비스
 */
class UserService {
  /**
   * 로그인 요청
   * @param {string} email 사용자 이메일
   * @param {string} password 비밀번호
   * @param {boolean} rememberMe 로그인 유지 여부
   * @returns {Promise} 로그인 결과
   */
  static async login(email, password, rememberMe = false) {
    return ApiClient.post('/login', { email, password, rememberMe });
  }

  /**
   * 로그아웃 요청
   * @returns {Promise} 로그아웃 결과
   */
  static async logout() {
    // 토큰 제거
    ApiClient.removeAuthToken();
    // 서버에 로그아웃 요청
    return ApiClient.post('/logout', {});
  }

  /**
   * 회원가입 요청
   * @param {Object} userData 사용자 데이터
   * @returns {Promise} 회원가입 결과
   */
  static async register(userData) {
    return ApiClient.post('/signup', userData);
  }

  /**
   * 이메일 중복 확인
   * @param {string} email 확인할 이메일
   * @returns {Promise} 중복 확인 결과
   */
  static async checkEmailDuplicate(email) {
    return ApiClient.get(`/signup?action=checkEmail&email=${encodeURIComponent(email)}`);
  }
  
  /**
   * 닉네임 중복 확인
   * @param {string} nickname 확인할 닉네임
   * @returns {Promise} 중복 확인 결과
   */
  static async checkNicknameDuplicate(nickname) {
    return ApiClient.get(`/signup?action=checkNickname&nickname=${encodeURIComponent(nickname)}`);
  }
  
  /**
   * 사용자 프로필 조회
   * @param {number} userId 선택적 사용자 ID (생략 시 현재 사용자)
   * @returns {Promise} 사용자 프로필 정보
   */
  static async getProfile(userId = null) {
    const endpoint = userId ? `/profile?id=${userId}` : '/profile';
    return ApiClient.get(endpoint);
  }
  
  /**
   * 사용자 프로필 업데이트
   * @param {Object} profileData 업데이트할 프로필 데이터
   * @returns {Promise} 업데이트 결과
   */
  static async updateProfile(profileData) {
    return ApiClient.post('/profile', profileData);
  }
  
  /**
   * 비밀번호 변경
   * @param {string} currentPassword 현재 비밀번호
   * @param {string} newPassword 새 비밀번호
   * @returns {Promise} 비밀번호 변경 결과
   */
  static async changePassword(currentPassword, newPassword) {
    return ApiClient.post('/profile', { 
      action: 'changePassword',
      currentPassword,
      newPassword
    });
  }
  
  /**
   * 비밀번호 찾기/재설정 요청
   * @param {string} email 사용자 이메일
   * @returns {Promise} 재설정 요청 결과
   */
  static async requestPasswordReset(email) {
    return ApiClient.post('/password', {
      action: 'resetRequest',
      email
    });
  }
}

/**
 * 키보드 관련 API 서비스
 */
class KeyboardService {
  /**
   * 키보드 목록 조회
   * @param {Object} params 검색/필터/정렬/페이징 파라미터
   * @returns {Promise} 키보드 목록 및 페이징 정보
   */
  static async getKeyboards(params = {}) {
    return ApiClient.get('/keyboard/list.do', params);
  }

  /**
   * 키보드 상세 정보 조회
   * @param {string} keyboardId 키보드 ID
   * @returns {Promise} 키보드 상세 정보
   */
  static async getKeyboardDetails(keyboardId) {
    return ApiClient.get(`/keyboard/detail.do`, { id: keyboardId });
  }

  /**
   * 키보드 검색
   * @param {string} query 검색어
   * @param {Object} filters 필터 옵션
   * @param {Object} sorting 정렬 옵션
   * @param {Object} pagination 페이징 옵션
   * @returns {Promise} 검색 결과 및 페이징 정보
   */
  static async searchKeyboards(query, filters = {}, sorting = {}, pagination = {}) {
    return ApiClient.get('/keyboard/search.do', {
      query,
      ...filters,
      ...sorting,
      ...pagination
    });
  }

  /**
   * 인기 키보드 조회
   * @param {number} limit 조회할 개수
   * @returns {Promise} 인기 키보드 목록
   */
  static async getPopularKeyboards(limit = 10) {
    return ApiClient.get('/keyboard/popular.do', { limit });
  }

  /**
   * 키보드 평점 제출
   * @param {string} keyboardId 키보드 ID
   * @param {number} rating 평점 (1-5)
   * @returns {Promise} 제출 결과
   */
  static async rateKeyboard(keyboardId, rating) {
    return ApiClient.post('/keyboard/rate.do', {
      keyboardId,
      rating
    }, true);
  }

  /**
   * 관련 키보드 조회
   * @param {string} keyboardId 기준 키보드 ID
   * @param {number} limit 조회할 개수
   * @returns {Promise} 관련 키보드 목록
   */
  static async getRelatedKeyboards(keyboardId, limit = 5) {
    return ApiClient.get('/keyboard/related.do', {
      id: keyboardId,
      limit
    });
  }
}

/**
 * 게시판 관련 API 서비스
 */
class BoardService {
  /**
   * 게시물 목록 조회
   * @param {string} boardType 게시판 유형 (free, qna, notice 등)
   * @param {Object} params 페이징 및 정렬 옵션
   * @returns {Promise} 게시물 목록
   */
  static async getPosts(boardType, params = {}) {
    return ApiClient.get(`/${boardType}/list.do`, params);
  }

  /**
   * 게시물 상세 조회
   * @param {string} boardType 게시판 유형
   * @param {string} postId 게시물 ID
   * @returns {Promise} 게시물 상세 정보
   */
  static async getPost(boardType, postId) {
    return ApiClient.get(`/${boardType}/detail.do`, { id: postId });
  }

  /**
   * 게시물 작성
   * @param {string} boardType 게시판 유형
   * @param {Object} postData 게시물 데이터
   * @returns {Promise} 작성 결과
   */
  static async createPost(boardType, postData) {
    return ApiClient.post(`/${boardType}/create.do`, postData, true);
  }

  /**
   * 게시물 수정
   * @param {string} boardType 게시판 유형
   * @param {string} postId 게시물 ID
   * @param {Object} postData 수정할 데이터
   * @returns {Promise} 수정 결과
   */
  static async updatePost(boardType, postId, postData) {
    return ApiClient.put(`/${boardType}/update.do`, {
      id: postId,
      ...postData
    }, true);
  }

  /**
   * 게시물 삭제
   * @param {string} boardType 게시판 유형
   * @param {string} postId 게시물 ID
   * @returns {Promise} 삭제 결과
   */
  static async deletePost(boardType, postId) {
    return ApiClient.post(`/${boardType}/delete.do`, { id: postId }, true);
  }

  /**
   * 댓글 작성
   * @param {string} boardType 게시판 유형
   * @param {string} postId 게시물 ID
   * @param {string} content 댓글 내용
   * @param {string} parentId 부모 댓글 ID (답글인 경우)
   * @returns {Promise} 작성 결과
   */
  static async createComment(boardType, postId, content, parentId = null) {
    return ApiClient.post(`/${boardType}/comment/create.do`, {
      postId,
      content,
      parentId
    }, true);
  }

  /**
   * 댓글 목록 조회
   * @param {string} boardType 게시판 유형
   * @param {string} postId 게시물 ID
   * @param {Object} params 페이징 옵션
   * @returns {Promise} 댓글 목록
   */
  static async getComments(boardType, postId, params = {}) {
    return ApiClient.get(`/${boardType}/comment/list.do`, {
      postId,
      ...params
    });
  }

  /**
   * 게시물 좋아요/싫어요
   * @param {string} boardType 게시판 유형
   * @param {string} postId 게시물 ID
   * @param {string} type 'like' 또는 'dislike'
   * @returns {Promise} 처리 결과
   */
  static async reactToPost(boardType, postId, type) {
    return ApiClient.post(`/${boardType}/react.do`, {
      postId,
      type
    }, true);
  }
}

/**
 * 용어사전 관련 API 서비스
 */
class GlossaryService {
  /**
   * 용어 목록 조회
   * @param {Object} params 필터 및 페이징 옵션
   * @returns {Promise} 용어 목록
   */
  static async getTerms(params = {}) {
    return ApiClient.get('/glossary/list.do', params);
  }

  /**
   * 용어 상세 조회
   * @param {string} termId 용어 ID
   * @returns {Promise} 용어 상세 정보
   */
  static async getTerm(termId) {
    return ApiClient.get('/glossary/detail.do', { id: termId });
  }

  /**
   * 용어 검색
   * @param {string} query 검색어
   * @param {Object} filters 필터 옵션
   * @returns {Promise} 검색 결과
   */
  static async searchTerms(query, filters = {}) {
    return ApiClient.get('/glossary/search.do', {
      query,
      ...filters
    });
  }

  /**
   * 용어 카테고리 목록 조회
   * @returns {Promise} 카테고리 목록
   */
  static async getCategories() {
    return ApiClient.get('/glossary/categories.do');
  }
}

/**
 * 리뷰 관련 API 서비스
 */
class ReviewService {
  /**
   * 리뷰 작성
   * @param {string} keyboardId 키보드 ID
   * @param {Object} reviewData 리뷰 데이터
   * @returns {Promise} 작성 결과
   */
  static async createReview(keyboardId, reviewData) {
    return ApiClient.post('/review/create.do', {
      keyboardId,
      ...reviewData
    }, true);
  }

  /**
   * 리뷰 목록 조회
   * @param {string} keyboardId 키보드 ID
   * @param {Object} params 정렬 및 페이징 옵션
   * @returns {Promise} 리뷰 목록
   */
  static async getReviews(keyboardId, params = {}) {
    return ApiClient.get('/review/list.do', {
      keyboardId,
      ...params
    });
  }

  /**
   * 리뷰 도움됨/안됨 평가
   * @param {string} reviewId 리뷰 ID
   * @param {boolean} helpful 도움 여부
   * @returns {Promise} 처리 결과
   */
  static async rateReviewHelpfulness(reviewId, helpful) {
    return ApiClient.post('/review/helpful.do', {
      reviewId,
      helpful
    }, true);
  }
}

// 브라우저에서 사용할 수 있도록 전역 객체에 노출
window.ApiClient = ApiClient;
window.UserService = UserService;
window.KeyboardService = KeyboardService;
window.BoardService = BoardService;
window.GlossaryService = GlossaryService;
window.ReviewService = ReviewService;
