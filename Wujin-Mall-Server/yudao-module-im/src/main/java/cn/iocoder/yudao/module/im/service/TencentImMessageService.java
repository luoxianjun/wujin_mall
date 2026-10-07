package cn.iocoder.yudao.module.im.service;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.im.client.tencent.TencentImClient;
import cn.iocoder.yudao.module.im.client.tencent.resp.TencentImSendMsgResp;
import cn.iocoder.yudao.module.im.client.tencent.vo.TencentImSendMsgReq;
import cn.iocoder.yudao.module.im.controller.admin.tencent.vo.TencentImSendMsgReqVO;
import cn.iocoder.yudao.module.im.controller.admin.tencent.vo.TencentImSendMsgRespVO;
import cn.iocoder.yudao.module.im.config.TencentImProperties;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.TENCENT_IM_REQUEST_FAILED;

@Service
@Validated
@Slf4j
public class TencentImMessageService {

    @Resource
    private TencentImClient tencentImClient;

    @Resource
    private TencentImProperties properties;

    public TencentImSendMsgRespVO sendSingleMessage(TencentImSendMsgReqVO reqVO) {
        TencentImSendMsgReq req = convert(reqVO);
        TencentImSendMsgResp resp = tencentImClient.sendSingleMessage(req, req.getFromAccount());
        if (!resp.isSuccess()) {
            log.warn("[sendSingleMessage][失败 code={}, msg={}]", resp.getErrorCode(), resp.getErrorInfo());
            throw ServiceExceptionUtil.exception(TENCENT_IM_REQUEST_FAILED);
        }
        TencentImSendMsgRespVO respVO = new TencentImSendMsgRespVO();
        respVO.setMsgId(resp.getMsgId());
        respVO.setMsgKey(resp.getMsgKey());
        respVO.setMsgTime(resp.getMsgTime());
        return respVO;
    }

    /**
     * 使用管理员账号发送简单文本消息
     */
    public void sendAdminTextMessage(String toAccount, String text) {
        TencentImSendMsgReqVO reqVO = new TencentImSendMsgReqVO();
        reqVO.setToAccount(toAccount);
        reqVO.setMsgRandom(RandomUtil.randomInt(1, Integer.MAX_VALUE));
        TencentImSendMsgReqVO.MsgBodyItem bodyItem = new TencentImSendMsgReqVO.MsgBodyItem();
        bodyItem.setMsgType("TIMTextElem");
        bodyItem.setMsgContent(java.util.Collections.singletonMap("Text", text));
        reqVO.setMsgBody(java.util.Collections.singletonList(bodyItem));
        sendSingleMessage(reqVO);
    }

    private TencentImSendMsgReq convert(TencentImSendMsgReqVO reqVO) {
        TencentImSendMsgReq req = new TencentImSendMsgReq();
        req.setFromAccount(reqVO.getFromAccount() != null ? reqVO.getFromAccount() : properties.getAdminIdentifier());
        req.setToAccount(reqVO.getToAccount());
        req.setMsgRandom(reqVO.getMsgRandom());
        req.setOnlineOnlyFlag(reqVO.getOnlineOnlyFlag());
        req.setSyncOtherMachine(reqVO.getSyncOtherMachine());
        req.setMsgSeq(reqVO.getMsgSeq());
        req.setSendMsgControl(reqVO.getSendMsgControl());
        req.setForbidCallbackControl(reqVO.getForbidCallbackControl());
        req.setCloudCustomData(reqVO.getCloudCustomData());
        req.setSupportMessageExtension(reqVO.getSupportMessageExtension());
        req.setOfflinePushInfo(reqVO.getOfflinePushInfo());
        req.setIsNeedReadReceipt(reqVO.getIsNeedReadReceipt());
        req.setMsgBody(CollUtil.isNotEmpty(reqVO.getMsgBody())
                ? reqVO.getMsgBody().stream().map(item -> {
                    TencentImSendMsgReq.MsgBodyItem bodyItem = new TencentImSendMsgReq.MsgBodyItem();
                    bodyItem.setMsgType(item.getMsgType());
                    bodyItem.setMsgContent(item.getMsgContent());
                    return bodyItem;
                }).collect(Collectors.toList())
                : null);
        return req;
    }
}
