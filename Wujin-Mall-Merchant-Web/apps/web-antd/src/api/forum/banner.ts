import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ForumBannerApi {
  /** Banner 详情 */
  export interface Banner {
    id?: number;
    title?: string;
    imageUrl?: string;
    targetType?: number;
    targetId?: string;
    targetUrl?: string;
    sort?: number;
    status?: number;
    startTime?: string;
    endTime?: string;
    remark?: string;
    createTime?: string;
  }

  /** Banner 创建请求 */
  export interface BannerCreateReq {
    title: string;
    imageUrl: string;
    targetType: number;
    targetId?: string;
    targetUrl?: string;
    sort?: number;
    status?: number;
    startTime?: string;
    endTime?: string;
    remark?: string;
  }

  /** Banner 更新请求 */
  export interface BannerUpdateReq {
    id: number;
    title?: string;
    imageUrl?: string;
    targetType?: number;
    targetId?: string;
    targetUrl?: string;
    sort?: number;
    status?: number;
    startTime?: string;
    endTime?: string;
    remark?: string;
  }

  /** Banner 分页请求 */
  export interface BannerPageReq extends PageParam {
    title?: string;
    targetType?: number;
    status?: number;
  }
}

/** 创建 Banner */
export function createBanner(data: ForumBannerApi.BannerCreateReq) {
  return requestClient.post<number>('/forum/banner/create', data);
}

/** 更新 Banner */
export function updateBanner(data: ForumBannerApi.BannerUpdateReq) {
  return requestClient.put('/forum/banner/update', data);
}

/** 删除 Banner */
export function deleteBanner(id: number) {
  return requestClient.delete(`/forum/banner/delete?id=${id}`);
}

/** 获取 Banner 详情 */
export function getBanner(id: number) {
  return requestClient.get<ForumBannerApi.Banner>(
    `/forum/banner/get?id=${id}`,
  );
}

/** 分页查询 Banner */
export function getBannerPage(params: ForumBannerApi.BannerPageReq) {
  return requestClient.get<PageResult<ForumBannerApi.Banner>>(
    '/forum/banner/page',
    { params },
  );
}

/** 更新 Banner 状态 */
export function updateBannerStatus(id: number, status: number) {
  return requestClient.put(
    `/forum/banner/update-status?id=${id}&status=${status}`,
  );
}
