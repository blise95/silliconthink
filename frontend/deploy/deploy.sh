#!/usr/bin/env bash
# 本地构建并上传到 Linux 服务器
# 用法: ./deploy/deploy.sh user@your-server
set -euo pipefail

REMOTE="${1:-}"
REMOTE_DIR="/var/www/silliconthink"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
FRONTEND_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

if [[ -z "$REMOTE" ]]; then
  echo "用法: $0 user@your-server"
  exit 1
fi

echo "==> 构建..."
cd "$FRONTEND_DIR"
npm ci
npm run build

echo "==> 上传到 $REMOTE:$REMOTE_DIR"
ssh "$REMOTE" "sudo mkdir -p $REMOTE_DIR && sudo chown \$(whoami):\$(whoami) $REMOTE_DIR"
rsync -avz --delete "$FRONTEND_DIR/dist/" "$REMOTE:$REMOTE_DIR/"

echo "==> 完成。请确保 Nginx root 指向 $REMOTE_DIR"
echo "    参考 deploy/nginx.conf.example 配置 SPA 路由回退"
