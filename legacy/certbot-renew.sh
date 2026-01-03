#!/bin/bash
cd /home/ubuntu/kirini || exit 1
# docker-compose 명령 실행으로 컨테이너 up
# docker-compose.yml에 컨테이너 실행과 동시에 renew 명령을 실행하도록 정의되어 있음
echo "[STEP] Certbot 인증서 갱신 시도"
sudo docker compose up -d certbot
#HAProxy 컨테이너를 재시작하여 갱신된 인증서를 적용
echo "[STEP] HAProxy 재시작"
sudo docker compose restart haproxy
echo "[DONE] 갱신 및 재시작 완료!"
