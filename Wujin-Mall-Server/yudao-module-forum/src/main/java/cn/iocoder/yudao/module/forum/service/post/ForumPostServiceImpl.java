package cn.iocoder.yudao.module.forum.service.post;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.forum.controller.app.post.vo.AppPostCreateReqVO;
import cn.iocoder.yudao.module.forum.controller.app.post.vo.AppPostPageReqVO;
import cn.iocoder.yudao.module.forum.controller.app.post.vo.AppPostRespVO;
import cn.iocoder.yudao.module.forum.convert.post.ForumPostConvert;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.ForumPostDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.ForumPostFollowDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.ForumPostLikeDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.PostVoteDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.PostVoteOptionDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.user.ForumUserProfileDO;
import cn.iocoder.yudao.module.forum.dal.mysql.post.ForumPostFollowMapper;
import cn.iocoder.yudao.module.forum.dal.mysql.post.ForumPostLikeMapper;
import cn.iocoder.yudao.module.forum.dal.mysql.post.ForumPostMapper;
import cn.iocoder.yudao.module.forum.dal.mysql.user.ForumUserProfileMapper;
import cn.iocoder.yudao.module.forum.enums.message.NoticeTypeEnum;
import cn.iocoder.yudao.module.forum.enums.post.PostCategoryEnum;
import cn.iocoder.yudao.module.forum.enums.post.PostStatusEnum;
import cn.iocoder.yudao.module.forum.service.message.ForumMessageService;
import cn.iocoder.yudao.module.forum.service.point.ForumPointService;
import cn.iocoder.yudao.module.forum.service.user.ForumUserProfileService;
import cn.iocoder.yudao.module.forum.service.text.ForumTextAuditService;
import cn.iocoder.yudao.module.forum.service.text.ForumTextAuditService.TextAuditResult;
import cn.iocoder.yudao.module.infra.service.file.FileConfigService;
import cn.iocoder.yudao.module.infra.service.file.InfraImageAuditService;
import cn.iocoder.yudao.module.infra.service.file.InfraImageAuditService.ImageAuditResult;
import cn.iocoder.yudao.module.forum.event.post.ForumPostSaveEvent;
import cn.iocoder.yudao.module.forum.service.message.ForumInteractionUnreadService;
import cn.iocoder.yudao.module.im.service.TencentImMessageService;
import cn.iocoder.yudao.module.system.api.social.SocialUserApi;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserRespDTO;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.forum.enums.ErrorCodeConstants.*;

/**
 * 论坛帖子 Service 实现类
 *
 * @author forum
 */
@Service
@Validated
@Slf4j
public class ForumPostServiceImpl implements ForumPostService {

    private static final int DEFAULT_HOT_LIMIT = 10;
    private static final int MIN_HOT_LIMIT = 10;
    private static final int MAX_HOT_LIMIT = 25;
    /** 默认热榜时间窗口：近 72 小时 */
    private static final int DEFAULT_HOT_WINDOW_HOURS = 72;
    /** 最大允许的时间窗口（30 天）避免全表扫描 */
    private static final int MAX_HOT_WINDOW_HOURS = 24 * 30;

    @Resource(name = "forumPostMapper")
    private ForumPostMapper postMapper;

    @Resource
    private ForumPostLikeMapper postLikeMapper;

    @Resource
    private ForumPostFollowMapper postFollowMapper;

    @Resource
    private ForumUserProfileMapper userProfileMapper;

    @Resource
    private ForumUserProfileService userProfileService;

    @Resource
    private ForumPointService pointService;

    @Resource
    private ForumInteractionUnreadService interactionUnreadService;

    @Resource
    private ForumMessageService messageService;

    @Resource
    private ForumTextAuditService textAuditService;

    @Resource
    private InfraImageAuditService imageAuditService;

    @Resource
    private FileConfigService fileConfigService;

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private TencentImMessageService tencentImMessageService;

    @Resource
    private SocialUserApi socialUserApi;

