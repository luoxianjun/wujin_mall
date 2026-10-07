package cn.iocoder.yudao.module.gamification.service.points;

import cn.iocoder.yudao.module.member.api.point.MemberPointApi;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 游戏化积分服务
 * 封装积分发放、扣除、查询等操作
 *
 * @author gamification
 */
@Service
public class GamificationPointsService {

    private static final Logger log = LoggerFactory.getLogger(GamificationPointsService.class);

    @Resource
    private MemberUserApi memberUserApi;

    @Resource
    private MemberPointApi memberPointApi;

    /**
     * 发放积分
     *
     * @param userId 用户ID
     * @param points 积分数量（正数）
     * @param bizType 业务类型（如：INVITATION_REGISTER）
     * @param bizId 业务ID
     * @param description 描述
     * @return 是否成功
     */
    public boolean grantPoints(Long userId, Integer points, Integer bizType, Long bizId, String description) {
        if (userId == null || points == null || points <= 0) {
            log.warn("[grantPoints] Invalid parameters: userId={}, points={}", userId, points);
            return false;
        }

        try {
            memberPointApi.addPoint(userId, points, bizType, String.valueOf(bizId));
            log.info("[grantPoints] Success: userId={}, points={}, bizType={}, bizId={}, desc={}",
                userId, points, bizType, bizId, description);
            return true;
        } catch (Exception e) {
            log.error("[grantPoints] Exception: userId={}, points={}", userId, points, e);
            return false;
        }
    }

    /**
     * 扣除积分
     *
     * @param userId 用户ID
     * @param points 积分数量（正数）
     * @param bizType 业务类型
     * @param bizId 业务ID
     * @param description 描述
     * @return 是否成功
     */
    public boolean deductPoints(Long userId, Integer points, Integer bizType, Long bizId, String description) {
        if (userId == null || points == null || points <= 0) {
            log.warn("[deductPoints] Invalid parameters: userId={}, points={}", userId, points);
            return false;
        }

        try {
            memberPointApi.reducePoint(userId, points, bizType, String.valueOf(bizId));
            log.info("[deductPoints] Success: userId={}, points={}, bizType={}, bizId={}, desc={}",
                userId, points, bizType, bizId, description);
            return true;
        } catch (Exception e) {
            log.error("[deductPoints] Exception: userId={}, points={}", userId, points, e);
            return false;
        }
    }

    /**
     * 查询用户积分
     *
     * @param userId 用户ID
     * @return 积分数量，失败返回null
     */
    public Integer getUserPoints(Long userId) {
        if (userId == null) {
            log.warn("[getUserPoints] Invalid userId: null");
            return null;
        }

        try {
            MemberUserRespDTO user = memberUserApi.getUser(userId);
            return user != null ? user.getPoint() : null;
        } catch (Exception e) {
            log.error("[getUserPoints] Exception: userId={}", userId, e);
            return null;
        }
    }

    /**
     * 批量发放积分
     *
     * @param userIds 用户ID列表
     * @param points 积分数量
     * @param bizType 业务类型
     * @param description 描述
     * @return 成功数量
     */
    public int batchGrantPoints(java.util.List<Long> userIds, Integer points, Integer bizType, String description) {
        if (userIds == null || userIds.isEmpty() || points == null || points <= 0) {
            log.warn("[batchGrantPoints] Invalid parameters");
            return 0;
        }

        int successCount = 0;
        for (Long userId : userIds) {
            if (grantPoints(userId, points, bizType, null, description)) {
                successCount++;
            }
        }

        log.info("[batchGrantPoints] Completed: total={}, success={}, bizType={}",
            userIds.size(), successCount, bizType);
        return successCount;
    }
}
