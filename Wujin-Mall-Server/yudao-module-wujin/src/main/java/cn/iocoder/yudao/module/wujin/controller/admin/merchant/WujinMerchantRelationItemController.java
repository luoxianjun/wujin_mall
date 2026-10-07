package cn.iocoder.yudao.module.wujin.controller.admin.merchant;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationItemListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationItemRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationItemSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.merchant.WujinMerchantRelationItemDO;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationItemAdminService;
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

@Tag(name = "管理后台 - 五金商家关系申报项")
@RestController
@RequestMapping("/wujin/merchant-relation-item")
@Validated
public class WujinMerchantRelationItemController {

    @Resource
    private WujinMerchantRelationItemAdminService itemService;

    @PostMapping("/create")
    @Operation(summary = "创建商家关系申报项")
    @PreAuthorize("@ss.hasPermission('wujin:merchant-relation-item:create')")
    public CommonResult<Long> createItem(@Valid @RequestBody WujinMerchantRelationItemSaveReqVO createReqVO) {
        return success(itemService.createItem(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新商家关系申报项")
    @PreAuthorize("@ss.hasPermission('wujin:merchant-relation-item:update')")
    public CommonResult<Boolean> updateItem(@Valid @RequestBody WujinMerchantRelationItemSaveReqVO updateReqVO) {
        itemService.updateItem(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除商家关系申报项")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wujin:merchant-relation-item:delete')")
    public CommonResult<Boolean> deleteItem(@RequestParam("id") Long id) {
        itemService.deleteItem(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得商家关系申报项")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wujin:merchant-relation-item:query')")
    public CommonResult<WujinMerchantRelationItemRespVO> getItem(@RequestParam("id") Long id) {
        WujinMerchantRelationItemDO item = itemService.getItem(id);
        return success(BeanUtils.toBean(item, WujinMerchantRelationItemRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得商家关系申报项列表")
    @PreAuthorize("@ss.hasPermission('wujin:merchant-relation-item:query')")
    public CommonResult<List<WujinMerchantRelationItemRespVO>> getItemList(@Valid WujinMerchantRelationItemListReqVO listReqVO) {
        List<WujinMerchantRelationItemDO> list = itemService.getItemList(listReqVO);
        return success(BeanUtils.toBean(list, WujinMerchantRelationItemRespVO.class));
    }
}
