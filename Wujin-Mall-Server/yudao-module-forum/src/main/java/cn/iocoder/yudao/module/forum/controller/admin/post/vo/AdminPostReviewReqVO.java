package cn.iocoder.yudao.module.forum.controller.admin.post.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 帖子复审请求 VO")
@Data
public class AdminPostReviewReqVO {

    @Schema(description = "帖子ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "帖子ID不能为空")
    private Long id;

    @Schema(description = "是否通过", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "审核结果不能为空")
    private Boolean approve;

    @Schema(description = "审核结论（审核不通过时必填）", example = "内容不符合社区规范")
    private String reviewRemark;

}

