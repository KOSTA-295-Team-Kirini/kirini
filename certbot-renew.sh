
cd /home/ubuntu/kirini || exit 1

echo "[STEP] Certbot 인증서 갱신 시도"
sudo docker exec certbot certbot renew --webroot -w /var/www/html

echo "[STEP] HAProxy 재시작"
sudo docker compose restart haproxy
echo "[DONE] 갱신 및 재시작 완료!"

