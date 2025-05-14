package dto.admin;

import java.sql.Timestamp;

/**
 * 삭제 로그 정보를 담는 데이터 전송 객체
 */
public class AdminDeleteLogDTO {
    // 공통 필드
    private long logDeleteUid;          // 로그 ID
    private String logDeleteBoardtype;  // 게시판 유형 (freeboard, news, notice, inquiry, chatboard)
    private Timestamp logDeleteDate;    // 삭제 날짜
    private long userUid;               // 삭제 작업을 수행한 사용자 ID
    
    // 게시글 또는 댓글 ID (타입에 따라 다름)
    private long deletedItemUid;        // 삭제된 항목 ID (게시글 ID 또는 댓글 ID)
    
    // 추가 정보 (JOIN으로 가져올 데이터)
    private String deletedItemTitle;    // 삭제된 항목 제목 (게시글 제목 또는 댓글 내용 일부)
    private String deletedByUsername;   // 삭제한 관리자 이름
    private String originalAuthorName;  // 원 작성자 이름
    private String itemType;            // "post" 또는 "comment"
    
    // 기본 생성자
    public AdminDeleteLogDTO() {
    }
    
    // Getter, Setter 메서드
    public long getLogDeleteUid() {
        return logDeleteUid;
    }
    
    public void setLogDeleteUid(long logDeleteUid) {
        this.logDeleteUid = logDeleteUid;
    }
    
    public String getLogDeleteBoardtype() {
        return logDeleteBoardtype;
    }
    
    public void setLogDeleteBoardtype(String logDeleteBoardtype) {
        this.logDeleteBoardtype = logDeleteBoardtype;
    }
    
    public Timestamp getLogDeleteDate() {
        return logDeleteDate;
    }
    
    public void setLogDeleteDate(Timestamp logDeleteDate) {
        this.logDeleteDate = logDeleteDate;
    }
    
    public long getUserUid() {
        return userUid;
    }
    
    public void setUserUid(long userUid) {
        this.userUid = userUid;
    }
    
    public long getDeletedItemUid() {
        return deletedItemUid;
    }
    
    public void setDeletedItemUid(long deletedItemUid) {
        this.deletedItemUid = deletedItemUid;
    }
    
    public String getDeletedItemTitle() {
        return deletedItemTitle;
    }
    
    public void setDeletedItemTitle(String deletedItemTitle) {
        this.deletedItemTitle = deletedItemTitle;
    }
    
    public String getDeletedByUsername() {
        return deletedByUsername;
    }
    
    public void setDeletedByUsername(String deletedByUsername) {
        this.deletedByUsername = deletedByUsername;
    }
    
    public String getOriginalAuthorName() {
        return originalAuthorName;
    }
    
    public void setOriginalAuthorName(String originalAuthorName) {
        this.originalAuthorName = originalAuthorName;
    }
    
    public String getItemType() {
        return itemType;
    }
    
    public void setItemType(String itemType) {
        this.itemType = itemType;
    }
    
    @Override
    public String toString() {
        return "AdminDeleteLogDTO{" +
               "logDeleteUid=" + logDeleteUid +
               ", logDeleteBoardtype='" + logDeleteBoardtype + '\'' +
               ", logDeleteDate=" + logDeleteDate +
               ", userUid=" + userUid +
               ", deletedItemUid=" + deletedItemUid +
               ", deletedItemTitle='" + deletedItemTitle + '\'' +
               ", deletedByUsername='" + deletedByUsername + '\'' +
               ", originalAuthorName='" + originalAuthorName + '\'' +
               ", itemType='" + itemType + '\'' +
               '}';
    }
}

