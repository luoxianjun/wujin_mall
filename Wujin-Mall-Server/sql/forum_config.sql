-- 论坛配置表
CREATE TABLE IF NOT EXISTS `forum_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text COMMENT '配置值',
  `name` varchar(100) DEFAULT NULL COMMENT '配置名称',
  `type` varchar(20) DEFAULT 'text' COMMENT '配置类型：text-普通文本, rich_text-富文本, json-JSON',
  `remark` varchar(500) DEFAULT NULL COMMENT '配置描述',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论坛配置表';

-- 插入默认福利群配置（tenant_id 设置为 1，与管理员租户匹配）
INSERT INTO `forum_config` (`config_key`, `config_value`, `name`, `type`, `remark`, `tenant_id`) VALUES
('forum.welfare.content', '<div style="text-align: center;"><h3>🎁 蜻葱校园福利群</h3><p>扫码加入福利群，获取专属优惠！</p><p style="color: #10b981;">• 每日红包雨</p><p style="color: #10b981;">• 限时优惠券</p><p style="color: #10b981;">• 活动第一手资讯</p><p style="margin-top: 20px;"><img src="https://via.placeholder.com/200x200?text=QRCode" alt="群二维码" style="max-width: 200px;"/></p></div>', '福利群内容', 'rich_text', '首页福利群浮窗点击后显示的富文本内容，支持HTML', 1);
