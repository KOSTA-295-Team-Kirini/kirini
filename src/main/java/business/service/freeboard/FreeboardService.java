package business.service.freeboard;

import java.sql.SQLException;
import java.util.List;

import dto.board.AttachmentDTO;
import dto.board.FreeboardCommentDTO;
import dto.board.FreeboardDTO;
import repository.dao.board.FreeboardDAO;

public class FreeboardService {
    private final FreeboardDAO freeboardDAO;
    
    public FreeboardService() {
        this.freeboardDAO = new FreeboardDAO();
    }
    
    /**
     * 게시글 목록 조회 (페이징 처리)
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
    public boolean createFreeboard(FreeboardDTO post) {
        try {
            // 제목, 내용 빈 값 체크
            if (post.getFreeboardTitle() == null || post.getFreeboardTitle().trim().isEmpty()) {
                return false;
            }
            
            if (post.getFreeboardContents() == null || post.getFreeboardContents().trim().isEmpty()) {
                return false;
            }
            
            // 기본값 설정
            if (post.getFreeboardNotify() == null) {
                post.setFreeboardNotify("common");
            }
            
            if (post.getFreeboardDeleted() == null) {
                post.setFreeboardDeleted("maintained");
            }
            
            return freeboardDAO.postFreeboard(post);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 게시글 수정
     */
    public boolean updateFreeboard(FreeboardDTO post, long userId, String userAuthority) {
        try {
            // 수정 권한 확인
            FreeboardDTO existingPost = freeboardDAO.getFreeboardById(post.getFreeboardUid());
            if (existingPost == null) {
                return false;
            }
            
            // 자신의 글이거나 관리자 권한인 경우만 수정 가능
            if (existingPost.getUserUid() == userId || "admin".equals(userAuthority) || "armband".equals(userAuthority)) {
                return freeboardDAO.updateFreeboardById(post);
            }
            
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 게시글 삭제
     */
    public boolean deleteFreeboard(long postId, long userId, String userAuthority) {
        try {
            // 삭제 권한 확인
            FreeboardDTO existingPost = freeboardDAO.getFreeboardById(postId);
            if (existingPost == null) {
                return false;
            }
            
            // 자신의 글이거나 관리자 권한인 경우만 삭제 가능
            if (existingPost.getUserUid() == userId || "admin".equals(userAuthority) || "armband".equals(userAuthority)) {
                return freeboardDAO.deleteFreeboardById(postId);
            }
            
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 공지사항 설정/해제 (관리자 전용)
     */
    public boolean setNotice(long postId, boolean isNotice, String userAuthority) {
        try {
            // 관리자 권한 확인
            if ("admin".equals(userAuthority) || "armband".equals(userAuthority)) {
                return freeboardDAO.setNoticeById(postId, isNotice);
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 게시글 숨김 처리 (관리자 전용)
     */
    public boolean hideFreeboard(long postId, String hideReason, String userAuthority) {
        try {
            // 관리자 권한 확인
            if ("admin".equals(userAuthority) || "armband".equals(userAuthority)) {
                return freeboardDAO.hideFreeboardById(postId, hideReason);
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 총 게시물 수 조회
     */
    public int getTotalCount() {
        try {
            return freeboardDAO.getTotalCount();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 게시글 신고
     */
    public boolean reportFreeboard(long postId, long reporterId, String reason, String category) {
        try {
            // postId 유효성 검증
            FreeboardDTO post = freeboardDAO.getFreeboardById(postId);
            if (post == null) {
                return false;
            }
            
            // 이미 신고한 게시글인지 확인 (동일 사용자가 같은 게시글 중복 신고 방지)
            // TODO: ReportDAO에서 확인 로직 추가
            
            // 신고 정보 저장
            return freeboardDAO.reportFreeboard(postId, reporterId, reason, category);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 이용자 신고
     */
    public boolean reportUser(long targetUserId, long reporterId, String reason, String category) {
        try {
            // userUid 유효성 검증 필요 (User 테이블에 존재하는 ID인지)
            
            // 이미 신고한 이용자인지 확인 (동일 사용자가 같은 이용자 중복 신고 방지)
            // TODO: ReportDAO에서 확인 로직 추가
            
            // 신고 정보 저장
            return freeboardDAO.reportUser(targetUserId, reporterId, reason, category);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 이용자 제재 (관리자/매니저 전용)
     */
    public boolean penalizeUser(long targetUserId, long adminId, String reason, 
                                String category, String penaltyType, int duration) {
        try {
            // userUid 유효성 검증 필요 (User 테이블에 존재하는 ID인지)
            
            // 제재 정보 저장 및 제재 적용
            return freeboardDAO.penalizeUser(targetUserId, adminId, reason, category, penaltyType, duration);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 첨부파일 삭제 (관리자/매니저 전용)
     */
    public boolean deleteAttachByFilename(long postId, String filename, String reason, long adminId) {
        try {
            // postId 유효성 검증
            FreeboardDTO post = freeboardDAO.getFreeboardById(postId);
            if (post == null) {
                return false;
            }
            
            // 첨부파일 존재 여부 확인
            // TODO: 첨부파일 테이블에서 확인 로직 추가
            
            // DB에서 첨부파일 정보 삭제 및 로그 기록
            boolean dbDeleteResult = freeboardDAO.deleteAttachByFilename(postId, filename, reason, adminId);
            
            if (dbDeleteResult) {
                // 실제 파일 시스템에서 파일 삭제
                // 파일 경로 구성 (실제 경로는 프로젝트 설정에 맞게 수정 필요)
                String uploadDir = "/uploads/freeboard";
                String filePath = uploadDir + "/" + postId + "/" + filename;
                
                java.io.File file = new java.io.File(filePath);
                if (file.exists()) {
                    return file.delete();
                }
                return true; // 파일이 이미 없는 경우에도 성공으로 간주
            }
            
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 첨부파일 추가
     */
    public boolean addAttachment(long postId, String fileName, String filePath, long fileSize) {
        try {
            return freeboardDAO.addAttachment(postId, fileName, filePath, fileSize);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 첨부파일 조회
     */
    public AttachmentDTO getAttachmentById(long attachId) {
        try {
            return freeboardDAO.getAttachmentById(attachId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 게시글의 댓글 목록 조회
     */
    public List<FreeboardCommentDTO> getCommentsByPostId(long postId) {
        try {
            return freeboardDAO.getCommentsByPostId(postId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 댓글 추가
     */
    public boolean addComment(FreeboardCommentDTO comment) {
        try {
            // 내용 빈 값 체크
            if (comment.getFreeboardCommentContents() == null || comment.getFreeboardCommentContents().trim().isEmpty()) {
                return false;
            }
            
            return freeboardDAO.addComment(comment);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 댓글 수정
     */
    public boolean updateComment(FreeboardCommentDTO comment, long userId, String userAuthority) {
        try {
            // 수정 권한 확인
            FreeboardCommentDTO existingComment = freeboardDAO.getCommentById(comment.getFreeboardCommentUid());
            if (existingComment == null) {
                return false;
            }
            
            // 내용 빈 값 체크
            if (comment.getFreeboardCommentContents() == null || comment.getFreeboardCommentContents().trim().isEmpty()) {
                return false;
            }
            
            // 자신의 댓글이거나 관리자 권한인 경우만 수정 가능
            if (existingComment.getUserUid() == userId || "admin".equals(userAuthority) || "armband".equals(userAuthority)) {
                return freeboardDAO.updateComment(comment);
            }
            
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 댓글 삭제
     */
    public boolean deleteComment(long commentId, long userId, String userAuthority) {
        try {
            // 삭제 권한 확인
            FreeboardCommentDTO existingComment = freeboardDAO.getCommentById(commentId);
            if (existingComment == null) {
                return false;
            }
            
            // 자신의 댓글이거나 관리자 권한인 경우만 삭제 가능
            if (existingComment.getUserUid() == userId || "admin".equals(userAuthority) || "armband".equals(userAuthority)) {
                return freeboardDAO.deleteComment(commentId, userId);
            }
            
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 댓글 상세 조회
     */
    public FreeboardCommentDTO getCommentById(long commentId) {
        try {
            return freeboardDAO.getCommentById(commentId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}