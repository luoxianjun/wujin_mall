-- ===========================================================================
-- Gamification V1 合并到 Master 统一迁移脚本
-- 分支: feature/gamification-v1 → master
-- 生成时间: 2026-04-05
-- 
-- ⚠️ 执行前请务必备份数据库！
-- ⚠️ 此脚本仅适用于全新部署（数据库中不存在 gamification 相关表）
--    如果是增量更新，请根据实际情况选择性执行。
-- ===========================================================================

-- ###########################################################################
-- 第一部分：邀请系统（Invitation）
-- 说明：代码中 InvitationConfigServiceImpl 使用 key-value 模式
--       （configKey + configValue），对应 gamification_schema.sql 中的表结构。
--       旧的 gamification_invitation_schema.sql（独立字段模式）已废弃，不执行。
-- ###########################################################################

-- 1.1 邀请码表
CREATE TABLE IF NOT EXISTS `gamification_invitation_code` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `code` varchar(8) NOT NULL COMMENT '邀请码',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1-有效 2-无效',
  `total_invitations` int NOT NULL DEFAULT 0 COMMENT '总邀请数',
  `valid_invitations` int NOT NULL DEFAULT 0 COMMENT '有效邀请数',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`, `deleted`),
  UNIQUE KEY `uk_code` (`code`, `deleted`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邀请码表';

-- 1.2 邀请关系表
CREATE TABLE IF NOT EXISTS `gamification_invitation_relation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `inviter_id` bigint NOT NULL COMMENT '邀请人用户ID',
  `invitee_id` bigint NOT NULL COMMENT '被邀请人用户ID',
  `invitation_code` varchar(8) NOT NULL COMMENT '邀请码',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1-待处理 2-已完成 3-无效',
  `register_time` datetime NOT NULL COMMENT '注册时间',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `device_fingerprint` varchar(64) DEFAULT NULL COMMENT '设备指纹',
  `register_ip` varchar(64) DEFAULT NULL COMMENT '注册IP',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invitee_id` (`invitee_id`, `deleted`),
  KEY `idx_inviter_id` (`inviter_id`),
  KEY `idx_status` (`status`),
  KEY `idx_register_time` (`register_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邀请关系表';

-- 1.3 邀请奖励表
CREATE TABLE IF NOT EXISTS `gamification_invitation_reward` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `relation_id` bigint NOT NULL COMMENT '关系表ID',
  `inviter_id` bigint NOT NULL COMMENT '邀请人用户ID',
  `invitee_id` bigint NOT NULL COMMENT '被邀请人用户ID',
  `reward_type` varchar(32) NOT NULL COMMENT '奖励类型',
  `reward_points` int NOT NULL COMMENT '奖励积分',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1-待发放 2-已发放 3-已取消',
  `grant_time` datetime DEFAULT NULL COMMENT '发放时间',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_relation_id` (`relation_id`),
  KEY `idx_inviter_id` (`inviter_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邀请奖励表';

