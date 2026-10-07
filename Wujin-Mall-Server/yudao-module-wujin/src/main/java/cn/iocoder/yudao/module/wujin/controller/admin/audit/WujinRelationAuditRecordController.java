package cn.iocoder.yudao.module.wujin.controller.admin.audit;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.audit.vo.WujinRelationAuditRecordListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.audit.vo.WujinRelationAuditRecordRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.audit.vo.WujinRelationAuditRecordSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.audit.WujinRelationAuditRecordDO;
import cn.iocoder.yudao.module.wujin.service.audit.WujinRelationAuditRecordAdminService;
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

@Tag(name = "管理后台 - 五金关系审核记录")
@RestController
@RequestMapping("/wujin/relation-audit-record")
@Validated
public class WujinRelationAuditRecordController {

    @Resource
    private WujinRelationAuditRecordAdminService auditRecordService;

    @PostMapping("/create")
    @Operation(summary = "创建关系审核记录")
    @PreAuthorize("@ss.hasPermission('wujin:relation-audit-record:create')")
    public CommonResult<Long> createAuditRecord(@Valid @RequestBody WujinRelationAuditRecordSaveReqVO createReqVO) {
        return success(auditRecordService.createAuditRecord(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新关系审核记录")
    @PreAuthorize("@ss.hasPermission('wujin:relation-audit-record:update')")
    public CommonResult<Boolean> updateAuditRecord(@Valid @RequestBody WujinRelationAuditRecordSaveReqVO updateReqVO) {
        auditRecordService.updateAuditRecord(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除关系审核记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wujin:relation-audit-record:delete')")
    public CommonResult<Boolean> deleteAuditRecord(@RequestParam("id") Long id) {
        auditRecordService.deleteAuditRecord(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得关系审核记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wujin:relation-audit-record:query')")
    public CommonResult<WujinRelationAuditRecordRespVO> getAuditRecord(@RequestParam("id") Long id) {
        WujinRelationAuditRecordDO record = auditRecordService.getAuditRecord(id);
        return success(BeanUtils.toBean(record, WujinRelationAuditRecordRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得关系审核记录列表")
    @PreAuthorize("@ss.hasPermission('wujin:relation-audit-record:query')")
    public CommonResult<List<WujinRelationAuditRecordRespVO>> getAuditRecordList(@Valid WujinRelationAuditRecordListReqVO listReqVO) {
        List<WujinRelationAuditRecordDO> list = auditRecordService.getAuditRecordList(listReqVO);
        return success(BeanUtils.toBean(list, WujinRelationAuditRecordRespVO.class));
    }
}
