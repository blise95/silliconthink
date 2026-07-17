## Why

香草云磁盘有限，家里 NAS 已通过 Tailscale 可达。博客正文与图片体积大、适合对象存储；列表筛选、状态、所有权等仍适合关系库。把 NAS 当作类 OSS 对象存储存放正文与媒体，MySQL 只保留元数据，可兼顾容量、备份与现有 API 查询能力。仓库已公开在 GitHub，说明文档需同时服务中英文读者，并写清安全边界与可执行部署步骤，避免把密钥或内网拓扑写进公开仓库。

## What Changes

- **存储分层**：NAS（挂载目录，语义等同对象存储）存 Markdown 正文文件与上传媒体；MySQL 只存文章元数据（标题、slug、摘要、状态、作者、标签关联、正文对象键、封面 URL 等）
- 后端引入可配置的对象存储根（`BLOG_STORAGE_ROOT` / 挂载点）；本地开发指向本机目录，生产指向 Tailscale 挂载的 NAS
- `blog_post` 不再把完整 `content_md` 作为权威数据源；改为存 `content_key`（对象路径），读写时经存储层加载/落盘；对外 API 仍收发 `contentMd`（对前端透明）
- 图片上传写入同一存储根下的 media 前缀；公开 URL 前缀保持 `/uploads`（或统一在存储根下映射）
- **NAS + 香草云部署实施方案**（含安全）：共享 ACL、Tailscale、systemd、env、Nginx、Runbook；示例文件不含真实 IP/密钥
- **开源风格双语文档**：根目录 `README.md`（英文）+ `README.zh-CN.md`（中文）；部署说明中英对等；含项目介绍、架构、快速开始、配置、部署、安全
- 存储不可用时写操作返回明确错误
- **不做**：把 MySQL 数据目录放到 NAS；引入云 OSS/S3 SDK；前端 API 字段大改；在 Git 中提交真实凭据、内网地址或私人拓扑图

## Capabilities

### New Capabilities
- `blog-nas-storage`: 将 NAS 挂载为博客对象存储根；正文/媒体对象键约定；挂载与健康约束；**含安全的部署实施与双语项目文档**

### Modified Capabilities
- `blog-persistence`: 正文权威数据迁到对象存储；MySQL 仅元数据 + `content_key`；媒体写入同一存储根；公开/作者 API 行为与所有权规则保持

## Impact

- **后端**：存储层、DDL/迁移、上传与静态映射
- **运维**：Tailscale 挂载、systemd、Nginx、env 示例（脱敏）
- **文档**：根 README 中英、`backend/deploy/nas-storage` 中英、Security 专节；子项目 README 交叉链接
- **安全**：公开仓库只保留 `.example`；真实 `backend.env` / NAS IP / SMB 凭据仅存服务器
- **前端**：API 契约基本不变
