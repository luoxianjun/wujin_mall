-- 五金商城供应能力索引表

CREATE TABLE IF NOT EXISTS `wujin_merchant_supply_capability` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '供应能力编号',
    `merchant_id` bigint NOT NULL COMMENT '商家编号',
    `product_id` bigint NOT NULL COMMENT '商品编号',
    `product_name` varchar(128) NOT NULL COMMENT '商品名称',
    `entity_id` bigint NOT NULL COMMENT '关联产业链实体编号',
    `lane` varchar(32) NOT NULL COMMENT '供应泳道：PRODUCT/PROCESS/MATERIAL',
    `industry` varchar(64) DEFAULT NULL COMMENT '行业上下文',
    `supply_status` tinyint NOT NULL COMMENT '供应状态：0=启用 1=停供',
    `stock_count` int DEFAULT NULL COMMENT '库存数量',
    `min_order_quantity` int DEFAULT NULL COMMENT '最小起订量',
    `delivery_days` int DEFAULT NULL COMMENT '交付周期天数',
    `service_area` varchar(128) DEFAULT NULL COMMENT '服务区域',
    `remark` varchar(512) DEFAULT NULL COMMENT '备注',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_wujin_supply_capability_merchant` (`merchant_id`),
    KEY `idx_wujin_supply_capability_product` (`product_id`),
    KEY `idx_wujin_supply_capability_entity` (`entity_id`),
    KEY `idx_wujin_supply_capability_lane` (`lane`, `industry`),
    KEY `idx_wujin_supply_capability_status` (`supply_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='五金商家供应能力索引';
