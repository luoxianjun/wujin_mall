package cn.iocoder.yudao.module.forum.controller.admin.banner.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 管理后台 - Banner 分页请求 VO
 *
 * @author forum
 */
@Schema(description = "管理后台 - Banner 分页请求 VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AdminBannerPageReqVO extends PageParam {

    @Schema(description = "Banner标题", example = "活动")
    private String title;

    @Schema(description = "跳转类型：1=帖子详情 2=活动详情 3=用户主页 4=外部链接", example = "1")
    private Integer targetType;

    @Schema(description = "状态：0=禁用 1=启用", example = "1")
    private Integer status;

}
