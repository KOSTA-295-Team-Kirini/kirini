package util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

// Part 인터페이스는 필요할 때 조건에 따라 적절한 패키지에서 임포트하고,
// 현재는 String 기반 메서드로 대체 구현

import util.config.AppConfig;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import jakarta.servlet.http.Part;

import util.config.AppConfig;

public class FileUtil {
    private static final Logger logger = Logger.getLogger(FileUtil.class.getName());

    /**
     * 허용된 파일 확장자 목록
     */
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            // 문서
            "txt", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            // 이미지
            "jpg", "jpeg", "png", "gif", "bmp",
            // 압축
            "zip", "rar", "7z"
    ));

    /**
     * 허용된 MIME 타입 목록
     */
    private static final Set<String> ALLOWED_MIME_TYPES = new HashSet<>(Arrays.asList(
            // 문서
            "text/plain", "application/pdf",
            "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            // 이미지
            "image/jpeg", "image/png", "image/gif", "image/bmp",
            // 압축
            "application/zip", "application/x-rar-compressed", "application/x-7z-compressed"
    ));

    /**
     * 파일 업로드 디렉토리 생성 및 경로 반환
     *
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

    /**
     * 파일 확장자가 허용 목록에 있는지 확인
     *
     * @param fileName 확인할 파일명
     * @return 허용된 확장자면 true, 아니면 false
     */
    public static boolean isAllowedFileType(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }

        // 파일 확장자 추출
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return false;  // 확장자가 없는 경우
        }

        String extension = fileName.substring(lastDotIndex + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }

    /**
     * 파일의 MIME 타입이 허용 목록에 있는지 확인
     *
     * @param contentType 확인할 파일의 MIME 타입
     * @return 허용된 MIME 타입이면 true, 아니면 false
     */
    public static boolean validateMimeType(String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return false;
        }

        // MIME 타입에서 메인 타입만 추출 (예: "application/pdf; charset=utf-8" -> "application/pdf")
        if (contentType.contains(";")) {
            contentType = contentType.split(";")[0].trim();
        }

        return ALLOWED_MIME_TYPES.contains(contentType);
    }

    /**
     * 파일의 MIME 타입이 허용 목록에 있는지 확인 (Part 객체용)
     *
     * @param filePart 확인할 파일 Part
     * @return 허용된 MIME 타입이면 true, 아니면 false
     */
    public static boolean validateMimeType(Part filePart) {
        if (filePart == null) {
            return false;
        }

        String contentType = filePart.getContentType();
        return validateMimeType(contentType);
    }

    /**
     * Part 객체에서 제출된 파일의 원본 이름을 추출
     *
     * @param part 파일이 포함된 Part 객체
     * @return 원본 파일명
     */
    public static String getSubmittedFileName(Part part) {
        if (part == null) return null;

        // Content-Disposition 헤더에서 filename 추출
        String contentDisp = part.getHeader("content-disposition");
        if (contentDisp == null) return null;

        // filename="example.jpg" 형태에서 파일명 추출
        String[] items = contentDisp.split(";");
        for (String item : items) {
            item = item.trim();
            if (item.startsWith("filename=")) {
                // "filename=" 부분 제거하고 따옴표 처리
                String fileName = item.substring(9).trim();

                // 따옴표 제거
                if (fileName.startsWith("\"") && fileName.endsWith("\"")) {
                    fileName = fileName.substring(1, fileName.length() - 1);
                }

                return fileName;
            }
        }

        return null;
    }
}