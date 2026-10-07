package cn.iocoder.yudao.module.forum.controller.admin.config.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 管理后台 - 配置保存请求 VO
 *
 * @author forum
 */
@Schema(description = "管理后台 - 配置保存请求 VO")
@Data
public class AdminConfigSaveReqVO {

    @Schema(description = "配置键", requiredMode = Schema.RequiredMode.REQUIRED, example = "forum.welfare.content")
    @NotBlank(message = "配置键不能为空")
    private String key;

    @Schema(description = "配置值", requiredMode = Schema.RequiredMode.REQUIRED, example = "<p>福利群内容</p>")
    @NotBlank(message = "配置值不能为空")
    private String value;

    @Schema(description = "配置名称", example = "福利群内容")
    private String name;

    @Schema(description = "配置类型：text-普通文本, rich_text-富文本, json-JSON", example = "rich_text")
    private String type;

    @Schema(description = "配置描述", example = "首页福利群浮窗点击后显示的富文本内容")
    private String remark;

}
