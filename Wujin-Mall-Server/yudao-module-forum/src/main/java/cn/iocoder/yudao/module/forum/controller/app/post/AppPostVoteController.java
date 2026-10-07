package cn.iocoder.yudao.module.forum.controller.app.post;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.PostVoteDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.PostVoteOptionDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.PostVoteRecordDO;
import cn.iocoder.yudao.module.forum.service.post.PostVoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "App - 帖子投票")
@RestController
@RequestMapping("/app/post/vote")
@Validated
public class AppPostVoteController {

    @Resource
    private PostVoteService postVoteService;

    @GetMapping("/detail")
    @Operation(summary = "获取帖子投票详情")
    public CommonResult<Map<String, Object>> getVoteDetail(@RequestParam("postId") Long postId) {
        PostVoteDO vote = postVoteService.getPostVote(postId);
        if (vote == null) {
            return success(null);
        }

        Long userId = getLoginUserId();
        Long voteId = vote.getId();

        // 获取选项
        List<PostVoteOptionDO> options = postVoteService.getVoteOptions(voteId);

        // 用户投票记录
        List<PostVoteRecordDO> userVotes = userId != null
                ? postVoteService.getUserVoteRecord(voteId, userId)
                : Collections.emptyList();
        boolean hasVoted = !userVotes.isEmpty();

        boolean ended = vote.getEndTime() != null && LocalDateTime.now().isAfter(vote.getEndTime());
        boolean showResults = ended || hasVoted || Boolean.TRUE.equals(vote.getShowRealtimeResult());

        Map<Long, Long> voteCounts = showResults
                ? postVoteService.getVoteCounts(voteId)
                : Collections.emptyMap();

        // 构建选项列表
        List<Map<String, Object>> optionList = new ArrayList<>();
        for (PostVoteOptionDO opt : options) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", opt.getId());
            m.put("title", opt.getTitle());
            m.put("sortOrder", opt.getSortOrder());
            if (showResults) {
                m.put("voteCount", voteCounts.getOrDefault(opt.getId(), 0L));
            }
            optionList.add(m);
        }

        Set<Long> userOptionIds = userVotes.stream()
                .map(PostVoteRecordDO::getOptionId)
                .collect(Collectors.toSet());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("vote", vote);
        result.put("options", optionList);
        result.put("userVotedOptionIds", userOptionIds);
        result.put("totalVoters", postVoteService.getVoterCount(voteId));
        result.put("hasVoted", hasVoted);
        result.put("ended", ended);

        return success(result);
    }

    @PostMapping("/cast")
    @Operation(summary = "帖子投票")
    public CommonResult<Boolean> castVote(@RequestBody Map<String, Object> body) {
        Long voteId = Long.valueOf(body.get("voteId").toString());
        Long userId = getLoginUserId();

        @SuppressWarnings("unchecked")
        List<Number> optionIdNums = (List<Number>) body.get("optionIds");
        List<Long> optionIds = optionIdNums.stream()
                .map(Number::longValue)
                .collect(Collectors.toList());

        postVoteService.castPostVote(voteId, userId, optionIds);
        return success(true);
    }
}
