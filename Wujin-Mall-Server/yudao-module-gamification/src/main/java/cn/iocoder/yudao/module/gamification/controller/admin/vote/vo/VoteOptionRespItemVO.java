package cn.iocoder.yudao.module.gamification.controller.admin.vote.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "投票选项响应")
@Data
public class VoteOptionRespItemVO {

    @Schema(description = "选项ID")
    private Long id;

    @Schema(description = "选项标题")
    private String title;

    @Schema(description = "选项配图URL")
    private String imageUrl;

    @Schema(description = "选项描述")
    private String description;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "是否用户添加")
    private Boolean addedByUser;

    @Schema(description = "审核状态：0=待审核, 1=通过, 2=拒绝")
    private Integer auditStatus;

    @Schema(description = "票数")
    private Long voteCount;
}
