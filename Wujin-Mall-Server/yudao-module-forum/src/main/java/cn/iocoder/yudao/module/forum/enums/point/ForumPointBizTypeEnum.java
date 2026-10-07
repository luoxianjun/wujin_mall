package cn.iocoder.yudao.module.forum.enums.point;

import cn.hutool.core.util.EnumUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 论坛积分的业务类型枚举
 */
@AllArgsConstructor
@Getter
public enum ForumPointBizTypeEnum implements ArrayValuable<Integer> {

    SIGN(1, "每日签到", "每日签到获得 {} 积分"),
    POST_PUBLISH(2, "发帖奖励", "发帖获得 {} 积分"),
    COMMENT_PUBLISH(3, "评论奖励", "评论获得 {} 积分"),
    POST_BE_LIKED(4, "帖子被点赞", "帖子被点赞获得 {} 积分"),
    ACTIVITY_SIGN_UP(5, "活动报名", "活动报名获得 {} 积分"),
    ACTIVITY_CHECK_IN(6, "活动签到", "活动签到获得 {} 积分"),
    ACTIVITY_SIGN_UP_COST(7, "活动报名消耗", "活动报名消耗 {} 积分"),
    ADMIN(99, "管理员调整", "管理员调整 {} 积分");

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名字
     */
    private final String name;
    /**
     * 描述
     */
    private final String description;

    @Override
    public Integer[] array() {
        return new Integer[0];
    }

    public static ForumPointBizTypeEnum getByType(Integer type) {
        return EnumUtil.getBy(ForumPointBizTypeEnum.class,
                e -> Objects.equals(type, e.getType()));
    }

}
