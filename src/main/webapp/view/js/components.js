// HTML 컴포넌트 로드 함수
document.addEventListener("DOMContentLoaded", function () {
  // 헤더 및 푸터 플레이스홀더 가져오기
  const headerPlaceholder = document.getElementById("header-placeholder");
  const footerPlaceholder = document.getElementById("footer-placeholder");

  // 현재 페이지의 경로 확인
  const currentPath = window.location.pathname;

  // 베이스 URL 구하기 - Eclipse에서 컨텍스트 루트와 함께 작동하도록 함
  let basePath = "";

  // 경로가 '/pages/' 포함되거나 '/Kirini/'와 같은 컨텍스트 루트 뒤에 있는 경우
  if (currentPath.includes("/pages/")) {
    basePath = "../";
  } else if (currentPath.match(/\/[^\/]+\/pages\//)) {
    // Eclipse 컨텍스트 루트 환경에서: /ProjectName/pages/
    const pathParts = currentPath.split("/");
    const pagesIndex = pathParts.indexOf("pages");
    if (pagesIndex > 0) {
      basePath = "../";
    }
  }

  // 관리자 페이지 확인
  const isAdminPage = currentPath.includes("admin.html");

  // XMLHttpRequest 사용 - fetch API 대신 (더 넓은 호환성)
  function loadComponent(url, placeholder) {
    const xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function () {
      if (xhr.readyState === 4) {
        if (xhr.status === 200) {
          placeholder.innerHTML = xhr.responseText;
        } else {
          console.error("컴포넌트 로드 오류:", url, xhr.status);
          // 오류 시 직접 경로 시도
          tryDirectPath(url, placeholder);
        }
      }
    };
    xhr.open("GET", url, true);
    xhr.send();
  }

  // 직접 경로 시도 (fallback)
  function tryDirectPath(url, placeholder) {
    const directXhr = new XMLHttpRequest();
    directXhr.onreadystatechange = function () {
      if (directXhr.readyState === 4 && directXhr.status === 200) {
        placeholder.innerHTML = directXhr.responseText;
      }
    };

    // url에서 basePath 제거하고 직접 경로 시도
    const directUrl = url.replace(basePath, "");
    directXhr.open("GET", directUrl, true);
    directXhr.send();
  }

  // 헤더 로드
  if (headerPlaceholder) {
    const headerFile = isAdminPage
      ? `${basePath}components/admin_header.html`
      : `${basePath}components/header.html`;
    loadComponent(headerFile, headerPlaceholder);
  }

  // 푸터 로드
  if (footerPlaceholder) {
    const footerFile = `${basePath}components/footer.html`;

    // 로드 완료 콜백 수정
    const xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function () {
      if (xhr.readyState === 4) {
        if (xhr.status === 200) {
          footerPlaceholder.innerHTML = xhr.responseText;

          // 모든 컴포넌트 로드 완료 시 이벤트 발생
          console.log("컴포넌트 로드 완료");
          document.dispatchEvent(new CustomEvent("componentsLoaded"));
        } else {
          console.error("푸터 로드 오류:", footerFile);
        }
      }
    };
    xhr.open("GET", footerFile, true);
    xhr.send();
  }

  // 콘솔에 현재 환경 정보 출력 (디버깅용)
  console.log("현재 경로:", currentPath);
  console.log("사용된 베이스 경로:", basePath);
});
