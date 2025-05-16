package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtil {
    public static String escapeXSS(String value) {
        if (value == null) return "";
        return value.replaceAll("<", "&lt;").replaceAll(">", "&gt;")
                   .replaceAll("\"", "&quot;").replaceAll("'", "&#39;");
    }
    
    /**
     * 비밀번호 암호화 (SHA-256)
     * @param password 평문 비밀번호
     * @return 암호화된 비밀번호
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; // 암호화 실패 시 원본 반환 (실제로는 더 나은 오류 처리 필요)
        }
    }
    
    // 다른 보안 관련 메서드들...
}