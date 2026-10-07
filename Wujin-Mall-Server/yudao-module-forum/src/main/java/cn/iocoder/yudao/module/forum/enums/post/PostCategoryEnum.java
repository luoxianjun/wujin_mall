package cn.iocoder.yudao.module.forum.enums.post;

import cn.hutool.core.util.EnumUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 帖子分类枚举
 *
 * @author forum
 */
@AllArgsConstructor
@Getter
public enum PostCategoryEnum implements ArrayValuable<Integer> {

    CONFESSION(1, "表白墙"),
    HELP(2, "求助"),
    SHARE(3, "分享"),
    DISCUSSION(4, "讨论"),
    SECOND_HAND(5, "二手交易"),
    LOST_FOUND(6, "失物招领"),
    OTHER(99, "其他");

    /**
     * 类型
     */
    private final Integer type;

    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return new Integer[0];
    }

    public static PostCategoryEnum getByType(Integer type) {
        return EnumUtil.getBy(PostCategoryEnum.class,
                e -> Objects.equals(type, e.getType()));
    }

}

