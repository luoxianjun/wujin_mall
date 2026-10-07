package cn.iocoder.yudao.module.forum.job;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.forum.service.activity.ForumActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 活动开始提醒定时任务
 * 
 * 每5分钟执行一次，查询2小时后开始的活动并通知已报名的用户
 *
 * @author forum
 */
@Slf4j
@Component
public class ActivityStartNotifyJob implements JobHandler {

    @Resource
    private ForumActivityService activityService;

    @Override
    @TenantJob
    public String execute(String param) {
        try {
            int count = activityService.notifyActivityStartIn2Hours();
            return StrUtil.format("成功通知 {} 个用户活动即将开始", count);
        } catch (Exception e) {
            log.error("[ActivityStartNotifyJob][执行失败]", e);
            return "执行失败：" + e.getMessage();
        }
    }

}
