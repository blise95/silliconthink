## 1. 后端正文加载与写入校验

- [x] 1.1 调整 `BlogPostService.loadContent`：对象缺失时回退非空 `content_md` 并打 WARN；皆无则 `CONTENT_OBJECT_MISSING`
- [x] 1.2 `create`/`update` 写正文前调用 `blogObjectStore.isRootWritable()`，失败抛 `MEDIA_STORAGE_UNAVAILABLE`
- [x] 1.3 补充/更新单元测试：回退路径、对象与遗留皆无

## 2. 前端错误与保存反馈

- [x] 2.1 `BlogListView` 使用 `usePosts().error`：失败展示错误，成功空列表才 EmptyState
- [x] 2.2 `PostEditorView`：保存成功提示；常见错误码中文映射
- [x] 2.3 详情 API：仅 `code===40400` 视为不存在（避免正文缺失被当成文章 404）

## 3. 生产配置与文档

- [ ] 3.1 根 README 故障排查补充
- [ ] 3.2 验证清单：生产回归

## 4. 验证

- [x] 4.1–4.3 单元测试 / 前端逻辑已覆盖
- [ ] 4.4 香草云：发版后回归列表/详情/保存草稿
