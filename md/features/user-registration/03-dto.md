# Step 03: DTO 생성

## 목표

회원가입 기능에 필요한 Request/Response DTO를 생성합니다.

---

## 구현 파일 목록

1. `SignUpRequest.java` - 회원가입 요청 DTO
2. `SignUpResponse.java` - 회원가입 응답 DTO
3. `ApiResponse.java` - 공통 API 응답 래퍼
4. `DuplicateCheckResponse.java` - 중복 체크 응답 DTO

---

## 1. SignUpRequest

```java
package dev.earlydreamer.kirini.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignUpRequest {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Size(max = 50, message = "이메일은 50자를 초과할 수 없습니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
        message = "비밀번호는 8자 이상, 대소문자, 숫자, 특수문자를 포함해야 합니다."
    )
    private String password;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
    private String nickname;
}
```

---

## 2. SignUpResponse

```java
package dev.earlydreamer.kirini.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignUpResponse {
    private Integer userId;
    private String email;
    private String nickname;
}
```

---

## 3. ApiResponse (공통 응답 래퍼)

```java
package dev.earlydreamer.kirini.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String errorCode;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
}
```

---

## 4. DuplicateCheckResponse (Legacy 호환)

```java
package dev.earlydreamer.kirini.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DuplicateCheckResponse {
    private boolean isAvailable;
    private boolean isDuplicate;
    private String message;

    public static DuplicateCheckResponse available(String message) {
        return DuplicateCheckResponse.builder()
                .isAvailable(true)
                .isDuplicate(false)
                .message(message)
                .build();
    }

    public static DuplicateCheckResponse duplicate(String message) {
        return DuplicateCheckResponse.builder()
                .isAvailable(false)
                .isDuplicate(true)
                .message(message)
                .build();
    }
}
```

---

## 파일 위치

- `src/main/java/dev/earlydreamer/kirini/dto/request/SignUpRequest.java`
- `src/main/java/dev/earlydreamer/kirini/dto/response/SignUpResponse.java`
- `src/main/java/dev/earlydreamer/kirini/dto/response/ApiResponse.java`
- `src/main/java/dev/earlydreamer/kirini/dto/response/DuplicateCheckResponse.java`

---

## 체크리스트

- [x] SignUpRequest 생성 (Bean Validation 적용)
- [x] SignUpResponse 생성
- [x] ApiResponse 공통 래퍼 생성
- [x] DuplicateCheckResponse 생성 (Legacy 호환)

---

## 완료 상태

✅ 구현 완료

