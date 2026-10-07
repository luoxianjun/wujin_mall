package cn.iocoder.yudao.module.gamification.controller.admin.quiz;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizAttemptExportData;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizAttemptPageReqVO;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizAttemptRespVO;
import cn.iocoder.yudao.module.gamification.service.quiz.QuizAttemptAdminService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QuizAttemptControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private QuizAttemptController quizAttemptController;

    @Mock
    private QuizAttemptAdminService quizAttemptAdminService;

    @Test
    void getQuizAttemptPage_delegatesToService() {
        QuizAttemptPageReqVO reqVO = new QuizAttemptPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);

        QuizAttemptRespVO item = new QuizAttemptRespVO();
        item.setId(101L);
        PageResult<QuizAttemptRespVO> pageResult = new PageResult<>(Collections.singletonList(item), 1L);
        when(quizAttemptAdminService.getQuizAttemptPage(same(reqVO))).thenReturn(pageResult);

        CommonResult<PageResult<QuizAttemptRespVO>> result = quizAttemptController.getQuizAttemptPage(reqVO);

        assertEquals(0, result.getCode());
        assertSame(pageResult, result.getData());
        verify(quizAttemptAdminService).getQuizAttemptPage(same(reqVO));
    }

    @Test
    void getQuizAttemptPage_declaresExpectedWebContractAnnotations() throws NoSuchMethodException {
        RequestMapping requestMapping = QuizAttemptController.class.getAnnotation(RequestMapping.class);
        Validated validated = QuizAttemptController.class.getAnnotation(Validated.class);
        assertNotNull(requestMapping);
        assertNotNull(validated);
        assertArrayEquals(new String[]{"/gamification/quiz/attempt"}, requestMapping.value());

        java.lang.reflect.Method method = QuizAttemptController.class
                .getDeclaredMethod("getQuizAttemptPage", QuizAttemptPageReqVO.class);
        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
        Valid valid = method.getParameters()[0].getAnnotation(Valid.class);

        assertNotNull(getMapping);
        assertArrayEquals(new String[]{"/page"}, getMapping.value());
        assertNotNull(preAuthorize);
        assertEquals("@ss.hasPermission('gamification:quiz:activity:query')", preAuthorize.value());
        assertNotNull(valid);
    }

    @Test
    void exportQuizAttempt_declaresExpectedWebContractAnnotations() throws NoSuchMethodException {
        java.lang.reflect.Method method = QuizAttemptController.class
                .getDeclaredMethod("exportQuizAttempt", QuizAttemptPageReqVO.class, HttpServletResponse.class);
        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
        Valid valid = method.getParameters()[0].getAnnotation(Valid.class);

        assertNotNull(getMapping);
        assertArrayEquals(new String[]{"/export"}, getMapping.value());
        assertNotNull(preAuthorize);
        assertEquals("@ss.hasPermission('gamification:quiz:activity:query')", preAuthorize.value());
        assertNotNull(valid);
    }

    @Test
    void exportQuizAttempt_delegatesToService() throws Exception {
        QuizAttemptPageReqVO reqVO = new QuizAttemptPageReqVO();
        HttpServletResponse response = org.mockito.Mockito.mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenReturn(org.mockito.Mockito.mock(javax.servlet.ServletOutputStream.class));
        QuizAttemptExportData exportData = new QuizAttemptExportData(
                Collections.singletonList(Collections.singletonList("答题记录ID")),
                Collections.singletonList(Collections.singletonList(1L)));
        when(quizAttemptAdminService.getQuizAttemptExportData(same(reqVO))).thenReturn(exportData);

        quizAttemptController.exportQuizAttempt(reqVO, response);

        verify(quizAttemptAdminService).getQuizAttemptExportData(same(reqVO));
    }
}
