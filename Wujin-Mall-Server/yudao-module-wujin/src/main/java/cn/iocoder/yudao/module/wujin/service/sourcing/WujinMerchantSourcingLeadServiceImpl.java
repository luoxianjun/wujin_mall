package cn.iocoder.yudao.module.wujin.service.sourcing;

import cn.iocoder.yudao.module.wujin.controller.admin.sourcing.vo.WujinSourcingLeadListReqVO;
import cn.iocoder.yudao.module.wujin.controller.merchant.sourcing.vo.WujinMerchantSourcingLeadHandleReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.sourcing.WujinSourcingLeadDO;
import cn.iocoder.yudao.module.wujin.dal.mysql.sourcing.WujinSourcingLeadMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@Validated
public class WujinMerchantSourcingLeadServiceImpl implements WujinMerchantSourcingLeadService {

    @Resource
    private WujinSourcingLeadMapper sourcingLeadMapper;

    @Override
    public List<WujinSourcingLeadDO> getLeadList(WujinSourcingLeadListReqVO reqVO) {
        return sourcingLeadMapper.selectList(reqVO);
    }

    @Override
    public void handleLead(WujinMerchantSourcingLeadHandleReqVO reqVO) {
        WujinSourcingLeadDO lead = validateMerchantLead(reqVO.getLeadId(), reqVO.getMerchantId());
        validateTransition(lead.getLeadStatus(), reqVO.getHandleAction());
        LocalDateTime now = LocalDateTime.now();
        lead.setLeadStatus(reqVO.getHandleAction());
        lead.setHandleRemark(reqVO.getHandleRemark());
        applyProcessTime(lead, reqVO.getHandleAction(), now);
        applyFollowUp(lead, reqVO);
        sourcingLeadMapper.updateById(lead);
    }

    private void applyFollowUp(WujinSourcingLeadDO lead, WujinMerchantSourcingLeadHandleReqVO reqVO) {
        if (reqVO.getFollowStage() != null && !reqVO.getFollowStage().trim().isEmpty()) {
            lead.setFollowStage(reqVO.getFollowStage().trim());
        }
        if (reqVO.getNextFollowTime() != null) {
            lead.setNextFollowTime(reqVO.getNextFollowTime());
        }
        if (reqVO.getQuotedAmount() != null) {
            lead.setQuotedAmount(reqVO.getQuotedAmount());
        }
        if (reqVO.getWinProbability() != null) {
            lead.setWinProbability(reqVO.getWinProbability());
        }
        if ("CONVERTED".equals(reqVO.getHandleAction())) {
            lead.setWinProbability(100);
        } else if ("LOST".equals(reqVO.getHandleAction())) {
            lead.setWinProbability(0);
        }
    }

    private void validateTransition(String currentStatus, String nextStatus) {
        if ("ASSIGNED".equals(currentStatus) && "CONTACTED".equals(nextStatus)) {
            return;
        }
        if ("CONTACTED".equals(currentStatus) && Arrays.asList("QUOTED", "LOST").contains(nextStatus)) {
            return;
        }
        if ("QUOTED".equals(currentStatus) && Arrays.asList("CONVERTED", "LOST").contains(nextStatus)) {
            return;
        }
        if (currentStatus != null && currentStatus.equals(nextStatus)) {
            return;
        }
        throw new IllegalArgumentException("线索状态不允许从 " + currentStatus + " 流转到 " + nextStatus);
    }

    private void applyProcessTime(WujinSourcingLeadDO lead, String nextStatus, LocalDateTime now) {
        if ("CONTACTED".equals(nextStatus) && lead.getFirstContactTime() == null) {
            lead.setFirstContactTime(now);
        } else if ("QUOTED".equals(nextStatus) && lead.getQuotedTime() == null) {
            lead.setQuotedTime(now);
        } else if ("CONVERTED".equals(nextStatus) && lead.getConvertedTime() == null) {
            lead.setConvertedTime(now);
            lead.setProcessDurationMinutes(durationFromCreate(lead, now));
        } else if ("LOST".equals(nextStatus) && lead.getLostTime() == null) {
            lead.setLostTime(now);
            lead.setProcessDurationMinutes(durationFromCreate(lead, now));
        }
    }

    private Long durationFromCreate(WujinSourcingLeadDO lead, LocalDateTime now) {
        if (lead.getCreateTime() == null) {
            return 0L;
        }
        long minutes = Duration.between(lead.getCreateTime(), now).toMinutes();
        return Math.max(0L, minutes);
    }

    private WujinSourcingLeadDO validateMerchantLead(Long leadId, Long merchantId) {
        WujinSourcingLeadDO lead = leadId == null ? null : sourcingLeadMapper.selectById(leadId);
        if (lead == null) {
            throw new IllegalArgumentException("寻源线索不存在");
        }
        if (merchantId == null || !merchantId.equals(lead.getMerchantId())) {
            throw new IllegalArgumentException("无权处理该寻源线索");
        }
        return lead;
    }
}
