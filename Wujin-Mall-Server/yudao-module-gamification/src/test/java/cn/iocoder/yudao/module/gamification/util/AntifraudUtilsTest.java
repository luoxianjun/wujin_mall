package cn.iocoder.yudao.module.gamification.util;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Import(AntifraudUtils.class)
class AntifraudUtilsTest extends BaseDbUnitTest {

    @Resource
    private AntifraudUtils antifraudUtils;

    @MockBean
    private RedissonClient redissonClient;

    @MockBean
    private MemberUserApi memberUserApi;

    @Test
    void checkIpLimit_allowsWhenBelowLimit() {
        @SuppressWarnings("unchecked")
        RBucket<Integer> bucket = mock(RBucket.class);
        when(redissonClient.<Integer>getBucket("gamification:antifraud:ip:127.0.0.1")).thenReturn(bucket);
        when(bucket.get()).thenReturn(1);

        boolean allowed = antifraudUtils.checkIpLimit("127.0.0.1", 5);

        assertTrue(allowed);
        verify(bucket).set(2, 24, java.util.concurrent.TimeUnit.HOURS);
    }

    @Test
    void checkIpLimit_rejectsWhenLimitExceeded() {
        @SuppressWarnings("unchecked")
        RBucket<Integer> bucket = mock(RBucket.class);
        when(redissonClient.<Integer>getBucket("gamification:antifraud:ip:127.0.0.2")).thenReturn(bucket);
        when(bucket.get()).thenReturn(5);

        boolean allowed = antifraudUtils.checkIpLimit("127.0.0.2", 5);

        assertFalse(allowed);
    }

    @Test
    void checkDeviceLimit_rejectsWhenLimitExceeded() {
        @SuppressWarnings("unchecked")
        RBucket<Integer> bucket = mock(RBucket.class);
        when(redissonClient.<Integer>getBucket("gamification:antifraud:device:device-1")).thenReturn(bucket);
        when(bucket.get()).thenReturn(3);

        boolean allowed = antifraudUtils.checkDeviceLimit("device-1", 3);

        assertFalse(allowed);
    }

    @Test
    void checkRealNameVerified_returnsTrueWhenNamePresent() {
        MemberUserRespDTO user = new MemberUserRespDTO();
        user.setId(1L);
        user.setName("Verified User");
        when(memberUserApi.getUser(1L)).thenReturn(user);

        boolean verified = antifraudUtils.checkRealNameVerified(1L);

        assertTrue(verified);
    }

    @Test
    void checkIpLimit_degradesToAllowWhenRedisFails() {
        when(redissonClient.getBucket("gamification:antifraud:ip:127.0.0.9"))
                .thenThrow(new RuntimeException("redis down"));

        boolean allowed = antifraudUtils.checkIpLimit("127.0.0.9", 1);

        assertTrue(allowed);
    }
}
