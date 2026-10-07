-- Gamification schema

-- ================================
-- Invitation tables
-- ================================

CREATE TABLE IF NOT EXISTS `gamification_invitation_code` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `user_id` bigint NOT NULL COMMENT 'User id',
  `code` varchar(8) NOT NULL COMMENT 'Invitation code',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1-valid 2-invalid',
  `total_invitations` int NOT NULL DEFAULT 0 COMMENT 'Total invitations',
  `valid_invitations` int NOT NULL DEFAULT 0 COMMENT 'Valid invitations',
  `creator` varchar(64) DEFAULT '' COMMENT 'Creator',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `updater` varchar(64) DEFAULT '' COMMENT 'Updater',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT 'Deleted flag',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT 'Tenant id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`, `deleted`),
  UNIQUE KEY `uk_code` (`code`, `deleted`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Invitation code';

CREATE TABLE IF NOT EXISTS `gamification_invitation_relation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `inviter_id` bigint NOT NULL COMMENT 'Inviter user id',
  `invitee_id` bigint NOT NULL COMMENT 'Invitee user id',
  `invitation_code` varchar(8) NOT NULL COMMENT 'Invitation code',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1-pending 2-completed 3-invalid',
  `register_time` datetime NOT NULL COMMENT 'Register time',
  `complete_time` datetime DEFAULT NULL COMMENT 'Complete time',
  `device_fingerprint` varchar(64) DEFAULT NULL COMMENT 'Device fingerprint',
  `register_ip` varchar(64) DEFAULT NULL COMMENT 'Register ip',
  `creator` varchar(64) DEFAULT '' COMMENT 'Creator',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `updater` varchar(64) DEFAULT '' COMMENT 'Updater',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT 'Deleted flag',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT 'Tenant id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invitee_id` (`invitee_id`, `deleted`),
  KEY `idx_inviter_id` (`inviter_id`),
  KEY `idx_status` (`status`),
  KEY `idx_register_time` (`register_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Invitation relation';

CREATE TABLE IF NOT EXISTS `gamification_invitation_reward` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `relation_id` bigint NOT NULL COMMENT 'Relation id',
  `inviter_id` bigint NOT NULL COMMENT 'Inviter user id',
  `invitee_id` bigint NOT NULL COMMENT 'Invitee user id',
  `reward_type` varchar(32) NOT NULL COMMENT 'Reward type',
  `reward_points` int NOT NULL COMMENT 'Reward points',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1-pending 2-granted 3-cancelled',
  `grant_time` datetime DEFAULT NULL COMMENT 'Grant time',
  `creator` varchar(64) DEFAULT '' COMMENT 'Creator',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `updater` varchar(64) DEFAULT '' COMMENT 'Updater',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT 'Deleted flag',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT 'Tenant id',
  PRIMARY KEY (`id`),
  KEY `idx_relation_id` (`relation_id`),
  KEY `idx_inviter_id` (`inviter_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Invitation reward';

CREATE TABLE IF NOT EXISTS `gamification_invitation_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `config_key` varchar(64) NOT NULL COMMENT 'Config key',
  `config_value` varchar(512) NOT NULL COMMENT 'Config value',
  `description` varchar(256) DEFAULT NULL COMMENT 'Description',
  `creator` varchar(64) DEFAULT '' COMMENT 'Creator',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `updater` varchar(64) DEFAULT '' COMMENT 'Updater',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT 'Deleted flag',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT 'Tenant id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Invitation config';

INSERT INTO `gamification_invitation_config` (`config_key`, `config_value`, `description`) VALUES
('invitation.max_invitations', '100', 'Maximum invitations per user'),
('invitation.register_reward', '10', 'Register reward points'),
('invitation.complete_reward', '20', 'Completion reward points'),
('invitation.invitee_reward', '5', 'Invitee reward points'),
('invitation.code_length', '8', 'Invitation code length'),
('invitation.antifraud.ip_limit', '5', 'Registration limit per IP'),
('invitation.antifraud.device_limit', '3', 'Registration limit per device');

-- ================================
-- Quiz tables
-- ================================

CREATE TABLE IF NOT EXISTS `gamification_quiz_question_bank` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `name` varchar(128) NOT NULL COMMENT 'Question bank name',
  `description` varchar(512) DEFAULT NULL COMMENT 'Question bank description',
  `enabled` bit(1) NOT NULL DEFAULT b'1' COMMENT 'Enabled flag',
  `creator` varchar(64) DEFAULT '' COMMENT 'Creator',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `updater` varchar(64) DEFAULT '' COMMENT 'Updater',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT 'Deleted flag',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT 'Tenant id',
  PRIMARY KEY (`id`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Quiz question bank';

CREATE TABLE IF NOT EXISTS `gamification_quiz_question` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `bank_id` bigint NOT NULL COMMENT 'Question bank id',
  `question_type` varchar(32) NOT NULL COMMENT 'Question type',
  `content` varchar(2048) NOT NULL COMMENT 'Question content',
  `image_url` varchar(512) DEFAULT NULL COMMENT 'Question image url',
  `score` int NOT NULL DEFAULT 0 COMMENT 'Question score',
  `explanation` varchar(2048) DEFAULT NULL COMMENT 'Explanation',
  `sort` int NOT NULL DEFAULT 0 COMMENT 'Sort',
  `creator` varchar(64) DEFAULT '' COMMENT 'Creator',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `updater` varchar(64) DEFAULT '' COMMENT 'Updater',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT 'Deleted flag',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT 'Tenant id',
  PRIMARY KEY (`id`),
  KEY `idx_bank_id` (`bank_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Quiz question';

CREATE TABLE IF NOT EXISTS `gamification_quiz_question_option` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `question_id` bigint NOT NULL COMMENT 'Question id',
  `option_key` varchar(16) NOT NULL COMMENT 'Option key',
  `content` varchar(1024) NOT NULL COMMENT 'Option content',
  `is_correct` bit(1) NOT NULL DEFAULT b'0' COMMENT 'Correct flag',
  `sort` int NOT NULL DEFAULT 0 COMMENT 'Sort',
  `creator` varchar(64) DEFAULT '' COMMENT 'Creator',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `updater` varchar(64) DEFAULT '' COMMENT 'Updater',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT 'Deleted flag',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT 'Tenant id',
  PRIMARY KEY (`id`),
  KEY `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Quiz question option';

CREATE TABLE IF NOT EXISTS `gamification_quiz_activity` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `activity_id` bigint NOT NULL COMMENT 'Forum activity id',
  `question_bank_id` bigint NOT NULL COMMENT 'Question bank id',
  `question_count` int NOT NULL DEFAULT 0 COMMENT 'Question count',
  `max_attempts` int NOT NULL DEFAULT 1 COMMENT 'Max attempts',
  `duration_seconds` int NOT NULL DEFAULT 0 COMMENT 'Duration seconds',
  `random_question_order` bit(1) NOT NULL DEFAULT b'0' COMMENT 'Random question order',
  `random_option_order` bit(1) NOT NULL DEFAULT b'0' COMMENT 'Random option order',
  `leaderboard_size` int NOT NULL DEFAULT 10 COMMENT 'Leaderboard size',
  `answer_reveal_mode` varchar(32) NOT NULL COMMENT 'Answer reveal mode',
  `status` varchar(32) NOT NULL COMMENT 'Quiz status',
  `creator` varchar(64) DEFAULT '' COMMENT 'Creator',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `updater` varchar(64) DEFAULT '' COMMENT 'Updater',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT 'Deleted flag',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT 'Tenant id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_id` (`activity_id`, `deleted`),
  KEY `idx_question_bank_id` (`question_bank_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Quiz activity config';

CREATE TABLE IF NOT EXISTS `gamification_quiz_attempt` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `quiz_activity_id` bigint NOT NULL COMMENT 'Quiz activity id',
  `activity_id` bigint NOT NULL COMMENT 'Forum activity id',
  `user_id` bigint NOT NULL COMMENT 'User id',
  `attempt_no` int NOT NULL COMMENT 'Attempt number',
  `status` varchar(32) NOT NULL COMMENT 'Attempt status',
  `score` int NOT NULL DEFAULT 0 COMMENT 'Score',
  `elapsed_millis` bigint NOT NULL DEFAULT 0 COMMENT 'Elapsed milliseconds',
  `started_at` datetime NOT NULL COMMENT 'Start time',
  `submitted_at` datetime DEFAULT NULL COMMENT 'Submit time',
  `invalidated_reason` varchar(128) DEFAULT NULL COMMENT 'Invalidated reason',
  `creator` varchar(64) DEFAULT '' COMMENT 'Creator',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `updater` varchar(64) DEFAULT '' COMMENT 'Updater',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT 'Deleted flag',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT 'Tenant id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_quiz_user_attempt` (`quiz_activity_id`, `user_id`, `attempt_no`, `deleted`),
  KEY `idx_activity_user` (`activity_id`, `user_id`),
  KEY `idx_quiz_status` (`quiz_activity_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Quiz attempt';

CREATE TABLE IF NOT EXISTS `gamification_quiz_answer` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `attempt_id` bigint NOT NULL COMMENT 'Attempt id',
  `question_id` bigint NOT NULL COMMENT 'Question id',
  `selected_option_keys` varchar(256) DEFAULT NULL COMMENT 'Selected option keys',
  `is_correct` bit(1) NOT NULL DEFAULT b'0' COMMENT 'Correct flag',
  `earned_score` int NOT NULL DEFAULT 0 COMMENT 'Earned score',
  `marked` bit(1) NOT NULL DEFAULT b'0' COMMENT 'Marked for later',
  `answered_at` datetime DEFAULT NULL COMMENT 'Answered time',
  `creator` varchar(64) DEFAULT '' COMMENT 'Creator',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `updater` varchar(64) DEFAULT '' COMMENT 'Updater',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT 'Deleted flag',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT 'Tenant id',
  PRIMARY KEY (`id`),
  KEY `idx_attempt_id` (`attempt_id`),
  KEY `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Quiz answer';

CREATE TABLE IF NOT EXISTS `gamification_quiz_reward_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `quiz_activity_id` bigint NOT NULL COMMENT 'Quiz activity id',
  `rank_start` int NOT NULL COMMENT 'Rank start',
  `rank_end` int NOT NULL COMMENT 'Rank end',
  `reward_type` varchar(32) NOT NULL COMMENT 'Reward type',
  `point_amount` int NOT NULL DEFAULT 0 COMMENT 'Point amount',
  `reward_name` varchar(128) NOT NULL COMMENT 'Reward name',
  `creator` varchar(64) DEFAULT '' COMMENT 'Creator',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `updater` varchar(64) DEFAULT '' COMMENT 'Updater',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT 'Deleted flag',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT 'Tenant id',
  PRIMARY KEY (`id`),
  KEY `idx_quiz_activity_id` (`quiz_activity_id`),
  KEY `idx_rank_range` (`quiz_activity_id`, `rank_start`, `rank_end`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Quiz reward rule';

CREATE TABLE IF NOT EXISTS `gamification_quiz_reward_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `quiz_activity_id` bigint NOT NULL COMMENT 'Quiz activity id',
  `attempt_id` bigint DEFAULT NULL COMMENT 'Attempt id',
  `user_id` bigint NOT NULL COMMENT 'User id',
  `reward_type` varchar(32) NOT NULL COMMENT 'Reward type',
  `point_amount` int NOT NULL DEFAULT 0 COMMENT 'Point amount',
  `reward_name` varchar(128) NOT NULL COMMENT 'Reward name',
  `status` varchar(32) NOT NULL COMMENT 'Reward status',
  `retry_count` int NOT NULL DEFAULT 0 COMMENT 'Retry count',
  `distributed_at` datetime DEFAULT NULL COMMENT 'Distributed time',
  `creator` varchar(64) DEFAULT '' COMMENT 'Creator',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `updater` varchar(64) DEFAULT '' COMMENT 'Updater',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT 'Deleted flag',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT 'Tenant id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_quiz_user_reward` (`quiz_activity_id`, `user_id`, `reward_name`, `deleted`),
  KEY `idx_reward_status` (`quiz_activity_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Quiz reward record';
