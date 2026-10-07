package cn.iocoder.yudao.module.forum.controller.app.banner.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户 APP - Banner 响应 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - Banner 响应 VO")
@Data
public class AppBannerRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "Banner标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "新活动上线")
    private String title;

    @Schema(description = "图片地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://example.com/banner.jpg")
    private String imageUrl;

    @Schema(description = "跳转类型：1=帖子详情 2=活动详情 3=用户主页 4=外部链接", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer targetType;

    @Schema(description = "跳转目标ID（帖子ID/活动ID/用户ID）", example = "1024")
    private String targetId;

    @Schema(description = "跳转链接（外部链接时使用）", example = "https://www.baidu.com")
    private String targetUrl;

    // ========== 活动相关信息（targetType=2时返回） ==========
    @Schema(description = "活动名称（活动类型时返回）", example = "校园音乐节")
    private String activityTitle;

    @Schema(description = "报名要求（活动类型时返回）", example = "需要提前报名，携带学生证")
    private String activityRequirements;

    @Schema(description = "活动奖励（活动类型时返回，选填）", example = "参与可获得50积分")
    private String activityReward;

    @Schema(description = "活动时间（活动类型时返回）", example = "2024-01-01 10:00 - 2024-01-01 18:00")
    private String activityTime;

    @Schema(description = "活动地点（活动类型时返回）", example = "学校大礼堂")
    private String activityLocation;

    // ========== 帖子相关信息（targetType=1时返回） ==========
    @Schema(description = "帖子标题（帖子类型时返回）", example = "关于校园网使用的问题")
    private String postTitle;

}
