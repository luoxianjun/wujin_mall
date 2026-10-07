package cn.iocoder.yudao.module.forum.service.post;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.forum.controller.app.post.vo.AppCommentCreateReqVO;
import cn.iocoder.yudao.module.forum.controller.app.post.vo.AppCommentPageReqVO;
import cn.iocoder.yudao.module.forum.controller.app.post.vo.AppCommentRespVO;
import cn.iocoder.yudao.module.forum.convert.post.ForumPostConvert;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.ForumCommentDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.ForumCommentLikeDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.ForumPostDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.user.ForumUserProfileDO;
import cn.iocoder.yudao.module.forum.dal.mysql.post.ForumCommentLikeMapper;
import cn.iocoder.yudao.module.forum.dal.mysql.post.ForumCommentMapper;
import cn.iocoder.yudao.module.forum.dal.mysql.post.ForumPostFollowMapper;
import cn.iocoder.yudao.module.forum.dal.mysql.post.ForumPostMapper;
import cn.iocoder.yudao.module.forum.dal.mysql.user.ForumUserProfileMapper;
import cn.iocoder.yudao.module.forum.enums.message.NoticeTypeEnum;
import cn.iocoder.yudao.module.forum.service.point.ForumPointService;
import cn.iocoder.yudao.module.forum.service.message.ForumInteractionUnreadService;
import cn.iocoder.yudao.module.forum.service.message.ForumMessageService;
import cn.iocoder.yudao.module.forum.service.text.ForumTextAuditService;
import cn.iocoder.yudao.module.im.service.TencentImMessageService;
import cn.iocoder.yudao.module.system.api.social.SocialUserApi;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserRespDTO;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.forum.enums.ErrorCodeConstants.*;

/**
 * 论坛评论 Service 实现类
 *
 * @author forum
 */
@Service
@Validated
@Slf4j
public class ForumCommentServiceImpl implements ForumCommentService {

    @Resource
    private ForumCommentMapper commentMapper;

    @Resource
    private ForumCommentLikeMapper commentLikeMapper;

    @Resource(name = "forumPostMapper")
    private ForumPostMapper postMapper;

    @Resource
    private ForumPostFollowMapper postFollowMapper;

    @Resource
    private ForumUserProfileMapper userProfileMapper;

    @Resource
    private ForumPointService pointService;

    @Resource
    private ForumInteractionUnreadService interactionUnreadService;

    @Resource
    private ForumMessageService messageService;

    @Resource
    private ForumTextAuditService textAuditService;

    @Resource
    private TencentImMessageService tencentImMessageService;

