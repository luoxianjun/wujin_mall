package cn.iocoder.yudao.module.forum.controller.app.post.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * 评论分页请求 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 评论分页请求 VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppCommentPageReqVO extends PageParam {

    @Schema(description = "帖子ID", example = "1")
    private Long postId;

    @Schema(description = "根评论ID（查询子评论时使用）", example = "1")
    private Long rootId;

    @Schema(description = "评论者用户ID（查询指定用户的评论列表）", example = "1")
    private Long userId;

}

