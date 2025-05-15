package util.logging;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * 로깅 시스템 구성을 담당하는 클래스
 */
public class LoggerConfig {
    
    private static final String LOG_DIRECTORY = "logs";
    private static final String ERROR_LOG_FILE = "kirini_error.log";
    private static final String INFO_LOG_FILE = "kirini_info.log";
    
    /**
     * 주어진 클래스에 대한 로거 인스턴스 반환
     */
    public static Logger getLogger(Class<?> clazz) {
        Logger logger = Logger.getLogger(clazz.getName());
        
        // 이미 핸들러가 설정되어 있으면 재설정하지 않음
        if (logger.getHandlers().length > 0) {
            return logger;
        }
        
        try {
            // 로그 디렉토리 생성
            File logDir = new File(LOG_DIRECTORY);
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            
            // 에러 로그 핸들러 설정
            FileHandler errorHandler = new FileHandler(LOG_DIRECTORY + File.separator + ERROR_LOG_FILE, true);
            errorHandler.setFormatter(new SimpleFormatter());
            errorHandler.setLevel(Level.SEVERE);
            
            // 정보 로그 핸들러 설정
            FileHandler infoHandler = new FileHandler(LOG_DIRECTORY + File.separator + INFO_LOG_FILE, true);
            infoHandler.setFormatter(new SimpleFormatter());
            infoHandler.setLevel(Level.INFO);
            
            logger.setUseParentHandlers(false);
            logger.addHandler(errorHandler);
            logger.addHandler(infoHandler);
            logger.setLevel(Level.INFO);
            
        } catch (IOException e) {
            // 로그 설정 실패 시 콘솔에 출력
            System.err.println("로깅 설정 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
        
        return logger;
    }
}