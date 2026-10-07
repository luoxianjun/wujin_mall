package cn.iocoder.yudao.module.forum.controller.admin.activity.vo;

import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台 - 活动报名 Excel 导出 VO
 */
@Schema(description = "管理后台 - 活动报名 Excel 导出 VO")
@Data
public class AdminActivitySignUpExcelVO {

    @ExcelProperty("报名ID")
    @Schema(description = "报名ID", example = "1")
    private Long id;

    @ExcelProperty("活动标题")
    @Schema(description = "活动标题", example = "学术讲座")
    private String activityTitle;

    @ExcelProperty("用户UID")
    @Schema(description = "用户UID", example = "U123456")
    private String uid;

    @ExcelProperty("昵称")
    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @ExcelProperty("报名备注")
    @Schema(description = "报名备注", example = "我想参加")
    private String remark;

    @ExcelProperty("审核状态")
    @Schema(description = "审核状态：0-待审核，1-已通过，2-已拒绝，3-已取消", example = "1")
    private String approvalStatusName;

    @ExcelProperty("审核备注")
    @Schema(description = "审核备注", example = "通过")
    private String approvalRemark;

    @ExcelProperty("是否签到")
    @Schema(description = "是否已签到", example = "false")
    private String checkedIn;

    @ExcelProperty("签到时间")
    @Schema(description = "签到时间", example = "2024-01-01 12:00:00")
    private LocalDateTime checkInTime;

    @ExcelProperty("报名时间")
    @Schema(description = "报名时间", example = "2024-01-01 10:00:00")
    private LocalDateTime createTime;

}
