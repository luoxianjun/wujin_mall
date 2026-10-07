package cn.iocoder.yudao.module.gamification.job;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.gamification.service.invitation.InvitationRewardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 邀请奖励重试定时任务
 *
 * 定期重试失败的邀请奖励发放
 * 建议配置：每5分钟执行一次
 *
 * @author gamification
 */
@Slf4j
@Component
public class InvitationRewardRetryJob implements JobHandler {

    @Resource
    private InvitationRewardService invitationRewardService;

    /**
     * 每次处理的最大记录数
     */
    private static final Integer BATCH_SIZE = 100;

    @Override
    @TenantJob
    public String execute(String param) {
        log.info("[InvitationRewardRetryJob] Starting retry job");

        try {
            // 解析参数，如果传入了批次大小则使用，否则使用默认值
            Integer limit = BATCH_SIZE;
            if (StrUtil.isNotBlank(param)) {
                try {
                    limit = Integer.parseInt(param);
                } catch (NumberFormatException e) {
                    log.warn("[InvitationRewardRetryJob] Invalid param: {}, using default: {}", param, BATCH_SIZE);
                }
            }

            // 执行重试
            int successCount = invitationRewardService.retryFailedRewards(limit);

            String result = StrUtil.format("成功重试 {} 个失败的奖励发放", successCount);
            log.info("[InvitationRewardRetryJob] {}", result);
            return result;

        } catch (Exception e) {
            log.error("[InvitationRewardRetryJob] Execution failed", e);
            return "执行失败：" + e.getMessage();
        }
    }
}
