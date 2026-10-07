package cn.iocoder.yudao.module.forum.dal.dataobject.sign;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 论坛签到规则 DO（周期积分计算）
 *
 * 对应表 forum_sign_rule
 */
@TableName("forum_sign_rule")
@KeySequence("forum_sign_rule_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForumSignRuleDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 周期类型：1=周，2=月
     */
    private Integer periodType;

    /**
     * 连续天数下限
     */
    private Integer minDays;

    /**
     * 连续天数上限（含）
     */
    private Integer maxDays;

    /**
     * 匹配该区间的积分
     */
    private Integer points;
}

