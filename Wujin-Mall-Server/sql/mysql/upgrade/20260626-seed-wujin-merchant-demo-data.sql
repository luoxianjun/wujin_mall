-- 五金商家后台演示数据：关系申报、供应能力、寻源线索
-- 可重复执行；仅覆盖下方固定 ID 的演示数据。

SET NAMES utf8mb4;
SET @T := 1;
SET @U := '1';
SET @NOW := NOW();

START TRANSACTION;

INSERT INTO wujin_category
  (id, parent_id, lane, code, name, level, sort, status, display_depth, health_status, description, creator, create_time, updater, update_time, tenant_id)
VALUES
  (1, 0, 'PRODUCT', 'P', '五金成品', 1, 1, 0, 3, 'HEALTHY', '成品泳道根类目', @U, @NOW, @U, @NOW, @T),
  (2, 1, 'PRODUCT', 'P.TIRE', '轮胎', 2, 1, 0, 3, 'HEALTHY', '乘用车/卡车轮胎成品', @U, @NOW, @U, @NOW, @T),
  (3, 2, 'PRODUCT', 'P.TIRE.CAR', '乘用车轮胎', 3, 1, 0, 3, 'HEALTHY', '规格级最细层展示', @U, @NOW, @U, @NOW, @T),
  (4, 1, 'PRODUCT', 'P.FASTENER', '紧固件', 2, 2, 0, 3, 'HEALTHY', '螺栓螺母等紧固件', @U, @NOW, @U, @NOW, @T),
  (5, 4, 'PRODUCT', 'P.FASTENER.BOLT', '螺栓', 3, 1, 0, 3, 'HEALTHY', '各类螺栓', @U, @NOW, @U, @NOW, @T),
  (6, 5, 'PRODUCT', 'P.FASTENER.BOLT.HEX', '外六角螺栓', 4, 1, 0, 4, 'HEALTHY', '规格级：M6-M24', @U, @NOW, @U, @NOW, @T),
  (7, 1, 'PRODUCT', 'P.BEARING', '轴承', 2, 3, 0, 3, 'NEEDS_SPLIT', '类目过宽建议拆分', @U, @NOW, @U, @NOW, @T),
  (8, 7, 'PRODUCT', 'P.BEARING.DEEP', '深沟球轴承', 3, 1, 0, 3, 'HEALTHY', '规格级：6200 系列', @U, @NOW, @U, @NOW, @T)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id), lane = VALUES(lane), name = VALUES(name), level = VALUES(level),
  sort = VALUES(sort), status = VALUES(status), display_depth = VALUES(display_depth),
  health_status = VALUES(health_status), description = VALUES(description), updater = @U, update_time = @NOW;

INSERT INTO wujin_chain_entity
  (id, entity_code, name, lane, industries, junction_flag, risk_note, status, creator, create_time, updater, update_time, tenant_id)
VALUES
  (100, 'E.P.TIRE', '轮胎', 'PRODUCT', '轮胎', b'0', NULL, 0, @U, @NOW, @U, @NOW, @T),
  (101, 'E.P.BOLT', '外六角螺栓', 'PRODUCT', '紧固件', b'0', NULL, 0, @U, @NOW, @U, @NOW, @T),
  (102, 'E.P.BEARING', '深沟球轴承', 'PRODUCT', '轴承', b'0', NULL, 0, @U, @NOW, @U, @NOW, @T),
  (200, 'E.M.NR', '天然橡胶', 'MATERIAL', '轮胎,医疗', b'1', NULL, 0, @U, @NOW, @U, @NOW, @T),
  (201, 'E.M.CARBON', '碳素钢45#', 'MATERIAL', '紧固件,机械', b'1', NULL, 0, @U, @NOW, @U, @NOW, @T),
  (202, 'E.M.GCR15', '轴承钢GCr15', 'MATERIAL', '轴承', b'0', NULL, 0, @U, @NOW, @U, @NOW, @T),
  (203, 'E.M.CARBONBLK', '炭黑', 'MATERIAL', '轮胎,涂料', b'1', NULL, 0, @U, @NOW, @U, @NOW, @T),
  (300, 'E.W.VULCAN', '硫化', 'PROCESS', '轮胎', b'0', NULL, 0, @U, @NOW, @U, @NOW, @T),
  (301, 'E.W.FORGE', '冷镦锻造', 'PROCESS', '紧固件', b'0', NULL, 0, @U, @NOW, @U, @NOW, @T),
  (303, 'E.W.ZINC', '镀锌', 'PROCESS', '紧固件', b'0', NULL, 0, @U, @NOW, @U, @NOW, @T)
