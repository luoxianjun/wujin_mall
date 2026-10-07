package cn.iocoder.yudao.module.forum.controller.app.message.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 会话分页请求 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 会话分页请求 VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppConversationPageReqVO extends PageParam {

}

