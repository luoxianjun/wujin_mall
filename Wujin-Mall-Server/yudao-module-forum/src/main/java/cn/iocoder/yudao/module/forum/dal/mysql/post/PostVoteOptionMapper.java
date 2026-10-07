package cn.iocoder.yudao.module.forum.dal.mysql.post;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.PostVoteOptionDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PostVoteOptionMapper extends BaseMapperX<PostVoteOptionDO> {

    default List<PostVoteOptionDO> selectByVoteId(Long voteId) {
        return selectList(new LambdaQueryWrapperX<PostVoteOptionDO>()
                .eq(PostVoteOptionDO::getVoteId, voteId)
                .orderByAsc(PostVoteOptionDO::getSortOrder));
    }

    default void deleteByVoteId(Long voteId) {
        delete(new LambdaQueryWrapperX<PostVoteOptionDO>()
                .eq(PostVoteOptionDO::getVoteId, voteId));
    }
}
