-- 五金寻源线索深度流转与转化报表字段

ALTER TABLE `wujin_sourcing_lead`
    ADD COLUMN `first_contact_time` datetime DEFAULT NULL COMMENT '首次联系时间' AFTER `handle_remark`,
    ADD COLUMN `quoted_time` datetime DEFAULT NULL COMMENT '报价时间' AFTER `first_contact_time`,
    ADD COLUMN `converted_time` datetime DEFAULT NULL COMMENT '转化时间' AFTER `quoted_time`,
    ADD COLUMN `lost_time` datetime DEFAULT NULL COMMENT '流失时间' AFTER `converted_time`,
    ADD COLUMN `process_duration_minutes` bigint DEFAULT NULL COMMENT '处理耗时分钟' AFTER `lost_time`;
