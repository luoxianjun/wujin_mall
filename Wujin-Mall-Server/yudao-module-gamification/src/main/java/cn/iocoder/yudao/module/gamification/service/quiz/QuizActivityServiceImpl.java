package cn.iocoder.yudao.module.gamification.service.quiz;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizActivityPageReqVO;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizActivityRespVO;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizActivitySaveReqVO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizActivityDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizForumActivityDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizQuestionBankDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizRewardRuleDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizActivityMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizForumActivityMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizQuestionBankMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizQuestionMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizRewardRuleMapper;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuizActivityServiceImpl implements QuizActivityService {

    @Resource
    private QuizActivityMapper quizActivityMapper;
    @Resource
    private QuizRewardRuleMapper quizRewardRuleMapper;
    @Resource
    private QuizForumActivityMapper quizForumActivityMapper;
    @Resource
    private QuizQuestionBankMapper quizQuestionBankMapper;
    @Resource
    private QuizQuestionMapper quizQuestionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createQuizActivity(QuizActivitySaveReqVO createReqVO) {
        QuizActivityDO existing = quizActivityMapper.selectByActivityId(createReqVO.getActivityId());
        if (existing != null) {
            throw new IllegalArgumentException("Quiz activity already exists for activityId=" + createReqVO.getActivityId());
        }
        QuizActivityDO quizActivity = buildQuizActivityDO(createReqVO);
        quizActivityMapper.insert(quizActivity);
        saveRewardRules(quizActivity.getId(), createReqVO.getRewardRules());
        return quizActivity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuizActivity(QuizActivitySaveReqVO updateReqVO) {
        if (updateReqVO.getId() == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        QuizActivityDO existing = quizActivityMapper.selectById(updateReqVO.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Quiz activity not found, id=" + updateReqVO.getId());
        }
        QuizActivityDO updateObj = buildQuizActivityDO(updateReqVO);
        updateObj.setId(updateReqVO.getId());
        quizActivityMapper.updateById(updateObj);
        replaceRewardRules(updateReqVO.getId(), updateReqVO.getRewardRules());
    }

    @Override
    public QuizActivityRespVO getQuizActivity(Long id) {
        QuizActivityDO quizActivity = quizActivityMapper.selectById(id);
        if (quizActivity == null) {
            return null;
        }
        QuizForumActivityDO activity = quizForumActivityMapper.selectById(quizActivity.getActivityId());
        QuizQuestionBankDO questionBank = quizQuestionBankMapper.selectById(quizActivity.getQuestionBankId());
        return convert(
                quizActivity,
                activity == null ? Collections.emptyMap() : Collections.singletonMap(activity.getId(), activity),
                questionBank == null
                        ? Collections.emptyMap()
                        : Collections.singletonMap(questionBank.getId(), questionBank));
    }

    @Override
    public PageResult<QuizActivityRespVO> getQuizActivityPage(QuizActivityPageReqVO pageReqVO) {
        PageResult<QuizActivityDO> pageResult = quizActivityMapper.selectPage(pageReqVO);
        Set<Long> activityIds = pageResult.getList().stream()
                .map(QuizActivityDO::getActivityId)
                .collect(Collectors.toSet());
        Set<Long> questionBankIds = pageResult.getList().stream()
                .map(QuizActivityDO::getQuestionBankId)
                .collect(Collectors.toSet());
        Map<Long, QuizForumActivityDO> activityMap = activityIds.isEmpty()
                ? Collections.emptyMap()
                : quizForumActivityMapper.selectBatchIds(activityIds).stream()
                        .collect(Collectors.toMap(QuizForumActivityDO::getId, item -> item));
        Map<Long, QuizQuestionBankDO> questionBankMap = questionBankIds.isEmpty()
                ? Collections.emptyMap()
                : quizQuestionBankMapper.selectBatchIds(questionBankIds).stream()
                        .collect(Collectors.toMap(QuizQuestionBankDO::getId, item -> item));
        List<QuizActivityRespVO> list = pageResult.getList().stream()
                .map(item -> convert(item, activityMap, questionBankMap))
                .collect(Collectors.toList());
        return new PageResult<>(list, pageResult.getTotal());
    }

    private QuizActivityDO buildQuizActivityDO(QuizActivitySaveReqVO reqVO) {
        return QuizActivityDO.builder()
                .activityId(reqVO.getActivityId())
                .questionBankId(reqVO.getQuestionBankId())
                .questionCount(reqVO.getQuestionCount())
                .maxAttempts(reqVO.getMaxAttempts())
                .durationSeconds(reqVO.getDurationSeconds())
                .randomQuestionOrder(reqVO.getRandomQuestionOrder())
                .randomOptionOrder(reqVO.getRandomOptionOrder())
                .leaderboardSize(reqVO.getLeaderboardSize())
                .answerRevealMode(reqVO.getAnswerRevealMode())
                .status(reqVO.getStatus())
                .build();
    }

    private QuizActivityRespVO convert(
            QuizActivityDO quizActivity,
            Map<Long, QuizForumActivityDO> activityMap,
            Map<Long, QuizQuestionBankDO> questionBankMap) {
        QuizActivityRespVO respVO = new QuizActivityRespVO();
        respVO.setId(quizActivity.getId());
        respVO.setActivityId(quizActivity.getActivityId());
        QuizForumActivityDO activity = activityMap.get(quizActivity.getActivityId());
        if (activity != null) {
            respVO.setActivityTitle(activity.getTitle());
            respVO.setActivityStartTime(activity.getStartTime());
            respVO.setActivityEndTime(activity.getEndTime());
        }
        respVO.setQuestionBankId(quizActivity.getQuestionBankId());
        QuizQuestionBankDO questionBank = questionBankMap.get(quizActivity.getQuestionBankId());
        if (questionBank != null) {
            respVO.setQuestionBankName(questionBank.getName());
            respVO.setQuestionBankQuestionCount(quizQuestionMapper.selectByBankId(questionBank.getId()).size());
            respVO.setQuestionBankCreateTime(questionBank.getCreateTime());
            respVO.setQuestionBankUpdateTime(questionBank.getUpdateTime());
        }
        respVO.setQuestionCount(quizActivity.getQuestionCount());
        respVO.setMaxAttempts(quizActivity.getMaxAttempts());
        respVO.setDurationSeconds(quizActivity.getDurationSeconds());
        respVO.setRandomQuestionOrder(quizActivity.getRandomQuestionOrder());
        respVO.setRandomOptionOrder(quizActivity.getRandomOptionOrder());
        respVO.setLeaderboardSize(quizActivity.getLeaderboardSize());
        respVO.setAnswerRevealMode(quizActivity.getAnswerRevealMode());
        respVO.setStatus(quizActivity.getStatus());
        respVO.setRewardRules(quizRewardRuleMapper.selectByQuizActivityId(quizActivity.getId()).stream()
                .map(rule -> {
                    QuizActivityRespVO.RewardRuleRespVO item = new QuizActivityRespVO.RewardRuleRespVO();
                    item.setId(rule.getId());
                    item.setRankStart(rule.getRankStart());
                    item.setRankEnd(rule.getRankEnd());
                    item.setRewardType(rule.getRewardType());
                    item.setPointAmount(rule.getPointAmount());
                    item.setRewardName(rule.getRewardName());
                    return item;
                })
                .collect(Collectors.toList()));
        return respVO;
    }

    private void replaceRewardRules(Long quizActivityId, List<QuizActivitySaveReqVO.RewardRuleSaveReqVO> rewardRules) {
        quizRewardRuleMapper.delete(new LambdaQueryWrapperX<QuizRewardRuleDO>()
                .eq(QuizRewardRuleDO::getQuizActivityId, quizActivityId));
        saveRewardRules(quizActivityId, rewardRules);
    }

    private void saveRewardRules(Long quizActivityId, List<QuizActivitySaveReqVO.RewardRuleSaveReqVO> rewardRules) {
        if (CollectionUtils.isEmpty(rewardRules)) {
            return;
        }
        for (QuizActivitySaveReqVO.RewardRuleSaveReqVO rewardRule : rewardRules) {
            QuizRewardRuleDO record = QuizRewardRuleDO.builder()
                    .quizActivityId(quizActivityId)
                    .rankStart(rewardRule.getRankStart())
                    .rankEnd(rewardRule.getRankEnd())
                    .rewardType(rewardRule.getRewardType())
                    .pointAmount(rewardRule.getPointAmount() == null ? 0 : rewardRule.getPointAmount())
                    .rewardName(rewardRule.getRewardName())
                    .build();
            quizRewardRuleMapper.insert(record);
        }
    }
}
