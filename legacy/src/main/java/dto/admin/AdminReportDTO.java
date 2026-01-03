package dto.admin;

import java.sql.Date;

/**
 * 관리자용 신고 정보를 담는 데이터 전송 객체
 */
public class AdminReportDTO {
    private long reportUid;           // 신고 고유 ID (report_uid)
    private long reportUserUid;       // 신고자 ID (report_user_uid)
    private String reportTargetType;  // 신고 대상 유형 (report_target_type)
    private String reportReason;      // 신고 사유 (report_reason)
    private String reportStatus;      // 신고 상태 (report_status)
    private Date reportCreatetime;    // 신고 생성 시간 (report_createtime)
    private long targetUserUid;       // 신고 대상 사용자 ID

    // 조인을 통해 가져오는 추가 정보
    private String reporterUsername;  // 신고자 이름 (users 테이블에서 조인)
    private String targetUsername;    // 신고 대상자 이름 (users 테이블에서 조인)

    // 기본 생성자
    public AdminReportDTO() {
    }

    // 모든 필드를 매개변수로 받는 생성자
    public AdminReportDTO(long reportUid, long reportUserUid, String reportTargetType, String reportReason,
                          String reportStatus, Date reportCreatetime, long targetUserUid,
                          String reporterUsername, String targetUsername) {
        this.reportUid = reportUid;
        this.reportUserUid = reportUserUid;
        this.reportTargetType = reportTargetType;
        this.reportReason = reportReason;
        this.reportStatus = reportStatus;
        this.reportCreatetime = reportCreatetime;
        this.targetUserUid = targetUserUid;
        this.reporterUsername = reporterUsername;
        this.targetUsername = targetUsername;
    }

    // Getter, Setter 메서드
    public long getReportUid() {
        return reportUid;
    }

    public void setReportUid(long reportUid) {
        this.reportUid = reportUid;
    }

    public long getReportUserUid() {
        return reportUserUid;
    }

    public void setReportUserUid(long reportUserUid) {
        this.reportUserUid = reportUserUid;
    }

    public String getReportTargetType() {
        return reportTargetType;
    }

    public void setReportTargetType(String reportTargetType) {
        this.reportTargetType = reportTargetType;
    }

    public String getReportReason() {
        return reportReason;
    }

    public void setReportReason(String reportReason) {
        this.reportReason = reportReason;
    }

    public String getReportStatus() {
        return reportStatus;
    }

    public void setReportStatus(String reportStatus) {
        this.reportStatus = reportStatus;
    }

    public Date getReportCreatetime() {
        return reportCreatetime;
    }

    public void setReportCreatetime(Date reportCreatetime) {
        this.reportCreatetime = reportCreatetime;
    }

    public long getTargetUserUid() {
        return targetUserUid;
    }

    public void setTargetUserUid(long targetUserUid) {
        this.targetUserUid = targetUserUid;
    }

    public String getReporterUsername() {
        return reporterUsername;
    }

    public void setReporterUsername(String reporterUsername) {
        this.reporterUsername = reporterUsername;
    }

    public String getTargetUsername() {
        return targetUsername;
    }

    public void setTargetUsername(String targetUsername) {
        this.targetUsername = targetUsername;
    }

    @Override
    public String toString() {
        return "AdminReportDTO [reportUid=" + reportUid + ", reportUserUid=" + reportUserUid + ", reportTargetType="
                + reportTargetType + ", reportReason=" + reportReason + ", reportStatus=" + reportStatus
                + ", reportCreatetime=" + reportCreatetime + ", targetUserUid=" + targetUserUid + ", reporterUsername="
                + reporterUsername + ", targetUsername=" + targetUsername + "]";
    }
}