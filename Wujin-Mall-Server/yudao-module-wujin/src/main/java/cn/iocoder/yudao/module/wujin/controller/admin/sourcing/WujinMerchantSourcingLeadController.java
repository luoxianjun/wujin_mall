package cn.iocoder.yudao.module.wujin.controller.admin.sourcing;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.sourcing.vo.WujinSourcingLeadConversionReportReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.sourcing.vo.WujinSourcingLeadConversionReportRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.sourcing.vo.WujinSourcingLeadListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.sourcing.vo.WujinSourcingLeadRespVO;
import cn.iocoder.yudao.module.wujin.controller.merchant.sourcing.vo.WujinMerchantSourcingLeadHandleReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.sourcing.WujinSourcingLeadDO;
import cn.iocoder.yudao.module.wujin.service.sourcing.WujinMerchantSourcingLeadService;
import cn.iocoder.yudao.module.wujin.service.sourcing.WujinSourcingLeadAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "商家后台 - 五金寻源线索")
@RestController
@RequestMapping("/wujin/merchant-sourcing-lead")
@Validated
public class WujinMerchantSourcingLeadController {

    @Resource
    private WujinMerchantSourcingLeadService merchantSourcingLeadService;
    @Resource
    private WujinSourcingLeadAdminService sourcingLeadAdminService;

    @GetMapping("/list")
    @Operation(summary = "获得商家寻源线索列表")
    @PreAuthorize("@ss.hasPermission('wujin:merchant-sourcing-lead:query')")
    public CommonResult<List<WujinSourcingLeadRespVO>> getLeadList(@Valid WujinSourcingLeadListReqVO reqVO) {
        List<WujinSourcingLeadDO> list = merchantSourcingLeadService.getLeadList(reqVO);
        return success(BeanUtils.toBean(list, WujinSourcingLeadRespVO.class));
    }

    @PostMapping("/handle")
    @Operation(summary = "处理商家寻源线索")
    @PreAuthorize("@ss.hasPermission('wujin:merchant-sourcing-lead:update')")
    public CommonResult<Boolean> handleLead(@Valid @RequestBody WujinMerchantSourcingLeadHandleReqVO reqVO) {
        if (reqVO.getMerchantId() == null) {
            reqVO.setMerchantId(SecurityFrameworkUtils.getLoginUserId());
        }
        merchantSourcingLeadService.handleLead(reqVO);
        return success(true);
    }

    @GetMapping("/conversion-report")
    @Operation(summary = "获得商家寻源线索转化报表")
    @PreAuthorize("@ss.hasPermission('wujin:merchant-sourcing-lead:query')")
    public CommonResult<WujinSourcingLeadConversionReportRespVO> getConversionReport(
            @Valid WujinSourcingLeadConversionReportReqVO reqVO) {
        if (reqVO.getMerchantId() == null) {
            reqVO.setMerchantId(SecurityFrameworkUtils.getLoginUserId());
        }
        return success(sourcingLeadAdminService.getConversionReport(reqVO));
    }
}
