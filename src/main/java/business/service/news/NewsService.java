package business.service.news;

import java.sql.SQLException;
import java.util.List;

import dto.board.NewsDTO;
import dto.board.NewsCommentDTO;
import repository.dao.board.NewsDAO;

/**
 * 키보드 소식 게시판 서비스 클래스
 */
public class NewsService {
    private final NewsDAO newsDAO;
    
    public NewsService() {
        this.newsDAO = new NewsDAO();
    }
    
    /**
     * 소식 게시글 등록 (관리자만 가능)
     */
    public boolean postNews(NewsDTO news, String userAuthority) {
        try {
            // 관리자 권한 확인
            if (!"admin".equals(userAuthority)) {
                return false;
            }
            
            // 제목, 내용 빈 값 체크
            if (news.getNewsTitle() == null || news.getNewsTitle().trim().isEmpty()) {
                return false;
            }
            
            if (news.getNewsContents() == null || news.getNewsContents().trim().isEmpty()) {
                return false;
            }
            
            // 기본값 설정
            if (news.getNewsNotify() == null) {
                news.setNewsNotify("common");
            }
            
            return newsDAO.postNews(news);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 소식 목록 조회
     */
    public List<NewsDTO> getAllNews(int page, int pageSize) {
        try {
            return newsDAO.getAllNews(page, pageSize);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 소식 상세 조회
     */
    public NewsDTO getNewsById(long newsId) {
        try {
            return newsDAO.getNewsById(newsId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 소식 검색
     */
    public List<NewsDTO> searchNewsBy(String keyword, String searchType, int page, int pageSize) {
        try {
            return newsDAO.searchNewsBy(keyword, searchType, page, pageSize);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 소식 게시글 수정 (관리자만 가능)
     */
    public boolean updateNewsById(NewsDTO news, String userAuthority) {
        try {
            // 관리자 권한 확인
            if (!"admin".equals(userAuthority)) {
                return false;
            }
            
            // 제목, 내용 빈 값 체크
            if (news.getNewsTitle() == null || news.getNewsTitle().trim().isEmpty()) {
                return false;
            }
            
            if (news.getNewsContents() == null || news.getNewsContents().trim().isEmpty()) {
                return false;
            }
            
            return newsDAO.updateNewsById(news);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 소식 게시글 삭제 (관리자만 가능)
     */
    public boolean deleteNewsById(long newsId, long userId, String userAuthority) {
        try {
            // 관리자 권한 확인
            if (!"admin".equals(userAuthority)) {
                return false;
            }
            
            return newsDAO.deleteNewsById(newsId, userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 공지사항으로 지정/해제 (관리자만 가능)
     */
    public boolean setNoticeById(long newsId, boolean isNotice, String userAuthority) {
        try {
            // 관리자 권한 확인
            if (!"admin".equals(userAuthority)) {
                return false;
            }
            
            return newsDAO.setNoticeById(newsId, isNotice);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 소식 게시글 추천
     */
    public boolean recommendNewsById(long newsId, long userId) {
        try {
            return newsDAO.recommendNewsById(newsId, userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 소식 게시글 추천 취소
     */
    public boolean recommendNewsCancelById(long newsId, long userId) {
        try {
            return newsDAO.recommendNewsCancelById(newsId, userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 총 게시물 수 조회 (페이징용)
     */
    public int getTotalNewsCount() {
        try {
            return newsDAO.getTotalCount();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 조회수 증가
     */
    public boolean increaseViewCount(long newsId) {
        try {
            return newsDAO.increaseViewCount(newsId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 사용자가 이미 추천했는지 확인
     */
    public boolean hasUserRecommended(long newsId, long userId) {
        try {
            return newsDAO.hasUserRecommended(newsId, userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 게시글의 추천 수 조회
     */
    public int getRecommendCount(long newsId) {
        try {
            return newsDAO.getRecommendCount(newsId);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    // ------------------ 댓글 관련 메서드 ------------------
    
    /**
     * 댓글 목록 조회
     */
    public List<NewsCommentDTO> getNewsComments(long newsId) {
        try {
            return newsDAO.getCommentsByNewsId(newsId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 댓글 추가
     */
    public boolean addNewsComment(NewsCommentDTO comment) {
        try {
            // 내용 빈 값 체크
            if (comment.getNewsCommentContents() == null || comment.getNewsCommentContents().trim().isEmpty()) {
                return false;
            }
            
            return newsDAO.addNewsComment(comment);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 댓글 수정 (본인 글)
     */
    public boolean updateNewsCommentById(NewsCommentDTO comment, long userId) {
        try {
            // 내용 빈 값 체크
            if (comment.getNewsCommentContents() == null || comment.getNewsCommentContents().trim().isEmpty()) {
                return false;
            }
            
            // 기존 댓글 가져오기
            NewsCommentDTO existingComment = newsDAO.getCommentById(comment.getNewsCommentId());
            if (existingComment == null) {
                return false;
            }
            
            // 본인 글이 맞는지 확인
            if (existingComment.getUserId() != userId) {
                return false;
            }
            
            // 수정 내용 설정
            comment.setUserId(userId);
            
            return newsDAO.updateNewsCommentById(comment, false);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 댓글 수정 (관리자 권한으로 다른 사람의 글 수정)
     */
    public boolean updateNewsCommentById(NewsCommentDTO comment, String userAuthority) {
        try {
            // 관리자 권한 확인
            if (!"admin".equals(userAuthority) && !"armband".equals(userAuthority)) {
                return false;
            }
            
            // 내용 빈 값 체크
            if (comment.getNewsCommentContents() == null || comment.getNewsCommentContents().trim().isEmpty()) {
                return false;
            }
            
            return newsDAO.updateNewsCommentById(comment, true);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 댓글 삭제 (본인 글)
     */
    public boolean deleteNewsCommentById(long commentId, long userId) {
        try {
            // 기존 댓글 가져오기
            NewsCommentDTO existingComment = newsDAO.getCommentById(commentId);
            if (existingComment == null) {
                return false;
            }
            
            // 본인 글이 맞는지 확인
            if (existingComment.getUserId() != userId) {
                return false;
            }
            
            return newsDAO.deleteNewsCommentById(commentId, userId, false);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 댓글 삭제 (관리자 권한으로 다른 사람의 글 삭제)
     */
    public boolean deleteNewsCommentById(long commentId, long userId, String userAuthority) {
        try {
            // 관리자 권한 확인
            if (!"admin".equals(userAuthority) && !"armband".equals(userAuthority)) {
                return false;
            }
            
            return newsDAO.deleteNewsCommentById(commentId, userId, true);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
