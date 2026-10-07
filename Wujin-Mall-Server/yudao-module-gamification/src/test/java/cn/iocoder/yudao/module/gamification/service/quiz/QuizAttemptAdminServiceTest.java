package cn.iocoder.yudao.module.gamification.service.quiz;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizAttemptExportData;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizAttemptPageReqVO;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizAttemptRespVO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizAttemptDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizForumActivityDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizForumActivitySignUpDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizAttemptMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizForumActivityMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizForumActivitySignUpMapper;
import cn.iocoder.yudao.module.forum.api.user.ForumUserProfileApi;
import cn.iocoder.yudao.module.forum.api.user.dto.ForumUserProfileDTO;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QuizAttemptAdminServiceTest extends BaseMockitoUnitTest {

    @InjectMocks
    private QuizAttemptAdminServiceImpl quizAttemptAdminService;

    @Mock
    private QuizAttemptMapper quizAttemptMapper;

    @Mock
    private MemberUserApi memberUserApi;
    @Mock
    private ForumUserProfileApi forumUserProfileApi;
    @Mock
    private QuizLeaderboardService quizLeaderboardService;
    @Mock
    private QuizForumActivityMapper quizForumActivityMapper;
    @Mock
    private QuizForumActivitySignUpMapper quizForumActivitySignUpMapper;

    @Test
    void getQuizAttemptPage_returnsPagedRecordsWithUserInfo() {
        QuizAttemptPageReqVO reqVO = new QuizAttemptPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        reqVO.setQuizActivityId(1L);

        when(quizAttemptMapper.selectPage(same(reqVO), org.mockito.ArgumentMatchers.<Collection<Long>>isNull())).thenReturn(new PageResult<>(
                Arrays.asList(
                        buildAttempt(12L, 1L, 502L, 2, QuizAttemptDO.STATUS_TIMEOUT_SUBMITTED, 76, 52_000L),
                        buildAttempt(11L, 1L, 501L, 1, QuizAttemptDO.STATUS_SUBMITTED, 88, 45_000L)),
                2L));
        when(memberUserApi.getUserList(argThat(ids -> containsExactly(ids, 501L, 502L)))).thenReturn(Arrays.asList(
                member(501L, "阿星", "13800000001"),
                member(502L, "阿月", "13800000002")));

        PageResult<QuizAttemptRespVO> result = quizAttemptAdminService.getQuizAttemptPage(reqVO);

        assertEquals(2L, result.getTotal());
        assertEquals(2, result.getList().size());
        assertEquals(12L, result.getList().get(0).getId().longValue());
        assertEquals("阿月", result.getList().get(0).getUserNickname());
        assertEquals("13800000002", result.getList().get(0).getUserMobile());
        assertEquals("阿星", result.getList().get(1).getUserNickname());
        verify(memberUserApi).getUserList(argThat(ids -> containsExactly(ids, 501L, 502L)));
    }

    @Test
    void getQuizAttemptPage_filtersByNicknameBeforePaging() {
        QuizAttemptPageReqVO reqVO = new QuizAttemptPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        reqVO.setQuizActivityId(2L);
        reqVO.setUserNickname("学霸");

        when(memberUserApi.getUserListByNickname("学霸")).thenReturn(Arrays.asList(
                member(601L, "学霸一号", "13800000011"),
                member(603L, "学霸二号", "13800000013")));
        when(quizAttemptMapper.selectPage(same(reqVO), argThat((Collection<Long> ids) -> containsExactly(ids, 601L, 603L))))
                .thenReturn(new PageResult<>(Arrays.asList(
                        buildAttempt(23L, 2L, 603L, 1, QuizAttemptDO.STATUS_SUBMITTED, 70, 42_000L),
                        buildAttempt(21L, 2L, 601L, 1, QuizAttemptDO.STATUS_SUBMITTED, 90, 40_000L)), 2L));
        when(memberUserApi.getUserList(argThat(ids -> containsExactly(ids, 601L, 603L)))).thenReturn(Arrays.asList(
                member(601L, "学霸一号", "13800000011"),
                member(603L, "学霸二号", "13800000013")));

        PageResult<QuizAttemptRespVO> result = quizAttemptAdminService.getQuizAttemptPage(reqVO);

        assertEquals(2L, result.getTotal());
        assertEquals(2, result.getList().size());
        assertTrue(result.getList().stream().allMatch(item -> item.getUserNickname().contains("学霸")));
        verify(memberUserApi).getUserListByNickname("学霸");
        verify(quizAttemptMapper).selectPage(same(reqVO), argThat((Collection<Long> ids) -> containsExactly(ids, 601L, 603L)));
    }

    @Test
    void getQuizAttemptPage_filtersByMobileBeforePaging() {
        QuizAttemptPageReqVO reqVO = new QuizAttemptPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        reqVO.setQuizActivityId(3L);
        reqVO.setUserMobile("13800000022");

        when(memberUserApi.getUserByMobile("13800000022"))
                .thenReturn(member(702L, "手机号命中", "13800000022"));
        when(quizAttemptMapper.selectPage(same(reqVO), argThat((Collection<Long> ids) -> containsExactly(ids, 702L))))
                .thenReturn(new PageResult<>(Collections.singletonList(
                        buildAttempt(32L, 3L, 702L, 1, QuizAttemptDO.STATUS_SUBMITTED, 77, 51_000L)), 1L));
        when(memberUserApi.getUserList(argThat(ids -> containsExactly(ids, 702L))))
                .thenReturn(Collections.singletonList(member(702L, "手机号命中", "13800000022")));

        PageResult<QuizAttemptRespVO> result = quizAttemptAdminService.getQuizAttemptPage(reqVO);

        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals(32L, result.getList().get(0).getId().longValue());
        assertEquals("13800000022", result.getList().get(0).getUserMobile());
        verify(memberUserApi).getUserByMobile("13800000022");
        verify(quizAttemptMapper).selectPage(same(reqVO), argThat((Collection<Long> ids) -> containsExactly(ids, 702L)));
    }

    @Test
    void getQuizAttemptPage_enrichesUidRankActivityTitleAndSignUpRemark() {
        QuizAttemptPageReqVO reqVO = new QuizAttemptPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        reqVO.setQuizActivityId(4L);

        QuizAttemptDO attempt = buildAttempt(41L, 4L, 801L, 1, QuizAttemptDO.STATUS_SUBMITTED, 95, 30_000L);
        when(quizAttemptMapper.selectPage(same(reqVO), org.mockito.ArgumentMatchers.<Collection<Long>>isNull()))
                .thenReturn(new PageResult<>(Collections.singletonList(attempt), 1L));
        when(memberUserApi.getUserList(argThat(ids -> containsExactly(ids, 801L))))
                .thenReturn(Collections.singletonList(member(801L, "会员名", "13800000801")));
        Map<Long, ForumUserProfileDTO> profiles = new HashMap<>();
        profiles.put(801L, profile(801L, "U00801", "论坛名"));
        when(forumUserProfileApi.getUserProfileMapByUserIds(argThat(ids -> containsExactly(ids, 801L))))
                .thenReturn(profiles);
        when(quizForumActivityMapper.selectBatchIds(argThat(ids -> containsExactly(ids, 400L))))
                .thenReturn(Collections.singletonList(activity(400L, "答题活动", null)));
        QuizLeaderboardService.LeaderboardDetail detail = new QuizLeaderboardService.LeaderboardDetail();
        detail.setUserId(801L);
        detail.setAttemptId(41L);
        when(quizLeaderboardService.getRankedDetails(4L)).thenReturn(Collections.singletonList(detail));
        when(quizForumActivitySignUpMapper.selectByActivityIdsAndUserIds(
                argThat(ids -> containsExactly(ids, 400L)),
                argThat(ids -> containsExactly(ids, 801L))))
                .thenReturn(Collections.singletonList(signUp(400L, 801L, "{\"姓名\":\"张三\"}")));

        PageResult<QuizAttemptRespVO> result = quizAttemptAdminService.getQuizAttemptPage(reqVO);

        QuizAttemptRespVO item = result.getList().get(0);
        assertEquals("U00801", item.getUid());
        assertEquals("会员名", item.getUserNickname());
        assertEquals("答题活动", item.getActivityTitle());
        assertEquals(1, item.getActivityRank());
        assertEquals("{\"姓名\":\"张三\"}", item.getSignUpRemark());
    }

    @Test
    void getQuizAttemptExportData_includesDynamicRegistrationColumns() {
        QuizAttemptPageReqVO reqVO = new QuizAttemptPageReqVO();
        reqVO.setQuizActivityId(5L);

        QuizAttemptDO attempt = buildAttempt(51L, 5L, 901L, 2, QuizAttemptDO.STATUS_SUBMITTED, 88, 42_000L);
        when(quizAttemptMapper.selectListForExport(same(reqVO), org.mockito.ArgumentMatchers.<Collection<Long>>isNull()))
                .thenReturn(Collections.singletonList(attempt));
        when(memberUserApi.getUserList(argThat(ids -> containsExactly(ids, 901L))))
                .thenReturn(Collections.singletonList(member(901L, "导出用户", "13800000901")));
        Map<Long, ForumUserProfileDTO> profiles = new HashMap<>();
        profiles.put(901L, profile(901L, "U00901", "论坛导出用户"));
        when(forumUserProfileApi.getUserProfileMapByUserIds(argThat(ids -> containsExactly(ids, 901L))))
                .thenReturn(profiles);
        when(quizForumActivityMapper.selectBatchIds(argThat(ids -> containsExactly(ids, 500L))))
                .thenReturn(Collections.singletonList(activity(500L, "导出答题活动",
                        "[{\"key\":\"姓名\",\"type\":\"input\"},{\"key\":\"附件\",\"type\":\"file\"}]")));
        QuizLeaderboardService.LeaderboardDetail detail = new QuizLeaderboardService.LeaderboardDetail();
        detail.setUserId(901L);
        detail.setAttemptId(51L);
        when(quizLeaderboardService.getRankedDetails(5L)).thenReturn(Collections.singletonList(detail));
        when(quizForumActivitySignUpMapper.selectByActivityIdsAndUserIds(
                argThat(ids -> containsExactly(ids, 500L)),
                argThat(ids -> containsExactly(ids, 901L))))
                .thenReturn(Collections.singletonList(signUp(500L, 901L,
                        "{\"姓名\":\"李四\",\"附件\":{\"name\":\"证明.pdf\",\"url\":\"https://oss.example/proof.pdf\"}}")));

        QuizAttemptExportData exportData = quizAttemptAdminService.getQuizAttemptExportData(reqVO);

        List<List<String>> head = exportData.getHead();
        assertEquals("答题记录ID", head.get(0).get(0));
        assertTrue(head.stream().anyMatch(column -> "姓名".equals(column.get(0))));
        assertTrue(head.stream().anyMatch(column -> "附件".equals(column.get(0))));

        List<Object> row = exportData.getRows().get(0);
        assertEquals(51L, row.get(0));
        assertTrue(row.contains("U00901"));
        assertTrue(row.contains(1));
        assertTrue(row.contains("李四"));
        assertTrue(row.contains("https://oss.example/proof.pdf"));
    }

    private boolean containsExactly(Collection<?> actualIds, Long... expectedIds) {
        return actualIds != null && actualIds.size() == expectedIds.length
                && actualIds.containsAll(Arrays.asList(expectedIds));
    }

    private QuizAttemptDO buildAttempt(Long id, Long quizActivityId, Long userId, Integer attemptNo,
                                       String status, Integer score, Long elapsedMillis) {
        return QuizAttemptDO.builder()
                .id(id)
                .quizActivityId(quizActivityId)
                .activityId(quizActivityId * 100)
                .userId(userId)
                .attemptNo(attemptNo)
                .status(status)
                .score(score)
                .elapsedMillis(elapsedMillis)
                .startedAt(LocalDateTime.of(2026, 4, 20, 10, 0))
                .submittedAt(LocalDateTime.of(2026, 4, 20, 10, 5))
                .build();
    }

    private MemberUserRespDTO member(Long id, String nickname, String mobile) {
        MemberUserRespDTO user = new MemberUserRespDTO();
        user.setId(id);
        user.setNickname(nickname);
        user.setMobile(mobile);
        return user;
    }

    private ForumUserProfileDTO profile(Long userId, String uid, String nickname) {
        ForumUserProfileDTO profile = new ForumUserProfileDTO();
        profile.setUserId(userId);
        profile.setUid(uid);
        profile.setNickname(nickname);
        return profile;
    }

    private QuizForumActivityDO activity(Long id, String title, String customFields) {
        QuizForumActivityDO activity = new QuizForumActivityDO();
        activity.setId(id);
        activity.setTitle(title);
        activity.setCustomFields(customFields);
        return activity;
    }

    private QuizForumActivitySignUpDO signUp(Long activityId, Long userId, String remark) {
        QuizForumActivitySignUpDO signUp = new QuizForumActivitySignUpDO();
        signUp.setActivityId(activityId);
        signUp.setUserId(userId);
        signUp.setRemark(remark);
        return signUp;
    }
}
