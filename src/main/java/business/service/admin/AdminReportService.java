package business.service.admin;

import java.sql.SQLException;
import java.util.List;

import dto.board.ReportDTO;
import repository.dao.board.ReportDAO;

public class AdminReportService {
    private ReportDAO reportDAO;
    
    public AdminReportService() {
        reportDAO = new ReportDAO();
    }
    
    public List<ReportDTO> getAllReports() {
        try {
            return reportDAO.getAllReports();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<ReportDTO> getReportsByCondition(String status, String targetType) {
        try {
            return reportDAO.getReportsByCondition(status, targetType);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}