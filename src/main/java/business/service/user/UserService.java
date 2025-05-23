package business.service.user;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dto.user.UserDTO;
import exception.UserStatusException;
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
     * @param email 이메일
     * @param password 비밀번호
     * @return 로그인 성공 시 UserDTO 객체, 실패 시 null
     */    public UserDTO login(String email, String password) {
        try {
        	
            
            // 1. 이메일로 사용자 정보 조회
            UserDTO user = userDAO.getUserByEmail(email);
            if (user == null) {
                return null; // 사용자가 존재하지 않음
            }
            
            // 2. 비밀번호 암호화 후 비교
            String hashedPassword = hashPassword(password);
            
            // 3. 암호화된 비밀번호끼리 비교
            if (hashedPassword.equals(user.getPassword())) {
                // 로그인 성공 처리 (마지막 로그인 시간 업데이트 등)
                
                // 프론트엔드와 일치하도록 userAuthority 값 설정
                String userAuthority = "USER";
                if (user.getUserLevel() == 3) {
                    userAuthority = "ADMIN";
                } else if (user.getUserLevel() == 2) {
                    userAuthority = "MANAGER";
                }
                user.setUserAuthority(userAuthority);
                
                return user;
            } else {
                return null; // 비밀번호 불일치
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("SQLEXCEPTION");
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
     * 비밀번호 검증 (사용자 ID와 입력 비밀번호 기반)
     * @param userId 사용자 ID
     * @param inputPassword 입력받은 비밀번호
     * @return 일치 여부
     */
    public boolean verifyPassword(long userId, String inputPassword) {
        try {
            // 사용자 정보 조회
            UserDTO user = getUserById(userId);
            if (user == null) {
                return false;
            }
            
            // 저장된 비밀번호 가져오기
            String storedPassword = user.getPassword();
            
            // 비밀번호 검증
            return verifyPassword(inputPassword, storedPassword);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 비밀번호 검증 (UserpageController 호환용 메서드)
     */
    public boolean validatePassword(long userId, String password) {
        return verifyPassword(userId, password);
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
     * 회원 탈퇴 (UserpageController 호환용 메서드)
     */
    public boolean deleteUser(long userId) {
        return deactivateUser(userId);
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
    
    /**
     * 사용자의 제한 상태 확인
     * @param userId 사용자 ID
     * @return 제한된 상태이면 true, 아니면 false
     */
    public boolean isUserRestricted(long userId) {
        try {
            return userDAO.isUserRestricted(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // 에러 발생 시 안전하게 false 반환
        }
    }

    /**
     * 로그인 시 사용자 상태 확인
     * @param email 이메일
     * @param password 비밀번호
     * @return UserDTO 객체 또는 null (상태 정보 포함)
     * @throws UserStatusException 사용자 계정이 제한된 경우
     */
    public UserDTO loginWithStatusCheck(String email, String password) throws UserStatusException {
        UserDTO user = login(email, password);
        
        if (user != null && isUserRestricted(user.getUserId())) {
            throw new UserStatusException("계정이 제한 상태입니다. 관리자에게 문의하세요.");
        }
        
        return user;
    }

    /**
     * 회원 탈퇴 요청 - 이유 없이 기본 처리
     */
    public boolean requestDeleteUser(long userId) {
        return requestDeleteUser(userId, null);
    }    /**
     * 회원 탈퇴 요청 - 이유를 포함한 처리
     */
    public boolean requestDeleteUser(long userId, String reason) {
        try {
            // 사용자 정보 확인
            UserDTO user = userDAO.getUserById(userId);
            if (user == null) {
                return false;
            }
            
            // 탈퇴 사유 기록 (선택적)
            if (reason != null && !reason.trim().isEmpty()) {
                // 탈퇴 사유 로그 기록 로직 추가
                // 로깅 기능은 나중에 구현 예정
                System.out.println("회원 탈퇴 요청 - 사용자 ID: " + userId + ", 사유: " + reason);
            } else {
                System.out.println("회원 탈퇴 요청 - 사용자 ID: " + userId);
            }
            
            // 회원 비활성화 처리
            return userDAO.deactivateUser(userId);
        } catch (SQLException e) {
            System.err.println("회원 탈퇴 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 사용자의 현재 포인트 조회
     * @param userId 사용자 ID
     * @return 사용자의 현재 포인트
     */
    public int getUserPoints(long userId) {
        try {
            // TODO: 실제 포인트 조회 로직 구현
            // 임시 구현: 사용자 ID를 기반으로 더미 포인트 생성
            return 1000 + (int)(userId % 1000);
        } catch (Exception e) {
            System.err.println("포인트 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 사용자의 포인트 변동 내역 조회
     * @param userId 사용자 ID
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @return 포인트 변동 내역 목록
     */
    public List<Object> getUserPointHistory(long userId, int page, int pageSize) {
        try {
            // TODO: 실제 포인트 내역 조회 로직 구현
            // 임시 구현: 더미 데이터 반환
            List<Object> dummyHistory = new ArrayList<>();
            // 더미 데이터는 UserpageController에서 처리됨
            return dummyHistory;
        } catch (Exception e) {
            System.err.println("포인트 내역 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * 사용자의 전체 포인트 변동 내역 개수 조회
     * @param userId 사용자 ID
     * @return 전체 내역 개수
     */
    public int getTotalUserPointHistory(long userId) {
        try {
            // TODO: 실제 포인트 내역 개수 조회 로직 구현
            // 임시 구현: 더미 데이터 개수 반환
            return 5;
        } catch (Exception e) {
            System.err.println("포인트 내역 개수 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 사용자 커스터마이징 저장
     * @param userId 사용자 ID
     * @param selectedIconId 선택한 아이콘 ID
     * @param selectedThemeId 선택한 테마 ID
     * @return 저장 성공 여부
     */
    public boolean saveUserCustomization(long userId, String selectedIconId, String selectedThemeId) {
        try {
            // TODO: 실제 사용자 커스터마이징 저장 로직 구현
            // 임시 구현: 항상 성공 반환
            System.out.println("사용자 커스터마이징 저장 - 사용자 ID: " + userId 
                + ", 아이콘: " + selectedIconId 
                + ", 테마: " + selectedThemeId);
            return true;
        } catch (Exception e) {
            System.err.println("커스터마이징 저장 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}