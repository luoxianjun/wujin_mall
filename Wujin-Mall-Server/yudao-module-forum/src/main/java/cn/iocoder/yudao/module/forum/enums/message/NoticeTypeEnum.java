package cn.iocoder.yudao.module.forum.enums.message;

import cn.hutool.core.util.EnumUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 通知类型枚举
 *
 * @author forum
 */
@AllArgsConstructor
@Getter
public enum NoticeTypeEnum {

    LIKE(1, "点赞通知"),
    COMMENT(2, "评论通知"),
    FOLLOW(3, "关注通知"),
    SYSTEM(4, "系统通知"),
    ACTIVITY(5, "活动通知"),
    CONTENT_VIOLATION(6, "内容违规通知");

    private final Integer type;
    private final String name;

    public static NoticeTypeEnum getByType(Integer type) {
        return EnumUtil.getBy(NoticeTypeEnum.class,
                e -> Objects.equals(type, e.getType()));
    }

}

