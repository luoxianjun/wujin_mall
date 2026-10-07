package cn.iocoder.yudao.module.gamification.dal.dataobject.lottery;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@TableName("gamification_lottery_activity")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotteryActivityDO extends TenantBaseDO {

    /** 抽奖类型：定时开奖 */
    public static final Integer TYPE_SCHEDULED = 0;
    /** 抽奖类型：即时（摇一摇） */
    public static final Integer TYPE_INSTANT = 1;

    /** 参与条件：所有人 */
    public static final Integer CONDITION_ALL = 0;
    /** 参与条件：已报名 */
    public static final Integer CONDITION_ENROLLED = 1;
    /** 参与条件：受邀人群 */
    public static final Integer CONDITION_INVITED = 2;

    /** 费用类型：免费 */
    public static final Integer COST_FREE = 0;
    /** 费用类型：积分 */
    public static final Integer COST_POINTS = 1;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的论坛活动ID */
    private Long activityId;

    /** 抽奖类型：0=定时开奖, 1=即时摇一摇 */
    private Integer type;

    /** 定时开奖时间（仅type=SCHEDULED时使用） */
    private LocalDateTime drawTime;

    /** 每日抽奖次数上限 */
    private Integer maxDrawsPerDay;

    /** 总抽奖次数上限 */
    private Integer maxDrawsTotal;

    /** 费用类型：0=免费, 1=积分 */
    private Integer costType;

    /** 积分消耗数量（costType=POINTS时使用） */
    private Integer costAmount;

    /** 保底机制：连续N次未中奖则保底中奖（0=禁用） */
    private Integer guaranteeDraws;

    /** 参与条件：0=所有人, 1=已报名, 2=受邀人群 */
    private Integer participationCondition;

    /** 状态：0=启用, 1=禁用 */
    private Integer status;
}
