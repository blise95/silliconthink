## 1. 数据层与领域模型

- [x] 1.1 在 `schema.sql` 增加 `blog_post`、`blog_tag`、`blog_post_tag` DDL（含审计字段与索引）
- [x] 1.2 新增 `BlogPostDO` / `BlogTagDO` / `BlogPostTagDO`（继承 `BaseDO`）与对应 Mapper
- [x] 1.3 实现标签查找或创建、文章-标签关联维护的辅助逻辑

## 2. 后端公开读 API

- [x] 2.1 实现 `BlogPostService` 公开查询：分页列表、按 slug、latest（仅 published 未删除）
- [x] 2.2 实现 `PublicPostController`：`GET /api/v1/posts`、`/by-slug/{slug}`、`/latest`
- [x] 2.3 SecurityConfig 放行公开 posts GET；统一 VO 字段对齐前端 `Post`（含 tags、可选 authorDisplayName）

## 3. 后端作者写 API

- [x] 3.1 实现创建草稿、更新、软删除（强制 `author_id` 来自当前用户；slug 唯一校验）
- [x] 3.2 实现 publish / unpublish 状态流转与发布校验
- [x] 3.3 实现 `AuthorPostController`：`/api/v1/me/posts` 系列接口与所有权 403/业务错误
- [x] 3.4 补充必要的业务错误码与参数校验（Bean Validation）

## 4. 前端 Repository 与类型

- [x] 4.1 扩展 `Post` 类型（如 `authorDisplayName`）与 `PostRepository` 写/我的方法签名
- [x] 4.2 实现 Mock 写路径：所有权、published 过滤、与公开读一致
- [x] 4.3 实现 API `postRepository`：公开读 + 作者写（带 Bearer token）
- [x] 4.4 确认 `VITE_USE_MOCK` 切换内容仓储；认证开关保持独立

## 5. 前端作者工作台与公开联调

- [x] 5.1 新增路由 `/author/posts`、`/new`、`/:id/edit` 与登录守卫（redirect）
- [x] 5.2 实现我的文章列表页（状态标识、编辑/发布/取消发布/删除入口）
- [x] 5.3 实现分栏 Markdown 编辑器：工具栏（至少加粗/代码块）+ textarea + 实时预览（墨简细线分栏）
- [x] 5.4 实现粘贴/选择图片上传并插入 `![](url)`；对接后端上传（Mock 可用本地 blob URL）
- [x] 5.5 Header 增加已登录入口；公开 `/blog` 在 API 模式下走真实数据并展示作者名（若有）
- [x] 5.6 端到端自测：登录 → 边写边看 → 工具栏/粘贴图 → 发布 → 公开可见 → 取消发布

## 6. 图片上传后端

- [x] 6.1 实现 `POST /api/v1/me/media/images`（类型/大小校验、本地落盘、返回 URL）
- [x] 6.2 配置静态资源映射公开访问上传文件；Security 放行读、保护写

## 7. 墨简视觉升级

- [x] 7.1 重写 `tokens.css`：墨色 / 纸色 / 朱砂 / 字体栈（Noto Serif SC + Source Sans 3 等），移除紫系品牌色
- [x] 7.2 更新 `base.css` / `components.css` / `article.css` 与 Header、按钮、标签样式对齐墨简
- [x] 7.3 首页 Hero、博客列表/详情、登录/注册页微调至同一语言（留白、细线、克制动效）
- [x] 7.4 作者列表与编辑器按墨简打磨（朱砂发布按钮、预览分栏、工具栏线框图标）
