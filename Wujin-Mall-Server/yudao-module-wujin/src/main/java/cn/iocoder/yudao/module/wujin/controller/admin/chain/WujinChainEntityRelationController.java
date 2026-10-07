package cn.iocoder.yudao.module.wujin.controller.admin.chain;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityRelationListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityRelationRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityRelationSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.chain.WujinChainEntityRelationDO;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityRelationAdminService;
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

@Tag(name = "管理后台 - 五金产业链实体关系")
@RestController
@RequestMapping("/wujin/chain-relation")
@Validated
public class WujinChainEntityRelationController {

    @Resource
    private WujinChainEntityRelationAdminService relationService;

    @PostMapping("/create")
    @Operation(summary = "创建产业链实体关系")
    @PreAuthorize("@ss.hasPermission('wujin:chain-relation:create')")
    public CommonResult<Long> createRelation(@Valid @RequestBody WujinChainEntityRelationSaveReqVO createReqVO) {
        return success(relationService.createRelation(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新产业链实体关系")
    @PreAuthorize("@ss.hasPermission('wujin:chain-relation:update')")
    public CommonResult<Boolean> updateRelation(@Valid @RequestBody WujinChainEntityRelationSaveReqVO updateReqVO) {
        relationService.updateRelation(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除产业链实体关系")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wujin:chain-relation:delete')")
    public CommonResult<Boolean> deleteRelation(@RequestParam("id") Long id) {
        relationService.deleteRelation(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得产业链实体关系")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wujin:chain-relation:query')")
    public CommonResult<WujinChainEntityRelationRespVO> getRelation(@RequestParam("id") Long id) {
        WujinChainEntityRelationDO relation = relationService.getRelation(id);
        return success(BeanUtils.toBean(relation, WujinChainEntityRelationRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得产业链实体关系列表")
    @PreAuthorize("@ss.hasPermission('wujin:chain-relation:query')")
    public CommonResult<List<WujinChainEntityRelationRespVO>> getRelationList(@Valid WujinChainEntityRelationListReqVO listReqVO) {
        List<WujinChainEntityRelationDO> list = relationService.getRelationList(listReqVO);
        return success(BeanUtils.toBean(list, WujinChainEntityRelationRespVO.class));
    }
}
