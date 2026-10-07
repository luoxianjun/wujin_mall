-- =============================================
-- 抽奖系统数据库表
-- Gamification Lottery System Tables
-- Created: 2026-03-23
-- =============================================

-- ----------------------------
-- 抽奖活动表
-- ----------------------------
DROP TABLE IF EXISTS `gamification_lottery_activity`;
CREATE TABLE `gamification_lottery_activity` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `activity_id` bigint NOT NULL COMMENT '关联论坛活动ID',
  `type` tinyint NOT NULL DEFAULT 0 COMMENT '抽奖类型：0=定时开奖, 1=即时摇一摇',
  `draw_time` datetime DEFAULT NULL COMMENT '定时开奖时间',
  `max_draws_per_day` int DEFAULT NULL COMMENT '每日抽奖次数上限',
  `max_draws_total` int DEFAULT NULL COMMENT '总抽奖次数上限',
  `cost_type` tinyint NOT NULL DEFAULT 0 COMMENT '费用类型：0=免费, 1=积分',
  `cost_amount` int DEFAULT 0 COMMENT '积分消耗数量',
  `guarantee_draws` int DEFAULT 0 COMMENT '保底次数（0=禁用）',
  `participation_condition` tinyint NOT NULL DEFAULT 0 COMMENT '参与条件：0=所有人, 1=已报名, 2=受邀',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0=启用, 1=禁用',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_type_status` (`type`, `status`),
  KEY `idx_draw_time` (`draw_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='抽奖活动表';

-- ----------------------------
-- 抽奖奖品表
-- ----------------------------
DROP TABLE IF EXISTS `gamification_lottery_prize`;
CREATE TABLE `gamification_lottery_prize` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `lottery_activity_id` bigint NOT NULL COMMENT '抽奖活动ID',
  `name` varchar(128) NOT NULL COMMENT '奖品名称',
  `type` tinyint NOT NULL DEFAULT 0 COMMENT '奖品类型：0=积分, 1=优惠券, 2=实物, 3=虚拟, 4=谢谢参与',
  `value` int DEFAULT 0 COMMENT '积分数量（积分类型时使用）',
  `image_url` varchar(512) DEFAULT NULL COMMENT '奖品图片URL',
  `total_stock` int NOT NULL DEFAULT 0 COMMENT '总库存',
  `remaining_stock` int NOT NULL DEFAULT 0 COMMENT '剩余库存',
  `probability` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '中奖概率（0-100%）',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `require_address` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否需要填写地址',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_lottery_activity_id` (`lottery_activity_id`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='抽奖奖品表';

-- ----------------------------
-- 抽奖记录表
-- ----------------------------
DROP TABLE IF EXISTS `gamification_lottery_record`;
CREATE TABLE `gamification_lottery_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `lottery_activity_id` bigint NOT NULL COMMENT '抽奖活动ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `prize_id` bigint DEFAULT NULL COMMENT '奖品ID',
  `prize_name` varchar(128) DEFAULT NULL COMMENT '奖品名称',
  `prize_type` tinyint DEFAULT NULL COMMENT '奖品类型',
  `won` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否中奖',
  `draw_time` datetime NOT NULL COMMENT '抽奖时间',
  `delivered` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否已发放',
  `delivery_address` varchar(512) DEFAULT NULL COMMENT '收货地址',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_lottery_activity_id` (`lottery_activity_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_draw_time` (`draw_time`),
  KEY `idx_user_activity` (`user_id`, `lottery_activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='抽奖记录表';
