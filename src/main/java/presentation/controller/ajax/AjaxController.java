package presentation.controller.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import business.service.guide.GuideService;
import business.service.database.KeyboardInfoService;
import business.service.chatboard.ChatboardService;
import dto.keyboard.GuideDTO;
import dto.keyboard.KeyboardInfoDTO;
import dto.keyboard.KeyboardScoreDTO;
import dto.board.ChatboardDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import dto.user.UserDTO;

/**
 * 통합 AJAX 컨트롤러
 * 여러 기능에 공통적으로 필요한 AJAX 요청을 처리하는 컨트롤러
 */
public class AjaxController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private GuideService guideService;
    private KeyboardInfoService keyboardInfoService;
    private ChatboardService chatboardService;
    
    public AjaxController() {
        this.guideService = new GuideService();
        this.keyboardInfoService = new KeyboardInfoService();
        this.chatboardService = new ChatboardService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if (action == null) {
            sendErrorResponse(response, "요청 작업을 지정해주세요.");
            return;
        }
        
        try {
            switch (action) {
                case "getGuides":
                    getGuides(request, response);
                    break;
                case "searchGuides":
                    searchGuides(request, response);
                    break;
                case "getGuideDetail":
                    getGuideDetail(request, response);
                    break;
                case "getKeyboardInfos":
                    getKeyboardInfos(request, response);
                    break;
                case "getRecentChats":
                    getRecentChats(request, response);
                    break;
                default:
                    sendErrorResponse(response, "지원하지 않는 작업입니다: " + action);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, "서버 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if (action == null) {
            sendErrorResponse(response, "요청 작업을 지정해주세요.");
            return;
        }
        
        // 로그인 확인이 필요한 작업 체크
        if (requiresLogin(action)) {
            HttpSession session = request.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            
            if (user == null) {
                sendErrorResponse(response, "로그인이 필요합니다.");
                return;
            }
        }
        
        try {
            switch (action) {
                // 여기에 POST 요청을 처리하는 메서드 추가
                case "postChat":
                    postChat(request, response);
                    break;
                case "addKeyboardScore":
                    addKeyboardScore(request, response);
                    break;
                default:
                    sendErrorResponse(response, "지원하지 않는 작업입니다: " + action);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, "서버 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 해당 작업이 로그인이 필요한지 확인
     */
    private boolean requiresLogin(String action) {
        return action.equals("postChat") || 
               action.equals("addKeyboardScore") || 
               action.equals("addComment");
    }
    
    /**
     * 에러 응답 전송
     */
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        
        PrintWriter out = response.getWriter();
        out.print(new Gson().toJson(result));
        out.flush();
    }
    
    /**
     * 성공 응답 전송
     */
    private void sendSuccessResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", data);
        
        PrintWriter out = response.getWriter();
        out.print(new Gson().toJson(result));
        out.flush();
    }
    
    /**
     * 키보드 용어집 전체 목록 조회
     */
    private void getGuides(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<GuideDTO> guides = guideService.getAllGuides();
        sendSuccessResponse(response, guides);
    }
    
    /**
     * 키보드 용어집 검색
     */
    private void searchGuides(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String keyword = request.getParameter("keyword");
        List<GuideDTO> searchResults;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            searchResults = guideService.searchGuidesByKeyword(keyword);
        } else {
            searchResults = guideService.getAllGuides();
        }
        
        sendSuccessResponse(response, searchResults);
    }
    
    /**
     * 키보드 용어 상세 정보 조회
     */
    private void getGuideDetail(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String guideIdStr = request.getParameter("id");
        
        try {
            long guideId = Long.parseLong(guideIdStr);
            GuideDTO guide = guideService.getGuideById(guideId);
            
            if (guide != null) {
                sendSuccessResponse(response, guide);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                sendErrorResponse(response, "해당 ID의 용어를 찾을 수 없습니다.");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            sendErrorResponse(response, "유효하지 않은 ID 형식입니다.");
        }
    }
    
    /**
     * 키보드 정보 목록 조회
     */
    private void getKeyboardInfos(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 페이지네이션을 위한 파라미터 처리
        int page = 1;
        int pageSize = 12;
        
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isEmpty()) {
            try {
                page = Integer.parseInt(pageStr);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                // 기본값 사용
            }
        }
        
        // 키보드 목록 가져오기
        List<KeyboardInfoDTO> keyboardList = keyboardInfoService.getAllKeyboardInfos(page, pageSize);
        int totalKeyboards = keyboardInfoService.getTotalKeyboardCount();
        int totalPages = (int) Math.ceil(totalKeyboards / (double) pageSize);
        
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("keyboardList", keyboardList);
        resultData.put("currentPage", page);
        resultData.put("totalPages", totalPages);
        resultData.put("totalItems", totalKeyboards);
        
        sendSuccessResponse(response, resultData);
    }
    
    /**
     * 최근 채팅 메시지 조회
     */
    private void getRecentChats(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int limit = 20; // 기본값
        
        String limitStr = request.getParameter("limit");
        if (limitStr != null && !limitStr.isEmpty()) {
            try {
                limit = Integer.parseInt(limitStr);
                if (limit < 1) limit = 20;
                if (limit > 50) limit = 50; // 최대 50개까지만 허용
            } catch (NumberFormatException e) {
                // 기본값 사용
            }
        }
        
        sendSuccessResponse(response, chatboardService.getRecentChats(limit));
    }
    
    /**
     * 채팅 메시지 추가
     */
    private void postChat(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String message = request.getParameter("message");
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (message == null || message.trim().isEmpty()) {
            sendErrorResponse(response, "메시지를 입력해주세요.");
            return;
        }
        
        if (user == null) {
            sendErrorResponse(response, "로그인이 필요합니다.");
            return;
        }
        
        // ChatboardDTO 생성
        ChatboardDTO chat = new ChatboardDTO();
        chat.setChatboardTitle(message);
        chat.setUserUid(user.getUserUid());
        chat.setChatboardAuthorIp(request.getRemoteAddr());
        
        boolean success = chatboardService.postChat(chat);
        
        if (success) {
            sendSuccessResponse(response, "메시지가 성공적으로 추가되었습니다.");
        } else {
            sendErrorResponse(response, "메시지 추가에 실패했습니다.");
        }
    }
    
    /**
     * 키보드 별점 추가
     */
    private void addKeyboardScore(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 파라미터 가져오기
        String keyboardIdStr = request.getParameter("keyboardId");
        String scoreValueStr = request.getParameter("scoreValue");
        String review = request.getParameter("review");
        
        // 세션에서 사용자 정보 가져오기
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            sendErrorResponse(response, "로그인이 필요합니다.");
            return;
        }
        
        // 필수 파라미터 검증
        if (keyboardIdStr == null || scoreValueStr == null) {
            sendErrorResponse(response, "필수 정보가 누락되었습니다.");
            return;
        }
        
        try {
            // 변환 작업
            long keyboardId = Long.parseLong(keyboardIdStr);
            int scoreValue = Integer.parseInt(scoreValueStr);
            
            // 점수 범위 확인 (1-5점 사이)
            if (scoreValue < 1 || scoreValue > 5) {
                sendErrorResponse(response, "별점은 1-5점 사이여야 합니다.");
                return;
            }
            
            // KeyboardScoreDTO 생성
            KeyboardScoreDTO score = new KeyboardScoreDTO();
            score.setKeyboardId(keyboardId);
            score.setUserId(user.getUserUid());
            score.setScoreValue(scoreValue);
            
            if (review != null && !review.trim().isEmpty()) {
                // 리뷰 길이 제한 (예: 200자)
                if (review.length() > 200) {
                    review = review.substring(0, 200);
                }
                score.setReview(review);
            }
            
            // 기존 별점이 있는지 확인
            KeyboardScoreDTO existingScore = keyboardInfoService.getUserScore(keyboardId, user.getUserUid());
            boolean success;
            
            if (existingScore != null) {
                // 기존 별점이 있으면 업데이트
                score.setScoreId(existingScore.getScoreId());
                success = keyboardInfoService.updateKeyboardScore(score);
            } else {
                // 새 별점 추가
                success = keyboardInfoService.addKeyboardScore(score);
            }
            
            if (success) {
                Map<String, Object> resultData = new HashMap<>();
                resultData.put("message", "별점이 성공적으로 저장되었습니다.");
                resultData.put("scoreValue", scoreValue);
                
                sendSuccessResponse(response, resultData);
            } else {
                sendErrorResponse(response, "별점 저장에 실패했습니다.");
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(response, "잘못된 형식의 입력 데이터입니다.");
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, "서버 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
