package business.service.admin;

import java.sql.SQLException;
import java.util.List;

import dto.admin.AdminUserPenaltyDTO;
import repository.dao.admin.AdminUserPenaltyDAO;

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
            e.printStackTrace();
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
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 사용자의 패널티 상태를 변경합니다. (제재 내용 해제)
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
     * 사용자에게 패널티를 부여합니다.
     * 
     * @param penalty 패널티 정보
     * @return 처리 성공 여부
     */
    public boolean addUserPenalty(AdminUserPenaltyDTO penalty) {
        try {
            return penaltyDAO.addUserPenalty(penalty);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
