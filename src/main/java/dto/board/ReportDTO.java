package dto.board;

import java.time.LocalDateTime;

public class ReportDTO {
    private long reportId;
    private long reporterId; // 신고한 사용자
    private long targetId; // 신고 대상 ID
    private String targetType; // 대상 유형 (게시글, 댓글, 채팅 등)
    private String reportReason;
    private LocalDateTime reportDate;
    private boolean isProcessed;
    private long processedById; // 처리한 관리자 ID
    
    // Getter, Setter, 생성자 등
}