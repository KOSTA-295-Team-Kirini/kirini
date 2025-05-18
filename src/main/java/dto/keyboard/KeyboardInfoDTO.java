package dto.keyboard;

import java.sql.Date;
import java.util.List;

public class KeyboardInfoDTO {    private long keyboardId;
    private String name;
    private String manufacturer;
    private String description;
    private String imageUrl;
    private double averageScore;
    private String switchType;
    private String layoutType;
    private String connectType;
    private List<String> tags;
    private List<Long> tagIds; // 호환성을 위해 추가
    private String type; // 호환성을 위해 추가
    private String layout; // 호환성을 위해 추가
    private Date releaseDate; // 호환성을 위해 추가
    private int price; // 호환성을 위해 추가
    private long categoryId; // 키보드 카테고리 ID
    
    // 스크랩 관련 속성
    private java.sql.Timestamp scrapDate;
    
    // 기본 생성자
    public KeyboardInfoDTO() {
    }
    
    // Getter 메서드들
    public long getKeyboardId() {
        return keyboardId;
    }
    
    public String getName() {
        return name;
    }
    
    public String getManufacturer() {
        return manufacturer;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public double getAverageScore() {
        return averageScore;
    }
    
    public String getSwitchType() {
        return switchType;
    }
    
    public String getLayoutType() {
        return layoutType;
    }
    
    public String getConnectType() {
        return connectType;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    // 스크랩 날짜 Getter
    public java.sql.Timestamp getScrapDate() {
        return scrapDate;
    }
    
    // Setter 메서드들
    public void setKeyboardId(long keyboardId) {
        this.keyboardId = keyboardId;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }
    
    public void setSwitchType(String switchType) {
        this.switchType = switchType;
    }
    
    public void setLayoutType(String layoutType) {
        this.layoutType = layoutType;
    }
    
    public void setConnectType(String connectType) {
        this.connectType = connectType;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    // 스크랩 날짜 Setter
    public void setScrapDate(java.sql.Timestamp scrapDate) {
        this.scrapDate = scrapDate;
    }
    
    // 호환성을 위한 별칭 메서드들
    public long getId() {
        return keyboardId;
    }
    
    public void setId(long id) {
        this.keyboardId = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getLayout() {
        return layout;
    }
    
    public void setLayout(String layout) {
        this.layout = layout;
    }
    
    public Date getReleaseDate() {
        return releaseDate;
    }
    
    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }
    
    public int getPrice() {
        return price;
    }
    
    public void setPrice(int price) {
        this.price = price;
    }
    
    public String getConnectionType() {
        return connectType;
    }
    
    public void setConnectionType(String connectionType) {
        this.connectType = connectionType;
    }
    
    public List<Long> getTagIds() {
        return tagIds;
    }
      public void setTagIds(List<Long> tagIds) {
        this.tagIds = tagIds;
    }
    
    // 카테고리 ID getter/setter
    public long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }
}