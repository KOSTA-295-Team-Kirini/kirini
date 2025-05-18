package repository.dao.board;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import dto.board.ChatboardDTO;
import util.db.DBConnectionUtil;
import util.logging.LoggerConfig;

public class ChatboardDAO {
    private static final Logger logger = LoggerConfig.getLogger(ChatboardDAO.class);
    private Connection conn = null;
    private PreparedStatement pstmt = null;
    private ResultSet rs = null;
    
    // 모든 채팅 메시지 조회
    public List<ChatboardDTO> getAllChats() throws SQLException {
        List<ChatboardDTO> chatList = new ArrayList<>();
        String sql = "SELECT c.* FROM chatboard c " + 
                     "WHERE c.chatboard_deleted = 'maintained' " +
                     "ORDER BY c.chatboard_writetime DESC " +
                     "LIMIT 100"; // 최근 100개만 가져오기
        
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                ChatboardDTO chat = new ChatboardDTO();
                chat.setChatboardUid(rs.getLong("chatboard_uid"));
                chat.setChatboardTitle(rs.getString("chatboard_title"));
                chat.setChatboardWritetime(rs.getTimestamp("chatboard_writetime").toLocalDateTime());
                if (rs.getTimestamp("chatboard_modify_time") != null) {
                    chat.setChatboardModifyTime(rs.getTimestamp("chatboard_modify_time").toLocalDateTime());
                }
                chat.setChatboardDeleted(rs.getString("chatboard_deleted"));
                chat.setUserUid(rs.getLong("user_uid"));
                
                // Controller에서 사용하는 방식대로 닉네임 생성
                int hash = (int)((chat.getUserUid() * 31) % 0xffffff);
                chat.setAnonymousNickname("익명_" + Integer.toHexString(hash));
                
                chatList.add(chat);
            }
            
