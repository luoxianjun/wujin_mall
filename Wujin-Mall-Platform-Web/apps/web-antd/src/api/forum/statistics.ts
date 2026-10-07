import { requestClient } from '#/api/request';

export namespace ForumStatisticsApi {
  export interface Summary {
    todayMemberCount: number;
    totalMemberCount: number;
    todayPostCount: number;
    totalPostCount: number;
    todayActivityCount: number;
    totalActivityCount: number;
    todayInteractionCount: number;
    totalInteractionCount: number;
  }

  export interface Trend {
    dates: string[];
    memberIncrements: number[];
    postIncrements: number[];
  }
}

/** 获取论坛统计：会员、帖子、活动与交互 */
export function getForumStatisticsSummary() {
  return requestClient.get<ForumStatisticsApi.Summary>(
    '/forum/statistics/summary',
  );
}

/** 获取近30天的会员、帖子增量趋势 */
export function getForumStatisticsTrend() {
  return requestClient.get<ForumStatisticsApi.Trend>('/forum/statistics/trend');
}
