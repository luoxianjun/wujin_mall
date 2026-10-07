-- 活动新增跳转小程序配置字段
ALTER TABLE `forum_activity`
    ADD COLUMN `redirect_app_id` varchar(64) DEFAULT NULL COMMENT '跳转小程序appId' AFTER `show_participant_count`,
    ADD COLUMN `redirect_app_path` varchar(512) DEFAULT NULL COMMENT '跳转小程序页面路径' AFTER `redirect_app_id`,
    ADD COLUMN `redirect_app_name` varchar(128) DEFAULT NULL COMMENT '跳转小程序名称（按钮显示文案）' AFTER `redirect_app_path`;
