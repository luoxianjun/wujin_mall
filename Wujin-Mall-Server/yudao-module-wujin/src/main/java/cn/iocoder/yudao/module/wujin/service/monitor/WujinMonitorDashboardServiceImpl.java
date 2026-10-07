package cn.iocoder.yudao.module.wujin.service.monitor;

import cn.iocoder.yudao.module.wujin.audit.WujinAuditAction;
import cn.iocoder.yudao.module.wujin.controller.admin.audit.vo.WujinRelationAuditRecordListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinMonitorDashboardRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinSearchBehaviorLogListReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.audit.WujinRelationAuditRecordDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.monitor.WujinSearchBehaviorLogDO;
import cn.iocoder.yudao.module.wujin.monitor.WujinMonitorAlert;
import cn.iocoder.yudao.module.wujin.monitor.WujinMonitorService;
import cn.iocoder.yudao.module.wujin.monitor.WujinMonitorSummary;
import cn.iocoder.yudao.module.wujin.monitor.WujinRelationAuditMonitorRecord;
import cn.iocoder.yudao.module.wujin.monitor.WujinSearchMonitorRecord;
import cn.iocoder.yudao.module.wujin.service.audit.WujinRelationAuditRecordAdminService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Validated
public class WujinMonitorDashboardServiceImpl implements WujinMonitorDashboardService {

    @Resource
    private WujinSearchBehaviorLogAdminService behaviorLogService;
    @Resource
    private WujinRelationAuditRecordAdminService auditRecordService;

    private final WujinMonitorService monitorService = new WujinMonitorService();

    @Override
    public WujinMonitorDashboardRespVO getSummary() {
        List<WujinSearchBehaviorLogDO> logs = behaviorLogService.getBehaviorLogList(new WujinSearchBehaviorLogListReqVO());
        List<WujinRelationAuditRecordDO> auditRecords = auditRecordService.getAuditRecordList(new WujinRelationAuditRecordListReqVO());

        List<WujinSearchMonitorRecord> searchRecords = toSearchRecords(logs);
        List<WujinRelationAuditMonitorRecord> auditMonitorRecords = toAuditRecords(auditRecords);
        WujinMonitorSummary summary = monitorService.summarize(searchRecords, auditMonitorRecords);

        WujinMonitorDashboardRespVO respVO = new WujinMonitorDashboardRespVO();
        respVO.setSearchSatisfaction(summary.getSearchSatisfaction());
        respVO.setChainViewRate(summary.getChainViewRate());
        respVO.setClassificationAccuracy(summary.getClassificationAccuracy());
        respVO.setAverageResponseTimeMillis(summary.getAverageResponseTimeMillis());
        respVO.setRelationAuditPassRate(summary.getRelationAuditPassRate());
        respVO.setHighRiskWarningCount(summary.getHighRiskWarningCount());
        respVO.setSearchSampleCount(logs.size());
        respVO.setAuditSampleCount(auditRecords.size());
        respVO.setAlerts(toAlertRespVOs(summary.getAlerts()));
        return respVO;
    }

    private List<WujinSearchMonitorRecord> toSearchRecords(List<WujinSearchBehaviorLogDO> logs) {
        List<WujinSearchMonitorRecord> records = new ArrayList<>();
        for (WujinSearchBehaviorLogDO log : logs) {
            records.add(WujinSearchMonitorRecord.builder()
                    .keyword(log.getKeyword())
                    .satisfactionScore(log.getSatisfactionScore())
                    .chainViewed(Boolean.TRUE.equals(log.getChainViewed()))
                    .classificationCorrect(log.getClassificationCorrect())
                    .responseTimeMillis(log.getResponseTimeMillis() == null ? 0L : log.getResponseTimeMillis())
                    .highRiskWarningTriggered(Boolean.TRUE.equals(log.getHighRiskWarningTriggered()))
                    .build());
        }
        return records;
    }

    private List<WujinRelationAuditMonitorRecord> toAuditRecords(List<WujinRelationAuditRecordDO> auditRecords) {
        List<WujinRelationAuditMonitorRecord> records = new ArrayList<>();
        for (WujinRelationAuditRecordDO record : auditRecords) {
            records.add(new WujinRelationAuditMonitorRecord(String.valueOf(record.getId()),
                    WujinAuditAction.valueOf(record.getAction())));
        }
        return records;
    }

    private List<WujinMonitorDashboardRespVO.Alert> toAlertRespVOs(List<WujinMonitorAlert> alerts) {
        List<WujinMonitorDashboardRespVO.Alert> respVOs = new ArrayList<>();
        for (WujinMonitorAlert alert : alerts) {
            WujinMonitorDashboardRespVO.Alert respVO = new WujinMonitorDashboardRespVO.Alert();
            respVO.setMetric(alert.getMetric().name());
            respVO.setMessage(alert.getMessage());
            respVOs.add(respVO);
        }
        return respVOs;
    }
}
