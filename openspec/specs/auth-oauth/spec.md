# auth-oauth Specification

## Purpose
GitHub OAuth 登录与一次性码换票。

## Requirements

### Requirement: 访客可通过 GitHub OAuth 登录
系统 SHALL 支持 GitHub OAuth 授权码流程：引导用户授权、处理回调、建立或复用绑定用户，并完成登录。

#### Scenario: 发起授权
- **WHEN** 客户端请求发起 GitHub OAuth（如访问授权入口）
- **THEN** 系统提供带有效 state 的 GitHub 授权地址（重定向或返回 URL）

#### Scenario: 首次授权自动建号
- **WHEN** 用户完成 GitHub 授权且该 GitHub 账号尚未绑定本地用户
- **THEN** 系统创建本地用户与 OAuth 绑定记录，并进入可换取 access token 的成功流程

#### Scenario: 再次授权登录已有绑定
- **WHEN** 已绑定过的 GitHub 账号再次完成授权
- **THEN** 系统复用已绑定的本地用户，不重复创建用户，并进入成功换票流程

### Requirement: OAuth 成功后通过一次性码换取 JWT
系统 SHALL 在 OAuth 回调成功后向前端交付短期一次性兑换码，前端再调用换票接口获取 access token；SHALL NOT 将长期 JWT 直接放在易被 Referer 泄露的持久 URL 中作为唯一交付方式。

#### Scenario: 换票成功
- **WHEN** 前端持有未过期且未使用的一次性码调用换票接口
- **THEN** 系统返回 access token 与用户基本信息，该一次性码失效

#### Scenario: 重复或过期码
- **WHEN** 前端使用已兑换或已过期的码换票
- **THEN** 系统拒绝并返回错误，不签发 token

### Requirement: OAuth state 防 CSRF
系统 SHALL 在授权发起时生成并校验 state；回调 state 不匹配时拒绝完成登录。

#### Scenario: state 不匹配
- **WHEN** 回调携带的 state 与发起时不一致或未知
- **THEN** 系统拒绝完成 OAuth 登录，不签发 token
