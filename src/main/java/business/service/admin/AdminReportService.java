package business.service.admin;

import java.sql.SQLException;
import java.util.List;

import dto.admin.AdminReportDTO;
import dto.admin.AdminUserPenaltyDTO;
import repository.dao.admin.AdminReportDAO;
import repository.dao.admin.AdminUserPenaltyDAO;

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
            e.printStackTrace();
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
            e.printStackTrace();
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
            return penaltyDAO.updateUserPenaltyStatusByPenaltyId(penaltyUid, newStatus);
        } catch (SQLException e) {
            e.printStackTrace();
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
            return reportDAO.updateReportStatus(reportUid, status);
        } catch (SQLException e) {
            e.printStackTrace();
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
                reportDAO.updateReportStatus(reportUid, "처리완료");
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
