package util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import util.config.AppConfig;

public class FileUtil {
    private static final Logger logger = Logger.getLogger(FileUtil.class.getName());
    
    /**
     * 파일 업로드 디렉토리 생성 및 경로 반환
     * @return 날짜별 폴더 구조가 포함된 업로드 디렉토리 경로
     */
    public static String getUploadDirectoryPath() throws IOException {
        // 기본 업로드 경로
        String basePath = AppConfig.getUploadPath();
        File baseDir = new File(basePath);
        
        // 기본 디렉토리 생성
        if (!baseDir.exists()) {
            if (!baseDir.mkdirs()) {
                logger.severe("기본 업로드 디렉토리를 생성할 수 없습니다: " + basePath);
                throw new IOException("업로드 디렉토리를 생성할 수 없습니다: " + basePath);
            }
        }
        
        // 날짜별 폴더 구조 생성 (yyyy/MM/dd)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String datePath = sdf.format(new Date());
        
        File dateDir = new File(baseDir, datePath);
        if (!dateDir.exists()) {
            if (!dateDir.mkdirs()) {
                logger.severe("날짜별 디렉토리를 생성할 수 없습니다: " + dateDir.getPath());
                throw new IOException("날짜별 디렉토리를 생성할 수 없습니다: " + dateDir.getPath());
            }
        }
        
        return dateDir.getAbsolutePath();
    }
    
    /**
     * 파일명 정규화 (안전한 파일명으로 변환)
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null) return null;
        
        // 경로 문자 제거 (파일명만 추출)
        String name = new File(filename).getName();
        
        // 위험한 문자 제거
        return name.replaceAll("[^a-zA-Z0-9가-힣._-]", "_");
    }
    
    /**
     * 고유한 파일명 생성
     */
    public static String generateUniqueFilename(String originalFilename) {
        String sanitized = sanitizeFilename(originalFilename);
        return System.currentTimeMillis() + "_" + sanitized;
    }
}