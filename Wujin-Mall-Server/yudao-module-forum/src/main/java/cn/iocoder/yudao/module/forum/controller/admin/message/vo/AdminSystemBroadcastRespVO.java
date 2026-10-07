package cn.iocoder.yudao.module.forum.controller.admin.message.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台 - 系统广播消息响应 VO
 *
 * @author forum
 */
@Schema(description = "管理后台 - 系统广播消息响应 VO")
@Data
public class AdminSystemBroadcastRespVO {

    @Schema(description = "消息ID", example = "1")
    private Long id;

    @Schema(description = "消息标题", example = "系统公告")
    private String title;

    @Schema(description = "消息内容（富文本HTML）", example = "<p>欢迎使用本系统</p>")
    private String content;

    @Schema(description = "发送者用户ID", example = "1")
    private Long senderId;

    @Schema(description = "发送成功数量", example = "100")
    private Integer successCount;

    @Schema(description = "发送失败数量", example = "2")
    private Integer failCount;

    @Schema(description = "发送状态：0-发送中，1-发送完成，2-发送失败", example = "1")
    private Integer status;

    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    private LocalDateTime createTime;

}