    @Resource
    private PostVoteService postVoteService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPost(Long userId, AppPostCreateReqVO reqVO) {
        // 1. 校验用户资料是否存在
        ForumUserProfileDO profile = userProfileMapper.selectByUserId(userId);
        if (profile == null) {
            throw ServiceExceptionUtil.exception(USER_PROFILE_NOT_EXISTS);
        }

        // 1.0 获取用户的微信 openid（用于微信内容安全审核）
        String openid = getUserWxOpenid(userId);

        // 1.0 文本审核：标题 + 内容（同步审核，审核失败直接抛异常）
        try {
            textAuditService.audit(reqVO.getTitle(), openid, "post-title");
            textAuditService.audit(reqVO.getContent(), openid, "post-content");
        } catch (Exception e) {
            // 文本审核失败，删除已上传的图片
            if (CollUtil.isNotEmpty(reqVO.getImageUrls())) {
                fileConfigService.deleteByUrls(reqVO.getImageUrls());
            }
            throw e;
        }

        // 1.0.1 图片审核（同步审核，审核失败直接抛异常）
        try {
            imageAuditService.auditImageUrls(reqVO.getImageUrls(), openid, "post-image");
        } catch (Exception e) {
            // 图片审核失败，删除已上传的图片
            if (CollUtil.isNotEmpty(reqVO.getImageUrls())) {
                fileConfigService.deleteByUrls(reqVO.getImageUrls());
            }
            throw e;
        }

        // 1.1 处理分类（兼容单选与多选）
        List<Integer> categories = resolveCategories(reqVO);
        Integer primaryCategory = categories.isEmpty() ? null : categories.get(0);

        // 1.2 判断是否为管理员，管理员才能置顶
        boolean isAdmin = profile.getIsAdmin() != null && profile.getIsAdmin();
        boolean shouldTop = isAdmin && Boolean.TRUE.equals(reqVO.getTop());

        // 2. 构建帖子对象（审核通过，直接设为已通过状态）
        ForumPostDO post = ForumPostDO.builder()
                .userId(userId)
                .title(reqVO.getTitle())
                .content(reqVO.getContent())
                .category(primaryCategory)
                .categories(CollUtil.isNotEmpty(categories) ? JSONUtil.toJsonStr(categories) : null)
                .imageUrls(CollUtil.isNotEmpty(reqVO.getImageUrls()) ? JSONUtil.toJsonStr(reqVO.getImageUrls()) : null)
                .anonymous(reqVO.getAnonymous())
                .schoolOnly(reqVO.getSchoolOnly() != null ? reqVO.getSchoolOnly() : false)
                .school(reqVO.getSchool())
                .status(PostStatusEnum.APPROVED.getStatus())
                .isTop(shouldTop)
                .likeCount(0)
                .commentCount(0)
                .followCount(0)
                .viewCount(0)
                .build();

        // 3. “不展示个人主页”模式：不生成匿名昵称/头像，展示时用用户真实昵称和头像，但不暴露 userId

        // 4. 插入帖子
        postMapper.insert(post);

        // 4.1 如果有投票选项，创建帖子内嵌投票
        if (CollUtil.isNotEmpty(reqVO.getVoteOptions()) && reqVO.getVoteOptions().size() >= 2) {
            PostVoteDO vote = PostVoteDO.builder()
                    .voteType(reqVO.getVoteType() != null ? reqVO.getVoteType() : 0)
                    .maxChoices(reqVO.getVoteMaxChoices() != null ? reqVO.getVoteMaxChoices() : 1)
                    .anonymous(false)
                    .showRealtimeResult(true)
                    .build();

            List<PostVoteOptionDO> options = new ArrayList<>();
            for (AppPostCreateReqVO.VoteOptionItem item : reqVO.getVoteOptions()) {
                PostVoteOptionDO opt = PostVoteOptionDO.builder()
                        .title(item.getTitle())
                        .build();
                options.add(opt);
            }
            postVoteService.createPostVote(post.getId(), vote, options);
            log.info("[createPost][帖子附带投票，postId={}, optionCount={}]", post.getId(), options.size());
        }

        // 5. 增加用户发帖数
        userProfileService.increasePostCount(userId);

        // 注：发帖不再给积分，只有签到和参加完活动才给积分
        // pointService.addPostPoint(userId, post.getId().toString());

        log.info("[createPost][用户发帖成功，userId={}, postId={}]", userId, post.getId());

        // 6. 异步同步到 ES
        applicationContext.publishEvent(new ForumPostSaveEvent(post.getId()));

        return post.getId();
    }

