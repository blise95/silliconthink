# auth-register Specification

## Purpose
开放注册：访客自助创建账号。

## Requirements

### Requirement: 访客可自助注册账号
系统 SHALL 提供开放注册接口，允许访客使用用户名与密码创建新账号；用户名全局唯一，密码须满足最小强度要求并以哈希存储。

#### Scenario: 注册成功并获得令牌
- **WHEN** 客户端向 `POST /api/v1/auth/register` 提交合法且未占用的用户名、符合强度的密码
- **THEN** 系统创建启用状态账号，返回成功，并签发与登录一致的 access token 及用户基本信息

#### Scenario: 用户名已存在
- **WHEN** 客户端使用已存在的用户名注册
- **THEN** 系统拒绝注册，不创建用户，不返回 access token

#### Scenario: 密码不符合强度
- **WHEN** 客户端提交过短或不满足规则的密码
- **THEN** 系统拒绝注册并返回可读的校验错误信息

### Requirement: 注册接口公开可访问
系统 SHALL 将注册接口列为公开端点，无需预先登录即可调用。

#### Scenario: 未登录调用注册
- **WHEN** 未携带 Authorization 的客户端调用注册接口并提交合法数据
- **THEN** 系统接受请求并按业务规则处理（成功或校验失败），不因缺少令牌而返回未授权
