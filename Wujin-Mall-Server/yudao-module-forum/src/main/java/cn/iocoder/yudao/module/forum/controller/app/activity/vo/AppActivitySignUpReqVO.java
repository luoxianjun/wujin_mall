package cn.iocoder.yudao.module.forum.controller.app.activity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 活动报名请求 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 活动报名请求 VO")
@Data
public class AppActivitySignUpReqVO {

    @Schema(description = "活动ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @Schema(description = "报名备注", example = "我想参加")
    private String remark;

}

