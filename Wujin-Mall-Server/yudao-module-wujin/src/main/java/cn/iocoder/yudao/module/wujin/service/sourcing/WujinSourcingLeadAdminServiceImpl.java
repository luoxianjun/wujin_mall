package cn.iocoder.yudao.module.wujin.service.sourcing;

import cn.iocoder.yudao.module.wujin.controller.admin.sourcing.vo.WujinSourcingLeadDispatchReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.sourcing.vo.WujinSourcingLeadListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.sourcing.vo.WujinSourcingLeadConversionReportReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.sourcing.vo.WujinSourcingLeadConversionReportRespVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSupplierCandidateReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSupplierCandidateRespVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.sourcing.WujinSourcingLeadDO;
import cn.iocoder.yudao.module.wujin.dal.mysql.sourcing.WujinSourcingLeadMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Validated
public class WujinSourcingLeadAdminServiceImpl implements WujinSourcingLeadAdminService {

    @Resource
    private WujinSourcingLeadMapper sourcingLeadMapper;
    @Resource
    private WujinAppSourcingService appSourcingService;

    @Override
    public List<WujinSourcingLeadDO> getLeadList(WujinSourcingLeadListReqVO reqVO) {
        return sourcingLeadMapper.selectList(reqVO);
    }

    @Override
    public void dispatchLead(WujinSourcingLeadDispatchReqVO reqVO) {
        WujinSourcingLeadDO lead = validateLeadExists(reqVO.getLeadId());
        lead.setMerchantId(reqVO.getMerchantId());
        lead.setDispatchStatus("DISPATCHED");
        lead.setLeadStatus("ASSIGNED");
        lead.setDispatchRemark(reqVO.getDispatchRemark());
        sourcingLeadMapper.updateById(lead);
    }

    @Override
    public void autoDispatchLead(Long leadId) {
        WujinSourcingLeadDO lead = validateLeadExists(leadId);
        WujinSupplierCandidateRespVO candidate = findBestMerchantCandidate(lead);
        if (candidate == null) {
            throw new IllegalArgumentException("暂无可自动分发的审核通过商家");
        }

        lead.setMerchantId(candidate.getId());
        lead.setSupplierId(candidate.getId());
        lead.setSupplierName(candidate.getSupplierName());
        lead.setDispatchStatus("DISPATCHED");
        lead.setLeadStatus("ASSIGNED");
        lead.setDispatchRemark("自动匹配分发给" + candidate.getSupplierName()
                + "，匹配分" + candidate.getMatchScore() + "；" + candidate.getServiceNote());
        sourcingLeadMapper.updateById(lead);
    }

    @Override
    public WujinSourcingLeadConversionReportRespVO getConversionReport(WujinSourcingLeadConversionReportReqVO reqVO) {
        List<WujinSourcingLeadDO> leads = sourcingLeadMapper.selectConversionReportList(reqVO);
        WujinSourcingLeadConversionReportRespVO report = new WujinSourcingLeadConversionReportRespVO();
        report.setTotalLeadCount(leads.size());
        report.setContactedCount(countContacted(leads));
        report.setQuotedCount(countStatus(leads, "QUOTED") + countStatus(leads, "CONVERTED"));
        report.setConvertedCount(countStatus(leads, "CONVERTED"));
        report.setLostCount(countStatus(leads, "LOST"));
        report.setConversionRate(percent(report.getConvertedCount(), report.getTotalLeadCount()));
        report.setAverageProcessDurationMinutes(averageDuration(leads));
        report.setMerchantStats(buildMerchantStats(leads));
        report.setLaneStats(buildLaneStats(leads));
        return report;
    }

    private WujinSupplierCandidateRespVO findBestMerchantCandidate(WujinSourcingLeadDO lead) {
        WujinSupplierCandidateReqVO reqVO = new WujinSupplierCandidateReqVO();
        reqVO.setKeyword(lead.getKeyword());
        reqVO.setLane(lead.getLane());
        reqVO.setSourceKeyword(lead.getSourceKeyword());
        reqVO.setIndustry(lead.getIndustry());
        List<WujinSupplierCandidateRespVO> candidates = appSourcingService.getSupplierCandidates(reqVO);
        for (WujinSupplierCandidateRespVO candidate : candidates) {
            if (candidate.getId() != null && candidate.getEntityId() != null
                    && !candidate.getId().equals(candidate.getEntityId())) {
                return candidate;
            }
        }
        return null;
    }

