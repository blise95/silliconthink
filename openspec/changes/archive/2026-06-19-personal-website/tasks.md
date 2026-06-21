## 1. 项目初始化

- [x] 1.1 创建 `frontend/`，用 Vite 初始化 Vue 3 + TypeScript 项目
- [x] 1.2 配置 Vue Router、Pinia、路径别名 `@/`
- [x] 1.3 添加 markdown-it、highlight.js、@unhead/vue、lucide-vue-next
- [x] 1.4 建立 `styles/`：tokens.css（设计令牌）、base.css、components.css（card/btn/section）；自托管 Sora / Plus Jakarta Sans / JetBrains Mono
- [x] 1.5 配置 `VITE_USE_MOCK=true` 与 ESLint/Prettier（可选）

## 2. 类型与 Mock 数据

- [x] 2.1 定义 `types/`：SiteConfig、Post、Project、Tag、Paginated 等
- [x] 2.2 编写 `mocks/site.ts`：站点信息、about、skills、contact、SEO
- [x] 2.3 编写 `mocks/posts.ts`：5～8 篇示例 Markdown 文章（含 draft 用于验证过滤）
- [x] 2.4 编写 `mocks/projects.ts`：4～6 个项目（含 featured、分类、sortOrder）
- [x] 2.5 编写 `mocks/tags.ts` 或从 posts 推导标签列表

## 3. Repository 层（Mock 实现）

- [x] 3.1 定义 `PostRepository`、`ProjectRepository`、`SiteRepository` 接口
- [x] 3.2 实现 `mock/postRepository.mock.ts`：list 分页/标签/搜索、getBySlug、getLatest，过滤 draft
- [x] 3.3 实现 `mock/projectRepository.mock.ts`：list 分类筛选、getBySlug、getFeatured
- [x] 3.4 实现 `mock/siteRepository.mock.ts`：getSiteConfig
- [x] 3.5 实现 `services/index.ts`：按 `VITE_USE_MOCK` 导出 Mock 实例；预留 api 占位文件

## 4. 布局与通用组件

- [x] 4.1 实现 `AppLayout`：sticky Header（Logo + Nav + 移动菜单）、Footer、`<router-view>`
- [x] 4.2 实现通用 UI：`BaseCard`、`BaseButton`、`SectionTitle`（BEM + CSS 变量，参考 Omni-Growth）
- [x] 4.3 实现 `PageContainer`、`LoadingState`、`EmptyState`、`NotFoundView`
- [x] 4.4 实现 composables：`useSiteConfig`、`usePosts`、`useProjects`、`useScrollReveal`（滚动入场动效）

## 5. 公开页面

- [x] 5.1 实现 `HomeView`：Hero（文字居中 + 双 CTA）、Featured Projects、Latest Posts、Skills 条
- [x] 5.2 实现 `AboutView`：简介 Markdown/结构化渲染、技能 Tag、联系方式
- [x] 5.3 实现 `BlogListView` + `PostCard`：列表、标签筛选、分页
- [x] 5.4 实现 `BlogDetailView` + `MarkdownViewer`：正文渲染、代码高亮；样式参考 `article-mai.css` 排版节奏
- [x] 5.5 实现 `ProjectListView` + `ProjectCard`：Grid、分类筛选
- [x] 5.6 实现 `ProjectDetailView`：描述、技术栈、Demo/Repo 按钮
- [x] 5.7 实现搜索：Header 内联或 `/search` 页，调用 `postRepository.list({ keyword })`

## 6. SEO、路由与收尾

- [x] 6.1 配置路由表与 404 兜底路由
- [x] 6.2 各页面设置动态 title、description、基础 Open Graph
- [x] 6.3 响应式断点检查（mobile / tablet / desktop）
- [x] 6.4 编写 `frontend/README.md`：安装、dev、mock 数据说明、目录结构
- [x] 6.5 运行 `openspec validate personal-website --strict`
