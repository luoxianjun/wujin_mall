package cn.iocoder.yudao.module.gamification.job;

import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryActivityDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.lottery.LotteryActivityMapper;
import cn.iocoder.yudao.module.gamification.service.lottery.LotteryDrawService;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LotteryScheduledDrawJobTest {

    @InjectMocks
    private LotteryScheduledDrawJob job;

    @Mock
    private LotteryActivityMapper activityMapper;
    @Mock
    private LotteryDrawService drawService;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    void testExecute_scheduledActivity_executesDraw() {
        // given: a scheduled activity past its draw time
        LotteryActivityDO activity = LotteryActivityDO.builder()
                .id(1L)
                .activityId(100L)
                .type(LotteryActivityDO.TYPE_SCHEDULED)
                .drawTime(LocalDateTime.now().minusMinutes(5))
                .status(0)
                .build();
        activity.setTenantId(1L);

        when(activityMapper.selectList(any())).thenReturn(Collections.singletonList(activity));
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // when
        job.execute();

        // then — verify draw was triggered
        verify(drawService).executeScheduledDraw(1L);
    }

    @Test
    void testExecute_scheduledActivity_executesDrawWithActivityTenantContext() {
        // given: a scheduled activity belongs to tenant 1
        LotteryActivityDO activity = LotteryActivityDO.builder()
                .id(1L)
                .activityId(100L)
                .type(LotteryActivityDO.TYPE_SCHEDULED)
                .drawTime(LocalDateTime.now().minusMinutes(5))
                .status(0)
                .build();
        activity.setTenantId(1L);

        when(activityMapper.selectList(any())).thenReturn(Collections.singletonList(activity));
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doAnswer(invocation -> {
            assertEquals(1L, TenantContextHolder.getTenantId());
            assertFalse(TenantContextHolder.isIgnore());
            return null;
        }).when(drawService).executeScheduledDraw(1L);

        // when
        job.execute();

        // then: scheduled draw uses tenant-scoped DB writes so later app/admin queries can see records
        verify(drawService).executeScheduledDraw(1L);
    }

    @Test
    void testExecute_noActivities_noop() {
        // given: no scheduled activities
        when(activityMapper.selectList(any())).thenReturn(Collections.emptyList());

        // when
        job.execute();

        // then
        verify(drawService, never()).executeScheduledDraw(anyLong());
    }
}
