package cn.iocoder.yudao.module.wujin.controller.admin.template;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateItemBatchMigrateReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateItemListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateItemRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateItemSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.template.WujinIndustryTemplateItemDO;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateItemAdminService;
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

@Tag(name = "管理后台 - 五金行业模板项")
@RestController
@RequestMapping("/wujin/industry-template-item")
@Validated
public class WujinIndustryTemplateItemController {

    @Resource
    private WujinIndustryTemplateItemAdminService templateItemService;

    @PostMapping("/create")
    @Operation(summary = "创建行业模板项")
    @PreAuthorize("@ss.hasPermission('wujin:industry-template-item:create')")
    public CommonResult<Long> createTemplateItem(@Valid @RequestBody WujinIndustryTemplateItemSaveReqVO createReqVO) {
        return success(templateItemService.createTemplateItem(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新行业模板项")
    @PreAuthorize("@ss.hasPermission('wujin:industry-template-item:update')")
    public CommonResult<Boolean> updateTemplateItem(@Valid @RequestBody WujinIndustryTemplateItemSaveReqVO updateReqVO) {
        templateItemService.updateTemplateItem(updateReqVO);
        return success(true);
    }

    @PutMapping("/batch-migrate")
    @Operation(summary = "批量迁移行业模板项")
    @PreAuthorize("@ss.hasPermission('wujin:industry-template-item:update')")
    public CommonResult<Integer> batchMigrateTemplateItem(@Valid @RequestBody WujinIndustryTemplateItemBatchMigrateReqVO migrateReqVO) {
        return success(templateItemService.batchMigrateTemplateItem(migrateReqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除行业模板项")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wujin:industry-template-item:delete')")
    public CommonResult<Boolean> deleteTemplateItem(@RequestParam("id") Long id) {
        templateItemService.deleteTemplateItem(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得行业模板项")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wujin:industry-template-item:query')")
    public CommonResult<WujinIndustryTemplateItemRespVO> getTemplateItem(@RequestParam("id") Long id) {
        WujinIndustryTemplateItemDO item = templateItemService.getTemplateItem(id);
        return success(BeanUtils.toBean(item, WujinIndustryTemplateItemRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得行业模板项列表")
    @PreAuthorize("@ss.hasPermission('wujin:industry-template-item:query')")
    public CommonResult<List<WujinIndustryTemplateItemRespVO>> getTemplateItemList(@Valid WujinIndustryTemplateItemListReqVO listReqVO) {
        List<WujinIndustryTemplateItemDO> list = templateItemService.getTemplateItemList(listReqVO);
        return success(BeanUtils.toBean(list, WujinIndustryTemplateItemRespVO.class));
    }
}
