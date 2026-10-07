package cn.iocoder.yudao.module.gamification.controller.admin.lottery.vo;

import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "Admin lottery record Excel export VO")
@Data
public class LotteryRecordExcelVO {

    @ExcelProperty("记录ID")
    private Long id;

    @ExcelProperty("抽奖活动")
    private String activityName;

    @ExcelProperty("用户")
    private String userNickname;

    @ExcelProperty("UID")
    private String uid;

    @ExcelProperty("奖品名称")
    private String prizeName;

    @ExcelProperty("奖品类型")
    private String prizeType;

    @ExcelProperty("是否中奖")
    private String won;

    @ExcelProperty("抽奖时间")
    private LocalDateTime drawTime;

    @ExcelProperty("发放状态")
    private String delivered;

    @ExcelProperty("收货地址")
    private String deliveryAddress;
}
