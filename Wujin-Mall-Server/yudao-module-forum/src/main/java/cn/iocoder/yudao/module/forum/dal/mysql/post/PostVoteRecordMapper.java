package cn.iocoder.yudao.module.forum.dal.mysql.post;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.PostVoteRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PostVoteRecordMapper extends BaseMapperX<PostVoteRecordDO> {

    default List<PostVoteRecordDO> selectByVoteIdAndUserId(Long voteId, Long userId) {
        return selectList(new LambdaQueryWrapperX<PostVoteRecordDO>()
                .eq(PostVoteRecordDO::getVoteId, voteId)
                .eq(PostVoteRecordDO::getUserId, userId));
    }

    @Select("SELECT option_id, COUNT(*) AS cnt FROM forum_post_vote_record " +
            "WHERE vote_id = #{voteId} AND deleted = 0 GROUP BY option_id")
    List<java.util.Map<String, Object>> selectVoteCountsByVoteId(@Param("voteId") Long voteId);

    default void deleteByVoteId(Long voteId) {
        delete(new LambdaQueryWrapperX<PostVoteRecordDO>()
                .eq(PostVoteRecordDO::getVoteId, voteId));
    }

    @Select("SELECT COUNT(DISTINCT user_id) FROM forum_post_vote_record " +
            "WHERE vote_id = #{voteId} AND deleted = 0")
    Long selectVoterCount(@Param("voteId") Long voteId);
}
