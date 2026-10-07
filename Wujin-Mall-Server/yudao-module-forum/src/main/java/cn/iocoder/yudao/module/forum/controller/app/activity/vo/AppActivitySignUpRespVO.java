package cn.iocoder.yudao.module.forum.controller.app.activity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动报名响应 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 活动报名响应 VO")
@Data
public class AppActivitySignUpRespVO {

    @Schema(description = "报名ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "活动ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long activityId;

    @Schema(description = "活动标题", example = "学术讲座")
    private String activityTitle;

    @Schema(description = "活动封面图", example = "https://example.com/cover.jpg")
    private String coverImage;

    @Schema(description = "是否需要签到", example = "true")
    private Boolean needCheckIn;

    @Schema(description = "签到类型：1-自助签到, 2-定位签到, 3-扫码签到", example = "2")
    private Integer checkInType;

    @Schema(description = "签到位置纬度", example = "22.123456")
    private Double latitude;

    @Schema(description = "签到位置经度", example = "113.654321")
    private Double longitude;

    @Schema(description = "签到距离(米)", example = "100")
    private Integer checkInDistance;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "用户UID", example = "U123456")
    private String uid;

    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @Schema(description = "用户头像", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "报名备注", example = "我想参加")
    private String remark;

    @Schema(description = "点评/回顾/反馈", example = "活动组织很棒")
    private String feedback;

    @Schema(description = "审核状态：0-待审核，1-已通过，2-已拒绝，3-已取消", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer approvalStatus;

    @Schema(description = "审核状态名称", example = "已通过")
    private String approvalStatusName;

    @Schema(description = "审核备注", example = "通过")
    private String approvalRemark;

    @Schema(description = "是否已签到", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean checkedIn;

    @Schema(description = "签到时间", example = "2024-01-01 12:00:00")
    private LocalDateTime checkInTime;

    @Schema(description = "报名时间", example = "2024-01-01 10:00:00")
    private LocalDateTime createTime;

}