    /**
     * 构建拒绝原因
     */
    private String buildRejectReason(TextAuditResult titleResult, TextAuditResult contentResult,
            ImageAuditResult imageResult) {
        StringBuilder reason = new StringBuilder();
        if (!titleResult.isPassed() && titleResult.getRejectReason() != null) {
            reason.append("标题：").append(titleResult.getRejectReason());
        }
        if (!contentResult.isPassed() && contentResult.getRejectReason() != null) {
            if (reason.length() > 0) {
                reason.append("；");
            }
            reason.append("内容：").append(contentResult.getRejectReason());
        }
        if (!imageResult.isPassed() && imageResult.getRejectReason() != null) {
            if (reason.length() > 0) {
                reason.append("；");
            }
            reason.append("图片：").append(imageResult.getRejectReason());
        }
        return reason.length() > 0 ? reason.toString() : "内容包含敏感信息";
    }

    /**
     * 发送内容违规通知（通过 IM 服务）
     */
    private void sendContentViolationNotice(Long userId, Long postId, String postTitle, String reason) {
        String content = "【内容审核通知】您发布的内容因涉及违规内容已被系统拦截，暂不会公开显示。如有疑问，请联系客服处理。";

        // 通过 IM 服务发送消息通知用户
        try {
            String toAccount = String.valueOf(userId);
            tencentImMessageService.sendAdminTextMessage(toAccount, content);
            log.info("[sendContentViolationNotice][通过IM发送内容违规通知成功，userId={}, postId={}]", userId, postId);
        } catch (Exception e) {
            log.warn("[sendContentViolationNotice][通过IM发送内容违规通知失败，userId={}, postId={}]", userId, postId, e);
            // IM 发送失败时，降级使用站内消息通知
            messageService.createSystemNotice(
                    userId,
                    NoticeTypeEnum.CONTENT_VIOLATION.getType(),
                    "内容审核通知",
                    content,
                    postId,
                    1 // relatedType: 1 表示帖子
            );
        }
    }

    @Override
    public AppPostRespVO getPost(Long postId, Long userId) {
        // 1. 查询帖子
        ForumPostDO post = postMapper.selectById(postId);
        if (post == null) {
            throw ServiceExceptionUtil.exception(POST_NOT_EXISTS);
        }

        // 2. 转换为 VO
        AppPostRespVO respVO = buildPostRespVO(post, userId);

        // 3. 异步增加浏览次数
        increaseViewCount(postId);

        return respVO;
    }

