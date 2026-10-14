import { requestClient } from '#/api/request';

/**
 * 五金三泳道类目（平台后台与商家后台共用同一套接口与页面）
 */
export namespace WujinCategoryApi {
  export type Lane = 'MATERIAL' | 'PROCESS' | 'PRODUCT';

  export interface Category {
    id?: number;
    parentId?: number;
    lane?: Lane;
    code?: string;
    name?: string;
    /** 1 大行业，2 一级分类，3 二级分类，4 三级分类；由后端按父级推导 */
    level?: number;
    sort?: number;
    status?: number;
    displayDepth?: number;
    healthStatus?: string;
    description?: string;
    createTime?: number | string;
  }

  export interface SortRequest {
    parentId: number;
    ids: number[];
  }
}

export function getWujinCategoryList(
  params?: Partial<WujinCategoryApi.Category>,
) {
  return requestClient.get<WujinCategoryApi.Category[]>(
    '/wujin/category/list',
    { params },
  );
}

export function createWujinCategory(data: WujinCategoryApi.Category) {
  return requestClient.post<number>('/wujin/category/create', data);
}

export function updateWujinCategory(data: WujinCategoryApi.Category) {
  return requestClient.put<boolean>('/wujin/category/update', data);
}

/** cascade 为 true 时同时删除全部子级，返回删除的类目数量 */
export function deleteWujinCategory(id: number, cascade = false) {
  return requestClient.delete<number>('/wujin/category/delete', {
    params: { cascade, id },
  });
}

export function updateWujinCategorySort(data: WujinCategoryApi.SortRequest) {
  return requestClient.put<boolean>('/wujin/category/update-sort', data);
}
