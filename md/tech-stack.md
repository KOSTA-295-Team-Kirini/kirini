# 기술 스택 (Tech Stack)

프로젝트: kirini-spring
작성일: 2026-01-02

## 언어
- Java 25 (Java Language Version: 25, toolchain 설정)

## 프레임워크 / 라이브러리
- Spring Boot 4.0.1
  - Spring WebMVC
  - Spring Data JPA
  - Spring Boot DevTools
  - Spring Boot Docker Compose integration (개발 편의)
- Hibernate (JPA 구현체)
- HikariCP (커넥션 풀)

## 빌드 도구
- Gradle (Gradle Wrapper 포함)
  - build.gradle에 toolchain, 의존성 등 정의

## 데이터베이스
- 로컬 개발: H2 (인메모리)
- 개발 배포서버(devtest): MySQL
- 운영(prod): MySQL
- (참고로 프로젝트에 PostgreSQL 드라이버 의존성이 존재했으나, 런타임 다중 JdbcConnectionDetails 문제로 Postgres 관련 설정은 제거/비활성화됨)

## 드라이버/런타임 의존성
- com.mysql:mysql-connector-j (MySQL)
- com.h2database:h2 (H2, 로컬)
- org.postgresql:postgresql (기존 의존성 — 필요 없으면 제거 가능)
- lombok (컴파일 어노테이션 프로세서)

## 기타 도구 / 설정
- Docker / Docker Compose (로컬/devtest에서 DB 컨테이너 관리)
- IntelliJ IDEA (개발자 환경)
- .editorconfig (파일 인코딩 UTF-8 지정)

## 인코딩 / 로케일
- 프로젝트 리소스 및 컴파일 인코딩을 UTF-8로 고정 (build.gradle에 설정)
- properties 파일에 한글 주석을 사용함(UTF-8 필요)

## 주요 파일/위치
- 애플리케이션 설정
  - `src/main/resources/application.properties` (로컬 H2 기본)
  - `src/main/resources/application-devtest.properties` (devtest: MySQL)
  - `src/main/resources/application-prod.properties` (prod: MySQL, 비밀번호는 환경변수로 주입)
- Docker Compose
  - `compose.yaml` (프로젝트 루트) — Postgres 제거, MySQL 포트 명시(3306:3306)
- 빌드 설정
  - `build.gradle` (의존성 및 UTF-8 인코딩 설정)
- 프로젝트 루트
  - `.editorconfig` (properties 파일 charset = utf-8)

## 실행 요약
- 로컬(H2): `./gradlew.bat bootRun` (프로파일 지정 안 하면 기본 H2 사용)
- devtest(MySQL): `./gradlew.bat bootRun --args="--spring.profiles.active=devtest"` (또는 환경변수로 주입)
- prod(MySQL): 환경변수 `DB_PASSWORD`를 설정 후 `--spring.profiles.active=prod`로 실행

## 권장/주의사항
- 운영 환경 비밀번호는 환경변수/시크릿 매니저로 관리
- 필요 시 `org.postgresql:postgresql` 의존성을 제거하여 불필요한 드라이버 로딩 방지
- 멀티 데이터소스가 필요하지 않다면 Postgres 관련 자동검출/서비스를 제거하여 빈 충돌 방지

