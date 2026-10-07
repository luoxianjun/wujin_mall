package cn.iocoder.yudao.module.forum.controller.app.sign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 签到响应 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 签到响应 VO")
@Data
public class AppSignRespVO {

    @Schema(description = "签到记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "签到日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-01-01")
    private LocalDate signDate;

    @Schema(description = "连续签到天数", requiredMode = Schema.RequiredMode.REQUIRED, example = "7")
    private Integer continuousDays;

    @Schema(description = "获得的积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer point;

    @Schema(description = "签到备注", example = "连续签到7天，额外奖励5积分")
    private String remark;

}

