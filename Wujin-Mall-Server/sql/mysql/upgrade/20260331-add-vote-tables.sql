-- =============================================
-- 投票系统数据库表
-- Gamification Voting System Tables
-- Created: 2026-03-31
-- =============================================

-- ----------------------------
-- 投票活动表（独立投票活动，关联论坛活动）
-- ----------------------------
DROP TABLE IF EXISTS `gamification_vote_activity`;
CREATE TABLE `gamification_vote_activity` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `activity_id` bigint NOT NULL COMMENT '关联论坛活动ID',
  `vote_type` tinyint NOT NULL DEFAULT 0 COMMENT '投票类型：0=单选, 1=多选, 2=排序',
  `max_choices` int DEFAULT 1 COMMENT '最多可选数量（多选时使用）',
  `anonymous` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否匿名投票',
  `show_realtime_result` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否显示实时结果',
  `allow_user_add_option` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否允许用户添加选项',
  `require_real_name` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否要求实名用户参与',
  `max_options` int NOT NULL DEFAULT 10 COMMENT '选项数量上限',
  `end_time` datetime DEFAULT NULL COMMENT '投票截止时间',
  `min_participants` int DEFAULT 0 COMMENT '最低参与人数',
  `allow_comment` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否允许评论',
  `votes_per_user` int NOT NULL DEFAULT 1 COMMENT '每人票数',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0=启用, 1=禁用',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投票活动表';

-- ----------------------------
-- 投票选项表
-- ----------------------------
DROP TABLE IF EXISTS `gamification_vote_option`;
CREATE TABLE `gamification_vote_option` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `vote_activity_id` bigint NOT NULL COMMENT '投票活动ID',
  `title` varchar(256) NOT NULL COMMENT '选项标题',
  `image_url` varchar(512) DEFAULT NULL COMMENT '选项配图URL',
  `description` varchar(1024) DEFAULT NULL COMMENT '选项描述',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `added_by_user` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否用户添加',
  `user_id` bigint DEFAULT NULL COMMENT '添加用户ID（用户添加时）',
  `audit_status` tinyint NOT NULL DEFAULT 1 COMMENT '审核状态：0=待审核, 1=通过, 2=拒绝',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_vote_activity_id` (`vote_activity_id`),
  KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投票选项表';

-- ----------------------------
-- 投票记录表
-- ----------------------------
DROP TABLE IF EXISTS `gamification_vote_record`;
CREATE TABLE `gamification_vote_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `vote_activity_id` bigint NOT NULL COMMENT '投票活动ID',
  `option_id` bigint NOT NULL COMMENT '选项ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `rank_order` int DEFAULT NULL COMMENT '排序名次（排序投票使用）',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_vote_activity_id` (`vote_activity_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_option_id` (`option_id`),
  UNIQUE KEY `uk_activity_user_option` (`vote_activity_id`, `user_id`, `option_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投票记录表';
