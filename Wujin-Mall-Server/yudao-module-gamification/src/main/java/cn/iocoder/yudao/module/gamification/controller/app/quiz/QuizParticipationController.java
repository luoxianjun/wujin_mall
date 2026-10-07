package cn.iocoder.yudao.module.gamification.controller.app.quiz;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.gamification.controller.app.quiz.vo.AppQuizHeartbeatReqVO;
import cn.iocoder.yudao.module.gamification.controller.app.quiz.vo.AppQuizLeaderboardRespVO;
import cn.iocoder.yudao.module.gamification.controller.app.quiz.vo.AppQuizResultRespVO;
import cn.iocoder.yudao.module.gamification.controller.app.quiz.vo.AppQuizStartReqVO;
import cn.iocoder.yudao.module.gamification.controller.app.quiz.vo.AppQuizStartRespVO;
import cn.iocoder.yudao.module.gamification.controller.app.quiz.vo.AppQuizSubmitReqVO;
import cn.iocoder.yudao.module.gamification.service.quiz.QuizLeaderboardService;
import cn.iocoder.yudao.module.gamification.service.quiz.QuizSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "App - quiz participation")
@RestController
@RequestMapping("/gamification/quiz")
@Validated
public class QuizParticipationController {

    @Resource
    private QuizSessionService quizSessionService;

    @Resource
    private QuizLeaderboardService quizLeaderboardService;

    @PostMapping("/start")
    @Operation(summary = "Start quiz")
    @PreAuthenticated
    public CommonResult<AppQuizStartRespVO> startQuiz(@Valid @RequestBody AppQuizStartReqVO reqVO) {
        return success(quizSessionService.startQuiz(SecurityFrameworkUtils.getLoginUserId(), reqVO));
    }

    @PostMapping("/heartbeat")
    @Operation(summary = "Quiz heartbeat")
    @PreAuthenticated
    public CommonResult<Boolean> heartbeatQuiz(@Valid @RequestBody AppQuizHeartbeatReqVO reqVO) {
        return success(quizSessionService.heartbeatQuiz(SecurityFrameworkUtils.getLoginUserId(), reqVO));
    }

    @PostMapping("/submit")
    @Operation(summary = "Submit quiz")
    @PreAuthenticated
    public CommonResult<AppQuizResultRespVO> submitQuiz(@Valid @RequestBody AppQuizSubmitReqVO reqVO) {
        return success(quizSessionService.submitQuiz(SecurityFrameworkUtils.getLoginUserId(), reqVO));
    }

    @GetMapping("/result")
    @Operation(summary = "Get quiz result")
    @PreAuthenticated
    public CommonResult<AppQuizResultRespVO> getQuizResult(
            @RequestParam("quizActivityId") Long quizActivityId,
            @RequestParam(value = "attemptNo", required = false) Integer attemptNo) {
        return success(quizSessionService.getQuizResult(
                SecurityFrameworkUtils.getLoginUserId(), quizActivityId, attemptNo));
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "Get quiz leaderboard")
    @PreAuthenticated
    public CommonResult<AppQuizLeaderboardRespVO> getQuizLeaderboard(
            @RequestParam("quizActivityId") Long quizActivityId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(quizLeaderboardService.getLeaderboard(quizActivityId, userId));
    }
}
