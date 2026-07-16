## MODIFIED Requirements

### Requirement: 全局布局与导航
前端 SHALL 提供一致的 Header、Footer 与主导航，各页面复用同一布局；Header SHALL 提供登录入口，并在用户已登录时展示用户标识与登出操作。

#### Scenario: 跨页导航
- **WHEN** 访客在任意页面点击导航「博客」
- **THEN** 路由跳转至 `/blog`，Header/Footer 保持不变

#### Scenario: 未登录显示登录入口
- **WHEN** 访客未登录浏览任意布局内页面
- **THEN** Header 展示可进入登录流程的入口（如「登录」）

#### Scenario: 已登录显示用户态
- **WHEN** 用户已登录
- **THEN** Header 展示用户标识（如展示名或用户名）及登出操作，且不再以「仅登录入口」为唯一认证相关控件
