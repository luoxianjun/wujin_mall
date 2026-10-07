package cn.iocoder.yudao.module.forum.controller.app.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Pattern;
import java.time.LocalDate;

/**
 * 用户资料更新请求 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 用户资料更新请求 VO")
@Data
public class AppUserProfileUpdateReqVO {

    @Schema(description = "论坛昵称", example = "张三")
    @Length(max = 64, message = "昵称长度不能超过 64 个字符")
    private String nickname;

    @Schema(description = "论坛头像", example = "https://xxx.com/avatar.jpg")
    @Length(max = 512, message = "头像 URL 长度不能超过 512 个字符")
    private String avatar;

    @Schema(description = "出生年月", example = "2000-01-01")
    private LocalDate birthday;

    @Schema(description = "MBTI 性格类型", example = "INTJ")
    @Pattern(regexp = "^$|^[A-Z]{4}$", message = "MBTI 格式不正确，应为 4 个大写字母")
    private String mbti;

    @Schema(description = "星座", example = "摩羯座")
    private String constellation;

    @Schema(description = "个人介绍", example = "热爱编程的学生")
    @Length(max = 500, message = "个人介绍长度不能超过 500 个字符")
    private String introduction;

}
