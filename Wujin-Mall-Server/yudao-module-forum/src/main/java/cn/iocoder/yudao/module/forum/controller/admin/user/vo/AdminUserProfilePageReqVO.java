package cn.iocoder.yudao.module.forum.controller.admin.user.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 论坛用户资料分页请求 VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminUserProfilePageReqVO extends PageParam {

    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @Schema(description = "论坛UID", example = "U123456")
    private String uid;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "是否为管理员")
    private Boolean isAdmin;

}
