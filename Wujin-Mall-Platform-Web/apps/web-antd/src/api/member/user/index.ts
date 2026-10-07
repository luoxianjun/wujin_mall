import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MemberUserApi {
  /** 会员用户信息 */
  export interface User {
    id?: number;
    avatar?: string;
    birthday?: number;
    createTime?: number;
    loginDate?: number;
    loginIp: string;
    mark: string;
    mobile: string;
    name?: string;
    nickname?: string;
    registerIp: string;
    sex: number;
    status: number;
    areaId?: number;
    areaName?: string;
    levelName: null | string;
    point?: null | number;
    totalPoint?: null | number;
    experience?: null | number;
    isAdmin?: boolean; // 是否论坛管理员
    uid?: string; // 论坛UID
    realName?: string; // 真实姓名
    forumPoint?: number; // 论坛积分
    // 学校认证信息（来自论坛资料）
    schoolName?: string; // 学校名称
    schoolEmail?: string; // 学校邮箱
    majorAndGrade?: string; // 专业及年级
    schoolInfoPublic?: boolean; // 是否公开学校信息
    schoolEmailVerified?: boolean; // 是否完成学校邮箱认证
    // 扩展信息（来自论坛资料）
    constellation?: string; // 星座
    mbti?: string; // MBTI 性格类型
    introduction?: string; // 个人介绍
    // 隐私设置（来自论坛资料）
    allowPrivateChat?: boolean; // 是否允许私聊
    allowSystemMessage?: boolean; // 是否接收系统消息
    hideSchoolInfo?: boolean; // 是否隐藏学校信息
    loading?: boolean; // 加载状态
  }

  /** 会员用户等级更新信息 */
  export interface UserLevelUpdate {
    id: number;
    levelId: number;
  }

  /** 会员用户积分更新信息 */
  export interface UserPointUpdate {
    id: number;
    point: number;
  }

  export interface UserPageReq extends PageParam {
    uid?: string;
    mobile?: string;
    nickname?: string;
    loginDate?: string[];
    createTime?: string[];
  }
}

/** 查询会员用户列表 */
export function getUserPage(params: MemberUserApi.UserPageReq) {
  return requestClient.get<PageResult<MemberUserApi.User>>(
    '/member/user/page',
    {
      params,
    },
  );
}

/** 查询会员用户详情 */
export function getUser(id: number) {
  return requestClient.get<MemberUserApi.User>(`/member/user/get?id=${id}`);
}

/** 修改会员用户 */
export function updateUser(data: MemberUserApi.User) {
  return requestClient.put('/member/user/update', data);
}

/** 修改会员用户等级 */
export function updateUserLevel(data: MemberUserApi.UserLevelUpdate) {
  return requestClient.put('/member/user/update-level', data);
}

/** 修改会员用户积分 */
export function updateUserPoint(data: MemberUserApi.UserPointUpdate) {
  return requestClient.put('/member/user/update-point', data);
}

/** 导出会员用户列表 */
export function exportMemberUser(params?: Record<string, any>) {
  return requestClient.download('/member/user/export', {
    params,
  });
}
