-- ============================================
-- 用户认证状态修改脚本
-- ============================================

-- 方法1：通过用户ID修改（推荐）
-- 将 user_id = 1 的用户设置为已认证
UPDATE `forum_user_profile`
SET
    `school_email_verified` = b'1',                          -- 设置为已认证
    `school_email_verify_time` = NOW(),                      -- 设置认证时间为当前时间
    `school_name` = '测试大学',                               -- 设置学校名称（可选）
    `school_email` = 'test@example.edu.cn',                  -- 设置学校邮箱（可选）
    `school_email_prefix` = 'test',                          -- 设置邮箱前缀（可选）
    `real_name` = '张三',                                     -- 设置真实姓名（可选）
    `update_time` = NOW()
WHERE `user_id` = 1                                          -- 替换为实际的用户ID
  AND `deleted` = b'0';

-- ============================================

-- 方法2：通过UID修改
-- 将 UID = 'U123456' 的用户设置为已认证
UPDATE `forum_user_profile`
SET
    `school_email_verified` = b'1',
    `school_email_verify_time` = NOW(),
    `school_name` = '测试大学',
    `school_email` = 'test@example.edu.cn',
    `school_email_prefix` = 'test',
    `real_name` = '张三',
    `update_time` = NOW()
WHERE `uid` = 'U123456'                                      -- 替换为实际的UID
  AND `deleted` = b'0';

-- ============================================

-- 方法3：通过昵称修改
-- 将昵称为 '测试用户' 的用户设置为已认证
UPDATE `forum_user_profile`
SET
    `school_email_verified` = b'1',
    `school_email_verify_time` = NOW(),
    `school_name` = '测试大学',
    `school_email` = 'test@example.edu.cn',
    `school_email_prefix` = 'test',
    `real_name` = '张三',
    `update_time` = NOW()
WHERE `nickname` = '测试用户'                                 -- 替换为实际的昵称
  AND `deleted` = b'0';

-- ============================================

-- 方法4：批量设置所有用户为已认证（谨慎使用！）
UPDATE `forum_user_profile`
SET
    `school_email_verified` = b'1',
    `school_email_verify_time` = NOW(),
    `update_time` = NOW()
WHERE `deleted` = b'0';

-- ============================================

-- 查询用户认证状态
-- 查看所有用户的认证状态
SELECT
    `id`,
    `user_id`,
    `uid`,
    `nickname`,
    `school_name`,
    `school_email`,
    `school_email_verified`,
    `school_email_verify_time`,
    `real_name`
FROM `forum_user_profile`
WHERE `deleted` = b'0'
ORDER BY `id`;

-- ============================================

-- 查询特定用户的认证状态
SELECT
    `id`,
    `user_id`,
    `uid`,
    `nickname`,
    `school_name`,
    `school_email`,
    `school_email_verified`,
    `school_email_verify_time`,
    `real_name`
FROM `forum_user_profile`
WHERE `user_id` = 1                                          -- 替换为实际的用户ID
  AND `deleted` = b'0';

-- ============================================

-- 取消用户认证（恢复为未认证状态）
UPDATE `forum_user_profile`
SET
    `school_email_verified` = b'0',
    `school_email_verify_time` = NULL,
    `update_time` = NOW()
WHERE `user_id` = 1                                          -- 替换为实际的用户ID
  AND `deleted` = b'0';
