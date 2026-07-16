## ADDED Requirements

### Requirement: 公开博客可对接真实内容 API
当前端内容数据源切换为 API 时，博客列表与详情 SHALL 从后端公开接口读取已发布文章，行为与 Mock 过滤规则一致（仅已发布可见）。

#### Scenario: API 模式下列表来自后端
- **WHEN** 内容 API 已启用且访客打开 `/blog`
- **THEN** 列表数据来自公开文章列表接口，且仅含已发布文章

#### Scenario: API 模式下详情来自后端
- **WHEN** 内容 API 已启用且访客访问已发布文章的 `/blog/:slug`
- **THEN** 页面展示该文章标题与渲染后正文

### Requirement: 文章详情可展示作者展示名
前端文章详情在数据包含作者展示名时 SHALL 展示作者信息。

#### Scenario: 展示作者
- **WHEN** 详情数据包含 `authorDisplayName`（或等价字段）
- **THEN** 页面展示该作者展示名
