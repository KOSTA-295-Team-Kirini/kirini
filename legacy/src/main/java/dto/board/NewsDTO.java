package dto.board;

import java.time.LocalDateTime;

/**
 * 키보드 소식 게시판 DTO 클래스
 */
public class NewsDTO {
    private long newsId;
    private String newsTitle;
    private String newsContents;
    private int newsRead;
    private int newsRecommend;
    private LocalDateTime newsWritetime;
    private LocalDateTime newsModifyTime;
    private String newsAuthorIp;
    private String newsNotify;      // 'common' 또는 'notification'
    private String newsDeleted;     // 'maintained' 또는 'deleted'
    private long userId;
    private String userName;        // 작성자 이름 (JOIN 결과)
    private int commentCount;       // 댓글 수 (JOIN 결과)

    // 기본 생성자
    public NewsDTO() {
    }

    // Getter와 Setter 메소드
    public long getNewsId() {
        return newsId;
    }

    public void setNewsId(long newsId) {
        this.newsId = newsId;
    }

    public String getNewsTitle() {
        return newsTitle;
    }

    public void setNewsTitle(String newsTitle) {
        this.newsTitle = newsTitle;
    }

    public String getNewsContents() {
        return newsContents;
    }

    public void setNewsContents(String newsContents) {
        this.newsContents = newsContents;
    }

    public int getNewsRead() {
        return newsRead;
    }

    public void setNewsRead(int newsRead) {
        this.newsRead = newsRead;
    }

    public int getNewsRecommend() {
        return newsRecommend;
    }

    public void setNewsRecommend(int newsRecommend) {
        this.newsRecommend = newsRecommend;
    }

    public LocalDateTime getNewsWritetime() {
        return newsWritetime;
    }

    public void setNewsWritetime(LocalDateTime newsWritetime) {
        this.newsWritetime = newsWritetime;
    }

    public LocalDateTime getNewsModifyTime() {
        return newsModifyTime;
    }

    public void setNewsModifyTime(LocalDateTime newsModifyTime) {
        this.newsModifyTime = newsModifyTime;
    }

    public String getNewsAuthorIp() {
        return newsAuthorIp;
    }

    public void setNewsAuthorIp(String newsAuthorIp) {
        this.newsAuthorIp = newsAuthorIp;
    }

    public String getNewsNotify() {
        return newsNotify;
    }

    public void setNewsNotify(String newsNotify) {
        this.newsNotify = newsNotify;
    }

    public String getNewsDeleted() {
        return newsDeleted;
    }

    public void setNewsDeleted(String newsDeleted) {
        this.newsDeleted = newsDeleted;
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

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    @Override
    public String toString() {
        return "NewsDTO [newsId=" + newsId + ", newsTitle=" + newsTitle + ", newsRead=" + newsRead
                + ", newsRecommend=" + newsRecommend + ", newsWritetime=" + newsWritetime
                + ", userId=" + userId + ", userName=" + userName + "]";
    }
}
