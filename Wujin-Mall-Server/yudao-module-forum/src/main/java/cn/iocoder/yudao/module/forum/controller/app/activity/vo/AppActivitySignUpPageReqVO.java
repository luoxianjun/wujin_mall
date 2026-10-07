package cn.iocoder.yudao.module.forum.controller.app.activity.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 活动报名分页请求 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 活动报名分页请求 VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppActivitySignUpPageReqVO extends PageParam {

    @Schema(description = "活动ID", example = "1")
    private Long activityId;

    @Schema(description = "审核状态：0-待审核，1-已通过，2-已拒绝，3-已取消", example = "1")
    private Integer approvalStatus;

}

