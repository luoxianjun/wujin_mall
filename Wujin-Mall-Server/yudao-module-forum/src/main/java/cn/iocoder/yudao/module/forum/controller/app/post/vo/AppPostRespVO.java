package cn.iocoder.yudao.module.forum.controller.app.post.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子响应 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 帖子响应 VO")
@Data
public class AppPostRespVO {

    @Schema(description = "帖子ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "用户UID", example = "U123456")
    private String uid;

    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @Schema(description = "用户头像", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "帖子标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "求助：如何学好Java")
    private String title;

    @Schema(description = "帖子内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "我是一名大一新生...")
    private String content;

    @Schema(description = "帖子分类", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer category;

    @Schema(description = "帖子分类名称", example = "求助")
    private String categoryName;

    @Schema(description = "帖子分类名称列表", example = "[\"求助\"]")
    private List<String> categoryNames;

    @Schema(description = "帖子分类列表", example = "[110, 120]")
    private List<Integer> categories;

    @Schema(description = "图片URL列表", example = "[\"https://example.com/1.jpg\"]")
    private List<String> imageUrls;

    @Schema(description = "是否匿名", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean anonymous;

    @Schema(description = "是否仅本校可见", example = "false")
    private Boolean schoolOnly;

    @Schema(description = "学校", example = "澳门大学")
    private String school;

    @Schema(description = "审核状态：0-待审核，1-已通过，2-已驳回", example = "1")
    private Integer status;

    @Schema(description = "审核结论（审核不通过时的拒绝原因）", example = "内容不符合社区规范")
    private String reviewResult;

    @Schema(description = "是否置顶", example = "false")
    private Boolean isTop;

    @Schema(description = "点赞数", example = "10")
    private Integer likeCount;

    @Schema(description = "评论数", example = "5")
    private Integer commentCount;

    @Schema(description = "蹲后续人数", example = "3")
    private Integer followCount;

    @Schema(description = "浏览次数", example = "100")
    private Integer viewCount;

    @Schema(description = "最后评论时间", example = "2024-01-01 12:00:00")
    private LocalDateTime latestCommentTime;

    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    private LocalDateTime createTime;

    @Schema(description = "当前用户是否点赞", example = "true")
    private Boolean liked;

    @Schema(description = "当前用户是否关注", example = "false")
    private Boolean followed;

    @Schema(description = "是否管理员帖子（如果发帖人是管理员，则为true）", example = "false")
    private Boolean isAdminPost;

    @Schema(description = "帖子作者ID（用于判断楼主标签，即使匿名帖子也会返回）", example = "1")
    private Long authorUserId;

    @Schema(description = "是否包含内嵌投票", example = "false")
    private Boolean hasVote;

}
