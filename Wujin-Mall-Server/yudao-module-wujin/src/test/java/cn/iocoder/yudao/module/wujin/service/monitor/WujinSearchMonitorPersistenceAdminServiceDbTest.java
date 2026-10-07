package cn.iocoder.yudao.module.wujin.service.monitor;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.wujin.audit.WujinAuditAction;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinMonitorSnapshotListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinMonitorSnapshotSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinSearchBehaviorLogListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinSearchBehaviorLogSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinSearchRuleConfigListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinSearchRuleConfigSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.monitor.WujinMonitorSnapshotDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.monitor.WujinSearchBehaviorLogDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.monitor.WujinSearchRuleConfigDO;
import cn.iocoder.yudao.module.wujin.monitor.WujinMonitorMetric;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Import({WujinSearchRuleConfigAdminServiceImpl.class,
        WujinSearchBehaviorLogAdminServiceImpl.class,
        WujinMonitorSnapshotAdminServiceImpl.class})
class WujinSearchMonitorPersistenceAdminServiceDbTest extends BaseDbUnitTest {

    @Resource
    private WujinSearchRuleConfigAdminService ruleConfigService;
    @Resource
    private WujinSearchBehaviorLogAdminService behaviorLogService;
    @Resource
    private WujinMonitorSnapshotAdminService monitorSnapshotService;

    @Test
    void createRuleConfigPersistsAndFiltersByLaneAndIndustry() {
        Long ruleId = ruleConfigService.createRuleConfig(ruleReq("GRANULARITY_LIMIT", WujinLane.PRODUCT.name(), "轮胎橡胶", "3"));
        ruleConfigService.createRuleConfig(ruleReq("GRANULARITY_LIMIT", WujinLane.MATERIAL.name(), "医用乳胶", "2"));

        WujinSearchRuleConfigListReqVO listReqVO = new WujinSearchRuleConfigListReqVO();
        listReqVO.setLane(WujinLane.PRODUCT.name());
        listReqVO.setIndustryCode("轮胎橡胶");
        listReqVO.setStatus(0);
        List<WujinSearchRuleConfigDO> rules = ruleConfigService.getRuleConfigList(listReqVO);

        assertEquals(1, rules.size());
        assertEquals(ruleId, rules.get(0).getId());
        assertEquals("GRANULARITY_LIMIT", rules.get(0).getRuleType());
        assertEquals("3", rules.get(0).getRuleValue());
    }

    @Test
    void createSearchBehaviorLogPersistsAndFiltersByLaneAndRisk() {
        Long logId = behaviorLogService.createBehaviorLog(logReq("轮胎", WujinLane.PRODUCT.name(), true, 5, 780L));
        behaviorLogService.createBehaviorLog(logReq("天然橡胶", WujinLane.MATERIAL.name(), false, 4, 420L));

        WujinSearchBehaviorLogListReqVO listReqVO = new WujinSearchBehaviorLogListReqVO();
        listReqVO.setResultLane(WujinLane.PRODUCT.name());
        listReqVO.setHighRiskWarningTriggered(true);
        List<WujinSearchBehaviorLogDO> logs = behaviorLogService.getBehaviorLogList(listReqVO);

        assertEquals(1, logs.size());
        assertEquals(logId, logs.get(0).getId());
        assertEquals("轮胎", logs.get(0).getKeyword());
        assertEquals(780L, logs.get(0).getResponseTimeMillis());
    }

    @Test
    void createMonitorSnapshotPersistsAndFiltersByMetric() {
        Long snapshotId = monitorSnapshotService.createSnapshot(snapshotReq(WujinMonitorMetric.RELATION_AUDIT_PASS_RATE.name(), 85, 90,
                WujinAuditAction.APPROVE.name()));
        monitorSnapshotService.createSnapshot(snapshotReq(WujinMonitorMetric.HIGH_RISK_WARNING_COUNT.name(), 12, 0,
                WujinAuditAction.BLOCK.name()));

        WujinMonitorSnapshotListReqVO listReqVO = new WujinMonitorSnapshotListReqVO();
        listReqVO.setMetric(WujinMonitorMetric.RELATION_AUDIT_PASS_RATE.name());
        List<WujinMonitorSnapshotDO> snapshots = monitorSnapshotService.getSnapshotList(listReqVO);

        assertEquals(1, snapshots.size());
        assertEquals(snapshotId, snapshots.get(0).getId());
        assertEquals(85, snapshots.get(0).getMetricValue());
        assertEquals(WujinAuditAction.APPROVE.name(), snapshots.get(0).getAuditAction());
    }

    private WujinSearchRuleConfigSaveReqVO ruleReq(String ruleType, String lane, String industryCode, String ruleValue) {
        WujinSearchRuleConfigSaveReqVO reqVO = new WujinSearchRuleConfigSaveReqVO();
        reqVO.setRuleType(ruleType);
        reqVO.setLane(lane);
        reqVO.setIndustryCode(industryCode);
        reqVO.setRuleValue(ruleValue);
        reqVO.setWeight(80);
        reqVO.setStatus(0);
        reqVO.setRemark("运营配置");
        return reqVO;
    }

    private WujinSearchBehaviorLogSaveReqVO logReq(String keyword, String resultLane, Boolean highRiskWarningTriggered,
                                                   Integer satisfactionScore, Long responseTimeMillis) {
        WujinSearchBehaviorLogSaveReqVO reqVO = new WujinSearchBehaviorLogSaveReqVO();
        reqVO.setUserId(101L);
        reqVO.setKeyword(keyword);
        reqVO.setIntent("PRODUCT");
        reqVO.setResultLane(resultLane);
        reqVO.setIndustryCode("轮胎橡胶");
        reqVO.setChainViewed(true);
        reqVO.setClassificationCorrect(true);
        reqVO.setHighRiskWarningTriggered(highRiskWarningTriggered);
        reqVO.setSatisfactionScore(satisfactionScore);
        reqVO.setResponseTimeMillis(responseTimeMillis);
        return reqVO;
    }

    private WujinMonitorSnapshotSaveReqVO snapshotReq(String metric, Integer metricValue, Integer thresholdValue, String auditAction) {
        WujinMonitorSnapshotSaveReqVO reqVO = new WujinMonitorSnapshotSaveReqVO();
        reqVO.setMetric(metric);
        reqVO.setMetricValue(metricValue);
        reqVO.setThresholdValue(thresholdValue);
        reqVO.setAuditAction(auditAction);
        reqVO.setAlertFlag(metricValue < thresholdValue);
        reqVO.setRemark("运营看板快照");
        return reqVO;
    }
}
