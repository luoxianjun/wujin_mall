-- Wujin default role permission initialization.
-- Run after 20260610-add-wujin-admin-menus.sql.

SET @wujin_platform_root_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `path` = '/wujin'
  LIMIT 1
);

SET @wujin_platform_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `component` = 'wujin/platform/index'
  LIMIT 1
);

SET @wujin_merchant_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `path` = '/merchant/wujin'
  LIMIT 1
);

SET @wujin_platform_role_id := COALESCE(
  (SELECT `id`
   FROM `system_role`
   WHERE `deleted` = b'0' AND `code` = 'wujin_platform_operator'
   LIMIT 1),
  7200
);

SET @wujin_merchant_role_id := COALESCE(
  (SELECT `id`
   FROM `system_role`
   WHERE `deleted` = b'0' AND `code` = 'wujin_merchant_operator'
   LIMIT 1),
  7300
);

INSERT INTO `system_role` (`id`, `name`, `code`, `sort`, `data_scope`, `data_scope_dept_ids`, `status`, `type`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT @wujin_platform_role_id, '五金平台运营员', 'wujin_platform_operator', 57, 1, '', 0, 2, '五金平台 Web 后台默认运营角色', '1', NOW(), '1', NOW(), b'0', 1
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_role`
  WHERE `deleted` = b'0' AND `code` = 'wujin_platform_operator'
);

INSERT INTO `system_role` (`id`, `name`, `code`, `sort`, `data_scope`, `data_scope_dept_ids`, `status`, `type`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT @wujin_merchant_role_id, '五金商家运营员', 'wujin_merchant_operator', 58, 2, '', 0, 2, '五金商家 Web 后台默认运营角色', '1', NOW(), '1', NOW(), b'0', 1
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_role`
  WHERE `deleted` = b'0' AND `code` = 'wujin_merchant_operator'
);

INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT @wujin_platform_role_id, m.`id`, '1', NOW(), '1', NOW(), b'0', 1
FROM `system_menu` m
WHERE m.`deleted` = b'0'
  AND (
    m.`id` IN (@wujin_platform_root_id, @wujin_platform_menu_id)
    OR (
      m.`parent_id` = @wujin_platform_menu_id
      AND m.`permission` LIKE 'wujin:%'
      AND m.`permission` NOT LIKE 'wujin:merchant-%'
    )
  )
  AND NOT EXISTS (
    SELECT 1
    FROM `system_role_menu` rm
    WHERE rm.`deleted` = b'0'
      AND rm.`role_id` = @wujin_platform_role_id AND rm.`menu_id` = m.`id`
  );

INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT @wujin_merchant_role_id, m.`id`, '1', NOW(), '1', NOW(), b'0', 1
FROM `system_menu` m
WHERE m.`deleted` = b'0'
  AND (
    m.`id` = @wujin_merchant_menu_id
    OR (
      m.`parent_id` = @wujin_merchant_menu_id
      AND m.`permission` LIKE 'wujin:merchant-%'
    )
  )
  AND NOT EXISTS (
    SELECT 1
    FROM `system_role_menu` rm
    WHERE rm.`deleted` = b'0'
      AND rm.`role_id` = @wujin_merchant_role_id AND rm.`menu_id` = m.`id`
  );
