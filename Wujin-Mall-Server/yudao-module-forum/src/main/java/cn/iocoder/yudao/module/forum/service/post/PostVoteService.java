package cn.iocoder.yudao.module.forum.service.post;

import cn.iocoder.yudao.module.forum.dal.dataobject.post.PostVoteDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.PostVoteOptionDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.PostVoteRecordDO;

import java.util.List;
import java.util.Map;

/**
 * 帖子内嵌投票 Service
 */
public interface PostVoteService {

    /**
     * 创建帖子投票（发帖时调用）
     */
    void createPostVote(Long postId, PostVoteDO vote, List<PostVoteOptionDO> options);

    /**
     * 获取帖子投票
     */
    PostVoteDO getPostVote(Long postId);

    /**
     * 获取投票选项列表
     */
    List<PostVoteOptionDO> getVoteOptions(Long voteId);

    /**
     * 投票
     */
    void castPostVote(Long voteId, Long userId, List<Long> optionIds);

    /**
     * 获取用户投票记录
     */
    List<PostVoteRecordDO> getUserVoteRecord(Long voteId, Long userId);

    /**
     * 获取投票结果（选项ID -> 票数）
     */
    Map<Long, Long> getVoteCounts(Long voteId);

    /**
     * 获取参与人数
     */
    Long getVoterCount(Long voteId);

    /**
     * 删除帖子投票（帖子删除时级联删除）
     */
    void deleteByPostId(Long postId);
}