ON DUPLICATE KEY UPDATE
  name = VALUES(name), lane = VALUES(lane), industries = VALUES(industries), junction_flag = VALUES(junction_flag),
  risk_note = VALUES(risk_note), status = VALUES(status), updater = @U, update_time = @NOW;

INSERT INTO wujin_industry_template
  (id, template_code, name, industry_code, product_lane, status, remark, creator, create_time, updater, update_time, tenant_id)
VALUES
  (500, 'TPL.TIRE', '轮胎行业关系模板', 'TIRE', 'PRODUCT', 0, '成品轮胎必绑橡胶+硫化', @U, @NOW, @U, @NOW, @T),
  (501, 'TPL.FASTENER', '紧固件行业关系模板', 'FASTENER', 'PRODUCT', 0, '成品螺栓必绑钢材+锻造', @U, @NOW, @U, @NOW, @T),
  (502, 'TPL.BEARING', '轴承行业关系模板', 'BEARING', 'PRODUCT', 0, '成品轴承必绑轴承钢', @U, @NOW, @U, @NOW, @T)
ON DUPLICATE KEY UPDATE
  name = VALUES(name), industry_code = VALUES(industry_code), product_lane = VALUES(product_lane),
  status = VALUES(status), remark = VALUES(remark), updater = @U, update_time = @NOW;

INSERT INTO product_category
  (id, parent_id, name, pic_url, big_pic_url, sort, status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
  (1000, 0, '五金成品', 'https://cdn.wujin-mall.test/product/category/finished.png', NULL, 1, 0, @U, @NOW, @U, @NOW, b'0', @T),
  (1001, 1000, '轮胎', 'https://cdn.wujin-mall.test/product/category/tire.png', NULL, 2, 0, @U, @NOW, @U, @NOW, b'0', @T),
  (1002, 1000, '紧固件', 'https://cdn.wujin-mall.test/product/category/fastener.png', NULL, 3, 0, @U, @NOW, @U, @NOW, b'0', @T),
  (1003, 1000, '轴承', 'https://cdn.wujin-mall.test/product/category/bearing.png', NULL, 4, 0, @U, @NOW, @U, @NOW, b'0', @T)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id), name = VALUES(name), pic_url = VALUES(pic_url), big_pic_url = VALUES(big_pic_url),
  sort = VALUES(sort), status = VALUES(status), updater = @U, update_time = @NOW, deleted = b'0';

