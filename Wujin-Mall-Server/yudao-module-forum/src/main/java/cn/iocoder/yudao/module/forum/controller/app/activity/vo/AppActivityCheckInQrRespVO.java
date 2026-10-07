package cn.iocoder.yudao.module.forum.controller.app.activity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 活动签到二维码响应 VO
 */
@Schema(description = "用户 APP - 活动签到二维码响应 VO")
@Data
public class AppActivityCheckInQrRespVO {

    @Schema(description = "签到二维码内容（Base64 编码）", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "MTIzfDQ1Nnw3ODk=")
    private String qrContent;

}
