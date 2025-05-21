package presentation.controller.page.board;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import business.service.chatboard.ChatboardService;
import dto.board.ChatboardDTO;
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
 * 익명 게시판 관련 요청을 처리하는 컨트롤러
 * URL 패턴: /chatboard.do 형식 지원
 */
@WebServlet({"/chatboard/*", "/chatboard.do"})
public class ChatboardController extends HttpServlet implements Controller {
    private static final long serialVersionUID = 1L;
    private ChatboardService chatboardService;
    private util.web.RequestRouter router;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.chatboardService = new ChatboardService();
        
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
            result.put("message", "채팅게시판 API");
            return result;
        });
        
        router.getJson("/list", (req, res) -> {
            List<ChatboardDTO> chatList = chatboardService.getAllChats();
            Map<String, Object> result = new HashMap<>();
            result.put("chatList", chatList);
            return result;
        });
        
        // POST 요청 JSON 라우터 설정
        router.postJson("/post", (req, res) -> {
            // 로그인 확인
            HttpSession session = req.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            
            if (user == null) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "로그인이 필요합니다.");
                return errorResult;
            }            // JSON 요청 바디 읽기
            StringBuilder sb = new StringBuilder();
            String line;
            try (BufferedReader reader = req.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "요청 데이터를 읽는 중 오류가 발생했습니다: " + e.getMessage());
                return errorResult;
            }
            
            // JSON 파싱
            JsonObject jsonRequest = new Gson().fromJson(sb.toString(), JsonObject.class);
            String content = jsonRequest.has("content") ? jsonRequest.get("content").getAsString() : "";
            String clientIp = IpUtil.getClientIpAddr(req);

            // 내용 유효성 검사
            if (content == null || content.trim().isEmpty()) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "내용을 입력해주세요.");
                return errorResult;
            }

            // ChatboardDTO 생성 및 설정
            ChatboardDTO chat = new ChatboardDTO();
            chat.setChatboardTitle(content);  // content를 title 필드에 저장
            chat.setChatboardAuthorIp(clientIp);
            chat.setUserUid(user.getUserUid());
            
            // 채팅 등록
            boolean result = chatboardService.postChat(chat);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", result);
            
            if (result) {
                String nickname = generateConsistentNickname(user.getUserUid());
                response.put("message", "게시글이 등록되었습니다.");
                response.put("chatId", chat.getChatboardUid());
                response.put("nickname", nickname);
            } else {
                response.put("message", "게시글 등록에 실패했습니다.");
            }
            
            return response;
        });
    }
    
    /**
     * JSON 응답 전송
     */
    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print(new Gson().toJson(data));
        out.flush();
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // API 요청인지 먼저 확인 (pathInfo 있는 요청은 API 요청으로 간주)
        String pathInfo = request.getPathInfo();
        
        // pathInfo가 있으면 API 요청으로 간주하고 Router를 통해 처리 시도
        if (pathInfo != null) {
            boolean handled = router.handleGetJson(request, response);
            if (handled) {
                // Router가 요청을 처리했으므로 메서드 종료
                return;
            }
        }
        
        // .do 요청일 경우 JSON 응답으로 처리
        String requestURI = request.getRequestURI();
        if (requestURI != null && requestURI.endsWith(".do")) {
            // JSON 응답 처리
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "채팅게시판 API");
            sendJsonResponse(response, result);
            return;
        }
        
        String action = request.getParameter("action");
        
        if (action == null || action.equals("list")) {
            // 채팅 목록 조회
            getAllChats(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // API 요청인지 먼저 확인 (pathInfo 있는 요청은 API 요청으로 간주)
        String pathInfo = request.getPathInfo();
        
        // pathInfo가 있으면 API 요청으로 간주하고 Router를 통해 처리 시도
        if (pathInfo != null) {
            boolean handled = router.handlePostJson(request, response);
            if (handled) {
                // Router가 요청을 처리했으므로 메서드 종료
                return;
            }
        }
        
        String action = request.getParameter("action");
        
        if (action == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } else if (action.equals("post")) {
            // 채팅 등록
            postChat(request, response);
        } else if (action.equals("update")) {
            // 채팅 수정
            updateChatById(request, response);
        } else if (action.equals("delete")) {
            // 채팅 삭제
            deleteChatById(request, response);
        } else if (action.equals("report")) {
            // 불량 채팅 신고
            reportChat(request, response);
        } else if (action.equals("penalty")) {
            // 불량 이용자 제재
            updateUserPenaltyStatusByUserId(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     * 모든 채팅 메시지 조회
     */
    private void getAllChats(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<ChatboardDTO> chatList = chatboardService.getAllChats();
        
        // JSON 응답으로 채팅 데이터 반환
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("chatList", chatList);
        
        sendJsonResponse(response, result);
    }
    
    /**
     * 채팅 메시지 등록
     */    private void postChat(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            sendJsonResponse(response, false, "로그인이 필요합니다.");
            return;
        }        // JSON 요청 바디 읽기
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            sendJsonResponse(response, false, "요청 데이터를 읽는 중 오류가 발생했습니다: " + e.getMessage());
            return;
        }

        // JSON 파싱
        JsonObject jsonRequest = new Gson().fromJson(sb.toString(), JsonObject.class);
        String content = jsonRequest.has("content") ? jsonRequest.get("content").getAsString() : "";
        String clientIp = IpUtil.getClientIpAddr(request);
        
        if (content == null || content.trim().isEmpty()) {
            sendJsonResponse(response, false, "내용을 입력해주세요.");
            return;
        }
        
        ChatboardDTO chat = new ChatboardDTO(content, clientIp, user.getUserUid());
        
        boolean result = chatboardService.postChat(chat);
        
        if (result) {
            // 사용자 ID를 기반으로 일관된 익명 닉네임 생성
            // 동일 사용자는 항상 같은 닉네임을 갖게 됨
            String anonymousNickname = generateConsistentNickname(user.getUserUid());
            chat.setAnonymousNickname(anonymousNickname);
            
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": true, \"message\": \"메시지가 등록되었습니다.\", " +
                        "\"id\": " + chat.getChatboardUid() + ", " +
                        "\"nickname\": \"" + anonymousNickname + "\"}");
            } else {
                response.sendRedirect(request.getContextPath() + "/chatboard");
            }
        } else {
            sendJsonResponse(response, false, "메시지 등록에 실패했습니다.");
        }
    }
    
    /**
     * 사용자 ID를 기반으로 일관된 닉네임 생성
     */
    private String generateConsistentNickname(long userId) {
        // 사용자 ID를 시드로 사용하여 일관된 결과 생성
        int hash = (int)((userId * 31) % 0xffffff);
        return "익명_" + Integer.toHexString(hash);
    }
    
    /**
     * 채팅 메시지 수정
     */
    private void updateChatById(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            sendJsonResponse(response, false, "로그인이 필요합니다.");
            return;
        }
        
        try {
            long chatId = Long.parseLong(request.getParameter("id"));
            String content = request.getParameter("content");
            
            if (content == null || content.trim().isEmpty()) {
                sendJsonResponse(response, false, "내용을 입력해주세요.");
                return;
            }
            
            ChatboardDTO chat = new ChatboardDTO();
            chat.setChatboardUid(chatId);
            chat.setChatboardTitle(content);
            
            boolean result = chatboardService.updateChatById(chat, user.getUserUid(), user.getUserAuthority());
            
            if (result) {
                sendJsonResponse(response, true, "메시지가 수정되었습니다.");
            } else {
                sendJsonResponse(response, false, "메시지 수정에 실패했습니다. 권한을 확인해주세요.");
            }
        } catch (NumberFormatException e) {
            sendJsonResponse(response, false, "잘못된 메시지 ID입니다.");
        }
    }
    
    /**
     * 채팅 메시지 삭제
     */
    private void deleteChatById(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            sendJsonResponse(response, false, "로그인이 필요합니다.");
            return;
        }
        
        try {
            long chatId = Long.parseLong(request.getParameter("id"));
            boolean result = chatboardService.deleteChatById(chatId, user.getUserUid(), user.getUserAuthority());
            
            if (result) {
                sendJsonResponse(response, true, "메시지가 삭제되었습니다.");
            } else {
                sendJsonResponse(response, false, "메시지 삭제에 실패했습니다. 권한을 확인해주세요.");
            }
        } catch (NumberFormatException e) {
            sendJsonResponse(response, false, "잘못된 메시지 ID입니다.");
        }
    }
    
    /**
     * 불량 채팅 신고
     */
    private void reportChat(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            sendJsonResponse(response, false, "로그인이 필요합니다.");
            return;
        }
        
        try {
            long chatId = Long.parseLong(request.getParameter("id"));
            String reportReason = request.getParameter("reason");
            String reportCategory = request.getParameter("category");
            
            if (reportReason == null || reportReason.trim().isEmpty()) {
                sendJsonResponse(response, false, "신고 사유를 입력해주세요.");
                return;
            }
            
            if (reportCategory == null || reportCategory.trim().isEmpty() || 
                !isValidCategory(reportCategory)) {
                reportCategory = "spam_ad"; // 기본값
            }
            
            boolean result = chatboardService.reportChat(chatId, user.getUserUid(), reportReason, reportCategory);
            
            if (result) {
                sendJsonResponse(response, true, "신고가 접수되었습니다.");
            } else {
                sendJsonResponse(response, false, "신고 처리 중 오류가 발생했습니다.");
            }
        } catch (NumberFormatException e) {
            sendJsonResponse(response, false, "잘못된 메시지 ID입니다.");
        }
    }
    
    /**
     * 불량 이용자 제재 (관리자 전용)
     */
    private void updateUserPenaltyStatusByUserId(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 로그인 및 관리자 권한 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null || !("admin".equals(user.getUserAuthority()) || "armband".equals(user.getUserAuthority()))) {
            sendJsonResponse(response, false, "관리자 권한이 필요합니다.");
            return;
        }
        
        try {
            long targetUserId = Long.parseLong(request.getParameter("userId"));
            String penaltyType = request.getParameter("penaltyType");
            String reason = request.getParameter("reason");
            
            if (reason == null || reason.trim().isEmpty()) {
                sendJsonResponse(response, false, "제재 사유를 입력해주세요.");
                return;
            }
            
            int duration = 7; // 기본 7일
            try {
                if (request.getParameter("duration") != null) {
                    duration = Integer.parseInt(request.getParameter("duration"));
                }
            } catch (NumberFormatException e) {
                // 기본값 사용
            }
            
            boolean result = chatboardService.updateUserPenaltyStatus(
                targetUserId, penaltyType, duration, reason, user.getUserUid(), user.getUserAuthority()
            );
            
            if (result) {
                sendJsonResponse(response, true, "이용자 제재가 처리되었습니다.");
            } else {
                sendJsonResponse(response, false, "이용자 제재 처리 중 오류가 발생했습니다.");
            }
        } catch (NumberFormatException e) {
            sendJsonResponse(response, false, "잘못된 사용자 ID입니다.");
        }
    }
    
    /**
     * JSON 응답 헬퍼 메서드
     */
    private void sendJsonResponse(HttpServletResponse response, boolean success, String message) 
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // GSON 라이브러리 사용
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("success", success);
        jsonResponse.addProperty("message", message);
        
        Gson gson = new Gson();
        response.getWriter().write(gson.toJson(jsonResponse));
    }
    
    /**
     * JSON 문자열 이스케이프
     */
    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    /**
     * 신고 카테고리 유효성 검사
     */
    private boolean isValidCategory(String category) {
        return category.equals("spam_ad") || 
               category.equals("profanity_hate_speech") ||
               category.equals("adult_content") ||
               category.equals("impersonation_fraud") ||
               category.equals("copyright_infringement");
    }
}
