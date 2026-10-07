package cn.iocoder.yudao.module.forum.controller.app.post.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 帖子分页请求 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 帖子分页请求 VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppPostPageReqVO extends PageParam {

    @Schema(description = "帖子分类", example = "1")
    private Integer category;

    @Schema(description = "帖子状态", example = "1")
    private Integer status;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "搜索关键词", example = "Java")
    private String keyword;

    @Schema(description = "排序方式：1-最新，2-热度", example = "1")
    private Integer orderBy;

    @Schema(description = "筛选学校", example = "澳门大学")
    private String school;

    @Schema(description = "是否匿名", example = "true")
    private Boolean anonymous;

    @Schema(description = "时间范围(天数)", example = "7")
    private Integer days;

}
