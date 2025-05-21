package business.service.chatboard;

import java.sql.SQLException;
import java.util.List;

import dto.board.ChatboardDTO;
import repository.dao.board.ChatboardDAO;

public class ChatboardService {
    private final ChatboardDAO chatboardDAO;
    
    public ChatboardService() {
        this.chatboardDAO = new ChatboardDAO();
    }
    
    /**
     * 모든 채팅 메시지 조회
     */
    public List<ChatboardDTO> getAllChats() {
        try {
            return chatboardDAO.getAllChats();
        } catch (SQLException e) {
            return null;
        }
    }
    
    /**
     * 채팅 ID로 작성자 확인
     */
    public long getUserIdByChatId(long chatId) {
        try {
            return chatboardDAO.getUserIdByChatId(chatId);
        } catch (SQLException e) {
            return -1;
        }
    }
    
    /**
     * 채팅 메시지 등록
     */
    public boolean postChat(ChatboardDTO chat) {
        try {
            // 간단한 유효성 검사
            if (chat.getChatboardTitle() == null || chat.getChatboardTitle().trim().isEmpty()) {
                return false;
            }
            
            // 내용이 너무 길면 자르기
            if (chat.getChatboardTitle().length() > 200) {
                chat.setChatboardTitle(chat.getChatboardTitle().substring(0, 200));
            }
            
            return chatboardDAO.postChat(chat);
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * 채팅 메시지 수정 (본인 또는 관리자)
     */
    public boolean updateChatById(ChatboardDTO chat, long userId, String userAuthority) {
        try {
            long authorId = chatboardDAO.getUserIdByChatId(chat.getChatboardUid());
            
            // 본인이거나 관리자/매니저인 경우만 수정 가능
            if (authorId == userId || "admin".equals(userAuthority) || "armband".equals(userAuthority)) {
                return chatboardDAO.updateChatById(chat);
            }
            
            return false;
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * 채팅 메시지 삭제 (본인 또는 관리자)
     */
    public boolean deleteChatById(long chatId, long userId, String userAuthority) {
        try {
            long authorId = chatboardDAO.getUserIdByChatId(chatId);
            
            // 본인이거나 관리자/매니저인 경우만 삭제 가능
            if (authorId == userId || "admin".equals(userAuthority) || "armband".equals(userAuthority)) {
                return chatboardDAO.deleteChatById(chatId);
            }
            
            return false;
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * 불량 채팅 신고
     */
    public boolean reportChat(long chatId, long reporterId, String reason, String category) {
        try {
            return chatboardDAO.reportChat(chatId, reporterId, reason, category);
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * 불량 이용자 제재 (관리자 전용)
     */
    public boolean updateUserPenaltyStatus(long userId, String penaltyType, int duration, 
                                          String reason, long adminId, String adminAuthority) {
        // 관리자 권한 확인
        if (!("admin".equals(adminAuthority) || "armband".equals(adminAuthority))) {
            return false;
        }
        
        try {
            return chatboardDAO.updateUserPenaltyStatus(userId, penaltyType, duration, reason, adminId);
        } catch (SQLException e) {
            return false;
        }
    }
}