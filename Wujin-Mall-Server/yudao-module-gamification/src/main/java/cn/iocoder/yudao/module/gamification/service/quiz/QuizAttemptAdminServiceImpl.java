package cn.iocoder.yudao.module.gamification.service.quiz;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Service
public class QuizAttemptAdminServiceImpl implements QuizAttemptAdminService {

    private static final Map<String, String> STATUS_LABELS = new HashMap<>();

    static {
        STATUS_LABELS.put(QuizAttemptDO.STATUS_IN_PROGRESS, "答题中");
        STATUS_LABELS.put(QuizAttemptDO.STATUS_SUBMITTED, "已提交");
        STATUS_LABELS.put(QuizAttemptDO.STATUS_TIMEOUT_SUBMITTED, "超时提交");
        STATUS_LABELS.put(QuizAttemptDO.STATUS_INVALIDATED, "已失效");
    }

    @Resource
    private QuizAttemptMapper quizAttemptMapper;

    @Resource
    private MemberUserApi memberUserApi;

    @Resource
    private ForumUserProfileApi forumUserProfileApi;

    @Resource
    private QuizLeaderboardService quizLeaderboardService;

    @Resource
    private QuizForumActivityMapper quizForumActivityMapper;

    @Resource
    private QuizForumActivitySignUpMapper quizForumActivitySignUpMapper;

    @Override
    public PageResult<QuizAttemptRespVO> getQuizAttemptPage(QuizAttemptPageReqVO reqVO) {
        Set<Long> filteredUserIds = resolveFilteredUserIds(reqVO);
        if (filteredUserIds != null && filteredUserIds.isEmpty()) {
            return PageResult.empty();
        }

        PageResult<QuizAttemptDO> pageResult = quizAttemptMapper.selectPage(reqVO, filteredUserIds);
        if (pageResult.getList().isEmpty()) {
            return PageResult.empty(pageResult.getTotal());
        }

        List<QuizAttemptRespVO> voList = buildRespVOList(pageResult.getList());
        return new PageResult<>(voList, pageResult.getTotal());
    }

    @Override
    public QuizAttemptExportData getQuizAttemptExportData(QuizAttemptPageReqVO reqVO) {
        Set<Long> filteredUserIds = resolveFilteredUserIds(reqVO);
        if (filteredUserIds != null && filteredUserIds.isEmpty()) {
            return new QuizAttemptExportData(buildExportHead(Collections.emptyList()), Collections.emptyList());
        }

        List<QuizAttemptDO> attempts = safeList(quizAttemptMapper.selectListForExport(reqVO, filteredUserIds));
        EnrichmentContext context = buildEnrichmentContext(attempts);
        List<JSONObject> customFields = collectCustomFields(context.activityMap.values());
        List<List<Object>> rows = attempts.stream()
                .map(attempt -> buildExportRow(attempt, context, customFields))
                .collect(Collectors.toList());
        return new QuizAttemptExportData(buildExportHead(customFields), rows);
    }

    private Set<Long> resolveFilteredUserIds(QuizAttemptPageReqVO reqVO) {
        Set<Long> filteredUserIds = null;
        if (reqVO.getUserId() != null) {
            filteredUserIds = new LinkedHashSet<>(Collections.singleton(reqVO.getUserId()));
        }
        if (StringUtils.hasText(reqVO.getUserNickname())) {
            List<MemberUserRespDTO> nicknameUsers = memberUserApi.getUserListByNickname(reqVO.getUserNickname());
            filteredUserIds = intersect(
                    filteredUserIds,
                    CollectionUtils.convertSet(nicknameUsers, MemberUserRespDTO::getId));
        }
        if (StringUtils.hasText(reqVO.getUserMobile())) {
            MemberUserRespDTO user = memberUserApi.getUserByMobile(reqVO.getUserMobile());
            Set<Long> mobileUserIds = user == null || user.getId() == null
                    ? Collections.emptySet()
                    : new LinkedHashSet<>(Collections.singleton(user.getId()));
            filteredUserIds = intersect(filteredUserIds, mobileUserIds);
        }
        return filteredUserIds;
    }

    private Set<Long> intersect(Set<Long> base, Collection<Long> incoming) {
        Set<Long> incomingIds = incoming == null
                ? Collections.emptySet()
                : incoming.stream()
                        .filter(id -> id != null)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
        if (base == null) {
            return incomingIds;
        }
        base.retainAll(incomingIds);
        return base;
    }

    private List<QuizAttemptRespVO> buildRespVOList(List<QuizAttemptDO> attempts) {
        EnrichmentContext context = buildEnrichmentContext(attempts);
        return attempts.stream()
                .map(attempt -> toRespVO(attempt, context))
                .collect(Collectors.toList());
    }

