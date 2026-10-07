package cn.iocoder.yudao.module.forum.controller.app.message.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 互动未读统计响应 VO
 */
@Schema(description = "用户 APP - 互动未读统计响应 VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppInteractionUnreadRespVO {

    @Schema(description = "未读新点赞数", example = "3")
    private Integer unreadLikeCount;

    @Schema(description = "未读新评论数", example = "5")
    private Integer unreadCommentCount;

    @Schema(description = "未读帖子新回复数", example = "7")
    private Integer unreadPostReplyCount;

}
