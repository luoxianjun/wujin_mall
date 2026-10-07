-- 五金平台属性字典、商品标准属性/自定义标签审核、寻源线索跟进字段与商家关系模板导入
-- 依赖：20260610-add-wujin-admin-menus.sql、20260613-init-wujin-role-permissions.sql、20260624-split-wujin-platform-tabs-to-menus.sql
-- 脚本可重复执行。

CREATE TABLE IF NOT EXISTS `wujin_attribute_dictionary` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '属性编号',
    `code` varchar(64) NOT NULL COMMENT '属性编码',
    `name` varchar(64) NOT NULL COMMENT '属性名称',
    `group_name` varchar(64) NOT NULL COMMENT '属性分组',
    `lane` varchar(32) DEFAULT NULL COMMENT '适用泳道：PRODUCT/PROCESS/MATERIAL，为空表示三泳道通用',
    `category_id` bigint DEFAULT NULL COMMENT '适用五金分类编号',
    `value_type` varchar(32) NOT NULL COMMENT '值类型：TEXT/NUMBER/ENUM/MULTI_ENUM/BOOLEAN',
    `value_options` varchar(1024) DEFAULT NULL COMMENT '可选值 JSON 数组',
    `unit` varchar(32) DEFAULT NULL COMMENT '单位',
    `required_flag` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否必填',
    `searchable_flag` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否参与搜索筛选',
    `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0=启用 1=停用',
    `remark` varchar(512) DEFAULT NULL COMMENT '备注',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_wujin_attribute_dictionary_code` (`code`),
    KEY `idx_wujin_attribute_dictionary_lane` (`lane`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='五金平台属性字典';

CREATE TABLE IF NOT EXISTS `wujin_product_attribute_value` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '属性值编号',
    `submission_id` bigint NOT NULL COMMENT '商家关系申报编号',
    `merchant_id` bigint NOT NULL COMMENT '商家编号',
    `product_id` bigint NOT NULL COMMENT '商品编号',
    `attribute_id` bigint DEFAULT NULL COMMENT '平台属性字典编号，非字典属性为空',
    `attribute_code` varchar(64) DEFAULT NULL COMMENT '属性编码',
    `attribute_name` varchar(64) NOT NULL COMMENT '属性名称',
    `attribute_value` varchar(255) NOT NULL COMMENT '属性值',
    `standard_flag` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否平台标准属性',
    `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_wujin_product_attribute_value_submission` (`submission_id`),
    KEY `idx_wujin_product_attribute_value_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='五金商品标准属性值';

CREATE TABLE IF NOT EXISTS `wujin_product_custom_tag` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签编号',
    `submission_id` bigint DEFAULT NULL COMMENT '商家关系申报编号',
    `merchant_id` bigint NOT NULL COMMENT '商家编号',
    `product_id` bigint NOT NULL COMMENT '商品编号',
    `product_name` varchar(128) DEFAULT NULL COMMENT '商品名称',
    `tag_name` varchar(32) NOT NULL COMMENT '标签名称',
    `review_note` varchar(512) DEFAULT NULL COMMENT '商家审核说明',
    `audit_status` tinyint NOT NULL COMMENT '审核状态：10=待审核 30=审核通过 40=驳回',
    `audit_comment` varchar(512) DEFAULT NULL COMMENT '平台审核意见',
    `auditor_id` bigint DEFAULT NULL COMMENT '审核人编号',
    `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_wujin_product_custom_tag_product` (`product_id`, `tag_name`),
    KEY `idx_wujin_product_custom_tag_status` (`audit_status`),
    KEY `idx_wujin_product_custom_tag_merchant` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='五金商品自定义标签';

SET @wujin_column_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'wujin_sourcing_lead' AND COLUMN_NAME = 'dispatch_time'
);
SET @wujin_ddl := IF(@wujin_column_exists = 0,
  'ALTER TABLE `wujin_sourcing_lead` ADD COLUMN `dispatch_time` datetime DEFAULT NULL COMMENT ''分发时间'' AFTER `handle_remark`',
  'DO 0');
PREPARE wujin_stmt FROM @wujin_ddl;
EXECUTE wujin_stmt;
DEALLOCATE PREPARE wujin_stmt;

SET @wujin_column_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'wujin_sourcing_lead' AND COLUMN_NAME = 'follow_stage'
);
SET @wujin_ddl := IF(@wujin_column_exists = 0,
  'ALTER TABLE `wujin_sourcing_lead` ADD COLUMN `follow_stage` varchar(32) DEFAULT NULL COMMENT ''商家跟进阶段'' AFTER `process_duration_minutes`',
  'DO 0');
PREPARE wujin_stmt FROM @wujin_ddl;
EXECUTE wujin_stmt;
DEALLOCATE PREPARE wujin_stmt;

SET @wujin_column_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'wujin_sourcing_lead' AND COLUMN_NAME = 'next_follow_time'
);
SET @wujin_ddl := IF(@wujin_column_exists = 0,
  'ALTER TABLE `wujin_sourcing_lead` ADD COLUMN `next_follow_time` datetime DEFAULT NULL COMMENT ''下次跟进时间'' AFTER `follow_stage`',
  'DO 0');
PREPARE wujin_stmt FROM @wujin_ddl;
EXECUTE wujin_stmt;
DEALLOCATE PREPARE wujin_stmt;

SET @wujin_column_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'wujin_sourcing_lead' AND COLUMN_NAME = 'quoted_amount'
);
SET @wujin_ddl := IF(@wujin_column_exists = 0,
  'ALTER TABLE `wujin_sourcing_lead` ADD COLUMN `quoted_amount` int DEFAULT NULL COMMENT ''报价金额，单位：分'' AFTER `next_follow_time`',
  'DO 0');
PREPARE wujin_stmt FROM @wujin_ddl;
EXECUTE wujin_stmt;
DEALLOCATE PREPARE wujin_stmt;

SET @wujin_column_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'wujin_sourcing_lead' AND COLUMN_NAME = 'win_probability'
);
SET @wujin_ddl := IF(@wujin_column_exists = 0,
  'ALTER TABLE `wujin_sourcing_lead` ADD COLUMN `win_probability` int DEFAULT NULL COMMENT ''预计转化概率 0-100'' AFTER `quoted_amount`',
  'DO 0');
PREPARE wujin_stmt FROM @wujin_ddl;
EXECUTE wujin_stmt;
DEALLOCATE PREPARE wujin_stmt;

SET @wujin_platform_root_id := (
  SELECT `id` FROM `system_menu` WHERE `deleted` = b'0' AND `path` = '/wujin' LIMIT 1
);
SET @wujin_platform_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `deleted` = b'0' AND `parent_id` = @wujin_platform_root_id AND `path` = 'platform'
  LIMIT 1
);
SET @wujin_merchant_menu_id := (
  SELECT `id` FROM `system_menu` WHERE `deleted` = b'0' AND `path` = '/merchant/wujin' LIMIT 1
);

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 7401, '平台属性字典', 'wujin:attribute-dictionary:query', 2, 10, @wujin_platform_menu_id, 'attribute-dictionary', 'lucide:list-tree', 'wujin/platform/attribute-dictionary', 'WujinPlatformAttributeDictionary', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @wujin_platform_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `parent_id` = @wujin_platform_menu_id AND `path` = 'attribute-dictionary');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 7411, '标签审核', 'wujin:product-custom-tag:query', 2, 11, @wujin_platform_menu_id, 'custom-tag', 'lucide:tags', 'wujin/platform/custom-tag-audit', 'WujinPlatformCustomTagAudit', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @wujin_platform_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `parent_id` = @wujin_platform_menu_id AND `path` = 'custom-tag');

SET @wujin_attribute_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `deleted` = b'0' AND `parent_id` = @wujin_platform_menu_id AND `path` = 'attribute-dictionary'
  LIMIT 1
);
SET @wujin_custom_tag_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `deleted` = b'0' AND `parent_id` = @wujin_platform_menu_id AND `path` = 'custom-tag'
  LIMIT 1
);

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 7403, '平台属性创建', 'wujin:attribute-dictionary:create', 3, 2, @wujin_attribute_menu_id, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @wujin_attribute_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `permission` = 'wujin:attribute-dictionary:create');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 7404, '平台属性更新', 'wujin:attribute-dictionary:update', 3, 3, @wujin_attribute_menu_id, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @wujin_attribute_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `permission` = 'wujin:attribute-dictionary:update');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 7405, '平台属性删除', 'wujin:attribute-dictionary:delete', 3, 4, @wujin_attribute_menu_id, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @wujin_attribute_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `permission` = 'wujin:attribute-dictionary:delete');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 7413, '自定义标签审核', 'wujin:product-custom-tag:review', 3, 2, @wujin_custom_tag_menu_id, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @wujin_custom_tag_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `permission` = 'wujin:product-custom-tag:review');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 7421, '商家关系模板导入', 'wujin:merchant-import:import', 3, 51, @wujin_merchant_menu_id, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @wujin_merchant_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `deleted` = b'0' AND `permission` = 'wujin:merchant-import:import');

SET @wujin_platform_role_id := (
  SELECT `id` FROM `system_role` WHERE `deleted` = b'0' AND `code` = 'wujin_platform_operator' LIMIT 1
);
SET @wujin_merchant_role_id := (
  SELECT `id` FROM `system_role` WHERE `deleted` = b'0' AND `code` = 'wujin_merchant_operator' LIMIT 1
);

INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT @wujin_platform_role_id, m.`id`, '1', NOW(), '1', NOW(), b'0', 1
FROM `system_menu` m
WHERE @wujin_platform_role_id IS NOT NULL
  AND m.`deleted` = b'0'
  AND (
    m.`id` IN (@wujin_attribute_menu_id, @wujin_custom_tag_menu_id)
    OR m.`parent_id` IN (@wujin_attribute_menu_id, @wujin_custom_tag_menu_id)
  )
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`deleted` = b'0' AND rm.`role_id` = @wujin_platform_role_id AND rm.`menu_id` = m.`id`
  );

INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT @wujin_merchant_role_id, m.`id`, '1', NOW(), '1', NOW(), b'0', 1
FROM `system_menu` m
WHERE @wujin_merchant_role_id IS NOT NULL
  AND m.`deleted` = b'0'
  AND m.`permission` = 'wujin:merchant-import:import'
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`deleted` = b'0' AND rm.`role_id` = @wujin_merchant_role_id AND rm.`menu_id` = m.`id`
  );

-- 默认属性字典：与平台 Web 原占位项保持一致，便于商家发布向导直接使用
INSERT INTO `wujin_attribute_dictionary` (`code`, `name`, `group_name`, `lane`, `value_type`, `value_options`, `unit`, `required_flag`, `searchable_flag`, `sort`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 'FINISHED_PRODUCT_SPEC', '规格型号', '成品属性', 'PRODUCT', 'TEXT', '[]', NULL, b'1', b'1', 1, 0, '成品规格型号，例如 205/55R16', '1', NOW(), '1', NOW(), b'0', 1
WHERE NOT EXISTS (SELECT 1 FROM `wujin_attribute_dictionary` WHERE `deleted` = b'0' AND `code` = 'FINISHED_PRODUCT_SPEC');

INSERT INTO `wujin_attribute_dictionary` (`code`, `name`, `group_name`, `lane`, `value_type`, `value_options`, `unit`, `required_flag`, `searchable_flag`, `sort`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 'MATERIAL_GRADE', '材质牌号', '原材料属性', 'MATERIAL', 'TEXT', '[]', NULL, b'1', b'1', 2, 0, '原材料牌号，例如 STR20、Q235', '1', NOW(), '1', NOW(), b'0', 1
WHERE NOT EXISTS (SELECT 1 FROM `wujin_attribute_dictionary` WHERE `deleted` = b'0' AND `code` = 'MATERIAL_GRADE');

INSERT INTO `wujin_attribute_dictionary` (`code`, `name`, `group_name`, `lane`, `value_type`, `value_options`, `unit`, `required_flag`, `searchable_flag`, `sort`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 'PROCESS_METHOD', '加工方式', '加工属性', 'PROCESS', 'ENUM', '["切割","冲压","表面处理"]', NULL, b'0', b'1', 3, 0, '加工工艺类型', '1', NOW(), '1', NOW(), b'0', 1
WHERE NOT EXISTS (SELECT 1 FROM `wujin_attribute_dictionary` WHERE `deleted` = b'0' AND `code` = 'PROCESS_METHOD');
