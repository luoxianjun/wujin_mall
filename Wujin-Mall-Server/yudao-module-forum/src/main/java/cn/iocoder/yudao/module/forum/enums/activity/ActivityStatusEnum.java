package cn.iocoder.yudao.module.forum.enums.activity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 活动状态枚举
 *
 * @author forum
 */
@Getter
@AllArgsConstructor
public enum ActivityStatusEnum {

    DRAFT(0, "草稿"),
    SIGN_UP(1, "报名中"),
    IN_PROGRESS(2, "进行中"),
    ENDED(3, "已结束"),
    CANCELLED(4, "已取消"),
    NOT_STARTED(5, "未开始");

    /**
     * 状态
     */
    private final Integer status;

    /**
     * 状态名称
     */
    private final String name;

    public static ActivityStatusEnum getByStatus(Integer status) {
        for (ActivityStatusEnum value : values()) {
            if (value.getStatus().equals(status)) {
                return value;
            }
        }
        return null;
    }

}

