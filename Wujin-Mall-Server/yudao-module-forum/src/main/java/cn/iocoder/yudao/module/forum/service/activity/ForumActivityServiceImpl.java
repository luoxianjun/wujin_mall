package cn.iocoder.yudao.module.forum.service.activity;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.forum.controller.admin.activity.vo.AdminActivitySignUpExportData;
import cn.iocoder.yudao.module.forum.controller.admin.activity.vo.AdminActivitySignUpExcelVO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizActivityDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizRewardRecordDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizRewardRuleDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizActivityMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizAttemptMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizRewardRecordMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizRewardRuleMapper;
import cn.iocoder.yudao.module.forum.controller.admin.activity.vo.AdminActivityUpdateReqVO;
import cn.iocoder.yudao.module.forum.controller.app.activity.vo.*;
import cn.iocoder.yudao.module.forum.controller.admin.activity.vo.AdminActivityFeedbackReqVO;
import cn.iocoder.yudao.module.forum.convert.activity.ForumActivityConvert;
import cn.iocoder.yudao.module.forum.controller.app.activity.vo.AppActivitySimpleRespVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.activity.ForumActivityDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.activity.ForumActivitySignUpDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.user.ForumUserProfileDO;
import cn.iocoder.yudao.module.forum.dal.mysql.activity.ForumActivityMapper;
import cn.iocoder.yudao.module.forum.dal.mysql.activity.ForumActivitySignUpMapper;
import cn.iocoder.yudao.module.forum.dal.mysql.user.ForumUserProfileMapper;
import cn.iocoder.yudao.module.forum.enums.activity.ActivityCategoryEnum;
import cn.iocoder.yudao.module.forum.enums.activity.ActivityCheckInTypeEnum;
import cn.iocoder.yudao.module.forum.enums.activity.ActivityStatusEnum;
import cn.iocoder.yudao.module.forum.enums.activity.ApprovalStatusEnum;
import cn.iocoder.yudao.module.forum.service.point.ForumPointService;
import cn.iocoder.yudao.module.forum.service.user.ForumUserProfileService;
import cn.iocoder.yudao.module.member.api.address.MemberAddressApi;
import cn.iocoder.yudao.module.member.api.address.dto.MemberAddressRespDTO;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.dal.mysql.user.MemberUserMapper;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.system.api.social.SocialClientApi;
import cn.iocoder.yudao.module.system.api.social.dto.SocialWxaSubscribeMessageSendReqDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Base64;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.forum.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 论坛活动 Service 实现类
 *
 * @author forum
 */
@Service
@Validated
@Slf4j
public class ForumActivityServiceImpl implements ForumActivityService {

    private static final String CHECK_IN_QR_DELIMITER = "|";

    @Resource
    private ForumActivityMapper activityMapper;

    @Resource
    private ForumActivitySignUpMapper signUpMapper;

    @Resource
    private QuizActivityMapper quizActivityMapper;

    @Resource
    private QuizRewardRuleMapper quizRewardRuleMapper;

    @Resource
    private QuizRewardRecordMapper quizRewardRecordMapper;

    @Resource
    private QuizAttemptMapper quizAttemptMapper;

    @Resource
    private ForumUserProfileMapper userProfileMapper;

    @Resource
    private ForumUserProfileService userProfileService;

    @Resource
    private ForumPointService pointService;

    @Resource
    private MemberUserMapper memberUserMapper;

    @Resource
    private MemberAddressApi memberAddressApi;

    @Resource
    private cn.iocoder.yudao.module.forum.service.text.ForumTextAuditService textAuditService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createActivity(Long userId, AppActivityCreateReqVO reqVO) {

        // 2. 解析并校验时间
        LocalDateTime startTime = parseDateTime(reqVO.getStartTime());
        LocalDateTime endTime = parseDateTime(reqVO.getEndTime());
        LocalDateTime signUpStartTime = parseDateTime(reqVO.getSignUpStartTime());
        LocalDateTime signUpEndTime = parseDateTime(reqVO.getSignUpEndTime());
        LocalDateTime checkInStartTime = parseDateTime(reqVO.getCheckInStartTime());
        LocalDateTime checkInEndTime = parseDateTime(reqVO.getCheckInEndTime());
        if (endTime != null && startTime != null && endTime.isBefore(startTime)) {
            throw ServiceExceptionUtil.exception(ACTIVITY_TIME_INVALID);
        }

        // 3. 构建活动对象
        ForumActivityDO activity = ForumActivityDO.builder()
                .userId(userId)
                .adminMemberIds(
                        CollUtil.isNotEmpty(reqVO.getAdminMemberIds()) ? JSONUtil.toJsonStr(reqVO.getAdminMemberIds())
                                : null)
                .title(reqVO.getTitle())
                .description(reqVO.getDescription())
                .requirements(reqVO.getRequirements())
                .coverImage(reqVO.getCoverImage())
                .detailImages(CollUtil.isNotEmpty(reqVO.getDetailImages()) ? JSONUtil.toJsonStr(reqVO.getDetailImages())
                        : null)
                .category(reqVO.getCategory())
                .location(reqVO.getLocation())
                .longitude(reqVO.getLongitude())
                .latitude(reqVO.getLatitude())
                .startTime(startTime)
                .endTime(endTime)
                .signUpStartTime(signUpStartTime)
                .signUpEndTime(signUpEndTime)
                .checkInStartTime(checkInStartTime)
                .checkInEndTime(checkInEndTime)
                .checkInDistance(reqVO.getCheckInDistance() != null ? reqVO.getCheckInDistance() : 100)
                .checkInType(reqVO.getCheckInType())
                .maxParticipants(reqVO.getMaxParticipants() != null ? reqVO.getMaxParticipants() : 0)
                .currentParticipants(0)
                .needApproval(reqVO.getNeedApproval() != null ? reqVO.getNeedApproval() : false)
                .schoolOnly(reqVO.getSchoolOnly() != null ? reqVO.getSchoolOnly() : false)
                .hot(reqVO.getHot() != null ? reqVO.getHot() : 0)
                .needPoint(reqVO.getNeedPoint() != null ? reqVO.getNeedPoint() : false)
                .pointAmount(reqVO.getPointAmount() != null ? reqVO.getPointAmount() : 0)
                .allowUnverified(reqVO.getAllowUnverified() != null ? reqVO.getAllowUnverified() : true)
                .showParticipantCount(reqVO.getShowParticipantCount() != null ? reqVO.getShowParticipantCount() : true)
                .redirectAppId(reqVO.getRedirectAppId())
                .redirectAppPath(reqVO.getRedirectAppPath())
                .redirectAppName(reqVO.getRedirectAppName())
                // 如果 customFields 为空字符串或 "[]"，视为 null
                .customFields(
                        (StrUtil.isNotBlank(reqVO.getCustomFields()) && !"[]".equals(reqVO.getCustomFields().trim()))
                                ? reqVO.getCustomFields()
                                : null)
                .status(ActivityStatusEnum.SIGN_UP.getStatus()) // 默认报名中
                .viewCount(0)
                .likeCount(0)
                .build();

        // 4. 插入活动
        activityMapper.insert(activity);

        log.info("[createActivity][创建活动成功，activityId={}, userId={}]", activity.getId(), userId);

        return activity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateActivity(Long userId, AdminActivityUpdateReqVO reqVO) {
        ForumActivityDO activity = activityMapper.selectById(reqVO.getId());
        if (activity == null) {
            throw ServiceExceptionUtil.exception(ACTIVITY_NOT_EXISTS);
        }

        LocalDateTime startTime = parseDateTime(reqVO.getStartTime());
        LocalDateTime endTime = parseDateTime(reqVO.getEndTime());
        LocalDateTime signUpStartTime = parseDateTime(reqVO.getSignUpStartTime());
        LocalDateTime signUpEndTime = parseDateTime(reqVO.getSignUpEndTime());
        LocalDateTime checkInStartTime = parseDateTime(reqVO.getCheckInStartTime());
        LocalDateTime checkInEndTime = parseDateTime(reqVO.getCheckInEndTime());
        startTime = startTime != null ? startTime : activity.getStartTime();
        endTime = endTime != null ? endTime : activity.getEndTime();
        signUpStartTime = signUpStartTime != null ? signUpStartTime : activity.getSignUpStartTime();
        signUpEndTime = signUpEndTime != null ? signUpEndTime : activity.getSignUpEndTime();
        checkInStartTime = checkInStartTime != null ? checkInStartTime : activity.getCheckInStartTime();
        checkInEndTime = checkInEndTime != null ? checkInEndTime : activity.getCheckInEndTime();
        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            throw ServiceExceptionUtil.exception(ACTIVITY_TIME_INVALID);
        }

        String detailImages = reqVO.getDetailImages() != null
                ? JSONUtil.toJsonStr(reqVO.getDetailImages())
                : activity.getDetailImages();
        ForumActivityDO updateObj = ForumActivityDO.builder()
                .id(reqVO.getId())
                .title(reqVO.getTitle() != null ? reqVO.getTitle() : activity.getTitle())
                .description(reqVO.getDescription() != null ? reqVO.getDescription() : activity.getDescription())
                .requirements(reqVO.getRequirements() != null ? reqVO.getRequirements() : activity.getRequirements())
                .adminMemberIds(
                        reqVO.getAdminMemberIds() != null ? JSONUtil.toJsonStr(reqVO.getAdminMemberIds())
                                : activity.getAdminMemberIds())
                .coverImage(reqVO.getCoverImage() != null ? reqVO.getCoverImage() : activity.getCoverImage())
                .detailImages(detailImages)
                .category(reqVO.getCategory() != null ? reqVO.getCategory() : activity.getCategory())
                .location(reqVO.getLocation() != null ? reqVO.getLocation() : activity.getLocation())
                .longitude(reqVO.getLongitude() != null ? reqVO.getLongitude() : activity.getLongitude())
                .latitude(reqVO.getLatitude() != null ? reqVO.getLatitude() : activity.getLatitude())
                .startTime(startTime)
                .endTime(endTime)
                .signUpStartTime(signUpStartTime)
                .signUpEndTime(signUpEndTime)
                .checkInStartTime(checkInStartTime)
                .checkInEndTime(checkInEndTime)
                .checkInDistance(reqVO.getCheckInDistance() != null ? reqVO.getCheckInDistance()
                        : (activity.getCheckInDistance() != null ? activity.getCheckInDistance() : 100))
                .checkInType(reqVO.getCheckInType() != null ? reqVO.getCheckInType() : activity.getCheckInType())
                .maxParticipants(reqVO.getMaxParticipants() != null ? reqVO.getMaxParticipants()
                        : (activity.getMaxParticipants() != null ? activity.getMaxParticipants() : 0))
                .needApproval(reqVO.getNeedApproval() != null ? reqVO.getNeedApproval() : activity.getNeedApproval())
                .schoolOnly(reqVO.getSchoolOnly() != null ? reqVO.getSchoolOnly() : activity.getSchoolOnly())
                .hot(reqVO.getHot() != null ? reqVO.getHot() : (activity.getHot() != null ? activity.getHot() : 0))
                .needPoint(reqVO.getNeedPoint() != null ? reqVO.getNeedPoint()
                        : (activity.getNeedPoint() != null ? activity.getNeedPoint() : false))
                .pointAmount(reqVO.getPointAmount() != null ? reqVO.getPointAmount()
                        : (activity.getPointAmount() != null ? activity.getPointAmount() : 0))
                .allowUnverified(reqVO.getAllowUnverified() != null ? reqVO.getAllowUnverified()
                        : (activity.getAllowUnverified() != null ? activity.getAllowUnverified() : true))
                .showParticipantCount(reqVO.getShowParticipantCount() != null ? reqVO.getShowParticipantCount()
                        : (activity.getShowParticipantCount() != null ? activity.getShowParticipantCount() : true))
                .hidden(reqVO.getHidden() != null ? reqVO.getHidden() : (activity.getHidden() != null ? activity.getHidden() : false))
                .redirectAppId(reqVO.getRedirectAppId() != null ? reqVO.getRedirectAppId() : activity.getRedirectAppId())
                .redirectAppPath(reqVO.getRedirectAppPath() != null ? reqVO.getRedirectAppPath() : activity.getRedirectAppPath())
                .redirectAppName(reqVO.getRedirectAppName() != null ? reqVO.getRedirectAppName() : activity.getRedirectAppName())
                .customFields(
                        // 如果 customFields 为空字符串或 "[]"，视为 null，保留原值
                        (StrUtil.isNotBlank(reqVO.getCustomFields()) && !"[]".equals(reqVO.getCustomFields().trim()))
                                ? reqVO.getCustomFields()
                                : activity.getCustomFields())
                .build();
        activityMapper.updateById(updateObj);

        log.info("[updateActivity][编辑活动成功，activityId={}, userId={}]", reqVO.getId(), userId);
    }

