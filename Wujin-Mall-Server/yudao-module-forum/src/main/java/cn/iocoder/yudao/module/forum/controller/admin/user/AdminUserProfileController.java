package cn.iocoder.yudao.module.forum.controller.admin.user;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.forum.controller.admin.user.vo.AdminSetUserAdminReqVO;
import cn.iocoder.yudao.module.forum.controller.admin.user.vo.AdminUserProfilePageReqVO;
import cn.iocoder.yudao.module.forum.controller.admin.user.vo.AdminUserProfileRespVO;
import cn.iocoder.yudao.module.forum.service.user.ForumUserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 论坛用户管理")
@RestController
@RequestMapping("/forum/admin/user-profile")
@Validated
public class AdminUserProfileController {

    @Resource
    private ForumUserProfileService userProfileService;

    @GetMapping("/page")
    @Operation(summary = "获取论坛用户分页列表")
    public CommonResult<PageResult<AdminUserProfileRespVO>> getUserProfilePage(@Valid AdminUserProfilePageReqVO reqVO) {
        return success(userProfileService.getAdminUserProfilePage(reqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "根据用户ID获取用户资料")
    @PreAuthorize("@ss.hasPermission('forum:user-profile:query')")
    public CommonResult<AdminUserProfileRespVO> getUserProfile(@RequestParam("userId") Long userId) {
        return success(userProfileService.getAdminUserProfileByUserId(userId));
    }

    @PutMapping("/set-admin")
    @Operation(summary = "设置用户管理员状态")
    @PreAuthorize("@ss.hasPermission('forum:user-profile:update')")
    public CommonResult<Boolean> setUserAdmin(@Valid @RequestBody AdminSetUserAdminReqVO reqVO) {
        userProfileService.setUserAdmin(reqVO.getUserId(), reqVO.getIsAdmin());
        return success(true);
    }

}
