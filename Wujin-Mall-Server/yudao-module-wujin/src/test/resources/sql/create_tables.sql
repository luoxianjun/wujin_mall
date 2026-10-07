CREATE TABLE IF NOT EXISTS `wujin_category` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类编号',
    `parent_id` bigint NOT NULL COMMENT '父分类编号，根节点为 0',
    `lane` varchar(32) NOT NULL COMMENT '泳道：PRODUCT/PROCESS/MATERIAL',
    `code` varchar(64) NOT NULL COMMENT '分类编码',
    `name` varchar(128) NOT NULL COMMENT '分类名称',
    `level` int NOT NULL COMMENT '分类层级',
    `sort` int DEFAULT 0 COMMENT '排序',
    `status` tinyint NOT NULL COMMENT '开启状态',
    `display_depth` int DEFAULT NULL COMMENT '默认展示深度',
    `health_status` varchar(32) DEFAULT NULL COMMENT '健康状态',
    `description` varchar(512) DEFAULT NULL COMMENT '分类说明',
    "creator" varchar(64) DEFAULT '',
    "create_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updater" varchar(64) DEFAULT '',
    "update_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" bit NOT NULL DEFAULT FALSE,
    "tenant_id" bigint NOT NULL DEFAULT '0',
    PRIMARY KEY ("id")
) COMMENT '五金三泳道分类';

CREATE TABLE IF NOT EXISTS `wujin_category_mapping` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '映射编号',
    `source_category_id` bigint NOT NULL COMMENT '来源分类编号',
    `source_lane` varchar(32) NOT NULL COMMENT '来源泳道',
    `target_category_id` bigint NOT NULL COMMENT '目标分类编号',
    `target_lane` varchar(32) NOT NULL COMMENT '目标泳道',
    `mapping_type` varchar(64) NOT NULL COMMENT '映射类型',
    `confidence` int DEFAULT NULL COMMENT '置信度',
    `status` tinyint NOT NULL COMMENT '开启状态',
    `risk_note` varchar(512) DEFAULT NULL COMMENT '风险说明',
    "creator" varchar(64) DEFAULT '',
    "create_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updater" varchar(64) DEFAULT '',
    "update_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" bit NOT NULL DEFAULT FALSE,
    "tenant_id" bigint NOT NULL DEFAULT '0',
    PRIMARY KEY ("id")
) COMMENT '五金跨泳道分类映射';

CREATE TABLE IF NOT EXISTS `wujin_chain_entity` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '实体编号',
    `entity_code` varchar(64) NOT NULL COMMENT '实体编码',
    `name` varchar(128) NOT NULL COMMENT '实体名称',
    `lane` varchar(32) NOT NULL COMMENT '语义泳道',
    `industries` varchar(512) DEFAULT NULL COMMENT '适用行业，逗号分隔',
    `junction_flag` bit NOT NULL DEFAULT FALSE COMMENT '是否交汇点实体',
    `risk_note` varchar(512) DEFAULT NULL COMMENT '风险说明',
    `status` tinyint NOT NULL COMMENT '开启状态',
    "creator" varchar(64) DEFAULT '',
    "create_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updater" varchar(64) DEFAULT '',
    "update_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" bit NOT NULL DEFAULT FALSE,
    "tenant_id" bigint NOT NULL DEFAULT '0',
    PRIMARY KEY ("id")
) COMMENT '五金产业链实体';

CREATE TABLE IF NOT EXISTS `wujin_chain_entity_relation` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关系编号',
    `source_entity_id` bigint NOT NULL COMMENT '下游实体编号',
    `target_entity_id` bigint NOT NULL COMMENT '上游实体编号',
    `relation_type` varchar(64) NOT NULL COMMENT '关系类型',
    `weight` int DEFAULT NULL COMMENT '权重，百分制',
    `cost_ratio` int DEFAULT NULL COMMENT '成本占比，百分制',
    `industry_context` varchar(64) DEFAULT NULL COMMENT '适用行业上下文',
    `audit_status` tinyint NOT NULL COMMENT '审核状态',
    `audit_remark` varchar(512) DEFAULT NULL COMMENT '审核备注',
    "creator" varchar(64) DEFAULT '',
    "create_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updater" varchar(64) DEFAULT '',
    "update_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" bit NOT NULL DEFAULT FALSE,
    "tenant_id" bigint NOT NULL DEFAULT '0',
    PRIMARY KEY ("id")
) COMMENT '五金产业链实体上下游关系';

