package cn.iocoder.yudao.module.gamification.service.invitation;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.forum.api.user.ForumUserProfileApi;
import cn.iocoder.yudao.module.forum.api.user.dto.ForumUserProfileDTO;
import cn.iocoder.yudao.module.gamification.controller.app.invitation.vo.AppInvitationRecordPageReqVO;
import cn.iocoder.yudao.module.gamification.controller.app.invitation.vo.AppInvitationRecordRespVO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.invitation.InvitationRelationDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.invitation.InvitationRelationMapper;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Import(InvitationRecordServiceImpl.class)
class InvitationRecordServiceTest extends BaseDbUnitTest {

    @Resource
    private InvitationRecordService invitationRecordService;

    @Resource
    private InvitationRelationMapper invitationRelationMapper;

    @MockBean
    private MemberUserApi memberUserApi;

    @MockBean
    private ForumUserProfileApi forumUserProfileApi;

    @Test
    void getMyInvitationRecordPage_returnsOnlyCurrentInviterRowsWithStatusFlags() {
        invitationRelationMapper.insert(InvitationRelationDO.builder()
                .inviterId(100L)
                .inviteeId(201L)
                .invitationCode("INV-1")
                .status(InvitationRelationDO.STATUS_PENDING)
                .registerTime(LocalDateTime.of(2026, 4, 3, 14, 53, 55))
                .build());
        invitationRelationMapper.insert(InvitationRelationDO.builder()
                .inviterId(100L)
                .inviteeId(202L)
                .invitationCode("INV-2")
                .status(InvitationRelationDO.STATUS_COMPLETED)
                .registerTime(LocalDateTime.of(2026, 4, 4, 10, 0, 0))
                .completeTime(LocalDateTime.of(2026, 4, 5, 9, 0, 0))
                .build());
        invitationRelationMapper.insert(InvitationRelationDO.builder()
                .inviterId(101L)
                .inviteeId(203L)
                .invitationCode("INV-3")
                .status(InvitationRelationDO.STATUS_COMPLETED)
                .registerTime(LocalDateTime.of(2026, 4, 6, 10, 0, 0))
                .build());

        Map<Long, MemberUserRespDTO> memberUserMap = new HashMap<>();
        MemberUserRespDTO memberA = new MemberUserRespDTO();
        memberA.setId(201L);
        memberA.setNickname("会员昵称A");
        memberUserMap.put(201L, memberA);
        MemberUserRespDTO memberB = new MemberUserRespDTO();
        memberB.setId(202L);
        memberB.setNickname("会员昵称B");
        memberUserMap.put(202L, memberB);
        when(memberUserApi.getUserMap(anyCollection())).thenReturn(memberUserMap);

        Map<Long, ForumUserProfileDTO> forumProfileMap = new HashMap<>();
        ForumUserProfileDTO profileA = new ForumUserProfileDTO();
        profileA.setUserId(201L);
        profileA.setNickname("论坛昵称A");
        profileA.setSchoolEmailVerified(Boolean.FALSE);
        forumProfileMap.put(201L, profileA);
        ForumUserProfileDTO profileB = new ForumUserProfileDTO();
        profileB.setUserId(202L);
        profileB.setSchoolEmailVerified(Boolean.TRUE);
        forumProfileMap.put(202L, profileB);
        when(forumUserProfileApi.getUserProfileMapByUserIds(anyCollection())).thenReturn(forumProfileMap);

        AppInvitationRecordPageReqVO reqVO = new AppInvitationRecordPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);

        PageResult<AppInvitationRecordRespVO> pageResult =
                invitationRecordService.getMyInvitationRecordPage(100L, reqVO);

