import { requestClient } from '#/api/request';

export namespace ForumUserProfileApi {
  export interface AdminUserProfileRespVO {
    userId: number;
    uid: string;
    nickname: string;
    avatar?: string;
    mobile?: string;
    campus?: string;
    point?: number;
    postCount?: number;
    activityCount?: number;
    isAdmin?: boolean;
    schoolEmailVerified?: boolean;
    createTime?: string;
  }

  export interface AdminUserProfilePageReqVO {
    pageNo: number;
    pageSize: number;
    nickname?: string;
    uid?: string;
    userId?: number;
    isAdmin?: boolean;
  }

  export interface AdminSetUserAdminReqVO {
    userId: number;
    isAdmin: boolean;
  }
}

/** 获取论坛用户分页列表 */
export function getForumUserProfilePage(params: ForumUserProfileApi.AdminUserProfilePageReqVO) {
  return requestClient.get<{
    list: ForumUserProfileApi.AdminUserProfileRespVO[];
    total: number;
  }>('/forum/admin/user-profile/page', { params });
}

/** 根据用户ID获取用户资料 */
export function getForumUserProfileByUserId(userId: number) {
  return requestClient.get<ForumUserProfileApi.AdminUserProfileRespVO>(
    `/forum/admin/user-profile/get?userId=${userId}`,
  );
}

/** 设置用户管理员状态 */
export function setUserAdmin(data: ForumUserProfileApi.AdminSetUserAdminReqVO) {
  return requestClient.put<boolean>('/forum/admin/user-profile/set-admin', data);
}
