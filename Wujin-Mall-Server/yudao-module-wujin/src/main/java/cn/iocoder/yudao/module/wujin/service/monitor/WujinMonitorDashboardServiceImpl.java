package cn.iocoder.yudao.module.wujin.service.monitor;

import cn.iocoder.yudao.module.wujin.audit.WujinAuditAction;
import cn.iocoder.yudao.module.wujin.controller.admin.audit.vo.WujinRelationAuditRecordListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinMonitorDashboardRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinMonitorTrendRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinSearchBehaviorLogListReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.audit.WujinRelationAuditRecordDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.monitor.WujinSearchBehaviorLogDO;
import cn.iocoder.yudao.module.wujin.dal.mysql.monitor.WujinSearchBehaviorLogMapper;
import cn.iocoder.yudao.module.wujin.monitor.WujinMonitorAlert;
import cn.iocoder.yudao.module.wujin.monitor.WujinMonitorService;
import cn.iocoder.yudao.module.wujin.monitor.WujinMonitorSummary;
import cn.iocoder.yudao.module.wujin.monitor.WujinRelationAuditMonitorRecord;
import cn.iocoder.yudao.module.wujin.monitor.WujinSearchMonitorRecord;
import cn.iocoder.yudao.module.wujin.service.audit.WujinRelationAuditRecordAdminService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Validated
public class WujinMonitorDashboardServiceImpl implements WujinMonitorDashboardService {

    @Resource
    private WujinSearchBehaviorLogAdminService behaviorLogService;
    @Resource
    private WujinRelationAuditRecordAdminService auditRecordService;
    @Resource
    private WujinSearchBehaviorLogMapper behaviorLogMapper;

    static final int MAX_TREND_DAYS = 90;
    private static final int TOP_KEYWORD_LIMIT = 10;

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

    @Override
    public WujinMonitorTrendRespVO getTrend(LocalDate endDate, int days) {
        int safeDays = Math.max(1, Math.min(MAX_TREND_DAYS, days));
        LocalDate end = endDate == null ? LocalDate.now() : endDate;
        LocalDate start = end.minusDays(safeDays - 1L);
        List<WujinSearchBehaviorLogDO> logs = behaviorLogMapper.selectListByCreateTimeRange(start.atStartOfDay(),
                end.plusDays(1).atStartOfDay());

        Map<LocalDate, List<WujinSearchBehaviorLogDO>> byDate = new LinkedHashMap<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            byDate.put(date, new ArrayList<WujinSearchBehaviorLogDO>());
        }
        Map<String, List<WujinSearchBehaviorLogDO>> byKeyword = new LinkedHashMap<>();
        Map<String, Integer> laneCounts = new LinkedHashMap<>();
        for (WujinSearchBehaviorLogDO log : logs) {
            if (log.getCreateTime() != null && byDate.containsKey(log.getCreateTime().toLocalDate())) {
                byDate.get(log.getCreateTime().toLocalDate()).add(log);
            }
            String keyword = log.getKeyword() == null ? "" : log.getKeyword().trim();
            if (!keyword.isEmpty()) {
                if (!byKeyword.containsKey(keyword)) {
                    byKeyword.put(keyword, new ArrayList<WujinSearchBehaviorLogDO>());
                }
                byKeyword.get(keyword).add(log);
            }
            String lane = log.getResultLane() == null ? "UNKNOWN" : log.getResultLane();
            laneCounts.put(lane, laneCounts.containsKey(lane) ? laneCounts.get(lane) + 1 : 1);
        }

        WujinMonitorTrendRespVO respVO = new WujinMonitorTrendRespVO();
        respVO.setStartDate(start);
        respVO.setEndDate(end);
        respVO.setTotalSearchCount(logs.size());
        respVO.setPoints(buildDailyPoints(byDate));
        respVO.setTopKeywords(buildTopKeywords(byKeyword));
        respVO.setLaneStats(buildLaneStats(laneCounts));
        return respVO;
    }

    private List<WujinMonitorTrendRespVO.DailyPoint> buildDailyPoints(
            Map<LocalDate, List<WujinSearchBehaviorLogDO>> byDate) {
        List<WujinMonitorTrendRespVO.DailyPoint> points = new ArrayList<>();
        for (Map.Entry<LocalDate, List<WujinSearchBehaviorLogDO>> entry : byDate.entrySet()) {
            WujinMonitorSummary summary = monitorService.summarize(toSearchRecords(entry.getValue()),
                    Collections.<WujinRelationAuditMonitorRecord>emptyList());
            WujinMonitorTrendRespVO.DailyPoint point = new WujinMonitorTrendRespVO.DailyPoint();
            point.setDate(entry.getKey());
            point.setSearchCount(entry.getValue().size());
            point.setSearchSatisfaction(round(summary.getSearchSatisfaction()));
            point.setChainViewRate(round(summary.getChainViewRate()));
            point.setClassificationAccuracy(round(summary.getClassificationAccuracy()));
            point.setAverageResponseTimeMillis(round(summary.getAverageResponseTimeMillis()));
            point.setHighRiskWarningCount(summary.getHighRiskWarningCount());
            points.add(point);
        }
        return points;
    }

    private List<WujinMonitorTrendRespVO.KeywordStat> buildTopKeywords(
            Map<String, List<WujinSearchBehaviorLogDO>> byKeyword) {
        List<WujinMonitorTrendRespVO.KeywordStat> stats = new ArrayList<>();
        for (Map.Entry<String, List<WujinSearchBehaviorLogDO>> entry : byKeyword.entrySet()) {
            int chainViewed = 0;
            for (WujinSearchBehaviorLogDO log : entry.getValue()) {
                if (Boolean.TRUE.equals(log.getChainViewed())) {
                    chainViewed++;
                }
            }
            WujinMonitorTrendRespVO.KeywordStat stat = new WujinMonitorTrendRespVO.KeywordStat();
            stat.setKeyword(entry.getKey());
            stat.setSearchCount(entry.getValue().size());
            stat.setChainViewRate(round((double) chainViewed / entry.getValue().size()));
            stats.add(stat);
        }
        stats.sort((left, right) -> right.getSearchCount().compareTo(left.getSearchCount()));
        return stats.size() > TOP_KEYWORD_LIMIT ? new ArrayList<>(stats.subList(0, TOP_KEYWORD_LIMIT)) : stats;
    }

    private List<WujinMonitorTrendRespVO.LaneStat> buildLaneStats(Map<String, Integer> laneCounts) {
        List<WujinMonitorTrendRespVO.LaneStat> stats = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : laneCounts.entrySet()) {
            WujinMonitorTrendRespVO.LaneStat stat = new WujinMonitorTrendRespVO.LaneStat();
            stat.setLane(entry.getKey());
            stat.setSearchCount(entry.getValue());
            stats.add(stat);
        }
        stats.sort((left, right) -> right.getSearchCount().compareTo(left.getSearchCount()));
        return stats;
    }

    private Double round(double value) {
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP).doubleValue();
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
