## Why

需要一个可展示个人品牌、项目经历与技术博客的个人网站。当前阶段优先把**前端体验与信息架构**做扎实，用 Mock 数据快速迭代页面与交互；后端（Spring Boot / MySQL）留到下一阶段再设计，避免过早绑定 API 与部署复杂度。

## What Changes

- 新建 Vue 3 前端站点：首页、关于、项目展示、博客列表与详情
- 建立 **Mock 数据层 + Repository 抽象**，页面只依赖接口，不直接读 JSON 文件
- 用本地 Mock 数据（JSON/TS）模拟站点配置、文章、项目、标签
- 实现响应式布局、Markdown 渲染、标签筛选、搜索等核心交互
- **本阶段不包含**：后端服务、数据库、管理后台、登录鉴权、文件上传

## Capabilities

### New Capabilities

- `public-site`: 公开页面（首页、关于、全局布局、SEO meta）
- `portfolio`: 项目列表、分类筛选、详情页
- `blog`: 博客列表、详情、标签筛选、搜索、Markdown 渲染
- `mock-data-layer`: 数据访问抽象与 Mock 实现，预留后续切换真实 API

### Modified Capabilities

（无）

### Deferred Capabilities（后续阶段，本变更不实现）

- `admin-auth`、`content-admin`、`media-upload` — 待后端就绪后再提案

## Impact

- **前端**: Vue 3 + Vite + Vue Router + Pinia + Tailwind（或同类 UI 方案）
- **Mock**: `src/mocks/` 静态数据 + `src/services/` Repository 接口
- **无后端**: 不引入 Spring Cloud、MySQL、Docker 基础设施
- **部署**: 静态站点即可（Vercel / GitHub Pages / Nginx）
