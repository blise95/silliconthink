#!/usr/bin/env bash
# 服务器上构建并重启后端（代码已在 APP_DIR）
# 用法: sudo bash /opt/silliconthink/backend/deploy/update.sh
# 选项: --skip-git  不拉取代码（由上层统一 pull 时用）
set -euo pipefail

APP_DIR="${APP_DIR:-/opt/silliconthink}"
BACKEND_DIR="$APP_DIR/backend"
JAR_DIR="${JAR_DIR:-/opt/silliconthink-runtime}"
LOG_DIR="${LOG_DIR:-/var/log/silliconthink}"
SERVICE_NAME="${SERVICE_NAME:-silliconthink-backend}"
JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-17-openjdk-amd64}"

SKIP_GIT=0
for arg in "$@"; do
  case "$arg" in
    --skip-git) SKIP_GIT=1 ;;
  esac
done

if [[ "$(id -u)" -ne 0 ]]; then
  echo "请使用 root 或 sudo 运行"
  exit 1
fi

if [[ ! -d "$BACKEND_DIR" ]]; then
  echo "找不到后端目录: $BACKEND_DIR"
  exit 1
fi

if [[ "$SKIP_GIT" -eq 0 ]]; then
  cd "$APP_DIR"
  git fetch origin
  git checkout main
  git reset --hard origin/main
fi

export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

if ! command -v java >/dev/null || ! java -version 2>&1 | grep -q '"17'; then
  echo "需要 JDK 17，当前 JAVA_HOME=$JAVA_HOME"
  exit 1
fi

if ! command -v mvn >/dev/null; then
  echo "未找到 mvn，请先执行 backend/deploy/server-setup.sh"
  exit 1
fi

echo "==> Maven 打包..."
cd "$BACKEND_DIR"
mvn -q -DskipTests package

JAR_SRC="$(ls -1 "$BACKEND_DIR"/target/silliconthink-backend-*.jar | grep -v '\.original$' | head -1)"
if [[ -z "$JAR_SRC" || ! -f "$JAR_SRC" ]]; then
  echo "未找到可执行 jar"
  exit 1
fi

mkdir -p "$JAR_DIR" "$LOG_DIR"
chown www-data:www-data "$LOG_DIR"
chmod 755 "$LOG_DIR"
cp -f "$JAR_SRC" "$JAR_DIR/app.jar"
chown www-data:www-data "$JAR_DIR/app.jar"
chmod 644 "$JAR_DIR/app.jar"

echo "==> 重启 $SERVICE_NAME ..."
systemctl restart "$SERVICE_NAME"
systemctl --no-pager --full status "$SERVICE_NAME" | head -20

echo "后端已发布。健康检查: curl -s http://127.0.0.1:8080/api/v1/health"
echo "日志文件: $LOG_DIR/backend.log"
