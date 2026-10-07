package cn.iocoder.yudao.module.forum.controller.app.banner;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.forum.controller.app.banner.vo.AppBannerRespVO;
import cn.iocoder.yudao.module.forum.convert.banner.ForumBannerConvert;
import cn.iocoder.yudao.module.forum.service.banner.ForumBannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 用户 APP - Banner Controller
 *
 * @author forum
 */
@Tag(name = "用户 APP - Banner")
@RestController
@RequestMapping("/forum/app/banner")
@Validated
public class AppBannerController {

    @Resource
    private ForumBannerService bannerService;

    @GetMapping("/list")
    @Operation(summary = "获取有效的 Banner 列表（包含活动和帖子的详细信息）")
    public CommonResult<List<AppBannerRespVO>> getActiveBannerList() {
        return success(bannerService.getActiveBannerListWithDetails());
    }

}
