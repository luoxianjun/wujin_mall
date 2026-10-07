package cn.iocoder.yudao.module.forum.controller.admin.sign.vo;

import cn.iocoder.yudao.module.forum.controller.app.sign.vo.AppSignRecordPageReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 管理后台 - 签到记录分页请求 VO
 * 复用 App 请求字段。
 */
@Schema(description = "管理后台 - 签到记录分页请求 VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AdminSignRecordPageReqVO extends AppSignRecordPageReqVO {

    @Schema(description = "用户UID", example = "U123456")
    private String uid;

}
