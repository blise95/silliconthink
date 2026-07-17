CREATE DATABASE IF NOT EXISTS silliconthink DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE silliconthink;

CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username        VARCHAR(64)  NOT NULL COMMENT '登录用户名',
    password_hash   VARCHAR(100) NULL COMMENT '密码哈希（OAuth 用户可为空）',
    display_name    VARCHAR(64)  NOT NULL COMMENT '显示名称',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1=启用，0=禁用',
    deleted         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
    create_date     DATETIME     NOT NULL COMMENT '创建时间',
    update_date     DATETIME     NOT NULL COMMENT '更新时间',
    create_by       BIGINT       NOT NULL DEFAULT 0 COMMENT '创建人ID',
    update_by       BIGINT       NOT NULL DEFAULT 0 COMMENT '更新人ID',
    UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

CREATE TABLE IF NOT EXISTS sys_user_oauth (
    id                  BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id             BIGINT       NOT NULL COMMENT '关联用户ID',
    provider            VARCHAR(32)  NOT NULL COMMENT 'OAuth 提供方（如 github、google）',
    provider_user_id    VARCHAR(128) NOT NULL COMMENT '提供方侧用户唯一标识',
    provider_username   VARCHAR(128) NULL COMMENT '提供方侧用户名',
    create_date         DATETIME     NOT NULL COMMENT '创建时间',
    update_date         DATETIME     NOT NULL COMMENT '更新时间',
    create_by           BIGINT       NOT NULL DEFAULT 0 COMMENT '创建人ID',
    update_by           BIGINT       NOT NULL DEFAULT 0 COMMENT '更新人ID',
    UNIQUE KEY uk_provider_user (provider, provider_user_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户 OAuth 绑定表';

CREATE TABLE IF NOT EXISTS blog_post (
    id              BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    author_id       BIGINT        NOT NULL COMMENT '作者用户ID',
    title           VARCHAR(200)  NOT NULL COMMENT '文章标题',
    slug            VARCHAR(200)  NOT NULL COMMENT 'URL 友好标识',
    summary         VARCHAR(500)  NOT NULL DEFAULT '' COMMENT '文章摘要',
    content_key     VARCHAR(512)  NULL COMMENT '正文对象键（相对 BLOG_STORAGE_ROOT）',
    content_md      MEDIUMTEXT    NULL COMMENT '遗留 Markdown 正文（迁移后可清空/删除列）',
    cover_url       VARCHAR(512)  NULL COMMENT '封面图 URL',
    status          VARCHAR(16)   NOT NULL DEFAULT 'draft' COMMENT '状态：draft=草稿，published=已发布',
    published_at    DATETIME      NULL COMMENT '发布时间',
    deleted         TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
    create_date     DATETIME      NOT NULL COMMENT '创建时间',
    update_date     DATETIME      NOT NULL COMMENT '更新时间',
    create_by       BIGINT        NOT NULL DEFAULT 0 COMMENT '创建人ID',
    update_by       BIGINT        NOT NULL DEFAULT 0 COMMENT '更新人ID',
    UNIQUE KEY uk_blog_post_slug (slug),
    KEY idx_author_status (author_id, status),
    KEY idx_published_at (published_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='博客文章表';

CREATE TABLE IF NOT EXISTS blog_tag (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name            VARCHAR(64)  NOT NULL COMMENT '标签名称',
    slug            VARCHAR(64)  NOT NULL COMMENT 'URL 友好标识',
    create_date     DATETIME     NOT NULL COMMENT '创建时间',
    update_date     DATETIME     NOT NULL COMMENT '更新时间',
    create_by       BIGINT       NOT NULL DEFAULT 0 COMMENT '创建人ID',
    update_by       BIGINT       NOT NULL DEFAULT 0 COMMENT '更新人ID',
    UNIQUE KEY uk_blog_tag_slug (slug),
    UNIQUE KEY uk_blog_tag_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='博客标签表';

CREATE TABLE IF NOT EXISTS blog_post_tag (
    id              BIGINT   NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    post_id         BIGINT   NOT NULL COMMENT '文章ID',
    tag_id          BIGINT   NOT NULL COMMENT '标签ID',
    create_date     DATETIME NOT NULL COMMENT '创建时间',
    update_date     DATETIME NOT NULL COMMENT '更新时间',
    create_by       BIGINT   NOT NULL DEFAULT 0 COMMENT '创建人ID',
    update_by       BIGINT   NOT NULL DEFAULT 0 COMMENT '更新人ID',
    UNIQUE KEY uk_post_tag (post_id, tag_id),
    KEY idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签关联表';
