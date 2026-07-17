## MODIFIED Requirements

### Requirement: 博客文章持久化与审计字段
系统 SHALL 在 MySQL 中提供博客文章及相关标签表，且每张业务表包含 `create_date`、`update_date`、`create_by`、`update_by`。文章表 SHALL 将 Markdown 正文的权威存储放在对象存储中，并在元数据行保存 `content_key`（或等价对象引用）；MySQL 不作为正文全文的权威数据源。

#### Scenario: 建表包含文章与标签
- **WHEN** 执行项目提供的 DDL 初始化脚本
- **THEN** 数据库中存在文章主表、标签表与文章-标签关联表，均含四审计字段，且文章主表含对象引用字段而非依赖库内全文正文作为唯一来源

#### Scenario: 写入时维护审计
- **WHEN** 通过应用创建或更新一篇文章
- **THEN** 对应行的创建/更新审计字段被正确赋值

#### Scenario: 正文落在对象存储
- **WHEN** 作者保存含 Markdown 正文的文章且对象存储可用
- **THEN** 正文内容写入对象存储，元数据行保存可据此读取的 `content_key`

### Requirement: 登录用户可上传图片并获得可访问 URL
系统 SHALL 提供需登录的图片上传接口；校验类型与大小后将文件写入与博客正文相同的对象存储根下的媒体路径，并返回可供 Markdown 与封面使用的公开访问 URL。公开 URL 路径前缀保持可配置的既有前缀（默认 `/uploads`）。

#### Scenario: 上传成功
- **WHEN** 已登录用户上传合法图片文件（如 png/jpeg，且未超大小上限），且对象存储根可写
- **THEN** 系统保存文件并返回可公开 GET 访问的 URL

#### Scenario: 非法类型拒绝
- **WHEN** 已登录用户上传非图片或不在允许列表内的文件
- **THEN** 系统拒绝上传且不返回可用 URL

#### Scenario: 公开可读上传文件
- **WHEN** 客户端使用上传返回的 URL 发起 GET
- **THEN** 系统返回对应图片内容（无需登录）

#### Scenario: 存储根为 NAS 挂载时行为一致
- **WHEN** 对象存储根指向已挂载且可写的 NAS 目录，用户上传合法图片
- **THEN** 文件落在该存储根的 media 约定路径下，返回的 URL 前缀与本地存储配置时相同，公开 GET 可读取该文件

## ADDED Requirements

### Requirement: 详情组装时从对象存储加载正文
系统 SHALL 在返回需要正文的详情（公开已发布详情、作者自己的详情）时，根据元数据中的 `content_key` 从对象存储加载 Markdown，填入响应中的 `contentMd`。

#### Scenario: 公开详情返回正文
- **WHEN** 访客请求某已发布文章的详情且对象存在
- **THEN** 响应包含从对象存储加载的 `contentMd`

#### Scenario: 对象缺失时详情失败可识别
- **WHEN** 元数据存在但对应正文对象缺失
- **THEN** 系统返回可理解的错误或未找到类结果，不假装正文为空成功（除非产品明确允许空正文草稿）
