package cn.iocoder.yudao.module.forum.controller.app.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * 学校邮箱验证请求 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 学校邮箱验证请求 VO")
@Data
public class AppSchoolEmailVerifyReqVO {

    @Schema(description = "学校邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "student@um.edu.mo")
    @NotBlank(message = "学校邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "验证码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotBlank(message = "验证码不能为空")
    private String code;

}

