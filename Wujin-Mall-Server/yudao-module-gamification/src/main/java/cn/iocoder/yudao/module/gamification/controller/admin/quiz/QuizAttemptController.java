package cn.iocoder.yudao.module.gamification.controller.admin.quiz;

import cn.idev.excel.FastExcelFactory;
import cn.idev.excel.converters.longconverter.LongStringConverter;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.excel.core.handler.ColumnWidthMatchStyleStrategy;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizAttemptExportData;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizAttemptPageReqVO;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizAttemptRespVO;
import cn.iocoder.yudao.module.gamification.service.quiz.QuizAttemptAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin - quiz attempt records")
@RestController
@RequestMapping("/gamification/quiz/attempt")
@Validated
public class QuizAttemptController {

    @Resource
    private QuizAttemptAdminService quizAttemptAdminService;

    @GetMapping("/page")
    @Operation(summary = "Get quiz attempt page")
    @PreAuthorize("@ss.hasPermission('gamification:quiz:activity:query')")
    public CommonResult<PageResult<QuizAttemptRespVO>> getQuizAttemptPage(@Valid QuizAttemptPageReqVO pageReqVO) {
        return success(quizAttemptAdminService.getQuizAttemptPage(pageReqVO));
    }

    @GetMapping("/export")
    @Operation(summary = "Export quiz attempt records")
    @PreAuthorize("@ss.hasPermission('gamification:quiz:activity:query')")
    public void exportQuizAttempt(@Valid QuizAttemptPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        QuizAttemptExportData exportData = quizAttemptAdminService.getQuizAttemptExportData(pageReqVO);
        FastExcelFactory.write(response.getOutputStream())
                .head(exportData.getHead())
                .autoCloseStream(false)
                .registerWriteHandler(new ColumnWidthMatchStyleStrategy())
                .registerConverter(new LongStringConverter())
                .sheet("答题记录")
                .doWrite(exportData.getRows());
        response.addHeader("Content-Disposition", "attachment;filename=" + HttpUtils.encodeUtf8("答题记录.xlsx"));
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
    }
}
