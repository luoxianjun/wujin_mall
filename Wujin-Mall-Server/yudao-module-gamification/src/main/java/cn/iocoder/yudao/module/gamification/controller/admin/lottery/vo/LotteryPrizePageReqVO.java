package cn.iocoder.yudao.module.gamification.controller.admin.lottery.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "Admin lottery prize page request")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LotteryPrizePageReqVO extends PageParam {

    @Schema(description = "Prize name")
    private String name;

    @Schema(description = "Prize type")
    private Integer type;
}
