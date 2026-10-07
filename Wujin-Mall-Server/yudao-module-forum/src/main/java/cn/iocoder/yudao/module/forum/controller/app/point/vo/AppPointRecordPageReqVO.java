package cn.iocoder.yudao.module.forum.controller.app.point.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 积分记录分页请求 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 积分记录分页请求 VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppPointRecordPageReqVO extends PageParam {

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "业务类型", example = "1")
    private Integer bizType;

}

