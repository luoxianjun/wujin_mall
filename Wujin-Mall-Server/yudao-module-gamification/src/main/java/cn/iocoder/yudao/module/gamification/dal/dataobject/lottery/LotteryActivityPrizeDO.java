package cn.iocoder.yudao.module.gamification.dal.dataobject.lottery;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 抽奖活动-奖品关联表（M:N junction table）
 * 一个活动可以关联多个奖品，同一个奖品可被多个活动复用，概率在此配置
 */
@TableName("gamification_lottery_activity_prize")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotteryActivityPrizeDO extends BaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的抽奖活动ID */
    private Long lotteryActivityId;

    /** 关联的奖品ID */
    private Long prizeId;

    /** 中奖概率百分比（0-100），每个活动可单独配置 */
    private BigDecimal probability;

    /** 排序顺序 */
    private Integer sortOrder;
}
