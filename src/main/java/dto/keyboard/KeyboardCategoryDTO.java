package dto.keyboard;

/**
 * 키보드 카테고리 정보를 담는 DTO 클래스
 */
public class KeyboardCategoryDTO {
    private long keyboardCategoryUid;
    private String keyboardCategoryName;
    private String description;
    private String type; // switch, layout 등의 카테고리 타입을 구분하기 위한 필드
    
    // 기본 생성자
    public KeyboardCategoryDTO() {
    }
    
    // 전체 필드 생성자
    public KeyboardCategoryDTO(long keyboardCategoryUid, String keyboardCategoryName, String description, String type) {
        this.keyboardCategoryUid = keyboardCategoryUid;
        this.keyboardCategoryName = keyboardCategoryName;
        this.description = description;
        this.type = type;
    }
    
    // Getter 메서드
    public long getKeyboardCategoryUid() {
        return keyboardCategoryUid;
    }
    
    public String getKeyboardCategoryName() {
        return keyboardCategoryName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getType() {
        return type;
    }
    
    // Setter 메서드
    public void setKeyboardCategoryUid(long keyboardCategoryUid) {
        this.keyboardCategoryUid = keyboardCategoryUid;
    }
    
    public void setKeyboardCategoryName(String keyboardCategoryName) {
        this.keyboardCategoryName = keyboardCategoryName;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    // 호환성을 위한 편의 메서드
    public void setName(String name) {
        this.keyboardCategoryName = name;
    }
}