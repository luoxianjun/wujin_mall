package cn.iocoder.yudao.module.wujin.controller.admin.category;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategoryMappingListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategoryMappingRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategoryMappingSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.category.WujinCategoryMappingDO;
import cn.iocoder.yudao.module.wujin.service.category.WujinCategoryMappingAdminService;
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

@Tag(name = "管理后台 - 五金分类映射")
@RestController
@RequestMapping("/wujin/category-mapping")
@Validated
public class WujinCategoryMappingController {

    @Resource
    private WujinCategoryMappingAdminService mappingService;

    @PostMapping("/create")
    @Operation(summary = "创建五金分类映射")
    @PreAuthorize("@ss.hasPermission('wujin:category-mapping:create')")
    public CommonResult<Long> createMapping(@Valid @RequestBody WujinCategoryMappingSaveReqVO createReqVO) {
        return success(mappingService.createMapping(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新五金分类映射")
    @PreAuthorize("@ss.hasPermission('wujin:category-mapping:update')")
    public CommonResult<Boolean> updateMapping(@Valid @RequestBody WujinCategoryMappingSaveReqVO updateReqVO) {
        mappingService.updateMapping(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除五金分类映射")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wujin:category-mapping:delete')")
    public CommonResult<Boolean> deleteMapping(@RequestParam("id") Long id) {
        mappingService.deleteMapping(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得五金分类映射")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wujin:category-mapping:query')")
    public CommonResult<WujinCategoryMappingRespVO> getMapping(@RequestParam("id") Long id) {
        WujinCategoryMappingDO mapping = mappingService.getMapping(id);
        return success(BeanUtils.toBean(mapping, WujinCategoryMappingRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得五金分类映射列表")
    @PreAuthorize("@ss.hasPermission('wujin:category-mapping:query')")
    public CommonResult<List<WujinCategoryMappingRespVO>> getMappingList(@Valid WujinCategoryMappingListReqVO listReqVO) {
        List<WujinCategoryMappingDO> list = mappingService.getMappingList(listReqVO);
        return success(BeanUtils.toBean(list, WujinCategoryMappingRespVO.class));
    }
}
