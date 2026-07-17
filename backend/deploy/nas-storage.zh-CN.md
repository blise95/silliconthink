# 博客内容 NAS 对象存储

[English](./nas-storage.md) | **简体中文**

将博客**正文**与**媒体**存到 NAS 共享（类 OSS），**MySQL 只存元数据**。

> **安全：** 本文仅使用占位符（`100.x.x.x`、`YOUR_NAS_EXPORT`）。切勿把真实 Tailscale IP、密码、JWT、SMB 凭据提交到 GitHub。

## 架构

```text
公网 → Nginx（香草云）→ Spring Boot
                           ├─ MySQL（元数据：标题、slug、状态、content_key、标签）
                           └─ BLOG_STORAGE_ROOT=/mnt/nas-blog  （经 Tailscale 的 NFS/CIFS）
                                  ├─ posts/{authorId}/{postId}.md
                                  └─ media/yyyy/MM/dd/{uuid}.ext
```

## 前置条件

- 香草云已能经 Tailscale `ping` 通 NAS
- NAS 已建 NFS（优先）或 SMB 共享
- 后端以 `www-data` 运行（见 `silliconthink-backend.service`）

## 1. NAS 侧

1. 创建专用共享，例如 `silliconthink-blog`
2. 开启 NFS；仅允许香草云 Tailscale IP（或 `100.64.0.0/10`）——**不要**对公网开放
3. 保证映射用户可写（尽量对齐 Linux uid `33` / `www-data`）
4. 可选：对共享开启快照，备份 `posts/` 与 `media/`

私下记录：

| 项 | 占位 |
|----|------|
| NAS Tailscale 地址 | `100.x.x.x` |
| 导出路径 | `/volume1/silliconthink-blog` |

## 2. 香草云：Tailscale

```bash
sudo tailscale up
tailscale status
ping 100.x.x.x
```

## 3. 香草云：挂载

```bash
sudo apt-get install -y nfs-common
sudo mkdir -p /mnt/nas-blog
```

复制并编辑 [mnt-nas-blog.mount](./mnt-nas-blog.mount)：

```bash
sudo cp /opt/silliconthink/backend/deploy/mnt-nas-blog.mount /etc/systemd/system/
# 将 What= 改为真实导出路径
sudo systemctl daemon-reload
sudo systemctl enable --now mnt-nas-blog.mount
findmnt /mnt/nas-blog
sudo -u www-data touch /mnt/nas-blog/.write-test && sudo -u www-data rm /mnt/nas-blog/.write-test
```

挂载选项含 `_netdev`、`nofail`、`soft`：开机不阻塞，I/O 可快速失败。

### SMB 附录

```ini
What=//100.x.x.x/silliconthink-blog
Type=cifs
Options=_netdev,nofail,uid=33,gid=33,credentials=/etc/silliconthink/nas.cred
```

凭据文件 `chmod 600`，**不要**提交到 Git。

## 4. 后端环境变量与 systemd

`/etc/silliconthink/backend.env`（不进 Git）：

```bash
BLOG_STORAGE_ROOT=/mnt/nas-blog
# 可选；默认 ${BLOG_STORAGE_ROOT}/media
# UPLOAD_DIR=/mnt/nas-blog/media
```

服务 unit 应包含：

```ini
After=network-online.target mysql.service mnt-nas-blog.mount
RequiresMountsFor=/mnt/nas-blog
```

```bash
sudo systemctl daemon-reload
sudo systemctl restart silliconthink-backend
```

## 5. 数据库迁移

```bash
mysql -u... -p silliconthink < /opt/silliconthink/backend/src/main/resources/db/migration/2026-07-17-add-content-key.sql
```

一次性导出旧 `content_md` 到对象存储：

```bash
# 仅一次重启时写入 backend.env：
BLOG_MIGRATE_CONTENT=true
sudo systemctl restart silliconthink-backend
# 完成后删除该变量或设为 false
```

验证后再考虑删除列（见 deferred SQL）。

## 6. Nginx `/uploads`

**方案 A（默认）：** 反代到 Spring Boot。  
**方案 B：** `alias /mnt/nas-blog/media/;` —— 见 `frontend/deploy/nginx.conf.example`。

## 7. 上线检查清单

1. `www-data` 对挂载点可写
2. 部署支持对象存储的应用
3. 执行 DDL + 正文迁移
4. 新建文章 → `/mnt/nas-blog/posts/` 出现文件
5. 上传图片 → `/mnt/nas-blog/media/`
6. 旧公开链接仍可用
7. 可选 umount：写失败可识别，列表仍可用

## 8. 安全

| 应当 | 禁止 |
|------|------|
| 真实 IP/密钥只放服务器 | 提交 `backend.env`、`*.cred`、真实 Tailscale 主机名 |
| NFS/SMB 仅 Tailscale | NAS 对 `0.0.0.0/0` 开放 |
| 泄露后轮换 JWT/库密码 | 生产沿用文档默认管理员密码 |
| env 文件 `chmod 640` | 在 README 示例里写真实密钥 |

## 9. 回滚

1. 停后端
2. 将 `BLOG_STORAGE_ROOT` 改回本机盘并按需 rsync，**或**回退到仍可读 `content_md` 的版本（删列前）
3. 启动后端

## 健康检查

```bash
findmnt /mnt/nas-blog >/dev/null \
  && sudo -u www-data test -w /mnt/nas-blog \
  && curl -sf 'http://127.0.0.1:8080/api/v1/posts?page=1' >/dev/null
```
