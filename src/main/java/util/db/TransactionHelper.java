package util.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * 데이터베이스 트랜잭션 처리에 필요한 유틸리티 클래스
 */
public class TransactionHelper {
    private static final Logger logger = Logger.getLogger(TransactionHelper.class.getName());
    
    /**
     * 트랜잭션 롤백을 안전하게 처리합니다.
     * 
     * @param conn 롤백할 데이터베이스 연결 객체
     */
    public static void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                logger.severe("트랜잭션 롤백 중 오류 발생: " + ex.getMessage());
            }
        }
    }
    
    /**
     * 트랜잭션의 자동 커밋 모드를 안전하게 설정합니다.
     * 
     * @param conn 설정할 데이터베이스 연결 객체
     * @param autoCommit 자동 커밋 여부
     */
    public static void setAutoCommit(Connection conn, boolean autoCommit) {
        if (conn != null) {
            try {
                conn.setAutoCommit(autoCommit);
            } catch (SQLException ex) {
                logger.severe("자동 커밋 모드 설정 중 오류 발생: " + ex.getMessage());
            }
        }
    }
    
    /**
     * 트랜잭션 작업을 안전하게 커밋합니다.
     * 
     * @param conn 커밋할 데이터베이스 연결 객체
     * @return 커밋 성공 여부
     */
    public static boolean commit(Connection conn) {
        if (conn != null) {
            try {
                conn.commit();
                return true;
            } catch (SQLException ex) {
                logger.severe("트랜잭션 커밋 중 오류 발생: " + ex.getMessage());
                rollback(conn);
                return false;
            }
        }
        return false;
    }
}
