## Context

个人网站项目，用户指定 Vue 前端。经讨论调整：**不做微服务、本阶段不设计后端**，先用 Mock 数据把公开站点做出来。后续可无缝替换 Repository 实现为真实 REST API。

## Goals / Non-Goals

**Goals:**
- Vue 3 单页应用，公开站点完整可用（首页 / 关于 / 博客 / 项目）
- Mock 数据层与真实 API 形状一致，便于日后对接 Spring Boot + MySQL
- 响应式、可读性好的排版；Markdown 博客渲染与代码高亮
- 组件化、目录清晰，方便继续迭代 UI

**Non-Goals（本阶段）:**
- Spring Cloud Alibaba / 微服务 / Gateway / Nacos
- MySQL、MyBatis Plus、任何后端代码
- 管理后台、登录、JWT、媒体上传
- SSR/SSG（首版 CSR + 基础 SEO meta）
- 评论、订阅等社交功能

## Decisions

### 1. 仓库结构（仅前端）

```
silliconthink/
├── frontend/
│   ├── src/
│   │   ├── assets/           # 静态资源、头像、封面占位图
│   │   ├── components/       # 通用 UI 组件
│   │   │   ├── layout/       # AppHeader, AppFooter, PageContainer
│   │   │   ├── blog/         # PostCard, PostList, MarkdownViewer
│   │   │   └── project/      # ProjectCard, ProjectGrid
│   │   ├── composables/      # useSiteConfig, usePosts, useProjects
│   │   ├── mocks/            # JSON 或 TS 常量：site, posts, projects, tags
│   │   ├── pages/            # 路由页面（或 views/）
│   │   ├── router/           # 路由定义
│   │   ├── services/         # Repository 接口 + Mock 实现
│   │   ├── stores/           # Pinia（站点配置缓存等）
│   │   ├── types/            # Post, Project, SiteConfig, Tag 等 TS 类型
│   │   └── utils/            # 日期格式化、slug 工具
│   ├── index.html
│   ├── vite.config.ts
│   └── package.json
└── openspec/
```

**理由**：单 frontend 目录，结构简单；`services/` 隔离数据来源，Mock 与 API 可切换。

### 2. 参考站点分析：[Omni-Growth 官网](https://omni-growth.ai/index.html)

该站为**静态多页站点（MPA）**，无 Vue/React 框架，但视觉与工程模式值得借鉴：

| 维度 | Omni-Growth 做法 | 个人站借鉴方式 |
|------|------------------|----------------|
| 架构 | 多 HTML + 原生 JS（`main.js` / `i18n.js`） | **保留 Vue 3 SPA**（Mock + 组件化更易维护） |
| 样式 | 手写 CSS + **CSS Variables 设计令牌**，BEM 命名（`header__logo`） | 采用相同令牌体系，Vue 组件用 scoped CSS + BEM |
| 字体 | 自托管 **Sora**（标题）+ **Plus Jakarta Sans**（正文）+ **JetBrains Mono**（代码） | 同字体栈，`assets/fonts/` 自托管 |
| 图标 | **Lucide** subset（`lucide-subset.min.js`） | `lucide-vue-next`，按需引入 |
| 视觉 | 浅色、科技感、卡片 + 阴影分层；品牌靛紫 `#6366F1`（参考 mai.co 风格） | 沿用布局模式，品牌色改为个人站配色 |
| 动效 | 滚动入场 `animate-on-scroll`、平滑滚动 | composable `useScrollReveal` + CSS transition |
| i18n | 路径分语言（`/en/`）+ localStorage + `hreflang` | 首版中文；Vue Router 预留 `/en` 前缀 |
| SEO | OG / canonical / JSON-LD / GTM（Cookie 同意后才加载） | `@unhead/vue` 动态 meta；分析脚本后续再加 |
| 博客 | 独立 HTML + `article-mai.css` | Vue 路由 `/blog/:slug` + 独立文章样式表 |

