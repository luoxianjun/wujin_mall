package cn.iocoder.yudao.module.wujin.controller.admin.template;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.template.WujinIndustryTemplateDO;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateAdminService;
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

@Tag(name = "管理后台 - 五金行业模板")
@RestController
@RequestMapping("/wujin/industry-template")
@Validated
public class WujinIndustryTemplateController {

    @Resource
    private WujinIndustryTemplateAdminService templateService;

    @PostMapping("/create")
    @Operation(summary = "创建行业模板")
    @PreAuthorize("@ss.hasPermission('wujin:industry-template:create')")
    public CommonResult<Long> createTemplate(@Valid @RequestBody WujinIndustryTemplateSaveReqVO createReqVO) {
        return success(templateService.createTemplate(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新行业模板")
    @PreAuthorize("@ss.hasPermission('wujin:industry-template:update')")
    public CommonResult<Boolean> updateTemplate(@Valid @RequestBody WujinIndustryTemplateSaveReqVO updateReqVO) {
        templateService.updateTemplate(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除行业模板")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wujin:industry-template:delete')")
    public CommonResult<Boolean> deleteTemplate(@RequestParam("id") Long id) {
        templateService.deleteTemplate(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得行业模板")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wujin:industry-template:query')")
    public CommonResult<WujinIndustryTemplateRespVO> getTemplate(@RequestParam("id") Long id) {
        WujinIndustryTemplateDO template = templateService.getTemplate(id);
        return success(BeanUtils.toBean(template, WujinIndustryTemplateRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得行业模板列表")
    @PreAuthorize("@ss.hasPermission('wujin:industry-template:query')")
    public CommonResult<List<WujinIndustryTemplateRespVO>> getTemplateList(@Valid WujinIndustryTemplateListReqVO listReqVO) {
        List<WujinIndustryTemplateDO> list = templateService.getTemplateList(listReqVO);
        return success(BeanUtils.toBean(list, WujinIndustryTemplateRespVO.class));
    }
}
