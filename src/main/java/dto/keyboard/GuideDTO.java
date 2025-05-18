package dto.keyboard;

import java.time.LocalDateTime;

public class GuideDTO {
    private long guideId;
    private String term;
    private String description;
    private String category;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private String url; // URL 필드 추가
    
    // 기본 생성자
    public GuideDTO() {
    }
      // 모든 필드를 받는 생성자
    public GuideDTO(long guideId, String term, String description, String category, 
                   LocalDateTime createDate, LocalDateTime updateDate, String url) {
        this.guideId = guideId;
        this.term = term;
        this.description = description;
        this.category = category;
        this.createDate = createDate;
        this.updateDate = updateDate;
        this.url = url;
    }
    
    // Getter 메서드
    public long getGuideId() {
        return guideId;
    }
    
    public String getTerm() {
        return term;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public LocalDateTime getCreateDate() {
        return createDate;
    }
    
    public LocalDateTime getUpdateDate() {
        return updateDate;
    }
    
    // URL 관련 Getter/Setter
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    // 호환성을 위한 별칭 메서드
    public long getId() {
        return guideId;
    }
    
    public void setId(long id) {
        this.guideId = id;
    }
    
    // Setter 메서드
    public void setGuideId(long guideId) {
        this.guideId = guideId;
    }
    
    public void setTerm(String term) {
        this.term = term;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }
    
    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }
    
    @Override
    public String toString() {
        return "GuideDTO [guideId=" + guideId + ", term=" + term + ", description=" + description 
                + ", category=" + category + ", createDate=" + createDate + ", updateDate=" + updateDate + "]";
    }
}