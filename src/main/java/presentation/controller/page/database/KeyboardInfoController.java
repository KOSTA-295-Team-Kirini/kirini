package presentation.controller.page.database;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import business.service.database.KeyboardInfoService;
import dto.keyboard.KeyboardInfoDTO;
import dto.keyboard.KeyboardScoreDTO;
import dto.keyboard.KeyboardTagDTO;
import dto.user.UserDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import presentation.controller.page.Controller;
import util.SecurityUtil;

/**
 * 키보드 정보 페이지 컨트롤러
 * 키보드 정보 조회, 검색, 한줄평, 태그, 별점 관리 기능 제공
 */
public class KeyboardInfoController implements Controller {
    private final KeyboardInfoService keyboardInfoService;
    
    public KeyboardInfoController() {
        this.keyboardInfoService = new KeyboardInfoService();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if (action == null) {
            // 기본: 키보드 정보 목록 페이지
            getAllKeyboardInfos(request, response);
            return;
        }
        
        switch (action) {
            case "view":
                getKeyboardInfoById(request, response);
                break;
            case "search":
                searchKeyboardInfosByCondition(request, response);
                break;
            default:
                getAllKeyboardInfos(request, response);
                break;
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if (action == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "요청된 작업이 없습니다.");
            return;
        }
        
        // 로그인 확인 (스크랩, 한줄평, 태그 투표 등은 로그인 필요)
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            if (isAjaxRequest(request)) {
                sendJsonResponse(response, false, "로그인이 필요한 기능입니다.");
            } else {
                response.sendRedirect(request.getContextPath() + "/login");
            }
            return;
        }
        
