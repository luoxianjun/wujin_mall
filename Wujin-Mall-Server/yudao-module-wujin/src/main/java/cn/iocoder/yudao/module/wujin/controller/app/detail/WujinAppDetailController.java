package cn.iocoder.yudao.module.wujin.controller.app.detail;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.wujin.controller.app.detail.vo.WujinEntityDetailReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.detail.vo.WujinEntityDetailRespVO;
import cn.iocoder.yudao.module.wujin.controller.app.detail.vo.WujinProductDetailRespVO;
import cn.iocoder.yudao.module.wujin.service.detail.WujinAppDetailService;
import cn.iocoder.yudao.module.wujin.service.detail.WujinAppProductDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 五金详情")
@RestController
@RequestMapping("/wujin/detail")
@Validated
public class WujinAppDetailController {

    @Resource
    private WujinAppDetailService detailService;
    @Resource
    private WujinAppProductDetailService productDetailService;

    @GetMapping("/entity")
    @Operation(summary = "获得五金实体详情")
    public CommonResult<WujinEntityDetailRespVO> getEntityDetail(@Valid WujinEntityDetailReqVO reqVO) {
        return success(detailService.getEntityDetail(reqVO));
    }

    @GetMapping("/product")
    @Operation(summary = "获得五金商品详情：真实商品、规格参数、认证标签与上游关联")
    @Parameter(name = "id", description = "商品 SPU 编号", required = true)
    public CommonResult<WujinProductDetailRespVO> getProductDetail(@RequestParam("id") Long id) {
        return success(productDetailService.getProductDetail(id));
    }
}
