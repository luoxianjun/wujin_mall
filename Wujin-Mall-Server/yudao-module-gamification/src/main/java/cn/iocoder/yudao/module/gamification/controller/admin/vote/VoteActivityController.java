package cn.iocoder.yudao.module.gamification.controller.admin.vote;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gamification.controller.admin.vote.vo.*;
import cn.iocoder.yudao.module.gamification.dal.dataobject.vote.VoteActivityDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.vote.VoteOptionDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.vote.VoteActivityMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.vote.VoteRecordMapper;
import cn.iocoder.yudao.module.gamification.service.vote.VoteActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin - 投票活动管理")
@RestController
@RequestMapping("/gamification/vote")
@Validated
public class VoteActivityController {

    @Resource
    private VoteActivityService voteActivityService;
    @Resource
    private VoteActivityMapper voteActivityMapper;
    @Resource
    private VoteRecordMapper voteRecordMapper;

    @PostMapping("/activity/create")
    @Operation(summary = "创建投票活动")
    @PreAuthorize("@ss.hasPermission('gamification:vote-activity:create')")
    public CommonResult<Long> createVoteActivity(@Valid @RequestBody VoteActivitySaveReqVO reqVO) {
        VoteActivityDO activity = convertToDO(reqVO);
        List<VoteOptionDO> options = convertToOptionDOs(reqVO.getOptions());
        Long id = voteActivityService.createVoteActivity(activity, options);
        return success(id);
    }

    @PutMapping("/activity/update")
    @Operation(summary = "更新投票活动")
    @PreAuthorize("@ss.hasPermission('gamification:vote-activity:update')")
    public CommonResult<Boolean> updateVoteActivity(@Valid @RequestBody VoteActivitySaveReqVO reqVO) {
        VoteActivityDO activity = convertToDO(reqVO);
        List<VoteOptionDO> options = convertToOptionDOs(reqVO.getOptions());
        voteActivityService.updateVoteActivity(activity, options);
        return success(true);
    }

    @DeleteMapping("/activity/delete")
    @Operation(summary = "删除投票活动")
    @PreAuthorize("@ss.hasPermission('gamification:vote-activity:delete')")
    @Parameter(name = "id", description = "投票活动ID", required = true, example = "1")
    public CommonResult<Boolean> deleteVoteActivity(@RequestParam("id") Long id) {
        voteActivityService.deleteVoteActivity(id);
        return success(true);
    }

    @GetMapping("/activity/get")
    @Operation(summary = "获取投票活动详情")
    @PreAuthorize("@ss.hasPermission('gamification:vote-activity:query')")
    @Parameter(name = "id", description = "投票活动ID", required = true, example = "1")
    public CommonResult<VoteActivityRespVO> getVoteActivity(@RequestParam("id") Long id) {
        VoteActivityDO activity = voteActivityService.getVoteActivity(id);
        if (activity == null) {
            return success(null);
        }
        return success(convertToRespVO(activity));
    }

