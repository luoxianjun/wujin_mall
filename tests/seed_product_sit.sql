-- 商品模块 SIT 测试数据种子
-- 数据库: wujin_mall_sit
-- 说明: 补充 product_category / product_brand / product_spu / product_sku，
--       用于商品分类、品牌、SPU 列表与展开信息的基础展示。
-- ID 策略: 显式 ID，便于重复执行前先清理。
-- 租户: tenant_id = 1

SET NAMES utf8mb4;
SET @T := 1;
SET @U := '1';
SET @NOW := NOW();

CREATE TABLE IF NOT EXISTS `product_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类编号',
  `parent_id` bigint NOT NULL COMMENT '父分类编号',
  `name` varchar(255) NOT NULL COMMENT '分类名称',
  `pic_url` varchar(255) NOT NULL COMMENT '移动端分类图',
  `big_pic_url` varchar(255) DEFAULT NULL COMMENT 'PC 端分类图',
  `sort` int DEFAULT 0 COMMENT '分类排序',
  `status` tinyint NOT NULL COMMENT '开启状态',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类';

CREATE TABLE IF NOT EXISTS `product_brand` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '品牌编号',
  `name` varchar(255) NOT NULL COMMENT '品牌名称',
  `pic_url` varchar(255) NOT NULL COMMENT '品牌图片',
  `sort` int DEFAULT 0 COMMENT '品牌排序',
  `description` varchar(1024) DEFAULT NULL COMMENT '品牌描述',
  `status` tinyint NOT NULL COMMENT '状态',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品品牌';

CREATE TABLE IF NOT EXISTS `product_spu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品 SPU 编号',
  `name` varchar(128) NOT NULL COMMENT '商品名称',
  `keyword` varchar(256) NOT NULL COMMENT '关键字',
  `introduction` varchar(256) NOT NULL COMMENT '商品简介',
  `description` text NOT NULL COMMENT '商品详情',
  `bar_code` varchar(64) DEFAULT NULL COMMENT '条形码',
  `category_id` bigint NOT NULL COMMENT '商品分类编号',
  `brand_id` bigint DEFAULT NULL COMMENT '商品品牌编号',
  `pic_url` varchar(256) NOT NULL COMMENT '商品封面图',
  `slider_pic_urls` varchar(2000) DEFAULT '[]' COMMENT '商品轮播图',
  `video_url` varchar(256) DEFAULT NULL COMMENT '商品视频',
  `unit` tinyint DEFAULT 1 COMMENT '单位',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序字段',
  `status` tinyint NOT NULL COMMENT '商品状态: 1 上架, 0 下架, -1 回收',
  `spec_type` bit(1) NOT NULL COMMENT '规格类型：0 单规格 1 多规格',
  `price` int NOT NULL DEFAULT -1 COMMENT '商品价格，单位：分',
  `market_price` int NOT NULL COMMENT '市场价，单位：分',
  `cost_price` int NOT NULL DEFAULT -1 COMMENT '成本价，单位：分',
  `stock` int NOT NULL DEFAULT 0 COMMENT '库存',
  `delivery_types` varchar(32) DEFAULT '1' COMMENT '配送方式数组',
  `delivery_template_id` bigint DEFAULT NULL COMMENT '物流配置模板编号',
  `recommend_hot` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否热卖推荐',
  `recommend_benefit` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否优惠推荐',
  `recommend_best` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否精品推荐',
  `recommend_new` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否新品推荐',
  `recommend_good` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否优品推荐',
  `give_integral` int NOT NULL DEFAULT 0 COMMENT '赠送积分',
  `give_coupon_template_ids` varchar(512) DEFAULT '[]' COMMENT '赠送优惠券编号数组',
  `sub_commission_type` bit(1) NOT NULL DEFAULT b'0' COMMENT '分销类型',
  `activity_orders` varchar(16) NOT NULL DEFAULT '' COMMENT '活动显示排序',
  `sales_count` int DEFAULT 0 COMMENT '商品销量',
  `virtual_sales_count` int DEFAULT 0 COMMENT '虚拟销量',
  `browse_count` int DEFAULT 0 COMMENT '商品点击量',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品 SPU';

