package dto.board;

import java.time.LocalDateTime;

public class PostDTO {
    private long postId;
    private String boardType;
    private String title;
    private String content;
    private int readCount;
    private int recommendCount;
    private LocalDateTime writeTime;
    private LocalDateTime modifyTime;
    private String status;
    private long userId;
    
    // 기본 생성자
    public PostDTO() {
    }
    
    // Getter/Setter 메소드
    public long getPostId() {
        return postId;
    }
    
    public void setPostId(long postId) {
        this.postId = postId;
    }
    
    public String getBoardType() {
        return boardType;
    }
    
    public void setBoardType(String boardType) {
        this.boardType = boardType;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public int getReadCount() {
        return readCount;
    }
    
    public void setReadCount(int readCount) {
        this.readCount = readCount;
    }
    
    public int getRecommendCount() {
        return recommendCount;
    }
    
    public void setRecommendCount(int recommendCount) {
        this.recommendCount = recommendCount;
    }
    
    public LocalDateTime getWriteTime() {
        return writeTime;
    }
    
    public void setWriteTime(LocalDateTime writeTime) {
        this.writeTime = writeTime;
    }
    
    public LocalDateTime getModifyTime() {
        return modifyTime;
    }
    
    public void setModifyTime(LocalDateTime modifyTime) {
        this.modifyTime = modifyTime;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public long getUserId() {
        return userId;
    }
      public void setUserId(long userId) {
        this.userId = userId;
    }
    
    /**
     * getViewCount 메서드 - 마이페이지 컨트롤러 호환용
     * readCount 필드를 반환합니다.
     */
    public int getViewCount() {
        return readCount;
    }
}
