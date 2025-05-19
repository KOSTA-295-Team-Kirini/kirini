package repository.dao.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import dto.admin.AdminDeleteLogDTO;
import util.db.DBConnectionUtil;
import util.logging.LoggerConfig;

/**
 * 삭제된 게시물/댓글 로그를 데이터베이스에서 조회하는 DAO 클래스
 */
public class AdminDeleteLogDAO {
    private static final Logger logger = LoggerConfig.getLogger(AdminDeleteLogDAO.class);
    
    /**
     * 전체 게시글 삭제 내역을 조회합니다.
     * 
     * @return 게시글 삭제 로그 목록
     * @throws SQLException SQL 예외 발생 시
     */
    public List<AdminDeleteLogDTO> getAllDeletePostLogs() throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<AdminDeleteLogDTO> logList = new ArrayList<>();
        
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "SELECT l.*, u.user_name as deleted_by_username " +
                    "FROM log_delete_post l " +
                    "JOIN user u ON l.user_uid = u.user_uid " +
                    "ORDER BY l.log_delete_date DESC";
            
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                AdminDeleteLogDTO log = new AdminDeleteLogDTO();
                log.setLogId(rs.getLong("log_delete_uid"));
                log.setBoardType(rs.getString("log_delete_boardtype"));
                log.setDeleteDate(rs.getDate("log_delete_date"));
                log.setContentId(rs.getLong("log_deleted_post_uid"));
                log.setUserUid(rs.getLong("user_uid"));
                log.setUserName(rs.getString("deleted_by_username"));
                
