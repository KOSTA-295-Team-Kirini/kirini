// API 호출을 위한 기본 클라이언트
const API = {
  // 기본 API 설정
  baseUrl: '', // 상대 경로로 설정 (같은 도메인)
  
  /**
   * 기본 fetch wrapper 함수
   * @param {string} endpoint - API 엔드포인트
   * @param {Object} options - fetch 옵션
   * @returns {Promise} - API 응답
   */
  async fetch(endpoint, options = {}) {
    const url = `${this.baseUrl}${endpoint}`;
    
    // 기본 헤더 설정
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers
    };

    try {
      const response = await fetch(url, {
        ...options,
        headers
      });

      // 응답이 JSON이 아닌 경우 처리
      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        const data = await response.json();
        
        // 성공 응답이 아닌 경우 에러 던지기
        if (!response.ok) {
          const error = new Error(data.message || '서버 오류가 발생했습니다.');
          error.status = response.status;
          error.data = data;
          throw error;
        }
        
        return data;
      } else {
        // JSON이 아닌 응답 처리
        if (!response.ok) {
          const error = new Error('서버 오류가 발생했습니다.');
          error.status = response.status;
          throw error;
        }
        
        return await response.text();
      }
    } catch (error) {
      console.error(`API 오류 (${endpoint}):`, error);
      throw error;
    }
  },

  // 키보드 관련 API
  keyboard: {
    /**
     * 키보드 목록 가져오기
     * @param {number} page - 페이지 번호
     * @param {number} pageSize - 페이지 크기
     * @returns {Promise} - 키보드 목록
     */
    async getList(page = 1, pageSize = 12) {
      return API.fetch(`/keyboard.do?action=list&page=${page}&pageSize=${pageSize}`);
    },
    
    /**
     * 키보드 상세 정보 가져오기
     * @param {number} id - 키보드 ID
     * @returns {Promise} - 키보드 상세 정보
     */
    async getDetail(id) {
      return API.fetch(`/keyboard.do?action=view&id=${id}`);
    },
    
    /**
     * 키보드 검색하기
     * @param {Object} params - 검색 파라미터
     * @returns {Promise} - 검색 결과
     */
    async search(params = {}) {
      const queryParams = new URLSearchParams();
      queryParams.append('action', 'search');
      
      Object.keys(params).forEach(key => {
        if (params[key]) queryParams.append(key, params[key]);
      });
      
      return API.fetch(`/keyboard.do?${queryParams.toString()}`);
    },
    
    /**
     * 키보드 태그에 투표하기
     * @param {number} keyboardId - 키보드 ID
     * @param {number} tagId - 태그 ID
     * @param {string} voteType - 투표 타입 ('up' 또는 'down')
     * @returns {Promise} - 투표 결과
     */
    async voteTag(keyboardId, tagId, voteType) {
      return API.fetch(`/keyboard.do`, {
        method: 'POST',
        body: JSON.stringify({ 
          action: 'voteTag',
          keyboardId,
          tagId,
          voteType
        })
      });
    },
    
    /**
     * 키보드 스크랩하기
     * @param {number} keyboardId - 키보드 ID
     * @returns {Promise} - 스크랩 결과
     */
    async scrap(keyboardId) {
      return API.fetch(`/keyboard.do`, {
        method: 'POST',
        body: JSON.stringify({ 
          action: 'scrap',
          keyboardId
        })
      });
    },
    
    /**
     * 키보드 별점 등록/수정
     * @param {number} keyboardId - 키보드 ID
     * @param {number} scoreValue - 별점 (1-5)
     * @param {string} review - 한줄평
     * @returns {Promise} - 평가 결과
     */
    async rate(keyboardId, scoreValue, review = '') {
      return API.fetch(`/keyboard.do`, {
        method: 'POST',
        body: JSON.stringify({ 
          action: 'addScore',
          keyboardId,
          scoreValue,
          review
        })
      });
    },
    
    /**
     * 키보드 태그 제안하기
     * @param {number} keyboardId - 키보드 ID
     * @param {string} tagName - 태그 이름
     * @param {string} reason - 태그 제안 이유
     * @returns {Promise} - 제안 결과
     */
    async suggestTag(keyboardId, tagName, reason = '') {
      return API.fetch(`/keyboard.do`, {
        method: 'POST',
        body: JSON.stringify({ 
          action: 'suggestTag',
          keyboardId,
          tagName,
          reason
        })
      });
    }
  },
  
  // 사용자 관련 API
  user: {
    /**
     * 로그인
     * @param {string} email - 이메일
     * @param {string} password - 비밀번호
     * @param {boolean} rememberMe - 로그인 상태 유지 여부
     * @returns {Promise} - 로그인 결과
     */
    async login(email, password, rememberMe = false) {
      return API.fetch('/user/login', {
        method: 'POST',
        body: JSON.stringify({ email, password, rememberMe })
      });
    },
    
    /**
     * 회원가입
     * @param {Object} userData - 사용자 데이터
     * @returns {Promise} - 회원가입 결과
     */
    async register(userData) {
      return API.fetch('/user/register', {
        method: 'POST',
        body: JSON.stringify(userData)
      });
    },
    
    /**
     * 이메일 중복 확인
     * @param {string} email - 이메일
     * @returns {Promise} - 중복 확인 결과
     */
    async checkEmail(email) {
      return API.fetch(`/user/check-email?email=${encodeURIComponent(email)}`);
    },
    
    /**
     * 닉네임 중복 확인
     * @param {string} nickname - 닉네임
     * @returns {Promise} - 중복 확인 결과
     */
    async checkNickname(nickname) {
      return API.fetch(`/user/check-nickname?nickname=${encodeURIComponent(nickname)}`);
    }
  },
  
  // 게시판 관련 API
  board: {
    /**
     * 게시글 생성
     * @param {Object} postData - 게시글 데이터
     * @returns {Promise} - 생성 결과
     */
    async createPost(postData) {
      const formData = new FormData();
      
      Object.keys(postData).forEach(key => {
        formData.append(key, postData[key]);
      });
      
      return API.fetch(`/board/create`, {
        method: 'POST',
        headers: {}, // FormData를 사용할 때는 Content-Type 헤더를 설정하지 않음
        body: formData
      });
    },
    
    /**
     * 게시글에 좋아요 처리
     * @param {number} postId - 게시글 ID
     * @returns {Promise} - 좋아요 결과
     */
    async likePost(postId) {
      return API.fetch(`/post/like`, {
        method: 'POST',
        body: JSON.stringify({ postId })
      });
    }
  },
  
  // QnA 관련 API
  qna: {
    /**
     * 질문 생성
     * @param {Object} questionData - 질문 데이터
     * @returns {Promise} - 생성 결과
     */
    async createQuestion(questionData) {
      const formData = new FormData();
      
      Object.keys(questionData).forEach(key => {
        formData.append(key, questionData[key]);
      });
      
      return API.fetch(`/qna/create`, {
        method: 'POST',
        headers: {}, // FormData를 사용할 때는 Content-Type 헤더를 설정하지 않음
        body: formData
      });
    },
    
    /**
     * 답변 생성
     * @param {Object} answerData - 답변 데이터
     * @returns {Promise} - 생성 결과
     */
    async createAnswer(answerData) {
      const formData = new FormData();
      
      Object.keys(answerData).forEach(key => {
        formData.append(key, answerData[key]);
      });
      
      return API.fetch(`/qna/answer/create`, {
        method: 'POST',
        headers: {}, // FormData를 사용할 때는 Content-Type 헤더를 설정하지 않음
        body: formData
      });
    },
    
    /**
     * 질문에 좋아요 처리
     * @param {number} questionId - 질문 ID
     * @returns {Promise} - 좋아요 결과
     */
    async likeQuestion(questionId) {
      return API.fetch(`/qna/like`, {
        method: 'POST',
        body: JSON.stringify({ questionId })
      });
    }
  }
};