package repository.dao.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dto.admin.AdminReportDTO;
import util.db.DBConnectionUtil;

/**
 * 관리자용 신고 관련 데이터 액세스 객체
 */
public class AdminReportDAO {
    private Connection conn;
    private PreparedStatement pstmt;
    private ResultSet rs;
    
    /**
     * 모든 신고 내역을 조회합니다.
     */
    public List<AdminReportDTO> getAllReports() throws SQLException {
        List<AdminReportDTO> reportList = new ArrayList<>();
        
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "SELECT r.*, " +
                         "u1.user_name as reporter_username, " +
                         "u2.user_name as target_username " +
                         "FROM report r " +
                         "JOIN user u1 ON r.report_user_uid = u1.user_uid " +
                         "LEFT JOIN user u2 ON r.target_user_uid = u2.user_uid " +
                         "ORDER BY r.report_createtime DESC";
            
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                AdminReportDTO report = new AdminReportDTO();
                report.setReportUid(rs.getLong("report_uid"));
                report.setReportTargetType(rs.getString("report_target_type"));
                report.setReportReason(rs.getString("report_reason"));
                report.setReportStatus(rs.getString("report_status"));
                report.setReportCreatetime(rs.getDate("report_createtime"));
                report.setReportUserUid(rs.getLong("report_user_uid"));
                report.setTargetUserUid(rs.getLong("target_user_uid"));
                report.setReporterUsername(rs.getString("reporter_username"));
                report.setTargetUsername(rs.getString("target_username"));
                
                reportList.add(report);
            }
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
        
        return reportList;
    }
    
    /**
     * 조건에 맞는 신고 내역을 조회합니다.
     */
    public List<AdminReportDTO> getReportsByCondition(String status, String targetType) throws SQLException {
        List<AdminReportDTO> reportList = new ArrayList<>();
        
        try {
            conn = DBConnectionUtil.getConnection();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT r.*, ");
            sql.append("u1.user_name as reporter_username, ");
            sql.append("u2.user_name as target_username ");
            sql.append("FROM report r ");
            sql.append("JOIN user u1 ON r.report_user_uid = u1.user_uid ");
            sql.append("LEFT JOIN user u2 ON r.target_user_uid = u2.user_uid ");
            sql.append("WHERE 1=1 ");
            
            List<Object> params = new ArrayList<>();
            
            if (status != null && !status.isEmpty()) {
                sql.append("AND r.report_status = ? ");
                params.add(status);
            }
            
            if (targetType != null && !targetType.isEmpty()) {
                sql.append("AND r.report_target_type = ? ");
                params.add(targetType);
            }
            
            sql.append("ORDER BY r.report_createtime DESC");
            
            pstmt = conn.prepareStatement(sql.toString());
            
            // 파라미터 설정
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                AdminReportDTO report = new AdminReportDTO();
                report.setReportUid(rs.getLong("report_uid"));
                report.setReportTargetType(rs.getString("report_target_type"));
                report.setReportReason(rs.getString("report_reason"));
                report.setReportStatus(rs.getString("report_status"));
                report.setReportCreatetime(rs.getDate("report_createtime"));
                report.setReportUserUid(rs.getLong("report_user_uid"));
                report.setTargetUserUid(rs.getLong("target_user_uid"));
                report.setReporterUsername(rs.getString("reporter_username"));
                report.setTargetUsername(rs.getString("target_username"));
                
                reportList.add(report);
            }
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
        
        return reportList;
    }
    
    /**
     * 특정 신고의 상세 정보를 조회합니다.
     */
    public AdminReportDTO getReportById(long reportUid) throws SQLException {
        AdminReportDTO report = null;
        
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "SELECT r.*, " +
                         "u1.user_name as reporter_username, " +
                         "u2.user_name as target_username " +
                         "FROM report r " +
                         "JOIN user u1 ON r.report_user_uid = u1.user_uid " +
                         "LEFT JOIN user u2 ON r.target_user_uid = u2.user_uid " +
                         "WHERE r.report_uid = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, reportUid);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                report = new AdminReportDTO();
                report.setReportUid(rs.getLong("report_uid"));
                report.setReportTargetType(rs.getString("report_target_type"));
                report.setReportReason(rs.getString("report_reason"));
                report.setReportStatus(rs.getString("report_status"));
                report.setReportCreatetime(rs.getDate("report_createtime"));
                report.setReportUserUid(rs.getLong("report_user_uid"));
                report.setTargetUserUid(rs.getLong("target_user_uid"));
                report.setReporterUsername(rs.getString("reporter_username"));
                report.setTargetUsername(rs.getString("target_username"));
            }
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
        
        return report;
    }
    
    /**
     * 신고 상태를 업데이트합니다.
     */
    public boolean updateReportStatus(long reportUid, String status) throws SQLException {
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "UPDATE report SET report_status = ? WHERE report_uid = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setLong(2, reportUid);
            
            int result = pstmt.executeUpdate();
            return result > 0;
        } finally {
            DBConnectionUtil.close(null, pstmt, conn);
        }
    }
    
    /**
     * 특정 상태의 신고 개수를 조회합니다.
     * 
     * @param status 조회할 상태 (null인 경우 전체 개수)
     * @return 신고 개수
     */
    public int getReportCount(String status) throws SQLException {
        try {
            conn = DBConnectionUtil.getConnection();
            String sql;
            
            if (status == null) {
                sql = "SELECT COUNT(*) FROM report";
                pstmt = conn.prepareStatement(sql);
            } else {
                sql = "SELECT COUNT(*) FROM report WHERE report_status = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, status);
            }
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
            return 0;
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
    }
}