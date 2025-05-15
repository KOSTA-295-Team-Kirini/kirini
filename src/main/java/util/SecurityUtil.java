package util;

public class SecurityUtil {
    public static String escapeXSS(String value) {
        if (value == null) return "";
        return value.replaceAll("<", "&lt;").replaceAll(">", "&gt;")
                   .replaceAll("\"", "&quot;").replaceAll("'", "&#39;");
    }
    
    // 다른 보안 관련 메서드들...
}