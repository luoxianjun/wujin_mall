package cn.iocoder.yudao.module.gamification.controller.admin.stats.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 游戏化综合统计 Response VO")
@Data
public class GamificationStatsRespVO {

    // === 邀请统计 ===
    @Schema(description = "总邀请数")
    private Long totalInvitations;
    @Schema(description = "成功邀请数")
    private Long successInvitations;
    @Schema(description = "邀请积分发放总量")
    private Long invitationPointsTotal;

    // === 答题统计 ===
    @Schema(description = "答题活动数")
    private Long totalQuizActivities;
    @Schema(description = "总答题人次")
    private Long totalQuizParticipants;
    @Schema(description = "答题积分发放总量")
    private Long quizPointsTotal;

    // === 抽奖统计 ===
    @Schema(description = "抽奖活动数")
    private Long totalLotteryActivities;
    @Schema(description = "总抽奖次数")
    private Long totalLotteryDraws;
    @Schema(description = "中奖次数")
    private Long totalLotteryWins;

    // === 投票统计 ===
    @Schema(description = "投票活动数")
    private Long totalVoteActivities;
    @Schema(description = "总投票人次")
    private Long totalVoteParticipants;

    // === 积分汇总 ===
    @Schema(description = "积分发放总量（所有模块）")
    private Long totalPointsGranted;
    @Schema(description = "今日积分发放")
    private Long todayPointsGranted;
}
