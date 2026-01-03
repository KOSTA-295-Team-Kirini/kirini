package dto.board;

import java.time.LocalDateTime;

public class CommentDTO {
    // 기본 필드
    private long commentId;            // 댓글 고유 ID
    private long postId;               // 게시글 ID
    private long userId;               // 작성자 ID
    private String content;            // 댓글 내용
    private LocalDateTime createDate;  // 작성일시
    private LocalDateTime updateDate;  // 수정일시
    private String boardType;          // 게시판 타입 (freeboard, news 등)
    private String authorIp;           // 작성자 IP

    // 추가 필드 (조인 등을 통해 가져올 데이터)
    private String userName;           // 작성자 이름

    // 기본 생성자
    public CommentDTO() {
    }

    // 댓글 작성용 생성자
    public CommentDTO(long postId, long userId, String content, String authorIp, String boardType) {
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.authorIp = authorIp;
        this.boardType = boardType;
        this.createDate = LocalDateTime.now();
    }

    // Getter 및 Setter 메서드
    public long getCommentId() {
        return commentId;
    }

    public void setCommentId(long commentId) {
        this.commentId = commentId;
    }

    public long getPostId() {
        return postId;
    }

    public void setPostId(long postId) {
        this.postId = postId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }

    public String getBoardType() {
        return boardType;
    }

    public void setBoardType(String boardType) {
        this.boardType = boardType;
    }

    public String getAuthorIp() {
        return authorIp;
    }

    public void setAuthorIp(String authorIp) {
        this.authorIp = authorIp;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "CommentDTO [commentId=" + commentId + ", postId=" + postId +
                ", userId=" + userId + ", content=" + content +
                ", createDate=" + createDate + ", boardType=" + boardType + "]";
    }
}