package dto.keyboard;

public class KeyboardTagDTO {
    private long tagId;
    private String tagName;
    private String tagType; // 'admin' 또는 'user'
    private int voteCount;
    private String userVote; // 현재 사용자의 투표 ('up', 'down' 또는 null)
    
    // 기본 생성자
    public KeyboardTagDTO() {
    }
    
    // Getter/Setter 메소드
    public long getTagId() {
        return tagId;
    }
    
    public void setTagId(long tagId) {
        this.tagId = tagId;
    }
    
    public String getTagName() {
        return tagName;
    }
    
    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
    
    public String getTagType() {
        return tagType;
    }
    
    public void setTagType(String tagType) {
        this.tagType = tagType;
    }
    
    public int getVoteCount() {
        return voteCount;
    }
    
    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }
    
    public String getUserVote() {
        return userVote;
    }
    
    public void setUserVote(String userVote) {
        this.userVote = userVote;
    }
    
    // 호환성을 위한 별칭 메서드들
    public long getId() {
        return tagId;
    }
    
    public void setId(long id) {
        this.tagId = id;
    }
    
    public String getName() {
        return tagName;
    }
    
    public void setName(String name) {
        this.tagName = name;
    }
    
    public String getStatus() {
        return tagType;
    }
    
    public void setStatus(String status) {
        this.tagType = status;
    }
}