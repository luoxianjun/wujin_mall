package cn.iocoder.yudao.module.forum.controller.app.activity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 扫码签到请求 VO
 */
@Schema(description = "用户 APP - 扫码签到请求 VO")
@Data
public class AppActivityScanCheckInReqVO {

    @Schema(description = "签到二维码内容（Base64 编码）", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "MTIzfDQ1Nnw3ODk=")
    @NotBlank(message = "签到二维码内容不能为空")
    private String qrContent;

}
