package cn.iocoder.yudao.module.forum.controller.admin.sign;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.forum.controller.admin.sign.vo.AdminSignRuleCreateReqVO;
import cn.iocoder.yudao.module.forum.controller.admin.sign.vo.AdminSignRuleRespVO;
import cn.iocoder.yudao.module.forum.controller.admin.sign.vo.AdminSignRuleUpdateReqVO;
import cn.iocoder.yudao.module.forum.service.sign.ForumSignRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 签到规则")
@RestController
@RequestMapping("/forum/sign/rule")
@Validated
public class AdminSignRuleController {

    @Resource
    private ForumSignRuleService signRuleService;

    @PostMapping("/create")
    @Operation(summary = "新增签到规则")
    @PreAuthorize("@ss.hasPermission('forum:sign-rule:create')")
    public CommonResult<Long> createRule(@Valid @RequestBody AdminSignRuleCreateReqVO reqVO) {
        return success(signRuleService.createRule(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改签到规则")
    @PreAuthorize("@ss.hasPermission('forum:sign-rule:update')")
    public CommonResult<Boolean> updateRule(@Valid @RequestBody AdminSignRuleUpdateReqVO reqVO) {
        signRuleService.updateRule(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除签到规则")
    @Parameter(name = "id", description = "规则ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('forum:sign-rule:delete')")
    public CommonResult<Boolean> deleteRule(@RequestParam("id") Long id) {
        signRuleService.deleteRule(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取签到规则详情")
    @Parameter(name = "id", description = "规则ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('forum:sign-rule:query')")
    public CommonResult<AdminSignRuleRespVO> getRule(@RequestParam("id") Long id) {
        return success(signRuleService.getRule(id));
    }

    @GetMapping("/list")
    @Operation(summary = "签到规则列表")
    @Parameter(name = "periodType", description = "周期类型：1=周，2=月", required = false, example = "1")
    @PreAuthorize("@ss.hasPermission('forum:sign-rule:query')")
    public CommonResult<List<AdminSignRuleRespVO>> getRuleList(@RequestParam(value = "periodType", required = false) Integer periodType) {
        return success(signRuleService.getRuleList(periodType));
    }
}

