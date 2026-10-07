-- 添加活动开始提醒定时任务
-- 每5分钟执行一次，查询2小时后开始的活动并通知已报名的用户

INSERT INTO `infra_job` (`name`, `status`, `handler_name`, `handler_param`, `cron_expression`, `retry_count`, `retry_interval`, `monitor_timeout`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) 
VALUES ('活动开始提醒 Job', 2, 'activityStartNotifyJob', NULL, '0 */5 * * * ?', 0, 0, 0, 'admin', NOW(), 'admin', NOW(), b'0');