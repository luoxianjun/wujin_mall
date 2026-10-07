-- Split Wujin platform page tabs into sidebar menu routes for permission control.

SET @wujin_platform_root_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `path` = '/wujin'
  LIMIT 1
);

SET @wujin_platform_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `deleted` = b'0'
    AND `parent_id` = @wujin_platform_root_id
    AND `path` = 'platform'
  LIMIT 1
);

SET @wujin_platform_dashboard_menu_id := COALESCE((SELECT `id` FROM `system_menu` WHERE `deleted` = b'0' AND `parent_id` = @wujin_platform_menu_id AND `path` = 'dashboard' LIMIT 1), 7202);
SET @wujin_platform_category_menu_id := COALESCE((SELECT `id` FROM `system_menu` WHERE `deleted` = b'0' AND `parent_id` = @wujin_platform_menu_id AND `path` = 'category' LIMIT 1), 7203);
SET @wujin_platform_mapping_menu_id := COALESCE((SELECT `id` FROM `system_menu` WHERE `deleted` = b'0' AND `parent_id` = @wujin_platform_menu_id AND `path` = 'mapping' LIMIT 1), 7204);
SET @wujin_platform_template_menu_id := COALESCE((SELECT `id` FROM `system_menu` WHERE `deleted` = b'0' AND `parent_id` = @wujin_platform_menu_id AND `path` = 'template' LIMIT 1), 7205);
SET @wujin_platform_audit_menu_id := COALESCE((SELECT `id` FROM `system_menu` WHERE `deleted` = b'0' AND `parent_id` = @wujin_platform_menu_id AND `path` = 'audit' LIMIT 1), 7206);
SET @wujin_platform_search_rule_menu_id := COALESCE((SELECT `id` FROM `system_menu` WHERE `deleted` = b'0' AND `parent_id` = @wujin_platform_menu_id AND `path` = 'search-rule' LIMIT 1), 7207);
SET @wujin_platform_search_log_menu_id := COALESCE((SELECT `id` FROM `system_menu` WHERE `deleted` = b'0' AND `parent_id` = @wujin_platform_menu_id AND `path` = 'search-log' LIMIT 1), 7208);
SET @wujin_platform_sourcing_lead_menu_id := COALESCE((SELECT `id` FROM `system_menu` WHERE `deleted` = b'0' AND `parent_id` = @wujin_platform_menu_id AND `path` = 'sourcing-lead' LIMIT 1), 7209);
SET @wujin_platform_monitor_menu_id := COALESCE((SELECT `id` FROM `system_menu` WHERE `deleted` = b'0' AND `parent_id` = @wujin_platform_menu_id AND `path` = 'monitor' LIMIT 1), 7210);

