package cn.iocoder.yudao.module.gamification.controller.admin.quiz;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizQuestionBankPageReqVO;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizQuestionBankRespVO;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizQuestionBankSaveReqVO;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizQuestionImportRespVO;
import cn.iocoder.yudao.module.gamification.service.quiz.QuizQuestionBankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin - quiz question bank")
@RestController
@RequestMapping("/gamification/quiz/question-bank")
@Validated
public class QuizQuestionBankController {

    @Resource
    private QuizQuestionBankService quizQuestionBankService;

    @PostMapping("/create")
    @Operation(summary = "Create question bank")
    @PreAuthorize("@ss.hasPermission('gamification:quiz:question-bank:create')")
    public CommonResult<Long> createQuestionBank(@Valid @RequestBody QuizQuestionBankSaveReqVO createReqVO) {
        return success(quizQuestionBankService.createQuestionBank(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "Update question bank")
    @PreAuthorize("@ss.hasPermission('gamification:quiz:question-bank:update')")
    public CommonResult<Boolean> updateQuestionBank(@Valid @RequestBody QuizQuestionBankSaveReqVO updateReqVO) {
        quizQuestionBankService.updateQuestionBank(updateReqVO);
        return success(true);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "Download quiz question import template")
    @PreAuthorize("@ss.hasPermission('gamification:quiz:question-bank:import')")
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        quizQuestionBankService.downloadImportTemplate(response);
    }

    @PostMapping("/import")
    @Operation(summary = "Import question bank by Excel")
    @Parameter(name = "file", description = "Excel file", required = true)
    @PreAuthorize("@ss.hasPermission('gamification:quiz:question-bank:import')")
    public CommonResult<QuizQuestionImportRespVO> importQuestionBank(@RequestParam("file") MultipartFile file)
            throws Exception {
        return success(quizQuestionBankService.importQuestionBank(file));
    }

    @GetMapping("/get")
    @Operation(summary = "Get question bank")
    @PreAuthorize("@ss.hasPermission('gamification:quiz:question-bank:query')")
    public CommonResult<QuizQuestionBankRespVO> getQuestionBank(@RequestParam("id") Long id) {
        return success(quizQuestionBankService.getQuestionBank(id));
    }

    @GetMapping("/page")
    @Operation(summary = "Get question bank page")
    @PreAuthorize("@ss.hasPermission('gamification:quiz:question-bank:query')")
    public CommonResult<PageResult<QuizQuestionBankRespVO>> getQuestionBankPage(
            @Valid QuizQuestionBankPageReqVO pageReqVO) {
        return success(quizQuestionBankService.getQuestionBankPage(pageReqVO));
    }
}
