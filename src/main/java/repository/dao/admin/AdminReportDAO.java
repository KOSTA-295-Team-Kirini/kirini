package repository.dao.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import dto.admin.AdminReportDTO;
import util.db.DBConnectionUtil;
import util.logging.LoggerConfig;

/**
 * 관리자용 신고 데이터 액세스 객체
 */
public class AdminReportDAO {
    private Connection conn = null;
    private PreparedStatement pstmt = null;
    private ResultSet rs = null;
    private static final Logger logger = LoggerConfig.getLogger(AdminReportDAO.class);
    
    /**
     * 전체 신고 내역 조회
     * @return 신고 내역 목록
     * @throws SQLException
     */
    public List<AdminReportDTO> getAllReports() throws SQLException {
        List<AdminReportDTO> reportList = new ArrayList<>();
        
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "SELECT r.*, " +
                         "reporter.user_name as reporter_username, " +
                         "target.user_name as target_username " +
                         "FROM report r " +
                         "JOIN user reporter ON r.report_user_uid = reporter.user_uid " +
                         "JOIN user target ON r.target_user_uid = target.user_uid " +
                         "ORDER BY r.report_createtime DESC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                AdminReportDTO report = new AdminReportDTO();
                report.setReportUid(rs.getLong("report_uid"));
                report.setReportUserUid(rs.getLong("report_user_uid"));
                report.setReportTargetType(rs.getString("report_target_type"));
                report.setReportReason(rs.getString("report_reason"));
                report.setReportStatus(rs.getString("report_status"));
                report.setReportCreatetime(rs.getDate("report_createtime"));
                report.setTargetUserUid(rs.getLong("target_user_uid"));
                report.setReporterUsername(rs.getString("reporter_username"));
                report.setTargetUsername(rs.getString("target_username"));
                reportList.add(report);
            }
            
            logger.info("전체 신고 내역 " + reportList.size() + "건 조회됨");
            return reportList;
        } catch (SQLException e) {
            logger.severe("전체 신고 내역 조회 중 오류 발생: " + e.getMessage());
            throw e;
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
    }
    
    /**
     * 조건에 맞는 신고 내역 조회
     * @param status 신고 상태
     * @param targetType 신고 대상 유형
     * @return 조건에 맞는 신고 내역 목록
     * @throws SQLException
     */
    public List<AdminReportDTO> getReportsByCondition(String status, String targetType) throws SQLException {
        List<AdminReportDTO> reportList = new ArrayList<>();
        
        try {
            conn = DBConnectionUtil.getConnection();
            StringBuilder sql = new StringBuilder(
                    "SELECT r.*, " +
                    "reporter.user_name as reporter_username, " +
                    "target.user_name as target_username " +
                    "FROM report r " +
                    "JOIN user reporter ON r.report_user_uid = reporter.user_uid " +
                    "JOIN user target ON r.target_user_uid = target.user_uid " +
                    "WHERE 1=1 ");
            
            if (status != null && !status.trim().isEmpty()) {
                sql.append("AND r.report_status = ? ");
            }
            
            if (targetType != null && !targetType.trim().isEmpty()) {
                sql.append("AND r.report_target_type = ? ");
            }
            
            sql.append("ORDER BY r.report_createtime DESC");
            
            pstmt = conn.prepareStatement(sql.toString());
            
            int paramIndex = 1;
            if (status != null && !status.trim().isEmpty()) {
                pstmt.setString(paramIndex++, status);
            }
            
            if (targetType != null && !targetType.trim().isEmpty()) {
                pstmt.setString(paramIndex++, targetType);
            }
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                AdminReportDTO report = new AdminReportDTO();
                report.setReportUid(rs.getLong("report_uid"));
                report.setReportUserUid(rs.getLong("report_user_uid"));
                report.setReportTargetType(rs.getString("report_target_type"));
                report.setReportReason(rs.getString("report_reason"));
                report.setReportStatus(rs.getString("report_status"));
                report.setReportCreatetime(rs.getDate("report_createtime"));
                report.setTargetUserUid(rs.getLong("target_user_uid"));
                report.setReporterUsername(rs.getString("reporter_username"));
                report.setTargetUsername(rs.getString("target_username"));
                reportList.add(report);
            }
            
            logger.info("조건별 신고 내역 " + reportList.size() + "건 조회됨 (상태: " + status + ", 대상유형: " + targetType + ")");
            return reportList;
        } catch (SQLException e) {
            logger.severe("조건별 신고 내역 조회 중 오류 발생: " + e.getMessage());
            throw e;
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
    }
    
    /**
     * 신고 상태 변경
     * @param reportUid 신고 ID
     * @param status 변경할 상태
     * @return 성공 여부
     * @throws SQLException
     */
    public boolean updateReportStatus(long reportUid, String status) throws SQLException {
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "UPDATE report SET report_status = ? WHERE report_uid = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setLong(2, reportUid);
            
            int result = pstmt.executeUpdate();
            boolean success = result > 0;
            
            if (success) {
                logger.info("신고 상태 변경 성공: ID=" + reportUid + ", 상태=" + status);
            } else {
                logger.warning("신고 상태 변경 실패: ID=" + reportUid + ", 영향받은 행 없음");
            }
            
            return success;
        } catch (SQLException e) {
            logger.severe("신고 상태 변경 중 오류 발생: " + e.getMessage());
            throw e;
        } finally {
            DBConnectionUtil.close(null, pstmt, conn);
        }
    }
}
