#!/usr/bin/env bash
# Ubuntu 一键部署 — siliconthink.top
# 在服务器上: sudo bash server-setup.sh
set -euo pipefail

REPO_URL="${REPO_URL:-https://github.com/blise95/silliconthink.git}"
WEB_ROOT="${WEB_ROOT:-/var/www/silliconthink}"
APP_DIR="${APP_DIR:-/opt/silliconthink}"
DOMAIN="${DOMAIN:-siliconthink.top}"
WWW_DOMAIN="${WWW_DOMAIN:-www.siliconthink.top}"

echo "==> 安装依赖 (git, nginx, nodejs)..."
export DEBIAN_FRONTEND=noninteractive
apt-get update -qq
apt-get install -y git nginx curl rsync

if ! command -v node &>/dev/null; then
  curl -fsSL https://deb.nodesource.com/setup_20.x | bash -
  apt-get install -y nodejs
fi

echo "==> 拉取代码..."
if [ -d "$APP_DIR/.git" ]; then
  cd "$APP_DIR" && git pull
else
  rm -rf "$APP_DIR"
  git clone "$REPO_URL" "$APP_DIR"
fi

echo "==> 构建前端..."
cd "$APP_DIR/frontend"
npm ci
npm run build

echo "==> 部署静态文件到 $WEB_ROOT..."
mkdir -p "$WEB_ROOT"
rsync -a --delete dist/ "$WEB_ROOT/"
chown -R www-data:www-data "$WEB_ROOT"

echo "==> 配置 Nginx..."
cat > /etc/nginx/sites-available/silliconthink <<NGINX
server {
    listen 80;
    listen [::]:80;
    server_name ${DOMAIN} ${WWW_DOMAIN};

    root ${WEB_ROOT};
    index index.html;

    location /assets/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
        try_files \$uri =404;
    }

    location / {
        try_files \$uri \$uri/ /index.html;
    }

    gzip on;
    gzip_types text/plain text/css application/javascript application/json image/svg+xml;
    gzip_min_length 256;
}
NGINX

ln -sf /etc/nginx/sites-available/silliconthink /etc/nginx/sites-enabled/silliconthink
rm -f /etc/nginx/sites-enabled/default 2>/dev/null || true

nginx -t
systemctl enable nginx
systemctl reload nginx

echo ""
echo "=========================================="
echo "  部署完成！"
echo "  访问: http://${WWW_DOMAIN}"
echo "  网站目录: ${WEB_ROOT}"
echo "  源码目录: ${APP_DIR}"
echo ""
echo "  下一步（HTTPS，推荐）:"
echo "    apt install -y certbot python3-certbot-nginx"
echo "    certbot --nginx -d ${DOMAIN} -d ${WWW_DOMAIN}"
echo ""
echo "  后续更新:"
echo "    sudo bash ${APP_DIR}/frontend/deploy/update.sh"
echo "=========================================="
