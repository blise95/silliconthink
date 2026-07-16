## Context

仓库已有：

- **前端**：Vue 3 博客列表 / 详情（Markdown 渲染），数据经 `PostRepository`，当前主要为 Mock；认证（登录 / 注册 / OAuth）可走真实 API。
- **后端**：Spring Boot 3 + MySQL 8 + MyBatis-Plus + JWT；仅有 `sys_user` / `sys_user_oauth`，无博客表与内容 API。
- **约束**：业务表必须含 `create_date`、`update_date`、`create_by`、`update_by`；API 前缀 `/api/v1`；统一响应 `{ code, message, data }`；分层 controller / service / mapper。

本变更在现有认证之上，落地「登录用户管理自己的博客文章」端到端能力。

## Goals / Non-Goals

**Goals:**

- 持久化博客文章，支持草稿与已发布两种状态
- 登录用户可创建、编辑、发布、取消发布**自己的**文章；公开访客仅见已发布内容
- 公开读 API 与作者写 API 分离；前端作者工作台 + 扩展 Repository
- 编辑器：**左右分栏边写边预览**；工具栏插入加粗/代码块等；粘贴或选择上传图片并插入 Markdown
- 前端呈现「墨简」气质：乔布斯式克制留白 + 东方墨色/朱砂/衬线标题；写作台与公开站统一
- 内容源可通过环境变量在 Mock / API 间切换（与认证开关独立）

**Non-Goals:**

- 评论、点赞、收藏、全文搜索引擎、云对象存储（OSS/S3）
- 多作者协作、角色权限（编辑/审核）、定时发布
- 管理后台式 CMS、富文本所见即所得（仍是 Markdown，不是 Word 式编辑）
- 项目（Portfolio）内容的 CRUD
- 图片 CDN、水印、缩略图多尺寸（首版原图落盘即可）
- 暗黑模式、霓虹发光、紫粉渐变、玻璃拟态堆叠等流行模板风
## Decisions

### 1. 模块边界：后端 `blog` 包 + 前端作者域

```
backend/.../blog/
├── controller/   # PublicPostController, AuthorPostController, MediaController
├── service/
├── mapper/
├── entity/
└── dto/

frontend/src/
├── pages/author/           # MyPostsView, PostEditorView
├── components/editor/      # MarkdownEditor（工具栏 + textarea + 预览）
├── services/               # PostRepository + media upload
└── router                  # /author/* 需登录
```

- **理由**：与现有 `auth` / `user` 包并列，公开读与作者写 Controller 分开，避免单 Controller 权限混杂。
- **备选**：一个 `PostController` 用方法级注解区分 — 可读性较差，不选。

### 2. 数据模型

#### 2.1 `blog_post`（主表）

| 字段 | 说明 |
|------|------|
| `id` | BIGINT PK |
| `author_id` | 作者 `sys_user.id`，所有权依据 |
| `title` | 标题 |
| `slug` | URL 段，**全局唯一**（公开路由 `/blog/:slug`） |
| `summary` | 摘要 |
| `content_md` | Markdown 正文 |
| `cover_url` | 可选封面 URL（外链，首版不上传） |
| `status` | `draft` \| `published`（库内可用 VARCHAR 或 TINYINT 映射） |
| `published_at` | 首次发布或最近一次发布成功时间；草稿可为 NULL |
| `deleted` | 逻辑删除标记（与用户表一致） |
| 审计四字段 | 强制 |

索引：`uk_slug(slug)`、`idx_author_status(author_id, status)`、`idx_published_at(published_at)`（公开列表排序）。

#### 2.2 标签：`blog_tag` + `blog_post_tag`

| 表 | 作用 |
|----|------|
| `blog_tag` | `id`, `name`, `slug`（唯一）+ 审计字段 |
| `blog_post_tag` | `post_id`, `tag_id` + 审计字段；唯一 `(post_id, tag_id)` |

- **理由**：前端已有按标签筛选；规范化便于公开 tag 查询。
- **备选**：`tags` JSON 列 — 实现快但筛选与去重弱，不选。
- 写文章时：客户端传 `tags: string[]`（名称）；服务端按名查找或创建 tag，再维护关联。

