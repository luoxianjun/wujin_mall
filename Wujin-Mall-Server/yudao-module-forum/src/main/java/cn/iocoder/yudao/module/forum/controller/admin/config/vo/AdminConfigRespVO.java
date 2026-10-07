package cn.iocoder.yudao.module.forum.controller.admin.config.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台 - 配置响应 VO
 *
 * @author forum
 */
@Schema(description = "管理后台 - 配置响应 VO")
@Data
public class AdminConfigRespVO {

    @Schema(description = "配置ID", example = "1")
    private Long id;

    @Schema(description = "配置键", example = "forum.welfare.content")
    private String configKey;

    @Schema(description = "配置值", example = "<p>福利群内容</p>")
    private String configValue;

    @Schema(description = "配置名称", example = "福利群内容")
    private String name;

    @Schema(description = "配置类型：text-普通文本, rich_text-富文本, json-JSON", example = "rich_text")
    private String type;

    @Schema(description = "配置描述", example = "首页福利群浮窗点击后显示的富文本内容")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
