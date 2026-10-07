package cn.iocoder.yudao.module.gamification.service.quiz;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizActivityPageReqVO;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizActivityRespVO;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizActivitySaveReqVO;

public interface QuizActivityService {

    Long createQuizActivity(QuizActivitySaveReqVO createReqVO);

    void updateQuizActivity(QuizActivitySaveReqVO updateReqVO);

    QuizActivityRespVO getQuizActivity(Long id);

    PageResult<QuizActivityRespVO> getQuizActivityPage(QuizActivityPageReqVO pageReqVO);
}
