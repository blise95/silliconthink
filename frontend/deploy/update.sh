#!/usr/bin/env bash
# 服务器上更新前端站点（代码已 clone 到 /opt/silliconthink 时）
# 用法: sudo bash /opt/silliconthink/frontend/deploy/update.sh
# 选项: --skip-git  不拉取代码
set -euo pipefail

APP_DIR="${APP_DIR:-/opt/silliconthink}"
WEB_ROOT="${WEB_ROOT:-/var/www/silliconthink}"

SKIP_GIT=0
for arg in "$@"; do
  case "$arg" in
    --skip-git) SKIP_GIT=1 ;;
  esac
done

cd "$APP_DIR"
if [[ "$SKIP_GIT" -eq 0 ]]; then
  git fetch origin
  git checkout main
  git reset --hard origin/main
fi

cd frontend
npm ci
npm run build

mkdir -p "$WEB_ROOT"
rsync -a --delete dist/ "$WEB_ROOT/"
chown -R www-data:www-data "$WEB_ROOT"

echo "前端更新完成"
