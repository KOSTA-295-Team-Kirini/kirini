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
const REFRESH_TOKEN_KEY = 'kirini_refresh_token';

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
    localStorage.removeItem(REFRESH_TOKEN_KEY);
  }

  /**
   * 리프레시 토큰 가져오기
   * @returns {string|null} 저장된 리프레시 토큰
   */
  static getRefreshToken() {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  /**
   * 리프레시 토큰 저장
   * @param {string} token 저장할 리프레시 토큰
   */
  static setRefreshToken(token) {
    if (token) {
      localStorage.setItem(REFRESH_TOKEN_KEY, token);
    } else {
      localStorage.removeItem(REFRESH_TOKEN_KEY);
    }
  }

  /**
   * 헤더에 인증 토큰 추가
   * @param {Object} headers 기존 헤더
   * @returns {Object} 인증 토큰이 추가된 헤더
   */
  static getAuthHeaders(headers = {}) {
    const token = ApiClient.getAuthToken();
    return token ? { ...headers, 'Authorization': `Bearer ${token}` } : headers;
  }

  /**
   * API 요청 생성
   * @param {string} endpoint API 엔드포인트 (예: '/user/login.do')
   * @param {Object} options fetch 옵션
   * @returns {Promise} API 응답
   */
  static async request(endpoint, options = {}) {
    const url = `${API_CONFIG.baseUrl}${endpoint}`;
    const headers = {
      ...API_CONFIG.defaultHeaders,
      ...options.headers
    };

    const config = {
      ...options,
      headers
    };

    try {
      const response = await fetch(url, config);
      
      // 토큰 리프레시 로직 (401 응답 처리)
      if (response.status === 401 && endpoint !== '/auth/refresh.do' && endpoint !== '/user/login.do') {
        return ApiClient.handleUnauthorized(endpoint, options);
      }
      
      // JSON 응답 반환 또는 에러 처리
      if (response.ok) {
        if (response.headers.get('content-type')?.includes('application/json')) {
          return await response.json();
        }
        return await response.text();
      }
      
      throw new Error(`API 요청 실패: ${response.status}`);
    } catch (error) {
      console.error('API 요청 오류:', error);
      throw error;
    }
  }

  /**
   * 401 Unauthorized 응답 처리 및 토큰 갱신
   * @param {string} originalEndpoint 원본 요청 엔드포인트
   * @param {Object} originalOptions 원본 요청 옵션
   * @returns {Promise} 재요청 결과
   */
  static async handleUnauthorized(originalEndpoint, originalOptions) {
    try {
      const refreshToken = ApiClient.getRefreshToken();
      if (!refreshToken) {
        // 리프레시 토큰이 없으면 로그인 페이지로 이동
        window.location.href = '/view/login.html';
        throw new Error('인증이 필요합니다.');
      }

      // 리프레시 토큰으로 새 접근 토큰 요청
      const refreshResult = await fetch(`${API_CONFIG.baseUrl}/auth/refresh.do`, {
        method: 'POST',
        headers: API_CONFIG.defaultHeaders,
        body: JSON.stringify({ refreshToken })
      });

      if (refreshResult.ok) {
        const tokenData = await refreshResult.json();
        
        // 새 토큰 저장
        ApiClient.setAuthToken(tokenData.accessToken);
        if (tokenData.refreshToken) {
          ApiClient.setRefreshToken(tokenData.refreshToken);
        }

        // 원본 요청 재시도 (새 토큰으로)
        const newHeaders = {
          ...originalOptions.headers,
          'Authorization': `Bearer ${tokenData.accessToken}`
        };

        // 원본 요청 재시도
        return ApiClient.request(originalEndpoint, {
          ...originalOptions,
          headers: newHeaders
        });
      } else {
        // 리프레시 실패 - 로그아웃 처리
        ApiClient.removeAuthToken();
        window.location.href = '/view/login.html';
        throw new Error('세션이 만료되었습니다.');
      }
    } catch (error) {
      console.error('토큰 갱신 오류:', error);
      throw error;
    }
  }

  /**
   * GET 요청
   * @param {string} endpoint API 엔드포인트
   * @param {Object} params URL 쿼리 매개변수
   * @param {boolean} auth 인증 필요 여부
   * @returns {Promise} API 응답
   */
  static async get(endpoint, params = {}, auth = false) {
    // URL 파라미터 추가
    const queryParams = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        queryParams.append(key, value);
      }
    });
    
    const queryString = queryParams.toString();
    const url = queryString ? `${endpoint}?${queryString}` : endpoint;
    
    // 인증 헤더 추가
    const headers = auth ? ApiClient.getAuthHeaders() : {};
    
    return ApiClient.request(url, {
      method: 'GET',
      headers
    });
  }

  /**
   * POST 요청
   * @param {string} endpoint API 엔드포인트
   * @param {Object} data 요청 본문 데이터
   * @param {boolean} auth 인증 필요 여부
   * @returns {Promise} API 응답
   */
  static async post(endpoint, data = {}, auth = false) {
    const headers = auth ? ApiClient.getAuthHeaders() : {};
    
    return ApiClient.request(endpoint, {
      method: 'POST',
      headers,
      body: JSON.stringify(data)
    });
  }

  /**
   * PUT 요청
   * @param {string} endpoint API 엔드포인트
   * @param {Object} data 요청 본문 데이터
   * @param {boolean} auth 인증 필요 여부
   * @returns {Promise} API 응답
   */
  static async put(endpoint, data = {}, auth = false) {
    const headers = auth ? ApiClient.getAuthHeaders() : {};
    
    return ApiClient.request(endpoint, {
      method: 'PUT',
      headers,
      body: JSON.stringify(data)
    });
  }

  /**
   * DELETE 요청
   * @param {string} endpoint API 엔드포인트
   * @param {Object} params URL 쿼리 매개변수
   * @param {boolean} auth 인증 필요 여부
   * @returns {Promise} API 응답
   */
  static async delete(endpoint, params = {}, auth = false) {
    // URL 파라미터 추가
    const queryParams = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        queryParams.append(key, value);
      }
    });
    
    const queryString = queryParams.toString();
    const url = queryString ? `${endpoint}?${queryString}` : endpoint;
    
    // 인증 헤더 추가
    const headers = auth ? ApiClient.getAuthHeaders() : {};
    
    return ApiClient.request(url, {
      method: 'DELETE',
      headers
    });
  }

  /**
   * 파일 업로드 요청
   * @param {string} endpoint API 엔드포인트
   * @param {FormData} formData 폼 데이터 (파일 포함)
   * @param {boolean} auth 인증 필요 여부
   * @returns {Promise} API 응답
   */
  static async uploadFile(endpoint, formData, auth = false) {
    // 파일 업로드는 Content-Type 헤더를 자동으로 설정
    const headers = auth ? ApiClient.getAuthHeaders({}) : {};
    delete headers['Content-Type']; // FormData에서는 Content-Type을 설정하지 않음
    
    return ApiClient.request(endpoint, {
      method: 'POST',
      headers,
      body: formData
    });
  }
}

