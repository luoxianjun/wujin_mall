package cn.iocoder.yudao.module.forum.controller.admin.point.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 积分调整请求 VO")
@Data
public class AdminPointChangeReqVO {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "调整积分（正数增加，负数减少）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "调整积分不能为空")
    private Integer point;

    @Schema(description = "调整原因", example = "活动奖励")
    private String reason;

}
