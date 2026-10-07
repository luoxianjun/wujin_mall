package cn.iocoder.yudao.module.forum.controller.admin.sign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 签到记录响应 VO")
@Data
public class AdminSignRecordRespVO {

    @Schema(description = "记录ID", example = "1")
    private Long id;

    @Schema(description = "用户ID", example = "1024")
    private Long userId;

    @Schema(description = "用户昵称", example = "小明")
    private String nickname;

    @Schema(description = "用户UID", example = "U123456")
    private String uid;

    @Schema(description = "签到日期", example = "2024-05-01")
    private LocalDate signDate;

    @Schema(description = "连续签到天数", example = "5")
    private Integer continuousDays;

    @Schema(description = "获得积分", example = "10")
    private Integer point;

    @Schema(description = "备注", example = "week:3;")
    private String remark;

    @Schema(description = "创建时间", example = "2024-05-01 08:00:00")
    private LocalDateTime createTime;
}
