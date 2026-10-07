-- 添加答题活动奖励结算定时任务
-- 说明：
-- 1. 本脚本只负责补充 infra_job 记录，不会直接把任务注册到 Quartz 运行时。
-- 2. 执行完成后，还需要在管理后台“定时任务”里执行一次“同步定时任务”，
--    或调用 /admin-api/infra/job/sync，让 Quartz 载入该任务。
-- 3. handler_name 必须对应 Spring Bean 名：quizRewardDistributionJob

INSERT INTO `infra_job` (
  `name`,
  `status`,
  `handler_name`,
  `handler_param`,
  `cron_expression`,
  `retry_count`,
  `retry_interval`,
  `monitor_timeout`,
  `creator`,
  `create_time`,
  `updater`,
  `update_time`,
  `deleted`
)
SELECT
  '答题奖励结算 Job',
  1,
  'quizRewardDistributionJob',
  NULL,
  '0 */5 * * * ?',
  0,
  0,
  0,
  'admin',
  NOW(),
  'admin',
  NOW(),
  b'0'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1
  FROM `infra_job`
  WHERE `handler_name` = 'quizRewardDistributionJob'
    AND `deleted` = b'0'
);
