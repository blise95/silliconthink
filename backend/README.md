# Silicon Think — Backend

Java 17 + Spring Boot 3 + MySQL 8 + MyBatis-Plus. Audit fields: `create_date` / `update_date` / `create_by` / `update_by`.

[根 README（中文）](../README.zh-CN.md) · [Root README (EN)](../README.md)

## Storage model

| Data | Where |
|------|--------|
| Post metadata (title, slug, status, tags, `content_key`) | MySQL |
| Markdown body + uploaded images | Object store root (`BLOG_STORAGE_ROOT`) — local dir or NAS mount |

- [NAS storage (EN)](deploy/nas-storage.md)
- [NAS 存储（中文）](deploy/nas-storage.zh-CN.md)

Local default: `data/blog-storage`. Existing DBs: run `src/main/resources/db/migration/2026-07-17-add-content-key.sql`, then optionally `BLOG_MIGRATE_CONTENT=true` once.

## Requirements

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
# 编辑数据源、JWT secret、GitHub OAuth — 勿提交真实密钥
```

`application.yml` 已默认 `spring.profiles.active=local`。

可选：`export BLOG_STORAGE_ROOT=/path/to/store`

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

## 安全

- 生产密钥只放 `/etc/silliconthink/backend.env`，仓库仅保留 `deploy/backend.env.example`
- NAS/NFS 仅经 Tailscale；示例 mount 使用占位 IP
- 详见根 README Security 与 [nas-storage.zh-CN.md](deploy/nas-storage.zh-CN.md)

## 香草云单节点发布

### 首次

```bash
# 1) 仓库已在 /opt/silliconthink
# 2) MySQL 建库并导入 schema.sql / data.sql
sudo bash /opt/silliconthink/backend/deploy/server-setup.sh
sudo vim /etc/silliconthink/backend.env              # 改 DB_* / JWT_SECRET / BLOG_STORAGE_ROOT
sudo systemctl restart silliconthink-backend

# 3) Nginx 增加 /api/ 与 /uploads/ 反代（见 frontend/deploy/nginx.conf.example）
sudo nginx -t && sudo systemctl reload nginx
```

博客 NAS 挂载步骤：[deploy/nas-storage.zh-CN.md](deploy/nas-storage.zh-CN.md)

生产配置在 **`/etc/silliconthink/application-prod.yml`** + **`/etc/silliconthink/backend.env`**（不进 Git），jar 在 `/opt/silliconthink-runtime/app.jar`。

### 日志

生产**只写文件**，不写 journal。

| 环境 | 路径 |
|------|------|
| 生产 | `/var/log/silliconthink/backend.log`（`LOG_PATH` 可改） |
| 本地 | `backend/logs/backend.log`（同时打控制台） |

```bash
sudo tail -f /var/log/silliconthink/backend.log
```

### 日常更新

```bash
sudo bash /opt/silliconthink/backend/deploy/update.sh
# 或全量
sudo bash /opt/silliconthink/deploy/update.sh
```
