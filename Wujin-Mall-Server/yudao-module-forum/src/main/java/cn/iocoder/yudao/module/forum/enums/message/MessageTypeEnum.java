package cn.iocoder.yudao.module.forum.enums.message;

import cn.hutool.core.util.EnumUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 消息类型枚举
 *
 * @author forum
 */
@AllArgsConstructor
@Getter
public enum MessageTypeEnum {

    TEXT(1, "文本"),
    IMAGE(2, "图片"),
    VOICE(3, "语音"),
    VIDEO(4, "视频");

    private final Integer type;
    private final String name;

    public static MessageTypeEnum getByType(Integer type) {
        return EnumUtil.getBy(MessageTypeEnum.class,
                e -> Objects.equals(type, e.getType()));
    }

}

