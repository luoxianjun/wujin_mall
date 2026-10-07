package cn.iocoder.yudao.module.forum.controller.admin.message;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.forum.controller.admin.message.vo.AdminSystemBroadcastReqVO;
import cn.iocoder.yudao.module.forum.controller.admin.message.vo.AdminSystemBroadcastRespVO;
import cn.iocoder.yudao.module.forum.service.message.ForumSystemBroadcastService;
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
 * 管理后台 - 系统广播消息
 *
 * @author forum
 */
@Tag(name = "管理后台 - 系统广播消息")
@RestController
@RequestMapping("/forum/system-broadcast")
@Validated
public class AdminSystemBroadcastController {

    @Resource
    private ForumSystemBroadcastService broadcastService;

    @PostMapping("/send")
    @Operation(summary = "发送系统广播消息")
    @PreAuthorize("@ss.hasPermission('forum:system-broadcast:send')")
    public CommonResult<Long> sendBroadcast(@Valid @RequestBody AdminSystemBroadcastReqVO reqVO) {
        Long senderId = SecurityFrameworkUtils.getLoginUserId();
        Long broadcastId = broadcastService.broadcast(senderId, reqVO);
        return success(broadcastId);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询广播消息历史")
    @PreAuthorize("@ss.hasPermission('forum:system-broadcast:query')")
    public CommonResult<PageResult<AdminSystemBroadcastRespVO>> getBroadcastPage(@Valid PageParam reqVO) {
        return success(broadcastService.getBroadcastPage(reqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获取广播消息详情")
    @Parameter(name = "id", description = "消息ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('forum:system-broadcast:query')")
    public CommonResult<AdminSystemBroadcastRespVO> getBroadcast(@RequestParam("id") Long id) {
        return success(broadcastService.getBroadcast(id));
    }

}
