## Context

当前实现：

- `blog_post.content_md` 整篇 Markdown 存在 MySQL（`MEDIUMTEXT`）
- 图片经 `MediaUploadService` 写入 `app.upload.dir`（默认本地 `uploads`）
- 香草云已能经 Tailscale ping 通家里 NAS

目标架构（用户明确）：

- **NAS ≈ OSS**：存博客「主要内容」——Markdown 正文文件 + 媒体二进制
- **MySQL ≈ 元数据索引**：标题、slug、摘要、状态、作者、标签、对象键、封面 URL、发布时间等，支撑列表/筛选/权限

对外 REST 仍返回 `contentMd` 字符串，对前端透明。

## Goals / Non-Goals

**Goals:**

- 正文与媒体以对象键落在可配置存储根（生产 = NAS 挂载；开发 = 本地目录）
- MySQL 不再作为正文权威存储；仅元数据 + `content_key`
- 列表/筛选/所有权仍走 MySQL；详情/编辑组装时再读对象
- 迁移既有 `content_md` 行到对象文件
- 给出可执行的 **NAS + 香草云服务部署方案**（共享、Tailscale、systemd、backend.env、Nginx、Runbook）
- 按主流开源项目结构提供 **中英双语 README / 部署说明**，含项目介绍、架构、实施步骤与 **Security**（公开仓库脱敏）
- 存储不可用时写失败可识别

**Non-Goals:**

- 接入阿里云 OSS / S3 兼容 API（首版文件系统模拟；接口可预留以便日后换驱动）
- MySQL datadir 放到 NAS
- 改前端字段名或公开 URL 大规模变更
- 全文搜索引擎、CDN、多活副本
- 在公开文档中写死真实 Tailscale IP、家宽拓扑、账号密码

## Decisions

### 1. 职责划分

| 层 | 存什么 | 不存什么 |
|----|--------|----------|
| MySQL | `id`, `author_id`, `title`, `slug`, `summary`, `content_key`, `cover_url`, `status`, `published_at`, 标签关联, 审计/软删 | 正文全文、图片二进制 |
| NAS 对象存储 | `posts/.../*.md` 正文；`media/...` 图片 | 业务索引字段（避免只扫盘做分页） |

列表、按 tag/keyword（标题/摘要）筛选继续用 SQL；**不对正文做 DB LIKE**（若以后要搜正文，再加索引或扫对象，首版不承诺）。

### 2. 用「挂载目录」模拟 OSS，而不是云 SDK

```
BLOG_STORAGE_ROOT=/mnt/nas-blog   # 生产 NAS
                 ./data/blog-storage  # 本地开发
```

抽象：

```text
BlogObjectStore
  put(key, bytes/string)
  get(key) -> Optional
  delete(key)           # 软删文章时可保留或延后清理
  exists / isWritable(root)
```

- **理由**：NAS 已是文件共享；挂载后与本地路径同构，零厂商绑定。
- **备选**：直接 S3 SDK 指 MinIO on NAS — 运维更重，首版不选；接口形状保持可替换。

### 3. 对象键约定

```text
{BLOG_STORAGE_ROOT}/
  posts/{authorId}/{postId}.md     # 正文（UTF-8 Markdown，无 frontmatter 也可；元数据在 DB）
  media/{yyyy}/{MM}/{dd}/{uuid}.{ext}
```

- 创建文章：先插元数据得 `postId`，再 `put` 正文；失败则补偿删行或标错误（事务外；文档说明）
- 更新正文：覆盖同 key
- `content_key` 存相对键，如 `posts/1/42.md`（不要存绝对路径，便于换挂载点）
- 媒体 URL：仍映射 `GET /uploads/**` → `media/**` 或 `BLOG_STORAGE_ROOT/media`（与现网一致可把 public 前缀指到 media 子树）

### 4. 表结构变更

`blog_post`：

| 变更 | 说明 |
|------|------|
| 新增 `content_key` VARCHAR | 相对对象键；草稿亦可先有空文件或占位 |
| 废弃 `content_md` | 迁移脚本：逐行写出文件并填 `content_key`，确认后删列 |

迁移步骤（实现任务细化）：

1. 加可空 `content_key`
2. 批处理导出 `content_md` → 对象，回填 key
3. 应用改为只读写对象
4. 删 `content_md` 列（或先保留只读备份一版）

### 5. 服务读写路径

