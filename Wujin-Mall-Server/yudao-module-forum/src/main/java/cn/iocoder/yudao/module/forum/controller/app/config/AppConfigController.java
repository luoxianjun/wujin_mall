package cn.iocoder.yudao.module.forum.controller.app.config;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.forum.service.config.ForumConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 用户 APP - 论坛配置 Controller
 *
 * @author forum
 */
@Tag(name = "用户 APP - 论坛配置")
@RestController
@RequestMapping("/forum/config")
@Validated
public class AppConfigController {

    @Resource
    private ForumConfigService configService;

    @GetMapping("/get-value-by-key")
    @Operation(summary = "根据配置键获取配置值")
    @Parameter(name = "key", description = "配置键", required = true, example = "forum.welfare.content")
    public CommonResult<String> getValueByKey(@RequestParam("key") String key) {
        return success(configService.getValueByKey(key));
    }

}
