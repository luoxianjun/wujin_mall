package cn.iocoder.yudao.module.forum.service.point;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.forum.controller.admin.point.vo.AdminPointRecordExcelVO;
import cn.iocoder.yudao.module.forum.controller.admin.point.vo.AdminPointRecordPageReqVO;
import cn.iocoder.yudao.module.forum.controller.app.point.vo.AppPointRecordPageReqVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.point.ForumPointRecordDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.user.ForumUserProfileDO;
import cn.iocoder.yudao.module.forum.dal.mysql.point.ForumPointRecordMapper;
import cn.iocoder.yudao.module.forum.dal.mysql.user.ForumUserProfileMapper;
import cn.iocoder.yudao.module.forum.enums.point.ForumPointBizTypeEnum;
import cn.iocoder.yudao.module.forum.service.user.ForumUserProfileService;
import cn.iocoder.yudao.module.member.dal.mysql.user.MemberUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.forum.enums.ErrorCodeConstants.*;

/**
 * 论坛积分 Service 实现类
 *
 * @author forum
 */
@Service
@Validated
@Slf4j
public class ForumPointServiceImpl implements ForumPointService {

    @Resource
    private ForumPointRecordMapper pointRecordMapper;

    @Resource
    private ForumUserProfileMapper userProfileMapper;

    @Resource
    private MemberUserMapper memberUserMapper;

    @Resource
    private ForumUserProfileService userProfileService;

    @Resource
    private cn.iocoder.yudao.module.forum.service.text.ForumTextAuditService textAuditService;

    /**
     * 发帖奖励积分
     */
    private static final int POST_POINT = 10;

    /**
     * 评论奖励积分
     */
    private static final int COMMENT_POINT = 5;

    /**
     * 帖子被点赞奖励积分
     */
    private static final int POST_LIKE_POINT = 2;

    /**
     * 活动报名奖励积分
     */
    private static final int ACTIVITY_SIGN_UP_POINT = 5;

