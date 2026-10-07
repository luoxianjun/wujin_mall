package cn.iocoder.yudao.module.gamification.controller.app.invitation;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.gamification.controller.app.invitation.vo.AppInvitationRecordPageReqVO;
import cn.iocoder.yudao.module.gamification.controller.app.invitation.vo.AppInvitationRecordRespVO;
import cn.iocoder.yudao.module.gamification.service.invitation.InvitationRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 邀请记录 - 小程序 API
 *
 * @author gamification
 */
@Tag(name = "小程序 - 邀请记录")
@RestController
@RequestMapping("/gamification/invitation/record")
@Validated
public class InvitationRecordController {

    @Resource
    private InvitationRecordService invitationRecordService;

    @GetMapping("/page")
    @Operation(summary = "分页获取我的邀请记录")
    @PreAuthenticated
    public CommonResult<PageResult<AppInvitationRecordRespVO>> getMyInvitationRecordPage(
            @Valid AppInvitationRecordPageReqVO pageReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(invitationRecordService.getMyInvitationRecordPage(userId, pageReqVO));
    }

}
