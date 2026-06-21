# mock-data-layer Specification

## Purpose
TBD - created by archiving change personal-website. Update Purpose after archive.
## Requirements
### Requirement: 页面通过 Repository 访问数据
前端 SHALL 通过 Repository 接口获取数据，页面组件不得直接 import Mock 原始数据文件。

#### Scenario: 博客列表取数
- **WHEN** BlogListView 挂载并请求文章列表
- **THEN** 调用 `postRepository.list()`，而非直接读取 `mocks/posts.ts`

### Requirement: Mock 实现模拟 published 过滤
Mock Repository SHALL 仅返回 `status === 'published'` 的记录，与预期后端行为一致。

#### Scenario: 草稿不可见
- **WHEN** Mock 数据中存在 status 为 draft 的文章
- **THEN** `postRepository.list()` 与 `getBySlug()` 均不返回该文章

### Requirement: Mock 支持分页与筛选
Mock Repository SHALL 在内存中实现分页、标签筛选与关键词搜索。

#### Scenario: 按标签筛选
- **WHEN** 调用 `postRepository.list({ tag: 'vue' })`
- **THEN** 返回 tags 包含 `vue` 的已发布文章

#### Scenario: 关键词搜索
- **WHEN** 调用 `postRepository.list({ keyword: '组件' })`
- **THEN** 返回标题或摘要包含该关键词的已发布文章

### Requirement: 可通过环境变量切换数据源
项目 SHALL 支持 `VITE_USE_MOCK` 切换 Mock 与 API 实现；本阶段仅实现 Mock 分支。

#### Scenario: 默认使用 Mock
- **WHEN** 未设置或 `VITE_USE_MOCK=true`
- **THEN** `services/index.ts` 导出 Mock Repository 实例

### Requirement: 类型定义与 Repository 契约集中管理
项目 SHALL 在 `types/` 定义 Post、Project、SiteConfig 等类型，Repository 方法签名与类型一致。

#### Scenario: 类型复用
- **WHEN** 新增 Project 相关页面
- **THEN** 使用 `types/project.ts` 中的 `Project` 类型，不重复定义