CREATE TABLE IF NOT EXISTS `product_sku` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品 SKU 编号',
  `spu_id` bigint NOT NULL COMMENT 'SPU 编号',
  `properties` varchar(512) DEFAULT '[]' COMMENT '属性数组，JSON 格式',
  `price` int NOT NULL DEFAULT -1 COMMENT '商品价格，单位：分',
  `market_price` int DEFAULT NULL COMMENT '市场价，单位：分',
  `cost_price` int NOT NULL DEFAULT -1 COMMENT '成本价，单位：分',
  `bar_code` varchar(64) DEFAULT NULL COMMENT 'SKU 条形码',
  `pic_url` varchar(256) NOT NULL COMMENT '图片地址',
  `stock` int DEFAULT 0 COMMENT '库存',
  `weight` double DEFAULT NULL COMMENT '商品重量，单位：kg',
  `volume` double DEFAULT NULL COMMENT '商品体积，单位：m^3',
  `first_brokerage_price` int DEFAULT NULL COMMENT '一级分销佣金，单位：分',
  `second_brokerage_price` int DEFAULT NULL COMMENT '二级分销佣金，单位：分',
  `sales_count` int DEFAULT 0 COMMENT '商品销量',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品 SKU';

START TRANSACTION;

DELETE FROM `product_sku` WHERE `tenant_id` = @T AND `id` IN (4000, 4001, 4002);
DELETE FROM `product_spu` WHERE `tenant_id` = @T AND `id` IN (3000, 3001, 3002);
DELETE FROM `product_brand` WHERE `tenant_id` = @T AND `id` IN (2000, 2001, 2002);
DELETE FROM `product_category` WHERE `tenant_id` = @T AND `id` IN (1000, 1001, 1002, 1003);

