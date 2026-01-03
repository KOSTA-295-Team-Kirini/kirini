package dto.admin;

import java.sql.Date;

/**
 * 관리자용 사용자 패널티 DTO 클래스
 * 관리자 화면에서 패널티 정보를 표시하고 처리하기 위한 데이터 객체
 */
public class AdminUserPenaltyDTO {
    private long penaltyUid;           // penalty_uid
    private String penaltyReason;      // penalty_reason
    private Date penaltyStartDate;     // penalty_start_date
    private Date penaltyEndDate;       // penalty_end_date
    private String penaltyStatus;      // penalty_status: 'active' 또는 'inactive'
    private String penaltyDuration;    // penalty_duration: 'temporary' 또는 'permanent'
    private long userUid;              // user_uid
    private String username;           // 표시용 사용자 이름 (DB에는 없음)
    private long adminUid;             // 패널티 부여한 관리자 ID
    private String adminUsername;      // 관리자 이름 (표시용)

    // 기본 생성자
    public AdminUserPenaltyDTO() {
    }

    // 모든 필드를 포함한 생성자
    public AdminUserPenaltyDTO(long penaltyUid, String penaltyReason, Date penaltyStartDate,
                               Date penaltyEndDate, String penaltyStatus, String penaltyDuration,
                               long userUid, String username, long adminUid, String adminUsername) {
        this.penaltyUid = penaltyUid;
        this.penaltyReason = penaltyReason;
        this.penaltyStartDate = penaltyStartDate;
        this.penaltyEndDate = penaltyEndDate;
        this.penaltyStatus = penaltyStatus;
        this.penaltyDuration = penaltyDuration;
        this.userUid = userUid;
        this.username = username;
        this.adminUid = adminUid;
        this.adminUsername = adminUsername;
    }

    // Getter, Setter 메서드
    public long getPenaltyUid() {
        return penaltyUid;
    }

    public void setPenaltyUid(long penaltyUid) {
        this.penaltyUid = penaltyUid;
    }

    public String getPenaltyReason() {
        return penaltyReason;
    }

    public void setPenaltyReason(String penaltyReason) {
        this.penaltyReason = penaltyReason;
    }

    public Date getPenaltyStartDate() {
        return penaltyStartDate;
    }

    public void setPenaltyStartDate(Date penaltyStartDate) {
        this.penaltyStartDate = penaltyStartDate;
    }

    public Date getPenaltyEndDate() {
        return penaltyEndDate;
    }

    public void setPenaltyEndDate(Date penaltyEndDate) {
        this.penaltyEndDate = penaltyEndDate;
    }

    public String getPenaltyStatus() {
        return penaltyStatus;
    }

    public void setPenaltyStatus(String penaltyStatus) {
        this.penaltyStatus = penaltyStatus;
    }

    public String getPenaltyDuration() {
        return penaltyDuration;
    }

    public void setPenaltyDuration(String penaltyDuration) {
        this.penaltyDuration = penaltyDuration;
    }

    public long getUserUid() {
        return userUid;
    }

    public void setUserUid(long userUid) {
        this.userUid = userUid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getAdminUid() {
        return adminUid;
    }

    public void setAdminUid(long adminUid) {
        this.adminUid = adminUid;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    @Override
    public String toString() {
        return "AdminUserPenaltyDTO{" +
                "penaltyUid=" + penaltyUid +
                ", penaltyReason='" + penaltyReason + '\'' +
                ", penaltyStartDate=" + penaltyStartDate +
                ", penaltyEndDate=" + penaltyEndDate +
                ", penaltyStatus='" + penaltyStatus + '\'' +
                ", penaltyDuration='" + penaltyDuration + '\'' +
                ", userUid=" + userUid +
                ", username='" + username + '\'' +
                ", adminUid=" + adminUid +
                ", adminUsername='" + adminUsername + '\'' +
                '}';
    }
}