package cn.iocoder.yudao.module.gamification.service.vote;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.gamification.dal.dataobject.vote.VoteActivityDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.vote.VoteOptionDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.vote.VoteRecordDO;

import java.util.List;
import java.util.Map;

/**
 * 投票活动 Service 接口
 */
public interface VoteActivityService {

    /**
     * 创建投票活动（含选项）
     *
     * @param activity 投票活动
     * @param options 投票选项列表
     * @return 投票活动ID
     */
    Long createVoteActivity(VoteActivityDO activity, List<VoteOptionDO> options);

    /**
     * 更新投票活动（含选项）
     *
     * @param activity 投票活动
     * @param options 投票选项列表
     */
    void updateVoteActivity(VoteActivityDO activity, List<VoteOptionDO> options);

    /**
     * 删除投票活动
     *
     * @param id 投票活动ID
     */
    void deleteVoteActivity(Long id);

    /**
     * 获取投票活动
     *
     * @param id 投票活动ID
     * @return 投票活动
     */
    VoteActivityDO getVoteActivity(Long id);

    /**
     * 根据论坛活动ID获取投票活动
     *
     * @param activityId 论坛活动ID
     * @return 投票活动
     */
    VoteActivityDO getByActivityId(Long activityId);

    /**
     * 获取投票活动的选项列表（仅审核通过的）
     *
     * @param voteActivityId 投票活动ID
     * @return 选项列表
     */
    List<VoteOptionDO> getApprovedOptions(Long voteActivityId);

    /**
     * 获取投票活动的所有选项（含待审核，管理端使用）
     *
     * @param voteActivityId 投票活动ID
     * @return 选项列表
     */
    List<VoteOptionDO> getAllOptions(Long voteActivityId);

    /**
     * 投票
     *
     * @param voteActivityId 投票活动ID
     * @param userId 用户ID
     * @param optionIds 选中的选项ID列表
     * @param rankings 排序名次映射（排序投票使用，optionId → rankOrder）
     */
    void castVote(Long voteActivityId, Long userId, List<Long> optionIds, Map<Long, Integer> rankings);

    /**
     * 获取用户投票记录
     *
     * @param voteActivityId 投票活动ID
     * @param userId 用户ID
     * @return 投票记录列表
     */
    List<VoteRecordDO> getUserVoteRecords(Long voteActivityId, Long userId);

    /**
     * 获取投票结果（选项ID → 票数）
     *
     * @param voteActivityId 投票活动ID
     * @return 投票结果映射
     */
    Map<Long, Long> getVoteResults(Long voteActivityId);

    /**
     * 获取投票活动的参与人数（去重）
     *
     * @param voteActivityId 投票活动ID
     * @return 参与人数
     */
    Long getVoterCount(Long voteActivityId);

    /**
     * 用户添加选项（待审核）
     *
     * @param voteActivityId 投票活动ID
     * @param userId 用户ID
     * @param title 选项标题
     * @param imageUrl 选项配图URL
     * @return 选项ID
     */
    Long addUserOption(Long voteActivityId, Long userId, String title, String imageUrl);

    /**
     * 审核选项
     *
     * @param optionId 选项ID
     * @param auditStatus 审核状态
     */
    void auditOption(Long optionId, Integer auditStatus);
}
