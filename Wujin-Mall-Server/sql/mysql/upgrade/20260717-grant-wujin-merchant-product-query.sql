-- Allow the Wujin merchant role to view its tenant-scoped product list.

SET @wujin_merchant_role_id := (
  SELECT `id`
  FROM `system_role`
  WHERE `deleted` = b'0' AND `code` = 'wujin_merchant_operator'
  LIMIT 1
);

SET @product_spu_query_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `permission` = 'product:spu:query'
  LIMIT 1
);

INSERT INTO `system_role_menu`
  (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT @wujin_merchant_role_id, @product_spu_query_menu_id, '1', NOW(), '1', NOW(), b'0', 1
WHERE @wujin_merchant_role_id IS NOT NULL
  AND @product_spu_query_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `system_role_menu`
    WHERE `deleted` = b'0'
      AND `role_id` = @wujin_merchant_role_id
      AND `menu_id` = @product_spu_query_menu_id
  );
