package cn.iocoder.yudao.module.gamification.service.quiz;

import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizQuestionImportRespVO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizQuestionBankDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizQuestionDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizQuestionOptionDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizQuestionBankMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizQuestionMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizQuestionOptionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizQuestionBankServiceTest {

    @InjectMocks
    private QuizQuestionBankServiceImpl quizQuestionBankService;

    @Mock
    private QuizQuestionBankMapper quizQuestionBankMapper;
    @Mock
    private QuizQuestionMapper quizQuestionMapper;
    @Mock
    private QuizQuestionOptionMapper quizQuestionOptionMapper;

    private final AtomicLong bankIdGenerator = new AtomicLong(1);
    private final AtomicLong questionIdGenerator = new AtomicLong(10);

    @BeforeEach
    void setUp() {
        doAnswer(invocation -> {
            QuizQuestionBankDO bank = invocation.getArgument(0);
            if (bank.getId() == null) {
                bank.setId(bankIdGenerator.getAndIncrement());
            }
            return 1;
        }).when(quizQuestionBankMapper).insert(any(QuizQuestionBankDO.class));
        doAnswer(invocation -> {
            QuizQuestionDO question = invocation.getArgument(0);
            question.setId(questionIdGenerator.getAndIncrement());
            return 1;
        }).when(quizQuestionMapper).insert(any(QuizQuestionDO.class));
    }

    @Test
    void importRow_mapsSingleChoiceWithExplanationAndImage() throws Exception {
        when(quizQuestionBankMapper.selectByName("History")).thenReturn(null);
        doAnswer(invocation -> {
            QuizQuestionBankDO bank = invocation.getArgument(0);
            bank.setId(1L);
            return 1;
        }).when(quizQuestionBankMapper).insert(any(QuizQuestionBankDO.class));

        Object row = createImportRow(
                "History",
                "SINGLE_CHOICE",
                "Who discovered gravity?",
                "https://example.com/gravity.png",
                5,
                "Newton",
                "Einstein",
                null,
                null,
                "A",
                "Classic physics question");

        ReflectionTestUtils.invokeMethod(
                quizQuestionBankService,
                "importRow",
                row,
                new LinkedHashMap<String, Integer>());

        ArgumentCaptor<QuizQuestionDO> questionCaptor = ArgumentCaptor.forClass(QuizQuestionDO.class);
        verify(quizQuestionMapper).insert(questionCaptor.capture());
        QuizQuestionDO question = questionCaptor.getValue();
        assertEquals(QuizQuestionDO.QUESTION_TYPE_SINGLE_CHOICE, question.getQuestionType());
        assertEquals("https://example.com/gravity.png", question.getImageUrl());
        assertEquals("Classic physics question", question.getExplanation());

        ArgumentCaptor<QuizQuestionOptionDO> optionCaptor = ArgumentCaptor.forClass(QuizQuestionOptionDO.class);
        verify(quizQuestionOptionMapper, org.mockito.Mockito.times(2)).insert(optionCaptor.capture());
        assertEquals("A", optionCaptor.getAllValues().get(0).getOptionKey());
        assertTrue(optionCaptor.getAllValues().get(0).getIsCorrect());
    }

    @Test
    void importRow_mapsMultipleChoiceCorrectAnswers() throws Exception {
        when(quizQuestionBankMapper.selectByName("Science"))
                .thenReturn(QuizQuestionBankDO.builder().id(2L).name("Science").build());

        Object row = createImportRow(
                "Science",
                "MULTIPLE_CHOICE",
                "Select noble gases",
                null,
                8,
                "Helium",
                "Neon",
                "Oxygen",
                "Argon",
                "A,B,D",
                "Multiple correct answers");

        ReflectionTestUtils.invokeMethod(
                quizQuestionBankService,
                "importRow",
                row,
                new LinkedHashMap<String, Integer>());

        ArgumentCaptor<QuizQuestionOptionDO> optionCaptor = ArgumentCaptor.forClass(QuizQuestionOptionDO.class);
        verify(quizQuestionOptionMapper, org.mockito.Mockito.times(4)).insert(optionCaptor.capture());
        Map<String, Boolean> correctMap = new LinkedHashMap<>();
        optionCaptor.getAllValues().forEach(option -> correctMap.put(option.getOptionKey(), option.getIsCorrect()));
        assertEquals(Boolean.TRUE, correctMap.get("A"));
        assertEquals(Boolean.TRUE, correctMap.get("B"));
        assertEquals(Boolean.FALSE, correctMap.get("C"));
        assertEquals(Boolean.TRUE, correctMap.get("D"));
    }

    @Test
    void importRow_mapsTrueFalseQuestion() throws Exception {
        when(quizQuestionBankMapper.selectByName("Logic"))
                .thenReturn(QuizQuestionBankDO.builder().id(3L).name("Logic").build());

        Object row = createImportRow(
                "Logic",
                "TRUE_FALSE",
                "TRUE_FALSE statements use two options",
                null,
                2,
                null,
                null,
                null,
                null,
                "TRUE",
                "TRUE_FALSE explanation");

        ReflectionTestUtils.invokeMethod(
                quizQuestionBankService,
                "importRow",
                row,
                new LinkedHashMap<String, Integer>());

        ArgumentCaptor<QuizQuestionDO> questionCaptor = ArgumentCaptor.forClass(QuizQuestionDO.class);
        verify(quizQuestionMapper).insert(questionCaptor.capture());
        assertEquals(QuizQuestionDO.QUESTION_TYPE_TRUE_FALSE, questionCaptor.getValue().getQuestionType());

        ArgumentCaptor<QuizQuestionOptionDO> optionCaptor = ArgumentCaptor.forClass(QuizQuestionOptionDO.class);
        verify(quizQuestionOptionMapper, org.mockito.Mockito.times(2)).insert(optionCaptor.capture());
        assertEquals("TRUE", optionCaptor.getAllValues().get(0).getOptionKey());
        assertEquals("正确", optionCaptor.getAllValues().get(0).getContent());
        assertEquals("FALSE", optionCaptor.getAllValues().get(1).getOptionKey());
        assertEquals("错误", optionCaptor.getAllValues().get(1).getContent());
    }

    @Test
    void importQuestionBank_readsDownloadedTemplate() throws Exception {
        Map<String, QuizQuestionBankDO> banks = new LinkedHashMap<>();
        when(quizQuestionBankMapper.selectByName(anyString()))
                .thenAnswer(invocation -> banks.get(invocation.getArgument(0)));
        doAnswer(invocation -> {
            QuizQuestionBankDO bank = invocation.getArgument(0);
            if (bank.getId() == null) {
                bank.setId(bankIdGenerator.getAndIncrement());
            }
            banks.put(bank.getName(), bank);
            return 1;
        }).when(quizQuestionBankMapper).insert(any(QuizQuestionBankDO.class));

        MockHttpServletResponse response = new MockHttpServletResponse();
        quizQuestionBankService.downloadImportTemplate(response);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "quiz-template.xls",
                response.getContentType(),
                response.getContentAsByteArray());

        QuizQuestionImportRespVO result = quizQuestionBankService.importQuestionBank(file);

        assertEquals(3, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        assertTrue(result.getFailureMessages().isEmpty());
        assertEquals(1, banks.size());
        verify(quizQuestionMapper, times(3)).insert(any(QuizQuestionDO.class));
        verify(quizQuestionOptionMapper, times(10)).insert(any(QuizQuestionOptionDO.class));
    }

    private Object createImportRow(
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
            String explanation) throws Exception {
        Class<?> rowClass = Class.forName(
                "cn.iocoder.yudao.module.gamification.service.quiz.QuizQuestionBankServiceImpl$QuizQuestionImportExcelVO");
        Constructor<?> constructor = rowClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object row = constructor.newInstance();
        ReflectionTestUtils.setField(row, "bankName", bankName);
        ReflectionTestUtils.setField(row, "questionType", questionType);
        ReflectionTestUtils.setField(row, "content", content);
        ReflectionTestUtils.setField(row, "imageUrl", imageUrl);
        ReflectionTestUtils.setField(row, "score", score);
        ReflectionTestUtils.setField(row, "optionA", optionA);
        ReflectionTestUtils.setField(row, "optionB", optionB);
        ReflectionTestUtils.setField(row, "optionC", optionC);
        ReflectionTestUtils.setField(row, "optionD", optionD);
        ReflectionTestUtils.setField(row, "correctAnswers", correctAnswers);
        ReflectionTestUtils.setField(row, "explanation", explanation);
        return row;
    }
}
