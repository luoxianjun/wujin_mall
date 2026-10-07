package cn.iocoder.yudao.module.gamification.controller.admin.lottery;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.gamification.controller.admin.lottery.vo.*;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryActivityDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryActivityPrizeDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryPrizeDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryRecordDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.lottery.LotteryActivityMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.lottery.LotteryPrizeMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.lottery.LotteryRecordMapper;
import cn.iocoder.yudao.module.gamification.service.lottery.LotteryActivityService;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.module.forum.api.user.ForumUserProfileApi;
import cn.iocoder.yudao.module.forum.api.user.dto.ForumUserProfileDTO;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import java.util.Set;

@Tag(name = "Admin - 抽奖活动管理")
@RestController
@RequestMapping("/gamification/lottery")
@Validated
public class LotteryActivityController {

    private static final ErrorCode LOTTERY_RECORD_NOT_EXISTS =
            new ErrorCode(1_002_030_001, "抽奖记录不存在");
    private static final ErrorCode LOTTERY_RECORD_NOT_WINNING =
            new ErrorCode(1_002_030_002, "未中奖记录无需发放");

    @Resource
    private LotteryActivityService lotteryActivityService;
    @Resource
    private LotteryActivityMapper lotteryActivityMapper;
    @Resource
    private LotteryRecordMapper lotteryRecordMapper;
    @Resource
    private LotteryPrizeMapper lotteryPrizeMapper;
    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    private ForumUserProfileApi forumUserProfileApi;

    // ========== 活动管理 ==========

    @PostMapping("/activity/create")
    @Operation(summary = "创建抽奖活动")
    @PreAuthorize("@ss.hasPermission('forum:lottery-activity:create')")
    public CommonResult<Long> createLotteryActivity(@Valid @RequestBody LotteryActivitySaveReqVO reqVO) {
        LotteryActivityDO activity = new LotteryActivityDO();
        activity.setActivityId(reqVO.getActivityId());
        activity.setType(reqVO.getType());
        activity.setDrawTime(reqVO.getDrawTime());
        activity.setMaxDrawsPerDay(reqVO.getMaxDrawsPerDay());
        activity.setMaxDrawsTotal(reqVO.getMaxDrawsTotal());
        activity.setCostType(reqVO.getCostType());
        activity.setCostAmount(reqVO.getCostAmount());
        activity.setGuaranteeDraws(reqVO.getGuaranteeDraws());
        activity.setParticipationCondition(reqVO.getParticipationCondition());
        activity.setStatus(reqVO.getStatus());
        Long activityId = lotteryActivityService.createLotteryActivity(activity);

        // 保存关联奖品
        if (reqVO.getPrizes() != null && !reqVO.getPrizes().isEmpty()) {
            // 校验概率之和必须为100%
            BigDecimal totalProb = reqVO.getPrizes().stream()
                    .map(ActivityPrizeItemVO::getProbability)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalProb.compareTo(new BigDecimal("100")) != 0) {
                throw exception(new ErrorCode(1_002_030_000, "奖品概率之和必须为100%，当前为" + totalProb + "%"));
            }
            List<LotteryActivityPrizeDO> activityPrizes = reqVO.getPrizes().stream()
                    .map(item -> LotteryActivityPrizeDO.builder()
                            .prizeId(item.getPrizeId())
                            .probability(item.getProbability())
                            .sortOrder(item.getSortOrder())
                            .build())
                    .collect(Collectors.toList());
            lotteryActivityService.saveLotteryActivityPrizes(activityId, activityPrizes);
        }

