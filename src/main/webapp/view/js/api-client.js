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
  static clearAuthToken() {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
  }

  static async request(url, options, withAuth = false) {
    const effectiveOptions = { ...options };
    effectiveOptions.headers = {
      ...API_CONFIG.defaultHeaders,
      ...options.headers,
    };

    if (withAuth) {
      const token = ApiClient.getAuthToken();
      if (token) {
        effectiveOptions.headers["Authorization"] = `Bearer ${token}`;
      } else {
        if (
          !url.includes("/login") &&
          !url.includes("/join") &&
          !url.includes("/auth/check-id") &&
          !url.includes("/auth/check-nickname")
        ) {
          console.warn(
            `[DEBUG] ApiClient.request: Auth token is missing for protected route: ${url}. Proceeding without token.`
          );
        }
      }
    }

    effectiveOptions.credentials = "include"; // 세션 쿠키 전송을 위해 항상 포함

    console.log(
      `[DEBUG] ApiClient.request: URL: ${url}, Method: ${
        effectiveOptions.method || "GET"
      }`
    );
    console.log(
      `[DEBUG] ApiClient.request: Options:`,
      JSON.stringify(
        effectiveOptions,
        (key, value) => {
          if (value instanceof File || value instanceof Blob) {
            return `[${value.constructor.name}] ${
              value.name || "(no name)"
            }, Size: ${value.size}, Type: ${value.type || "(unknown type)"}`;
          }
          return value;
        },
        2
      )
    );

    if (effectiveOptions.body) {
      if (typeof effectiveOptions.body === "string") {
        console.log(
          `[DEBUG] ApiClient.request: Body (string):`,
          effectiveOptions.body
        );
      } else if (effectiveOptions.body instanceof FormData) {
        console.log(`[DEBUG] ApiClient.request: Body (FormData):`);
        for (let [key, value] of effectiveOptions.body.entries()) {
          if (value instanceof File) {
            console.log(
              `  ${key}: [File] ${value.name}, Size: ${value.size}, Type: ${value.type}`
            );
          } else {
            console.log(`  ${key}: ${value}`);
          }
        }
      }
    }

    try {
      const response = await fetch(url, effectiveOptions);

      console.log(
        `[DEBUG] ApiClient.request: Response Status for ${url}: ${response.status}`
      );
      const responseHeaders = {};
      response.headers.forEach((value, key) => (responseHeaders[key] = value));
      console.log(
        `[DEBUG] ApiClient.request: Response Headers for ${url}:`,
        JSON.stringify(responseHeaders)
      );

      const responseForText = response.clone();
      const rawResponseText = await responseForText.text();
      console.log(
        `[DEBUG] ApiClient.request: Raw Response Text for ${url} (length ${
          rawResponseText.length
        }):\n<<<<<<<<<<\n${rawResponseText.substring(0, 1000)}${
          rawResponseText.length > 1000 ? "..." : ""
        }\n>>>>>>>>>>`
      );

      if (!response.ok) {
        console.error(
          `[DEBUG] ApiClient.request: Error Response (Status ${response.status}) for ${url}. Raw text was logged above.`
        );
        let errorData = {
          message: `Server returned ${response.status}`,
          details:
            rawResponseText.substring(0, 500) +
            (rawResponseText.length > 500 ? "..." : ""),
        };
        const contentType = response.headers.get("content-type");
        if (
          contentType &&
          contentType.includes("application/json") &&
          rawResponseText.trim()
        ) {
          try {
            errorData = JSON.parse(rawResponseText);
          } catch (e) {
            console.warn(
              `[DEBUG] ApiClient.request: Failed to parse error JSON for ${url}, using raw text. Error: ${e.message}`
            );
          }
        }
        const error = new Error(
          errorData.message || `HTTP error ${response.status}`
        );
        error.status = response.status;
        error.data = errorData;
        error.originalResponseText = rawResponseText;
        throw error;
      }

      const contentType = response.headers.get("content-type");
      if (contentType && contentType.includes("application/json")) {
        if (!rawResponseText.trim()) {
          console.log(
            `[DEBUG] ApiClient.request: Empty JSON response for ${url}. Returning null.`
          );
          return null;
        }
        try {
          return JSON.parse(rawResponseText);
        } catch (e) {
          console.error(
            `[DEBUG] ApiClient.request: Failed to parse successful JSON response for ${url}. Error: ${e.message}. Raw text was logged above.`
          );
          throw new Error(
            `Failed to parse JSON response: ${
              e.message
            }. Raw text (start): ${rawResponseText.substring(0, 100)}...`
          );
        }
      } else {
        console.log(
          `[DEBUG] ApiClient.request: Response for ${url} is not JSON (Content-Type: ${contentType}), returning raw text.`
        );
        return rawResponseText;
      }
    } catch (error) {
      console.error(
        `[DEBUG] ApiClient.request: Fetch/Network error or error thrown from response handling for ${url}: ${error.message}`,
        error.status ? `Status: ${error.status}` : "",
        error.data ? `Data: ${JSON.stringify(error.data)}` : ""
      );
      if (error.originalResponseText) {
        console.error(
          `[DEBUG] ApiClient.request: Original error response text for ${url} was logged above.`
        );
      }
      throw error;
    }
  }

  static async get(url, params = {}, withAuth = false) {
    const query = new URLSearchParams(params).toString();
    const fullUrl = query ? `${url}?${query}` : url;
    return ApiClient.request(fullUrl, { method: "GET" }, withAuth);
  }

  /**
   * GET 요청을 JSON으로 응답받는 메소드
   * @param {string} url - API URL
   * @param {Object} params - 쿼리 파라미터
   * @param {boolean} withAuth - 인증 필요 여부
   * @returns {Promise<Object>} JSON 응답
   */
  static async getJson(url, params = {}, withAuth = true) {
    const query = new URLSearchParams(params).toString();
    const fullUrl = query ? `${url}?${query}` : url;
    return ApiClient.request(
      fullUrl,
      {
        method: "GET",
        headers: { Accept: "application/json" },
      },
      withAuth
    );
  }

  static async post(url, data, withAuth = false) {
    const body = new URLSearchParams();
    for (const key in data) {
      if (
        data.hasOwnProperty(key) &&
        data[key] !== undefined &&
        data[key] !== null
      ) {
        body.append(key, data[key]);
      }
    }
    return ApiClient.request(
      url,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
        },
        body: body.toString(),
      },
      withAuth
    );
  }

  static async postJson(url, data, withAuth = false) {
    return ApiClient.request(
      url,
      {
        method: "POST",
        headers: { "Content-Type": "application/json; charset=UTF-8" },
        body: JSON.stringify(data),
      },
      withAuth
    );
  }

  static async postFormData(url, formData, withAuth = false) {
    return ApiClient.request(
      url,
      {
        method: "POST",
        body: formData,
      },
      withAuth
    );
  }

  static async putJson(url, data, withAuth = false) {
    return ApiClient.request(
      url,
      {
        method: "PUT",
        headers: { "Content-Type": "application/json; charset=UTF-8" },
        body: JSON.stringify(data),
      },
      withAuth
    );
  }

  static async delete(url, data, withAuth = false) {
    return ApiClient.request(
      url,
      {
        method: "DELETE",
        headers: data
          ? { "Content-Type": "application/json; charset=UTF-8" }
          : {},
        body: data ? JSON.stringify(data) : null,
      },
      withAuth
    );
  }
}