            return chatList;
        } finally {
            closeResources();
        }
    }
    
    // 채팅 ID로 작성자 확인
    public long getUserIdByChatId(long chatId) throws SQLException {
        String sql = "SELECT user_uid FROM chatboard WHERE chatboard_uid = ?";
        
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, chatId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getLong("user_uid");
            }
            
            return -1; // 해당 채팅이 없는 경우
        } finally {
            closeResources();
        }
    }
    
    // 채팅 메시지 등록
    public boolean postChat(ChatboardDTO chat) throws SQLException {
        String sql = "INSERT INTO chatboard (chatboard_title, chatboard_writetime, " +
                     "chatboard_author_ip, chatboard_deleted, user_uid) " +
                     "VALUES (?, NOW(), ?, 'maintained', ?)";
        
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, chat.getChatboardTitle());
            pstmt.setString(2, chat.getChatboardAuthorIp());
            pstmt.setLong(3, chat.getUserUid());
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    chat.setChatboardUid(rs.getLong(1));
                }
                logger.info("새 채팅 메시지 등록 성공: " + chat.getChatboardTitle().substring(0, Math.min(chat.getChatboardTitle().length(), 20)) + "...");
                return true;
            }
            
            logger.warning("채팅 메시지 등록 실패");
            return false;
        } catch (SQLException e) {
            logger.severe("채팅 메시지 등록 중 오류 발생: " + e.getMessage());
            throw e;
        } finally {
            closeResources();
        }
    }
    
    // 채팅 메시지 수정
    public boolean updateChatById(ChatboardDTO chat) throws SQLException {
        String sql = "UPDATE chatboard SET chatboard_title = ?, chatboard_modify_time = NOW() " +
                     "WHERE chatboard_uid = ?";
        
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, chat.getChatboardTitle());
            pstmt.setLong(2, chat.getChatboardUid());
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                logger.info("채팅 메시지 수정 성공: ID=" + chat.getChatboardUid());
                return true;
            } else {
                logger.warning("채팅 메시지 수정 실패: ID=" + chat.getChatboardUid() + ", 영향받은 행 없음");
                return false;
            }
        } catch (SQLException e) {
            logger.severe("채팅 메시지 수정 중 오류 발생: ID=" + chat.getChatboardUid() + ", 오류=" + e.getMessage());
            throw e;
        } finally {
            closeResources();
        }
    }
    
    // 채팅 메시지 삭제 (소프트 삭제)
    public boolean deleteChatById(long chatId) throws SQLException {
        String sql = "UPDATE chatboard SET chatboard_deleted = 'deleted' WHERE chatboard_uid = ?";
        
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, chatId);
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                logger.info("채팅 메시지 삭제 성공: ID=" + chatId);
                return true;
            } else {
                logger.warning("채팅 메시지 삭제 실패: ID=" + chatId + ", 영향받은 행 없음");
                return false;
            }
        } catch (SQLException e) {
            logger.severe("채팅 메시지 삭제 중 오류 발생: ID=" + chatId + ", 오류=" + e.getMessage());
            throw e;
        } finally {
            closeResources();
        }
    }
    
    // 불량 채팅 신고
    public boolean reportChat(long chatId, long reporterId, String reason, String category) throws SQLException {
        // 채팅 작성자 ID 조회
        long targetUserId = getUserIdByChatId(chatId);
        if (targetUserId == -1) {
            logger.warning("신고 실패: 대상 채팅 없음 (ID=" + chatId + ")");
            return false; // 대상 채팅이 없음
        }
        
        // DB 스키마에 맞게 수정된 쿼리
        String sql = "INSERT INTO report (report_target_type, report_reason, " +
                    "report_status, report_createtime, report_user_uid, target_user_uid) " +
                    "VALUES (?, ?, 'active', NOW(), ?, ?)";
        
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, category);  // 'spam_ad', 'profanity_hate_speech' 등의 유형
            pstmt.setString(2, reason);    // 신고 사유
            pstmt.setLong(3, reporterId);  // 신고자 ID
            pstmt.setLong(4, targetUserId); // 신고 대상 사용자 ID
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                logger.info("채팅 신고 성공: 채팅 ID=" + chatId + ", 신고 유형=" + category);
                return true;
            } else {
                logger.warning("채팅 신고 실패: 채팅 ID=" + chatId);
                return false;
            }
        } catch (SQLException e) {
            logger.severe("채팅 신고 중 오류 발생: 채팅 ID=" + chatId + ", 오류=" + e.getMessage());
            throw e;
        } finally {
            closeResources();
        }
    }
    
    // 사용자 제재 상태 업데이트
    public boolean updateUserPenaltyStatus(long userId, String penaltyType, int duration, String reason, long adminId) throws SQLException {
        String insertPenaltySql = "INSERT INTO user_penalty (user_uid, penalty_reason, penalty_start_date, " +
                                 "penalty_end_date, penalty_status, penalty_duration) " +
                                 "VALUES (?, ?, NOW(), DATE_ADD(NOW(), INTERVAL ? DAY), 'active', ?)";
        
        try {
            conn = DBConnectionUtil.getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작
            
            pstmt = conn.prepareStatement(insertPenaltySql);
            pstmt.setLong(1, userId);
            pstmt.setString(2, reason);
            pstmt.setInt(3, duration);
            pstmt.setString(4, penaltyType); // penaltyType 매개변수 사용
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                conn.commit();
                logger.info("사용자 제재 성공: 사용자 ID=" + userId + ", 제재 유형=" + penaltyType + ", 기간=" + duration + "일");
                return true;
            } else {
                conn.rollback();
                logger.warning("사용자 제재 실패: 사용자 ID=" + userId);
                return false;
            }
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            logger.severe("사용자 제재 중 오류 발생: 사용자 ID=" + userId + ", 오류=" + e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
            }
            closeResources();
        }
    }
    
    // 자원 해제
    private void closeResources() {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            logger.severe("자원 해제 중 오류 발생: " + e.getMessage());
        }
    }
}