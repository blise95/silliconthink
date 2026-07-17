# Silicon Think — 前端

Vue 3 个人站点。完整项目介绍见 [根 README（中文）](../README.zh-CN.md) / [Root README (EN)](../README.md)。

博客正文与图片由后端对象存储（可挂 NAS）提供；前端仍通过 API 收发 `contentMd` 与 `/uploads` URL。部署媒体反代见 [deploy/nginx.conf.example](deploy/nginx.conf.example)；NAS 方案见 [backend/deploy/nas-storage.zh-CN.md](../backend/deploy/nas-storage.zh-CN.md)。

## 环境要求

- Node.js 18+
- npm 9+

## 快速开始

```bash
cd frontend
npm install
cp .env.example .env   # 可选
npm run dev
```

浏览器打开 http://localhost:5173

## 环境变量

| 变量 | 说明 | 默认 |
|------|------|------|
| `VITE_USE_MOCK` | 内容数据是否使用 Mock | `true` |
| `VITE_SITE_URL` | 站点 URL（SEO 用） | `http://localhost:5173` |
| `VITE_API_BASE_URL` | 后端 API 根地址 | `http://localhost:8080` |
| `VITE_AUTH_USE_API` | 认证是否走真实后端（可与内容 Mock 并存） | `false` |

### 认证联调

1. 启动后端（见 `backend/README.md`），初始化 MySQL
2. 前端 `.env`：

```env
VITE_USE_MOCK=true
VITE_AUTH_USE_API=true
VITE_API_BASE_URL=http://localhost:8080
```

3. 打开 `/login` 或 `/register`；GitHub OAuth 需在后端配置 OAuth App，回调前端 `/oauth/callback`

Mock 认证账号：`demo` / `demo1234`（仅 `VITE_AUTH_USE_API=false` 时）。

## 安全提示

- 勿将含密钥的 `.env` / `.env.production` 提交到 Git
- 生产通过同源 Nginx 访问 `/api` 与 `/uploads`，避免在前端打包真实密钥

## 目录结构

```
src/
├── components/     # UI、布局、博客、项目组件
├── composables/    # 数据与页面逻辑（含 useAuth）
├── mocks/          # Mock 静态数据
├── pages/          # 路由页面（含登录/注册/OAuth 回调）
├── router/         # Vue Router
├── services/       # Repository 接口 + Mock/API 实现
├── styles/         # 设计令牌与全局样式
├── types/          # TypeScript 类型
└── utils/          # HTTP / Markdown 等工具
```

## Mock 数据

编辑以下文件即可更新内容：

- `src/mocks/site.ts` — 站点信息、关于页、技能、联系方式
- `src/mocks/posts.ts` — 博客文章（含 `draft` 测试过滤）
- `src/mocks/projects.ts` — 项目作品

页面通过 `services/` 下的 Repository 读取数据，不直接 import mock 文件。

## 构建

```bash
npm run build
npm run preview
```

产物在 `dist/` 目录，可直接作为静态站点部署。

## 部署到 Linux 服务器

本项目前端为 SPA，**Nginx 托管静态文件**。启用真实认证/博客 API 时需同时部署 `backend`。详见根 README 与 `backend/README.md`。

### 方式一：本地构建 + 上传（推荐）

```bash
cd frontend && npm install && npm run build
ssh user@your-server "sudo mkdir -p /var/www/silliconthink"
rsync -avz --delete dist/ user@your-server:/var/www/silliconthink/
```

或：`./deploy/deploy.sh user@your-server`

服务器 Nginx 需配置 History 模式；`/api/` 与 `/uploads/` 反代见 `deploy/nginx.conf.example`。

## 后续对接内容 API

1. 实现 `src/services/api/*.api.ts`
2. 在 `src/services/index.ts` 中切换为 API 实现
3. 设置 `VITE_USE_MOCK=false`
