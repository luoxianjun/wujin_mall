package cn.iocoder.yudao.module.forum.api.activity;

import cn.iocoder.yudao.module.forum.api.activity.dto.ForumActivityParticipationConfigDTO;
import cn.iocoder.yudao.module.forum.dal.dataobject.activity.ForumActivityDO;
import cn.iocoder.yudao.module.forum.dal.mysql.activity.ForumActivityMapper;
import cn.iocoder.yudao.module.forum.dal.mysql.activity.ForumActivitySignUpMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 论坛活动 API 实现
 */
@Service
public class ForumActivityApiImpl implements ForumActivityApi {

    @Resource
    private ForumActivityMapper activityMapper;
    @Resource
    private ForumActivitySignUpMapper signUpMapper;

    @Override
    public List<Long> getApprovedUserIds(Long activityId) {
        return signUpMapper.selectApprovedUserIdsByActivityId(activityId);
    }

    @Override
    public ForumActivityParticipationConfigDTO getParticipationConfig(Long activityId) {
        ForumActivityDO activity = activityMapper.selectById(activityId);
        if (activity == null) {
            return null;
        }
        boolean signUpRequired = activity.getSignUpStartTime() != null || activity.getSignUpEndTime() != null;
        ForumActivityParticipationConfigDTO config = new ForumActivityParticipationConfigDTO();
        config.setSignUpRequired(signUpRequired);
        config.setApprovalRequired(signUpRequired && Boolean.TRUE.equals(activity.getNeedApproval()));
        return config;
    }

    @Override
    public boolean isUserParticipated(Long activityId, Long userId) {
        return signUpMapper.selectByActivityIdAndUserId(activityId, userId) != null;
    }

    @Override
    public boolean isUserApprovedParticipated(Long activityId, Long userId) {
        return signUpMapper.selectApprovedByActivityIdAndUserId(activityId, userId) != null;
    }

    @Override
    public long getParticipantCount(Long activityId) {
        return signUpMapper.countByActivityId(activityId);
    }

    @Override
    public long getApprovedParticipantCount(Long activityId) {
        return signUpMapper.countApprovedByActivityId(activityId);
    }
}
