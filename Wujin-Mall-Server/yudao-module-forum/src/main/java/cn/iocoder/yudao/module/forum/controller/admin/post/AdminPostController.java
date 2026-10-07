package cn.iocoder.yudao.module.forum.controller.admin.post;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.forum.controller.app.post.vo.AppPostPageReqVO;
import cn.iocoder.yudao.module.forum.controller.app.post.vo.AppPostRespVO;
import cn.iocoder.yudao.module.forum.controller.admin.post.vo.AdminPostReviewReqVO;
import cn.iocoder.yudao.module.forum.service.post.ForumPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 论坛帖子 Controller
 *
 * 帖子分页查询、详情、删除、复审
 */
@Tag(name = "管理后台 - 论坛帖子")
@RestController
@RequestMapping("/forum/post")
@Validated
public class AdminPostController {

    @Resource
    private ForumPostService postService;

    @GetMapping("/page")
    @Operation(summary = "分页查询帖子")
    @PreAuthorize("@ss.hasPermission('forum:post:query')")
    public CommonResult<PageResult<AppPostRespVO>> getPostPage(@Valid AppPostPageReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(postService.getPostPage(reqVO, userId));
    }

    @GetMapping("/get")
    @Operation(summary = "获取帖子详情")
    @Parameter(name = "id", description = "帖子ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('forum:post:query')")
    public CommonResult<AppPostRespVO> getPost(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(postService.getPost(id, userId));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除帖子")
    @Parameter(name = "id", description = "帖子ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('forum:post:delete')")
    public CommonResult<Boolean> deletePost(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        postService.deletePostByAdmin(id, userId);
        return success(true);
    }

    @PostMapping("/review")
    @Operation(summary = "帖子复审（通过/不通过）")
    @PreAuthorize("@ss.hasPermission('forum:post:review')")
    public CommonResult<Boolean> reviewPost(@Valid @RequestBody AdminPostReviewReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        postService.reviewPost(reqVO.getId(), reqVO.getApprove(), reqVO.getReviewRemark(), userId);
        return success(true);
    }

    @PostMapping("/set-top")
    @Operation(summary = "设置帖子置顶状态")
    @PreAuthorize("@ss.hasPermission('forum:post:update')")
    public CommonResult<Boolean> setPostTop(@RequestParam("id") Long id, @RequestParam("isTop") Boolean isTop) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        postService.setPostTop(id, isTop, userId);
        return success(true);
    }
}
