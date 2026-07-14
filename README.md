# silliconthink

个人网站项目 — Vue 3 前端 + Java 后端（登录/注册/OAuth）+ OpenSpec 规格驱动。

**仓库：** https://github.com/blise95/silliconthink

## 快速开始

### 前端

```bash
cd frontend
npm install
cp .env.example .env   # 可选
npm run dev
```

浏览器打开 http://localhost:5173

### 后端

```bash
# JDK 17 + MySQL 8
mysql -uroot -p < backend/src/main/resources/db/schema.sql
mysql -uroot -p < backend/src/main/resources/db/data.sql
cp backend/src/main/resources/application-local.yml.example backend/src/main/resources/application-local.yml
# 编辑数据源与 JWT / GitHub OAuth
cd backend
# 推荐 JAVA_HOME 指向 JDK 17
mvn spring-boot:run
```

默认管理员：`admin` / `Admin@123456`（**上线前务必修改**）。详情见 [backend/README.md](backend/README.md)。

### 认证联调

前端 `.env`：

```env
VITE_USE_MOCK=true
VITE_AUTH_USE_API=true
VITE_API_BASE_URL=http://localhost:8080
```

内容仍可用 Mock，登录/注册/GitHub OAuth 走后端。

## 目录

| 目录 | 说明 |
|------|------|
| `frontend/` | Vue 3 个人站点 |
| `backend/` | Spring Boot 后端（认证） |
| `openspec/` | OpenSpec 规格与变更 |

## 部署（香草云单节点）

代码在服务器 `/opt/silliconthink`，跟踪 GitHub `main`。

**一行发布前后端：**

```bash
sudo bash /opt/silliconthink/deploy/update.sh
```

| 只发前端 | `sudo bash /opt/silliconthink/frontend/deploy/update.sh` |
| 只发后端 | `sudo bash /opt/silliconthink/backend/deploy/update.sh` |

- 前端：构建静态文件 → `/var/www/silliconthink`
- 后端：`mvn package` → `/opt/silliconthink-runtime/app.jar` → `systemctl restart silliconthink-backend`
- 配置：`/etc/silliconthink/application-prod.yml`（首次用 `backend/deploy/server-setup.sh`）
- Nginx：静态站点 + `/api/` 反代到 `127.0.0.1:8080`

详见 [frontend/README.md](frontend/README.md)、[backend/README.md](backend/README.md)。
