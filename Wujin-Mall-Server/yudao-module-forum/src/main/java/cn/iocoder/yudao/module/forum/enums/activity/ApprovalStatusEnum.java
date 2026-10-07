package cn.iocoder.yudao.module.forum.enums.activity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 活动报名审核状态枚举
 *
 * @author forum
 */
@Getter
@AllArgsConstructor
public enum ApprovalStatusEnum {

    PENDING(0, "待审核"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已拒绝"),
    CANCELLED(3, "已取消");

    /**
     * 状态值
     */
    private final Integer status;

    /**
     * 状态名称
     */
    private final String name;

    /**
     * 根据状态值获取枚举
     */
    public static ApprovalStatusEnum getByStatus(Integer status) {
        for (ApprovalStatusEnum value : values()) {
            if (value.getStatus().equals(status)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 根据状态值获取名称
     */
    public static String getNameByStatus(Integer status) {
        ApprovalStatusEnum approvalStatus = getByStatus(status);
        return approvalStatus != null ? approvalStatus.getName() : null;
    }

}

