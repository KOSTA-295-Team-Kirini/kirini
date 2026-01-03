# Step 06: UserController 생성

## 목표

회원가입 REST API 엔드포인트를 제공하는 UserController를 구현합니다.

---

## 구현 내용

### UserController

```java
package dev.earlydreamer.kirini.controller;

import dev.earlydreamer.kirini.dto.request.SignUpRequest;
import dev.earlydreamer.kirini.dto.response.ApiResponse;
import dev.earlydreamer.kirini.dto.response.DuplicateCheckResponse;
import dev.earlydreamer.kirini.dto.response.SignUpResponse;
import dev.earlydreamer.kirini.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 회원가입
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<SignUpResponse>> register(
            @Valid @RequestBody SignUpRequest request) {
        
        SignUpResponse response = userService.registerUser(request);
        
        return ResponseEntity.ok(
                ApiResponse.success("회원가입이 완료되었습니다.", response)
        );
    }

    /**
     * 이메일 중복 체크
     */
    @GetMapping("/check-email")
    public ResponseEntity<DuplicateCheckResponse> checkEmail(
            @RequestParam String email) {
        
        boolean isDuplicate = userService.checkDuplicateEmail(email);
        
        if (isDuplicate) {
            return ResponseEntity.ok(
                    DuplicateCheckResponse.duplicate("이미 사용 중인 이메일입니다.")
            );
        }
        return ResponseEntity.ok(
                DuplicateCheckResponse.available("사용 가능한 이메일입니다.")
        );
    }

    /**
     * 닉네임 중복 체크
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<DuplicateCheckResponse> checkNickname(
            @RequestParam String nickname) {
        
        boolean isDuplicate = userService.checkDuplicateNickname(nickname);
        
        if (isDuplicate) {
            return ResponseEntity.ok(
                    DuplicateCheckResponse.duplicate("이미 사용 중인 닉네임입니다.")
            );
        }
        return ResponseEntity.ok(
                DuplicateCheckResponse.available("사용 가능한 닉네임입니다.")
        );
    }
}
```

---

## API 엔드포인트 요약

| HTTP Method | URL | 설명 |
|-------------|-----|------|
| POST | `/api/user/register` | 회원가입 |
| GET | `/api/user/check-email?email={email}` | 이메일 중복 체크 |
| GET | `/api/user/check-nickname?nickname={nickname}` | 닉네임 중복 체크 |

---

## 파일 위치

`src/main/java/dev/earlydreamer/kirini/controller/UserController.java`

---

## 체크리스트

- [x] UserController 생성
- [x] 회원가입 API 구현 (`POST /api/user/register`)
- [x] 이메일 중복 체크 API 구현 (`GET /api/user/check-email`)
- [x] 닉네임 중복 체크 API 구현 (`GET /api/user/check-nickname`)
- [x] Bean Validation 적용 (`@Valid`)
- [x] 응답 형식 통일 (ApiResponse, DuplicateCheckResponse)

---

## 완료 상태

✅ 구현 완료

