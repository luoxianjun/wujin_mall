package cn.iocoder.yudao.module.forum.service.post;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.PostVoteDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.PostVoteOptionDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.PostVoteRecordDO;
import cn.iocoder.yudao.module.forum.dal.mysql.post.PostVoteMapper;
import cn.iocoder.yudao.module.forum.dal.mysql.post.PostVoteOptionMapper;
import cn.iocoder.yudao.module.forum.dal.mysql.post.PostVoteRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Slf4j
public class PostVoteServiceImpl implements PostVoteService {

    private static final ErrorCode POST_VOTE_NOT_EXISTS = new ErrorCode(1_080_001, "帖子投票不存在");
    private static final ErrorCode POST_VOTE_ENDED = new ErrorCode(1_080_002, "投票已截止");
    private static final ErrorCode POST_VOTE_ALREADY_VOTED = new ErrorCode(1_080_003, "您已投过票");
    private static final ErrorCode POST_VOTE_TOO_MANY = new ErrorCode(1_080_004, "选择数量超出限制");
    private static final ErrorCode POST_VOTE_SINGLE_ONLY_ONE = new ErrorCode(1_080_005, "单选投票只能选一个");

    @Resource
    private PostVoteMapper postVoteMapper;
    @Resource
    private PostVoteOptionMapper postVoteOptionMapper;
    @Resource
    private PostVoteRecordMapper postVoteRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPostVote(Long postId, PostVoteDO vote, List<PostVoteOptionDO> options) {
        vote.setPostId(postId);
        postVoteMapper.insert(vote);

        Long voteId = vote.getId();
        for (int i = 0; i < options.size(); i++) {
            PostVoteOptionDO opt = options.get(i);
            opt.setVoteId(voteId);
            opt.setSortOrder(i);
            postVoteOptionMapper.insert(opt);
        }
        log.info("[createPostVote] postId={}, voteId={}, options={}", postId, voteId, options.size());
    }

    @Override
    public PostVoteDO getPostVote(Long postId) {
        return postVoteMapper.selectByPostId(postId);
    }

    @Override
    public List<PostVoteOptionDO> getVoteOptions(Long voteId) {
        return postVoteOptionMapper.selectByVoteId(voteId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void castPostVote(Long voteId, Long userId, List<Long> optionIds) {
        // 1. 校验投票存在
        PostVoteDO vote = postVoteMapper.selectById(voteId);
        if (vote == null) {
            throw exception(POST_VOTE_NOT_EXISTS);
        }

        // 2. 校验是否截止
        if (vote.getEndTime() != null && LocalDateTime.now().isAfter(vote.getEndTime())) {
            throw exception(POST_VOTE_ENDED);
        }

        // 3. 校验是否已投票
        List<PostVoteRecordDO> existingVotes = postVoteRecordMapper.selectByVoteIdAndUserId(voteId, userId);
        if (!existingVotes.isEmpty()) {
            throw exception(POST_VOTE_ALREADY_VOTED);
        }

        // 4. 校验投票类型
        if (vote.getVoteType() == 0 && optionIds.size() != 1) {
            throw exception(POST_VOTE_SINGLE_ONLY_ONE);
        }
        if (vote.getVoteType() == 1 && optionIds.size() > vote.getMaxChoices()) {
            throw exception(POST_VOTE_TOO_MANY);
        }

        // 5. 记录投票
        for (Long optionId : optionIds) {
            PostVoteRecordDO record = PostVoteRecordDO.builder()
                    .voteId(voteId)
                    .optionId(optionId)
                    .userId(userId)
                    .build();
            postVoteRecordMapper.insert(record);
        }
        log.info("[castPostVote] voteId={}, userId={}, optionIds={}", voteId, userId, optionIds);
    }

    @Override
    public List<PostVoteRecordDO> getUserVoteRecord(Long voteId, Long userId) {
        return postVoteRecordMapper.selectByVoteIdAndUserId(voteId, userId);
    }

    @Override
    public Map<Long, Long> getVoteCounts(Long voteId) {
        List<Map<String, Object>> rows = postVoteRecordMapper.selectVoteCountsByVoteId(voteId);
        Map<Long, Long> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Long optionId = ((Number) row.get("option_id")).longValue();
            Long cnt = ((Number) row.get("cnt")).longValue();
            result.put(optionId, cnt);
        }
        return result;
    }

    @Override
    public Long getVoterCount(Long voteId) {
        return postVoteRecordMapper.selectVoterCount(voteId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByPostId(Long postId) {
        PostVoteDO vote = postVoteMapper.selectByPostId(postId);
        if (vote == null) return;
        Long voteId = vote.getId();
        postVoteRecordMapper.deleteByVoteId(voteId);
        postVoteOptionMapper.deleteByVoteId(voteId);
        postVoteMapper.deleteById(voteId);
        log.info("[deleteByPostId] postId={}, voteId={}", postId, voteId);
    }
}
