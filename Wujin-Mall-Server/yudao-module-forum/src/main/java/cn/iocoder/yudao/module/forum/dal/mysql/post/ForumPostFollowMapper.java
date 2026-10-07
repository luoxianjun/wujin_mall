package cn.iocoder.yudao.module.forum.dal.mysql.post;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.ForumPostFollowDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 论坛帖子蹲后续 Mapper
 *
 * @author forum
 */
@Mapper
public interface ForumPostFollowMapper extends BaseMapperX<ForumPostFollowDO> {

    /**
     * 查询用户是否关注了帖子
     */
    default ForumPostFollowDO selectByPostIdAndUserId(Long postId, Long userId) {
        return selectOne(new LambdaQueryWrapperX<ForumPostFollowDO>()
                .eq(ForumPostFollowDO::getPostId, postId)
                .eq(ForumPostFollowDO::getUserId, userId));
    }

    /**
     * 查询用户是否关注了帖子（包含已逻辑删除的数据）
     */
    @Select("SELECT * FROM forum_post_follow WHERE post_id = #{postId} AND user_id = #{userId} LIMIT 1")
    ForumPostFollowDO selectByPostIdAndUserIdIncludeDeleted(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * 删除关注记录
     */
    default int deleteByPostIdAndUserId(Long postId, Long userId) {
        return delete(new LambdaQueryWrapperX<ForumPostFollowDO>()
                .eq(ForumPostFollowDO::getPostId, postId)
                .eq(ForumPostFollowDO::getUserId, userId));
    }

    /**
     * 恢复已逻辑删除的关注记录
     */
    @Update("UPDATE forum_post_follow SET deleted = 0, update_time = NOW() WHERE post_id = #{postId} AND user_id = #{userId}")
    int recoverByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * 查询关注某帖子的用户 ID
     */
    @Select("SELECT user_id FROM forum_post_follow WHERE post_id = #{postId} AND deleted = 0")
    List<Long> selectUserIdsByPostId(@Param("postId") Long postId);

    /**
     * 查询用户收藏的帖子 ID 列表
     */
    @Select("SELECT post_id FROM forum_post_follow WHERE user_id = #{userId} AND deleted = 0 ORDER BY create_time DESC")
    List<Long> selectPostIdsByUserId(@Param("userId") Long userId);

}
