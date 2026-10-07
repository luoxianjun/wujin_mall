package cn.iocoder.yudao.module.wujin.controller.app.trace;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.wujin.controller.app.trace.vo.WujinTraceGraphReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.trace.vo.WujinTraceGraphRespVO;
import cn.iocoder.yudao.module.wujin.service.trace.WujinAppTraceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 五金溯源图谱")
@RestController
@RequestMapping("/wujin/trace")
@Validated
public class WujinAppTraceController {

    @Resource
    private WujinAppTraceService traceService;

    @GetMapping("/graph")
    @Operation(summary = "获得五金溯源图谱")
    public CommonResult<WujinTraceGraphRespVO> getTraceGraph(@Valid WujinTraceGraphReqVO reqVO) {
        return success(traceService.getTraceGraph(reqVO));
    }
}
