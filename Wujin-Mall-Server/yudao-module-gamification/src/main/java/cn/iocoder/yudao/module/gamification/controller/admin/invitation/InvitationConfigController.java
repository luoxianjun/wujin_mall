package cn.iocoder.yudao.module.gamification.controller.admin.invitation;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.gamification.controller.admin.invitation.vo.InvitationConfigRespVO;
import cn.iocoder.yudao.module.gamification.controller.admin.invitation.vo.InvitationConfigUpdateReqVO;
import cn.iocoder.yudao.module.gamification.service.invitation.InvitationConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 邀请配置")
@RestController
@RequestMapping("/gamification/invitation/config")
@Validated
public class InvitationConfigController {

    @Resource
    private InvitationConfigService invitationConfigService;

    @GetMapping("/get")
    @Operation(summary = "获取邀请配置")
    @PreAuthorize("@ss.hasPermission('gamification:invitation:query')")
    public CommonResult<InvitationConfigRespVO> getInvitationConfig() {
        return success(invitationConfigService.getConfig());
    }

    @PutMapping("/update")
    @Operation(summary = "更新邀请配置")
    @PreAuthorize("@ss.hasPermission('gamification:invitation:update')")
    public CommonResult<Boolean> updateInvitationConfig(@Valid @RequestBody InvitationConfigUpdateReqVO updateReqVO) {
        invitationConfigService.updateConfig(updateReqVO);
        return success(true);
    }

}
