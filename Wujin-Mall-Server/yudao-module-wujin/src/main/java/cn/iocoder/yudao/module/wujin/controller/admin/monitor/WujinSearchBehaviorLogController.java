package cn.iocoder.yudao.module.wujin.controller.admin.monitor;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinSearchBehaviorLogListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinSearchBehaviorLogRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinSearchBehaviorLogSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.monitor.WujinSearchBehaviorLogDO;
import cn.iocoder.yudao.module.wujin.service.monitor.WujinSearchBehaviorLogAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 五金搜索行为日志")
@RestController
@RequestMapping("/wujin/search-behavior-log")
@Validated
public class WujinSearchBehaviorLogController {

    @Resource
    private WujinSearchBehaviorLogAdminService behaviorLogService;

    @PostMapping("/create")
    @Operation(summary = "创建搜索行为日志")
    @PreAuthorize("@ss.hasPermission('wujin:search-behavior-log:create')")
    public CommonResult<Long> createBehaviorLog(@Valid @RequestBody WujinSearchBehaviorLogSaveReqVO createReqVO) {
        return success(behaviorLogService.createBehaviorLog(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新搜索行为日志")
    @PreAuthorize("@ss.hasPermission('wujin:search-behavior-log:update')")
    public CommonResult<Boolean> updateBehaviorLog(@Valid @RequestBody WujinSearchBehaviorLogSaveReqVO updateReqVO) {
        behaviorLogService.updateBehaviorLog(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除搜索行为日志")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wujin:search-behavior-log:delete')")
    public CommonResult<Boolean> deleteBehaviorLog(@RequestParam("id") Long id) {
        behaviorLogService.deleteBehaviorLog(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得搜索行为日志")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wujin:search-behavior-log:query')")
    public CommonResult<WujinSearchBehaviorLogRespVO> getBehaviorLog(@RequestParam("id") Long id) {
        WujinSearchBehaviorLogDO behaviorLog = behaviorLogService.getBehaviorLog(id);
        return success(BeanUtils.toBean(behaviorLog, WujinSearchBehaviorLogRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得搜索行为日志列表")
    @PreAuthorize("@ss.hasPermission('wujin:search-behavior-log:query')")
    public CommonResult<List<WujinSearchBehaviorLogRespVO>> getBehaviorLogList(@Valid WujinSearchBehaviorLogListReqVO listReqVO) {
        List<WujinSearchBehaviorLogDO> list = behaviorLogService.getBehaviorLogList(listReqVO);
        return success(BeanUtils.toBean(list, WujinSearchBehaviorLogRespVO.class));
    }
}
