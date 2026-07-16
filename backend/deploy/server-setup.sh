#!/usr/bin/env bash
# Ubuntu 后端首次部署（香草云单节点）
# 用法: sudo bash server-setup.sh
set -euo pipefail

APP_DIR="${APP_DIR:-/opt/silliconthink}"
JAR_DIR="${JAR_DIR:-/opt/silliconthink-runtime}"
CONF_DIR="${CONF_DIR:-/etc/silliconthink}"
LOG_DIR="${LOG_DIR:-/var/log/silliconthink}"
SERVICE_NAME="silliconthink-backend"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "==> 安装 JDK 17 / Maven..."
export DEBIAN_FRONTEND=noninteractive
apt-get update -qq
apt-get install -y openjdk-17-jdk maven curl

JAVA_HOME="$(dirname "$(dirname "$(readlink -f "$(which java)")")")"
# OpenJDK 包常见路径
if [[ -d /usr/lib/jvm/java-17-openjdk-amd64 ]]; then
  JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
elif [[ -d /usr/lib/jvm/java-17-openjdk-arm64 ]]; then
  JAVA_HOME=/usr/lib/jvm/java-17-openjdk-arm64
fi

echo "JAVA_HOME=$JAVA_HOME"

echo "==> 准备运行目录与配置..."
mkdir -p "$JAR_DIR" "$CONF_DIR" "$LOG_DIR"
chown -R www-data:www-data "$JAR_DIR" "$LOG_DIR"
chmod 755 "$CONF_DIR" "$LOG_DIR"
if [[ ! -f "$CONF_DIR/application-prod.yml" ]]; then
  cp "$SCRIPT_DIR/application-prod.yml.example" "$CONF_DIR/application-prod.yml"
  chown root:www-data "$CONF_DIR/application-prod.yml"
  chmod 640 "$CONF_DIR/application-prod.yml"
  echo "已生成 $CONF_DIR/application-prod.yml"
fi
if [[ ! -f "$CONF_DIR/backend.env" ]]; then
  cp "$SCRIPT_DIR/backend.env.example" "$CONF_DIR/backend.env"
  chown root:www-data "$CONF_DIR/backend.env"
  chmod 640 "$CONF_DIR/backend.env"
  echo "已生成 $CONF_DIR/backend.env ，请编辑 DB_NAME / DB_PASSWORD / JWT_SECRET 后再启动"
fi

echo "==> 安装 systemd 服务..."
sed "s|/usr/lib/jvm/java-17-openjdk-amd64|${JAVA_HOME}|g" \
  "$SCRIPT_DIR/silliconthink-backend.service" \
  > "/etc/systemd/system/${SERVICE_NAME}.service"

systemctl daemon-reload
systemctl enable "$SERVICE_NAME"

if [[ ! -d "$APP_DIR/backend" ]]; then
  echo "请先 clone 仓库到 $APP_DIR（可先跑 frontend/deploy/server-setup.sh）"
  exit 1
fi

echo "==> 首次构建并启动..."
APP_DIR="$APP_DIR" JAVA_HOME="$JAVA_HOME" bash "$SCRIPT_DIR/update.sh" --skip-git

echo ""
echo "=========================================="
echo "  后端部署完成"
echo "  配置 YAML: $CONF_DIR/application-prod.yml"
echo "  环境变量:  $CONF_DIR/backend.env   ← 库名/密码/LOG_PATH 写这里"
echo "  日志目录:  $LOG_DIR/backend.log"
echo "  服务名:    systemctl status $SERVICE_NAME"
echo "  后续更新:  sudo bash $APP_DIR/backend/deploy/update.sh"
echo "  全量更新:  sudo bash $APP_DIR/deploy/update.sh"
echo "=========================================="
echo "  记得:"
echo "  1) 导入 MySQL: schema.sql / data.sql"
echo "  2) 编辑 $CONF_DIR/backend.env"
echo "  3) Nginx 反代 /api/ （见 frontend/deploy/nginx.conf.example）"
echo "=========================================="
