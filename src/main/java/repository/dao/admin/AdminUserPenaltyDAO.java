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
            String sql = "SELECT p.penalty_uid, p.penalty_start_date, p.penalty_end_date, " +
                         "p.penalty_status, p.user_uid, u.user_email, u.user_name " +
                         "FROM user_penalty p " +
                         "JOIN user u ON p.user_uid = u.user_uid " +
                         "ORDER BY p.penalty_start_date DESC";
            
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                AdminUserPenaltyDTO penalty = new AdminUserPenaltyDTO();
                
                penalty.setPenaltyUid(rs.getLong("penalty_uid"));
                java.sql.Date startDateSql = rs.getDate("penalty_start_date");
                java.sql.Date endDateSql = rs.getDate("penalty_end_date");
                penalty.setPenaltyStartDate(startDateSql);
                penalty.setPenaltyEndDate(endDateSql);
                penalty.setPenaltyStatus(rs.getString("penalty_status"));
                penalty.setUserUid(rs.getLong("user_uid"));
                penalty.setUserEmail(rs.getString("user_email"));
                penalty.setUsername(rs.getString("user_name"));

                // penaltyDays 계산
                if (startDateSql != null && endDateSql != null) {
                    long diffInMillis = endDateSql.getTime() - startDateSql.getTime();
                    int days = (int) (diffInMillis / (1000 * 60 * 60 * 24));
                    penalty.setPenaltyDays(days);
                } else {
                    penalty.setPenaltyDays(0);
                }
                
                penaltyList.add(penalty);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("패널티 정보 조회 중 오류 발생", e);
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
            String sql = "SELECT p.penalty_uid, p.penalty_start_date, p.penalty_end_date, " +
                         "p.penalty_status, p.user_uid, u.user_email, u.user_name " +
                         "FROM user_penalty p " +
                         "JOIN user u ON p.user_uid = u.user_uid " +
                         "WHERE p.user_uid = ? " +
                         "ORDER BY p.penalty_start_date DESC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, userUid);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                AdminUserPenaltyDTO penalty = new AdminUserPenaltyDTO();
                penalty.setPenaltyUid(rs.getLong("penalty_uid"));
                java.sql.Date startDateSql = rs.getDate("penalty_start_date");
                java.sql.Date endDateSql = rs.getDate("penalty_end_date");
                penalty.setPenaltyStartDate(startDateSql);
                penalty.setPenaltyEndDate(endDateSql);
                penalty.setPenaltyStatus(rs.getString("penalty_status"));
                penalty.setUserUid(rs.getLong("user_uid"));
                penalty.setUserEmail(rs.getString("user_email"));
                penalty.setUsername(rs.getString("user_name"));

                if (startDateSql != null && endDateSql != null) {
                    long diffInMillis = endDateSql.getTime() - startDateSql.getTime();
                    int days = (int) (diffInMillis / (1000 * 60 * 60 * 24));
                    penalty.setPenaltyDays(days);
                } else {
                    penalty.setPenaltyDays(0);
                }
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
            String sql = "INSERT INTO user_penalty (penalty_start_date, penalty_end_date, " +
                         "penalty_status, user_uid) " +
                         "VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setDate(1, penalty.getPenaltyStartDate());
            pstmt.setDate(2, penalty.getPenaltyEndDate());
            pstmt.setString(3, penalty.getPenaltyStatus());
            pstmt.setLong(4, penalty.getUserUid());
            
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
            String sql = "UPDATE user_penalty SET penalty_status = ? WHERE penalty_uid = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newStatus);
            pstmt.setLong(2, penaltyUid);
            
            return pstmt.executeUpdate() > 0;
        } finally {
            DBConnectionUtil.close(null, pstmt, conn);
        }
    }
    /**
     * 사용자 검색 (닉네임으로 검색)
     */    
    public List<AdminUserPenaltyDTO> searchUserPenalty(String keyword) throws SQLException {
        List<AdminUserPenaltyDTO> penaltyList = new ArrayList<>();
        
        System.out.println("[AdminUserPenaltyDAO] 검색 시작: 키워드 = " + keyword);
        try {
            conn = DBConnectionUtil.getConnection();
            
            // 대소문자를 구분하지 않는 정확한 검색을 위해 LOWER 함수 사용
            String sql = "SELECT p.penalty_uid, p.penalty_start_date, p.penalty_end_date, " +
                        "p.penalty_status, p.user_uid, u.user_email, u.user_name " +
                        "FROM user_penalty p " +
                        "JOIN user u ON p.user_uid = u.user_uid " +
                        "WHERE LOWER(u.user_name) LIKE LOWER(?) " +
                        "ORDER BY p.penalty_start_date DESC";
            
            pstmt = conn.prepareStatement(sql);
            // 정확한 검색을 위해 정확히 검색어로 시작하는 패턴 사용
            pstmt.setString(1, keyword + "%");
            System.out.println("[AdminUserPenaltyDAO] 실제 검색 패턴: '" + keyword + "%', SQL: " + sql);
            
            rs = pstmt.executeQuery();
            
            System.out.println("[AdminUserPenaltyDAO] SQL 실행 완료, 결과 처리 시작");
            
            while (rs.next()) {
                String userName = rs.getString("user_name");
                // 추가 필터링: 대소문자 무시하고 정확히 키워드로 시작하는지 확인
                if (userName.toLowerCase().startsWith(keyword.toLowerCase())) {
                    AdminUserPenaltyDTO penalty = new AdminUserPenaltyDTO();
                    
                    penalty.setPenaltyUid(rs.getLong("penalty_uid"));
                    java.sql.Date startDateSql = rs.getDate("penalty_start_date");
                    java.sql.Date endDateSql = rs.getDate("penalty_end_date");
                    penalty.setPenaltyStartDate(startDateSql);
                    penalty.setPenaltyEndDate(endDateSql);
                    penalty.setPenaltyStatus(rs.getString("penalty_status"));
                    penalty.setUserUid(rs.getLong("user_uid"));
                    penalty.setUserEmail(rs.getString("user_email"));
                    penalty.setUsername(userName);
                    
                    // penaltyDays 계산
                    if (startDateSql != null && endDateSql != null) {
                        long diffInMillis = endDateSql.getTime() - startDateSql.getTime();
                        int days = (int) (diffInMillis / (1000 * 60 * 60 * 24));
                        penalty.setPenaltyDays(days);
                    } else {
                        penalty.setPenaltyDays(0);
                    }
                    
                    penaltyList.add(penalty);
                }
            }
            
            System.out.println("[AdminUserPenaltyDAO] 검색 완료, 총 " + penaltyList.size() + "개의 결과를 찾음");
            for (int i = 0; i < penaltyList.size(); i++) {
                AdminUserPenaltyDTO p = penaltyList.get(i);
                System.out.println("[AdminUserPenaltyDAO] 결과 #" + (i+1) + ": 사용자 ID=" + p.getUserUid() + 
                                ", 이름=" + p.getUsername() + ", 이메일=" + p.getUserEmail());
            }
        } catch (SQLException e) {
            System.err.println("[AdminUserPenaltyDAO] SQL 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            System.err.println("[AdminUserPenaltyDAO] 일반 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("사용자 검색 중 오류 발생", e);
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
        
        return penaltyList;
    }
}