## ADDED Requirements

### Requirement: 访客可浏览博客列表
前端 SHALL 提供博客列表页，展示已发布文章的分页列表（标题、摘要、日期、标签）。

#### Scenario: 浏览博客列表
- **WHEN** 访客打开 `/blog`
- **THEN** 页面按发布时间降序展示已发布文章，默认每页 10 篇

#### Scenario: 按标签筛选
- **WHEN** 访客点击某一标签
- **THEN** 列表仅展示带有该标签的已发布文章

### Requirement: 访客可阅读文章详情
前端 SHALL 提供文章详情页，将 Markdown 渲染为 HTML，并展示标签与发布时间。

#### Scenario: 阅读文章
- **WHEN** 访客访问 `/blog/:slug` 且 slug 对应已发布文章
- **THEN** 页面展示标题、渲染后正文、标签与发布日期

#### Scenario: 文章不存在
- **WHEN** 访客访问不存在的 slug
- **THEN** 页面展示 404 或 NotFound 提示

### Requirement: 访客可搜索文章
前端 SHALL 支持按标题或摘要关键词过滤已发布文章。

#### Scenario: 关键词搜索
- **WHEN** 访客输入关键词并提交搜索
- **THEN** 列表展示标题或摘要匹配该关键词的已发布文章

### Requirement: Markdown 代码高亮
前端 SHALL 对 Markdown 正文中的代码块进行语法高亮显示。

#### Scenario: 渲染代码块
- **WHEN** 文章正文包含 fenced code block
- **THEN** 页面以高亮样式展示对应语言代码