        switch (action) {
            case "addComment":
                addKeyboardComment(request, response);
                break;
            case "deleteComment":
                deleteKeyboardCommentById(request, response);
                break;
            case "scrap":
                scrapKeyboardInfo(request, response);
                break;
            case "voteTag":
                voteKeyboardTag(request, response);
                break;
            case "updateTagVote":
                updateKeyboardTagVote(request, response);
                break;
            case "suggestTag":
                suggestKeyboardTag(request, response);
                break;
            case "addScore":
                addKeyboardScore(request, response);
                break;
            case "updateScore":
                updateKeyboardScore(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "지원하지 않는 작업입니다.");
                break;
        }
    }

    /**
     * 키보드 전체 정보 조회
     */
    private void getAllKeyboardInfos(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 페이징 처리
            int page = 1;
            int pageSize = 12; // 한 페이지에 표시할 키보드 수
            
            if (request.getParameter("page") != null) {
                try {
                    page = Integer.parseInt(request.getParameter("page"));
                    if (page < 1) page = 1;
                } catch (NumberFormatException e) {
                    // 기본값 사용
                }
            }
            
            // 카테고리 필터링
            String category = request.getParameter("category");
            
            // 키보드 정보 목록 가져오기
            List<KeyboardInfoDTO> keyboardInfos;
            int totalCount;
            
            if (category != null && !category.equals("all")) {
                keyboardInfos = keyboardInfoService.getKeyboardInfosByCategory(category, page, pageSize);
                totalCount = keyboardInfoService.getTotalKeyboardCountByCategory(category);
            } else {
                keyboardInfos = keyboardInfoService.getAllKeyboardInfos(page, pageSize);
                totalCount = keyboardInfoService.getTotalKeyboardCount();
            }
            
            // 총 페이지 수 계산
            int totalPages = (int) Math.ceil((double) totalCount / pageSize);
            
            // 카테고리 목록 가져오기
            List<String> categories = keyboardInfoService.getAllCategories();
            
            // 요청 속성 설정
            request.setAttribute("keyboardInfos", keyboardInfos);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("categories", categories);
            request.setAttribute("selectedCategory", category);
            
            // 키보드 정보 목록 페이지로 포워딩
            request.getRequestDispatcher("/view/pages/keyboard_info.html").forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 키보드 상세 정보 조회
     */
    private void getKeyboardInfoById(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 키보드 ID 파라미터 가져오기
            String keyboardIdParam = request.getParameter("id");
            if (keyboardIdParam == null || keyboardIdParam.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "키보드 ID가 필요합니다.");
                return;
            }
            
            long keyboardId = Long.parseLong(keyboardIdParam);
            
            // 키보드 정보 가져오기
            KeyboardInfoDTO keyboardInfo = keyboardInfoService.getKeyboardInfoById(keyboardId);
            if (keyboardInfo == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "해당 키보드 정보를 찾을 수 없습니다.");
                return;
            }
            
            // 한줄평 목록 가져오기
            List<KeyboardScoreDTO> scores = keyboardInfoService.getKeyboardScoresByKeyboardId(keyboardId);
            
            // 태그 목록 가져오기
            List<KeyboardTagDTO> tags = keyboardInfoService.getKeyboardTagsByKeyboardId(keyboardId);
            
            // 현재 로그인한 사용자가 이 키보드를 스크랩했는지 확인
            boolean isScrapped = false;
            // 현재 로그인한 사용자의 별점 정보
            KeyboardScoreDTO userScore = null;
            
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("user") != null) {
                UserDTO user = (UserDTO) session.getAttribute("user");
                long userId = user.getUserUid();
                
                isScrapped = keyboardInfoService.isKeyboardScrappedByUser(keyboardId, userId);
                userScore = keyboardInfoService.getUserKeyboardScore(keyboardId, userId);
            }
            
            // 요청 속성 설정
            request.setAttribute("keyboardInfo", keyboardInfo);
            request.setAttribute("scores", scores);
            request.setAttribute("tags", tags);
            request.setAttribute("isScrapped", isScrapped);
            request.setAttribute("userScore", userScore);
            
            // 키보드 상세 정보 페이지로 포워딩
            request.getRequestDispatcher("/view/pages/keyboard_detail.html").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 키보드 ID 형식입니다.");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 키보드 한줄평 입력
     */
    private void addKeyboardComment(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 키보드 ID와 한줄평 내용 가져오기
            String keyboardIdParam = request.getParameter("keyboardId");
            String comment = request.getParameter("comment");
            
            if (keyboardIdParam == null || comment == null || comment.trim().isEmpty()) {
                sendJsonResponse(response, false, "필수 입력값이 누락되었습니다.");
                return;
            }
            
            long keyboardId = Long.parseLong(keyboardIdParam);
            
            // 현재 로그인한 사용자 정보 가져오기
            HttpSession session = request.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            long userId = user.getUserUid();
            
            // XSS 방지 처리
            comment = SecurityUtil.escapeXSS(comment);
            
            // 한줄평 추가
            boolean success = keyboardInfoService.addKeyboardComment(keyboardId, userId, comment);
            
            if (success) {
                sendJsonResponse(response, true, "한줄평이 추가되었습니다.");
            } else {
                sendJsonResponse(response, false, "한줄평 추가에 실패했습니다.");
            }
        } catch (NumberFormatException e) {
            sendJsonResponse(response, false, "잘못된 키보드 ID 형식입니다.");
        } catch (SQLException e) {
            e.printStackTrace();
            sendJsonResponse(response, false, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 키보드 한줄평 삭제
     */
    private void deleteKeyboardCommentById(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 한줄평 ID 가져오기
            String commentIdParam = request.getParameter("commentId");
            
            if (commentIdParam == null) {
                sendJsonResponse(response, false, "한줄평 ID가 필요합니다.");
                return;
            }
            
            long commentId = Long.parseLong(commentIdParam);
            
            // 현재 로그인한 사용자 정보 가져오기
            HttpSession session = request.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            long userId = user.getUserUid();
            String userAuthority = user.getUserAuthority();
            
            boolean isAdmin = "admin".equals(userAuthority);
            boolean isOwner = keyboardInfoService.isUserCommentOwner(commentId, userId);
            
            // 관리자이거나 자신의 한줄평인 경우만 삭제 가능
            if (!isAdmin && !isOwner) {
                sendJsonResponse(response, false, "삭제 권한이 없습니다.");
                return;
            }
            
            // 삭제 사유 (관리자가 다른 사용자의 한줄평을 삭제할 때 필요)
            String reason = null;
            if (isAdmin && !isOwner) {
                reason = request.getParameter("reason");
                if (reason == null || reason.trim().isEmpty()) {
                    sendJsonResponse(response, false, "삭제 사유를 입력해주세요.");
                    return;
                }
            }
            
            // 한줄평 삭제
            boolean success;
            if (isAdmin && !isOwner) {
                success = keyboardInfoService.deleteKeyboardCommentByAdmin(commentId, userId, reason);
            } else {
                success = keyboardInfoService.deleteKeyboardCommentByUser(commentId, userId);
            }
            
            if (success) {
                sendJsonResponse(response, true, "한줄평이 삭제되었습니다.");
            } else {
                sendJsonResponse(response, false, "한줄평 삭제에 실패했습니다.");
            }
        } catch (NumberFormatException e) {
            sendJsonResponse(response, false, "잘못된 한줄평 ID 형식입니다.");
        } catch (SQLException e) {
            e.printStackTrace();
            sendJsonResponse(response, false, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 키보드 정보 검색
     */
    private void searchKeyboardInfosByCondition(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 검색 조건 파라미터 가져오기
            String keyword = request.getParameter("keyword");
            String switchType = request.getParameter("switchType");
            String layoutType = request.getParameter("layoutType");
            String connectType = request.getParameter("connectType");
            String tagsParam = request.getParameter("tags");
            
            List<String> tags = null;
            if (tagsParam != null && !tagsParam.trim().isEmpty()) {
                tags = Arrays.asList(tagsParam.split(","));
            }
            
            // 페이징 처리
            int page = 1;
            int pageSize = 12;
            
            if (request.getParameter("page") != null) {
                try {
                    page = Integer.parseInt(request.getParameter("page"));
                    if (page < 1) page = 1;
                } catch (NumberFormatException e) {
                    // 기본값 사용
                }
            }
            
            // 검색 결과 가져오기
            List<KeyboardInfoDTO> searchResults = keyboardInfoService.searchKeyboardInfosByCondition(
                    keyword, switchType, layoutType, connectType, tags, page, pageSize);
            
            // 총 검색 결과 수 가져오기
            int totalCount = keyboardInfoService.getTotalSearchResultCount(
                    keyword, switchType, layoutType, connectType, tags);
            
            // 총 페이지 수 계산
            int totalPages = (int) Math.ceil((double) totalCount / pageSize);
            
            // 스위치 유형, 레이아웃 유형, 연결 유형 목록 가져오기
            List<String> switchTypes = keyboardInfoService.getAllSwitchTypes();
            List<String> layoutTypes = keyboardInfoService.getAllLayoutTypes();
            List<String> connectTypes = keyboardInfoService.getAllConnectTypes();
            List<String> popularTags = keyboardInfoService.getPopularTags(10); // 상위 10개 인기 태그
            
            // 요청 속성 설정
            request.setAttribute("keyboardInfos", searchResults);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("keyword", keyword);
            request.setAttribute("switchType", switchType);
            request.setAttribute("layoutType", layoutType);
            request.setAttribute("connectType", connectType);
            request.setAttribute("selectedTags", tags);
            
            request.setAttribute("switchTypes", switchTypes);
            request.setAttribute("layoutTypes", layoutTypes);
            request.setAttribute("connectTypes", connectTypes);
            request.setAttribute("popularTags", popularTags);
            
            // 검색 결과 페이지로 포워딩
            request.getRequestDispatcher("/view/pages/keyboard_search.html").forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 키보드 정보 스크랩
     */
    private void scrapKeyboardInfo(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 키보드 ID 가져오기
            String keyboardIdParam = request.getParameter("keyboardId");
            
            if (keyboardIdParam == null) {
                sendJsonResponse(response, false, "키보드 ID가 필요합니다.");
                return;
            }
            
            long keyboardId = Long.parseLong(keyboardIdParam);
            
            // 현재 로그인한 사용자 정보 가져오기
            HttpSession session = request.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            long userId = user.getUserUid();
            
            // 이미 스크랩했는지 확인
            boolean isScrapped = keyboardInfoService.isKeyboardScrappedByUser(keyboardId, userId);
            
            boolean success;
            String message;
            
            if (isScrapped) {
                // 이미 스크랩했다면 스크랩 취소
                success = keyboardInfoService.unsecrapKeyboardInfo(keyboardId, userId);
                message = "스크랩이 취소되었습니다.";
            } else {
                // 스크랩 추가
                success = keyboardInfoService.scrapKeyboardInfo(keyboardId, userId);
                message = "키보드가 스크랩되었습니다.";
            }
            
            if (success) {
                sendJsonResponse(response, true, message);
            } else {
                sendJsonResponse(response, false, "스크랩 처리에 실패했습니다.");
            }
        } catch (NumberFormatException e) {
            sendJsonResponse(response, false, "잘못된 키보드 ID 형식입니다.");
        } catch (SQLException e) {
            e.printStackTrace();
            sendJsonResponse(response, false, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 키보드 태그 투표
     */
    private void voteKeyboardTag(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 키보드 ID와 태그 ID, 투표 타입 가져오기
            String keyboardIdParam = request.getParameter("keyboardId");
            String tagIdParam = request.getParameter("tagId");
            String voteType = request.getParameter("voteType"); // 'up' 또는 'down'
            
            if (keyboardIdParam == null || tagIdParam == null || voteType == null) {
                sendJsonResponse(response, false, "필수 입력값이 누락되었습니다.");
                return;
            }
            
            if (!voteType.equals("up") && !voteType.equals("down")) {
                sendJsonResponse(response, false, "잘못된 투표 타입입니다.");
                return;
            }
            
            long keyboardId = Long.parseLong(keyboardIdParam);
            long tagId = Long.parseLong(tagIdParam);
            boolean isUpvote = voteType.equals("up");
            
            // 현재 로그인한 사용자 정보 가져오기
            HttpSession session = request.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            long userId = user.getUserUid();
            
            // 이미 투표했는지 확인
            String existingVote = keyboardInfoService.getUserTagVote(keyboardId, tagId, userId);
            
            boolean success;
            
            if (existingVote != null) {
                sendJsonResponse(response, false, "이미 이 태그에 투표하셨습니다.");
                return;
            }
            
            // 태그 투표 추가
            success = keyboardInfoService.voteKeyboardTag(keyboardId, tagId, userId, isUpvote);
            
            if (success) {
                // 태그 투표 결과 가져오기
                int voteCount = keyboardInfoService.getTagVoteCount(keyboardId, tagId);
                sendJsonResponse(response, true, "태그에 " + (isUpvote ? "추천" : "비추천") + "했습니다.", voteCount);
            } else {
                sendJsonResponse(response, false, "태그 투표에 실패했습니다.");
            }
        } catch (NumberFormatException e) {
            sendJsonResponse(response, false, "잘못된 ID 형식입니다.");
        } catch (SQLException e) {
            e.printStackTrace();
            sendJsonResponse(response, false, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 키보드 태그 투표 수정
     */
    private void updateKeyboardTagVote(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 키보드 ID와 태그 ID, 투표 타입 가져오기
            String keyboardIdParam = request.getParameter("keyboardId");
            String tagIdParam = request.getParameter("tagId");
            String voteType = request.getParameter("voteType"); // 'up' 또는 'down'
            
            if (keyboardIdParam == null || tagIdParam == null || voteType == null) {
                sendJsonResponse(response, false, "필수 입력값이 누락되었습니다.");
                return;
            }
            
            if (!voteType.equals("up") && !voteType.equals("down")) {
                sendJsonResponse(response, false, "잘못된 투표 타입입니다.");
                return;
            }
            
            long keyboardId = Long.parseLong(keyboardIdParam);
            long tagId = Long.parseLong(tagIdParam);
            boolean isUpvote = voteType.equals("up");
            
            // 현재 로그인한 사용자 정보 가져오기
            HttpSession session = request.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            long userId = user.getUserUid();
            
            // 이미 투표했는지 확인
            String existingVote = keyboardInfoService.getUserTagVote(keyboardId, tagId, userId);
            
            if (existingVote == null) {
                sendJsonResponse(response, false, "먼저 태그에 투표해야 합니다.");
                return;
            }
            
            // 이미 같은 방향으로 투표했다면 변경할 필요 없음
            if ((isUpvote && existingVote.equals("up")) || (!isUpvote && existingVote.equals("down"))) {
                sendJsonResponse(response, false, "이미 이 방향으로 투표하셨습니다.");
                return;
            }
            
            // 태그 투표 수정
            boolean success = keyboardInfoService.updateKeyboardTagVote(keyboardId, tagId, userId, isUpvote);
            
            if (success) {
                // 태그 투표 결과 가져오기
                int voteCount = keyboardInfoService.getTagVoteCount(keyboardId, tagId);
                sendJsonResponse(response, true, "태그 투표가 " + (isUpvote ? "추천" : "비추천") + "으로 변경되었습니다.", voteCount);
            } else {
                sendJsonResponse(response, false, "태그 투표 변경에 실패했습니다.");
            }
        } catch (NumberFormatException e) {
            sendJsonResponse(response, false, "잘못된 ID 형식입니다.");
        } catch (SQLException e) {
            e.printStackTrace();
            sendJsonResponse(response, false, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 키보드 태그 건의
     */
    private void suggestKeyboardTag(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 키보드 ID와 태그 이름, 건의 사유 가져오기
            String keyboardIdParam = request.getParameter("keyboardId");
            String tagName = request.getParameter("tagName");
            String reason = request.getParameter("reason");
            
            if (keyboardIdParam == null || tagName == null || reason == null 
                    || tagName.trim().isEmpty() || reason.trim().isEmpty()) {
                sendJsonResponse(response, false, "필수 입력값이 누락되었습니다.");
                return;
            }
            
            long keyboardId = Long.parseLong(keyboardIdParam);
            
            // 현재 로그인한 사용자 정보 가져오기
            HttpSession session = request.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            long userId = user.getUserUid();
            
            // XSS 방지 처리
            tagName = SecurityUtil.escapeXSS(tagName);
            reason = SecurityUtil.escapeXSS(reason);
            
            // 같은 이름의 태그가 이미 존재하는지 확인
            boolean tagExists = keyboardInfoService.isTagExistForKeyboard(keyboardId, tagName);
            
            if (tagExists) {
                sendJsonResponse(response, false, "이미 존재하는 태그입니다.");
                return;
            }
            
            // 태그 건의 추가
            boolean success = keyboardInfoService.suggestKeyboardTag(keyboardId, tagName, userId, reason);
            
            if (success) {
                sendJsonResponse(response, true, "태그가 관리자 검토를 위해 제출되었습니다.");
            } else {
                sendJsonResponse(response, false, "태그 건의에 실패했습니다.");
            }
        } catch (NumberFormatException e) {
            sendJsonResponse(response, false, "잘못된 키보드 ID 형식입니다.");
        } catch (SQLException e) {
            e.printStackTrace();
            sendJsonResponse(response, false, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 키보드 별점 입력
     */
    private void addKeyboardScore(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 키보드 ID와 별점, 한줄평 가져오기
            String keyboardIdParam = request.getParameter("keyboardId");
            String scoreParam = request.getParameter("score");
            String review = request.getParameter("review");
            
            if (keyboardIdParam == null || scoreParam == null) {
                sendJsonResponse(response, false, "필수 입력값이 누락되었습니다.");
                return;
            }
            
            long keyboardId = Long.parseLong(keyboardIdParam);
            int scoreValue;
            
            try {
                scoreValue = Integer.parseInt(scoreParam);
                if (scoreValue < 1 || scoreValue > 5) {
                    sendJsonResponse(response, false, "별점은 1~5 사이의 값이어야 합니다.");
                    return;
                }
            } catch (NumberFormatException e) {
                sendJsonResponse(response, false, "잘못된 별점 형식입니다.");
                return;
            }
            
            // 현재 로그인한 사용자 정보 가져오기
            HttpSession session = request.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            long userId = user.getUserUid();
            
            // XSS 방지 처리
            if (review != null) {
                review = SecurityUtil.escapeXSS(review);
            }
            
            // 이미 별점을 남겼는지 확인
            KeyboardScoreDTO existingScore = keyboardInfoService.getUserKeyboardScore(keyboardId, userId);
            
            if (existingScore != null) {
                sendJsonResponse(response, false, "이미 별점을 남겼습니다. 별점을 수정하려면 수정 기능을 사용하세요.");
                return;
            }
            
            // 별점 추가
            boolean success = keyboardInfoService.addKeyboardScore(keyboardId, userId, scoreValue, review);
            
            if (success) {
                // 새로운 평균 별점 가져오기
                double averageScore = keyboardInfoService.getAverageKeyboardScore(keyboardId);
                int scoreCount = keyboardInfoService.getKeyboardScoreCount(keyboardId);
                
                sendJsonResponse(response, true, "별점이 등록되었습니다.", averageScore, scoreCount);
            } else {
                sendJsonResponse(response, false, "별점 등록에 실패했습니다.");
            }
        } catch (NumberFormatException e) {
            sendJsonResponse(response, false, "잘못된 ID 형식입니다.");
        } catch (SQLException e) {
            e.printStackTrace();
            sendJsonResponse(response, false, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 키보드 별점 변경
     */
    private void updateKeyboardScore(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 키보드 ID와 별점, 한줄평 가져오기
            String keyboardIdParam = request.getParameter("keyboardId");
            String scoreParam = request.getParameter("score");
            String review = request.getParameter("review");
            
            if (keyboardIdParam == null || scoreParam == null) {
                sendJsonResponse(response, false, "필수 입력값이 누락되었습니다.");
                return;
            }
            
            long keyboardId = Long.parseLong(keyboardIdParam);
            int scoreValue;
            
            try {
                scoreValue = Integer.parseInt(scoreParam);
                if (scoreValue < 1 || scoreValue > 5) {
                    sendJsonResponse(response, false, "별점은 1~5 사이의 값이어야 합니다.");
                    return;
                }
            } catch (NumberFormatException e) {
                sendJsonResponse(response, false, "잘못된 별점 형식입니다.");
                return;
            }
            
            // 현재 로그인한 사용자 정보 가져오기
            HttpSession session = request.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            long userId = user.getUserUid();
            
            // XSS 방지 처리
            if (review != null) {
                review = SecurityUtil.escapeXSS(review);
            }
            
            // 이미 별점을 남겼는지 확인
            KeyboardScoreDTO existingScore = keyboardInfoService.getUserKeyboardScore(keyboardId, userId);
            
            if (existingScore == null) {
                sendJsonResponse(response, false, "먼저 별점을 등록해야 합니다.");
                return;
            }
            
            // 별점 수정
            boolean success = keyboardInfoService.updateKeyboardScore(keyboardId, userId, scoreValue, review);
            
            if (success) {
                // 새로운 평균 별점 가져오기
                double averageScore = keyboardInfoService.getAverageKeyboardScore(keyboardId);
                int scoreCount = keyboardInfoService.getKeyboardScoreCount(keyboardId);
                
                sendJsonResponse(response, true, "별점이 수정되었습니다.", averageScore, scoreCount);
            } else {
                sendJsonResponse(response, false, "별점 수정에 실패했습니다.");
            }
        } catch (NumberFormatException e) {
            sendJsonResponse(response, false, "잘못된 ID 형식입니다.");
        } catch (SQLException e) {
            e.printStackTrace();
            sendJsonResponse(response, false, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * AJAX 요청인지 확인
     */
    private boolean isAjaxRequest(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    /**
     * JSON 응답 전송 (성공/실패 메시지만)
     */
    private void sendJsonResponse(HttpServletResponse response, boolean success, String message) 
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"success\": " + success + ", \"message\": \"" + message + "\"}");
    }

    /**
     * JSON 응답 전송 (추가 데이터 포함)
     */
    private void sendJsonResponse(HttpServletResponse response, boolean success, String message, double data) 
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"success\": " + success + ", \"message\": \"" + message + 
                "\", \"data\": " + data + "}");
    }

    /**
     * JSON 응답 전송 (추가 데이터 2개 포함)
     */
    private void sendJsonResponse(HttpServletResponse response, boolean success, String message, 
            double data1, int data2) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"success\": " + success + ", \"message\": \"" + message + 
                "\", \"averageScore\": " + data1 + ", \"scoreCount\": " + data2 + "}");
    }
}
