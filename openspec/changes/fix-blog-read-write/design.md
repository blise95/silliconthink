## Context

`blog-nas-storage` 将正文/媒体迁到 `BLOG_STORAGE_ROOT` 后，生产出现：

- 公开 `/blog` 像空列表
- 已发布详情异常
- 写作台「保存草稿」无反馈

已定位的高概率原因：

1. **`BlogListView` 未渲染 `usePosts().error`**：请求失败时 `data` 仍为 `null`，模板走 `EmptyState`「没有找到文章」，掩盖 `media storage unavailable` / 5xx / 鉴权类错误。
2. **写路径依赖对象存储**：`create`/`update` 在根目录不可写时抛 `MEDIA_STORAGE_UNAVAILABLE`；若用户未注意页顶错误条，或存储路径未配对，表现为「点了没反应」。
3. **读路径过脆**：`content_key` 有值但文件不存在时直接 `CONTENT_OBJECT_MISSING`，即使库内仍有遗留 `content_md`；迁移未跑或目录换过会导致已发布文详情挂掉。

列表接口本身不读对象文件，若库中有 `published` 行仍应返回元数据；用户「看不到文章」更可能是前端把失败当成空，或生产 `VITE_USE_MOCK`/API 配置问题（实现时一并核对）。

## Goals / Non-Goals

**Goals:**

- 公开列表/详情与作者保存的错误对用户可见、可理解
- 正文加载：key → 文件；缺失则回退 `content_md`；再无则明确业务错误
- 保存成功有轻量成功反馈；失败保留并展示 API message
- 文档化生产自检（存储根、权限、迁移）

**Non-Goals:**

- 撤销对象存储、改回仅 DB 存正文
- 重做编辑器 UX / 富文本
- 自动从 NAS 同步（当前本机目录方案）

## Decisions

### 1. 前端：失败 ≠ 空列表

`BlogListView`（及若有同类 latest 区块）：

```text
loading → LoadingState
error   → 错误文案（可重试）
!list   → EmptyState
else    → 卡片列表
```

- **理由**：与 `BlogDetailView` 已展示 `error` 对齐；避免运维误判「库空了」。
- **备选**：toast 全局提示 — 列表页仍应就地展示。

### 2. 写作台：保存反馈

- 失败：已有 `error` 条；确保 API `message`（含「media storage unavailable」中文或统一映射）足够醒目；保存中按钮 `disabled` 已有。
- 成功：短时「已保存」文案或状态提示（非仅静默）。
- 可选：前端在提交前校验 title/slug 非空，减少无感 400。

### 3. 后端：`loadContent` 回退顺序

```text
1. content_key 非空且对象存在 → 读文件
2. content_key 非空但对象缺失 → 若 content_md 非空则回退并打 WARN；否则 CONTENT_OBJECT_MISSING
3. content_key 空 → 用 content_md（可空字符串）
```

- **理由**：兼容未迁移/搬迁存储根后的旧数据；列表仍不读正文。
- **备选**：缺失即 404 — 对已发布文过狠，不选。

### 4. 写入前 `isRootWritable`

`create`/`update` 在 `putString` 前显式检查；与上传共用 `MEDIA_STORAGE_UNAVAILABLE`。

### 5. 运维修复路径（写入 README 故障节或 tasks 验证）

```bash
# 存储
sudo -u www-data test -w "$BLOG_STORAGE_ROOT"
# 可选一次迁移
BLOG_MIGRATE_CONTENT=true  # 重启一次后关闭
# 核对
curl -s 'http://127.0.0.1:8080/api/v1/posts?page=1'
```

## Risks / Trade-offs

- **[Risk] 回退读到过期 content_md** → Mitigation：仅当对象缺失时回退；正常路径以文件为准；WARN 日志便于发现漂移。
- **[Risk] 前端仍 USE_MOCK 导致与后端数据不一致** → Mitigation：tasks 中核对生产构建环境变量。
- **[Trade-off] 成功提示增加一点 UI** → 换取「保存有反馈」，必要。

## Migration Plan

1. 发版前端错误态 + 后端 loadContent 回退。
2. 生产确认 `BLOG_STORAGE_ROOT` 与权限；按需跑迁移。
3. 回归：列表有已发布文、详情可读、保存草稿成功有提示、故意错误存储可见错误。
4. 回滚：回退该 commit；数据层无破坏性 DDL。

## Open Questions

- 是否把英文错误码 message 改为中文业务文案？**建议**：后端 message 可中英双语或前端按 code 映射中文（实现时选一，优先前端映射常见 code）。
