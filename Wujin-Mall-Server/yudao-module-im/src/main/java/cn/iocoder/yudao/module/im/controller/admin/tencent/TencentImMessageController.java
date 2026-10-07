package cn.iocoder.yudao.module.im.controller.admin.tencent;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.im.controller.admin.tencent.vo.TencentImSendMsgReqVO;
import cn.iocoder.yudao.module.im.controller.admin.tencent.vo.TencentImSendMsgRespVO;
import cn.iocoder.yudao.module.im.service.TencentImMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 腾讯 IM 消息")
@RestController
@RequestMapping("/im/tencent/message")
@Validated
public class TencentImMessageController {

    @Resource
    private TencentImMessageService tencentImMessageService;

    @PostMapping("/send")
    @Operation(summary = "发送单聊消息（管理员或指定账号）")
    @PreAuthorize("@ss.hasPermission('im:tencent:message:send')")
    public CommonResult<TencentImSendMsgRespVO> sendMessage(@Valid @RequestBody TencentImSendMsgReqVO reqVO) {
        return success(tencentImMessageService.sendSingleMessage(reqVO));
    }
}
