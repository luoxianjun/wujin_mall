package cn.iocoder.yudao.module.forum.service.user;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.forum.config.ForumMailProperties;
import cn.iocoder.yudao.module.forum.controller.app.user.vo.*;
import cn.iocoder.yudao.module.forum.convert.user.ForumUserProfileConvert;
import cn.iocoder.yudao.module.forum.dal.dataobject.user.ForumUserProfileDO;
import cn.iocoder.yudao.module.forum.dal.mysql.user.ForumUserProfileMapper;
import cn.iocoder.yudao.module.forum.enums.user.SchoolEmailSuffixEnum;
import cn.iocoder.yudao.module.forum.service.text.ForumTextAuditService;
import cn.iocoder.yudao.module.gamification.listener.MemberVerifiedEvent;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserPageReqDTO;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.system.api.social.SocialUserApi;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserRespDTO;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.im.service.TencentImAccountService;
import cn.iocoder.yudao.module.im.client.tencent.TencentImClient;
import cn.iocoder.yudao.module.im.client.tencent.vo.TencentImBlacklistAddReq;
import cn.iocoder.yudao.module.im.client.tencent.vo.TencentImBlacklistDeleteReq;
import cn.iocoder.yudao.module.im.client.tencent.vo.TencentImBlacklistGetReq;
import cn.iocoder.yudao.module.im.client.tencent.resp.TencentImBlacklistGetResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.module.forum.enums.ErrorCodeConstants.*;

/**
 * 论坛用户资料 Service 实现类
 *
 * @author forum
 */
@Service
@Validated
@Slf4j
public class ForumUserProfileServiceImpl implements ForumUserProfileService {

    @Resource
    private ForumUserProfileMapper userProfileMapper;

    @Resource
    private MemberUserApi memberUserApi;

    @Resource
    private ForumImSignatureService imSignatureService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private JavaMailSender javaMailSender;

    @Resource
    private ForumMailProperties forumMailProperties;

    @Resource
    private TencentImAccountService tencentImAccountService;

    @Resource
    private ForumTextAuditService textAuditService;

    @Resource
    private TencentImClient tencentImClient;

    @Resource
    private SocialUserApi socialUserApi;

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * 学校邮箱验证码 Redis Key 前缀
     */
    private static final String SCHOOL_EMAIL_CODE_KEY = "forum:school:email:code:";

    /**
     * 验证码过期时间（分钟）
     */
    private static final int CODE_EXPIRE_MINUTES = 10;

    private static final String DEFAULT_MAIL_SUBJECT = "【学生论坛】学校邮箱验证码";
    private static final String DEFAULT_MAIL_TEMPLATE = "您好，您正在申请学校邮箱认证，验证码为：${code}，有效期 ${minutes} 分钟。"
            + "如果非本人操作，请忽略本邮件。";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ForumUserProfileDO getOrCreateUserProfile(Long userId) {
        // 1. 先查询是否已存在
        ForumUserProfileDO profile = userProfileMapper.selectByUserId(userId);
        if (profile != null) {
            ensureImUserSig(profile);
            return profile;
        }

        // 2. 不存在则创建
        // 2.1 获取会员用户信息
        MemberUserRespDTO memberUser = memberUserApi.getUser(userId);
        if (memberUser == null) {
            throw ServiceExceptionUtil.exception(USER_PROFILE_NOT_EXISTS);
        }

        // 2.2 生成唯一 UID
        String uid = generateUniqueUid();
        ForumImSignatureService.ImUserSig imUserSig = imSignatureService.isEnabled()
                ? imSignatureService.generateUserSig(String.valueOf(userId))
                : null;

        // 2.3 创建论坛用户资料
        profile = ForumUserProfileDO.builder()
                .userId(userId)
                .uid(uid)
                .nickname(memberUser.getNickname())
                .avatar(memberUser.getAvatar())
                .imUserSig(imUserSig != null ? imUserSig.getSig() : null)
                .imUserSigExpireTime(imUserSig != null ? imUserSig.getExpireTime() : null)
                .schoolEmailVerified(false)
                .schoolInfoPublic(false)
                .point(0)
                .totalPoint(0)
                .continuousSignDays(0)
                .totalSignDays(0)
                .postCount(0)
                .activityCount(0)
                .likeCount(0)
                .favoriteCount(0)
                .build();

        userProfileMapper.insert(profile);
        tencentImAccountService.importUserIfEnabled(userId, profile.getNickname(), profile.getAvatar());
        log.info("[getOrCreateUserProfile][创建论坛用户资料成功，userId={}, uid={}]", userId, uid);
        return profile;
    }