// API 서비스 모듈 - 키보드 관련 API
class KeyboardService {
  /**
   * 키보드 목록 조회
   * @param {Object} params 검색 및 페이징 파라미터
   * @returns {Promise} 키보드 목록
   */
  static async getKeyboards(params = {}) {
    return ApiClient.get('/keyboard/list.do', params);
  }

  /**
   * 키보드 상세 정보 조회
   * @param {number} keyboardId 키보드 ID
   * @returns {Promise} 키보드 상세 정보
   */
  static async getKeyboardDetail(keyboardId) {
    return ApiClient.get(`/keyboard/detail.do`, { keyboardId });
  }

  /**
   * 키보드 리뷰 목록 조회
   * @param {number} keyboardId 키보드 ID
   * @param {Object} params 페이징 파라미터
   * @returns {Promise} 리뷰 목록
   */
  static async getKeyboardReviews(keyboardId, params = {}) {
    return ApiClient.get(`/keyboard/reviews.do`, { keyboardId, ...params });
  }

  /**
   * 키보드 리뷰 작성
   * @param {number} keyboardId 키보드 ID
   * @param {Object} reviewData 리뷰 데이터
   * @returns {Promise} 작성 결과
   */
  static async addReview(keyboardId, reviewData) {
    return ApiClient.post(`/keyboard/review/add.do`, { keyboardId, ...reviewData }, true);
  }