    /**
     * 活动签到奖励积分
     */
    private static final int ACTIVITY_CHECK_IN_POINT = 10;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPoint(Long userId, Integer point, ForumPointBizTypeEnum bizType, String bizId) {
        changePoint(userId, point, bizType, bizId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reducePoint(Long userId, Integer point, ForumPointBizTypeEnum bizType, String bizId) {
        changePoint(userId, -point, bizType, bizId);
    }

    @Override
    public void addSignPoint(Long userId, Integer point, String bizId) {
        addPoint(userId, point, ForumPointBizTypeEnum.SIGN, bizId);
    }

    @Override
    public void addPostPoint(Long userId, String bizId) {
        addPoint(userId, POST_POINT, ForumPointBizTypeEnum.POST_PUBLISH, bizId);
    }

    @Override
    public void addCommentPoint(Long userId, String bizId) {
        addPoint(userId, COMMENT_POINT, ForumPointBizTypeEnum.COMMENT_PUBLISH, bizId);
    }

    @Override
    public void addPostLikePoint(Long userId, String bizId) {
        addPoint(userId, POST_LIKE_POINT, ForumPointBizTypeEnum.POST_BE_LIKED, bizId);
    }

    @Override
    public void addActivitySignUpPoint(Long userId, Integer point, String bizId) {
        if (point != null && point > 0) {
            addPoint(userId, point, ForumPointBizTypeEnum.ACTIVITY_SIGN_UP, bizId);
        }
    }

    @Override
    public void addActivityCheckInPoint(Long userId, String bizId) {
        addPoint(userId, ACTIVITY_CHECK_IN_POINT, ForumPointBizTypeEnum.ACTIVITY_CHECK_IN, bizId);
    }

    @Override
    public PageResult<ForumPointRecordDO> getPointRecordPage(AppPointRecordPageReqVO reqVO) {
        return pointRecordMapper.selectPage(reqVO);
    }

    @Override
    public Integer getUserPoint(Long userId) {
        ForumUserProfileDO profile = userProfileMapper.selectByUserId(userId);
        if (profile == null) {
            throw ServiceExceptionUtil.exception(USER_PROFILE_NOT_EXISTS);
        }
        return profile.getPoint();
    }

    /**
     * 变更积分
     */
    @Transactional(rollbackFor = Exception.class)
    public void changePoint(Long userId, Integer point, ForumPointBizTypeEnum bizType, String bizId) {
        // 1. 获取用户资料
        ForumUserProfileDO profile = userProfileMapper.selectByUserId(userId);
        if (profile == null) {
            throw ServiceExceptionUtil.exception(USER_PROFILE_NOT_EXISTS);
        }

        // 2. 检查积分是否足够（减少积分时）
        if (point < 0 && profile.getPoint() < Math.abs(point)) {
            throw ServiceExceptionUtil.exception(POINT_NOT_ENOUGH);
        }

        // 3. 计算新的积分
        int newPoint = profile.getPoint() + point;
        int newTotalPoint = profile.getTotalPoint() + (point > 0 ? point : 0);

        // 4. 更新用户积分
        ForumUserProfileDO updateObj = ForumUserProfileDO.builder()
                .id(profile.getId())
                .point(newPoint)
                .totalPoint(newTotalPoint)
                .build();
        userProfileMapper.updateById(updateObj);
        // 同步更新 member_user 表的积分
        if (point > 0) {
            memberUserMapper.updatePointIncr(userId, point);
        } else if (point < 0) {
            memberUserMapper.updatePointDecr(userId, point);
        }

        // 5. 创建积分记录
        String description = String.format(bizType.getDescription(), Math.abs(point));
        ForumPointRecordDO record = ForumPointRecordDO.builder()
                .userId(userId)
                .bizId(bizId)
                .bizType(bizType.getType())
                .title(bizType.getName())
                .description(description)
                .point(point)
                .totalPoint(newPoint)
                .build();
        pointRecordMapper.insert(record);

        log.info("[changePoint][用户积分变更，userId={}, point={}, bizType={}, newPoint={}]", 
                userId, point, bizType.getName(), newPoint);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminChangePoint(Long userId, Integer point, String reason) {
        // 使用管理员调整类型，bizId 使用原因作为标识
        changePoint(userId, point, ForumPointBizTypeEnum.ADMIN, reason);
    }

    @Override
    public List<AdminPointRecordExcelVO> getPointRecordExcelList(AdminPointRecordPageReqVO reqVO) {
        // 1. 处理 UID 搜索：根据 UID 模糊匹配获取用户 ID 列表
        if (StrUtil.isNotBlank(reqVO.getUid())) {
            List<Long> userIds = userProfileService.getUserIdsByUidLike(reqVO.getUid());
            if (CollUtil.isEmpty(userIds)) {
                return Collections.emptyList();
            }
            // 如果同时指定了 userId，取交集
            if (reqVO.getUserId() != null) {
                if (!userIds.contains(reqVO.getUserId())) {
                    return Collections.emptyList();
                }
            } else if (userIds.size() == 1) {
                reqVO.setUserId(userIds.get(0));
            }
        }

        // 2. 查询积分记录（不分页，导出全部）
        List<ForumPointRecordDO> records = pointRecordMapper.selectList(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<ForumPointRecordDO>()
                        .eqIfPresent(ForumPointRecordDO::getUserId, reqVO.getUserId())
                        .eqIfPresent(ForumPointRecordDO::getBizType, reqVO.getBizType())
                        .orderByDesc(ForumPointRecordDO::getId)
                        .last("LIMIT 10000") // 限制最大导出10000条
        );

        if (CollUtil.isEmpty(records)) {
            return Collections.emptyList();
        }

        // 3. 填充用户信息
        Set<Long> userIds = convertSet(records, ForumPointRecordDO::getUserId);
        List<ForumUserProfileDO> profiles = userProfileMapper.selectBatchIds(userIds);
        Map<Long, ForumUserProfileDO> profileMap = convertMap(profiles, ForumUserProfileDO::getUserId);

        // 4. 转换为 Excel VO
        return records.stream()
                .map(record -> {
                    AdminPointRecordExcelVO vo = new AdminPointRecordExcelVO();
                    vo.setId(record.getId());
                    vo.setTitle(record.getTitle());
                    vo.setDescription(record.getDescription());
                    vo.setPoint(record.getPoint());
                    vo.setTotalPoint(record.getTotalPoint());
                    vo.setCreateTime(record.getCreateTime());

                    // 填充用户信息
                    ForumUserProfileDO profile = profileMap.get(record.getUserId());
                    if (profile != null) {
                        vo.setUid(profile.getUid());
                        vo.setNickname(profile.getNickname());
                    }
                    return vo;
                })
                .collect(Collectors.toList());
    }

}
