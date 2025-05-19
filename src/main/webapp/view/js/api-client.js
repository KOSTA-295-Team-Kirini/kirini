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
   * @param {string} endpoint API 엔드포인트 (예: 'login.do')
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
      if (response.status === 401 && endpoint !== '/auth/refresh.do' && endpoint !== 'login.do') {
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
   * 인증 만료 처리 (토큰 리프레시)
   * @param {string} originalEndpoint 실패한 요청의 엔드포인트
   * @param {Object} originalOptions 실패한 요청의 옵션
   * @returns {Promise} 새 토큰으로 재시도한 API 응답
   */
  static async handleUnauthorized(originalEndpoint, originalOptions) {
    try {
      // 토큰 갱신 요청
      const refreshToken = localStorage.getItem('kirini_refresh_token');
      if (!refreshToken) {
        throw new Error('리프레시 토큰 없음');
      }

      const refreshResponse = await fetch(`${API_CONFIG.baseUrl}/auth/refresh.do`, {
        method: 'POST',
        headers: API_CONFIG.defaultHeaders,
        body: JSON.stringify({ refreshToken })
      });

      if (!refreshResponse.ok) {
        throw new Error('토큰 갱신 실패');
      }

      const tokenData = await refreshResponse.json();
      ApiClient.setAuthToken(tokenData.token);
      localStorage.setItem('kirini_refresh_token', tokenData.refreshToken);
      
      // 기존 요청 재시도
      const newOptions = {
        ...originalOptions,
        headers: ApiClient.getAuthHeaders(originalOptions.headers)
      };
      
      return ApiClient.request(originalEndpoint, newOptions);
    } catch (error) {
      // 갱신 실패 시 로그아웃 처리
      ApiClient.removeAuthToken();
      localStorage.removeItem('kirini_refresh_token');
      window.dispatchEvent(new CustomEvent('auth:logout'));
      throw new Error('인증 만료: 다시 로그인 필요');
    }
  }

  /**
   * GET 요청
   * @param {string} endpoint API 엔드포인트
   * @param {Object} params URL 파라미터
   * @param {boolean} withAuth 인증 필요 여부
   * @returns {Promise} API 응답
   */
  static async get(endpoint, params = {}, withAuth = false) {
    const url = new URL(`${window.location.origin}${endpoint}`);
    Object.keys(params).forEach(key => url.searchParams.append(key, params[key]));
    
    const headers = withAuth ? ApiClient.getAuthHeaders() : {};
    
    return ApiClient.request(url.pathname + url.search, {
      method: 'GET',
      headers
    });
  }
  /**
   * POST 요청
   * @param {string} endpoint API 엔드포인트
   * @param {Object} data 요청 데이터
   * @param {boolean} withAuth 인증 필요 여부
   * @returns {Promise} API 응답
   */
  static async post(endpoint, data = {}, withAuth = false) {
    const headers = withAuth ? ApiClient.getAuthHeaders() : {};
    // Content-Type을 application/x-www-form-urlencoded로 변경
    headers['Content-Type'] = 'application/x-www-form-urlencoded';
    
    // URL 인코딩으로 데이터 변환
    const urlEncodedData = Object.keys(data)
      .map(key => encodeURIComponent(key) + '=' + encodeURIComponent(data[key]))
      .join('&');
    
    return ApiClient.request(endpoint, {
      method: 'POST',
      headers,
      body: urlEncodedData
    });
  }
  /**
   * POST 요청 (JSON 데이터)
   * @param {string} endpoint API 엔드포인트
   * @param {Object} data 요청 데이터
   * @param {boolean} withAuth 인증 필요 여부
   * @returns {Promise} API 응답
   */
  static async postJson(endpoint, data = {}, withAuth = false) {
    const headers = withAuth ? ApiClient.getAuthHeaders({'Content-Type': 'application/json'}) : {'Content-Type': 'application/json'};
    return ApiClient.request(endpoint, {
      method: 'POST',
      headers,
      body: JSON.stringify(data)
    });
  }

  /**
   * POST 요청 (FormData)
   * @param {string} endpoint API 엔드포인트
   * @param {FormData} formData 요청 데이터
   * @param {boolean} withAuth 인증 필요 여부
   * @returns {Promise} API 응답
   */
  static async postFormData(endpoint, formData, withAuth = false) {
    // FormData의 경우 Content-Type 헤더를 브라우저가 자동으로 설정하도록 비워둠
    const headers = withAuth ? ApiClient.getAuthHeaders({}) : {};
    return ApiClient.request(endpoint, {
      method: 'POST',
      headers,
      body: formData
    });
  }
  /**
   * PUT 요청
   * @param {string} endpoint API 엔드포인트
   * @param {Object} data 요청 데이터
   * @param {boolean} withAuth 인증 필요 여부
   * @returns {Promise} API 응답
   */
  static async put(endpoint, data = {}, withAuth = false) {
    const headers = withAuth ? ApiClient.getAuthHeaders() : {};
    // Content-Type을 application/x-www-form-urlencoded로 변경
    headers['Content-Type'] = 'application/x-www-form-urlencoded';
    
    // URL 인코딩으로 데이터 변환
    const urlEncodedData = Object.keys(data)
      .map(key => encodeURIComponent(key) + '=' + encodeURIComponent(data[key]))
      .join('&');
    
    return ApiClient.request(endpoint, {
      method: 'PUT',
      headers,
      body: urlEncodedData
    });
  }

  /**
   * DELETE 요청
   * @param {string} endpoint API 엔드포인트
   * @param {boolean} withAuth 인증 필요 여부
   * @returns {Promise} API 응답
   */
  static async delete(endpoint, withAuth = false) {
    const headers = withAuth ? ApiClient.getAuthHeaders() : {};
    
    return ApiClient.request(endpoint, {
      method: 'DELETE',
      headers
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
   * @returns {Promise} 로그인 결과
   */
  static async login(email, password) {
    return ApiClient.post('/login.do', { email, password });
  }
  /**
   * 회원가입 요청
   * @param {Object} userData 사용자 데이터
   * @returns {Promise} 회원가입 결과
   */
  static async register(userData) {
    return ApiClient.post('/register.do', userData);
  }
  /**
   * 이메일 중복 확인
   * @param {string} email 확인할 이메일
   * @returns {Promise} 중복 확인 결과
   */
  static async checkEmailDuplicate(email) {
    const response = await fetch(`/signup.do?action=checkEmail&email=${encodeURIComponent(email)}`);
    return await response.json();
  }
  /**
   * 닉네임 중복 확인
   * @param {string} nickname 확인할 닉네임
   * @returns {Promise} 중복 확인 결과
   */
  static async checkNicknameDuplicate(nickname) {
    const response = await fetch(`/signup.do?action=checkNickname&nickname=${encodeURIComponent(nickname)}`);
    return await response.json();
  }
  /**
   * 사용자 프로필 조회
   * @param {string} userId 사용자 ID (생략시 본인 프로필)
   * @returns {Promise} 사용자 프로필 정보
   */
  static async getProfile(userId) {
    const endpoint = userId ? `/profile.do?userId=${userId}` : '/profile.do';
    return ApiClient.get(endpoint, {}, true);
  }
  /**
   * 사용자 프로필 업데이트
   * @param {Object} profileData 업데이트할 프로필 데이터
   * @returns {Promise} 업데이트 결과
   */
  static async updateProfile(profileData) {
    return ApiClient.put('/profile.do', profileData, true);
  }
  /**
   * 비밀번호 변경
   * @param {string} currentPassword 현재 비밀번호
   * @param {string} newPassword 새 비밀번호
   * @returns {Promise} 변경 결과
   */
  static async changePassword(currentPassword, newPassword) {
    return ApiClient.post('/password.do', {
      currentPassword,
      newPassword
    }, true);
  }
  /**
   * 비밀번호 찾기 (이메일 발송)
   * @param {string} email 사용자 이메일
   * @returns {Promise} 처리 결과
   */
  static async forgotPassword(email) {
    return ApiClient.post('/forgot-password.do', { email });
  }

  /**
   * 로그아웃
   * @returns {void}
   */
  static logout() {
    ApiClient.removeAuthToken();
    localStorage.removeItem('kirini_refresh_token');
    window.dispatchEvent(new CustomEvent('auth:logout'));
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
   * 키보드 태그 제안하기
   * @param {string} keyboardId 키보드 ID
   * @param {string} tagName 제안할 태그 이름
   * @param {string} reason 제안 이유 (선택사항)
   * @returns {Promise} 제안 결과
   */
  static async suggestTag(keyboardId, tagName, reason = '') {
    return ApiClient.post('/keyboard.do', {
      action: 'suggestTag',
      keyboardId,
      tagName,
      reason
    }, true);
  }

  /**
   * 키보드 태그에 투표하기
   * @param {string} keyboardId 키보드 ID
   * @param {string} tagId 태그 ID
   * @param {string} voteType 투표 타입 ('up' 또는 'down')
   * @returns {Promise} 투표 결과
   */
  static async voteTag(keyboardId, tagId, voteType) {
    return ApiClient.post('/keyboard.do', {
      action: 'voteTag',
      keyboardId,
      tagId,
      voteType
    }, true);
  }

  /**
   * 키보드 평점 및 한줄평 등록
   * @param {string} keyboardId 키보드 ID
   * @param {number} scoreValue 평점 (1-5)
   * @param {string} review 한줄평 (선택사항)
   * @returns {Promise} 등록 결과
   */
  static async rateKeyboard(keyboardId, scoreValue, review = '') {
    return ApiClient.post('/keyboard.do', {
      action: 'addScore',
      keyboardId,
      scoreValue,
      review
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

/**
 * QnA 관련 API 서비스
 */
class QnaService {
  /**
   * 질문 생성
   * @param {FormData} formData - 질문 데이터 (FormData)
   * @returns {Promise} - 생성 결과
   */
  static async createQuestion(formData) {
    // FormData를 사용하는 경우 Content-Type 헤더를 브라우저가 자동으로 설정하게 함
    return ApiClient.request('/qna/create.do', {
      method: 'POST',
      body: formData,
      headers: ApiClient.getAuthHeaders({})
    });
  }

  /**
   * 답변 생성
   * @param {FormData} formData - 답변 데이터 (FormData)
   * @returns {Promise} - 생성 결과
   */
  static async createAnswer(formData) {
    return ApiClient.request('/qna/answer/create.do', {
      method: 'POST',
      body: formData,
      headers: ApiClient.getAuthHeaders({})
    });
  }

  /**
   * 질문에 좋아요 처리
   * @param {string} questionId - 질문 ID
   * @returns {Promise} - 좋아요 결과
   */
  static async likeQuestion(questionId) {
    return ApiClient.post('/qna/like.do', { questionId }, true);
  }
}

// 브라우저에서 사용할 수 있도록 전역 객체에 노출
window.ApiClient = ApiClient;
window.UserService = UserService;
window.KeyboardService = KeyboardService;
window.BoardService = BoardService;
window.GlossaryService = GlossaryService;
window.ReviewService = ReviewService;
window.QnaService = QnaService;
