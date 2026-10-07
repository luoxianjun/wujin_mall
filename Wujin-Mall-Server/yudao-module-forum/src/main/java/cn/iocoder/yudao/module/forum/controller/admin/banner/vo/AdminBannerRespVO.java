package cn.iocoder.yudao.module.forum.controller.admin.banner.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台 - Banner 响应 VO
 *
 * @author forum
 */
@Schema(description = "管理后台 - Banner 响应 VO")
@Data
public class AdminBannerRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "Banner标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "新活动上线")
    private String title;

    @Schema(description = "图片地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://example.com/banner.jpg")
    private String imageUrl;

    @Schema(description = "跳转类型：1=帖子详情 2=活动详情 3=用户主页 4=外部链接", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer targetType;

    @Schema(description = "跳转目标ID（帖子ID/活动ID/用户ID）", example = "1024")
    private String targetId;

    @Schema(description = "跳转链接（外部链接时使用）", example = "https://www.baidu.com")
    private String targetUrl;

    @Schema(description = "排序值，越大越靠前", example = "0")
    private Integer sort;

    @Schema(description = "状态：0=禁用 1=启用", example = "1")
    private Integer status;

    @Schema(description = "生效时间", example = "2024-01-01 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startTime;

    @Schema(description = "失效时间", example = "2024-12-31 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endTime;

    @Schema(description = "备注", example = "首页Banner")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createTime;

}
