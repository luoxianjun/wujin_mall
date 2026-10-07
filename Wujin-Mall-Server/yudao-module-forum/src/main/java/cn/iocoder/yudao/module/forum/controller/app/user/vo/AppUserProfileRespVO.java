package cn.iocoder.yudao.module.forum.controller.app.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户资料响应 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 用户资料响应 VO")
@Data
public class AppUserProfileRespVO {

    @Schema(description = "用户 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "论坛唯一 UID", requiredMode = Schema.RequiredMode.REQUIRED, example = "U123456")
    private String uid;

    @Schema(description = "论坛昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String nickname;

    @Schema(description = "论坛头像", example = "https://xxx.com/avatar.jpg")
    private String avatar;

    @Schema(description = "腾讯 IM UserSig", example = "eJx...")
    private String userSig;

    // ========== 学校认证信息 ==========

    @Schema(description = "学校名称", example = "澳门大学")
    private String schoolName;

    @Schema(description = "学校邮箱", example = "student@um.edu.mo")
    private String schoolEmail;

    @Schema(description = "是否完成学校邮箱认证", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean schoolEmailVerified;

    @Schema(description = "学校邮箱通过时间", example = "2024-01-01 12:00:00")
    private LocalDateTime schoolEmailVerifyTime;

    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "性别：1 男，2 女", example = "1")
    private Integer gender;

    @Schema(description = "专业", example = "计算机科学与技术")
    private String major;

    @Schema(description = "入学年份", example = "2021")
    private String enrollYear;

    @Schema(description = "学历：undergraduate-本科, master-硕士, doctor-博士", example = "undergraduate")
    private String degree;

    @Schema(description = "是否公开学校信息", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean schoolInfoPublic;

    // ========== 扩展信息 ==========

    @Schema(description = "出生年月", example = "2000-01-01")
    private LocalDate birthday;

    @Schema(description = "星座", example = "摩羯座")
    private String constellation;

    @Schema(description = "MBTI 性格类型", example = "INTJ")
    private String mbti;

    @Schema(description = "个人介绍", example = "热爱编程的学生")
    private String introduction;

    // ========== 积分与统计 ==========

    @Schema(description = "论坛积分余额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer point;

    @Schema(description = "累计获得的论坛积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "500")
    private Integer totalPoint;

    @Schema(description = "连续签到天数", requiredMode = Schema.RequiredMode.REQUIRED, example = "7")
    private Integer continuousSignDays;

    @Schema(description = "累计签到天数", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    private Integer totalSignDays;

    @Schema(description = "最后签到时间", example = "2024-01-01")
    private LocalDate lastSignDate;

    @Schema(description = "发帖数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer postCount;

    @Schema(description = "参与活动数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer activityCount;

    @Schema(description = "获得的赞数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    private Integer likeCount;

    @Schema(description = "获得的收藏数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer favoriteCount;

    @Schema(description = "是否隐藏学校信息", example = "false")
    private Boolean hideSchoolInfo;

}
