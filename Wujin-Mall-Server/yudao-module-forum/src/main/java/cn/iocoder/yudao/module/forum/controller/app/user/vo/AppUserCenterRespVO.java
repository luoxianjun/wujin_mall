package cn.iocoder.yudao.module.forum.controller.app.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 个人中心响应 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 个人中心响应 VO")
@Data
public class AppUserCenterRespVO {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "论坛唯一UID", requiredMode = Schema.RequiredMode.REQUIRED, example = "U123456")
    private String uid;

    @Schema(description = "昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String nickname;

    @Schema(description = "头像", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "学校名称", example = "澳门大学")
    private String schoolName;

    @Schema(description = "是否完成学校认证", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean schoolEmailVerified;

    @Schema(description = "星座", example = "摩羯座")
    private String constellation;

    @Schema(description = "MBTI 性格类型", example = "INTJ")
    private String mbti;

    @Schema(description = "个人介绍", example = "这是我的个人介绍")
    private String introduction;

    // 积分信息
    @Schema(description = "积分余额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer point;

    @Schema(description = "累计积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "500")
    private Integer totalPoint;

    // 签到信息
    @Schema(description = "连续签到天数", requiredMode = Schema.RequiredMode.REQUIRED, example = "7")
    private Integer continuousSignDays;

    @Schema(description = "累计签到天数", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    private Integer totalSignDays;

    @Schema(description = "最后签到日期", example = "2024-01-01")
    private LocalDate lastSignDate;

    @Schema(description = "今日是否已签到", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean todaySigned;

    // 统计信息
    @Schema(description = "发帖数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer postCount;

    @Schema(description = "参与活动数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer activityCount;

    @Schema(description = "获赞数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    private Integer likeCount;

    @Schema(description = "收藏数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer favoriteCount;

    @Schema(description = "IM 用户签名", example = "xxx")
    private String userSig;

    // 隐私设置
    @Schema(description = "是否允许私聊", example = "true")
    private Boolean allowPrivateChat;

    @Schema(description = "是否接收系统消息", example = "true")
    private Boolean allowSystemMessage;

    @Schema(description = "是否为管理员", example = "false")
    private Boolean isAdmin;

    @Schema(description = "是否隐藏认证学校信息", example = "false")
    private Boolean hideSchoolInfo;

}
