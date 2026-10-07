package cn.iocoder.yudao.module.forum.controller.app.user;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;

import cn.iocoder.yudao.module.forum.controller.app.user.vo.*;
import cn.iocoder.yudao.module.forum.convert.user.ForumUserProfileConvert;
import cn.iocoder.yudao.module.forum.dal.dataobject.user.ForumUserProfileDO;
import cn.iocoder.yudao.module.forum.service.user.ForumUserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 用户 APP - 用户资料 Controller
 *
 * @author forum
 */
@Tag(name = "用户 APP - 用户资料")
@RestController
@RequestMapping("/forum/user/profile")
@Validated
@Slf4j
public class AppUserProfileController {

    @Resource
    private ForumUserProfileService userProfileService;

    @GetMapping("/get")
    @Operation(summary = "获取当前用户资料")
    public CommonResult<AppUserProfileRespVO> getUserProfile() {
        // 获取当前登录用户 ID
        Long userId = SecurityFrameworkUtils.getLoginUserId();

        // 获取或创建用户资料
        ForumUserProfileDO profile = userProfileService.getOrCreateUserProfile(userId);

        return success(ForumUserProfileConvert.INSTANCE.convert(profile));
    }

    @GetMapping("/get-by-uid")
    @Operation(summary = "根据 UID 获取用户资料")
    @Parameter(name = "uid", description = "论坛 UID", required = true, example = "U123456")
    public CommonResult<AppUserProfileRespVO> getUserProfileByUid(@RequestParam("uid") String uid) {
        ForumUserProfileDO profile = userProfileService.getUserProfileByUid(uid);
        return success(ForumUserProfileConvert.INSTANCE.convert(profile));
    }

    @GetMapping("/get-by-user-id")
    @Operation(summary = "根据用户 ID 获取用户资料")
    @Parameter(name = "userId", description = "用户 ID", required = true, example = "1024")
    public CommonResult<AppUserProfileWithImRespVO> getUserProfileByUserId(@RequestParam("userId") Long userId) {
        ForumUserProfileDO profile = userProfileService.getUserProfileByUserId(userId);
        return success(ForumUserProfileConvert.INSTANCE.convertWithIm(profile));
    }

    @GetMapping("/page")
    @Operation(summary = "获得会员用户分页")
    public CommonResult<PageResult<AppForumUserPageRespVO>> getMemberPage(@Valid AppForumUserPageReqVO reqVO) {
        return success(userProfileService.getMemberPage(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新用户资料")
    public CommonResult<Boolean> updateUserProfile(@Valid @RequestBody AppUserProfileUpdateReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        userProfileService.updateUserProfile(userId, reqVO);
        return success(true);
    }

    @PostMapping("/school/submit")
    @Operation(summary = "提交学校认证")
    public CommonResult<Boolean> submitSchoolVerification(@Valid @RequestBody AppSchoolVerificationReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        userProfileService.submitSchoolVerification(userId, reqVO);
        return success(true);
    }

    @PostMapping("/school/send-code")
    @Operation(summary = "发送学校邮箱验证码")
    public CommonResult<Boolean> sendSchoolEmailCode(@Valid @RequestBody AppSchoolEmailCodeReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        userProfileService.sendSchoolEmailCode(userId, reqVO.getEmail());
        return success(true);
    }

    @PostMapping("/school/verify-code")
    @Operation(summary = "验证学校邮箱验证码")
    public CommonResult<Boolean> verifySchoolEmailCode(@Valid @RequestBody AppSchoolEmailVerifyReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        userProfileService.verifySchoolEmailCode(userId, reqVO.getEmail(), reqVO.getCode());
        return success(true);
    }

    @PutMapping("/school/update-public")
    @Operation(summary = "更新学校信息公开设置")
    @Parameter(name = "schoolInfoPublic", description = "是否公开学校信息", required = true, example = "true")
    public CommonResult<Boolean> updateSchoolInfoPublic(@RequestParam("schoolInfoPublic") Boolean schoolInfoPublic) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        userProfileService.updateSchoolInfoPublic(userId, schoolInfoPublic);
        return success(true);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取用户统计信息")
    public CommonResult<AppUserStatisticsRespVO> getUserStatistics() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(userProfileService.getUserStatistics(userId));
    }

    @GetMapping("/center")
    @Operation(summary = "获取个人中心信息")
    public CommonResult<AppUserCenterRespVO> getUserCenter() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(userProfileService.getUserCenter(userId));
    }

    @PostMapping("/block")
    @Operation(summary = "拉黑用户")
    @Parameter(name = "targetUserId", description = "目标用户ID", required = true, example = "1024")
    public CommonResult<Boolean> blockUser(@RequestParam("targetUserId") Long targetUserId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        userProfileService.blockUser(userId, targetUserId);
        return success(true);
    }

    @PostMapping("/unblock")
    @Operation(summary = "取消拉黑用户")
    @Parameter(name = "targetUserId", description = "目标用户ID", required = true, example = "1024")
    public CommonResult<Boolean> unblockUser(@RequestParam("targetUserId") Long targetUserId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        userProfileService.unblockUser(userId, targetUserId);
        return success(true);
    }

    @GetMapping("/is-blocked")
    @Operation(summary = "检查是否已拉黑用户")
    @Parameter(name = "targetUserId", description = "目标用户ID", required = true, example = "1024")
    public CommonResult<Boolean> isBlocked(@RequestParam("targetUserId") Long targetUserId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(userProfileService.isBlocked(userId, targetUserId));
    }

    @PutMapping("/privacy-settings")
    @Operation(summary = "更新隐私设置")
    public CommonResult<Boolean> updatePrivacySettings(@Valid @RequestBody AppUserPrivacySettingsReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        userProfileService.updatePrivacySettings(userId, reqVO);
        return success(true);
    }

}
