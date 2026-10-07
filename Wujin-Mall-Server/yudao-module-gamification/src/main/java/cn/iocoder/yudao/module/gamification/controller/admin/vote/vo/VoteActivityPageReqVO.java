package cn.iocoder.yudao.module.gamification.controller.admin.vote.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "Admin vote activity page request")
@Data
@EqualsAndHashCode(callSuper = true)
public class VoteActivityPageReqVO extends PageParam {

    @Schema(description = "关联论坛活动ID", example = "1001")
    private Long activityId;

    @Schema(description = "投票类型：0=单选, 1=多选, 2=排序", example = "0")
    private Integer voteType;

    @Schema(description = "状态：0=启用, 1=禁用", example = "0")
    private Integer status;
}
