-- 学生论坛模块建表语句

CREATE TABLE IF NOT EXISTS `forum_user_profile` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint NOT NULL COMMENT '关联 member_user.id',
    `uid` varchar(32) NOT NULL COMMENT '论坛唯一UID',
    `nickname` varchar(64) DEFAULT NULL COMMENT '论坛昵称，默认取会员昵称，可单独设置',
    `avatar` varchar(512) DEFAULT NULL COMMENT '论坛头像，默认取会员头像，可单独设置',

    -- IM 信息
    `im_user_sig` varchar(1024) DEFAULT NULL COMMENT '腾讯IM登录鉴权UserSig',
    `im_user_sig_expire_time` datetime DEFAULT NULL COMMENT 'UserSig过期时间',

    -- 学校认证信息
    `school_name` varchar(64) DEFAULT NULL COMMENT '学校名称',
    `school_email` varchar(128) DEFAULT NULL COMMENT '认证的学校邮箱',
    `school_email_prefix` varchar(64) DEFAULT NULL COMMENT '学校邮箱前缀（@之前的部分）',
    `school_email_verified` bit(1) DEFAULT b'0' COMMENT '是否完成学校邮箱认证',
    `school_email_verify_time` datetime DEFAULT NULL COMMENT '学校邮箱通过时间',
    `real_name` varchar(64) DEFAULT NULL COMMENT '真实姓名（认证后填写）',
    `gender` tinyint DEFAULT NULL COMMENT '性别：1 男，2 女',
    `major_and_grade` varchar(128) DEFAULT NULL COMMENT '专业及年级',
    `school_info_public` bit(1) DEFAULT b'0' COMMENT '是否公开学校信息',

    -- 扩展信息
    `birthday` date DEFAULT NULL COMMENT '出生年月',
    `constellation` varchar(16) DEFAULT NULL COMMENT '星座（根据生日自动计算）',
    `mbti` varchar(4) DEFAULT NULL COMMENT 'MBTI 性格类型',
    `introduction` varchar(500) DEFAULT NULL COMMENT '个人介绍',

    -- 积分与统计
    `point` int DEFAULT 0 COMMENT '论坛积分余额',
    `total_point` int DEFAULT 0 COMMENT '累计获得的论坛积分',
    `continuous_sign_days` int DEFAULT 0 COMMENT '连续签到天数',
    `total_sign_days` int DEFAULT 0 COMMENT '累计签到天数',
    `last_sign_date` date DEFAULT NULL COMMENT '最后签到时间',
    `post_count` int DEFAULT 0 COMMENT '发帖数量',
    `activity_count` int DEFAULT 0 COMMENT '参与活动数量',
    `like_count` int DEFAULT 0 COMMENT '获得的赞数量',
    `favorite_count` int DEFAULT 0 COMMENT '获得的收藏数量',

    -- 系统字段
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
    `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
    `deleted` bit(1) DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint DEFAULT 0 COMMENT '租户ID',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user` (`user_id`),
    UNIQUE KEY `uk_uid` (`uid`),
    KEY `idx_school_email` (`school_email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛用户扩展信息';

CREATE TABLE IF NOT EXISTS `forum_point_record` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL COMMENT '关联 member_user.id',
    `biz_id` varchar(64) DEFAULT NULL COMMENT '业务编号，如帖子ID、活动ID',
    `biz_type` int NOT NULL COMMENT '业务类型，对应 ForumPointBizTypeEnum',
    `title` varchar(64) NOT NULL COMMENT '积分标题',
    `description` varchar(255) DEFAULT NULL COMMENT '积分描述',
    `point` int NOT NULL COMMENT '变动积分，正数表示获得，负数表示消耗',
    `total_point` int NOT NULL COMMENT '变动后的积分余额',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator` varchar(64) DEFAULT NULL,
    `updater` varchar(64) DEFAULT NULL,
    `deleted` bit(1) DEFAULT b'0',
    `tenant_id` bigint DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛积分记录';

CREATE TABLE IF NOT EXISTS `forum_sign_record` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint NOT NULL COMMENT '关联 member_user.id',
    `sign_date` date NOT NULL COMMENT '签到日期',
    `continuous_days` int DEFAULT 1 COMMENT '连续签到天数',
    `point` int DEFAULT 0 COMMENT '获得的积分',
    `remark` varchar(255) DEFAULT NULL COMMENT '签到备注',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
    `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
    `deleted` bit(1) DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `sign_date`),
    KEY `idx_user` (`user_id`),
    KEY `idx_sign_date` (`sign_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛签到记录';

CREATE TABLE IF NOT EXISTS `forum_post` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL COMMENT '发帖人，关联 member_user.id',
    `title` varchar(128) NOT NULL COMMENT '帖子标题',
    `content` text NOT NULL COMMENT '帖子内容',
    `category` int DEFAULT 0 COMMENT '帖子分类枚举',
    `categories` text DEFAULT NULL COMMENT '帖子分类列表，JSON 数组',
    `image_urls` text DEFAULT NULL COMMENT '图片URL列表，JSON格式',
    `anonymous` bit(1) DEFAULT b'1' COMMENT '是否匿名',
    `anonymous_nickname` varchar(64) DEFAULT NULL COMMENT '匿名昵称',
    `anonymous_avatar` varchar(512) DEFAULT NULL COMMENT '匿名头像',
    `school_only` bit(1) DEFAULT b'0' COMMENT '是否仅本校可见',
    `status` int DEFAULT 0 COMMENT '审核状态：0待审核，1已通过，2已驳回',
    `is_top` bit(1) DEFAULT b'0' COMMENT '是否置顶',
    `like_count` int DEFAULT 0 COMMENT '点赞数',
    `comment_count` int DEFAULT 0 COMMENT '评论数',
    `follow_count` int DEFAULT 0 COMMENT '蹲后续人数',
    `view_count` int DEFAULT 0 COMMENT '浏览次数',
    `latest_comment_time` datetime DEFAULT NULL COMMENT '最后评论时间',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator` varchar(64) DEFAULT NULL,
    `updater` varchar(64) DEFAULT NULL,
    `deleted` bit(1) DEFAULT b'0',
    `tenant_id` bigint DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_status_create` (`status`, `create_time`),
    KEY `idx_latest_comment` (`latest_comment_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛帖子';

CREATE TABLE IF NOT EXISTS `forum_comment` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `post_id` bigint NOT NULL COMMENT '关联帖子ID',
    `user_id` bigint NOT NULL COMMENT '评论人，关联 member_user.id',
    `parent_id` bigint DEFAULT NULL COMMENT '父评论ID',
    `root_id` bigint DEFAULT NULL COMMENT '根评论ID',
    `content` text NOT NULL COMMENT '评论内容',
    `anonymous` bit(1) DEFAULT b'1' COMMENT '是否匿名',
    `anonymous_nickname` varchar(64) DEFAULT NULL COMMENT '匿名昵称',
    `anonymous_avatar` varchar(512) DEFAULT NULL COMMENT '匿名头像',
    `status` int DEFAULT 0 COMMENT '状态：0正常，1已删除，2待审核',
    `like_count` int DEFAULT 0 COMMENT '点赞数',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator` varchar(64) DEFAULT NULL,
    `updater` varchar(64) DEFAULT NULL,
    `deleted` bit(1) DEFAULT b'0',
    `tenant_id` bigint DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (`id`),
    KEY `idx_post` (`post_id`),
    KEY `idx_post_root` (`post_id`, `root_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛评论';

CREATE TABLE IF NOT EXISTS `forum_post_like` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `post_id` bigint NOT NULL COMMENT '帖子ID',
    `user_id` bigint NOT NULL COMMENT '点赞用户ID',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator` varchar(64) DEFAULT NULL,
    `updater` varchar(64) DEFAULT NULL,
    `deleted` bit(1) DEFAULT b'0',
    `tenant_id` bigint DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛帖子点赞';

CREATE TABLE IF NOT EXISTS `forum_post_follow` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `post_id` bigint NOT NULL COMMENT '帖子ID',
    `user_id` bigint NOT NULL COMMENT '关注用户ID',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator` varchar(64) DEFAULT NULL,
    `updater` varchar(64) DEFAULT NULL,
    `deleted` bit(1) DEFAULT b'0',
    `tenant_id` bigint DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛帖子关注（蹲后续）';

CREATE TABLE IF NOT EXISTS `forum_comment_like` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `comment_id` bigint NOT NULL COMMENT '评论ID',
    `user_id` bigint NOT NULL COMMENT '点赞用户ID',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator` varchar(64) DEFAULT NULL,
    `updater` varchar(64) DEFAULT NULL,
    `deleted` bit(1) DEFAULT b'0',
    `tenant_id` bigint DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛评论点赞';

CREATE TABLE IF NOT EXISTS `forum_report` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL COMMENT '举报人ID',
    `report_type` int NOT NULL COMMENT '举报类型：1-帖子，2-评论，3-用户',
    `target_id` bigint NOT NULL COMMENT '被举报对象ID',
    `reason_type` int NOT NULL COMMENT '举报原因类型',
    `reason_text` varchar(500) DEFAULT NULL COMMENT '举报原因详细描述',
    `images` text DEFAULT NULL COMMENT '举报截图，JSON格式',
    `contact` varchar(128) DEFAULT NULL COMMENT '联系方式',
    `status` int DEFAULT 0 COMMENT '处理状态：0-待处理，1-已处理，2-已驳回',
    `handle_result` varchar(500) DEFAULT NULL COMMENT '处理结果',
    `handle_user_id` bigint DEFAULT NULL COMMENT '处理人ID',
    `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator` varchar(64) DEFAULT NULL,
    `updater` varchar(64) DEFAULT NULL,
    `deleted` bit(1) DEFAULT b'0',
    `tenant_id` bigint DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_target` (`report_type`, `target_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛举报';

CREATE TABLE IF NOT EXISTS `forum_activity` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL COMMENT '发布人，关联 member_user.id',
    `admin_member_ids` text DEFAULT NULL COMMENT '管理员memberId列表，JSON数组格式，如 [1001, 1002]',
    `title` varchar(128) NOT NULL COMMENT '活动标题',
    `description` text NOT NULL COMMENT '活动描述',
    `cover_image` varchar(512) NOT NULL COMMENT '活动封面图',
    `detail_images` text DEFAULT NULL COMMENT '活动详情图片列表，JSON格式',
    `category` int NOT NULL COMMENT '活动分类：1-学术讲座，2-文体活动，3-社团活动，4-志愿服务，5-其他',
    `location` varchar(256) NOT NULL COMMENT '活动地点',
    `longitude` double DEFAULT NULL COMMENT '活动地点经度',
    `latitude` double DEFAULT NULL COMMENT '活动地点纬度',
    `start_time` datetime NOT NULL COMMENT '活动开始时间',
    `end_time` datetime NOT NULL COMMENT '活动结束时间',
    `sign_up_start_time` datetime DEFAULT NULL COMMENT '报名开始时间',
    `sign_up_end_time` datetime DEFAULT NULL COMMENT '报名结束时间',
    `check_in_start_time` datetime DEFAULT NULL COMMENT '签到开始时间',
    `check_in_end_time` datetime DEFAULT NULL COMMENT '签到结束时间',
    `check_in_distance` int DEFAULT 100 COMMENT '签到距离限制（米）',
    `check_in_type` int DEFAULT NULL COMMENT '签到方式：1-自助签到；2-定位签到；3-扫码签到',
    `max_participants` int DEFAULT 0 COMMENT '报名人数限制，0表示不限制',
    `current_participants` int DEFAULT 0 COMMENT '当前报名人数',
    `need_approval` bit(1) DEFAULT b'0' COMMENT '是否需要审核报名',
    `need_point` bit(1) DEFAULT b'0' COMMENT '报名是否需要积分',
    `point_amount` int DEFAULT 0 COMMENT '报名所需积分',
    `requirements` text DEFAULT NULL COMMENT '报名要求',
    `school_only` bit(1) DEFAULT b'0' COMMENT '是否仅本校可见',
    `allow_unverified` bit(1) DEFAULT b'1' COMMENT '是否允许未实名用户报名',
    `status` int DEFAULT 1 COMMENT '活动状态：0-草稿，1-报名中，2-进行中，3-已结束，4-已取消',
    `view_count` int DEFAULT 0 COMMENT '浏览次数',
    `like_count` int DEFAULT 0 COMMENT '点赞数',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator` varchar(64) DEFAULT NULL,
    `updater` varchar(64) DEFAULT NULL,
    `deleted` bit(1) DEFAULT b'0',
    `tenant_id` bigint DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_category_status` (`category`, `status`),
    KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛活动';

CREATE TABLE IF NOT EXISTS `forum_activity_sign_up` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `activity_id` bigint NOT NULL COMMENT '活动ID',
    `user_id` bigint NOT NULL COMMENT '报名用户ID',
    `remark` varchar(500) DEFAULT NULL COMMENT '报名备注',
    `feedback` varchar(500) DEFAULT NULL COMMENT '点评/回顾/反馈',
    `approval_status` int DEFAULT 0 COMMENT '审核状态：0-待审核，1-已通过，2-已拒绝',
    `approval_remark` varchar(500) DEFAULT NULL COMMENT '审核备注',
    `checked_in` bit(1) DEFAULT b'0' COMMENT '是否已签到',
    `check_in_time` datetime DEFAULT NULL COMMENT '签到时间',
    `check_in_longitude` double DEFAULT NULL COMMENT '签到经度',
    `check_in_latitude` double DEFAULT NULL COMMENT '签到纬度',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator` varchar(64) DEFAULT NULL,
    `updater` varchar(64) DEFAULT NULL,
    `deleted` bit(1) DEFAULT b'0',
    `tenant_id` bigint DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_approval_status` (`approval_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛活动报名';

CREATE TABLE IF NOT EXISTS `forum_conversation` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user1_id` bigint NOT NULL COMMENT '用户1 ID',
    `user2_id` bigint NOT NULL COMMENT '用户2 ID',
    `last_message_content` varchar(500) DEFAULT NULL COMMENT '最后一条消息内容',
    `last_message_time` datetime DEFAULT NULL COMMENT '最后一条消息时间',
    `last_message_sender_id` bigint DEFAULT NULL COMMENT '最后一条消息发送人ID',
    `user1_unread_count` int DEFAULT 0 COMMENT '用户1未读消息数',
    `user2_unread_count` int DEFAULT 0 COMMENT '用户2未读消息数',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator` varchar(64) DEFAULT NULL,
    `updater` varchar(64) DEFAULT NULL,
    `deleted` bit(1) DEFAULT b'0',
    `tenant_id` bigint DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (`id`),
    KEY `idx_user1` (`user1_id`),
    KEY `idx_user2` (`user2_id`),
    KEY `idx_last_message_time` (`last_message_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛会话';

CREATE TABLE IF NOT EXISTS `forum_message` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `conversation_id` bigint NOT NULL COMMENT '会话ID',
    `sender_id` bigint NOT NULL COMMENT '发送人ID',
    `receiver_id` bigint NOT NULL COMMENT '接收人ID',
    `message_type` int NOT NULL COMMENT '消息类型：1-文本，2-图片，3-语音，4-视频',
    `content` text NOT NULL COMMENT '消息内容',
    `read_status` bit(1) DEFAULT b'0' COMMENT '是否已读',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator` varchar(64) DEFAULT NULL,
    `updater` varchar(64) DEFAULT NULL,
    `deleted` bit(1) DEFAULT b'0',
    `tenant_id` bigint DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (`id`),
    KEY `idx_conversation` (`conversation_id`),
    KEY `idx_sender` (`sender_id`),
    KEY `idx_receiver` (`receiver_id`),
    KEY `idx_read_status` (`read_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛消息';

CREATE TABLE IF NOT EXISTS `forum_system_notice` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL COMMENT '接收人ID',
    `notice_type` int NOT NULL COMMENT '通知类型：1-点赞通知，2-评论通知，3-关注通知，4-系统通知，5-活动通知',
    `title` varchar(128) NOT NULL COMMENT '通知标题',
    `content` varchar(500) NOT NULL COMMENT '通知内容',
    `related_id` bigint DEFAULT NULL COMMENT '关联业务ID',
    `related_type` int DEFAULT NULL COMMENT '关联业务类型：1-帖子，2-评论，3-活动',
    `read_status` bit(1) DEFAULT b'0' COMMENT '是否已读',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `creator` varchar(64) DEFAULT NULL,
    `updater` varchar(64) DEFAULT NULL,
    `deleted` bit(1) DEFAULT b'0',
    `tenant_id` bigint DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_notice_type` (`notice_type`),
    KEY `idx_read_status` (`read_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛系统通知';

-- 为 forum_post 表添加审核结论字段
ALTER TABLE `forum_post` ADD COLUMN `review_result` varchar(512) DEFAULT NULL COMMENT '审核结论（审核不通过时的拒绝原因）' AFTER `status`;

-- 将 forum_activity 表的 admin_member_id 字段改为 admin_member_ids
ALTER TABLE `forum_activity` CHANGE COLUMN `admin_member_id` `admin_member_ids` text DEFAULT NULL COMMENT '管理员memberId列表，JSON数组格式，如 [1001, 1002]';

-- 为 forum_activity 表添加是否显示报名人数字段
ALTER TABLE `forum_activity` ADD COLUMN `show_participant_count` tinyint(1) DEFAULT 1 COMMENT '是否显示报名人数：1-显示，0-不显示' AFTER `custom_fields`;
