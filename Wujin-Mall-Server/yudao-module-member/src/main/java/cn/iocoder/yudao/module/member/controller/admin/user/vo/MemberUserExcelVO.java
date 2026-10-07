package cn.iocoder.yudao.module.member.controller.admin.user.vo;

import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台 - 会员用户 Excel 导出 VO
 */
@Schema(description = "管理后台 - 会员用户 Excel 导出 VO")
@Data
public class MemberUserExcelVO {

    @ExcelProperty("用户编号")
    @Schema(description = "用户编号", example = "23788")
    private Long id;

    @ExcelProperty("会员UID")
    @Schema(description = "论坛UID", example = "U123456")
    private String uid;

    @ExcelProperty("真实姓名")
    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @ExcelProperty("昵称")
    @Schema(description = "用户昵称", example = "小明")
    private String nickname;

    @ExcelProperty("手机号")
    @Schema(description = "手机号", example = "13800138000")
    private String mobile;

    @ExcelProperty("性别")
    @Schema(description = "性别", example = "男")
    private String sex;

    @ExcelProperty("积分")
    @Schema(description = "论坛积分", example = "100")
    private Integer forumPoint;

    @ExcelProperty("会员等级")
    @Schema(description = "会员等级", example = "黄金会员")
    private String levelName;

    @ExcelProperty("状态")
    @Schema(description = "状态", example = "正常")
    private String statusName;

    @ExcelProperty("学校名称")
    @Schema(description = "学校名称", example = "清华大学")
    private String schoolName;

    @ExcelProperty("学校邮箱")
    @Schema(description = "学校邮箱", example = "zhangsan@tsinghua.edu.cn")
    private String schoolEmail;

    @ExcelProperty("邮箱已认证")
    @Schema(description = "是否完成学校邮箱认证", example = "true")
    private Boolean schoolEmailVerified;

    @ExcelProperty("注册时间")
    @Schema(description = "注册时间")
    private LocalDateTime createTime;

    @ExcelProperty("最后登录时间")
    @Schema(description = "最后登录时间")
    private LocalDateTime loginDate;

}