    @Resource
    private SocialUserApi socialUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createComment(Long userId, AppCommentCreateReqVO reqVO) {
        // 0. 管理员不能评论
        ForumUserProfileDO userProfile = userProfileMapper.selectByUserId(userId);
        if (userProfile != null && Boolean.TRUE.equals(userProfile.getIsAdmin())) {
            throw ServiceExceptionUtil.exception(ADMIN_OPERATION_FORBIDDEN);
        }

        // 1. 校验帖子是否存在
        ForumPostDO post = postMapper.selectById(reqVO.getPostId());
        if (post == null) {
            throw ServiceExceptionUtil.exception(POST_NOT_EXISTS);
        }

        // 1.1 文本审核：评论内容（同步审核，审核失败直接抛异常）
        String openid = getUserWxOpenid(userId);
        textAuditService.audit(reqVO.getContent(), openid, "comment-content");

        // 2. 构建评论对象（审核通过，直接设为正常状态）
        ForumCommentDO comment = ForumCommentDO.builder()
                .postId(reqVO.getPostId())
                .userId(userId)
                .parentId(reqVO.getParentId())
                .rootId(reqVO.getRootId())
                .content(reqVO.getContent())
                .anonymous(reqVO.getAnonymous())
                .status(0) // 正常状态
                .likeCount(0)
                .build();

        // 3. “不展示个人主页”模式：不生成匿名昵称/头像，展示时用用户真实昵称和头像，但不暴露 userId

        // 4. 插入评论
        commentMapper.insert(comment);

        // 6. 更新帖子评论数和最后评论时间（只有正常评论才更新）
        ForumPostDO updatePost = ForumPostDO.builder()
                .id(reqVO.getPostId())
                .commentCount(post.getCommentCount() + 1)
                .latestCommentTime(LocalDateTime.now())
                .build();
        postMapper.updateById(updatePost);

        // 注：评论不再给积分，只有签到和参加完活动才给积分
        // pointService.addCommentPoint(userId, comment.getId().toString());

        // 7. 给被评论的用户增加未读评论提醒（帖子作者或被回复的评论作者）
        Long targetUserId = resolveCommentTargetUser(reqVO, post);
        if (targetUserId != null && !targetUserId.equals(userId)) {
            interactionUnreadService.incrementCommentUnread(targetUserId, 1);
            String title = targetUserId.equals(post.getUserId()) ? "你的帖子有新评论" : "你的评论有新回复";
            // 使用 getCommentNickname 以正确处理匿名评论
            String commenterNickname = getCommentNickname(comment);
            String content = String.format("%s 评论了%s：%s",
                    commenterNickname != null ? commenterNickname : "有人",
                    targetUserId.equals(post.getUserId()) ? "你的帖子" : "你的评论",
                    abbreviate(reqVO.getContent(), 50));
            // 使用帖子 ID 作为 relatedId，方便前端跳转到帖子详情页
            messageService.createSystemNotice(targetUserId, NoticeTypeEnum.COMMENT.getType(),
                    title, content, post.getId(), 1);
        }

        // 9. 给关注/点赞/评论过该帖子的用户增加"帖子新回复"未读数（排除自己、帖子作者和被评论用户）
        addPostReplyUnread(post.getId(), comment, post.getUserId(), targetUserId);

        log.info("[createComment][创建评论成功，commentId={}, userId={}]", comment.getId(), userId);

        return comment.getId();
    }

    /**
     * 发送评论违规通知（通过 IM 服务）
     */
    private void sendCommentViolationNotice(Long userId, Long commentId, String postTitle, String reason) {
        String content = "【内容审核通知】您发布的内容因涉及违规内容已被系统拦截，暂不会公开显示。如有疑问，请联系客服处理。";

        // 通过 IM 服务发送消息通知用户
        try {
            String toAccount = String.valueOf(userId);
            tencentImMessageService.sendAdminTextMessage(toAccount, content);
            log.info("[sendCommentViolationNotice][通过IM发送评论违规通知成功，userId={}, commentId={}]", userId, commentId);
        } catch (Exception e) {
            log.warn("[sendCommentViolationNotice][通过IM发送评论违规通知失败，userId={}, commentId={}]", userId, commentId, e);
            // IM 发送失败时，降级使用站内消息通知
            messageService.createSystemNotice(
                    userId,
                    NoticeTypeEnum.CONTENT_VIOLATION.getType(),
                    "内容审核通知",
                    content,
                    commentId,
                    2 // relatedType: 2 表示评论
            );
        }
    }

    @Override
    public PageResult<AppCommentRespVO> getCommentPage(AppCommentPageReqVO reqVO, Long userId) {
        // 如果是查询用户主页的评论，使用原有的扁平化分页逻辑
        if (reqVO.getUserId() != null) {
            return getCommentPageForUser(reqVO, userId);
        }

        // 如果是查询帖子的评论，使用按根评论分页的逻辑
        if (reqVO.getPostId() != null) {
            return getCommentPageForPost(reqVO, userId);
        }

        // 其他情况使用原有逻辑
        return getCommentPageForUser(reqVO, userId);
    }

