package cn.iocoder.yudao.module.gamification.controller.admin.invitation;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.gamification.controller.admin.invitation.vo.InvitationStatisticsRespVO;
import cn.iocoder.yudao.module.gamification.service.invitation.InvitationStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 邀请统计")
@RestController
@RequestMapping("/gamification/invitation/statistics")
@Validated
public class InvitationStatisticsController {

    @Resource
    private InvitationStatisticsService invitationStatisticsService;

    @GetMapping("/get")
    @Operation(summary = "获取邀请统计数据")
    @PreAuthorize("@ss.hasPermission('gamification:invitation:query')")
    public CommonResult<InvitationStatisticsRespVO> getInvitationStatistics() {
        return success(invitationStatisticsService.getStatistics());
    }

}
