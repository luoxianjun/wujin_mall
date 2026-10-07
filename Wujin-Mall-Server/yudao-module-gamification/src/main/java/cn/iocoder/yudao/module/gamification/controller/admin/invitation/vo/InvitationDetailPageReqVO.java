package cn.iocoder.yudao.module.gamification.controller.admin.invitation.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "Admin invitation detail page request")
@Data
@EqualsAndHashCode(callSuper = true)
public class InvitationDetailPageReqVO extends PageParam {

    @Schema(description = "Inviter nickname", example = "Alice")
    private String inviterNickname;

    @Schema(description = "Invitee nickname", example = "Bob")
    private String inviteeNickname;

    @Schema(description = "Relation status", example = "2")
    private Integer status;

    @Schema(description = "Begin registration time")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime beginTime;

    @Schema(description = "End registration time")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;
}