```text
Create/Update:
  API contentMd → BlogObjectStore.put(content_key) → 更新元数据行

Get detail / edit:
  元数据行 → Store.get(content_key) → VO.contentMd

List / latest:
  仅 SQL 元数据；contentMd 可空或不查（与现 PostVO 列表是否带正文对齐：现若列表不带长文则保持）
```

检查当前列表 VO 是否带 contentMd——设计上列表应不读对象，节省 NAS I/O。

### 6. 配置变量（应用）

| 变量 | 含义 | 生产示例 |
|------|------|----------|
| `BLOG_STORAGE_ROOT` | 对象存储根（NAS 挂载点） | `/mnt/nas-blog` |
| `UPLOAD_DIR` | 媒体目录；派生或显式等于 `{root}/media` | `/mnt/nas-blog/media` |

本地开发：`BLOG_STORAGE_ROOT=./data/blog-storage`（不依赖 Tailscale）。

### 7. API 兼容

- 请求/响应继续用 `contentMd`
- 前端无感；仅后端持久化换层

### 8. NAS 与香草云部署实施方案

本节是运维落地的权威步骤；实现时写入 `backend/deploy/nas-storage.md`，并与现有 `server-setup.sh` / systemd / Nginx 对齐。

#### 8.1 拓扑

```text
访客 ──HTTPS──► 香草云 Nginx
                  ├─ /          前端静态
                  ├─ /api/      → Spring Boot :8080
                  └─ /uploads/  → 反代到后端 或 直接 alias 到挂载的 media
                                  │
                     Spring Boot (www-data)
                                  ├─ MySQL（本机元数据）
                                  └─ BLOG_STORAGE_ROOT=/mnt/nas-blog
                                          │ NFS/CIFS over Tailscale
                                          ▼
                                   家里 NAS 共享目录
                                   posts/ + media/
```

原则：

- **公网只暴露香草云**；NAS 不对公网开 NFS/SMB，仅 Tailscale 网段可达。
- MySQL 仍在香草云；NAS 只当对象盘。
- 挂载失败时：列表/登录等元数据 API 可仍可用；正文读写与上传失败并返回明确错误。

#### 8.2 NAS 侧准备

1. 在 NAS 创建专用共享，例如 `silliconthink-blog`（群晖 Shared Folder / TrueNAS Dataset）。
2. 共享内预建（或由应用首次 `put` 自动创建）：
   - `posts/`
   - `media/`
3. 开启 **NFS**（首选）或 SMB：
   - NFS：导出路径对香草云的 **Tailscale IP**（或整个 `100.64.0.0/10`）授权 `rw`；关闭「对所有人可写」的广域授权。
   - 映射：尽量让远端 UID 与云主机 `www-data`（常见 uid 33）可写；群晖可用「全部用户映射为 admin」仅作临时，生产建议固定 map。
4. 防火墙 / 安全组：NFS 端口仅允许 Tailscale 接口，**禁止**对公网 `0.0.0.0/0`。
5. 可选：对该共享开启 NAS 快照 / 版本历史，作为媒体与正文的备份。

记录下来供云侧填写：

| 项 | 示例（占位） |
|----|----------------|
| NAS Tailscale IP 或 MagicDNS | `100.x.y.z` / `nas.tailnet.ts.net` |
| 导出路径 | `/volume1/silliconthink-blog` |
| 协议 | NFS v4（或 SMB） |

#### 8.3 香草云：Tailscale

1. 安装并登录：`curl -fsSL https://tailscale.com/install.sh | sh` → `tailscale up`。
2. 验证：`tailscale status`；`ping <nas-ip>`（用户已具备则可跳过）。
3. 建议：Tailscale ACL 仅允许香草云节点访问 NAS 的 NFS/SMB 端口；家用设备互访按现网策略。
4. `tailscaled.service` 开机自启；挂载 unit 依赖它（见下）。

#### 8.4 香草云：挂载（systemd）

安装客户端：`apt install -y nfs-common`（SMB 则 `cifs-utils`）。

挂载点：`/mnt/nas-blog`（空目录；**切勿**在未挂载时往里写，否则挂载后本地文件被遮住）。

提供仓库示例 `backend/deploy/mnt-nas-blog.mount`：

