package presentation.controller.page.database;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import business.service.database.KeyboardInfoService;
import dto.keyboard.KeyboardInfoDTO;
import dto.keyboard.KeyboardScoreDTO;
import dto.keyboard.KeyboardTagDTO;
import dto.user.UserDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import presentation.controller.page.Controller;
import util.web.IpUtil;
import util.web.RequestRouter;

/**
 * 키보드 정보 컨트롤러
 * 관리자가 등록한 키보드 정보를 볼 수 있는 페이지
 * 관심 키보드 스크랩, 키보드 한줄평, 태그 설정, 별점 입력 가능
 * URL 패턴: /keyboard.do 형식 지원
 */
@WebServlet({"/keyboard/*", "/keyboard.do"})
public class KeyboardInfoController extends HttpServlet implements Controller {
    private static final long serialVersionUID = 1L;
    private KeyboardInfoService keyboardInfoService;
    private util.web.RequestRouter router;
    private final Gson gson = new Gson();

    public KeyboardInfoController() {
        keyboardInfoService = new KeyboardInfoService();
    }
    
    @Override
    public void init() throws ServletException {
        super.init();
        
        // 라우터 설정
        initRequestRouter();
    }
      /**
     * 요청 라우터 초기화
     */
    private void initRequestRouter() {
        router = new util.web.RequestRouter();
        
        // GET 요청 JSON 라우터 설정
        router.getJson("/", (req, res) -> {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "키보드 정보 API");
            return result;
        });
        
