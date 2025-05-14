package presentation.controller.page.board;

import java.io.IOException;
import java.util.List;

import business.service.chatboard.ChatboardService;
import dto.board.ChatboardDTO;
import dto.user.UserDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import presentation.controller.page.Controller;
import util.web.IpUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class ChatboardController implements Controller {
    private final ChatboardService chatboardService;
    
    public ChatboardController() {
        this.chatboardService = new ChatboardService();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
        
        // AJAX 요청인 경우 JSON 응답
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            // GSON 라이브러리 사용
            Gson gson = new Gson();
            JsonArray jsonArray = new JsonArray();
            
            for (ChatboardDTO chat : chatList) {
                JsonObject chatJson = new JsonObject();
                chatJson.addProperty("id", chat.getChatboardUid());
                chatJson.addProperty("content", chat.getChatboardTitle());
                chatJson.addProperty("writetime", chat.getChatboardWritetime().toString());
                chatJson.addProperty("nickname", chat.getAnonymousNickname());
                jsonArray.add(chatJson);
            }
            
            response.getWriter().write(gson.toJson(jsonArray));
        } else {
            // 일반 요청인 경우 JSP로 포워딩
            request.setAttribute("chatList", chatList);
            request.getRequestDispatcher("/WEB-INF/views/board/chatboard.jsp").forward(request, response);
        }
    }
    
    /**
     * 채팅 메시지 등록
     */
    private void postChat(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": false, \"message\": \"로그인이 필요합니다.\"}");
            } else {
                response.sendRedirect(request.getContextPath() + "/login?redirect=chatboard");
            }
            return;
        }
        
        String content = request.getParameter("content");
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
