package cn.iocoder.yudao.module.forum.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Forum 错误码枚举类
 * 
 * forum 系统，使用 1-100-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 用户模块 1-100-001-000 ==========
    ErrorCode USER_PROFILE_NOT_EXISTS = new ErrorCode(1_100_001_000, "用户资料不存在");
    ErrorCode USER_PROFILE_EXISTS = new ErrorCode(1_100_001_001, "用户资料已存在");
    ErrorCode SCHOOL_EMAIL_INVALID = new ErrorCode(1_100_001_002, "学校邮箱格式不正确");
    ErrorCode SCHOOL_EMAIL_NOT_SUPPORTED = new ErrorCode(1_100_001_003, "不支持的学校邮箱后缀");
    ErrorCode SCHOOL_EMAIL_ALREADY_VERIFIED = new ErrorCode(1_100_001_004, "该邮箱已被认证");
    ErrorCode VERIFICATION_CODE_INVALID = new ErrorCode(1_100_001_005, "验证码无效或已过期");
    ErrorCode UID_GENERATION_FAILED = new ErrorCode(1_100_001_006, "UID 生成失败");
    ErrorCode SCHOOL_EMAIL_SEND_FAILED = new ErrorCode(1_100_001_007, "发送学校邮箱验证码失败");
    ErrorCode IM_CONFIG_INVALID = new ErrorCode(1_100_001_008, "IM 配置未正确设置");
    ErrorCode IM_USER_SIG_GENERATE_FAIL = new ErrorCode(1_100_001_009, "IM 签名生成失败");

    // ========== 帖子模块 1-100-002-000 ==========
    ErrorCode POST_NOT_EXISTS = new ErrorCode(1_100_002_000, "帖子不存在");
    ErrorCode POST_DELETED = new ErrorCode(1_100_002_001, "帖子已被删除");
    ErrorCode POST_NOT_APPROVED = new ErrorCode(1_100_002_002, "帖子未通过审核");
    ErrorCode POST_CONTENT_INVALID = new ErrorCode(1_100_002_003, "帖子内容包含敏感信息");
    ErrorCode POST_DELETE_FAIL_NOT_OWNER = new ErrorCode(1_100_002_004, "只能删除自己的帖子");
    ErrorCode POST_ALREADY_LIKED = new ErrorCode(1_100_002_005, "已点赞该帖子");
    ErrorCode POST_NOT_LIKED = new ErrorCode(1_100_002_006, "未点赞该帖子");
    ErrorCode POST_ALREADY_FOLLOWED = new ErrorCode(1_100_002_007, "已关注该帖子");
    ErrorCode POST_NOT_FOLLOWED = new ErrorCode(1_100_002_008, "未关注该帖子");
    ErrorCode POST_TOP_ONLY_ADMIN = new ErrorCode(1_100_002_009, "只能置顶管理员发布的帖子");

    // ========== 评论模块 1-100-003-000 ==========
    ErrorCode COMMENT_NOT_EXISTS = new ErrorCode(1_100_003_000, "评论不存在");
    ErrorCode COMMENT_DELETED = new ErrorCode(1_100_003_001, "评论已被删除");
    ErrorCode COMMENT_DELETE_FAIL_NOT_OWNER = new ErrorCode(1_100_003_002, "只能删除自己的评论");
    ErrorCode COMMENT_ALREADY_LIKED = new ErrorCode(1_100_003_003, "已点赞该评论");
    ErrorCode COMMENT_NOT_LIKED = new ErrorCode(1_100_003_004, "未点赞该评论");

    // ========== 积分模块 1-100-004-000 ==========
    ErrorCode POINT_NOT_ENOUGH = new ErrorCode(1_100_004_000, "积分不足");
    ErrorCode POINT_RECORD_NOT_EXISTS = new ErrorCode(1_100_004_001, "积分记录不存在");

    // ========== 签到模块 1-100-005-000 ==========
    ErrorCode SIGN_ALREADY_TODAY = new ErrorCode(1_100_005_000, "今日已签到");

    // ========== 活动模块 1-100-006-000 ==========
    ErrorCode ACTIVITY_NOT_EXISTS = new ErrorCode(1_100_006_000, "活动不存在");
    ErrorCode ACTIVITY_NOT_STARTED = new ErrorCode(1_100_006_001, "活动未开始");
    ErrorCode ACTIVITY_ENDED = new ErrorCode(1_100_006_002, "活动已结束");
    ErrorCode ACTIVITY_FULL = new ErrorCode(1_100_006_003, "活动报名人数已满");
    ErrorCode ACTIVITY_ALREADY_SIGNED_UP = new ErrorCode(1_100_006_004, "已报名该活动");
    ErrorCode ACTIVITY_NOT_SIGNED_UP = new ErrorCode(1_100_006_005, "未报名该活动");
    ErrorCode ACTIVITY_CHECKIN_NOT_STARTED = new ErrorCode(1_100_006_006, "签到未开始");
    ErrorCode ACTIVITY_CHECKIN_ENDED = new ErrorCode(1_100_006_007, "签到已结束");
    ErrorCode ACTIVITY_ALREADY_CHECKED_IN = new ErrorCode(1_100_006_008, "已签到");
    ErrorCode ACTIVITY_CHECKIN_LOCATION_TOO_FAR = new ErrorCode(1_100_006_009, "签到位置距离活动地点过远");
    ErrorCode ACTIVITY_OPERATE_FAIL_NOT_OWNER = new ErrorCode(1_100_006_010, "只能操作自己的活动");
    ErrorCode ACTIVITY_TIME_INVALID = new ErrorCode(1_100_006_011, "活动时间设置不正确");
    ErrorCode ACTIVITY_SIGN_UP_NOT_STARTED = new ErrorCode(1_100_006_012, "报名未开始");
    ErrorCode ACTIVITY_SIGN_UP_ENDED = new ErrorCode(1_100_006_013, "报名已结束");
    ErrorCode ACTIVITY_SIGN_UP_NOT_APPROVED = new ErrorCode(1_100_006_014, "报名未通过审核");
    ErrorCode ACTIVITY_CHECKIN_QR_INVALID = new ErrorCode(1_100_006_015, "签到二维码无效");
    ErrorCode ACTIVITY_CHECKIN_LOCATION_REQUIRED = new ErrorCode(1_100_006_016, "定位签到需要提供经纬度");
    ErrorCode ACTIVITY_CHECKIN_TYPE_MISMATCH = new ErrorCode(1_100_006_017, "该活动不支持当前签到方式");
    ErrorCode ACTIVITY_CHECKIN_LOCATION_NOT_SET = new ErrorCode(1_100_006_018, "活动未配置签到位置");
    ErrorCode ACTIVITY_SIGN_UP_CANCELLED = new ErrorCode(1_100_006_019, "您已取消过该活动报名，无法再次报名");
    ErrorCode ACTIVITY_CANCELLED = new ErrorCode(1_100_006_020, "活动已取消");

    // ========== 消息模块 1-100-007-000 ==========
    ErrorCode MESSAGE_NOT_EXISTS = new ErrorCode(1_100_007_000, "消息不存在");
    ErrorCode CONVERSATION_NOT_EXISTS = new ErrorCode(1_100_007_001, "会话不存在");
    ErrorCode MESSAGE_LIMIT_EXCEEDED = new ErrorCode(1_100_007_002, "未回复私信限制，最多发送3条消息");

    // ========== 签到规则 1-100-008-000 ==========
    ErrorCode SIGN_RULE_NOT_EXISTS = new ErrorCode(1_100_008_000, "签到规则不存在");

    // ========== 图片审核 1-100-009-000 ==========
    ErrorCode IMAGE_AUDIT_FAILED = new ErrorCode(1_100_009_000, "图片审核失败，请修改后重试");
    ErrorCode IMAGE_AUDIT_REJECTED = new ErrorCode(1_100_009_001, "图片未通过审核：{}");

    // ========== 文本审核 1-100-010-000 ==========
    ErrorCode TEXT_AUDIT_FAILED = new ErrorCode(1_100_010_000, "内容违规");
    ErrorCode TEXT_AUDIT_REJECTED = new ErrorCode(1_100_010_001, "文本未通过审核：{}");

    // ========== 管理员限制 1-100-011-000 ==========
    ErrorCode ADMIN_OPERATION_FORBIDDEN = new ErrorCode(1_100_011_000, "管理员不能执行此操作");

    // ========== Banner 模块 1-100-012-000 ==========
    ErrorCode BANNER_NOT_EXISTS = new ErrorCode(1_100_012_000, "Banner不存在");

}
