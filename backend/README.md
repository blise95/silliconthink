# Silicon Think — Backend

Java 17 + Spring Boot 3 + MySQL 8 + MyBatis-Plus。统一审计字段：`create_date` / `update_date` / `create_by` / `update_by`。

## 要求

- JDK 17+
- Maven 3.9+
- MySQL 8.0

## 初始化数据库

```bash
mysql -uroot -p < src/main/resources/db/schema.sql
mysql -uroot -p < src/main/resources/db/data.sql
```

默认管理员（**上线前必须改密**）：

| 用户名 | 密码 |
|--------|------|
| `admin` | `Admin@123456` |

## 本地配置

```bash
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
# 编辑数据源、JWT secret、GitHub OAuth
```

`application.yml` 已默认 `spring.profiles.active=local`。

### GitHub OAuth App

1. GitHub → Settings → Developer settings → OAuth Apps → New
2. Authorization callback URL 填：`http://localhost:8080/api/v1/auth/oauth/github/callback`
3. 将 Client ID / Secret 写入 `application-local.yml` 的 `app.oauth.github.*`
4. `app.oauth.frontend-callback-url` 设为：`http://localhost:5173/oauth/callback`

## 启动

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
mvn spring-boot:run
```

健康检查：`GET http://localhost:8080/api/v1/health`

## 主要 API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/auth/register` | 注册并返回 JWT |
| POST | `/api/v1/auth/login` | 登录 |
| GET | `/api/v1/auth/me` | 当前用户（需 Bearer） |
| POST | `/api/v1/auth/logout` | 登出（客户端丢弃 token） |
| GET | `/api/v1/auth/oauth/github/authorize` | 跳转 GitHub 授权 |
| GET | `/api/v1/auth/oauth/github/callback` | GitHub 回调 → 重定向前端 + 一次性 code |
| POST | `/api/v1/auth/oauth/exchange` | 一次性 code 换 JWT |

统一响应：`{ "code": 0, "message": "ok", "data": ... }`（`code=0` 成功）。

## 香草云单节点发布

### 首次

```bash
# 1) 仓库已在 /opt/silliconthink（可用 frontend/deploy/server-setup.sh 先装前端）
# 2) MySQL 建库并导入 schema.sql / data.sql
sudo bash /opt/silliconthink/backend/deploy/server-setup.sh
sudo vim /etc/silliconthink/backend.env              # 改 DB_NAME / DB_PASSWORD / JWT_SECRET
sudo systemctl restart silliconthink-backend

# 3) Nginx 增加 /api/ 反代（见 frontend/deploy/nginx.conf.example）后
sudo nginx -t && sudo systemctl reload nginx
```

生产配置在 **`/etc/silliconthink/application-prod.yml`** + **`/etc/silliconthink/backend.env`**（库名/密码/JWT 用环境变量，不进 Git），jar 在 `/opt/silliconthink-runtime/app.jar`，进程由 systemd 托管。

GitHub OAuth 回调请改为线上地址，例如：

`https://siliconthink.top/api/v1/auth/oauth/github/callback`

前端 `.env.production` 建议：

```env
VITE_API_BASE_URL=
VITE_AUTH_USE_API=true
```

（`VITE_API_BASE_URL` 留空则同源走 Nginx `/api`。）

### 日常一行更新（仅后端）

```bash
sudo bash /opt/silliconthink/backend/deploy/update.sh
```

### 前后端一起更新

```bash
sudo bash /opt/silliconthink/deploy/update.sh
```
