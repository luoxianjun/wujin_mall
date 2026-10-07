package cn.iocoder.yudao.module.gamification.controller.app.lottery;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.forum.api.activity.ForumActivityApi;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryRecordDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.lottery.LotteryPrizeMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.lottery.LotteryRecordMapper;
import cn.iocoder.yudao.module.gamification.service.lottery.LotteryActivityService;
import cn.iocoder.yudao.module.gamification.service.lottery.LotteryDrawService;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppLotteryActivityControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private AppLotteryActivityController appLotteryActivityController;

    @Mock
    private LotteryDrawService lotteryDrawService;
    @Mock
    private LotteryActivityService lotteryActivityService;
    @Mock
    private LotteryPrizeMapper lotteryPrizeMapper;
    @Mock
    private LotteryRecordMapper lotteryRecordMapper;
    @Mock
    private MemberUserApi memberUserApi;
    @Mock
    private ForumActivityApi forumActivityApi;

    @Test
    void submitAddress_updatesUndeliveredRecordForCurrentUser() {
        LotteryRecordDO record = LotteryRecordDO.builder()
                .id(36L)
                .userId(88L)
                .delivered(false)
                .deliveryAddress("旧地址")
                .build();
        when(lotteryRecordMapper.selectById(36L)).thenReturn(record);

        try (MockedStatic<SecurityFrameworkUtils> securityFrameworkUtilsMock = mockStatic(SecurityFrameworkUtils.class)) {
            securityFrameworkUtilsMock.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(88L);

            CommonResult<Boolean> result = appLotteryActivityController.submitAddress(36L, "新地址");

            assertEquals(0, result.getCode());
            assertEquals(Boolean.TRUE, result.getData());
            verify(lotteryRecordMapper).updateById(org.mockito.ArgumentMatchers.<LotteryRecordDO>argThat(updated ->
                    updated != null &&
                            Long.valueOf(36L).equals(updated.getId()) &&
                            "新地址".equals(updated.getDeliveryAddress())));
        }
    }

    @Test
    void submitAddress_rejectsDeliveredRecordUpdates() {
        LotteryRecordDO record = LotteryRecordDO.builder()
                .id(36L)
                .userId(88L)
                .delivered(true)
                .deliveryAddress("旧地址")
                .build();
        when(lotteryRecordMapper.selectById(36L)).thenReturn(record);

        try (MockedStatic<SecurityFrameworkUtils> securityFrameworkUtilsMock = mockStatic(SecurityFrameworkUtils.class)) {
            securityFrameworkUtilsMock.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(88L);

            CommonResult<Boolean> result = appLotteryActivityController.submitAddress(36L, "新地址");

            assertEquals(400, result.getCode());
            assertEquals("奖品已发放，无法修改地址", result.getMsg());
            verify(lotteryRecordMapper, never()).updateById(org.mockito.ArgumentMatchers.any(LotteryRecordDO.class));
        }
    }

    @Test
    void submitAddress_declaresExpectedWebContractAnnotations() throws NoSuchMethodException {
        RequestMapping requestMapping = AppLotteryActivityController.class.getAnnotation(RequestMapping.class);
        Validated validated = AppLotteryActivityController.class.getAnnotation(Validated.class);
        assertNotNull(requestMapping);
        assertNotNull(validated);
        assertArrayEquals(new String[]{"/gamification/lottery"}, requestMapping.value());

        java.lang.reflect.Method method = AppLotteryActivityController.class
                .getDeclaredMethod("submitAddress", Long.class, String.class);
        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        RequestParam recordId = method.getParameters()[0].getAnnotation(RequestParam.class);
        RequestParam address = method.getParameters()[1].getAnnotation(RequestParam.class);

        assertNotNull(postMapping);
        assertArrayEquals(new String[]{"/activity/submit-address"}, postMapping.value());
        assertNotNull(recordId);
        assertEquals("recordId", recordId.value());
        assertNotNull(address);
        assertEquals("address", address.value());
    }
}
