# Silicon Think — 前端

Vue 3 个人站点，Mock 数据驱动，视觉参考 [Omni-Growth](https://omni-growth.ai/index.html)。

## 环境要求

- Node.js 18+
- npm 9+

## 快速开始

```bash
cd frontend
npm install
npm run dev
```

浏览器打开 http://localhost:5173

## 环境变量

| 变量 | 说明 | 默认 |
|------|------|------|
| `VITE_USE_MOCK` | 是否使用 Mock 数据 | `true` |
| `VITE_SITE_URL` | 站点 URL（SEO 用） | `http://localhost:5173` |

## 目录结构

```
src/
├── components/     # UI、布局、博客、项目组件
├── composables/    # 数据与页面逻辑
├── mocks/          # Mock 静态数据
├── pages/          # 路由页面
├── router/         # Vue Router
├── services/       # Repository 接口 + Mock/API 实现
├── styles/         # 设计令牌与全局样式
├── types/          # TypeScript 类型
└── utils/          # Markdown 渲染等工具
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

本项目是纯前端 SPA（Mock 数据打包进 JS），**只需 Nginx 托管静态文件**，无需 Node 常驻进程。

### 方式一：本地构建 + 上传（推荐）

**1. 本地构建**

```bash
cd frontend
npm install
npm run build
```

**2. 上传 `dist/` 到服务器**

```bash
# 示例：上传到 /var/www/silliconthink
ssh user@your-server "sudo mkdir -p /var/www/silliconthink"
rsync -avz --delete dist/ user@your-server:/var/www/silliconthink/
```

或使用脚本（需本机已配置 SSH）：

```bash
chmod +x deploy/deploy.sh
./deploy/deploy.sh user@your-server
```

**3. 服务器安装 Nginx**

```bash
# Ubuntu / Debian
sudo apt update && sudo apt install -y nginx

# CentOS / RHEL
sudo yum install -y nginx
```

**4. 配置 Nginx**

复制 `deploy/nginx.conf.example` 到服务器，修改 `server_name` 和 `root`：

```bash
sudo cp deploy/nginx.conf.example /etc/nginx/sites-available/silliconthink
# 编辑 your-domain.com 和 root 路径
sudo ln -s /etc/nginx/sites-available/silliconthink /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

**关键**：Vue Router 使用 History 模式，必须配置：

```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

否则直接访问 `/blog` 等路径会 404。

**5. HTTPS（推荐）**

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d your-domain.com
```

### 方式二：在服务器上构建

```bash
# 服务器需 Node.js 18+
ssh user@your-server
git clone <你的仓库> && cd silliconthink/frontend
npm ci && npm run build
sudo mkdir -p /var/www/silliconthink
sudo cp -r dist/* /var/www/silliconthink/
# 再按上面步骤配置 Nginx
```

### 生产环境变量（可选）

构建前创建 `.env.production`：

```env
VITE_USE_MOCK=true
VITE_SITE_URL=https://your-domain.com
```

然后 `npm run build`，SEO meta 会使用正确域名。

### 更新站点

改 mock 或代码后重新构建并 rsync：

```bash
npm run build
rsync -avz --delete dist/ user@your-server:/var/www/silliconthink/
```

### 常见问题

| 问题 | 处理 |
|------|------|
| 刷新子路由 404 | Nginx 加 `try_files ... /index.html` |
| 页面空白 | 检查 Nginx `root` 是否指向 `dist` 内容目录 |
| 权限错误 | `sudo chown -R www-data:www-data /var/www/silliconthink` |

## 后续对接 API

1. 实现 `src/services/api/*.api.ts`
2. 在 `src/services/index.ts` 中切换为 API 实现
3. 设置 `VITE_USE_MOCK=false`
