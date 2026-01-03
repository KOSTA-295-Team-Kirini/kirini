# Kirini Spring 프로젝트 공통 개발 정책

작성일: 2026-01-02  
최종 수정일: 2026-01-02

## 1. 프로젝트 개요

- **프로젝트명**: kirini-spring
- **기술 스택**: Spring Boot 4.0.1, Java 25, JPA/Hibernate, Lombok
- **패키지 루트**: `dev.earlydreamer.kirini`

---

## 2. 패키지 구조 및 역할

```
dev.earlydreamer.kirini/
├── domain/          # JPA 엔티티 클래스
├── repository/      # Spring Data JPA Repository 인터페이스
├── service/         # 비즈니스 로직 서비스 클래스
├── controller/      # REST API 컨트롤러
├── dto/
│   ├── request/     # 요청 DTO
│   └── response/    # 응답 DTO
├── exception/       # 커스텀 예외 클래스
├── config/          # 설정 클래스 (Security, Web 등)
└── common/          # 공통 유틸리티, 상수 등
```

---

## 3. 네이밍 규칙

### 3.1 클래스 네이밍

| 계층 | 네이밍 패턴 | 예시 |
|------|------------|------|
| Entity | `{도메인명}` | `User`, `Freeboard`, `KeyboardScore` |
| Repository | `{도메인명}Repository` | `UserRepository`, `FreeboardRepository` |
| Service | `{도메인명}Service` | `UserService`, `FreeboardService` |
| Controller | `{도메인명}Controller` | `UserController`, `FreeboardController` |
| Request DTO | `{기능명}Request` | `SignUpRequest`, `LoginRequest` |
| Response DTO | `{기능명}Response` | `SignUpResponse`, `UserInfoResponse` |
| Exception | `{예외상황}Exception` | `DuplicateEmailException`, `UserNotFoundException` |

### 3.2 메서드 네이밍

| 유형 | 패턴 | 예시 |
|------|------|------|
| 조회 (단건) | `get{대상}By{조건}` | `getUserById()`, `getUserByEmail()` |
| 조회 (목록) | `get{대상}List`, `getAll{대상}s` | `getAllUsers()`, `getFreeboardList()` |
| 존재 확인 | `exists{대상}By{조건}`, `is{조건}Exists` | `existsByEmail()`, `isEmailExists()` |
| 등록 | `register{대상}`, `create{대상}` | `registerUser()`, `createFreeboard()` |
| 수정 | `update{대상}` | `updateUser()`, `updatePassword()` |
| 삭제 | `delete{대상}`, `deactivate{대상}` | `deleteUser()`, `deactivateUser()` |
| 검증 | `check{대상}`, `validate{대상}` | `checkDuplicateEmail()`, `validatePassword()` |

### 3.3 API 엔드포인트 네이밍

- **기본 형식**: `/api/{도메인}/{기능}`
- **RESTful 규칙 준수**

| HTTP 메서드 | 용도 | 예시 |
|-------------|------|------|
| GET | 조회 | `GET /api/user/{id}` |
| POST | 생성 | `POST /api/user/register` |
| PUT | 전체 수정 | `PUT /api/user/{id}` |
| PATCH | 부분 수정 | `PATCH /api/user/{id}/password` |
| DELETE | 삭제 | `DELETE /api/user/{id}` |

---

## 4. API 응답 형식

### 4.1 성공 응답

```json
{
  "success": true,
  "message": "처리가 완료되었습니다.",
  "data": { ... }
}
```

### 4.2 실패 응답

```json
{
  "success": false,
  "message": "오류 메시지",
  "errorCode": "ERROR_CODE"
}
```

### 4.3 중복 체크 응답 (legacy 호환)

```json
{
  "isAvailable": true,
  "isDuplicate": false,
  "message": "사용 가능한 이메일입니다."
}
```

---

## 5. 비밀번호 정책

### 5.1 암호화

- **알고리즘**: BCrypt (Spring Security의 `BCryptPasswordEncoder` 사용)
- **강도**: 기본값 10 (필요시 조정)

> ⚠️ Legacy 프로젝트는 SHA-256을 사용했으나, 보안 강화를 위해 BCrypt로 변경

### 5.2 유효성 검증 규칙

- 최소 8자 이상
- 영문 대문자 1개 이상 포함
- 영문 소문자 1개 이상 포함
- 숫자 1개 이상 포함
- 특수문자 1개 이상 포함 (`@#$%^&+=`)
- 공백 불가

```java
// 정규식 패턴
String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
```

---

## 6. 사용자 권한 및 상태

### 6.1 권한 (Authority)

| 값 | 설명 |
|----|------|
| `NORMAL` | 일반 회원 (기본값) |
| `ARMBAND` | 매니저 |
| `ADMIN` | 관리자 |

### 6.2 상태 (UserStatus)

| 값 | 설명 |
|----|------|
| `ACTIVE` | 활성 (기본값) |
| `RESTRICTED` | 제한 |
| `SUSPENDED` | 정지 |
| `BANNED` | 차단 |

