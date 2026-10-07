package cn.iocoder.yudao.module.im.controller.admin.tencent.vo;

import cn.hutool.core.collection.CollUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.AssertTrue;
import java.util.List;

@Schema(description = "管理后台 - 腾讯 IM 账号导入请求 VO")
@Data
public class TencentImAccountImportReqVO {

    @Schema(description = "是否导入全部会员用户", example = "false")
    private Boolean importAll = Boolean.FALSE;

    @Schema(description = "指定导入的会员用户编号列表", example = "[1,2,3]")
    private List<Long> userIds;

    @AssertTrue(message = "导入用户不能为空")
    public boolean isValidTarget() {
        return Boolean.TRUE.equals(importAll) || CollUtil.isNotEmpty(userIds);
    }

}
