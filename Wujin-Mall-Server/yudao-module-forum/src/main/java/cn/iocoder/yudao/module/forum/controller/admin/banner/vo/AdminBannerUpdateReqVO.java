package cn.iocoder.yudao.module.forum.controller.admin.banner.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 管理后台 - Banner 更新请求 VO
 *
 * @author forum
 */
@Schema(description = "管理后台 - Banner 更新请求 VO")
@Data
public class AdminBannerUpdateReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "主键不能为空")
    private Long id;

    @Schema(description = "Banner标题", example = "新活动上线")
    private String title;

    @Schema(description = "图片地址", example = "https://example.com/banner.jpg")
    private String imageUrl;

    @Schema(description = "跳转类型：1=帖子详情 2=活动详情 3=用户主页 4=外部链接", example = "1")
    private Integer targetType;

    @Schema(description = "跳转目标ID（帖子ID/活动ID/用户ID）", example = "1024")
    private String targetId;

    @Schema(description = "跳转链接（外部链接时使用）", example = "https://www.baidu.com")
    private String targetUrl;

    @Schema(description = "排序值，越大越靠前", example = "0")
    private Integer sort;

    @Schema(description = "状态：0=禁用 1=启用", example = "1")
    private Integer status;

    @Schema(description = "生效时间，格式：yyyy-MM-dd HH:mm", example = "2024-01-01 00:00")
    private String startTime;

    @Schema(description = "失效时间，格式：yyyy-MM-dd HH:mm", example = "2024-12-31 23:59")
    private String endTime;

    @Schema(description = "备注", example = "首页Banner")
    private String remark;

}
