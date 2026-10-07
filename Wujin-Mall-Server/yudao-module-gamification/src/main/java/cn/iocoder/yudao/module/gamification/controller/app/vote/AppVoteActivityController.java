package cn.iocoder.yudao.module.gamification.controller.app.vote;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.gamification.dal.dataobject.vote.VoteActivityDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.vote.VoteOptionDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.vote.VoteRecordDO;
import cn.iocoder.yudao.module.gamification.service.vote.VoteActivityService;
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

@Tag(name = "App - 投票活动")
@RestController
@RequestMapping("/gamification/vote")
@Validated
public class AppVoteActivityController {

    @Resource
    private VoteActivityService voteActivityService;

    @GetMapping("/activity/detail")
    @Operation(summary = "获取投票活动详情")
    public CommonResult<Map<String, Object>> getVoteDetail(@RequestParam("activityId") Long activityId) {
        VoteActivityDO activity = voteActivityService.getByActivityId(activityId);
        if (activity == null) {
            return success(null);
        }

        Long userId = getLoginUserId();
        Long voteActivityId = activity.getId();

        // 获取审核通过的选项
        List<VoteOptionDO> options = voteActivityService.getApprovedOptions(voteActivityId);

        // 获取用户投票记录
        List<VoteRecordDO> userVotes = userId != null
                ? voteActivityService.getUserVoteRecords(voteActivityId, userId)
                : Collections.emptyList();
        boolean hasVoted = !userVotes.isEmpty();

        // 是否显示实时结果
        boolean showResults = Boolean.TRUE.equals(activity.getShowRealtimeResult()) || hasVoted;

        // 投票结果
        Map<Long, Long> voteResults = showResults
                ? voteActivityService.getVoteResults(voteActivityId)
                : Collections.emptyMap();

        // 是否已截止
        boolean ended = activity.getEndTime() != null && LocalDateTime.now().isAfter(activity.getEndTime());

        // 如果已截止，强制显示结果
        if (ended) {
            voteResults = voteActivityService.getVoteResults(voteActivityId);
        }

        // 构建选项列表
        List<Map<String, Object>> optionList = new ArrayList<>();
        for (VoteOptionDO opt : options) {
            Map<String, Object> optMap = new LinkedHashMap<>();
            optMap.put("id", opt.getId());
            optMap.put("title", opt.getTitle());
            optMap.put("imageUrl", opt.getImageUrl());
            optMap.put("description", opt.getDescription());
            optMap.put("sortOrder", opt.getSortOrder());
            if (ended || showResults) {
                optMap.put("voteCount", voteResults.getOrDefault(opt.getId(), 0L));
            }
            optionList.add(optMap);
        }

        // 构建用户投票记录
        List<Map<String, Object>> userVoteList = userVotes.stream().map(v -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("optionId", v.getOptionId());
            m.put("rankOrder", v.getRankOrder());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("activity", activity);
        result.put("options", optionList);
        result.put("userVotes", userVoteList);
        result.put("totalVoters", voteActivityService.getVoterCount(voteActivityId));
        result.put("hasVoted", hasVoted);
        result.put("ended", ended);

        return success(result);
    }

    @PostMapping("/activity/cast")
    @Operation(summary = "投票")
    public CommonResult<Boolean> castVote(@RequestBody Map<String, Object> body) {
        Long activityId = Long.valueOf(body.get("activityId").toString());
        Long userId = getLoginUserId();

        // 通过论坛活动ID找到投票活动
        VoteActivityDO activity = voteActivityService.getByActivityId(activityId);
        if (activity == null) {
            return success(false);
        }

        @SuppressWarnings("unchecked")
        List<Number> optionIdNums = (List<Number>) body.get("optionIds");
        List<Long> optionIds = optionIdNums.stream()
                .map(Number::longValue)
                .collect(Collectors.toList());

        // 排序投票的排名映射
        Map<Long, Integer> rankings = new HashMap<>();
        if (body.containsKey("rankings") && body.get("rankings") != null) {
            @SuppressWarnings("unchecked")
            Map<String, Number> rawRankings = (Map<String, Number>) body.get("rankings");
            for (Map.Entry<String, Number> entry : rawRankings.entrySet()) {
                rankings.put(Long.valueOf(entry.getKey()), entry.getValue().intValue());
            }
        }

        voteActivityService.castVote(activity.getId(), userId, optionIds, rankings);
        return success(true);
    }

    @PostMapping("/option/add")
    @Operation(summary = "用户添加投票选项")
    public CommonResult<Long> addOption(@RequestBody Map<String, Object> body) {
        Long activityId = Long.valueOf(body.get("activityId").toString());
        Long userId = getLoginUserId();

        VoteActivityDO activity = voteActivityService.getByActivityId(activityId);
        if (activity == null) {
            return success(null);
        }

        String title = (String) body.get("title");
        String imageUrl = (String) body.get("imageUrl");

        Long optionId = voteActivityService.addUserOption(activity.getId(), userId, title, imageUrl);
        return success(optionId);
    }

    @GetMapping("/activity/results")
    @Operation(summary = "获取投票结果")
    public CommonResult<Map<String, Object>> getResults(@RequestParam("activityId") Long activityId) {
        VoteActivityDO activity = voteActivityService.getByActivityId(activityId);
        if (activity == null) {
            return success(null);
        }

        Long voteActivityId = activity.getId();
        List<VoteOptionDO> options = voteActivityService.getApprovedOptions(voteActivityId);
        Map<Long, Long> results = voteActivityService.getVoteResults(voteActivityId);
        Long totalVotes = results.values().stream().mapToLong(Long::longValue).sum();

        List<Map<String, Object>> optionResults = options.stream().map(opt -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", opt.getId());
            m.put("title", opt.getTitle());
            m.put("imageUrl", opt.getImageUrl());
            long count = results.getOrDefault(opt.getId(), 0L);
            m.put("voteCount", count);
            m.put("percentage", totalVotes > 0 ? Math.round(count * 1000.0 / totalVotes) / 10.0 : 0);
            return m;
        }).sorted((a, b) -> Long.compare((Long) b.get("voteCount"), (Long) a.get("voteCount")))
          .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("options", optionResults);
        result.put("totalVoters", voteActivityService.getVoterCount(voteActivityId));
        result.put("totalVotes", totalVotes);

        return success(result);
    }
}
