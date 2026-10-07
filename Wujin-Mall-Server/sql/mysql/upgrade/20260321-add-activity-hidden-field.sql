-- 新增活动隐藏字段
-- ALTER TABLE forum_activity ADD COLUMN hidden BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否隐藏：true-隐藏，false-展示' AFTER show_participant_count;

-- 将现有活动设置为不隐藏（默认展示）
UPDATE forum_activity SET hidden = b'0' WHERE hidden IS NULL;
