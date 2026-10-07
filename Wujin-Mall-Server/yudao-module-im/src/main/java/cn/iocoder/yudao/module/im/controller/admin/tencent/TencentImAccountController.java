package cn.iocoder.yudao.module.im.controller.admin.tencent;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.im.controller.admin.tencent.vo.TencentImAccountDeleteReqVO;
import cn.iocoder.yudao.module.im.controller.admin.tencent.vo.TencentImAccountDeleteRespVO;
import cn.iocoder.yudao.module.im.controller.admin.tencent.vo.TencentImAccountImportReqVO;
import cn.iocoder.yudao.module.im.controller.admin.tencent.vo.TencentImAccountImportRespVO;
import cn.iocoder.yudao.module.im.controller.admin.tencent.vo.TencentImProfileSetReqVO;
import cn.iocoder.yudao.module.im.controller.admin.tencent.vo.TencentImUserSigRespVO;
import cn.iocoder.yudao.module.im.service.TencentImAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 腾讯 IM 账号")
@RestController
@RequestMapping("/im/tencent/account")
@Validated
public class TencentImAccountController {

    @Resource
    private TencentImAccountService tencentImAccountService;

    @PostMapping("/import")
    @Operation(summary = "导入腾讯 IM 账号（支持导入指定用户或全部用户）")
    @PreAuthorize("@ss.hasPermission('im:tencent:account:import')")
    public CommonResult<TencentImAccountImportRespVO> importAccounts(
            @Valid @RequestBody TencentImAccountImportReqVO reqVO) {
        return success(tencentImAccountService.importAccounts(reqVO));
    }

    @PostMapping("/delete")
    @Operation(summary = "删除腾讯 IM 账号（支持批量）")
    @PreAuthorize("@ss.hasPermission('im:tencent:account:delete')")
    public CommonResult<TencentImAccountDeleteRespVO> deleteAccounts(
            @Valid @RequestBody TencentImAccountDeleteReqVO reqVO) {
        return success(tencentImAccountService.deleteAccounts(reqVO));
    }

    @PostMapping("/profile-set")
    @Operation(summary = "设置腾讯 IM 用户资料")
    @PreAuthorize("@ss.hasPermission('im:tencent:account:profile-set')")
    public CommonResult<Boolean> setProfile(@Valid @RequestBody TencentImProfileSetReqVO reqVO) {
        tencentImAccountService.setProfile(reqVO);
        return success(true);
    }

    @GetMapping("/user-sig")
    @Operation(summary = "获取当前用户 IM UserSig")
    public CommonResult<TencentImUserSigRespVO> getCurrentUserSig() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(tencentImAccountService.getCurrentUserSig(userId));
    }

}
