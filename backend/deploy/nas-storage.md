# NAS object storage for blog content

**English** | [简体中文](./nas-storage.zh-CN.md)

Store blog **body** and **media** on a NAS share (OSS-like), while **MySQL** keeps metadata only.

> **Security:** This guide uses placeholders only (`100.x.x.x`, `YOUR_NAS_EXPORT`). Never commit real Tailscale IPs, passwords, JWT secrets, or SMB credentials to GitHub.

## Architecture

```text
Internet → Nginx (VPS) → Spring Boot
                           ├─ MySQL (metadata: title, slug, status, content_key, tags)
                           └─ BLOG_STORAGE_ROOT=/mnt/nas-blog  (NFS/CIFS over Tailscale)
                                  ├─ posts/{authorId}/{postId}.md
                                  └─ media/yyyy/MM/dd/{uuid}.ext
```

## Prerequisites

- VPS can `ping` the NAS over Tailscale
- NAS NFS (preferred) or SMB share created
- Backend runs as `www-data` (see `silliconthink-backend.service`)

## 1. NAS side

1. Create a dedicated share, e.g. `silliconthink-blog`
2. Enable NFS; allow **only** the VPS Tailscale IP (or `100.64.0.0/10`) — **not** the public Internet
3. Ensure the mapped user can write (align with Linux uid `33` / `www-data` when possible)
4. Optional: enable NAS snapshots for `posts/` and `media/`

Record (keep private):

| Item | Placeholder |
|------|-------------|
| NAS Tailscale address | `100.x.x.x` |
| Export path | `/volume1/silliconthink-blog` |

## 2. VPS: Tailscale

```bash
# Install Tailscale, then:
sudo tailscale up
tailscale status
ping 100.x.x.x
```

## 3. VPS: mount

```bash
sudo apt-get install -y nfs-common
sudo mkdir -p /mnt/nas-blog
```

Copy and edit [mnt-nas-blog.mount](./mnt-nas-blog.mount):

```bash
sudo cp /opt/silliconthink/backend/deploy/mnt-nas-blog.mount /etc/systemd/system/
# Edit What= to YOUR_NAS_EXPORT
sudo systemctl daemon-reload
sudo systemctl enable --now mnt-nas-blog.mount
findmnt /mnt/nas-blog
sudo -u www-data touch /mnt/nas-blog/.write-test && sudo -u www-data rm /mnt/nas-blog/.write-test
```

Mount options use `_netdev`, `nofail`, `soft` so boot is not blocked and I/O can fail fast.

### SMB appendix

```ini
What=//100.x.x.x/silliconthink-blog
Type=cifs
Options=_netdev,nofail,uid=33,gid=33,credentials=/etc/silliconthink/nas.cred
```

Create `/etc/silliconthink/nas.cred` with `chmod 600` — **do not** commit it.

## 4. Backend env & systemd

`/etc/silliconthink/backend.env` (not in Git):

```bash
BLOG_STORAGE_ROOT=/mnt/nas-blog
# Optional override; default is ${BLOG_STORAGE_ROOT}/media
# UPLOAD_DIR=/mnt/nas-blog/media
```

Service unit should include:

```ini
After=network-online.target mysql.service mnt-nas-blog.mount
RequiresMountsFor=/mnt/nas-blog
```

Restart:

```bash
sudo systemctl daemon-reload
sudo systemctl restart silliconthink-backend
```

## 5. Database migration

```bash
mysql -u... -p silliconthink < /opt/silliconthink/backend/src/main/resources/db/migration/2026-07-17-add-content-key.sql
```

Export legacy `content_md` to objects (one-shot):

```bash
# In backend.env for one restart only:
BLOG_MIGRATE_CONTENT=true
sudo systemctl restart silliconthink-backend
# Then remove BLOG_MIGRATE_CONTENT or set false
```

After verification, optionally drop the column (see deferred SQL).

## 6. Nginx `/uploads`

**Option A (default):** proxy to Spring Boot (same as `/api/`).

**Option B:** `alias /mnt/nas-blog/media/;` — see `frontend/deploy/nginx.conf.example`.

## 7. Cutover checklist

1. Mount writable as `www-data`
2. Deploy app with object store
3. Run DDL + content migration
4. Create a post → file appears under `/mnt/nas-blog/posts/`
5. Upload image → under `/mnt/nas-blog/media/`
6. Old published URLs still work
7. Optional: `umount` test → writes fail clearly; list still works

## 8. Security

| Do | Don't |
|----|--------|
| Keep real IPs/secrets only on the server | Commit `backend.env`, `*.cred`, real Tailscale hostnames |
| NFS/SMB only on Tailscale | Expose NAS shares to `0.0.0.0/0` |
| Rotate JWT/DB passwords if leaked | Ship default admin password to production unchanged |
| `chmod 640` on env files | Put production secrets in README examples |

## 9. Rollback

1. Stop backend
2. Point `BLOG_STORAGE_ROOT` to a local disk and `rsync` if needed, **or** redeploy a build that still reads `content_md` (before dropping the column)
3. Start backend

## Health check

```bash
findmnt /mnt/nas-blog >/dev/null \
  && sudo -u www-data test -w /mnt/nas-blog \
  && curl -sf 'http://127.0.0.1:8080/api/v1/posts?page=1' >/dev/null
```
