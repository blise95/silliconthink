## ADDED Requirements

### Requirement: 详情正文对象缺失时的回退与错误
系统在组装需要正文的详情响应时，SHALL 优先从对象存储按 `content_key` 读取；若对象不存在且遗留 `content_md` 可用则回退使用之；若均不可用则返回内容缺失类业务错误。

#### Scenario: 回退遗留正文
- **WHEN** `content_key` 指向的对象不存在且 `content_md` 非空
- **THEN** 详情接口返回的 `contentMd` 为该遗留正文

#### Scenario: 无法提供正文
- **WHEN** 对象不存在且无可用遗留正文
- **THEN** 详情接口不以成功空正文冒充完整文章，而是返回可识别的错误
