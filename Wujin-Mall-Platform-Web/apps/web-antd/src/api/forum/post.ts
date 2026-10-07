import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ForumPostApi {
  export interface Post {
    id?: number;
    userId?: number;
    uid?: string;
    nickname?: string;
    avatar?: string;
    title?: string;
    content?: string;
    category?: number;
    categoryName?: string;
    categories?: number[];
    imageUrls?: string[];
    anonymous?: boolean;
    schoolOnly?: boolean;
    status?: number;
    reviewResult?: string;
    isTop?: boolean;
    likeCount?: number;
    commentCount?: number;
    followCount?: number;
    viewCount?: number;
    latestCommentTime?: string;
    createTime?: string;
    liked?: boolean;
    followed?: boolean;
    isAdminPost?: boolean;
  }

  export interface PostPageReq extends PageParam {
    category?: number;
    status?: number;
    userId?: number;
    keyword?: string;
    orderBy?: number;
  }
}

/** 帖子分页 */
export function getPostPage(params: ForumPostApi.PostPageReq) {
  return requestClient.get<PageResult<ForumPostApi.Post>>(
    '/forum/post/page',
    { params },
  );
}

/** 帖子详情 */
export function getPost(id: number) {
  return requestClient.get<ForumPostApi.Post>(`/forum/post/get?id=${id}`);
}

/** 删除帖子 */
export function deletePost(id: number) {
  return requestClient.delete(`/forum/post/delete?id=${id}`);
}

/** 帖子复审（通过/不通过） */
export function reviewPost(id: number, approve: boolean, reviewRemark?: string) {
  return requestClient.post('/forum/post/review', { id, approve, reviewRemark });
}

/** 设置帖子置顶状态 */
export function setPostTop(id: number, isTop: boolean) {
  return requestClient.post(`/forum/post/set-top?id=${id}&isTop=${isTop}`);
}
