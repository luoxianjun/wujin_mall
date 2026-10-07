package cn.iocoder.yudao.module.forum.controller.app.post.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论响应 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 评论响应 VO")
@Data
public class AppCommentRespVO {

    @Schema(description = "评论ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "帖子ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long postId;

    @Schema(description = "帖子标题", example = "这是一个帖子标题")
    private String postTitle;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "用户UID", example = "U123456")
    private String uid;

    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @Schema(description = "用户头像", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "父评论ID", example = "1")
    private Long parentId;

    @Schema(description = "被回复评论的用户昵称（回复评论时返回）", example = "李四")
    private String replyNickname;

    @Schema(description = "根评论ID", example = "1")
    private Long rootId;

    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "说得对")
    private String content;

    @Schema(description = "是否匿名", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean anonymous;

    @Schema(description = "点赞数", example = "5")
    private Integer likeCount;

    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    private LocalDateTime createTime;

    @Schema(description = "当前用户是否点赞", example = "true")
    private Boolean liked;

    @Schema(description = "子评论列表")
    private List<AppCommentRespVO> children;

}
