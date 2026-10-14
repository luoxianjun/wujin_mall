package cn.iocoder.yudao.module.wujin.controller.admin.category;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategoryBatchMigrateReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategoryListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategoryRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategorySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategorySortReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.category.WujinCategoryDO;
import cn.iocoder.yudao.module.wujin.service.category.WujinCategoryAdminService;
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

@Tag(name = "管理后台 - 五金三泳道分类")
@RestController
@RequestMapping("/wujin/category")
@Validated
public class WujinCategoryController {

    @Resource
    private WujinCategoryAdminService categoryService;

    @PostMapping("/create")
    @Operation(summary = "创建五金三泳道分类")
    @PreAuthorize("@ss.hasPermission('wujin:category:create')")
    public CommonResult<Long> createCategory(@Valid @RequestBody WujinCategorySaveReqVO createReqVO) {
        return success(categoryService.createCategory(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新五金三泳道分类")
    @PreAuthorize("@ss.hasPermission('wujin:category:update')")
    public CommonResult<Boolean> updateCategory(@Valid @RequestBody WujinCategorySaveReqVO updateReqVO) {
        categoryService.updateCategory(updateReqVO);
        return success(true);
    }

    @PutMapping("/batch-migrate")
    @Operation(summary = "批量迁移五金三泳道分类")
    @PreAuthorize("@ss.hasPermission('wujin:category:update')")
    public CommonResult<Integer> batchMigrateCategory(@Valid @RequestBody WujinCategoryBatchMigrateReqVO migrateReqVO) {
        return success(categoryService.batchMigrateCategory(migrateReqVO));
    }

    @PutMapping("/update-sort")
    @Operation(summary = "更新同级五金分类排序")
    @PreAuthorize("@ss.hasPermission('wujin:category:update')")
    public CommonResult<Boolean> updateCategorySort(@Valid @RequestBody WujinCategorySortReqVO sortReqVO) {
        categoryService.updateCategorySort(sortReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除五金三泳道分类；cascade=true 时同时删除全部子级")
    @Parameter(name = "id", description = "编号", required = true)
    @Parameter(name = "cascade", description = "是否同时删除子级")
    @PreAuthorize("@ss.hasPermission('wujin:category:delete')")
    public CommonResult<Integer> deleteCategory(@RequestParam("id") Long id,
                                                @RequestParam(value = "cascade", required = false,
                                                        defaultValue = "false") Boolean cascade) {
        return success(categoryService.deleteCategory(id, Boolean.TRUE.equals(cascade)));
    }

    @GetMapping("/get")
    @Operation(summary = "获得五金三泳道分类")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wujin:category:query')")
    public CommonResult<WujinCategoryRespVO> getCategory(@RequestParam("id") Long id) {
        WujinCategoryDO category = categoryService.getCategory(id);
        return success(BeanUtils.toBean(category, WujinCategoryRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得五金三泳道分类列表")
    @PreAuthorize("@ss.hasPermission('wujin:category:query')")
    public CommonResult<List<WujinCategoryRespVO>> getCategoryList(@Valid WujinCategoryListReqVO listReqVO) {
        List<WujinCategoryDO> list = categoryService.getCategoryList(listReqVO);
        return success(BeanUtils.toBean(list, WujinCategoryRespVO.class));
    }
}
