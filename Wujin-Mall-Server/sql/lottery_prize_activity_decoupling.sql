-- ============================================================
-- Prize-Activity Decoupling Migration
-- Changes 1:N (prize → activity) to M:N via junction table
-- ============================================================

-- 1. Create junction table
CREATE TABLE IF NOT EXISTS `gamification_lottery_activity_prize` (
    `id`                   BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `lottery_activity_id`  BIGINT NOT NULL COMMENT '关联的抽奖活动ID',
    `prize_id`             BIGINT NOT NULL COMMENT '关联的奖品ID',
    `probability`          DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '中奖概率百分比（0-100）',
    `sort_order`           INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    `creator`              VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`              VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`              BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`            BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    INDEX `idx_lottery_activity_id` (`lottery_activity_id`),
    INDEX `idx_prize_id` (`prize_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='抽奖活动-奖品关联表';

-- 2. Migrate existing prize-activity associations to junction table
INSERT INTO `gamification_lottery_activity_prize`
    (`lottery_activity_id`, `prize_id`, `probability`, `sort_order`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT
    `lottery_activity_id`, `id`, `probability`, `sort_order`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
FROM `gamification_lottery_prize`
WHERE `lottery_activity_id` IS NOT NULL AND `deleted` = 0;

-- 3. Remove columns from prize table
ALTER TABLE `gamification_lottery_prize`
    DROP COLUMN `lottery_activity_id`,
    DROP COLUMN `probability`;
