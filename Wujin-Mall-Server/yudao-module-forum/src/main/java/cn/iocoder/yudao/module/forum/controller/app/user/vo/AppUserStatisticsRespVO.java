package cn.iocoder.yudao.module.forum.controller.app.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 用户统计信息响应 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 用户统计信息响应 VO")
@Data
public class AppUserStatisticsRespVO {

    @Schema(description = "论坛积分余额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer point;

    @Schema(description = "累计获得的论坛积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "500")
    private Integer totalPoint;

    @Schema(description = "连续签到天数", requiredMode = Schema.RequiredMode.REQUIRED, example = "7")
    private Integer continuousSignDays;

    @Schema(description = "累计签到天数", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    private Integer totalSignDays;

    @Schema(description = "最后签到时间", example = "2024-01-01")
    private LocalDate lastSignDate;

    @Schema(description = "发帖数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer postCount;

    @Schema(description = "参与活动数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer activityCount;

    @Schema(description = "获得的赞数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    private Integer likeCount;

    @Schema(description = "获得的收藏数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer favoriteCount;

}

