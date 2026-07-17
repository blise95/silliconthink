## ADDED Requirements

### Requirement: 博客对象存储根可配置
系统 SHALL 通过配置（如 `BLOG_STORAGE_ROOT`）指定博客对象存储根目录；生产环境可为经 Tailscale 挂载的 NAS 路径，本地开发可为普通目录。该根目录语义为对象存储（按键读写文件），用于正文与媒体。

#### Scenario: 生产使用 NAS 挂载根
- **WHEN** 运维将存储根指向已挂载的 NAS 目录并启动后端
- **THEN** 新建文章正文与上传媒体写入该根下的约定对象键路径

#### Scenario: 本地使用本机目录
- **WHEN** 开发环境将存储根指向本机目录
- **THEN** 正文与媒体读写不依赖 NAS，行为与生产键约定一致

### Requirement: 正文以对象形式存储且 API 仍暴露 contentMd
系统 SHALL 将文章 Markdown 正文存为存储根下的对象（由元数据中的 `content_key` 引用）；创建/更新/详情 API 仍接受或返回 `contentMd` 字段，对客户端隐藏对象键细节。

#### Scenario: 创建后详情可读正文
- **WHEN** 作者创建或更新文章并提交非空 `contentMd`
- **THEN** 系统将正文写入对象存储，元数据保存对应 `content_key`，后续详情查询返回相同正文内容

#### Scenario: 列表不强制加载正文对象
- **WHEN** 客户端请求文章列表（公开或我的）
- **THEN** 系统可仅依赖 MySQL 元数据完成列表，不必为每条记录读取正文对象

### Requirement: 媒体与正文共用对象存储根
系统 SHALL 将上传图片写入同一对象存储根下的媒体键前缀，并返回可公开访问的 URL；URL 路径前缀保持既有约定（如 `/uploads`），不因迁到 NAS 而强制变更。

#### Scenario: 上传写入存储根 media 前缀
- **WHEN** 已登录用户上传合法图片且存储根可写
- **THEN** 文件落在存储根的 media 约定路径下，并返回可公开 GET 的 URL

### Requirement: 存储不可用时写操作可识别失败
系统 SHALL 在对象存储根不存在或不可写时拒绝正文保存与媒体上传，并返回可理解的业务错误。

#### Scenario: 存储根不可写时上传失败
- **WHEN** 存储根不可写且用户尝试上传图片
- **THEN** 系统拒绝上传并返回存储不可用类错误

#### Scenario: 存储根不可写时保存正文失败
- **WHEN** 存储根不可写且作者尝试创建或更新含正文的文章
- **THEN** 系统拒绝保存正文并返回存储不可用类错误

### Requirement: 部署实施文档覆盖 NAS 与服务衔接且含安全说明
项目 SHALL 提供可执行的部署实施文档（中英对等），覆盖：NAS 共享与访问控制、Tailscale、systemd 挂载、后端环境变量与服务对挂载点的依赖、Nginx 对 `/uploads` 的处理、正文迁移、上线验收与回滚，以及公开仓库脱敏与密钥存放位置。

#### Scenario: 按文档完成挂载与切换
- **WHEN** 运维在已能连通 NAS 的主机上按文档操作
- **THEN** 能够挂载、配置存储根与后端服务依赖，并将已有正文迁移为对象文件后验证详情可读

#### Scenario: 文档含服务与 Nginx 衔接
- **WHEN** 运维按文档配置生产后端与 Nginx
- **THEN** 文档说明 `BLOG_STORAGE_ROOT`/`UPLOAD_DIR`、systemd 对挂载点的依赖，以及 `/uploads` 反代或 alias 的可选方案

#### Scenario: 公开文档不含真实密钥与内网机密
- **WHEN** 审查仓库内 README 与 deploy 示例文件
- **THEN** 其中仅含占位符或 `.example` 配置，不包含真实 JWT/数据库密码/OAuth Secret/NAS 凭据或可识别的私人内网地址

### Requirement: 双语开源风格项目介绍文档
项目 SHALL 在仓库根目录提供英文 `README.md` 与中文 `README.zh-CN.md`，章节结构对等，内容对标常见开源项目：项目介绍、特性、架构、技术栈、快速开始、配置、部署入口、安全说明与文档索引。

#### Scenario: 根目录存在中英 README 且可互链
- **WHEN** 访客打开 GitHub 仓库首页或中文 README
- **THEN** 可见完整项目介绍，并可通过顶部链接切换到另一语言版本

#### Scenario: README 含安全与部署入口
- **WHEN** 读者浏览根 README 的 Security 与 Deployment 章节
- **THEN** 能了解密钥不进库、默认密码须修改、NAS 仅私网可达等要点，并找到详细部署文档链接