                logList.add(log);
            }
            
            logger.info("게시글 삭제 로그 " + logList.size() + "건 조회됨");
            return logList;
        } catch (SQLException e) {
            logger.severe("게시글 삭제 로그 조회 중 오류 발생: " + e.getMessage());
            throw e;
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
    }
    
    /**
     * 전체 댓글 삭제 내역을 조회합니다.
     * 
     * @return 댓글 삭제 로그 목록
     * @throws SQLException SQL 예외 발생 시
     */
    public List<AdminDeleteLogDTO> getAllDeleteCommentLogs() throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<AdminDeleteLogDTO> logList = new ArrayList<>();
        
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "SELECT l.*, u.user_name as deleted_by_username " +
                    "FROM log_delete_comment l " +
                    "JOIN user u ON l.user_uid = u.user_uid " +
                    "ORDER BY l.log_delete_date DESC";
            
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                AdminDeleteLogDTO log = new AdminDeleteLogDTO();
                log.setLogId(rs.getLong("log_delete_uid"));
                log.setBoardType(rs.getString("log_delete_boardtype"));
                log.setDeleteDate(rs.getDate("log_delete_date"));
                log.setContentId(rs.getLong("log_deleted_comment_uid"));
                log.setUserUid(rs.getLong("user_uid"));
                log.setUserName(rs.getString("deleted_by_username"));
                
                logList.add(log);
            }
            
            logger.info("댓글 삭제 로그 " + logList.size() + "건 조회됨");
            return logList;
        } catch (SQLException e) {
            logger.severe("댓글 삭제 로그 조회 중 오류 발생: " + e.getMessage());
            throw e;
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
    }
    
    /**
     * 조건에 맞는 삭제 게시글 내역을 검색합니다.
     * 
     * @param boardType 게시판 유형 (freeboard, news, notice, null: 전체)
     * @param keyword 검색어 (제목, 작성자)
     * @return 검색 결과 목록
     * @throws SQLException SQL 예외 발생 시
     */
    public List<AdminDeleteLogDTO> getDeletePostLogsByCondition(String boardType, String keyword) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<AdminDeleteLogDTO> logList = new ArrayList<>();
        
        try {
            conn = DBConnectionUtil.getConnection();
            StringBuilder sql = new StringBuilder(
                    "SELECT l.*, u.user_name as deleted_by_username " +
                    "FROM log_delete_post l " +
                    "JOIN user u ON l.user_uid = u.user_uid " +
                    "WHERE 1=1 ");
            
            if (boardType != null && !boardType.trim().isEmpty()) {
                sql.append("AND l.log_delete_boardtype = ? ");
            }
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                sql.append("AND (u.user_name LIKE ? OR EXISTS (" +
                        "SELECT 1 FROM user au WHERE au.user_uid = l.user_uid AND au.user_name LIKE ?))");
            }
            
            sql.append(" ORDER BY l.log_delete_date DESC");
            
            pstmt = conn.prepareStatement(sql.toString());
            
            int paramIndex = 1;
            if (boardType != null && !boardType.trim().isEmpty()) {
                pstmt.setString(paramIndex++, boardType);
            }
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchParam = "%" + keyword.trim() + "%";
                pstmt.setString(paramIndex++, searchParam);
                pstmt.setString(paramIndex++, searchParam);
            }
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                AdminDeleteLogDTO log = new AdminDeleteLogDTO();
                log.setLogId(rs.getLong("log_delete_uid"));
                log.setBoardType(rs.getString("log_delete_boardtype"));
                log.setDeleteDate(rs.getDate("log_delete_date"));
                log.setContentId(rs.getLong("log_deleted_post_uid"));
                log.setUserUid(rs.getLong("user_uid"));
                log.setUserName(rs.getString("deleted_by_username"));
                
                logList.add(log);
            }
            
            logger.info("조건별 게시글 삭제 로그 " + logList.size() + "건 조회됨 (게시판: " + boardType + ", 키워드: " + keyword + ")");
            return logList;
        } catch (SQLException e) {
            logger.severe("조건별 게시글 삭제 로그 조회 중 오류 발생: " + e.getMessage());
            throw e;
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
    }
    
    /**
     * 조건에 맞는 삭제 댓글 내역을 검색합니다.
     * 
     * @param boardType 게시판 유형 (freeboard, news, notice, null: 전체)
     * @param keyword 검색어 (내용, 작성자)
     * @return 검색 결과 목록
     * @throws SQLException SQL 예외 발생 시
     */
    public List<AdminDeleteLogDTO> getDeleteCommentLogsByCondition(String boardType, String keyword) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<AdminDeleteLogDTO> logList = new ArrayList<>();
        
        try {
            conn = DBConnectionUtil.getConnection();
            StringBuilder sql = new StringBuilder(
                    "SELECT l.*, u.user_name as deleted_by_username " +
                    "FROM log_delete_comment l " +
                    "JOIN user u ON l.user_uid = u.user_uid " +
                    "WHERE 1=1 ");
            
            if (boardType != null && !boardType.trim().isEmpty()) {
                sql.append("AND l.log_delete_boardtype = ? ");
            }
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                sql.append("AND (u.user_name LIKE ? OR EXISTS (" +
                        "SELECT 1 FROM user au WHERE au.user_uid = l.user_uid AND au.user_name LIKE ?))");
            }
            
            sql.append(" ORDER BY l.log_delete_date DESC");
            
            pstmt = conn.prepareStatement(sql.toString());
            
            int paramIndex = 1;
            if (boardType != null && !boardType.trim().isEmpty()) {
                pstmt.setString(paramIndex++, boardType);
            }
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchParam = "%" + keyword.trim() + "%";
                pstmt.setString(paramIndex++, searchParam);
                pstmt.setString(paramIndex++, searchParam);
            }
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                AdminDeleteLogDTO log = new AdminDeleteLogDTO();
                log.setLogId(rs.getLong("log_delete_uid"));
                log.setBoardType(rs.getString("log_delete_boardtype"));
                log.setDeleteDate(rs.getDate("log_delete_date"));
                log.setContentId(rs.getLong("log_deleted_comment_uid"));
                log.setUserUid(rs.getLong("user_uid"));
                log.setUserName(rs.getString("deleted_by_username"));
                
                logList.add(log);
            }
            
            logger.info("조건별 댓글 삭제 로그 " + logList.size() + "건 조회됨 (게시판: " + boardType + ", 키워드: " + keyword + ")");
            return logList;
        } catch (SQLException e) {
            logger.severe("조건별 댓글 삭제 로그 조회 중 오류 발생: " + e.getMessage());
            throw e;
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
    }
}
