#!/usr/bin/env bash
# 香草云单节点：拉取 GitHub main，发布前端 + 后端
# 用法（服务器）: sudo bash /opt/silliconthink/deploy/update.sh
set -euo pipefail

APP_DIR="${APP_DIR:-/opt/silliconthink}"
BRANCH="${BRANCH:-main}"

cd "$APP_DIR"
git fetch origin
git checkout "$BRANCH"
git reset --hard "origin/$BRANCH"

echo "==> 发布前端..."
bash "$APP_DIR/frontend/deploy/update.sh" --skip-git

echo "==> 发布后端..."
bash "$APP_DIR/backend/deploy/update.sh" --skip-git

echo "==> 全部完成"
