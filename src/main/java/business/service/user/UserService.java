package business.service.user;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;

import dto.user.UserDTO;
import repository.dao.user.UserDAO;

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
public class UserService {
    private UserDAO userDAO;
    
    public UserService() {
        userDAO = new UserDAO();
    }
    
    /**
     * 사용자 로그인
     * @param username 사용자 아이디
     * @param password 비밀번호
     * @return 로그인 성공 시 UserDTO 객체, 실패 시 null
     */
    public UserDTO login(String username, String password) {
        try {
            // 비밀번호 암호화
            String hashedPassword = hashPassword(password);
            return userDAO.login(username, hashedPassword);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 아이디 중복 확인
     * @param username 사용자 아이디
     * @return 중복이면 true, 아니면 false
     */
    public boolean checkDuplicateId(String username) {
        try {
            return userDAO.isUsernameExists(username);
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // 에러 발생 시 중복으로 처리 (안전하게)
        }
    }
    
    /**
     * 이메일 중복 확인
     * @param email 이메일 주소
     * @return 중복이면 true, 아니면 false
     */
    public boolean checkDuplicateEmail(String email) {
        try {
            return userDAO.isEmailExists(email);
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // 에러 발생 시 중복으로 처리 (안전하게)
        }
    }
    
    /**
     * 닉네임 중복 확인
     * @param nickname 닉네임
     * @return 중복이면 true, 아니면 false
     */
    public boolean checkDuplicateNickname(String nickname) {
        try {
            return userDAO.isNicknameExists(nickname);
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // 에러 발생 시 중복으로 처리 (안전하게)
        }
    }
    
    /**
     * 비밀번호 유효성 검사
     * @param password 비밀번호
     * @return 유효하면 true, 아니면 false
     */
    public boolean checkPasswordValidation(String password) {
        // 비밀번호는 8자 이상, 알파벳 대소문자, 숫자, 특수문자 포함
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
        return password.matches(passwordRegex);
    }
    
    /**
     * 사용자 등록
     * @param user 등록할 사용자 정보
     * @return 등록 성공 시 true, 실패 시 false
     */
    public boolean registerUser(UserDTO user) {
        try {
            // 비밀번호 암호화 처리
            String hashedPassword = hashPassword(user.getPassword());
            user.setPassword(hashedPassword);
            
            // DAO를 통해 사용자 등록
            return userDAO.registerUser(user);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 사용자 정보 조회
     * @param userId 사용자 ID
     * @return 사용자 정보 객체
     */
    public UserDTO getUserById(long userId) {
        try {
            return userDAO.getUserById(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 이메일로 사용자 정보 조회
     * @param email 이메일
     * @return 사용자 정보 객체, 없으면 null
     */
    public UserDTO getUserByEmail(String email) {
        try {
            return userDAO.getUserByEmail(email);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 사용자 정보 업데이트
     * @param user 업데이트할 사용자 정보
     * @return 업데이트 성공 시 true, 실패 시 false
     */
    public boolean updateUser(UserDTO user) {
        try {
            return userDAO.updateUser(user);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 비밀번호 변경
     * @param userId 사용자 ID
     * @param newPassword 새 비밀번호
     * @return 변경 성공 시 true, 실패 시 false
     */
    public boolean updatePassword(long userId, String newPassword) {
        try {
            // 비밀번호 암호화
            String hashedPassword = hashPassword(newPassword);
            return userDAO.updatePassword(userId, hashedPassword);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 비밀번호 검증
     * @param inputPassword 입력받은 비밀번호
     * @param storedPassword 저장된 암호화 비밀번호
     * @return 일치 여부
     */
    public boolean verifyPassword(String inputPassword, String storedPassword) {
        // 입력 비밀번호 암호화
        String hashedInput = hashPassword(inputPassword);
        // 저장된 암호화 비밀번호와 비교
        return hashedInput.equals(storedPassword);
    }
    
    /**
     * 회원 탈퇴
     * @param userId 사용자 ID
     * @return 성공 시 true, 실패 시 false
     */
    public boolean deactivateUser(long userId) {
        try {
            return userDAO.deactivateUser(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 모든 사용자 목록 조회 (관리자용)
     * @return 사용자 목록
     */
    public List<UserDTO> getAllUsers() {
        try {
            return userDAO.getAllUsers();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 비밀번호 암호화 (SHA-256)
     * @param password 평문 비밀번호
     * @return 암호화된 비밀번호
     */
    private String hashPassword(String password) {
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
}