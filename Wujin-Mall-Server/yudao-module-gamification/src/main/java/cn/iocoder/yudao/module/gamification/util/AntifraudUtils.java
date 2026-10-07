package cn.iocoder.yudao.module.gamification.util;

import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

/**
 * 防刷工具类
 * 提供设备指纹、IP限流、实名认证检查、邀请上限检查等功能
 *
 * @author gamification
 */
@Component
public class AntifraudUtils {

    private static final Logger log = LoggerFactory.getLogger(AntifraudUtils.class);

    private static final String REDIS_KEY_IP_LIMIT = "gamification:antifraud:ip:";
    private static final String REDIS_KEY_DEVICE_LIMIT = "gamification:antifraud:device:";
    private static final int IP_LIMIT_EXPIRE_HOURS = 24;
    private static final int DEVICE_LIMIT_EXPIRE_HOURS = 24;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private MemberUserApi memberUserApi;

    /**
     * 生成设备指纹
     * 基于设备信息生成唯一标识
     *
     * @param deviceInfo 设备信息（如：userAgent, deviceId等）
     * @return 设备指纹
     */
    public String generateDeviceFingerprint(String deviceInfo) {
        if (deviceInfo == null || deviceInfo.trim().isEmpty()) {
            return null;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(deviceInfo.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("[generateDeviceFingerprint] Failed to generate fingerprint", e);
            return null;
        }
    }

    /**
     * 检查IP是否超限
     *
     * @param ip IP地址
     * @param maxCount 最大次数
     * @return true-未超限，false-已超限
     */
    public boolean checkIpLimit(String ip, int maxCount) {
        if (ip == null || ip.trim().isEmpty()) {
            log.warn("[checkIpLimit] Invalid IP: null or empty");
            return true; // 降级处理，允许通过
        }

        try {
            String key = REDIS_KEY_IP_LIMIT + ip;
            RBucket<Integer> bucket = redissonClient.getBucket(key);
            Integer count = bucket.get();

            if (count == null) {
                count = 0;
            }

            if (count >= maxCount) {
                log.warn("[checkIpLimit] IP limit exceeded: ip={}, count={}, maxCount={}", ip, count, maxCount);
                return false;
            }

            // 增加计数
            bucket.set(count + 1, IP_LIMIT_EXPIRE_HOURS, TimeUnit.HOURS);
            log.info("[checkIpLimit] IP check passed: ip={}, count={}, maxCount={}", ip, count + 1, maxCount);
            return true;

        } catch (Exception e) {
            log.error("[checkIpLimit] Redis error, degrading to allow: ip={}", ip, e);
            return true; // Redis故障时降级处理
        }
    }

    /**
     * 检查设备是否超限
     *
     * @param deviceFingerprint 设备指纹
     * @param maxCount 最大次数
     * @return true-未超限，false-已超限
     */
    public boolean checkDeviceLimit(String deviceFingerprint, int maxCount) {
        if (deviceFingerprint == null || deviceFingerprint.trim().isEmpty()) {
            log.warn("[checkDeviceLimit] Invalid device fingerprint: null or empty");
            return true; // 降级处理，允许通过
        }

        try {
            String key = REDIS_KEY_DEVICE_LIMIT + deviceFingerprint;
            RBucket<Integer> bucket = redissonClient.getBucket(key);
            Integer count = bucket.get();

            if (count == null) {
                count = 0;
            }

            if (count >= maxCount) {
                log.warn("[checkDeviceLimit] Device limit exceeded: device={}, count={}, maxCount={}",
                    deviceFingerprint, count, maxCount);
                return false;
            }

            // 增加计数
            bucket.set(count + 1, DEVICE_LIMIT_EXPIRE_HOURS, TimeUnit.HOURS);
            log.info("[checkDeviceLimit] Device check passed: device={}, count={}, maxCount={}",
                deviceFingerprint, count + 1, maxCount);
            return true;

        } catch (Exception e) {
            log.error("[checkDeviceLimit] Redis error, degrading to allow: device={}", deviceFingerprint, e);
            return true; // Redis故障时降级处理
        }
    }

    /**
     * 检查用户是否实名认证
     *
     * @param userId 用户ID
     * @return true-已认证，false-未认证
     */
    public boolean checkRealNameVerified(Long userId) {
        if (userId == null) {
            log.warn("[checkRealNameVerified] Invalid userId: null");
            return false;
        }

        try {
            MemberUserRespDTO user = memberUserApi.getUser(userId);
            boolean verified = user != null && user.getName() != null && !user.getName().trim().isEmpty();
            log.info("[checkRealNameVerified] userId={}, verified={}", userId, verified);
            return verified;
        } catch (Exception e) {
            log.error("[checkRealNameVerified] Exception: userId={}", userId, e);
            return false;
        }
    }

    /**
     * 检查邀请人数是否超限
     *
     * @param userId 用户ID
     * @param currentCount 当前邀请人数
     * @param maxCount 最大邀请人数
     * @return true-未超限，false-已超限
     */
    public boolean checkInvitationLimit(Long userId, int currentCount, int maxCount) {
        if (userId == null) {
            log.warn("[checkInvitationLimit] Invalid userId: null");
            return false;
        }

        if (currentCount >= maxCount) {
            log.warn("[checkInvitationLimit] Invitation limit exceeded: userId={}, currentCount={}, maxCount={}",
                userId, currentCount, maxCount);
            return false;
        }

        log.info("[checkInvitationLimit] Check passed: userId={}, currentCount={}, maxCount={}",
            userId, currentCount, maxCount);
        return true;
    }

    /**
     * 重置IP限制（管理员操作）
     *
     * @param ip IP地址
     */
    public void resetIpLimit(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return;
        }

        try {
            String key = REDIS_KEY_IP_LIMIT + ip;
            redissonClient.getBucket(key).delete();
            log.info("[resetIpLimit] IP limit reset: ip={}", ip);
        } catch (Exception e) {
            log.error("[resetIpLimit] Failed to reset IP limit: ip={}", ip, e);
        }
    }

    /**
     * 重置设备限制（管理员操作）
     *
     * @param deviceFingerprint 设备指纹
     */
    public void resetDeviceLimit(String deviceFingerprint) {
        if (deviceFingerprint == null || deviceFingerprint.trim().isEmpty()) {
            return;
        }

        try {
            String key = REDIS_KEY_DEVICE_LIMIT + deviceFingerprint;
            redissonClient.getBucket(key).delete();
            log.info("[resetDeviceLimit] Device limit reset: device={}", deviceFingerprint);
        } catch (Exception e) {
            log.error("[resetDeviceLimit] Failed to reset device limit: device={}", deviceFingerprint, e);
        }
    }
}
