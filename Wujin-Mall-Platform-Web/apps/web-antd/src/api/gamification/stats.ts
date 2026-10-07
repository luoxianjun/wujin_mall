import { requestClient } from '#/api/request';

export namespace GamificationStatsApi {
  export interface StatsOverview {
    totalInvitations: number;
    successInvitations: number;
    invitationPointsTotal: number;
    totalQuizActivities: number;
    totalQuizParticipants: number;
    quizPointsTotal: number;
    totalLotteryActivities: number;
    totalLotteryDraws: number;
    totalLotteryWins: number;
    totalVoteActivities: number;
    totalVoteParticipants: number;
    totalPointsGranted: number;
    todayPointsGranted: number;
  }
}

/** 获取游戏化综合统计 */
export function getGamificationStats() {
  return requestClient.get<GamificationStatsApi.StatsOverview>(
    '/gamification/stats/overview',
  );
}