```ini
[Unit]
Description=Silicon Think blog object storage (NAS via Tailscale)
After=network-online.target tailscaled.service
Wants=network-online.target tailscaled.service

[Mount]
What=100.x.x.x:/volume1/silliconthink-blog
Where=/mnt/nas-blog
Type=nfs
Options=_netdev,nofail,soft,timeo=50,retrans=2,nfsvers=4

[Install]
WantedBy=multi-user.target
```

操作：

```bash
sudo mkdir -p /mnt/nas-blog
# 编辑 What= 为真实 NAS 导出
sudo cp backend/deploy/mnt-nas-blog.mount /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now mnt-nas-blog.mount
findmnt /mnt/nas-blog
sudo -u www-data touch /mnt/nas-blog/.write-test && sudo -u www-data rm /mnt/nas-blog/.write-test
```

选项说明：

| 选项 | 作用 |
|------|------|
| `_netdev` | 等网络再挂 |
| `nofail` | 挂载失败不阻塞开机 |
| `soft` | I/O 失败可返回错误，避免进程永久卡住 |
| `RequiresMountsFor=/mnt/nas-blog` | 用在后端 service，保证有挂载再跑业务（见 8.5） |

SMB 附录（设计文档级）：`What=//100.x.x.x/silliconthink-blog`，`Type=cifs`，`Options=_netdev,nofail,uid=33,gid=33,credentials=/etc/silliconthink/nas.cred`；凭据文件 `chmod 600`。

#### 8.5 香草云：后端服务部署衔接

与现有布局对齐：

| 路径 | 用途 |
|------|------|
| `/opt/silliconthink` | 代码仓库 |
| `/opt/silliconthink-runtime/app.jar` | 运行包 |
| `/etc/silliconthink/backend.env` | 环境变量 |
| `/etc/silliconthink/application-prod.yml` | 额外 YAML |
| `silliconthink-backend.service` | systemd |

**`backend.env` 增补：**

```bash
BLOG_STORAGE_ROOT=/mnt/nas-blog
UPLOAD_DIR=/mnt/nas-blog/media
```

**`silliconthink-backend.service` 调整：**

```ini
[Unit]
Description=Silicon Think Backend
After=network-online.target mysql.service mnt-nas-blog.mount
Wants=network-online.target
RequiresMountsFor=/mnt/nas-blog

[Service]
User=www-data
Group=www-data
EnvironmentFile=-/etc/silliconthink/backend.env
# ... 其余保持现有 ExecStart / 日志约定
```

发版流程（在现有 `backend/deploy/update.sh` 之上）：

1. `systemctl is-active mnt-nas-blog.mount`（或 `findmnt`）通过再更新。
2. `update.sh` 构建并重启后端（不变）。
3. 首次切换对象存储：跑迁移（把 `content_md` 导出到 `/mnt/nas-blog/posts/...`）后再切流量验证。

`server-setup.sh` 完成提示中增加一行：博客对象存储见 `nas-storage.md`。

#### 8.6 Nginx 与静态媒体

两种等价方案（二选一，文档写清）：

**A（推荐，改动小）**：`/uploads/` 继续反代到 Spring Boot，由 `WebConfig` 从 `UPLOAD_DIR` 读文件（已支持可配置目录）。

**B（减负后端）**：Nginx `alias /mnt/nas-blog/media/` 直接出图；需保证挂载对 `www-data`/nginx 用户可读，且路径与上传返回的 URL 一致。

示例（方案 B）：

```nginx
location /uploads/ {
    alias /mnt/nas-blog/media/;
    access_log off;
    expires 7d;
}
```

若用方案 A，现有 `nginx.conf.example` 为 `/api/` 反代即可；需补一段把 `/uploads/` 也 `proxy_pass` 到 `:8080`（若当前未配）。

#### 8.7 上线切换 Runbook

1. **NAS + 挂载**：完成 8.2–8.4；`www-data` 可写测试通过。
2. **发版 1**：上线存储抽象 + `content_key`；读路径 key 优先、否则回退 `content_md`；写路径双写或只写对象+key。
3. **迁移窗口**：停写或短维护；执行迁移脚本；抽查若干 `content_key` 文件与详情 API。
4. **发版 2**：只读对象；不再写 `content_md`。
5. **发版 3（可选）**：DDL 删除 `content_md`。
6. **验收**：新建文章见 NAS 上 `.md`；上传图片见 `media/`；公开详情与旧链接；故意 `umount` 后确认写失败信息、列表仍可用。

