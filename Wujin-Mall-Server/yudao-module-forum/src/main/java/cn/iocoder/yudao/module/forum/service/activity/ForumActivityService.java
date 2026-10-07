package cn.iocoder.yudao.module.forum.service.activity;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.forum.controller.admin.activity.vo.AdminActivityFeedbackReqVO;
import cn.iocoder.yudao.module.forum.controller.admin.activity.vo.AdminActivitySignUpExportData;
import cn.iocoder.yudao.module.forum.controller.admin.activity.vo.AdminActivitySignUpExcelVO;
import cn.iocoder.yudao.module.forum.controller.admin.activity.vo.AdminActivityUpdateReqVO;
import cn.iocoder.yudao.module.forum.controller.app.activity.vo.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 论坛活动 Service 接口
 *
 * @author forum
 */
public interface ForumActivityService {

    /**
     * 创建活动
     *
     * @param userId 用户ID
     * @param reqVO  创建请求
     * @return 活动ID
     */
    Long createActivity(Long userId, @Valid AppActivityCreateReqVO reqVO);

    /**
     * 编辑活动
     *
     * @param userId 用户ID
     * @param reqVO  编辑请求
     */
    void updateActivity(Long userId, @Valid AdminActivityUpdateReqVO reqVO);

    /**
     * 获取活动详情
     *
     * @param activityId 活动ID
     * @param userId     当前用户ID（可为空）
     * @return 活动详情
     */
    AppActivityRespVO getActivity(Long activityId, Long userId);

    /**
     * 分页查询活动
     *
     * @param reqVO  分页请求
     * @param userId 当前用户ID（可为空）
     * @return 活动分页
     */
    PageResult<AppActivityRespVO> getActivityPage(AppActivityPageReqVO reqVO, Long userId);

    PageResult<AppActivityRespVO> getVisibleActivityPage(AppActivityPageReqVO reqVO, Long userId);

    /**
     * 热门活动列表（简要信息）
     */
    java.util.List<AppActivitySimpleRespVO> getHotActivities(Integer limit);

    /**
     * 删除活动
     *
     * @param activityId 活动ID
     * @param userId     用户ID
     */
    void deleteActivity(Long activityId, Long userId);

    /**
     * 报名活动
     *
     * @param userId 用户ID
     * @param reqVO  报名请求
     */
    void signUpActivity(Long userId, @Valid AppActivitySignUpReqVO reqVO);

    /**
     * 取消报名
     *
     * @param activityId 活动ID
     * @param userId     用户ID
     */
    void cancelSignUp(Long activityId, Long userId);

    /**
     * 活动签到
     *
     * @param userId 用户ID
     * @param reqVO  签到请求
     */
    void checkInActivity(Long userId, @Valid AppActivityCheckInReqVO reqVO);

    /**
     * 获取签到二维码内容
     *
     * @param userId     用户ID
     * @param activityId 活动ID
     * @return 二维码内容
     */
    String generateCheckInQrContent(Long userId, Long activityId);

    /**
     * 管理员扫码签到
     *
     * @param userId    管理员用户ID（活动创建者）
     * @param qrContent 二维码内容
     */
    void checkInActivityByQr(Long userId, String qrContent);

    /**
     * 管理员扫码签到（指定活动）
     *
     * @param userId 管理员用户ID
     * @param reqVO  签到请求
     */
    void checkInActivityByAdmin(Long userId, AppActivityAdminCheckInReqVO reqVO);

    /**
     * 管理员手动签到（无需二维码）
     *
     * @param userId     管理员用户ID
     * @param activityId 活动ID
     * @param signUpId   报名ID
     */
    void manualCheckIn(Long userId, Long activityId, Long signUpId);

    /**
     * 增加浏览次数
     *
     * @param activityId 活动ID
     */
    void increaseViewCount(Long activityId);

    /**
     * 获取活动报名列表
     *
     * @param reqVO  分页请求
     * @param userId 当前用户ID（用于权限校验）
     * @return 报名列表
     */
    PageResult<AppActivitySignUpRespVO> getActivitySignUpPage(AppActivitySignUpPageReqVO reqVO, Long userId);

    /**
     * 获取活动成员列表（公开）
     *
     * @param reqVO 分页请求
     * @return 报名列表
     */
    PageResult<AppActivitySignUpRespVO> getActivityMembersPage(AppActivitySignUpPageReqVO reqVO);

    /**
     * 获取我的报名列表
     *
     * @param userId 用户ID
     * @param reqVO  分页请求
     * @return 报名列表
     */
    PageResult<AppActivitySignUpRespVO> getMySignUpPage(Long userId, AppActivitySignUpPageReqVO reqVO);

    /**
     * 审核报名申请
     *
     * @param userId 用户ID（活动创建者）
     * @param reqVO  审核请求
     */
    void approveSignUp(Long userId, AppActivityApprovalReqVO reqVO);

    /**
     * 点评报名记录
     *
     * @param userId 用户ID（活动创建者）
     * @param reqVO  点评请求
     */
    void feedbackSignUp(Long userId, AdminActivityFeedbackReqVO reqVO);

    /**
     * 通知2小时后开始的活动
     * 
     * @return 通知的用户数量
     */
    int notifyActivityStartIn2Hours();

    /**
     * 导出活动报名列表
     *
     * @param reqVO  查询条件
     * @param userId 当前用户ID（用于权限校验）
     * @return 报名列表（Excel格式）
     */
    List<AdminActivitySignUpExcelVO> getActivitySignUpExcelList(AppActivitySignUpPageReqVO reqVO, Long userId);

    /**
     * 导出活动报名列表，包含活动自定义字段动态列。
     *
     * @param reqVO  查询条件
     * @param userId 当前用户ID（用于权限校验）
     * @return 动态 Excel 表头和行数据
     */
    AdminActivitySignUpExportData getActivitySignUpExportData(AppActivitySignUpPageReqVO reqVO, Long userId);

    /**
     * 隐藏活动
     *
     * @param activityId 活动ID
     * @param userId     用户ID
     */
    void hideActivity(Long activityId, Long userId);

    /**
     * 展示已隐藏的活动
     *
     * @param activityId 活动ID
     * @param userId     用户ID
     */
    void showActivity(Long activityId, Long userId);

}
