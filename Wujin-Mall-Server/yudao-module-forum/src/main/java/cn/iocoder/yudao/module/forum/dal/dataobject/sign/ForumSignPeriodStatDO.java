package cn.iocoder.yudao.module.forum.dal.dataobject.sign;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDate;

/**
 * 论坛签到周期统计 DO（周/月）
 *
 * 对应表 forum_sign_period_stat
 */
@TableName("forum_sign_period_stat")
@KeySequence("forum_sign_period_stat_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForumSignPeriodStatDO extends BaseDO {

    /**
     * 自增主键
     */
    @TableId
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 周期类型：1=周，2=月
     */
    private Integer periodType;

    /**
     * 周期键，如 2025W47 / 2025M11
     */
    private String periodKey;

    /**
     * 当前连续签到天数
     */
    private Integer currentStreak;

    /**
     * 周期内最大连续天数
     */
    private Integer maxStreak;

    /**
     * 周期累计积分
     */
    private Integer totalPoints;

    /**
     * 最后一次签到日期
     */
    private LocalDate lastSignDate;

    /**
     * 乐观锁版本号
     */
    private Integer version;
}
