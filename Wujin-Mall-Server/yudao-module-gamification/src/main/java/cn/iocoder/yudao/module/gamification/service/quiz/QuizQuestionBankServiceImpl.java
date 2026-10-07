package cn.iocoder.yudao.module.gamification.service.quiz;

import cn.hutool.core.collection.CollUtil;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizQuestionBankPageReqVO;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizQuestionBankRespVO;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizQuestionBankSaveReqVO;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizQuestionImportRespVO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizQuestionBankDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizQuestionDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizQuestionOptionDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizQuestionBankMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizQuestionMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizQuestionOptionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuizQuestionBankServiceImpl implements QuizQuestionBankService {

    @Resource
    private QuizQuestionBankMapper quizQuestionBankMapper;

    @Resource
    private QuizQuestionMapper quizQuestionMapper;

    @Resource
    private QuizQuestionOptionMapper quizQuestionOptionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createQuestionBank(QuizQuestionBankSaveReqVO createReqVO) {
        QuizQuestionBankDO questionBank = QuizQuestionBankDO.builder()
                .name(createReqVO.getName())
                .description(createReqVO.getDescription())
                .enabled(createReqVO.getEnabled() == null || createReqVO.getEnabled())
                .build();
        quizQuestionBankMapper.insert(questionBank);
        saveQuestions(questionBank.getId(), createReqVO.getQuestions());
        return questionBank.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuestionBank(QuizQuestionBankSaveReqVO updateReqVO) {
        if (updateReqVO.getId() == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        QuizQuestionBankDO existing = quizQuestionBankMapper.selectById(updateReqVO.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Question bank not found, id=" + updateReqVO.getId());
        }
        QuizQuestionBankDO updateObj = QuizQuestionBankDO.builder()
                .id(updateReqVO.getId())
                .name(updateReqVO.getName())
                .description(updateReqVO.getDescription())
                .enabled(updateReqVO.getEnabled() == null ? existing.getEnabled() : updateReqVO.getEnabled())
                .build();
        quizQuestionBankMapper.updateById(updateObj);
        replaceQuestions(updateReqVO.getId(), updateReqVO.getQuestions());
    }

    @Override
    public QuizQuestionBankRespVO getQuestionBank(Long id) {
        QuizQuestionBankDO questionBank = quizQuestionBankMapper.selectById(id);
        return questionBank == null ? null : buildQuestionBankRespVO(questionBank);
    }

    @Override
    public PageResult<QuizQuestionBankRespVO> getQuestionBankPage(QuizQuestionBankPageReqVO pageReqVO) {
        PageResult<QuizQuestionBankDO> pageResult = quizQuestionBankMapper.selectPage(pageReqVO);
        List<QuizQuestionBankRespVO> list = pageResult.getList().stream()
                .map(this::buildQuestionBankRespVO)
                .collect(Collectors.toList());
        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        List<QuizQuestionImportExcelVO> rows = Arrays.asList(
                createTemplateRow("校园知识竞赛", "SINGLE_CHOICE", "学校图书馆周末开放到几点？",
                        null, 5, "18:00", "20:00", "21:00", "22:00", "B",
                        "单选题请在 correctAnswers 中填写唯一正确选项，例如 A"),
                createTemplateRow("校园知识竞赛", "MULTIPLE_CHOICE", "以下哪些属于校园常见志愿服务项目？",
                        null, 10, "迎新接待", "图书整理", "食堂值班", "社区支教", "A,B,D",
                        "多选题请用英文逗号分隔多个正确答案，例如 A,B,D"),
                createTemplateRow("校园知识竞赛", "TRUE_FALSE", "网络连接失败会导致本次答题作废。",
                        null, 5, null, null, null, null, "TRUE",
                        "判断题的 correctAnswers 只能填写 TRUE 或 FALSE"));
        ExcelUtils.write(response, "答题题库导入模板.xls", "题库题目", QuizQuestionImportExcelVO.class, rows);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuizQuestionImportRespVO importQuestionBank(MultipartFile file) throws IOException {
        List<QuizQuestionImportExcelVO> rows = ExcelUtils.read(file, QuizQuestionImportExcelVO.class);
        int successCount = 0;
        int failureCount = 0;
        List<String> failureMessages = new ArrayList<>();
        Map<String, Integer> bankSortMap = new LinkedHashMap<>();
        for (int i = 0; i < rows.size(); i++) {
            QuizQuestionImportExcelVO row = rows.get(i);
            int rowNum = i + 2;
            try {
                importRow(row, bankSortMap);
                successCount++;
            } catch (Exception ex) {
                failureCount++;
                failureMessages.add("Row " + rowNum + " import failed: " + ex.getMessage());
            }
        }
        return QuizQuestionImportRespVO.builder()
                .successCount(successCount)
                .failureCount(failureCount)
                .failureMessages(failureMessages)
                .build();
    }

    private void importRow(QuizQuestionImportExcelVO row, Map<String, Integer> bankSortMap) {
        if (!StringUtils.hasText(row.getBankName())) {
            throw new IllegalArgumentException("bankName cannot be blank");
        }
        if (!StringUtils.hasText(row.getQuestionType())) {
            throw new IllegalArgumentException("questionType cannot be blank");
        }
        if (!StringUtils.hasText(row.getContent())) {
            throw new IllegalArgumentException("content cannot be blank");
        }
        QuizQuestionBankDO questionBank = quizQuestionBankMapper.selectByName(row.getBankName().trim());
        if (questionBank == null) {
            questionBank = QuizQuestionBankDO.builder()
                    .name(row.getBankName().trim())
                    .description("Imported from Excel")
                    .enabled(Boolean.TRUE)
                    .build();
            quizQuestionBankMapper.insert(questionBank);
        }

        String questionType = normalizeQuestionType(row.getQuestionType());
        Set<String> correctAnswers = parseCorrectAnswers(row.getCorrectAnswers());
        List<QuizQuestionOptionDO> options = buildOptions(questionType, row, correctAnswers);
        Integer sort = bankSortMap.compute(questionBank.getName(), (key, value) -> value == null ? 1 : value + 1);

        QuizQuestionDO question = QuizQuestionDO.builder()
                .bankId(questionBank.getId())
                .questionType(questionType)
                .content(row.getContent().trim())
                .imageUrl(trimToNull(row.getImageUrl()))
                .score(row.getScore() == null ? 0 : row.getScore())
                .explanation(trimToNull(row.getExplanation()))
                .sort(sort)
                .build();
        quizQuestionMapper.insert(question);

        for (QuizQuestionOptionDO option : options) {
            option.setQuestionId(question.getId());
            quizQuestionOptionMapper.insert(option);
        }
    }

    private String normalizeQuestionType(String questionType) {
        String normalized = questionType.trim().toUpperCase(Locale.ROOT);
        if (QuizQuestionDO.QUESTION_TYPE_SINGLE_CHOICE.equals(normalized)
                || QuizQuestionDO.QUESTION_TYPE_MULTIPLE_CHOICE.equals(normalized)
                || QuizQuestionDO.QUESTION_TYPE_TRUE_FALSE.equals(normalized)) {
            return normalized;
        }
        throw new IllegalArgumentException("unsupported questionType: " + questionType);
    }

    private Set<String> parseCorrectAnswers(String correctAnswers) {
        if (!StringUtils.hasText(correctAnswers)) {
            throw new IllegalArgumentException("correctAnswers cannot be blank");
        }
        return Arrays.stream(correctAnswers.split(","))
                .map(item -> item == null ? null : item.trim().toUpperCase(Locale.ROOT))
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<QuizQuestionOptionDO> buildOptions(
            String questionType,
            QuizQuestionImportExcelVO row,
            Set<String> correctAnswers) {
        if (QuizQuestionDO.QUESTION_TYPE_TRUE_FALSE.equals(questionType)) {
            if (correctAnswers.size() != 1
                    || (!correctAnswers.contains("TRUE") && !correctAnswers.contains("FALSE"))) {
                throw new IllegalArgumentException("TRUE_FALSE correctAnswers must be TRUE or FALSE");
            }
            return Arrays.asList(
                    QuizQuestionOptionDO.builder()
                            .optionKey("TRUE")
                            .content("正确")
                            .isCorrect(correctAnswers.contains("TRUE"))
                            .sort(1)
                            .build(),
                    QuizQuestionOptionDO.builder()
                            .optionKey("FALSE")
                            .content("错误")
                            .isCorrect(correctAnswers.contains("FALSE"))
                            .sort(2)
                            .build());
        }

        Map<String, String> optionMap = new LinkedHashMap<>();
        optionMap.put("A", trimToNull(row.getOptionA()));
        optionMap.put("B", trimToNull(row.getOptionB()));
        optionMap.put("C", trimToNull(row.getOptionC()));
        optionMap.put("D", trimToNull(row.getOptionD()));
        List<QuizQuestionOptionDO> options = new ArrayList<>();
        int sort = 1;
        for (Map.Entry<String, String> entry : optionMap.entrySet()) {
            if (!StringUtils.hasText(entry.getValue())) {
                continue;
            }
            options.add(QuizQuestionOptionDO.builder()
                    .optionKey(entry.getKey())
                    .content(entry.getValue())
                    .isCorrect(correctAnswers.contains(entry.getKey()))
                    .sort(sort++)
                    .build());
        }
        if (options.isEmpty()) {
            throw new IllegalArgumentException("options cannot be empty");
        }
        Set<String> optionKeys = options.stream()
                .map(QuizQuestionOptionDO::getOptionKey)
                .collect(Collectors.toSet());
        if (!optionKeys.containsAll(correctAnswers)) {
            throw new IllegalArgumentException("correctAnswers contains undefined option key");
        }
        if (QuizQuestionDO.QUESTION_TYPE_SINGLE_CHOICE.equals(questionType) && correctAnswers.size() != 1) {
            throw new IllegalArgumentException("SINGLE_CHOICE must have exactly one correct answer");
        }
        return options;
    }

    private QuizQuestionBankRespVO buildQuestionBankRespVO(QuizQuestionBankDO questionBank) {
        QuizQuestionBankRespVO respVO = new QuizQuestionBankRespVO();
        respVO.setId(questionBank.getId());
        respVO.setName(questionBank.getName());
        respVO.setDescription(questionBank.getDescription());
        respVO.setEnabled(questionBank.getEnabled());
        respVO.setCreateTime(questionBank.getCreateTime());
        respVO.setUpdateTime(questionBank.getUpdateTime());

        List<QuizQuestionDO> questions = quizQuestionMapper.selectByBankId(questionBank.getId());
        respVO.setQuestionCount(questions.size());
        if (CollUtil.isEmpty(questions)) {
            respVO.setQuestions(Collections.emptyList());
            return respVO;
        }

        Map<Long, List<QuizQuestionOptionDO>> optionMap = quizQuestionOptionMapper.selectByQuestionIds(
                        questions.stream().map(QuizQuestionDO::getId).collect(Collectors.toList()))
                .stream()
                .collect(Collectors.groupingBy(QuizQuestionOptionDO::getQuestionId));

        respVO.setQuestions(questions.stream().map(question -> {
            QuizQuestionBankRespVO.QuestionRespVO questionRespVO = new QuizQuestionBankRespVO.QuestionRespVO();
            questionRespVO.setId(question.getId());
            questionRespVO.setQuestionType(question.getQuestionType());
            questionRespVO.setContent(question.getContent());
            questionRespVO.setImageUrl(question.getImageUrl());
            questionRespVO.setScore(question.getScore());
            questionRespVO.setExplanation(question.getExplanation());
            questionRespVO.setSort(question.getSort());
            questionRespVO.setOptions(optionMap.getOrDefault(question.getId(), Collections.emptyList())
                    .stream()
                    .map(option -> {
                        QuizQuestionBankRespVO.OptionRespVO optionRespVO = new QuizQuestionBankRespVO.OptionRespVO();
                        optionRespVO.setId(option.getId());
                        optionRespVO.setOptionKey(option.getOptionKey());
                        optionRespVO.setContent(option.getContent());
                        optionRespVO.setIsCorrect(option.getIsCorrect());
                        optionRespVO.setSort(option.getSort());
                        return optionRespVO;
                    })
                    .collect(Collectors.toList()));
            return questionRespVO;
        }).collect(Collectors.toList()));
        return respVO;
    }

    private void replaceQuestions(Long bankId, List<QuizQuestionBankSaveReqVO.QuestionSaveReqVO> questions) {
        List<QuizQuestionDO> existingQuestions = quizQuestionMapper.selectByBankId(bankId);
        List<Long> questionIds = existingQuestions.stream()
                .map(QuizQuestionDO::getId)
                .collect(Collectors.toList());
        if (!questionIds.isEmpty()) {
            quizQuestionOptionMapper.delete(new LambdaQueryWrapperX<QuizQuestionOptionDO>()
                    .in(QuizQuestionOptionDO::getQuestionId, questionIds));
        }
        quizQuestionMapper.delete(new LambdaQueryWrapperX<QuizQuestionDO>()
                .eq(QuizQuestionDO::getBankId, bankId));
        saveQuestions(bankId, questions);
    }

    private void saveQuestions(Long bankId, Collection<QuizQuestionBankSaveReqVO.QuestionSaveReqVO> questions) {
        int questionSort = 1;
        for (QuizQuestionBankSaveReqVO.QuestionSaveReqVO questionReqVO : questions) {
            QuizQuestionDO question = QuizQuestionDO.builder()
                    .bankId(bankId)
                    .questionType(questionReqVO.getQuestionType())
                    .content(questionReqVO.getContent())
                    .imageUrl(trimToNull(questionReqVO.getImageUrl()))
                    .score(questionReqVO.getScore() == null ? 0 : questionReqVO.getScore())
                    .explanation(trimToNull(questionReqVO.getExplanation()))
                    .sort(questionReqVO.getSort() == null ? questionSort : questionReqVO.getSort())
                    .build();
            quizQuestionMapper.insert(question);
            int optionSort = 1;
            for (QuizQuestionBankSaveReqVO.OptionSaveReqVO optionReqVO : questionReqVO.getOptions()) {
                QuizQuestionOptionDO option = QuizQuestionOptionDO.builder()
                        .questionId(question.getId())
                        .optionKey(optionReqVO.getOptionKey())
                        .content(optionReqVO.getContent())
                        .isCorrect(Boolean.TRUE.equals(optionReqVO.getIsCorrect()))
                        .sort(optionReqVO.getSort() == null ? optionSort : optionReqVO.getSort())
                        .build();
                quizQuestionOptionMapper.insert(option);
                optionSort++;
            }
            questionSort++;
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private QuizQuestionImportExcelVO createTemplateRow(
            String bankName,
            String questionType,
            String content,
            String imageUrl,
            Integer score,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            String correctAnswers,
            String explanation) {
        QuizQuestionImportExcelVO row = new QuizQuestionImportExcelVO();
        row.setBankName(bankName);
        row.setQuestionType(questionType);
        row.setContent(content);
        row.setImageUrl(imageUrl);
        row.setScore(score);
        row.setOptionA(optionA);
        row.setOptionB(optionB);
        row.setOptionC(optionC);
        row.setOptionD(optionD);
        row.setCorrectAnswers(correctAnswers);
        row.setExplanation(explanation);
        return row;
    }

    public static class QuizQuestionImportExcelVO {

        @ExcelProperty("bankName")
        private String bankName;

        @ExcelProperty("questionType")
        private String questionType;

        @ExcelProperty("content")
        private String content;

        @ExcelProperty("imageUrl")
        private String imageUrl;

        @ExcelProperty("score")
        private Integer score;

        @ExcelProperty("optionA")
        private String optionA;

        @ExcelProperty("optionB")
        private String optionB;

        @ExcelProperty("optionC")
        private String optionC;

        @ExcelProperty("optionD")
        private String optionD;

        @ExcelProperty("correctAnswers")
        private String correctAnswers;

        @ExcelProperty("explanation")
        private String explanation;

        public String getBankName() {
            return bankName;
        }

        public void setBankName(String bankName) {
            this.bankName = bankName;
        }

        public String getQuestionType() {
            return questionType;
        }

        public void setQuestionType(String questionType) {
            this.questionType = questionType;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }

        public String getOptionA() {
            return optionA;
        }

        public void setOptionA(String optionA) {
            this.optionA = optionA;
        }

        public String getOptionB() {
            return optionB;
        }

        public void setOptionB(String optionB) {
            this.optionB = optionB;
        }

        public String getOptionC() {
            return optionC;
        }

        public void setOptionC(String optionC) {
            this.optionC = optionC;
        }

        public String getOptionD() {
            return optionD;
        }

        public void setOptionD(String optionD) {
            this.optionD = optionD;
        }

        public String getCorrectAnswers() {
            return correctAnswers;
        }

        public void setCorrectAnswers(String correctAnswers) {
            this.correctAnswers = correctAnswers;
        }

        public String getExplanation() {
            return explanation;
        }

        public void setExplanation(String explanation) {
            this.explanation = explanation;
        }
    }
}
