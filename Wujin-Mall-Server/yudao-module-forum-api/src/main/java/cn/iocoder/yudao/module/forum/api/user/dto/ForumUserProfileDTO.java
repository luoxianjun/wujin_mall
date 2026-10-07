package cn.iocoder.yudao.module.forum.api.user.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 论坛用户资料 DTO
 * 
 * 用于跨模块数据传输，只包含必要字段
 *
 * @author forum
 */
@Data
public class ForumUserProfileDTO implements Serializable {

    /**
     * 自增主键
     */
    private Long id;

    /**
     * 用户编号，对应 member_user.id
     */
    private Long userId;

    /**
     * 是否为管理员
     */
    private Boolean isAdmin;

    /**
     * 论坛唯一 UID
     */
    private String uid;

    /**
     * 论坛昵称
     */
    private String nickname;

    /**
     * 论坛头像
     */
    private String avatar;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 性别：1 男，2 女
     */
    private Integer gender;

    /**
     * 论坛积分
     */
    private Integer point;

    // ========== 学校认证信息 ==========

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 认证的学校邮箱
     */
    private String schoolEmail;

    /**
     * 专业
     */
    private String major;

    /**
     * 入学年份
     */
    private String enrollYear;

    /**
     * 学历：undergraduate-本科, master-硕士, doctor-博士
     */
    private String degree;

    /**
     * 是否公开学校信息
     */
    private Boolean schoolInfoPublic;

    /**
     * 是否完成学校邮箱认证
     */
    private Boolean schoolEmailVerified;

    // ========== 扩展信息 ==========

    /**
     * 出生年月
     */
    private LocalDate birthday;

    /**
     * 星座
     */
    private String constellation;

    /**
     * MBTI 性格类型
     */
    private String mbti;

    /**
     * 个人介绍
     */
    private String introduction;

    // ========== 隐私设置 ==========

    /**
     * 是否允许私聊
     */
    private Boolean allowPrivateChat;

    /**
     * 是否接收系统消息
     */
    private Boolean allowSystemMessage;

    /**
     * 是否隐藏认证学校信息
     */
    private Boolean hideSchoolInfo;

}
