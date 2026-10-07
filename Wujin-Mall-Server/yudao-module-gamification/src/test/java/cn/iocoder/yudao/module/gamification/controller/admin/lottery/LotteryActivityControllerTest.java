package cn.iocoder.yudao.module.gamification.controller.admin.lottery;

import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gamification.controller.admin.lottery.vo.LotteryActivityPageReqVO;
import cn.iocoder.yudao.module.gamification.controller.admin.lottery.vo.LotteryPrizePageReqVO;
import cn.iocoder.yudao.module.forum.api.user.ForumUserProfileApi;
import cn.iocoder.yudao.module.forum.api.user.dto.ForumUserProfileDTO;
import cn.iocoder.yudao.module.gamification.controller.admin.lottery.vo.LotteryRecordExcelVO;
import cn.iocoder.yudao.module.gamification.controller.admin.lottery.vo.LotteryRecordPageReqVO;
import cn.iocoder.yudao.module.gamification.controller.admin.lottery.vo.LotteryRecordRespVO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryActivityDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryRecordDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.lottery.LotteryActivityMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.lottery.LotteryPrizeMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.lottery.LotteryRecordMapper;
import cn.iocoder.yudao.module.gamification.service.lottery.LotteryActivityService;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LotteryActivityControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private LotteryActivityController lotteryActivityController;

    @Mock
    private LotteryActivityService lotteryActivityService;
    @Mock
    private LotteryActivityMapper lotteryActivityMapper;
    @Mock
    private LotteryRecordMapper lotteryRecordMapper;
    @Mock
    private LotteryPrizeMapper lotteryPrizeMapper;
    @Mock
    private MemberUserApi memberUserApi;
    @Mock
    private ForumUserProfileApi forumUserProfileApi;

    @Test
    void getRecordPage_whenNoRecords_returnsEmptyPageWithoutBatchLookups() {
        LotteryRecordPageReqVO reqVO = new LotteryRecordPageReqVO();
        reqVO.setWon(true);
        when(lotteryRecordMapper.selectPage(eq(reqVO), any()))
                .thenReturn(new PageResult<>(Collections.emptyList(), 0L));

        CommonResult<PageResult<LotteryRecordRespVO>> result = lotteryActivityController.getRecordPage(reqVO);

        assertEquals(0, result.getCode());
        assertEquals(0L, result.getData().getTotal());
        assertTrue(result.getData().getList().isEmpty());
        verifyNoInteractions(memberUserApi, forumUserProfileApi, lotteryActivityMapper);
    }

    @Test
    void getRecordPage_enrichesUserUidFromForumProfile() throws Exception {
        LotteryRecordPageReqVO reqVO = new LotteryRecordPageReqVO();
        LotteryRecordDO record = LotteryRecordDO.builder()
                .id(36L)
                .lotteryActivityId(12L)
                .userId(1001L)
                .prizeName("一等奖")
                .won(true)
                .delivered(false)
                .build();
        when(lotteryRecordMapper.selectPage(eq(reqVO), any()))
                .thenReturn(new PageResult<>(Collections.singletonList(record), 1L));

        MemberUserRespDTO user = new MemberUserRespDTO();
        user.setId(1001L);
        user.setNickname("张三");
        when(memberUserApi.getUserMap(any()))
                .thenReturn(Collections.singletonMap(1001L, user));

        ForumUserProfileDTO profile = new ForumUserProfileDTO();
        profile.setUserId(1001L);
        profile.setUid("U123456");
        when(forumUserProfileApi.getUserProfileMapByUserIds(any()))
                .thenReturn(Collections.singletonMap(1001L, profile));

        LotteryActivityDO activity = new LotteryActivityDO();
        activity.setId(12L);
        activity.setActivityId(88L);
        when(lotteryActivityMapper.selectBatchIds(any()))
                .thenReturn(Collections.singletonList(activity));

        CommonResult<PageResult<LotteryRecordRespVO>> result = lotteryActivityController.getRecordPage(reqVO);

        assertEquals(0, result.getCode());
        LotteryRecordRespVO vo = result.getData().getList().get(0);
        Field uidField = LotteryRecordRespVO.class.getDeclaredField("uid");
        uidField.setAccessible(true);
        assertEquals("U123456", uidField.get(vo));
    }

    @Test
    void exportRecordExcelVo_declaresAndCopiesUserUid() throws Exception {
        Field uidField = LotteryRecordExcelVO.class.getDeclaredField("uid");
        ExcelProperty excelProperty = uidField.getAnnotation(ExcelProperty.class);
        assertNotNull(excelProperty);
        assertArrayEquals(new String[]{"UID"}, excelProperty.value());

        LotteryRecordRespVO record = new LotteryRecordRespVO();
        Field respUidField = LotteryRecordRespVO.class.getDeclaredField("uid");
        respUidField.setAccessible(true);
        respUidField.set(record, "U123456");

        Method method = LotteryActivityController.class.getDeclaredMethod("convertRecordToExcelVO", LotteryRecordRespVO.class);
        method.setAccessible(true);
        LotteryRecordExcelVO excelVO = (LotteryRecordExcelVO) method.invoke(lotteryActivityController, record);
        uidField.setAccessible(true);
        assertEquals("U123456", uidField.get(excelVO));
    }

    @Test
    void deliverRecord_marksWinningRecordAsDelivered() {
        LotteryRecordDO record = LotteryRecordDO.builder()
                .id(36L)
                .won(true)
                .delivered(false)
                .build();
        when(lotteryRecordMapper.selectById(36L)).thenReturn(record);

        CommonResult<Boolean> result = lotteryActivityController.deliverRecord(36L);

        assertEquals(0, result.getCode());
        assertEquals(Boolean.TRUE, result.getData());
        verify(lotteryRecordMapper).updateById(org.mockito.ArgumentMatchers.<LotteryRecordDO>argThat(updated ->
                updated != null &&
                        Long.valueOf(36L).equals(updated.getId()) &&
                        Boolean.TRUE.equals(updated.getDelivered())));
    }

    @Test
    void deliverRecord_declaresExpectedWebContractAnnotations() throws NoSuchMethodException {
        RequestMapping requestMapping = LotteryActivityController.class.getAnnotation(RequestMapping.class);
        assertNotNull(requestMapping);
        assertArrayEquals(new String[]{"/gamification/lottery"}, requestMapping.value());

        java.lang.reflect.Method method = LotteryActivityController.class
                .getDeclaredMethod("deliverRecord", Long.class);
        PutMapping putMapping = method.getAnnotation(PutMapping.class);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
        RequestParam requestParam = method.getParameters()[0].getAnnotation(RequestParam.class);

        assertNotNull(putMapping);
        assertArrayEquals(new String[]{"/record/deliver"}, putMapping.value());
        assertNotNull(preAuthorize);
        assertEquals("@ss.hasPermission('forum:lottery-record:deliver')", preAuthorize.value());
        assertNotNull(requestParam);
        assertEquals("id", requestParam.value());
    }

    @Test
    void listEndpoints_declareMenuBackedForumPermissions() throws NoSuchMethodException {
        assertPermission(
                "getLotteryActivityPage",
                new Class[]{LotteryActivityPageReqVO.class},
                "@ss.hasPermission('forum:lottery-activity:query')");
        assertPermission(
                "getPrizePage",
                new Class[]{LotteryPrizePageReqVO.class},
                "@ss.hasPermission('forum:lottery-prize:query')");
        assertPermission(
                "getRecordPage",
                new Class[]{LotteryRecordPageReqVO.class},
                "@ss.hasPermission('forum:lottery-record:query')");
    }

    @Test
    void exportRecord_declaresExpectedWebContractAnnotations() throws NoSuchMethodException {
        java.lang.reflect.Method method = LotteryActivityController.class
                .getDeclaredMethod("exportRecord", LotteryRecordPageReqVO.class, HttpServletResponse.class);
        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(getMapping);
        assertArrayEquals(new String[]{"/record/export"}, getMapping.value());
        assertNotNull(preAuthorize);
        assertEquals("@ss.hasPermission('forum:lottery-record:export')", preAuthorize.value());
    }

    private static void assertPermission(String methodName, Class<?>[] parameterTypes, String expected) throws NoSuchMethodException {
        java.lang.reflect.Method method = LotteryActivityController.class.getDeclaredMethod(methodName, parameterTypes);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
        assertNotNull(preAuthorize);
        assertEquals(expected, preAuthorize.value());
    }
}
