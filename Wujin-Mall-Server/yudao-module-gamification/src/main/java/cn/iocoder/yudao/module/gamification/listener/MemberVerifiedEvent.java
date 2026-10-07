package cn.iocoder.yudao.module.gamification.listener;

import org.springframework.context.ApplicationEvent;

/**
 * 会员实名认证完成事件
 *
 * @author gamification
 */
public class MemberVerifiedEvent extends ApplicationEvent {

    /**
     * 用户ID
     */
    private final Long userId;

    public MemberVerifiedEvent(Object source, Long userId) {
        super(source);
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return "MemberVerifiedEvent{" +
                "userId=" + userId +
                '}';
    }
}
