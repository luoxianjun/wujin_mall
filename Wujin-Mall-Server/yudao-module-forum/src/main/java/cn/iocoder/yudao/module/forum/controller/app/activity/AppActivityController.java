package cn.iocoder.yudao.module.forum.controller.app.activity;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.forum.controller.app.activity.vo.*;
import cn.iocoder.yudao.module.forum.controller.app.activity.vo.AppActivitySimpleRespVO;
import cn.iocoder.yudao.module.forum.service.activity.ForumActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 论坛活动 Controller
 *
 * @author forum
 */
@Tag(name = "用户 APP - 论坛活动")
@RestController
@RequestMapping("/forum/activity")
@Validated
public class AppActivityController {

    @Resource
    private ForumActivityService activityService;

    @GetMapping("/get")
    @Operation(summary = "获取活动详情")
    @Parameter(name = "id", description = "活动ID", required = true, example = "1")
    public CommonResult<AppActivityRespVO> getActivity(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(activityService.getActivity(id, userId));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询活动")
    public CommonResult<PageResult<AppActivityRespVO>> getActivityPage(@Valid AppActivityPageReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(activityService.getVisibleActivityPage(reqVO, userId));
    }

    @GetMapping("/hot")
    @Operation(summary = "热门活动列表", description = "按热度降序返回活动封面、标题、描述，默认10条，最大50条")
    public CommonResult<java.util.List<AppActivitySimpleRespVO>> getHotActivities(
            @Parameter(description = "返回数量，默认10，最大50", example = "10") @RequestParam(value = "limit", required = false) Integer limit) {
        return success(activityService.getHotActivities(limit));
    }

    @PostMapping("/sign-up")
    @Operation(summary = "报名活动")
    @PreAuthenticated
    public CommonResult<Boolean> signUpActivity(@Valid @RequestBody AppActivitySignUpReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        activityService.signUpActivity(userId, reqVO);
        return success(true);
    }

    @PostMapping("/cancel-sign-up")
    @Operation(summary = "取消报名")
    @Parameter(name = "activityId", description = "活动ID", required = true, example = "1")
    @PreAuthenticated
    public CommonResult<Boolean> cancelSignUp(@RequestParam("activityId") Long activityId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        activityService.cancelSignUp(activityId, userId);
        return success(true);
    }

    @PostMapping("/check-in")
    @Operation(summary = "活动签到")
    @PreAuthenticated
    public CommonResult<Boolean> checkInActivity(@Valid @RequestBody AppActivityCheckInReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        activityService.checkInActivity(userId, reqVO);
        return success(true);
    }

    @GetMapping("/check-in/qrcode")
    @Operation(summary = "获取签到二维码内容")
    @Parameter(name = "activityId", description = "活动ID", required = true, example = "1")
    @PreAuthenticated
    public CommonResult<AppActivityCheckInQrRespVO> getCheckInQrCode(@RequestParam("activityId") Long activityId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String qrContent = activityService.generateCheckInQrContent(userId, activityId);
        AppActivityCheckInQrRespVO respVO = new AppActivityCheckInQrRespVO();
        respVO.setQrContent(qrContent);
        return success(respVO);
    }

    @PostMapping("/check-in/scan")
    @Operation(summary = "扫码签到（管理员）")
    @PreAuthenticated
    public CommonResult<Boolean> scanCheckIn(@Valid @RequestBody AppActivityScanCheckInReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        activityService.checkInActivityByQr(userId, reqVO.getQrContent());
        return success(true);
    }

    @PostMapping("/admin/check-in")
    @Operation(summary = "管理员扫码签到（指定活动）")
    @PreAuthenticated
    public CommonResult<Boolean> adminCheckIn(@Valid @RequestBody AppActivityAdminCheckInReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        activityService.checkInActivityByAdmin(userId, reqVO);
        return success(true);
    }

    @PostMapping("/admin/manual-check-in")
    @Operation(summary = "管理员手动签到")
    @PreAuthenticated
    public CommonResult<Boolean> adminManualCheckIn(@RequestParam("activityId") Long activityId,
            @RequestParam("signUpId") Long signUpId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        activityService.manualCheckIn(userId, activityId, signUpId);
        return success(true);
    }

    @GetMapping("/my-sign-up/page")
    @Operation(summary = "获取我的报名列表")
    @PreAuthenticated
    public CommonResult<PageResult<AppActivitySignUpRespVO>> getMySignUpPage(@Valid AppActivitySignUpPageReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(activityService.getMySignUpPage(userId, reqVO));
    }

    @GetMapping("/members/page")
    @Operation(summary = "获取活动报名人员列表")
    public CommonResult<PageResult<AppActivitySignUpRespVO>> getActivityMembersPage(
            @Valid AppActivitySignUpPageReqVO reqVO) {
        return success(activityService.getActivityMembersPage(reqVO));
    }

}
