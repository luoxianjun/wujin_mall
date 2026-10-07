package cn.iocoder.yudao.module.forum.api.user;

import cn.iocoder.yudao.module.forum.api.user.dto.ForumUserProfileDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 论坛用户资料 API 接口
 * 
 * 用于跨模块调用，例如 member 模块查询用户的论坛管理员状态
 *
 * @author forum
 */
public interface ForumUserProfileApi {

    /**
     * 根据用户 ID 获取用户资料
     *
     * @param userId 会员用户 ID
     * @return 用户资料 DTO，如果不存在则返回 null
     */
    ForumUserProfileDTO getUserProfileByUserId(Long userId);

    /**
     * 根据用户 ID 列表批量获取用户资料
     *
     * @param userIds 会员用户 ID 列表
     * @return 用户资料 Map，key 为 userId，value 为 ForumUserProfileDTO
     */
    Map<Long, ForumUserProfileDTO> getUserProfileMapByUserIds(Collection<Long> userIds);

    /**
     * 根据 UID 模糊搜索返回用户 ID 列表
     *
     * @param uid UID（模糊匹配）
     * @return 匹配的用户 ID 列表
     */
    List<Long> getUserIdsByUidLike(String uid);

}
