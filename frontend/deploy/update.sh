#!/usr/bin/env bash
# 服务器上更新站点（代码已 clone 到 /opt/silliconthink 时）
set -euo pipefail

APP_DIR="${APP_DIR:-/opt/silliconthink}"
WEB_ROOT="${WEB_ROOT:-/var/www/silliconthink}"

cd "$APP_DIR"
git pull

cd frontend
npm ci
npm run build

rsync -a --delete dist/ "$WEB_ROOT/"
chown -R www-data:www-data "$WEB_ROOT"

echo "更新完成: http://$(curl -s ifconfig.me 2>/dev/null || echo '你的IP')"
