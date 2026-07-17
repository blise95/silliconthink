# Silicon Think

**English** | [简体中文](./README.zh-CN.md)

Personal site: public blog & portfolio, authoring studio, JWT / GitHub OAuth — Vue 3 frontend + Spring Boot backend, driven by [OpenSpec](./openspec/).

**Repository:** https://github.com/blise95/silliconthink

## Features

- Public blog list / detail (Markdown) and portfolio pages
- Auth: register, login, GitHub OAuth (JWT)
- Author workspace: drafts, publish, Markdown editor with image upload
- **Storage split:** MySQL holds **metadata** (title, slug, status, tags, `content_key`); blog **body & media** live in a configurable object-store root (local directory or NAS mount over Tailscale)

## Architecture

```text
Browser → Nginx
           ├─ static Vue SPA
           ├─ /api/     → Spring Boot
           └─ /uploads/ → media (proxy or alias)
                            │
               Spring Boot ─┼─ MySQL (metadata)
                            └─ BLOG_STORAGE_ROOT (NAS ≈ OSS via Tailscale)
                                 posts/*.md + media/*
```

## Tech stack

| Layer | Stack |
|-------|--------|
| Frontend | Vue 3, Vite, TypeScript |
| Backend | Java 17, Spring Boot 3, MyBatis-Plus, JWT |
| Data | MySQL 8 (metadata) + filesystem/NAS object store |
| Specs | OpenSpec under `openspec/` |

## Repository layout

| Path | Description |
|------|-------------|
| `frontend/` | Vue 3 site |
| `backend/` | Spring Boot API |
| `openspec/` | Specs & change proposals |
| `deploy/` | Full-stack update scripts |

## Quick start

### Frontend

```bash
cd frontend
npm install
cp .env.example .env   # optional
npm run dev
```

Open http://localhost:5173

### Backend

```bash
# JDK 17 + MySQL 8
mysql -uroot -p < backend/src/main/resources/db/schema.sql
mysql -uroot -p < backend/src/main/resources/db/data.sql
cp backend/src/main/resources/application-local.yml.example \
   backend/src/main/resources/application-local.yml
# Edit datasource, JWT, OAuth — do not commit secrets
cd backend && mvn spring-boot:run
```

Default seed admin: `admin` / `Admin@123456` — **change before production.**

Local object store defaults to `data/blog-storage` (`BLOG_STORAGE_ROOT`).

Auth + content API tips: see [frontend/README.md](frontend/README.md) and [backend/README.md](backend/README.md).

## Configuration (sensitive values)

| Variable | Purpose | Notes |
|----------|---------|--------|
| `DB_*` / `JWT_SECRET` | Database & auth | Server `/etc/silliconthink/backend.env` only |
| `BLOG_STORAGE_ROOT` | Object-store root | Prod: NAS mount e.g. `/mnt/nas-blog` |
| `UPLOAD_DIR` | Media dir | Optional; default `{BLOG_STORAGE_ROOT}/media` |
| `BLOG_MIGRATE_CONTENT` | One-shot body export | Set `true` once, then off |

Examples in repo are **placeholders only**.

## Deployment

Single-node (e.g. VPS) tracks GitHub `main` under `/opt/silliconthink`:

```bash
sudo bash /opt/silliconthink/deploy/update.sh
```

| Frontend only | `sudo bash /opt/silliconthink/frontend/deploy/update.sh` |
| Backend only | `sudo bash /opt/silliconthink/backend/deploy/update.sh` |

**NAS as object storage (recommended for blog body/media):**

- English: [backend/deploy/nas-storage.md](backend/deploy/nas-storage.md)
- 中文: [backend/deploy/nas-storage.zh-CN.md](backend/deploy/nas-storage.zh-CN.md)

Includes Tailscale mount, systemd, migration, Nginx `/uploads`, and rollback.

## Security

- **Never commit** real `backend.env`, `application-local.yml`, OAuth secrets, SMB `*.cred`, or private Tailscale IPs
- Keep NAS NFS/SMB reachable **only** via Tailscale — not the public Internet
- Change the default admin password before going live
- Prefer `chmod 640` on production env files (`root:www-data`)
- If secrets leak: rotate JWT, DB password, and OAuth client secret immediately

## Documentation

- [Frontend README](frontend/README.md)
- [Backend README](backend/README.md)
- [NAS storage (EN)](backend/deploy/nas-storage.md) / [NAS 存储（中文）](backend/deploy/nas-storage.zh-CN.md)
- [OpenSpec](openspec/)

## License

Personal project — license TBD. Contributions via GitHub Issues/PRs welcome for docs and fixes.
