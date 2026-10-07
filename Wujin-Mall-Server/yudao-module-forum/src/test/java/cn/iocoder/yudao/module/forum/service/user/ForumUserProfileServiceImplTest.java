package cn.iocoder.yudao.module.forum.service.user;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.forum.dal.dataobject.user.ForumUserProfileDO;
import cn.iocoder.yudao.module.forum.dal.mysql.user.ForumUserProfileMapper;
import cn.iocoder.yudao.module.gamification.listener.MemberVerifiedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ForumUserProfileServiceImplTest extends BaseMockitoUnitTest {

    private ForumUserProfileServiceImpl userProfileService;

    @Mock
    private ForumUserProfileMapper userProfileMapper;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setUp() {
        userProfileService = new ForumUserProfileServiceImpl();
        ReflectionTestUtils.setField(userProfileService, "userProfileMapper", userProfileMapper);
        ReflectionTestUtils.setField(userProfileService, "stringRedisTemplate", stringRedisTemplate);
        ReflectionTestUtils.setField(userProfileService, "applicationEventPublisher", applicationEventPublisher);
    }

    @Test
    void verifySchoolEmailCode_publishesMemberVerifiedEvent() {
        Long userId = 1001L;
        String email = "student@example.edu";
        String code = "123456";
        ForumUserProfileDO profile = ForumUserProfileDO.builder()
                .id(10L)
                .userId(userId)
                .schoolEmailVerified(false)
                .build();
        when(userProfileMapper.selectByUserId(userId)).thenReturn(profile);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("forum:school:email:code:" + email)).thenReturn(code);

        userProfileService.verifySchoolEmailCode(userId, email, code);

        verify(userProfileMapper).updateById(any(ForumUserProfileDO.class));
        verify(stringRedisTemplate).delete("forum:school:email:code:" + email);
        ArgumentCaptor<ApplicationEvent> eventCaptor = ArgumentCaptor.forClass(ApplicationEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        MemberVerifiedEvent event = assertInstanceOf(MemberVerifiedEvent.class, eventCaptor.getValue());
        assertEquals(userId, event.getUserId());
    }
}
