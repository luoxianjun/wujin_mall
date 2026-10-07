package cn.iocoder.yudao.module.forum.controller.admin.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 论坛统计响应 VO")
@Data
public class AdminForumStatisticsRespVO {

    @Schema(description = "今日新增会员数", example = "10")
    private Long todayMemberCount;

    @Schema(description = "总会员数", example = "200")
    private Long totalMemberCount;

    @Schema(description = "今日新增帖子数", example = "5")
    private Long todayPostCount;

    @Schema(description = "总帖子数", example = "300")
    private Long totalPostCount;

    @Schema(description = "今日新增活动数", example = "2")
    private Long todayActivityCount;

    @Schema(description = "总活动数", example = "50")
    private Long totalActivityCount;

    @Schema(description = "今日新增交互数（浏览帖子+评论帖子）", example = "120")
    private Long todayInteractionCount;

    @Schema(description = "总交互数（浏览帖子+评论帖子）", example = "8000")
    private Long totalInteractionCount;

}
