package repository.dao.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import dto.user.UserPenaltyDTO;
import util.db.DBConnectionUtil;

public class UserPenaltyDAO {
    private Connection conn = null;
    private PreparedStatement pstmt = null;
    private ResultSet rs = null;
    
    // 불량 이용자 제재 등록
    public boolean addUserPenalty(UserPenaltyDTO penalty) throws SQLException {
        // 구현...
        return false;
    }
    
    // 모든 제재 내역 조회
    public List<UserPenaltyDTO> getAllUserPenalties() throws SQLException {
        List<UserPenaltyDTO> penalties = new ArrayList<>();
        
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "SELECT * FROM user_penalty ORDER BY penalty_start_date DESC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                UserPenaltyDTO penalty = new UserPenaltyDTO();
                penalty.setPenaltyId(rs.getLong("penalty_uid"));
                penalty.setUserId(rs.getLong("user_uid"));
                penalty.setPenaltyReason(rs.getString("penalty_reason"));
                penalty.setPenaltyStartDate(rs.getTimestamp("penalty_start_date").toLocalDateTime());
                
                if (rs.getTimestamp("penalty_end_date") != null) {
                    penalty.setPenaltyEndDate(rs.getTimestamp("penalty_end_date").toLocalDateTime());
                }
                
                penalty.setActive(rs.getString("penalty_status").equals("active"));
                
                penalties.add(penalty);
            }
            
            return penalties;
        } finally {
            DBConnectionUtil.close(conn, pstmt, rs);
        }
    }
    
    // 사용자별 제재 내역 조회
    public List<UserPenaltyDTO> getUserPenaltiesByUserId(long userId) throws SQLException {
        List<UserPenaltyDTO> penalties = new ArrayList<>();
        
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "SELECT * FROM user_penalty WHERE user_uid = ? ORDER BY penalty_start_date DESC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, userId);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                UserPenaltyDTO penalty = new UserPenaltyDTO();
                penalty.setPenaltyId(rs.getLong("penalty_uid"));
                penalty.setUserId(rs.getLong("user_uid"));
                penalty.setPenaltyReason(rs.getString("penalty_reason"));
                penalty.setPenaltyStartDate(rs.getTimestamp("penalty_start_date").toLocalDateTime());
                
                if (rs.getTimestamp("penalty_end_date") != null) {
                    penalty.setPenaltyEndDate(rs.getTimestamp("penalty_end_date").toLocalDateTime());
                }
                
                penalty.setActive(rs.getString("penalty_status").equals("active"));
                
                penalties.add(penalty);
            }
            
            return penalties;
        } finally {
            DBConnectionUtil.close(conn, pstmt, rs);
        }
    }
    
    // 제재 상태 변경
    public boolean updateUserPenaltyStatus(long penaltyId, String status) throws SQLException {
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "UPDATE user_penalty SET penalty_status = ? WHERE penalty_uid = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setLong(2, penaltyId);
            
            return pstmt.executeUpdate() > 0;
        } finally {
            DBConnectionUtil.close(conn, pstmt, rs);
        }
    }
}