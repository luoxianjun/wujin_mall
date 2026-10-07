package cn.iocoder.yudao.module.gamification.service.quiz;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizQuestionBankPageReqVO;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizQuestionBankRespVO;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizQuestionBankSaveReqVO;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizQuestionImportRespVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface QuizQuestionBankService {

    Long createQuestionBank(QuizQuestionBankSaveReqVO createReqVO);

    void updateQuestionBank(QuizQuestionBankSaveReqVO updateReqVO);

    QuizQuestionBankRespVO getQuestionBank(Long id);

    PageResult<QuizQuestionBankRespVO> getQuestionBankPage(QuizQuestionBankPageReqVO pageReqVO);

    void downloadImportTemplate(HttpServletResponse response) throws IOException;

    QuizQuestionImportRespVO importQuestionBank(MultipartFile file) throws IOException;
}
