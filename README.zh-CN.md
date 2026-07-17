# Silicon Think（硅思）

[English](./README.md) | **简体中文**

Silicon Think 是一套个人站点方案：公开博客与作品集、作者写作台、账号认证（密码登录 + GitHub OAuth）。技术栈为 Vue 3 前端与 Spring Boot API，规格变更由 OpenSpec 管理。

| | |
|---|---|
| **仓库** | https://github.com/blise95/silliconthink |
| **运行时** | Java 17 · Node.js 18+ · MySQL 8 |
| **典型部署** | Nginx（静态站点 + 反代）· systemd 托管后端 |

---

## 目录

1. [概述](#概述)
2. [架构](#架构)
3. [仓库结构](#仓库结构)
4. [环境要求](#环境要求)
5. [本地快速开始](#本地快速开始)
6. [配置参考](#配置参考)
7. [存储模型与路径](#存储模型与路径)
8. [生产部署](#生产部署)
9. [数据库](#数据库)
10. [安全](#安全)
11. [运维检查清单](#运维检查清单)
12. [故障排查](#故障排查)
13. [许可](#许可)

---

## 概述

### 功能

- 公开博客（列表、标签、搜索、Markdown 详情）与作品集
- 注册 / 登录与 GitHub OAuth（JWT）
- 作者工作台：草稿、发布 / 取消发布、Markdown 编辑、图片上传
- 数据分层：
  - **MySQL** — 元数据（标题、slug、摘要、状态、标签、`content_key` 等）
  - **文件系统对象存储** — Markdown 正文与上传媒体（默认本机目录；可选 NAS 挂载）

### 原则

- 对前端 API 保持稳定：仍收发 `contentMd`，对象键仅存在服务端
- 密钥不进 Git，仓库只保留 `.example` 模板
- **唯一权威说明文档**为本 README（及英文版）；`*/deploy/` 下脚本为实施辅助，不以子目录 README 为准

---

## 架构

```text
                         公网
                          │
                          ▼
                     ┌─────────────┐
                     │    Nginx    │
                     │  (TLS/HTTP) │
                     └──────┬──────┘
            ┌───────────────┼───────────────┐
            │               │               │
            ▼               ▼               ▼
       静态 SPA         /api/*           /uploads/*
       (Vue 构建产物)  Spring Boot      （媒体文件）
                            │
              ┌─────────────┴─────────────┐
              ▼                           ▼
           MySQL 8                 BLOG_STORAGE_ROOT
          （仅元数据）              posts/ + media/
```

| 职责 | 组件 |
|------|------|
| 界面 | `frontend/` — Vue 3 + Vite + TypeScript |
| API | `backend/` — Spring Boot 3 + MyBatis-Plus + Security/JWT |
| 元数据 | MySQL 8 |
| 正文与图片 | `BLOG_STORAGE_ROOT` 下的目录树 |
| 规格 | `openspec/` |

对象键约定：

```text
{BLOG_STORAGE_ROOT}/
├── posts/{authorId}/{postId}.md    # 文章 Markdown（UTF-8）
└── media/{yyyy}/{MM}/{dd}/{uuid}.{ext}
```

`posts/`、`media/` **无需手工创建**，首次写入时由应用自动建目录；只需准备好**存储根目录**并赋权。

---

## 仓库结构

```text
silliconthink/
├── README.md / README.zh-CN.md     # ← 工程唯一权威文档
├── deploy/                         # 前后端一并更新入口
├── frontend/                       # Vue SPA + Nginx 示例
├── backend/                        # Spring Boot + systemd 示例
└── openspec/                       # OpenSpec 提案与规格
```

| 路径 | 作用 |
|------|------|
| `frontend/deploy/` | 前端构建发布脚本、`nginx.conf.example` |
| `backend/deploy/` | `server-setup.sh`、`update.sh`、`backend.env.example`、systemd unit |
| `backend/src/main/resources/db/` | `schema.sql`、`data.sql`、`migration/` |
| `openspec/changes/` | 变更提案（设计 / 任务） |

---

## 环境要求

| 软件 | 版本 |
|------|------|
| JDK | 17+ |
| Maven | 3.9+ |
| Node.js / npm | 18+ / 9+ |
| MySQL | 8.0+ |
| Nginx | （生产） |
| systemd | （Linux 生产） |

---

## 本地快速开始

### 前端

```bash
cd frontend
npm install
cp .env.example .env    # 可选，见「配置参考」
npm run dev
```

浏览器打开 http://localhost:5173

### 后端

```bash
mysql -uroot -p < backend/src/main/resources/db/schema.sql
mysql -uroot -p < backend/src/main/resources/db/data.sql

cp backend/src/main/resources/application-local.yml.example \
   backend/src/main/resources/application-local.yml
# 编辑数据源、JWT、OAuth — 勿提交该文件

cd backend
export JAVA_HOME=$(/usr/libexec/java_home -v 17)   # macOS 示例
mvn spring-boot:run
```

健康检查：`GET http://localhost:8080/api/v1/health`

**种子管理员（对公网开放前必须改密）：**

| 用户名 | 密码 |
|--------|------|
| `admin` | `Admin@123456` |

本地对象存储默认：`data/blog-storage`（相对进程工作目录），可用 `BLOG_STORAGE_ROOT` 覆盖。

### 常用前端本地 `.env`

```env
VITE_USE_MOCK=false
VITE_AUTH_USE_API=true
VITE_API_BASE_URL=http://localhost:8080
```

---

## 配置参考

### 前端（`frontend/.env` / `.env.production`）

| 变量 | 默认 | 说明 |
|------|------|------|
| `VITE_USE_MOCK` | `true` | `true` 时内容走 Mock |
| `VITE_AUTH_USE_API` | `false` | `true` 时认证走真实 API |
| `VITE_API_BASE_URL` | `http://localhost:8080` | API 根地址；生产可留空走同源 `/api` |
| `VITE_SITE_URL` | `http://localhost:5173` | 站点对外 URL（SEO 等） |

### 后端环境变量（生产：`/etc/silliconthink/backend.env`）

从 `backend/deploy/backend.env.example` 复制。**切勿提交填好密钥的 env。**

| 变量 | 必需 | 说明 |
|------|------|------|
| `DB_HOST` | 是 | MySQL 主机 |
| `DB_PORT` | 否 | 默认 `3306` |
| `DB_NAME` | 是 | 库名 |
| `DB_USERNAME` | 是 | 用户名 |
| `DB_PASSWORD` | 是 | 密码 |
| `JWT_SECRET` | 是 | 至少 32 位随机串 |
| `JWT_EXPIRE_SECONDS` | 否 | 默认 `7200` |
| `LOG_PATH` | 否 | 日志目录；文件为 `${LOG_PATH}/backend.log` |
| `BLOG_STORAGE_ROOT` | 建议 | 对象存储根目录（绝对路径） |
| `UPLOAD_DIR` | 否 | 媒体目录；**默认** `{BLOG_STORAGE_ROOT}/media` |
| `BLOG_MIGRATE_CONTENT` | 否 | 一次性把旧 `content_md` 导出为文件，用完改回 `false` |
| `SERVER_PORT` | 否 | 默认 `8080` |
| `OAUTH_FRONTEND_CALLBACK` | OAuth | 如 `https://你的域名/oauth/callback` |
| `GITHUB_CLIENT_ID` / `GITHUB_CLIENT_SECRET` | OAuth | GitHub OAuth App |
| `GITHUB_REDIRECT_URI` | OAuth | 后端回调地址 |

Spring 配置对应关系：

| 配置项 | 环境变量 |
|--------|----------|
| `app.storage.root` | `BLOG_STORAGE_ROOT` |
| `app.upload.dir` | `UPLOAD_DIR` |
| `app.storage.migrate-on-startup` | `BLOG_MIGRATE_CONTENT` |

---

## 存储模型与路径

### 数据放哪

| 数据 | 位置 |
|------|------|
| 标题、slug、摘要、状态、标签、作者、`content_key`、时间戳 | MySQL `blog_*` 表 |
| 文章 Markdown 正文 | `{BLOG_STORAGE_ROOT}/posts/...` |
| 上传图片 | `{BLOG_STORAGE_ROOT}/media/...`（对外 URL 前缀 `/uploads/...`） |

### 生产推荐（香草云等 VPS 本机盘）

```bash
sudo mkdir -p /var/lib/silliconthink/blog-storage
sudo chown -R www-data:www-data /var/lib/silliconthink/blog-storage
sudo chmod 750 /var/lib/silliconthink/blog-storage
```

`/etc/silliconthink/backend.env`：

```bash
BLOG_STORAGE_ROOT=/var/lib/silliconthink/blog-storage
```

### 可选：NAS 作为对象存储根

若日后经 Tailscale 等私网挂载 NFS/SMB，将 `BLOG_STORAGE_ROOT` 指向挂载点即可。NFS/SMB **不要对公网开放**。示例 unit：`backend/deploy/mnt-nas-blog.mount`；补充说明：`backend/deploy/nas-storage.zh-CN.md`。

### 单机目录一览

| 路径 | 用途 |
|------|------|
| `/opt/silliconthink` | Git 工作副本 |
| `/opt/silliconthink-runtime/app.jar` | 运行中的后端 JAR |
| `/etc/silliconthink/backend.env` | 密钥与存储路径（`chmod 640`，`root:www-data`） |
| `/etc/silliconthink/application-prod.yml` | 可选额外 Spring 配置 |
| `/var/www/silliconthink` | 前端静态资源 |
| `/var/lib/silliconthink/blog-storage` | 博客正文 + 媒体 |
| `/var/log/silliconthink/backend.log` | 后端日志 |

---

## 生产部署

假设 Ubuntu 类主机，代码位于 `/opt/silliconthink`，跟踪 GitHub `main`。

### 1. 首次初始化

```bash
sudo mkdir -p /opt/silliconthink
sudo git clone https://github.com/blise95/silliconthink.git /opt/silliconthink

sudo bash /opt/silliconthink/frontend/deploy/server-setup.sh
sudo bash /opt/silliconthink/backend/deploy/server-setup.sh
```

### 2. 数据库

```bash
mysql -u root -p < /opt/silliconthink/backend/src/main/resources/db/schema.sql
mysql -u root -p < /opt/silliconthink/backend/src/main/resources/db/data.sql
# 已有库升级对象存储：
mysql -u ... -p silliconthink < \
  /opt/silliconthink/backend/src/main/resources/db/migration/2026-07-17-add-content-key.sql
```

### 3. 环境变量与存储目录

```bash
sudo cp /opt/silliconthink/backend/deploy/backend.env.example \
  /etc/silliconthink/backend.env
sudo vim /etc/silliconthink/backend.env
# 填写 DB_*、JWT_SECRET、BLOG_STORAGE_ROOT=/var/lib/silliconthink/blog-storage
sudo chown root:www-data /etc/silliconthink/backend.env
sudo chmod 640 /etc/silliconthink/backend.env

sudo mkdir -p /var/lib/silliconthink/blog-storage
sudo chown -R www-data:www-data /var/lib/silliconthink/blog-storage
```

从 `backend/deploy/silliconthink-backend.service` 安装/更新 systemd（按需改 `JAVA_HOME`）。**未使用 NAS 挂载时不要启用 `RequiresMountsFor=`。**

### 4. Nginx

参考 `frontend/deploy/nginx.conf.example`：

- `/` → 前端静态目录
- `/api/` → `http://127.0.0.1:8080`
- `/uploads/` → 反代后端（或 `alias` 到 `{BLOG_STORAGE_ROOT}/media/`）

```bash
sudo nginx -t && sudo systemctl reload nginx
```

### 5. 发布 / 更新

```bash
sudo bash /opt/silliconthink/deploy/update.sh

# 或分别更新
sudo bash /opt/silliconthink/frontend/deploy/update.sh
sudo bash /opt/silliconthink/backend/deploy/update.sh
```

### 6. 可选：一次性正文迁移

若表中仍有旧版 `content_md`：

```bash
# backend.env 中临时：
BLOG_MIGRATE_CONTENT=true
sudo systemctl restart silliconthink-backend
# 完成后改回 false 再重启
```

删除 `content_md` 列见 `db/migration/2026-07-17-drop-content-md-deferred.sql`（确认迁移成功后再执行）。

---

## 数据库

| 脚本 | 时机 |
|------|------|
| `db/schema.sql` | 全新安装 |
| `db/data.sql` | 种子管理员（务必改密） |
| `db/migration/2026-07-17-add-content-key.sql` | 已有库增加 `content_key` |
| `db/migration/2026-07-17-drop-content-md-deferred.sql` | 迁移验证后再删列 |

API 前缀：`/api/v1`。统一响应：`{ "code": 0, "message": "ok", "data": ... }`。

---

## 安全

1. **密钥** — 只提交 `*.example`；真实 `backend.env`、`application-local.yml`、OAuth 密钥仅存服务器（`chmod 640`）。
2. **默认管理员** — 上线前修改 `admin` / `Admin@123456`。
3. **对象存储** — 使用独立目录并由 `www-data` 拥有；勿随意放在未加保护的 Web 根下。
4. **网络** — 若使用 NAS，NFS/SMB 仅限私网（如 Tailscale），禁止对 `0.0.0.0/0` 开放。
5. **泄露处置** — 立即轮换 `JWT_SECRET`、数据库密码、GitHub OAuth Client Secret。

---

## 运维检查清单

部署后建议执行：

```bash
systemctl is-active silliconthink-backend

sudo -u www-data test -w /var/lib/silliconthink/blog-storage && echo OK

curl -sf 'http://127.0.0.1:8080/api/v1/posts?page=1' >/dev/null && echo OK

# 新建文章 / 上传图片后
sudo ls /var/lib/silliconthink/blog-storage/posts/
sudo ls /var/lib/silliconthink/blog-storage/media/
```

日志：`/var/log/silliconthink/backend.log`（可搜 `Blog object storage root OK`）。

---

## 故障排查

| 现象 | 可能原因 | 处理 |
|------|----------|------|
| `media storage unavailable` | 根目录不存在或 `www-data` 不可写 | `mkdir` + `chown www-data` |
| 详情正文缺失 | `content_key` 对应文件不存在 | 重新保存文章或跑迁移 |
| 浏览器 `/uploads/` 404 | Nginx 未配置 `/uploads/` | 按 `nginx.conf.example` 补齐 |
| 服务因挂载依赖起不来 | 残留 `RequiresMountsFor=` | 使用当前无 NAS 强制依赖的 unit |
| NFS `Permission denied` | NAS ACL / 客户端 IP 错误 | 修正 NFS 白名单（可选方案） |

---

## 许可

个人项目 — 许可证待定。欢迎通过 Issue / PR 改进文档与缺陷修复。
