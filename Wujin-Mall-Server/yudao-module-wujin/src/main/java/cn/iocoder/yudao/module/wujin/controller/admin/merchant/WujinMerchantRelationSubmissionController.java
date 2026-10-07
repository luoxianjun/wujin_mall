package cn.iocoder.yudao.module.wujin.controller.admin.merchant;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationSubmissionListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationSubmissionRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationSubmissionSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.merchant.WujinMerchantRelationSubmissionDO;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationSubmissionAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 五金商家关系申报")
@RestController
@RequestMapping("/wujin/merchant-relation-submission")
@Validated
public class WujinMerchantRelationSubmissionController {

    @Resource
    private WujinMerchantRelationSubmissionAdminService submissionService;

    @PostMapping("/create")
    @Operation(summary = "创建商家关系申报")
    @PreAuthorize("@ss.hasPermission('wujin:merchant-relation-submission:create')")
    public CommonResult<Long> createSubmission(@Valid @RequestBody WujinMerchantRelationSubmissionSaveReqVO createReqVO) {
        return success(submissionService.createSubmission(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新商家关系申报")
    @PreAuthorize("@ss.hasPermission('wujin:merchant-relation-submission:update')")
    public CommonResult<Boolean> updateSubmission(@Valid @RequestBody WujinMerchantRelationSubmissionSaveReqVO updateReqVO) {
        submissionService.updateSubmission(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除商家关系申报")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wujin:merchant-relation-submission:delete')")
    public CommonResult<Boolean> deleteSubmission(@RequestParam("id") Long id) {
        submissionService.deleteSubmission(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得商家关系申报")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wujin:merchant-relation-submission:query')")
    public CommonResult<WujinMerchantRelationSubmissionRespVO> getSubmission(@RequestParam("id") Long id) {
        WujinMerchantRelationSubmissionDO submission = submissionService.getSubmission(id);
        return success(BeanUtils.toBean(submission, WujinMerchantRelationSubmissionRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得商家关系申报列表")
    @PreAuthorize("@ss.hasPermission('wujin:merchant-relation-submission:query')")
    public CommonResult<List<WujinMerchantRelationSubmissionRespVO>> getSubmissionList(
            @Valid WujinMerchantRelationSubmissionListReqVO listReqVO) {
        List<WujinMerchantRelationSubmissionDO> list = submissionService.getSubmissionList(listReqVO);
        return success(BeanUtils.toBean(list, WujinMerchantRelationSubmissionRespVO.class));
    }
}
