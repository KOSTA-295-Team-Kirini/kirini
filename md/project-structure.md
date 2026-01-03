# 프로젝트 패키지 구조

작성일: 2026-01-02
최종 수정일: 2026-01-02

## 개요

kirini-spring 프로젝트의 패키지 구조 및 각 패키지의 역할을 정의합니다.

---

## 패키지 구조

```
src/main/java/dev/earlydreamer/kirini/
├── KiriniSpringApplication.java    # Spring Boot 메인 클래스
│
├── domain/                          # JPA 엔티티
│   ├── User.java                    # account 테이블
│   ├── Freeboard.java               # freeboard 테이블
│   ├── FreeboardAttach.java         # freeboard_attach 테이블
│   ├── FreeboardComment.java        # freeboard_comment 테이블
│   ├── KeyboardCategory.java        # keyboard_category 테이블
│   ├── KeyboardInformation.java     # keyboard_information 테이블
│   ├── KeyboardScore.java           # keyboard_score 테이블
│   ├── KeyboardTag.java             # keyboard_tag 테이블
│   ├── KeyboardTaglist.java         # keyboard_taglist 테이블
│   ├── Scrap.java                   # scrap 테이블
│   └── UserPenalty.java             # account_penalty 테이블
│
├── repository/                      # Spring Data JPA Repository
│   └── UserRepository.java
│
├── service/                         # 비즈니스 로직
│   └── UserService.java
│
├── controller/                      # REST API 컨트롤러
│   └── UserController.java
│
├── dto/                             # Data Transfer Object
│   ├── request/                     # 요청 DTO
│   │   └── SignUpRequest.java
│   └── response/                    # 응답 DTO
│       ├── ApiResponse.java
│       ├── SignUpResponse.java
│       └── DuplicateCheckResponse.java
│
├── exception/                       # 예외 클래스
│   ├── BusinessException.java
│   ├── GlobalExceptionHandler.java
│   └── user/
│       ├── DuplicateEmailException.java
│       └── DuplicateNicknameException.java
│
├── config/                          # 설정 클래스
│   └── PasswordEncoderConfig.java
│
└── common/                          # 공통 유틸리티
    └── (향후 추가)
```

---

## 패키지별 역할

### 1. domain/
- **역할**: JPA 엔티티 클래스 정의
- **규칙**:
  - `@Entity` 어노테이션 사용
  - `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 적용
  - Setter 대신 정적 팩토리 메서드 사용
  - 연관관계는 지연 로딩(LAZY) 기본 적용

### 2. repository/
- **역할**: 데이터 접근 계층 (DAO)
- **규칙**:
  - `JpaRepository<Entity, ID>` 상속
  - 쿼리 메서드 네이밍 컨벤션 준수
  - 복잡한 쿼리는 `@Query` 어노테이션 사용

### 3. service/
- **역할**: 비즈니스 로직 처리
- **규칙**:
  - `@Service` 어노테이션 사용
  - `@Transactional` 적용 (읽기 전용은 `readOnly = true`)
  - Repository 의존성 주입

### 4. controller/
- **역할**: REST API 엔드포인트 정의
- **규칙**:
  - `@RestController` 어노테이션 사용
  - `@RequestMapping("/api/{도메인}")` 기본 경로 설정
  - 입력 검증은 `@Valid` 사용

### 5. dto/
- **역할**: 계층 간 데이터 전송 객체
- **하위 패키지**:
  - `request/`: 클라이언트 → 서버 요청 데이터
  - `response/`: 서버 → 클라이언트 응답 데이터
- **규칙**:
  - 불변 객체 권장 (record 또는 final 필드)
  - Bean Validation 어노테이션 적용 (request)

### 6. exception/
- **역할**: 커스텀 예외 및 전역 예외 처리
- **구성**:
  - `BusinessException`: 비즈니스 예외 기본 클래스
  - `GlobalExceptionHandler`: `@RestControllerAdvice` 전역 핸들러
  - 도메인별 하위 패키지 (`user/`, `board/` 등)

### 7. config/
- **역할**: Spring 설정 클래스
- **예시**:
  - `PasswordEncoderConfig`: BCryptPasswordEncoder 빈 등록
  - `WebConfig`: CORS, 인터셉터 등 웹 설정

### 8. common/
- **역할**: 공통 유틸리티, 상수, 헬퍼 클래스
- **예시**:
  - 날짜/시간 유틸리티
  - 공통 상수 정의
  - 페이징 유틸리티

---

## 의존성 흐름

```
Controller → Service → Repository → Database
    ↓           ↓
   DTO      Exception
```

- Controller는 Service만 의존
- Service는 Repository와 다른 Service 의존 가능
- Repository는 Entity(Domain)만 의존
- DTO는 계층 간 데이터 전달에만 사용

