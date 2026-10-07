-- 轮胎原材料商品搜索测试数据
-- 可重复执行：固定 ID 仅用于五金商城演示租户（tenant_id = 1）。

SET NAMES utf8mb4;
SET @T := 1;
SET @U := '1';
SET @NOW := NOW();

START TRANSACTION;

INSERT INTO wujin_category
  (id, parent_id, lane, code, name, level, sort, status, display_depth, health_status, description,
   creator, create_time, updater, update_time, tenant_id)
VALUES
  (1200, 0, 'MATERIAL', 'M.TIRE', '轮胎原材料', 1, 1, 0, 2, 'HEALTHY', '轮胎制造所需原材料', @U, @NOW, @U, @NOW, @T),
  (1201, 1200, 'MATERIAL', 'M.TIRE.RUBBER', '橡胶材料', 2, 1, 0, 3, 'HEALTHY', '轮胎胶料主体材料', @U, @NOW, @U, @NOW, @T),
  (1202, 1201, 'MATERIAL', 'M.TIRE.RUBBER.NATURAL', '天然橡胶', 3, 1, 0, 3, 'HEALTHY', 'STR20、RSS3 等轮胎级天然橡胶', @U, @NOW, @U, @NOW, @T),
  (1203, 1201, 'MATERIAL', 'M.TIRE.RUBBER.SYNTHETIC', '合成橡胶', 3, 2, 0, 3, 'HEALTHY', '顺丁橡胶、丁苯橡胶等', @U, @NOW, @U, @NOW, @T),
  (1204, 1200, 'MATERIAL', 'M.TIRE.FILLER', '补强填料', 2, 2, 0, 3, 'HEALTHY', '炭黑等轮胎补强材料', @U, @NOW, @U, @NOW, @T),
  (1205, 1204, 'MATERIAL', 'M.TIRE.FILLER.CARBON_BLACK', '炭黑', 3, 1, 0, 3, 'HEALTHY', 'N330 等橡胶用炭黑', @U, @NOW, @U, @NOW, @T),
  (1206, 1200, 'MATERIAL', 'M.TIRE.SKELETON', '骨架材料', 2, 3, 0, 3, 'HEALTHY', '钢帘线和纤维帘布', @U, @NOW, @U, @NOW, @T),
  (1207, 1206, 'MATERIAL', 'M.TIRE.SKELETON.STEEL_CORD', '钢帘线', 3, 1, 0, 3, 'HEALTHY', '全钢子午线轮胎增强材料', @U, @NOW, @U, @NOW, @T)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id), lane = VALUES(lane), code = VALUES(code), name = VALUES(name),
  level = VALUES(level), sort = VALUES(sort), status = VALUES(status), display_depth = VALUES(display_depth),
  health_status = VALUES(health_status), description = VALUES(description), updater = @U, update_time = @NOW,
  deleted = b'0';

INSERT INTO wujin_category_mapping
  (id, source_category_id, source_lane, target_category_id, target_lane, mapping_type, confidence, status,
   risk_note, creator, create_time, updater, update_time, tenant_id)
VALUES
  (1200, 2, 'PRODUCT', 1200, 'MATERIAL', 'REQUIRES_MATERIAL', 100, 0,
   '轮胎切换原材料泳道时展示轮胎材料树', @U, @NOW, @U, @NOW, @T)
ON DUPLICATE KEY UPDATE
  source_category_id = VALUES(source_category_id), source_lane = VALUES(source_lane),
  target_category_id = VALUES(target_category_id), target_lane = VALUES(target_lane),
  mapping_type = VALUES(mapping_type), confidence = VALUES(confidence), status = VALUES(status),
  risk_note = VALUES(risk_note), updater = @U, update_time = @NOW, deleted = b'0';

INSERT INTO wujin_chain_entity
  (id, entity_code, name, lane, industries, junction_flag, risk_note, status,
   creator, create_time, updater, update_time, tenant_id)
