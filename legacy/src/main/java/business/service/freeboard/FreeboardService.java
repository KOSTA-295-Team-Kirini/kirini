package business.service.freeboard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import dto.board.AttachmentDTO;
import dto.board.FreeboardCommentDTO;
import dto.board.FreeboardDTO;
import repository.dao.board.FreeboardDAO;
import util.db.DBConnectionUtil;

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
     * 첨부파일 객체 추가
     */
    public boolean addAttachment(AttachmentDTO attachment) {
        try {
            return addAttachment(
                    attachment.getPostId(),
                    attachment.getFileName(),
                    attachment.getFilePath(),
                    attachment.getFileSize()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 파일명으로 첨부파일 조회
     */
    public AttachmentDTO getAttachmentByFilename(String filename) {
        try {
            return freeboardDAO.getAttachmentByFilename(filename);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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

    /**
     * 첨부파일 다운로드 수 증가
     */
    public boolean increaseDownloadCount(long attachId) {
        try {
            return freeboardDAO.increaseDownloadCount(attachId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 게시글 추천 처리 (좋아요/좋아요 취소 토글)
     *
     * @param postId 게시글 ID
     * @param userId 사용자 ID
     * @param type   추천 유형 (현재는 "like"만 사용됨)
     * @return 처리 성공 여부
     */
    public boolean handleRecommendation(long postId, long userId, String type) {
        try {
            if (!"like".equalsIgnoreCase(type)) {
                // 현재는 "like" 타입만 지원, 필요시 다른 타입 처리 로직 추가
                return false;
            }

            // 기존 freeboard 테이블의 freeboard_recommend 컬럼에 직접 +1/-1 하는 방식으로 구현
            // 사용자별 추천 상태는 log_recommend 테이블을 통해 관리

            // 이미 추천했는지 확인
            boolean hasRecommended = false;
            try {
                // log_recommend 테이블에서 조회
                String sql = "SELECT COUNT(*) FROM log_recommend " +
                        "WHERE log_recommend_boardtype = 'freeboard' " +
                        "AND log_recommend_post_id = ? AND user_uid = ?";

                Connection conn = DBConnectionUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setLong(1, postId);
                pstmt.setLong(2, userId);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    hasRecommended = rs.getInt(1) > 0;
                }

                rs.close();
                pstmt.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }

            if (hasRecommended) {
                // 이미 추천한 상태면 추천 취소
                try {
                    Connection conn = DBConnectionUtil.getConnection();
                    conn.setAutoCommit(false);

                    // 1. 추천 로그 삭제
                    String deleteSql = "DELETE FROM log_recommend " +
                            "WHERE log_recommend_boardtype = 'freeboard' " +
                            "AND log_recommend_post_id = ? AND user_uid = ?";

                    PreparedStatement pstmt = conn.prepareStatement(deleteSql);
                    pstmt.setLong(1, postId);
                    pstmt.setLong(2, userId);
                    int deleteResult = pstmt.executeUpdate();

                    // 2. freeboard 테이블의 추천 수 감소
                    String updateSql = "UPDATE freeboard SET freeboard_recommend = " +
                            "GREATEST(0, freeboard_recommend - 1) " +
                            "WHERE freeboard_uid = ?";

                    pstmt = conn.prepareStatement(updateSql);
                    pstmt.setLong(1, postId);
                    int updateResult = pstmt.executeUpdate();

                    if (deleteResult > 0 && updateResult > 0) {
                        conn.commit();
                        pstmt.close();
                        conn.close();
                        return true;
                    } else {
                        conn.rollback();
                        pstmt.close();
                        conn.close();
                        return false;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                // 아직 추천하지 않은 상태면 추천 추가
                try {
                    Connection conn = DBConnectionUtil.getConnection();
                    conn.setAutoCommit(false);

                    // 1. 추천 로그 추가
                    String insertSql = "INSERT INTO log_recommend " +
                            "(log_recommend_boardtype, log_recommend_post_id, log_recommend_date, user_uid) " +
                            "VALUES ('freeboard', ?, NOW(), ?)";

                    PreparedStatement pstmt = conn.prepareStatement(insertSql);
                    pstmt.setLong(1, postId);
                    pstmt.setLong(2, userId);
                    int insertResult = pstmt.executeUpdate();

                    // 2. freeboard 테이블의 추천 수 증가
                    String updateSql = "UPDATE freeboard SET freeboard_recommend = freeboard_recommend + 1 " +
                            "WHERE freeboard_uid = ?";

                    pstmt = conn.prepareStatement(updateSql);
                    pstmt.setLong(1, postId);
                    int updateResult = pstmt.executeUpdate();

                    if (insertResult > 0 && updateResult > 0) {
                        conn.commit();
                        pstmt.close();
                        conn.close();
                        return true;
                    } else {
                        conn.rollback();
                        pstmt.close();
                        conn.close();
                        return false;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 게시글의 추천 수 조회
     *
     * @param postId 게시글 ID
     * @return 추천 수
     */
    public int getPostLikeCount(long postId) {
        try {
            // freeboard 테이블에서 직접 추천 수를 가져옴
            String sql = "SELECT freeboard_recommend FROM freeboard WHERE freeboard_uid = ?";

            Connection conn = DBConnectionUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, postId);
            ResultSet rs = pstmt.executeQuery();

            int likeCount = 0;
            if (rs.next()) {
                likeCount = rs.getInt("freeboard_recommend");
            }

            rs.close();
            pstmt.close();
            conn.close();

            return likeCount;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}