package cn.iocoder.yudao.module.wujin.controller.app.search;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.wujin.controller.app.search.vo.WujinAppSearchReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.search.vo.WujinLaneProductPageRespVO;
import cn.iocoder.yudao.module.wujin.controller.app.search.vo.WujinLaneProductReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.search.vo.WujinAppSearchRespVO;
import cn.iocoder.yudao.module.wujin.service.search.WujinAppSearchService;
import cn.iocoder.yudao.module.wujin.service.search.WujinLaneProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 五金三泳道搜索")
@RestController
@RequestMapping("/wujin/search")
@Validated
public class WujinAppSearchController {

    @Resource
    private WujinAppSearchService appSearchService;
    @Resource
    private WujinLaneProductService laneProductService;

    @GetMapping("/result")
    @Operation(summary = "获得三泳道搜索结果摘要")
    public CommonResult<WujinAppSearchRespVO> search(@Valid WujinAppSearchReqVO reqVO) {
        return success(appSearchService.search(reqVO));
    }

    @GetMapping("/products")
    @Operation(summary = "获得泳道商品分页：同一商品只出现在一个泳道，分类、品牌筛选不改变搜索词")
    public CommonResult<WujinLaneProductPageRespVO> getLaneProducts(@Valid WujinLaneProductReqVO reqVO) {
        return success(laneProductService.getLaneProductPage(reqVO));
    }
}
