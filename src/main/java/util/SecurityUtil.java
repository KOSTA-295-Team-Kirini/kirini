package util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class SecurityUtil {
    public static String escapeXSS(String value) {
        if (value == null) return "";
        return value.replaceAll("<", "&lt;").replaceAll(">", "&gt;")
                   .replaceAll("\"", "&quot;").replaceAll("'", "&#39;");
    }

    // 비밀번호 해시 형식: pbkdf2_sha256$반복횟수$솔트(Base64)$해시(Base64)
    private static final String PBKDF2_PREFIX = "pbkdf2_sha256$";
    private static final int PBKDF2_ITERATIONS = 310_000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 비밀번호 해시 (PBKDF2-HMAC-SHA256, 사용자별 솔트)
     * @param password 평문 비밀번호
     * @return 저장용 해시 문자열
     */
    public static String hashPassword(String password) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(password, salt, PBKDF2_ITERATIONS);
        Base64.Encoder encoder = Base64.getEncoder();
        return PBKDF2_PREFIX + PBKDF2_ITERATIONS + "$" + encoder.encodeToString(salt) + "$" + encoder.encodeToString(hash);
    }

    /**
     * 비밀번호 검증. 기존 무솔트 SHA-256 해시도 검증한다(로그인 시 needsRehash로 재저장).
     * @param password 입력 평문 비밀번호
     * @param storedHash 저장된 해시
     * @return 일치 여부
     */
    public static boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null) {
            return false;
        }
        if (!storedHash.startsWith(PBKDF2_PREFIX)) {
            return MessageDigest.isEqual(
                    legacySha256Hex(password).getBytes(StandardCharsets.UTF_8),
                    storedHash.getBytes(StandardCharsets.UTF_8));
        }
        String[] parts = storedHash.substring(PBKDF2_PREFIX.length()).split("\\$");
        if (parts.length != 3) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expected = Base64.getDecoder().decode(parts[2]);
            return MessageDigest.isEqual(expected, pbkdf2(password, salt, iterations));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 기존 SHA-256 형식이면 재해시가 필요하다.
     */
    public static boolean needsRehash(String storedHash) {
        return storedHash == null || !storedHash.startsWith(PBKDF2_PREFIX + PBKDF2_ITERATIONS + "$");
    }

    private static byte[] pbkdf2(String password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            // 해시 실패 시 평문으로 대체하지 않는다
            throw new IllegalStateException("비밀번호 해시 계산 실패", e);
        } finally {
            spec.clearPassword();
        }
    }

    // 기존 저장 형식(무솔트 SHA-256 hex) — 이전 사용자 검증 전용
    private static String legacySha256Hex(String password) {
        try {
            byte[] hashBytes = MessageDigest.getInstance("SHA-256").digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("비밀번호 해시 계산 실패", e);
        }
    }

    // 다른 보안 관련 메서드들...
}
