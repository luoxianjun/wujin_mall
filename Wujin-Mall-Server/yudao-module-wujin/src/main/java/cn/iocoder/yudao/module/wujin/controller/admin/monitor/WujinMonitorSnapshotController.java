package cn.iocoder.yudao.module.wujin.controller.admin.monitor;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinMonitorSnapshotListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinMonitorSnapshotRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinMonitorSnapshotSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.monitor.WujinMonitorSnapshotDO;
import cn.iocoder.yudao.module.wujin.service.monitor.WujinMonitorSnapshotAdminService;
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

@Tag(name = "管理后台 - 五金监控指标快照")
@RestController
@RequestMapping("/wujin/monitor-snapshot")
@Validated
public class WujinMonitorSnapshotController {

    @Resource
    private WujinMonitorSnapshotAdminService monitorSnapshotService;

    @PostMapping("/create")
    @Operation(summary = "创建监控指标快照")
    @PreAuthorize("@ss.hasPermission('wujin:monitor-snapshot:create')")
    public CommonResult<Long> createSnapshot(@Valid @RequestBody WujinMonitorSnapshotSaveReqVO createReqVO) {
        return success(monitorSnapshotService.createSnapshot(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新监控指标快照")
    @PreAuthorize("@ss.hasPermission('wujin:monitor-snapshot:update')")
    public CommonResult<Boolean> updateSnapshot(@Valid @RequestBody WujinMonitorSnapshotSaveReqVO updateReqVO) {
        monitorSnapshotService.updateSnapshot(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除监控指标快照")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wujin:monitor-snapshot:delete')")
    public CommonResult<Boolean> deleteSnapshot(@RequestParam("id") Long id) {
        monitorSnapshotService.deleteSnapshot(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得监控指标快照")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wujin:monitor-snapshot:query')")
    public CommonResult<WujinMonitorSnapshotRespVO> getSnapshot(@RequestParam("id") Long id) {
        WujinMonitorSnapshotDO snapshot = monitorSnapshotService.getSnapshot(id);
        return success(BeanUtils.toBean(snapshot, WujinMonitorSnapshotRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得监控指标快照列表")
    @PreAuthorize("@ss.hasPermission('wujin:monitor-snapshot:query')")
    public CommonResult<List<WujinMonitorSnapshotRespVO>> getSnapshotList(@Valid WujinMonitorSnapshotListReqVO listReqVO) {
        List<WujinMonitorSnapshotDO> list = monitorSnapshotService.getSnapshotList(listReqVO);
        return success(BeanUtils.toBean(list, WujinMonitorSnapshotRespVO.class));
    }
}
