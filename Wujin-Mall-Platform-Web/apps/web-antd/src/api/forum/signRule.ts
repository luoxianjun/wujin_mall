import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ForumSignRuleApi {
  export interface SignRule {
    id?: number;
    periodType?: number; // 1=周，2=月
    minDays?: number;
    maxDays?: number;
    points?: number;
    createTime?: string;
  }

  export interface SignRulePageReq extends PageParam {
    periodType?: number;
  }
}

export function getSignRule(id: number) {
  return requestClient.get<ForumSignRuleApi.SignRule>(
    `/forum/sign/rule/get?id=${id}`,
  );
}

export function createSignRule(data: ForumSignRuleApi.SignRule) {
  return requestClient.post('/forum/sign/rule/create', data);
}

export function updateSignRule(data: ForumSignRuleApi.SignRule) {
  return requestClient.put('/forum/sign/rule/update', data);
}

export function deleteSignRule(id: number) {
  return requestClient.delete(`/forum/sign/rule/delete?id=${id}`);
}

/** 查询签到规则列表（不分页） */
export function getSignRuleList(params?: ForumSignRuleApi.SignRulePageReq) {
  return requestClient.get<ForumSignRuleApi.SignRule[]>(
    '/forum/sign/rule/list',
    { params },
  );
}
