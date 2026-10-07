import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace InvitationApi {
  export interface InvitationConfig {
    id?: number;
    inviterRewardPoints?: number;
    inviteeRewardPoints?: number;
    rewardMode?: 'both' | 'inviter_only';
    maxInvitations?: number;
    enabled?: boolean;
    rewardRetryLimit?: number;
    ipLimit?: number;
    deviceLimit?: number;
  }

  export interface InvitationConfigUpdateReq {
    id: number;
    inviterRewardPoints: number;
    inviteeRewardPoints: number;
    rewardMode: 'both' | 'inviter_only';
    maxInvitations: number;
    enabled: boolean;
    rewardRetryLimit: number;
    ipLimit: number;
    deviceLimit: number;
  }

  export interface InvitationStatistics {
    totalInvitations?: number;
    successfulInvitations?: number;
    pendingInvitations?: number;
    failedInvitations?: number;
    todayInvitations?: number;
    todayRewardPoints?: number;
    totalRewardPoints?: number;
    activeInviters?: number;
    successRate?: number;
  }

  export interface InvitationDetail {
    id?: number;
    inviterId?: number;
    inviterNickname?: string;
    inviteeId?: number;
    inviteeNickname?: string;
    invitationCode?: string;
    status?: number;
    inviterRewardPoints?: number;
    inviteeRewardPoints?: number;
    registerTime?: string;
    verifiedTime?: string;
  }

  export interface InvitationDetailPageReq extends PageParam {
    inviterNickname?: string;
    inviteeNickname?: string;
    status?: number;
    beginTime?: string;
    endTime?: string;
  }
}

export function getInvitationConfig() {
  return requestClient.get<InvitationApi.InvitationConfig>(
    '/gamification/invitation/config/get',
  );
}

export function updateInvitationConfig(
  data: InvitationApi.InvitationConfigUpdateReq,
) {
  return requestClient.put('/gamification/invitation/config/update', data);
}

export function getInvitationStatistics() {
  return requestClient.get<InvitationApi.InvitationStatistics>(
    '/gamification/invitation/statistics/get',
  );
}

export function getInvitationDetailPage(
  params: InvitationApi.InvitationDetailPageReq,
) {
  return requestClient.get<PageResult<InvitationApi.InvitationDetail>>(
    '/gamification/invitation/detail/page',
    { params },
  );
}
