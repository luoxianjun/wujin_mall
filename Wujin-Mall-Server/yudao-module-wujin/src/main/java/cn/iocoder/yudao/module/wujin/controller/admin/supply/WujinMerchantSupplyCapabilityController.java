package cn.iocoder.yudao.module.wujin.controller.admin.supply;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.supply.vo.WujinMerchantSupplyCapabilityListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.supply.vo.WujinMerchantSupplyCapabilitySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.merchant.supply.vo.WujinMerchantSupplyCapabilityRespVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.supply.WujinMerchantSupplyCapabilityDO;
import cn.iocoder.yudao.module.wujin.service.supply.WujinMerchantSupplyCapabilityAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "商家后台 - 五金供应能力")
@RestController
@RequestMapping("/wujin/merchant-supply-capability")
@Validated
public class WujinMerchantSupplyCapabilityController {

    @Resource
    private WujinMerchantSupplyCapabilityAdminService capabilityService;

    @PostMapping("/create")
    @Operation(summary = "创建商家供应能力")
    @PreAuthorize("@ss.hasPermission('wujin:merchant-supply-capability:create')")
    public CommonResult<Long> createCapability(@Valid @RequestBody WujinMerchantSupplyCapabilitySaveReqVO createReqVO) {
        return success(capabilityService.createCapability(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新商家供应能力")
    @PreAuthorize("@ss.hasPermission('wujin:merchant-supply-capability:update')")
    public CommonResult<Boolean> updateCapability(@Valid @RequestBody WujinMerchantSupplyCapabilitySaveReqVO updateReqVO) {
        capabilityService.updateCapability(updateReqVO);
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获得商家供应能力列表")
    @PreAuthorize("@ss.hasPermission('wujin:merchant-supply-capability:query')")
    public CommonResult<List<WujinMerchantSupplyCapabilityRespVO>> getCapabilityList(
            @Valid WujinMerchantSupplyCapabilityListReqVO listReqVO) {
        List<WujinMerchantSupplyCapabilityDO> list = capabilityService.getCapabilityList(listReqVO);
        return success(BeanUtils.toBean(list, WujinMerchantSupplyCapabilityRespVO.class));
    }
}
