package cn.iocoder.yudao.module.forum.controller.admin.activity;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.idev.excel.FastExcelFactory;
import cn.idev.excel.converters.longconverter.LongStringConverter;
import cn.iocoder.yudao.module.forum.controller.admin.activity.vo.AdminActivityFeedbackReqVO;
import cn.iocoder.yudao.module.forum.controller.admin.activity.vo.AdminActivitySignUpExportData;
import cn.iocoder.yudao.module.forum.controller.admin.activity.vo.AdminActivitySimpleRespVO;
import cn.iocoder.yudao.module.forum.controller.admin.activity.vo.AdminActivityUpdateReqVO;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.excel.core.handler.ColumnWidthMatchStyleStrategy;
import cn.iocoder.yudao.module.forum.controller.app.activity.vo.AppActivityApprovalReqVO;
import cn.iocoder.yudao.module.forum.controller.app.activity.vo.AppActivityCreateReqVO;
import cn.iocoder.yudao.module.forum.controller.app.activity.vo.AppActivityPageReqVO;
import cn.iocoder.yudao.module.forum.controller.app.activity.vo.AppActivityRespVO;
import cn.iocoder.yudao.module.forum.controller.app.activity.vo.AppActivitySignUpPageReqVO;
import cn.iocoder.yudao.module.forum.controller.app.activity.vo.AppActivitySignUpRespVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.activity.ForumActivityDO;
import cn.iocoder.yudao.module.forum.dal.mysql.activity.ForumActivityMapper;
import cn.iocoder.yudao.module.forum.service.activity.ForumActivityService;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 论坛活动 Controller
 *
 * 后台管理活动的创建、删除，以及报名审核和报名列表查询
 *
 * @author forum
 */
@Tag(name = "管理后台 - 论坛活动")
@RestController
@RequestMapping("/forum/activity")
@Validated
public class AdminActivityController {

    @Resource
    private ForumActivityService activityService;
    @Resource
    private ForumActivityMapper activityMapper;

    @PostMapping("/create")
    @Operation(summary = "创建活动")
    @PreAuthorize("@ss.hasPermission('forum:activity:create')")
    public CommonResult<Long> createActivity(@Valid @RequestBody AppActivityCreateReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(activityService.createActivity(userId, reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "编辑活动")
    @PreAuthorize("@ss.hasPermission('forum:activity:update')")
    public CommonResult<Boolean> updateActivity(@Valid @RequestBody AdminActivityUpdateReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        activityService.updateActivity(userId, reqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取活动详情")
    @Parameter(name = "id", description = "活动ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('forum:activity:query')")
    public CommonResult<AppActivityRespVO> getActivity(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(activityService.getActivity(id, userId));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询活动")
    @PreAuthorize("@ss.hasPermission('forum:activity:query')")
    public CommonResult<PageResult<AppActivityRespVO>> getActivityPage(@Valid AppActivityPageReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(activityService.getActivityPage(reqVO, userId));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除活动")
    @Parameter(name = "id", description = "活动ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('forum:activity:delete')")
    public CommonResult<Boolean> deleteActivity(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        activityService.deleteActivity(id, userId);
        return success(true);
    }

    @GetMapping("/sign-up/page")
    @Operation(summary = "获取活动报名列表")
    @PreAuthorize("@ss.hasPermission('forum:activity-sign-up:query')")
    public CommonResult<PageResult<AppActivitySignUpRespVO>> getActivitySignUpPage(@Valid AppActivitySignUpPageReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(activityService.getActivitySignUpPage(reqVO, userId));
    }

    @PostMapping("/approve-sign-up")
    @Operation(summary = "审核报名申请")
    @PreAuthorize("@ss.hasPermission('forum:activity-sign-up:approve')")
    public CommonResult<Boolean> approveSignUp(@Valid @RequestBody AppActivityApprovalReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        activityService.approveSignUp(userId, reqVO);
        return success(true);
    }

    @PostMapping("/sign-up/feedback")
    @Operation(summary = "点评报名记录")
    @PreAuthorize("@ss.hasPermission('forum:activity-sign-up:feedback')")
    public CommonResult<Boolean> feedbackSignUp(@Valid @RequestBody AdminActivityFeedbackReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        activityService.feedbackSignUp(userId, reqVO);
        return success(true);
    }

    @GetMapping("/sign-up/export")
    @Operation(summary = "导出活动报名列表")
    @PreAuthorize("@ss.hasPermission('forum:activity-sign-up:export')")
    public void exportSignUpExcel(@Valid AppActivitySignUpPageReqVO reqVO, HttpServletResponse response) throws IOException {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        AdminActivitySignUpExportData exportData = activityService.getActivitySignUpExportData(reqVO, userId);
        writeDynamicSignUpExcel(response, exportData);
    }

    private void writeDynamicSignUpExcel(HttpServletResponse response,
                                         AdminActivitySignUpExportData exportData) throws IOException {
        FastExcelFactory.write(response.getOutputStream())
                .head(exportData.getHead())
                .autoCloseStream(false)
                .registerWriteHandler(new ColumnWidthMatchStyleStrategy())
                .registerConverter(new LongStringConverter())
                .sheet("报名列表")
                .doWrite(exportData.getRows());
        response.addHeader("Content-Disposition", "attachment;filename=" + HttpUtils.encodeUtf8("活动报名列表.xlsx"));
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
    }

    @PostMapping("/hide")
    @Operation(summary = "隐藏活动")
    @Parameter(name = "id", description = "活动ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('forum:activity:update')")
    public CommonResult<Boolean> hideActivity(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        activityService.hideActivity(id, userId);
        return success(true);
    }

    @PostMapping("/show")
    @Operation(summary = "展示已隐藏的活动")
    @Parameter(name = "id", description = "活动ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('forum:activity:update')")
    public CommonResult<Boolean> showActivity(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        activityService.showActivity(id, userId);
        return success(true);
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获取活动精简列表（用于下拉选择）")
    @PreAuthorize("@ss.hasPermission('forum:activity:query')")
    public CommonResult<List<AdminActivitySimpleRespVO>> getActivitySimpleList(
            @RequestParam(value = "category", required = false) Integer category) {
        LambdaQueryWrapperX<ForumActivityDO> wrapper = new LambdaQueryWrapperX<ForumActivityDO>()
                .eqIfPresent(ForumActivityDO::getCategory, category)
                .orderByDesc(ForumActivityDO::getCreateTime);
        List<ForumActivityDO> activities = activityMapper.selectList(wrapper);
        List<AdminActivitySimpleRespVO> result = activities.stream().map(activity -> {
            AdminActivitySimpleRespVO vo = new AdminActivitySimpleRespVO();
            vo.setId(activity.getId());
            vo.setTitle(activity.getTitle());
            return vo;
        }).collect(Collectors.toList());
        return success(result);
    }

}