        router.getJson("/list", (req, res) -> {
            // 페이지네이션을 위한 파라미터 처리
            int page = 1;
            int pageSize = 12;
            
            String pageStr = req.getParameter("page");
            if (pageStr != null && !pageStr.isEmpty()) {
                try {
                    page = Integer.parseInt(pageStr);
                    if (page < 1) page = 1;
                } catch (NumberFormatException e) {
                    // 숫자가 아닌 값이 들어온 경우 기본값 사용
                }
            }
            
            // 키보드 목록 가져오기
            List<KeyboardInfoDTO> keyboardList = keyboardInfoService.getAllKeyboardInfos(page, pageSize);
            int totalKeyboards = keyboardInfoService.getTotalKeyboardCount();
            int totalPages = (int) Math.ceil(totalKeyboards / (double) pageSize);
            
            Map<String, Object> result = new HashMap<>();
            result.put("keyboardList", keyboardList);
            result.put("currentPage", page);
            result.put("totalPages", totalPages);
            result.put("totalCount", totalKeyboards);
            
            return result;
        });
    }
    
    /**
     * JSON 응답 전송
     */
    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(data));
        out.flush();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // .do 요청일 경우 JSON 응답으로 처리
        String requestURI = request.getRequestURI();
        if (requestURI != null && requestURI.endsWith(".do")) {
            // 라우터로 처리 시도
            if (router.handle(request, response)) {
                return;  // 라우터가 요청을 처리함
            }
            
            // 기본 JSON 응답
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "키보드 정보 API");
            sendJsonResponse(response, result);
            return;
        }
        
        // 라우터로 처리 시도
        if (router.handle(request, response)) {
            return;  // 라우터가 요청을 처리함
        }
        
        // 기존 로직 처리
        String action = request.getParameter("action");
        
        if (action == null) {
            action = "list"; // 기본 액션은 목록 보기
        }
        
        try {
            switch (action) {
                case "list":
                    listKeyboardInfos(request, response);
                    break;
                case "view":
                    viewKeyboardInfo(request, response);
                    break;
                case "search":
                    searchKeyboardInfos(request, response);
                    break;
                default:
                    listKeyboardInfos(request, response);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // .do 요청일 경우 JSON 응답으로 처리
        String requestURI = request.getRequestURI();
        if (requestURI != null && requestURI.endsWith(".do")) {
            // 라우터로 처리 시도
            if (router.handle(request, response)) {
                return;  // 라우터가 요청을 처리함
            }
            
            // 기본 JSON 응답
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "키보드 정보 API - POST");
            sendJsonResponse(response, result);
            return;
        }
        
        // 라우터로 처리 시도
        if (router.handle(request, response)) {
            return;  // 라우터가 요청을 처리함
        }
        
        // 기존 로직 처리
        String action = request.getParameter("action");
        
        if (action == null) {
            response.sendRedirect("keyboard.do");
            return;
        }
        
        try {
            switch (action) {
                case "addComment":
                    addKeyboardComment(request, response);
                    break;
                case "deleteComment":
                    deleteKeyboardComment(request, response);
                    break;
                case "scrap":
                    scrapKeyboardInfo(request, response);
                    break;
                case "voteTag":
                    voteKeyboardTag(request, response);
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
                    response.sendRedirect("keyboard.do");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 키보드 전체정보 열람
     */
    private void listKeyboardInfos(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 페이지네이션을 위한 파라미터 처리
        int page = 1;
        int pageSize = 12;
        
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isEmpty()) {
            try {
                page = Integer.parseInt(pageStr);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                // 숫자가 아닌 값이 들어온 경우 기본값 사용
            }
        }
        
        // 키보드 목록 가져오기
        List<KeyboardInfoDTO> keyboardList = keyboardInfoService.getAllKeyboardInfos(page, pageSize);
        int totalKeyboards = keyboardInfoService.getTotalKeyboardCount();
        int totalPages = (int) Math.ceil(totalKeyboards / (double) pageSize);
        
        // 필터 옵션 데이터 가져오기
        List<String> manufacturers = keyboardInfoService.getAllManufacturers();
        List<String> switchTypes = keyboardInfoService.getAllSwitchTypes();
        List<String> layoutTypes = keyboardInfoService.getAllLayoutTypes();
        List<String> connectTypes = keyboardInfoService.getAllConnectTypes();
        
        // 요청 속성에 설정
        request.setAttribute("keyboardList", keyboardList);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("manufacturers", manufacturers);
        request.setAttribute("switchTypes", switchTypes);
        request.setAttribute("layoutTypes", layoutTypes);
        request.setAttribute("connectTypes", connectTypes);
        
        // 목록 페이지로 포워딩
        request.getRequestDispatcher("/view/pages/keyboard/list.jsp").forward(request, response);
    }

    /**
     * 키보드 상세정보 열람
     */
    private void viewKeyboardInfo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyboardIdStr = request.getParameter("id");
        
        if (keyboardIdStr == null || keyboardIdStr.isEmpty()) {
            response.sendRedirect("keyboard.do");
            return;
        }
        
        try {
            long keyboardId = Long.parseLong(keyboardIdStr);
            
            // 키보드 정보 가져오기
            KeyboardInfoDTO keyboard = keyboardInfoService.getKeyboardInfoById(keyboardId);
            
            if (keyboard == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "존재하지 않는 키보드 정보입니다.");
                return;
            }
            
            // 한줄평 목록 가져오기
            List<KeyboardScoreDTO> comments = keyboardInfoService.getKeyboardComments(keyboardId);
            
            // 로그인 사용자 정보
            HttpSession session = request.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            
            // 태그 정보 가져오기 (로그인한 경우 사용자의 태그 투표 정보 포함)
            List<KeyboardTagDTO> tags = null;
            boolean isScraped = false;
            KeyboardScoreDTO userScore = null;
            
            if (user != null) {
                tags = keyboardInfoService.getKeyboardTagsWithVotes(keyboardId, user.getUserId());
                isScraped = keyboardInfoService.hasAlreadyScrapped(keyboardId, user.getUserId());
                userScore = keyboardInfoService.getUserScore(keyboardId, user.getUserId());
            } else {
                tags = keyboardInfoService.getKeyboardTagsWithVotes(keyboardId, 0);
            }
            
            // 요청 속성에 설정
            request.setAttribute("keyboard", keyboard);
            request.setAttribute("comments", comments);
            request.setAttribute("tags", tags);
            request.setAttribute("isScraped", isScraped);
            request.setAttribute("userScore", userScore);
            
            // 상세 페이지로 포워딩
            request.getRequestDispatcher("/view/pages/keyboard/view.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendRedirect("keyboard.do");
        }
    }

    /**
     * 키보드 정보 검색
     */
    private void searchKeyboardInfos(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 검색 파라미터 가져오기
        String keyword = request.getParameter("keyword");
        String manufacturer = request.getParameter("manufacturer");
        String switchType = request.getParameter("switchType");
        String layoutType = request.getParameter("layoutType");
        String connectType = request.getParameter("connectType");
        
        // 페이지네이션을 위한 파라미터 처리
        int page = 1;
        int pageSize = 12;
        
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isEmpty()) {
            try {
                page = Integer.parseInt(pageStr);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                // 숫자가 아닌 값이 들어온 경우 기본값 사용
            }
        }
        
        // 검색 결과 가져오기
        List<KeyboardInfoDTO> searchResults = keyboardInfoService.searchKeyboardInfosByCondition(
                keyword, manufacturer, switchType, layoutType, connectType, page, pageSize);
        
        int totalResults = keyboardInfoService.getFilteredKeyboardCount(
                keyword, manufacturer, switchType, layoutType, connectType);
        int totalPages = (int) Math.ceil(totalResults / (double) pageSize);
        
        // 필터 옵션 데이터 가져오기
        List<String> manufacturers = keyboardInfoService.getAllManufacturers();
        List<String> switchTypes = keyboardInfoService.getAllSwitchTypes();
        List<String> layoutTypes = keyboardInfoService.getAllLayoutTypes();
        List<String> connectTypes = keyboardInfoService.getAllConnectTypes();
        
        // 요청 속성에 설정
        request.setAttribute("keyboardList", searchResults);
        request.setAttribute("keyword", keyword);
        request.setAttribute("selectedManufacturer", manufacturer);
        request.setAttribute("selectedSwitchType", switchType);
        request.setAttribute("selectedLayoutType", layoutType);
        request.setAttribute("selectedConnectType", connectType);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("manufacturers", manufacturers);
        request.setAttribute("switchTypes", switchTypes);
        request.setAttribute("layoutTypes", layoutTypes);
        request.setAttribute("connectTypes", connectTypes);
        request.setAttribute("isSearch", true);
        
        // 검색 결과 페이지로 포워딩 (목록 페이지와 동일한 JSP 사용)
        request.getRequestDispatcher("/view/pages/keyboard/list.jsp").forward(request, response);
    }

    /**
     * 키보드 한줄평 입력
     */
    private void addKeyboardComment(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"로그인이 필요합니다.\"}");
            return;
        }
        
        // 파라미터 가져오기
        String keyboardIdStr = request.getParameter("keyboardId");
        String scoreValueStr = request.getParameter("scoreValue");
        String review = request.getParameter("review");
        
        if (keyboardIdStr == null || scoreValueStr == null || review == null) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"필수 항목이 누락되었습니다.\"}");
            return;
        }
        
        try {
            long keyboardId = Long.parseLong(keyboardIdStr);
            int scoreValue = Integer.parseInt(scoreValueStr);
            
            if (scoreValue < 1 || scoreValue > 5) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"별점은 1~5 사이의 값이어야 합니다.\"}");
                return;
            }
            
            // 한줄평 DTO 생성
            KeyboardScoreDTO comment = new KeyboardScoreDTO();
            comment.setKeyboardId(keyboardId);
            comment.setUserId(user.getUserId());
            comment.setScoreValue(scoreValue);
            comment.setReview(review);
            
            // 한줄평 저장
            boolean success = keyboardInfoService.addKeyboardComment(comment);
            
            response.setContentType("application/json");
            if (success) {
                response.getWriter().write("{\"success\": true, \"message\": \"한줄평이 등록되었습니다.\"}");
            } else {
                response.getWriter().write("{\"success\": false, \"message\": \"한줄평 등록에 실패했습니다.\"}");
            }
        } catch (NumberFormatException e) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"잘못된 파라미터입니다.\"}");
        }
    }

    /**
     * 다른 이용자의 한줄평 삭제 (관리자 권한)
     */
    private void deleteKeyboardComment(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"로그인이 필요합니다.\"}");
            return;
        }
        
        // 파라미터 가져오기
        String commentIdStr = request.getParameter("commentId");
        String keyboardIdStr = request.getParameter("keyboardId");
        
        if (commentIdStr == null || keyboardIdStr == null) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"필수 항목이 누락되었습니다.\"}");
            return;
        }
        
        try {
            long commentId = Long.parseLong(commentIdStr);
            long keyboardId = Long.parseLong(keyboardIdStr);
            boolean success;
            
            // 관리자인 경우 모든 한줄평 삭제 가능, 일반 사용자는 자신의 한줄평만 삭제 가능
            if (user.getUserLevel() >= 3) {
                success = keyboardInfoService.deleteKeyboardCommentById(commentId, user.getUserId(), "admin");
            } else {
                success = keyboardInfoService.deleteKeyboardCommentById(commentId, user.getUserId());
            }
            
            response.setContentType("application/json");
            if (success) {
                response.getWriter().write("{\"success\": true, \"message\": \"한줄평이 삭제되었습니다.\"}");
            } else {
                response.getWriter().write("{\"success\": false, \"message\": \"한줄평 삭제에 실패했습니다.\"}");
            }
        } catch (NumberFormatException e) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"잘못된 파라미터입니다.\"}");
        }
    }

    /**
     * 키보드 정보 스크랩
     */
    private void scrapKeyboardInfo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"로그인이 필요합니다.\"}");
            return;
        }
        
        // 파라미터 가져오기
        String keyboardIdStr = request.getParameter("keyboardId");
        
        if (keyboardIdStr == null) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"키보드 ID가 필요합니다.\"}");
            return;
        }
        
        try {
            long keyboardId = Long.parseLong(keyboardIdStr);
            
            // 스크랩 상태 확인
            boolean isScraped = keyboardInfoService.hasAlreadyScrapped(keyboardId, user.getUserId());
            
            // 스크랩 처리
            boolean success = keyboardInfoService.scrapKeyboardInfo(keyboardId, user.getUserId());
            
            response.setContentType("application/json");
            if (success) {
                if (isScraped) {
                    response.getWriter().write("{\"success\": true, \"message\": \"스크랩이 취소되었습니다.\", \"status\": \"unscraped\"}");
                } else {
                    response.getWriter().write("{\"success\": true, \"message\": \"스크랩이 완료되었습니다.\", \"status\": \"scraped\"}");
                }
            } else {
                response.getWriter().write("{\"success\": false, \"message\": \"스크랩 처리에 실패했습니다.\"}");
            }
        } catch (NumberFormatException e) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"잘못된 키보드 ID입니다.\"}");
        }
    }

    /**
     * 키보드 태그 투표
     */
    private void voteKeyboardTag(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"로그인이 필요합니다.\"}");
            return;
        }
        
        // 파라미터 가져오기
        String keyboardIdStr = request.getParameter("keyboardId");
        String tagIdStr = request.getParameter("tagId");
        String voteType = request.getParameter("voteType");
        
        if (keyboardIdStr == null || tagIdStr == null || voteType == null) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"필수 항목이 누락되었습니다.\"}");
            return;
        }
        
        // 투표 타입 검증
        if (!voteType.equals("up") && !voteType.equals("down")) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"유효하지 않은 투표 타입입니다.\"}");
            return;
        }
        
        try {
            long keyboardId = Long.parseLong(keyboardIdStr);
            long tagId = Long.parseLong(tagIdStr);
            
            // 태그 투표 처리
            boolean success = keyboardInfoService.voteKeyboardTag(keyboardId, tagId, user.getUserId(), voteType);
            
            response.setContentType("application/json");
            if (success) {
                response.getWriter().write("{\"success\": true, \"message\": \"태그 투표가 처리되었습니다.\"}");
            } else {
                response.getWriter().write("{\"success\": false, \"message\": \"태그 투표 처리에 실패했습니다.\"}");
            }
        } catch (NumberFormatException e) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"잘못된 파라미터입니다.\"}");
        }
    }

    /**
     * 키보드 태그 제안 (새 태그 추가)
     */
    private void suggestKeyboardTag(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"로그인이 필요합니다.\"}");
            return;
        }
        
        // 파라미터 가져오기
        String keyboardIdStr = request.getParameter("keyboardId");
        String tagName = request.getParameter("tagName");
        
        if (keyboardIdStr == null || tagName == null || tagName.trim().isEmpty()) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"태그 이름이 필요합니다.\"}");
            return;
        }
        
        try {
            long keyboardId = Long.parseLong(keyboardIdStr);
            
            // 태그 제안 처리
            boolean success = keyboardInfoService.suggestKeyboardTag(tagName, keyboardId, user.getUserId());
            
            response.setContentType("application/json");
            if (success) {
                response.getWriter().write("{\"success\": true, \"message\": \"태그가 추가되었습니다.\"}");
            } else {
                response.getWriter().write("{\"success\": false, \"message\": \"태그 추가에 실패했습니다.\"}");
            }
        } catch (NumberFormatException e) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"잘못된 키보드 ID입니다.\"}");
        }
    }

    /**
     * 키보드 별점 입력
     */
    private void addKeyboardScore(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"로그인이 필요합니다.\"}");
            return;
        }
        
        // 파라미터 가져오기
        String keyboardIdStr = request.getParameter("keyboardId");
        String scoreValueStr = request.getParameter("scoreValue");
        String review = request.getParameter("review");
        
        if (keyboardIdStr == null || scoreValueStr == null) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"필수 항목이 누락되었습니다.\"}");
            return;
        }
        
        try {
            long keyboardId = Long.parseLong(keyboardIdStr);
            int scoreValue = Integer.parseInt(scoreValueStr);
            
            if (scoreValue < 1 || scoreValue > 5) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"별점은 1~5 사이의 값이어야 합니다.\"}");
                return;
            }
            
            // 별점 DTO 생성
            KeyboardScoreDTO score = new KeyboardScoreDTO();
            score.setKeyboardId(keyboardId);
            score.setUserId(user.getUserId());
            score.setScoreValue(scoreValue);
            score.setReview(review != null ? review : "");
            
            // 별점 저장
            boolean success = keyboardInfoService.addKeyboardScore(score);
            
            response.setContentType("application/json");
            if (success) {
                response.getWriter().write("{\"success\": true, \"message\": \"별점이 등록되었습니다.\"}");
            } else {
                response.getWriter().write("{\"success\": false, \"message\": \"별점 등록에 실패했습니다.\"}");
            }
        } catch (NumberFormatException e) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"잘못된 파라미터입니다.\"}");
        }
    }

    /**
     * 키보드 별점 변경
     */
    private void updateKeyboardScore(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"로그인이 필요합니다.\"}");
            return;
        }
        
        // 파라미터 가져오기
        String keyboardIdStr = request.getParameter("keyboardId");
        String scoreValueStr = request.getParameter("scoreValue");
        String review = request.getParameter("review");
        
        if (keyboardIdStr == null || scoreValueStr == null) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"필수 항목이 누락되었습니다.\"}");
            return;
        }
        
        try {
            long keyboardId = Long.parseLong(keyboardIdStr);
            int scoreValue = Integer.parseInt(scoreValueStr);
            
            if (scoreValue < 1 || scoreValue > 5) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"별점은 1~5 사이의 값이어야 합니다.\"}");
                return;
            }
            
            // 별점 DTO 생성
            KeyboardScoreDTO score = new KeyboardScoreDTO();
            score.setKeyboardId(keyboardId);
            score.setUserId(user.getUserId());
            score.setScoreValue(scoreValue);
            score.setReview(review != null ? review : "");
            
            // 별점 수정
            boolean success = keyboardInfoService.updateKeyboardScore(score);
            
            response.setContentType("application/json");
            if (success) {
                response.getWriter().write("{\"success\": true, \"message\": \"별점이 수정되었습니다.\"}");
            } else {
                response.getWriter().write("{\"success\": false, \"message\": \"별점 수정에 실패했습니다.\"}");
            }
        } catch (NumberFormatException e) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"잘못된 파라미터입니다.\"}");
        }
    }
}
