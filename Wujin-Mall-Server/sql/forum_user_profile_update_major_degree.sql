-- 论坛用户资料表字段更新：将 majorAndGrade 拆分为 major、enrollYear，并添加 degree 字段
-- 执行前请备份数据

-- 1. 添加新字段
ALTER TABLE `forum_user_profile` 
ADD COLUMN `major` varchar(64) DEFAULT NULL COMMENT '专业' AFTER `gender`,
ADD COLUMN `enroll_year` varchar(10) DEFAULT NULL COMMENT '入学年份' AFTER `major`,
ADD COLUMN `degree` varchar(20) DEFAULT NULL COMMENT '学历：undergraduate-本科, master-硕士, doctor-博士' AFTER `enroll_year`;

-- 2. 迁移数据（将旧的 majorAndGrade 数据迁移到 major 字段，作为兼容）
UPDATE `forum_user_profile` 
SET `major` = `major_and_grade` 
WHERE `major_and_grade` IS NOT NULL AND `major` IS NULL;

-- 3. 如果不再需要旧字段，可以删除（建议先观察一段时间再删除）
-- ALTER TABLE `forum_user_profile` DROP COLUMN `major_and_grade`;
