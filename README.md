# Silicon Think

**English** | [简体中文](./README.zh-CN.md)

Silicon Think is a personal website platform: public blog and portfolio, author workspace, and authentication (password + GitHub OAuth). It is built as a Vue 3 SPA with a Spring Boot API, with specifications managed under OpenSpec.

| | |
|---|---|
| **Repository** | https://github.com/blise95/silliconthink |
| **Runtime** | Java 17 · Node.js 18+ · MySQL 8 |
| **Default site layout** | Nginx (static + reverse proxy) · systemd backend |

---

## Table of contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Repository layout](#repository-layout)
4. [Requirements](#requirements)
5. [Quick start (local)](#quick-start-local)
6. [Configuration reference](#configuration-reference)
7. [Storage model and paths](#storage-model-and-paths)
8. [Production deployment](#production-deployment)
9. [Database](#database)
10. [Security](#security)
11. [Operations checklist](#operations-checklist)
12. [Troubleshooting](#troubleshooting)
13. [License](#license)

---

## Overview

### Features

- Public blog (list, tags, search, Markdown detail) and portfolio pages
- User registration / login and GitHub OAuth (JWT)
- Author workspace: drafts, publish / unpublish, Markdown editor, image upload
- Clear data split:
  - **MySQL** — metadata (title, slug, summary, status, tags, `content_key`, …)
  - **Filesystem object store** — Markdown bodies and uploaded media (local disk by default; optional NAS mount)

### Design principles

- API-stable for the SPA: clients still send/receive `contentMd`; object keys stay server-side
- Production secrets never enter Git; only `.example` templates are committed
- Single canonical document: this README (and the Chinese sibling). Scripts under `*/deploy/` are implementation aids, not a second source of truth

---

## Architecture

```text
                         Internet
                            │
                            ▼
                     ┌─────────────┐
                     │    Nginx    │
                     │  (TLS/HTTP) │
                     └──────┬──────┘
            ┌───────────────┼───────────────┐
            │               │               │
            ▼               ▼               ▼
       static SPA      /api/*           /uploads/*
       (Vue dist)   Spring Boot      (media files)
                            │
              ┌─────────────┴─────────────┐
              ▼                           ▼
           MySQL 8                 BLOG_STORAGE_ROOT
        (metadata only)         posts/ + media/
```

| Concern | Component |
|---------|-----------|
| UI | `frontend/` — Vue 3 + Vite + TypeScript |
| API | `backend/` — Spring Boot 3 + MyBatis-Plus + Security/JWT |
| Metadata | MySQL 8 |
| Bodies & images | Directory tree under `BLOG_STORAGE_ROOT` |
| Specs / changes | `openspec/` |

Object key layout:

```text
{BLOG_STORAGE_ROOT}/
├── posts/{authorId}/{postId}.md    # article Markdown (UTF-8)
└── media/{yyyy}/{MM}/{dd}/{uuid}.{ext}
```

Subdirectories are created automatically on first write; you only need to create and chown the **root**.

---

## Repository layout

```text
silliconthink/
├── README.md / README.zh-CN.md     # ← canonical documentation
├── deploy/                         # full-stack update entry
├── frontend/                       # Vue SPA + Nginx examples
├── backend/                        # Spring Boot + systemd examples
└── openspec/                       # OpenSpec proposals & specs
```

| Path | Role |
|------|------|
| `frontend/deploy/` | Frontend build/publish scripts, `nginx.conf.example` |
| `backend/deploy/` | `server-setup.sh`, `update.sh`, `backend.env.example`, systemd unit |
| `backend/src/main/resources/db/` | `schema.sql`, `data.sql`, `migration/` |
| `openspec/changes/` | Change proposals (design / tasks) |

---

## Requirements

| Software | Version |
|----------|---------|
| JDK | 17+ |
| Maven | 3.9+ |
| Node.js / npm | 18+ / 9+ |
| MySQL | 8.0+ |
| Nginx | (production) |
| systemd | (production on Linux) |

---

## Quick start (local)

### Frontend

```bash
cd frontend
npm install
cp .env.example .env    # optional; see Configuration
npm run dev
```

Open http://localhost:5173

### Backend

```bash
mysql -uroot -p < backend/src/main/resources/db/schema.sql
mysql -uroot -p < backend/src/main/resources/db/data.sql

cp backend/src/main/resources/application-local.yml.example \
   backend/src/main/resources/application-local.yml
# Edit datasource, JWT, OAuth — do not commit this file

cd backend
export JAVA_HOME=$(/usr/libexec/java_home -v 17)   # macOS example
mvn spring-boot:run
```

Health: `GET http://localhost:8080/api/v1/health`

**Seed admin (change before any public deployment):**

| Username | Password |
|----------|----------|
| `admin` | `Admin@123456` |

Local object store default: `backend/data/blog-storage` (or `data/blog-storage` relative to process CWD), overridable with `BLOG_STORAGE_ROOT`.

### Typical local `.env` (frontend)

```env
VITE_USE_MOCK=false
VITE_AUTH_USE_API=true
VITE_API_BASE_URL=http://localhost:8080
```

---

## Configuration reference

### Frontend (`frontend/.env` / `.env.production`)

| Variable | Default | Description |
|----------|---------|-------------|
| `VITE_USE_MOCK` | `true` | Use mock content repositories when `true` |
| `VITE_AUTH_USE_API` | `false` | Use real auth API when `true` |
| `VITE_API_BASE_URL` | `http://localhost:8080` | API origin; leave empty in production to use same-origin `/api` |
| `VITE_SITE_URL` | `http://localhost:5173` | Public site URL (SEO / absolute links) |

### Backend environment (`/etc/silliconthink/backend.env` in production)

Copy from `backend/deploy/backend.env.example`. **Never commit a filled env file.**

| Variable | Required | Description |
|----------|----------|-------------|
| `DB_HOST` | yes | MySQL host |
| `DB_PORT` | no | Default `3306` |
| `DB_NAME` | yes | Database name |
| `DB_USERNAME` | yes | DB user |
| `DB_PASSWORD` | yes | DB password |
| `JWT_SECRET` | yes | ≥ 32 random characters |
| `JWT_EXPIRE_SECONDS` | no | Default `7200` |
| `LOG_PATH` | no | Log directory; file is `${LOG_PATH}/backend.log` |
| `BLOG_STORAGE_ROOT` | recommended | Absolute path to object-store root |
| `UPLOAD_DIR` | no | Media directory; **default** `{BLOG_STORAGE_ROOT}/media` |
| `BLOG_MIGRATE_CONTENT` | no | `true` once to export legacy `content_md` → files, then set `false` |
| `SERVER_PORT` | no | Default `8080` |
| `OAUTH_FRONTEND_CALLBACK` | OAuth | e.g. `https://your.domain/oauth/callback` |
| `GITHUB_CLIENT_ID` / `GITHUB_CLIENT_SECRET` | OAuth | GitHub OAuth App |
| `GITHUB_REDIRECT_URI` | OAuth | Backend callback URL |

Also configurable via Spring (`application-local.yml` / `application-prod.yml`):

| Property | Env equivalent |
|----------|----------------|
| `app.storage.root` | `BLOG_STORAGE_ROOT` |
| `app.upload.dir` | `UPLOAD_DIR` |
| `app.storage.migrate-on-startup` | `BLOG_MIGRATE_CONTENT` |

---

## Storage model and paths

### What lives where

| Data | Location |
|------|----------|
| Title, slug, summary, status, tags, author, `content_key`, timestamps | MySQL `blog_*` tables |
| Article Markdown body | `{BLOG_STORAGE_ROOT}/posts/...` |
| Uploaded images | `{BLOG_STORAGE_ROOT}/media/...` (URL prefix `/uploads/...`) |

You do **not** need to pre-create `posts/` or `media/`; the application creates them on write.

### Recommended production path (VPS local disk)

```bash
sudo mkdir -p /var/lib/silliconthink/blog-storage
sudo chown -R www-data:www-data /var/lib/silliconthink/blog-storage
sudo chmod 750 /var/lib/silliconthink/blog-storage
```

In `/etc/silliconthink/backend.env`:

```bash
BLOG_STORAGE_ROOT=/var/lib/silliconthink/blog-storage
```

### Optional: NAS as the object-store root

If NFS/SMB over a private network (e.g. Tailscale) is available later, mount the share and point `BLOG_STORAGE_ROOT` at the mount point (or a subdirectory). Keep NFS/SMB **off the public Internet**. Example unit template: `backend/deploy/mnt-nas-blog.mount`. Detailed NAS notes: `backend/deploy/nas-storage.md`.

### Server filesystem map (typical single node)

| Path | Purpose |
|------|---------|
| `/opt/silliconthink` | Git working copy |
| `/opt/silliconthink-runtime/app.jar` | Running backend JAR |
| `/etc/silliconthink/backend.env` | Secrets & storage path (`chmod 640`, `root:www-data`) |
| `/etc/silliconthink/application-prod.yml` | Optional extra Spring config |
| `/var/www/silliconthink` | Frontend static files |
| `/var/lib/silliconthink/blog-storage` | Blog bodies + media |
| `/var/log/silliconthink/backend.log` | Backend log |

---

## Production deployment

Assumes Ubuntu-like host, code at `/opt/silliconthink`, tracking GitHub `main`.

### 1. First-time host setup

```bash
# Clone (once)
sudo mkdir -p /opt/silliconthink
sudo git clone https://github.com/blise95/silliconthink.git /opt/silliconthink

# Frontend toolchain + Nginx site (see script)
sudo bash /opt/silliconthink/frontend/deploy/server-setup.sh

# Backend JDK/Maven + systemd (see script)
sudo bash /opt/silliconthink/backend/deploy/server-setup.sh
```

### 2. Database

```bash
mysql -u root -p < /opt/silliconthink/backend/src/main/resources/db/schema.sql
mysql -u root -p < /opt/silliconthink/backend/src/main/resources/db/data.sql
# Existing DBs upgrading to object storage:
mysql -u ... -p silliconthink < \
  /opt/silliconthink/backend/src/main/resources/db/migration/2026-07-17-add-content-key.sql
```

### 3. Environment and storage

```bash
sudo cp /opt/silliconthink/backend/deploy/backend.env.example \
  /etc/silliconthink/backend.env
sudo vim /etc/silliconthink/backend.env
# Set DB_*, JWT_SECRET, BLOG_STORAGE_ROOT=/var/lib/silliconthink/blog-storage
sudo chown root:www-data /etc/silliconthink/backend.env
sudo chmod 640 /etc/silliconthink/backend.env

sudo mkdir -p /var/lib/silliconthink/blog-storage
sudo chown -R www-data:www-data /var/lib/silliconthink/blog-storage
```

Install / refresh systemd unit from `backend/deploy/silliconthink-backend.service` (adjust `JAVA_HOME` if needed). Do **not** enable `RequiresMountsFor=` unless storage is on a dedicated mount.

### 4. Nginx

Use `frontend/deploy/nginx.conf.example`:

- `/` → SPA static root
- `/api/` → `http://127.0.0.1:8080`
- `/uploads/` → proxy to backend (or `alias` to `{BLOG_STORAGE_ROOT}/media/`)

```bash
sudo nginx -t && sudo systemctl reload nginx
```

### 5. Publish / update

```bash
# Frontend + backend
sudo bash /opt/silliconthink/deploy/update.sh

# Or separately
sudo bash /opt/silliconthink/frontend/deploy/update.sh
sudo bash /opt/silliconthink/backend/deploy/update.sh
```

### 6. Optional one-shot content migration

If rows still hold legacy `content_md`:

```bash
# In backend.env for a single restart:
BLOG_MIGRATE_CONTENT=true
sudo systemctl restart silliconthink-backend
# Then set BLOG_MIGRATE_CONTENT=false and restart again
```

Dropping the `content_md` column is deferred; see `db/migration/2026-07-17-drop-content-md-deferred.sql`.

---

## Database

| Script | When |
|--------|------|
| `db/schema.sql` | Fresh install |
| `db/data.sql` | Seed admin (change password) |
| `db/migration/2026-07-17-add-content-key.sql` | Add `content_key` on existing DBs |
| `db/migration/2026-07-17-drop-content-md-deferred.sql` | After migration verified |

API prefix: `/api/v1`. Uniform JSON envelope: `{ "code": 0, "message": "ok", "data": ... }`.

---

## Security

1. **Secrets** — Only commit `*.example` files. Real `backend.env`, `application-local.yml`, OAuth secrets, and credential files stay on the server (`chmod 640`).
2. **Default admin** — Change `admin` / `Admin@123456` before exposing the site.
3. **Object store** — Prefer a dedicated directory owned by `www-data`; do not place the store inside the public web root unless intentionally serving files via Nginx alias with care.
4. **Network** — If using NAS later, restrict NFS/SMB to private overlay networks (e.g. Tailscale); never `0.0.0.0/0`.
5. **Rotation** — On leak: rotate `JWT_SECRET`, DB password, and GitHub OAuth client secret.

---

## Operations checklist

After deploy:

```bash
# Service
systemctl is-active silliconthink-backend

# Storage writable by runtime user
sudo -u www-data test -w /var/lib/silliconthink/blog-storage && echo OK

# API
curl -sf 'http://127.0.0.1:8080/api/v1/posts?page=1' >/dev/null && echo OK

# After creating a post / uploading an image
sudo ls /var/lib/silliconthink/blog-storage/posts/
sudo ls /var/lib/silliconthink/blog-storage/media/
```

Log file: `/var/log/silliconthink/backend.log` (look for `Blog object storage root OK`).

---

## Troubleshooting

| Symptom | Likely cause | Action |
|---------|--------------|--------|
| `media storage unavailable` | Root missing or not writable by `www-data` | `mkdir` + `chown www-data` |
| Post detail 404 / content missing | `content_key` file absent | Re-save post or run migration |
| `/uploads/` 404 in browser | Nginx missing `/uploads/` proxy | Apply `nginx.conf.example` |
| Backend won't start with mount dependency | Leftover `RequiresMountsFor=` | Use current unit without NAS requires |
| NFS `Permission denied` | NAS ACL / wrong client IP | Fix NAS NFS allow-list (optional path) |

---

## License

Personal project — license to be determined. Issues and pull requests for documentation and fixes are welcome.