CREATE TABLE IF NOT EXISTS `wujin_industry_template` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板编号',
    `template_code` varchar(64) NOT NULL COMMENT '模板编码',
    `name` varchar(128) NOT NULL COMMENT '模板名称',
    `industry_code` varchar(64) NOT NULL COMMENT '行业编码',
    `product_lane` varchar(32) NOT NULL COMMENT '成品泳道',
    `status` tinyint NOT NULL COMMENT '开启状态',
    `remark` varchar(512) DEFAULT NULL COMMENT '备注',
    "creator" varchar(64) DEFAULT '',
    "create_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updater" varchar(64) DEFAULT '',
    "update_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" bit NOT NULL DEFAULT FALSE,
    "tenant_id" bigint NOT NULL DEFAULT '0',
    PRIMARY KEY ("id")
) COMMENT '五金行业模板';

CREATE TABLE IF NOT EXISTS `wujin_industry_template_item` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板项编号',
    `template_id` bigint NOT NULL COMMENT '模板编号',
    `entity_id` bigint NOT NULL COMMENT '关联实体编号',
    `relation_type` varchar(64) NOT NULL COMMENT '关系类型',
    `required_flag` bit NOT NULL DEFAULT FALSE COMMENT '是否必填',
    `sort` int DEFAULT 0 COMMENT '排序',
    `weight` int DEFAULT NULL COMMENT '权重，百分制',
    `remark` varchar(512) DEFAULT NULL COMMENT '备注',
    "creator" varchar(64) DEFAULT '',
    "create_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updater" varchar(64) DEFAULT '',
    "update_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" bit NOT NULL DEFAULT FALSE,
    "tenant_id" bigint NOT NULL DEFAULT '0',
    PRIMARY KEY ("id")
) COMMENT '五金行业模板项';

CREATE TABLE IF NOT EXISTS `wujin_merchant_relation_submission` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申报编号',
    `merchant_id` bigint NOT NULL COMMENT '商家编号',
    `product_id` bigint NOT NULL COMMENT '商品编号',
    `product_name` varchar(128) NOT NULL COMMENT '商品名称',
    `product_lane` varchar(32) NOT NULL COMMENT '商品泳道',
    `product_category_id` bigint DEFAULT NULL COMMENT '商品分类编号',
    `template_id` bigint NOT NULL COMMENT '行业模板编号',
    `audit_status` tinyint NOT NULL COMMENT '审核状态',
    `audit_route` varchar(64) DEFAULT NULL COMMENT '审核路线',
    `completeness_score` int DEFAULT NULL COMMENT '完善度分数',
    `remark` varchar(512) DEFAULT NULL COMMENT '备注',
    "creator" varchar(64) DEFAULT '',
    "create_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updater" varchar(64) DEFAULT '',
    "update_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" bit NOT NULL DEFAULT FALSE,
    "tenant_id" bigint NOT NULL DEFAULT '0',
    PRIMARY KEY ("id")
) COMMENT '五金商家关系申报';

CREATE TABLE IF NOT EXISTS `wujin_merchant_relation_item` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申报项编号',
    `submission_id` bigint NOT NULL COMMENT '申报编号',
    `entity_id` bigint NOT NULL COMMENT '关联实体编号',
    `relation_type` varchar(64) NOT NULL COMMENT '关系类型',
    `from_template` bit NOT NULL DEFAULT FALSE COMMENT '是否来自模板',
    `required_flag` bit NOT NULL DEFAULT FALSE COMMENT '是否必填',
    `remark` varchar(512) DEFAULT NULL COMMENT '备注',
    "creator" varchar(64) DEFAULT '',
    "create_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updater" varchar(64) DEFAULT '',
    "update_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" bit NOT NULL DEFAULT FALSE,
    "tenant_id" bigint NOT NULL DEFAULT '0',
    PRIMARY KEY ("id")
) COMMENT '五金商家关系申报项';

