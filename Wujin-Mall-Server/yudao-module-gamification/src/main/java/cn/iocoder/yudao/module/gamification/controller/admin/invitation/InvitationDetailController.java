package cn.iocoder.yudao.module.gamification.controller.admin.invitation;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.gamification.controller.admin.invitation.vo.InvitationDetailPageReqVO;
import cn.iocoder.yudao.module.gamification.controller.admin.invitation.vo.InvitationDetailRespVO;
import cn.iocoder.yudao.module.gamification.service.invitation.InvitationRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 邀请明细")
@RestController
@RequestMapping("/gamification/invitation/detail")
@Validated
public class InvitationDetailController {

    @Resource
    private InvitationRecordService invitationRecordService;

    @GetMapping("/page")
    @Operation(summary = "获取邀请明细分页")
    @PreAuthorize("@ss.hasPermission('gamification:invitation:query')")
    public CommonResult<PageResult<InvitationDetailRespVO>> getInvitationDetailPage(@Valid InvitationDetailPageReqVO pageReqVO) {
        return success(invitationRecordService.getInvitationDetailPage(pageReqVO));
    }

}
