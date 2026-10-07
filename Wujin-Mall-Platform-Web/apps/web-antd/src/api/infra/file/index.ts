import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

/** Axios 上传进度事件回调类型 */
export type AxiosProgressEvent = (progressEvent: {
  loaded: number;
  total?: number;
  progress?: number;
}) => void;

export namespace InfraFileApi {
  /** 文件信息 */
  export interface File {
    id?: number;
    configId?: number;
    name?: string;
    path?: string;
    url?: string;
    type?: string;
    size?: number;
    createTime?: Date;
  }

  /** 文件预签名 URL 响应 */
  export interface FilePresignedUrlResp {
    configId: number;
    uploadUrl: string;
    url: string;
    path: string;
  }

  /** 上传文件参数 */
  export interface UploadFileParams {
    file: File;
    directory?: string;
  }
}

/** 查询文件列表 */
export function getFilePage(params: PageParam) {
  return requestClient.get<PageResult<InfraFileApi.File>>('/infra/file/page', {
    params,
  });
}

/** 删除文件 */
export function deleteFile(id: number) {
  return requestClient.delete(`/infra/file/delete?id=${id}`);
}

/** 批量删除文件 */
export function deleteFileList(ids: number[]) {
  return requestClient.delete(`/infra/file/delete-list?ids=${ids.join(',')}`);
}

/** 获取文件预签名 URL */
export function getFilePresignedUrl(
  fileName: string,
  directory?: string,
): Promise<InfraFileApi.FilePresignedUrlResp> {
  return requestClient.get<InfraFileApi.FilePresignedUrlResp>(
    '/infra/file/presigned-url',
    {
      params: {
        fileName,
        directory,
      },
    },
  );
}

/** 创建文件记录 */
export function createFile(
  data: InfraFileApi.File,
): Promise<InfraFileApi.File> {
  return requestClient.post<InfraFileApi.File>('/infra/file/create', data);
}

/** 上传文件到服务器 */
export function uploadFile(
  params: { file: globalThis.File; directory?: string },
  onUploadProgress?: AxiosProgressEvent,
): Promise<{ url: string }> {
  const formData = new FormData();
  formData.append('file', params.file);
  if (params.directory) {
    formData.append('directory', params.directory);
  }
  return requestClient.post<{ url: string }>('/infra/file/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    onUploadProgress,
    timeout: 60_000, // 设置上传超时时间为60秒
  });
}
