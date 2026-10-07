package cn.iocoder.yudao.module.forum.service.post;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.forum.controller.app.post.vo.AppPostCreateReqVO;
import cn.iocoder.yudao.module.forum.controller.app.post.vo.AppPostPageReqVO;
import cn.iocoder.yudao.module.forum.controller.app.post.vo.AppPostRespVO;

import javax.validation.Valid;
import java.util.List;

/**
 * 论坛帖子 Service 接口
 *
 * @author forum
 */
public interface ForumPostService {

    /**
     * 创建帖子
     *
     * @param userId 用户ID
     * @param reqVO  创建请求
     * @return 帖子ID
     */
    Long createPost(Long userId, @Valid AppPostCreateReqVO reqVO);

    /**
     * 获取帖子详情
     *
     * @param postId 帖子ID
     * @param userId 当前用户ID（可为空）
     * @return 帖子详情
     */
    AppPostRespVO getPost(Long postId, Long userId);

    /**
     * 分页查询帖子
     *
     * @param reqVO  分页请求
     * @param userId 当前用户ID（可为空）
     * @return 帖子分页
     */
    PageResult<AppPostRespVO> getPostPage(AppPostPageReqVO reqVO, Long userId);

    /**
     * 热点帖子列表
     *
     * @param limit       返回数量
     * @param windowHours 时间窗口（小时），用于控制只统计近 N 小时的帖子；为空则使用默认窗口
     * @param userId      当前用户ID（可为空）
     * @return 热点帖子列表
     */
    List<AppPostRespVO> getHotPosts(Integer limit, Integer windowHours, Long userId);

    /**
     * 删除帖子
     *
     * @param postId 帖子ID
     * @param userId 用户ID
     */
    void deletePost(Long postId, Long userId);

    /**
     * 后台删除帖子（无需本人）
     *
     * @param postId 帖子ID
     * @param userId 操作人ID
     */
    void deletePostByAdmin(Long postId, Long userId);

    /**
     * 后台审核帖子
     *
     * @param postId  帖子ID
     * @param approve 是否通过
     * @param reviewRemark 审核结论（审核不通过时必填）
     * @param userId  审核人ID
     */
    void reviewPost(Long postId, boolean approve, String reviewRemark, Long userId);

    /**
     * 点赞帖子
     *
     * @param postId 帖子ID
     * @param userId 用户ID
     */
    void likePost(Long postId, Long userId);

    /**
     * 取消点赞帖子
     *
     * @param postId 帖子ID
     * @param userId 用户ID
     */
    void unlikePost(Long postId, Long userId);

    /**
     * 关注帖子（蹲后续）
     *
     * @param postId 帖子ID
     * @param userId 用户ID
     */
    void followPost(Long postId, Long userId);

    /**
     * 取消关注帖子
     *
     * @param postId 帖子ID
     * @param userId 用户ID
     */
    void unfollowPost(Long postId, Long userId);

    /**
     * 增加浏览次数
     *
     * @param postId 帖子ID
     */
    void increaseViewCount(Long postId);

    /**
     * 获取用户收藏的帖子列表
     *
     * @param userId 用户ID
     * @return 收藏的帖子列表
     */
    List<AppPostRespVO> getMyFollowedPosts(Long userId);

    /**
     * 分页获取指定用户点赞的帖子列表
     *
     * @param targetUserId  目标用户ID（要查询的用户）
     * @param currentUserId 当前登录用户ID（可为空，用于判断当前用户是否点赞/关注）
     * @param pageNo        页码
     * @param pageSize      每页数量
     * @return 点赞的帖子分页结果
     */
    PageResult<AppPostRespVO> getLikedPostsByUserId(Long targetUserId, Long currentUserId, Integer pageNo, Integer pageSize);

    /**
     * 设置帖子置顶状态（后台管理）
     *
     * @param postId  帖子ID
     * @param isTop   是否置顶
     * @param adminId 操作人ID
     */
    void setPostTop(Long postId, boolean isTop, Long adminId);

}