CREATE TABLE IF NOT EXISTS `wujin_merchant_supply_capability` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '供应能力编号',
    `merchant_id` bigint NOT NULL COMMENT '商家编号',
    `product_id` bigint NOT NULL COMMENT '商品编号',
    `product_name` varchar(128) NOT NULL COMMENT '商品名称',
    `entity_id` bigint NOT NULL COMMENT '关联产业链实体编号',
    `lane` varchar(32) NOT NULL COMMENT '供应泳道',
    `industry` varchar(64) DEFAULT NULL COMMENT '行业上下文',
    `supply_status` tinyint NOT NULL COMMENT '供应状态',
    `stock_count` int DEFAULT NULL COMMENT '库存数量',
    `min_order_quantity` int DEFAULT NULL COMMENT '最小起订量',
    `delivery_days` int DEFAULT NULL COMMENT '交付周期天数',
    `service_area` varchar(128) DEFAULT NULL COMMENT '服务区域',
    `remark` varchar(512) DEFAULT NULL COMMENT '备注',
    "creator" varchar(64) DEFAULT '',
    "create_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updater" varchar(64) DEFAULT '',
    "update_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" bit NOT NULL DEFAULT FALSE,
    "tenant_id" bigint NOT NULL DEFAULT '0',
    PRIMARY KEY ("id")
) COMMENT '五金商家供应能力索引';

CREATE TABLE IF NOT EXISTS `wujin_relation_audit_record` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '审核记录编号',
    `submission_id` bigint NOT NULL COMMENT '申报编号',
    `auditor_id` bigint NOT NULL COMMENT '审核人编号',
    `action` varchar(64) NOT NULL COMMENT '审核动作',
    `reason` varchar(64) NOT NULL COMMENT '审核原因',
    `comment` varchar(512) DEFAULT NULL COMMENT '审核意见',
    `effective_flag` bit NOT NULL DEFAULT FALSE COMMENT '是否生效入网',
    "creator" varchar(64) DEFAULT '',
    "create_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updater" varchar(64) DEFAULT '',
    "update_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" bit NOT NULL DEFAULT FALSE,
    "tenant_id" bigint NOT NULL DEFAULT '0',
    PRIMARY KEY ("id")
) COMMENT '五金关系审核记录';

CREATE TABLE IF NOT EXISTS `wujin_search_rule_config` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '规则编号',
    `rule_type` varchar(64) NOT NULL COMMENT '规则类型',
    `lane` varchar(32) DEFAULT NULL COMMENT '泳道',
    `industry_code` varchar(64) DEFAULT NULL COMMENT '行业编码',
    `rule_value` varchar(512) NOT NULL COMMENT '规则值',
    `weight` int DEFAULT NULL COMMENT '权重',
    `status` tinyint NOT NULL COMMENT '开启状态',
    `remark` varchar(512) DEFAULT NULL COMMENT '备注',
    "creator" varchar(64) DEFAULT '',
    "create_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updater" varchar(64) DEFAULT '',
    "update_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" bit NOT NULL DEFAULT FALSE,
    "tenant_id" bigint NOT NULL DEFAULT '0',
    PRIMARY KEY ("id")
) COMMENT '五金搜索规则配置';

