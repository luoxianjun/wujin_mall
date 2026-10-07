package cn.iocoder.yudao.module.forum.controller.admin.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 论坛统计趋势响应 VO")
@Data
public class AdminForumStatisticsTrendRespVO {

    @Schema(description = "日期（yyyy-MM-dd），作为 Echarts x 轴", example = "[\"2024-05-01\",\"2024-05-02\"]")
    private List<String> dates;

    @Schema(description = "对应日期的会员新增数量", example = "[5,8]")
    private List<Long> memberIncrements;

    @Schema(description = "对应日期的帖子新增数量", example = "[10,12]")
    private List<Long> postIncrements;

}
