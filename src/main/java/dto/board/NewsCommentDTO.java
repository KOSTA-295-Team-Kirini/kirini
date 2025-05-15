package dto.board;

import java.time.LocalDateTime;

/**
 * 키보드 소식 게시판 댓글 DTO 클래스
 */
public class NewsCommentDTO {
    private long newsCommentId;
    private String newsCommentContents;
    private LocalDateTime newsCommentWritetime;
    private LocalDateTime newsCommentModifytime;
    private String newsCommentAuthorIp;
    private long newsId;
    private long userId;
    private String userName;  // JOIN 결과

    // 기본 생성자
    public NewsCommentDTO() {
    }

    // Getter와 Setter 메소드
    public long getNewsCommentId() {
        return newsCommentId;
    }

    public void setNewsCommentId(long newsCommentId) {
        this.newsCommentId = newsCommentId;
    }

    public String getNewsCommentContents() {
        return newsCommentContents;
    }

    public void setNewsCommentContents(String newsCommentContents) {
        this.newsCommentContents = newsCommentContents;
    }

    public LocalDateTime getNewsCommentWritetime() {
        return newsCommentWritetime;
    }

    public void setNewsCommentWritetime(LocalDateTime newsCommentWritetime) {
        this.newsCommentWritetime = newsCommentWritetime;
    }

    public LocalDateTime getNewsCommentModifytime() {
        return newsCommentModifytime;
    }

    public void setNewsCommentModifytime(LocalDateTime newsCommentModifytime) {
        this.newsCommentModifytime = newsCommentModifytime;
    }

    public String getNewsCommentAuthorIp() {
        return newsCommentAuthorIp;
    }

    public void setNewsCommentAuthorIp(String newsCommentAuthorIp) {
        this.newsCommentAuthorIp = newsCommentAuthorIp;
    }

    public long getNewsId() {
        return newsId;
    }

    public void setNewsId(long newsId) {
        this.newsId = newsId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "NewsCommentDTO [newsCommentId=" + newsCommentId + ", newsCommentContents=" 
                + newsCommentContents + ", newsCommentWritetime=" + newsCommentWritetime 
                + ", userId=" + userId + ", userName=" + userName + "]";
    }
}
