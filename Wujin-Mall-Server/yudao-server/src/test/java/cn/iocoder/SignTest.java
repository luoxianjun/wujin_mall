package cn.iocoder;

import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.forum.service.sign.ForumSignService;
import cn.iocoder.yudao.server.YudaoServerApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import javax.annotation.Resource;

/**
 * 签到服务的简单调用示例。
 *
 * 如需集成测试，可改为 @SpringBootTest 注入真实 Bean。
 */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = YudaoServerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SignTest {

    @Resource
    private ForumSignService forumSignService;

    @Test
    void testSignCall() {

        TenantContextHolder.setTenantId(1L);
        Long userId = 2L;

        LocalDate startDate = LocalDate.of(2025, 11, 10);
        LocalDate endDate = LocalDate.of(2025, 11, 23);

        LocalDate date = startDate;
        while (date.isBefore(endDate)) {
            log.info("签到日期{}", date.toString());
            forumSignService.sign(userId, true, date);
            date = date.plusDays(1);
        }

    }

}
