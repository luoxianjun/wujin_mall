package cn.iocoder.yudao.module.forum.controller.admin.config;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.forum.controller.admin.config.vo.AdminConfigRespVO;
import cn.iocoder.yudao.module.forum.controller.admin.config.vo.AdminConfigSaveReqVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.config.ForumConfigDO;
import cn.iocoder.yudao.module.forum.service.config.ForumConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 论坛配置 Controller
 *
 * @author forum
 */
@Tag(name = "管理后台 - 论坛配置")
@RestController
@RequestMapping("/forum/config")
@Validated
@Slf4j
public class AdminConfigController {

    @Resource
    private ForumConfigService configService;

    @GetMapping("/list")
    @Operation(summary = "获取所有配置列表")
    @PreAuthorize("@ss.hasPermission('forum:config:query')")
    public CommonResult<List<AdminConfigRespVO>> getConfigList() {
        List<ForumConfigDO> list = configService.getAll();
        log.info("[getConfigList][查询到配置数量={}]", list.size());
        List<AdminConfigRespVO> result = list.stream().map(this::convertToRespVO).collect(Collectors.toList());
        log.info("[getConfigList][转换后结果数量={}, 第一条数据={}]", result.size(), result.isEmpty() ? "无" : result.get(0));
        return success(result);
    }

    @GetMapping("/get")
    @Operation(summary = "根据配置键获取配置")
    @Parameter(name = "key", description = "配置键", required = true, example = "forum.welfare.content")
    @PreAuthorize("@ss.hasPermission('forum:config:query')")
    public CommonResult<AdminConfigRespVO> getConfig(@RequestParam("key") String key) {
        ForumConfigDO config = configService.getByKey(key);
        return success(config != null ? convertToRespVO(config) : null);
    }

    @PostMapping("/save")
    @Operation(summary = "保存配置（新增或更新）")
    @PreAuthorize("@ss.hasPermission('forum:config:update')")
    public CommonResult<Long> saveConfig(@Valid @RequestBody AdminConfigSaveReqVO reqVO) {
        Long id = configService.saveConfig(
                reqVO.getKey(),
                reqVO.getValue(),
                reqVO.getName(),
                reqVO.getType(),
                reqVO.getRemark()
        );
        return success(id);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除配置")
    @Parameter(name = "key", description = "配置键", required = true, example = "forum.welfare.content")
    @PreAuthorize("@ss.hasPermission('forum:config:delete')")
    public CommonResult<Boolean> deleteConfig(@RequestParam("key") String key) {
        configService.deleteByKey(key);
        return success(true);
    }

    private AdminConfigRespVO convertToRespVO(ForumConfigDO config) {
        AdminConfigRespVO respVO = new AdminConfigRespVO();
        respVO.setId(config.getId());
        respVO.setConfigKey(config.getConfigKey());
        respVO.setConfigValue(config.getConfigValue());
        respVO.setName(config.getName());
        respVO.setType(config.getType());
        respVO.setRemark(config.getRemark());
        respVO.setCreateTime(config.getCreateTime());
        respVO.setUpdateTime(config.getUpdateTime());
        return respVO;
    }

}
