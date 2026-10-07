package cn.iocoder.yudao.module.gamification.service.notification;

import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class GamificationNotificationService {

    private static final Logger log = LoggerFactory.getLogger(GamificationNotificationService.class);

    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    public boolean sendInvitationRewardNotification(Long userId, String inviteeName, Integer points) {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("inviteeName", inviteeName);
        templateParams.put("points", points);

        return sendNotification(
                userId,
                "INVITATION_REWARD",
                "Invitation Reward",
                String.format(
                        "The invited user %s completed the task and you received %d points.",
                        inviteeName,
                        points),
                templateParams);
    }

    public boolean sendWelcomeNotification(Long userId, Integer points) {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("points", points);

        return sendNotification(
                userId,
                "WELCOME",
                "Welcome",
                String.format("Welcome to the student forum. You received %d bonus points.", points),
                templateParams);
    }

    public boolean sendPointsChangeNotification(Long userId, Integer points, String reason) {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("points", Math.abs(points));
        templateParams.put("change", points > 0 ? "increase" : "decrease");
        templateParams.put("reason", reason);

        String content = String.format(
                "Your points %s by %d. Reason: %s",
                points > 0 ? "increased" : "decreased",
                Math.abs(points),
                reason);

        return sendNotification(userId, "POINTS_CHANGE", "Points Update", content, templateParams);
    }

    /**
     * 活动开始时发送确认参与提醒 (CROSS-02)
     * Sends a participation reminder when a forum activity is about to start.
     */
    public boolean sendActivityStartReminder(Long userId, Long activityId, String activityTitle) {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("activityTitle", activityTitle);
        templateParams.put("activityId", activityId);

        String content = String.format("您参与的活动「%s」即将开始，请做好准备！", activityTitle);
        return sendNotification(userId, "ACTIVITY_START_REMINDER", "活动即将开始", content, templateParams);
    }

    /**
     * 批量发送活动开始提醒
     */
    public int batchSendActivityStartReminder(java.util.List<Long> userIds, Long activityId, String activityTitle) {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("activityTitle", activityTitle);
        templateParams.put("activityId", activityId);

        String content = String.format("您参与的活动「%s」即将开始，请做好准备！", activityTitle);
        return batchSendNotification(userIds, "ACTIVITY_START_REMINDER", "活动即将开始", content, templateParams);
    }

    public boolean sendNotification(
            Long userId,
            String templateCode,
            String title,
            String content,
            Map<String, Object> templateParams) {
        if (userId == null) {
            log.warn("[sendNotification] Invalid userId: null");
            return false;
        }

        try {
            NotifySendSingleToUserReqDTO reqDTO = new NotifySendSingleToUserReqDTO();
            reqDTO.setUserId(userId);
            reqDTO.setTemplateCode(templateCode);
            reqDTO.setTemplateParams(templateParams != null ? templateParams : new HashMap<>());

            Long messageId = notifyMessageSendApi.sendSingleMessageToMember(reqDTO);
            if (messageId != null) {
                log.info(
                        "[sendNotification] Success: userId={}, templateCode={}, messageId={}",
                        userId,
                        templateCode,
                        messageId);
                return true;
            }
            log.error("[sendNotification] Failed: userId={}, templateCode={}, messageId=null",
                    userId, templateCode);
            return false;
        } catch (Exception e) {
            log.error("[sendNotification] Exception: userId={}, templateCode={}", userId, templateCode, e);
            return false;
        }
    }

    public int batchSendNotification(
            java.util.List<Long> userIds,
            String templateCode,
            String title,
            String content,
            Map<String, Object> templateParams) {
        if (userIds == null || userIds.isEmpty()) {
            log.warn("[batchSendNotification] Invalid userIds");
            return 0;
        }

        int successCount = 0;
        for (Long userId : userIds) {
            if (sendNotification(userId, templateCode, title, content, templateParams)) {
                successCount++;
            }
        }

        log.info("[batchSendNotification] Completed: total={}, success={}, templateCode={}",
                userIds.size(), successCount, templateCode);
        return successCount;
    }
}
