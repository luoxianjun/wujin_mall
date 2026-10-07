package cn.iocoder.yudao.module.forum.controller.app.message;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.forum.controller.app.message.vo.*;
import cn.iocoder.yudao.module.forum.service.message.ForumInteractionUnreadService;
import cn.iocoder.yudao.module.forum.service.message.ForumMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 论坛消息 Controller
 *
 * @author forum
 */
@Tag(name = "用户 APP - 论坛消息")
@RestController
@RequestMapping("/forum/message")
@Validated
public class AppMessageController {

    @Resource
    private ForumMessageService messageService;

    @Resource
    private ForumInteractionUnreadService interactionUnreadService;

    @PostMapping("/send")
    @Operation(summary = "发送私信")
    @PreAuthenticated
    public CommonResult<Long> sendMessage(@Valid @RequestBody AppSendMessageReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(messageService.sendMessage(userId, reqVO));
    }

    @GetMapping("/conversation/page")
    @Operation(summary = "获取会话列表")
    @PreAuthenticated
    public CommonResult<PageResult<AppConversationRespVO>> getConversationPage(@Valid AppConversationPageReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(messageService.getConversationPage(userId, reqVO));
    }

    @GetMapping("/page")
    @Operation(summary = "获取会话消息列表")
    @PreAuthenticated
    public CommonResult<PageResult<AppMessageRespVO>> getMessagePage(@Valid AppMessagePageReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(messageService.getMessagePage(userId, reqVO));
    }

    @PostMapping("/mark-read")
    @Operation(summary = "标记会话消息为已读")
    @Parameter(name = "conversationId", description = "会话ID", required = true, example = "1")
    @PreAuthenticated
    public CommonResult<Boolean> markConversationAsRead(@RequestParam("conversationId") Long conversationId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        messageService.markConversationAsRead(userId, conversationId);
        return success(true);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读消息统计")
    @PreAuthenticated
    public CommonResult<AppUnreadCountRespVO> getUnreadCount() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(messageService.getUnreadCount(userId));
    }

    @GetMapping("/notice/page")
    @Operation(summary = "获取系统通知列表")
    @PreAuthenticated
    public CommonResult<PageResult<AppSystemNoticeRespVO>> getSystemNoticePage(@Valid AppSystemNoticePageReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(messageService.getSystemNoticePage(userId, reqVO));
    }

    @PostMapping("/notice/mark-read")
    @Operation(summary = "标记系统通知为已读")
    @Parameter(name = "noticeId", description = "通知ID", required = true, example = "1")
    @PreAuthenticated
    public CommonResult<Boolean> markNoticeAsRead(@RequestParam("noticeId") Long noticeId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        messageService.markNoticeAsRead(userId, noticeId);
        return success(true);
    }

    @PostMapping("/notice/mark-all-read")
    @Operation(summary = "标记所有系统通知为已读")
    @PreAuthenticated
    public CommonResult<Boolean> markAllNoticesAsRead() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        messageService.markAllNoticesAsRead(userId);
        return success(true);
    }

    @GetMapping("/interaction/unread-count")
    @Operation(summary = "获取互动未读统计")
    @PreAuthenticated
    public CommonResult<AppInteractionUnreadRespVO> getInteractionUnreadCount() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(interactionUnreadService.getUnreadCount(userId));
    }

}
