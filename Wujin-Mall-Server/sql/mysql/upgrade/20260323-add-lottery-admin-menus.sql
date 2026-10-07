-- =============================================
-- 抽奖活动管理菜单 (Lottery Activity Admin Menus)
-- Created: 2026-03-23
-- =============================================

-- 获取论坛管理根菜单ID
SET @forum_root_id := COALESCE(
  (SELECT `id`
   FROM `system_menu`
   WHERE `deleted` = b'0' AND `parent_id` = 0 AND `path` = '/forum'
   LIMIT 1),
  6100
);

-- ----------------------------
-- 二级菜单 ID 定义
-- ----------------------------
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

-- =============================
-- 二级菜单 (Page Menus)
-- =============================

-- 抽奖活动管理
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
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `id` = @lottery_activity_menu_id
);

-- 抽奖奖品管理
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
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `id` = @lottery_prize_menu_id
);

-- 抽奖记录管理
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
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `id` = @lottery_record_menu_id
);

-- =============================
-- 三级按钮权限 (Button Permissions)
-- =============================

-- ---- 抽奖活动权限 ----

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6181, '抽奖活动查询', 'forum:lottery-activity:query', 3, 1, @lottery_activity_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:lottery-activity:query'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6182, '抽奖活动创建', 'forum:lottery-activity:create', 3, 2, @lottery_activity_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:lottery-activity:create'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6183, '抽奖活动更新', 'forum:lottery-activity:update', 3, 3, @lottery_activity_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:lottery-activity:update'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6184, '抽奖活动删除', 'forum:lottery-activity:delete', 3, 4, @lottery_activity_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:lottery-activity:delete'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6185, '抽奖活动开奖', 'forum:lottery-activity:draw', 3, 5, @lottery_activity_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:lottery-activity:draw'
);

-- ---- 抽奖奖品权限 ----

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6191, '抽奖奖品查询', 'forum:lottery-prize:query', 3, 1, @lottery_prize_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:lottery-prize:query'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6192, '抽奖奖品创建', 'forum:lottery-prize:create', 3, 2, @lottery_prize_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:lottery-prize:create'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6193, '抽奖奖品更新', 'forum:lottery-prize:update', 3, 3, @lottery_prize_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:lottery-prize:update'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6194, '抽奖奖品删除', 'forum:lottery-prize:delete', 3, 4, @lottery_prize_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:lottery-prize:delete'
);

-- ---- 抽奖记录权限 ----

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6201, '抽奖记录查询', 'forum:lottery-record:query', 3, 1, @lottery_record_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:lottery-record:query'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6202, '抽奖记录发放', 'forum:lottery-record:deliver', 3, 2, @lottery_record_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:lottery-record:deliver'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6203, '抽奖记录导出', 'forum:lottery-record:export', 3, 3, @lottery_record_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:lottery-record:export'
);
