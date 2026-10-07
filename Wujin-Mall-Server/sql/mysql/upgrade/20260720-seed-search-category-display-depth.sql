-- 搜索分类单层/多层展示测试数据
-- 可重复执行：轮胎展开一个直接下级层，安全防护只展示命中层。

SET NAMES utf8mb4;
SET @T := 1;
SET @U := '1';
SET @NOW := NOW();

START TRANSACTION;

INSERT INTO wujin_category
  (id, parent_id, lane, code, name, level, sort, status, display_depth, health_status, description,
   creator, create_time, updater, update_time, tenant_id)
VALUES
  (2, 1, 'PRODUCT', 'P.TIRE', '轮胎', 2, 1, 0, 3, 'HEALTHY',
   '搜索命中轮胎时展示当前分类及多个直接下级', @U, @NOW, @U, @NOW, @T),
  (3, 2, 'PRODUCT', 'P.TIRE.CAR', '乘用车轮胎', 3, 1, 0, 3, 'HEALTHY',
   '轮胎多层搜索展示测试下级', @U, @NOW, @U, @NOW, @T),
  (1320, 1, 'PRODUCT', 'P.SAFETY', '安全防护', 2, 4, 0, 2, 'HEALTHY',
   '搜索命中时只展示安全防护当前层', @U, @NOW, @U, @NOW, @T),
  (1321, 1320, 'PRODUCT', 'P.SAFETY.GLOVE', '劳保手套', 3, 1, 0, 3, 'HEALTHY',
   '安全防护单层展示的隐藏下级测试数据', @U, @NOW, @U, @NOW, @T),
  (1322, 1320, 'PRODUCT', 'P.SAFETY.HELMET', '安全帽', 3, 2, 0, 3, 'HEALTHY',
   '安全防护单层展示的隐藏下级测试数据', @U, @NOW, @U, @NOW, @T)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id), lane = VALUES(lane), code = VALUES(code), name = VALUES(name),
  level = VALUES(level), sort = VALUES(sort), status = VALUES(status), display_depth = VALUES(display_depth),
  health_status = VALUES(health_status), description = VALUES(description), updater = @U, update_time = @NOW,
  deleted = b'0';

-- 兼容已存在但编码不规范的同名测试分类，避免重复造“小推车轮胎”。
SET @TIRE_TROLLEY_ID := (
  SELECT id
  FROM wujin_category
  WHERE tenant_id = @T
    AND deleted = b'0'
    AND (code = 'P.TIRE.TROLLEY' OR (parent_id = 2 AND name = '小推车轮胎'))
  ORDER BY IF(code = 'P.TIRE.TROLLEY', 0, 1), id
  LIMIT 1
);

INSERT INTO wujin_category
  (parent_id, lane, code, name, level, sort, status, display_depth, health_status, description,
   creator, create_time, updater, update_time, tenant_id)
SELECT
  2, 'PRODUCT', 'P.TIRE.TROLLEY', '小推车轮胎', 3, 2, 0, 3, 'HEALTHY',
  '轮胎多层搜索展示测试下级', @U, @NOW, @U, @NOW, @T
WHERE @TIRE_TROLLEY_ID IS NULL;

SET @TIRE_TROLLEY_ID := COALESCE(@TIRE_TROLLEY_ID, LAST_INSERT_ID());

UPDATE wujin_category
SET parent_id = 2,
    lane = 'PRODUCT',
    code = 'P.TIRE.TROLLEY',
    name = '小推车轮胎',
    level = 3,
    sort = 2,
    status = 0,
    display_depth = 3,
    health_status = 'HEALTHY',
    description = '轮胎多层搜索展示测试下级',
    updater = @U,
    update_time = @NOW,
    deleted = b'0'
WHERE id = @TIRE_TROLLEY_ID;

COMMIT;
