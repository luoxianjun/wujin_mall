package cn.iocoder.yudao.module.forum.service.post;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.forum.controller.app.post.vo.AppCommentCreateReqVO;
import cn.iocoder.yudao.module.forum.controller.app.post.vo.AppCommentPageReqVO;
import cn.iocoder.yudao.module.forum.controller.app.post.vo.AppCommentRespVO;

import javax.validation.Valid;
import java.util.List;

/**
 * 论坛评论 Service 接口
 *
 * @author forum
 */
public interface ForumCommentService {

    /**
     * 创建评论
     *
     * @param userId 用户ID
     * @param reqVO 创建请求
     * @return 评论ID
     */
    Long createComment(Long userId, @Valid AppCommentCreateReqVO reqVO);

    /**
     * 分页查询评论
     *
     * @param reqVO 分页请求
     * @param userId 当前用户ID（可为空）
     * @return 评论分页
     */
    PageResult<AppCommentRespVO> getCommentPage(AppCommentPageReqVO reqVO, Long userId);

    /**
     * 查询帖子的评论列表（树形结构）
     *
     * @param postId 帖子ID
     * @param userId 当前用户ID（可为空）
     * @return 评论列表
     */
    List<AppCommentRespVO> getCommentTree(Long postId, Long userId);

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @param userId 用户ID
     */
    void deleteComment(Long commentId, Long userId);

    /**
     * 点赞评论
     *
     * @param commentId 评论ID
     * @param userId 用户ID
     */
    void likeComment(Long commentId, Long userId);

    /**
     * 取消点赞评论
     *
     * @param commentId 评论ID
     * @param userId 用户ID
     */
    void unlikeComment(Long commentId, Long userId);

}

