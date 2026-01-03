# 회원가입 기능 개요

작성일: 2026-01-02

## 기능 설명

사용자가 이메일, 비밀번호, 닉네임을 입력하여 회원가입을 수행하는 기능입니다.

---

## 요구사항

### 기능 요구사항

1. 사용자는 이메일, 비밀번호, 닉네임을 입력하여 회원가입할 수 있다.
2. 이메일은 중복될 수 없다.
3. 닉네임은 중복될 수 없다.
4. 비밀번호는 정해진 규칙을 만족해야 한다.
5. 회원가입 완료 시 기본 권한은 `NORMAL`이다.
6. 회원가입 완료 시 기본 상태는 `ACTIVE`이다.
7. 회원가입 완료 시 기본 포인트는 `0`이다.

### 비기능 요구사항

1. 비밀번호는 BCrypt로 암호화하여 저장한다.
2. 입력값 검증 실패 시 적절한 에러 메시지를 반환한다.
3. 중복 체크 API는 별도로 제공한다.

---

## API 명세

### 1. 회원가입

| 항목 | 내용 |
|------|------|
| URL | `POST /api/user/register` |
| Content-Type | `application/json` |

**Request Body**
```json
{
  "email": "user@example.com",
  "password": "Password1@",
  "nickname": "사용자닉네임"
}
```

**Response (성공)**
```json
{
  "success": true,
  "message": "회원가입이 완료되었습니다.",
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "nickname": "사용자닉네임"
  }
}
```

**Response (실패)**
```json
{
  "success": false,
  "message": "이미 사용 중인 이메일입니다.",
  "errorCode": "DUPLICATE_EMAIL"
}
```

### 2. 이메일 중복 체크

| 항목 | 내용 |
|------|------|
| URL | `GET /api/user/check-email?email={email}` |

**Response**
```json
{
  "isAvailable": true,
  "isDuplicate": false,
  "message": "사용 가능한 이메일입니다."
}
```

### 3. 닉네임 중복 체크

| 항목 | 내용 |
|------|------|
| URL | `GET /api/user/check-nickname?nickname={nickname}` |

**Response**
```json
{
  "isAvailable": true,
  "isDuplicate": false,
  "message": "사용 가능한 닉네임입니다."
}
```

---

## 데이터베이스 스키마

### account 테이블

> ⚠️ `user`는 H2 예약어이므로 `account`로 변경됨

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| account_uid | INT (PK, AUTO_INCREMENT) | 사용자 고유 ID |
| account_id | VARCHAR(20) | 사용자 ID (미사용) |
| account_password | VARCHAR(600) | 암호화된 비밀번호 |
| account_name | VARCHAR(20) | 닉네임 |
| account_email | VARCHAR(50) | 이메일 |
| account_introduce | VARCHAR(200) | 자기소개 |
| account_authority | ENUM('NORMAL', 'ARMBAND', 'ADMIN') | 권한 |
| account_point | INT | 포인트 |
| account_icon | TEXT | 아이콘 |
| account_status | ENUM('ACTIVE', 'RESTRICTED', 'SUSPENDED', 'BANNED') | 상태 |

---

## 구현 단계

| 단계 | 파일 | 설명 |
|------|------|------|
| 01 | [01-domain-entity.md](01-domain-entity.md) | User 엔티티에 정적 팩토리 메서드 추가 |
| 02 | [02-repository.md](02-repository.md) | UserRepository 인터페이스 생성 |
| 03 | [03-dto.md](03-dto.md) | Request/Response DTO 생성 |
| 04 | [04-exception.md](04-exception.md) | 커스텀 예외 클래스 생성 |
| 05 | [05-service.md](05-service.md) | UserService 비즈니스 로직 구현 |
| 06 | [06-controller.md](06-controller.md) | UserController REST API 구현 |

---

## 참고

- Legacy 코드: `legacy/src/main/java/business/service/user/UserService.java`
- Legacy 컨트롤러: `legacy/src/main/java/presentation/controller/page/user/UserRegisterController.java`
- MySQL 스키마: `src/main/resources/schema-mysql.sql`
- 트러블슈팅: `md/troubleshooting.md`
