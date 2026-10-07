package cn.iocoder.yudao.module.forum.enums.post;

import cn.hutool.core.util.EnumUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 帖子状态枚举
 *
 * @author forum
 */
@AllArgsConstructor
@Getter
public enum PostStatusEnum implements ArrayValuable<Integer> {

    PENDING(0, "待审核"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已驳回");

    /**
     * 状态
     */
    private final Integer status;

    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return new Integer[0];
    }

    public static PostStatusEnum getByStatus(Integer status) {
        return EnumUtil.getBy(PostStatusEnum.class,
                e -> Objects.equals(status, e.getStatus()));
    }

}

