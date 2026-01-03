# Step 05: UserService 생성

## 목표

회원가입 비즈니스 로직을 처리하는 UserService를 구현합니다.

---

## 구현 내용

### UserService

```java
package dev.earlydreamer.kirini.service;

import dev.earlydreamer.kirini.domain.User;
import dev.earlydreamer.kirini.dto.request.SignUpRequest;
import dev.earlydreamer.kirini.dto.response.SignUpResponse;
import dev.earlydreamer.kirini.exception.user.DuplicateEmailException;
import dev.earlydreamer.kirini.exception.user.DuplicateNicknameException;
import dev.earlydreamer.kirini.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     */
    @Transactional
    public SignUpResponse registerUser(SignUpRequest request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException();
        }

        // 닉네임 중복 체크
        if (userRepository.existsByName(request.getNickname())) {
            throw new DuplicateNicknameException();
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // User 엔티티 생성
        User user = User.createForSignUp(
                request.getEmail(),
                encodedPassword,
                request.getNickname()
        );

        // 저장
        User savedUser = userRepository.save(user);

        // 응답 생성
        return SignUpResponse.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .nickname(savedUser.getName())
                .build();
    }

    /**
     * 이메일 중복 체크
     */
    public boolean checkDuplicateEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 닉네임 중복 체크
     */
    public boolean checkDuplicateNickname(String nickname) {
        return userRepository.existsByName(nickname);
    }
}
```

---

## 추가 설정: PasswordEncoder 빈 등록

### PasswordEncoderConfig

```java
package dev.earlydreamer.kirini.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

## 의존성 추가 (build.gradle)

```groovy
// BCryptPasswordEncoder 사용을 위한 Spring Security Crypto
implementation 'org.springframework.security:spring-security-crypto'
```

---

## 파일 위치

- `src/main/java/dev/earlydreamer/kirini/service/UserService.java`
- `src/main/java/dev/earlydreamer/kirini/config/PasswordEncoderConfig.java`

---

## 메서드 설명

| 메서드 | 용도 |
|--------|------|
| `registerUser()` | 회원가입 처리 (중복 체크 → 암호화 → 저장) |
| `checkDuplicateEmail()` | 이메일 중복 여부 반환 |
| `checkDuplicateNickname()` | 닉네임 중복 여부 반환 |

---

## 체크리스트

- [x] UserService 생성
- [x] 회원가입 메서드 구현
- [x] 중복 체크 메서드 구현
- [x] BCrypt 암호화 적용
- [x] PasswordEncoderConfig 설정 클래스 생성
- [x] 트랜잭션 설정

---

## 완료 상태

✅ 구현 완료

