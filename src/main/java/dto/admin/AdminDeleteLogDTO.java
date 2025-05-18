package dto.admin;

import java.sql.Date;

/**
 * 삭제된 게시글/댓글 로그 DTO 클래스
 */
public class AdminDeleteLogDTO {
    private long logId;
    private String boardType;
    private long contentId;
    private String contentTitle;
    private String contentText;
    private String deleteReason;
    private Date deleteDate;
    private long adminUid;
    private String adminName;
    private long userUid;
    private String userName;
    
    // 기본 생성자
    public AdminDeleteLogDTO() {
    }
    
    // Getter/Setter 메소드
    public long getLogId() {
        return logId;
    }
    
    public void setLogId(long logId) {
        this.logId = logId;
    }
    
    public String getBoardType() {
        return boardType;
    }
    
    public void setBoardType(String boardType) {
        this.boardType = boardType;
    }
    
    public long getContentId() {
        return contentId;
    }
    
    public void setContentId(long contentId) {
        this.contentId = contentId;
    }
    
    public String getContentTitle() {
        return contentTitle;
    }
    
    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }
    
    public String getContentText() {
        return contentText;
    }
    
    public void setContentText(String contentText) {
        this.contentText = contentText;
    }
    
    public String getDeleteReason() {
        return deleteReason;
    }
    
    public void setDeleteReason(String deleteReason) {
        this.deleteReason = deleteReason;
    }
    
    public Date getDeleteDate() {
        return deleteDate;
    }
    
    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }
    
    public long getAdminUid() {
        return adminUid;
    }
    
    public void setAdminUid(long adminUid) {
        this.adminUid = adminUid;
    }
    
    public String getAdminName() {
        return adminName;
    }
    
    public void setAdminName(String adminName) {
        this.adminName = adminName;
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
}
