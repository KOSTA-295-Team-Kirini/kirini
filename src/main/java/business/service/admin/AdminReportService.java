package business.service.admin;

import java.sql.SQLException;
import java.util.List;

import dto.admin.AdminReportDTO;
import dto.admin.AdminUserPenaltyDTO;
import repository.dao.admin.AdminReportDAO;
import repository.dao.admin.AdminUserPenaltyDAO;
import util.logging.LoggerConfig;

/**
 * 관리자용 신고 처리 서비스 클래스
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
     * @return 신고 내역 목록
     */
    public List<AdminReportDTO> getAllReport() {
        try {
            return reportDAO.getAllReports();
        } catch (SQLException e) {
            LoggerConfig.logError(AdminReportService.class, "getAllReport", "신고 내역 조회 실패", e);
            return null;
        }
    }
    
    /**
     * 조건에 맞는 신고 내역을 검색합니다.
     * 
     * @param status 신고 상태
     * @param targetType 신고 대상 타입
     * @return 검색 결과 목록
     */
    public List<AdminReportDTO> getReportsByCondition(String status, String targetType) {
        try {
            return reportDAO.getReportsByCondition(status, targetType);
        } catch (SQLException e) {
            LoggerConfig.logError(AdminReportService.class, "getReportsByCondition", 
                              "조건별 신고 조회 실패 - 상태: " + status + ", 대상타입: " + targetType, e);
            return null;
        }
    }
    
    /**
     * 사용자의 패널티 상태를 변경합니다. (불량 이용자 제재)
     * 
     * @param penaltyUid 패널티 ID
     * @param newStatus 새로운 상태
     * @return 처리 성공 여부
     */
    public boolean updateUserPenaltyStatusByUserId(long penaltyUid, String newStatus) {
        try {
            boolean result = penaltyDAO.updateUserPenaltyStatusByPenaltyId(penaltyUid, newStatus);
            if (result) {
                LoggerConfig.logBusinessAction(AdminReportService.class, "updateUserPenaltyStatusByUserId", 
                                        "패널티 상태 변경", "패널티ID: " + penaltyUid + ", 신규상태: " + newStatus, null);
            }
            return result;
        } catch (SQLException e) {
            LoggerConfig.logError(AdminReportService.class, "updateUserPenaltyStatusByUserId", 
                              "패널티 상태 변경 실패 - 패널티ID: " + penaltyUid + ", 신규상태: " + newStatus, e);
            return false;
        }
    }
    
    /**
     * 신고 상태를 변경합니다.
     * 
     * @param reportUid 신고 ID
     * @param status 새로운 상태
     * @return 처리 성공 여부
     */
    public boolean updateReportStatus(long reportUid, String status) {
        try {
            boolean result = reportDAO.updateReportStatus(reportUid, status);
            if (result) {
                LoggerConfig.logBusinessAction(AdminReportService.class, "updateReportStatus", 
                                        "신고 상태 변경", "신고ID: " + reportUid + ", 신규상태: " + status, null);
            }
            return result;
        } catch (SQLException e) {
            LoggerConfig.logError(AdminReportService.class, "updateReportStatus", 
                              "신고 상태 변경 실패 - 신고ID: " + reportUid + ", 신규상태: " + status, e);
            return false;
        }
    }
    
    /**
     * 신고된 사용자에게 패널티를 부여합니다.
     * 
     * @param penalty 패널티 정보
     * @param reportUid 신고 ID
     * @return 처리 성공 여부
     */
    public boolean applyPenaltyToUser(AdminUserPenaltyDTO penalty, long reportUid) {
        try {
            boolean result = penaltyDAO.addUserPenalty(penalty);
            if (result) {
                // 신고 상태를 '처리완료'로 변경
                reportDAO.updateReportStatus(reportUid, "처리완료");                LoggerConfig.logBusinessAction(AdminReportService.class, "applyPenaltyToUser", 
                                        "사용자 패널티 부여", "신고ID: " + reportUid + 
                                        ", 사용자ID: " + penalty.getUserUid() + 
                                        ", 패널티: " + penalty.getPenaltyDuration(), null);
            }
            return result;
        } catch (SQLException e) {
            LoggerConfig.logError(AdminReportService.class, "applyPenaltyToUser", 
                              "사용자 패널티 부여 실패 - 신고ID: " + reportUid + 
                              ", 사용자ID: " + penalty.getUserUid(), e);
            return false;
        }
    }
}
