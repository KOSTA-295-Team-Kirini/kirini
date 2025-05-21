package repository.dao.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dto.admin.AdminUserPenaltyDTO;
import util.db.DBConnectionUtil;

/**
 * 관리자용 사용자 패널티 DAO 클래스
 */
public class AdminUserPenaltyDAO {
    private Connection conn;
    private PreparedStatement pstmt;
    private ResultSet rs;
    
    /**
     * 모든 사용자 패널티 목록 조회
     */
    public List<AdminUserPenaltyDTO> getAllUserPenalty() throws SQLException {
        List<AdminUserPenaltyDTO> penaltyList = new ArrayList<>();
        
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "SELECT p.*, u.user_name as username " +
                         "FROM penalty p " +
                         "JOIN user u ON p.user_uid = u.user_uid " +
                         "ORDER BY p.penalty_start_date DESC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                AdminUserPenaltyDTO penalty = new AdminUserPenaltyDTO();
                penalty.setPenaltyUid(rs.getLong("penalty_uid"));
                penalty.setPenaltyReason(rs.getString("penalty_reason"));
                penalty.setPenaltyStartDate(rs.getDate("penalty_start_date"));
                penalty.setPenaltyEndDate(rs.getDate("penalty_end_date"));
                penalty.setPenaltyStatus(rs.getString("penalty_status"));
                penalty.setPenaltyDuration(rs.getString("penalty_duration"));
                penalty.setUserUid(rs.getLong("user_uid"));
                penalty.setUsername(rs.getString("username"));
                penalty.setAdminUid(rs.getLong("admin_uid"));
                
                penaltyList.add(penalty);
            }
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
        
        return penaltyList;
    }
    
    /**
     * 특정 사용자의 패널티 목록 조회
     */
    public List<AdminUserPenaltyDTO> getUserPenaltyByUserId(long userUid) throws SQLException {
        List<AdminUserPenaltyDTO> penaltyList = new ArrayList<>();
        
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "SELECT p.*, u.user_name as username " +
                         "FROM penalty p " +
                         "JOIN user u ON p.user_uid = u.user_uid " +
                         "WHERE p.user_uid = ? " +
                         "ORDER BY p.penalty_start_date DESC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, userUid);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                AdminUserPenaltyDTO penalty = new AdminUserPenaltyDTO();
                penalty.setPenaltyUid(rs.getLong("penalty_uid"));
                penalty.setPenaltyReason(rs.getString("penalty_reason"));
                penalty.setPenaltyStartDate(rs.getDate("penalty_start_date"));
                penalty.setPenaltyEndDate(rs.getDate("penalty_end_date"));
                penalty.setPenaltyStatus(rs.getString("penalty_status"));
                penalty.setPenaltyDuration(rs.getString("penalty_duration"));
                penalty.setUserUid(rs.getLong("user_uid"));
                penalty.setUsername(rs.getString("username"));
                penalty.setAdminUid(rs.getLong("admin_uid"));
                
                penaltyList.add(penalty);
            }
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
        
        return penaltyList;
    }
    
    /**
     * 패널티 추가
     */
    public boolean addUserPenalty(AdminUserPenaltyDTO penalty) throws SQLException {
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "INSERT INTO penalty (penalty_reason, penalty_start_date, penalty_end_date, " +
                         "penalty_status, penalty_duration, user_uid, admin_uid) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, penalty.getPenaltyReason());
            pstmt.setDate(2, penalty.getPenaltyStartDate());
            pstmt.setDate(3, penalty.getPenaltyEndDate());
            pstmt.setString(4, penalty.getPenaltyStatus());
            pstmt.setString(5, penalty.getPenaltyDuration());
            pstmt.setLong(6, penalty.getUserUid());
            pstmt.setLong(7, penalty.getAdminUid());
            
            return pstmt.executeUpdate() > 0;
        } finally {
            DBConnectionUtil.close(null, pstmt, conn);
        }
    }
    
    /**
     * 패널티 상태 변경
     */
    public boolean updateUserPenaltyStatusByPenaltyId(long penaltyUid, String newStatus) throws SQLException {
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "UPDATE penalty SET penalty_status = ? WHERE penalty_uid = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newStatus);
            pstmt.setLong(2, penaltyUid);
            
            return pstmt.executeUpdate() > 0;
        } finally {
            DBConnectionUtil.close(null, pstmt, conn);
        }
    }
}