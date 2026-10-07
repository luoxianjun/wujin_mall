package cn.iocoder.yudao.module.forum.dal.mysql.post;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.ForumPostLikeDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 论坛帖子点赞 Mapper
 *
 * @author forum
 */
@Mapper
public interface ForumPostLikeMapper extends BaseMapperX<ForumPostLikeDO> {

    /**
     * 查询用户是否点赞了帖子
     */
    default ForumPostLikeDO selectByPostIdAndUserId(Long postId, Long userId) {
        return selectOne(new LambdaQueryWrapperX<ForumPostLikeDO>()
                .eq(ForumPostLikeDO::getPostId, postId)
                .eq(ForumPostLikeDO::getUserId, userId));
    }

    /**
     * 查询用户是否点赞了帖子（包含已逻辑删除的数据）
     */
    @Select("SELECT * FROM forum_post_like WHERE post_id = #{postId} AND user_id = #{userId} LIMIT 1")
    ForumPostLikeDO selectByPostIdAndUserIdIncludeDeleted(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * 删除点赞记录
     */
    default int deleteByPostIdAndUserId(Long postId, Long userId) {
        return delete(new LambdaQueryWrapperX<ForumPostLikeDO>()
                .eq(ForumPostLikeDO::getPostId, postId)
                .eq(ForumPostLikeDO::getUserId, userId));
    }

    /**
     * 恢复已逻辑删除的点赞记录
     */
    @Update("UPDATE forum_post_like SET deleted = 0, update_time = NOW() WHERE post_id = #{postId} AND user_id = #{userId}")
    int recoverByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * 查询点赞某帖子的用户 ID
     */
    @Select("SELECT user_id FROM forum_post_like WHERE post_id = #{postId} AND deleted = 0")
    List<Long> selectUserIdsByPostId(@Param("postId") Long postId);

    /**
     * 分页查询用户点赞的帖子 ID
     */
    default List<Long> selectPostIdsByUserIdPage(Long userId, int offset, int limit) {
        List<ForumPostLikeDO> likes = selectList(new LambdaQueryWrapperX<ForumPostLikeDO>()
                .eq(ForumPostLikeDO::getUserId, userId)
                .orderByDesc(ForumPostLikeDO::getCreateTime)
                .last("LIMIT " + offset + ", " + limit));
        return likes.stream().map(ForumPostLikeDO::getPostId).collect(java.util.stream.Collectors.toList());
    }

    /**
     * 统计用户点赞的帖子总数
     */
    default Long countByUserId(Long userId) {
        return selectCount(new LambdaQueryWrapperX<ForumPostLikeDO>()
                .eq(ForumPostLikeDO::getUserId, userId));
    }

}