UPDATE `system_menu`
SET `permission` = '',
    `type` = 1,
    `component` = '',
    `component_name` = NULL,
    `always_show` = b'1',
    `visible` = b'1',
    `status` = 0,
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @wujin_platform_menu_id;

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @wujin_platform_dashboard_menu_id, '运营看板', 'wujin:monitor-dashboard:query', 2, 1, @wujin_platform_menu_id, 'dashboard', 'lucide:layout-dashboard', 'wujin/platform/index', 'WujinPlatformDashboard', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `parent_id` = @wujin_platform_menu_id AND `path` = 'dashboard');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @wujin_platform_category_menu_id, '平台类目', 'wujin:category:query', 2, 2, @wujin_platform_menu_id, 'category', 'lucide:folder-tree', 'wujin/platform/index', 'WujinPlatformCategory', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `parent_id` = @wujin_platform_menu_id AND `path` = 'category');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @wujin_platform_mapping_menu_id, '跨泳道映射', 'wujin:category-mapping:query', 2, 3, @wujin_platform_menu_id, 'mapping', 'lucide:git-branch', 'wujin/platform/index', 'WujinPlatformMapping', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `parent_id` = @wujin_platform_menu_id AND `path` = 'mapping');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @wujin_platform_template_menu_id, '行业模板', 'wujin:industry-template:query', 2, 4, @wujin_platform_menu_id, 'template', 'lucide:blocks', 'wujin/platform/index', 'WujinPlatformTemplate', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `parent_id` = @wujin_platform_menu_id AND `path` = 'template');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @wujin_platform_audit_menu_id, '关系审核', 'wujin:relation-audit-record:query', 2, 5, @wujin_platform_menu_id, 'audit', 'lucide:clipboard-check', 'wujin/platform/index', 'WujinPlatformAudit', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `parent_id` = @wujin_platform_menu_id AND `path` = 'audit');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @wujin_platform_search_rule_menu_id, '搜索规则', 'wujin:search-rule-config:query', 2, 6, @wujin_platform_menu_id, 'search-rule', 'lucide:sliders-horizontal', 'wujin/platform/index', 'WujinPlatformSearchRule', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `parent_id` = @wujin_platform_menu_id AND `path` = 'search-rule');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @wujin_platform_search_log_menu_id, '搜索监控', 'wujin:search-behavior-log:query', 2, 7, @wujin_platform_menu_id, 'search-log', 'lucide:search-check', 'wujin/platform/index', 'WujinPlatformSearchLog', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `parent_id` = @wujin_platform_menu_id AND `path` = 'search-log');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @wujin_platform_sourcing_lead_menu_id, '寻源线索', 'wujin:sourcing-lead:query', 2, 8, @wujin_platform_menu_id, 'sourcing-lead', 'lucide:radar', 'wujin/platform/index', 'WujinPlatformSourcingLead', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `parent_id` = @wujin_platform_menu_id AND `path` = 'sourcing-lead');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @wujin_platform_monitor_menu_id, '监控快照', 'wujin:monitor-snapshot:query', 2, 9, @wujin_platform_menu_id, 'monitor', 'lucide:activity', 'wujin/platform/index', 'WujinPlatformMonitor', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `parent_id` = @wujin_platform_menu_id AND `path` = 'monitor');

UPDATE `system_menu`
SET `parent_id` = CASE
      WHEN `permission` IN ('wujin:monitor-dashboard:query') THEN @wujin_platform_dashboard_menu_id
      WHEN `permission` IN ('wujin:category:query', 'wujin:category:create', 'wujin:category:update', 'wujin:category:delete') THEN @wujin_platform_category_menu_id
      WHEN `permission` IN ('wujin:category-mapping:query', 'wujin:category-mapping:create', 'wujin:category-mapping:update', 'wujin:category-mapping:delete') THEN @wujin_platform_mapping_menu_id
      WHEN `permission` IN (
        'wujin:industry-template:query',
        'wujin:industry-template:create',
        'wujin:industry-template:update',
        'wujin:industry-template:delete',
        'wujin:industry-template-item:query',
        'wujin:industry-template-item:create',
        'wujin:industry-template-item:update',
        'wujin:industry-template-item:delete'
      ) THEN @wujin_platform_template_menu_id
      WHEN `permission` IN (
        'wujin:relation-audit-record:query',
        'wujin:relation-audit-record:create',
        'wujin:relation-audit-record:update',
        'wujin:relation-audit-record:delete',
        'wujin:relation-audit-review:update'
      ) THEN @wujin_platform_audit_menu_id
      WHEN `permission` IN ('wujin:search-rule-config:query', 'wujin:search-rule-config:create', 'wujin:search-rule-config:update', 'wujin:search-rule-config:delete') THEN @wujin_platform_search_rule_menu_id
      WHEN `permission` IN ('wujin:search-behavior-log:query', 'wujin:search-behavior-log:create', 'wujin:search-behavior-log:update', 'wujin:search-behavior-log:delete') THEN @wujin_platform_search_log_menu_id
      WHEN `permission` IN ('wujin:sourcing-lead:query', 'wujin:sourcing-lead:dispatch') THEN @wujin_platform_sourcing_lead_menu_id
      WHEN `permission` IN ('wujin:monitor-snapshot:query', 'wujin:monitor-snapshot:create', 'wujin:monitor-snapshot:update', 'wujin:monitor-snapshot:delete') THEN @wujin_platform_monitor_menu_id
      ELSE `parent_id`
    END,
    `updater` = '1',
    `update_time` = NOW()
