/**
 * Kirini API Client
 *
 * 모든 API 요청을 처리하기 위한 재사용 가능한 클라이언트
 * Spring MVC/Struts에 맞게 .do 접미사 사용
 */

// 기본 API 구성
const API_CONFIG = {
  baseUrl: "", // 상대 경로 사용 (같은 도메인)
  defaultHeaders: {
    "Content-Type": "application/json",
    Accept: "application/json",
  },
};

// 토큰 스토리지 키
const TOKEN_STORAGE_KEY = "kirini_auth_token";

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
  static getAuthHeaders(headers = {}) {
    const token = ApiClient.getAuthToken();
    return token ? { ...headers, Authorization: `Bearer ${token}` } : headers;
  }
  /**
   * API 요청 생성
   * @param {string} endpoint API 엔드포인트 (예: 'login.do')
   * @param {Object} options fetch 옵션
   * @returns {Promise} API 응답
   */ static async request(endpoint, options = {}) {
    const url = `${API_CONFIG.baseUrl}${endpoint}`;
    const headers = {
      ...API_CONFIG.defaultHeaders,
      ...(options.headers || {}),
    };

    // 인증 토큰이 있고 endpoint가 인증이 필요한 엔드포인트인 경우에만 토큰 추가
    // news, freeboard 관련 조회는 인증을 요구하지 않도록 변경
    const token = this.getAuthToken();
    const isPublicEndpoint =
      endpoint.includes("/news/list") ||
      endpoint.includes("/news/view") ||
      endpoint.includes("/news/comments") ||
      endpoint.includes("/freeboard/list") ||
      endpoint.includes("/freeboard/view") ||
      endpoint.includes("/freeboard/comments");

    // 인증 디버깅
    console.log(
      `API 요청: ${endpoint}, 공개 엔드포인트: ${isPublicEndpoint}, 토큰 있음: ${!!token}`
    );
    console.log("[ApiClient] 현재 localStorage 토큰:", ApiClient.getAuthToken()); // localStorage 토큰 상태 확인
    console.log("[ApiClient] 현재 document.cookie:", document.cookie); // 세션 쿠키 확인용 로그

    if (token && !isPublicEndpoint) {
      headers["Authorization"] = `Bearer ${token}`;
    }

    // fetch 옵션 구성
    const fetchOptions = {
      ...options,
      headers,
      credentials: "include", // 세션 쿠키를 자동으로 포함시킵니다
    };

    try {
      // API 요청 실행
      const response = await fetch(url, fetchOptions);

      // 토큰 리프레시 로직 (401 응답 처리)
      if (
        response.status === 401 &&
        endpoint !== "/auth/refresh.do" &&
        endpoint !== "login.do"
      ) {
        // 401 Unauthorized 에러 처리 - 토큰 만료 등 처리 로직이 구현될 예정
        console.warn(
          "인증 토큰이 만료되었거나 유효하지 않습니다. 재로그인이 필요합니다."
        );
        // 로그아웃 처리
        ApiClient.removeAuthToken();
        return {
          status: "error",
          message: "인증이 만료되었습니다. 다시 로그인해주세요.",
        };
      }

      // 응답 데이터가 없는 경우 (204 No Content)
      if (response.status === 204) {
        return { status: "success" };
      }

      // JSON 응답 파싱 및 반환
      const data = await response.json();
      return data;
    } catch (error) {
      // 네트워크 오류나 JSON 파싱 오류 처리
      console.error("API 요청 오류:", error);
      throw error;
    }
  }
  /**
   * GET 요청 헬퍼 함수
   * @param {string} endpoint - API 엔드포인트
   * @param {Object} options - 추가 fetch 옵션
   * @returns {Promise} - API 응답
   */ static async get(endpoint, params = {}, withAuth = false) {
    const queryParams = new URLSearchParams();

    for (const key in params) {
      if (params[key] !== undefined && params[key] !== null) {
        queryParams.append(key, String(params[key]));
      }
    }

    const queryString = queryParams.toString();
    const url = queryString ? `${endpoint}?${queryString}` : endpoint;

    console.log(`API GET 요청: ${url}, 파라미터:`, params);

    const headers = withAuth ? ApiClient.getAuthHeaders() : {};

    return ApiClient.request(url, {
      method: "GET",
      headers: headers,
    });
  }
  /**
   * POST 요청 헬퍼 함수
   * @param {string} endpoint - API 엔드포인트
   * @param {Object} data - 요청 본문 데이터
   * @param {Object} options - 추가 fetch 옵션
   * @returns {Promise} - API 응답
   */ static async post(endpoint, data = {}, withAuth = false) {
    const headers = withAuth ? ApiClient.getAuthHeaders() : {};
    // Content-Type을 application/x-www-form-urlencoded로 변경
    headers["Content-Type"] = "application/x-www-form-urlencoded";

    // URL 인코딩으로 데이터 변환
    const urlEncodedData = Object.keys(data)
      .map(
        (key) => encodeURIComponent(key) + "=" + encodeURIComponent(data[key])
      )
      .join("&");

    return ApiClient.request(endpoint, {
      method: "POST",
      headers: headers,
      body: urlEncodedData,
    });
  }
  /**
   * POST 요청 (JSON 데이터)
   * @param {string} endpoint API 엔드포인트
   * @param {Object} data 요청 데이터
   * @param {boolean} withAuth 인증 필요 여부
   * @returns {Promise} API 응답
   */ static async postJson(endpoint, data = {}, withAuth = false) {
    const headers = withAuth
      ? ApiClient.getAuthHeaders({ "Content-Type": "application/json" })
      : { "Content-Type": "application/json" };
    return ApiClient.request(endpoint, {
      method: "POST",
      headers: headers,
      body: JSON.stringify(data),
    });
  }

  /**
   * POST 요청 (FormData)
   * @param {string} endpoint API 엔드포인트
   * @param {FormData} formData 요청 데이터
   * @param {boolean} withAuth 인증 필요 여부
   * @returns {Promise} API 응답
   */ static async postFormData(endpoint, formData, withAuth = false) {
    // FormData의 경우 Content-Type 헤더를 브라우저가 자동으로 설정하도록 비워둠
    const headers = withAuth ? ApiClient.getAuthHeaders({}) : {};
    return ApiClient.request(endpoint, {
      method: "POST",
      headers: headers,
      body: formData,
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
    headers["Content-Type"] = "application/x-www-form-urlencoded";

    // URL 인코딩으로 데이터 변환
    const urlEncodedData = Object.keys(data)
      .map(
        (key) => encodeURIComponent(key) + "=" + encodeURIComponent(data[key])
      )
      .join("&");
    return ApiClient.request(endpoint, {
      method: "PUT",
      headers: headers,
      body: urlEncodedData,
    });
  }
  /**
   * DELETE 요청 헬퍼 함수
   * @param {string} endpoint - API 엔드포인트
   * @param {Object} options - 추가 fetch 옵션
   * @returns {Promise} - API 응답
   */
  static async delete(endpoint, options = {}) {
    return ApiClient.request(endpoint, {
      ...options,
      method: "DELETE",
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
   */ static async login(email, password) {
    try {
      const response = await fetch("/login.do", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include", // 세션 쿠키를 포함
        body: JSON.stringify({ email, password }),
      });

      return await response.json();
    } catch (error) {
      console.error("로그인 요청 오류:", error);
      return {
        success: false,
        message: "로그인 처리 중 오류가 발생했습니다.",
      };
    }
  }
  /**
   * 회원가입 요청
   * @param {Object} userData 사용자 데이터
   * @returns {Promise} 회원가입 결과
   */
  static async register(userData) {
    return ApiClient.post("/register.do", userData);
  }
  /**
   * 이메일 중복 확인
   * @param {string} email 확인할 이메일
   * @returns {Promise} 중복 확인 결과
   */ static async checkEmailDuplicate(email) {
    const response = await fetch(
      `/signup.do?action=checkEmail&email=${encodeURIComponent(email)}`,
      {
        credentials: "include", // 세션 쿠키를 포함
      }
    );
    return await response.json();
  }
  /**
   * 닉네임 중복 확인
   * @param {string} nickname 확인할 닉네임
   * @returns {Promise} 중복 확인 결과
   */ static async checkNicknameDuplicate(nickname) {
    const response = await fetch(
      `/signup.do?action=checkNickname&nickname=${encodeURIComponent(
        nickname
      )}`,
      {
        credentials: "include", // 세션 쿠키를 포함
      }
    );
    return await response.json();
  }
  /**
   * 사용자 프로필 조회
   * @param {number} userId 선택적 사용자 ID (생략 시 현재 사용자)
   * @returns {Promise} 사용자 프로필 정보
   */
  static async getProfile(userId) {
    const endpoint = userId ? `/profile.do?userId=${userId}` : "/profile.do";
    return ApiClient.get(endpoint, {}, true);
  }
  /**
   * 사용자 프로필 업데이트
   * @param {Object} profileData 업데이트할 프로필 데이터
   * @returns {Promise} 업데이트 결과
   */
  static async updateProfile(profileData) {
    return ApiClient.put("/profile.do", profileData, true);
  }
  /**
   * 비밀번호 변경
   * @param {string} currentPassword 현재 비밀번호
   * @param {string} newPassword 새 비밀번호
   * @returns {Promise} 비밀번호 변경 결과
   */
  static async changePassword(currentPassword, newPassword) {
    return ApiClient.post("/password.do", {
      currentPassword,
      newPassword,
    });
  }
  /**
   * 비밀번호 찾기/재설정 요청
   * @param {string} email 사용자 이메일
   * @returns {Promise} 재설정 요청 결과
   */
  static async forgotPassword(email) {
    return ApiClient.post("/forgot-password.do", { email });
  }

  /**
   * 로그아웃
   * @returns {void}
   */ static logout() {
    // 토큰 기반 인증 데이터 삭제
    ApiClient.removeAuthToken();
    localStorage.removeItem("kirini_refresh_token");

    // 서버에 로그아웃 요청 보내기 (세션 삭제)
    fetch("/logout.do", {
      method: "GET",
      credentials: "include", // 세션 쿠키를 포함
    }).finally(() => {
      // 로그아웃 이벤트 발생
      window.dispatchEvent(new CustomEvent("auth:logout"));
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
    return ApiClient.get("/keyboard/list.do", params);
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
  static async searchKeyboards(
    query,
    filters = {},
    sorting = {},
    pagination = {}
  ) {
    return ApiClient.get("/keyboard/search.do", {
      query,
      ...filters,
      ...sorting,
      ...pagination,
    });
  }

  /**
   * 인기 키보드 조회
   * @param {number} limit 조회할 개수
   * @returns {Promise} 인기 키보드 목록
   */
  static async getPopularKeyboards(limit = 10) {
    return ApiClient.get("/keyboard/popular.do", { limit });
  }

  /**
   * 키보드 태그 제안하기
   * @param {string} keyboardId 키보드 ID
   * @param {string} tagName 제안할 태그 이름
   * @param {string} reason 제안 이유 (선택사항)
   * @returns {Promise} 제안 결과
   */
  static async suggestTag(keyboardId, tagName, reason = "") {
    return ApiClient.post(
      "/keyboard.do",
      {
        action: "suggestTag",
        keyboardId,
        tagName,
        reason,
      },
      true
    );
  }

  /**
   * 키보드 태그에 투표하기
   * @param {string} keyboardId 키보드 ID
   * @param {string} tagId 태그 ID
   * @param {string} voteType 투표 타입 ('up' 또는 'down')
   * @returns {Promise} 투표 결과
   */
  static async voteTag(keyboardId, tagId, voteType) {
    return ApiClient.post(
      "/keyboard.do",
      {
        action: "voteTag",
        keyboardId,
        tagId,
        voteType,
      },
      true
    );
  }

  /**
   * 키보드 평점 및 한줄평 등록
   * @param {string} keyboardId 키보드 ID
   * @param {number} scoreValue 평점 (1-5)
   * @param {string} review 한줄평 (선택사항)
   * @returns {Promise} 등록 결과
   */
  static async rateKeyboard(keyboardId, scoreValue, review = "") {
    return ApiClient.post(
      "/keyboard.do",
      {
        action: "addScore",
        keyboardId,
        scoreValue,
        review,
      },
      true
    );
  }

  /**
   * 관련 키보드 조회
   * @param {string} keyboardId 기준 키보드 ID
   * @param {number} limit 조회할 개수
   * @returns {Promise} 관련 키보드 목록
   */
  static async getRelatedKeyboards(keyboardId, limit = 5) {
    return ApiClient.get("/keyboard/related.do", {
      id: keyboardId,
      limit,
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
    // 백엔드 컨트롤러 URL 패턴에 맞게 조정
    const boardTypeMapping = {
      news: "news",
      free: "freeboard",
    };

    const mappedType = boardTypeMapping[boardType] || boardType;
    // withAuth 매개변수를 false로 명시적으로 설정하여 인증 없이도 접근 가능하게 함
    return ApiClient.get(`/${mappedType}/list`, params);
  }
  /**
   * 게시물 상세 조회 (댓글 포함)
   * @param {string} boardType 게시판 유형
   * @param {string} postId 게시물 ID
   * @param {boolean} increaseReadCount 조회수 증가 여부
   * @returns {Promise} 게시물 및 댓글 상세 정보
   */
  static async getPost(boardType, postId, increaseReadCount = true) {
    // 백엔드 컨트롤러 URL 패턴에 맞게 조정
    const boardTypeMapping = {
      news: "news",
      free: "freeboard",
    };
    const mappedType = boardTypeMapping[boardType] || boardType;
    console.log(
      `getPost 호출: boardType=${boardType}, mappedType=${mappedType}, postId=${postId}, increaseReadCount=${increaseReadCount}`
    );

    // 게시글 ID 확인
    if (!postId || isNaN(postId)) {
      console.error(`유효하지 않은 게시글 ID: ${postId}`);
      return { status: "error", message: "유효하지 않은 게시글 ID입니다." };
    }

    // 게시글 조회 요청 파라미터
    const params = {
      id: Number(postId),
      postId: Number(postId),
      includeComments: true, // 서버에 댓글도 함께 요청
    };

    // increaseReadCount 파라미터가 false인 경우에만 명시적으로 추가
    // (기본값이 true이므로 true인 경우 전송하지 않음)
    if (increaseReadCount === false) {
      params.increaseReadCount = false;
    }

    console.log(`게시글 상세 요청 파라미터:`, params);

    // 게시글 상세 정보 API 호출
    return ApiClient.get(`/${mappedType}/view`, params);
  }
  /**
   * 게시물 작성
   * @param {string} boardType 게시판 유형
   * @param {Object|FormData} postData 게시물 데이터
   * @returns {Promise} 작성 결과
   */
  static async createPost(boardType, postData) {
    // 백엔드 컨트롤러 URL 패턴에 맞게 조정
    const boardTypeMapping = {
      news: "news",
      free: "freeboard",
      anonymous: "anonymous",
    };

    const mappedType = boardTypeMapping[boardType] || boardType;

    // postData가 FormData인지 확인
    if (postData instanceof FormData) {
      return ApiClient.postFormData(`/${mappedType}/create`, postData, true);
    } else if (typeof postData === "object") {
      // JSON 데이터인 경우 postJson 사용
      return ApiClient.postJson(`/${mappedType}/create`, postData, true);
    } else {
      return ApiClient.post(`/${mappedType}/create`, postData, true);
    }
  }

  /**
   * 게시물 수정
   * @param {string} boardType 게시판 유형
   * @param {string} postId 게시물 ID
   * @param {Object} postData 수정할 데이터
   * @returns {Promise} 수정 결과
   */
  static async updatePost(boardType, postId, postData) {
    return ApiClient.put(
      `/${boardType}/update.do`,
      {
        id: postId,
        ...postData,
      },
      true
    );
  }
  /**
   * 게시물 삭제
   * @param {string} boardType 게시판 유형
   * @param {string} postId 게시물 ID
   * @returns {Promise} 삭제 결과
   */
  static async deletePost(boardType, postId) {
    // 백엔드 URL 패턴에 맞게 조정
    const boardTypeMapping = {
      news: "news",
      free: "freeboard",
    };

    const mappedType = boardTypeMapping[boardType] || boardType;
    console.log(
      `게시글 삭제 요청: boardType=${boardType}, mappedType=${mappedType}, postId=${postId}`
    );

    // 삭제 요청 - /delete 엔드포인트를 사용하여 삭제 요청 전송
    return ApiClient.post(
      `/${mappedType}/delete`,
      {
        id: Number(postId),
      },
      true // withAuth를 true로 설정
    );
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
    // 백엔드 URL 패턴에 맞게 조정
    const boardTypeMapping = {
      news: "news",
      free: "freeboard",
    };

    const mappedType = boardTypeMapping[boardType] || boardType;
    return ApiClient.post(
      `/${mappedType}/comment`,
      {
        postId,
        content,
        parentId,
      },
      true
    );
  }
  /**
   * 댓글 목록 조회
   * @param {string} boardType 게시판 유형
   * @param {string} postId 게시물 ID
   * @param {Object} params 페이징 옵션
   * @returns {Promise} 댓글 목록
   */ static async getComments(boardType, postId, params = {}) {
    // 백엔드 URL 패턴에 맞게 조정
    const boardTypeMapping = {
      news: "news",
      free: "freeboard",
    };

    const mappedType = boardTypeMapping[boardType] || boardType;
    console.log(
      `getComments 호출: boardType=${boardType}, mappedType=${mappedType}, postId=${postId}`
    );

    // 게시글 ID 확인
    if (isNaN(postId)) {
      console.error(`유효하지 않은 게시글 ID: ${postId}`);
      return { status: "error", message: "유효하지 않은 게시글 ID입니다." };
    }

    // 두 가지 파라미터 이름을 모두 지원하기 위해 id와 postId 둘 다 전송
    const queryParams = {
      postId: postId,
      id: postId,
      increaseReadCount: false, // 중요: 댓글 조회 시 조회수 증가하지 않도록 설정
      ...params,
    };

    return ApiClient.get(`/${mappedType}/comments`, queryParams);
  }
  /**
   * 게시물 좋아요/싫어요
   * @param {string} boardType 게시판 유형
   * @param {string} postId 게시물 ID
   * @param {string} type 'like' 또는 'dislike'
   * @returns {Promise} 처리 결과
   */ static async reactToPost(boardType, postId, type) {
    const boardTypeMapping = {
      news: "news",
      free: "freeboard",
    };

    const mappedType = boardTypeMapping[boardType] || boardType;
    console.log(
      `게시글 추천 요청: 게시판=${mappedType}, 게시글ID=${postId}, 타입=${type}`
    );

    // withAuth 매개변수를 true로 설정하여 세션 쿠키 포함
    return ApiClient.get(
      `/${mappedType}/recommend`,
      {
        id: Number(postId),
        postId: Number(postId),
      },
      true // withAuth를 true로 설정
    );
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
    return ApiClient.get("/glossary/list.do", params);
  }

  /**
   * 용어 상세 조회
   * @param {string} termId 용어 ID
   * @returns {Promise} 용어 상세 정보
   */
  static async getTerm(termId) {
    return ApiClient.get("/glossary/detail.do", { id: termId });
  }

  /**
   * 용어 검색
   * @param {string} query 검색어
   * @param {Object} filters 필터 옵션
   * @returns {Promise} 검색 결과
   */
  static async searchTerms(query, filters = {}) {
    return ApiClient.get("/glossary/search.do", {
      query,
      ...filters,
    });
  }

  /**
   * 용어 카테고리 목록 조회
   * @returns {Promise} 카테고리 목록
   */
  static async getCategories() {
    return ApiClient.get("/glossary/categories.do");
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
    return ApiClient.post(
      "/review/create.do",
      {
        keyboardId,
        ...reviewData,
      },
      true
    );
  }

  /**
   * 리뷰 목록 조회
   * @param {string} keyboardId 키보드 ID
   * @param {Object} params 정렬 및 페이징 옵션
   * @returns {Promise} 리뷰 목록
   */
  static async getReviews(keyboardId, params = {}) {
    return ApiClient.get("/review/list.do", {
      keyboardId,
      ...params,
    });
  }

  /**
   * 리뷰 도움됨/안됨 평가
   * @param {string} reviewId 리뷰 ID
   * @param {boolean} helpful 도움 여부
   * @returns {Promise} 처리 결과
   */
  static async rateReviewHelpfulness(reviewId, helpful) {
    return ApiClient.post(
      "/review/helpful.do",
      {
        reviewId,
        helpful,
      },
      true
    );
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
    return ApiClient.request("/qna/create.do", {
      method: "POST",
      body: formData,
      headers: ApiClient.getAuthHeaders({}),
    });
  }

  /**
   * 답변 생성
   * @param {FormData} formData - 답변 데이터 (FormData)
   * @returns {Promise} - 생성 결과
   */
  static async createAnswer(formData) {
    return ApiClient.request("/qna/answer/create.do", {
      method: "POST",
      body: formData,
      headers: ApiClient.getAuthHeaders({}),
    });
  }

  /**
   * 질문에 좋아요 처리
   * @param {string} questionId - 질문 ID
   * @returns {Promise} - 좋아요 결과
   */
  static async likeQuestion(questionId) {
    return ApiClient.post("/qna/like.do", { questionId }, true);
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

// 새로운 라우터 추가
router.getJson("/view", (req, res) => {
  try {
    // 모든 요청 파라미터 디버깅
    const paramNames = req.getParameterNames();
    const paramDebug = ["요청 파라미터: "];
    while (paramNames.hasMoreElements()) {
      const name = paramNames.nextElement();
      const value = req.getParameter(name);
      paramDebug.push(`${name}=${value}, `);
    }
    console.log(paramDebug.join(""));
    // 나머지 코드는 그대로 유지
    // ...
  } catch (error) {
    console.error("요청 처리 중 오류:", error);
    res.status(500).send("서버 오류");
  }
});
