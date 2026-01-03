package util.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 데이터베이스 연결을 관리하는 유틸리티 클래스
 */
public class DBConnectionUtil {

    // 데이터베이스 연결 정보
    private static final String DRIVER = System.getenv("DB_DRIVER");
    private static final String URL = System.getenv("DB_URL");
    private static final String USER = System.getenv("DB_USER");
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    // 정적 초기화 블록으로 드라이버 로드
    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("DB 드라이버 로드 실패", e);
        }
    }

    /**
     * 데이터베이스 연결 객체 반환
     *
     * @return Connection 객체
     * @throws SQLException 연결 실패 시 예외 발생
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * 자원 해제 (조회 사용 후)
     *
     * @param rs    ResultSet 객체
     * @param pstmt PreparedStatement 객체
     * @param conn  Connection 객체
     */
    public static void close(ResultSet rs, PreparedStatement pstmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}