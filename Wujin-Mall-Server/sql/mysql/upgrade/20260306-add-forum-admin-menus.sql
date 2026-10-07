-- Forum admin menus and permission fixes

-- Fix legacy member-center permissions after member pages switched to forum APIs
UPDATE `system_menu`
SET `permission` = 'forum:point-record:query',
    `update_time` = NOW()
WHERE `deleted` = b'0'
  AND (`id` = 2288 OR `permission` = 'point:record:query');

UPDATE `system_menu`
SET `permission` = 'forum:sign-record:query',
    `update_time` = NOW()
WHERE `deleted` = b'0'
  AND (`id` = 2294 OR `permission` = 'point:sign-in-record:query');

UPDATE `system_menu`
SET `permission` = 'forum:point-record:change',
    `update_time` = NOW()
WHERE `deleted` = b'0'
  AND (`id` = 2363 OR `permission` = 'member:user:update-point');

SET @forum_root_id := COALESCE(
  (SELECT `id`
   FROM `system_menu`
   WHERE `deleted` = b'0' AND `parent_id` = 0 AND `path` = '/forum'
   LIMIT 1),
  6100
);

SET @forum_user_menu_id := COALESCE(
  (SELECT `id`
   FROM `system_menu`
   WHERE `deleted` = b'0' AND `component` = 'forum/user/index'
   LIMIT 1),
  6101
);

SET @forum_post_menu_id := COALESCE(
  (SELECT `id`
   FROM `system_menu`
   WHERE `deleted` = b'0' AND `component` = 'forum/post/index'
   LIMIT 1),
  6102
);

SET @forum_activity_menu_id := COALESCE(
  (SELECT `id`
   FROM `system_menu`
   WHERE `deleted` = b'0' AND `component` = 'forum/activity/index'
   LIMIT 1),
  6103
);

SET @forum_banner_menu_id := COALESCE(
  (SELECT `id`
   FROM `system_menu`
   WHERE `deleted` = b'0' AND `component` = 'forum/banner/index'
   LIMIT 1),
  6104
);

SET @forum_sign_rule_menu_id := COALESCE(
  (SELECT `id`
   FROM `system_menu`
   WHERE `deleted` = b'0' AND `component` = 'forum/signRule/index'
   LIMIT 1),
  6105
);

SET @forum_config_menu_id := COALESCE(
  (SELECT `id`
   FROM `system_menu`
   WHERE `deleted` = b'0' AND `component` = 'forum/config/index'
   LIMIT 1),
  6106
);

SET @forum_broadcast_menu_id := COALESCE(
  (SELECT `id`
   FROM `system_menu`
   WHERE `deleted` = b'0' AND `component` = 'forum/system-broadcast/index'
   LIMIT 1),
  6107
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  @forum_root_id, '论坛管理', '', 1, 56, 0, '/forum', 'ep:chat-dot-round',
  NULL, NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `id` = @forum_root_id
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  @forum_user_menu_id, '论坛用户', '', 2, 0, @forum_root_id, 'user', 'ep:user',
  'forum/user/index', 'ForumUserProfile', 0, b'0', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `id` = @forum_user_menu_id
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  @forum_post_menu_id, '帖子管理', '', 2, 1, @forum_root_id, 'post', 'ep:document',
  'forum/post/index', 'ForumPostManage', 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `id` = @forum_post_menu_id
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  @forum_activity_menu_id, '活动管理', '', 2, 2, @forum_root_id, 'activity', 'ep:calendar',
  'forum/activity/index', 'ForumActivityManage', 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `id` = @forum_activity_menu_id
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  @forum_banner_menu_id, 'Banner 管理', '', 2, 3, @forum_root_id, 'banner', 'ep:picture',
  'forum/banner/index', 'ForumBannerManage', 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `id` = @forum_banner_menu_id
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  @forum_sign_rule_menu_id, '签到规则', '', 2, 4, @forum_root_id, 'sign-rule', 'ep:checked',
  'forum/signRule/index', 'ForumSignRuleManage', 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `id` = @forum_sign_rule_menu_id
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  @forum_config_menu_id, '论坛配置', '', 2, 5, @forum_root_id, 'config', 'ep:setting',
  'forum/config/index', 'ForumConfigManage', 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `id` = @forum_config_menu_id
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  @forum_broadcast_menu_id, '系统广播', '', 2, 6, @forum_root_id, 'system-broadcast', 'ep:message',
  'forum/system-broadcast/index', 'SystemBroadcast', 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `id` = @forum_broadcast_menu_id
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6111, '论坛用户查询', 'forum:user-profile:query', 3, 1, @forum_user_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:user-profile:query'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6112, '论坛用户更新', 'forum:user-profile:update', 3, 2, @forum_user_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:user-profile:update'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6121, '帖子查询', 'forum:post:query', 3, 1, @forum_post_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:post:query'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6122, '帖子更新', 'forum:post:update', 3, 2, @forum_post_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:post:update'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6123, '帖子审核', 'forum:post:review', 3, 3, @forum_post_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:post:review'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6124, '帖子删除', 'forum:post:delete', 3, 4, @forum_post_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:post:delete'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6131, '活动查询', 'forum:activity:query', 3, 1, @forum_activity_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:activity:query'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6132, '活动创建', 'forum:activity:create', 3, 2, @forum_activity_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:activity:create'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6133, '活动更新', 'forum:activity:update', 3, 3, @forum_activity_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:activity:update'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6134, '活动删除', 'forum:activity:delete', 3, 4, @forum_activity_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:activity:delete'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6135, '活动报名查询', 'forum:activity-sign-up:query', 3, 5, @forum_activity_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:activity-sign-up:query'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6136, '活动报名审核', 'forum:activity-sign-up:approve', 3, 6, @forum_activity_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:activity-sign-up:approve'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6137, '活动报名点评', 'forum:activity-sign-up:feedback', 3, 7, @forum_activity_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:activity-sign-up:feedback'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6141, 'Banner 查询', 'forum:banner:query', 3, 1, @forum_banner_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:banner:query'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6142, 'Banner 创建', 'forum:banner:create', 3, 2, @forum_banner_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:banner:create'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6143, 'Banner 更新', 'forum:banner:update', 3, 3, @forum_banner_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:banner:update'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6144, 'Banner 删除', 'forum:banner:delete', 3, 4, @forum_banner_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:banner:delete'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6151, '签到规则查询', 'forum:sign-rule:query', 3, 1, @forum_sign_rule_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:sign-rule:query'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6152, '签到规则创建', 'forum:sign-rule:create', 3, 2, @forum_sign_rule_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:sign-rule:create'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6153, '签到规则更新', 'forum:sign-rule:update', 3, 3, @forum_sign_rule_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:sign-rule:update'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6154, '签到规则删除', 'forum:sign-rule:delete', 3, 4, @forum_sign_rule_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:sign-rule:delete'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6161, '论坛配置查询', 'forum:config:query', 3, 1, @forum_config_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:config:query'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6162, '论坛配置更新', 'forum:config:update', 3, 2, @forum_config_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:config:update'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6163, '论坛配置删除', 'forum:config:delete', 3, 3, @forum_config_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:config:delete'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6171, '系统广播查询', 'forum:system-broadcast:query', 3, 1, @forum_broadcast_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:system-broadcast:query'
);

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
  `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
  6172, '系统广播发送', 'forum:system-broadcast:send', 3, 2, @forum_broadcast_menu_id, '', '', '',
  NULL, 0, b'1', b'1', b'1',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'forum:system-broadcast:send'
);
