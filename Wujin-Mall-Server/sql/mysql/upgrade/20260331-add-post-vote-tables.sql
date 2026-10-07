-- =============================================
-- 帖子内嵌投票数据库表 (Post-Embedded Voting)
-- Created: 2026-03-31
-- =============================================

-- ----------------------------
-- 帖子投票表（轻量级，独立于独立投票活动）
-- ----------------------------
DROP TABLE IF EXISTS `forum_post_vote`;
CREATE TABLE `forum_post_vote` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `vote_type` tinyint NOT NULL DEFAULT 0 COMMENT '0=单选, 1=多选',
  `max_choices` int DEFAULT 1 COMMENT '最多选择数',
  `end_time` datetime DEFAULT NULL COMMENT '截止时间',
  `anonymous` bit(1) NOT NULL DEFAULT b'1' COMMENT '匿名投票',
  `show_realtime_result` bit(1) NOT NULL DEFAULT b'1' COMMENT '显示实时结果',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子投票表';

-- ----------------------------
-- 帖子投票选项表
-- ----------------------------
DROP TABLE IF EXISTS `forum_post_vote_option`;
CREATE TABLE `forum_post_vote_option` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `vote_id` bigint NOT NULL COMMENT '投票ID',
  `title` varchar(256) NOT NULL COMMENT '选项内容',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_vote_id` (`vote_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子投票选项表';

-- ----------------------------
-- 帖子投票记录表
-- ----------------------------
DROP TABLE IF EXISTS `forum_post_vote_record`;
CREATE TABLE `forum_post_vote_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `vote_id` bigint NOT NULL COMMENT '投票ID',
  `option_id` bigint NOT NULL COMMENT '选项ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_vote_id` (`vote_id`),
  KEY `idx_user_id` (`user_id`),
  UNIQUE KEY `uk_vote_user_option` (`vote_id`, `user_id`, `option_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子投票记录表';
