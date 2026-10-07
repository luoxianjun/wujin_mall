package cn.iocoder.yudao.module.forum.controller.admin.point.vo;

import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台 - 积分明细 Excel 导出 VO
 */
@Schema(description = "管理后台 - 积分明细 Excel 导出 VO")
@Data
public class AdminPointRecordExcelVO {

    @ExcelProperty("记录ID")
    @Schema(description = "记录ID", example = "1")
    private Long id;

    @ExcelProperty("用户UID")
    @Schema(description = "用户UID", example = "U123456")
    private String uid;

    @ExcelProperty("昵称")
    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @ExcelProperty("积分标题")
    @Schema(description = "积分标题", example = "发布帖子")
    private String title;

    @ExcelProperty("积分描述")
    @Schema(description = "积分描述", example = "发布帖子获得10积分")
    private String description;

    @ExcelProperty("变动积分")
    @Schema(description = "变动积分", example = "10")
    private Integer point;

    @ExcelProperty("总积分")
    @Schema(description = "变动后积分余额", example = "120")
    private Integer totalPoint;

    @ExcelProperty("获得时间")
    @Schema(description = "获得时间", example = "2024-05-01 08:00:00")
    private LocalDateTime createTime;

}
