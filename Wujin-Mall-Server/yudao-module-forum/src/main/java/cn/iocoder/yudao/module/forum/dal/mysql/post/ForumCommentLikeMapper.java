package cn.iocoder.yudao.module.forum.dal.mysql.post;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.ForumCommentLikeDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 论坛评论点赞 Mapper
 *
 * @author forum
 */
@Mapper
public interface ForumCommentLikeMapper extends BaseMapperX<ForumCommentLikeDO> {

    /**
     * 查询用户是否点赞了评论
     */
    default ForumCommentLikeDO selectByCommentIdAndUserId(Long commentId, Long userId) {
        return selectOne(new LambdaQueryWrapperX<ForumCommentLikeDO>()
                .eq(ForumCommentLikeDO::getCommentId, commentId)
                .eq(ForumCommentLikeDO::getUserId, userId));
    }

    /**
     * 查询用户是否点赞了评论（包含已逻辑删除的数据）
     */
    @Select("SELECT * FROM forum_comment_like WHERE comment_id = #{commentId} AND user_id = #{userId} LIMIT 1")
    ForumCommentLikeDO selectByCommentIdAndUserIdIncludeDeleted(@Param("commentId") Long commentId, @Param("userId") Long userId);

    /**
     * 删除点赞记录
     */
    default int deleteByCommentIdAndUserId(Long commentId, Long userId) {
        return delete(new LambdaQueryWrapperX<ForumCommentLikeDO>()
                .eq(ForumCommentLikeDO::getCommentId, commentId)
                .eq(ForumCommentLikeDO::getUserId, userId));
    }

    /**
     * 恢复已逻辑删除的点赞记录
     */
    @Update("UPDATE forum_comment_like SET deleted = 0, update_time = NOW() WHERE comment_id = #{commentId} AND user_id = #{userId}")
    int recoverByCommentIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);

}