    @Override
    public AppActivityRespVO getActivity(Long activityId, Long userId) {
        // 1. 查询活动
        ForumActivityDO activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw ServiceExceptionUtil.exception(ACTIVITY_NOT_EXISTS);
        }

        // 2. 转换为 VO
        AppActivityRespVO respVO = buildActivityRespVO(activity, userId);

        // 3. 异步增加浏览次数
        increaseViewCount(activityId);

        return respVO;
    }

    @Override
    public PageResult<AppActivityRespVO> getActivityPage(AppActivityPageReqVO reqVO, Long userId) {
        // 调试日志：打印请求参数
        log.info("[getActivityPage] 请求参数 - status: {}, category: {}, keyword: {}",
                reqVO.getStatus(), reqVO.getCategory(), reqVO.getKeyword());

        // 1. 分页查询活动（状态根据时间过滤）
        PageResult<ForumActivityDO> pageResult = activityMapper.selectPage(reqVO);

        // 调试日志：打印查询结果
        if (pageResult.getList() != null && !pageResult.getList().isEmpty()) {
            log.info("[getActivityPage] 查询结果数量: {}", pageResult.getList().size());
        }

        // 2. 转换为 VO
        List<AppActivityRespVO> list = pageResult.getList().stream()
                .map(activity -> buildActivityRespVO(activity, userId))
                .collect(Collectors.toList());

        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    public PageResult<AppActivityRespVO> getVisibleActivityPage(AppActivityPageReqVO reqVO, Long userId) {
        log.info("[getVisibleActivityPage] request - status: {}, category: {}, keyword: {}",
                reqVO.getStatus(), reqVO.getCategory(), reqVO.getKeyword());

        PageResult<ForumActivityDO> pageResult = activityMapper.selectVisiblePage(reqVO);
        if (pageResult.getList() != null && !pageResult.getList().isEmpty()) {
            log.info("[getVisibleActivityPage] query result size: {}", pageResult.getList().size());
        }

        List<AppActivityRespVO> list = pageResult.getList().stream()
                .map(activity -> buildActivityRespVO(activity, userId))
                .collect(Collectors.toList());

        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    public List<AppActivitySimpleRespVO> getHotActivities(Integer limit) {
        int realLimit = (limit == null ? 10 : Math.min(Math.max(limit, 1), 50));
        List<ForumActivityDO> list = activityMapper.selectVisibleHotList(realLimit);
        return list.stream().map(activity -> {
            AppActivitySimpleRespVO vo = new AppActivitySimpleRespVO();
            vo.setId(activity.getId());
            vo.setTitle(activity.getTitle());
            vo.setDescription(activity.getDescription());
            vo.setCoverImage(activity.getCoverImage());
            vo.setRequirements(activity.getRequirements());
            vo.setNeedPoint(activity.getNeedPoint());
            vo.setPointAmount(activity.getPointAmount());
            vo.setLocation(activity.getLocation());
            vo.setStartTime(activity.getStartTime());
            vo.setEndTime(activity.getEndTime());
            fillQuizMarkers(vo, activity, null);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteActivity(Long activityId, Long userId) {
        // 1. 查询活动
        ForumActivityDO activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw ServiceExceptionUtil.exception(ACTIVITY_NOT_EXISTS);
        }

        // 2. 删除活动（web端管理功能，只要有权限即可删除，不限制只能删除自己的活动）
        activityMapper.deleteById(activityId);

        // 3. 减少活动创建者的活动数（而不是当前操作者的活动数）
        userProfileService.decreaseActivityCount(activity.getUserId());

        log.info("[deleteActivity][删除活动成功，activityId={}, operatorId={}, ownerId={}]", 
                activityId, userId, activity.getUserId());
    }

    @Override
    public void hideActivity(Long activityId, Long userId) {
        ForumActivityDO activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw ServiceExceptionUtil.exception(ACTIVITY_NOT_EXISTS);
        }
        ForumActivityDO updateObj = ForumActivityDO.builder()
                .id(activityId)
                .hidden(true)
                .build();
        activityMapper.updateById(updateObj);
        log.info("[hideActivity][隐藏活动成功，activityId={}, operatorId={}]", activityId, userId);
    }

    @Override
    public void showActivity(Long activityId, Long userId) {
        ForumActivityDO activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw ServiceExceptionUtil.exception(ACTIVITY_NOT_EXISTS);
        }
        ForumActivityDO updateObj = ForumActivityDO.builder()
                .id(activityId)
                .hidden(false)
                .build();
        activityMapper.updateById(updateObj);
        log.info("[showActivity][展示活动成功，activityId={}, operatorId={}]", activityId, userId);
    }

    /**
     * 解析管理员ID列表
     */
    private List<Long> parseAdminMemberIds(ForumActivityDO activity) {
        if (StrUtil.isNotBlank(activity.getAdminMemberIds())) {
            return JSONUtil.parseArray(activity.getAdminMemberIds()).stream()
                    .map(obj -> {
                        if (obj instanceof Number) {
                            return ((Number) obj).longValue();
                        }
                        return Long.valueOf(obj.toString());
                    })
                    .collect(Collectors.toList());
        }
        return CollUtil.newArrayList();
    }

    private LocalDateTime parseDateTime(String timeStr) {
        if (StrUtil.isBlank(timeStr)) {
            return null;
        }
        return LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern(FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND));
    }

    private void validateActivityCapacity(Long activityId, Integer maxParticipants) {
        if (maxParticipants == null || maxParticipants <= 0) {
            return;
        }
        Long occupiedCount = signUpMapper.countByActivityId(activityId);
        if (occupiedCount != null && occupiedCount >= maxParticipants) {
            throw ServiceExceptionUtil.exception(ACTIVITY_FULL);
        }
    }

    private boolean occupiesParticipantSlot(Integer approvalStatus) {
        return ApprovalStatusEnum.PENDING.getStatus().equals(approvalStatus)
                || ApprovalStatusEnum.APPROVED.getStatus().equals(approvalStatus);
    }

    private void refreshCurrentParticipants(Long activityId) {
        long occupiedCount = signUpMapper.countByActivityId(activityId);
        ForumActivityDO updateObj = ForumActivityDO.builder()
                .id(activityId)
                .currentParticipants(Math.toIntExact(occupiedCount))
                .build();
        activityMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void signUpActivity(Long userId, AppActivitySignUpReqVO reqVO) {
        // 1. 查询活动
        ForumActivityDO activity = activityMapper.selectByIdForUpdate(reqVO.getActivityId());
        if (activity == null) {
            throw ServiceExceptionUtil.exception(ACTIVITY_NOT_EXISTS);
        }

        // 2. 校验活动状态
        LocalDateTime now = LocalDateTime.now();

        // 2.1 校验活动是否已取消
        if (ActivityStatusEnum.CANCELLED.getStatus().equals(activity.getStatus())) {
            throw ServiceExceptionUtil.exception(ACTIVITY_CANCELLED);
        }

        // 2.2 校验活动是否已结束
        if (activity.getEndTime() != null && now.isAfter(activity.getEndTime())) {
            throw ServiceExceptionUtil.exception(ACTIVITY_ENDED);
        }

        // 2.3 校验报名时间
        if (activity.getSignUpStartTime() != null && now.isBefore(activity.getSignUpStartTime())) {
            throw ServiceExceptionUtil.exception(ACTIVITY_SIGN_UP_NOT_STARTED);
        }
        if (activity.getSignUpEndTime() != null && now.isAfter(activity.getSignUpEndTime())) {
            throw ServiceExceptionUtil.exception(ACTIVITY_SIGN_UP_ENDED);
        }

        // 3. 检查是否已报名或已取消报名
        ForumActivitySignUpDO existSignUp = signUpMapper
                .selectByActivityIdAndUserIdIncludeCancelled(reqVO.getActivityId(), userId);
        if (existSignUp != null) {
            // 如果是已取消状态(approvalStatus=3)，不允许重新报名
            if (existSignUp.getApprovalStatus() == 3) {
                throw ServiceExceptionUtil.exception(ACTIVITY_SIGN_UP_CANCELLED);
            }
            throw ServiceExceptionUtil.exception(ACTIVITY_ALREADY_SIGNED_UP);
        }

        // 4. 检查人数限制
        validateActivityCapacity(reqVO.getActivityId(), activity.getMaxParticipants());

        // 5. 报名信息内容审核
        // if (StrUtil.isNotBlank(reqVO.getRemark())) {
        // textAuditService.audit(reqVO.getRemark(), "活动报名信息");
        // }

        // 6. 创建报名记录
        ForumActivitySignUpDO signUp = ForumActivitySignUpDO.builder()
                .activityId(reqVO.getActivityId())
                .userId(userId)
                .remark(reqVO.getRemark())
                .approvalStatus(activity.getNeedApproval() ? 0 : 1) // 需要审核则待审核，否则直接通过
                .checkedIn(false)
                .build();
        signUpMapper.insert(signUp);

        // 7. 待审核和已通过都占用名额，报名成功后立即刷新占用人数
        refreshCurrentParticipants(reqVO.getActivityId());

        // 8. 如果不需要审核，直接发放奖励积分并增加用户活动数
        if (!activity.getNeedApproval()) {
            if (Boolean.TRUE.equals(activity.getNeedPoint()) && activity.getPointAmount() != null && activity.getPointAmount() > 0) {
                pointService.addActivitySignUpPoint(userId, activity.getPointAmount(), reqVO.getActivityId().toString());
            }

            // 9. 增加用户活动数
            userProfileService.increaseActivityCount(userId);
        }

        log.info("[signUpActivity][报名活动成功，activityId={}, userId={}]", reqVO.getActivityId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelSignUp(Long activityId, Long userId) {
        // 1. 查询报名记录
        ForumActivitySignUpDO signUp = signUpMapper.selectByActivityIdAndUserId(activityId, userId);
        if (signUp == null) {
            throw ServiceExceptionUtil.exception(ACTIVITY_NOT_SIGNED_UP);
        }

        boolean occupiedBeforeCancel = occupiesParticipantSlot(signUp.getApprovalStatus());
        if (occupiedBeforeCancel) {
            activityMapper.selectByIdForUpdate(activityId);
        }

        // 2. 更新报名状态为已取消(approvalStatus=3)，保留记录以阻止重新报名
        ForumActivitySignUpDO updateObj = ForumActivitySignUpDO.builder()
                .id(signUp.getId())
                .approvalStatus(3) // 3 = 已取消
                .build();
        signUpMapper.updateById(updateObj);

        // 3. 待审核和已通过都占用名额，取消后立即释放
        if (occupiedBeforeCancel) {
            refreshCurrentParticipants(activityId);
        }

        // 4. 如果已通过审核，减少用户活动数
        if (ApprovalStatusEnum.APPROVED.getStatus().equals(signUp.getApprovalStatus())) {
            userProfileService.decreaseActivityCount(userId);
        }

        log.info("[cancelSignUp][取消报名成功，activityId={}, userId={}]", activityId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkInActivity(Long userId, AppActivityCheckInReqVO reqVO) {
        // 1. 查询活动
        ForumActivityDO activity = activityMapper.selectById(reqVO.getActivityId());
        if (activity == null) {
            throw ServiceExceptionUtil.exception(ACTIVITY_NOT_EXISTS);
        }

        // 2. 查询报名记录
        ForumActivitySignUpDO signUp = signUpMapper.selectByActivityIdAndUserId(reqVO.getActivityId(), userId);
        if (signUp == null) {
            throw ServiceExceptionUtil.exception(ACTIVITY_NOT_SIGNED_UP);
        }

        // 3. 校验审核状态
        if (signUp.getApprovalStatus() != 1) {
            throw ServiceExceptionUtil.exception(ACTIVITY_SIGN_UP_NOT_APPROVED);
        }

        // 4. 检查是否已签到
        if (signUp.getCheckedIn()) {
            throw ServiceExceptionUtil.exception(ACTIVITY_ALREADY_CHECKED_IN);
        }

        // 5. 校验签到方式与时间
        Integer checkInType = resolveCheckInType(activity);
        if (ActivityCheckInTypeEnum.QR.getType().equals(checkInType)) {
            throw ServiceExceptionUtil.exception(ACTIVITY_CHECKIN_TYPE_MISMATCH);
        }
        LocalDateTime now = LocalDateTime.now();
        validateCheckInTime(activity, now);

        // 6. 定位签到需要校验距离
        if (ActivityCheckInTypeEnum.LOCATION.getType().equals(checkInType)) {
            validateLocationCheckIn(activity, reqVO);
        }

        // 7. 更新签到记录
        ForumActivitySignUpDO updateObj = ForumActivitySignUpDO.builder()
                .id(signUp.getId())
                .checkedIn(true)
                .checkInTime(now)
                .checkInLongitude(reqVO.getLongitude())
                .checkInLatitude(reqVO.getLatitude())
                .build();
        signUpMapper.updateById(updateObj);

        // 8. 增加签到积分
        pointService.addActivityCheckInPoint(userId, reqVO.getActivityId().toString());

        log.info("[checkInActivity][签到成功，activityId={}, userId={}]", reqVO.getActivityId(), userId);
    }

    @Override
    public String generateCheckInQrContent(Long userId, Long activityId) {
        // 1. 查询活动
        ForumActivityDO activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw ServiceExceptionUtil.exception(ACTIVITY_NOT_EXISTS);
        }

        // 2. 查询报名记录
        ForumActivitySignUpDO signUp = signUpMapper.selectByActivityIdAndUserId(activityId, userId);
        if (signUp == null) {
            throw ServiceExceptionUtil.exception(ACTIVITY_NOT_SIGNED_UP);
        }

        // 3. 校验审核及签到状态
        if (signUp.getApprovalStatus() != 1) {
            throw ServiceExceptionUtil.exception(ACTIVITY_SIGN_UP_NOT_APPROVED);
        }
        if (Boolean.TRUE.equals(signUp.getCheckedIn())) {
            throw ServiceExceptionUtil.exception(ACTIVITY_ALREADY_CHECKED_IN);
        }

        // 4. 校验签到方式
        Integer checkInType = resolveCheckInType(activity);
        if (!ActivityCheckInTypeEnum.QR.getType().equals(checkInType)) {
            throw ServiceExceptionUtil.exception(ACTIVITY_CHECKIN_TYPE_MISMATCH);
        }

        // 5. 生成二维码内容：activityId|signUpId|userId（Base64 编码）
        String payload = activityId + CHECK_IN_QR_DELIMITER + signUp.getId() + CHECK_IN_QR_DELIMITER + userId;
        return Base64.getEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkInActivityByQr(Long userId, String qrContent) {
        // 1. 解析二维码
        CheckInQrData qrData = parseCheckInQrContent(qrContent);

        // 2. 查询活动
        ForumActivityDO activity = activityMapper.selectById(qrData.getActivityId());
        if (activity == null) {
            throw ServiceExceptionUtil.exception(ACTIVITY_NOT_EXISTS);
        }

        // 3. 校验权限：活动创建者或系统管理员可扫码签到
        ForumUserProfileDO userProfile = userProfileMapper.selectByUserId(userId);
        boolean isSystemAdmin = userProfile != null && Boolean.TRUE.equals(userProfile.getIsAdmin());
        if (!activity.getUserId().equals(userId) && !isSystemAdmin) {
            throw ServiceExceptionUtil.exception(ACTIVITY_OPERATE_FAIL_NOT_OWNER);
        }

        // 4. 查询报名记录并校验关联
        ForumActivitySignUpDO signUp = signUpMapper.selectById(qrData.getSignUpId());
        if (signUp == null || !signUp.getActivityId().equals(activity.getId())
                || !signUp.getUserId().equals(qrData.getUserId())) {
            throw ServiceExceptionUtil.exception(ACTIVITY_CHECKIN_QR_INVALID);
        }

        // 5. 校验审核与时间
        if (signUp.getApprovalStatus() != 1) {
            throw ServiceExceptionUtil.exception(ACTIVITY_SIGN_UP_NOT_APPROVED);
        }

        Integer checkInType = resolveCheckInType(activity);
        if (!ActivityCheckInTypeEnum.QR.getType().equals(checkInType)) {
            throw ServiceExceptionUtil.exception(ACTIVITY_CHECKIN_TYPE_MISMATCH);
        }

        LocalDateTime now = LocalDateTime.now();
        validateCheckInTime(activity, now);

        if (Boolean.TRUE.equals(signUp.getCheckedIn())) {
            throw ServiceExceptionUtil.exception(ACTIVITY_ALREADY_CHECKED_IN);
        }

        // 6. 更新签到记录
        ForumActivitySignUpDO updateObj = ForumActivitySignUpDO.builder()
                .id(signUp.getId())
                .checkedIn(true)
                .checkInTime(now)
                .checkInLongitude(null)
                .checkInLatitude(null)
                .build();
        signUpMapper.updateById(updateObj);

        // 7. 增加签到积分
        pointService.addActivityCheckInPoint(signUp.getUserId(), signUp.getActivityId().toString());

        log.info("[checkInActivityByQr][扫码签到成功，activityId={}, signUpId={}, adminUserId={}]", activity.getId(),
                signUp.getId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkInActivityByAdmin(Long userId, AppActivityAdminCheckInReqVO reqVO) {
        // 1. 解析二维码
        CheckInQrData qrData = parseCheckInQrContent(reqVO.getQrContent());

        // 2. 校验活动ID是否匹配
        if (!qrData.getActivityId().equals(reqVO.getActivityId())) {
            throw ServiceExceptionUtil.exception(ACTIVITY_CHECKIN_QR_INVALID);
        }

        // 3. 调用原有扫码签到逻辑
        checkInActivityByQr(userId, reqVO.getQrContent());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void manualCheckIn(Long userId, Long activityId, Long signUpId) {
        // 1. 查询活动
        ForumActivityDO activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw ServiceExceptionUtil.exception(ACTIVITY_NOT_EXISTS);
        }

        // 2. 校验权限：活动创建者或系统管理员可手动签到
        ForumUserProfileDO userProfile = userProfileMapper.selectByUserId(userId);
        boolean isSystemAdmin = userProfile != null && Boolean.TRUE.equals(userProfile.getIsAdmin());
        if (!activity.getUserId().equals(userId) && !isSystemAdmin) {
            throw ServiceExceptionUtil.exception(ACTIVITY_OPERATE_FAIL_NOT_OWNER);
        }

        // 3. 查询报名记录并校验关联
        ForumActivitySignUpDO signUp = signUpMapper.selectById(signUpId);
        if (signUp == null || !signUp.getActivityId().equals(activityId)) {
            throw ServiceExceptionUtil.exception(ACTIVITY_NOT_SIGNED_UP);
        }

        // 4. 校验审核状态
        if (signUp.getApprovalStatus() != 1) {
            throw ServiceExceptionUtil.exception(ACTIVITY_SIGN_UP_NOT_APPROVED);
        }

        // 5. 检查是否已签到
        if (Boolean.TRUE.equals(signUp.getCheckedIn())) {
            throw ServiceExceptionUtil.exception(ACTIVITY_ALREADY_CHECKED_IN);
        }

        // 6. 更新签到记录
        LocalDateTime now = LocalDateTime.now();
        ForumActivitySignUpDO updateObj = ForumActivitySignUpDO.builder()
                .id(signUp.getId())
                .checkedIn(true)
                .checkInTime(now)
                .checkInLongitude(null)
                .checkInLatitude(null)
                .build();
        signUpMapper.updateById(updateObj);

        // 7. 增加签到积分
        pointService.addActivityCheckInPoint(signUp.getUserId(), signUp.getActivityId().toString());

        log.info("[manualCheckIn][手动签到成功，activityId={}, signUpId={}, adminUserId={}]", activityId, signUpId, userId);
    }

    @Override
    public void increaseViewCount(Long activityId) {
        ForumActivityDO activity = activityMapper.selectById(activityId);
        if (activity != null) {
            ForumActivityDO updateObj = ForumActivityDO.builder()
                    .id(activityId)
                    .viewCount(activity.getViewCount() + 1)
                    .build();
            activityMapper.updateById(updateObj);
        }
    }

    /**
     * 构建活动响应 VO
     */
    private AppActivityRespVO buildActivityRespVO(ForumActivityDO activity, Long userId) {
        AppActivityRespVO respVO = ForumActivityConvert.INSTANCE.convert(activity);
        LocalDateTime now = LocalDateTime.now();

        // 1. 设置分类名称
        ActivityCategoryEnum category = ActivityCategoryEnum.getByCategory(activity.getCategory());
        if (category != null) {
            respVO.setCategoryName(category.getName());
        }

        // 2. 根据时间动态计算状态并设置状态名称
        Integer dynamicStatus = calculateDynamicStatus(activity);
        respVO.setStatus(dynamicStatus);
        ActivityStatusEnum statusEnum = ActivityStatusEnum.getByStatus(dynamicStatus);
        if (statusEnum != null) {
            // 当状态为"未开始"时，显示名称为"报名结束"
            if (ActivityStatusEnum.NOT_STARTED.getStatus().equals(dynamicStatus)) {
                respVO.setStatusName("报名结束");
            } else {
                respVO.setStatusName(statusEnum.getName());
            }
        }

        // 3. 设置基于时间的便捷状态字段
        boolean isCancelled = ActivityStatusEnum.CANCELLED.getStatus().equals(activity.getStatus());
        respVO.setIsCancelled(isCancelled);

        // 是否可以报名
        boolean canSignUp = false;
        boolean signUpClosed = false;
        if (!isCancelled && activity.getSignUpStartTime() != null && activity.getSignUpEndTime() != null) {
            canSignUp = !now.isBefore(activity.getSignUpStartTime()) && !now.isAfter(activity.getSignUpEndTime());
            signUpClosed = now.isAfter(activity.getSignUpEndTime());
        }
        respVO.setCanSignUp(canSignUp);
        respVO.setSignUpClosed(signUpClosed);

        // 是否可以签到
        boolean canCheckIn = false;
        if (!isCancelled && activity.getCheckInStartTime() != null && activity.getCheckInEndTime() != null) {
            canCheckIn = !now.isBefore(activity.getCheckInStartTime()) && !now.isAfter(activity.getCheckInEndTime());
        }
        respVO.setCanCheckIn(canCheckIn);

        // 活动是否进行中
        boolean isOngoing = false;
        if (!isCancelled && activity.getStartTime() != null && activity.getEndTime() != null) {
            isOngoing = !now.isBefore(activity.getStartTime()) && !now.isAfter(activity.getEndTime());
        }
        respVO.setIsOngoing(isOngoing);

        // 活动是否已结束
        boolean isEnded = false;
        if (!isCancelled && activity.getEndTime() != null) {
            isEnded = now.isAfter(activity.getEndTime());
        }
        respVO.setIsEnded(isEnded);

        // 4. 设置详情图片列表
        if (activity.getDetailImages() != null) {
            respVO.setDetailImages(JSONUtil.toList(activity.getDetailImages(), String.class));
        }

        // 5. 设置发布人信息
        ForumUserProfileDO profile = userProfileMapper.selectByUserId(activity.getUserId());
        if (profile != null) {
            respVO.setUid(profile.getUid());
            respVO.setNickname(profile.getNickname());
            respVO.setAvatar(profile.getAvatar());
        }

        // 5. 设置管理员信息
        List<Long> adminMemberIds = parseAdminMemberIds(activity);
        respVO.setAdminMemberIds(adminMemberIds);
        respVO.setHidden(activity.getHidden());
        if (CollUtil.isNotEmpty(adminMemberIds)) {
            List<String> adminMemberNicknames = adminMemberIds.stream()
                    .map(memberUserMapper::selectById)
                    .filter(user -> user != null)
                    .map(MemberUserDO::getNickname)
                    .collect(Collectors.toList());
            respVO.setAdminMemberNicknames(adminMemberNicknames);

            // 设置管理员UID列表
            List<String> adminMemberUids = adminMemberIds.stream()
                    .map(adminMemberId -> {
                        ForumUserProfileDO adminProfile = userProfileMapper.selectByUserId(adminMemberId);
                        return adminProfile != null ? adminProfile.getUid() : null;
                    })
                    .filter(uid -> uid != null)
                    .collect(Collectors.toList());
            respVO.setAdminMemberUids(adminMemberUids);
        }

        // 7. 如果有当前用户，设置报名和签到状态
        if (userId != null) {
            ForumActivitySignUpDO signUp = signUpMapper.selectByActivityIdAndUserId(activity.getId(), userId);
            if (signUp != null) {
                respVO.setSignedUp(true);
                respVO.setApprovalStatus(signUp.getApprovalStatus());
                respVO.setApprovalRemark(signUp.getApprovalRemark());
                respVO.setCheckedIn(signUp.getCheckedIn());
                respVO.setSignUpRemark(signUp.getRemark());
            } else {
                respVO.setSignedUp(false);
                respVO.setCheckedIn(false);
            }
        } else {
            respVO.setSignedUp(false);
            respVO.setCheckedIn(false);
        }

        // 8. 使用报名表实时统计占用名额人数，避免缓存值不一致
        Long occupiedCount = signUpMapper.countByActivityId(activity.getId());
        respVO.setCurrentParticipants(occupiedCount != null ? occupiedCount.intValue() : 0);
        fillQuizMarkers(respVO, activity, userId);

        return respVO;
    }

    private void fillQuizMarkers(AppActivityRespVO respVO, ForumActivityDO activity, Long userId) {
        QuizActivityDO quizActivity = quizActivityMapper.selectByActivityId(activity.getId());
        if (quizActivity == null) {
            respVO.setHasQuiz(false);
            respVO.setQuizActivityId(null);
            respVO.setCanJoinQuiz(false);
            respVO.setQuizStatus(null);
            respVO.setQuizQuestionCount(null);
            respVO.setQuizMaxAttempts(null);
            respVO.setQuizDurationSeconds(null);
            respVO.setQuizLeaderboardSize(null);
            respVO.setQuizAnswerRevealMode(null);
            respVO.setQuizRewardRules(Collections.emptyList());
            respVO.setQuizRewardRecords(Collections.emptyList());
            respVO.setQuizRewardClaimGuide(null);
            respVO.setQuizDefaultAddress(null);
            return;
        }
        respVO.setHasQuiz(true);
        respVO.setQuizActivityId(quizActivity.getId());
        int usedAttempts = (userId != null) ? quizAttemptMapper.selectMaxAttemptNo(quizActivity.getId(), userId) : 0;
        respVO.setQuizUsedAttempts(usedAttempts);
        respVO.setCanJoinQuiz(canJoinQuiz(activity, quizActivity, userId, usedAttempts));
        respVO.setQuizStatus(quizActivity.getStatus());
        respVO.setQuizQuestionCount(quizActivity.getQuestionCount());
        respVO.setQuizMaxAttempts(quizActivity.getMaxAttempts());
        respVO.setQuizDurationSeconds(quizActivity.getDurationSeconds());
        respVO.setQuizLeaderboardSize(quizActivity.getLeaderboardSize());
        respVO.setQuizAnswerRevealMode(quizActivity.getAnswerRevealMode());
        List<QuizRewardRuleDO> rewardRules = quizRewardRuleMapper.selectByQuizActivityId(quizActivity.getId());
        respVO.setQuizRewardRules(rewardRules.stream().map(rule -> {
            AppActivityRespVO.QuizRewardRuleRespVO item = new AppActivityRespVO.QuizRewardRuleRespVO();
            item.setRankStart(rule.getRankStart());
            item.setRankEnd(rule.getRankEnd());
            item.setRewardType(rule.getRewardType());
            item.setPointAmount(rule.getPointAmount());
            item.setRewardName(rule.getRewardName());
            return item;
        }).collect(Collectors.toList()));
        boolean hasPhysicalReward = rewardRules.stream()
                .anyMatch(rule -> QuizRewardRuleDO.REWARD_TYPE_PHYSICAL.equals(rule.getRewardType()));
        if (rewardRules.isEmpty()) {
            respVO.setQuizRewardClaimGuide(null);
        } else if (hasPhysicalReward) {
            respVO.setQuizRewardClaimGuide(
                    "实物奖励将在活动结束并完成排名结算后发放，请提前维护默认收货地址，中奖后将按默认地址联系兑奖。");
        } else {
            respVO.setQuizRewardClaimGuide("积分奖励将在活动结束后按排行榜自动发放至积分账户。");
        }
        if (userId == null) {
            respVO.setQuizRewardRecords(Collections.emptyList());
            respVO.setQuizDefaultAddress(null);
            return;
        }
        respVO.setQuizRewardRecords(quizRewardRecordMapper
                .selectByQuizActivityIdAndUserId(quizActivity.getId(), userId)
                .stream()
                .map(record -> {
                    AppActivityRespVO.QuizRewardRecordRespVO item = new AppActivityRespVO.QuizRewardRecordRespVO();
                    item.setRewardType(record.getRewardType());
                    item.setPointAmount(record.getPointAmount());
                    item.setRewardName(record.getRewardName());
                    item.setStatus(record.getStatus());
                    item.setDistributedAt(record.getDistributedAt());
                    return item;
                }).collect(Collectors.toList()));
        if (!hasPhysicalReward) {
            respVO.setQuizDefaultAddress(null);
            return;
        }
        MemberAddressRespDTO address = memberAddressApi.getDefaultAddress(userId);
        if (address == null) {
            respVO.setQuizDefaultAddress(null);
            return;
        }
        AppActivityRespVO.QuizDefaultAddressRespVO addressRespVO = new AppActivityRespVO.QuizDefaultAddressRespVO();
        addressRespVO.setId(address.getId());
        addressRespVO.setName(address.getName());
        addressRespVO.setMobile(address.getMobile());
        addressRespVO.setDetailAddress(address.getDetailAddress());
        respVO.setQuizDefaultAddress(addressRespVO);
    }

    private void fillQuizMarkers(AppActivitySimpleRespVO respVO, ForumActivityDO activity, Long userId) {
        QuizActivityDO quizActivity = quizActivityMapper.selectByActivityId(activity.getId());
        if (quizActivity == null) {
            respVO.setHasQuiz(false);
            respVO.setQuizActivityId(null);
            respVO.setCanJoinQuiz(false);
            respVO.setQuizStatus(null);
            respVO.setQuizQuestionCount(null);
            respVO.setQuizMaxAttempts(null);
            respVO.setQuizDurationSeconds(null);
            respVO.setQuizLeaderboardSize(null);
            respVO.setQuizAnswerRevealMode(null);
            return;
        }
        respVO.setHasQuiz(true);
        respVO.setQuizActivityId(quizActivity.getId());
        int usedAttemptsSimple = (userId != null) ? quizAttemptMapper.selectMaxAttemptNo(quizActivity.getId(), userId) : 0;
        respVO.setQuizUsedAttempts(usedAttemptsSimple);
        respVO.setCanJoinQuiz(canJoinQuiz(activity, quizActivity, userId, usedAttemptsSimple));
        respVO.setQuizStatus(quizActivity.getStatus());
        respVO.setQuizQuestionCount(quizActivity.getQuestionCount());
        respVO.setQuizMaxAttempts(quizActivity.getMaxAttempts());
        respVO.setQuizDurationSeconds(quizActivity.getDurationSeconds());
        respVO.setQuizLeaderboardSize(quizActivity.getLeaderboardSize());
        respVO.setQuizAnswerRevealMode(quizActivity.getAnswerRevealMode());
    }

    private boolean canJoinQuiz(ForumActivityDO activity, QuizActivityDO quizActivity, Long userId, int usedAttempts) {
        if (userId == null || !QuizActivityDO.STATUS_ENABLED.equals(quizActivity.getStatus())) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        boolean afterStart = activity.getStartTime() == null || !now.isBefore(activity.getStartTime());
        boolean beforeEnd = activity.getEndTime() == null || !now.isAfter(activity.getEndTime());
        if (!afterStart || !beforeEnd) {
            return false;
        }
        if (quizActivity.getMaxAttempts() != null && usedAttempts >= quizActivity.getMaxAttempts()) {
            return false;
        }
        return true;
    }

    /**
     * 根据时间动态计算活动状态
     * - 已取消(4)：数据库中 status = 4，优先级最高
     * - 报名中(1)：当前时间在报名时间范围内
     * - 进行中(2)：当前时间在活动时间范围内
     * - 已结束(3)：当前时间已过活动结束时间
     * - 未开始(5)：报名已结束但活动还未开始
     * - 草稿(0)：其他情况（活动尚未开始报名）
     */
    private Integer calculateDynamicStatus(ForumActivityDO activity) {
        // 已取消状态从数据库读取，优先级最高
        if (ActivityStatusEnum.CANCELLED.getStatus().equals(activity.getStatus())) {
            return ActivityStatusEnum.CANCELLED.getStatus();
        }

        LocalDateTime now = LocalDateTime.now();

        // 检查是否在报名时间范围内
        LocalDateTime signUpStart = activity.getSignUpStartTime();
        LocalDateTime signUpEnd = activity.getSignUpEndTime();
        if (signUpStart != null && signUpEnd != null) {
            if (!now.isBefore(signUpStart) && !now.isAfter(signUpEnd)) {
                return ActivityStatusEnum.SIGN_UP.getStatus(); // 报名中
            }
        }

        // 检查是否在活动进行时间范围内
        LocalDateTime startTime = activity.getStartTime();
        LocalDateTime endTime = activity.getEndTime();
        if (startTime != null && endTime != null) {
            if (!now.isBefore(startTime) && !now.isAfter(endTime)) {
                return ActivityStatusEnum.IN_PROGRESS.getStatus(); // 进行中
            }
            // 活动已结束
            if (now.isAfter(endTime)) {
                return ActivityStatusEnum.ENDED.getStatus(); // 已结束
            }
        }

        // 检查是否报名已结束但活动还未开始
        if (signUpEnd != null && startTime != null) {
            if (now.isAfter(signUpEnd) && now.isBefore(startTime)) {
                return ActivityStatusEnum.NOT_STARTED.getStatus(); // 未开始
            }
        }

        // 默认返回草稿状态（活动尚未开始报名）
        return ActivityStatusEnum.DRAFT.getStatus();
    }

    private Integer resolveCheckInType(ForumActivityDO activity) {
        Integer type = activity.getCheckInType();
        if (type == null || ActivityCheckInTypeEnum.getName(type) == null) {
            return ActivityCheckInTypeEnum.SELF.getType();
        }
        return type;
    }

    private void validateCheckInTime(ForumActivityDO activity, LocalDateTime now) {
        if (activity.getCheckInStartTime() != null && now.isBefore(activity.getCheckInStartTime())) {
            throw ServiceExceptionUtil.exception(ACTIVITY_CHECKIN_NOT_STARTED);
        }
        if (activity.getCheckInEndTime() != null && now.isAfter(activity.getCheckInEndTime())) {
            throw ServiceExceptionUtil.exception(ACTIVITY_CHECKIN_ENDED);
        }
    }

    private void validateLocationCheckIn(ForumActivityDO activity, AppActivityCheckInReqVO reqVO) {
        if (reqVO.getLongitude() == null || reqVO.getLatitude() == null) {
            throw ServiceExceptionUtil.exception(ACTIVITY_CHECKIN_LOCATION_REQUIRED);
        }
        if (activity.getLongitude() == null || activity.getLatitude() == null) {
            throw ServiceExceptionUtil.exception(ACTIVITY_CHECKIN_LOCATION_NOT_SET);
        }
        Integer distanceLimit = activity.getCheckInDistance();
        if (distanceLimit == null || distanceLimit <= 0) {
            return;
        }
        double distance = calculateDistance(
                activity.getLatitude(), activity.getLongitude(),
                reqVO.getLatitude(), reqVO.getLongitude());
        if (distance > distanceLimit) {
            throw ServiceExceptionUtil.exception(ACTIVITY_CHECKIN_LOCATION_TOO_FAR);
        }
    }

    /**
     * 计算两点之间的距离（米）
     * 使用 Haversine 公式
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // 地球半径（米）

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    private CheckInQrData parseCheckInQrContent(String qrContent) {
        try {
            String decoded = new String(Base64.getDecoder().decode(qrContent), StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\|");
            if (parts.length != 3) {
                throw ServiceExceptionUtil.exception(ACTIVITY_CHECKIN_QR_INVALID);
            }
            return new CheckInQrData(Long.valueOf(parts[0]), Long.valueOf(parts[1]), Long.valueOf(parts[2]));
        } catch (Exception ex) {
            throw ServiceExceptionUtil.exception(ACTIVITY_CHECKIN_QR_INVALID);
        }
    }

    private static final class CheckInQrData {
        private final Long activityId;
        private final Long signUpId;
        private final Long userId;

        private CheckInQrData(Long activityId, Long signUpId, Long userId) {
            this.activityId = activityId;
            this.signUpId = signUpId;
            this.userId = userId;
        }

        public Long getActivityId() {
            return activityId;
        }

        public Long getSignUpId() {
            return signUpId;
        }

        public Long getUserId() {
            return userId;
        }
    }

    @Override
    public PageResult<AppActivitySignUpRespVO> getActivitySignUpPage(AppActivitySignUpPageReqVO reqVO, Long userId) {
        // 1. 校验活动是否存在
        if (reqVO.getActivityId() == null) {
            throw ServiceExceptionUtil.exception(ACTIVITY_NOT_EXISTS);
        }

        ForumActivityDO activity = activityMapper.selectById(reqVO.getActivityId());
        if (activity == null) {
            throw ServiceExceptionUtil.exception(ACTIVITY_NOT_EXISTS);
        }

        // 3. 分页查询报名列表
        PageResult<ForumActivitySignUpDO> pageResult = signUpMapper.selectPage(reqVO);

        // 4. 转换为 VO
        List<AppActivitySignUpRespVO> list = pageResult.getList().stream()
                .map(signUp -> buildSignUpRespVO(signUp, activity))
                .collect(Collectors.toList());

        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    public PageResult<AppActivitySignUpRespVO> getActivityMembersPage(AppActivitySignUpPageReqVO reqVO) {
        // 1. 校验活动是否存在
        if (reqVO.getActivityId() == null) {
            throw ServiceExceptionUtil.exception(ACTIVITY_NOT_EXISTS);
        }

        ForumActivityDO activity = activityMapper.selectById(reqVO.getActivityId());
        if (activity == null) {
            throw ServiceExceptionUtil.exception(ACTIVITY_NOT_EXISTS);
        }

        // 2. 分页查询报名列表
        PageResult<ForumActivitySignUpDO> pageResult = signUpMapper.selectPage(reqVO);

        // 3. 转换为 VO
        List<AppActivitySignUpRespVO> list = pageResult.getList().stream()
                .map(signUp -> buildSignUpRespVO(signUp, activity))
                .collect(Collectors.toList());

        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    public PageResult<AppActivitySignUpRespVO> getMySignUpPage(Long userId, AppActivitySignUpPageReqVO reqVO) {
        // 1. 设置查询条件为当前用户
        reqVO.setActivityId(null); // 清空活动ID，查询所有活动的报名

        // 2. 分页查询报名列表
        PageResult<ForumActivitySignUpDO> pageResult = signUpMapper.selectPageByUserId(reqVO, userId);

        // 3. 批量查询活动信息（忽略逻辑删除，确保已删除的活动也能展示标题和封面）
        Set<Long> activityIds = pageResult.getList().stream()
                .map(ForumActivitySignUpDO::getActivityId)
                .collect(Collectors.toSet());
        Map<Long, ForumActivityDO> activityMap = activityMapper.selectBatchByIdsIgnoreDeletedSafe(activityIds)
                .stream()
                .collect(Collectors.toMap(ForumActivityDO::getId, a -> a));

        // 4. 转换为 VO
        List<AppActivitySignUpRespVO> list = pageResult.getList().stream()
                .map(signUp -> {
                    ForumActivityDO activity = activityMap.get(signUp.getActivityId());
                    return buildSignUpRespVO(signUp, activity);
                })
                .collect(Collectors.toList());

        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveSignUp(Long userId, AppActivityApprovalReqVO reqVO) {
        // 1. 查询报名记录
        ForumActivitySignUpDO signUp = signUpMapper.selectById(reqVO.getSignUpId());
        if (signUp == null) {
            throw ServiceExceptionUtil.exception(ACTIVITY_NOT_SIGNED_UP);
        }

        // 2. 查询活动
        ForumActivityDO activity = activityMapper.selectByIdForUpdate(signUp.getActivityId());
        if (activity == null) {
            throw ServiceExceptionUtil.exception(ACTIVITY_NOT_EXISTS);
        }

        // 4. 校验审核状态
        if (reqVO.getApprovalStatus() != 1 && reqVO.getApprovalStatus() != 2) {
            throw ServiceExceptionUtil.exception(ACTIVITY_TIME_INVALID);
        }

        Integer oldApprovalStatus = signUp.getApprovalStatus();
        Integer newApprovalStatus = reqVO.getApprovalStatus();
        boolean occupiedBeforeReview = occupiesParticipantSlot(oldApprovalStatus);
        boolean occupiedAfterReview = occupiesParticipantSlot(newApprovalStatus);

        // 5. 只有从不占名额切换为占名额时，才需要再次校验人数上限
        if (occupiedAfterReview && !occupiedBeforeReview) {
            validateActivityCapacity(signUp.getActivityId(), activity.getMaxParticipants());
        }

        // 6. 更新审核状态
        ForumActivitySignUpDO updateObj = ForumActivitySignUpDO.builder()
                .id(reqVO.getSignUpId())
                .approvalStatus(newApprovalStatus)
                .approvalRemark(reqVO.getApprovalRemark())
                .build();
        signUpMapper.updateById(updateObj);

        // 7. 名额占用状态发生变化时，实时刷新占用人数
        if (occupiedBeforeReview != occupiedAfterReview) {
            refreshCurrentParticipants(signUp.getActivityId());
        }

        // 8. 审核通过后发放报名奖励积分
        if (ApprovalStatusEnum.APPROVED.getStatus().equals(newApprovalStatus)
                && !ApprovalStatusEnum.APPROVED.getStatus().equals(oldApprovalStatus)) {
            if (Boolean.TRUE.equals(activity.getNeedPoint()) && activity.getPointAmount() != null && activity.getPointAmount() > 0) {
                pointService.addActivitySignUpPoint(signUp.getUserId(), activity.getPointAmount(), signUp.getActivityId().toString());
            }
        }

        log.info("[approveSignUp][审核报名成功，signUpId={}, approvalStatus={}]", reqVO.getSignUpId(),
                reqVO.getApprovalStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void feedbackSignUp(Long userId, AdminActivityFeedbackReqVO reqVO) {
        // 1. 查询报名记录
        ForumActivitySignUpDO signUp = signUpMapper.selectById(reqVO.getSignUpId());
        if (signUp == null) {
            throw ServiceExceptionUtil.exception(ACTIVITY_NOT_SIGNED_UP);
        }

        // 2. 查询活动（web端管理功能，只要有权限即可点评，不限制只能点评自己的活动）
        ForumActivityDO activity = activityMapper.selectById(signUp.getActivityId());
        if (activity == null) {
            throw ServiceExceptionUtil.exception(ACTIVITY_NOT_EXISTS);
        }

        // 3. 更新反馈
        ForumActivitySignUpDO updateObj = ForumActivitySignUpDO.builder()
                .id(reqVO.getSignUpId())
                .feedback(reqVO.getFeedback())
                .build();
        signUpMapper.updateById(updateObj);

        log.info("[feedbackSignUp][点评报名记录成功，signUpId={}, userId={}]", reqVO.getSignUpId(), userId);
    }

    /**
     * 构建报名响应 VO
     */
    private AppActivitySignUpRespVO buildSignUpRespVO(ForumActivitySignUpDO signUp, ForumActivityDO activity) {
        // 使用 MapStruct 转换基础字段
        AppActivitySignUpRespVO respVO = ForumActivityConvert.INSTANCE.convertSignUp(signUp);

        // 设置审核状态名称
        ApprovalStatusEnum approvalStatus = ApprovalStatusEnum.getByStatus(signUp.getApprovalStatus());
        if (approvalStatus != null) {
            respVO.setApprovalStatusName(approvalStatus.getName());
        }

        // 设置活动信息
        if (activity != null) {
            respVO.setActivityTitle(activity.getTitle());
            respVO.setCoverImage(activity.getCoverImage());
            respVO.setNeedCheckIn(activity.getCheckInStartTime() != null
                    || activity.getCheckInEndTime() != null
                    || activity.getCheckInType() != null);
            // 设置签到相关字段
            respVO.setCheckInType(activity.getCheckInType());
            respVO.setLatitude(activity.getLatitude());
            respVO.setLongitude(activity.getLongitude());
            respVO.setCheckInDistance(activity.getCheckInDistance());
        }

        // 设置用户信息
        ForumUserProfileDO profile = userProfileMapper.selectByUserId(signUp.getUserId());
        if (profile != null) {
            respVO.setUid(profile.getUid());
            respVO.setNickname(profile.getNickname());
            respVO.setAvatar(profile.getAvatar());
        }

        return respVO;
    }

    @Resource
    private SocialClientApi socialClientApi;

    @Override
    public List<AdminActivitySignUpExcelVO> getActivitySignUpExcelList(AppActivitySignUpPageReqVO reqVO, Long userId) {
        // 1. 校验活动是否存在（如果指定了活动ID）
        if (reqVO.getActivityId() != null) {
            ForumActivityDO activity = activityMapper.selectById(reqVO.getActivityId());
            if (activity == null) {
                throw ServiceExceptionUtil.exception(ACTIVITY_NOT_EXISTS);
            }
        }

        // 2. 查询所有报名记录（不分页，导出全部）
        AppActivitySignUpPageReqVO exportReqVO = new AppActivitySignUpPageReqVO();
        exportReqVO.setActivityId(reqVO.getActivityId());
        exportReqVO.setApprovalStatus(reqVO.getApprovalStatus());
        exportReqVO.setPageNo(1);
        exportReqVO.setPageSize(10000); // 限制最大导出10000条

        PageResult<ForumActivitySignUpDO> pageResult = signUpMapper.selectPage(exportReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return Collections.emptyList();
        }

        // 3. 批量查询活动信息
        Set<Long> activityIds = pageResult.getList().stream()
                .map(ForumActivitySignUpDO::getActivityId)
                .collect(Collectors.toSet());
        Map<Long, ForumActivityDO> activityMap = activityMapper.selectBatchByIdsIgnoreDeletedSafe(activityIds)
                .stream()
                .collect(Collectors.toMap(ForumActivityDO::getId, a -> a));

        // 4. 转换为 Excel VO
        return pageResult.getList().stream()
                .map(signUp -> {
                    ForumActivityDO activity = activityMap.get(signUp.getActivityId());
                    return buildSignUpExcelVO(signUp, activity);
                })
                .collect(Collectors.toList());
    }

    @Override
    public AdminActivitySignUpExportData getActivitySignUpExportData(AppActivitySignUpPageReqVO reqVO, Long userId) {
        ForumActivityDO requestedActivity = null;
        if (reqVO.getActivityId() != null) {
            requestedActivity = activityMapper.selectById(reqVO.getActivityId());
            if (requestedActivity == null) {
                throw ServiceExceptionUtil.exception(ACTIVITY_NOT_EXISTS);
            }
        }

        AppActivitySignUpPageReqVO exportReqVO = new AppActivitySignUpPageReqVO();
        exportReqVO.setActivityId(reqVO.getActivityId());
        exportReqVO.setApprovalStatus(reqVO.getApprovalStatus());
        exportReqVO.setPageNo(1);
        exportReqVO.setPageSize(10000);

        PageResult<ForumActivitySignUpDO> pageResult = signUpMapper.selectPage(exportReqVO);
        List<ForumActivitySignUpDO> signUps = pageResult.getList();

        Map<Long, ForumActivityDO> activityMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(signUps)) {
            Set<Long> activityIds = signUps.stream()
                    .map(ForumActivitySignUpDO::getActivityId)
                    .collect(Collectors.toSet());
            activityMap = activityMapper.selectBatchByIdsIgnoreDeletedSafe(activityIds)
                    .stream()
                    .collect(Collectors.toMap(ForumActivityDO::getId, a -> a));
        }

        List<JSONObject> customFields = parseActivityCustomFields(requestedActivity);
        if (CollUtil.isEmpty(customFields) && requestedActivity == null && CollUtil.isNotEmpty(activityMap)) {
            customFields = activityMap.values().stream()
                    .flatMap(activity -> parseActivityCustomFields(activity).stream())
                    .collect(Collectors.toList());
        }

        List<List<String>> head = buildSignUpExportHead(customFields);
        List<List<Object>> rows = buildSignUpExportRows(signUps, activityMap, customFields);
        return new AdminActivitySignUpExportData(head, rows);
    }

    private List<List<String>> buildSignUpExportHead(List<JSONObject> customFields) {
        List<List<String>> head = new ArrayList<>();
        for (String title : Arrays.asList("报名ID", "活动标题", "用户UID", "昵称", "报名备注",
                "审核状态", "审核备注", "是否签到", "签到时间", "报名时间")) {
            head.add(Collections.singletonList(title));
        }
        for (JSONObject field : customFields) {
            String key = field.getStr("key");
            if (StrUtil.isBlank(key)) {
                continue;
            }
            head.add(Collections.singletonList(key));
        }
        return head;
    }

    private List<List<Object>> buildSignUpExportRows(List<ForumActivitySignUpDO> signUps,
                                                     Map<Long, ForumActivityDO> activityMap,
                                                     List<JSONObject> customFields) {
        if (CollUtil.isEmpty(signUps)) {
            return Collections.emptyList();
        }
        return signUps.stream().map(signUp -> {
            ForumActivityDO activity = activityMap.get(signUp.getActivityId());
            ForumUserProfileDO profile = userProfileMapper.selectByUserId(signUp.getUserId());
            JSONObject remarkJson = parseSignUpRemark(signUp.getRemark());

            List<Object> row = new ArrayList<>();
            row.add(signUp.getId());
            row.add(activity != null ? activity.getTitle() : null);
            row.add(profile != null ? profile.getUid() : null);
            row.add(profile != null ? profile.getNickname() : null);
            row.add(signUp.getRemark());
            row.add(ApprovalStatusEnum.getNameByStatus(signUp.getApprovalStatus()));
            row.add(signUp.getApprovalRemark());
            row.add(signUp.getCheckedIn() != null && signUp.getCheckedIn() ? "已签到" : "未签到");
            row.add(formatExportTime(signUp.getCheckInTime()));
            row.add(formatExportTime(signUp.getCreateTime()));

            for (JSONObject field : customFields) {
                row.add(extractSignUpRemarkValue(remarkJson, field));
            }
            return row;
        }).collect(Collectors.toList());
    }

    private List<JSONObject> parseActivityCustomFields(ForumActivityDO activity) {
        if (activity == null || StrUtil.isBlank(activity.getCustomFields())
                || "[]".equals(activity.getCustomFields().trim())) {
            return Collections.emptyList();
        }
        try {
            JSONArray array = JSONUtil.parseArray(activity.getCustomFields());
            List<JSONObject> fields = new ArrayList<>();
            for (Object item : array) {
                if (item instanceof JSONObject) {
                    JSONObject field = (JSONObject) item;
                    if (StrUtil.isNotBlank(field.getStr("key"))) {
                        fields.add(field);
                    }
                }
            }
            return fields;
        } catch (Exception e) {
            log.warn("[parseActivityCustomFields][活动自定义字段解析失败，activityId={}]", activity.getId(), e);
            return Collections.emptyList();
        }
    }

    private JSONObject parseSignUpRemark(String remark) {
        if (StrUtil.isBlank(remark)) {
            return new JSONObject();
        }
        try {
            Object parsed = JSONUtil.parse(remark);
            return parsed instanceof JSONObject ? (JSONObject) parsed : new JSONObject();
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    private Object extractSignUpRemarkValue(JSONObject remarkJson, JSONObject field) {
        String key = field.getStr("key");
        if (StrUtil.isBlank(key) || remarkJson == null || !remarkJson.containsKey(key)) {
            return null;
        }
        Object value = remarkJson.get(key);
        if (value == null) {
            return null;
        }
        if ("file".equals(field.getStr("type"))) {
            if (value instanceof JSONObject) {
                JSONObject file = (JSONObject) value;
                return StrUtil.blankToDefault(file.getStr("url"), file.getStr("name"));
            }
            return value.toString();
        }
        if (value instanceof JSONArray) {
            return ((JSONArray) value).stream()
                    .map(Object::toString)
                    .collect(Collectors.joining("、"));
        }
        if (value instanceof JSONObject) {
            JSONObject object = (JSONObject) value;
            return StrUtil.blankToDefault(object.getStr("url"), object.toString());
        }
        return value;
    }

    private String formatExportTime(LocalDateTime time) {
        return time == null ? null : time.format(DateTimeFormatter.ofPattern(FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND));
    }

    /**
     * 构建报名 Excel VO
     */
    private AdminActivitySignUpExcelVO buildSignUpExcelVO(ForumActivitySignUpDO signUp, ForumActivityDO activity) {
        AdminActivitySignUpExcelVO vo = new AdminActivitySignUpExcelVO();
        vo.setId(signUp.getId());
        vo.setActivityTitle(activity != null ? activity.getTitle() : null);
        vo.setRemark(signUp.getRemark());
        vo.setApprovalStatusName(ApprovalStatusEnum.getNameByStatus(signUp.getApprovalStatus()));
        vo.setApprovalRemark(signUp.getApprovalRemark());
        vo.setCheckedIn(signUp.getCheckedIn() != null && signUp.getCheckedIn() ? "已签到" : "未签到");
        vo.setCheckInTime(signUp.getCheckInTime());
        vo.setCreateTime(signUp.getCreateTime());

        // 设置用户信息
        ForumUserProfileDO profile = userProfileMapper.selectByUserId(signUp.getUserId());
        if (profile != null) {
            vo.setUid(profile.getUid());
            vo.setNickname(profile.getNickname());
        }

        return vo;
    }

    @Override
    public int notifyActivityStartIn2Hours() {
        LocalDateTime now = LocalDateTime.now();
        // 查询未来2小时内开始且尚未发送过通知的活动
        // 这样可以确保：
        // 1. 新创建的活动即使距离开始不足2小时也能被通知到
        // 2. 每个活动只会被通知一次，不会重复
        LocalDateTime endTime = now.plusHours(2);
        
        // 查询符合条件的活动
        List<ForumActivityDO> activities = activityMapper.selectActivitiesNeedNotify(now, endTime);
        if (CollUtil.isEmpty(activities)) {
            log.info("[notifyActivityStartIn2Hours][没有找到需要通知的活动]");
            return 0;
        }
        
        int totalNotifyCount = 0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (ForumActivityDO activity : activities) {
            try {
                // 先标记活动为已通知，避免下次重复处理
                activityMapper.updateStartNotified(activity.getId(), true);
                
                // 查询已通过审核的报名用户
                List<Long> userIds = signUpMapper.selectApprovedUserIdsByActivityId(activity.getId());
                if (CollUtil.isEmpty(userIds)) {
                    log.info("[notifyActivityStartIn2Hours][活动无报名用户，跳过，activityId={}]", activity.getId());
                    continue;
                }
                
                // 格式化活动时间
                String activityTime = activity.getStartTime().format(formatter);
                String activityLocation = StrUtil.blankToDefault(activity.getLocation(), "待定");
                
                // 为每个用户发送通知
                // 模板ID: YDVLF5bjMcVK8ifB0bgmJjiIYHtP5txjNtmsK4K3htA
                // 模板字段：thing4(活动名称)、date3(开始时间)、thing6(活动地点)、thing2(活动内容)
                for (Long userId : userIds) {
                    try {
                        // 构建消息内容
                        SocialWxaSubscribeMessageSendReqDTO reqDTO = new SocialWxaSubscribeMessageSendReqDTO()
                                .setUserId(userId)
                                .setUserType(UserTypeEnum.MEMBER.getValue())
                                .setTemplateTitle("活动开始通知") // 模板标题，需要与微信后台配置一致
                                .setPage("pages/events/register?id=" + activity.getId()); // 跳转到活动详情页
                        
                        // 添加模板参数（根据模板字段配置）
                        reqDTO.addMessage("thing4", StrUtil.maxLength(activity.getTitle(), 20)); // 活动名称，限制20字符
                        reqDTO.addMessage("date3", activityTime); // 开始时间，格式：yyyy-MM-dd HH:mm
                        reqDTO.addMessage("thing6", StrUtil.maxLength(activityLocation, 20)); // 活动地点，限制20字符
                        // 活动内容：使用活动描述或报名要求，去除HTML标签，如果没有则使用默认值
                        String activityContent = "";
                        if (StrUtil.isNotBlank(activity.getDescription())) {
                            // 去除HTML标签，只保留纯文本
                            activityContent = activity.getDescription()
                                    .replaceAll("<[^>]+>", "") // 去除HTML标签
                                    .replaceAll("&nbsp;", " ") // 替换&nbsp;为空格
                                    .replaceAll("&amp;", "&") // 替换&amp;为&
                                    .replaceAll("&lt;", "<") // 替换&lt;为<
                                    .replaceAll("&gt;", ">") // 替换&gt;为>
                                    .replaceAll("&quot;", "\"") // 替换&quot;为"
                                    .replaceAll("\\s+", " ") // 合并多个空格
                                    .trim();
                        } else if (StrUtil.isNotBlank(activity.getRequirements())) {
                            activityContent = activity.getRequirements();
                        }
                        // 如果内容为空，使用默认值；限制20字符（微信订阅消息thing类型字段限制）
                        activityContent = StrUtil.blankToDefault(StrUtil.maxLength(activityContent, 20), "活动即将开始");
                        reqDTO.addMessage("thing2", activityContent); // 活动内容，限制20字符
                        
                        socialClientApi.sendWxaSubscribeMessage(reqDTO);
                        
                        totalNotifyCount++;
                        log.info("[notifyActivityStartIn2Hours][通知用户成功，activityId={}, userId={}]", 
                                activity.getId(), userId);
                    } catch (Exception e) {
                        log.error("[notifyActivityStartIn2Hours][通知用户失败，activityId={}, userId={}]", 
                                activity.getId(), userId, e);
                    }
                }
            } catch (Exception e) {
                log.error("[notifyActivityStartIn2Hours][处理活动失败，activityId={}]", activity.getId(), e);
            }
        }
        
        log.info("[notifyActivityStartIn2Hours][完成，共通知 {} 个用户]", totalNotifyCount);
        return totalNotifyCount;
    }

}
