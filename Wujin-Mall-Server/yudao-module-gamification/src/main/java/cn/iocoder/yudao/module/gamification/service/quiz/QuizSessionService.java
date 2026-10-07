package cn.iocoder.yudao.module.gamification.service.quiz;

import cn.iocoder.yudao.module.gamification.controller.app.quiz.vo.AppQuizHeartbeatReqVO;
import cn.iocoder.yudao.module.gamification.controller.app.quiz.vo.AppQuizResultRespVO;
import cn.iocoder.yudao.module.gamification.controller.app.quiz.vo.AppQuizStartReqVO;
import cn.iocoder.yudao.module.gamification.controller.app.quiz.vo.AppQuizStartRespVO;
import cn.iocoder.yudao.module.gamification.controller.app.quiz.vo.AppQuizSubmitReqVO;

public interface QuizSessionService {

    AppQuizStartRespVO startQuiz(Long userId, AppQuizStartReqVO reqVO);

    Boolean heartbeatQuiz(Long userId, AppQuizHeartbeatReqVO reqVO);

    AppQuizResultRespVO submitQuiz(Long userId, AppQuizSubmitReqVO reqVO);

    AppQuizResultRespVO getQuizResult(Long userId, Long quizActivityId, Integer attemptNo);

    int autoSubmitExpiredAttempts();
}
