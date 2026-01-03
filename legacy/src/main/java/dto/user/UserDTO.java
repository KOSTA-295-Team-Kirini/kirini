package dto.user;

import java.time.LocalDateTime;

public class UserDTO {
    private long userId;
    private String username;
    private String password; // 실제로는 암호화된 상태로 저장, 전송 시에는 제외
    private String email;
    private String nickname;
    private int userLevel; // 1: 일반회원, 2: 매니저, 3: 관리자
    private LocalDateTime registerDate;
    private LocalDateTime lastLoginDate;
    private boolean isActive;
    private String introduce; // 사용자 자기소개
    private String userStatus;
    private String userAuthority; // 사용자 권한 (admin, armband, user 등)

    // JSON 직렬화를 위한 날짜 문자열 필드
    private transient String registerDateStr;
    private transient String lastLoginDateStr;

    // 기본 생성자
    public UserDTO() {
    }

    // 로그인용 생성자 - username 대신 email 사용
    public UserDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // 회원가입용 생성자 - username 필드 제거, 필요한 3개 필드만 사용
    public UserDTO(String password, String email, String nickname) {
        this.password = password;
        this.email = email;
        this.nickname = nickname;
        this.userLevel = 1; // 일반 회원
        this.isActive = true;
        this.registerDate = LocalDateTime.now();
    }

    // 전체 생성자
    public UserDTO(long userId, String username, String password, String email, String nickname,
                   int userLevel, LocalDateTime registerDate, LocalDateTime lastLoginDate, boolean isActive) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.nickname = nickname;
        this.userLevel = userLevel;
        this.registerDate = registerDate;
        this.lastLoginDate = lastLoginDate;
        this.isActive = isActive;
    }

    // 권한 포함 전체 생성자
    public UserDTO(long userId, String username, String password, String email, String nickname,
                   int userLevel, LocalDateTime registerDate, LocalDateTime lastLoginDate, boolean isActive,
                   String userAuthority) {
        this(userId, username, password, email, nickname, userLevel, registerDate, lastLoginDate, isActive);
        this.userAuthority = userAuthority;
    }

    // UserRegisterController.java에서 사용
    public static UserDTO createUserForRegistration(String password, String email, String nickname) {
        return new UserDTO(password, email, nickname);
    }

    // Getters and Setters
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(int userLevel) {
        this.userLevel = userLevel;
    }

    public LocalDateTime getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(LocalDateTime registerDate) {
        this.registerDate = registerDate;
        if (registerDate != null) {
            this.registerDateStr = registerDate.toString();
        }
    }

    public String getRegisterDateStr() {
        if (registerDateStr == null && registerDate != null) {
            registerDateStr = registerDate.toString();
        }
        return registerDateStr;
    }

    public void setRegisterDateStr(String registerDateStr) {
        this.registerDateStr = registerDateStr;
    }

    public LocalDateTime getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(LocalDateTime lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
        if (lastLoginDate != null) {
            this.lastLoginDateStr = lastLoginDate.toString();
        }
    }

    public String getLastLoginDateStr() {
        if (lastLoginDateStr == null && lastLoginDate != null) {
            lastLoginDateStr = lastLoginDate.toString();
        }
        return lastLoginDateStr;
    }

    public void setLastLoginDateStr(String lastLoginDateStr) {
        this.lastLoginDateStr = lastLoginDateStr;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    // getUserUid 메서드 - userId 필드를 사용
    public long getUserUid() {
        return userId;
    }

    // getUserAuthority 메서드
    public String getUserAuthority() {
        // userLevel에 따라 권한 반환
        if (userAuthority != null) {
            return userAuthority;
        }

        // userAuthority가 설정되지 않은 경우 userLevel 기반으로 권한 결정
        switch (userLevel) {
            case 3:
                return "admin";
            case 2:
                return "armband";
            default:
                return "user";
        }
    }

    // userAuthority 설정 메서드
    public void setUserAuthority(String userAuthority) {
        this.userAuthority = userAuthority;
    }

    // 마이페이지 컨트롤러 호환용 메서드 추가
    public String getUserName() {
        return username;
    }

    public void setUserName(String username) {
        this.username = username;
    }

    public String getUserEmail() {
        return email;
    }

    public void setUserEmail(String email) {
        this.email = email;
    }

    public String getUserIntroduce() {
        return introduce;
    }

    public void setUserIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public String getUserPw() {
        return password;
    }

    public void setUserPw(String password) {
        this.password = password;
    }

    public void setUserUid(long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", nickname='" + nickname + '\'' +
                ", userLevel=" + userLevel +
                ", registerDate='" + getRegisterDateStr() + '\'' +
                ", lastLoginDate='" + getLastLoginDateStr() + '\'' +
                ", isActive=" + isActive +
                ", userAuthority='" + getUserAuthority() + '\'' +
                '}';
    }
}