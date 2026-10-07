package cn.iocoder.yudao.module.gamification.controller.admin.quiz;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizActivityPageReqVO;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizActivityRespVO;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizActivitySaveReqVO;
import cn.iocoder.yudao.module.gamification.service.quiz.QuizActivityService;
import io.swagger.v3.oas.annotations.Operation;
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

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin - quiz activity")
@RestController
@RequestMapping("/gamification/quiz/activity")
@Validated
public class QuizActivityController {

    @Resource
    private QuizActivityService quizActivityService;

    @PostMapping("/create")
    @Operation(summary = "Create quiz activity config")
    @PreAuthorize("@ss.hasPermission('gamification:quiz:activity:create')")
    public CommonResult<Long> createQuizActivity(@Valid @RequestBody QuizActivitySaveReqVO createReqVO) {
        return success(quizActivityService.createQuizActivity(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "Update quiz activity config")
    @PreAuthorize("@ss.hasPermission('gamification:quiz:activity:update')")
    public CommonResult<Boolean> updateQuizActivity(@Valid @RequestBody QuizActivitySaveReqVO updateReqVO) {
        quizActivityService.updateQuizActivity(updateReqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "Get quiz activity config")
    @PreAuthorize("@ss.hasPermission('gamification:quiz:activity:query')")
    public CommonResult<QuizActivityRespVO> getQuizActivity(@RequestParam("id") Long id) {
        return success(quizActivityService.getQuizActivity(id));
    }

    @GetMapping("/page")
    @Operation(summary = "Get quiz activity page")
    @PreAuthorize("@ss.hasPermission('gamification:quiz:activity:query')")
    public CommonResult<PageResult<QuizActivityRespVO>> getQuizActivityPage(@Valid QuizActivityPageReqVO pageReqVO) {
        return success(quizActivityService.getQuizActivityPage(pageReqVO));
    }
}
