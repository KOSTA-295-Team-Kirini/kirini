package business.service.admin;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

import com.google.gson.JsonObject;

import dto.admin.AdminReportDTO;
import dto.admin.AdminUserPenaltyDTO;
import repository.dao.admin.AdminReportDAO;
import repository.dao.admin.AdminUserPenaltyDAO;

/**
 * 관리자용 신고 처리 서비스
 * 신고 내역 조회 및 패널티 부여 기능 제공
 */
public class AdminReportService {
    private AdminReportDAO reportDAO;
    private AdminUserPenaltyDAO penaltyDAO;
    
    public AdminReportService() {
        reportDAO = new AdminReportDAO();
        penaltyDAO = new AdminUserPenaltyDAO();
    }
    
    /**
     * 모든 신고 내역을 조회합니다.
     * 
     * @return 전체 신고 목록
     */
    public List<AdminReportDTO> getAllReports() {
        try {
            return reportDAO.getAllReports();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 조건에 맞는 신고 내역을 조회합니다.
     * 
     * @param status 신고 상태 (active, inactive, null: 전체)
     * @param targetType 신고 대상 유형 (user, post, comment, null: 전체)
     * @return 조건에 맞는 신고 목록
     */
    public List<AdminReportDTO> getReportsByCondition(String status, String targetType) {
        try {
            return reportDAO.getReportsByCondition(status, targetType);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 특정 신고의 상세 정보를 조회합니다.
     * 
     * @param reportUid 신고 ID
     * @return 신고 상세 정보
     */
    public AdminReportDTO getReportById(long reportUid) {
        try {
            return reportDAO.getReportById(reportUid);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 신고를 처리하고 해당 사용자에게 패널티를 부여합니다.
     * 
     * @param reportUid 처리할 신고 ID
     * @param targetUserUid 패널티를 부여할 사용자 ID
     * @param penaltyReason 패널티 사유
     * @param penaltyDuration 패널티 기간 유형 (temporary/permanent)
     * @param penaltyEndDate 패널티 종료일 (temporary인 경우만 해당)
     * @param adminUid 패널티를 부여한 관리자 ID
     * @return 처리 성공 여부
     */
    public boolean processReportAndApplyPenalty(long reportUid, long targetUserUid, 
                                              String penaltyReason, String penaltyDuration, 
                                              Date penaltyEndDate, long adminUid) {
        try {
            // 1. 신고 상태를 '처리됨(inactive)'으로 업데이트
            boolean reportUpdated = reportDAO.updateReportStatus(reportUid, "inactive");
            if (!reportUpdated) {
                return false;
            }
            
            // 2. 패널티 정보 생성
            AdminUserPenaltyDTO penalty = new AdminUserPenaltyDTO();
            penalty.setPenaltyReason(penaltyReason);
            penalty.setPenaltyStartDate(new Date(System.currentTimeMillis())); // 현재 날짜
            penalty.setPenaltyEndDate(penaltyEndDate);
            penalty.setPenaltyStatus("active");
            penalty.setPenaltyDuration(penaltyDuration);
            penalty.setUserUid(targetUserUid);
            penalty.setAdminUid(adminUid);
            
            // 3. 패널티 부여
            return penaltyDAO.addUserPenalty(penalty);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 신고를 거부합니다. (패널티 부여 없이 처리)
     * 
     * @param reportUid 처리할 신고 ID
     * @return 처리 성공 여부
     */
    public boolean rejectReport(long reportUid) {
        try {
            return reportDAO.updateReportStatus(reportUid, "inactive");
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 신고 통계를 가져옵니다.
     * 
     * @return 신고 통계 정보 (총 신고 수, 처리된 신고 수, 처리 대기 중인 신고 수)
     */
    public JsonObject getReportStatistics() {
        try {
            JsonObject statistics = new JsonObject();
            int totalCount = reportDAO.getReportCount(null);
            int activeCount = reportDAO.getReportCount("active");
            int inactiveCount = reportDAO.getReportCount("inactive");
            
            statistics.addProperty("totalCount", totalCount);
            statistics.addProperty("activeCount", activeCount);
            statistics.addProperty("inactiveCount", inactiveCount);
            
            return statistics;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}