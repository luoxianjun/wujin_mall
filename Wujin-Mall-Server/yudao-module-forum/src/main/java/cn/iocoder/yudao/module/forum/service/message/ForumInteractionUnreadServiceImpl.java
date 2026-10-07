package cn.iocoder.yudao.module.forum.service.message;

import cn.hutool.core.util.NumberUtil;
import cn.iocoder.yudao.module.forum.controller.app.message.vo.AppInteractionUnreadRespVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 论坛互动未读统计 Service 实现
 */
@Service
@Validated
@Slf4j
public class ForumInteractionUnreadServiceImpl implements ForumInteractionUnreadService {

    private static final String UNREAD_KEY_PREFIX = "forum:interaction:unread:";
    private static final String FIELD_LIKE = "like";
    private static final String FIELD_COMMENT = "comment";
    private static final String FIELD_POST_REPLY = "postReply";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void incrementLikeUnread(Long userId, long delta) {
        increment(userId, FIELD_LIKE, delta);
    }

    @Override
    public void incrementCommentUnread(Long userId, long delta) {
        increment(userId, FIELD_COMMENT, delta);
    }

    @Override
    public void incrementPostReplyUnread(Long userId, long delta) {
        increment(userId, FIELD_POST_REPLY, delta);
    }

    @Override
    public void clearLikeUnread(Long userId) {
        clear(userId, FIELD_LIKE);
    }

    @Override
    public void clearCommentUnread(Long userId) {
        clear(userId, FIELD_COMMENT);
    }

    @Override
    public void clearPostReplyUnread(Long userId) {
        clear(userId, FIELD_POST_REPLY);
    }

    @Override
    public AppInteractionUnreadRespVO getUnreadCount(Long userId) {
        String key = buildKey(userId);
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
        long likeCount = parse(entries.get(FIELD_LIKE));
        long commentCount = parse(entries.get(FIELD_COMMENT));
        long postReplyCount = parse(entries.get(FIELD_POST_REPLY));
        return AppInteractionUnreadRespVO.builder()
                .unreadLikeCount((int) likeCount)
                .unreadCommentCount((int) commentCount)
                .unreadPostReplyCount((int) postReplyCount)
                .build();
    }

    private void increment(Long userId, String field, long delta) {
        if (userId == null || delta <= 0) {
            return;
        }
        String key = buildKey(userId);
        try {
            stringRedisTemplate.opsForHash().increment(key, field, delta);
        } catch (Exception ex) {
            log.warn("[increment][写入未读计数失败 userId={}, field={}, delta={}]", userId, field, delta, ex);
        }
    }

    private void clear(Long userId, String field) {
        if (userId == null) {
            return;
        }
        String key = buildKey(userId);
        try {
            stringRedisTemplate.opsForHash().put(key, field, "0");
        } catch (Exception ex) {
            log.warn("[clear][清除未读计数失败 userId={}, field={}]", userId, field, ex);
        }
    }

    private String buildKey(Long userId) {
        return UNREAD_KEY_PREFIX + userId;
    }

    private long parse(Object value) {
        if (value == null) {
            return 0L;
        }
        return NumberUtil.parseLong(value.toString());
    }
}
