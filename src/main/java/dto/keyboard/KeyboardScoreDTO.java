package dto.keyboard;

import java.time.LocalDateTime;

public class KeyboardScoreDTO {
    private long scoreId;
    private long keyboardId;
    private long userId;
    private String userName;
    private int scoreValue;
    private String review;
    private LocalDateTime createdAt;
    private String keyboardName;
    
    // 기본 생성자
    public KeyboardScoreDTO() {
    }
    
    // Getter/Setter 메소드
    public long getScoreId() {
        return scoreId;
    }
    
    public void setScoreId(long scoreId) {
        this.scoreId = scoreId;
    }
    
    public long getKeyboardId() {
        return keyboardId;
    }
    
    public void setKeyboardId(long keyboardId) {
        this.keyboardId = keyboardId;
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
    
    public int getScoreValue() {
        return scoreValue;
    }
    
    public void setScoreValue(int scoreValue) {
        this.scoreValue = scoreValue;
    }
    
    public String getReview() {
        return review;
    }
    
    public void setReview(String review) {
        this.review = review;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getKeyboardName() {
        return keyboardName;
    }
      public void setKeyboardName(String keyboardName) {
        this.keyboardName = keyboardName;
    }
    
    /**
     * getScoreTime 메서드 - 마이페이지 컨트롤러 호환용
     * createdAt 필드를 반환합니다.
     */
    public LocalDateTime getScoreTime() {
        return createdAt;
    }
}