-- 1.4 邀请配置表（key-value 模式）
CREATE TABLE IF NOT EXISTS `gamification_invitation_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `config_key` varchar(64) NOT NULL COMMENT '配置键',
  `config_value` varchar(512) NOT NULL COMMENT '配置值',
  `description` varchar(256) DEFAULT NULL COMMENT '配置描述',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邀请配置表';

-- 1.5 邀请配置初始数据
INSERT INTO `gamification_invitation_config` (`config_key`, `config_value`, `description`) VALUES
('invitation.max_invitations', '100', '每人最大邀请数'),
('invitation.register_reward', '10', '注册奖励积分'),
('invitation.complete_reward', '20', '完成奖励积分'),
('invitation.invitee_reward', '5', '被邀请人奖励积分'),
('invitation.code_length', '8', '邀请码长度'),
('invitation.antifraud.ip_limit', '5', '每IP注册限制'),
('invitation.antifraud.device_limit', '3', '每设备注册限制');


-- ###########################################################################
-- 第二部分：答题系统（Quiz）
-- ###########################################################################

-- 2.1 题库表
CREATE TABLE IF NOT EXISTS `gamification_quiz_question_bank` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(128) NOT NULL COMMENT '题库名称',
  `description` varchar(512) DEFAULT NULL COMMENT '题库描述',
  `enabled` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否启用',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答题题库表';

-- 2.2 题目表
CREATE TABLE IF NOT EXISTS `gamification_quiz_question` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `bank_id` bigint NOT NULL COMMENT '题库ID',
  `question_type` varchar(32) NOT NULL COMMENT '题目类型',
  `content` varchar(2048) NOT NULL COMMENT '题目内容',
  `image_url` varchar(512) DEFAULT NULL COMMENT '题目图片URL',
  `score` int NOT NULL DEFAULT 0 COMMENT '题目分值',
  `explanation` varchar(2048) DEFAULT NULL COMMENT '解析',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_bank_id` (`bank_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答题题目表';

-- 2.3 题目选项表
CREATE TABLE IF NOT EXISTS `gamification_quiz_question_option` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `option_key` varchar(16) NOT NULL COMMENT '选项标识',
  `content` varchar(1024) NOT NULL COMMENT '选项内容',
  `is_correct` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否正确',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答题选项表';

-- 2.4 答题活动配置表
CREATE TABLE IF NOT EXISTS `gamification_quiz_activity` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `activity_id` bigint NOT NULL COMMENT '论坛活动ID',
  `question_bank_id` bigint NOT NULL COMMENT '题库ID',
  `question_count` int NOT NULL DEFAULT 0 COMMENT '题目数量',
  `max_attempts` int NOT NULL DEFAULT 1 COMMENT '最大答题次数',
  `duration_seconds` int NOT NULL DEFAULT 0 COMMENT '答题时长（秒）',
  `random_question_order` bit(1) NOT NULL DEFAULT b'0' COMMENT '随机题目顺序',
  `random_option_order` bit(1) NOT NULL DEFAULT b'0' COMMENT '随机选项顺序',
  `leaderboard_size` int NOT NULL DEFAULT 10 COMMENT '排行榜大小',
  `answer_reveal_mode` varchar(32) NOT NULL COMMENT '答案揭示模式',
  `status` varchar(32) NOT NULL COMMENT '答题状态',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_id` (`activity_id`, `deleted`),
  KEY `idx_question_bank_id` (`question_bank_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答题活动配置表';

-- 2.5 答题记录表
CREATE TABLE IF NOT EXISTS `gamification_quiz_attempt` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `quiz_activity_id` bigint NOT NULL COMMENT '答题活动ID',
  `activity_id` bigint NOT NULL COMMENT '论坛活动ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `attempt_no` int NOT NULL COMMENT '答题次数',
  `status` varchar(32) NOT NULL COMMENT '答题状态',
  `score` int NOT NULL DEFAULT 0 COMMENT '得分',
  `elapsed_millis` bigint NOT NULL DEFAULT 0 COMMENT '用时（毫秒）',
  `started_at` datetime NOT NULL COMMENT '开始时间',
  `submitted_at` datetime DEFAULT NULL COMMENT '提交时间',
  `invalidated_reason` varchar(128) DEFAULT NULL COMMENT '作废原因',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_quiz_user_attempt` (`quiz_activity_id`, `user_id`, `attempt_no`, `deleted`),
  KEY `idx_activity_user` (`activity_id`, `user_id`),
  KEY `idx_quiz_status` (`quiz_activity_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答题记录表';

-- 2.6 答题答案表
CREATE TABLE IF NOT EXISTS `gamification_quiz_answer` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `attempt_id` bigint NOT NULL COMMENT '答题记录ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `selected_option_keys` varchar(256) DEFAULT NULL COMMENT '选中的选项',
  `is_correct` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否正确',
  `earned_score` int NOT NULL DEFAULT 0 COMMENT '获得分数',
  `marked` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否标记',
  `answered_at` datetime DEFAULT NULL COMMENT '回答时间',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_attempt_id` (`attempt_id`),
  KEY `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答题答案表';

-- 2.7 答题奖励规则表
CREATE TABLE IF NOT EXISTS `gamification_quiz_reward_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `quiz_activity_id` bigint NOT NULL COMMENT '答题活动ID',
  `rank_start` int NOT NULL COMMENT '排名起始',
  `rank_end` int NOT NULL COMMENT '排名截止',
  `reward_type` varchar(32) NOT NULL COMMENT '奖励类型',
  `point_amount` int NOT NULL DEFAULT 0 COMMENT '积分数量',
  `reward_name` varchar(128) NOT NULL COMMENT '奖励名称',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_quiz_activity_id` (`quiz_activity_id`),
  KEY `idx_rank_range` (`quiz_activity_id`, `rank_start`, `rank_end`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答题奖励规则表';

-- 2.8 答题奖励记录表
CREATE TABLE IF NOT EXISTS `gamification_quiz_reward_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `quiz_activity_id` bigint NOT NULL COMMENT '答题活动ID',
  `attempt_id` bigint DEFAULT NULL COMMENT '答题记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `reward_type` varchar(32) NOT NULL COMMENT '奖励类型',
  `point_amount` int NOT NULL DEFAULT 0 COMMENT '积分数量',
  `reward_name` varchar(128) NOT NULL COMMENT '奖励名称',
  `status` varchar(32) NOT NULL COMMENT '奖励状态',
  `retry_count` int NOT NULL DEFAULT 0 COMMENT '重试次数',
  `distributed_at` datetime DEFAULT NULL COMMENT '发放时间',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_quiz_user_reward` (`quiz_activity_id`, `user_id`, `reward_name`, `deleted`),
  KEY `idx_reward_status` (`quiz_activity_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答题奖励记录表';


-- ###########################################################################
-- 第三部分：抽奖系统（Lottery）
-- 说明：代码中 LotteryPrizeDO 已经没有 lottery_activity_id 和 probability 字段，
--       所以这里直接创建解耦后的表结构，跳过旧的历史迁移步骤。
-- ###########################################################################

-- 3.1 抽奖活动表
CREATE TABLE IF NOT EXISTS `gamification_lottery_activity` (
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

-- 3.2 抽奖奖品表（已解耦，无 lottery_activity_id 和 probability 字段）
CREATE TABLE IF NOT EXISTS `gamification_lottery_prize` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(128) NOT NULL COMMENT '奖品名称',
  `type` tinyint NOT NULL DEFAULT 0 COMMENT '奖品类型：0=积分, 1=优惠券, 2=实物, 3=虚拟, 4=谢谢参与',
  `value` int DEFAULT 0 COMMENT '积分数量（积分类型时使用）',
  `image_url` varchar(512) DEFAULT NULL COMMENT '奖品图片URL',
  `total_stock` int NOT NULL DEFAULT 0 COMMENT '总库存',
  `remaining_stock` int NOT NULL DEFAULT 0 COMMENT '剩余库存',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `require_address` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否需要填写地址',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='抽奖奖品表';

-- 3.3 抽奖活动-奖品关联表（M:N 多对多关系）
CREATE TABLE IF NOT EXISTS `gamification_lottery_activity_prize` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `lottery_activity_id` bigint NOT NULL COMMENT '关联的抽奖活动ID',
  `prize_id` bigint NOT NULL COMMENT '关联的奖品ID',
  `probability` decimal(10,2) NOT NULL DEFAULT 0 COMMENT '中奖概率百分比（0-100）',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序顺序',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_lottery_activity_id` (`lottery_activity_id`),
  KEY `idx_prize_id` (`prize_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='抽奖活动-奖品关联表';

-- 3.4 抽奖记录表
CREATE TABLE IF NOT EXISTS `gamification_lottery_record` (
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


-- ###########################################################################
-- 第四部分：投票系统（Vote）— 独立投票活动
-- ###########################################################################

-- 4.1 投票活动表
CREATE TABLE IF NOT EXISTS `gamification_vote_activity` (
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

-- 4.2 投票选项表
CREATE TABLE IF NOT EXISTS `gamification_vote_option` (
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

-- 4.3 投票记录表
CREATE TABLE IF NOT EXISTS `gamification_vote_record` (
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


-- ###########################################################################
-- 第五部分：帖子内嵌投票（Post Vote）— 轻量级帖子投票
-- ###########################################################################

-- 5.1 帖子投票表
CREATE TABLE IF NOT EXISTS `forum_post_vote` (
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

-- 5.2 帖子投票选项表
CREATE TABLE IF NOT EXISTS `forum_post_vote_option` (
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

-- 5.3 帖子投票记录表
CREATE TABLE IF NOT EXISTS `forum_post_vote_record` (
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


-- ###########################################################################
-- 第六部分：系统配置数据（字典、定时任务、菜单权限）
-- ###########################################################################

-- ===========================================================
-- 6.1 活动分类字典数据
-- ===========================================================

-- 添加"抽奖"活动分类
INSERT INTO `system_dict_data` (
  `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT 11, '抽奖', '11', 'frum_activity_type', 0, 'warning', '', '抽奖活动',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_dict_data`
  WHERE `deleted` = b'0' AND `dict_type` = 'frum_activity_type' AND `value` = '11'
);

-- 添加"答题"活动分类
INSERT INTO `system_dict_data` (
  `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT 12, '答题', '12', 'frum_activity_type', 0, 'success', '', '答题活动',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_dict_data`
  WHERE `deleted` = b'0' AND `dict_type` = 'frum_activity_type' AND `value` = '12'
);

-- ===========================================================
-- 6.2 答题奖励结算定时任务
-- 说明：执行后还需在管理后台"定时任务"页面点击"同步定时任务"
-- ===========================================================

INSERT INTO `infra_job` (
  `name`, `status`, `handler_name`, `handler_param`,
  `cron_expression`, `retry_count`, `retry_interval`, `monitor_timeout`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  '答题奖励结算 Job', 1, 'quizRewardDistributionJob', NULL,
  '0 */5 * * * ?', 0, 0, 0,
  'admin', NOW(), 'admin', NOW(), b'0'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `infra_job`
  WHERE `handler_name` = 'quizRewardDistributionJob' AND `deleted` = b'0'
);

-- ===========================================================
-- 6.3 抽奖管理后台菜单
-- ===========================================================

-- 获取论坛管理根菜单ID
SET @forum_root_id := COALESCE(
  (SELECT `id`
   FROM `system_menu`
   WHERE `deleted` = b'0' AND `parent_id` = 0 AND `path` = '/forum'
   LIMIT 1),
  6100
);

-- 二级菜单 ID 定义
SET @lottery_activity_menu_id := COALESCE(
  (SELECT `id`
   FROM `system_menu`
   WHERE `deleted` = b'0' AND `component` = 'forum/lottery/index'
   LIMIT 1),
  6108
);

SET @lottery_prize_menu_id := COALESCE(
  (SELECT `id`
   FROM `system_menu`
   WHERE `deleted` = b'0' AND `component` = 'forum/lotteryPrize/index'
   LIMIT 1),
  6109
);

SET @lottery_record_menu_id := COALESCE(
  (SELECT `id`
   FROM `system_menu`
   WHERE `deleted` = b'0' AND `component` = 'forum/lotteryRecord/index'
   LIMIT 1),
  6110
);

-- 抽奖活动管理菜单
INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  @lottery_activity_menu_id, '抽奖活动', '', 2, 7, @forum_root_id, 'lottery', 'ep:present',
  'forum/lottery/index', 'ForumLottery', 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `deleted` = b'0' AND `id` = @lottery_activity_menu_id
);

-- 抽奖奖品管理菜单
INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  @lottery_prize_menu_id, '抽奖奖品', '', 2, 8, @forum_root_id, 'lottery-prize', 'ep:gift',
  'forum/lotteryPrize/index', 'ForumLotteryPrize', 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `deleted` = b'0' AND `id` = @lottery_prize_menu_id
);

-- 抽奖记录管理菜单
INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  @lottery_record_menu_id, '抽奖记录', '', 2, 9, @forum_root_id, 'lottery-record', 'ep:document',
  'forum/lotteryRecord/index', 'ForumLotteryRecord', 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `deleted` = b'0' AND `id` = @lottery_record_menu_id
);

-- 抽奖活动权限按钮
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 6181, '抽奖活动查询', 'forum:lottery-activity:query', 3, 1, @lottery_activity_menu_id, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `permission` = 'forum:lottery-activity:query');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 6182, '抽奖活动创建', 'forum:lottery-activity:create', 3, 2, @lottery_activity_menu_id, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `permission` = 'forum:lottery-activity:create');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 6183, '抽奖活动更新', 'forum:lottery-activity:update', 3, 3, @lottery_activity_menu_id, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `permission` = 'forum:lottery-activity:update');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 6184, '抽奖活动删除', 'forum:lottery-activity:delete', 3, 4, @lottery_activity_menu_id, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `permission` = 'forum:lottery-activity:delete');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 6185, '抽奖活动开奖', 'forum:lottery-activity:draw', 3, 5, @lottery_activity_menu_id, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `permission` = 'forum:lottery-activity:draw');

-- 抽奖奖品权限按钮
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 6191, '抽奖奖品查询', 'forum:lottery-prize:query', 3, 1, @lottery_prize_menu_id, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `permission` = 'forum:lottery-prize:query');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 6192, '抽奖奖品创建', 'forum:lottery-prize:create', 3, 2, @lottery_prize_menu_id, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `permission` = 'forum:lottery-prize:create');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 6193, '抽奖奖品更新', 'forum:lottery-prize:update', 3, 3, @lottery_prize_menu_id, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `permission` = 'forum:lottery-prize:update');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 6194, '抽奖奖品删除', 'forum:lottery-prize:delete', 3, 4, @lottery_prize_menu_id, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `permission` = 'forum:lottery-prize:delete');

-- 抽奖记录权限按钮
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 6201, '抽奖记录查询', 'forum:lottery-record:query', 3, 1, @lottery_record_menu_id, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `permission` = 'forum:lottery-record:query');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 6202, '抽奖记录发放', 'forum:lottery-record:deliver', 3, 2, @lottery_record_menu_id, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `permission` = 'forum:lottery-record:deliver');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 6203, '抽奖记录导出', 'forum:lottery-record:export', 3, 3, @lottery_record_menu_id, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `permission` = 'forum:lottery-record:export');

-- ===========================================================
-- 6.4 投票管理后台菜单
-- ===========================================================

SET @vote_activity_menu_id := COALESCE(
  (SELECT `id`
   FROM `system_menu`
   WHERE `deleted` = b'0' AND `component` = 'forum/vote/index'
   LIMIT 1),
  6111
);

-- 投票活动管理菜单
INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  @vote_activity_menu_id, '投票活动', '', 2, 10, @forum_root_id, 'vote', 'ep:pie-chart',
  'forum/vote/index', 'ForumVote', 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `deleted` = b'0' AND `id` = @vote_activity_menu_id
);

-- 投票活动权限按钮
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 6211, '投票活动查询', 'gamification:vote-activity:query', 3, 1, @vote_activity_menu_id, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `permission` = 'gamification:vote-activity:query');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 6212, '投票活动创建', 'gamification:vote-activity:create', 3, 2, @vote_activity_menu_id, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `permission` = 'gamification:vote-activity:create');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 6213, '投票活动更新', 'gamification:vote-activity:update', 3, 3, @vote_activity_menu_id, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `permission` = 'gamification:vote-activity:update');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 6214, '投票活动删除', 'gamification:vote-activity:delete', 3, 4, @vote_activity_menu_id, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `permission` = 'gamification:vote-activity:delete');


-- ###########################################################################
-- 执行完成后提醒
-- ###########################################################################
-- 1. 在管理后台 → 定时任务 → 点击"同步定时任务"
-- 2. 为超级管理员角色分配新增的菜单权限
-- 3. 刷新前端页面缓存
-- ###########################################################################
