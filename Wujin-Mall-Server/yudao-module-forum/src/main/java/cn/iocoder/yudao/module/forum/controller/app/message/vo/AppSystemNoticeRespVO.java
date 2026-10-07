package cn.iocoder.yudao.module.forum.controller.app.message.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统通知响应 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 系统通知响应 VO")
@Data
public class AppSystemNoticeRespVO {

    @Schema(description = "通知ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "通知类型：1-点赞通知，2-评论通知，3-关注通知，4-系统通知，5-活动通知", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer noticeType;

    @Schema(description = "通知类型名称", example = "点赞通知")
    private String noticeTypeName;

    @Schema(description = "通知标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "您的帖子被点赞了")
    private String title;

    @Schema(description = "通知内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "用户张三点赞了您的帖子")
    private String content;

    @Schema(description = "关联业务ID", example = "1")
    private Long relatedId;

    @Schema(description = "关联业务类型：1-帖子，2-评论，3-活动", example = "1")
    private Integer relatedType;

    @Schema(description = "是否已读", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean readStatus;

    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    private LocalDateTime createTime;

}

