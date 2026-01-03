package dto.board;

import java.time.LocalDateTime;

public class FreeboardDTO {
    private long freeboardUid;            // PK
    private String freeboardTitle;        // 제목
    private String freeboardContents;     // 내용
    private int freeboardRead;            // 조회수
    private int freeboardRecommend;       // 추천수
    private LocalDateTime freeboardWritetime;  // 작성일시
    private LocalDateTime freeboardModifyTime; // 수정일시
    private String freeboardAuthorIp;     // 작성자 IP
    private String freeboardNotify;       // 공지 여부 (common/notification)
    private String freeboardDeleted;      // 삭제 여부 (maintained/deleted)
    private long userUid;                 // 작성자 ID (FK)

    // 추가 필드 (DTO에서만 사용) - 작성자 이름, 댓글 수 등 추가 가능
    private String userName;              // 작성자 이름
    private int commentCount;             // 댓글 수

    // 기본 생성자
    public FreeboardDTO() {
    }

    // 게시글 작성용 생성자
    public FreeboardDTO(String freeboardTitle, String freeboardContents, String freeboardAuthorIp, long userUid) {
        this.freeboardTitle = freeboardTitle;
        this.freeboardContents = freeboardContents;
        this.freeboardAuthorIp = freeboardAuthorIp;
        this.userUid = userUid;
        this.freeboardNotify = "common";
        this.freeboardDeleted = "maintained";
    }

    // Getter 및 Setter 메서드
    public long getFreeboardUid() {
        return freeboardUid;
    }

    public void setFreeboardUid(long freeboardUid) {
        this.freeboardUid = freeboardUid;
    }

    public String getFreeboardTitle() {
        return freeboardTitle;
    }

    public void setFreeboardTitle(String freeboardTitle) {
        this.freeboardTitle = freeboardTitle;
    }

    public String getFreeboardContents() {
        return freeboardContents;
    }

    public void setFreeboardContents(String freeboardContents) {
        this.freeboardContents = freeboardContents;
    }

    public int getFreeboardRead() {
        return freeboardRead;
    }

    public void setFreeboardRead(int freeboardRead) {
        this.freeboardRead = freeboardRead;
    }

    public int getFreeboardRecommend() {
        return freeboardRecommend;
    }

    public void setFreeboardRecommend(int freeboardRecommend) {
        this.freeboardRecommend = freeboardRecommend;
    }

    public LocalDateTime getFreeboardWritetime() {
        return freeboardWritetime;
    }

    public void setFreeboardWritetime(LocalDateTime freeboardWritetime) {
        this.freeboardWritetime = freeboardWritetime;
    }

    public LocalDateTime getFreeboardModifyTime() {
        return freeboardModifyTime;
    }

    public void setFreeboardModifyTime(LocalDateTime freeboardModifyTime) {
        this.freeboardModifyTime = freeboardModifyTime;
    }

    public String getFreeboardAuthorIp() {
        return freeboardAuthorIp;
    }

    public void setFreeboardAuthorIp(String freeboardAuthorIp) {
        this.freeboardAuthorIp = freeboardAuthorIp;
    }

    public String getFreeboardNotify() {
        return freeboardNotify;
    }

    public void setFreeboardNotify(String freeboardNotify) {
        this.freeboardNotify = freeboardNotify;
    }

    public String getFreeboardDeleted() {
        return freeboardDeleted;
    }

    public void setFreeboardDeleted(String freeboardDeleted) {
        this.freeboardDeleted = freeboardDeleted;
    }

    public long getUserUid() {
        return userUid;
    }

    public void setUserUid(long userUid) {
        this.userUid = userUid;
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
        return "FreeboardDTO [freeboardUid=" + freeboardUid + ", freeboardTitle=" + freeboardTitle + ", userUid="
                + userUid + ", freeboardWritetime=" + freeboardWritetime + "]";
    }
}