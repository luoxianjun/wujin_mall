package cn.iocoder.yudao.module.gamification.controller.app.invitation;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.module.forum.api.user.ForumUserProfileApi;
import cn.iocoder.yudao.module.forum.api.user.dto.ForumUserProfileDTO;
import cn.iocoder.yudao.module.gamification.controller.app.invitation.vo.InvitationPosterRespVO;
import cn.iocoder.yudao.module.gamification.controller.app.invitation.vo.InviterInfoRespVO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.invitation.InvitationCodeDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.invitation.InvitationCodeMapper;
import cn.iocoder.yudao.module.gamification.service.invitation.InvitationCodeService;
import cn.iocoder.yudao.module.gamification.service.invitation.InvitationPosterService;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.system.api.social.SocialClientApi;
import cn.iocoder.yudao.module.system.api.social.dto.SocialWxQrcodeReqDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletResponse;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 用户 APP - 邀请海报 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "用户 APP - 邀请海报")
@RestController
@RequestMapping("/gamification/invitation/poster")
@Validated
@Slf4j
public class InvitationPosterController {

    @Resource
    private InvitationPosterService invitationPosterService;

    @Resource
    private InvitationCodeMapper invitationCodeMapper;

    @Resource
    private MemberUserApi memberUserApi;

    @Resource
    private ForumUserProfileApi forumUserProfileApi;

    @Resource
    private InvitationCodeService invitationCodeService;

    @Resource
    private SocialClientApi socialClientApi;

    @GetMapping("/generate")
    @Operation(summary = "生成邀请海报")
    @PreAuthenticated
    public CommonResult<InvitationPosterRespVO> generatePoster() {
        Long userId = getLoginUserId();
        log.info("[generatePoster] 用户请求生成邀请海报，userId: {}", userId);

        // 生成海报
        String posterUrl = invitationPosterService.generatePoster(userId);

        // 获取用户信息，优先使用论坛资料，缺失时回退到会员资料
        MemberUserRespDTO memberUser = memberUserApi.getUser(userId);
        ForumUserProfileDTO forumProfile = forumUserProfileApi.getUserProfileByUserId(userId);

        // 获取邀请码
        InvitationCodeDO invitationCode = invitationCodeMapper.selectByUserId(userId);

        // 构建响应
        InvitationPosterRespVO respVO = new InvitationPosterRespVO();
        respVO.setPosterUrl(posterUrl);
        respVO.setInvitationCode(invitationCode != null ? invitationCode.getCode() : null);
        respVO.setNickname(resolveDisplayValue(
                forumProfile != null ? forumProfile.getNickname() : null,
                memberUser != null ? memberUser.getNickname() : null));
        respVO.setAvatar(resolveDisplayValue(
                forumProfile != null ? forumProfile.getAvatar() : null,
                memberUser != null ? memberUser.getAvatar() : null));
        respVO.setCacheHours(24);

        return success(respVO);
    }

    @GetMapping("/clear-cache")
    @Operation(summary = "清除海报缓存")
    @PreAuthenticated
    public CommonResult<Boolean> clearCache() {
        Long userId = getLoginUserId();
        log.info("[clearCache] 用户请求清除海报缓存，userId: {}", userId);

        invitationPosterService.clearPosterCache(userId);

        return success(true);
    }

    /**
     * 测试接口：直接生成微信小程序码并返回图片
     * 绕过海报生成和缓存，直接验证微信API是否正常工作
     * 用浏览器访问此接口可以直接看到小程序码图片，用微信扫码验证是否能打开小程序
     */
    @GetMapping("/test-wxacode")
    @Operation(summary = "测试生成微信小程序码（调试用）")
    @PermitAll
    public void testWxacode(
            @RequestParam(value = "code", defaultValue = "TEST1234") String invitationCode,
            HttpServletResponse response) {
        log.info("[testWxacode] 测试生成小程序码, code: {}", invitationCode);

        SocialWxQrcodeReqDTO reqDTO = new SocialWxQrcodeReqDTO();
        reqDTO.setScene("code=" + invitationCode);
        reqDTO.setPath("pages/invitation/register");
        reqDTO.setWidth(430);
        reqDTO.setCheckPath(false);
        reqDTO.setHyaline(false);

        try {
            byte[] qrCodeBytes = socialClientApi.getWxaQrcode(reqDTO);
            if (qrCodeBytes == null || qrCodeBytes.length == 0) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"微信API返回为空\"}");
                return;
            }

            // 检查是否返回了 JSON 错误
            if (qrCodeBytes.length < 1000) {
                String possibleError = new String(qrCodeBytes, "UTF-8");
                if (possibleError.contains("errcode") || possibleError.contains("errmsg")) {
                    log.error("[testWxacode] 微信API返回错误: {}", possibleError);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(possibleError);
                    return;
                }
            }

            log.info("[testWxacode] 小程序码生成成功, 字节数: {}", qrCodeBytes.length);
            response.setContentType("image/png");
            response.setHeader("Content-Disposition", "inline; filename=wxacode.png");
            response.getOutputStream().write(qrCodeBytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("[testWxacode] 生成失败", e);
            try {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                String errorMsg = e.getMessage() != null ? e.getMessage() : "未知错误";
                response.getWriter().write("{\"error\":\"" + errorMsg.replace("\"", "'") + "\"}");
            } catch (Exception ignored) {}
        }
    }

    /**
     * 根据邀请码获取邀请人信息（免登录，用于接受邀请页面展示邀请人头像）
     */
    @GetMapping("/inviter-info")
    @Operation(summary = "根据邀请码获取邀请人信息")
    @PermitAll
    public CommonResult<InviterInfoRespVO> getInviterInfo(@RequestParam("code") String code) {
        InvitationCodeDO invitationCode = invitationCodeService.getInvitationCodeByCode(code);
        InviterInfoRespVO respVO = new InviterInfoRespVO();
        if (invitationCode == null) {
            return success(respVO);
        }
        Long inviterUserId = invitationCode.getUserId();
        // 优先论坛资料，回退会员资料
        ForumUserProfileDTO forumProfile = forumUserProfileApi.getUserProfileByUserId(inviterUserId);
        MemberUserRespDTO memberUser = memberUserApi.getUser(inviterUserId);
        respVO.setNickname(resolveDisplayValue(
                forumProfile != null ? forumProfile.getNickname() : null,
                memberUser != null ? memberUser.getNickname() : null));
        respVO.setAvatar(resolveDisplayValue(
                forumProfile != null ? forumProfile.getAvatar() : null,
                memberUser != null ? memberUser.getAvatar() : null));
        return success(respVO);
    }

    private String resolveDisplayValue(String forumValue, String memberValue) {
        return StrUtil.blankToDefault(StrUtil.trim(forumValue), StrUtil.trim(memberValue));
    }
}

