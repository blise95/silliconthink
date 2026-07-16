## Context

仓库当前仅有 Vue 3 前端（Mock 数据）与 OpenSpec 规格；无 Java 后端，无登录/注册能力。本设计落地后端骨架、账号密码登录、开放注册、GitHub OAuth，并与前端完成联调。风格约束参考 Nacos 源码与《阿里巴巴 Java 开发手册》。

## Goals / Non-Goals

**Goals:**

- 新增可独立构建运行的 Spring Boot 后端（Java 17 + MySQL 8 + MyBatis-Plus）
- 全库表统一审计字段：`create_date`、`update_date`、`create_by`、`update_by`（字段名固定）
- 账号密码登录 / 登出 / 当前用户；Bearer JWT 保护受保护接口
- 开放注册（自助开户）
- GitHub OAuth 登录（授权码模式），回调后签发同一套 JWT
- Vue 前端：登录/注册页、OAuth 按钮、token 存储、Header 用户态，对接真实 API

**Non-Goals:**

- 博客、项目内容的 CRUD 与完整管理后台
- 手机/邮箱验证码、找回密码、刷新令牌轮换、多端踢下线
- 细粒度 RBAC（本阶段仅「已登录 / 未登录」）
- 除 GitHub 外的其他 OAuth 提供商（接口可扩展，本变更只交付 GitHub）

## Decisions

### 1. 工程形态：单模块 `backend/`

- **选择**：`backend/` 下 Spring Boot 3.x + Maven 单模块（JDK 17）
- **理由**：个人站点体量小；包内按 `controller` / `service` / `mapper` / `config` / `exception` / `common` 分层即可
- **备选**：多模块 — 能力增多后再拆

### 2. 技术栈版本基线

| 组件 | 选择 | 说明 |
|------|------|------|
| JDK | 17 | 用户指定 |
| Spring Boot | 3.2.x / 3.3.x LTS 线 | Jakarta EE |
| MySQL | 8.0 | utf8mb4 |
| ORM | MyBatis-Plus 3.5.x | |
| 安全 | Spring Security + JWT | 无状态 |
| OAuth | 自建授权码流程或 Spring Security OAuth2 Client | 首版 GitHub |
| 构建 | Maven | |
| 前端 | Vue 3 现有栈 + 轻量 HTTP client | |

### 3. 认证方案：统一 JWT

- 本地登录、注册后登录、OAuth 回调成功后，均签发同一格式 Access Token（默认 2h，可配置）
- 客户端统一：`Authorization: Bearer <token>`；前端存 `localStorage`（或 `sessionStorage`，默认 localStorage）
- 密码：BCrypt；OAuth 用户无本地密码时 `password_hash` 可为空，禁止密码登录直至设置密码（首版可不支持设密，仅 OAuth/已有密码用户密码登录）

### 4. API 约定

- 前缀：`/api/v1`
- 统一响应：`{ code, message, data }`（`code=0` 成功）
- 端点：
  - `POST /api/v1/auth/register` — 公开
  - `POST /api/v1/auth/login` — 公开
  - `POST /api/v1/auth/logout` — 需登录
  - `GET /api/v1/auth/me` — 需登录
  - `GET /api/v1/auth/oauth/github/authorize` — 公开，返回/重定向到 GitHub 授权 URL（含 state）
  - `GET /api/v1/auth/oauth/github/callback` — 公开，处理 code，成功后重定向到前端并携带一次性 code 或 fragment token（见下）
- 健康检查：`GET /api/v1/health` 公开

### 5. OAuth 回调如何把 token 交给前端

- **选择**：回调成功后 **302 重定向** 到前端约定页，例如  
  `https://frontend/oauth/callback?token=<jwt>`（开发环境 localhost）  
  或更稳妥：`?code=<oneTimeCode>`，前端再用 `POST /api/v1/auth/oauth/exchange` 换 JWT（防 Referer 泄露）
- **本变更采用**：`oneTimeCode` 换票（TTL 短、一次性），避免 JWT 出现在浏览器历史与 Referer
- state：服务端缓存（内存即可，单机）校验，防 CSRF

### 6. 开放注册规则

- 字段：`username`（4–32，字母数字下划线）、`password`（8–64）、可选 `displayName`
- 用户名唯一；默认 `status=启用`
- 注册成功：返回与登录相同结构（直接签发 token）或仅提示去登录——**选择直接签发 token**，减少一步
- 限流：首版不做 IP 限流（文档标明生产需加）；依赖密码强度与唯一约束

### 7. 包结构（扩展）

