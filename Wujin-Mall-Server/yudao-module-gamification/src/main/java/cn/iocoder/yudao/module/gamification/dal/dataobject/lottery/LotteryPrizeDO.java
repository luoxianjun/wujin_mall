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

@TableName("gamification_lottery_prize")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotteryPrizeDO extends BaseDO {

    /** 奖品类型：积分 */
    public static final Integer TYPE_POINTS = 0;
    /** 奖品类型：优惠券 */
    public static final Integer TYPE_COUPON = 1;
    /** 奖品类型：实物 */
    public static final Integer TYPE_PHYSICAL = 2;
    /** 奖品类型：虚拟物品 */
    public static final Integer TYPE_VIRTUAL = 3;
    /** 奖品类型：谢谢参与 */
    public static final Integer TYPE_THANK_YOU = 4;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 奖品名称 */
    private String name;

    /** 奖品类型：0=积分, 1=优惠券, 2=实物, 3=虚拟物品, 4=谢谢参与 */
    private Integer type;

    /** 积分数量（type=POINTS时使用） */
    private Integer value;

    /** 奖品图片URL */
    private String imageUrl;

    /** 奖品总库存 */
    private Integer totalStock;

    /** 奖品剩余库存 */
    private Integer remainingStock;

    /** 排序顺序 */
    private Integer sortOrder;

    /** 是否需要填写地址（实物奖品） */
    private Boolean requireAddress;
}
