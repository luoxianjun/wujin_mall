package cn.iocoder.yudao.module.forum.dal.mysql.post;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.forum.controller.app.post.vo.AppCommentPageReqVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.ForumCommentDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 论坛评论 Mapper
 */
@Mapper
public interface ForumCommentMapper extends BaseMapperX<ForumCommentDO> {

    /**
     * 查询帖子的评论列表
     */
    default List<ForumCommentDO> selectListByPostId(Long postId) {
        return selectList(new LambdaQueryWrapperX<ForumCommentDO>()
                .eq(ForumCommentDO::getPostId, postId)
                .eq(ForumCommentDO::getStatus, 0) // 正常状态
                .orderByAsc(ForumCommentDO::getId));
    }

    /**
     * 分页查询评论
     * 
     * 注意：匿名过滤逻辑
     * - 如果查询的是他人主页（targetUserId != null && targetUserId != currentUserId），则过滤掉匿名评论
     * - 如果查询的是自己的主页或浏览所有评论，不过滤匿名评论
     */
    default PageResult<ForumCommentDO> selectPage(AppCommentPageReqVO reqVO, Long currentUserId) {
        LambdaQueryWrapperX<ForumCommentDO> wrapper = new LambdaQueryWrapperX<ForumCommentDO>()
                .eqIfPresent(ForumCommentDO::getPostId, reqVO.getPostId())
                .eqIfPresent(ForumCommentDO::getRootId, reqVO.getRootId())
                .eqIfPresent(ForumCommentDO::getUserId, reqVO.getUserId())
                .eq(ForumCommentDO::getStatus, 0); // 正常状态
        
        // 如果查询的是他人主页，过滤掉匿名评论
        if (reqVO.getUserId() != null && currentUserId != null && !reqVO.getUserId().equals(currentUserId)) {
            // 查看他人主页时，排除匿名评论
            wrapper.eq(ForumCommentDO::getAnonymous, false);
        }
        
        wrapper.orderByDesc(ForumCommentDO::getCreateTime); // 按时间倒序，最新的在前面
        
        return selectPage(reqVO, wrapper);
    }

    /**
     * 查询在指定帖子下评论过的用户 ID
     */
    @Select("SELECT DISTINCT user_id FROM forum_comment WHERE post_id = #{postId} AND status = 0")
    List<Long> selectUserIdsByPostId(@Param("postId") Long postId);

}
