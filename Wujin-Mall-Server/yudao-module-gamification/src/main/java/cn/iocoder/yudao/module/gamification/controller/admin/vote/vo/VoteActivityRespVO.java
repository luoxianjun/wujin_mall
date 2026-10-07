package cn.iocoder.yudao.module.gamification.controller.admin.vote.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Admin vote activity response")
@Data
public class VoteActivityRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "关联论坛活动ID")
    private Long activityId;

    @Schema(description = "活动名称")
    private String activityName;

    @Schema(description = "投票类型：0=单选, 1=多选, 2=排序")
    private Integer voteType;

    @Schema(description = "最多可选数量")
    private Integer maxChoices;

    @Schema(description = "是否匿名投票")
    private Boolean anonymous;

    @Schema(description = "是否显示实时结果")
    private Boolean showRealtimeResult;

    @Schema(description = "是否允许用户添加选项")
    private Boolean allowUserAddOption;

    @Schema(description = "是否要求实名用户参与")
    private Boolean requireRealName;

    @Schema(description = "选项数量上限")
    private Integer maxOptions;

    @Schema(description = "投票截止时间")
    private LocalDateTime endTime;

    @Schema(description = "最低参与人数")
    private Integer minParticipants;

    @Schema(description = "是否允许评论")
    private Boolean allowComment;

    @Schema(description = "每人票数")
    private Integer votesPerUser;

    @Schema(description = "状态：0=启用, 1=禁用")
    private Integer status;

    @Schema(description = "参与人数")
    private Long voterCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "投票选项列表")
    private List<VoteOptionRespItemVO> options;
}
