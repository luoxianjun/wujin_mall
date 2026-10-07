package cn.iocoder.yudao.module.forum.enums.banner;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Banner 跳转类型枚举
 *
 * @author forum
 */
@Getter
@AllArgsConstructor
public enum BannerTargetTypeEnum {

    POST(1, "帖子详情"),
    ACTIVITY(2, "活动详情"),
    USER(3, "用户主页"),
    URL(4, "外部链接");

    /**
     * 类型值
     */
    private final Integer type;

    /**
     * 类型名称
     */
    private final String name;

    public static BannerTargetTypeEnum getByType(Integer type) {
        if (type == null) {
            return null;
        }
        for (BannerTargetTypeEnum value : values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return null;
    }

}
