package cn.iocoder.yudao.module.gamification.job;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.gamification.service.quiz.QuizSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class QuizAutoSubmitJob implements JobHandler {

    @Resource
    private QuizSessionService quizSessionService;

    @Override
    @TenantJob
    public String execute(String param) {
        int count = quizSessionService.autoSubmitExpiredAttempts();
        String result = StrUtil.format("Processed {} expired quiz attempts", count);
        log.info("[QuizAutoSubmitJob] {}", result);
        return result;
    }
}
