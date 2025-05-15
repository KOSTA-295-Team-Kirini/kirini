package dto.board;

import java.time.LocalDateTime;

/**
 * 자유게시판 댓글 DTO
 * 데이터베이스의 freeboard_comment 테이블과 매핑
 */
public class FreeboardCommentDTO {
    // 기본 필드
    private long freeboardCommentUid;           // 댓글 고유 ID (PK)
    private String freeboardCommentContents;    // 댓글 내용
    private LocalDateTime freeboardCommentWritetime; // 작성일시
    private LocalDateTime freeboardCommentModifytime; // 수정일시
    private String freeboardCommentAuthorIp;    // 작성자 IP
    private long freeboardUid;                  // 게시글 ID (FK)
    private long userUid;                       // 작성자 ID (FK)
    
    // 추가 필드 (조인 등으로 가져올 데이터)
    private String userName;                    // 작성자 이름
    
    // 기본 생성자
    public FreeboardCommentDTO() {}
    
    // 댓글 작성용 생성자
    public FreeboardCommentDTO(long freeboardUid, long userUid, String contents, String authorIp) {
        this.freeboardUid = freeboardUid;
        this.userUid = userUid;
        this.freeboardCommentContents = contents;
        this.freeboardCommentAuthorIp = authorIp;
        this.freeboardCommentWritetime = LocalDateTime.now();
    }
    
    // Getter 및 Setter 메서드
    public long getFreeboardCommentUid() {
        return freeboardCommentUid;
    }

    public void setFreeboardCommentUid(long freeboardCommentUid) {
        this.freeboardCommentUid = freeboardCommentUid;
    }

    public String getFreeboardCommentContents() {
        return freeboardCommentContents;
    }

    public void setFreeboardCommentContents(String freeboardCommentContents) {
        this.freeboardCommentContents = freeboardCommentContents;
    }

    public LocalDateTime getFreeboardCommentWritetime() {
        return freeboardCommentWritetime;
    }

    public void setFreeboardCommentWritetime(LocalDateTime freeboardCommentWritetime) {
        this.freeboardCommentWritetime = freeboardCommentWritetime;
    }

    public LocalDateTime getFreeboardCommentModifytime() {
        return freeboardCommentModifytime;
    }

    public void setFreeboardCommentModifytime(LocalDateTime freeboardCommentModifytime) {
        this.freeboardCommentModifytime = freeboardCommentModifytime;
    }

    public String getFreeboardCommentAuthorIp() {
        return freeboardCommentAuthorIp;
    }

    public void setFreeboardCommentAuthorIp(String freeboardCommentAuthorIp) {
        this.freeboardCommentAuthorIp = freeboardCommentAuthorIp;
    }

    public long getFreeboardUid() {
        return freeboardUid;
    }

    public void setFreeboardUid(long freeboardUid) {
        this.freeboardUid = freeboardUid;
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
    
    @Override
    public String toString() {
        return "FreeboardCommentDTO [freeboardCommentUid=" + freeboardCommentUid + 
                ", freeboardUid=" + freeboardUid + 
                ", userUid=" + userUid + 
                ", freeboardCommentWritetime=" + freeboardCommentWritetime + "]";
    }
}
