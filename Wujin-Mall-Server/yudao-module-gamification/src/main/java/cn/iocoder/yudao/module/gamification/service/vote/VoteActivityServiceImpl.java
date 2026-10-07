package cn.iocoder.yudao.module.gamification.service.vote;

import cn.iocoder.yudao.module.gamification.dal.dataobject.vote.VoteActivityDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.vote.VoteOptionDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.vote.VoteRecordDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.vote.VoteActivityMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.vote.VoteOptionMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.vote.VoteRecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 投票活动 Service 实现
 */
@Service
public class VoteActivityServiceImpl implements VoteActivityService {

    private static final Logger log = LoggerFactory.getLogger(VoteActivityServiceImpl.class);

    // ========== 错误码 ==========
    private static final ErrorCode VOTE_ACTIVITY_NOT_EXISTS = new ErrorCode(1_040_001_000, "投票活动不存在");
    private static final ErrorCode VOTE_ALREADY_ENDED = new ErrorCode(1_040_001_001, "投票已截止");
    private static final ErrorCode VOTE_ACTIVITY_DISABLED = new ErrorCode(1_040_001_002, "投票活动已禁用");
    private static final ErrorCode VOTE_ALREADY_CAST = new ErrorCode(1_040_001_003, "您已参与过投票，不可重复投票");
    private static final ErrorCode VOTE_SINGLE_ONLY_ONE = new ErrorCode(1_040_001_004, "单选投票只能选择一个选项");
    private static final ErrorCode VOTE_EXCEED_MAX_CHOICES = new ErrorCode(1_040_001_005, "超过最大可选数量");
    private static final ErrorCode VOTE_AT_LEAST_ONE = new ErrorCode(1_040_001_006, "请至少选择一个选项");
    private static final ErrorCode VOTE_RANKING_REQUIRED = new ErrorCode(1_040_001_007, "排序投票需要提供排名");
    private static final ErrorCode VOTE_USER_ADD_NOT_ALLOWED = new ErrorCode(1_040_001_008, "该投票不允许用户添加选项");
    private static final ErrorCode VOTE_OPTIONS_LIMIT_REACHED = new ErrorCode(1_040_001_009, "选项数量已达上限");
    private static final ErrorCode VOTE_OPTION_NOT_EXISTS = new ErrorCode(1_040_001_010, "选项不存在");