### 3. 状态机与发布语义

```
[创建] → draft
draft ──publish──► published
published ──unpublish──► draft
published / draft 均可 update（作者）
```

- **发布**：`status=published`，若 `published_at` 为空则设为当前时间（再次发布保留原 `published_at`，或更新为「最近发布」——**选择保留首次发布时间**，更符合博客惯例）。
- **取消发布**：回到 `draft`，公开列表立即不可见；保留 `published_at` 历史值供作者参考。
- **编辑已发布文**：允许直接改标题 / 正文 / 标签，无需先下架。
- **删除（首版）**：提供作者侧软删除（`deleted=1`），草稿与已发布均可；公开与作者列表均过滤已删。

### 4. API 设计

统一前缀 `/api/v1`，鉴权：`Authorization: Bearer <jwt>`。

**公开（无需登录）**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/posts` | 仅 `published`；分页、`tag`、`keyword`（标题/摘要） |
| GET | `/posts/by-slug/{slug}` | 仅已发布；不存在或草稿 → 业务 404 |
| GET | `/posts/latest?count=` | 最新已发布 |

响应字段对齐前端 `Post`：`id`, `title`, `slug`, `summary`, `contentMd`, `coverUrl`, `tags[]`, `publishedAt`, `status`；额外可返回 `authorDisplayName`（列表可选、详情建议有）。

**作者（需登录，且仅操作自己的数据）**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/me/posts` | 当前用户文章（含草稿）；分页、可选 status 过滤 |
| POST | `/me/posts` | 创建；默认 `draft`；可带初始字段 |
| GET | `/me/posts/{id}` | 自己的文章详情（含草稿） |
| PUT | `/me/posts/{id}` | 更新标题/slug/摘要/正文/封面/标签 |
| POST | `/me/posts/{id}/publish` | 发布 |
| POST | `/me/posts/{id}/unpublish` | 取消发布 |
| DELETE | `/me/posts/{id}` | 软删除 |
| POST | `/me/media/images` | multipart 上传图片，返回公开 URL |

所有权：任何写 / 读作者资源时校验 `author_id == 当前用户`；否则 403（或统一业务码「无权限」）。

校验要点：

- `slug`：小写字母数字与连字符；创建与更新时全局唯一（排除自身）
- `title` / `content_md`：非空长度上限（实现时定具体数字）
- 发布时至少要求 title、slug、content_md 非空

### 5. 安全

- 公开接口只返回 `published` 且未删除记录；禁止通过 slug 猜草稿。
- 写接口全部走 JWT；禁止客户端伪造 `author_id`（服务端从 SecurityContext 注入）。
- Markdown 存储原文；**渲染在前端**（与现状一致）。后端不做 HTML 消毒存储；若未来服务端渲染需另议 XSS。
- CORS 沿用现有前端 origin 配置。

### 6. 前端架构

```
PostRepository（扩展）
├── list / getBySlug / getLatest          # 公开读（已有）
├── listMine / getMineById                # 作者读
├── create / update / publish / unpublish / remove
├── mock 实现：内存数组，按当前 mock 用户过滤
└── api 实现：调用上述 REST

开关：VITE_CONTENT_USE_API（或复用/扩展现有 VITE_USE_MOCK）
- 建议：保留 VITE_USE_MOCK 控制内容仓储；true → Mock，false → API
- 认证仍由 VITE_AUTH_USE_API 独立控制
```

页面与路由：

| 路由 | 说明 |
|------|------|
| `/author/posts` | 我的文章列表（草稿/已发布标识） |
| `/author/posts/new` | 新建 |
| `/author/posts/:id/edit` | 编辑 |
| 现有 `/blog`、`/blog/:slug` | 公开浏览；API 模式下走真实数据 |

未登录访问 `/author/*` → 跳转 `/login?redirect=...`。Header 在已登录时展示「写文章 / 我的文章」入口。

### 6.1 编辑器：边写边看 + 工具栏 + 粘贴上传

**布局（桌面默认左右分栏，窄屏上下堆叠）：**

