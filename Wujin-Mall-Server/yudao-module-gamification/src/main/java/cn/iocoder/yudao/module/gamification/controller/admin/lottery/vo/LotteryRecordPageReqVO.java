package cn.iocoder.yudao.module.gamification.controller.admin.lottery.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "Admin lottery record page request")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LotteryRecordPageReqVO extends PageParam {

    @Schema(description = "Lottery activity id")
    private Long lotteryActivityId;

    @Schema(description = "User id")
    private Long userId;

    @Schema(description = "Whether won")
    private Boolean won;

    @Schema(description = "Whether delivered")
    private Boolean delivered;

    @Schema(description = "Start time")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] drawTime;
}
