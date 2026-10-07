package cn.iocoder.yudao.module.gamification.service.quiz;

public interface QuizRewardService {

    int distributePendingRewards();

    int distributeQuizRewards(Long quizActivityId);
}
