import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace SystemBroadcastApi {
  /** 广播消息详情 */
  export interface Broadcast {
    id?: number;
    title?: string;
    content?: string;
    senderId?: number;
    successCount?: number;
    failCount?: number;
    status?: number;
    createTime?: string;
  }

  /** 发送请求 */
  export interface SendReq {
    title?: string;
    content: string;
  }
}

/** 发送系统广播消息 */
export function sendBroadcast(data: SystemBroadcastApi.SendReq) {
  return requestClient.post<number>('/forum/system-broadcast/send', data);
}

/** 分页查询广播消息历史 */
export function getBroadcastPage(params: PageParam) {
  return requestClient.get<PageResult<SystemBroadcastApi.Broadcast>>(
    '/forum/system-broadcast/page',
    { params },
  );
}

/** 获取广播消息详情 */
export function getBroadcast(id: number) {
  return requestClient.get<SystemBroadcastApi.Broadcast>(
    `/forum/system-broadcast/get?id=${id}`,
  );
}
