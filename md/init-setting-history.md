# 초기 세팅 기록 (한국어)

작성일: 2026-01-02
작성자: 자동 생성(assistant)

목표
- 로컬 개발 환경에서는 H2를 사용하고, devtest(개발 배포서버)와 prod(실서버) 환경에서는 MySQL을 사용하도록 설정.
- Spring Boot가 Docker Compose에서 노출한 두 개의 데이터베이스(JdbcConnectionDetails)를 자동으로 인식해 발생한 빈 모호성 문제를 해결.

진행한 작업(요약)
1. Docker Compose 정리
   - `compose.yaml`에서 Postgres 서비스를 제거하고 MySQL 서비스만 남겼습니다.
   - MySQL의 포트 매핑을 명시적으로 `3306:3306`으로 설정하여 호스트에서 접근 가능하도록 했습니다.

2. 애플리케이션 프로파일/설정 정리
   - `src/main/resources/application.properties`를 로컬(H2) 기본 설정으로 변경했습니다. 한국어 주석을 추가하여 목적을 명시했습니다.
   - `src/main/resources/application-devtest.properties` 파일을 생성해 devtest 환경에서 사용할 MySQL 연결 정보를 추가했습니다. (예시 값: localhost:3306, myuser/secret)
   - `src/main/resources/application-prod.properties` 파일을 생성해 prod 환경용 MySQL 설정 템플릿을 추가했습니다. 비밀번호는 환경변수 `DB_PASSWORD`로 주입하도록 템플릿화했습니다.

3. 빌드 설정 및 인코딩
   - `build.gradle`에 `runtimeOnly 'com.h2database:h2'`를 추가하여 H2 드라이버를 런타임에서 사용할 수 있게 했습니다.
   - 한글 주석이 포함된 properties 파일을 안전히 처리하기 위해 Gradle 컴파일 및 리소스 인코딩을 UTF-8로 설정했습니다 (`options.encoding = 'UTF-8'`, `filteringCharset = 'UTF-8'`).
   - 프로젝트 루트에 `.editorconfig`를 추가해 에디터에서 properties 파일을 UTF-8로 취급하도록 힌트를 추가했습니다.

4. 기타
   - `application-devtest.properties`와 `application-prod.properties`는 배포 환경에 맞게 실제 호스트명/포트/계정 정보를 수정해야 합니다.
   - 운영 환경에서는 비밀번호를 절대 평문으로 저장하지 말고, 환경변수 또는 시크릿 매니저를 사용하도록 권장합니다.


## Legacy 코드 보관 및 마이그레이션 계획
- 기존 바닐라 Servlet 기반 프로젝트는 `legacy/` 폴더에 백업해 두었습니다. (예: JSP, `web.xml`, 서블릿 클래스, 초기 SQL 스크립트, 정적 리소스 등 포함)
- 마이그레이션 방향: API(엔드포인트) 구조는 가능한 한 유지하되, API 외의 전체 동작(뷰, 서블릿 기반 로직, 포워딩/필터 동작 등)은 Spring Boot 기반의 컨트롤러/서비스/레포지토리 계층으로 재구현할 예정입니다.
- 구체 작업 예시:
  - JSP/서블릿 기반 뷰는 필요 시 Thymeleaf로 전환하거나, 프런트엔드를 별도 분리합니다.
  - `web.xml`의 서블릿/필터 설정은 Spring의 `@Bean`/`FilterRegistrationBean` 또는 Spring Security 설정으로 이전합니다.
  - 비즈니스 로직은 서비스 계층으로 추출하고, DB 접근은 JPA 리포지토리로 재구성합니다.
  - 기존 SQL/스키마는 Flyway 또는 Liquibase 등 마이그레이션 도구로 정리하여 배포 파이프라인에서 관리합니다.
  - 파일 업로드/세션/인증 방식 등 서블릿 특화 코드는 Spring의 표준 방식으로 재설계합니다.
- 테스트 및 검증:
  - 레거시 기능을 재현하는 통합 테스트와 회귀 테스트를 작성해 기능 보존을 검증합니다.
  - 로컬에서는 H2로 빠르게 반복 테스트하고, devtest에서 MySQL 기반 통합 테스트로 프로덕션 유사 환경을 검증합니다.
- 위험 요소 및 주의사항:
  - 레거시에서 사용하던 서블릿 전용 API(예: RequestDispatcher 포워딩, javax.servlet 특정 기능 등)와 톰캣/서버 종속 코드가 재설계의 주요 원인이 될 수 있습니다.
  - 데이터 타입/인코딩/문자셋 차이로 인한 마이그레이션 오류 가능성(특히 문자열/날짜/타입 변환)을 주의해야 합니다.

문제 및 주의사항
- 원래 프로젝트에 `org.postgresql:postgresql` 의존성이 남아 있어 개발 중 Postgres가 자동 감지되었을 가능성이 있습니다. 의존성이 있더라도 compose 파일에서 Postgres를 제거하면 자동으로 JdbcConnectionDetails가 생성되지 않습니다.
- DevTools의 Docker Compose 통합 기능은 `compose.yaml`을 읽어 자동으로 컨테이너를 관리합니다. 이 기능 때문에 Docker Compose 파일에 Postgres가 있으면 Spring Boot 실행 시 Postgres용 커넥션 정보가 자동으로 등록될 수 있습니다.

