package cn.iocoder.yudao.module.forum.controller.app.post;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.forum.controller.app.post.vo.AppCommentCreateReqVO;
import cn.iocoder.yudao.module.forum.controller.app.post.vo.AppCommentPageReqVO;
import cn.iocoder.yudao.module.forum.controller.app.post.vo.AppCommentRespVO;
import cn.iocoder.yudao.module.forum.service.post.ForumCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 论坛评论 Controller
 *
 * @author forum
 */
@Tag(name = "用户 APP - 论坛评论")
@RestController
@RequestMapping("/forum/comment")
@Validated
public class AppCommentController {

    @Resource
    private ForumCommentService commentService;

    @PostMapping("/create")
    @Operation(summary = "创建评论")
    @PreAuthenticated
    public CommonResult<Long> createComment(@Valid @RequestBody AppCommentCreateReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(commentService.createComment(userId, reqVO));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询评论")
    public CommonResult<PageResult<AppCommentRespVO>> getCommentPage(@Valid AppCommentPageReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(commentService.getCommentPage(reqVO, userId));
    }

    @GetMapping("/tree")
    @Operation(summary = "查询帖子的评论树")
    @Parameter(name = "postId", description = "帖子ID", required = true, example = "1")
    public CommonResult<List<AppCommentRespVO>> getCommentTree(@RequestParam("postId") Long postId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(commentService.getCommentTree(postId, userId));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除评论")
    @Parameter(name = "id", description = "评论ID", required = true, example = "1")
    @PreAuthenticated
    public CommonResult<Boolean> deleteComment(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        commentService.deleteComment(id, userId);
        return success(true);
    }

    @PostMapping("/like")
    @Operation(summary = "点赞评论")
    @Parameter(name = "id", description = "评论ID", required = true, example = "1")
    @PreAuthenticated
    public CommonResult<Boolean> likeComment(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        commentService.likeComment(id, userId);
        return success(true);
    }

    @PostMapping("/unlike")
    @Operation(summary = "取消点赞评论")
    @Parameter(name = "id", description = "评论ID", required = true, example = "1")
    @PreAuthenticated
    public CommonResult<Boolean> unlikeComment(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        commentService.unlikeComment(id, userId);
        return success(true);
    }

}