  /**
   * 키보드 태그 추가
   * @param {number} keyboardId 키보드 ID
   * @param {string} tag 태그 문자열
   * @returns {Promise} 추가 결과
   */
  static async addTag(keyboardId, tag) {
    return ApiClient.post(`/keyboard/tag/add.do`, { keyboardId, tag }, true);
  }
}

// API 서비스 모듈 - 사용자 관련 API
class UserService {
  /**
   * 로그인 요청
   * @param {string} username 사용자명
   * @param {string} password 비밀번호
   * @returns {Promise} 로그인 결과 및 토큰
   */
  static async login(username, password) {
    const result = await ApiClient.post('/user/login.do', { username, password });
    
    // 토큰 저장
    if (result.accessToken) {
      ApiClient.setAuthToken(result.accessToken);
      if (result.refreshToken) {
        ApiClient.setRefreshToken(result.refreshToken);
      }
    }
    
    return result;
  }

  /**
   * 회원가입 요청
   * @param {Object} userData 사용자 데이터
   * @returns {Promise} 회원가입 결과
   */
  static async register(userData) {
    return ApiClient.post('/user/register.do', userData);
  }

  /**
   * 로그아웃 처리
   */
  static async logout() {
    // 서버에 로그아웃 요청 (토큰 무효화)
    try {
      await ApiClient.post('/user/logout.do', {}, true);
    } catch (error) {
      console.warn('로그아웃 요청 실패:', error);
    }
    
    // 로컬 토큰 삭제
    ApiClient.removeAuthToken();
  }

  /**
   * 사용자 프로필 조회
   * @returns {Promise} 사용자 프로필 정보
   */
  static async getProfile() {
    return ApiClient.get('/user/profile.do', {}, true);
  }

  /**
   * 사용자 프로필 업데이트
   * @param {Object} profileData 프로필 데이터
   * @returns {Promise} 업데이트 결과
   */
  static async updateProfile(profileData) {
    return ApiClient.put('/user/profile/update.do', profileData, true);
  }

  /**
   * 비밀번호 변경
   * @param {string} currentPassword 현재 비밀번호
   * @param {string} newPassword 새 비밀번호
   * @returns {Promise} 변경 결과
   */
  static async changePassword(currentPassword, newPassword) {
    return ApiClient.post('/user/password/change.do', { currentPassword, newPassword }, true);
  }

  /**
   * 비밀번호 재설정 요청 (비밀번호 찾기)
   * @param {string} email 이메일
   * @returns {Promise} 요청 결과
   */
  static async requestPasswordReset(email) {
    return ApiClient.post('/user/password/reset-request.do', { email });
  }
}

// API 서비스 모듈 - 게시판 관련 API
class BoardService {
  /**
   * 게시글 목록 조회
   * @param {string} boardType 게시판 타입 (free, qna, news)
   * @param {Object} params 검색 및 페이징 파라미터
   * @returns {Promise} 게시글 목록
   */
  static async getPosts(boardType, params = {}) {
    return ApiClient.get(`/board/${boardType}/list.do`, params);
  }

  /**
   * 게시글 상세 조회
   * @param {string} boardType 게시판 타입
   * @param {number} postId 게시글 ID
   * @returns {Promise} 게시글 상세 정보
   */
  static async getPostDetail(boardType, postId) {
    return ApiClient.get(`/board/${boardType}/detail.do`, { postId });
  }

