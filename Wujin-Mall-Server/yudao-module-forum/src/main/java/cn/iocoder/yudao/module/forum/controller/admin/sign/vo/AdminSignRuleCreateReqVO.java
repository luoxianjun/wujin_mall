package cn.iocoder.yudao.module.forum.controller.admin.sign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 新增签到规则请求 VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminSignRuleCreateReqVO extends AdminSignRuleBaseVO {
}

