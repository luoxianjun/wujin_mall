-- 清空论坛与会员模块业务数据（MySQL）
-- 使用前请确认：仅会清空业务数据，不会删除表结构；执行前建议备份。

SET FOREIGN_KEY_CHECKS = 0;

-- 论坛模块
TRUNCATE TABLE forum_message;
TRUNCATE TABLE forum_conversation;
TRUNCATE TABLE forum_comment_like;
TRUNCATE TABLE forum_post_like;
TRUNCATE TABLE forum_post_follow;
TRUNCATE TABLE forum_comment;
TRUNCATE TABLE forum_report;
TRUNCATE TABLE forum_system_notice;
TRUNCATE TABLE forum_post;
TRUNCATE TABLE forum_sign_record;
TRUNCATE TABLE forum_point_record;
TRUNCATE TABLE forum_user_profile;

-- 会员模块
TRUNCATE TABLE member_address;
TRUNCATE TABLE member_user;
TRUNCATE TABLE member_group;
TRUNCATE TABLE member_level;

SET FOREIGN_KEY_CHECKS = 1;
