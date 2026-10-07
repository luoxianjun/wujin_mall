package cn.iocoder.yudao.module.forum.service.search;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.forum.controller.app.post.vo.AppPostRespVO;
import cn.iocoder.yudao.module.forum.controller.app.search.vo.AppPostSearchReqVO;

public interface ForumSearchService {

    /**
     * 搜索帖子
     *
     * @param reqVO  搜索请求
     * @param userId 当前登录用户 ID
     * @return 帖子列表
     */
    PageResult<AppPostRespVO> searchPosts(AppPostSearchReqVO reqVO, Long userId);

    /**
     * 重建 ES 索引：删除旧索引，重新创建 mapping，将 DB 中所有帖子同步到 ES
     *
     * @return 同步的帖子数量
     */
    int reindexAllPosts();

}
