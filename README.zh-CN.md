# Silicon Think（硅思）

[English](./README.md) | **简体中文**

个人站点：公开博客与作品集、作者写作台、JWT / GitHub OAuth — Vue 3 前端 + Spring Boot 后端，规格由 [OpenSpec](./openspec/) 驱动。

**仓库：** https://github.com/blise95/silliconthink

## 特性

- 公开博客列表 / 详情（Markdown）与作品集
- 认证：注册、登录、GitHub OAuth（JWT）
- 作者工作台：草稿、发布、分栏 Markdown 编辑与图片上传
- **存储分层：** MySQL 存**元数据**（标题、slug、状态、标签、`content_key`）；博客**正文与媒体**存可配置对象存储根（本机目录，或经 Tailscale 挂载的 NAS）

## 架构

```text
浏览器 → Nginx
           ├─ Vue 静态站
           ├─ /api/     → Spring Boot
           └─ /uploads/ → 媒体（反代或 alias）
                            │
               Spring Boot ─┼─ MySQL（元数据）
                            └─ BLOG_STORAGE_ROOT（NAS ≈ OSS，经 Tailscale）
                                 posts/*.md + media/*
```

## 技术栈

| 层 | 技术 |
|----|------|
| 前端 | Vue 3、Vite、TypeScript |
| 后端 | Java 17、Spring Boot 3、MyBatis-Plus、JWT |
| 数据 | MySQL 8（元数据）+ 文件系统 / NAS 对象存储 |
| 规格 | `openspec/` 下的 OpenSpec |

## 仓库结构

| 路径 | 说明 |
|------|------|
| `frontend/` | Vue 3 站点 |
| `backend/` | Spring Boot API |
| `openspec/` | 规格与变更提案 |
| `deploy/` | 前后端一并更新脚本 |

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
cp backend/src/main/resources/application-local.yml.example \
   backend/src/main/resources/application-local.yml
# 编辑数据源、JWT、OAuth — 勿提交真实密钥
cd backend && mvn spring-boot:run
```

默认种子管理员：`admin` / `Admin@123456` — **上线前务必修改。**

本地对象存储默认目录：`data/blog-storage`（`BLOG_STORAGE_ROOT`）。

认证与内容 API 说明见 [frontend/README.md](frontend/README.md)、[backend/README.md](backend/README.md)。

## 配置（敏感项）

| 变量 | 用途 | 说明 |
|------|------|------|
| `DB_*` / `JWT_SECRET` | 库与鉴权 | 仅服务器 `/etc/silliconthink/backend.env` |
| `BLOG_STORAGE_ROOT` | 对象存储根 | 生产：NAS 挂载如 `/mnt/nas-blog` |
| `UPLOAD_DIR` | 媒体目录 | 可选；默认 `{BLOG_STORAGE_ROOT}/media` |
| `BLOG_MIGRATE_CONTENT` | 一次性正文导出 | 设 `true` 跑一次后关闭 |

仓库内示例均为**占位符**。

## 部署

香草云等单节点，代码在 `/opt/silliconthink`，跟踪 GitHub `main`：

```bash
sudo bash /opt/silliconthink/deploy/update.sh
```

| 只发前端 | `sudo bash /opt/silliconthink/frontend/deploy/update.sh` |
| 只发后端 | `sudo bash /opt/silliconthink/backend/deploy/update.sh` |

**NAS 作为博客正文/媒体对象存储（推荐）：**

- 中文：[backend/deploy/nas-storage.zh-CN.md](backend/deploy/nas-storage.zh-CN.md)
- English：[backend/deploy/nas-storage.md](backend/deploy/nas-storage.md)

含 Tailscale 挂载、systemd、迁移、Nginx `/uploads` 与回滚。

## 安全

- **切勿提交**真实 `backend.env`、`application-local.yml`、OAuth 密钥、SMB `*.cred`、私人 Tailscale IP
- NAS 的 NFS/SMB **仅**经 Tailscale 可达，不对公网开放
- 上线前修改默认管理员密码
- 生产 env 建议 `chmod 640`（`root:www-data`）
- 若密钥泄露：立即轮换 JWT、数据库密码与 OAuth Client Secret

## 文档

- [前端 README](frontend/README.md)
- [后端 README](backend/README.md)
- [NAS 存储（中文）](backend/deploy/nas-storage.zh-CN.md) / [NAS storage (EN)](backend/deploy/nas-storage.md)
- [OpenSpec](openspec/)

## 许可

个人项目 — 许可证待定。欢迎通过 GitHub Issues/PR 改进文档与缺陷修复。