**不照搬**：Omni-Growth 无构建工具、无组件框架；我们用 **Vite + Vue** 获得 HMR 与 Mock 数据层，视觉语言对齐即可。

### 3. 技术选型（Vue 实现 + Omni-Growth 视觉参考）

| 类别 | 选型 | 说明 |
|------|------|------|
| 框架 | Vue 3 + Composition API + `<script setup>` | 用户指定 |
| 构建 | Vite | 快速 HMR、静态资源打包 |
| 路由 | Vue Router 4 | History 模式 |
| 状态 | Pinia | 站点配置、列表缓存（可选） |
| 样式 | **CSS Variables + 全局 `styles/`** | 对齐 Omni-Growth 令牌体系，**不用 Tailwind** |
| 字体 | Sora + Plus Jakarta Sans + JetBrains Mono | 自托管 woff2 |
| Markdown | markdown-it + highlight.js | 博客正文，代码块用 JetBrains Mono |
| 图标 | **lucide-vue-next** | 与参考站 Lucide 一致 |
| Head/SEO | **@unhead/vue** | 动态 title / OG / canonical |
| HTTP（预留） | axios | 本阶段 Mock 不用 |

**设计令牌**（`styles/tokens.css`，结构参考 Omni-Growth）：

```css
:root {
  --color-brand: #6366F1;       /* 可改为个人品牌色 */
  --color-brand-ink: #4F46E5;
  --color-text: #1a1a1a;
  --color-text-muted: #6b7280;
  --color-surface: #FFFFFF;
  --color-surface-muted: #F2F3FD;
  --font-display: 'Sora', sans-serif;
  --font-body: 'Plus Jakarta Sans', 'PingFang SC', sans-serif;
  --font-mono: 'JetBrains Mono', monospace;
  --shadow-md: 0 4px 12px -2px rgba(17, 17, 26, 0.08);
  --radius-lg: 12px;
  --spacing-section: 64px;
}
```

### 4. Mock 数据层设计

**原则**：页面与 composable **只调用 Repository**，不 import mock 文件。

```typescript
// types/post.ts
export interface Post {
  id: string
  title: string
  slug: string
  summary: string
  contentMd: string
  coverUrl?: string
  tags: string[]
  publishedAt: string
  status: 'published' | 'draft'
}

// services/postRepository.ts
export interface PostRepository {
  list(params: { page?: number; pageSize?: number; tag?: string; keyword?: string }): Promise<Paginated<Post>>
  getBySlug(slug: string): Promise<Post | null>
  getLatest(n: number): Promise<Post[]>
}

// services/mock/postRepository.mock.ts — 读 mocks/posts.ts，内存过滤/分页
// services/api/postRepository.api.ts — 后续实现，调用 /api/public/v1/posts
```

**切换方式**（`services/index.ts`）：

```typescript
const useMock = import.meta.env.VITE_USE_MOCK !== 'false'
export const postRepo = useMock ? mockPostRepository : apiPostRepository
```

Mock 数据文件：
- `mocks/site.ts` — 站点名、tagline、about、skills、contact、SEO
- `mocks/posts.ts` — 5～8 篇示例文章（含 Markdown）
- `mocks/projects.ts` — 4～6 个项目
- `mocks/tags.ts` — 标签列表

Mock 行为需模拟真实 API：**只返回 `status === 'published'`**；分页、标签筛选、关键词搜索在 Mock 层用数组 filter 实现。

### 5. 路由与页面

| 路径 | 页面 | 数据来源 |
|------|------|----------|
| `/` | HomeView | site + latest posts + featured projects |
| `/about` | AboutView | site.about, skills, contact |
| `/blog` | BlogListView | postRepo.list |
| `/blog/:slug` | BlogDetailView | postRepo.getBySlug |
| `/projects` | ProjectListView | projectRepo.list |
| `/projects/:slug` | ProjectDetailView | projectRepo.getBySlug |
| `/search` | SearchView（可选） | postRepo.list({ keyword }) |

**404**：未知 slug 显示 NotFound 页。

