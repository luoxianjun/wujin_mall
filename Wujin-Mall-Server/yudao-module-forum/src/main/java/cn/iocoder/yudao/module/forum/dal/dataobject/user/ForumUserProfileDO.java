package cn.iocoder.yudao.module.forum.dal.dataobject.user;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 论坛用户扩展信息 DO
 *
 * 一条记录对应一个 member_user 用户。
 */
@TableName("forum_user_profile")
@KeySequence("forum_user_profile_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForumUserProfileDO extends BaseDO {

    /**
     * 自增主键
     */
    @TableId
    private Long id;

    /**
     * 用户编号，对应 member_user.id
     */
    private Long userId;

    /**
     * 论坛唯一 UID（用户可见的唯一标识）
     */
    private String uid;

    /**
     * 论坛昵称，默认取 MemberUserDO.nickname，可单独设置
     */
    private String nickname;

    /**
     * 论坛头像，默认取 MemberUserDO.avatar，可单独设置
     */
    private String avatar;

    // ========== IM 信息 ==========

    /**
     * 腾讯 IM 登录鉴权 UserSig
     */
    private String imUserSig;

    /**
     * UserSig 过期时间
     */
    private LocalDateTime imUserSigExpireTime;

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
     * 学校邮箱前缀（@之前的部分）
     */
    private String schoolEmailPrefix;

    /**
     * 是否完成学校邮箱认证
     */
    private Boolean schoolEmailVerified;

    /**
     * 学校邮箱通过时间
     */
    private LocalDateTime schoolEmailVerifyTime;

    /**
     * 真实姓名（认证后填写）
     */
    private String realName;

    /**
     * 性别：1 男，2 女
     */
    private Integer gender;

    /**
     * 专业
     */
    private String major;

    /**
     * 入学年份（如 2021）
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

    // ========== 扩展信息 ==========

    /**
     * 出生年月
     */
    private LocalDate birthday;

    /**
     * 星座（根据生日自动计算）
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

    // ========== 积分与统计 ==========

    /**
     * 论坛积分余额
     */
    private Integer point;

    /**
     * 累计获得的论坛积分
     */
    private Integer totalPoint;

    /**
     * 连续签到天数
     */
    private Integer continuousSignDays;

    /**
     * 累计签到天数
     */
    private Integer totalSignDays;

    /**
     * 最后签到时间
     */
    private LocalDate lastSignDate;

    /**
     * 发帖数量
     */
    private Integer postCount;

    /**
     * 参与活动数量
     */
    private Integer activityCount;

    /**
     * 获得的赞数量
     */
    private Integer likeCount;

    /**
     * 获得的收藏数量
     */
    private Integer favoriteCount;

    // ========== 隐私设置 ==========

    /**
     * 是否允许私聊，默认 true
     */
    private Boolean allowPrivateChat;

    /**
     * 是否接收系统消息，默认 true
     */
    private Boolean allowSystemMessage;

    /**
     * 是否为管理员
     */
    private Boolean isAdmin;

    /**
     * 是否隐藏认证学校信息
     */
    private Boolean hideSchoolInfo;

}
