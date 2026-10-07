import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ForumPointRecordApi {
  export interface PointRecord {
    id?: number;
    userId?: number;
    nickname?: string;
    uid?: string;
    bizId?: string;
    bizType?: number;
    title?: string;
    description?: string;
    point?: number;
    totalPoint?: number;
    createTime?: string;
  }

  export interface PointRecordPageReq extends PageParam {
    userId?: number;
    uid?: string;
    bizType?: number;
    title?: string;
    startDate?: string;
    endDate?: string;
  }

  /** 调整积分请求 */
  export interface PointChangeReq {
    userId: number;
    point: number;
    reason?: string;
  }
}

/** 分页查询积分记录 */
export function getForumPointRecordPage(
  params: ForumPointRecordApi.PointRecordPageReq,
) {
  return requestClient.get<PageResult<ForumPointRecordApi.PointRecord>>(
    '/forum/point/record/page',
    { params },
  );
}

/** 调整用户积分 */
export function changeForumPoint(data: ForumPointRecordApi.PointChangeReq) {
  return requestClient.post('/forum/point/record/change', data);
}

/** 导出积分明细列表 */
export function exportPointRecord(
  params?: Omit<ForumPointRecordApi.PointRecordPageReq, 'pageNo' | 'pageSize'>,
) {
  return requestClient.download('/forum/point/record/export', {
    params,
  });
}
