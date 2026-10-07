package cn.iocoder.yudao.module.forum.controller.admin.message.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 管理后台 - 系统广播消息发送请求 VO
 *
 * @author forum
 */
@Schema(description = "管理后台 - 系统广播消息发送请求 VO")
@Data
public class AdminSystemBroadcastReqVO {

    @Schema(description = "消息标题", example = "系统公告")
    private String title;

    @Schema(description = "消息内容（富文本HTML）", requiredMode = Schema.RequiredMode.REQUIRED, example = "<p>欢迎使用本系统</p>")
    @NotBlank(message = "消息内容不能为空")
    private String content;

}
