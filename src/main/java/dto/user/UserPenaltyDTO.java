package dto.user;

import java.time.LocalDateTime;

public class UserPenaltyDTO {
    private long penaltyId;
    private long userId;
    private String penaltyReason;
    private LocalDateTime penaltyStartDate;
    private LocalDateTime penaltyEndDate;
    private boolean isActive;
    private long adminId; // 제재를 가한 관리자 ID
    
    // Getter, Setter, 생성자 등
}