package dev.earlydreamer.kirini.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "account")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_uid")
    private Integer id;

    @Column(name = "account_id", length = 20)
    private String userId;

    @Column(name = "account_password", length = 600)
    private String password;

    @Column(name = "account_name", length = 20)
    private String name;

    @Column(name = "account_email", length = 50)
    private String email;

    @Column(name = "account_introduce", length = 200)
    private String introduce;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_authority")
    private Authority authority;

    @Column(name = "account_point")
    private Integer point;

    @Column(name = "account_icon", columnDefinition = "TEXT")
    private String icon;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Freeboard> freeboards = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<FreeboardComment> freeboardComments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserPenalty> penalties = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<KeyboardScore> keyboardScores = new ArrayList<>();

    public enum Authority {
        NORMAL, ARMBAND, ADMIN
    }

    public enum UserStatus {
        ACTIVE, RESTRICTED, SUSPENDED, BANNED
    }

    /**
     * 회원가입용 정적 팩토리 메서드
     *
     * @param email           이메일
     * @param encodedPassword BCrypt로 암호화된 비밀번호
     * @param nickname        닉네임
     * @return 생성된 User 엔티티
     */
    public static User createForSignUp(String email, String encodedPassword, String nickname) {
        User user = new User();
        user.email = email;
        user.password = encodedPassword;
        user.name = nickname;
        user.authority = Authority.NORMAL;
        user.status = UserStatus.ACTIVE;
        user.point = 0;
        user.introduce = "";
        return user;
    }
}