```
┌─ 工具栏（加粗 / 斜体 / 标题 / 链接 / 代码块 / 图片…）─┐
├──────────────────┬─────────────────────────────────────┤
│  Markdown 编辑区  │  实时预览（MarkdownViewer）            │
│  （textarea）     │  content 与左侧双向：左侧改 → 右侧即刷新 │
└──────────────────┴─────────────────────────────────────┘
元数据区：标题、slug、摘要、标签、封面；操作：保存草稿 / 发布 / 取消发布
```

- **实时预览**：`v-model` 绑定正文；预览用现有 `renderMarkdown`，输入即更新（可用极短 debounce，如 50–100ms，避免大文卡顿）。
- **工具栏**：在光标处插入/包裹 Markdown，例如：
  - 加粗 → `**选中文本**`
  - 斜体 → `*选中文本*`
  - 行内代码 → `` `code` ``
  - 代码块 → 围栏 \`\`\`lang … \`\`\`
  - 标题 / 引用 / 列表 / 链接 → 对应语法
  - 插入图片 → 打开文件选择，上传成功后插入 `![alt](url)`
- **粘贴上传图片**：监听编辑区 `paste`；若剪贴板含图片文件，调用上传接口，成功后在光标处插入 `![](url)`；预览区随即显示图片。非图片粘贴保持浏览器默认（粘贴纯文本）。
- **仍非 WYSIWYG**：左侧始终是源码，右侧是渲染结果；工具栏只是「快捷插入语法」。

### 6.2 图片上传（本地存储）

| 项 | 约定 |
|----|------|
| 接口 | `POST /api/v1/me/media/images`（multipart，需登录） |
| 响应 | `{ url }` 可公开访问的绝对或站点相对 URL |
| 存储 | 后端本地目录（如 `app.upload.dir`），按日期/UUID 命名 |
| 访问 | `GET /uploads/**` 或 `/api/v1/media/**` 静态映射（公开可读） |
| 限制 | 仅常见图片 MIME（jpeg/png/gif/webp）；单文件大小上限（如 5MB）；需登录 |

- Mock 模式：上传可转为 object URL 或占位 data URL，便于纯前端预览工具栏/粘贴流程。
- 封面图可复用同一上传接口，写入 `coverUrl`。
- **不做**：OSS、图片鉴权下载、删除孤立文件清理（可后续）。

编辑器元数据字段与操作同前：标题、slug（可据标题自动生成、可改）、摘要、标签、Markdown 正文；保存草稿、发布、取消发布。

### 6.3 视觉语言「墨简」（Jobs × 东方）

命名灵感：**乔布斯的克制**（少即是多、字体与留白说话）× **东方留白与墨韵**（不是庙宇堆砌，也不是紫系 SaaS 模板）。

#### 原则

| 来源 | 落到界面 |
|------|----------|
| Jobs | 大留白、对齐精准、一层信息优先、按钮极少且有重量、动效短而稳（ease-out 200–300ms） |
| 东方 | 墨色为主、朱砂作唯一强调色、标题用衬线中文、背景呈冷宣纸/雾感、列表与卡片少边框多分隔线 |

#### 色板（替换现有 indigo/violet）

| Token | 建议值 | 用途 |
|-------|--------|------|
| `--color-ink` | `#141414` | 主文字、标题 |
| `--color-ink-soft` | `#3d3d3d` | 次级正文 |
| `--color-mist` | `#6b6b6b` | 辅助说明 |
| `--color-paper` | `#f5f5f7` | 页面底（偏 Apple 冷灰，避免暖奶油模板） |
| `--color-surface` | `#ffffff` | 内容面 |
| `--color-line` | `#e8e6e3` | 细线分隔（宣纸折痕感） |
| `--color-cinnabar` | `#b83b2d` | 唯一强调：主按钮、关键链接 hover、发布态 |
| `--color-cinnabar-ink` | `#8f2e23` | 强调 hover |

禁止：紫/靛品牌色、大面积渐变紫、霓虹 glow、圆角药丸标签堆叠。

#### 字体

| 角色 | 选择 | 说明 |
|------|------|------|
| Display | `Noto Serif SC` + 西文衬线兜底（如 `Source Serif 4`） | 标题、站点名：书卷气 |
| Body | `Source Sans 3` + `Noto Sans SC` | 正文清晰，近 Jobs 无衬线阅读感 |
| Mono | `IBM Plex Mono` 或现有等宽 | 代码块安静不抢戏 |

字号：标题用较大字重与字距略紧；正文行高约 1.7；写作预览区与公开文章共用 `article` 排版。

#### 版式与组件

- **少卡片**：列表以细底边或大留白分区；写作台编辑/预览用细竖线分隔，不用厚阴影盒子。
- **圆角**：小而克制（4–8px），不追求「超圆」。
- **阴影**：几乎不用；必要时仅一层极淡 `shadow-sm`。
- **背景氛围**：`--color-paper` 上叠加极淡噪点或径向雾（opacity ≤ 4%），避免装饰插画喧宾夺主。
- **东方符号克制**：可用一处细竖线/印章红点作品牌锚，不做龙纹、水墨大图铺满。
- **写作台**：工具栏细线图标、文字按钮；「发布」用朱砂实心；草稿/取消为墨色线框。
- **动效**：进入页标题轻微 fade-up；预览切换无花哨；禁止弹跳与长循环动画。

#### 落地范围

1. 重写 `frontend/src/styles/tokens.css` 与依赖品牌色的 `components.css` / `base.css` / Header
2. 首页 Hero、博客列表/详情、登录页与**作者编辑器**统一语言
3. `favicon` / 简单品牌字标可改为墨底朱砂点（可选，不阻塞）

### 7. 与公开博客规格的关系

- 行为不变：访客只看已发布、分页、标签、搜索、Markdown 高亮。
- 数据源从 Mock 变为可选 API；Mock 过滤 `published` 的约定保持，API 侧同等保证。

### 8. 包与类命名（后端示例）

- `BlogPostDO` extends `BaseDO`
- `BlogPostService` / `BlogPostServiceImpl`
- `PublicPostController`、`AuthorPostController`、`MediaController`
- DTO：`PostCreateRequest`、`PostUpdateRequest`、`PostVO`、`PostListItemVO`、`MediaUploadVO`

SecurityConfig：放行 `GET /api/v1/posts/**` 与上传文件的公开读路径；`/api/v1/me/**` 需认证。

## Risks / Trade-offs

- **[Risk] slug 全局唯一在多作者下易冲突** → Mitigation：创建时后端校验 + 前端提示；可选自动追加短后缀（首版仅报错即可）。
- **[Risk] 标签随意创建导致脏数据** → Mitigation：首版允许自由创建；后续可加白名单或合并。
- **[Risk] Mock / API 双实现行为漂移** → Mitigation：规格场景同时约束两端；关键过滤逻辑（published / 所有权）在规格中写死。
- **[Risk] 本地上传占磁盘 / 路径穿越** → Mitigation：UUID 文件名、限制扩展名与大小、目录写死在配置根下。
- **[Trade-off] 不做服务端 Markdown→HTML** → 实现简单，与现前端一致；SEO 对 SPA 仍弱（本站已是 SPA）。
- **[Trade-off] 本地存储而非 OSS** → 部署简单；多机部署需共享盘或再迁对象存储。
- **[Trade-off] 分栏预览非所见即所得** → 实现与 XSS 边界清晰；工具栏降低 Markdown 门槛。
- **[Trade-off] 墨简克制 vs 装饰性水墨** → 选克制，避免民俗贴纸感；东方只通过色、字、留白传达。

## Migration Plan

1. 执行 DDL：`blog_post`、`blog_tag`、`blog_post_tag`（可追加到 `schema.sql` + 可选种子数据）。
2. 部署后端（含新接口）；旧前端仅 Mock 时不受影响。
3. 前端打开内容 API 开关联调；验证公开读与作者写。
4. 回滚：关闭 `VITE_USE_MOCK=false` 即可退回 Mock；表可保留。

## Open Questions

- 是否需要「仅站点所有者可写作」限制？当前按 proposal：**任意已登录启用用户**均可写自己的博客（开放注册场景下人人可写）。若需改为仅管理员，可后续加 role，本设计不阻塞。
- 已发布文章取消发布后，原 slug 是否允许他人占用？**建议占用规则不变（仍属原作者记录）**，软删前 slug 不释放。
