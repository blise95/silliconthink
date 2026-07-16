## ADDED Requirements

### Requirement: PostRepository 支持作者侧写操作
`PostRepository` SHALL 提供创建、更新、发布、取消发布、软删除以及「我的文章」列表/详情方法；页面不得直接调用 HTTP 或读写 Mock 原始数组。

#### Scenario: 通过 Repository 创建
- **WHEN** 作者编辑页提交新建
- **THEN** 调用 `postRepository.create(...)`（或约定等价方法名），而非直接 `fetch` 或改 `mocks/posts.ts`

#### Scenario: 通过 Repository 发布
- **WHEN** 用户点击发布
- **THEN** 调用 Repository 的 publish 方法

### Requirement: Mock 写实现遵守所有权与 published 过滤
Mock `PostRepository` SHALL 在内存中模拟：公开读仅 `published`；作者写仅能操作归属当前 Mock 登录用户的文章（若无登录上下文则写操作失败）。

#### Scenario: Mock 下草稿不出现在公开 list
- **WHEN** Mock 中存在 draft，且调用公开 `list()`
- **THEN** 不返回该草稿

#### Scenario: Mock 下非作者不可更新
- **WHEN** Mock 登录用户尝试更新另一作者的文章
- **THEN** 更新失败（抛错或返回失败结果）

### Requirement: API 实现对接后端公开与作者接口
当 `VITE_USE_MOCK` 为 false（或项目约定的内容走 API 配置）时，`PostRepository` 的 API 实现 SHALL 分别调用公开读与作者写后端接口，并携带认证 token（写与我的资源）。

#### Scenario: API 模式写请求带 token
- **WHEN** 已登录用户在 API 模式下调用 create/update/publish
- **THEN** 请求携带有效 Bearer token
