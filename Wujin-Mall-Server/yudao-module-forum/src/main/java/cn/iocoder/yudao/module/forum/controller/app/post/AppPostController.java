package cn.iocoder.yudao.module.forum.controller.app.post;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.forum.controller.app.post.vo.AppPostCreateReqVO;
import cn.iocoder.yudao.module.forum.controller.app.post.vo.AppPostPageReqVO;
import cn.iocoder.yudao.module.forum.controller.app.post.vo.AppPostRespVO;
import cn.iocoder.yudao.module.forum.service.post.ForumPostService;
import cn.iocoder.yudao.module.system.api.social.dto.SocialWxQrcodeReqDTO;
import cn.iocoder.yudao.module.system.service.social.SocialClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 论坛帖子 Controller
 *
 * @author forum
 */
@Tag(name = "用户 APP - 论坛帖子")
@RestController
@RequestMapping("/forum/post")
@Validated
@Slf4j
public class AppPostController {

    @Resource
    private ForumPostService postService;

    @Resource
    private SocialClientService socialClientService;

    @PostMapping("/create")
    @Operation(summary = "创建帖子")
    @PreAuthenticated
    public CommonResult<Long> createPost(@Valid @RequestBody AppPostCreateReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(postService.createPost(userId, reqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获取帖子详情")
    @Parameter(name = "id", description = "帖子ID", required = true, example = "1")
    public CommonResult<AppPostRespVO> getPost(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(postService.getPost(id, userId));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询帖子")
    public CommonResult<PageResult<AppPostRespVO>> getPostPage(@Valid AppPostPageReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(postService.getPostPage(reqVO, userId));
    }

    @GetMapping("/hot")
    @Operation(summary = "热点帖子列表")
    public CommonResult<List<AppPostRespVO>> getHotPosts(
            @Parameter(description = "返回数量，默认 10，最大 50", example = "10") @RequestParam(value = "limit", required = false) @Min(1) Integer limit,
            @Parameter(description = "时间窗口（小时），默认 72 小时，最大 720 小时", example = "72") @RequestParam(value = "windowHours", required = false) @Min(1) Integer windowHours) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(postService.getHotPosts(limit, windowHours, userId));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除帖子")
    @Parameter(name = "id", description = "帖子ID", required = true, example = "1")
    @PreAuthenticated
    public CommonResult<Boolean> deletePost(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        postService.deletePost(id, userId);
        return success(true);
    }

    @PostMapping("/like")
    @Operation(summary = "点赞帖子")
    @Parameter(name = "id", description = "帖子ID", required = true, example = "1")
    @PreAuthenticated
    public CommonResult<Boolean> likePost(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        postService.likePost(id, userId);
        return success(true);
    }

    @PostMapping("/unlike")
    @Operation(summary = "取消点赞帖子")
    @Parameter(name = "id", description = "帖子ID", required = true, example = "1")
    @PreAuthenticated
    public CommonResult<Boolean> unlikePost(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        postService.unlikePost(id, userId);
        return success(true);
    }

    @PostMapping("/follow")
    @Operation(summary = "关注帖子（蹲后续）")
    @Parameter(name = "id", description = "帖子ID", required = true, example = "1")
    @PreAuthenticated
    public CommonResult<Boolean> followPost(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        postService.followPost(id, userId);
        return success(true);
    }

    @PostMapping("/unfollow")
    @Operation(summary = "取消关注帖子")
    @Parameter(name = "id", description = "帖子ID", required = true, example = "1")
    @PreAuthenticated
    public CommonResult<Boolean> unfollowPost(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        postService.unfollowPost(id, userId);
        return success(true);
    }

    @GetMapping("/my-followed")
    @Operation(summary = "获取我的收藏帖子列表")
    @PreAuthenticated
    public CommonResult<List<AppPostRespVO>> getMyFollowedPosts() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(postService.getMyFollowedPosts(userId));
    }

    @GetMapping("/liked-page")
    @Operation(summary = "分页获取指定用户点赞的帖子列表")
    @Parameter(name = "userId", description = "目标用户ID", required = true, example = "1")
    @Parameter(name = "pageNo", description = "页码", example = "1")
    @Parameter(name = "pageSize", description = "每页数量", example = "10")
    public CommonResult<PageResult<AppPostRespVO>> getLikedPostsByUserId(
            @RequestParam("userId") Long targetUserId,
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();
        return success(postService.getLikedPostsByUserId(targetUserId, currentUserId, pageNo, pageSize));
    }

    @GetMapping("/qrcode")
    @Operation(summary = "获取帖子小程序码")
    @Parameter(name = "postId", description = "帖子ID", required = true, example = "1")
    @PermitAll
    public void getPostQrCode(@RequestParam("postId") Long postId, HttpServletResponse response) throws IOException {
        // 设置响应编码
        response.setCharacterEncoding("UTF-8");

        // 构建小程序码请求
        SocialWxQrcodeReqDTO reqDTO = new SocialWxQrcodeReqDTO();
        reqDTO.setScene("id=" + postId); // scene 参数，最大32字符
        reqDTO.setPath("pages/posts/detail"); // 帖子详情页路径
        reqDTO.setWidth(280); // 二维码宽度
        reqDTO.setCheckPath(false); // 不检查路径，允许开发版
        reqDTO.setHyaline(false); // 不需要透明背景

        try {
            byte[] qrCodeBytes = socialClientService.getWxaQrcode(reqDTO);
            response.setContentType("image/png");
            response.getOutputStream().write(qrCodeBytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("[getPostQrCode][获取帖子小程序码失败, postId={}]", postId, e);
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            String errorMsg = e.getMessage() != null ? e.getMessage() : "获取小程序码失败";
            response.getWriter().write("{\"code\":500,\"msg\":\"" + errorMsg + "\"}");
        }
    }

}