        return success(activityId);
    }

    @PutMapping("/activity/update")
    @Operation(summary = "更新抽奖活动")
    @PreAuthorize("@ss.hasPermission('forum:lottery-activity:update')")
    public CommonResult<Boolean> updateLotteryActivity(@Valid @RequestBody LotteryActivitySaveReqVO reqVO) {
        LotteryActivityDO activity = new LotteryActivityDO();
        activity.setId(reqVO.getId());
        activity.setType(reqVO.getType());
        activity.setDrawTime(reqVO.getDrawTime());
        activity.setMaxDrawsPerDay(reqVO.getMaxDrawsPerDay());
        activity.setMaxDrawsTotal(reqVO.getMaxDrawsTotal());
        activity.setCostType(reqVO.getCostType());
        activity.setCostAmount(reqVO.getCostAmount());
        activity.setGuaranteeDraws(reqVO.getGuaranteeDraws());
        activity.setParticipationCondition(reqVO.getParticipationCondition());
        activity.setStatus(reqVO.getStatus());
        lotteryActivityService.updateLotteryActivity(activity);

        // 更新关联奖品
        if (reqVO.getPrizes() != null && !reqVO.getPrizes().isEmpty()) {
            // 校验概率之和必须为100%
            BigDecimal totalProb = reqVO.getPrizes().stream()
                    .map(ActivityPrizeItemVO::getProbability)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalProb.compareTo(new BigDecimal("100")) != 0) {
                throw exception(new ErrorCode(1_002_030_000, "奖品概率之和必须为100%，当前为" + totalProb + "%"));
            }
            List<LotteryActivityPrizeDO> activityPrizes = reqVO.getPrizes().stream()
                    .map(item -> LotteryActivityPrizeDO.builder()
                            .prizeId(item.getPrizeId())
                            .probability(item.getProbability())
                            .sortOrder(item.getSortOrder())
                            .build())
                    .collect(Collectors.toList());
            lotteryActivityService.saveLotteryActivityPrizes(reqVO.getId(), activityPrizes);
        } else if (reqVO.getPrizes() != null && reqVO.getPrizes().isEmpty()) {
            // 空列表：清除所有关联
            lotteryActivityService.saveLotteryActivityPrizes(reqVO.getId(), Collections.emptyList());
        }

        return success(true);
    }

    @DeleteMapping("/activity/delete")
    @Operation(summary = "删除抽奖活动")
    @PreAuthorize("@ss.hasPermission('forum:lottery-activity:delete')")
    public CommonResult<Boolean> deleteLotteryActivity(@RequestParam("id") Long id) {
        lotteryActivityMapper.deleteById(id);
        return success(true);
    }

    @GetMapping("/activity/get")
    @Operation(summary = "获取抽奖活动详情")
    @PreAuthorize("@ss.hasPermission('forum:lottery-activity:query')")
    public CommonResult<LotteryActivityRespVO> getLotteryActivity(@RequestParam("id") Long id) {
        LotteryActivityDO activity = lotteryActivityService.getLotteryActivity(id);
        if (activity == null) {
            return success(null);
        }
        LotteryActivityRespVO respVO = convertActivityToRespVO(activity);
        // 加载关联奖品
        respVO.setPrizes(getActivityPrizeRespItems(id));
        return success(respVO);
    }

    @GetMapping("/activity/page")
    @Operation(summary = "获取抽奖活动分页列表")
    @PreAuthorize("@ss.hasPermission('forum:lottery-activity:query')")
    public CommonResult<PageResult<LotteryActivityRespVO>> getLotteryActivityPage(@Valid LotteryActivityPageReqVO reqVO) {
        PageResult<LotteryActivityDO> pageResult = lotteryActivityMapper.selectPage(reqVO,
                new LambdaQueryWrapperX<LotteryActivityDO>()
                        .eqIfPresent(LotteryActivityDO::getType, reqVO.getType())
                        .eqIfPresent(LotteryActivityDO::getStatus, reqVO.getStatus())
                        .orderByDesc(LotteryActivityDO::getId));
        PageResult<LotteryActivityRespVO> result = new PageResult<>();
        result.setTotal(pageResult.getTotal());
        result.setList(pageResult.getList().stream()
                .map(this::convertActivityToRespVO)
                .collect(Collectors.toList()));
        return success(result);
    }

    // ========== 奖品管理（独立，不关联活动） ==========

    @PostMapping("/prize/create")
    @Operation(summary = "创建奖品")
    @PreAuthorize("@ss.hasPermission('forum:lottery-prize:create')")
    public CommonResult<Long> createPrize(@Valid @RequestBody LotteryPrizeSaveReqVO reqVO) {
        LotteryPrizeDO prize = new LotteryPrizeDO();
        prize.setName(reqVO.getName());
        prize.setType(reqVO.getType());
        prize.setValue(reqVO.getValue());
        prize.setImageUrl(reqVO.getImageUrl());
        prize.setTotalStock(reqVO.getTotalStock());
        prize.setRemainingStock(reqVO.getTotalStock()); // 初始库存=总库存
        prize.setSortOrder(reqVO.getSortOrder());
        prize.setRequireAddress(reqVO.getRequireAddress());
        return success(lotteryActivityService.createPrize(prize));
    }

    @PutMapping("/prize/update")
    @Operation(summary = "更新奖品")
    @PreAuthorize("@ss.hasPermission('forum:lottery-prize:update')")
    public CommonResult<Boolean> updatePrize(@Valid @RequestBody LotteryPrizeSaveReqVO reqVO) {
        LotteryPrizeDO prize = new LotteryPrizeDO();
        prize.setId(reqVO.getId());
        prize.setName(reqVO.getName());
        prize.setType(reqVO.getType());
        prize.setValue(reqVO.getValue());
        prize.setImageUrl(reqVO.getImageUrl());
        prize.setTotalStock(reqVO.getTotalStock());
        prize.setSortOrder(reqVO.getSortOrder());
        prize.setRequireAddress(reqVO.getRequireAddress());
        lotteryActivityService.updatePrize(prize);
        return success(true);
    }

    @DeleteMapping("/prize/delete")
    @Operation(summary = "删除奖品")
    @PreAuthorize("@ss.hasPermission('forum:lottery-prize:delete')")
    public CommonResult<Boolean> deletePrize(@RequestParam("id") Long id) {
        lotteryActivityService.deletePrize(id);
        return success(true);
    }

    @GetMapping("/prize/get")
    @Operation(summary = "获取奖品详情")
    @PreAuthorize("@ss.hasPermission('forum:lottery-prize:query')")
    public CommonResult<LotteryPrizeRespVO> getPrize(@RequestParam("id") Long id) {
        LotteryPrizeDO prize = lotteryPrizeMapper.selectById(id);
        if (prize == null) {
            return success(null);
        }
        return success(convertPrizeToRespVO(prize));
    }

    @GetMapping("/prize/page")
    @Operation(summary = "获取奖品分页列表")
    @PreAuthorize("@ss.hasPermission('forum:lottery-prize:query')")
    public CommonResult<PageResult<LotteryPrizeRespVO>> getPrizePage(@Valid LotteryPrizePageReqVO reqVO) {
        PageResult<LotteryPrizeDO> pageResult = lotteryPrizeMapper.selectPage(reqVO,
                new LambdaQueryWrapperX<LotteryPrizeDO>()
                        .likeIfPresent(LotteryPrizeDO::getName, reqVO.getName())
                        .eqIfPresent(LotteryPrizeDO::getType, reqVO.getType())
                        .orderByAsc(LotteryPrizeDO::getSortOrder));
        PageResult<LotteryPrizeRespVO> result = new PageResult<>();
        result.setTotal(pageResult.getTotal());
        result.setList(pageResult.getList().stream()
                .map(this::convertPrizeToRespVO)
                .collect(Collectors.toList()));
        return success(result);
    }

    @GetMapping("/prize/simple-list")
    @Operation(summary = "获取全部奖品简单列表（用于活动表单选择）")
    @PreAuthorize("@ss.hasPermission('forum:lottery-prize:query')")
    public CommonResult<List<LotteryPrizeRespVO>> getPrizeSimpleList() {
        List<LotteryPrizeDO> prizes = lotteryPrizeMapper.selectList(
                new LambdaQueryWrapperX<LotteryPrizeDO>()
                        .orderByAsc(LotteryPrizeDO::getSortOrder));
        return success(prizes.stream().map(this::convertPrizeToRespVO).collect(Collectors.toList()));
    }

    // ========== 抽奖记录 ==========

    @GetMapping("/record/page")
    @Operation(summary = "获取抽奖记录分页列表")
    @PreAuthorize("@ss.hasPermission('forum:lottery-record:query')")
    public CommonResult<PageResult<LotteryRecordRespVO>> getRecordPage(@Valid LotteryRecordPageReqVO reqVO) {
        PageResult<LotteryRecordDO> pageResult = getRecordPageResult(reqVO);
        PageResult<LotteryRecordRespVO> result = convertRecordPage(pageResult);
        return success(result);
    }

    @GetMapping("/record/export")
    @Operation(summary = "导出抽奖记录")
    @PreAuthorize("@ss.hasPermission('forum:lottery-record:export')")
    public void exportRecord(@Valid LotteryRecordPageReqVO reqVO, HttpServletResponse response) throws IOException {
        reqVO.setPageNo(1);
        reqVO.setPageSize(10000);
        List<LotteryRecordExcelVO> list = convertRecordPage(getRecordPageResult(reqVO)).getList().stream()
                .map(this::convertRecordToExcelVO)
                .collect(Collectors.toList());
        ExcelUtils.write(response, "抽奖记录.xlsx", "抽奖记录", LotteryRecordExcelVO.class, list);
    }

    private PageResult<LotteryRecordDO> getRecordPageResult(LotteryRecordPageReqVO reqVO) {
        PageResult<LotteryRecordDO> pageResult = lotteryRecordMapper.selectPage(reqVO,
                new LambdaQueryWrapperX<LotteryRecordDO>()
                        .eqIfPresent(LotteryRecordDO::getLotteryActivityId, reqVO.getLotteryActivityId())
                        .eqIfPresent(LotteryRecordDO::getUserId, reqVO.getUserId())
                        .eqIfPresent(LotteryRecordDO::getWon, reqVO.getWon())
                        .eqIfPresent(LotteryRecordDO::getDelivered, reqVO.getDelivered())
                        .betweenIfPresent(LotteryRecordDO::getDrawTime, reqVO.getDrawTime())
                        .orderByDesc(LotteryRecordDO::getId));
        return pageResult;
    }

    private PageResult<LotteryRecordRespVO> convertRecordPage(PageResult<LotteryRecordDO> pageResult) {
        PageResult<LotteryRecordRespVO> result = new PageResult<>();
        result.setTotal(pageResult.getTotal());
        if (pageResult.getList().isEmpty()) {
            result.setList(Collections.emptyList());
            return result;
        }
        List<LotteryRecordRespVO> voList = pageResult.getList().stream()
                .map(this::convertRecordToRespVO)
                .collect(Collectors.toList());

        // 批量查询用户昵称
        Set<Long> userIds = pageResult.getList().stream()
                .map(LotteryRecordDO::getUserId)
                .collect(Collectors.toSet());
        Map<Long, MemberUserRespDTO> userMap = memberUserApi.getUserMap(userIds);
        Map<Long, ForumUserProfileDTO> profileMap = forumUserProfileApi.getUserProfileMapByUserIds(userIds);
        if (profileMap == null) {
            profileMap = Collections.emptyMap();
        }

        // 批量查询活动名称
        Set<Long> activityIds = pageResult.getList().stream()
                .map(LotteryRecordDO::getLotteryActivityId)
                .collect(Collectors.toSet());
        Map<Long, LotteryActivityDO> activityMap = lotteryActivityMapper.selectBatchIds(activityIds)
                .stream().collect(Collectors.toMap(LotteryActivityDO::getId, a -> a));

        for (LotteryRecordRespVO vo : voList) {
            // 用户昵称
            MemberUserRespDTO user = userMap.get(vo.getUserId());
            if (user != null && user.getNickname() != null) {
                vo.setUserNickname(user.getNickname() + "(" + vo.getUserId() + ")");
            } else {
                vo.setUserNickname("用户" + vo.getUserId());
            }
            ForumUserProfileDTO profile = profileMap.get(vo.getUserId());
            if (profile != null) {
                vo.setUid(profile.getUid());
            }
            // 活动名称
            LotteryActivityDO act = activityMap.get(vo.getLotteryActivityId());
            if (act != null && act.getActivityId() != null) {
                vo.setActivityName("活动" + act.getActivityId() + "(" + vo.getLotteryActivityId() + ")");
            } else {
                vo.setActivityName(String.valueOf(vo.getLotteryActivityId()));
            }
        }

        result.setList(voList);
        return result;
    }

    @PutMapping("/record/deliver")
    @Operation(summary = "发放中奖奖品")
    @PreAuthorize("@ss.hasPermission('forum:lottery-record:deliver')")
    public CommonResult<Boolean> deliverRecord(@RequestParam("id") Long id) {
        LotteryRecordDO record = lotteryRecordMapper.selectById(id);
        if (record == null) {
            throw exception(LOTTERY_RECORD_NOT_EXISTS);
        }
        if (!Boolean.TRUE.equals(record.getWon())) {
            throw exception(LOTTERY_RECORD_NOT_WINNING);
        }
        if (Boolean.TRUE.equals(record.getDelivered())) {
            return success(true);
        }
        record.setDelivered(true);
        lotteryRecordMapper.updateById(record);
        return success(true);
    }

    // ========== 内部方法 ==========

    /**
     * 获取活动关联奖品的响应列表
     */
    private List<ActivityPrizeRespItemVO> getActivityPrizeRespItems(Long lotteryActivityId) {
        List<LotteryActivityPrizeDO> activityPrizes = lotteryActivityService.getActivityPrizes(lotteryActivityId);
        if (activityPrizes == null || activityPrizes.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询奖品
        List<Long> prizeIds = activityPrizes.stream()
                .map(LotteryActivityPrizeDO::getPrizeId)
                .collect(Collectors.toList());
        List<LotteryPrizeDO> prizes = lotteryPrizeMapper.selectBatchIds(prizeIds);
        Map<Long, LotteryPrizeDO> prizeMap = prizes.stream()
                .collect(Collectors.toMap(LotteryPrizeDO::getId, p -> p));

        List<ActivityPrizeRespItemVO> result = new ArrayList<>();
        for (LotteryActivityPrizeDO ap : activityPrizes) {
            LotteryPrizeDO prize = prizeMap.get(ap.getPrizeId());
            if (prize == null) continue;
            ActivityPrizeRespItemVO item = new ActivityPrizeRespItemVO();
            item.setPrizeId(prize.getId());
            item.setPrizeName(prize.getName());
            item.setPrizeType(prize.getType());
            item.setImageUrl(prize.getImageUrl());
            item.setTotalStock(prize.getTotalStock());
            item.setRemainingStock(prize.getRemainingStock());
            item.setProbability(ap.getProbability());
            item.setSortOrder(ap.getSortOrder());
            result.add(item);
        }
        return result;
    }

    // ========== 转换方法 ==========

    private LotteryActivityRespVO convertActivityToRespVO(LotteryActivityDO activity) {
        LotteryActivityRespVO respVO = new LotteryActivityRespVO();
        respVO.setId(activity.getId());
        respVO.setActivityId(activity.getActivityId());
        respVO.setType(activity.getType());
        respVO.setDrawTime(activity.getDrawTime());
        respVO.setMaxDrawsPerDay(activity.getMaxDrawsPerDay());
        respVO.setMaxDrawsTotal(activity.getMaxDrawsTotal());
        respVO.setCostType(activity.getCostType());
        respVO.setCostAmount(activity.getCostAmount());
        respVO.setGuaranteeDraws(activity.getGuaranteeDraws());
        respVO.setParticipationCondition(activity.getParticipationCondition());
        respVO.setStatus(activity.getStatus());
        respVO.setCreateTime(activity.getCreateTime());
        return respVO;
    }

    private LotteryPrizeRespVO convertPrizeToRespVO(LotteryPrizeDO prize) {
        LotteryPrizeRespVO respVO = new LotteryPrizeRespVO();
        respVO.setId(prize.getId());
        respVO.setName(prize.getName());
        respVO.setType(prize.getType());
        respVO.setValue(prize.getValue());
        respVO.setImageUrl(prize.getImageUrl());
        respVO.setTotalStock(prize.getTotalStock());
        respVO.setRemainingStock(prize.getRemainingStock());
        respVO.setSortOrder(prize.getSortOrder());
        respVO.setRequireAddress(prize.getRequireAddress());
        respVO.setCreateTime(prize.getCreateTime());
        return respVO;
    }

    private LotteryRecordRespVO convertRecordToRespVO(LotteryRecordDO record) {
        LotteryRecordRespVO respVO = new LotteryRecordRespVO();
        respVO.setId(record.getId());
        respVO.setLotteryActivityId(record.getLotteryActivityId());
        respVO.setUserId(record.getUserId());
        respVO.setPrizeName(record.getPrizeName());
        respVO.setPrizeType(record.getPrizeType());
        respVO.setWon(record.getWon());
        respVO.setDrawTime(record.getDrawTime());
        respVO.setDelivered(record.getDelivered());
        respVO.setDeliveryAddress(record.getDeliveryAddress());
        return respVO;
    }

    private LotteryRecordExcelVO convertRecordToExcelVO(LotteryRecordRespVO record) {
        LotteryRecordExcelVO excelVO = new LotteryRecordExcelVO();
        excelVO.setId(record.getId());
        excelVO.setActivityName(record.getActivityName());
        excelVO.setUserNickname(record.getUserNickname());
        excelVO.setUid(record.getUid());
        excelVO.setPrizeName(record.getPrizeName());
        excelVO.setPrizeType(formatPrizeType(record.getPrizeType()));
        excelVO.setWon(Boolean.TRUE.equals(record.getWon()) ? "中奖" : "未中奖");
        excelVO.setDrawTime(record.getDrawTime());
        excelVO.setDelivered(Boolean.TRUE.equals(record.getDelivered()) ? "已发放" : "未发放");
        excelVO.setDeliveryAddress(record.getDeliveryAddress());
        return excelVO;
    }

    private String formatPrizeType(Integer prizeType) {
        if (prizeType == null) {
            return "-";
        }
        switch (prizeType) {
            case 0:
                return "积分";
            case 1:
                return "优惠券";
            case 2:
                return "实物";
            case 3:
                return "虚拟物品";
            case 4:
                return "谢谢参与";
            default:
                return String.valueOf(prizeType);
        }
    }
}
