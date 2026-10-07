package cn.iocoder.yudao.module.forum.controller.app.user.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "用户 APP - 论坛用户分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppForumUserPageReqVO extends PageParam {

    @Schema(description = "用户昵称，模糊匹配", example = "芋艿")
    private String nickname;

}
