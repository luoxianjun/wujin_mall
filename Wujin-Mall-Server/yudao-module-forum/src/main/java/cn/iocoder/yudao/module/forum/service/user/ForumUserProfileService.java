package cn.iocoder.yudao.module.forum.service.user;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.forum.controller.app.user.vo.*;

import cn.iocoder.yudao.module.forum.dal.dataobject.user.ForumUserProfileDO;

import javax.validation.Valid;

/**
 * 论坛用户资料 Service 接口
 *
 * @author forum
 */
public interface ForumUserProfileService {

    /**
     * 获取或创建用户资料
     * 
     * 当用户首次登录时，自动创建论坛用户资料
     *
     * @param userId 会员用户 ID
     * @return 用户资料
     */
    ForumUserProfileDO getOrCreateUserProfile(Long userId);

    /**
     * 根据用户 ID 获取用户资料
     *
     * @param userId 会员用户 ID
     * @return 用户资料
     */
    ForumUserProfileDO getUserProfileByUserId(Long userId);

    /**
     * 根据论坛 UID 获取用户资料
     *
     * @param uid 论坛 UID
     * @return 用户资料
     */
    ForumUserProfileDO getUserProfileByUid(String uid);

    /**
     * 更新用户基本信息
     *
     * @param userId 会员用户 ID
     * @param reqVO  更新请求
     */
    void updateUserProfile(Long userId, @Valid AppUserProfileUpdateReqVO reqVO);

    /**
     * 提交学校认证
     *
     * @param userId 会员用户 ID
     * @param reqVO  认证请求
     */
    void submitSchoolVerification(Long userId, @Valid AppSchoolVerificationReqVO reqVO);

    /**
     * 发送学校邮箱验证码
     *
     * @param userId 会员用户 ID
     * @param email  学校邮箱
     */
    void sendSchoolEmailCode(Long userId, String email);

    /**
     * 验证学校邮箱验证码
     *
     * @param userId 会员用户 ID
     * @param email  学校邮箱
     * @param code   验证码
     */
    void verifySchoolEmailCode(Long userId, String email, String code);

    /**
     * 更新学校信息公开设置
     *
     * @param userId           会员用户 ID
     * @param schoolInfoPublic 是否公开
     */
    void updateSchoolInfoPublic(Long userId, Boolean schoolInfoPublic);

    /**
     * 获取用户统计信息
     *
     * @param userId 会员用户 ID
     * @return 统计信息
     */
    AppUserStatisticsRespVO getUserStatistics(Long userId);

    /**
     * 获取个人中心信息
     *
     * @param userId 会员用户 ID
     * @return 个人中心信息
     */
    AppUserCenterRespVO getUserCenter(Long userId);

    /**
     * 增加发帖数量
     *
     * @param userId 会员用户 ID
     */
    void increasePostCount(Long userId);

    /**
     * 减少发帖数量
     *
     * @param userId 会员用户 ID
     */
    void decreasePostCount(Long userId);

    /**
     * 增加活动参与数量
     *
     * @param userId 会员用户 ID
     */
    void increaseActivityCount(Long userId);

    /**
     * 减少活动参与数量
     *
     * @param userId 会员用户 ID
     */
    void decreaseActivityCount(Long userId);

    /**
     * 增加获赞数量
     *
     * @param userId 会员用户 ID
     * @param count  增加数量
     */
    void increaseLikeCount(Long userId, Integer count);

    /**
     * 增加收藏数量
     *
     * @param userId 会员用户 ID
     * @param count  增加数量
     */
    void increaseFavoriteCount(Long userId, Integer count);

    /**
     * 获得会员用户分页
     *
     * @param reqVO 分页查询参数
     * @return 会员用户分页
     */
    PageResult<AppForumUserPageRespVO> getMemberPage(AppForumUserPageReqVO reqVO);

    /**
     * 拉黑用户
     *
     * @param userId       当前用户 ID
     * @param targetUserId 目标用户 ID
     */
    void blockUser(Long userId, Long targetUserId);

    /**
     * 取消拉黑用户
     *
     * @param userId       当前用户 ID
     * @param targetUserId 目标用户 ID
     */
    void unblockUser(Long userId, Long targetUserId);

    /**
     * 检查是否已拉黑用户
     *
     * @param userId       当前用户 ID
     * @param targetUserId 目标用户 ID
     * @return 是否已拉黑
     */
    boolean isBlocked(Long userId, Long targetUserId);

    /**
     * 更新隐私设置
     *
     * @param userId 会员用户 ID
     * @param reqVO  隐私设置请求
     */
    void updatePrivacySettings(Long userId, @Valid AppUserPrivacySettingsReqVO reqVO);

    // ========== 管理员功能 ==========

    /**
     * 获取论坛用户分页列表（管理端）
     *
     * @param reqVO 分页请求
     * @return 用户分页列表
     */
    PageResult<cn.iocoder.yudao.module.forum.controller.admin.user.vo.AdminUserProfileRespVO> getAdminUserProfilePage(
            cn.iocoder.yudao.module.forum.controller.admin.user.vo.AdminUserProfilePageReqVO reqVO);

    /**
     * 设置用户管理员状态
     *
     * @param userId  用户ID
     * @param isAdmin 是否为管理员
     */
    void setUserAdmin(Long userId, Boolean isAdmin);

    /**
     * 根据用户ID获取用户资料（管理端）
     *
     * @param userId 用户ID
     * @return 用户资料
     */
    cn.iocoder.yudao.module.forum.controller.admin.user.vo.AdminUserProfileRespVO getAdminUserProfileByUserId(Long userId);

    /**
     * 根据用户 ID 列表批量获取用户资料
     *
     * @param userIds 会员用户 ID 列表
     * @return 用户资料列表
     */
    java.util.List<ForumUserProfileDO> getUserProfileListByUserIds(java.util.Collection<Long> userIds);

    /**
     * 根据 UID 模糊搜索返回用户 ID 列表
     *
     * @param uid UID（模糊匹配）
     * @return 匹配的用户 ID 列表
     */
    java.util.List<Long> getUserIdsByUidLike(String uid);

}