VALUES
  (100, 'E.P.TIRE', '轮胎', 'PRODUCT', '轮胎', b'0', NULL, 0, @U, @NOW, @U, @NOW, @T),
  (200, 'E.M.NR', '天然橡胶', 'MATERIAL', '轮胎,医疗', b'1', '不同行业规格不可直接互换', 0, @U, @NOW, @U, @NOW, @T),
  (203, 'E.M.CARBONBLK', '炭黑', 'MATERIAL', '轮胎,涂料', b'1', NULL, 0, @U, @NOW, @U, @NOW, @T),
  (1204, 'E.M.SYNTHETIC_RUBBER', '合成橡胶', 'MATERIAL', '轮胎,工业橡胶', b'1', NULL, 0, @U, @NOW, @U, @NOW, @T),
  (1205, 'E.M.STEEL_CORD', '钢帘线', 'MATERIAL', '轮胎', b'0', NULL, 0, @U, @NOW, @U, @NOW, @T)
ON DUPLICATE KEY UPDATE
  entity_code = VALUES(entity_code), name = VALUES(name), lane = VALUES(lane), industries = VALUES(industries),
  junction_flag = VALUES(junction_flag), risk_note = VALUES(risk_note), status = VALUES(status),
  updater = @U, update_time = @NOW, deleted = b'0';

INSERT INTO wujin_chain_entity_relation
  (id, source_entity_id, target_entity_id, relation_type, weight, cost_ratio, industry_context,
   audit_status, audit_remark, creator, create_time, updater, update_time, tenant_id)
VALUES
  (4201, 100, 200, 'REQUIRES_MATERIAL', 100, 38, 'TIRE', 1, '轮胎行业基础关系', @U, @NOW, @U, @NOW, @T),
  (4202, 100, 1204, 'REQUIRES_MATERIAL', 95, 22, 'TIRE', 1, '轮胎行业基础关系', @U, @NOW, @U, @NOW, @T),
  (4203, 100, 203, 'REQUIRES_MATERIAL', 90, 18, 'TIRE', 1, '轮胎行业基础关系', @U, @NOW, @U, @NOW, @T),
  (4204, 100, 1205, 'REQUIRES_MATERIAL', 85, 14, 'TIRE', 1, '轮胎行业基础关系', @U, @NOW, @U, @NOW, @T)
ON DUPLICATE KEY UPDATE
  source_entity_id = VALUES(source_entity_id), target_entity_id = VALUES(target_entity_id),
  relation_type = VALUES(relation_type), weight = VALUES(weight), cost_ratio = VALUES(cost_ratio),
  industry_context = VALUES(industry_context), audit_status = VALUES(audit_status),
  audit_remark = VALUES(audit_remark), updater = @U, update_time = @NOW, deleted = b'0';

INSERT INTO product_category
  (id, parent_id, name, pic_url, big_pic_url, sort, status,
   creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
  (1220, 0, '工业原材料', '/static/icons/local/search-empty.png', NULL, 20, 0, @U, @NOW, @U, @NOW, b'0', @T),
  (1221, 1220, '橡胶原料', '/static/icons/local/search-empty.png', NULL, 1, 0, @U, @NOW, @U, @NOW, b'0', @T),
  (1222, 1220, '橡胶助剂与填料', '/static/icons/local/search-empty.png', NULL, 2, 0, @U, @NOW, @U, @NOW, b'0', @T),
  (1223, 1220, '轮胎骨架材料', '/static/icons/local/search-empty.png', NULL, 3, 0, @U, @NOW, @U, @NOW, b'0', @T)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id), name = VALUES(name), pic_url = VALUES(pic_url), sort = VALUES(sort),
  status = VALUES(status), updater = @U, update_time = @NOW, deleted = b'0';

