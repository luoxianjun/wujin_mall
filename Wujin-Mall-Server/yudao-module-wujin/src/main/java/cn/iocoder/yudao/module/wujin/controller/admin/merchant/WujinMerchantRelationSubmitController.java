package cn.iocoder.yudao.module.wujin.controller.admin.merchant;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.wujin.controller.merchant.relation.vo.WujinMerchantRelationSubmitReqVO;
import cn.iocoder.yudao.module.wujin.controller.merchant.relation.vo.WujinMerchantRelationSubmitRespVO;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationSubmitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "商家后台 - 五金关系申报提交")
@RestController
@RequestMapping("/wujin/merchant-relation-submit")
@Validated
public class WujinMerchantRelationSubmitController {

    @Resource
    private WujinMerchantRelationSubmitService submitService;

    @PostMapping("/submit")
    @Operation(summary = "提交商品关系申报")
    @PreAuthorize("@ss.hasPermission('wujin:merchant-relation-submit:create')")
    public CommonResult<WujinMerchantRelationSubmitRespVO> submitRelation(
            @Valid @RequestBody WujinMerchantRelationSubmitReqVO reqVO) {
        return success(submitService.submitRelation(reqVO));
    }
}
