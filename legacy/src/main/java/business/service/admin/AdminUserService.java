package business.service.admin;

import java.sql.SQLException;
import java.util.List;

import dto.admin.AdminUserPenaltyDTO;
import repository.dao.admin.AdminUserPenaltyDAO;
import util.logging.LoggerConfig;

/**
 * 관리자용 사용자 관리 서비스 클래스
 */
public class AdminUserService {

    private AdminUserPenaltyDAO penaltyDAO;

    public AdminUserService() {
        penaltyDAO = new AdminUserPenaltyDAO();
    }

    /**
     * 모든 사용자 제재 내역을 조회합니다.
     *
     * @return 사용자 제재 내역 목록
     */
    public List<AdminUserPenaltyDTO> getAllUserPenalty() {
        try {
            return penaltyDAO.getAllUserPenalty();
        } catch (SQLException e) {
            LoggerConfig.logError(AdminUserService.class, "getAllUserPenalty", "모든 사용자 제재 내역 조회 실패", e);
            return null;
        }
    }

    /**
     * 특정 사용자의 제재 내역을 검색합니다.
     *
     * @param userUid 사용자 ID
     * @return 제재 내역 목록
     */
    public List<AdminUserPenaltyDTO> getUserPenaltyByUserId(long userUid) {
        try {
            return penaltyDAO.getUserPenaltyByUserId(userUid);
        } catch (SQLException e) {
            LoggerConfig.logError(AdminUserService.class, "getUserPenaltyByUserId", "사용자 ID별 제재 내역 조회 실패 - 사용자ID: " + userUid, e);
            return null;
        }
    }

    /**
     * 사용자의 패널티 상태를 변경합니다. (제재 내용 해제)
     *
     * @param penaltyUid 패널티 ID
     * @param newStatus  새로운 상태
     * @return 처리 성공 여부
     */
    public boolean updateUserPenaltyStatusByUserId(long penaltyUid, String newStatus) {
        try {
            boolean result = penaltyDAO.updateUserPenaltyStatusByPenaltyId(penaltyUid, newStatus);
            if (result) {
                LoggerConfig.logBusinessAction(AdminUserService.class, "updateUserPenaltyStatusByUserId",
                        "패널티 상태 변경", "패널티ID: " + penaltyUid + ", 신규상태: " + newStatus, null);
            }
            return result;
        } catch (SQLException e) {
            LoggerConfig.logError(AdminUserService.class, "updateUserPenaltyStatusByUserId",
                    "패널티 상태 변경 실패 - 패널티ID: " + penaltyUid + ", 신규상태: " + newStatus, e);
            return false;
        }
    }

    /**
     * 사용자에게 패널티를 부여합니다.
     *
     * @param penalty 패널티 정보
     * @return 처리 성공 여부
     */
    public boolean addUserPenalty(AdminUserPenaltyDTO penalty) {
        try {
            boolean result = penaltyDAO.addUserPenalty(penalty);
            if (result) {
                LoggerConfig.logBusinessAction(AdminUserService.class, "addUserPenalty",
                        "사용자 패널티 추가", "사용자ID: " + penalty.getUserUid() +
                                ", 사유: " + penalty.getPenaltyReason() +
                                ", 기간: " + penalty.getPenaltyDuration(), null);
            }
            return result;
        } catch (SQLException e) {
            LoggerConfig.logError(AdminUserService.class, "addUserPenalty",
                    "패널티 추가 실패 - 사용자ID: " + penalty.getUserUid(), e);
            return false;
        }
    }
}
