## ADDED Requirements

### Requirement: 后端工程可独立构建与启动
系统 SHALL 在仓库中提供基于 Java 17 与 Spring Boot 的后端工程，使用 Maven 构建，并可在配置好 MySQL 8.0 后成功启动 HTTP 服务。

#### Scenario: 本地构建成功
- **WHEN** 开发者在 `backend/` 目录执行 `mvn -q -DskipTests package`
- **THEN** 构建成功并产出可执行 jar（或 Boot 可运行产物）

#### Scenario: 应用启动并监听端口
- **WHEN** 开发者使用有效的数据源与 JWT 配置启动应用
- **THEN** 进程正常启动且 HTTP 端口可访问

### Requirement: 数据访问使用 MySQL 8.0 与 MyBatis-Plus
系统 SHALL 通过 MyBatis-Plus 访问 MySQL 8.0，并提供用户表的建表与初始化脚本。

#### Scenario: 初始化用户表
- **WHEN** 运维/开发执行项目提供的 SQL 初始化脚本
- **THEN** 数据库中存在 `sys_user`（或设计约定表名）且可插入/查询用户记录

### Requirement: 所有业务表必须具备固定审计字段
系统 SHALL 保证每一张 MySQL 业务表都包含且仅允许以如下字段名承载审计信息：`create_date`、`update_date`、`create_by`、`update_by`。字段名不可替换为其它别名（如 `created_at`），不可缺失；插入与更新时 SHALL 正确维护这些字段。

#### Scenario: 建表脚本包含四字段
- **WHEN** 审查本变更新增的任意业务表 DDL（含 `sys_user`、`sys_user_oauth`）
- **THEN** 表定义中同时存在 `create_date`、`update_date`、`create_by`、`update_by` 四列

#### Scenario: 插入时写入创建审计
- **WHEN** 通过应用创建一条新业务记录（如注册用户）
- **THEN** 该行的 `create_date` 与 `create_by` 已被赋值（无登录上下文时使用约定的系统标识）

#### Scenario: 更新时写入更新审计
- **WHEN** 通过应用更新一条已有业务记录
- **THEN** 该行的 `update_date` 与 `update_by` 被更新为当前时间与操作者（或系统标识）

### Requirement: 统一 API 响应与全局异常处理
系统 SHALL 对业务 API 返回统一 JSON 结构（至少包含业务码、消息、数据载荷），并对未捕获业务异常与参数错误给出一致错误响应。

#### Scenario: 成功响应结构
- **WHEN** 客户端调用一个成功的业务接口
- **THEN** 响应体包含表示成功的业务码、消息字段以及数据字段

#### Scenario: 业务失败响应结构
- **WHEN** 业务逻辑抛出可识别的业务异常
- **THEN** 响应体使用非成功业务码与可读错误消息，且不暴露内部堆栈给客户端

### Requirement: 代码分层与命名约定
后端代码 SHALL 按 controller / service / mapper（或 repository）/ model 分层组织，类命名与包结构遵循设计文档约定，避免 Controller 内直接编写数据访问逻辑。

#### Scenario: 登录入口不直接访问 Mapper
- **WHEN** 审查登录相关 Controller 实现
- **THEN** Controller 仅依赖 Service（或 Facade），不直接依赖 Mapper 执行 SQL