/**
 * 사용자 관련 API 서비스
 */
class UserService {
  static async login(email, password) {
    try {
      const response = await fetch("/login.do", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
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

  static async register(userData) {
    return ApiClient.post("/register.do", userData);
  }

  static async checkEmailDuplicate(email) {
    const response = await fetch(
      `/signup.do?action=checkEmail&email=${encodeURIComponent(email)}`,
      {
        credentials: "include",
      }
    );
    return await response.json();
  }

  static async checkNicknameDuplicate(nickname) {
    const response = await fetch(
      `/signup.do?action=checkNickname&nickname=${encodeURIComponent(
        nickname
      )}`,
      {
        credentials: "include",
      }
    );
    return await response.json();
  }

  static async getProfile(userId) {
    const endpoint = userId ? `/profile.do?userId=${userId}` : "/profile.do";
    return ApiClient.get(endpoint, {}, true);
  }

  static async updateProfile(profileData) {
    return ApiClient.putJson("/profile.do", profileData, true);
  }

  static async changePassword(currentPassword, newPassword) {
    return ApiClient.postJson("/password.do", {
      currentPassword,
      newPassword,
    });
  }

  static async forgotPassword(email) {
    return ApiClient.postJson("/forgot-password.do", { email });
  }

  static logout() {
    ApiClient.clearAuthToken();
    localStorage.removeItem("kirini_refresh_token");

    fetch("/logout.do", {
      method: "GET",
      credentials: "include",
    }).finally(() => {
      window.dispatchEvent(new CustomEvent("auth:logout"));
    });
  }
}

/**
 * 키보드 관련 API 서비스
 */
class KeyboardService {
  static async getKeyboards(params = {}) {
    return ApiClient.get("/keyboard/list.do", params);
  }

  static async getKeyboardDetails(keyboardId) {
    return ApiClient.get(`/keyboard/detail.do`, { id: keyboardId });
  }

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

  static async getPopularKeyboards(limit = 10) {
    return ApiClient.get("/keyboard/popular.do", { limit });
  }

  static async suggestTag(keyboardId, tagName, reason = "") {
    return ApiClient.postJson(
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

  static async voteTag(keyboardId, tagId, voteType) {
    return ApiClient.postJson(
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

  static async rateKeyboard(keyboardId, scoreValue, review = "") {
    return ApiClient.postJson(
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
   * 게시판 타입을 API 요청에 맞는 형식으로 변환
   * @param {string} boardType - 게시판 타입 (news, free 등)
   * @returns {string} 매핑된 타입
   */
  static mapBoardType(boardType) {
    const boardTypeMapping = {
      news: "news",
      free: "freeboard",
      anonymous: "chatboard",
      chatboard: "chatboard",
    };
    return boardTypeMapping[boardType] || boardType;
  }

  static async getPosts(boardType, params = {}) {
    const mappedType = this.mapBoardType(boardType);
    return ApiClient.get(`/${mappedType}/list`, params);
  }

  static async getPost(boardType, postId, increaseReadCount = true) {
    const mappedType = this.mapBoardType(boardType);

    if (!postId || isNaN(postId)) {
      console.error(`유효하지 않은 게시글 ID: ${postId}`);
      return { status: "error", message: "유효하지 않은 게시글 ID입니다." };
    }

    const params = {
      id: Number(postId),
      includeComments: true,
    };

    if (mappedType === "freeboard") {
      params.freeboardUid = Number(postId);
    } else if (mappedType === "news") {
      params.newsId = Number(postId);
    } else {
      params.postId = Number(postId);
    }

    if (increaseReadCount === false) {
      params.increaseReadCount = false;
    }

    return ApiClient.get(`/${mappedType}/view`, params);
  }

  static async createPost(boardType, postData) {
    const mappedType = this.mapBoardType(boardType);

    if (postData instanceof FormData) {
      if (mappedType === "freeboard") {
        return ApiClient.postFormData(
          `/freeboard?action=write`,
          postData,
          true
        );
      } else {
        return ApiClient.postFormData(`/${mappedType}/create`, postData, true);
      }
    } else if (typeof postData === "object") {
      if (mappedType === "freeboard") {
        return ApiClient.postJson(`/freeboard?action=write`, postData, true);
      } else {
        return ApiClient.postJson(`/${mappedType}/create`, postData, true);
      }
    } else {
      return ApiClient.post(`/${mappedType}/create`, postData, true);
    }
  }

  static async updatePost(boardType, postId, postData) {
    const mappedType = this.mapBoardType(boardType);
    return ApiClient.putJson(
      `/${mappedType}/update.do`,
      {
        id: postId,
        ...postData,
      },
      true
    );
  }

  static async deletePost(boardType, postId) {
    const mappedType = this.mapBoardType(boardType);

    return ApiClient.postJson(
      `/${mappedType}/delete`,
      {
        id: Number(postId),
      },
      true
    );
  }

  static async createComment(boardType, postId, content, parentId = null) {
    const mappedType = this.mapBoardType(boardType);
    console.log(
      `[DEBUG] BoardService.createComment called with: boardType=${boardType}, postId=${postId}, content=${content}, parentId=${parentId}, mappedType=${mappedType}`
    );

    // 페이로드 구성
    let payload;
    let endpoint;

    if (mappedType === "freeboard") {
      payload = {
        freeboardUid: Number(postId),
        freeboardCommentContents: content,
        parentId:
          parentId !== null && parentId !== undefined ? Number(parentId) : null,
      };
      endpoint = `/freeboard/addComment`;

      console.log(
        `[DEBUG] BoardService.createComment (freeboard): Endpoint=${endpoint}, Payload:`,
        payload
      );
      return ApiClient.postJson(endpoint, payload, true);
    } else if (mappedType === "news") {
      payload = {
        newsId: Number(postId),
        newsCommentContents: content,
        parentId:
          parentId !== null && parentId !== undefined ? Number(parentId) : null,
      };
      endpoint = `/news/addComment`;

      console.log(
        `[DEBUG] BoardService.createComment (news): Endpoint=${endpoint}, Payload:`,
        payload
      );
      return ApiClient.postJson(endpoint, payload, true);
    } else {
      // 익명게시판(chatboard) 또는 기타 게시판은 댓글 기능이 다를 수 있음
      console.error(
        `[ApiClient] createComment: Unsupported board type for comments: ${mappedType}`
      );
      return Promise.reject(
        new Error(`Unsupported board type for comments: ${mappedType}`)
      );
    }
  }

  static async getComments(boardType, postId, params = {}) {
    const mappedType = this.mapBoardType(boardType);

    if (isNaN(postId)) {
      console.error(`유효하지 않은 게시글 ID: ${postId}`);
      return { status: "error", message: "유효하지 않은 게시글 ID입니다." };
    }

    const queryParams = {
      postId: postId,
      id: postId,
      increaseReadCount: false,
      ...params,
    };

    return ApiClient.get(`/${mappedType}/comments`, queryParams);
  }

  static async reactToPost(boardType, postId, reactionType) {
    const mappedType = this.mapBoardType(boardType);

    // 자유게시판 추천 기능
    if (mappedType === "freeboard") {
      const payload = {
        freeboardUid: Number(postId), // postId -> freeboardUid (FreeboardDTO 필드명에 맞춤)
        type: reactionType,
      };
      console.log(
        `[DEBUG] BoardService.reactToPost (freeboard): Payload:`,
        payload
      );
      return ApiClient.postJson(`/${mappedType}/recommend`, payload, true);
    }
    // 뉴스 게시판 추천 기능
    else if (mappedType === "news") {
      const payload = {
        newsId: Number(postId),
        type: reactionType,
      };
      console.log(`[DEBUG] BoardService.reactToPost (news): Payload:`, payload);
      return ApiClient.postJson(`/${mappedType}/recommend`, payload, true);
    }
    // 다른 게시판 유형
    else {
      return Promise.reject(
        new Error(`Unsupported board type for recommendation: ${mappedType}`)
      );
    }
  }
}

/**
 * 용어사전 관련 API 서비스
 */
class GlossaryService {
  static async getTerms(params = {}) {
    return ApiClient.get("/glossary/list.do", params);
  }

  static async getTerm(termId) {
    return ApiClient.get("/glossary/detail.do", { id: termId });
  }

  static async searchTerms(query, filters = {}) {
    return ApiClient.get("/glossary/search.do", {
      query,
      ...filters,
    });
  }

  static async getCategories() {
    return ApiClient.get("/glossary/categories.do");
  }
}

/**
 * 리뷰 관련 API 서비스
 */
class ReviewService {
  static async createReview(keyboardId, reviewData) {
    return ApiClient.postJson(
      "/review/create.do",
      {
        keyboardId,
        ...reviewData,
      },
      true
    );
  }

  static async getReviews(keyboardId, params = {}) {
    return ApiClient.get("/review/list.do", {
      keyboardId,
      ...params,
    });
  }

  static async rateReviewHelpfulness(reviewId, helpful) {
    return ApiClient.postJson(
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
  static async createQuestion(formData) {
    return ApiClient.request("/qna/create.do", {
      method: "POST",
      body: formData,
      headers: ApiClient.getAuthHeaders({}),
    });
  }

  static async createAnswer(formData) {
    return ApiClient.request("/qna/answer/create.do", {
      method: "POST",
      body: formData,
      headers: ApiClient.getAuthHeaders({}),
    });
  }

  static async likeQuestion(questionId) {
    return ApiClient.postJson("/qna/like.do", { questionId }, true);
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