INSERT INTO `product_category`
  (`id`, `parent_id`, `name`, `pic_url`, `big_pic_url`, `sort`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES
 (1000, 0,    '五金成品', 'https://cdn.wujin-mall.test/product/category/finished.png', NULL, 1, 0, @U, @NOW, @U, @NOW, b'0', @T),
 (1001, 1000, '轮胎',     'https://cdn.wujin-mall.test/product/category/tire.png',     NULL, 2, 0, @U, @NOW, @U, @NOW, b'0', @T),
 (1002, 1000, '紧固件',   'https://cdn.wujin-mall.test/product/category/fastener.png', NULL, 3, 0, @U, @NOW, @U, @NOW, b'0', @T),
 (1003, 1000, '轴承',     'https://cdn.wujin-mall.test/product/category/bearing.png',  NULL, 4, 0, @U, @NOW, @U, @NOW, b'0', @T);

INSERT INTO `product_brand`
  (`id`, `name`, `pic_url`, `sort`, `description`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES
 (2000, '米其林',   'https://cdn.wujin-mall.test/product/brand/michelin.png', 1, '轮胎品牌',   0, @U, @NOW, @U, @NOW, b'0', @T),
 (2001, '国标五金', 'https://cdn.wujin-mall.test/product/brand/guobiao.png',  2, '紧固件品牌', 0, @U, @NOW, @U, @NOW, b'0', @T),
 (2002, '中轴精工', 'https://cdn.wujin-mall.test/product/brand/bearing.png',  3, '轴承品牌',   0, @U, @NOW, @U, @NOW, b'0', @T);

INSERT INTO `product_spu`
  (`id`, `name`, `keyword`, `introduction`, `description`, `bar_code`, `category_id`, `brand_id`, `pic_url`, `slider_pic_urls`, `video_url`, `unit`, `sort`, `status`, `spec_type`, `price`, `market_price`, `cost_price`, `stock`, `delivery_types`, `delivery_template_id`, `recommend_hot`, `recommend_benefit`, `recommend_best`, `recommend_new`, `recommend_good`, `give_integral`, `give_coupon_template_ids`, `sub_commission_type`, `activity_orders`, `sales_count`, `virtual_sales_count`, `browse_count`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES
 (3000, '米其林 205/55R16 乘用车轮胎', '轮胎,205/55R16,乘用车', '适配家用轿车的常规轮胎', '适配家用轿车的常规轮胎', 'BAR-3000', 1001, 2000, 'https://cdn.wujin-mall.test/product/spu/tire-3000-cover.png',    '["https://cdn.wujin-mall.test/product/spu/tire-3000-1.png","https://cdn.wujin-mall.test/product/spu/tire-3000-2.png"]', NULL, 1, 3, 1, b'0', 39900, 45900, 32000, 860,   '1', 1, b'1', b'0', b'1', b'1', b'1', 100, '[]', b'0', '', 128, 32, 560, @U, @NOW, @U, @NOW, b'0', @T),
 (3001, '8.8级 M12 外六角螺栓',        '螺栓,M12,外六角',     '工业通用高强度螺栓',   '工业通用高强度螺栓',   'BAR-3001', 1002, 2001, 'https://cdn.wujin-mall.test/product/spu/bolt-3001-cover.png',    '["https://cdn.wujin-mall.test/product/spu/bolt-3001-1.png"]', NULL, 1, 2, 1, b'0', 1200,  1500,  800,   50000, '1', 1, b'0', b'1', b'0', b'0', b'1', 20,  '[]', b'0', '', 76,  15, 244, @U, @NOW, @U, @NOW, b'0', @T),
 (3002, '6204 深沟球轴承',             '轴承,6204,深沟球',    '常规深沟球轴承',       '常规深沟球轴承',       'BAR-3002', 1003, 2002, 'https://cdn.wujin-mall.test/product/spu/bearing-3002-cover.png', '["https://cdn.wujin-mall.test/product/spu/bearing-3002-1.png"]', NULL, 1, 1, 0, b'0', 8800,  9600,  6500,  0,     '1', 1, b'0', b'0', b'0', b'0', b'0', 0,   '[]', b'0', '', 21,  4,  88,  @U, @NOW, @U, @NOW, b'0', @T);

INSERT INTO `product_sku`
  (`id`, `spu_id`, `properties`, `price`, `market_price`, `cost_price`, `bar_code`, `pic_url`, `stock`, `weight`, `volume`, `first_brokerage_price`, `second_brokerage_price`, `sales_count`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES
 (4000, 3000, '[]', 39900, 45900, 32000, 'SKU-4000', 'https://cdn.wujin-mall.test/product/spu/tire-3000-sku.png',    860,   12.5, 0.08,  1990, 890, 128, @U, @NOW, @U, @NOW, b'0', @T),
 (4001, 3001, '[]', 1200,  1500,  800,   'SKU-4001', 'https://cdn.wujin-mall.test/product/spu/bolt-3001-sku.png',    50000, 0.05, 0.001, 60,   30,  76,  @U, @NOW, @U, @NOW, b'0', @T),
 (4002, 3002, '[]', 8800,  9600,  6500,  'SKU-4002', 'https://cdn.wujin-mall.test/product/spu/bearing-3002-sku.png', 0,     0.22, 0.002, 420,  180, 21,  @U, @NOW, @U, @NOW, b'0', @T);

COMMIT;

SELECT 'product_category' AS `t`, COUNT(*) AS `c` FROM `product_category` WHERE `tenant_id` = @T
UNION ALL SELECT 'product_brand', COUNT(*) FROM `product_brand` WHERE `tenant_id` = @T
UNION ALL SELECT 'product_spu', COUNT(*) FROM `product_spu` WHERE `tenant_id` = @T
UNION ALL SELECT 'product_sku', COUNT(*) FROM `product_sku` WHERE `tenant_id` = @T;
