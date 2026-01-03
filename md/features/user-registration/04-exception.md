# Step 04: 예외 클래스 생성

## 목표

회원가입 기능에 필요한 커스텀 예외 클래스와 전역 예외 핸들러를 생성합니다.

---

## 구현 파일 목록

1. `BusinessException.java` - 비즈니스 예외 기본 클래스
2. `DuplicateEmailException.java` - 이메일 중복 예외
3. `DuplicateNicknameException.java` - 닉네임 중복 예외
4. `GlobalExceptionHandler.java` - 전역 예외 핸들러

---

## 1. BusinessException (기본 클래스)

```java
package dev.earlydreamer.kirini.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final String errorCode;

    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
```

---

## 2. DuplicateEmailException

```java
package dev.earlydreamer.kirini.exception.user;

import dev.earlydreamer.kirini.exception.BusinessException;

public class DuplicateEmailException extends BusinessException {
    public DuplicateEmailException() {
        super("이미 사용 중인 이메일입니다.", "DUPLICATE_EMAIL");
    }
}
```

---

## 3. DuplicateNicknameException

```java
package dev.earlydreamer.kirini.exception.user;

import dev.earlydreamer.kirini.exception.BusinessException;

public class DuplicateNicknameException extends BusinessException {
    public DuplicateNicknameException() {
        super("이미 사용 중인 닉네임입니다.", "DUPLICATE_NICKNAME");
    }
}
```

---

## 4. GlobalExceptionHandler

```java
package dev.earlydreamer.kirini.exception;

import dev.earlydreamer.kirini.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(e.getMessage(), e.getErrorCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("입력값이 올바르지 않습니다.");

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(message, "VALIDATION_ERROR"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("서버 오류가 발생했습니다.", "INTERNAL_ERROR"));
    }
}
```

---

## 파일 위치

- `src/main/java/dev/earlydreamer/kirini/exception/BusinessException.java`
- `src/main/java/dev/earlydreamer/kirini/exception/user/DuplicateEmailException.java`
- `src/main/java/dev/earlydreamer/kirini/exception/user/DuplicateNicknameException.java`
- `src/main/java/dev/earlydreamer/kirini/exception/GlobalExceptionHandler.java`

---

## 체크리스트

- [x] BusinessException 기본 클래스 생성
- [x] DuplicateEmailException 생성
- [x] DuplicateNicknameException 생성
- [x] GlobalExceptionHandler 생성
- [x] Validation 예외 처리 추가

---

## 완료 상태

✅ 구현 완료

