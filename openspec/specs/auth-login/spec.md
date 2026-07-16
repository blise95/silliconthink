# auth-login Specification

## Purpose
账号密码登录、JWT 鉴权、登出与密码安全存储。

## Requirements

### Requirement: 用户可使用账号密码登录
系统 SHALL 提供登录接口，校验用户名与密码；凭证正确且账号启用时返回访问令牌与基本用户信息。

#### Scenario: 登录成功
- **WHEN** 客户端向 `POST /api/v1/auth/login` 提交正确的用户名与密码
- **THEN** 响应成功，并返回可用于后续鉴权的 access token 以及用户标识与展示名等信息

#### Scenario: 密码错误
- **WHEN** 客户端提交存在的用户名但密码错误
- **THEN** 系统拒绝登录，不返回 access token，并返回表示认证失败的错误信息

#### Scenario: 账号禁用
- **WHEN** 客户端使用密码正确但状态为禁用的账号登录
- **THEN** 系统拒绝登录且不返回 access token

#### Scenario: 无本地密码的 OAuth 用户
- **WHEN** 仅通过 OAuth 创建、尚未设置本地密码的用户调用密码登录
- **THEN** 系统拒绝登录并提示使用 OAuth 或无法使用密码登录

### Requirement: 受保护接口需要有效令牌
系统 SHALL 要求除公开端点外的受保护 API 携带有效的 Bearer access token；缺失或无效令牌时拒绝访问。

#### Scenario: 无令牌访问受保护接口
- **WHEN** 客户端未携带 Authorization 头调用 `GET /api/v1/auth/me`
- **THEN** 系统返回未授权，且不返回用户详情

#### Scenario: 有效令牌访问当前用户
- **WHEN** 客户端携带有效 access token 调用 `GET /api/v1/auth/me`
- **THEN** 系统返回当前登录用户的基本信息（至少包含用户名或展示名）

#### Scenario: 无效令牌
- **WHEN** 客户端携带伪造或已损坏的 token 调用受保护接口
- **THEN** 系统拒绝访问并返回未授权结果

### Requirement: 用户可登出
系统 SHALL 提供登出接口；在携带有效令牌时登出成功，客户端应丢弃本地 token。

#### Scenario: 登出成功
- **WHEN** 已登录客户端调用 `POST /api/v1/auth/logout` 并携带有效 token
- **THEN** 系统返回成功响应

### Requirement: 密码安全存储
系统 SHALL 不以明文存储用户密码，须使用不可逆哈希（如 BCrypt）保存凭证；无本地密码的账号不存储明文占位密码。

#### Scenario: 数据库中无明文密码
- **WHEN** 查询已通过注册或初始化创建的用户的密码字段
- **THEN** 字段内容为哈希值，或对 OAuth-only 用户为空，且绝不为明文密码
