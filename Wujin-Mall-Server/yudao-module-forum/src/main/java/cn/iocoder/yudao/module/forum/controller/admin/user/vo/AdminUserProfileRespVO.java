package cn.iocoder.yudao.module.forum.controller.admin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 论坛用户资料响应 VO")
@Data
public class AdminUserProfileRespVO {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "论坛唯一UID", requiredMode = Schema.RequiredMode.REQUIRED, example = "U123456")
    private String uid;

    @Schema(description = "昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String nickname;

    @Schema(description = "头像", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "手机号", example = "13800138000")
    private String mobile;

    @Schema(description = "校园", example = "北京大学")
    private String campus;

    @Schema(description = "积分", example = "1000")
    private Integer point;

    @Schema(description = "发帖数量", example = "10")
    private Integer postCount;

    @Schema(description = "活动数量", example = "5")
    private Integer activityCount;

    @Schema(description = "是否为管理员", example = "false")
    private Boolean isAdmin;

    @Schema(description = "学校邮箱验证状态", example = "true")
    private Boolean schoolEmailVerified;

    @Schema(description = "注册时间", example = "2024-01-01 00:00:00")
    private LocalDateTime createTime;

}
