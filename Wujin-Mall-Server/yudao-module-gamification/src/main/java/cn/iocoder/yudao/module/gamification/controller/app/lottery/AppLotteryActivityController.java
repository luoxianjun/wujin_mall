package cn.iocoder.yudao.module.gamification.controller.app.lottery;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.gamification.controller.admin.lottery.vo.ActivityPrizeRespItemVO;
import cn.iocoder.yudao.module.gamification.controller.admin.lottery.vo.LotteryActivityRespVO;
import cn.iocoder.yudao.module.gamification.controller.admin.lottery.vo.LotteryPrizeRespVO;
import cn.iocoder.yudao.module.gamification.controller.admin.lottery.vo.LotteryRecordRespVO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryActivityDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryActivityPrizeDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryPrizeDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryRecordDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.lottery.LotteryPrizeMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.lottery.LotteryRecordMapper;
import cn.iocoder.yudao.module.gamification.service.lottery.LotteryActivityService;
import cn.iocoder.yudao.module.gamification.service.lottery.LotteryDrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.forum.api.activity.ForumActivityApi;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "App - 抽奖活动")
@RestController
@RequestMapping("/gamification/lottery")
@Validated
public class AppLotteryActivityController {

    private static final String ADDRESS_UPDATE_FORBIDDEN_MESSAGE = "奖品已发放，无法修改地址";

    @Resource
    private LotteryDrawService lotteryDrawService;
    @Resource
    private LotteryActivityService lotteryActivityService;
    @Resource
    private LotteryPrizeMapper lotteryPrizeMapper;
    @Resource
    private LotteryRecordMapper lotteryRecordMapper;
    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    private ForumActivityApi forumActivityApi;

    @GetMapping("/activity/get")
    @Operation(summary = "获取抽奖活动详情")
    public CommonResult<Map<String, Object>> getLotteryActivity(@RequestParam("id") Long id) {
        LotteryActivityDO activity = lotteryActivityService.getLotteryActivity(id);
        if (activity == null) {
            return success(null);
        }
        return success(buildActivityDetailResult(id, activity));
    }

    @GetMapping("/activity/get-by-activity-id")
    @Operation(summary = "根据论坛活动ID获取抽奖活动详情")
    public CommonResult<Map<String, Object>> getLotteryActivityByActivityId(@RequestParam("activityId") Long activityId) {
        LotteryActivityDO activity = lotteryActivityService.getByActivityId(activityId);
        if (activity == null) {
            return success(null);
        }
        return success(buildActivityDetailResult(activity.getId(), activity));
    }