    private EnrichmentContext buildEnrichmentContext(List<QuizAttemptDO> attempts) {
        EnrichmentContext context = new EnrichmentContext();
        if (attempts == null || attempts.isEmpty()) {
            return context;
        }

        Set<Long> userIds = attempts.stream()
                .map(QuizAttemptDO::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<Long> activityIds = attempts.stream()
                .map(QuizAttemptDO::getActivityId)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        context.userMap = CollectionUtils.convertMap(
                safeList(memberUserApi.getUserList(userIds)),
                MemberUserRespDTO::getId);
        Map<Long, ForumUserProfileDTO> profileMap = forumUserProfileApi.getUserProfileMapByUserIds(userIds);
        context.profileMap = profileMap == null ? Collections.emptyMap() : profileMap;
        context.activityMap = CollectionUtils.convertMap(
                safeList(quizForumActivityMapper.selectBatchIds(activityIds)),
                QuizForumActivityDO::getId);
        context.signUpMap = buildSignUpMap(
                safeList(quizForumActivitySignUpMapper.selectByActivityIdsAndUserIds(activityIds, userIds)));
        context.rankMap = buildRankMap(attempts);
        return context;
    }

    private Map<String, QuizForumActivitySignUpDO> buildSignUpMap(List<QuizForumActivitySignUpDO> signUps) {
        Map<String, QuizForumActivitySignUpDO> signUpMap = new HashMap<>();
        for (QuizForumActivitySignUpDO signUp : signUps) {
            if (signUp.getActivityId() == null || signUp.getUserId() == null) {
                continue;
            }
            signUpMap.putIfAbsent(buildSignUpKey(signUp.getActivityId(), signUp.getUserId()), signUp);
        }
        return signUpMap;
    }

    private Map<Long, Map<Long, Integer>> buildRankMap(List<QuizAttemptDO> attempts) {
        Map<Long, Map<Long, Integer>> rankMap = new HashMap<>();
        Set<Long> quizActivityIds = attempts.stream()
                .map(QuizAttemptDO::getQuizActivityId)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        for (Long quizActivityId : quizActivityIds) {
            List<QuizLeaderboardService.LeaderboardDetail> rankedDetails = quizLeaderboardService.getRankedDetails(quizActivityId);
            if (rankedDetails == null || rankedDetails.isEmpty()) {
                continue;
            }
            Map<Long, Integer> userRanks = new HashMap<>();
            int rank = 1;
            for (QuizLeaderboardService.LeaderboardDetail detail : rankedDetails) {
                if (detail.getUserId() != null && !userRanks.containsKey(detail.getUserId())) {
                    userRanks.put(detail.getUserId(), rank);
                }
                rank++;
            }
            rankMap.put(quizActivityId, userRanks);
        }
        return rankMap;
    }

    private QuizAttemptRespVO toRespVO(QuizAttemptDO attempt, EnrichmentContext context) {
        MemberUserRespDTO user = context.userMap.get(attempt.getUserId());
        ForumUserProfileDTO profile = context.profileMap.get(attempt.getUserId());
        QuizForumActivityDO activity = context.activityMap.get(attempt.getActivityId());
        QuizForumActivitySignUpDO signUp = context.signUpMap.get(buildSignUpKey(attempt.getActivityId(), attempt.getUserId()));

        QuizAttemptRespVO vo = new QuizAttemptRespVO();
        vo.setId(attempt.getId());
        vo.setQuizActivityId(attempt.getQuizActivityId());
        vo.setActivityId(attempt.getActivityId());
        vo.setActivityTitle(activity == null ? null : activity.getTitle());
        vo.setUserId(attempt.getUserId());
        vo.setUid(profile == null ? null : profile.getUid());
        vo.setAttemptNo(attempt.getAttemptNo());
        vo.setStatus(attempt.getStatus());
        vo.setScore(attempt.getScore());
        vo.setElapsedMillis(attempt.getElapsedMillis());
        vo.setStartedAt(attempt.getStartedAt());
        vo.setSubmittedAt(attempt.getSubmittedAt());
        vo.setInvalidatedReason(attempt.getInvalidatedReason());
        vo.setCreateTime(attempt.getCreateTime());
        vo.setActivityRank(context.rankMap
                .getOrDefault(attempt.getQuizActivityId(), Collections.emptyMap())
                .get(attempt.getUserId()));
        vo.setSignUpRemark(signUp == null ? null : signUp.getRemark());
        vo.setUserNickname(firstText(user == null ? null : user.getNickname(), profile == null ? null : profile.getNickname()));
        if (user != null) {
            vo.setUserMobile(user.getMobile());
        }
        return vo;
    }

    private List<List<String>> buildExportHead(List<JSONObject> customFields) {
        List<List<String>> head = new ArrayList<>();
        for (String title : Arrays.asList("答题记录ID", "答题活动ID", "论坛活动ID", "活动标题", "用户名", "UID",
                "手机号", "活动排名", "第N次", "得分", "用时", "状态", "失效原因", "开始时间", "提交时间",
                "报名信息", "报名时间", "记录创建时间")) {
            head.add(Collections.singletonList(title));
        }
        for (JSONObject field : customFields) {
            String key = field.getStr("key");
            if (StrUtil.isBlank(key)) {
                continue;
            }
            head.add(Collections.singletonList(key));
        }
        return head;
    }

    private List<Object> buildExportRow(QuizAttemptDO attempt,
                                        EnrichmentContext context,
                                        List<JSONObject> customFields) {
        QuizAttemptRespVO vo = toRespVO(attempt, context);
        QuizForumActivitySignUpDO signUp = context.signUpMap.get(buildSignUpKey(attempt.getActivityId(), attempt.getUserId()));
        JSONObject remarkJson = parseSignUpRemark(signUp == null ? null : signUp.getRemark());

        List<Object> row = new ArrayList<>();
        row.add(vo.getId());
        row.add(vo.getQuizActivityId());
        row.add(vo.getActivityId());
        row.add(vo.getActivityTitle());
        row.add(vo.getUserNickname());
        row.add(vo.getUid());
        row.add(vo.getUserMobile());
        row.add(vo.getActivityRank());
        row.add(vo.getAttemptNo());
        row.add(vo.getScore());
        row.add(formatElapsed(vo.getElapsedMillis()));
        row.add(STATUS_LABELS.getOrDefault(vo.getStatus(), vo.getStatus()));
        row.add(vo.getInvalidatedReason());
        row.add(formatExportTime(vo.getStartedAt()));
        row.add(formatExportTime(vo.getSubmittedAt()));
        row.add(vo.getSignUpRemark());
        row.add(formatExportTime(signUp == null ? null : signUp.getCreateTime()));
        row.add(formatExportTime(vo.getCreateTime()));

        for (JSONObject field : customFields) {
            row.add(extractSignUpRemarkValue(remarkJson, field));
        }
        return row;
    }

    private List<JSONObject> collectCustomFields(Collection<QuizForumActivityDO> activities) {
        Map<String, JSONObject> fields = new LinkedHashMap<>();
        for (QuizForumActivityDO activity : activities) {
            for (JSONObject field : parseActivityCustomFields(activity)) {
                String key = field.getStr("key");
                if (StrUtil.isNotBlank(key) && !fields.containsKey(key)) {
                    fields.put(key, field);
                }
            }
        }
        return new ArrayList<>(fields.values());
    }

    private List<JSONObject> parseActivityCustomFields(QuizForumActivityDO activity) {
        if (activity == null || StrUtil.isBlank(activity.getCustomFields())
                || "[]".equals(activity.getCustomFields().trim())) {
            return Collections.emptyList();
        }
        try {
            JSONArray array = JSONUtil.parseArray(activity.getCustomFields());
            List<JSONObject> fields = new ArrayList<>();
            for (Object item : array) {
                if (item instanceof JSONObject) {
                    JSONObject field = (JSONObject) item;
                    if (StrUtil.isNotBlank(field.getStr("key"))) {
                        fields.add(field);
                    }
                }
            }
            return fields;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private JSONObject parseSignUpRemark(String remark) {
        if (StrUtil.isBlank(remark)) {
            return new JSONObject();
        }
        try {
            Object parsed = JSONUtil.parse(remark);
            return parsed instanceof JSONObject ? (JSONObject) parsed : new JSONObject();
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    private Object extractSignUpRemarkValue(JSONObject remarkJson, JSONObject field) {
        String key = field.getStr("key");
        if (StrUtil.isBlank(key) || remarkJson == null || !remarkJson.containsKey(key)) {
            return null;
        }
        Object value = remarkJson.get(key);
        if (value == null) {
            return null;
        }
        if ("file".equals(field.getStr("type"))) {
            if (value instanceof JSONObject) {
                JSONObject file = (JSONObject) value;
                return StrUtil.blankToDefault(file.getStr("url"), file.getStr("name"));
            }
            return value.toString();
        }
        if (value instanceof JSONArray) {
            return ((JSONArray) value).stream()
                    .map(Object::toString)
                    .collect(Collectors.joining("、"));
        }
        if (value instanceof JSONObject) {
            JSONObject object = (JSONObject) value;
            return StrUtil.blankToDefault(object.getStr("url"), object.toString());
        }
        return value;
    }

    private String formatElapsed(Long elapsedMillis) {
        if (elapsedMillis == null) {
            return null;
        }
        long seconds = elapsedMillis / 1000;
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return minutes + "分" + String.format("%02d", remainingSeconds) + "秒";
    }

    private String formatExportTime(LocalDateTime time) {
        return time == null ? null : time.format(DateTimeFormatter.ofPattern(FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND));
    }

    private String firstText(String first, String second) {
        return StringUtils.hasText(first) ? first : second;
    }

    private String buildSignUpKey(Long activityId, Long userId) {
        return activityId + ":" + userId;
    }

    private <T> List<T> safeList(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    private static class EnrichmentContext {
        private Map<Long, MemberUserRespDTO> userMap = Collections.emptyMap();
        private Map<Long, ForumUserProfileDTO> profileMap = Collections.emptyMap();
        private Map<Long, QuizForumActivityDO> activityMap = Collections.emptyMap();
        private Map<String, QuizForumActivitySignUpDO> signUpMap = Collections.emptyMap();
        private Map<Long, Map<Long, Integer>> rankMap = Collections.emptyMap();
    }
}
