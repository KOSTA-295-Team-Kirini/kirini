#!/bin/bash
# 이 스크립트는 초기 발행 시 딱 한번만 실행하면 된다.

docker run --rm \
  -v "$(pwd)/certbot/conf:/etc/letsencrypt" \
  -v "$(pwd)/certbot/www:/var/www/html" \
  certbot/certbot certonly \
  --webroot -w /var/www/html \
  -d kirini.info -d www.kirini.info \
  --email earlydreamer@naver.com \
  --agree-tos \
  --no-eff-email