package cn.iocoder.yudao.module.gamification.controller.app.invitation;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gamification.controller.app.invitation.vo.AppInvitationRecordPageReqVO;
import cn.iocoder.yudao.module.gamification.controller.app.invitation.vo.AppInvitationRecordRespVO;
import cn.iocoder.yudao.module.gamification.service.invitation.InvitationRecordService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InvitationRecordControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private InvitationRecordController invitationRecordController;

    @Mock
    private InvitationRecordService invitationRecordService;

    @Test
    void getMyInvitationRecordPage_delegatesToServiceWithCurrentLoginUser() {
        AppInvitationRecordPageReqVO reqVO = new AppInvitationRecordPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);

        AppInvitationRecordRespVO item = new AppInvitationRecordRespVO();
        item.setInviteeNickname("小星");
        PageResult<AppInvitationRecordRespVO> pageResult = new PageResult<>(Collections.singletonList(item), 1L);
        when(invitationRecordService.getMyInvitationRecordPage(eq(88L), same(reqVO))).thenReturn(pageResult);

        try (MockedStatic<SecurityFrameworkUtils> securityFrameworkUtilsMock = mockStatic(SecurityFrameworkUtils.class)) {
            securityFrameworkUtilsMock.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(88L);

            CommonResult<PageResult<AppInvitationRecordRespVO>> result =
                    invitationRecordController.getMyInvitationRecordPage(reqVO);

            assertEquals(0, result.getCode());
            assertSame(pageResult, result.getData());
            verify(invitationRecordService).getMyInvitationRecordPage(eq(88L), same(reqVO));
        }
    }

    @Test
    void getMyInvitationRecordPage_declaresExpectedWebContractAnnotations() throws NoSuchMethodException {
        RequestMapping requestMapping = InvitationRecordController.class.getAnnotation(RequestMapping.class);
        Validated validated = InvitationRecordController.class.getAnnotation(Validated.class);
        assertNotNull(requestMapping);
        assertNotNull(validated);
        assertArrayEquals(new String[]{"/gamification/invitation/record"}, requestMapping.value());

        java.lang.reflect.Method method = InvitationRecordController.class
                .getDeclaredMethod("getMyInvitationRecordPage", AppInvitationRecordPageReqVO.class);
        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        PreAuthenticated preAuthenticated = method.getAnnotation(PreAuthenticated.class);
        Valid valid = method.getParameters()[0].getAnnotation(Valid.class);

        assertNotNull(getMapping);
        assertArrayEquals(new String[]{"/page"}, getMapping.value());
        assertNotNull(preAuthenticated);
        assertNotNull(valid);
    }

}
