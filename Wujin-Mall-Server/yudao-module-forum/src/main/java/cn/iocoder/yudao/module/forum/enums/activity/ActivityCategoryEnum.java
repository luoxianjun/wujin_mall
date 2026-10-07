package cn.iocoder.yudao.module.forum.enums.activity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 活动分类枚举
 *
 * @author forum
 */
@Getter
@AllArgsConstructor
public enum ActivityCategoryEnum {

    ACADEMIC(1, "学术讲座"),
    SPORTS(2, "文体活动"),
    CLUB(3, "社团活动"),
    VOLUNTEER(4, "志愿服务"),
    OTHER(5, "其他"),
    LOTTERY(11, "抽奖"),
    QUIZ(12, "答题"),
    VOTE(13, "投票");

    /**
     * 分类
     */
    private final Integer category;

    /**
     * 分类名称
     */
    private final String name;

    public static ActivityCategoryEnum getByCategory(Integer category) {
        for (ActivityCategoryEnum value : values()) {
            if (value.getCategory().equals(category)) {
                return value;
            }
        }
        return null;
    }

}

