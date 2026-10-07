package cn.iocoder.yudao.module.wujin.controller.app.search;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.wujin.controller.app.search.vo.WujinAppSearchReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.search.vo.WujinAppSearchRespVO;
import cn.iocoder.yudao.module.wujin.service.search.WujinAppSearchService;
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

    @GetMapping("/result")
    @Operation(summary = "获得三泳道搜索结果摘要")
    public CommonResult<WujinAppSearchRespVO> search(@Valid WujinAppSearchReqVO reqVO) {
        return success(appSearchService.search(reqVO));
    }
}
