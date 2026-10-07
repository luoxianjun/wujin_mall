package cn.iocoder.yudao.module.forum.service.statistics;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.forum.controller.admin.statistics.vo.AdminForumStatisticsRespVO;
import cn.iocoder.yudao.module.forum.controller.admin.statistics.vo.AdminForumStatisticsTrendRespVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.activity.ForumActivityDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.ForumCommentDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.post.ForumPostDO;
import cn.iocoder.yudao.module.forum.dal.mysql.activity.ForumActivityMapper;
import cn.iocoder.yudao.module.forum.dal.mysql.post.ForumCommentMapper;
import cn.iocoder.yudao.module.forum.dal.mysql.post.ForumPostMapper;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.dal.mysql.user.MemberUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 论坛统计 Service 实现类
 */
@Service
@Validated
public class ForumStatisticsServiceImpl implements ForumStatisticsService {

    @Resource
    private MemberUserMapper memberUserMapper;
    @Resource
    private ForumPostMapper forumPostMapper;
    @Resource
    private ForumActivityMapper activityMapper;
    @Resource
    private ForumCommentMapper commentMapper;

    @Override
    public AdminForumStatisticsRespVO getStatisticsSummary() {
        LocalDateTime today = LocalDateTimeUtils.getToday();
        LocalDateTime tomorrow = today.plusDays(1);

        long todayMemberCount = memberUserMapper.selectCount(new LambdaQueryWrapperX<MemberUserDO>()
                .ge(MemberUserDO::getCreateTime, today)
                .lt(MemberUserDO::getCreateTime, tomorrow));
        long totalMemberCount = memberUserMapper.selectCount(new LambdaQueryWrapperX<>());

        long todayPostCount = forumPostMapper.selectCount(new LambdaQueryWrapperX<ForumPostDO>()
                .ge(ForumPostDO::getCreateTime, today)
                .lt(ForumPostDO::getCreateTime, tomorrow));
        long totalPostCount = forumPostMapper.selectCount(new LambdaQueryWrapperX<>());

        long todayActivityCount = activityMapper.selectCount(new LambdaQueryWrapperX<ForumActivityDO>()
                .ge(ForumActivityDO::getCreateTime, today)
                .lt(ForumActivityDO::getCreateTime, tomorrow));
        long totalActivityCount = activityMapper.selectCount(new LambdaQueryWrapperX<>());

        long todayCommentCount = commentMapper.selectCount(new LambdaQueryWrapperX<ForumCommentDO>()
                .ge(ForumCommentDO::getCreateTime, today)
                .lt(ForumCommentDO::getCreateTime, tomorrow)
                .ne(ForumCommentDO::getStatus, 1));
        long totalCommentCount = commentMapper.selectCount(new LambdaQueryWrapperX<ForumCommentDO>()
                .ne(ForumCommentDO::getStatus, 1));

        // 浏览记录未按天存储，这里按帖子创建时间聚合，能体现当日新增内容的浏览量
        Long todayViewCount = forumPostMapper.selectSumViewCount(today);
        Long totalViewCount = forumPostMapper.selectSumViewCount(null);

        long todayInteractionCount = todayViewCount + todayCommentCount;
        long totalInteractionCount = totalViewCount + totalCommentCount;

        return new AdminForumStatisticsRespVO()
                .setTodayMemberCount(todayMemberCount)
                .setTotalMemberCount(totalMemberCount)
                .setTodayPostCount(todayPostCount)
                .setTotalPostCount(totalPostCount)
                .setTodayActivityCount(todayActivityCount)
                .setTotalActivityCount(totalActivityCount)
                .setTodayInteractionCount(todayInteractionCount)
                .setTotalInteractionCount(totalInteractionCount);
    }

    @Override
    public AdminForumStatisticsTrendRespVO getLast30DaysTrend() {
        List<String> dates = new ArrayList<>();
        List<Long> memberCounts = new ArrayList<>();
        List<Long> postCounts = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;

        LocalDate today = LocalDate.now();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = start.plusDays(1);

            dates.add(formatter.format(date));
            memberCounts.add(memberUserMapper.selectCount(new LambdaQueryWrapperX<MemberUserDO>()
                    .ge(MemberUserDO::getCreateTime, start)
                    .lt(MemberUserDO::getCreateTime, end)));
            postCounts.add(forumPostMapper.selectCount(new LambdaQueryWrapperX<ForumPostDO>()
                    .ge(ForumPostDO::getCreateTime, start)
                    .lt(ForumPostDO::getCreateTime, end)));
        }

        return new AdminForumStatisticsTrendRespVO()
                .setDates(dates)
                .setMemberIncrements(memberCounts)
                .setPostIncrements(postCounts);
    }
}