#### 8.8 日常运维与故障

| 场景 | 处理 |
|------|------|
| Tailscale 断线 | 恢复 `tailscaled`；挂载自动或 `systemctl restart mnt-nas-blog.mount` |
| 挂载成功但不可写 | 查 NFS map / 目录 owner；`sudo -u www-data touch ...` |
| 误在未挂载目录写入 | 卸载前备份 `/mnt/nas-blog` 本地残留，挂载后合并到 NAS |
| 回滚应用 | `BLOG_STORAGE_ROOT` 可临时改回本机盘并 rsync；或恢复读 `content_md`（删列前） |
| 备份 | MySQL 逻辑备份在云；`posts/`+`media/` 靠 NAS 快照 |

健康检查（可脚本化进 deploy）：

```bash
findmnt /mnt/nas-blog >/dev/null && \
  sudo -u www-data test -w /mnt/nas-blog && \
  curl -sf http://127.0.0.1:8080/api/v1/posts?page=1 >/dev/null
```

#### 8.9 交付物清单（实现必须落盘）

| 交付物 | 说明 |
|--------|------|
| `backend/deploy/nas-storage.md` | 英文部署实施（或主文件 + 语言切换） |
| `backend/deploy/nas-storage.zh-CN.md` | 中文部署实施（与英文内容对等） |
| `backend/deploy/mnt-nas-blog.mount` | systemd 挂载示例（占位 IP/路径） |
| `backend/deploy/backend.env.example` | 含 `BLOG_STORAGE_ROOT` / `UPLOAD_DIR`，无真实密钥 |
| `backend/deploy/silliconthink-backend.service` | `After` / `RequiresMountsFor` |
| `frontend/deploy/nginx.conf.example` | `/uploads/` 反代或 alias 注释说明 |
| 迁移脚本/命令 | DB `content_md` → NAS 对象 |
| `README.md` / `README.zh-CN.md` | 见 §9 |

#### 8.10 公开仓库安全约束（实施文档必须遵守）

GitHub 公开仓库中的说明类文件 SHALL 遵守：

| 允许 | 禁止 |
|------|------|
| 占位符：`100.x.x.x`、`YOUR_NAS_EXPORT`、`change-me` | 真实 Tailscale IP、MagicDNS 主机名（若可推断家庭身份） |
| `.example` / `.mount` 模板 | 真实 `JWT_SECRET`、DB 密码、OAuth Secret、SMB credentials |
| 「仅 Tailscale 网段开放 NFS」等原则 | 把完整家用网络拓扑图当广告贴出 |
| 默认口令说明 + **必须修改** 警告 | 暗示生产仍使用文档中的默认密码 |

服务器侧真实值只写在：

- `/etc/silliconthink/backend.env`（`chmod 640`，不进 Git）
- systemd 覆盖或本地未跟踪文件
- NAS 管理界面 / Tailscale ACL 控制台

文档中单独设 **Security** 小节，列出上述清单与「泄露后轮换密钥」提示。

### 9. 双语项目文档（开源 README 风格）

仓库在 GitHub，文档对标常见优质开源项目（如 Vite / Spring Boot 样例仓）的信息架构，而不是只有几行启动命令。

#### 9.1 文件约定

| 文件 | 语言 | 角色 |
|------|------|------|
| `README.md` | English | GitHub 默认首页：项目介绍 + 链到中文版 |
| `README.zh-CN.md` | 中文 | 与英文版章节对等的完整说明 |
| `backend/README.md` | 可中英择一为主，顶部互链 | 后端细节；部署深链到 `deploy/nas-storage*.md` |
| `frontend/README.md` | 同上 | 前端细节 |
| `backend/deploy/nas-storage.md` | English | NAS≈OSS 部署实施全文 |
| `backend/deploy/nas-storage.zh-CN.md` | 中文 | 与英文对等 |

根 README 顶部语言切换示例：

```markdown
**English** | [简体中文](./README.zh-CN.md)
```

```markdown
[English](./README.md) | **简体中文**
```

#### 9.2 根 README 推荐目录（中英结构一致）

