package cn.iocoder.yudao.module.forum.controller.app.message.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 未读消息统计响应 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 未读消息统计响应 VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUnreadCountRespVO {

    @Schema(description = "未读私信数", example = "5")
    private Integer unreadMessageCount;

    @Schema(description = "未读系统通知数", example = "3")
    private Integer unreadNoticeCount;

    @Schema(description = "未读总数", example = "8")
    private Integer totalUnreadCount;

}

