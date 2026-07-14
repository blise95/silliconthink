CREATE DATABASE IF NOT EXISTS silliconthink DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE silliconthink;

CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(64)  NOT NULL,
    password_hash   VARCHAR(100) NULL,
    display_name    VARCHAR(64)  NOT NULL,
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '1=enabled,0=disabled',
    deleted         TINYINT      NOT NULL DEFAULT 0,
    create_date     DATETIME     NOT NULL,
    update_date     DATETIME     NOT NULL,
    create_by       BIGINT       NOT NULL DEFAULT 0,
    update_by       BIGINT       NOT NULL DEFAULT 0,
    UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_user_oauth (
    id                  BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT       NOT NULL,
    provider            VARCHAR(32)  NOT NULL,
    provider_user_id    VARCHAR(128) NOT NULL,
    provider_username   VARCHAR(128) NULL,
    create_date         DATETIME     NOT NULL,
    update_date         DATETIME     NOT NULL,
    create_by           BIGINT       NOT NULL DEFAULT 0,
    update_by           BIGINT       NOT NULL DEFAULT 0,
    UNIQUE KEY uk_provider_user (provider, provider_user_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
