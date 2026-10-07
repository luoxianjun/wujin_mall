package cn.iocoder.yudao.module.forum.controller.app.activity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 管理员扫码签到请求 VO
 */
@Schema(description = "用户 APP - 管理员扫码签到请求 VO")
@Data
public class AppActivityAdminCheckInReqVO {

    @Schema(description = "活动ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @Schema(description = "签到二维码内容（Base64 编码）", requiredMode = Schema.RequiredMode.REQUIRED, example = "MTIzfDQ1Nnw3ODk=")
    @NotBlank(message = "签到二维码内容不能为空")
    private String qrContent;

}