    @GetMapping("/activity/page")
    @Operation(summary = "投票活动分页")
    @PreAuthorize("@ss.hasPermission('gamification:vote-activity:query')")
    public CommonResult<PageResult<VoteActivityRespVO>> getVoteActivityPage(@Valid VoteActivityPageReqVO reqVO) {
        PageResult<VoteActivityDO> pageResult = voteActivityMapper.selectPage(reqVO,
                new LambdaQueryWrapperX<VoteActivityDO>()
                        .eqIfPresent(VoteActivityDO::getActivityId, reqVO.getActivityId())
                        .eqIfPresent(VoteActivityDO::getVoteType, reqVO.getVoteType())
                        .eqIfPresent(VoteActivityDO::getStatus, reqVO.getStatus())
                        .orderByDesc(VoteActivityDO::getId));

        // 转换为RespVO
        List<VoteActivityRespVO> list = pageResult.getList().stream()
                .map(this::convertToRespVO)
                .collect(Collectors.toList());

        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    @PutMapping("/option/audit")
    @Operation(summary = "审核投票选项")
    @PreAuthorize("@ss.hasPermission('gamification:vote-activity:update')")
    public CommonResult<Boolean> auditOption(@RequestParam("optionId") Long optionId,
                                              @RequestParam("auditStatus") Integer auditStatus) {
        voteActivityService.auditOption(optionId, auditStatus);
        return success(true);
    }

    // ========== 转换方法 ==========

    private VoteActivityDO convertToDO(VoteActivitySaveReqVO reqVO) {
        return VoteActivityDO.builder()
                .id(reqVO.getId())
                .activityId(reqVO.getActivityId())
                .voteType(reqVO.getVoteType())
                .maxChoices(reqVO.getMaxChoices() != null ? reqVO.getMaxChoices() : 1)
                .anonymous(reqVO.getAnonymous() != null ? reqVO.getAnonymous() : true)
                .showRealtimeResult(reqVO.getShowRealtimeResult() != null ? reqVO.getShowRealtimeResult() : true)
                .allowUserAddOption(reqVO.getAllowUserAddOption() != null ? reqVO.getAllowUserAddOption() : false)
                .requireRealName(reqVO.getRequireRealName() != null ? reqVO.getRequireRealName() : false)
                .maxOptions(reqVO.getMaxOptions() != null ? reqVO.getMaxOptions() : 10)
                .endTime(reqVO.getEndTime())
                .minParticipants(reqVO.getMinParticipants() != null ? reqVO.getMinParticipants() : 0)
                .allowComment(reqVO.getAllowComment() != null ? reqVO.getAllowComment() : true)
                .votesPerUser(reqVO.getVotesPerUser() != null ? reqVO.getVotesPerUser() : 1)
                .status(reqVO.getStatus() != null ? reqVO.getStatus() : 0)
                .build();
    }

    private List<VoteOptionDO> convertToOptionDOs(List<VoteOptionItemVO> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        return items.stream().map(item -> VoteOptionDO.builder()
                .id(item.getId())
                .title(item.getTitle())
                .imageUrl(item.getImageUrl())
                .description(item.getDescription())
                .sortOrder(item.getSortOrder() != null ? item.getSortOrder() : 0)
                .build()
        ).collect(Collectors.toList());
    }

    private VoteActivityRespVO convertToRespVO(VoteActivityDO activity) {
        VoteActivityRespVO respVO = new VoteActivityRespVO();
        respVO.setId(activity.getId());
        respVO.setActivityId(activity.getActivityId());
        respVO.setVoteType(activity.getVoteType());
        respVO.setMaxChoices(activity.getMaxChoices());
        respVO.setAnonymous(activity.getAnonymous());
        respVO.setShowRealtimeResult(activity.getShowRealtimeResult());
        respVO.setAllowUserAddOption(activity.getAllowUserAddOption());
        respVO.setRequireRealName(activity.getRequireRealName());
        respVO.setMaxOptions(activity.getMaxOptions());
        respVO.setEndTime(activity.getEndTime());
        respVO.setMinParticipants(activity.getMinParticipants());
        respVO.setAllowComment(activity.getAllowComment());
        respVO.setVotesPerUser(activity.getVotesPerUser());
        respVO.setStatus(activity.getStatus());
        respVO.setCreateTime(activity.getCreateTime());
        respVO.setUpdateTime(activity.getUpdateTime());

        // 加载选项（含票数）
        List<VoteOptionDO> options = voteActivityService.getAllOptions(activity.getId());
        Map<Long, Long> results = voteActivityService.getVoteResults(activity.getId());
        List<VoteOptionRespItemVO> optionVOs = options.stream().map(opt -> {
            VoteOptionRespItemVO vo = new VoteOptionRespItemVO();
            vo.setId(opt.getId());
            vo.setTitle(opt.getTitle());
            vo.setImageUrl(opt.getImageUrl());
            vo.setDescription(opt.getDescription());
            vo.setSortOrder(opt.getSortOrder());
            vo.setAddedByUser(opt.getAddedByUser());
            vo.setAuditStatus(opt.getAuditStatus());
            vo.setVoteCount(results.getOrDefault(opt.getId(), 0L));
            return vo;
        }).collect(Collectors.toList());
        respVO.setOptions(optionVOs);

        // 参与人数
        respVO.setVoterCount(voteActivityService.getVoterCount(activity.getId()));

        return respVO;
    }
}
