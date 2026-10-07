package cn.iocoder.yudao.module.wujin.controller.admin.attribute;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo.WujinProductAttributeValueRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo.WujinProductCustomTagListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo.WujinProductCustomTagRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo.WujinProductCustomTagReviewReqVO;
import cn.iocoder.yudao.module.wujin.service.attribute.WujinProductAttributeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@Tag(name = "管理后台 - 五金商品标准属性与自定义标签")
@RestController
@RequestMapping("/wujin/product-attribute")
@Validated
public class WujinProductAttributeController {

    @Resource
    private WujinProductAttributeService productAttributeService;

    @GetMapping("/value-list")
    @Operation(summary = "获得商品标准属性值")
    @Parameter(name = "submissionId", description = "申报单编号")
    @Parameter(name = "productId", description = "商品编号")
    @PreAuthorize("@ss.hasAnyPermissions('wujin:product-custom-tag:query', 'wujin:merchant-relation-submission:query')")
    public CommonResult<List<WujinProductAttributeValueRespVO>> getAttributeValueList(
            @RequestParam(value = "submissionId", required = false) Long submissionId,
            @RequestParam(value = "productId", required = false) Long productId) {
        return success(BeanUtils.toBean(productAttributeService.getAttributeValueList(submissionId, productId),
                WujinProductAttributeValueRespVO.class));
    }

    @GetMapping("/custom-tag/list")
    @Operation(summary = "获得商品自定义标签列表")
    @PreAuthorize("@ss.hasAnyPermissions('wujin:product-custom-tag:query', 'wujin:merchant-relation-submission:query')")
    public CommonResult<List<WujinProductCustomTagRespVO>> getCustomTagList(
            @Valid WujinProductCustomTagListReqVO reqVO) {
        return success(BeanUtils.toBean(productAttributeService.getCustomTagList(reqVO),
                WujinProductCustomTagRespVO.class));
    }

    @PostMapping("/custom-tag/review")
    @Operation(summary = "审核商品自定义标签")
    @PreAuthorize("@ss.hasPermission('wujin:product-custom-tag:review')")
    public CommonResult<Boolean> reviewCustomTag(@Valid @RequestBody WujinProductCustomTagReviewReqVO reqVO) {
        productAttributeService.reviewCustomTag(reqVO, SecurityFrameworkUtils.getLoginUserId());
        return success(true);
    }
}
