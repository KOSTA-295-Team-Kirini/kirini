package repository.dao.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import util.db.DBConnectionUtil;

/**
 * 삭제된 게시물/댓글/첨부파일을 복원하는 DAO 클래스
 */
public class AdminContentRecoveryDAO {
    private DBConnectionUtil dbUtil;
    
    public AdminContentRecoveryDAO() {
        dbUtil = new DBConnectionUtil();
    }
    
    /**
     * 삭제된 게시글을 복원합니다.
     * 
     * @param boardType 게시판 유형 (freeboard, news, notice, inquiry, chatboard)
     * @param postId 게시글 ID
     * @return 복원 성공 여부
     * @throws SQLException SQL 예외 발생 시
     */
    public boolean recoverDeletedPost(String boardType, long postId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = dbUtil.getConnection();
            
            // 트랜잭션 시작
            conn.setAutoCommit(false);
            
            // 게시판 종류에 따라 적절한 테이블 업데이트
            String tableName = getTableNameByBoardType(boardType);
            String sql = "UPDATE " + tableName + " SET " + tableName + "_deleted = 'maintained' " +
                         "WHERE " + tableName + "_uid = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, postId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            // 로그 테이블에서 삭제 기록 업데이트 또는 삭제
            if (rowsAffected > 0) {
                dbUtil.close(null, pstmt, null);
                
                sql = "DELETE FROM log_delete_post WHERE log_delete_boardtype = ? AND log_deleted_post_uid = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, boardType);
                pstmt.setLong(2, postId);
                pstmt.executeUpdate();
                
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    // 롤백 실패는 로깅만 하고 넘어감
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            dbUtil.close(null, pstmt, conn);
        }
    }
    
    /**
     * 삭제된 댓글을 복원합니다.
     * 
     * @param boardType 게시판 유형 (freeboard, news)
     * @param commentId 댓글 ID
     * @return 복원 성공 여부
     * @throws SQLException SQL 예외 발생 시
     */
    public boolean recoverDeletedComment(String boardType, long commentId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = dbUtil.getConnection();
            
            // 트랜잭션 시작
            conn.setAutoCommit(false);
            
            // 게시판 종류에 따라 적절한 댓글 테이블 업데이트
            String commentTableName = getCommentTableNameByBoardType(boardType);
            String sql = "UPDATE " + commentTableName + " SET " + commentTableName + "_deleted = 'maintained' " +
                         "WHERE " + commentTableName + "_uid = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, commentId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            // 로그 테이블에서 삭제 기록 업데이트 또는 삭제
            if (rowsAffected > 0) {
                dbUtil.close(null, pstmt, null);
                
                sql = "DELETE FROM log_delete_comment WHERE log_delete_boardtype = ? AND log_deleted_comment_uid = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, boardType);
                pstmt.setLong(2, commentId);
                pstmt.executeUpdate();
                
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    // 롤백 실패는 로깅만 하고 넘어감
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            dbUtil.close(null, pstmt, conn);
        }
    }
    
    /**
     * 삭제된 첨부파일을 복원합니다.
     * 
     * @param boardType 게시판 유형
     * @param attachId 첨부파일 ID
     * @return 복원 성공 여부
     * @throws SQLException SQL 예외 발생 시
     */
    public boolean recoverDeletedAttach(String boardType, long attachId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = dbUtil.getConnection();
            
            // 게시판 종류에 따라 적절한 첨부파일 테이블
            String attachTableName = getAttachmentTableByBoardType(boardType);
            String sql = "UPDATE " + attachTableName + " SET is_deleted = 0 WHERE attach_uid = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, attachId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } finally {
            dbUtil.close(null, pstmt, conn);
        }
    }
    
    /**
     * 게시판 유형에 따른 테이블 이름을 반환합니다.
     */
    private String getTableNameByBoardType(String boardType) {
        switch (boardType.toLowerCase()) {
            case "freeboard":
                return "freeboard";
            case "news":
                return "news";
            case "notice":
                return "notice";
            case "inquiry":
                return "inquiry";
            case "chatboard":
                return "chatboard";
            default:
                return "freeboard";
        }
    }
    
    /**
     * 게시판 유형에 따른 댓글 테이블 이름을 반환합니다.
     */
    private String getCommentTableNameByBoardType(String boardType) {
        switch (boardType.toLowerCase()) {
            case "freeboard":
                return "freeboard_comment";
            case "news":
                return "news_comment";
            default:
                return "freeboard_comment";
        }
    }
    
    /**
     * 게시판 유형에 따른 첨부파일 테이블 이름을 반환합니다.
     */
    private String getAttachmentTableByBoardType(String boardType) {
        switch (boardType.toLowerCase()) {
            case "freeboard":
                return "freeboard_attach";
            case "news":
                return "news_attach";
            case "notice":
                return "notice_attach";
            default:
                return "freeboard_attach";
        }
    }
}
