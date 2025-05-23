package repository.dao.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import dto.user.UserDTO;
import util.db.DBConnectionUtil;
import util.logging.LoggerConfig;

public class UserDAO {
    private Connection conn = null;
    private PreparedStatement pstmt = null;
    private ResultSet rs = null;
    
    private static final Logger logger = LoggerConfig.getLogger(UserDAO.class);
    
    // 사용자 등록 - 3가지 필드만 사용
    public boolean registerUser(UserDTO user) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {            conn = DBConnectionUtil.getConnection();
            logger.info("DB 연결 성공: " + conn);
            
            // 디버깅용 로그
            logger.info("회원가입 정보: " + user.getEmail() + ", " + user.getNickname());
            
            // 데이터베이스 스키마에 맞는 SQL 쿼리 - user_id 제외
            String sql = "INSERT INTO user (user_password, user_name, user_email, " +
                         "user_introduce, user_authority, user_point) " +
                         "VALUES (?, ?, ?, ?, 'normal', 0)";
            
            pstmt = conn.prepareStatement(sql);
            
            // DTO → DB 필드 매핑 (user_id 제외)
            pstmt.setString(1, user.getPassword());    // DTO: password → DB: user_password
            pstmt.setString(2, user.getNickname());    // DTO: nickname → DB: user_name
            pstmt.setString(3, user.getEmail());       // DTO: email → DB: user_email
            
            // introduce 필드 안전하게 처리
            if (user.getIntroduce() == null) {
                pstmt.setString(4, ""); // NULL 대신 빈 문자열 사용
            } else {
                pstmt.setString(4, user.getIntroduce());
            }
              // SQL 실행 및 결과 확인
            int result = pstmt.executeUpdate();
            logger.info("SQL 실행 결과: " + result + "행 삽입됨");
            return result > 0;
        } catch (SQLException e) {
            logger.severe("회원가입 SQL 오류: " + e.getMessage() + 
                         ", SQL 상태 코드: " + e.getSQLState() + 
                         ", 오류 코드: " + e.getErrorCode());
            throw e;
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }
      // 사용자 ID로 조회
    public UserDTO getUserById(long userId) throws SQLException {
        UserDTO user = null;
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "SELECT * FROM user WHERE user_uid = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, userId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                user = mapResultSetToUser(rs);
            }
            return user;
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
    }
    
    // 사용자 이름으로 조회
    public UserDTO getUserByUsername(String username) throws SQLException {
        UserDTO user = null;
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "SELECT * FROM user WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                user = mapResultSetToUser(rs);
            }
            return user;
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
    }
    
    // 이메일로 사용자 조회 (중복 체크용)
    public boolean isEmailExists(String email) throws SQLException {
        // user_email 컬럼명 사용
        String sql = "SELECT COUNT(*) FROM user WHERE user_email = ?";
        
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    // 사용자 이름 중복 체크
    public boolean isUsernameExists(String username) throws SQLException {
        // user_id 컬럼명 사용 (username은 DB에서 user_id로 저장됨)
        String sql = "SELECT COUNT(*) FROM user WHERE user_id = ?";
        
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
      /**
     * 닉네임 중복 검사
     * @param nickname 검사할 닉네임
     * @return 중복이면 true, 아니면 false
     */
    public boolean isNicknameExists(String nickname) throws SQLException {
        // user_name 컬럼명 사용 (nickname은 DB에서 user_name으로 저장됨)
        String sql = "SELECT COUNT(*) FROM user WHERE user_name = ?";
        
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nickname);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    boolean exists = rs.getInt(1) > 0;
                    if (exists) {
                        logger.info("중복된 닉네임 발견: " + nickname);
                    }
                    return exists;
                }
            }
        } catch (SQLException e) {
            logger.severe("닉네임 중복 검사 중 오류 발생: " + e.getMessage());
            throw e;
        }
        
        return false;
    }
    
    /**
     * 이메일로 사용자 정보 조회
     * @param email 이메일
     * @return 사용자 정보 객체
     */
    public UserDTO getUserByEmail(String email) throws SQLException {
        UserDTO user = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "SELECT * FROM user WHERE user_email = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                user = mapResultSetToUser(rs);
            }
            return user;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }
      // 이메일과 비밀번호로 로그인하는 메서드
    public UserDTO login(String email, String password) throws SQLException {
        UserDTO user = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnectionUtil.getConnection();
            
            // 먼저 이메일로 사용자 정보 조회
            String sql = "SELECT * FROM user WHERE user_email = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // 사용자 정보 매핑
                user = mapResultSetToUser(rs);
                
                // 입력된 비밀번호와 저장된 비밀번호가 일치하는지 확인
                if (user.getPassword().equals(password)) {
                    // 일치하면 마지막 로그인 시간 업데이트
                    updateLastLoginDate(user.getUserId());
                    return user;
                } else {
                    // 비밀번호 불일치
                    return null;
                }
            }
            
            return null; // 사용자를 찾지 못함
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
    }
    
    // 마지막 로그인 시간 업데이트
    private void updateLastLoginDate(long userId) throws SQLException {
        Connection conn2 = null;
        PreparedStatement pstmt2 = null;
        
        try {
            conn2 = DBConnectionUtil.getConnection();
            String sql = "UPDATE user SET last_login_date = NOW() WHERE user_id = ?";
            pstmt2 = conn2.prepareStatement(sql);
            pstmt2.setLong(1, userId);
            pstmt2.executeUpdate();
        } finally {
            if (pstmt2 != null) {
                try {
                    pstmt2.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            
            if (conn2 != null) {
                try {
                    conn2.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
      // 사용자 정보 업데이트
    public boolean updateUser(UserDTO user) throws SQLException {
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "UPDATE user SET user_email = ?, user_name = ?, user_introduce = ? WHERE user_uid = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, user.getUserName());
            pstmt.setString(3, user.getIntroduce());
            pstmt.setLong(4, user.getUserId());
            
            System.out.println("SQL 실행: " + sql);
            System.out.println("파라미터: user_email=" + user.getEmail() + 
                             ", user_name=" + user.getUserName() + 
                             ", user_introduce=" + user.getIntroduce() + 
                             ", user_uid=" + user.getUserId());
            
            return pstmt.executeUpdate() > 0;
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
    }
      // 비밀번호 변경
    public boolean updatePassword(long userId, String newPassword) throws SQLException {
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "UPDATE user SET user_password = ? WHERE user_uid = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newPassword); // 실제로는 암호화 처리 필요
            pstmt.setLong(2, userId);
            
            System.out.println("비밀번호 업데이트 SQL 실행: " + sql);
            System.out.println("파라미터: user_password=[암호화된 값], user_uid=" + userId);
            
            return pstmt.executeUpdate() > 0;
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
    }    // 회원 탈퇴 (user_status를 'banned'으로 변경)
    public boolean deactivateUser(long userId) throws SQLException {
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "UPDATE user SET user_status = 'banned' WHERE user_uid = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, userId);
            
            System.out.println("회원 탈퇴 처리 SQL 실행: " + sql);
            System.out.println("파라미터: user_uid=" + userId);
            
            return pstmt.executeUpdate() > 0;
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
    }
      // DTO와 DB 매핑을 처리하는 메서드
    private UserDTO mapResultSetToUser(ResultSet rs) throws SQLException {
        UserDTO user = new UserDTO();
        
        // 디버그 로그 추가
        long userUid = rs.getLong("user_uid");
        String userId = rs.getString("user_id");
        String userName = rs.getString("user_name");
        String userEmail = rs.getString("user_email");
        
        logger.info("사용자 매핑: user_uid=" + userUid + ", user_id=" + userId + ", user_name=" + userName + ", user_email=" + userEmail);
        
        // 핵심 매핑 (필드명 불일치 해결)
        user.setUserId(userUid);  // user_uid → userId
        user.setUsername(userId);     // DB: user_id → DTO: username
        user.setPassword(rs.getString("user_password")); // DB: user_password → DTO: password
        user.setEmail(userEmail);     // DB: user_email → DTO: email
        user.setNickname(userName);   // DB: user_name → DTO: nickname
        // userName도 설정 (마이페이지 호환용)
        user.setUserName(userName);   // DB: user_name → DTO: userName
          // 권한 변환 (enum → int)
        String authority = rs.getString("user_authority");
        if ("admin".equals(authority)) {
            user.setUserLevel(3);  // 관리자
        } else if ("manager".equals(authority)) {
            user.setUserLevel(2);  // 매니저
        } else {
            user.setUserLevel(1);  // 일반 회원 (normal)
        }
        
        // DB에 없는 필드는 기본값 설정
        user.setRegisterDate(LocalDateTime.now());
        user.setLastLoginDate(LocalDateTime.now());
        user.setActive(true);
        
        // 추가 필드 매핑
        user.setIntroduce(rs.getString("user_introduce"));
        user.setUserStatus(rs.getString("user_status"));
        
        logger.info("UserDTO 객체 생성 완료: " + user.toString());
        return user;
    }
    
    // 전체 사용자 목록 조회 (관리자용)
    public List<UserDTO> getAllUsers() throws SQLException {
        List<UserDTO> users = new ArrayList<>();
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "SELECT * FROM user ORDER BY user_id";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            return users;
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
    }
    
    // 사용자 제한 여부 확인
    public boolean isUserRestricted(long userId) throws SQLException {
        String sql = "SELECT user_status FROM user WHERE user_uid = ?";
        try {
            conn = DBConnectionUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, userId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String status = rs.getString("user_status");
                return "restricted".equals(status) || "suspended".equals(status) || "banned".equals(status);
            }
            return false;
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
    }
}