        assertEquals(2L, pageResult.getTotal());
        assertEquals(2, pageResult.getList().size());
        assertEquals("会员昵称B", pageResult.getList().get(0).getInviteeNickname());
        assertTrue(pageResult.getList().get(0).getVerified());
        assertTrue(pageResult.getList().get(0).getCompleted());
        assertEquals("论坛昵称A", pageResult.getList().get(1).getInviteeNickname());
        assertFalse(pageResult.getList().get(1).getVerified());
        assertFalse(pageResult.getList().get(1).getCompleted());
    }

    @Test
    void getMyInvitationRecordPage_fallsBackToDefaultNicknameWhenProfilesMissing() {
        invitationRelationMapper.insert(InvitationRelationDO.builder()
                .inviterId(300L)
                .inviteeId(401L)
                .invitationCode("INV-4")
                .status(InvitationRelationDO.STATUS_PENDING)
                .registerTime(LocalDateTime.of(2026, 4, 7, 8, 0, 0))
                .build());
        when(memberUserApi.getUserMap(anyCollection())).thenReturn(new HashMap<>());
        when(forumUserProfileApi.getUserProfileMapByUserIds(anyCollection())).thenReturn(new HashMap<>());

        AppInvitationRecordPageReqVO reqVO = new AppInvitationRecordPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);

        PageResult<AppInvitationRecordRespVO> pageResult =
                invitationRecordService.getMyInvitationRecordPage(300L, reqVO);

        assertEquals(1, pageResult.getList().size());
        assertEquals("好友用户", pageResult.getList().get(0).getInviteeNickname());
        assertFalse(pageResult.getList().get(0).getVerified());
        assertFalse(pageResult.getList().get(0).getCompleted());
    }

    @Test
    void getMyInvitationRecordPage_filtersNullInviteeIdsBeforeBatchLookup() {
        InvitationRelationMapper mapper = mock(InvitationRelationMapper.class);
        MemberUserApi localMemberUserApi = mock(MemberUserApi.class);
        ForumUserProfileApi localForumUserProfileApi = mock(ForumUserProfileApi.class);
        InvitationRecordServiceImpl service = new InvitationRecordServiceImpl();
        ReflectionTestUtils.setField(service, "invitationRelationMapper", mapper);
        ReflectionTestUtils.setField(service, "memberUserApi", localMemberUserApi);
        ReflectionTestUtils.setField(service, "forumUserProfileApi", localForumUserProfileApi);

        InvitationRelationDO nullInviteeRelation = InvitationRelationDO.builder()
                .id(1L)
                .inviterId(500L)
                .inviteeId(null)
                .invitationCode("NULL001")
                .status(InvitationRelationDO.STATUS_PENDING)
                .registerTime(LocalDateTime.of(2026, 4, 8, 10, 0, 0))
                .build();
        InvitationRelationDO normalRelation = InvitationRelationDO.builder()
                .id(2L)
                .inviterId(500L)
                .inviteeId(501L)
                .invitationCode("OK0001")
                .status(InvitationRelationDO.STATUS_COMPLETED)
                .registerTime(LocalDateTime.of(2026, 4, 8, 9, 0, 0))
                .build();

        AppInvitationRecordPageReqVO reqVO = new AppInvitationRecordPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        when(mapper.selectPageByInviterId(any(AppInvitationRecordPageReqVO.class), eq(500L)))
                .thenReturn(new PageResult<>(Arrays.asList(nullInviteeRelation, normalRelation), 2L));
        when(localMemberUserApi.getUserMap(anyCollection())).thenReturn(Collections.emptyMap());
        when(localForumUserProfileApi.getUserProfileMapByUserIds(anyCollection())).thenReturn(Collections.emptyMap());

        service.getMyInvitationRecordPage(500L, reqVO);

        org.mockito.ArgumentCaptor<Collection<Long>> memberCaptor = org.mockito.ArgumentCaptor.forClass(Collection.class);
        verify(localMemberUserApi).getUserMap(memberCaptor.capture());
        Collection<Long> memberLookupIds = memberCaptor.getValue();
        assertEquals(Collections.singleton(501L), memberLookupIds.stream().collect(Collectors.toSet()));

        org.mockito.ArgumentCaptor<Collection<Long>> forumCaptor = org.mockito.ArgumentCaptor.forClass(Collection.class);
        verify(localForumUserProfileApi).getUserProfileMapByUserIds(forumCaptor.capture());
        Collection<Long> forumLookupIds = forumCaptor.getValue();
        assertEquals(Collections.singleton(501L), forumLookupIds.stream().collect(Collectors.toSet()));
    }

    @Test
    void getMyInvitationRecordPage_ordersDeterministicallyWhenRegisterTimeSame() {
        LocalDateTime sameTime = LocalDateTime.of(2026, 4, 9, 10, 0, 0);
        InvitationRelationDO first = InvitationRelationDO.builder()
                .inviterId(600L)
                .inviteeId(601L)
                .invitationCode("ORD0001")
                .status(InvitationRelationDO.STATUS_PENDING)
                .registerTime(sameTime)
                .build();
        invitationRelationMapper.insert(first);
        InvitationRelationDO second = InvitationRelationDO.builder()
                .inviterId(600L)
                .inviteeId(602L)
                .invitationCode("ORD0002")
                .status(InvitationRelationDO.STATUS_PENDING)
                .registerTime(sameTime)
                .build();
        invitationRelationMapper.insert(second);

        when(memberUserApi.getUserMap(anyCollection())).thenReturn(Collections.emptyMap());
        when(forumUserProfileApi.getUserProfileMapByUserIds(anyCollection())).thenReturn(Collections.emptyMap());

        AppInvitationRecordPageReqVO reqVO = new AppInvitationRecordPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);

        PageResult<AppInvitationRecordRespVO> pageResult = invitationRecordService.getMyInvitationRecordPage(600L, reqVO);

        assertEquals(2, pageResult.getList().size());
        assertTrue(pageResult.getList().get(0).getId() > pageResult.getList().get(1).getId());
    }

    @Test
    void getMyInvitationRecordPage_handlesNullBatchLookupResults() {
        invitationRelationMapper.insert(InvitationRelationDO.builder()
                .inviterId(700L)
                .inviteeId(701L)
                .invitationCode("NMAP001")
                .status(InvitationRelationDO.STATUS_PENDING)
                .registerTime(LocalDateTime.of(2026, 4, 8, 8, 0, 0))
                .build());
        when(memberUserApi.getUserMap(anyCollection())).thenReturn(null);
        when(forumUserProfileApi.getUserProfileMapByUserIds(anyCollection())).thenReturn(null);

        AppInvitationRecordPageReqVO reqVO = new AppInvitationRecordPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);

        PageResult<AppInvitationRecordRespVO> pageResult =
                invitationRecordService.getMyInvitationRecordPage(700L, reqVO);

        assertEquals(1, pageResult.getList().size());
        assertEquals("好友用户", pageResult.getList().get(0).getInviteeNickname());
        assertFalse(pageResult.getList().get(0).getVerified());
    }

    @Test
    void getMyInvitationRecordPage_appliesTrimmedNicknameFallbackRules() {
        invitationRelationMapper.insert(InvitationRelationDO.builder()
                .inviterId(800L)
                .inviteeId(801L)
                .invitationCode("TRIM001")
                .status(InvitationRelationDO.STATUS_PENDING)
                .registerTime(LocalDateTime.of(2026, 4, 8, 10, 0, 0))
                .build());
        invitationRelationMapper.insert(InvitationRelationDO.builder()
                .inviterId(800L)
                .inviteeId(802L)
                .invitationCode("TRIM002")
                .status(InvitationRelationDO.STATUS_PENDING)
                .registerTime(LocalDateTime.of(2026, 4, 8, 9, 0, 0))
                .build());

        Map<Long, MemberUserRespDTO> memberUserMap = new HashMap<>();
        MemberUserRespDTO memberA = new MemberUserRespDTO();
        memberA.setId(801L);
        memberA.setNickname("  会员昵称A  ");
        memberUserMap.put(801L, memberA);
        MemberUserRespDTO memberB = new MemberUserRespDTO();
        memberB.setId(802L);
        memberB.setNickname("  会员昵称B  ");
        memberUserMap.put(802L, memberB);
        when(memberUserApi.getUserMap(anyCollection())).thenReturn(memberUserMap);

        Map<Long, ForumUserProfileDTO> forumProfileMap = new HashMap<>();
        ForumUserProfileDTO profileA = new ForumUserProfileDTO();
        profileA.setUserId(801L);
        profileA.setNickname("   ");
        forumProfileMap.put(801L, profileA);
        ForumUserProfileDTO profileB = new ForumUserProfileDTO();
        profileB.setUserId(802L);
        profileB.setNickname("  论坛昵称B  ");
        forumProfileMap.put(802L, profileB);
        when(forumUserProfileApi.getUserProfileMapByUserIds(anyCollection())).thenReturn(forumProfileMap);

        AppInvitationRecordPageReqVO reqVO = new AppInvitationRecordPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);

        PageResult<AppInvitationRecordRespVO> pageResult =
                invitationRecordService.getMyInvitationRecordPage(800L, reqVO);

        assertEquals(2, pageResult.getList().size());
        assertEquals("会员昵称A", pageResult.getList().get(0).getInviteeNickname());
        assertEquals("论坛昵称B", pageResult.getList().get(1).getInviteeNickname());
    }

    @Test
    void getMyInvitationRecordPage_doesNotLookupNullInviteeOnNullHostileMaps() {
        InvitationRelationMapper mapper = mock(InvitationRelationMapper.class);
        MemberUserApi localMemberUserApi = mock(MemberUserApi.class);
        ForumUserProfileApi localForumUserProfileApi = mock(ForumUserProfileApi.class);
        InvitationRecordServiceImpl service = new InvitationRecordServiceImpl();
        ReflectionTestUtils.setField(service, "invitationRelationMapper", mapper);
        ReflectionTestUtils.setField(service, "memberUserApi", localMemberUserApi);
        ReflectionTestUtils.setField(service, "forumUserProfileApi", localForumUserProfileApi);

        InvitationRelationDO nullInviteeRelation = InvitationRelationDO.builder()
                .id(11L)
                .inviterId(900L)
                .inviteeId(null)
                .invitationCode("NULL900")
                .status(InvitationRelationDO.STATUS_PENDING)
                .registerTime(LocalDateTime.of(2026, 4, 10, 10, 0, 0))
                .build();
        InvitationRelationDO normalRelation = InvitationRelationDO.builder()
                .id(12L)
                .inviterId(900L)
                .inviteeId(901L)
                .invitationCode("OK0901")
                .status(InvitationRelationDO.STATUS_COMPLETED)
                .registerTime(LocalDateTime.of(2026, 4, 10, 9, 0, 0))
                .build();
        when(mapper.selectPageByInviterId(any(AppInvitationRecordPageReqVO.class), eq(900L)))
                .thenReturn(new PageResult<>(Arrays.asList(nullInviteeRelation, normalRelation), 2L));

        Map<Long, MemberUserRespDTO> memberUserMap = new ConcurrentHashMap<>();
        MemberUserRespDTO member = new MemberUserRespDTO();
        member.setId(901L);
        member.setNickname("成员901");
        memberUserMap.put(901L, member);
        when(localMemberUserApi.getUserMap(anyCollection())).thenReturn(memberUserMap);

        Map<Long, ForumUserProfileDTO> forumProfileMap = new ConcurrentHashMap<>();
        ForumUserProfileDTO profile = new ForumUserProfileDTO();
        profile.setUserId(901L);
        profile.setNickname("论坛901");
        profile.setSchoolEmailVerified(Boolean.TRUE);
        forumProfileMap.put(901L, profile);
        when(localForumUserProfileApi.getUserProfileMapByUserIds(anyCollection())).thenReturn(forumProfileMap);

        AppInvitationRecordPageReqVO reqVO = new AppInvitationRecordPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);

        PageResult<AppInvitationRecordRespVO> result = service.getMyInvitationRecordPage(900L, reqVO);

        assertEquals(2, result.getList().size());
        assertEquals("好友用户", result.getList().get(0).getInviteeNickname());
        assertEquals("论坛901", result.getList().get(1).getInviteeNickname());
    }
}