    private WujinSourcingLeadDO validateLeadExists(Long id) {
        WujinSourcingLeadDO lead = id == null ? null : sourcingLeadMapper.selectById(id);
        if (lead == null) {
            throw new IllegalArgumentException("寻源线索不存在");
        }
        return lead;
    }

    private int countContacted(List<WujinSourcingLeadDO> leads) {
        int count = 0;
        for (WujinSourcingLeadDO lead : leads) {
            if (lead.getFirstContactTime() != null || statusAtOrAfterContact(lead.getLeadStatus())) {
                count++;
            }
        }
        return count;
    }

    private boolean statusAtOrAfterContact(String status) {
        return "CONTACTED".equals(status) || "QUOTED".equals(status)
                || "CONVERTED".equals(status) || "LOST".equals(status);
    }

    private int countStatus(List<WujinSourcingLeadDO> leads, String status) {
        int count = 0;
        for (WujinSourcingLeadDO lead : leads) {
            if (status.equals(lead.getLeadStatus())) {
                count++;
            }
        }
        return count;
    }

    private Long averageDuration(List<WujinSourcingLeadDO> leads) {
        long total = 0L;
        int count = 0;
        for (WujinSourcingLeadDO lead : leads) {
            if (lead.getProcessDurationMinutes() != null) {
                total += lead.getProcessDurationMinutes();
                count++;
            }
        }
        return count == 0 ? 0L : total / count;
    }

    private List<WujinSourcingLeadConversionReportRespVO.MerchantStat> buildMerchantStats(List<WujinSourcingLeadDO> leads) {
        Map<Long, Counter> counters = new LinkedHashMap<>();
        for (WujinSourcingLeadDO lead : leads) {
            if (lead.getMerchantId() == null) {
                continue;
            }
            Counter counter = counters.get(lead.getMerchantId());
            if (counter == null) {
                counter = new Counter();
                counters.put(lead.getMerchantId(), counter);
            }
            counter.add(lead);
        }
        List<WujinSourcingLeadConversionReportRespVO.MerchantStat> stats = new ArrayList<>();
        for (Map.Entry<Long, Counter> entry : counters.entrySet()) {
            WujinSourcingLeadConversionReportRespVO.MerchantStat stat =
                    new WujinSourcingLeadConversionReportRespVO.MerchantStat();
            stat.setMerchantId(entry.getKey());
            stat.setTotalLeadCount(entry.getValue().total);
            stat.setConvertedCount(entry.getValue().converted);
            stat.setConversionRate(percent(entry.getValue().converted, entry.getValue().total));
            stats.add(stat);
        }
        return stats;
    }

    private List<WujinSourcingLeadConversionReportRespVO.LaneStat> buildLaneStats(List<WujinSourcingLeadDO> leads) {
        Map<String, Counter> counters = new LinkedHashMap<>();
        for (WujinSourcingLeadDO lead : leads) {
            Counter counter = counters.get(lead.getLane());
            if (counter == null) {
                counter = new Counter();
                counters.put(lead.getLane(), counter);
            }
            counter.add(lead);
        }
        List<WujinSourcingLeadConversionReportRespVO.LaneStat> stats = new ArrayList<>();
        for (Map.Entry<String, Counter> entry : counters.entrySet()) {
            WujinSourcingLeadConversionReportRespVO.LaneStat stat =
                    new WujinSourcingLeadConversionReportRespVO.LaneStat();
            stat.setLane(entry.getKey());
            stat.setTotalLeadCount(entry.getValue().total);
            stat.setConvertedCount(entry.getValue().converted);
            stat.setConversionRate(percent(entry.getValue().converted, entry.getValue().total));
            stats.add(stat);
        }
        return stats;
    }

    private String percent(int numerator, int denominator) {
        if (denominator == 0) {
            return "0.00%";
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP)
                .toPlainString() + "%";
    }

    private static class Counter {
        private int total;
        private int converted;

        void add(WujinSourcingLeadDO lead) {
            total++;
            if ("CONVERTED".equals(lead.getLeadStatus())) {
                converted++;
            }
        }
    }
}
