#!/bin/bash
set -euo pipefail
echo "[KIRINI] ROOT.war 파일 빌드 스크립트"

# ---------------------------
# 경로/설정
# ---------------------------
PROJECT_DIR="/volume1/docker/kirini"
JAVA_SRC="$PROJECT_DIR/src/main/java"
WEBAPP_SRC="$PROJECT_DIR/src/main/webapp"
BUILD_DIR="$PROJECT_DIR/build"
BUILD_CLASSES="$BUILD_DIR/classes"
TEMP_WAR_DIR="$BUILD_DIR/war-temp"
LIB_DIR="$PROJECT_DIR/src/main/webapp/WEB-INF/lib"   # 런타임 포함용
LIB_COMPILE_DIR="$PROJECT_DIR/lib-compile"            # 컴파일 전용(예: jakarta.servlet-api)
WAR_OUT="$PROJECT_DIR/docker-settings/service1-tomcat/build/ROOT.war"

# 컴파일 타깃 릴리즈(정수만)
JAVAC_RELEASE="${JAVAC_RELEASE:-21}"
JAVAC_OPTS="${JAVAC_OPTS:--encoding UTF-8 -g}"
# ---------------------------

# 0) 호스트에 javac가 없으면 Docker JDK로 자기 자신 재실행(절대경로 보장)
if ! command -v javac >/dev/null 2>&1; then
  if [[ "${INSIDE_BUILDER:-0}" != "1" ]]; then
    echo "[INFO] 로컬에 javac가 없어 Docker 빌더 컨테이너로 실행합니다."

    UID_CUR=$(id -u)
    GID_CUR=$(id -g)

    # 스크립트 절대경로 계산
    DEFAULT_SCRIPT="$PROJECT_DIR/build-war.sh"
    if [[ -f "$DEFAULT_SCRIPT" ]]; then
      SCRIPT_ABS="$DEFAULT_SCRIPT"
    else
      case "$0" in
        /*) SCRIPT_ABS="$0" ;;
        *)  SCRIPT_ABS="$(pwd)/$0" ;;
      esac
    fi
    SCRIPT_DIR="$(cd "$(dirname "$SCRIPT_ABS")" && pwd)"
    SCRIPT_BASE="$(basename "$SCRIPT_ABS")"

    docker run --rm \
      -u "${UID_CUR}:${GID_CUR}" \
      -v "$PROJECT_DIR":"$PROJECT_DIR" \
      -v "$SCRIPT_DIR":"$SCRIPT_DIR" \
      -w "$SCRIPT_DIR" \
      -e INSIDE_BUILDER=1 \
      -e JAVAC_RELEASE="$JAVAC_RELEASE" \
      -e JAVAC_OPTS="$JAVAC_OPTS" \
      eclipse-temurin:21-jdk \
      bash -lc "chmod +x '$SCRIPT_BASE'; exec './$SCRIPT_BASE'"

    exit $?
  else
    echo "[WARN] 컨테이너 내부인데도 javac가 없습니다. JDK 이미지 확인 필요."
    exit 1
  fi
fi

# 1) 기존 빌드 정리
echo "[STEP] 기존 빌드 정리"
rm -rf "$BUILD_CLASSES" "$TEMP_WAR_DIR" "$WAR_OUT"
mkdir -p "$BUILD_CLASSES" "$TEMP_WAR_DIR"
mkdir -p "$(dirname "$WAR_OUT")"

# 2) Java 소스 컴파일 준비
echo "[STEP] Java 소스 컴파일 준비..."
SOURCE_LIST="$BUILD_DIR/sources.txt"
mkdir -p "$BUILD_DIR"
find "$JAVA_SRC" -type f -name "*.java" > "$SOURCE_LIST"
if [[ ! -s "$SOURCE_LIST" ]]; then
  echo "[ERROR] Java 소스를 찾지 못했습니다: $JAVA_SRC"
  exit 1
fi

# 2-1) 클래스패스 구성 (lib-compile + WEB-INF/lib)
echo "[STEP] 클래스패스 구성..."
LIB_CP=""
build_cp_from_dir() {
  local dir="$1"
  shopt -s nullglob
  for jar in "$dir"/*.jar; do
    if [[ -z "$LIB_CP" ]]; then
      LIB_CP="$jar"
    else
      LIB_CP="$LIB_CP:$jar"
    fi
  done
  shopt -u nullglob
}
[ -d "$LIB_COMPILE_DIR" ] && build_cp_from_dir "$LIB_COMPILE_DIR"
[ -d "$LIB_DIR" ] && build_cp_from_dir "$LIB_DIR"

# 2-2) 컴파일 실행
echo "[STEP] Java 컴파일 중..."
if [[ -n "$LIB_CP" ]]; then
  javac $JAVAC_OPTS --release "$JAVAC_RELEASE" -cp "$LIB_CP" -d "$BUILD_CLASSES" @"$SOURCE_LIST"
else
  javac $JAVAC_OPTS --release "$JAVAC_RELEASE" -d "$BUILD_CLASSES" @"$SOURCE_LIST"
fi
rm -f "$SOURCE_LIST"

# 3) WAR 구조 구성: webapp 복사
echo "[STEP] WAR 구조 구성 중..."
# 숨김파일 포함 전체 복사(. 포함)
cp -a "$WEBAPP_SRC"/. "$TEMP_WAR_DIR"/

# 4) 컴파일 산출물 포함
mkdir -p "$TEMP_WAR_DIR/WEB-INF/classes"
cp -a "$BUILD_CLASSES"/. "$TEMP_WAR_DIR/WEB-INF/classes/"

# 5) 라이브러리 JAR 포함 (jakarta.servlet-api*는 제외 → 컨테이너 제공)
mkdir -p "$TEMP_WAR_DIR/WEB-INF/lib"
shopt -s nullglob
copied_any=0
for jar in "$LIB_DIR"/*.jar; do
  base="$(basename "$jar")"
  case "$base" in
    jakarta.servlet-api*.jar)
      echo "※ 제외: $base (서블릿 API는 톰캣이 제공)"
      continue
      ;;
  esac
  cp -a "$jar" "$TEMP_WAR_DIR/WEB-INF/lib/"
  copied_any=1
done
shopt -u nullglob
if [[ "$copied_any" -eq 0 ]]; then
  echo "※ WEB-INF/lib에 복사할 jar 없음(무시)"
fi

# 6) WAR 생성
echo "[STEP] WAR 생성 중..."
(
  cd "$TEMP_WAR_DIR"
  jar -cf "$WAR_OUT" .
)
echo "빌드 완료: $WAR_OUT"
exit 0

