# frontend-auth Specification

## Purpose
前端登录/注册/OAuth 联调、会话态与认证/内容 Mock 独立配置。

## Requirements

### Requirement: 前端提供登录与注册页面并对接后端
前端 SHALL 提供登录页与注册页，在启用真实认证 API 时调用后端对应接口，成功后持久化 access token 并进入已登录态。

#### Scenario: 登录页联调成功
- **WHEN** 用户在登录页提交正确账号密码且认证 API 已启用
- **THEN** 前端保存 token，可成功请求当前用户信息，并展示已登录状态

#### Scenario: 注册页联调成功
- **WHEN** 用户在注册页提交合法新账号且认证 API 已启用
- **THEN** 前端进入已登录态（或获得 token）并展示用户信息

#### Scenario: 登录失败提示
- **WHEN** 后端返回认证失败
- **THEN** 前端展示错误提示且不写入有效登录态

### Requirement: 前端支持 GitHub OAuth 联调
前端 SHALL 提供 GitHub 登录入口，并处理 OAuth 回调页：用一次性码换取 token 后进入已登录态。

#### Scenario: OAuth 回调换票
- **WHEN** 用户从 OAuth 回调页携带有效一次性码进入前端回调路由
- **THEN** 前端调用换票接口，保存 token，并展示已登录状态

### Requirement: 前端会话与登出
前端 SHALL 在本地持久化 token，启动时尝试恢复会话；登出时清除 token 并调用后端登出（若已启用 API）。

#### Scenario: 刷新后保持登录
- **WHEN** 用户已登录后刷新页面且本地仍有有效 token
- **THEN** 前端恢复已登录展示（或通过 me 接口确认）

#### Scenario: 登出
- **WHEN** 用户点击登出
- **THEN** 本地 token 被清除，界面回到未登录态

### Requirement: 认证与内容 Mock 可独立配置
前端 SHALL 允许在内容数据仍使用 Mock 时单独启用真实认证 API，以便联调认证而不强制切换全部 Repository。

#### Scenario: 仅认证走真实 API
- **WHEN** 配置为认证使用真实 API 且内容仍为 Mock
- **THEN** 登录/注册/OAuth/me 走后端，博客/项目等内容仍可读 Mock 数据
