-- 添加活动开始提醒已通知标记字段
-- 用于防止重复通知，并支持新创建的活动即使距离开始不足2小时也能被通知到

ALTER TABLE `forum_activity` 
ADD COLUMN `start_notified` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否已发送活动开始提醒：0-否，1-是' AFTER `show_participant_count`;

-- 为已经结束的活动设置为已通知（避免给历史活动发送通知）
UPDATE `forum_activity` SET `start_notified` = b'1' WHERE `start_time` < NOW();
