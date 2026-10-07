package cn.iocoder.yudao.module.forum.controller.app.post.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建评论请求 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 创建评论请求 VO")
@Data
public class AppCommentCreateReqVO {

    @Schema(description = "帖子ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    @Schema(description = "父评论ID", example = "1")
    private Long parentId;

    @Schema(description = "根评论ID", example = "1")
    private Long rootId;

    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "说得对")
    @NotBlank(message = "评论内容不能为空")
    private String content;

    @Schema(description = "是否匿名", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否匿名不能为空")
    private Boolean anonymous;

}

