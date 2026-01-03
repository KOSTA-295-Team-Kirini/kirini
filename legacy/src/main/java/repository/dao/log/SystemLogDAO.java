package repository.dao.log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import dto.log.SystemLogDTO;
import util.db.DBConnectionUtil;

/**
 * 시스템 로그 데이터 액세스 객체
 */
public class SystemLogDAO {
    private Connection conn;
    private PreparedStatement pstmt;
    private ResultSet rs;

    /**
     * 시스템 로그 추가
     *
     * @param logDTO 로그 정보
     * @return 추가 성공 여부
     */
    public boolean addLog(SystemLogDTO logDTO) {
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "INSERT INTO system_log (log_level, log_message, log_exception, log_class, log_method, log_timestamp, user_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, logDTO.getLogLevel());
            pstmt.setString(2, logDTO.getLogMessage());
            pstmt.setString(3, logDTO.getLogException());
            pstmt.setString(4, logDTO.getLogClass());
            pstmt.setString(5, logDTO.getLogMethod());
            pstmt.setTimestamp(6, Timestamp.valueOf(logDTO.getLogTimestamp()));

            if (logDTO.getUserId() != null) {
                pstmt.setLong(7, logDTO.getUserId());
            } else {
                pstmt.setNull(7, java.sql.Types.INTEGER);
            }

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            // 로그 저장 실패 시 콘솔에만 출력
            System.err.println("[SystemLogDAO] 로그 저장 실패: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DBConnectionUtil.close(null, pstmt, conn);
        }
    }

    /**
     * 로그 레벨에 따른 로그 조회
     *
     * @param logLevel 조회할 로그 레벨
     * @param limit    최대 결과 수
     * @return 로그 목록
     */
    public List<SystemLogDTO> getLogsByLevel(String logLevel, int limit) {
        List<SystemLogDTO> logList = new ArrayList<>();

        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "SELECT * FROM system_log WHERE log_level = ? ORDER BY log_timestamp DESC LIMIT ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, logLevel);
            pstmt.setInt(2, limit);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                SystemLogDTO log = new SystemLogDTO();
                log.setLogId(rs.getLong("log_id"));
                log.setLogLevel(rs.getString("log_level"));
                log.setLogMessage(rs.getString("log_message"));
                log.setLogException(rs.getString("log_exception"));
                log.setLogClass(rs.getString("log_class"));
                log.setLogMethod(rs.getString("log_method"));
                log.setLogTimestamp(rs.getTimestamp("log_timestamp").toLocalDateTime());

                long userId = rs.getLong("user_id");
                if (!rs.wasNull()) {
                    log.setUserId(userId);
                }

                logList.add(log);
            }
        } catch (SQLException e) {
            System.err.println("[SystemLogDAO-getLogsByLevel] 로그 조회 실패: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }

        return logList;
    }

    /**
     * 특정 기간 동안의 로그 조회
     *
     * @param startDate 시작 날짜
     * @param endDate   종료 날짜
     * @param limit     최대 결과 수
     * @return 로그 목록
     */
    public List<SystemLogDTO> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        List<SystemLogDTO> logList = new ArrayList<>();

        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "SELECT * FROM system_log WHERE log_timestamp BETWEEN ? AND ? " +
                    "ORDER BY log_timestamp DESC LIMIT ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setTimestamp(1, Timestamp.valueOf(startDate));
            pstmt.setTimestamp(2, Timestamp.valueOf(endDate));
            pstmt.setInt(3, limit);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                SystemLogDTO log = new SystemLogDTO();
                log.setLogId(rs.getLong("log_id"));
                log.setLogLevel(rs.getString("log_level"));
                log.setLogMessage(rs.getString("log_message"));
                log.setLogException(rs.getString("log_exception"));
                log.setLogClass(rs.getString("log_class"));
                log.setLogMethod(rs.getString("log_method"));
                log.setLogTimestamp(rs.getTimestamp("log_timestamp").toLocalDateTime());

                long userId = rs.getLong("user_id");
                if (!rs.wasNull()) {
                    log.setUserId(userId);
                }

                logList.add(log);
            }
        } catch (SQLException e) {
            System.err.println("[SystemLogDAO-getLogsByDateRange] 로그 조회 실패: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }

        return logList;
    }

    /**
     * 특정 클래스의 로그 조회
     *
     * @param className 클래스 이름
     * @param limit     최대 결과 수
     * @return 로그 목록
     */
    public List<SystemLogDTO> getLogsByClass(String className, int limit) {
        List<SystemLogDTO> logList = new ArrayList<>();

        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "SELECT * FROM system_log WHERE log_class = ? ORDER BY log_timestamp DESC LIMIT ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, className);
            pstmt.setInt(2, limit);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                SystemLogDTO log = new SystemLogDTO();
                log.setLogId(rs.getLong("log_id"));
                log.setLogLevel(rs.getString("log_level"));
                log.setLogMessage(rs.getString("log_message"));
                log.setLogException(rs.getString("log_exception"));
                log.setLogClass(rs.getString("log_class"));
                log.setLogMethod(rs.getString("log_method"));
                log.setLogTimestamp(rs.getTimestamp("log_timestamp").toLocalDateTime());

                long userId = rs.getLong("user_id");
                if (!rs.wasNull()) {
                    log.setUserId(userId);
                }

                logList.add(log);
            }
        } catch (SQLException e) {
            System.err.println("[SystemLogDAO-getLogsByClass] 로그 조회 실패: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }

        return logList;
    }
}
