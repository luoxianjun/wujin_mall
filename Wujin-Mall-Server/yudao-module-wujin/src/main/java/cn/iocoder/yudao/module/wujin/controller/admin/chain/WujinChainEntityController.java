package cn.iocoder.yudao.module.wujin.controller.admin.chain;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntitySaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.chain.WujinChainEntityDO;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminService;
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

@Tag(name = "管理后台 - 五金产业链实体")
@RestController
@RequestMapping("/wujin/chain-entity")
@Validated
public class WujinChainEntityController {

    @Resource
    private WujinChainEntityAdminService entityService;

    @PostMapping("/create")
    @Operation(summary = "创建产业链实体")
    @PreAuthorize("@ss.hasPermission('wujin:chain-entity:create')")
    public CommonResult<Long> createEntity(@Valid @RequestBody WujinChainEntitySaveReqVO createReqVO) {
        return success(entityService.createEntity(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新产业链实体")
    @PreAuthorize("@ss.hasPermission('wujin:chain-entity:update')")
    public CommonResult<Boolean> updateEntity(@Valid @RequestBody WujinChainEntitySaveReqVO updateReqVO) {
        entityService.updateEntity(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除产业链实体")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wujin:chain-entity:delete')")
    public CommonResult<Boolean> deleteEntity(@RequestParam("id") Long id) {
        entityService.deleteEntity(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得产业链实体")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wujin:chain-entity:query')")
    public CommonResult<WujinChainEntityRespVO> getEntity(@RequestParam("id") Long id) {
        WujinChainEntityDO entity = entityService.getEntity(id);
        return success(BeanUtils.toBean(entity, WujinChainEntityRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得产业链实体列表")
    @PreAuthorize("@ss.hasPermission('wujin:chain-entity:query')")
    public CommonResult<List<WujinChainEntityRespVO>> getEntityList(@Valid WujinChainEntityListReqVO listReqVO) {
        List<WujinChainEntityDO> list = entityService.getEntityList(listReqVO);
        return success(BeanUtils.toBean(list, WujinChainEntityRespVO.class));
    }
}
