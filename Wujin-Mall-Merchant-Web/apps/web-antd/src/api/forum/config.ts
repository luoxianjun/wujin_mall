import { requestClient } from '#/api/request';

export namespace ForumConfigApi {
  /** 配置详情 */
  export interface Config {
    id?: number;
    configKey?: string;
    configValue?: string;
    name?: string;
    type?: string;
    remark?: string;
    createTime?: string;
    updateTime?: string;
  }

  /** 配置保存请求 */
  export interface ConfigSaveReq {
    key: string;
    value: string;
    name?: string;
    type?: string;
    remark?: string;
  }
}

/** 获取所有配置列表 */
export function getConfigList() {
  return requestClient.get<ForumConfigApi.Config[]>('/forum/config/list');
}

/** 根据配置键获取配置 */
export function getConfig(key: string) {
  return requestClient.get<ForumConfigApi.Config>('/forum/config/get', {
    params: { key },
  });
}

/** 保存配置（新增或更新） */
export function saveConfig(data: ForumConfigApi.ConfigSaveReq) {
  return requestClient.post<number>('/forum/config/save', data);
}

/** 删除配置 */
export function deleteConfig(key: string) {
  return requestClient.delete(`/forum/config/delete?key=${key}`);
}