---

## 7. 예외 처리 정책

### 7.1 커스텀 예외 클래스 구조

```
exception/
├── BusinessException.java        # 비즈니스 예외 기본 클래스
├── user/
│   ├── DuplicateEmailException.java
│   ├── DuplicateNicknameException.java
│   ├── UserNotFoundException.java
│   └── InvalidPasswordException.java
└── GlobalExceptionHandler.java   # @RestControllerAdvice
```

### 7.2 예외 처리 원칙

1. **비즈니스 예외**: 커스텀 예외 클래스 사용
2. **입력 검증 실패**: `@Valid` + `MethodArgumentNotValidException` 처리
3. **에러 발생 시 안전한 기본값**: 중복 체크 실패 시 `true` 반환 (안전 우선)

---

## 8. 입력 검증 (Validation)

- Bean Validation (`jakarta.validation`) 사용
- Request DTO에 검증 어노테이션 적용

```java
public class SignUpRequest {
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
             message = "비밀번호는 8자 이상, 대소문자, 숫자, 특수문자를 포함해야 합니다.")
    private String password;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
    private String nickname;
}
```

---

## 9. 엔티티 설계 원칙

### 9.1 기본 규칙

- `@NoArgsConstructor(access = AccessLevel.PROTECTED)`: 기본 생성자 보호
- `@Getter`: Lombok 사용
- Setter 대신 **정적 팩토리 메서드** 또는 **빌더 패턴** 사용
- 연관관계는 **지연 로딩(LAZY)** 기본 적용

### 9.2 정적 팩토리 메서드 패턴

```java
// 회원가입용 정적 팩토리 메서드
public static User createForSignUp(String email, String password, String nickname) {
    User user = new User();
    user.email = email;
    user.password = password;
    user.name = nickname;
    user.authority = Authority.NORMAL;
    user.status = UserStatus.ACTIVE;
    user.point = 0;
    return user;
}
```

---

## 10. 문서화 규칙

### 10.1 기능별 문서 구조

```
md/
├── project-structure.md          # 패키지 구조 문서
└── features/
    └── {기능명}/
        ├── 00-overview.md        # 기능 개요
        ├── 01-xxx.md             # 단계별 구현 계획
        ├── 02-xxx.md
        └── ...
```

### 10.2 문서 작성 원칙

- 모든 문서는 **한국어**로 작성
- 각 기능은 **단계별(step-by-step)** 구현 계획 문서 포함
- 코드 예시는 실제 구현과 동일하게 유지

---

## 11. 데이터베이스 설정

### 11.1 프로파일별 설정

| 프로파일 | DB | Dialect | ddl-auto | Docker Compose |
|---------|-----|---------|----------|----------------|
| 기본 (로컬) | H2 | H2Dialect | create-drop | 비활성화 |
| devtest | MySQL 8.4.4 | MySQLDialect | update | 활성화 |
| prod | MySQL 8.4.4 | MySQLDialect | validate | - |

### 11.2 테이블 네이밍 규칙

> ⚠️ `user`는 H2 예약어이므로 사용 금지. `account`로 대체함.

| 엔티티 | 테이블명 | PK 컬럼 |
|--------|----------|---------|
| User | `account` | `account_uid` |
| UserPenalty | `account_penalty` | `penalty_uid` |
| Freeboard | `freeboard` | `freeboard_uid` |
| FreeboardComment | `freeboard_comment` | `freeboard_comment_uid` |
| FreeboardAttach | `freeboard_attach` | `attach_uid` |
| KeyboardCategory | `keyboard_category` | `keyboard_category_uid` |
| KeyboardInformation | `keyboard_information` | `keyboard_information_uid` |
| KeyboardScore | `keyboard_score` | `keyboard_score_uid` |
| KeyboardTag | `keyboard_tag` | `tag_uid` |
| KeyboardTaglist | `keyboard_taglist` | `taglist_uid` |
| Scrap | `scrap` | `scrap_uid` |

### 11.3 외래키 컬럼명 규칙

- User(Account) 참조: `account_uid`
- 예외: Scrap 테이블은 `scrap_account_uid` 사용

### 11.4 H2 예약어 주의

다음 단어는 H2에서 예약어이므로 테이블/컬럼명으로 사용 금지:
- `USER`, `ORDER`, `GROUP`, `KEY`, `INDEX`, `CONSTRAINT`, `CHECK`, `VALUE` 등

---

## 12. 참고 사항

- Legacy 프로젝트 위치: `./legacy/`
- Legacy API 구조는 가능한 한 유지하되, Spring Boot 방식으로 재구현
- DB 스키마 참조:
  - Legacy ERD: `legacy/docker-settings/classes/kirini-ERD-1.sql`
  - 신규 MySQL 스키마: `src/main/resources/schema-mysql.sql`
- 트러블슈팅 기록: `md/troubleshooting.md`
