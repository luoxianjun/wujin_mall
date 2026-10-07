package cn.iocoder.yudao.module.forum.controller.app.activity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 活动签到请求 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 活动签到请求 VO")
@Data
public class AppActivityCheckInReqVO {

    @Schema(description = "活动ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @Schema(description = "签到经度（定位签到必填）", example = "113.12345")
    private Double longitude;

    @Schema(description = "签到纬度（定位签到必填）", example = "22.12345")
    private Double latitude;

}
