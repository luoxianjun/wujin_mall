package cn.iocoder.yudao.module.forum.controller.app.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户 APP - 带 IM 签名的用户资料响应 VO
 */
@Schema(description = "用户 APP - 带 IM 签名的用户资料响应 VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppUserProfileWithImRespVO extends AppUserProfileRespVO {

    @Schema(description = "IM 鉴权 UserSig", example = "eJwtzE0PgjAUhuH...")
    private String imUserSig;

    @Schema(description = "UserSig 过期时间", example = "2024-12-01 12:00:00")
    private LocalDateTime imUserSigExpireTime;

}
