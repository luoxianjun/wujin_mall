import type { PageParam } from '@vben/request';

import { requestClient } from '#/api/request';

/** 查询用户管理列表 */
export function getPage(params: PageParam) {
  return requestClient.get('/matrix/acct/page', { params });
}

/** 查询用户详情 */
export function getAcct(id: number) {
  return requestClient.get(`/matrix/acct/get?id=${id}`);
}

/** 删除用户 */
export function deleteAcct(id: number) {
  return requestClient.delete(`/matrix/acct/delete?id=${id}`);
}