1. **Title + one-liner** — Silicon Think：个人站点（博客 / 作品集 / 认证）
2. **Badges（可选）** — License、Java 17、Vue 3（若有 CI 再加 build）
3. **Features** — 公开博客、作者写作台、JWT/OAuth、元数据在 MySQL、正文与媒体在可挂载对象存储（NAS）
4. **Architecture** — 简图：Browser → Nginx → Vue / API → MySQL + `BLOG_STORAGE_ROOT`（NAS via Tailscale）
5. **Tech Stack** — Vue 3、Spring Boot 3、MySQL 8、OpenSpec
6. **Repository layout** — `frontend/` `backend/` `openspec/` `deploy/`
7. **Quick Start** — 本地前后端最短路径（链到子 README）
8. **Configuration** — 关键环境变量表（脱敏示例）
9. **Deployment** — 香草云单节点摘要 + 链到 `nas-storage` 完整实施
10. **Security** — 密钥不进库、改默认管理员密码、NAS/NFS 仅私网、上报漏洞方式（可简写 Issues）
11. **Documentation** — 链到 backend/frontend/openspec/deploy
12. **License** — 若已有则声明；无则注明 personal / TBD

中英文内容应对等，避免「英文只有三行、中文一篇长文」。

#### 9.3 与本变更的衔接文案（写入 README Features / Architecture）

- Blog **metadata** (title, slug, status, tags, `content_key`) → MySQL  
- Blog **body & media** → object storage root (local dir or NAS mount over Tailscale)  
- Production mount & systemd → see deploy docs（勿在 README 贴真实 IP）

#### 9.4 子文档与安全性检查清单（发 PR 前）

- [ ] 无真实密钥、token、私钥、连接串密码
- [ ] NAS 示例均为占位符
- [ ] 默认账号密码旁有「生产必须修改」
- [ ] `.gitignore` 已忽略 `application-local.yml`、`backend.env`、`*.cred`
- [ ] 中英 README 均含 Security 与 Deployment 入口

## Risks / Trade-offs

- **[Risk] 公开 GitHub 文档泄露内网地址或密钥** → Mitigation：§8.10 脱敏规范；仅 `.example`；PR 检查清单；Security 专节。
- **[Risk] NAS/Tailscale 断线 → 详情与上传失败，列表元数据仍可读** → Mitigation：写/读正文明确错误；列表不依赖对象；`nofail`+`soft`；健康检查脚本。
- **[Risk] 未挂载时写入挂载点本地目录，挂载后文件「消失」** → Mitigation：`RequiresMountsFor`；上线检查清单禁止未挂载写；文档强调空挂载点。
- **[Risk] 先写 DB 后写对象失败 → 有元数据无正文** → Mitigation：创建时事务外补偿；或先写临时 key 再提交；更新失败保留旧对象。
- **[Risk] UID 映射导致 www-data 不可写** → Mitigation：Runbook 含 `touch` 验收；SMB 用 uid/gid=33。
- **[Risk] 双写迁移期不一致** → Mitigation：一次性迁移 + 维护窗口；迁移后只走对象。
- **[Trade-off] keyword 不搜正文** → 首版只搜 title/summary。
- **[Trade-off] 文件系统非真 OSS** → 单机挂载足够；接口可日后换 S3 驱动。
- **[Trade-off] soft mount 可能短时 I/O 错误** → 优于 hard mount 卡死 JVM。
- **[Trade-off] 维护中英双语文档成本** → 结构对等、先改提纲再填两语；避免只更新一侧。

## Migration Plan

应用数据迁移与基础设施切换合并为一条路径（细节见 8.7）：

1. NAS 建共享 + 香草云 Tailscale + systemd 挂载验收（8.2–8.4）
2. 配置 `backend.env`，更新 systemd / Nginx（8.5–8.6）
3. 发版：存储层 + `content_key`；兼容读
4. 跑正文导出迁移到 `/mnt/nas-blog/posts/`
5. 发版：只走对象；可选删 `content_md` 列
6. 回滚：恢复 env / 读 DB（删列前）或 rsync 对象到本地盘

## Open Questions

- 软删文章是否立即 `delete` 对象？**建议**：软删仅 DB；对象保留或异步 GC，避免误删难恢复。
- 列表接口是否已返回 `contentMd`？实现时与现 VO 对齐；若有则改为详情才填充。
- `/uploads/` 用 Nginx alias（8.6-B）还是反代后端（8.6-A）？**建议默认 A**，与现 WebConfig 一致；流量大再改 B。
- NAS 最终协议（NFS vs SMB）以实机为准；文档以 NFS 为主模板、SMB 为附录。
