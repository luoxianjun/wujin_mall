package cn.iocoder.yudao.module.member.controller.admin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 会员用户 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MemberUserRespVO extends MemberUserBaseVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23788")
    private Long id;

    @Schema(description = "注册 IP", requiredMode = Schema.RequiredMode.REQUIRED, example = "127.0.0.1")
    private String registerIp;

    @Schema(description = "最后登录IP", requiredMode = Schema.RequiredMode.REQUIRED, example = "127.0.0.1")
    private String loginIp;

    @Schema(description = "最后登录时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime loginDate;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    // ========== 其它信息 ==========

    @Schema(description = "积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer point;

    @Schema(description = "总积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "2000")
    private Integer totalPoint;

    @Schema(description = "会员标签", example = "[红色, 快乐]")
    private List<String> tagNames;

    @Schema(description = "会员等级", example = "黄金会员")
    private String levelName;

    @Schema(description = "用户分组", example = "购物达人")
    private String groupName;

    @Schema(description = "用户经验值", requiredMode = Schema.RequiredMode.REQUIRED, example = "200")
    private Integer experience;

    @Schema(description = "是否论坛管理员", example = "false")
    private Boolean isAdmin;

    @Schema(description = "论坛UID", example = "U123456")
    private String uid;

    @Schema(description = "真实姓名（来自论坛资料）", example = "张三")
    private String realName;

    @Schema(description = "论坛积分", example = "100")
    private Integer forumPoint;

    // ========== 学校认证信息（来自论坛资料）==========

    @Schema(description = "学校名称", example = "清华大学")
    private String schoolName;

    @Schema(description = "学校邮箱", example = "zhangsan@tsinghua.edu.cn")
    private String schoolEmail;

    @Schema(description = "专业", example = "计算机科学与技术")
    private String major;

    @Schema(description = "入学年份", example = "2021")
    private String enrollYear;

    @Schema(description = "学历：undergraduate-本科, master-硕士, doctor-博士", example = "undergraduate")
    private String degree;

    @Schema(description = "是否公开学校信息", example = "true")
    private Boolean schoolInfoPublic;

    @Schema(description = "是否完成学校邮箱认证", example = "true")
    private Boolean schoolEmailVerified;

    // ========== 扩展信息（来自论坛资料）==========

    @Schema(description = "星座", example = "狮子座")
    private String constellation;

    @Schema(description = "MBTI 性格类型", example = "INTJ")
    private String mbti;

    @Schema(description = "个人介绍", example = "热爱编程的大学生")
    private String introduction;

    // ========== 隐私设置（来自论坛资料）==========

    @Schema(description = "是否允许私聊", example = "true")
    private Boolean allowPrivateChat;

    @Schema(description = "是否接收系统消息", example = "true")
    private Boolean allowSystemMessage;

    @Schema(description = "是否隐藏认证学校信息", example = "false")
    private Boolean hideSchoolInfo;

}
