package business.service.admin;

import java.sql.SQLException;
import java.util.List;

import dto.admin.AdminDeleteLogDTO;
import repository.dao.admin.AdminContentRecoveryDAO;
import repository.dao.admin.AdminDeleteLogDAO;
import util.logging.LoggerConfig;

/**
 * 삭제된 게시물/댓글 로그 관리를 위한 서비스 클래스
 */
public class AdminLogService {
    private AdminDeleteLogDAO logDAO;
    private AdminContentRecoveryDAO recoveryDAO;

    public AdminLogService() {
        logDAO = new AdminDeleteLogDAO();
        recoveryDAO = new AdminContentRecoveryDAO();
    }

    /**
     * 시스템 에러를 콘솔에 출력합니다.
     *
     * @param errorType  에러 유형 (SQL, 시스템 등)
     * @param e          발생한 예외 객체
     * @param methodName 에러가 발생한 메소드 이름
     * @param params     메소드 호출 시 사용된 파라미터 (선택적)
     */
    private void logError(String errorType, Exception e, String methodName, String params) {
        String message = "유형: " + errorType;
        if (params != null) {
            message += ", 파라미터: " + params;
        }
        LoggerConfig.logError(AdminLogService.class, methodName, message, e);
    }

    /**
     * 전체 게시글 삭제 내역을 조회합니다.
     *
     * @return 게시글 삭제 로그 목록
     */
    public List<AdminDeleteLogDTO> getAllDeletePostLogs() {
        try {
            return logDAO.getAllDeletePostLogs();
        } catch (SQLException e) {
            logError("SQL", e, "getAllDeletePostLogs", null);
            return null;
        }
    }

    /**
     * 전체 댓글 삭제 내역을 조회합니다.
     *
     * @return 댓글 삭제 로그 목록
     */
    public List<AdminDeleteLogDTO> getAllDeleteCommentLogs() {
        try {
            return logDAO.getAllDeleteCommentLogs();
        } catch (SQLException e) {
            logError("SQL", e, "getAllDeleteCommentLogs", null);
            return null;
        }
    }

    /**
     * 조건에 맞는 삭제 게시글 내역을 검색합니다.
     *
     * @param boardType 게시판 유형 (freeboard, news, notice, null: 전체)
     * @param keyword   검색어 (제목, 작성자)
     * @return 검색 결과 목록
     */
    public List<AdminDeleteLogDTO> getDeletePostLogsByCondition(String boardType, String keyword) {
        try {
            return logDAO.getDeletePostLogsByCondition(boardType, keyword);
        } catch (SQLException e) {
            logError("SQL", e, "getDeletePostLogsByCondition", "boardType: " + boardType + ", keyword: " + keyword);
            return null;
        }
    }

    /**
     * 조건에 맞는 삭제 댓글 내역을 검색합니다.
     *
     * @param boardType 게시판 유형 (freeboard, news, notice, null: 전체)
     * @param keyword   검색어 (내용, 작성자)
     * @return 검색 결과 목록
     */
    public List<AdminDeleteLogDTO> getDeleteCommentLogsByCondition(String boardType, String keyword) {
        try {
            return logDAO.getDeleteCommentLogsByCondition(boardType, keyword);
        } catch (SQLException e) {
            logError("SQL", e, "getDeleteCommentLogsByCondition", "boardType: " + boardType + ", keyword: " + keyword);
            return null;
        }
    }

    /**
     * 삭제된 게시글을 복원합니다.
     *
     * @param boardType 게시판 유형 (freeboard, news, notice, inquiry, chatboard)
     * @param postId    게시글 ID
     * @return 복원 성공 여부
     */
    public boolean recoverDeletedPost(String boardType, long postId) {
        try {
            return recoveryDAO.recoverDeletedPost(boardType, postId);
        } catch (SQLException e) {
            logError("SQL", e, "recoverDeletedPost", "boardType: " + boardType + ", postId: " + postId);
            return false;
        }
    }

    /**
     * 삭제된 댓글을 복원합니다.
     *
     * @param boardType 게시판 유형 (freeboard, news)
     * @param commentId 댓글 ID
     * @return 복원 성공 여부
     */
    public boolean recoverDeletedComment(String boardType, long commentId) {
        try {
            return recoveryDAO.recoverDeletedComment(boardType, commentId);
        } catch (SQLException e) {
            logError("SQL", e, "recoverDeletedComment", "boardType: " + boardType + ", commentId: " + commentId);
            return false;
        }
    }

    /**
     * 삭제된 첨부파일을 복원합니다.
     *
     * @param boardType 게시판 유형
     * @param attachId  첨부파일 ID
     * @return 복원 성공 여부
     */
    public boolean recoverDeletedAttach(String boardType, long attachId) {
        try {
            return recoveryDAO.recoverDeletedAttach(boardType, attachId);
        } catch (SQLException e) {
            logError("SQL", e, "recoverDeletedAttach", "boardType: " + boardType + ", attachId: " + attachId);
            return false;
        }
    }
}