    @Override
    public PageResult<AppPostRespVO> getPostPage(AppPostPageReqVO reqVO, Long userId) {
        // 1. 分页查询帖子（如果查询的是他人主页，会自动过滤掉匿名帖子）
        PageResult<ForumPostDO> pageResult = postMapper.selectPage(reqVO, userId);

        // 2. 转换为 VO
        List<AppPostRespVO> list = pageResult.getList().stream()
                .map(post -> buildPostRespVO(post, userId))
                .collect(Collectors.toList());

        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    public List<AppPostRespVO> getHotPosts(Integer limit, Integer windowHours, Long userId) {
        int size = resolveHotLimit(limit);
        LocalDateTime startTime = resolveWindowStart(windowHours);

        List<ForumPostDO> hotPosts = postMapper.selectHotList(size, startTime, PostStatusEnum.APPROVED.getStatus());

        // 如果热点帖子不足最小数量，补充按点赞数和发布时间排序的帖子
        if (hotPosts.size() < MIN_HOT_LIMIT) {
            int needed = MIN_HOT_LIMIT - hotPosts.size();
            // 获取已有帖子ID列表，避免重复
            List<Long> existingIds = hotPosts.stream()
                    .map(ForumPostDO::getId)
                    .collect(Collectors.toList());

            // 查询补充帖子：按点赞数和创建时间排序
            List<ForumPostDO> fallbackPosts = postMapper.selectList(
                    new LambdaQueryWrapperX<ForumPostDO>()
                            .eq(ForumPostDO::getStatus, PostStatusEnum.APPROVED.getStatus())
                            .notIn(CollUtil.isNotEmpty(existingIds), ForumPostDO::getId, existingIds)
                            .orderByDesc(ForumPostDO::getLikeCount)
                            .orderByDesc(ForumPostDO::getCreateTime)
                            .last("LIMIT " + needed));

            if (CollUtil.isNotEmpty(fallbackPosts)) {
                hotPosts = new ArrayList<>(hotPosts);
                hotPosts.addAll(fallbackPosts);
            }
        }

        if (CollUtil.isEmpty(hotPosts)) {
            return Collections.emptyList();
        }

        return hotPosts.stream()
                .map(post -> buildPostRespVO(post, userId))
                .collect(Collectors.toList());
    }

    private int resolveHotLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_HOT_LIMIT;
        }
        // 限制在 MIN_HOT_LIMIT 和 MAX_HOT_LIMIT 之间
        return Math.max(MIN_HOT_LIMIT, Math.min(limit, MAX_HOT_LIMIT));
    }

    private LocalDateTime resolveWindowStart(Integer windowHours) {
        int hours = DEFAULT_HOT_WINDOW_HOURS;
        if (windowHours != null && windowHours > 0) {
            hours = Math.min(windowHours, MAX_HOT_WINDOW_HOURS);
        }
        return LocalDateTime.now().minusHours(hours);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long postId, Long userId) {
        // 1. 查询帖子
        ForumPostDO post = postMapper.selectById(postId);
        if (post == null) {
            throw ServiceExceptionUtil.exception(POST_NOT_EXISTS);
        }

        // 2. 校验是否是本人的帖子
        if (!post.getUserId().equals(userId)) {
            throw ServiceExceptionUtil.exception(POST_DELETE_FAIL_NOT_OWNER);
        }

        // 3. 删除帖子
        postMapper.deleteById(postId);

        // 4. 减少用户发帖数
        userProfileService.decreasePostCount(userId);

        log.info("[deletePost][删除帖子成功，postId={}, userId={}]", postId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePostByAdmin(Long postId, Long userId) {
        ForumPostDO post = postMapper.selectById(postId);
        if (post == null) {
            throw ServiceExceptionUtil.exception(POST_NOT_EXISTS);
        }

        postMapper.deleteById(postId);
        userProfileService.decreasePostCount(post.getUserId());

        log.info("[deletePostByAdmin][管理员删除帖子成功，postId={}, adminId={}, ownerId={}]", postId, userId, post.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewPost(Long postId, boolean approve, String reviewRemark, Long userId) {
        ForumPostDO post = postMapper.selectById(postId);
        if (post == null) {
            throw ServiceExceptionUtil.exception(POST_NOT_EXISTS);
        }

        int targetStatus = approve ? PostStatusEnum.APPROVED.getStatus() : PostStatusEnum.REJECTED.getStatus();
        ForumPostDO updateObj = ForumPostDO.builder()
                .id(postId)
                .status(targetStatus)
                .build();
        // 如果审核不通过，保存审核结论
        if (!approve) {
            updateObj.setReviewResult(reviewRemark);
        }
        postMapper.updateById(updateObj);

        log.info("[reviewPost][审核帖子成功，postId={}, approve={}, adminId={}]", postId, approve, userId);

        // 如果审核不通过，发送通知给发帖人
        if (!approve && StrUtil.isNotBlank(reviewRemark)) {
            sendPostRejectionNotice(post.getUserId(), postId, post.getTitle(), reviewRemark);
        }

        // 异步同步到 ES
        applicationContext.publishEvent(new ForumPostSaveEvent(postId));
    }

    /**
     * 发送帖子审核不通过通知
     */
    private void sendPostRejectionNotice(Long userId, Long postId, String postTitle, String reason) {
        String content = "【内容审核通知】您发布的内容审核未通过，因涉及违规内容。如有疑问，请联系客服处理。";

        // 通过 IM 服务发送消息通知用户
        try {
            String toAccount = String.valueOf(userId);
            tencentImMessageService.sendAdminTextMessage(toAccount, content);
            log.info("[sendPostRejectionNotice][通过IM发送帖子审核不通过通知成功，userId={}, postId={}]", userId, postId);
        } catch (Exception e) {
            log.warn("[sendPostRejectionNotice][通过IM发送帖子审核不通过通知失败，userId={}, postId={}]", userId, postId, e);
            // IM 发送失败时，降级使用站内消息通知
            messageService.createSystemNotice(
                    userId,
                    NoticeTypeEnum.SYSTEM.getType(),
                    "内容审核通知",
                    content,
                    postId,
                    1 // relatedType: 1 表示帖子
            );
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likePost(Long postId, Long userId) {
        // 0. 管理员不能点赞
        ForumUserProfileDO userProfile = userProfileMapper.selectByUserId(userId);
        if (userProfile != null && Boolean.TRUE.equals(userProfile.getIsAdmin())) {
            throw ServiceExceptionUtil.exception(ADMIN_OPERATION_FORBIDDEN);
        }

        // 1. 查询帖子
        ForumPostDO post = postMapper.selectById(postId);
        if (post == null) {
            throw ServiceExceptionUtil.exception(POST_NOT_EXISTS);
        }

        // 2. 检查是否已点赞
        ForumPostLikeDO existLike = postLikeMapper.selectByPostIdAndUserId(postId, userId);
        if (existLike != null) {
            throw ServiceExceptionUtil.exception(POST_ALREADY_LIKED);
        }

        // 3. 创建点赞记录
        ForumPostLikeDO deletedLike = postLikeMapper.selectByPostIdAndUserIdIncludeDeleted(postId, userId);
        if (deletedLike != null) {
            postLikeMapper.recoverByPostIdAndUserId(postId, userId);
        } else {
            ForumPostLikeDO like = ForumPostLikeDO.builder()
                    .postId(postId)
                    .userId(userId)
                    .build();
            postLikeMapper.insert(like);
        }

        // 4. 增加帖子点赞数
        ForumPostDO updateObj = ForumPostDO.builder()
                .id(postId)
                .likeCount(post.getLikeCount() + 1)
                .build();
        postMapper.updateById(updateObj);

        // 5. 通知帖子作者（如果不是自己点赞自己的帖子）
        // 注：点赞不再给积分，只有签到和参加完活动才给积分
        if (!post.getUserId().equals(userId)) {
            // pointService.addPostLikePoint(post.getUserId(), postId.toString());
            interactionUnreadService.incrementLikeUnread(post.getUserId(), 1);
            messageService.createSystemNotice(post.getUserId(), NoticeTypeEnum.LIKE.getType(),
                    "你的帖子被点赞了",
                    String.format("%s 点赞了你的帖子《%s》", getNickname(userId), StrUtil.nullToDefault(post.getTitle(), "")),
                    postId, 1);
            // 6. 增加帖子作者的获赞数
            userProfileService.increaseLikeCount(post.getUserId(), 1);
        }

        log.info("[likePost][点赞帖子成功，postId={}, userId={}]", postId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlikePost(Long postId, Long userId) {
        // 1. 查询帖子
        ForumPostDO post = postMapper.selectById(postId);
        if (post == null) {
            throw ServiceExceptionUtil.exception(POST_NOT_EXISTS);
        }

        // 2. 删除点赞记录
        int deleted = postLikeMapper.deleteByPostIdAndUserId(postId, userId);
        if (deleted == 0) {
            throw ServiceExceptionUtil.exception(POST_NOT_LIKED);
        }

        // 3. 减少帖子点赞数
        if (post.getLikeCount() > 0) {
            ForumPostDO updateObj = ForumPostDO.builder()
                    .id(postId)
                    .likeCount(post.getLikeCount() - 1)
                    .build();
            postMapper.updateById(updateObj);
        }

        // 4. 减少帖子作者的获赞数
        if (!post.getUserId().equals(userId)) {
            userProfileService.increaseLikeCount(post.getUserId(), -1);
        }

        log.info("[unlikePost][取消点赞帖子成功，postId={}, userId={}]", postId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void followPost(Long postId, Long userId) {
        // 1. 查询帖子
        ForumPostDO post = postMapper.selectById(postId);
        if (post == null) {
            throw ServiceExceptionUtil.exception(POST_NOT_EXISTS);
        }

        // 2. 检查是否已关注
        ForumPostFollowDO existFollow = postFollowMapper.selectByPostIdAndUserId(postId, userId);
        if (existFollow != null) {
            throw ServiceExceptionUtil.exception(POST_ALREADY_FOLLOWED);
        }

        // 3. 创建关注记录
        ForumPostFollowDO deletedFollow = postFollowMapper.selectByPostIdAndUserIdIncludeDeleted(postId, userId);
        if (deletedFollow != null) {
            postFollowMapper.recoverByPostIdAndUserId(postId, userId);
        } else {
            ForumPostFollowDO follow = ForumPostFollowDO.builder()
                    .postId(postId)
                    .userId(userId)
                    .build();
            postFollowMapper.insert(follow);
        }

        // 4. 增加帖子关注数
        ForumPostDO updateObj = ForumPostDO.builder()
                .id(postId)
                .followCount(post.getFollowCount() + 1)
                .build();
        postMapper.updateById(updateObj);

        // 5. 增加当前用户的收藏数（我的收藏数）
        userProfileService.increaseFavoriteCount(userId, 1);

        // 6. 当关注数达到15的倍数时，通知所有蹲后续用户
        int newFollowCount = post.getFollowCount() + 1;
        if (newFollowCount > 0 && newFollowCount % 15 == 0) {
            // 获取所有蹲后续该帖子的用户（排除当前用户和帖子作者）
            List<Long> followerUserIds = postFollowMapper.selectUserIdsByPostId(postId);
            if (CollUtil.isNotEmpty(followerUserIds)) {
                String postTitle = StrUtil.nullToDefault(post.getTitle(), "某帖子");
                // 标题直接显示完整信息
                String title = String.format("你蹲的帖子有%d个新关注，快看看他们在聊什么吧", newFollowCount);
                String content = String.format("你蹲的帖子《%s》有%d个新关注，快看看他们在聊什么吧", postTitle, newFollowCount);
                for (Long followerUserId : followerUserIds) {
                    // 排除当前关注者自己
                    if (!followerUserId.equals(userId)) {
                        messageService.createSystemNotice(followerUserId, NoticeTypeEnum.FOLLOW.getType(),
                                title, content, postId, 1);
                    }
                }
            }
        }

        log.info("[followPost][关注帖子成功，postId={}, userId={}]", postId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollowPost(Long postId, Long userId) {
        // 1. 查询帖子
        ForumPostDO post = postMapper.selectById(postId);
        if (post == null) {
            throw ServiceExceptionUtil.exception(POST_NOT_EXISTS);
        }

        // 2. 删除关注记录
        int deleted = postFollowMapper.deleteByPostIdAndUserId(postId, userId);
        if (deleted == 0) {
            throw ServiceExceptionUtil.exception(POST_NOT_FOLLOWED);
        }

        // 3. 减少帖子关注数
        if (post.getFollowCount() > 0) {
            ForumPostDO updateObj = ForumPostDO.builder()
                    .id(postId)
                    .followCount(post.getFollowCount() - 1)
                    .build();
            postMapper.updateById(updateObj);
        }

        // 4. 减少当前用户的收藏数（我的收藏数）
        userProfileService.increaseFavoriteCount(userId, -1);

        log.info("[unfollowPost][取消关注帖子成功，postId={}, userId={}]", postId, userId);
    }

    @Override
    public void increaseViewCount(Long postId) {
        ForumPostDO post = postMapper.selectById(postId);
        if (post != null) {
            ForumPostDO updateObj = ForumPostDO.builder()
                    .id(postId)
                    .viewCount(post.getViewCount() + 1)
                    .build();
            postMapper.updateById(updateObj);
        }
    }

    /**
     * 构建帖子响应 VO
     */
    private AppPostRespVO buildPostRespVO(ForumPostDO post, Long userId) {
        AppPostRespVO respVO = ForumPostConvert.INSTANCE.convert(post);
        respVO.setSchool(post.getSchool());
        // 1. 设置分类名称
        PostCategoryEnum category = PostCategoryEnum.getByType(post.getCategory());
        if (category != null) {
            respVO.setCategoryName(category.getName());
        }

        List<Integer> categoryIds = parseCategoryIds(post);
        respVO.setCategories(categoryIds);
        List<String> categoryNames = new ArrayList<>();
        categoryIds.forEach(catId -> {
            PostCategoryEnum catEnum = PostCategoryEnum.getByType(catId);
            if (catEnum != null) {
                categoryNames.add(catEnum.getName());
            }
        });
        respVO.setCategoryNames(categoryNames);
        if (respVO.getCategoryName() == null && CollUtil.isNotEmpty(categoryNames)) {
            respVO.setCategoryName(categoryNames.get(0));
        }

        // 2. 设置图片列表
        if (post.getImageUrls() != null) {
            respVO.setImageUrls(JSONUtil.toList(post.getImageUrls(), String.class));
        }

        // 3. 设置用户信息
        ForumUserProfileDO profile = userProfileMapper.selectByUserId(post.getUserId());
        // 始终设置 authorUserId，用于前端判断楼主标签
        respVO.setAuthorUserId(post.getUserId());

        if (post.getAnonymous()) {
            // “不展示个人主页”：展示真实昵称和头像，但不暴露 userId/uid，无法查看主页
            if (profile != null) {
                respVO.setNickname(profile.getNickname());
                respVO.setAvatar(profile.getAvatar());
            } else {
                respVO.setNickname("用户");
                respVO.setAvatar(null);
            }
            respVO.setUid(null);
            respVO.setUserId(null);
        } else {
            if (profile != null) {
                respVO.setUid(profile.getUid());
                respVO.setNickname(profile.getNickname());
                respVO.setAvatar(profile.getAvatar());
            }
        }

        // 3.1. 判断是否为管理员帖子
        if (profile != null && Boolean.TRUE.equals(profile.getIsAdmin())) {
            respVO.setIsAdminPost(true);
        } else {
            respVO.setIsAdminPost(false);
        }

        // 3.2. 设置审核结论
        respVO.setReviewResult(post.getReviewResult());

        // 4. 如果有当前用户，设置点赞和关注状态
        if (userId != null) {
            ForumPostLikeDO like = postLikeMapper.selectByPostIdAndUserId(post.getId(), userId);
            respVO.setLiked(like != null);

            ForumPostFollowDO follow = postFollowMapper.selectByPostIdAndUserId(post.getId(), userId);
            respVO.setFollowed(follow != null);
        } else {
            respVO.setLiked(false);
            respVO.setFollowed(false);
        }
        // 5. 检查是否有帖子内嵌投票
        PostVoteDO postVote = postVoteService.getPostVote(post.getId());
        respVO.setHasVote(postVote != null);

        return respVO;
    }

    /**
     * 兼容单选/多选分类，生成分类列表
     */
    private List<Integer> resolveCategories(AppPostCreateReqVO reqVO) {
        if (CollUtil.isNotEmpty(reqVO.getCategories())) {
            return reqVO.getCategories();
        }
        if (reqVO.getCategory() != null) {
            return Collections.singletonList(reqVO.getCategory());
        }
        return Collections.emptyList();
    }

    private List<Integer> parseCategoryIds(ForumPostDO post) {
        if (StrUtil.isNotBlank(post.getCategories())) {
            return JSONUtil.parseArray(post.getCategories()).stream()
                    .map(obj -> {
                        if (obj instanceof Number) {
                            return ((Number) obj).intValue();
                        }
                        return Integer.valueOf(obj.toString());
                    })
                    .collect(Collectors.toList());
        }
        if (post.getCategory() != null) {
            return Collections.singletonList(post.getCategory());
        }
        return Collections.emptyList();
    }

    private String getNickname(Long userId) {
        ForumUserProfileDO profile = userProfileMapper.selectByUserId(userId);
        if (profile != null && StrUtil.isNotBlank(profile.getNickname())) {
            return profile.getNickname();
        }
        return "有人";
    }

    /**
     * 获取用户的微信小程序 openid
     */
    private String getUserWxOpenid(Long userId) {
        try {
            SocialUserRespDTO socialUser = socialUserApi.getSocialUserByUserId(
                    UserTypeEnum.MEMBER.getValue(), userId, SocialTypeEnum.WECHAT_MINI_PROGRAM.getType());
            String openid = socialUser != null ? socialUser.getOpenid() : null;
            log.info("[getUserWxOpenid][获取用户微信openid，userId={} openid={}]", userId, openid);
            return openid;
        } catch (Exception e) {
            log.error("[getUserWxOpenid][获取用户微信openid失败，userId={}]", userId, e);
            return null;
        }
    }

    @Override
    public List<AppPostRespVO> getMyFollowedPosts(Long userId) {
        // 1. 查询用户收藏的帖子ID列表
        List<Long> postIds = postFollowMapper.selectPostIdsByUserId(userId);
        if (CollUtil.isEmpty(postIds)) {
            return Collections.emptyList();
        }

        // 2. 批量查询帖子
        List<ForumPostDO> posts = postMapper.selectBatchIds(postIds);
        if (CollUtil.isEmpty(posts)) {
            return Collections.emptyList();
        }

        // 3. 按收藏顺序排序（保持postIds的顺序）
        Map<Long, ForumPostDO> postMap = posts.stream()
                .collect(java.util.stream.Collectors.toMap(ForumPostDO::getId, p -> p));
        List<ForumPostDO> orderedPosts = postIds.stream()
                .map(postMap::get)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());

        // 4. 转换为 VO
        return orderedPosts.stream()
                .map(post -> buildPostRespVO(post, userId))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public PageResult<AppPostRespVO> getLikedPostsByUserId(Long targetUserId, Long currentUserId, Integer pageNo,
            Integer pageSize) {
        // 1. 计算分页偏移量
        int offset = (pageNo - 1) * pageSize;

        // 2. 查询用户点赞的帖子ID列表
        List<Long> postIds = postLikeMapper.selectPostIdsByUserIdPage(targetUserId, offset, pageSize);

        // 3. 查询总数
        Long total = postLikeMapper.countByUserId(targetUserId);

        if (CollUtil.isEmpty(postIds)) {
            return new PageResult<>(Collections.emptyList(), total);
        }

        // 4. 批量查询帖子
        List<ForumPostDO> posts = postMapper.selectBatchIds(postIds);
        if (CollUtil.isEmpty(posts)) {
            return new PageResult<>(Collections.emptyList(), total);
        }

        // 5. 按点赞顺序排序（保持postIds的顺序）
        Map<Long, ForumPostDO> postMap = posts.stream()
                .collect(Collectors.toMap(ForumPostDO::getId, p -> p));
        List<ForumPostDO> orderedPosts = postIds.stream()
                .map(postMap::get)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        // 6. 转换为 VO
        List<AppPostRespVO> list = orderedPosts.stream()
                .map(post -> buildPostRespVO(post, currentUserId))
                .collect(Collectors.toList());

        return new PageResult<>(list, total);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPostTop(Long postId, boolean isTop, Long adminId) {
        // 1. 查询帖子
        ForumPostDO post = postMapper.selectById(postId);
        if (post == null) {
            throw ServiceExceptionUtil.exception(POST_NOT_EXISTS);
        }

        // 2. 检查帖子是否是管理员发布的，只有管理员发布的帖子才能被置顶
        // ForumUserProfileDO postAuthor = userProfileService.getUserProfileByUserId(post.getUserId());
        // if (postAuthor == null || !Boolean.TRUE.equals(postAuthor.getIsAdmin())) {
        //     throw ServiceExceptionUtil.exception(POST_TOP_ONLY_ADMIN);
        // }

        // 3. 更新置顶状态
        ForumPostDO updateObj = ForumPostDO.builder()
                .id(postId)
                .isTop(isTop)
                .build();
        postMapper.updateById(updateObj);

        log.info("[setPostTop][{}帖子成功，postId={}, adminId={}]", isTop ? "置顶" : "取消置顶", postId, adminId);
    }
}