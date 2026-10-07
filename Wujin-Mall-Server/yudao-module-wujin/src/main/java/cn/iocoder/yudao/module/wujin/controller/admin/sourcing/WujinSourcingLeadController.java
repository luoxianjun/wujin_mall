package cn.iocoder.yudao.module.wujin.controller.admin.sourcing;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.sourcing.vo.WujinSourcingLeadDispatchReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.sourcing.vo.WujinSourcingLeadListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.sourcing.vo.WujinSourcingLeadConversionReportReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.sourcing.vo.WujinSourcingLeadConversionReportRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.sourcing.vo.WujinSourcingLeadRespVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.sourcing.WujinSourcingLeadDO;
import cn.iocoder.yudao.module.wujin.service.sourcing.WujinSourcingLeadAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 五金寻源线索")
@RestController
@RequestMapping("/wujin/sourcing-lead")
@Validated
public class WujinSourcingLeadController {

    @Resource
    private WujinSourcingLeadAdminService sourcingLeadAdminService;

    @GetMapping("/list")
    @Operation(summary = "获得寻源线索列表")
    @PreAuthorize("@ss.hasPermission('wujin:sourcing-lead:query')")
    public CommonResult<List<WujinSourcingLeadRespVO>> getLeadList(@Valid WujinSourcingLeadListReqVO reqVO) {
        List<WujinSourcingLeadDO> list = sourcingLeadAdminService.getLeadList(reqVO);
        return success(BeanUtils.toBean(list, WujinSourcingLeadRespVO.class));
    }

    @PostMapping("/dispatch")
    @Operation(summary = "分发寻源线索给商家")
    @PreAuthorize("@ss.hasPermission('wujin:sourcing-lead:dispatch')")
    public CommonResult<Boolean> dispatchLead(@Valid @RequestBody WujinSourcingLeadDispatchReqVO reqVO) {
        sourcingLeadAdminService.dispatchLead(reqVO);
        return success(true);
    }

    @PostMapping("/auto-dispatch")
    @Operation(summary = "自动匹配分发寻源线索")
    @PreAuthorize("@ss.hasPermission('wujin:sourcing-lead:dispatch')")
    public CommonResult<Boolean> autoDispatchLead(@RequestParam("leadId") Long leadId) {
        sourcingLeadAdminService.autoDispatchLead(leadId);
        return success(true);
    }

    @GetMapping("/conversion-report")
    @Operation(summary = "获得寻源线索转化报表")
    @PreAuthorize("@ss.hasPermission('wujin:sourcing-lead:query')")
    public CommonResult<WujinSourcingLeadConversionReportRespVO> getConversionReport(
            @Valid WujinSourcingLeadConversionReportReqVO reqVO) {
        return success(sourcingLeadAdminService.getConversionReport(reqVO));
    }
}
