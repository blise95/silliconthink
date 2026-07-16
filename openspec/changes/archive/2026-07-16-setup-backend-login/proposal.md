## Why

个人站点目前只有 Vue 前端与 Mock 数据，缺少可落地的后端与认证能力，无法进入内容管理与真实数据阶段。现在搭好 Java 后端底座，完成账号密码登录、开放注册与 OAuth，并与前端联调打通完整认证体验。

## What Changes

- 新增 Java 17 + Spring Boot 后端工程（独立于 `frontend/`），使用 MySQL 8.0 与 MyBatis-Plus
- 按 Nacos 源码风格与《阿里巴巴 Java 开发手册》约定分层、命名、包结构与异常/统一响应规范
- 实现登录：账号密码校验、签发访问凭证、登出、当前用户查询；未授权请求拦截
- 实现开放注册：访客可自助创建账号（校验唯一性与密码强度），注册成功后可登录
- 实现 OAuth：至少支持 GitHub；授权回调后绑定或自动建号并签发与本地登录一致的 JWT
- 前端联调：登录/注册页、OAuth 入口、token 持久化、Header 用户态、`auth` API 仓储；可通过配置对接真实后端
- 提供初始化 SQL（用户表、OAuth 绑定表等）与本地开发配置示例；**所有 MySQL 业务表固定包含** `create_date`、`update_date`、`create_by`、`update_by`（字段名不可替换）
- **本变更不包含**：博客/项目 CRUD、RBAC 细粒度权限、手机验证码、刷新令牌轮换

## Capabilities

### New Capabilities

- `backend-foundation`: 后端工程骨架、依赖、分层约定、统一响应/异常、数据源与 MyBatis-Plus 配置
- `auth-login`: 用户登录、令牌鉴权、登出与当前用户信息接口
- `auth-register`: 开放注册（账号密码自助开户）
- `auth-oauth`: 第三方 OAuth 登录（首版 GitHub）及账号绑定
- `frontend-auth`: 前端登录/注册/OAuth 联调、会话态与导航展示

### Modified Capabilities

- `public-site`: Header/导航增加登录入口与已登录用户态（登出），不改变首页信息架构主体

## Impact

- **新增目录**：`backend/`（Spring Boot 单模块）
- **前端改动**：`frontend/src` 新增认证页面、composables、services/api；路由与布局调整
- **基础设施**：MySQL 8.0；OAuth 需配置 GitHub Client ID/Secret 与回调 URL
- **安全**：密码 BCrypt；JWT；OAuth state 防 CSRF；默认管理员仅开发用
- **依赖**：Spring Boot 3.x、Spring Security、MyBatis-Plus、JWT；前端 `fetch`/`axios` 调用后端
