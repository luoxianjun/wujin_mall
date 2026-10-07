package cn.iocoder.yudao.module.forum.controller.app.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 学校认证请求 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 学校认证请求 VO")
@Data
public class AppSchoolVerificationReqVO {

    @Schema(description = "学校邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "student@um.edu.mo")
    @NotBlank(message = "学校邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String schoolEmail;

    @Schema(description = "真实姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "真实姓名不能为空")
    @Length(max = 64, message = "真实姓名长度不能超过 64 个字符")
    private String realName;

    @Schema(description = "性别：1 男，2 女", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "性别不能为空")
    private Integer gender;

    @Schema(description = "专业", requiredMode = Schema.RequiredMode.REQUIRED, example = "计算机科学与技术")
    @NotBlank(message = "专业不能为空")
    @Length(max = 64, message = "专业长度不能超过 64 个字符")
    private String major;

    @Schema(description = "入学年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2021")
    @NotBlank(message = "入学时间不能为空")
    @Length(max = 10, message = "入学年份长度不能超过 10 个字符")
    private String enrollYear;

    @Schema(description = "学历：undergraduate-本科, master-硕士, doctor-博士", requiredMode = Schema.RequiredMode.REQUIRED, example = "undergraduate")
    @NotBlank(message = "学历不能为空")
    @Length(max = 20, message = "学历长度不能超过 20 个字符")
    private String degree;

}

