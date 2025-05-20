const Auth = {
  // 사용자 역할 정의
  ROLES: {
    GUEST: "GUEST",
    USER: "USER",
    MANAGER: "MANAGER",
    ADMIN: "ADMIN",
  },

  // 로그인 상태 확인
  isLoggedIn: function () {
    return (
      localStorage.getItem("isLoggedIn") === "true" ||
      sessionStorage.getItem("isLoggedIn") === "true"
    );
  },

  // 현재 사용자 역할 확인
  getCurrentRole: function () {
    if (!this.isLoggedIn()) {
      return this.ROLES.GUEST;
    }

    return (
      localStorage.getItem("userRole") ||
      sessionStorage.getItem("userRole") ||
      this.ROLES.USER
    );
  },

  // 권한 확인 메소드
  isGuest: function () {
    return this.getCurrentRole() === this.ROLES.GUEST;
  },
  isUser: function () {
    return this.isLoggedIn();
  }, // 로그인한 모든 사용자
  isRegularUser: function () {
    return this.getCurrentRole() === this.ROLES.USER;
  }, // 일반 사용자(매니저, 관리자 제외)
  isManager: function () {
    return this.getCurrentRole() === this.ROLES.MANAGER;
  },
  isAdmin: function () {
    return this.getCurrentRole() === this.ROLES.ADMIN;
  },
  isManagerOrAdmin: function () {
    const role = this.getCurrentRole();
    return role === this.ROLES.MANAGER || role === this.ROLES.ADMIN;
  },
  // UI 요소 권한 관리
  applyRoleVisibility: function () {
    console.log("권한 적용:", this.getCurrentRole());

    // 모든 권한 섹션 비활성화 (클래스 제거)
    document.querySelectorAll(".auth-section").forEach((section) => {
      section.classList.remove("active");
    });

    // 모든 역할별 개별 요소 기본적으로 숨김
    const allRoleSpecificElements = document.querySelectorAll(
      ".guest-only, .logged-in-only, .user-only, .user-only-not-admin, .admin-only, .manager-only, .manager-admin-only"
    );
    allRoleSpecificElements.forEach((el) => {
      el.classList.add("element-hidden");
    });

    if (this.isLoggedIn()) {
      // 로그인 사용자 UI: logged-in-only 요소들을 보이게 함
      document.querySelectorAll(".logged-in-only").forEach((el) => {
        el.classList.remove("element-hidden");
      });

      // 사용자 이름 표시
      const usernameDisplay = document.getElementById("username-display");
      if (usernameDisplay) {
        const userName =
          localStorage.getItem("userName") ||
          sessionStorage.getItem("userName") ||
          "사용자";
        usernameDisplay.textContent = userName;
      }

      // 로그아웃 버튼과 사용자 인사말 섹션 활성화
      this._showSection(".logout-common-section");
      this._showSection("#user-greeting-section");

      // 권한별 UI 처리
      const role = this.getCurrentRole();
      if (this.isAdmin()) {
        // 관리자 UI
        this._showSection(".admin-section"); // admin-section이 있다면 활성화
        document
          .querySelectorAll(".admin-only, .manager-admin-only")
          .forEach((el) => {
            el.classList.remove("element-hidden");
          });
      } else if (this.isManager()) {
        // 매니저 UI
        this._showSection(".manager-section"); // manager-section이 있다면 활성화
        document
          .querySelectorAll(".manager-only, .manager-admin-only")
          .forEach((el) => {
            el.classList.remove("element-hidden");
          });
      } else if (this.isRegularUser()) {
        // 일반 사용자 UI
        this._showSection(".user-section"); // user-section이 있다면 활성화
        document
          .querySelectorAll(".user-only, .user-only-not-admin")
          .forEach((el) => {
            el.classList.remove("element-hidden");
          });
      }
    } else {
      // 비로그인 사용자(게스트) UI
      this._showSection(".guest-section");
      document.querySelectorAll(".guest-only").forEach((el) => {
        el.classList.remove("element-hidden");
      });
    }
  },

  // 섹션 표시 헬퍼 메소드
  _showSection: function (selector) {
    const section = document.querySelector(selector);
    if (section) {
      section.classList.add("active");
      // section.style.display = ''; // CSS의 .active 클래스가 처리
    }
  },
  // 역할 설정 (로그인 처리)
  setRole: function (role, userName) {
    const storage = document.getElementById("remember-me")?.checked
      ? localStorage
      : sessionStorage;

    storage.setItem("userRole", role);
    storage.setItem("isLoggedIn", "true");

    // 사용자 이름 저장 (파라미터가 없으면 기본값 사용)
    if (userName) {
      storage.setItem("userName", userName);
    }

    console.log("사용자 역할 설정:", role);
  },
  // 로그아웃 처리
  logout: function () {
    // 로컬/세션 스토리지에서 로그인 정보 제거
    localStorage.removeItem("isLoggedIn");
    localStorage.removeItem("userName");
    localStorage.removeItem("userRole");

    sessionStorage.removeItem("isLoggedIn");
    sessionStorage.removeItem("userName");
    sessionStorage.removeItem("userRole");

    // 서버에 로그아웃 요청 (세션 제거)
    fetch("/logout.do", {
      method: "GET",
      credentials: "include",
    }).finally(() => {
      // 홈페이지로 이동
      window.location.href = "../pages/index.html";
    });
  },
};

// 페이지 로드 시 권한 적용 - header.js와 충돌하지 않게 수정
document.addEventListener("DOMContentLoaded", function () {
  console.log("권한 관리 초기화");
  console.log("현재 역할:", Auth.getCurrentRole());

  // 권한 적용 (로그아웃 버튼 이벤트 설정은 header.js로 이동)
  if (!document.getElementById("header-placeholder")) {
    // 헤더 플레이스홀더가 없는 페이지에서만 직접 적용 (중복 방지)
    Auth.applyRoleVisibility();
  }
});

// 외부 호환성 유지
window.AuthRoles = Auth;
window.kirini = window.kirini || {};
window.kirini.auth = { isLoggedIn: Auth.isLoggedIn() };

// setLoginStatus를 setRole의 별칭으로 추가 (하위 호환성)
Auth.setLoginStatus = function (isLoggedIn, userName, role) {
  if (isLoggedIn) {
    this.setRole(role || "USER", userName);
  } else {
    this.logout();
  }
};

// 전역 노출
window.Auth = Auth;
