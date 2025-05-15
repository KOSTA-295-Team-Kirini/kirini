package dto.board;

import java.time.LocalDateTime;

/**
 * 첨부파일 정보를 담는 DTO 클래스
 */
public class AttachmentDTO {
    private long attachId;        // 첨부파일 ID
    private long postId;          // 게시글 ID
    private String fileName;      // 원본 파일명
    private String filePath;      // 저장 경로
    private long fileSize;        // 파일 크기
    private LocalDateTime uploadDate; // 업로드 날짜
    
    // 기본 생성자
    public AttachmentDTO() {
    }
    
    // 게터/세터 메서드
    public long getAttachId() {
        return attachId;
    }
    
    public void setAttachId(long attachId) {
        this.attachId = attachId;
    }
    
    public long getPostId() {
        return postId;
    }
    
    public void setPostId(long postId) {
        this.postId = postId;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public LocalDateTime getUploadDate() {
        return uploadDate;
    }
    
    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }
}