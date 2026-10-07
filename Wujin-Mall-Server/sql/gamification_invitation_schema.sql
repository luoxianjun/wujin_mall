-- 游戏化功能 - 邀请系统数据库表结构

-- 邀请配置表
CREATE TABLE IF NOT EXISTS `gamification_invitation_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `inviter_reward_points` int NOT NULL DEFAULT 100 COMMENT '邀请人奖励积分',
  `invitee_reward_points` int NOT NULL DEFAULT 50 COMMENT '被邀请人奖励积分',
  `daily_invite_limit` int NOT NULL DEFAULT 10 COMMENT '每日邀请上限',
  `invite_code_valid_days` int NOT NULL DEFAULT 30 COMMENT '邀请码有效期（天）',
  `enabled` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否启用邀请功能',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邀请配置表';

-- 邀请记录表
CREATE TABLE IF NOT EXISTS `gamification_invitation_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '邀请记录ID',
  `inviter_id` bigint NOT NULL COMMENT '邀请人ID',
  `invitee_id` bigint DEFAULT NULL COMMENT '被邀请人ID',
  `invite_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邀请码',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '邀请状态：0-待确认，1-已成功，2-已失效',
  `inviter_reward_points` int DEFAULT NULL COMMENT '邀请人获得积分',
  `invitee_reward_points` int DEFAULT NULL COMMENT '被邀请人获得积分',
  `invite_time` datetime NOT NULL COMMENT '邀请时间',
  `confirm_time` datetime DEFAULT NULL COMMENT '确认时间',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invite_code` (`invite_code`),
  KEY `idx_inviter_id` (`inviter_id`),
  KEY `idx_invitee_id` (`invitee_id`),
  KEY `idx_status` (`status`),
  KEY `idx_invite_time` (`invite_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邀请记录表';

-- 插入默认配置
INSERT INTO `gamification_invitation_config`
  (`id`, `inviter_reward_points`, `invitee_reward_points`, `daily_invite_limit`, `invite_code_valid_days`, `enabled`, `creator`, `updater`)
VALUES
  (1, 100, 50, 10, 30, b'1', 'system', 'system')
ON DUPLICATE KEY UPDATE
  `update_time` = CURRENT_TIMESTAMP;
