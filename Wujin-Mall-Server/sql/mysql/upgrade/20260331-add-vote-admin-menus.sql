-- =============================================
-- 投票活动管理菜单 (Vote Activity Admin Menus)
-- Created: 2026-03-31
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
SET @vote_activity_menu_id := COALESCE(
  (SELECT `id`
   FROM `system_menu`
   WHERE `deleted` = b'0' AND `component` = 'forum/vote/index'
   LIMIT 1),
  6111
);

-- =============================
-- 二级菜单 (Page Menus)
-- =============================

-- 投票活动管理
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
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `id` = @vote_activity_menu_id
);

-- =============================
-- 三级按钮权限 (Button Permissions)
-- =============================

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6211, '投票活动查询', 'gamification:vote-activity:query', 3, 1, @vote_activity_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'gamification:vote-activity:query'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6212, '投票活动创建', 'gamification:vote-activity:create', 3, 2, @vote_activity_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'gamification:vote-activity:create'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6213, '投票活动更新', 'gamification:vote-activity:update', 3, 3, @vote_activity_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'gamification:vote-activity:update'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6214, '投票活动删除', 'gamification:vote-activity:delete', 3, 4, @vote_activity_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'gamification:vote-activity:delete'
);