WHERE `deleted` = b'0'
  AND `type` = 3
  AND `permission` IN (
    'wujin:monitor-dashboard:query',
    'wujin:category:query', 'wujin:category:create', 'wujin:category:update', 'wujin:category:delete',
    'wujin:category-mapping:query', 'wujin:category-mapping:create', 'wujin:category-mapping:update', 'wujin:category-mapping:delete',
    'wujin:industry-template:query', 'wujin:industry-template:create', 'wujin:industry-template:update', 'wujin:industry-template:delete',
    'wujin:industry-template-item:query', 'wujin:industry-template-item:create', 'wujin:industry-template-item:update', 'wujin:industry-template-item:delete',
    'wujin:relation-audit-record:query', 'wujin:relation-audit-record:create', 'wujin:relation-audit-record:update', 'wujin:relation-audit-record:delete', 'wujin:relation-audit-review:update',
    'wujin:search-rule-config:query', 'wujin:search-rule-config:create', 'wujin:search-rule-config:update', 'wujin:search-rule-config:delete',
    'wujin:search-behavior-log:query', 'wujin:search-behavior-log:create', 'wujin:search-behavior-log:update', 'wujin:search-behavior-log:delete',
    'wujin:sourcing-lead:query', 'wujin:sourcing-lead:dispatch',
    'wujin:monitor-snapshot:query', 'wujin:monitor-snapshot:create', 'wujin:monitor-snapshot:update', 'wujin:monitor-snapshot:delete'
  );

SET @wujin_platform_role_id := (
  SELECT `id`
  FROM `system_role`
  WHERE `deleted` = b'0' AND `code` = 'wujin_platform_operator'
  LIMIT 1
);

INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT @wujin_platform_role_id, m.`id`, '1', NOW(), '1', NOW(), b'0', 1
FROM `system_menu` m
WHERE @wujin_platform_role_id IS NOT NULL
  AND m.`deleted` = b'0'
  AND m.`id` IN (
    @wujin_platform_root_id,
    @wujin_platform_menu_id,
    @wujin_platform_dashboard_menu_id,
    @wujin_platform_category_menu_id,
    @wujin_platform_mapping_menu_id,
    @wujin_platform_template_menu_id,
    @wujin_platform_audit_menu_id,
    @wujin_platform_search_rule_menu_id,
    @wujin_platform_search_log_menu_id,
    @wujin_platform_sourcing_lead_menu_id,
    @wujin_platform_monitor_menu_id
  )
  AND NOT EXISTS (
    SELECT 1
    FROM `system_role_menu` rm
    WHERE rm.`deleted` = b'0'
      AND rm.`role_id` = @wujin_platform_role_id
      AND rm.`menu_id` = m.`id`
  );

INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT granted_role.`role_id`, section_menu.`menu_id`, '1', NOW(), '1', NOW(), b'0', granted_role.`tenant_id`
FROM (
  SELECT rm.`role_id`, rm.`tenant_id`
  FROM `system_role_menu` rm
  WHERE rm.`deleted` = b'0'
    AND rm.`menu_id` = @wujin_platform_menu_id
) granted_role
JOIN (
  SELECT @wujin_platform_dashboard_menu_id AS `menu_id`
  UNION ALL SELECT @wujin_platform_category_menu_id
  UNION ALL SELECT @wujin_platform_mapping_menu_id
  UNION ALL SELECT @wujin_platform_template_menu_id
  UNION ALL SELECT @wujin_platform_audit_menu_id
  UNION ALL SELECT @wujin_platform_search_rule_menu_id
  UNION ALL SELECT @wujin_platform_search_log_menu_id
  UNION ALL SELECT @wujin_platform_sourcing_lead_menu_id
  UNION ALL SELECT @wujin_platform_monitor_menu_id
) section_menu
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_role_menu` existing_rm
  WHERE existing_rm.`deleted` = b'0'
    AND existing_rm.`role_id` = granted_role.`role_id`
    AND existing_rm.`menu_id` = section_menu.`menu_id`
);
