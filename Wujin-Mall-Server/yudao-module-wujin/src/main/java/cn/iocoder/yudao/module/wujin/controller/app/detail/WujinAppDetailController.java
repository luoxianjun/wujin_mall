package cn.iocoder.yudao.module.wujin.controller.app.detail;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.wujin.controller.app.detail.vo.WujinEntityDetailReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.detail.vo.WujinEntityDetailRespVO;
import cn.iocoder.yudao.module.wujin.service.detail.WujinAppDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping("/entity")
    @Operation(summary = "获得五金实体详情")
    public CommonResult<WujinEntityDetailRespVO> getEntityDetail(@Valid WujinEntityDetailReqVO reqVO) {
        return success(detailService.getEntityDetail(reqVO));
    }
}
