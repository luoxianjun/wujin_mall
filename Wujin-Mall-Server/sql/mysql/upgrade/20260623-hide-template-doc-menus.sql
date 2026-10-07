-- Hide template demo/documentation menus that should not appear in Wujin Mall admin.

UPDATE `system_menu`
SET `visible` = 0,
    `status` = 1,
    `updater` = '1',
    `update_time` = NOW()
WHERE `deleted` = 0
  AND `name` IN ('作者动态', 'Boot 开发文档', 'Cloud 开发文档');

-- Hide non-Wujin template/demo top-level modules that were inherited from the forum template.
UPDATE `system_menu`
SET `visible` = 0,
    `status` = 1,
    `updater` = '1',
    `update_time` = NOW()
WHERE `deleted` = 0
  AND `parent_id` = 0
  AND (
    `path` IN (
      '/ai',
      '/bpm',
      '/crm',
      '/erp',
      '/iot',
      '/mall',
      '/mp',
      '/pay',
      '/report'
    )
    OR `name` IN (
      'AI 大模型',
      'CRM 系统',
      'ERP 系统',
      'IoT 物联网',
      '公众号管理',
      '商城系统',
      '支付系统',
      '报表管理',
      '工作流程'
    )
  );

-- Hide student-forum and gamification leftovers from the backend permission menu.
UPDATE `system_menu`
SET `visible` = 0,
    `status` = 1,
    `updater` = '1',
    `update_time` = NOW()
WHERE `deleted` = 0
  AND (
    `path` LIKE '/forum%'
    OR `path` LIKE '/gamification%'
    OR `component` LIKE 'forum/%'
    OR `component` LIKE 'gamification/%'
    OR `name` IN ('激励中心', '邀请裂变', '答题活动', '论坛管理', '活动管理')
  );
