-- Default admin (change password before production!)
-- username: admin
-- password: Admin@123456
USE silliconthink;

INSERT INTO sys_user (
    username, password_hash, display_name, status, deleted,
    create_date, update_date, create_by, update_by
) VALUES (
    'admin',
    '$2y$10$aoLiqT6wCORLCNoJ7pnFtuEGTe.E3kC486JvgKu6xiczkboR5THmC',
    'Administrator',
    1,
    0,
    NOW(),
    NOW(),
    0,
    0
) ON DUPLICATE KEY UPDATE update_date = VALUES(update_date);
