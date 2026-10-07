package cn.iocoder.yudao.module.wujin.controller.admin.monitor;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinSearchRuleConfigListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinSearchRuleConfigRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinSearchRuleConfigSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.monitor.WujinSearchRuleConfigDO;
import cn.iocoder.yudao.module.wujin.service.monitor.WujinSearchRuleConfigAdminService;
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

@Tag(name = "管理后台 - 五金搜索规则配置")
@RestController
@RequestMapping("/wujin/search-rule-config")
@Validated
public class WujinSearchRuleConfigController {

    @Resource
    private WujinSearchRuleConfigAdminService ruleConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建搜索规则配置")
    @PreAuthorize("@ss.hasPermission('wujin:search-rule-config:create')")
    public CommonResult<Long> createRuleConfig(@Valid @RequestBody WujinSearchRuleConfigSaveReqVO createReqVO) {
        return success(ruleConfigService.createRuleConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新搜索规则配置")
    @PreAuthorize("@ss.hasPermission('wujin:search-rule-config:update')")
    public CommonResult<Boolean> updateRuleConfig(@Valid @RequestBody WujinSearchRuleConfigSaveReqVO updateReqVO) {
        ruleConfigService.updateRuleConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除搜索规则配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wujin:search-rule-config:delete')")
    public CommonResult<Boolean> deleteRuleConfig(@RequestParam("id") Long id) {
        ruleConfigService.deleteRuleConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得搜索规则配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wujin:search-rule-config:query')")
    public CommonResult<WujinSearchRuleConfigRespVO> getRuleConfig(@RequestParam("id") Long id) {
        WujinSearchRuleConfigDO ruleConfig = ruleConfigService.getRuleConfig(id);
        return success(BeanUtils.toBean(ruleConfig, WujinSearchRuleConfigRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得搜索规则配置列表")
    @PreAuthorize("@ss.hasPermission('wujin:search-rule-config:query')")
    public CommonResult<List<WujinSearchRuleConfigRespVO>> getRuleConfigList(@Valid WujinSearchRuleConfigListReqVO listReqVO) {
        List<WujinSearchRuleConfigDO> list = ruleConfigService.getRuleConfigList(listReqVO);
        return success(BeanUtils.toBean(list, WujinSearchRuleConfigRespVO.class));
    }
}
