package cn.iocoder.yudao.module.forum.controller.admin.point;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.forum.controller.admin.point.vo.AdminPointChangeReqVO;
import cn.iocoder.yudao.module.forum.controller.admin.point.vo.AdminPointRecordExcelVO;
import cn.iocoder.yudao.module.forum.controller.admin.point.vo.AdminPointRecordPageReqVO;
import cn.iocoder.yudao.module.forum.controller.admin.point.vo.AdminPointRecordRespVO;
import cn.iocoder.yudao.module.forum.convert.point.ForumPointConvert;
import cn.iocoder.yudao.module.forum.dal.dataobject.user.ForumUserProfileDO;
import cn.iocoder.yudao.module.forum.service.point.ForumPointService;
import cn.iocoder.yudao.module.forum.service.user.ForumUserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 积分记录")
@RestController
@RequestMapping("/forum/point/record")
@Validated
public class AdminPointRecordController {

    @Resource
    private ForumPointService pointService;

    @Resource
    private ForumUserProfileService userProfileService;

    @GetMapping("/page")
    @Operation(summary = "分页查询积分记录")
    @PreAuthorize("@ss.hasPermission('forum:point-record:query')")
    public CommonResult<PageResult<AdminPointRecordRespVO>> getPointRecordPage(@Valid AdminPointRecordPageReqVO reqVO) {
        // 处理 UID 搜索：根据 UID 模糊匹配获取用户 ID 列表
        if (StrUtil.isNotBlank(reqVO.getUid())) {
            List<Long> userIds = userProfileService.getUserIdsByUidLike(reqVO.getUid());
            if (CollectionUtils.isEmpty(userIds)) {
                return success(PageResult.empty());
            }
            // 如果同时指定了 userId，取交集
            if (reqVO.getUserId() != null) {
                if (!userIds.contains(reqVO.getUserId())) {
                    return success(PageResult.empty());
                }
            } else if (userIds.size() == 1) {
                reqVO.setUserId(userIds.get(0));
            }
        }

        PageResult<AdminPointRecordRespVO> pageResult = ForumPointConvert.INSTANCE
                .convertAdminPage(pointService.getPointRecordPage(reqVO));
        if (CollectionUtils.isEmpty(pageResult.getList())) {
            return success(pageResult);
        }

        // 使用 forum_user_profile 填充用户信息
        List<ForumUserProfileDO> profiles = userProfileService.getUserProfileListByUserIds(
                convertSet(pageResult.getList(), AdminPointRecordRespVO::getUserId));
        Map<Long, ForumUserProfileDO> profileMap = convertMap(profiles, ForumUserProfileDO::getUserId);
        pageResult.getList().forEach(item -> {
            ForumUserProfileDO profile = profileMap.get(item.getUserId());
            if (profile != null) {
                item.setNickname(profile.getNickname());
                item.setUid(profile.getUid());
            }
        });
        return success(pageResult);
    }

    @PostMapping("/change")
    @Operation(summary = "调整用户积分")
    @PreAuthorize("@ss.hasPermission('forum:point-record:change')")
    public CommonResult<Boolean> changePoint(@Valid @RequestBody AdminPointChangeReqVO reqVO) {
        pointService.adminChangePoint(reqVO.getUserId(), reqVO.getPoint(), reqVO.getReason());
        return success(true);
    }

    @GetMapping("/export")
    @Operation(summary = "导出积分明细列表")
    @PreAuthorize("@ss.hasPermission('forum:point-record:export')")
    public void exportExcel(@Valid AdminPointRecordPageReqVO reqVO, HttpServletResponse response) throws IOException {
        List<AdminPointRecordExcelVO> list = pointService.getPointRecordExcelList(reqVO);
        ExcelUtils.write(response, "积分明细列表.xlsx", "积分明细", AdminPointRecordExcelVO.class, list);
    }

}
