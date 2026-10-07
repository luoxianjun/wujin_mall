package cn.iocoder.yudao.module.forum.controller.app.post.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 创建帖子请求 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 创建帖子请求 VO")
@Data
public class AppPostCreateReqVO {

    @Schema(description = "帖子标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "求助：如何学好Java")
    @NotBlank(message = "帖子标题不能为空")
    private String title;

    @Schema(description = "帖子内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "我是一名大一新生...")
    @NotBlank(message = "帖子内容不能为空")
    private String content;

    @Schema(description = "帖子分类（单选，兼容旧版）", example = "2")
    private Integer category;

    @Schema(description = "帖子分类列表（多选），如 [\"110\", \"120\"]", example = "[\"110\", \"120\"]")
    private List<Integer> categories;

    @Schema(description = "图片URL列表", example = "[\"https://example.com/1.jpg\"]")
    private List<String> imageUrls;

    @Schema(description = "是否匿名", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否匿名不能为空")
    private Boolean anonymous;

    @Schema(description = "是否仅本校可见", example = "false")
    private Boolean schoolOnly;

    @Schema(description = "发帖选择的学校", example = "澳门大学")
    private String school;

    @Schema(description = "是否置顶（仅管理员可用）", example = "false")
    private Boolean top;

    @Schema(description = "投票选项列表（发帖附带投票时使用）")
    private List<VoteOptionItem> voteOptions;

    @Schema(description = "投票类型：0=单选, 1=多选，默认单选", example = "0")
    private Integer voteType;

    @Schema(description = "多选时最多可选数", example = "3")
    private Integer voteMaxChoices;

    @AssertTrue(message = "帖子分类不能为空")
    public boolean isCategoryValid() {
        return category != null || (categories != null && !categories.isEmpty());
    }

    @Schema(description = "投票选项")
    @Data
    public static class VoteOptionItem {
        @Schema(description = "选项标题", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "选项标题不能为空")
        private String title;
    }

}
