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

import java.time.LocalDateTime;

@TableName("gamification_lottery_record")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotteryRecordDO extends BaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的抽奖活动ID */
    private Long lotteryActivityId;

    /** 用户ID */
    private Long userId;

    /** 奖品ID（未中奖/谢谢参与时为null） */
    private Long prizeId;

    /** 奖品名称 */
    private String prizeName;

    /** 奖品类型 */
    private Integer prizeType;

    /** 是否中奖 */
    private Boolean won;

    /** 抽奖时间 */
    private LocalDateTime drawTime;

    /** 奖品是否已发放 */
    private Boolean delivered;

    /** 收货地址（实物奖品） */
    private String deliveryAddress;
}
