#!/bin/bash
set -e  # 실패 시 즉시 종료
echo "[KIRINI] ROOT.war 파일 빌드 스크립트"
# 디렉토리 설정
PROJECT_DIR="/home/ubuntu/kirini"
JAVA_SRC="$PROJECT_DIR/src/main/java"
WEBAPP_SRC="$PROJECT_DIR/src/main/webapp"
BUILD_CLASSES="$PROJECT_DIR/build/classes"
LIB_DIR="$PROJECT_DIR/src/main/webapp/WEB-INF/lib"
WAR_OUT="$PROJECT_DIR/docker-settings/service1-tomcat/ROOT.war"

# 1. 기존 클래스, WAR 삭제
echo "[STEP] 기존 빌드 정리"
rm -rf "$BUILD_CLASSES" "$WAR_OUT"
mkdir -p "$BUILD_CLASSES"

# 2. Java 소스 컴파일
echo "[STEP] Java 소스 컴파일 중..."
find "$JAVA_SRC" -name "*.java" > sources.txt

# 라이브러리 클래스패스 구성
LIB_CP=$(find "$LIB_DIR" -name "*.jar" | tr '\n' ':')

# 컴파일
javac -encoding UTF-8 -cp "$LIB_CP" -d "$BUILD_CLASSES" @sources.txt
rm sources.txt

# 3. WAR용 임시 디렉토리 준비
echo "[STEP] WAR 구조 구성 중..."
TEMP_WAR_DIR="$PROJECT_DIR/build/war-temp"
rm -rf "$TEMP_WAR_DIR"
mkdir -p "$TEMP_WAR_DIR"

# 4. webapp 복사
cp -r "$WEBAPP_SRC"/* "$TEMP_WAR_DIR"

# 5. 컴파일된 클래스 포함
mkdir -p "$TEMP_WAR_DIR/WEB-INF/classes"
cp -r "$BUILD_CLASSES"/* "$TEMP_WAR_DIR/WEB-INF/classes/"

# 6. 라이브러리 JAR 포함
mkdir -p "$TEMP_WAR_DIR/WEB-INF/lib"
cp "$LIB_DIR"/*.jar "$TEMP_WAR_DIR/WEB-INF/lib/" || echo "※ lib 디렉토리에 jar 없음 (무시)"

# 7. WAR 생성
echo "[STEP] WAR 생성 중..."
cd "$TEMP_WAR_DIR"
jar -cvf "$WAR_OUT" *
cd -

echo "빌드 완료: $WAR_OUT"
exit 0