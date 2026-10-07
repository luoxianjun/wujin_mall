package cn.iocoder.yudao.module.forum.controller.admin.activity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 活动精简信息 VO（用于下拉列表）
 */
@Schema(description = "管理后台 - 活动精简信息 VO")
@Data
public class AdminActivitySimpleRespVO {

    @Schema(description = "活动ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "活动标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "校园抽奖")
    private String title;

}
