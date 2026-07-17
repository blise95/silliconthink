-- Step 2 (after content migrated to object storage and app no longer reads content_md):
-- ALTER TABLE blog_post DROP COLUMN content_md;
--
-- Do NOT run until:
-- 1) All rows with body have content_key set
-- 2) App version that only loads body from object store is deployed
-- 3) You have a DB backup

SELECT 'Deferred: drop content_md after migration verification' AS notice;
