package cn.iocoder.yudao.module.forum.controller.app.message.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 系统通知分页请求 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 系统通知分页请求 VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppSystemNoticePageReqVO extends PageParam {

    @Schema(description = "通知类型：1-点赞通知，2-评论通知，3-关注通知，4-系统通知，5-活动通知", example = "1")
    private Integer noticeType;

    @Schema(description = "是否已读", example = "false")
    private Boolean readStatus;

}

