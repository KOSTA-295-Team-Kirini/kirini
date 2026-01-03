# 환경 변수(.env) 설정 가이드

## 개요
이 프로젝트는 민감한 정보(데이터베이스 비밀번호, API 키 등)를 `.env` 파일로 관리합니다.
`.env` 파일은 Git에 커밋되지 않으며(`.gitignore`에 포함), 각 개발자/서버에서 개별적으로 설정해야 합니다.

## 환경 변수 네이밍 규칙

### 접두사 규칙
- **공용 변수** (접두사 없음): devtest와 prod에서 공통으로 사용하는 기본값
  - 예: `HOST`, `PORT`, `DATABASE_NAME`, `USERNAME`, `PASSWORD`
  - 예: `JWT_SECRET`, `API_KEY` (DB 외 설정도 접두사 없이 사용)
- **Dev 배포 환경** (`DEV_` 접두사): devtest 프로파일 전용 설정
  - 예: `DEV_HOST`, `DEV_PASSWORD`
- **Prod 환경** (`PROD_` 접두사): prod 프로파일 전용 설정
  - 예: `PROD_HOST`, `PROD_PASSWORD`

### 우선순위
1. 환경별 접두사 변수 (`DEV_*`, `PROD_*`)
2. 공용 변수 (접두사 없음)
3. properties 파일의 기본값

예시: devtest 환경에서 DB 호스트를 찾는 순서
1. `DEV_HOST` 확인
2. 없으면 `HOST` 확인
3. 없으면 기본값 `localhost` 사용

## 초기 설정

### 1. .env 파일 생성
프로젝트 루트 디렉터리에서 `.env.example`을 복사하여 `.env` 파일을 생성합니다.

**Windows (PowerShell)**
```powershell
Copy-Item .env.example .env
```

**Linux/Mac (Bash)**
```bash
cp .env.example .env
```

### 2. .env 파일 수정
생성된 `.env` 파일을 열어 실제 값으로 변경합니다.

```bash
# .env 파일 예시

# === 공용 데이터베이스 설정 (접두사 없음) ===
HOST=localhost
PORT=3306
DATABASE_NAME=mydatabase
USERNAME=myuser
PASSWORD=actual_password_here

# === Dev 배포 환경 전용 설정 (DEV_ 접두사) ===
DEV_HOST=dev-db-host.example.com
DEV_PORT=3306
DEV_DATABASE_NAME=dev_database
DEV_USERNAME=dev_user
DEV_PASSWORD=actual_dev_password_here

# === Prod 환경 전용 설정 (PROD_ 접두사) ===
PROD_HOST=prod-db-host.example.com
PROD_PORT=3306
PROD_DATABASE_NAME=production_db
PROD_USERNAME=prod_user
PROD_PASSWORD=actual_prod_password_here

# === 기타 공용 설정 (접두사 없음) ===
JWT_SECRET=your_jwt_secret_here
API_KEY=your_api_key_here
```

## 프로파일별 사용

### 로컬 개발 (기본 프로파일)
- H2 인메모리 DB를 사용하므로 `.env` 파일 불필요
- 실행: `./gradlew bootRun`

### devtest 환경
- `.env` 파일의 `DEV_*` 변수를 우선 사용 (없으면 공용 변수 사용)
- 실행: `./gradlew bootRun --args="--spring.profiles.active=devtest"`
- 사용 변수 예시: `DEV_HOST`, `DEV_PASSWORD` 또는 `HOST`, `PASSWORD`

### prod 환경
- `.env` 파일의 `PROD_*` 변수를 우선 사용 (없으면 공용 변수 사용)
- 실행: `./gradlew bootRun --args="--spring.profiles.active=prod"`
- 사용 변수 예시: `PROD_HOST`, `PROD_PASSWORD` 또는 `HOST`, `PASSWORD`

## 보안 주의사항

### ⚠️ 절대 하지 말아야 할 것
- ❌ `.env` 파일을 Git에 커밋하지 마세요
- ❌ `.env` 파일을 슬랙/이메일 등으로 공유하지 마세요
- ❌ 실제 비밀번호를 코드나 커밋 메시지에 포함하지 마세요

### ✅ 권장 사항
- ✅ `.env.example`을 최신 상태로 유지하세요 (실제 값 제외)
- ✅ 서버 배포 시 안전한 방법(환경 변수, 시크릿 매니저)으로 주입하세요
- ✅ 비밀번호는 팀 내 보안 채널(1Password, Vault 등)을 통해 공유하세요
- ✅ 프로덕션 비밀번호는 정기적으로 교체하세요

## 문제 해결

### .env 파일이 로드되지 않는 경우
1. `.env` 파일이 프로젝트 루트에 있는지 확인
2. 파일 이름이 정확히 `.env`인지 확인 (확장자 없음)
3. Gradle 의존성 재로드: `./gradlew clean build`
4. IDE 재시작

### 환경 변수가 적용되지 않는 경우
1. `.env` 파일의 변수 이름과 `application-*.properties`의 참조가 일치하는지 확인
2. 변수 형식: `${VARIABLE_NAME:default_value}`
3. 공백이나 따옴표 없이 작성: `DB_PASSWORD=mypassword` (O) / `DB_PASSWORD = "mypassword"` (X)

## 추가 환경 변수

필요에 따라 `.env` 파일과 `application.properties`에 추가 변수를 정의할 수 있습니다:

```bash
# JWT 설정
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=86400000

# 외부 API
EXTERNAL_API_KEY=your_api_key
EXTERNAL_API_URL=https://api.example.com
```

해당 변수를 사용하려면 `application.properties`에 다음과 같이 추가:
```properties
jwt.secret=${JWT_SECRET:default_secret}
jwt.expiration=${JWT_EXPIRATION:86400000}
```

