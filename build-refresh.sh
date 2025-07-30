#!/bin/bash

echo "Kirini 반자동 배포 Script"

cd /volume1/docker/kirini || exit 1

echo "[STEP] Git 저장소에서 최신 main 브랜치 강제 pull"
sudo git fetch origin
sudo git reset --hard origin/main
sudo git pull origin main || {
  echo "git 이슈 발생" >&2
  exit 1
}

echo "[STEP] 빌드 스크립트 실행"
sudo bash ./build-war.sh || {
  echo "WAR 빌드 실패" >&2
  exit 1
}

echo "[STEP] docker-compose 빌드"
sudo docker compose build --no-cache || {
  echo "docker compose 빌드 실패" >&2
  exit 1
}

echo "[STEP] service1-tomcat 컨테이너 중지"
sudo docker compose stop service1-tomcat || {
  echo "service1-tomcat 컨테이너 중지 실패" >&2
  exit 1
}

echo "[STEP] service1-tomcat 컨테이너 재시작"
sudo docker compose up -d service1-tomcat || {
  echo "service1-tomcat 컨테이너 시작 실패" >&2
  exit 1
}

echo "[STEP] 배포 완료"
exit 0
