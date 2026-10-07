-- =============================================
-- 新增活动分类：抽奖、答题
-- Created: 2026-03-23
-- Updated: 2026-03-24 (fix: 实际 dict value 为 11/12)
-- =============================================

-- 添加"抽奖"活动分类到字典数据
INSERT INTO `system_dict_data` (
  `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT 11, '抽奖', '11', 'frum_activity_type', 0, 'warning', '', '抽奖活动',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_dict_data`
  WHERE `deleted` = b'0' AND `dict_type` = 'frum_activity_type' AND `value` = '11'
);

-- 添加"答题"活动分类到字典数据
INSERT INTO `system_dict_data` (
  `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`,
  `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT 12, '答题', '12', 'frum_activity_type', 0, 'success', '', '答题活动',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_dict_data`
  WHERE `deleted` = b'0' AND `dict_type` = 'frum_activity_type' AND `value` = '12'
);