검증 방법
1. 로컬(H2):
   - `.
   gradlew.bat bootRun` (프로파일 지정 없이 시작) — H2 초기화 로그 및 애플리케이션 기동 확인
2. devtest(MySQL):
   - `docker compose -f .\compose.yaml up -d` — MySQL 컨테이너가 올라오는지 확인
   - `.
   gradlew.bat bootRun --args="--spring.profiles.active=devtest"` — MySQL 접속 성공 로그 확인
3. prod(MySQL):
   - `set DB_PASSWORD=실DB비밀번호` (Windows) 또는 PowerShell에서 `$env:DB_PASSWORD='...'`
   - `.
   gradlew.bat bootRun --args="--spring.profiles.active=prod"`

추가 권장 작업
- `org.postgresql:postgresql` 의존성 제거(프로젝트에서 PostgreSQL을 더 이상 사용하지 않는다면)로 의존성 경량화
- `@Primary`로 명시적 DataSource 빈을 등록하거나 DataSource 자동설정을 제외하고 수동으로 DataSource 빈을 등록하는 경우를 대비한 샘플 코드 추가
- README에 실행 가이드(프로파일별 실행 명령)를 추가


## 의사결정 히스토리
- 배경: 기존 레거시 프로젝트는 MySQL 8.4.4를 사용하고 있었습니다. 이력과 호환성 때문에 초기 세팅에서 MySQL을 포함시키는 방향이 우선 고려되었습니다.
- Postgres 포함 이유: 장기적으로 PostgreSQL로의 마이그레이션 가능성을 검토하고 있었기 때문에 초기 세팅 패키지에 PostgreSQL 드라이버 및 관련 설정을 잠시 포함했습니다.
- 충돌 발생: Docker Compose와 Spring DevTools의 통합으로 인해 Compose 파일에 정의된 DB 서비스들이 자동으로 감지되면서 `JdbcConnectionDetails` 타입 빈이 두 개 생성되는 상황(예: MySQL과 Postgres 둘 다 감지)으로 인해 애플리케이션 기동 시 빈 모호성 에러가 발생했습니다.
- 의사결정: 당장의 서비스 안정성과 레거시 호환성을 우선시하여 MySQL을 유지하고, PostgreSQL은 당장은 제거(또는 설치 패키지에서 제외)하는 쪽으로 결정했습니다. Postgres 관련 의존성은 필요 시 나중에 다시 추가할 수 있도록 기록해 두었습니다.
- 로컬 개발 전략: 개발 생산성·속도와 반복 테스트의 편의성을 위해 로컬에서는 인메모리 DB인 H2를 사용하도록 기본 프로파일을 설정했습니다. 로컬에서의 빠른 피드백 주기와 단위 개발에 적합합니다.
- 배포 전략: devtest와 prod 환경에서는 환경변수 또는 프로파일에 따라 MySQL로 동작하도록 구성했습니다. 이는 실제 운영 DB와의 호환성 보장 및 데이터 이관의 안전성을 고려한 결정입니다.
- JPA 사용 고려: 개발은 JPA를 사용하므로 데이터베이스별 다소의 방언 차이는 Hibernate의 다이얼렉트 설정으로 조정 가능합니다. 다만 복잡한 SQL(특히 DB 특정 함수·타입·인덱스/쿼리 힌트)을 사용하면 마이그레이션 시 추가 작업이 필요합니다.

### 추가 검토 항목(권장)
- Postgres로의 향후 마이그레이션 계획이 확정되면 다음 작업을 고려하세요:
  - `org.postgresql:postgresql` 의존성을 명확한 프로파일(예: `postgres` 프로파일)에서만 로드하거나, 빌드 스크립트/배포 스크립트에서 필요 시 포함하도록 변경.
  - 데이터 이관 전략(데이터 형식, 문자셋, 인덱스/제약조건, 시퀀스/auto-increment 매핑 등) 수립.
  - 마이그레이션을 위한 통합 테스트(데이터 유효성, 성능 회귀) 수행.
- 개발/테스트 검증
  - devtest 환경에서 MySQL로 애플리케이션을 충분히 테스트하여 JPA 설정과 쿼리 호환성 문제를 사전에 발견하세요.
  - 로컬에서 H2 사용 시, H2와 MySQL의 차이로 인해 통과하는 테스트가 prod에서 실패할 수 있으므로 CI 파이프라인에 MySQL 기반 통합 테스트를 추가하는 것을 권장합니다.
- 보안/운영
  - 운영 비밀번호는 시크릿 매니저 또는 환경변수로 관리하고, 저장소에 평문으로 올리지 마세요.
  - 프로덕션에서의 DB 설정(커넥션 풀, 타임아웃, SSL, 모니터링)을 devtest와 분리하여 운영 환경 최적화를 진행하세요.

끝.
