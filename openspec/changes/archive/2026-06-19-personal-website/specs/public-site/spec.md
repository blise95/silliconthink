## ADDED Requirements

### Requirement: 访客可浏览首页
前端 SHALL 提供首页，展示个人简介摘要、最新博客与精选项目。

#### Scenario: 访问首页
- **WHEN** 访客打开 `/`
- **THEN** 页面展示姓名、一句话简介、最新 3 篇博客卡片与 3 个精选项目卡片

### Requirement: 访客可查看关于页
前端 SHALL 提供关于页，展示自我介绍、技能列表与联系方式。

#### Scenario: 查看关于页
- **WHEN** 访客导航至 `/about`
- **THEN** 页面展示完整简介、技能标签及 GitHub、邮箱等联系方式链接

### Requirement: 全局布局与导航
前端 SHALL 提供一致的 Header、Footer 与主导航，各页面复用同一布局。

#### Scenario: 跨页导航
- **WHEN** 访客在任意页面点击导航「博客」
- **THEN** 路由跳转至 `/blog`，Header/Footer 保持不变

### Requirement: 页面 SEO Meta
前端 SHALL 为各页面设置动态 document title 与 meta description。

#### Scenario: 首页 Meta
- **WHEN** 访客打开首页
- **THEN** `<title>` 为站点名称，meta description 为站点 SEO 描述

#### Scenario: 文章页 Meta
- **WHEN** 访客打开某篇博客详情
- **THEN** `<title>` 为「文章标题 | 站点名」，description 为文章摘要
