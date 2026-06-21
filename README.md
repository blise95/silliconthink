# silliconthink

个人网站项目 — Vue 3 前端 + Mock 数据 + OpenSpec 规格驱动。

**仓库：** https://github.com/blise95/silliconthink

## 快速开始

```bash
cd frontend
npm install
cp .env.example .env   # 可选
npm run dev
```

浏览器打开 http://localhost:5173

## 目录

| 目录 | 说明 |
|------|------|
| `frontend/` | Vue 3 个人站点（Mock 数据） |
| `openspec/` | OpenSpec 规格与归档变更 |
| `.cursor/` | Cursor 工作流命令与 Skills |

## 部署

见 [frontend/README.md](frontend/README.md) 中的 Linux / Nginx 部署说明。

## 推送代码

```bash
git remote -v   # 应指向 github.com/blise95/silliconthink
git push -u origin main
```
