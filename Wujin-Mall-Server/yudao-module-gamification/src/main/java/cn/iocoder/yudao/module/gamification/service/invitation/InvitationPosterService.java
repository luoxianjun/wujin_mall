package cn.iocoder.yudao.module.gamification.service.invitation;

/**
 * 邀请海报 Service 接口
 *
 * @author 芋道源码
 */
public interface InvitationPosterService {

    /**
     * 生成邀请海报
     *
     * @param userId 用户ID
     * @return 海报图片URL
     */
    String generatePoster(Long userId);

    /**
     * 清除用户的海报缓存
     *
     * @param userId 用户ID
     */
    void clearPosterCache(Long userId);
}
