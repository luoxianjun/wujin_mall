import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ForumSignRecordApi {
  export interface SignRecord {
    id?: number;
    userId?: number;
    nickname?: string;
    uid?: string;
    signDate?: string;
    continuousDays?: number;
    point?: number;
    remark?: string;
    createTime?: string;
  }

  export interface SignRecordPageReq extends PageParam {
    userId?: number;
    uid?: string;
    startDate?: string;
    endDate?: string;
  }
}

/** 分页查询论坛签到记录 */
export function getForumSignRecordPage(
  params: ForumSignRecordApi.SignRecordPageReq,
) {
  return requestClient.get<PageResult<ForumSignRecordApi.SignRecord>>(
    '/forum/sign/record/page',
    { params },
  );
}
