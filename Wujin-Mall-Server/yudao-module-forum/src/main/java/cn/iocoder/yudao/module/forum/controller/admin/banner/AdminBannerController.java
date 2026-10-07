package cn.iocoder.yudao.module.forum.controller.admin.banner;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.forum.controller.admin.banner.vo.*;
import cn.iocoder.yudao.module.forum.convert.banner.ForumBannerConvert;
import cn.iocoder.yudao.module.forum.service.banner.ForumBannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - Banner 配置 Controller
 *
 * @author forum
 */
@Tag(name = "管理后台 - Banner 配置")
@RestController
@RequestMapping("/forum/banner")
@Validated
public class AdminBannerController {

    @Resource
    private ForumBannerService bannerService;

    @PostMapping("/create")
    @Operation(summary = "创建 Banner")
    @PreAuthorize("@ss.hasPermission('forum:banner:create')")
    public CommonResult<Long> createBanner(@Valid @RequestBody AdminBannerCreateReqVO createReqVO) {
        return success(bannerService.createBanner(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新 Banner")
    @PreAuthorize("@ss.hasPermission('forum:banner:update')")
    public CommonResult<Boolean> updateBanner(@Valid @RequestBody AdminBannerUpdateReqVO updateReqVO) {
        bannerService.updateBanner(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除 Banner")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('forum:banner:delete')")
    public CommonResult<Boolean> deleteBanner(@RequestParam("id") Long id) {
        bannerService.deleteBanner(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取 Banner")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('forum:banner:query')")
    public CommonResult<AdminBannerRespVO> getBanner(@RequestParam("id") Long id) {
        return success(ForumBannerConvert.INSTANCE.convert(bannerService.getBanner(id)));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询 Banner")
    @PreAuthorize("@ss.hasPermission('forum:banner:query')")
    public CommonResult<PageResult<AdminBannerRespVO>> getBannerPage(@Valid AdminBannerPageReqVO pageReqVO) {
        return success(ForumBannerConvert.INSTANCE.convertPage(bannerService.getBannerPage(pageReqVO)));
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新 Banner 状态")
    @PreAuthorize("@ss.hasPermission('forum:banner:update')")
    public CommonResult<Boolean> updateBannerStatus(@RequestParam("id") Long id,
                                                    @RequestParam("status") Integer status) {
        bannerService.updateBannerStatus(id, status);
        return success(true);
    }

}
