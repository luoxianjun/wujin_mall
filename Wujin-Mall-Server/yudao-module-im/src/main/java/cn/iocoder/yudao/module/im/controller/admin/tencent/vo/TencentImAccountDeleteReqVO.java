package cn.iocoder.yudao.module.im.controller.admin.tencent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "管理后台 - 腾讯 IM 账号删除请求 VO")
@Data
public class TencentImAccountDeleteReqVO {

    @Schema(description = "需要删除的会员用户编号列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1,2,3]")
    @NotEmpty(message = "删除的用户编号列表不能为空")
    private List<Long> userIds;

}