    @Override
    public ForumUserProfileDO getUserProfileByUserId(Long userId) {
        ForumUserProfileDO profile = userProfileMapper.selectByUserId(userId);
        if (profile != null) {
            ensureImUserSig(profile);
        }
        return profile;
    }

    @Override
    public ForumUserProfileDO getUserProfileByUid(String uid) {
        return userProfileMapper.selectOne(ForumUserProfileDO::getUid, uid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserProfile(Long userId, AppUserProfileUpdateReqVO reqVO) {
        // 1. 校验用户资料是否存在
        ForumUserProfileDO profile = userProfileMapper.selectByUserId(userId);
        if (profile == null) {
            throw ServiceExceptionUtil.exception(USER_PROFILE_NOT_EXISTS);
        }

        // 1.0 文本审核：昵称与个人简介
        String openid = getUserWxOpenid(userId);
        textAuditService.audit(reqVO.getNickname(), openid, "user-nickname");
        textAuditService.audit(reqVO.getIntroduction(), openid, "user-introduction");

        // 2. 更新用户资料
        ForumUserProfileDO updateObj = ForumUserProfileDO.builder()
                .id(profile.getId())
                .nickname(reqVO.getNickname())
                .avatar(reqVO.getAvatar())
                .birthday(reqVO.getBirthday())
                .mbti(reqVO.getMbti())
                .constellation(reqVO.getConstellation())
                .introduction(reqVO.getIntroduction())
                .build();

        // 3. 根据生日计算星座
        if (reqVO.getBirthday() != null) {
            updateObj.setConstellation(calculateConstellation(reqVO.getBirthday()));
        }

        userProfileMapper.updateById(updateObj);
        tencentImAccountService.setProfileIfEnabled(userId, reqVO.getNickname(), reqVO.getAvatar());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitSchoolVerification(Long userId, AppSchoolVerificationReqVO reqVO) {
        // 1. 校验用户资料是否存在
        ForumUserProfileDO profile = userProfileMapper.selectByUserId(userId);
        if (profile == null) {
            throw ServiceExceptionUtil.exception(USER_PROFILE_NOT_EXISTS);
        }

        // 2. 校验学校邮箱后缀
        if (!SchoolEmailSuffixEnum.isSupported(reqVO.getSchoolEmail())) {
            throw ServiceExceptionUtil.exception(SCHOOL_EMAIL_NOT_SUPPORTED);
        }

        // 3. 校验邮箱是否已被其他用户认证
        ForumUserProfileDO existProfile = userProfileMapper.selectOne(
                ForumUserProfileDO::getSchoolEmail, reqVO.getSchoolEmail(),
                ForumUserProfileDO::getSchoolEmailVerified, true);
        if (existProfile != null && !existProfile.getUserId().equals(userId)) {
            throw ServiceExceptionUtil.exception(SCHOOL_EMAIL_ALREADY_VERIFIED);
        }

        // 4. 更新认证信息（未验证状态）
        ForumUserProfileDO updateObj = ForumUserProfileDO.builder()
                .id(profile.getId())
                .schoolEmail(reqVO.getSchoolEmail())
                .schoolEmailPrefix(SchoolEmailSuffixEnum.getEmailPrefix(reqVO.getSchoolEmail()))
                .schoolName(SchoolEmailSuffixEnum.getSchoolName(reqVO.getSchoolEmail()))
                .realName(reqVO.getRealName())
                .gender(reqVO.getGender())
                .major(reqVO.getMajor())
                .enrollYear(reqVO.getEnrollYear())
                .degree(reqVO.getDegree())
                .build();

        userProfileMapper.updateById(updateObj);
    }

    private void ensureImUserSig(ForumUserProfileDO profile) {
        if (!imSignatureService.isEnabled()) {
            log.info("IM 未启用，跳过生成用户签名");
            return;
        }
        if (StrUtil.isNotBlank(profile.getImUserSig())
                && !imSignatureService.isExpired(profile.getImUserSigExpireTime())) {
            log.info("IM 用户签名未过期，跳过生成用户签名");
            return;
        }
        ForumImSignatureService.ImUserSig newSig = imSignatureService
                .generateUserSig(String.valueOf(profile.getUserId()));
        userProfileMapper.updateById(ForumUserProfileDO.builder()
                .id(profile.getId())
                .imUserSig(newSig.getSig())
                .imUserSigExpireTime(newSig.getExpireTime())
                .build());
        profile.setImUserSig(newSig.getSig());
        profile.setImUserSigExpireTime(newSig.getExpireTime());
        log.info("IM 用户签名生成成功，userId={}, uid={}，userSig={}，expireTime={}", profile.getUserId(), profile.getUid(),
                newSig.getSig(), newSig.getExpireTime());
    }

    /**
     * 生成唯一 UID
     */
    private String generateUniqueUid() {
        int maxRetries = 10;
        for (int i = 0; i < maxRetries; i++) {
            // 生成格式：U + 6位随机数字
            String uid = "U" + RandomUtil.randomNumbers(6);

            // 检查是否已存在
            ForumUserProfileDO existing = userProfileMapper.selectOne(ForumUserProfileDO::getUid, uid);
            if (existing == null) {
                return uid;
            }
        }

        // 如果10次都失败，使用 UUID 后6位
        throw ServiceExceptionUtil.exception(UID_GENERATION_FAILED);
    }

    @Override
    public void sendSchoolEmailCode(Long userId, String email) {
        // 1. 校验学校邮箱后缀
        if (!SchoolEmailSuffixEnum.isSupported(email)) {
            throw ServiceExceptionUtil.exception(SCHOOL_EMAIL_NOT_SUPPORTED);
        }

        // 2. 生成6位数字验证码
        String code = RandomUtil.randomNumbers(6);

        // 3. 存储到 Redis，过期时间10分钟
        String key = SCHOOL_EMAIL_CODE_KEY + email;
        stringRedisTemplate.opsForValue().set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 4. 发送邮件
        try {
            sendSchoolVerificationEmail(email, code);
            log.info("[sendSchoolEmailCode][发送验证码成功，email={}, code={}]", email, code);
        } catch (MessagingException | MailException ex) {
            stringRedisTemplate.delete(key);
            log.error("[sendSchoolEmailCode][发送验证码失败，email={}]", email, ex);
            throw ServiceExceptionUtil.exception(SCHOOL_EMAIL_SEND_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifySchoolEmailCode(Long userId, String email, String code) {
        // 1. 校验用户资料是否存在
        ForumUserProfileDO profile = userProfileMapper.selectByUserId(userId);
        if (profile == null) {
            throw ServiceExceptionUtil.exception(USER_PROFILE_NOT_EXISTS);
        }

        // 2. 校验验证码
        String key = SCHOOL_EMAIL_CODE_KEY + email;
        String storedCode = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(storedCode) || !storedCode.equals(code)) {
            throw ServiceExceptionUtil.exception(VERIFICATION_CODE_INVALID);
        }

        // 3. 更新认证状态
        ForumUserProfileDO updateObj = ForumUserProfileDO.builder()
                .id(profile.getId())
                .schoolEmailVerified(true)
                .schoolEmailVerifyTime(LocalDateTime.now())
                .build();

        userProfileMapper.updateById(updateObj);

        // 4. 删除验证码
        stringRedisTemplate.delete(key);

        publishMemberVerifiedEventAfterCommit(userId);

        log.info("[verifySchoolEmailCode][验证成功，userId={}, email={}]", userId, email);
    }

    private void publishMemberVerifiedEventAfterCommit(Long userId) {
        Runnable publishTask = () -> applicationEventPublisher.publishEvent(new MemberVerifiedEvent(this, userId));
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            publishTask.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {
                publishTask.run();
            }

        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSchoolInfoPublic(Long userId, Boolean schoolInfoPublic) {
        // 1. 校验用户资料是否存在
        ForumUserProfileDO profile = userProfileMapper.selectByUserId(userId);
        if (profile == null) {
            throw ServiceExceptionUtil.exception(USER_PROFILE_NOT_EXISTS);
        }

        // 2. 更新公开设置
        ForumUserProfileDO updateObj = ForumUserProfileDO.builder()
                .id(profile.getId())
                .schoolInfoPublic(schoolInfoPublic)
                .build();

        userProfileMapper.updateById(updateObj);
    }

    @Override
    public AppUserStatisticsRespVO getUserStatistics(Long userId) {
        ForumUserProfileDO profile = userProfileMapper.selectByUserId(userId);
        if (profile == null) {
            throw ServiceExceptionUtil.exception(USER_PROFILE_NOT_EXISTS);
        }

        return ForumUserProfileConvert.INSTANCE.convertToStatistics(profile);
    }

    @Override
    public AppUserCenterRespVO getUserCenter(Long userId) {
        // 1. 获取用户资料
        ForumUserProfileDO profile = userProfileMapper.selectByUserId(userId);
        if (profile == null) {
            throw ServiceExceptionUtil.exception(USER_PROFILE_NOT_EXISTS);
        }

        // 2. 检查今天是否已签到
        LocalDate today = LocalDate.now();
        boolean todaySigned = false;
        if (profile.getLastSignDate() != null && profile.getLastSignDate().equals(today)) {
            todaySigned = true;
        }

        // 3. 构建响应
        AppUserCenterRespVO respVO = new AppUserCenterRespVO();
        respVO.setUserId(profile.getUserId());
        respVO.setUid(profile.getUid());
        respVO.setNickname(profile.getNickname());
        respVO.setAvatar(profile.getAvatar());
        respVO.setSchoolName(profile.getSchoolName());
        respVO.setSchoolEmailVerified(profile.getSchoolEmailVerified());
        respVO.setConstellation(profile.getConstellation());
        respVO.setMbti(profile.getMbti());
        respVO.setIntroduction(profile.getIntroduction());

        // 积分信息
        respVO.setPoint(profile.getPoint());
        respVO.setTotalPoint(profile.getTotalPoint());

        // 签到信息
        respVO.setContinuousSignDays(profile.getContinuousSignDays());
        respVO.setTotalSignDays(profile.getTotalSignDays());
        respVO.setLastSignDate(profile.getLastSignDate());
        respVO.setTodaySigned(todaySigned);

        // 统计信息
        respVO.setPostCount(profile.getPostCount());
        respVO.setActivityCount(profile.getActivityCount());
        respVO.setLikeCount(profile.getLikeCount());
        respVO.setFavoriteCount(profile.getFavoriteCount());

        // IM 签名
        ensureImUserSig(profile);
        respVO.setUserSig(profile.getImUserSig());

        // 隐私设置（默认 true）
        respVO.setAllowPrivateChat(profile.getAllowPrivateChat() != null ? profile.getAllowPrivateChat() : true);
        respVO.setAllowSystemMessage(profile.getAllowSystemMessage() != null ? profile.getAllowSystemMessage() : true);
        respVO.setHideSchoolInfo(Boolean.TRUE.equals(profile.getHideSchoolInfo()));

        // 管理员标识
        respVO.setIsAdmin(Boolean.TRUE.equals(profile.getIsAdmin()));

        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increasePostCount(Long userId) {
        userProfileMapper.updateById(ForumUserProfileDO.builder()
                .id(getUserProfileByUserId(userId).getId())
                .postCount(getUserProfileByUserId(userId).getPostCount() + 1)
                .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decreasePostCount(Long userId) {
        ForumUserProfileDO profile = getUserProfileByUserId(userId);
        if (profile == null) {
            log.warn("[decreasePostCount][用户资料不存在，跳过减少发帖数，userId={}]", userId);
            return;
        }
        Integer postCount = profile.getPostCount();
        if (postCount == null || postCount <= 0) {
            log.warn("[decreasePostCount][发帖数无效，跳过减少发帖数，userId={}, postCount={}]", userId, postCount);
            return;
        }
        userProfileMapper.updateById(ForumUserProfileDO.builder()
                .id(profile.getId())
                .postCount(postCount - 1)
                .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increaseActivityCount(Long userId) {
        ForumUserProfileDO profile = getUserProfileByUserId(userId);
        userProfileMapper.updateById(ForumUserProfileDO.builder()
                .id(profile.getId())
                .activityCount(profile.getActivityCount() + 1)
                .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decreaseActivityCount(Long userId) {
        ForumUserProfileDO profile = getUserProfileByUserId(userId);
        if (profile == null) {
            log.warn("[decreaseActivityCount][用户资料不存在，跳过减少活动数，userId={}]", userId);
            return;
        }
        Integer activityCount = profile.getActivityCount();
        if (activityCount == null || activityCount <= 0) {
            log.warn("[decreaseActivityCount][活动数无效，跳过减少活动数，userId={}, activityCount={}]", userId, activityCount);
            return;
        }
        userProfileMapper.updateById(ForumUserProfileDO.builder()
                .id(profile.getId())
                .activityCount(activityCount - 1)
                .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increaseLikeCount(Long userId, Integer count) {
        ForumUserProfileDO profile = getUserProfileByUserId(userId);
        userProfileMapper.updateById(ForumUserProfileDO.builder()
                .id(profile.getId())
                .likeCount(profile.getLikeCount() + count)
                .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increaseFavoriteCount(Long userId, Integer count) {
        ForumUserProfileDO profile = getUserProfileByUserId(userId);
        userProfileMapper.updateById(ForumUserProfileDO.builder()
                .id(profile.getId())
                .favoriteCount(profile.getFavoriteCount() + count)
                .build());
    }

    private void sendSchoolVerificationEmail(String email, String code) throws MessagingException {
        if (Boolean.FALSE.equals(forumMailProperties.getEnabled())) {
            log.warn("[sendSchoolVerificationEmail][邮件发送已关闭，email={}]", email);
            return;
        }
        ForumMailProperties.SchoolEmailProperties schoolEmail = forumMailProperties.getSchoolEmail();
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());
        helper.setTo(email);
        if (StrUtil.isNotBlank(schoolEmail.getFrom())) {
            helper.setFrom(schoolEmail.getFrom());
        }
        helper.setSubject(StrUtil.blankToDefault(schoolEmail.getSubject(), DEFAULT_MAIL_SUBJECT));
        String content = renderEmailTemplate(
                StrUtil.blankToDefault(schoolEmail.getTemplate(), DEFAULT_MAIL_TEMPLATE),
                email,
                code);
        helper.setText(content, Boolean.TRUE.equals(schoolEmail.getHtml()));
        javaMailSender.send(mimeMessage);
    }

    private String renderEmailTemplate(String template, String email, String code) {
        return template
                .replace("${code}", code)
                .replace("${email}", email)
                .replace("${minutes}", String.valueOf(CODE_EXPIRE_MINUTES));
    }

    /**
     * 根据生日计算星座
     */
    private String calculateConstellation(LocalDate birthday) {
        if (birthday == null) {
            return null;
        }

        int month = birthday.getMonthValue();
        int day = birthday.getDayOfMonth();

        if ((month == 1 && day >= 20) || (month == 2 && day <= 18)) {
            return "水瓶座";
        } else if ((month == 2 && day >= 19) || (month == 3 && day <= 20)) {
            return "双鱼座";
        } else if ((month == 3 && day >= 21) || (month == 4 && day <= 19)) {
            return "白羊座";
        } else if ((month == 4 && day >= 20) || (month == 5 && day <= 20)) {
            return "金牛座";
        } else if ((month == 5 && day >= 21) || (month == 6 && day <= 21)) {
            return "双子座";
        } else if ((month == 6 && day >= 22) || (month == 7 && day <= 22)) {
            return "巨蟹座";
        } else if ((month == 7 && day >= 23) || (month == 8 && day <= 22)) {
            return "狮子座";
        } else if ((month == 8 && day >= 23) || (month == 9 && day <= 22)) {
            return "处女座";
        } else if ((month == 9 && day >= 23) || (month == 10 && day <= 23)) {
            return "天秤座";
        } else if ((month == 10 && day >= 24) || (month == 11 && day <= 22)) {
            return "天蝎座";
        } else if ((month == 11 && day >= 23) || (month == 12 && day <= 21)) {
            return "射手座";
        } else {
            return "摩羯座";
        }
    }

    @Override
    public PageResult<AppForumUserPageRespVO> getMemberPage(AppForumUserPageReqVO reqVO) {
        // 直接查询论坛用户表，搜索和显示使用同一个 nickname 字段
        PageResult<ForumUserProfileDO> pageResult = userProfileMapper.selectPage(reqVO, reqVO.getNickname());
        return ForumUserProfileConvert.INSTANCE.convertProfilePage(pageResult);
    }

    @Override
    public void blockUser(Long userId, Long targetUserId) {
        if (!tencentImClient.isEnabled()) {
            log.warn("[blockUser][IM 未启用，跳过拉黑操作]");
            return;
        }
        // 构建 IM 用户标识
        String fromAccount = String.valueOf(userId);
        String toAccount = String.valueOf(targetUserId);

        TencentImBlacklistAddReq req = TencentImBlacklistAddReq.builder()
                .fromAccount(fromAccount)
                .toAccount(java.util.Collections.singletonList(toAccount))
                .build();

        tencentImClient.addBlacklist(req);
        log.info("[blockUser][拉黑用户成功，userId={}, targetUserId={}]", userId, targetUserId);
    }

    @Override
    public void unblockUser(Long userId, Long targetUserId) {
        if (!tencentImClient.isEnabled()) {
            log.warn("[unblockUser][IM 未启用，跳过取消拉黑操作]");
            return;
        }
        // 构建 IM 用户标识
        String fromAccount = String.valueOf(userId);
        String toAccount = String.valueOf(targetUserId);

        TencentImBlacklistDeleteReq req = TencentImBlacklistDeleteReq.builder()
                .fromAccount(fromAccount)
                .toAccount(java.util.Collections.singletonList(toAccount))
                .build();

        tencentImClient.deleteBlacklist(req);
        log.info("[unblockUser][取消拉黑用户成功，userId={}, targetUserId={}]", userId, targetUserId);
    }

    @Override
    public boolean isBlocked(Long userId, Long targetUserId) {
        if (!tencentImClient.isEnabled()) {
            log.warn("[isBlocked][IM 未启用，返回未拉黑]");
            return false;
        }
        // 构建 IM 用户标识
        String fromAccount = String.valueOf(userId);
        String targetAccount = String.valueOf(targetUserId);

        TencentImBlacklistGetReq req = TencentImBlacklistGetReq.builder()
                .fromAccount(fromAccount)
                .startIndex(0)
                .maxLimited(100)
                .lastSequence(0)
                .build();

        TencentImBlacklistGetResp resp = tencentImClient.getBlacklist(req);
        if (resp.getBlackListItem() == null) {
            return false;
        }

        return resp.getBlackListItem().stream()
                .anyMatch(item -> targetAccount.equals(item.getToAccount()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePrivacySettings(Long userId, AppUserPrivacySettingsReqVO reqVO) {
        ForumUserProfileDO profile = getUserProfileByUserId(userId);
        ForumUserProfileDO updateObj = ForumUserProfileDO.builder()
                .id(profile.getId())
                .allowPrivateChat(reqVO.getAllowPrivateChat())
                .allowSystemMessage(reqVO.getAllowSystemMessage())
                .hideSchoolInfo(reqVO.getHideSchoolInfo())
                .build();
        userProfileMapper.updateById(updateObj);
    }

    // ========== 管理员功能 ==========

    @Override
    public PageResult<cn.iocoder.yudao.module.forum.controller.admin.user.vo.AdminUserProfileRespVO> getAdminUserProfilePage(
            cn.iocoder.yudao.module.forum.controller.admin.user.vo.AdminUserProfilePageReqVO reqVO) {
        // 使用 LambdaQueryWrapperX 构建查询条件
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ForumUserProfileDO> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (cn.hutool.core.util.StrUtil.isNotBlank(reqVO.getNickname())) {
            wrapper.like(ForumUserProfileDO::getNickname, reqVO.getNickname());
        }
        if (cn.hutool.core.util.StrUtil.isNotBlank(reqVO.getUid())) {
            wrapper.like(ForumUserProfileDO::getUid, reqVO.getUid());
        }
        if (reqVO.getUserId() != null) {
            wrapper.eq(ForumUserProfileDO::getUserId, reqVO.getUserId());
        }
        if (reqVO.getIsAdmin() != null) {
            wrapper.eq(ForumUserProfileDO::getIsAdmin, reqVO.getIsAdmin());
        }
        wrapper.orderByDesc(ForumUserProfileDO::getId);

        // 分页查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ForumUserProfileDO> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                reqVO.getPageNo(), reqVO.getPageSize());
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ForumUserProfileDO> result = userProfileMapper
                .selectPage(page, wrapper);

        // 转换为响应 VO
        java.util.List<cn.iocoder.yudao.module.forum.controller.admin.user.vo.AdminUserProfileRespVO> list = new java.util.ArrayList<>();
        for (ForumUserProfileDO profile : result.getRecords()) {
            cn.iocoder.yudao.module.forum.controller.admin.user.vo.AdminUserProfileRespVO respVO = new cn.iocoder.yudao.module.forum.controller.admin.user.vo.AdminUserProfileRespVO();
            respVO.setUserId(profile.getUserId());
            respVO.setUid(profile.getUid());
            respVO.setNickname(profile.getNickname());
            respVO.setAvatar(profile.getAvatar());
            respVO.setCampus(profile.getSchoolName());
            respVO.setPoint(profile.getPoint());
            respVO.setPostCount(profile.getPostCount());
            respVO.setActivityCount(profile.getActivityCount());
            respVO.setIsAdmin(Boolean.TRUE.equals(profile.getIsAdmin()));
            respVO.setSchoolEmailVerified(Boolean.TRUE.equals(profile.getSchoolEmailVerified()));
            respVO.setCreateTime(profile.getCreateTime());
            list.add(respVO);
        }

        return new PageResult<>(list, result.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setUserAdmin(Long userId, Boolean isAdmin) {
        ForumUserProfileDO profile = getUserProfileByUserId(userId);
        if (profile == null) {
            throw ServiceExceptionUtil.exception(USER_PROFILE_NOT_EXISTS);
        }
        ForumUserProfileDO updateObj = ForumUserProfileDO.builder()
                .id(profile.getId())
                .isAdmin(isAdmin)
                .build();
        userProfileMapper.updateById(updateObj);
        log.info("[setUserAdmin][userId={}, isAdmin={}]", userId, isAdmin);
    }

    @Override
    public cn.iocoder.yudao.module.forum.controller.admin.user.vo.AdminUserProfileRespVO getAdminUserProfileByUserId(
            Long userId) {
        ForumUserProfileDO profile = getUserProfileByUserId(userId);
        if (profile == null) {
            return null;
        }
        cn.iocoder.yudao.module.forum.controller.admin.user.vo.AdminUserProfileRespVO respVO = new cn.iocoder.yudao.module.forum.controller.admin.user.vo.AdminUserProfileRespVO();
        respVO.setUserId(profile.getUserId());
        respVO.setUid(profile.getUid());
        respVO.setNickname(profile.getNickname());
        respVO.setAvatar(profile.getAvatar());
        respVO.setCampus(profile.getSchoolName());
        respVO.setPoint(profile.getPoint());
        respVO.setPostCount(profile.getPostCount());
        respVO.setActivityCount(profile.getActivityCount());
        respVO.setIsAdmin(Boolean.TRUE.equals(profile.getIsAdmin()));
        respVO.setSchoolEmailVerified(Boolean.TRUE.equals(profile.getSchoolEmailVerified()));
        respVO.setCreateTime(profile.getCreateTime());
        return respVO;
    }

    @Override
    public java.util.List<ForumUserProfileDO> getUserProfileListByUserIds(java.util.Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return userProfileMapper.selectList(ForumUserProfileDO::getUserId, userIds);
    }

    @Override
    public java.util.List<Long> getUserIdsByUidLike(String uid) {
        if (StrUtil.isBlank(uid)) {
            return java.util.Collections.emptyList();
        }
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ForumUserProfileDO> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.like(ForumUserProfileDO::getUid, uid);
        java.util.List<ForumUserProfileDO> profiles = userProfileMapper.selectList(wrapper);
        return profiles.stream()
                .map(ForumUserProfileDO::getUserId)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取用户的微信小程序 openid
     */
    private String getUserWxOpenid(Long userId) {
        try {
            SocialUserRespDTO socialUser = socialUserApi.getSocialUserByUserId(
                    UserTypeEnum.MEMBER.getValue(), userId, SocialTypeEnum.WECHAT_MINI_PROGRAM.getType());
            return socialUser != null ? socialUser.getOpenid() : null;
        } catch (Exception e) {
            log.error("[getUserWxOpenid][获取用户微信openid失败，userId={}]", userId, e);
            return null;
        }
    }

}
