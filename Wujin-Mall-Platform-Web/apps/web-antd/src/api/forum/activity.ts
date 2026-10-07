import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ForumActivityApi {
  /** 活动详情 */
  export interface Activity {
    id?: number;
    userId?: number;
    uid?: string;
    nickname?: string;
    avatar?: string;
    adminMemberIds?: number[];
    adminMemberNicknames?: string[];
    adminMemberUids?: string[];
    title?: string;
    description?: string;
    coverImage?: string;
    detailImages?: string[];
    category?: number;
    categoryName?: string;
    location?: string;
    longitude?: number;
    latitude?: number;
    startTime?: string;
    endTime?: string;
    signUpStartTime?: string;
    signUpEndTime?: string;
    checkInStartTime?: string;
    checkInEndTime?: string;
    checkInType?: number;
    checkInDistance?: number;
    maxParticipants?: number;
    currentParticipants?: number;
    needApproval?: boolean;
    needPoint?: boolean;
    pointAmount?: number;
    /** 报名要求 */
    requirements?: string;
    /** 是否允许未实名用户报名 */
    allowUnverified?: boolean;
    schoolOnly?: boolean;
    hot?: number;
    status?: number;
    statusName?: string;
    viewCount?: number;
    likeCount?: number;
    signedUp?: boolean;
    signUpStatus?: number;
    checkedIn?: boolean;
    createTime?: string;
    /** 自定义报名字段配置JSON */
    customFields?: string;
    /** 是否显示报名人数 */
    showParticipantCount?: boolean;
    /** 跳转小程序appId */
    redirectAppId?: string;
    /** 跳转小程序页面路径 */
    redirectAppPath?: string;
    /** 跳转小程序名称（按钮显示文案） */
    redirectAppName?: string;
    /** 是否隐藏：true-隐藏，false-展示 */
    hidden?: boolean;
    hasQuiz?: boolean;
    quizActivityId?: number;
    quizStatus?: string;
  }

  export interface ActivityPageReq extends PageParam {
    category?: number;
    status?: number;
    keyword?: string;
  }

  export interface SignUp {
    id?: number;
    activityId?: number;
    activityTitle?: string;
    userId?: number;
    uid?: string;
    nickname?: string;
    avatar?: string;
    remark?: string;
    approvalStatus?: number;
    approvalStatusName?: string;
    approvalRemark?: string;
    checkedIn?: boolean;
    checkInTime?: null | string;
    createTime?: string;
  }

  export interface SignUpPageReq extends PageParam {
    activityId?: number;
    approvalStatus?: number;
  }

  export interface ApproveSignUpReq {
    signUpId: number;
    approvalStatus: number;
    approvalRemark?: string;
  }

  export interface FeedbackSignUpReq {
    signUpId: number;
    feedback: string;
  }
}

/** 创建活动 */
export function createActivity(data: ForumActivityApi.Activity) {
  return requestClient.post('/forum/activity/create', data);
}

/** 编辑活动 */
export function updateActivity(data: ForumActivityApi.Activity) {
  return requestClient.put('/forum/activity/update', data);
}

/** 获取活动详情 */
export function getActivity(id: number) {
  return requestClient.get<ForumActivityApi.Activity>(
    `/forum/activity/get?id=${id}`,
  );
}

/** 分页查询活动 */
export function getActivityPage(params: ForumActivityApi.ActivityPageReq) {
  return requestClient.get<PageResult<ForumActivityApi.Activity>>(
    '/forum/activity/page',
    {
      params,
    },
  );
}

/** 删除活动 */
export function deleteActivity(id: number) {
  return requestClient.delete(`/forum/activity/delete?id=${id}`);
}

/** 隐藏活动 */
export function hideActivity(id: number) {
  return requestClient.post(`/forum/activity/hide?id=${id}`);
}

/** 展示已隐藏的活动 */
export function showActivity(id: number) {
  return requestClient.post(`/forum/activity/show?id=${id}`);
}

/** 分页查询活动报名 */
export function getActivitySignUpPage(params: ForumActivityApi.SignUpPageReq) {
  return requestClient.get<PageResult<ForumActivityApi.SignUp>>(
    '/forum/activity/sign-up/page',
    { params },
  );
}

/** 审核报名 */
export function approveSignUp(data: ForumActivityApi.ApproveSignUpReq) {
  return requestClient.post('/forum/activity/approve-sign-up', data);
}

/** 点评报名记录 */
export function feedbackSignUp(data: ForumActivityApi.FeedbackSignUpReq) {
  return requestClient.post('/forum/activity/sign-up/feedback', data);
}

/** 导出活动报名列表 */
export function exportActivitySignUp(
  params?: Omit<ForumActivityApi.SignUpPageReq, 'pageNo' | 'pageSize'>,
) {
  return requestClient.download('/forum/activity/sign-up/export', {
    params,
  });
}
