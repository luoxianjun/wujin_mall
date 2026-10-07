package cn.iocoder.yudao.module.gamification.controller.app.invitation.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "小程序 - 邀请记录分页请求")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppInvitationRecordPageReqVO extends PageParam {
}
