package cn.iocoder.yudao.module.gamification.service.stats;

import cn.iocoder.yudao.module.gamification.controller.admin.stats.vo.GamificationStatsRespVO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.invitation.InvitationRelationDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.invitation.InvitationRelationMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.lottery.LotteryActivityMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.lottery.LotteryRecordMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizActivityMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizAttemptMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.vote.VoteActivityMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.vote.VoteRecordMapper;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 游戏化综合统计 Service 实现
 *
 * @author gamification
 */
@Service
public class GamificationStatsServiceImpl implements GamificationStatsService {

    private static final Logger log = LoggerFactory.getLogger(GamificationStatsServiceImpl.class);

    @Resource
    private InvitationRelationMapper invitationRelationMapper;
    @Resource
    private QuizActivityMapper quizActivityMapper;
    @Resource
    private QuizAttemptMapper quizAttemptMapper;
    @Resource
    private LotteryActivityMapper lotteryActivityMapper;
    @Resource
    private LotteryRecordMapper lotteryRecordMapper;
    @Resource
    private VoteActivityMapper voteActivityMapper;
    @Resource
    private VoteRecordMapper voteRecordMapper;

    @Override
    public GamificationStatsRespVO getOverviewStats() {
        GamificationStatsRespVO stats = new GamificationStatsRespVO();

        try {
            // === 邀请统计 ===
            Long totalInvitations = invitationRelationMapper.selectCount();
            stats.setTotalInvitations(totalInvitations != null ? totalInvitations : 0L);

            Long successInvitations = invitationRelationMapper.selectCount(
                    new LambdaQueryWrapperX<InvitationRelationDO>()
                            .eq(InvitationRelationDO::getStatus, InvitationRelationDO.STATUS_COMPLETED));
            stats.setSuccessInvitations(successInvitations != null ? successInvitations : 0L);
            stats.setInvitationPointsTotal(0L); // TODO: aggregate from member_point_record

            // === 答题统计 ===
            Long totalQuizActivities = quizActivityMapper.selectCount();
            stats.setTotalQuizActivities(totalQuizActivities != null ? totalQuizActivities : 0L);

            Long totalQuizParticipants = quizAttemptMapper.selectCount();
            stats.setTotalQuizParticipants(totalQuizParticipants != null ? totalQuizParticipants : 0L);
            stats.setQuizPointsTotal(0L); // TODO: aggregate from member_point_record

            // === 抽奖统计 ===
            Long totalLotteryActivities = lotteryActivityMapper.selectCount();
            stats.setTotalLotteryActivities(totalLotteryActivities != null ? totalLotteryActivities : 0L);

            Long totalLotteryDraws = lotteryRecordMapper.selectCount();
            stats.setTotalLotteryDraws(totalLotteryDraws != null ? totalLotteryDraws : 0L);
            stats.setTotalLotteryWins(0L); // TODO: count where is_winning = true

            // === 投票统计 ===
            Long totalVoteActivities = voteActivityMapper.selectCount();
            stats.setTotalVoteActivities(totalVoteActivities != null ? totalVoteActivities : 0L);

            Long totalVoteParticipants = voteRecordMapper.selectCount();
            stats.setTotalVoteParticipants(totalVoteParticipants != null ? totalVoteParticipants : 0L);

            // === 积分汇总 ===
            stats.setTotalPointsGranted(0L); // TODO: aggregate from member_point_record for biz_type 31-37
            stats.setTodayPointsGranted(0L); // TODO: aggregate with date filter

        } catch (Exception e) {
            log.error("[getOverviewStats] Failed to aggregate statistics", e);
            // Return partially filled stats rather than failing
        }

        return stats;
    }
}
