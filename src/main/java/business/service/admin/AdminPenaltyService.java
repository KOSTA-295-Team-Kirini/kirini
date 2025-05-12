package business.service.admin;

import java.sql.SQLException;
import java.util.List;

import dto.user.UserPenaltyDTO;
import repository.dao.user.UserPenaltyDAO;

public class AdminPenaltyService {
    private UserPenaltyDAO penaltyDAO;
    
    public AdminPenaltyService() {
        penaltyDAO = new UserPenaltyDAO();
    }
    
    public List<UserPenaltyDTO> getAllUserPenalties() {
        try {
            return penaltyDAO.getAllUserPenalties();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<UserPenaltyDTO> getUserPenaltiesByUserId(long userId) {
        try {
            return penaltyDAO.getUserPenaltiesByUserId(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean updateUserPenaltyStatus(long penaltyId, String status) {
        try {
            return penaltyDAO.updateUserPenaltyStatus(penaltyId, status);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}