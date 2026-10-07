package cn.iocoder.yudao.module.gamification.service.lottery;

import cn.iocoder.yudao.module.forum.api.activity.ForumActivityApi;
import cn.iocoder.yudao.module.forum.api.activity.dto.ForumActivityParticipationConfigDTO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryActivityDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryActivityPrizeDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryPrizeDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryRecordDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.lottery.LotteryActivityMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.lottery.LotteryActivityPrizeMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.lottery.LotteryPrizeMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.lottery.LotteryRecordMapper;
import cn.iocoder.yudao.module.gamification.service.notification.GamificationNotificationService;
import cn.iocoder.yudao.module.gamification.service.points.GamificationPointsService;
import cn.iocoder.yudao.module.member.enums.point.MemberPointBizTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LotteryDrawServiceTest {

    @InjectMocks
    private LotteryDrawServiceImpl lotteryDrawService;

    @Mock
    private LotteryActivityMapper lotteryActivityMapper;
    @Mock
    private LotteryPrizeMapper lotteryPrizeMapper;
    @Mock
    private LotteryActivityPrizeMapper lotteryActivityPrizeMapper;
    @Mock
    private LotteryRecordMapper lotteryRecordMapper;
    @Mock
    private GamificationPointsService pointsService;
    @Mock
    private GamificationNotificationService notificationService;
    @Mock
    private ForumActivityApi forumActivityApi;

    private LotteryActivityDO activity;
    private LotteryPrizeDO pointsPrize;
    private LotteryPrizeDO thankYouPrize;
    private LotteryActivityPrizeDO pointsActivityPrize;
    private LotteryActivityPrizeDO thankYouActivityPrize;

    @BeforeEach
    void setUp() {
        activity = LotteryActivityDO.builder()
                .id(1L)
                .activityId(100L)
                .type(LotteryActivityDO.TYPE_INSTANT)
                .maxDrawsPerDay(3)
                .maxDrawsTotal(10)
                .costType(LotteryActivityDO.COST_FREE)
                .costAmount(0)
                .guaranteeDraws(5)
                .participationCondition(LotteryActivityDO.CONDITION_ALL)
                .status(0) // ENABLED
                .build();

        // Prizes are now independent — no lotteryActivityId or probability
        pointsPrize = LotteryPrizeDO.builder()
                .id(10L)
                .name("100积分")
                .type(LotteryPrizeDO.TYPE_POINTS)
                .value(100)
                .totalStock(10)
                .remainingStock(10)
                .sortOrder(1)
                .requireAddress(false)
                .build();

        thankYouPrize = LotteryPrizeDO.builder()
                .id(11L)
                .name("谢谢参与")
                .type(LotteryPrizeDO.TYPE_THANK_YOU)
                .value(0)
                .totalStock(999)
                .remainingStock(999)
                .sortOrder(2)
                .requireAddress(false)
                .build();

        // Junction table entries carry the probability
        pointsActivityPrize = LotteryActivityPrizeDO.builder()
                .id(1L)
                .lotteryActivityId(1L)
                .prizeId(10L)
                .probability(new BigDecimal("30"))
                .sortOrder(1)
                .build();

        thankYouActivityPrize = LotteryActivityPrizeDO.builder()
                .id(2L)
                .lotteryActivityId(1L)
                .prizeId(11L)
                .probability(new BigDecimal("70"))
                .sortOrder(2)
                .build();
    }

    private void mockStandardDrawSetup() {
        when(lotteryActivityMapper.selectById(1L)).thenReturn(activity);
        when(lotteryRecordMapper.countByUserAndActivityToday(eq(1L), eq(1L))).thenReturn(0L);
        when(lotteryRecordMapper.countByUserAndActivity(eq(1L), eq(1L))).thenReturn(0L);
        // Junction table query replaces direct prize query
        when(lotteryActivityPrizeMapper.selectByLotteryActivityId(1L))
                .thenReturn(Arrays.asList(pointsActivityPrize, thankYouActivityPrize));
        when(lotteryPrizeMapper.selectBatchIds(Arrays.asList(10L, 11L)))
                .thenReturn(Arrays.asList(pointsPrize, thankYouPrize));
        when(lotteryRecordMapper.countConsecutiveLosses(eq(1L), eq(1L))).thenReturn(0L);
        when(lotteryPrizeMapper.decrementStock(anyLong())).thenReturn(1);
        when(notificationService.sendNotification(anyLong(), anyString(), anyString(), anyString(), any())).thenReturn(true);
    }

    @Test
    void testDraw_successful() {
        // given
        mockStandardDrawSetup();

        // when
        LotteryRecordDO record = lotteryDrawService.draw(1L, 1L);

        // then
        assertNotNull(record);
        assertEquals(1L, record.getLotteryActivityId());
        assertEquals(1L, record.getUserId());
        assertNotNull(record.getDrawTime());
        verify(lotteryRecordMapper).insert(any(LotteryRecordDO.class));
    }

    @Test
    void testDraw_stockDepleted_returnsThankYou() {
        // given: physical prize has no stock
        LotteryPrizeDO depletedPrize = LotteryPrizeDO.builder()
                .id(10L)
                .name("大奖")
                .type(LotteryPrizeDO.TYPE_PHYSICAL)
                .totalStock(1)
                .remainingStock(0)
                .sortOrder(1)
                .requireAddress(true)
                .build();

        when(lotteryActivityMapper.selectById(1L)).thenReturn(activity);
        when(lotteryRecordMapper.countByUserAndActivityToday(eq(1L), eq(1L))).thenReturn(0L);
        when(lotteryRecordMapper.countByUserAndActivity(eq(1L), eq(1L))).thenReturn(0L);
        when(lotteryActivityPrizeMapper.selectByLotteryActivityId(1L))
                .thenReturn(Arrays.asList(pointsActivityPrize, thankYouActivityPrize));
        when(lotteryPrizeMapper.selectBatchIds(Arrays.asList(10L, 11L)))
                .thenReturn(Arrays.asList(depletedPrize, thankYouPrize));
        when(lotteryRecordMapper.countConsecutiveLosses(eq(1L), eq(1L))).thenReturn(0L);
        when(notificationService.sendNotification(anyLong(), anyString(), anyString(), anyString(), any())).thenReturn(true);

        // when
        LotteryRecordDO record = lotteryDrawService.draw(1L, 1L);

        // then: won a thank_you or the depleted prize was skipped
        assertNotNull(record);
        assertNotNull(record.getPrizeType());
    }

    @Test
    void testDraw_guaranteeMechanism() {
        // given: user has had 5 consecutive losses (guaranteeDraws=5)
        when(lotteryActivityMapper.selectById(1L)).thenReturn(activity);
        when(lotteryRecordMapper.countByUserAndActivityToday(eq(1L), eq(1L))).thenReturn(0L);
        when(lotteryRecordMapper.countByUserAndActivity(eq(1L), eq(1L))).thenReturn(5L);
        when(lotteryActivityPrizeMapper.selectByLotteryActivityId(1L))
                .thenReturn(Arrays.asList(pointsActivityPrize, thankYouActivityPrize));
        when(lotteryPrizeMapper.selectBatchIds(Arrays.asList(10L, 11L)))
                .thenReturn(Arrays.asList(pointsPrize, thankYouPrize));
        when(lotteryRecordMapper.countConsecutiveLosses(eq(1L), eq(1L))).thenReturn(5L); // trigger guarantee
        when(lotteryPrizeMapper.decrementStock(10L)).thenReturn(1);
        when(pointsService.grantPoints(eq(1L), eq(100), eq(MemberPointBizTypeEnum.LOTTERY_WIN.getType()), eq(1L), anyString())).thenReturn(true);
        when(notificationService.sendNotification(anyLong(), anyString(), anyString(), anyString(), any())).thenReturn(true);

        // when
        LotteryRecordDO record = lotteryDrawService.draw(1L, 1L);

        // then: guarantee triggered, user must win a non-thank-you prize
        assertNotNull(record);
        assertTrue(record.getWon());
        assertEquals("100积分", record.getPrizeName());
        verify(pointsService).grantPoints(eq(1L), eq(100), eq(MemberPointBizTypeEnum.LOTTERY_WIN.getType()), eq(1L), anyString());
    }

    @Test
    void testCanDraw_dailyLimitReached() {
        // given
        when(lotteryActivityMapper.selectById(1L)).thenReturn(activity);
        when(lotteryRecordMapper.countByUserAndActivityToday(eq(1L), eq(1L))).thenReturn(3L); // daily limit = 3

        // when
        boolean result = lotteryDrawService.canDraw(1L, 1L);

        // then
        assertFalse(result);
    }

    @Test
    void testCanDraw_totalLimitReached() {
        // given
        when(lotteryActivityMapper.selectById(1L)).thenReturn(activity);
        when(lotteryRecordMapper.countByUserAndActivityToday(eq(1L), eq(1L))).thenReturn(0L);
        when(lotteryRecordMapper.countByUserAndActivity(eq(1L), eq(1L))).thenReturn(10L); // total limit = 10

        // when
        boolean result = lotteryDrawService.canDraw(1L, 1L);

        // then
        assertFalse(result);
    }

    @Test
    void testCanDraw_enrolledCondition_requiresApprovedSignUp() {
        // given
        activity.setParticipationCondition(LotteryActivityDO.CONDITION_ENROLLED);
        when(lotteryActivityMapper.selectById(1L)).thenReturn(activity);
        when(forumActivityApi.isUserApprovedParticipated(100L, 1L)).thenReturn(false);

        // when
        boolean result = lotteryDrawService.canDraw(1L, 1L);

        // then
        assertFalse(result);
        verify(forumActivityApi).isUserApprovedParticipated(100L, 1L);
        verifyNoInteractions(pointsService);
    }

    @Test
    void testCanDraw_enrolledCondition_approvedSignUpCanDraw() {
        // given
        activity.setParticipationCondition(LotteryActivityDO.CONDITION_ENROLLED);
        when(lotteryActivityMapper.selectById(1L)).thenReturn(activity);
        when(forumActivityApi.isUserApprovedParticipated(100L, 1L)).thenReturn(true);
        when(lotteryRecordMapper.countByUserAndActivityToday(eq(1L), eq(1L))).thenReturn(0L);
        when(lotteryRecordMapper.countByUserAndActivity(eq(1L), eq(1L))).thenReturn(0L);

        // when
        boolean result = lotteryDrawService.canDraw(1L, 1L);

        // then
        assertTrue(result);
        verify(forumActivityApi).isUserApprovedParticipated(100L, 1L);
    }

    @Test
    void testCanDraw_linkedActivityWithApprovalRequired_requiresApprovedSignUp() {
        // given
        ForumActivityParticipationConfigDTO participationConfig = new ForumActivityParticipationConfigDTO();
        participationConfig.setSignUpRequired(true);
        participationConfig.setApprovalRequired(true);
        when(lotteryActivityMapper.selectById(1L)).thenReturn(activity);
        when(forumActivityApi.getParticipationConfig(100L)).thenReturn(participationConfig);
        when(forumActivityApi.isUserApprovedParticipated(100L, 1L)).thenReturn(false);

        // when
        boolean result = lotteryDrawService.canDraw(1L, 1L);

        // then
        assertFalse(result);
        verify(forumActivityApi).getParticipationConfig(100L);
        verify(forumActivityApi).isUserApprovedParticipated(100L, 1L);
    }

    @Test
    void testDraw_enrolledCondition_withoutApprovedSignUpThrows() {
        // given
        activity.setParticipationCondition(LotteryActivityDO.CONDITION_ENROLLED);
        when(lotteryActivityMapper.selectById(1L)).thenReturn(activity);
        when(forumActivityApi.isUserApprovedParticipated(100L, 1L)).thenReturn(false);

        // when
        RuntimeException exception = assertThrows(RuntimeException.class, () -> lotteryDrawService.draw(1L, 1L));

        // then
        assertEquals("不满足抽奖条件", exception.getMessage());
        verify(forumActivityApi).isUserApprovedParticipated(100L, 1L);
        verify(lotteryRecordMapper, never()).insert(any(LotteryRecordDO.class));
    }

    @Test
    void testDraw_linkedActivityWithApprovalRequired_withoutApprovedSignUpThrows() {
        // given
        ForumActivityParticipationConfigDTO participationConfig = new ForumActivityParticipationConfigDTO();
        participationConfig.setSignUpRequired(true);
        participationConfig.setApprovalRequired(true);
        when(lotteryActivityMapper.selectById(1L)).thenReturn(activity);
        when(forumActivityApi.getParticipationConfig(100L)).thenReturn(participationConfig);
        when(forumActivityApi.isUserApprovedParticipated(100L, 1L)).thenReturn(false);

        // when
        RuntimeException exception = assertThrows(RuntimeException.class, () -> lotteryDrawService.draw(1L, 1L));

        // then
        assertEquals("不满足抽奖条件", exception.getMessage());
        verify(forumActivityApi).getParticipationConfig(100L);
        verify(forumActivityApi).isUserApprovedParticipated(100L, 1L);
        verify(lotteryRecordMapper, never()).insert(any(LotteryRecordDO.class));
    }

    @Test
    void testDraw_pointsAutoDistribution() {
        // given: user wins a points prize
        when(lotteryActivityMapper.selectById(1L)).thenReturn(activity);
        when(lotteryRecordMapper.countByUserAndActivityToday(eq(1L), eq(1L))).thenReturn(0L);
        when(lotteryRecordMapper.countByUserAndActivity(eq(1L), eq(1L))).thenReturn(0L);
        // Only points prize available via junction table
        when(lotteryActivityPrizeMapper.selectByLotteryActivityId(1L))
                .thenReturn(Collections.singletonList(pointsActivityPrize));
        when(lotteryPrizeMapper.selectBatchIds(Collections.singletonList(10L)))
                .thenReturn(Collections.singletonList(pointsPrize));
        when(lotteryRecordMapper.countConsecutiveLosses(eq(1L), eq(1L))).thenReturn(0L);
        when(lotteryPrizeMapper.decrementStock(10L)).thenReturn(1);
        when(pointsService.grantPoints(eq(1L), eq(100), eq(MemberPointBizTypeEnum.LOTTERY_WIN.getType()), eq(1L), anyString())).thenReturn(true);
        when(notificationService.sendNotification(anyLong(), anyString(), anyString(), anyString(), any())).thenReturn(true);

        // when
        LotteryRecordDO record = lotteryDrawService.draw(1L, 1L);

        // then
        assertNotNull(record);
        assertTrue(record.getWon());
        assertTrue(record.getDelivered());
        verify(pointsService).grantPoints(eq(1L), eq(100), eq(MemberPointBizTypeEnum.LOTTERY_WIN.getType()), eq(1L), anyString());
    }

    @Test
    void testCanDraw_disabledActivity() {
        // given
        activity.setStatus(1); // DISABLED
        when(lotteryActivityMapper.selectById(1L)).thenReturn(activity);

        // when
        boolean result = lotteryDrawService.canDraw(1L, 1L);

        // then
        assertFalse(result);
    }

    @Test
    void testDraw_pointsCostDeduction() {
        // given: activity requires 50 points to draw
        activity.setCostType(LotteryActivityDO.COST_POINTS);
        activity.setCostAmount(50);

        when(lotteryActivityMapper.selectById(1L)).thenReturn(activity);
        when(lotteryRecordMapper.countByUserAndActivityToday(eq(1L), eq(1L))).thenReturn(0L);
        when(lotteryRecordMapper.countByUserAndActivity(eq(1L), eq(1L))).thenReturn(0L);
        when(pointsService.getUserPoints(1L)).thenReturn(100); // enough points
        when(pointsService.deductPoints(eq(1L), eq(50), eq(MemberPointBizTypeEnum.LOTTERY_COST.getType()), eq(1L), anyString())).thenReturn(true);
        when(lotteryActivityPrizeMapper.selectByLotteryActivityId(1L))
                .thenReturn(Arrays.asList(pointsActivityPrize, thankYouActivityPrize));
        when(lotteryPrizeMapper.selectBatchIds(Arrays.asList(10L, 11L)))
                .thenReturn(Arrays.asList(pointsPrize, thankYouPrize));
        when(lotteryRecordMapper.countConsecutiveLosses(eq(1L), eq(1L))).thenReturn(0L);
        when(lotteryPrizeMapper.decrementStock(anyLong())).thenReturn(1);
        when(notificationService.sendNotification(anyLong(), anyString(), anyString(), anyString(), any())).thenReturn(true);

        // when
        LotteryRecordDO record = lotteryDrawService.draw(1L, 1L);

        // then: points should be deducted
        assertNotNull(record);
        verify(pointsService).deductPoints(eq(1L), eq(50), eq(MemberPointBizTypeEnum.LOTTERY_COST.getType()), eq(1L), anyString());
    }
}
