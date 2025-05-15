package util.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class AppConfig {
    private static final Logger logger = Logger.getLogger(AppConfig.class.getName());
    private static final Properties props = new Properties();
    private static boolean loaded = false;
    
    static {
        loadConfig();
    }
    
    private static void loadConfig() {
        if (loaded) return;
        
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                logger.warning("config.properties 파일을 찾을 수 없습니다. 기본값을 사용합니다.");
                return;
            }
            
            props.load(input);
            loaded = true;
            logger.info("설정 파일을 성공적으로 로드했습니다.");
        } catch (IOException e) {
            logger.severe("설정 파일 로드 중 오류 발생: " + e.getMessage());
        }
    }
    
    public static String getUploadPath() {
        // 1. 시스템 속성 확인 (우선순위 높음)
        String path = System.getProperty("kirini.upload.path");
        
        // 2. 환경 변수 확인
        if (path == null || path.trim().isEmpty()) {
            path = System.getenv("KIRINI_UPLOAD_PATH");
        }
        
        // 3. 설정 파일에서 확인
        if (path == null || path.trim().isEmpty()) {
            path = props.getProperty("upload.path");
        }
        
        // 4. 기본값 사용
        if (path == null || path.trim().isEmpty()) {
            path = "C:/kirini/uploads";
        }
        
        return path;
    }
}