CREATE TABLE IF NOT EXISTS `wujin_search_behavior_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '搜索行为编号',
    `user_id` bigint DEFAULT NULL COMMENT '用户编号',
    `keyword` varchar(128) NOT NULL COMMENT '关键词',
    `intent` varchar(64) DEFAULT NULL COMMENT '搜索意图',
    `result_lane` varchar(32) NOT NULL COMMENT '结果泳道',
    `industry_code` varchar(64) DEFAULT NULL COMMENT '行业编码',
    `chain_viewed` bit NOT NULL DEFAULT FALSE COMMENT '是否查看制造链',
    `classification_correct` bit DEFAULT NULL COMMENT '分类是否正确',
    `high_risk_warning_triggered` bit NOT NULL DEFAULT FALSE COMMENT '是否触发高风险提示',
    `satisfaction_score` int DEFAULT NULL COMMENT '满意度评分',
    `response_time_millis` bigint NOT NULL COMMENT '响应时间毫秒',
    "creator" varchar(64) DEFAULT '',
    "create_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updater" varchar(64) DEFAULT '',
    "update_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" bit NOT NULL DEFAULT FALSE,
    "tenant_id" bigint NOT NULL DEFAULT '0',
    PRIMARY KEY ("id")
) COMMENT '五金搜索行为日志';

CREATE TABLE IF NOT EXISTS `wujin_sourcing_lead` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '寻源线索编号',
    `user_id` bigint DEFAULT NULL COMMENT '用户编号',
    `keyword` varchar(128) NOT NULL COMMENT '关键词',
    `lane` varchar(32) NOT NULL COMMENT '当前泳道',
    `source_keyword` varchar(128) DEFAULT NULL COMMENT '来源关键词',
    `industry` varchar(64) DEFAULT NULL COMMENT '行业上下文',
    `supplier_id` bigint DEFAULT NULL COMMENT '候选供应商编号',
    `supplier_name` varchar(128) DEFAULT NULL COMMENT '候选供应商名称',
    `merchant_id` bigint DEFAULT NULL COMMENT '分发商家编号',
    `contact_name` varchar(64) DEFAULT NULL COMMENT '联系人',
    `contact_phone` varchar(32) NOT NULL COMMENT '联系方式',
    `requirement` varchar(512) NOT NULL COMMENT '需求说明',
    `lead_status` varchar(32) NOT NULL COMMENT '线索状态',
    `dispatch_status` varchar(32) NOT NULL COMMENT '分发状态',
    `dispatch_remark` varchar(512) DEFAULT NULL COMMENT '分发备注',
    `handle_remark` varchar(512) DEFAULT NULL COMMENT '商家处理备注',
    `first_contact_time` timestamp DEFAULT NULL COMMENT '首次联系时间',
    `quoted_time` timestamp DEFAULT NULL COMMENT '报价时间',
    `converted_time` timestamp DEFAULT NULL COMMENT '转化时间',
    `lost_time` timestamp DEFAULT NULL COMMENT '流失时间',
    `process_duration_minutes` bigint DEFAULT NULL COMMENT '处理耗时分钟',
    "creator" varchar(64) DEFAULT '',
    "create_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updater" varchar(64) DEFAULT '',
    "update_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" bit NOT NULL DEFAULT FALSE,
    "tenant_id" bigint NOT NULL DEFAULT '0',
    PRIMARY KEY ("id")
) COMMENT '五金一键寻源线索';

CREATE TABLE IF NOT EXISTS `wujin_monitor_snapshot` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '快照编号',
    `metric` varchar(64) NOT NULL COMMENT '指标',
    `metric_value` int NOT NULL COMMENT '指标值',
    `threshold_value` int DEFAULT NULL COMMENT '阈值',
    `audit_action` varchar(64) DEFAULT NULL COMMENT '审核动作',
    `alert_flag` bit NOT NULL DEFAULT FALSE COMMENT '是否告警',
    `remark` varchar(512) DEFAULT NULL COMMENT '备注',
    "creator" varchar(64) DEFAULT '',
    "create_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updater" varchar(64) DEFAULT '',
    "update_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" bit NOT NULL DEFAULT FALSE,
    "tenant_id" bigint NOT NULL DEFAULT '0',
    PRIMARY KEY ("id")
) COMMENT '五金监控指标快照';
