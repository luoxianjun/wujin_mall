package cn.iocoder.yudao.module.forum.controller.app.user.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 我的帖子分页请求 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 我的帖子分页请求 VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppMyPostPageReqVO extends PageParam {

    @Schema(description = "帖子状态", example = "1")
    private Integer status;

}