```
com.silliconthink
├── common/ config/ exception/
├── auth/
│   ├── controller/     # AuthController, OAuthController
│   ├── service/        # AuthService, OAuthService
│   ├── dto/
│   └── security/       # JwtFilter, ...
├── user/
│   ├── entity/         # UserDO, UserOauthDO
│   ├── mapper/
│   └── service/
└── oauth/              # 可选：GitHubClient 等基础设施
```

前端：

```
frontend/src/
├── pages/LoginView.vue, RegisterView.vue, OAuthCallbackView.vue
├── composables/useAuth.ts
├── services/authRepository.ts + api/mock 实现
├── router 增加 /login /register /oauth/callback
└── components/layout/AppHeader 用户态
```

### 8. 数据模型

#### 8.1 全表审计字段（不可变更约定）

**所有 MySQL 业务表 MUST 包含以下四个字段，字段名与语义固定，不得替换为 `created_at` / `updated_at` 等别名，不得省略：**

| 字段 | 类型（建议） | 说明 |
|------|--------------|------|
| `create_date` | DATETIME / TIMESTAMP | 创建时间 |
| `update_date` | DATETIME / TIMESTAMP | 最后更新时间 |
| `create_by` | VARCHAR / BIGINT | 创建人（用户 ID 或系统标识，如 `system`） |
| `update_by` | VARCHAR / BIGINT | 最后更新人 |

实现约定：

- Entity 抽公共基类（如 `BaseDO`）承载四字段；MyBatis-Plus 元数据自动填充（插入填 `create_date`/`create_by`，更新填 `update_date`/`update_by`）
- 无登录上下文（注册、OAuth 建号、初始化脚本）时：`create_by` / `update_by` 使用约定系统值（如 `0` 或 `system`），类型在实现中统一（推荐 `BIGINT`，系统操作用 `0`）
- 后续新增任何表（博客、项目等）同样遵守，不在本变更例外

#### 8.2 `sys_user`

允许 `password_hash` 为空（OAuth-only 用户）。业务字段示例：`id`、`username`、`password_hash`、`display_name`、`status`、逻辑删除字段（若需要）——**另加** `create_date`、`update_date`、`create_by`、`update_by`。

#### 8.3 `sys_user_oauth`

| 字段 | 说明 |
|------|------|
| id | PK |
| user_id | 关联 sys_user |
| provider | 如 `github` |
| provider_user_id | 第三方唯一 ID |
| provider_username | 可选展示 |
| create_date / update_date / create_by / update_by | 强制审计字段 |

唯一索引：`(provider, provider_user_id)`。

绑定策略：首次 OAuth → 自动创建用户（displayName 取 GitHub login）+ 绑定行；再次 OAuth → 找到绑定用户发 JWT。不强制与本地用户名合并（减少复杂度）。

### 9. 配置与环境

- 后端：`app.jwt.*`、`app.oauth.github.client-id/secret/redirect-uri`、`app.oauth.frontend-callback-url`
- 前端：`VITE_API_BASE_URL`、`VITE_USE_MOCK`（认证相关在联调时走真实 API；内容 Mock 可仍开启——**决策**：`VITE_USE_MOCK` 控制内容仓储；认证仓储单独用 `VITE_API_BASE_URL` 是否配置来决定，或增加 `VITE_AUTH_USE_API=true`）
- **选择**：增加 `VITE_AUTH_USE_API`（默认开发联调为 true 时需后端）；未开启时可用轻量 mock 便于纯前端预览
- CORS：允许前端 origin

## Risks / Trade-offs

- [JWT 无法即时吊销] → logout 客户端删 token；OAuth oneTimeCode 短 TTL
- [开放注册被滥用] → 首版无验证码；生产前应加限流/人机校验（另开变更）
- [OAuth token 泄露窗口] → 用 oneTimeCode 换票，不用 URL 直接挂 JWT
- [GitHub 应用配置错误] → README 写清回调 URL 与环境变量
- [内容仍 Mock、认证走真实 API] → 双模式文档说清，避免混淆

## Migration Plan

1. 建库表 + 启动后端，curl 验证 register/login/me
2. 配置 GitHub OAuth App，验证授权回调与换票
3. 前端打开 `VITE_AUTH_USE_API`，联调登录/注册/OAuth/Header
4. 回滚：关前端开关即可回 Mock；删 `backend/` 与表不影响静态内容浏览

## Open Questions

- 是否同时支持 Google OAuth — **本变更不做**，仅 GitHub，接口预留 provider
- 注册是否强制邮箱 — **否**，首版仅用户名密码
- 刷新令牌 — **不做**