    /**
     * 按根评论分页查询帖子评论
     * 每页返回 N 个根评论及其所有子评论
     */
    private PageResult<AppCommentRespVO> getCommentPageForPost(AppCommentPageReqVO reqVO, Long userId) {
        Long postId = reqVO.getPostId();

        // 1. 查询所有根评论（rootId 为 null）
        LambdaQueryWrapperX<ForumCommentDO> rootWrapper = new LambdaQueryWrapperX<>();
        rootWrapper.eq(ForumCommentDO::getPostId, postId)
                .isNull(ForumCommentDO::getRootId)
                .eq(ForumCommentDO::getStatus, 0)
                .orderByDesc(ForumCommentDO::getCreateTime);
        List<ForumCommentDO> allRootComments = commentMapper.selectList(rootWrapper);

        // 2. 计算分页
        int pageNo = reqVO.getPageNo() != null ? reqVO.getPageNo() : 1;
        int pageSize = reqVO.getPageSize() != null ? reqVO.getPageSize() : 10;
        int total = allRootComments.size();
        int fromIndex = (pageNo - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        if (fromIndex >= total) {
            return new PageResult<>(CollUtil.newArrayList(), (long) total);
        }

        // 3. 获取当前页的根评论
        List<ForumCommentDO> pageRootComments = allRootComments.subList(fromIndex, toIndex);
        List<Long> rootCommentIds = pageRootComments.stream()
                .map(ForumCommentDO::getId)
                .collect(Collectors.toList());

        // 4. 查询这些根评论的所有子评论
        List<ForumCommentDO> childComments = CollUtil.newArrayList();
        if (CollUtil.isNotEmpty(rootCommentIds)) {
            LambdaQueryWrapperX<ForumCommentDO> childWrapper = new LambdaQueryWrapperX<>();
            childWrapper.in(ForumCommentDO::getRootId, rootCommentIds)
                    .eq(ForumCommentDO::getStatus, 0)
                    .orderByAsc(ForumCommentDO::getCreateTime);
            childComments = commentMapper.selectList(childWrapper);
        }

        // 5. 合并根评论和子评论
        List<ForumCommentDO> allComments = new java.util.ArrayList<>();
        allComments.addAll(pageRootComments);
        allComments.addAll(childComments);

        // 6. 构建评论 Map（用于查找父评论）
        Map<Long, ForumCommentDO> commentMap = allComments.stream()
                .collect(Collectors.toMap(ForumCommentDO::getId, c -> c, (a, b) -> a));

        // 7. 转换为 VO
        List<AppCommentRespVO> list = allComments.stream()
                .map(comment -> buildCommentRespVO(comment, userId, commentMap))
                .collect(Collectors.toList());

        return new PageResult<>(list, (long) total);
    }

    /**
     * 原有的扁平化分页逻辑（用于用户主页等场景）
     */
    private PageResult<AppCommentRespVO> getCommentPageForUser(AppCommentPageReqVO reqVO, Long userId) {
        // 如果查询的是他人主页，会自动过滤掉匿名评论
        PageResult<ForumCommentDO> pageResult = commentMapper.selectPage(reqVO, userId);

        Map<Long, ForumCommentDO> commentMap = pageResult.getList().stream()
                .collect(Collectors.toMap(ForumCommentDO::getId, c -> c, (a, b) -> a));

        // 如果按用户查询，需要获取帖子标题信息
        Map<Long, String> postTitleMap = null;
        if (reqVO.getUserId() != null) {
            List<Long> postIds = pageResult.getList().stream()
                    .map(ForumCommentDO::getPostId)
                    .distinct()
                    .collect(Collectors.toList());
            if (CollUtil.isNotEmpty(postIds)) {
                List<ForumPostDO> posts = postMapper.selectBatchIds(postIds);
                postTitleMap = posts.stream()
                        .collect(Collectors.toMap(ForumPostDO::getId, ForumPostDO::getTitle, (a, b) -> a));
            }
        }

        final Map<Long, String> finalPostTitleMap = postTitleMap;
        List<AppCommentRespVO> list = pageResult.getList().stream()
                .map(comment -> {
                    AppCommentRespVO vo = buildCommentRespVO(comment, userId, commentMap);
                    // 填充帖子标题
                    if (finalPostTitleMap != null) {
                        vo.setPostTitle(finalPostTitleMap.get(comment.getPostId()));
                    }
                    return vo;
                })
                .collect(Collectors.toList());

        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    public List<AppCommentRespVO> getCommentTree(Long postId, Long userId) {
        // 查询所有评论
        List<ForumCommentDO> allComments = commentMapper.selectListByPostId(postId);

        Map<Long, ForumCommentDO> commentMap = allComments.stream()
                .collect(Collectors.toMap(ForumCommentDO::getId, c -> c, (a, b) -> a));

        // 构建树形结构
        Map<Long, List<ForumCommentDO>> childrenMap = allComments.stream()
                .filter(c -> c.getRootId() != null)
                .collect(Collectors.groupingBy(ForumCommentDO::getRootId));

        // 获取根评论
        List<AppCommentRespVO> rootComments = allComments.stream()
                .filter(c -> c.getRootId() == null)
                .map(comment -> {
                    AppCommentRespVO vo = buildCommentRespVO(comment, userId, commentMap);
                    // 设置子评论
                    List<ForumCommentDO> children = childrenMap.get(comment.getId());
                    if (children != null) {
                        vo.setChildren(children.stream()
                                .map(child -> buildCommentRespVO(child, userId, commentMap))
                                .collect(Collectors.toList()));
                    }
                    return vo;
                })
                .collect(Collectors.toList());

        return rootComments;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, Long userId) {
        ForumCommentDO comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw ServiceExceptionUtil.exception(COMMENT_NOT_EXISTS);
        }

        if (!comment.getUserId().equals(userId)) {
            throw ServiceExceptionUtil.exception(COMMENT_DELETE_FAIL_NOT_OWNER);
        }

        commentMapper.deleteById(commentId);

        // 更新帖子评论数
        ForumPostDO post = postMapper.selectById(comment.getPostId());
        if (post != null && post.getCommentCount() > 0) {
            ForumPostDO updatePost = ForumPostDO.builder()
                    .id(comment.getPostId())
                    .commentCount(post.getCommentCount() - 1)
                    .build();
            postMapper.updateById(updatePost);
        }

        log.info("[deleteComment][删除评论成功，commentId={}, userId={}]", commentId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeComment(Long commentId, Long userId) {
        ForumCommentDO comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw ServiceExceptionUtil.exception(COMMENT_NOT_EXISTS);
        }

        ForumCommentLikeDO existLike = commentLikeMapper.selectByCommentIdAndUserId(commentId, userId);
        if (existLike != null) {
            throw ServiceExceptionUtil.exception(COMMENT_ALREADY_LIKED);
        }

        ForumCommentLikeDO deletedLike = commentLikeMapper.selectByCommentIdAndUserIdIncludeDeleted(commentId, userId);
        if (deletedLike != null) {
            commentLikeMapper.recoverByCommentIdAndUserId(commentId, userId);
        } else {
            ForumCommentLikeDO like = ForumCommentLikeDO.builder()
                    .commentId(commentId)
                    .userId(userId)
                    .build();
            commentLikeMapper.insert(like);
        }

        ForumCommentDO updateObj = ForumCommentDO.builder()
                .id(commentId)
                .likeCount(comment.getLikeCount() + 1)
                .build();
        commentMapper.updateById(updateObj);

        if (!comment.getUserId().equals(userId)) {
            interactionUnreadService.incrementLikeUnread(comment.getUserId(), 1);
            messageService.createSystemNotice(comment.getUserId(), NoticeTypeEnum.LIKE.getType(),
                    "你的评论被点赞了",
                    String.format("%s 点赞了你的评论：%s", getNickname(userId), abbreviate(comment.getContent(), 50)),
                    commentId, 2);
        }

        log.info("[likeComment][点赞评论成功，commentId={}, userId={}]", commentId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlikeComment(Long commentId, Long userId) {
        ForumCommentDO comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw ServiceExceptionUtil.exception(COMMENT_NOT_EXISTS);
        }

        int deleted = commentLikeMapper.deleteByCommentIdAndUserId(commentId, userId);
        if (deleted == 0) {
            throw ServiceExceptionUtil.exception(COMMENT_NOT_LIKED);
        }

        if (comment.getLikeCount() > 0) {
            ForumCommentDO updateObj = ForumCommentDO.builder()
                    .id(commentId)
                    .likeCount(comment.getLikeCount() - 1)
                    .build();
            commentMapper.updateById(updateObj);
        }

        log.info("[unlikeComment][取消点赞评论成功，commentId={}, userId={}]", commentId, userId);
    }

    private AppCommentRespVO buildCommentRespVO(ForumCommentDO comment, Long userId,
            Map<Long, ForumCommentDO> commentMap) {
        AppCommentRespVO respVO = ForumPostConvert.INSTANCE.convertComment(comment);

        ForumUserProfileDO profile = userProfileMapper.selectByUserId(comment.getUserId());
        if (Boolean.TRUE.equals(comment.getAnonymous())) {
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

        if (comment.getParentId() != null) {
            ForumCommentDO parent = findParentComment(comment.getParentId(), commentMap);
            respVO.setReplyNickname(getCommentNickname(parent));
        }

        if (userId != null) {
            ForumCommentLikeDO like = commentLikeMapper.selectByCommentIdAndUserId(comment.getId(), userId);
            respVO.setLiked(like != null);
        } else {
            respVO.setLiked(false);
        }

        return respVO;
    }

    private void addPostReplyUnread(Long postId, ForumCommentDO comment, Long postOwnerId, Long commentTargetUserId) {
        Set<Long> targetUserIds = new HashSet<>();
        // 只通知显式点击"蹲后续"的用户
        List<Long> followUserIds = postFollowMapper.selectUserIdsByPostId(postId);
        if (CollUtil.isNotEmpty(followUserIds)) {
            targetUserIds.addAll(followUserIds);
        }
        targetUserIds.remove(comment.getUserId());
        targetUserIds.remove(postOwnerId);
        if (commentTargetUserId != null) {
            targetUserIds.remove(commentTargetUserId);
        }

        // 获取帖子信息用于生成通知内容
        ForumPostDO post = postMapper.selectById(postId);
        // 使用 getCommentNickname 以正确处理匿名评论
        String actorNickname = getCommentNickname(comment);
        if (actorNickname == null) {
            actorNickname = "有人";
        }
        String postTitle = post != null ? StrUtil.nullToDefault(post.getTitle(), "某帖子") : "某帖子";

        for (Long uid : targetUserIds) {
            // 增加未读计数
            interactionUnreadService.incrementPostReplyUnread(uid, 1);
            // 创建系统通知，让蹲后续用户能看到评论消息
            String title = "你蹲的帖子有新评论";
            String content = String.format("%s 评论了你蹲的帖子《%s》", actorNickname, postTitle);
            messageService.createSystemNotice(uid, NoticeTypeEnum.FOLLOW.getType(),
                    title, content, postId, 1);
            log.info("[addPostReplyUnread][为蹲后续用户创建评论通知，postId={}, userId={}, targetUserId={}]",
                    postId, comment.getUserId(), uid);
        }
    }

    private ForumCommentDO findParentComment(Long parentId, Map<Long, ForumCommentDO> commentMap) {
        if (parentId == null) {
            return null;
        }
        if (commentMap != null) {
            ForumCommentDO parent = commentMap.get(parentId);
            if (parent != null) {
                return parent;
            }
        }
        return commentMapper.selectById(parentId);
    }

    private String getCommentNickname(ForumCommentDO comment) {
        if (comment == null) {
            return null;
        }
        // “不展示个人主页”与普通评论均返回用户真实昵称
        ForumUserProfileDO profile = userProfileMapper.selectByUserId(comment.getUserId());
        if (profile != null && StrUtil.isNotBlank(profile.getNickname())) {
            return profile.getNickname();
        }
        return null;
    }

    private Long resolveCommentTargetUser(AppCommentCreateReqVO reqVO, ForumPostDO post) {
        if (reqVO.getParentId() != null) {
            ForumCommentDO parent = commentMapper.selectById(reqVO.getParentId());
            if (parent != null) {
                return parent.getUserId();
            }
        }
        return post.getUserId();
    }

    private String getNickname(Long userId) {
        ForumUserProfileDO profile = userProfileMapper.selectByUserId(userId);
        if (profile != null && StrUtil.isNotBlank(profile.getNickname())) {
            return profile.getNickname();
        }
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
            return socialUser != null ? socialUser.getOpenid() : null;
        } catch (Exception e) {
            log.error("[getUserWxOpenid][获取用户微信openid失败，userId={}]", userId, e);
            return null;
        }
    }

    private String abbreviate(String content, int maxLen) {
        if (content == null) {
            return "";
        }
        return content.length() > maxLen ? content.substring(0, maxLen) + "..." : content;
    }

}
