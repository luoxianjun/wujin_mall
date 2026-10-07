package cn.iocoder.yudao.module.wujin.controller.admin.attribute;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo.WujinAttributeDictionaryListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo.WujinAttributeDictionaryRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo.WujinAttributeDictionarySaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.attribute.WujinAttributeDictionaryDO;
import cn.iocoder.yudao.module.wujin.service.attribute.WujinAttributeDictionaryAdminService;
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

@Tag(name = "管理后台 - 五金平台属性字典")
@RestController
@RequestMapping("/wujin/attribute-dictionary")
@Validated
public class WujinAttributeDictionaryController {

    @Resource
    private WujinAttributeDictionaryAdminService attributeDictionaryService;

    @PostMapping("/create")
    @Operation(summary = "创建平台属性")
    @PreAuthorize("@ss.hasPermission('wujin:attribute-dictionary:create')")
    public CommonResult<Long> createAttribute(@Valid @RequestBody WujinAttributeDictionarySaveReqVO createReqVO) {
        return success(attributeDictionaryService.createAttribute(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新平台属性")
    @PreAuthorize("@ss.hasPermission('wujin:attribute-dictionary:update')")
    public CommonResult<Boolean> updateAttribute(@Valid @RequestBody WujinAttributeDictionarySaveReqVO updateReqVO) {
        attributeDictionaryService.updateAttribute(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除平台属性")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wujin:attribute-dictionary:delete')")
    public CommonResult<Boolean> deleteAttribute(@RequestParam("id") Long id) {
        attributeDictionaryService.deleteAttribute(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得平台属性")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wujin:attribute-dictionary:query')")
    public CommonResult<WujinAttributeDictionaryRespVO> getAttribute(@RequestParam("id") Long id) {
        WujinAttributeDictionaryDO attribute = attributeDictionaryService.getAttribute(id);
        return success(BeanUtils.toBean(attribute, WujinAttributeDictionaryRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得平台属性列表")
    @PreAuthorize("@ss.hasPermission('wujin:attribute-dictionary:query')")
    public CommonResult<List<WujinAttributeDictionaryRespVO>> getAttributeList(
            @Valid WujinAttributeDictionaryListReqVO listReqVO) {
        List<WujinAttributeDictionaryDO> list = attributeDictionaryService.getAttributeList(listReqVO);
        return success(BeanUtils.toBean(list, WujinAttributeDictionaryRespVO.class));
    }

    @GetMapping("/enabled-list")
    @Operation(summary = "获得泳道可用的启用属性，供商家发布商品填写标准属性")
    @Parameter(name = "lane", description = "泳道，为空返回全部启用属性")
    @PreAuthorize("@ss.hasAnyPermissions('wujin:attribute-dictionary:query', 'wujin:merchant-product:create')")
    public CommonResult<List<WujinAttributeDictionaryRespVO>> getEnabledAttributeList(
            @RequestParam(value = "lane", required = false) String lane) {
        List<WujinAttributeDictionaryDO> list = attributeDictionaryService.getEnabledAttributeList(lane);
        return success(BeanUtils.toBean(list, WujinAttributeDictionaryRespVO.class));
    }
}