### 6. 布局与 UI 结构（参考 Omni-Growth 信息架构）

```
┌─────────────────────────────────────────┐
│  Header: Logo | Nav | 中/EN | CTA(可选)  │  ← sticky，移动端汉堡菜单
├─────────────────────────────────────────┤
│  Hero（居中标题 + 副标题 + 双 CTA）        │
│  Section 卡片 Grid（项目 / 技能 / 博客）   │  ← animate-on-scroll
├─────────────────────────────────────────┤
│  Footer: 链接 | 社交 | 版权              │
└─────────────────────────────────────────┘
```

**首页区块**（对齐 [Omni-Growth 首页](https://omni-growth.ai/index.html) 节奏，内容改为个人向）：
1. **Hero** — 姓名 + 一句话定位 + 双 CTA（「看项目」「读博客」），可选淡品牌底 `--color-surface-muted`
2. **Featured Projects** — 3 张 `card` 网格，封面 + 技术栈 Tag
3. **Latest Posts** — 3 篇摘要卡片
4. **Skills / About 摘要** — Tag 条或简短自我介绍
5. **Footer CTA** — 联系方式 / GitHub（可选）

**关于页**：Markdown 或结构化字段渲染；技能 Tag；联系方式图标链接。

**博客列表**：PostCard（封面、标题、摘要、日期、标签）；侧边或顶栏标签筛选；简单分页或「加载更多」。

**博客详情**：标题、元信息、Markdown 正文、目录（可选 h2/h3 anchor）。

**项目列表**：Grid 卡片 + 分类 Tab/Select。

**项目详情**：封面、描述、技术栈 Tag、Demo / Repo 外链按钮。

### 7. SEO 与 Meta（对齐 Omni-Growth 做法）

- `@unhead/vue`：每页 `title`、`meta description`、`link canonical`
- Open Graph：`og:title`、`og:description`、`og:image`
- 博客/项目详情页：JSON-LD `BlogPosting` / `CreativeWork`（可选）
- 分析脚本（GTM/GA）：**本阶段不加**；后续用 Cookie 同意后再动态加载

### 8. 与未来后端的契约（仅文档约定，本阶段不实现）

Mock 类型字段与此前 API 设计对齐，便于后续对接：

- `GET /api/public/v1/site-config` → `SiteConfig`
- `GET /api/public/v1/posts` → 分页 Post 列表
- `GET /api/public/v1/posts/:slug` → Post 详情
- `GET /api/public/v1/projects` → 分页 Project 列表
- `GET /api/public/v1/home` → `{ latestPosts, featuredProjects }`

后端阶段再单独开变更，实现 Spring Boot 单体（非微服务）+ MySQL + MyBatis Plus。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| Mock 与真实 API 字段不一致 | 集中定义 `types/`，Repository 接口即契约 |
| 纯 CSR SEO 较弱 | 基础 meta + 后续可加 prerender |
| Mock 数据维护在代码里 | 结构清晰；后端上线后 mock 仅用于 Storybook/离线 demo |
| 无管理后台内容更新麻烦 | 本阶段接受；内容改 mock 文件即可 |

## Migration Plan（Mock → 真实 API）

1. 实现 `*.api.ts` Repository，对接 Spring Boot 公开 API
2. 环境变量 `VITE_USE_MOCK=false`，配置 `VITE_API_BASE_URL`
3. 联调分页、筛选、404 行为
4. 管理后台与持久化作为**独立后续变更**

## Open Questions

1. **品牌主色**：沿用 Omni-Growth 靛紫 `#6366F1`，还是换成其他色（如蓝/绿）？
2. **首页 Hero**：Omni-Growth 无大头像，纯文字 Hero — 个人站是否加头像？
3. **博客目录**：详情页是否需要 TOC（h2/h3）？
4. **搜索**：Header 内联 vs 独立 `/search` 页？
5. **语言**：首版仅中文，还是同步做 `/en` 路由（参考站有中英双版）？
