package cn.iocoder.yudao.module.gamification.controller.app.invitation;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.gamification.controller.app.invitation.vo.InvitationCodeRespVO;
import cn.iocoder.yudao.module.gamification.controller.app.invitation.vo.InvitationRegisterReqVO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.invitation.InvitationCodeDO;
import cn.iocoder.yudao.module.gamification.service.invitation.InvitationCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 邀请码 - 小程序 API
 *
 * @author gamification
 */
@Tag(name = "小程序 - 邀请码")
@RestController
@RequestMapping("/gamification/invitation")
@Validated
public class InvitationCodeController {

    @Resource
    private InvitationCodeService invitationCodeService;

    @GetMapping("/my-code")
    @Operation(summary = "获取我的邀请码")
    @PreAuthenticated
    public CommonResult<InvitationCodeRespVO> getMyInvitationCode() {
        // 获取当前登录用户ID
        Long userId = SecurityFrameworkUtils.getLoginUserId();

        // 获取或创建邀请码
        String code = invitationCodeService.getOrCreateInvitationCode(userId);

        // 查询邀请码详情
        InvitationCodeDO invitationCode = invitationCodeService.getInvitationCodeByCode(code);

        // 构建响应
        InvitationCodeRespVO respVO = new InvitationCodeRespVO();
        respVO.setCode(code);
        respVO.setTotalInvitations(invitationCode.getTotalInvitations());
        respVO.setValidInvitations(invitationCode.getValidInvitations());
        respVO.setInviteUrl("https://example.com/invite?code=" + code); // TODO: 替换为实际域名
        respVO.setQrCodeUrl("https://example.com/qrcode/" + code + ".png"); // TODO: 实现二维码生成

        return success(respVO);
    }

    @PostMapping("/register")
    @Operation(summary = "使用邀请码注册")
    @PreAuthenticated
    public CommonResult<Boolean> registerWithInvitationCode(
            @Valid @RequestBody InvitationRegisterReqVO reqVO,
            HttpServletRequest request) {
        // 获取当前登录用户ID
        Long userId = SecurityFrameworkUtils.getLoginUserId();

        // 获取IP地址
        String ip = getClientIp(request);

        // 使用邀请码注册
        boolean success = invitationCodeService.registerWithInvitationCode(
                userId,
                reqVO.getInvitationCode(),
                reqVO.getDeviceInfo(),
                ip
        );

        return success(success);
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @GetMapping("/statistics/my-stats")
    @Operation(summary = "获取我的邀请统计")
    @PreAuthenticated
    public CommonResult<java.util.Map<String, Object>> getMyInvitationStats() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();

        // 获取或创建邀请码
        String code = invitationCodeService.getOrCreateInvitationCode(userId);
        InvitationCodeDO invitationCode = invitationCodeService.getInvitationCodeByCode(code);

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalInvitations", invitationCode != null ? invitationCode.getTotalInvitations() : 0);
        stats.put("totalPoints", invitationCode != null ? invitationCode.getValidInvitations() * 20 : 0); // 按默认积分估算
        return success(stats);
    }
}

