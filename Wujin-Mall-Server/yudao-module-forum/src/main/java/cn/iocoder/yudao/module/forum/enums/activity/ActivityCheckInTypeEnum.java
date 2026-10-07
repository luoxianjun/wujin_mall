package cn.iocoder.yudao.module.forum.enums.activity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 活动签到方式枚举
 */
@AllArgsConstructor
@Getter
public enum ActivityCheckInTypeEnum {

    SELF(1, "自助签到"),
    LOCATION(2, "定位签到"),
    QR(3, "扫码签到");

    private final Integer type;
    private final String name;

    public static String getName(Integer type) {
        for (ActivityCheckInTypeEnum value : values()) {
            if (value.type.equals(type)) {
                return value.name;
            }
        }
        return null;
    }
}