  /**
   * 게시글 작성
   * @param {string} boardType 게시판 타입
   * @param {Object} postData 게시글 데이터
   * @returns {Promise} 작성 결과
   */
  static async createPost(boardType, postData) {
    return ApiClient.post(`/board/${boardType}/write.do`, postData, true);
  }

  /**
   * 게시글 수정
   * @param {string} boardType 게시판 타입
   * @param {number} postId 게시글 ID
   * @param {Object} postData 게시글 데이터
   * @returns {Promise} 수정 결과
   */
  static async updatePost(boardType, postId, postData) {
    return ApiClient.put(`/board/${boardType}/update.do`, { postId, ...postData }, true);
  }

  /**
   * 게시글 삭제
   * @param {string} boardType 게시판 타입
   * @param {number} postId 게시글 ID
   * @returns {Promise} 삭제 결과
   */
  static async deletePost(boardType, postId) {
    return ApiClient.delete(`/board/${boardType}/delete.do`, { postId }, true);
  }

  /**
   * 댓글 작성
   * @param {string} boardType 게시판 타입
   * @param {number} postId 게시글 ID
   * @param {Object} commentData 댓글 데이터
   * @returns {Promise} 작성 결과
   */
  static async addComment(boardType, postId, commentData) {
    return ApiClient.post(`/board/${boardType}/comment/add.do`, { postId, ...commentData }, true);
  }

  /**
   * 댓글 목록 조회
   * @param {string} boardType 게시판 타입
   * @param {number} postId 게시글 ID
   * @param {Object} params 페이징 파라미터
   * @returns {Promise} 댓글 목록
   */
  static async getComments(boardType, postId, params = {}) {
    return ApiClient.get(`/board/${boardType}/comments.do`, { postId, ...params });
  }
}

// API 서비스 모듈 - 용어 사전 관련 API
class GlossaryService {
  /**
   * 용어 목록 조회
   * @param {Object} params 검색 및 필터 파라미터
   * @returns {Promise} 용어 목록
   */
  static async getTerms(params = {}) {
    return ApiClient.get('/glossary/list.do', params);
  }

  /**
   * 용어 상세 조회
   * @param {number} termId 용어 ID
   * @returns {Promise} 용어 상세 정보
   */
  static async getTermDetail(termId) {
    return ApiClient.get('/glossary/detail.do', { termId });
  }

  /**
   * 용어 카테고리 목록 조회
   * @returns {Promise} 카테고리 목록
   */
  static async getCategories() {
    return ApiClient.get('/glossary/categories.do');
  }

  /**
   * 용어 검색
   * @param {string} keyword 검색 키워드
   * @returns {Promise} 검색 결과
   */
  static async searchTerms(keyword) {
    return ApiClient.get('/glossary/search.do', { keyword });
  }

  /**
   * 알파벳 인덱스별 용어 조회
   * @param {string} letter 알파벳(A-Z) 또는 기타(#)
   * @returns {Promise} 해당 알파벳으로 시작하는 용어 목록
   */
  static async getTermsByLetter(letter) {
    return ApiClient.get('/glossary/by-letter.do', { letter });
  }
  
  /**
   * 관련 용어 조회
   * @param {number} termId 용어 ID
   * @returns {Promise} 관련 용어 목록
   */
  static async getRelatedTerms(termId) {
    return ApiClient.get('/glossary/related.do', { termId });
  }
  
  /**
   * 인기 용어 조회
   * @param {number} limit 조회 개수
   * @returns {Promise} 인기 용어 목록
   */
  static async getPopularTerms(limit = 10) {
    return ApiClient.get('/glossary/popular.do', { limit });
  }
}

// 서비스 내보내기
window.ApiClient = ApiClient;
window.KeyboardService = KeyboardService;
window.UserService = UserService;
window.BoardService = BoardService;
window.GlossaryService = GlossaryService;
