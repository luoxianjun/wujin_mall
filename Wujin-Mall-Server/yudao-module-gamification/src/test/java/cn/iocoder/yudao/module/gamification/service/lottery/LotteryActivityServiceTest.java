package cn.iocoder.yudao.module.gamification.service.lottery;

import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryActivityDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryActivityPrizeDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryPrizeDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.lottery.LotteryActivityMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.lottery.LotteryActivityPrizeMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.lottery.LotteryPrizeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LotteryActivityServiceTest {

    @InjectMocks
    private LotteryActivityServiceImpl activityService;

    @Mock
    private LotteryActivityMapper activityMapper;
    @Mock
    private LotteryPrizeMapper prizeMapper;
    @Mock
    private LotteryActivityPrizeMapper activityPrizeMapper;

    @Test
    void testCreateLotteryActivity() {
        // given
        LotteryActivityDO activity = LotteryActivityDO.builder()
                .activityId(100L)
                .type(0) // SCHEDULED
                .costType(0)
                .status(0)
                .build();

        // when
        activityService.createLotteryActivity(activity);

        // then
        verify(activityMapper).insert(activity);
    }

    @Test
    void testCreatePrize_independent() {
        // given: prize is now independent — no activityId or probability
        LotteryPrizeDO prize = LotteryPrizeDO.builder()
                .name("100积分")
                .type(LotteryPrizeDO.TYPE_POINTS)
                .value(100)
                .totalStock(50)
                .remainingStock(50)
                .sortOrder(1)
                .requireAddress(false)
                .build();

        // when
        activityService.createPrize(prize);

        // then
        verify(prizeMapper).insert(prize);
    }

    @Test
    void testDeletePrize() {
        // when
        activityService.deletePrize(10L);

        // then
        verify(prizeMapper).deleteById(10L);
    }

    @Test
    void testSaveLotteryActivityPrizes() {
        // given
        Long activityId = 1L;
        LotteryActivityPrizeDO ap1 = LotteryActivityPrizeDO.builder()
                .prizeId(10L)
                .probability(new BigDecimal("30"))
                .sortOrder(1)
                .build();
        LotteryActivityPrizeDO ap2 = LotteryActivityPrizeDO.builder()
                .prizeId(11L)
                .probability(new BigDecimal("70"))
                .sortOrder(2)
                .build();

        // when
        activityService.saveLotteryActivityPrizes(activityId, Arrays.asList(ap1, ap2));

        // then: old associations deleted, new ones inserted
        verify(activityPrizeMapper).deleteByLotteryActivityId(activityId);
        verify(activityPrizeMapper, times(2)).insert(any(LotteryActivityPrizeDO.class));
        assertEquals(activityId, ap1.getLotteryActivityId());
        assertEquals(activityId, ap2.getLotteryActivityId());
    }

    @Test
    void testGetActivityPrizes() {
        // given
        LotteryActivityPrizeDO ap1 = LotteryActivityPrizeDO.builder()
                .id(1L).lotteryActivityId(1L).prizeId(10L).probability(new BigDecimal("30")).build();
        LotteryActivityPrizeDO ap2 = LotteryActivityPrizeDO.builder()
                .id(2L).lotteryActivityId(1L).prizeId(11L).probability(new BigDecimal("70")).build();
        when(activityPrizeMapper.selectByLotteryActivityId(1L)).thenReturn(Arrays.asList(ap1, ap2));

        // when
        List<LotteryActivityPrizeDO> result = activityService.getActivityPrizes(1L);

        // then
        assertEquals(2, result.size());
        assertEquals(10L, result.get(0).getPrizeId());
    }
}
