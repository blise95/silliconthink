-- Existing databases: add content_key for object-store migration.
-- Run once. If column already exists, MySQL will error — that is OK (skip).

ALTER TABLE blog_post
    ADD COLUMN content_key VARCHAR(512) NULL COMMENT '正文对象键（相对 BLOG_STORAGE_ROOT）' AFTER summary;

ALTER TABLE blog_post
    MODIFY COLUMN content_md MEDIUMTEXT NULL COMMENT '遗留 Markdown 正文（迁移后可清空/删除列）';
