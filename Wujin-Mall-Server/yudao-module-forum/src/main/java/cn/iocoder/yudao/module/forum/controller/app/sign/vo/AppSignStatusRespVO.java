package cn.iocoder.yudao.module.forum.controller.app.sign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 签到状态响应 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 签到状态响应 VO")
@Data
public class AppSignStatusRespVO {

    @Schema(description = "今日是否已签到", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean todaySigned;

    @Schema(description = "连续签到天数", requiredMode = Schema.RequiredMode.REQUIRED, example = "7")
   private Integer continuousDays;

    @Schema(description = "累计签到天数", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    private Integer totalDays;

    @Schema(description = "最后签到日期", example = "2024-01-01")
    private LocalDate lastSignDate;

    @Schema(description = "本周连续签到天数", example = "3")
    private Integer weekContinuousDays;

    @Schema(description = "本周最高连续签到天数", example = "4")
    private Integer weekMaxContinuousDays;

    @Schema(description = "本周累计签到积分", example = "15")
    private Integer weekTotalPoints;

    @Schema(description = "本周最后一次签到日期", example = "2024-01-02")
    private LocalDate weekLastSignDate;

    @Schema(description = "本周已签到的日期列表，按日期升序", example = "[\"2024-01-01\",\"2024-01-03\"]")
    private java.util.List<LocalDate> weekSignedDates;

    @Schema(description = "当前生效的签到规则列表", example = "[{\"periodType\":1,\"minDays\":1,\"maxDays\":1,\"points\":1}]")
    private List<AppSignRuleRespVO> signRules;

    @Schema(description = "本周 7 天对应的奖励积分列表（按周一到周日顺序）", example = "[1,1,3,5,7,9,10]")
    private List<Integer> weekRewardPoints;
}