INSERT INTO product_brand
  (id, name, pic_url, sort, description, status,
   creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
  (2020, '东南橡胶原料', '/static/icons/local/search-empty.png', 20, '轮胎级天然橡胶供应商', 0, @U, @NOW, @U, @NOW, b'0', @T),
  (2021, '华北合成橡胶', '/static/icons/local/search-empty.png', 21, '合成橡胶供应商', 0, @U, @NOW, @U, @NOW, b'0', @T),
  (2022, '中原炭黑', '/static/icons/local/search-empty.png', 22, '橡胶用炭黑供应商', 0, @U, @NOW, @U, @NOW, b'0', @T),
  (2023, '鲁东钢帘线', '/static/icons/local/search-empty.png', 23, '轮胎钢帘线供应商', 0, @U, @NOW, @U, @NOW, b'0', @T)
ON DUPLICATE KEY UPDATE
  name = VALUES(name), pic_url = VALUES(pic_url), sort = VALUES(sort), description = VALUES(description),
  status = VALUES(status), updater = @U, update_time = @NOW, deleted = b'0';

INSERT INTO product_spu
  (id, name, keyword, introduction, description, category_id, brand_id, pic_url, slider_pic_urls,
   sort, status, spec_type, price, market_price, cost_price, stock, delivery_types, delivery_template_id,
   give_integral, sub_commission_type, sales_count, virtual_sales_count, browse_count,
   creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
  (9201, '泰国 STR20 轮胎级天然橡胶', '天然橡胶,STR20,轮胎原料', '轮胎胎面及胎侧用标准胶',
   '20kg/包，支持批量采购并提供 SGS 检测报告', 1221, 2020, '/static/icons/local/search-empty.png',
   '["/static/icons/local/search-empty.png"]', 20, 1, b'0', 1250000, 1320000, 1180000, 200,
   '1', 1, 0, b'0', 36, 8, 120, @U, @NOW, @U, @NOW, b'0', @T),
  (9202, 'BR9000 高顺式顺丁合成橡胶', '合成橡胶,顺丁橡胶,BR9000,轮胎原料', '高耐磨轮胎用顺丁橡胶',
   '适用于乘用车轮胎和卡车胎配方', 1221, 2021, '/static/icons/local/search-empty.png',
   '["/static/icons/local/search-empty.png"]', 21, 1, b'0', 1380000, 1450000, 1290000, 160,
   '1', 1, 0, b'0', 24, 6, 90, @U, @NOW, @U, @NOW, b'0', @T),
  (9203, '橡胶用炭黑 N330', '炭黑,N330,轮胎补强', '通用高耐磨橡胶补强炭黑',
   '吨袋包装，适用于胎面胶和工业橡胶制品', 1222, 2022, '/static/icons/local/search-empty.png',
   '["/static/icons/local/search-empty.png"]', 22, 1, b'0', 820000, 880000, 760000, 300,
   '1', 1, 0, b'0', 28, 5, 76, @U, @NOW, @U, @NOW, b'0', @T),
  (9204, '高强度轮胎钢帘线 3+9+15', '钢帘线,轮胎骨架,3+9+15', '全钢子午线轮胎骨架增强材料',
   '镀铜钢帘线，支持规格定制和批次检测', 1223, 2023, '/static/icons/local/search-empty.png',
   '["/static/icons/local/search-empty.png"]', 23, 1, b'0', 1680000, 1780000, 1520000, 120,
   '1', 1, 0, b'0', 18, 4, 52, @U, @NOW, @U, @NOW, b'0', @T)
ON DUPLICATE KEY UPDATE
  name = VALUES(name), keyword = VALUES(keyword), introduction = VALUES(introduction), description = VALUES(description),
  category_id = VALUES(category_id), brand_id = VALUES(brand_id), pic_url = VALUES(pic_url),
  slider_pic_urls = VALUES(slider_pic_urls), sort = VALUES(sort), status = VALUES(status),
  price = VALUES(price), market_price = VALUES(market_price), cost_price = VALUES(cost_price), stock = VALUES(stock),
  sales_count = VALUES(sales_count), virtual_sales_count = VALUES(virtual_sales_count), browse_count = VALUES(browse_count),
  updater = @U, update_time = @NOW, deleted = b'0';

INSERT INTO product_sku
  (id, spu_id, properties, price, market_price, cost_price, bar_code, pic_url, stock, weight, volume,
   first_brokerage_price, second_brokerage_price, sales_count,
   creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
  (9301, 9201, '[]', 1250000, 1320000, 1180000, 'SKU-9201', '/static/icons/local/search-empty.png', 200, 1000, 1.5, 0, 0, 36, @U, @NOW, @U, @NOW, b'0', @T),
  (9302, 9202, '[]', 1380000, 1450000, 1290000, 'SKU-9202', '/static/icons/local/search-empty.png', 160, 1000, 1.5, 0, 0, 24, @U, @NOW, @U, @NOW, b'0', @T),
  (9303, 9203, '[]', 820000, 880000, 760000, 'SKU-9203', '/static/icons/local/search-empty.png', 300, 1000, 1.8, 0, 0, 28, @U, @NOW, @U, @NOW, b'0', @T),
  (9304, 9204, '[]', 1680000, 1780000, 1520000, 'SKU-9204', '/static/icons/local/search-empty.png', 120, 1000, 1.2, 0, 0, 18, @U, @NOW, @U, @NOW, b'0', @T)
ON DUPLICATE KEY UPDATE
  spu_id = VALUES(spu_id), price = VALUES(price), market_price = VALUES(market_price),
  cost_price = VALUES(cost_price), bar_code = VALUES(bar_code), pic_url = VALUES(pic_url), stock = VALUES(stock),
  weight = VALUES(weight), volume = VALUES(volume), sales_count = VALUES(sales_count),
  updater = @U, update_time = @NOW, deleted = b'0';

-- 停用旧演示数据中“成品商品同时供应其所需材料”的错误绑定。
UPDATE wujin_merchant_supply_capability
SET supply_status = 1,
    remark = '历史错误绑定已停用：成品所需材料不等于该商品的供应内容',
    updater = @U,
    update_time = @NOW
WHERE tenant_id = @T
  AND product_id IN (9001, 9002, 9003, 9004)
  AND lane IN ('MATERIAL', 'PROCESS');

INSERT INTO wujin_merchant_supply_capability
  (id, merchant_id, product_id, product_name, entity_id, lane, industry, supply_status,
   stock_count, min_order_quantity, delivery_days, service_area, remark,
   creator, create_time, updater, update_time, tenant_id)
VALUES
  (7201, 120, 9201, '泰国 STR20 轮胎级天然橡胶', 200, 'MATERIAL', 'TIRE', 0, 200, 1, 7, '全国', '天然橡胶现货供应', @U, @NOW, @U, @NOW, @T),
  (7202, 121, 9202, 'BR9000 高顺式顺丁合成橡胶', 1204, 'MATERIAL', 'TIRE', 0, 160, 1, 10, '华北,华东', '合成橡胶厂家直供', @U, @NOW, @U, @NOW, @T),
  (7203, 122, 9203, '橡胶用炭黑 N330', 203, 'MATERIAL', 'TIRE', 0, 300, 2, 5, '全国', '吨袋包装，可提供检测报告', @U, @NOW, @U, @NOW, @T),
  (7204, 123, 9204, '高强度轮胎钢帘线 3+9+15', 1205, 'MATERIAL', 'TIRE', 0, 120, 5, 12, '华东,华南', '支持规格定制', @U, @NOW, @U, @NOW, @T)
ON DUPLICATE KEY UPDATE
  merchant_id = VALUES(merchant_id), product_id = VALUES(product_id), product_name = VALUES(product_name),
  entity_id = VALUES(entity_id), lane = VALUES(lane), industry = VALUES(industry),
  supply_status = VALUES(supply_status), stock_count = VALUES(stock_count),
  min_order_quantity = VALUES(min_order_quantity), delivery_days = VALUES(delivery_days),
  service_area = VALUES(service_area), remark = VALUES(remark), updater = @U, update_time = @NOW,
  deleted = b'0';

COMMIT;
