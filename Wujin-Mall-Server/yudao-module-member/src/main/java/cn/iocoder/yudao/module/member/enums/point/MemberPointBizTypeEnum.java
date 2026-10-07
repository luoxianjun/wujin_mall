package cn.iocoder.yudao.module.member.enums.point;

import cn.hutool.core.util.EnumUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 会员积分的业务类型枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum MemberPointBizTypeEnum implements ArrayValuable<Integer> {

    SIGN(1, "签到", "签到获得 {} 积分", true),
    ADMIN(2, "管理员修改", "管理员修改 {} 积分", true),

    ORDER_USE(11, "订单积分抵扣", "下单使用 {} 积分", false), // 下单时，扣减积分
    ORDER_USE_CANCEL(12, "订单积分抵扣（整单取消）", "订单取消，退还 {} 积分", true), // ORDER_USE 的取消
    ORDER_USE_CANCEL_ITEM(13, "订单积分抵扣（单个退款）", "订单退款，退还 {} 积分", true), // ORDER_USE 的取消

    ORDER_GIVE(21, "订单积分奖励", "下单获得 {} 积分", true), // 支付订单时，赠送积分
    ORDER_GIVE_CANCEL(22, "订单积分奖励（整单取消）", "订单取消，退还 {} 积分", false), // ORDER_GIVE 的取消
    ORDER_GIVE_CANCEL_ITEM(23, "订单积分奖励（单个退款）", "订单退款，扣除赠送的 {} 积分", false), // ORDER_GIVE 的取消

    INVITATION_REGISTER(31, "邀请注册", "邀请注册奖励 {} 积分", true),
    INVITATION_COMPLETE(32, "邀请完成", "邀请认证奖励 {} 积分", true),
    INVITATION_INVITEE(33, "被邀请奖励", "被邀请用户奖励 {} 积分", true),
    QUIZ_RANK_REWARD(34, "答题排行奖励", "答题排行奖励 {} 积分", true),

    LOTTERY_COST(35, "抽奖消耗", "抽奖消耗 {} 积分", false), // 抽奖扣减积分
    LOTTERY_WIN(36, "抽奖中奖", "抽奖中奖奖励 {} 积分", true), // 抽奖中奖积分奖励
    VOTE_PARTICIPATE(37, "投票参与", "投票参与奖励 {} 积分", true) // 投票参与积分奖励
    ;

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名字
     */
    private final String name;
    /**
     * 描述
     */
    private final String description;
    /**
     * 是否为扣减积分
     */
    private final boolean add;

    @Override
    public Integer[] array() {
        return new Integer[0];
    }

    public static MemberPointBizTypeEnum getByType(Integer type) {
        return EnumUtil.getBy(MemberPointBizTypeEnum.class,
                e -> Objects.equals(type, e.getType()));
    }

}
