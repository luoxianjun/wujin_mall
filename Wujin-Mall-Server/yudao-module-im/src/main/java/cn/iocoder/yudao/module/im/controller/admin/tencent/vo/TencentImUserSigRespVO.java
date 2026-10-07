package cn.iocoder.yudao.module.im.controller.admin.tencent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 当前用户 IM UserSig 响应 VO
 *
 * @author codex
 */
@Schema(description = "管理后台 - 腾讯 IM UserSig 响应 VO")
@Data
public class TencentImUserSigRespVO {

    @Schema(description = "用户 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long userId;

    @Schema(description = "腾讯 IM 应用的 SDKAppID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1600114313")
    private Long sdkAppId;

    @Schema(description = "IM 登录鉴权 UserSig", requiredMode = Schema.RequiredMode.REQUIRED, example = "eJwtzE0PgjAUhuH...")
    private String userSig;

    @Schema(description = "UserSig 过期时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-12-01 12:00:00")
    private LocalDateTime expireTime;

}
