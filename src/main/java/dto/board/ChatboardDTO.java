package dto.board;

import java.time.LocalDateTime;

public class ChatboardDTO {
    private long chatboardUid;         // PK
    private String chatboardTitle;     // 메시지 내용 (한줄)
    private LocalDateTime chatboardWritetime; // 작성시간
    private LocalDateTime chatboardModifyTime; // 수정시간
    private String chatboardAuthorIp;  // IP 주소
    private String chatboardDeleted;   // 삭제 여부
    private long userUid;              // 작성자 ID (비노출)
    
    // 추가 필드 - 익명 닉네임 생성용
    private String anonymousNickname;  // 랜덤 생성 익명 닉네임
    
    // 기본 생성자
    public ChatboardDTO() {}
    
    // 게시글 작성용 생성자
    public ChatboardDTO(String chatboardTitle, String chatboardAuthorIp, long userUid) {
        this.chatboardTitle = chatboardTitle;
        this.chatboardAuthorIp = chatboardAuthorIp;
        this.userUid = userUid;
        this.chatboardDeleted = "maintained";
    }
    
    // getter와 setter 메서드들
    public long getChatboardUid() {
        return chatboardUid;
    }

    public void setChatboardUid(long chatboardUid) {
        this.chatboardUid = chatboardUid;
    }

    public String getChatboardTitle() {
        return chatboardTitle;
    }

    public void setChatboardTitle(String chatboardTitle) {
        this.chatboardTitle = chatboardTitle;
    }

    public LocalDateTime getChatboardWritetime() {
        return chatboardWritetime;
    }

    public void setChatboardWritetime(LocalDateTime chatboardWritetime) {
        this.chatboardWritetime = chatboardWritetime;
    }

    public LocalDateTime getChatboardModifyTime() {
        return chatboardModifyTime;
    }

    public void setChatboardModifyTime(LocalDateTime chatboardModifyTime) {
        this.chatboardModifyTime = chatboardModifyTime;
    }

    public String getChatboardAuthorIp() {
        return chatboardAuthorIp;
    }

    public void setChatboardAuthorIp(String chatboardAuthorIp) {
        this.chatboardAuthorIp = chatboardAuthorIp;
    }

    public String getChatboardDeleted() {
        return chatboardDeleted;
    }

    public void setChatboardDeleted(String chatboardDeleted) {
        this.chatboardDeleted = chatboardDeleted;
    }

    public long getUserUid() {
        return userUid;
    }

    public void setUserUid(long userUid) {
        this.userUid = userUid;
    }

    public String getAnonymousNickname() {
        return anonymousNickname;
    }

    public void setAnonymousNickname(String anonymousNickname) {
        this.anonymousNickname = anonymousNickname;
    }
}