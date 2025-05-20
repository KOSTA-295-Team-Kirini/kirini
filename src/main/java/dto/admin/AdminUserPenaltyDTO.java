package dto.admin;

import java.sql.Date;

/**
 * 관리자용 사용자 패널티 DTO 클래스
 * 관리자 화면에서 패널티 정보를 표시하고 처리하기 위한 데이터 객체
 */
public class AdminUserPenaltyDTO {
    private long penaltyUid;           // penalty_uid
    private Date penaltyStartDate;     // penalty_start_date
    private Date penaltyEndDate;       // penalty_end_date
    private String penaltyStatus;      // penalty_status: 'active' 또는 'inactive'
    private long userUid;              // user_uid
    private String username;           // 표시용 사용자 이름 (DB의 user_name 매핑)
    private String userEmail;          // 표시용 사용자 이메일
    private int penaltyDays;           // 차단 일수 (계산된 값)


    // 기본 생성자
    public AdminUserPenaltyDTO() {
    }

    // 모든 필드를 사용하는 생성자 (penaltyDays는 보통 계산되므로 제외하거나 필요시 추가)
    public AdminUserPenaltyDTO(long penaltyUid, Date penaltyStartDate,
                         Date penaltyEndDate, String penaltyStatus,
                         long userUid, String username, String userEmail) {
        this.penaltyUid = penaltyUid;
        this.penaltyStartDate = penaltyStartDate;
        this.penaltyEndDate = penaltyEndDate;
        this.penaltyStatus = penaltyStatus;
        this.userUid = userUid;
        this.username = username;
        this.userEmail = userEmail;
        // penaltyDays는 보통 startDate와 endDate로 계산되므로 생성자에서 직접 받지 않을 수 있음
    }

    // Getter, Setter 메서드
    public long getPenaltyUid() {
        return penaltyUid;
    }

    public void setPenaltyUid(long penaltyUid) {
        this.penaltyUid = penaltyUid;
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

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public int getPenaltyDays() {
        return penaltyDays;
    }

    public void setPenaltyDays(int penaltyDays) {
        this.penaltyDays = penaltyDays;
    }

    @Override
    public String toString() {
        return "AdminUserPenaltyDTO{" +
                "penaltyUid=" + penaltyUid +
                ", penaltyStartDate=" + penaltyStartDate +
                ", penaltyEndDate=" + penaltyEndDate +
                ", penaltyStatus='" + penaltyStatus + '\'' +
                ", userUid=" + userUid +
                ", username='" + username + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", penaltyDays=" + penaltyDays +
                '}';
    }
}