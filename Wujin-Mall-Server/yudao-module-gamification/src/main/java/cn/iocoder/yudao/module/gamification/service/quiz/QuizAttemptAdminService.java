package cn.iocoder.yudao.module.gamification.service.quiz;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizAttemptExportData;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizAttemptPageReqVO;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizAttemptRespVO;

public interface QuizAttemptAdminService {

    PageResult<QuizAttemptRespVO> getQuizAttemptPage(QuizAttemptPageReqVO pageReqVO);

    QuizAttemptExportData getQuizAttemptExportData(QuizAttemptPageReqVO pageReqVO);
}
