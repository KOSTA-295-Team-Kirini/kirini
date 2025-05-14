package business.service.board;

import java.sql.SQLException;
import java.util.List;
import java.util.Date;

import dto.board.FreeboardDTO;
import repository.dao.board.FreeboardDAO;

public class FreeboardService {
    private FreeboardDAO freeboardDAO;
    
    public FreeboardService() {
        freeboardDAO = new FreeboardDAO();
    }
    
    /**
     * 게시글 목록 조회
     */
    public List<FreeboardDTO> getAllFreeboards(int page, int pageSize) {
        try {
            return freeboardDAO.getAllFreeboards(page, pageSize);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 전체 게시글 수 조회
     */
    public int getTotalPostsCount() {
        try {
            return freeboardDAO.getTotalPostsCount();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 게시글 상세 조회
     */
    public FreeboardDTO getFreeboardById(long postId) {
        try {
            return freeboardDAO.getFreeboardById(postId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 게시글 등록
     */
    public boolean postFreeboard(FreeboardDTO post) {
        try {
            return freeboardDAO.postFreeboard(post);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 게시글 수정
     */
    public boolean updateFreeboardById(FreeboardDTO post) {
        try {
            return freeboardDAO.updateFreeboardById(post);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 게시글 삭제
     */
    public boolean deleteFreeboardById(long postId) {
        try {
            return freeboardDAO.deleteFreeboardById(postId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 게시글 숨김 처리
     */
    public boolean hideFreeboardById(long postId, String hideReason) {
        try {
            return freeboardDAO.hideFreeboardById(postId, hideReason);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 공지사항 지정/해제
     */
    public boolean setNoticeById(long postId, boolean isNotice) {
        try {
            return freeboardDAO.setNoticeById(postId, isNotice);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 조회수 증가
     */
    public boolean increaseViewCount(long postId) {
        try {
            return freeboardDAO.increaseViewCount(postId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 게시글 신고
     */
    public boolean reportFreeboardById(long postId, long targetUserId, long reporterUid, String reportReason) {
        try {
            return freeboardDAO.reportFreeboardById(postId, targetUserId, reporterUid, reportReason);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 사용자 신고
     */
    public boolean reportUserById(long targetUserId, long reporterUid, String reportReason) {
        try {
            return freeboardDAO.reportUserById(targetUserId, reporterUid, reportReason);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 첨부파일 삭제
     */
    public boolean deleteFreeboardAttachByFilename(long postId, String filename) {
        try {
            return freeboardDAO.deleteFreeboardAttachByFilename(postId, filename);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}