## ADDED Requirements

### Requirement: 访客可浏览项目列表
前端 SHALL 提供项目列表页，展示已发布项目，支持按分类筛选。

#### Scenario: 浏览项目列表
- **WHEN** 访客打开 `/projects`
- **THEN** 页面展示项目名称、封面、简介、技术栈标签，按 sortOrder 降序排列

#### Scenario: 按分类筛选
- **WHEN** 访客选择某一分类
- **THEN** 列表仅展示该分类下的已发布项目

### Requirement: 访客可查看项目详情
前端 SHALL 提供项目详情页，含完整描述、技术栈与外链。

#### Scenario: 查看项目详情
- **WHEN** 访客访问 `/projects/:slug`
- **THEN** 页面展示完整描述、封面、技术栈 Tag、Demo 与 Repo 外链按钮

#### Scenario: 项目不存在
- **WHEN** 访客访问不存在的 slug
- **THEN** 页面展示 404 或 NotFound 提示

### Requirement: 首页展示精选项目
前端 SHALL 在首页展示标记为 featured 的已发布项目（最多 3 个）。

#### Scenario: 精选项目区块
- **WHEN** 访客打开首页
- **THEN** Featured 区块展示 featured=true 的项目，按 sortOrder 排序，最多 3 项