    private Map<String, Object> buildActivityDetailResult(Long lotteryActivityId, LotteryActivityDO activity) {
        // 通过关联表获取奖品列表
        List<LotteryActivityPrizeDO> activityPrizes = lotteryActivityService.getActivityPrizes(lotteryActivityId);
        List<ActivityPrizeRespItemVO> prizeItems = new ArrayList<>();
        if (!activityPrizes.isEmpty()) {
            List<Long> prizeIds = activityPrizes.stream()
                    .map(LotteryActivityPrizeDO::getPrizeId).collect(Collectors.toList());
            List<LotteryPrizeDO> prizes = lotteryPrizeMapper.selectBatchIds(prizeIds);
            Map<Long, LotteryPrizeDO> prizeMap = prizes.stream()
                    .collect(Collectors.toMap(LotteryPrizeDO::getId, p -> p));
            for (LotteryActivityPrizeDO ap : activityPrizes) {
                LotteryPrizeDO p = prizeMap.get(ap.getPrizeId());
                if (p == null) continue;
                ActivityPrizeRespItemVO item = new ActivityPrizeRespItemVO();
                item.setPrizeId(p.getId());
                item.setPrizeName(p.getName());
                item.setPrizeType(p.getType());
                item.setImageUrl(p.getImageUrl());
                item.setProbability(ap.getProbability());
                // 不暴露库存信息给前端
                prizeItems.add(item);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("activity", activity);
        result.put("prizes", prizeItems);
        return result;
    }

    @PostMapping("/activity/draw")
    @Operation(summary = "执行抽奖")
    public CommonResult<LotteryRecordRespVO> draw(@RequestParam("id") Long lotteryActivityId) {
        Long userId = getLoginUserId();
        LotteryRecordDO record = lotteryDrawService.draw(userId, lotteryActivityId);

        LotteryRecordRespVO respVO = new LotteryRecordRespVO();
        respVO.setId(record.getId());
        respVO.setPrizeName(record.getPrizeName());
        respVO.setPrizeType(record.getPrizeType());
        respVO.setWon(record.getWon());
        respVO.setDrawTime(record.getDrawTime());
        return success(respVO);
    }

    @GetMapping("/activity/can-draw")
    @Operation(summary = "检查是否可以抽奖")
    public CommonResult<Boolean> canDraw(@RequestParam("id") Long lotteryActivityId) {
        Long userId = getLoginUserId();
        return success(lotteryDrawService.canDraw(userId, lotteryActivityId));
    }

    @GetMapping("/activity/my-records")
    @Operation(summary = "获取我的抽奖记录")
    public CommonResult<List<LotteryRecordRespVO>> getMyRecords(@RequestParam("id") Long lotteryActivityId) {
        Long userId = getLoginUserId();
        List<LotteryRecordDO> records = lotteryDrawService.getUserRecords(userId, lotteryActivityId);
        return success(records.stream().map(r -> {
            LotteryRecordRespVO vo = new LotteryRecordRespVO();
            vo.setId(r.getId());
            vo.setPrizeName(r.getPrizeName());
            vo.setPrizeType(r.getPrizeType());
            vo.setWon(r.getWon());
            vo.setDrawTime(r.getDrawTime());
            vo.setDelivered(r.getDelivered());
            vo.setDeliveryAddress(r.getDeliveryAddress());
            return vo;
        }).collect(Collectors.toList()));
    }

    @GetMapping("/activity/winners")
    @Operation(summary = "获取最近中奖名单")
    public CommonResult<List<LotteryRecordRespVO>> getWinners(@RequestParam("id") Long lotteryActivityId) {
        List<LotteryRecordDO> winners = lotteryRecordMapper.selectRecentWinners(lotteryActivityId, 20);

        // 批量查询用户昵称
        Set<Long> userIds = winners.stream().map(LotteryRecordDO::getUserId).collect(Collectors.toSet());
        Map<Long, MemberUserRespDTO> userMap = memberUserApi.getUserMap(userIds);

        return success(winners.stream().map(r -> {
            LotteryRecordRespVO vo = new LotteryRecordRespVO();
            vo.setId(r.getId());
            vo.setUserId(r.getUserId());
            vo.setPrizeName(r.getPrizeName());
            vo.setPrizeType(r.getPrizeType());
            vo.setWon(r.getWon());
            vo.setDrawTime(r.getDrawTime());
            // 设置用户昵称（脱敏）
            MemberUserRespDTO user = userMap.get(r.getUserId());
            if (user != null && user.getNickname() != null) {
                String nick = user.getNickname();
                if (nick.length() > 1) {
                    vo.setUserNickname(nick.substring(0, 1) + "***" + nick.substring(nick.length() - 1));
                } else {
                    vo.setUserNickname(nick + "***");
                }
            } else {
                vo.setUserNickname("用户" + r.getUserId());
            }
            return vo;
        }).collect(Collectors.toList()));
    }

    @PostMapping("/activity/submit-address")
    @Operation(summary = "提交中奖地址")
    public CommonResult<Boolean> submitAddress(
            @RequestParam("recordId") Long recordId,
            @RequestParam("address") String address) {
        Long userId = getLoginUserId();
        LotteryRecordDO record = lotteryRecordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            return success(false);
        }
        if (Boolean.TRUE.equals(record.getDelivered())) {
            return error(BAD_REQUEST.getCode(), ADDRESS_UPDATE_FORBIDDEN_MESSAGE);
        }
        record.setDeliveryAddress(address);
        lotteryRecordMapper.updateById(record);
        return success(true);
    }

    @GetMapping("/activity/participated")
    @Operation(summary = "检查是否已参与定时抽奖")
    public CommonResult<Boolean> hasParticipated(@RequestParam("id") Long lotteryActivityId) {
        Long userId = getLoginUserId();
        LotteryActivityDO activity = lotteryActivityService.getLotteryActivity(lotteryActivityId);
        if (activity == null || activity.getActivityId() == null) {
            return success(false);
        }
        if (LotteryActivityDO.CONDITION_ENROLLED.equals(activity.getParticipationCondition())) {
            return success(forumActivityApi.isUserApprovedParticipated(activity.getActivityId(), userId));
        }
        return success(forumActivityApi.isUserParticipated(activity.getActivityId(), userId));
    }

    @GetMapping("/activity/participant-count")
    @Operation(summary = "获取参与人数")
    public CommonResult<Long> getParticipantCount(@RequestParam("id") Long lotteryActivityId) {
        LotteryActivityDO activity = lotteryActivityService.getLotteryActivity(lotteryActivityId);
        if (activity == null || activity.getActivityId() == null) {
            return success(0L);
        }
        if (LotteryActivityDO.CONDITION_ENROLLED.equals(activity.getParticipationCondition())) {
            return success(forumActivityApi.getApprovedParticipantCount(activity.getActivityId()));
        }
        return success(forumActivityApi.getParticipantCount(activity.getActivityId()));
    }
}
