# Step 02: UserRepository 생성

## 목표

Spring Data JPA Repository 인터페이스를 생성하여 User 엔티티에 대한 데이터 접근 기능을 구현합니다.

---

## 구현 내용

### UserRepository 인터페이스

```java
package dev.earlydreamer.kirini.repository;

import dev.earlydreamer.kirini.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);

    /**
     * 이메일 존재 여부 확인 (중복 체크용)
     */
    boolean existsByEmail(String email);

    /**
     * 닉네임 존재 여부 확인 (중복 체크용)
     */
    boolean existsByName(String name);

    /**
     * userId 존재 여부 확인
     */
    boolean existsByUserId(String userId);
}
```

---

## 파일 위치

`src/main/java/dev/earlydreamer/kirini/repository/UserRepository.java`

---

## 메서드 설명

| 메서드 | 용도 |
|--------|------|
| `findByEmail()` | 로그인 시 이메일로 사용자 조회 |
| `existsByEmail()` | 이메일 중복 체크 |
| `existsByName()` | 닉네임 중복 체크 |
| `existsByUserId()` | 사용자 ID 중복 체크 (향후 사용) |

---

## 체크리스트

- [x] JpaRepository 상속
- [x] 이메일 조회 메서드
- [x] 이메일 중복 체크 메서드
- [x] 닉네임 중복 체크 메서드

---

## 완료 상태

✅ 구현 완료

