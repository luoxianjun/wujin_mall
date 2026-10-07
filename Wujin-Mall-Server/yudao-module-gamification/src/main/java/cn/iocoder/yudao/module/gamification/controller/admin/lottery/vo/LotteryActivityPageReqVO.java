package cn.iocoder.yudao.module.gamification.controller.admin.lottery.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "Admin lottery activity page request")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LotteryActivityPageReqVO extends PageParam {

    @Schema(description = "Lottery type: 0=scheduled, 1=instant")
    private Integer type;

    @Schema(description = "Status: 0=enabled, 1=disabled")
    private Integer status;
}
