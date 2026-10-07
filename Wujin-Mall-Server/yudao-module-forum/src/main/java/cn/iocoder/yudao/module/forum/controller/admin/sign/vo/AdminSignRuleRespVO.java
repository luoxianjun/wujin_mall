package cn.iocoder.yudao.module.forum.controller.admin.sign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 签到规则返回 VO")
@Data
public class AdminSignRuleRespVO extends AdminSignRuleBaseVO {

    @Schema(description = "规则ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "创建时间", example = "2024-01-01 10:00:00")
    private LocalDateTime createTime;
}

