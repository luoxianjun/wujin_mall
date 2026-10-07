package cn.iocoder.yudao.module.gamification.controller.admin.vote.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Admin vote activity save request")
@Data
public class VoteActivitySaveReqVO {

    @Schema(description = "主键（更新时传入）", example = "1")
    private Long id;

    @Schema(description = "关联论坛活动ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    @NotNull(message = "activityId不能为空")
    private Long activityId;

    @Schema(description = "投票类型：0=单选, 1=多选, 2=排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "voteType不能为空")
    private Integer voteType;

    @Schema(description = "最多可选数量（多选时使用）", example = "3")
    private Integer maxChoices;

    @Schema(description = "是否匿名投票", example = "true")
    private Boolean anonymous;

    @Schema(description = "是否显示实时结果", example = "true")
    private Boolean showRealtimeResult;

    @Schema(description = "是否允许用户添加选项", example = "false")
    private Boolean allowUserAddOption;

    @Schema(description = "是否要求实名用户参与", example = "false")
    private Boolean requireRealName;

    @Schema(description = "选项数量上限", example = "10")
    private Integer maxOptions;

    @Schema(description = "投票截止时间")
    private LocalDateTime endTime;

    @Schema(description = "最低参与人数", example = "0")
    private Integer minParticipants;

    @Schema(description = "是否允许评论", example = "true")
    private Boolean allowComment;

    @Schema(description = "每人票数", example = "1")
    private Integer votesPerUser;

    @Schema(description = "状态：0=启用, 1=禁用", example = "0")
    private Integer status;

    @Schema(description = "投票选项列表")
    @Valid
    private List<VoteOptionItemVO> options;
}
