package cn.iocoder.yudao.module.forum.controller.app.activity.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 活动分页请求 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 活动分页请求 VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppActivityPageReqVO extends PageParam {

    @Schema(description = "活动分类", example = "1")
    private Integer category;

    @Schema(description = "活动状态", example = "1")
    private Integer status;

    @Schema(description = "搜索关键词", example = "马拉松")
    private String keyword;

    @Schema(description = "发布者用户编号", example = "1024")
    private Long userId;

    @Schema(description = "管理员 memberId", example = "1001")
    private Long adminMemberId;

}