    @Resource
    private VoteActivityMapper voteActivityMapper;
    @Resource
    private VoteOptionMapper voteOptionMapper;
    @Resource
    private VoteRecordMapper voteRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createVoteActivity(VoteActivityDO activity, List<VoteOptionDO> options) {
        // 插入投票活动
        voteActivityMapper.insert(activity);
        Long voteActivityId = activity.getId();

        // 批量插入选项
        if (options != null && !options.isEmpty()) {
            for (VoteOptionDO option : options) {
                option.setVoteActivityId(voteActivityId);
                option.setAddedByUser(false);
                option.setAuditStatus(VoteOptionDO.AUDIT_APPROVED); // 管理员创建的选项默认通过
            }
            voteOptionMapper.insertBatch(options);
        }

        log.info("[createVoteActivity] Created: id={}, activityId={}, voteType={}",
                voteActivityId, activity.getActivityId(), activity.getVoteType());
        return voteActivityId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateVoteActivity(VoteActivityDO activity, List<VoteOptionDO> options) {
        // 更新投票活动
        voteActivityMapper.updateById(activity);

        // 删除旧的管理员选项（保留用户添加的选项）
        Long voteActivityId = activity.getId();
        List<VoteOptionDO> existingOptions = voteOptionMapper.selectAllByVoteActivityId(voteActivityId);
        for (VoteOptionDO existing : existingOptions) {
            if (!Boolean.TRUE.equals(existing.getAddedByUser())) {
                voteOptionMapper.deleteById(existing.getId());
            }
        }

        // 插入新的选项
        if (options != null && !options.isEmpty()) {
            for (VoteOptionDO option : options) {
                option.setVoteActivityId(voteActivityId);
                option.setAddedByUser(false);
                option.setAuditStatus(VoteOptionDO.AUDIT_APPROVED);
                if (option.getId() != null) {
                    // 更新已有选项
                    voteOptionMapper.updateById(option);
                } else {
                    // 插入新选项
                    voteOptionMapper.insert(option);
                }
            }
        }

        log.info("[updateVoteActivity] Updated: id={}", activity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVoteActivity(Long id) {
        // 删除投票活动
        voteActivityMapper.deleteById(id);
        // 删除关联选项
        voteOptionMapper.deleteByVoteActivityId(id);
        log.info("[deleteVoteActivity] Deleted: id={}", id);
    }

    @Override
    public VoteActivityDO getVoteActivity(Long id) {
        return voteActivityMapper.selectById(id);
    }

    @Override
    public VoteActivityDO getByActivityId(Long activityId) {
        return voteActivityMapper.selectByActivityId(activityId);
    }

    @Override
    public List<VoteOptionDO> getApprovedOptions(Long voteActivityId) {
        return voteOptionMapper.selectByVoteActivityId(voteActivityId);
    }

    @Override
    public List<VoteOptionDO> getAllOptions(Long voteActivityId) {
        return voteOptionMapper.selectAllByVoteActivityId(voteActivityId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void castVote(Long voteActivityId, Long userId, List<Long> optionIds, Map<Long, Integer> rankings) {
        // 1. 校验投票活动存在
        VoteActivityDO activity = voteActivityMapper.selectById(voteActivityId);
        if (activity == null) {
            throw exception(VOTE_ACTIVITY_NOT_EXISTS);
        }

        // 2. 校验投票是否已截止
        if (activity.getEndTime() != null && LocalDateTime.now().isAfter(activity.getEndTime())) {
            throw exception(VOTE_ALREADY_ENDED);
        }

        // 3. 校验状态
        if (VoteActivityDO.STATUS_DISABLED.equals(activity.getStatus())) {
            throw exception(VOTE_ACTIVITY_DISABLED);
        }

        // 4. 校验用户是否已投票（不允许修改投票）
        List<VoteRecordDO> existingVotes = voteRecordMapper.selectByVoteActivityIdAndUserId(voteActivityId, userId);
        if (!existingVotes.isEmpty()) {
            throw exception(VOTE_ALREADY_CAST);
        }

        // 5. 校验投票类型与选项数量
        if (VoteActivityDO.TYPE_SINGLE.equals(activity.getVoteType())) {
            // 单选：只能选1个
            if (optionIds.size() != 1) {
                throw exception(VOTE_SINGLE_ONLY_ONE);
            }
        } else if (VoteActivityDO.TYPE_MULTIPLE.equals(activity.getVoteType())) {
            // 多选：不超过maxChoices
            if (optionIds.size() > activity.getMaxChoices()) {
                throw exception(VOTE_EXCEED_MAX_CHOICES);
            }
            if (optionIds.isEmpty()) {
                throw exception(VOTE_AT_LEAST_ONE);
            }
        } else if (VoteActivityDO.TYPE_RANKING.equals(activity.getVoteType())) {
            // 排序：必须提供排名
            if (rankings == null || rankings.isEmpty()) {
                throw exception(VOTE_RANKING_REQUIRED);
            }
        }

        // 6. 插入投票记录
        for (Long optionId : optionIds) {
            VoteRecordDO record = VoteRecordDO.builder()
                    .voteActivityId(voteActivityId)
                    .optionId(optionId)
                    .userId(userId)
                    .rankOrder(rankings != null ? rankings.get(optionId) : null)
                    .build();
            voteRecordMapper.insert(record);
        }

        log.info("[castVote] User {} voted in activity {} with {} options",
                userId, voteActivityId, optionIds.size());
    }

    @Override
    public List<VoteRecordDO> getUserVoteRecords(Long voteActivityId, Long userId) {
        return voteRecordMapper.selectByVoteActivityIdAndUserId(voteActivityId, userId);
    }

    @Override
    public Map<Long, Long> getVoteResults(Long voteActivityId) {
        List<VoteOptionDO> options = voteOptionMapper.selectByVoteActivityId(voteActivityId);
        Map<Long, Long> results = new HashMap<>();
        for (VoteOptionDO option : options) {
            Long count = voteRecordMapper.selectCountByOptionId(option.getId());
            results.put(option.getId(), count);
        }
        return results;
    }

    @Override
    public Long getVoterCount(Long voteActivityId) {
        return voteRecordMapper.selectDistinctUserCountByVoteActivityId(voteActivityId);
    }

    @Override
    public Long addUserOption(Long voteActivityId, Long userId, String title, String imageUrl) {
        // 1. 校验投票活动存在
        VoteActivityDO activity = voteActivityMapper.selectById(voteActivityId);
        if (activity == null) {
            throw exception(VOTE_ACTIVITY_NOT_EXISTS);
        }

        // 2. 校验是否允许用户添加选项
        if (!Boolean.TRUE.equals(activity.getAllowUserAddOption())) {
            throw exception(VOTE_USER_ADD_NOT_ALLOWED);
        }

        // 3. 校验选项数量上限
        Long currentCount = voteOptionMapper.selectCountByVoteActivityId(voteActivityId);
        if (currentCount >= activity.getMaxOptions()) {
            throw exception(VOTE_OPTIONS_LIMIT_REACHED);
        }

        // 4. 校验投票是否已截止
        if (activity.getEndTime() != null && LocalDateTime.now().isAfter(activity.getEndTime())) {
            throw exception(VOTE_ALREADY_ENDED);
        }

        // 5. 插入选项（待审核）
        VoteOptionDO option = VoteOptionDO.builder()
                .voteActivityId(voteActivityId)
                .title(title)
                .imageUrl(imageUrl)
                .addedByUser(true)
                .userId(userId)
                .auditStatus(VoteOptionDO.AUDIT_PENDING) // 用户添加的选项默认待审核
                .sortOrder(currentCount.intValue()) // 排在最后
                .build();
        voteOptionMapper.insert(option);

        log.info("[addUserOption] User {} added option to activity {}, optionId={}, pending audit",
                userId, voteActivityId, option.getId());
        return option.getId();
    }

    @Override
    public void auditOption(Long optionId, Integer auditStatus) {
        VoteOptionDO option = voteOptionMapper.selectById(optionId);
        if (option == null) {
            throw exception(VOTE_OPTION_NOT_EXISTS);
        }
        option.setAuditStatus(auditStatus);
        voteOptionMapper.updateById(option);
        log.info("[auditOption] Option {} audited: status={}", optionId, auditStatus);
    }
}
