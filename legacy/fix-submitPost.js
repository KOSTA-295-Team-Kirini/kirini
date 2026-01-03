/**
 * 게시판 글 작성 기능
 * @param {string} boardType - 게시판 유형 (news, free, anonymous)
 * @param {string} title - 글 제목
 * @param {string} content - 글 내용
 * @param {File|null} file - 첨부 파일
 */
async function submitPost(boardType, title, content, file) {
    // 로깅
    console.log("게시글 등록:", {boardType, title, content, file});

    // 간단한 유효성 검사
    if (!title || title.trim().length === 0) {
        alert("제목을 입력해주세요.");
        return false;
    }

    if (!content || content.trim().length === 0) {
        alert("내용을 입력해주세요.");
        return false;
    }

    try {
        // BoardService를 이용하여 서버에 게시글 제출
        const boardTypeMapping = {
            news: "news",
            free: "freeboard",
            anonymous: "chatboard", // anonymous -> chatboard로 변경 (실제 컨트롤러 URL과 일치)
        };

        // API 경로 생성 및 엔드포인트 설정
        const mappedType = boardTypeMapping[boardType] || boardType;
        let response = null;

        if (boardType === "news") {
            const newsData = {
                newsTitle: title,
                newsContents: content,
                userId: 1, // 실제 사용자 ID로 교체 필요
            };
            console.log("뉴스 게시글 데이터:", newsData);

            if (window.BoardService) {
                response = await window.BoardService.createPost(mappedType, newsData);
            } else {
                response = await window.ApiClient.postJson(
                    `/${mappedType}/create`,
                    newsData,
                    true
                );
            }

            if (
                response &&
                (response.status === "success" || response.success === true)
            ) {
                alert("게시글이 성공적으로 등록되었습니다.");
                if (window.loadBoardData) {
                    loadBoardData(boardType, 1, 10);
                }
                // 페이지 새로고침 추가
                setTimeout(() => {
                    window.location.reload();
                }, 500);
                return true;
            }
        } else if (boardType === "anonymous") {
            // ChatboardController는 JSON 형식의 데이터를 기대합니다.
            const postData = {
                content: title + "\n\n" + content, // 서버의 ChatboardDTO 필드명에 맞춰야 합니다. 'content'로 가정합니다.
                // 만약 사용자 ID 등 다른 정보가 필요하다면 여기에 추가해야 합니다.
                // 예: userId: currentUser.id (실제 사용자 정보 가져오는 로직 필요)
            };

            console.log("익명 게시글 등록 요청 데이터:", postData);
            console.log("익명 게시글 등록 요청 경로: /chatboard/post");

            // ApiClient.postJson을 사용하여 /chatboard/post 엔드포인트로 JSON 데이터 전송
            response = await window.ApiClient.postJson(
                "/chatboard/post",
                postData,
                true
            );

            if (
                response &&
                (response.id ||
                    response.chatboardId ||
                    response.success === true ||
                    response.status === "success")
            ) {
                alert("익명 게시글이 성공적으로 등록되었습니다.");
                if (window.loadBoardData) {
                    await loadBoardData(boardType, 1, 10); // 'anonymous'
                }
                if (window.showBoardList) {
                    window.showBoardList("anonymous-board"); // 해당 게시판으로 이동 또는 새로고침
                }
                // 글쓰기 성공 후 모달 닫기 및 폼 초기화는 이 함수를 호출한 곳에서 처리합니다.
                return true;
            } else {
                const errorMsg =
                    response?.message ||
                    "익명 게시글 등록에 실패했습니다. 서버 응답을 확인해주세요.";
                alert(errorMsg);
                console.error("익명 게시글 등록 실패:", response);
                return false;
            }
        } else {
            // 자유게시판
            const formData = new FormData();
            formData.append("title", title);
            formData.append("content", content);
            // userId는 서버에서 세션에서 가져오므로 클라이언트에서 전송할 필요 없음

            if (file) {
                formData.append("file", file);
            }

            // 개발 디버깅 로그 추가
            console.log("자유게시판 게시글 작성 요청");
            console.log("boardType:", boardType);
            console.log("title:", title);
            console.log("content 길이:", content.length);
            console.log("file:", file ? `${file.name} (${file.size} bytes)` : "없음");

            if (window.BoardService) {
                console.log("BoardService를 통해 요청 시도");
                response = await window.BoardService.createPost(
                    "free", // boardType만 전달 (free)
                    formData
                );
                console.log("BoardService 응답:", response);
            } else {
                console.log("ApiClient를 통해 요청 시도");
                response = await window.ApiClient.postFormData(
                    `/freeboard?action=write`,
                    formData,
                    true
                );
                console.log("ApiClient 응답:", response);
            }

            if (
                response &&
                (response.status === "success" || response.success === true)
            ) {
                alert("게시글이 성공적으로 등록되었습니다.");
                if (window.loadBoardData) {
                    await loadBoardData(boardType, 1, 10);
                }
                if (window.showBoardList) {
                    const mappedType = boardType === "free" ? "freeboard" : boardType;
                    window.showBoardList(mappedType + "-board");
                }
                return true;
            }

            // 요청이 실패한 경우
            const errorMsg = response?.message || "게시글 등록에 실패했습니다.";
            alert(errorMsg);
            console.error("게시글 등록 실패:", response);
            return false;
        }
    } catch (error) {
        console.error("게시글 등록 중 오류 발생:", error);
        alert("게시글 등록 중 오류가 발생했습니다.");
        return false;
    }
}
