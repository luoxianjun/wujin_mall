package cn.iocoder.yudao.module.gamification.controller.admin.vote.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Schema(description = "投票选项")
@Data
public class VoteOptionItemVO {

    @Schema(description = "选项ID（更新时传入）", example = "1")
    private Long id;

    @Schema(description = "选项标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "选项A")
    @NotBlank(message = "选项标题不能为空")
    private String title;

    @Schema(description = "选项配图URL", example = "https://example.com/image.png")
    private String imageUrl;

    @Schema(description = "选项描述", example = "这是选项A的描述")
    private String description;

    @Schema(description = "排序", example = "0")
    private Integer sortOrder;
}
