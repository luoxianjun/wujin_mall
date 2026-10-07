package cn.iocoder.yudao.module.forum.event.post;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 帖子保存事件
 */
@Data
@AllArgsConstructor
public class ForumPostSaveEvent {

    /**
     * 帖子 ID
     */
    private Long id;

}
