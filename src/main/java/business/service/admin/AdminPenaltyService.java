package business.service.admin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dto.admin.AdminUserPenaltyDTO; // 변경된 DTO
import repository.dao.admin.AdminUserPenaltyDAO; // 변경된 DAO

public class AdminPenaltyService {
    private AdminUserPenaltyDAO penaltyDAO;

    public AdminPenaltyService() {
        penaltyDAO = new AdminUserPenaltyDAO();
    }

    /**
     * 모든 사용자 패널티 목록을 조회합니다.
     */
    public List<AdminUserPenaltyDTO> getAllUserPenalties() {
        try {
            return penaltyDAO.getAllUserPenalty();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 특정 사용자의 패널티 목록을 조회합니다.
     */
    public List<AdminUserPenaltyDTO> getUserPenaltiesByUserId(long userUid) {
        try {
            return penaltyDAO.getUserPenaltyByUserId(userUid);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 패널티 상태를 업데이트합니다.
     */
    public boolean updateUserPenaltyStatus(long penaltyUid, String penaltyStatus) {
        try {
            // 상태값 검증 ('active' 또는 'inactive'만 허용)
            if (!penaltyStatus.equals("active") && !penaltyStatus.equals("inactive")) {
                return false;
            }

            return penaltyDAO.updateUserPenaltyStatusByPenaltyId(penaltyUid, penaltyStatus);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 새로운 패널티를 추가합니다.
     */
    public boolean addUserPenalty(AdminUserPenaltyDTO penalty) {
        try {
            // 필수 값 검증
            if (penalty.getUserUid() <= 0 || 
                penalty.getPenaltyReason() == null || 
                penalty.getPenaltyStartDate() == null || 
                penalty.getPenaltyStatus() == null || 
                penalty.getPenaltyDuration() == null) {
                return false;
            }

            // 상태값 검증
            if (!penalty.getPenaltyStatus().equals("active") && 
                !penalty.getPenaltyStatus().equals("inactive")) {
                return false;
            }

            // 기간 유형 검증
            if (!penalty.getPenaltyDuration().equals("temporary") && 
                !penalty.getPenaltyDuration().equals("permanent")) {
                return false;
            }

            return penaltyDAO.addUserPenalty(penalty);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}