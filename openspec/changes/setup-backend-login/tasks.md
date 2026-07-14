## 1. 工程骨架

- [x] 1.1 创建 `backend/` Maven + Spring Boot 3.x（Java 17）工程与主启动类
- [x] 1.2 添加依赖：Web、Validation、Security、MyBatis-Plus、MySQL、JWT、OAuth2 Client（或 HTTP 客户端调 GitHub）、Lombok（若使用）
- [x] 1.3 建立包结构：`common` / `config` / `exception` / `auth` / `user` / `oauth`
- [x] 1.4 编写 `application.yml` 与 `application-local.yml.example`（数据源、端口、JWT、CORS、GitHub OAuth、前端回调 URL）
- [x] 1.5 补充 `backend/README.md`（启动、默认管理员、OAuth App 配置说明）

## 2. 基础设施与约定

- [x] 2.1 实现统一响应体 `Result` 与 `ErrorCode`
- [x] 2.2 实现 `BizException` 与 `GlobalExceptionHandler`
- [x] 2.3 配置 MyBatis-Plus（分页、逻辑删除、驼峰）与数据源
- [x] 2.4 配置 CORS 允许本地前端源
- [x] 2.5 实现 `BaseDO`（`create_date`/`update_date`/`create_by`/`update_by`）与 MetaObjectHandler 自动填充
- [x] 2.6 提供 `schema.sql` / `data.sql`（`sys_user`、`sys_user_oauth` 均含四审计字段 + 可选初始化管理员）

## 3. 用户领域

- [x] 3.1 定义 `UserDO`、`UserOauthDO`（继承 `BaseDO`）与对应 Mapper
- [x] 3.2 实现 `UserService`：按用户名查询、创建用户、状态校验、OAuth 绑定查询/创建

## 4. 登录、注册与鉴权

- [x] 4.1 实现 JWT 工具（签发、解析、过期）与可配置 secret/ttl
- [x] 4.2 配置 Spring Security：放行 register/login/oauth/health；PasswordEncoder 使用 BCrypt
- [x] 4.3 实现 JWT 过滤器，从 `Authorization: Bearer` 注入 SecurityContext
- [x] 4.4 实现 `POST /api/v1/auth/register`（校验、唯一性、哈希、签发 token）
- [x] 4.5 实现 `POST /api/v1/auth/login`（含无本地密码用户拒绝）
- [x] 4.6 实现 `GET /api/v1/auth/me` 与 `POST /api/v1/auth/logout`
- [ ] 4.7 用 curl 验证注册/登录成功、冲突、密码错误、无 token、me、logout

## 5. GitHub OAuth

- [x] 5.1 实现授权发起（生成 state 并缓存）与 GitHub authorize URL
- [x] 5.2 实现 callback：校验 state、换 GitHub access token、拉取用户信息
- [x] 5.3 首次授权自动建号并写 `sys_user_oauth`；再次授权复用绑定
- [x] 5.4 签发一次性换票码并重定向到前端回调 URL
- [x] 5.5 实现 `POST /api/v1/auth/oauth/exchange`（一次性码换 JWT）
- [ ] 5.6 端到端验证 GitHub OAuth（可用本地配置的 OAuth App）

## 6. 前端认证联调

- [x] 6.1 新增环境变量：`VITE_API_BASE_URL`、`VITE_AUTH_USE_API`；封装带 Bearer 的 HTTP 客户端
- [x] 6.2 实现 `authRepository`（API 实现；可选轻量 mock）并接入 `useAuth`
- [x] 6.3 新增 `LoginView` / `RegisterView` / `OAuthCallbackView` 与路由
- [x] 6.4 Header 增加登录入口、已登录用户展示与登出
- [x] 6.5 对接 register/login/me/logout 与 OAuth 发起/换票；验证「内容 Mock + 认证走 API」双模式
- [x] 6.6 更新 `frontend/README.md` 联调与 OAuth 说明

## 7. 收尾

- [x] 7.1 更新根 `README.md`（后端、认证联调、OAuth 配置入口）
- [x] 7.2 `mvn -q -DskipTests package` 与前端 `npm run build` 通过；文档警示默认密钥/口令
