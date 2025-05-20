#!/bin/bash
#권한이슈로 인해 sudo를 사용하여 실행. 별로 세련되지 않은 방식이긴 한데 root가 소유자가 되는 파일들 때문
echo "Kirini 반자동 배포 Script"
cd /home/ubuntu/kirini || exit 1
# Git 저장소에 있는 최신 main 브랜치를 강제 pull
echo "[STEP] Git 저장소에서 최신 main 브랜치 강제 pull"
sudo git fetch --all 
sudo git reset --hard origin/main
sudo git pull origin main || {echo "git 이슈 발생" >&2; exit 1;}
echo "[STEP] ROOT.war 파일 빌드"
#webapp 디렉토리로 이동
cd /home/ubuntu/kirini/src/main/webapp || exit 1
#기존 ROOT.war 파일 삭제 후 재빌드
sudo rm -rf /home/ubuntu/kirini/docker-settings/service1-tomcat/ROOT.war || {echo "jar 파일 삭제 실패 " >&2;}
sudo jar -cvf /home/ubuntu/kirini/docker-settings/service1-tomcat/ROOT.war * || {echo "jar 파일 생성 실패" >&2; exit 1;}
cd -

#docker-compose 강제 빌드 후 컨테이너 재실행
echo "[STEP] docker-compose 빌드"
sudo docker compose build --no-cache || {echo "docker compose 빌드 실패" >&2; exit 1;}
echo "[STEP] service1-tomcat 컨테이너 중지"
sudo docker compose stop service1-tomcat || {echo "service1-tomcat 컨테이너 중지 실패" >&2; exit 1;}
echo "[STEP] service1-tomcat 컨테이너 재시작"
sudo docker compose up -d service1-tomcat || {echo "service1-tomcat 컨테이너 시작 실패" >&2; exit 1;}
echo "[STEP] 배포 완료"
exit 0