INSERT INTO product_brand
  (id, name, pic_url, sort, description, status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
  (2000, '米其林', 'https://cdn.wujin-mall.test/product/brand/michelin.png', 1, '轮胎品牌', 0, @U, @NOW, @U, @NOW, b'0', @T),
  (2001, '国标五金', 'https://cdn.wujin-mall.test/product/brand/guobiao.png', 2, '紧固件品牌', 0, @U, @NOW, @U, @NOW, b'0', @T),
  (2002, '中轴精工', 'https://cdn.wujin-mall.test/product/brand/bearing.png', 3, '轴承品牌', 0, @U, @NOW, @U, @NOW, b'0', @T)
ON DUPLICATE KEY UPDATE
  name = VALUES(name), pic_url = VALUES(pic_url), sort = VALUES(sort), description = VALUES(description),
  status = VALUES(status), updater = @U, update_time = @NOW, deleted = b'0';

INSERT INTO product_spu
  (id, name, keyword, introduction, description, category_id, brand_id, pic_url, slider_pic_urls, sort, status, spec_type, price, market_price, cost_price, stock, delivery_types, delivery_template_id, give_integral, sub_commission_type, sales_count, virtual_sales_count, browse_count, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
  (9001, '米其林 205/55R16 乘用车轮胎', '轮胎,205/55R16,乘用车', '适配家用轿车的常规轮胎', '适配家用轿车的常规轮胎', 1001, 2000, 'https://cdn.wujin-mall.test/product/spu/tire-9001-cover.png', '["https://cdn.wujin-mall.test/product/spu/tire-9001-1.png"]', 3, 1, b'0', 39900, 45900, 32000, 860, '1', 1, 100, b'0', 128, 32, 560, @U, @NOW, @U, @NOW, b'0', @T),
  (9002, '8.8级 M12 外六角螺栓', '螺栓,M12,外六角', '工业通用高强度螺栓', '工业通用高强度螺栓', 1002, 2001, 'https://cdn.wujin-mall.test/product/spu/bolt-9002-cover.png', '["https://cdn.wujin-mall.test/product/spu/bolt-9002-1.png"]', 2, 1, b'0', 1200, 1500, 800, 50000, '1', 1, 20, b'0', 76, 15, 244, @U, @NOW, @U, @NOW, b'0', @T),
  (9003, '6204 深沟球轴承', '轴承,6204,深沟球', '常规深沟球轴承', '常规深沟球轴承', 1003, 2002, 'https://cdn.wujin-mall.test/product/spu/bearing-9003-cover.png', '["https://cdn.wujin-mall.test/product/spu/bearing-9003-1.png"]', 1, 0, b'0', 8800, 9600, 6500, 0, '1', 1, 0, b'0', 21, 4, 88, @U, @NOW, @U, @NOW, b'0', @T),
  (9004, '卡车全钢子午线轮胎', '轮胎,卡车,全钢,子午线', '重载卡车耐磨轮胎', '重载卡车耐磨轮胎，适合长途运输场景', 1001, 2000, 'https://cdn.wujin-mall.test/product/spu/truck-tire-9004-cover.png', '["https://cdn.wujin-mall.test/product/spu/truck-tire-9004-1.png"]', 4, 0, b'0', 128000, 148000, 98000, 120, '1', 1, 160, b'0', 43, 12, 190, @U, @NOW, @U, @NOW, b'0', @T),
  (9005, 'GCr15 轴承钢供应', '轴承钢,GCr15,圆钢', '轴承钢原材料供应', 'GCr15 轴承钢圆钢，支持轴承加工企业批量采购', 1003, 2002, 'https://cdn.wujin-mall.test/product/spu/gcr15-9005-cover.png', '["https://cdn.wujin-mall.test/product/spu/gcr15-9005-1.png"]', 5, 1, b'0', 680000, 720000, 610000, 8000, '1', 1, 500, b'0', 18, 6, 75, @U, @NOW, @U, @NOW, b'0', @T),
  (9006, '304不锈钢内六角螺钉 M8', '螺钉,304不锈钢,M8,内六角', '耐腐蚀内六角螺钉', '304 不锈钢内六角螺钉，适用于设备装配和户外场景', 1002, 2001, 'https://cdn.wujin-mall.test/product/spu/screw-9006-cover.png', '["https://cdn.wujin-mall.test/product/spu/screw-9006-1.png"]', 6, 1, b'0', 260, 360, 160, 200000, '1', 1, 10, b'0', 95, 30, 420, @U, @NOW, @U, @NOW, b'0', @T),
  (9007, '碳钢45#线材供应', '碳钢,45#,线材,紧固件原料', '紧固件上游线材', '45# 碳钢线材，适配冷镦锻造工艺', 1002, 2001, 'https://cdn.wujin-mall.test/product/spu/carbon-wire-9007-cover.png', '["https://cdn.wujin-mall.test/product/spu/carbon-wire-9007-1.png"]', 7, 1, b'0', 520000, 560000, 470000, 30000, '1', 1, 300, b'0', 24, 10, 110, @U, @NOW, @U, @NOW, b'0', @T)
ON DUPLICATE KEY UPDATE
  name = VALUES(name), keyword = VALUES(keyword), introduction = VALUES(introduction), description = VALUES(description),
  category_id = VALUES(category_id), brand_id = VALUES(brand_id), pic_url = VALUES(pic_url),
  slider_pic_urls = VALUES(slider_pic_urls), sort = VALUES(sort), status = VALUES(status), spec_type = VALUES(spec_type),
  price = VALUES(price), market_price = VALUES(market_price), cost_price = VALUES(cost_price), stock = VALUES(stock),
  delivery_types = VALUES(delivery_types), delivery_template_id = VALUES(delivery_template_id),
  give_integral = VALUES(give_integral), sub_commission_type = VALUES(sub_commission_type),
  sales_count = VALUES(sales_count), virtual_sales_count = VALUES(virtual_sales_count),
  browse_count = VALUES(browse_count), updater = @U, update_time = @NOW, deleted = b'0';

INSERT INTO product_sku
  (id, spu_id, properties, price, market_price, cost_price, bar_code, pic_url, stock, weight, volume, first_brokerage_price, second_brokerage_price, sales_count, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
  (9101, 9001, '[]', 39900, 45900, 32000, 'SKU-9001', 'https://cdn.wujin-mall.test/product/spu/tire-9001-sku.png', 860, 12.5, 0.08, 1990, 890, 128, @U, @NOW, @U, @NOW, b'0', @T),
  (9102, 9002, '[]', 1200, 1500, 800, 'SKU-9002', 'https://cdn.wujin-mall.test/product/spu/bolt-9002-sku.png', 50000, 0.05, 0.001, 60, 30, 76, @U, @NOW, @U, @NOW, b'0', @T),
  (9103, 9003, '[]', 8800, 9600, 6500, 'SKU-9003', 'https://cdn.wujin-mall.test/product/spu/bearing-9003-sku.png', 0, 0.22, 0.002, 420, 180, 21, @U, @NOW, @U, @NOW, b'0', @T),
  (9104, 9004, '[]', 128000, 148000, 98000, 'SKU-9004', 'https://cdn.wujin-mall.test/product/spu/truck-tire-9004-sku.png', 120, 62.0, 0.32, 6400, 2800, 43, @U, @NOW, @U, @NOW, b'0', @T),
  (9105, 9005, '[]', 680000, 720000, 610000, 'SKU-9005', 'https://cdn.wujin-mall.test/product/spu/gcr15-9005-sku.png', 8000, 1000.0, 0.42, 12000, 6000, 18, @U, @NOW, @U, @NOW, b'0', @T),
  (9106, 9006, '[]', 260, 360, 160, 'SKU-9006', 'https://cdn.wujin-mall.test/product/spu/screw-9006-sku.png', 200000, 0.01, 0.0002, 12, 6, 95, @U, @NOW, @U, @NOW, b'0', @T),
  (9107, 9007, '[]', 520000, 560000, 470000, 'SKU-9007', 'https://cdn.wujin-mall.test/product/spu/carbon-wire-9007-sku.png', 30000, 1000.0, 0.55, 9000, 4500, 24, @U, @NOW, @U, @NOW, b'0', @T)
ON DUPLICATE KEY UPDATE
  spu_id = VALUES(spu_id), properties = VALUES(properties), price = VALUES(price), market_price = VALUES(market_price),
  cost_price = VALUES(cost_price), bar_code = VALUES(bar_code), pic_url = VALUES(pic_url), stock = VALUES(stock),
  weight = VALUES(weight), volume = VALUES(volume), first_brokerage_price = VALUES(first_brokerage_price),
  second_brokerage_price = VALUES(second_brokerage_price), sales_count = VALUES(sales_count),
  updater = @U, update_time = @NOW, deleted = b'0';

INSERT INTO wujin_merchant_relation_submission
  (id, merchant_id, product_id, product_name, product_lane, product_category_id, template_id, audit_status, audit_route, completeness_score, remark, creator, create_time, updater, update_time, tenant_id)
VALUES
  (600, 100, 9001, '米其林 205/55R16 乘用车轮胎', 'PRODUCT', 3, 500, 20, 'AUTO_APPROVE', 95, '模板必选项完整，自动通过', @U, @NOW, @U, @NOW, @T),
  (601, 103, 9002, '8.8级 M12 外六角螺栓', 'PRODUCT', 6, 501, 30, 'MANUAL_REVIEW', 90, '材料和工艺齐全，人工复核通过', @U, @NOW, @U, @NOW, @T),
  (602, 112, 9003, '6204 深沟球轴承', 'PRODUCT', 8, 502, 10, 'MANUAL_REVIEW', 55, '缺少必选轴承钢绑定，等待补充', @U, @NOW, @U, @NOW, @T),
  (603, 100, 9004, '卡车全钢子午线轮胎', 'PRODUCT', 2, 500, 40, 'MANUAL_REVIEW', 40, '移除模板必选硫化工艺，被驳回补充', @U, @NOW, @U, @NOW, @T)
ON DUPLICATE KEY UPDATE
  merchant_id = VALUES(merchant_id), product_id = VALUES(product_id), product_name = VALUES(product_name),
  product_lane = VALUES(product_lane), product_category_id = VALUES(product_category_id), template_id = VALUES(template_id),
  audit_status = VALUES(audit_status), audit_route = VALUES(audit_route), completeness_score = VALUES(completeness_score),
  remark = VALUES(remark), updater = @U, update_time = @NOW, deleted = b'0';

INSERT INTO wujin_merchant_relation_item
  (id, submission_id, entity_id, relation_type, from_template, required_flag, remark, creator, create_time, updater, update_time, tenant_id)
VALUES
  (610, 600, 200, 'REQUIRES_MATERIAL', b'1', b'1', '模板必选：天然橡胶', @U, @NOW, @U, @NOW, @T),
  (611, 600, 300, 'REQUIRES_PROCESS', b'1', b'1', '模板必选：硫化', @U, @NOW, @U, @NOW, @T),
  (612, 600, 203, 'REQUIRES_MATERIAL', b'1', b'0', '模板选填：炭黑', @U, @NOW, @U, @NOW, @T),
  (613, 601, 201, 'REQUIRES_MATERIAL', b'1', b'1', '模板必选：碳钢', @U, @NOW, @U, @NOW, @T),
  (614, 601, 301, 'REQUIRES_PROCESS', b'1', b'1', '模板必选：锻造', @U, @NOW, @U, @NOW, @T),
  (615, 601, 303, 'REQUIRES_PROCESS', b'0', b'0', '商家自定义：镀锌', @U, @NOW, @U, @NOW, @T),
  (616, 602, 202, 'REQUIRES_MATERIAL', b'1', b'1', '模板必选：轴承钢，待补充证明', @U, @NOW, @U, @NOW, @T),
  (617, 603, 200, 'REQUIRES_MATERIAL', b'1', b'1', '模板必选：天然橡胶', @U, @NOW, @U, @NOW, @T)
ON DUPLICATE KEY UPDATE
  submission_id = VALUES(submission_id), entity_id = VALUES(entity_id), relation_type = VALUES(relation_type),
  from_template = VALUES(from_template), required_flag = VALUES(required_flag), remark = VALUES(remark),
  updater = @U, update_time = @NOW, deleted = b'0';

INSERT INTO wujin_merchant_supply_capability
  (id, merchant_id, product_id, product_name, entity_id, lane, industry, supply_status, stock_count, min_order_quantity, delivery_days, service_area, remark, creator, create_time, updater, update_time, tenant_id)
VALUES
  (700, 100, 9001, '米其林 205/55R16 乘用车轮胎', 100, 'PRODUCT', '轮胎', 0, 1200, 4, 7, '华东,华南', '成品现货供应', @U, @NOW, @U, @NOW, @T),
  (701, 100, 9001, '天然橡胶供应', 200, 'MATERIAL', '轮胎', 0, 50000, 1000, 15, '全国', '原料配套供应', @U, @NOW, @U, @NOW, @T),
  (702, 103, 9002, '8.8级 M12 外六角螺栓', 101, 'PRODUCT', '紧固件', 0, 98000, 5000, 5, '华东', '成品供应', @U, @NOW, @U, @NOW, @T),
  (703, 103, 9002, '碳钢45#线材供应', 201, 'MATERIAL', '紧固件', 0, 30000, 2000, 10, '华东,华北', '原料配套供应', @U, @NOW, @U, @NOW, @T),
  (704, 112, 9003, '6204 深沟球轴承', 102, 'PRODUCT', '轴承', 1, 0, 100, 12, '华南', '暂时停供，缺料', @U, @NOW, @U, @NOW, @T),
  (705, 113, 9005, 'GCr15 轴承钢供应', 202, 'MATERIAL', '轴承', 0, 8000, 500, 20, '华东', '上游钢厂直供', @U, @NOW, @U, @NOW, @T)
ON DUPLICATE KEY UPDATE
  merchant_id = VALUES(merchant_id), product_id = VALUES(product_id), product_name = VALUES(product_name),
  entity_id = VALUES(entity_id), lane = VALUES(lane), industry = VALUES(industry), supply_status = VALUES(supply_status),
  stock_count = VALUES(stock_count), min_order_quantity = VALUES(min_order_quantity), delivery_days = VALUES(delivery_days),
  service_area = VALUES(service_area), remark = VALUES(remark), updater = @U, update_time = @NOW, deleted = b'0';

INSERT INTO wujin_sourcing_lead
  (id, user_id, keyword, lane, source_keyword, industry, supplier_id, supplier_name, merchant_id, contact_name, contact_phone, requirement, lead_status, dispatch_status, dispatch_remark, handle_remark, first_contact_time, quoted_time, converted_time, lost_time, process_duration_minutes, creator, create_time, updater, update_time, tenant_id)
VALUES
  (800, 110, '205/55R16轮胎', 'PRODUCT', '轮胎', '轮胎', 100, '米其林华东仓', 100, '张工', '13800001111', '采购乘用车轮胎2000条，要求3C认证', 'CONVERTED', 'DISPATCHED', '已分发至成品供应商', '已成交，签订季度框架', DATE_SUB(@NOW, INTERVAL 5 DAY), DATE_SUB(@NOW, INTERVAL 4 DAY), DATE_SUB(@NOW, INTERVAL 2 DAY), NULL, 4320, @U, @NOW, @U, @NOW, @T),
  (801, 111, 'M12螺栓', 'PRODUCT', '紧固件', '紧固件', 103, '紧固件优选商', 103, '李采购', '13800002222', 'M12 8.8级螺栓5万件，需镀锌', 'QUOTED', 'DISPATCHED', '已分发', '已报价待客户确认', DATE_SUB(@NOW, INTERVAL 3 DAY), DATE_SUB(@NOW, INTERVAL 1 DAY), NULL, NULL, 2880, @U, @NOW, @U, @NOW, @T),
  (802, 104, '天然橡胶', 'MATERIAL', '轮胎', '轮胎', NULL, '橡胶大宗供应', NULL, '王经理', '13800003333', '天然橡胶1#烟片50吨', 'CONTACTED', 'DISPATCHED', '原材料泳道分发', '已电话联系', DATE_SUB(@NOW, INTERVAL 1 DAY), NULL, NULL, NULL, 1440, @U, @NOW, @U, @NOW, @T),
  (803, NULL, '轴承钢GCr15', 'MATERIAL', '轴承', '轴承', NULL, NULL, NULL, '匿名访客', '13800004444', 'GCr15轴承钢询价', 'SUBMITTED', 'PENDING', NULL, NULL, NULL, NULL, NULL, NULL, NULL, @U, @NOW, @U, @NOW, @T),
  (804, 112, '深沟球轴承', 'PRODUCT', '轴承', '轴承', 112, '轴承直营店', 112, '赵工', '13800005555', '6204轴承1万套', 'LOST', 'DISPATCHED', '已分发', '客户选择其他供应商', DATE_SUB(@NOW, INTERVAL 6 DAY), NULL, NULL, DATE_SUB(@NOW, INTERVAL 3 DAY), 4320, @U, @NOW, @U, @NOW, @T)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id), keyword = VALUES(keyword), lane = VALUES(lane), source_keyword = VALUES(source_keyword),
  industry = VALUES(industry), supplier_id = VALUES(supplier_id), supplier_name = VALUES(supplier_name),
  merchant_id = VALUES(merchant_id), contact_name = VALUES(contact_name), contact_phone = VALUES(contact_phone),
  requirement = VALUES(requirement), lead_status = VALUES(lead_status), dispatch_status = VALUES(dispatch_status),
  dispatch_remark = VALUES(dispatch_remark), handle_remark = VALUES(handle_remark),
  first_contact_time = VALUES(first_contact_time), quoted_time = VALUES(quoted_time),
  converted_time = VALUES(converted_time), lost_time = VALUES(lost_time),
  process_duration_minutes = VALUES(process_duration_minutes), updater = @U, update_time = @NOW, deleted = b'0';

COMMIT;
