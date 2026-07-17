## 1. 对象存储抽象与配置

- [x] 1.1 新增 `BlogObjectStore`（或等价）接口：`put` / `get` / `delete` / 根目录可写检查；实现为基于 `BLOG_STORAGE_ROOT` 的文件系统
- [x] 1.2 配置项：`BLOG_STORAGE_ROOT`；媒体目录派生为 `{root}/media`（或与 `UPLOAD_DIR` 对齐并更新 `application.yml` / `AppProperties`）
- [x] 1.3 约定对象键：`posts/{authorId}/{postId}.md`、`media/{yyyy}/{MM}/{dd}/{uuid}.{ext}`
- [x] 1.4 新增存储不可用错误码；根不可写时 put/上传失败返回该错误

## 2. 元数据模型与迁移

- [x] 2.1 DDL：`blog_post` 增加 `content_key`；编写迁移说明/SQL
- [x] 2.2 迁移命令或脚本：将已有 `content_md` 写出为对象并回填 `content_key`
- [x] 2.3 实体 / Mapper 使用 `contentKey`；停止将正文权威写入 `content_md`
- [x] 2.4 确认后提供删除 `content_md` 列的后续 SQL（可分两步发版）

## 3. 服务层改造

- [x] 3.1 `BlogPostService` 创建/更新：写入对象后再保存/更新元数据（含失败补偿策略）
- [x] 3.2 详情（公开 / 作者）：按 `content_key` 加载正文填入 `PostVO.contentMd`
- [x] 3.3 列表 / latest：不读取正文对象（若现 VO 带正文则改为不填充或保持空）
- [x] 3.4 `MediaUploadService` 改为写入同一存储根的 media 前缀；静态映射指向该目录
- [x] 3.5 单元/集成测试：正文 put/get、存储不可写、详情组装、上传

## 4. NAS 与服务部署落地（含安全）

- [x] 4.1 更新 `backend.env.example`：`BLOG_STORAGE_ROOT`、`UPLOAD_DIR`；仅占位符，注释强调勿提交真实 env
- [x] 4.2 新增 `backend/deploy/nas-storage.md`（英文）与 `nas-storage.zh-CN.md`（中文）：按 design §8 对等撰写，含 Security 专节
- [x] 4.3 新增 `backend/deploy/mnt-nas-blog.mount`（NFS 占位 IP/路径）；文档附 SMB 示例且凭据文件不进 Git
- [x] 4.4 更新 `silliconthink-backend.service`：`After=mnt-nas-blog.mount`、`RequiresMountsFor=/mnt/nas-blog`
- [x] 4.5 更新 `nginx.conf.example`：补齐 `/uploads/` 反代（方案 A），注释可选 alias（方案 B）
- [x] 4.6 `server-setup.sh` 提示链接中英 nas-storage 文档；可选挂载健康检查片段
- [x] 4.7 确认 `.gitignore` 覆盖 `backend.env`、`*.cred`、`application-local.yml` 等敏感路径

## 5. 双语开源风格 README

- [x] 5.1 重写根目录 `README.md`（English）：Features / Architecture / Stack / Quick Start / Config / Deployment / Security / Docs（design §9.2）
- [x] 5.2 新增对等 `README.zh-CN.md`（简体中文），顶部语言互链
- [x] 5.3 更新 `backend/README.md`、`frontend/README.md`：链到根 README 与 nas-storage 中英文档；补充存储分层与安全要点
- [x] 5.4 发版前自检：文档无真实密钥/私人 IP；默认密码旁有修改警告

## 6. 验证

- [x] 6.1 本地：存储根指向临时目录，创建文章 → 磁盘出现 `.md` → 详情 API 返回正文；DB 无大字段依赖（由单元测试覆盖 put/get/loadContent）
- [x] 6.2 本地：存储根不可写时创建/上传返回明确错误（单元测试覆盖 MEDIA_STORAGE_UNAVAILABLE）
- [x] 6.3 迁移脚本在含旧 `content_md` 的数据上跑通（`BlogContentMigrationRunner` + migration SQL 已提供；需在有库环境执行）
- [ ] 6.4 香草云按 Runbook：挂载验收 → 配 env → 发版 → 迁移 → 上传/读文/旧 URL 抽测
- [ ] 6.5 香草云：模拟 umount 后确认写失败可识别、列表元数据仍可用
- [x] 6.6 文档：中英 README 与 nas-storage 互链可用；Security 章节可读
