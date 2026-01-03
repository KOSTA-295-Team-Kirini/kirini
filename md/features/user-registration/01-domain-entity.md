# Step 01: User 엔티티 수정

## 목표

User 엔티티에 회원가입용 정적 팩토리 메서드를 추가합니다.

---

## 현재 상태

- User 엔티티는 이미 존재함
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 적용됨
- Setter가 없어 객체 생성 방법 필요
- **테이블명**: `account` (H2 예약어 `user` 회피)

---

## 테이블 매핑

> ⚠️ `user`는 H2 예약어이므로 `account`로 변경됨

```java
@Entity
@Table(name = "account")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_uid")
    private Integer id;

    @Column(name = "account_email", length = 50)
    private String email;

    @Column(name = "account_password", length = 600)
    private String password;

    @Column(name = "account_name", length = 20)
    private String name;
    // ...
}
```

---

## 변경 사항

### 추가할 메서드

```java
/**
 * 회원가입용 정적 팩토리 메서드
 * 
 * @param email 이메일
 * @param encodedPassword BCrypt로 암호화된 비밀번호
 * @param nickname 닉네임
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
```

---

## 파일 위치

`src/main/java/dev/earlydreamer/kirini/domain/User.java`

---

## 체크리스트

- [x] 정적 팩토리 메서드 `createForSignUp()` 추가
- [x] 기본값 설정 (authority=NORMAL, status=ACTIVE, point=0)
- [x] 암호화된 비밀번호를 받도록 파라미터명 명확화

---

## 완료 상태

✅ 구현 완료

