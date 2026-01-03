package util.logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import dto.log.SystemLogDTO;
import repository.dao.log.SystemLogDAO;

/**
 * 로깅 시스템 구성을 담당하는 클래스
 * - 모든 로그: 콘솔에 출력
 * - 비즈니스 로직 관련 로그: 데이터베이스 테이블에 저장
 */
public class LoggerConfig {

    private static final SystemLogDAO logDAO = new SystemLogDAO();

    /**
     * 주어진 클래스에 대한 로거 인스턴스 반환
     */
    public static Logger getLogger(Class<?> clazz) {
        Logger logger = Logger.getLogger(clazz.getName());

        // 이미 핸들러가 설정되어 있으면 재설정하지 않음
        if (logger.getHandlers().length > 0) {
            return logger;
        }

        // 콘솔 핸들러 설정 (모든 로그 출력)
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter());
        consoleHandler.setLevel(Level.ALL); // 모든 로그 레벨을 콘솔에 출력

        // 데이터베이스 핸들러 설정 (비즈니스 로직 로그용)
        Handler dbHandler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                // INFO 레벨 이상의 로그만 데이터베이스에 저장 (에러 제외)
                if (!isLoggable(record) ||
                        record.getLevel().intValue() < Level.INFO.intValue() ||
                        record.getLevel().equals(Level.SEVERE)) {
                    return;
                }

                try {
                    // 로그 정보를 DTO로 변환
                    SystemLogDTO logDTO = new SystemLogDTO();
                    logDTO.setLogLevel(record.getLevel().getName());
                    logDTO.setLogMessage(record.getMessage());
                    logDTO.setLogClass(record.getSourceClassName());
                    logDTO.setLogMethod(record.getSourceMethodName());

                    // 비동기적으로 DB에 저장 (파일 락 방지)
                    new Thread(() -> {
                        try {
                            logDAO.addLog(logDTO);
                        } catch (Exception e) {
                            // 로그 저장 실패 시 콘솔에만 출력
                            System.err.println("[로그 저장 실패] " + e.getMessage());
                        }
                    }).start();
                } catch (Exception e) {
                    // 로그 처리 중 오류 발생 시 콘솔에 출력
                    System.err.println("[로그 처리 오류] " + e.getMessage());
                }
            }

            @Override
            public void flush() {
                // 필요 없음
            }

            @Override
            public void close() throws SecurityException {
                // 필요 없음
            }
        };
        dbHandler.setLevel(Level.INFO); // INFO 이상 레벨만 DB에 저장

        logger.setUseParentHandlers(false); // 부모 핸들러 사용 안 함
        logger.addHandler(consoleHandler);
        logger.addHandler(dbHandler);
        logger.setLevel(Level.ALL); // 모든 레벨 로깅

        return logger;
    }

    /**
     * 에러 로그를 콘솔에 포맷팅해서 출력합니다.
     *
     * @param clazz   로깅을 수행하는 클래스
     * @param method  로깅을 수행하는 메소드 이름
     * @param message 로그 메시지
     * @param e       예외 객체
     */
    public static void logError(Class<?> clazz, String method, String message, Exception e) {
        System.err.println("=== ERROR ===");
        System.err.println("Time: " + java.time.LocalDateTime.now());
        System.err.println("Class: " + clazz.getName());
        System.err.println("Method: " + method);
        System.err.println("Message: " + message);
        if (e != null) {
            System.err.println("Exception: " + e.getClass().getName());
            System.err.println("Cause: " + e.getMessage());
            System.err.println("Stack Trace:");
            for (StackTraceElement element : e.getStackTrace()) {
                System.err.println("    at " + element.toString());
            }
        }
        System.err.println("=============");
    }

    /**
     * 비즈니스 로직 로그를 데이터베이스에 저장합니다.
     *
     * @param clazz   로깅을 수행하는 클래스
     * @param method  로깅을 수행하는 메소드 이름
     * @param action  수행된 작업 (예: '게시글 삭제', '회원 정보 수정' 등)
     * @param details 상세 정보 (예: '게시글 ID: 123, 작성자: admin')
     * @param userId  작업을 수행한 사용자 ID (로그인된 경우)
     */
    public static void logBusinessAction(Class<?> clazz, String method, String action, String details, Long userId) {
        SystemLogDTO logDTO = new SystemLogDTO();
        logDTO.setLogLevel("INFO");
        logDTO.setLogMessage(action + ": " + details);
        logDTO.setLogClass(clazz.getName());
        logDTO.setLogMethod(method);
        logDTO.setLogTimestamp(java.time.LocalDateTime.now());
        logDTO.setUserId(userId);

        // 비동기적으로 DB에 저장
        new Thread(() -> {
            try {
                logDAO.addLog(logDTO);
            } catch (Exception e) {
                // 로그 저장 실패 시 콘솔에만 출력
                System.err.println("[로그 저장 실패] " + e.getMessage());
            }
        }).start();
    }
}