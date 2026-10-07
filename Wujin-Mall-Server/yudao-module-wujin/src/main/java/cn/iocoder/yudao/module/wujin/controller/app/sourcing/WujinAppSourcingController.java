package cn.iocoder.yudao.module.wujin.controller.app.sourcing;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSourcingLeadSubmitReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSourcingLeadSubmitRespVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSupplierCandidateReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSupplierCandidateRespVO;
import cn.iocoder.yudao.module.wujin.service.sourcing.WujinAppSourcingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "用户 App - 五金一键寻源")
@RestController
@RequestMapping("/wujin/sourcing")
@Validated
public class WujinAppSourcingController {

    @Resource
    private WujinAppSourcingService sourcingService;

    @GetMapping("/supplier-candidates")
    @Operation(summary = "获得一键寻源供应商候选")
    public CommonResult<List<WujinSupplierCandidateRespVO>> getSupplierCandidates(
            @Valid WujinSupplierCandidateReqVO reqVO) {
        return success(sourcingService.getSupplierCandidates(reqVO));
    }

    @PostMapping("/lead/submit")
    @Operation(summary = "提交一键寻源线索")
    public CommonResult<WujinSourcingLeadSubmitRespVO> submitLead(
            @Valid @RequestBody WujinSourcingLeadSubmitReqVO reqVO) {
        return success(sourcingService.submitLead(reqVO));
    }
}
