package cn.iocoder.yudao.module.forum.dal.mysql.post;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.PostVoteDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostVoteMapper extends BaseMapperX<PostVoteDO> {

    default PostVoteDO selectByPostId(Long postId) {
        return selectOne(PostVoteDO::getPostId, postId);
    }
}
