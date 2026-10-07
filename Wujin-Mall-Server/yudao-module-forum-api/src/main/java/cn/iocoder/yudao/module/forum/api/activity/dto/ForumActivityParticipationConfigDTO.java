package cn.iocoder.yudao.module.forum.api.activity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 论坛活动报名参与配置 DTO
 */
@Data
public class ForumActivityParticipationConfigDTO implements Serializable {

    /**
     * 是否启用报名流程
     */
    private Boolean signUpRequired;

    /**
     * 报名后是否需要审核
     */
    private Boolean approvalRequired;

}
