-- 平台类目改为独立的多列层级页面（wujin/category/index），并在商家后台提供同一页面
-- 平台运营后台：“平台类目”菜单指向新页面；商家后台：新增“平台类目”菜单，商家运营角色获得类目查询/新增/修改/删除权限
-- 依赖：20260610-add-wujin-admin-menus.sql、20260613-init-wujin-role-permissions.sql、20260624-split-wujin-platform-tabs-to-menus.sql
-- 脚本可重复执行。

SET @wujin_platform_root_id := (
  SELECT `id` FROM `system_menu` WHERE `deleted` = b'0' AND `path` = '/wujin' LIMIT 1
);
SET @wujin_platform_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `deleted` = b'0' AND `parent_id` = @wujin_platform_root_id AND `path` = 'platform'
  LIMIT 1
);

-- 平台运营后台：平台类目使用多列层级页面
UPDATE `system_menu`
SET `component` = 'wujin/category/index',
    `component_name` = 'WujinCategoryColumns',
    `updater` = '1',
    `update_time` = NOW()
WHERE `deleted` = b'0'
  AND `parent_id` = @wujin_platform_menu_id
  AND `path` = 'category'
  AND `type` = 2;

-- 商家后台：新增平台类目菜单，与平台共用同一套五金类目
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 7501, '平台类目', 'wujin:category:query', 2, 59, 0, '/merchant/wujin-category', 'lucide:folder-tree', 'wujin/category/index', 'WujinCategoryColumns', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `path` = '/merchant/wujin-category');

SET @wujin_merchant_category_menu_id := (
  SELECT `id` FROM `system_menu` WHERE `deleted` = b'0' AND `path` = '/merchant/wujin-category' LIMIT 1
);
-- 商家菜单自己的按钮权限：按钮必须挂在用户拥有的菜单下，权限才会下发到前端（父菜单未授权的按钮会被过滤）
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 7502, '商家类目创建', 'wujin:category:create', 3, 2, @wujin_merchant_category_menu_id, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @wujin_merchant_category_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `parent_id` = @wujin_merchant_category_menu_id AND `permission` = 'wujin:category:create');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 7503, '商家类目更新', 'wujin:category:update', 3, 3, @wujin_merchant_category_menu_id, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @wujin_merchant_category_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `parent_id` = @wujin_merchant_category_menu_id AND `permission` = 'wujin:category:update');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 7504, '商家类目删除', 'wujin:category:delete', 3, 4, @wujin_merchant_category_menu_id, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @wujin_merchant_category_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `parent_id` = @wujin_merchant_category_menu_id AND `permission` = 'wujin:category:delete');

SET @wujin_merchant_role_id := (
  SELECT `id` FROM `system_role` WHERE `deleted` = b'0' AND `code` = 'wujin_merchant_operator' LIMIT 1
);

-- 商家运营角色：商家平台类目菜单及其按钮
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT @wujin_merchant_role_id, m.`id`, '1', NOW(), '1', NOW(), b'0', 1
FROM `system_menu` m
WHERE @wujin_merchant_role_id IS NOT NULL
  AND @wujin_merchant_category_menu_id IS NOT NULL
  AND m.`deleted` = b'0'
  AND (m.`id` = @wujin_merchant_category_menu_id OR m.`parent_id` = @wujin_merchant_category_menu_id)
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`deleted` = b'0' AND rm.`role_id` = @wujin_merchant_role_id AND rm.`menu_id` = m.`id`
  );
