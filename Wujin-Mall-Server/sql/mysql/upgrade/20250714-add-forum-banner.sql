-- Banner配置表
CREATE TABLE IF NOT EXISTS `forum_banner` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title` varchar(128) NOT NULL COMMENT 'Banner标题',
    `image_url` varchar(512) NOT NULL COMMENT '图片地址',
    `target_type` tinyint NOT NULL COMMENT '跳转类型：1=帖子详情 2=活动详情 3=用户主页 4=外部链接',
    `target_id` varchar(128) DEFAULT NULL COMMENT '跳转目标ID（帖子ID/活动ID/用户ID）',
    `target_url` varchar(512) DEFAULT NULL COMMENT '跳转链接（外部链接时使用）',
    `sort` int NOT NULL DEFAULT 0 COMMENT '排序值，越大越靠前',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0=禁用 1=启用',
    `start_time` datetime DEFAULT NULL COMMENT '生效时间',
    `end_time` datetime DEFAULT NULL COMMENT '失效时间',
    `remark` varchar(256) DEFAULT NULL COMMENT '备注',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_status_sort` (`status`, `sort` DESC),
    KEY `idx_start_end_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Banner配置表';
