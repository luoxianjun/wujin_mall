package cn.iocoder.yudao.module.wujin.controller.admin.audit;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.wujin.controller.admin.audit.vo.WujinRelationAuditReviewReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.audit.vo.WujinRelationAuditReviewRespVO;
import cn.iocoder.yudao.module.wujin.service.audit.WujinRelationAuditReviewService;
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

@Tag(name = "管理后台 - 五金关系审核操作")
@RestController
@RequestMapping("/wujin/relation-audit-review")
@Validated
public class WujinRelationAuditReviewController {

    @Resource
    private WujinRelationAuditReviewService reviewService;

    @PostMapping("/review")
    @Operation(summary = "审核商家关系申报")
    @PreAuthorize("@ss.hasPermission('wujin:relation-audit-review:update')")
    public CommonResult<WujinRelationAuditReviewRespVO> review(@Valid @RequestBody WujinRelationAuditReviewReqVO reqVO) {
        return success(reviewService.review(reqVO));
    }
}
