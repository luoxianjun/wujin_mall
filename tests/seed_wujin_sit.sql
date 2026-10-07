-- =====================================================================
-- 五金商城 三泳道动态分类与产业链溯源系统 - SIT 测试数据种子
-- 数据库: wujin_mall_sit
-- 说明: 围绕「轮胎 / 紧固件 / 轴承」三个行业构建跨泳道(成品/加工/原材料)
--       的一致性数据，含分类树、映射、产业链实体与关系、行业模板、
--       商家关系申报、供应能力索引、寻源线索、审核、搜索行为与监控快照。
-- ID 策略: 跨表引用的行使用显式 ID，保证外键式一致性；可重复执行(先清后插)。
-- 租户: tenant_id = 1 (默认管理租户)；creator/updater = '1' (admin)
-- =====================================================================

SET NAMES utf8mb4;
SET @T := 1;          -- tenant_id
SET @U := '1';        -- 操作人(admin)
SET @NOW := NOW();

START TRANSACTION;

-- 清理(便于重复执行) ---------------------------------------------------
DELETE FROM wujin_monitor_snapshot           WHERE tenant_id = @T;
DELETE FROM wujin_search_rule_config         WHERE tenant_id = @T;
DELETE FROM wujin_search_behavior_log        WHERE tenant_id = @T;
DELETE FROM wujin_relation_audit_record      WHERE tenant_id = @T;
DELETE FROM wujin_sourcing_lead              WHERE tenant_id = @T;
DELETE FROM wujin_merchant_supply_capability WHERE tenant_id = @T;
DELETE FROM wujin_merchant_relation_item     WHERE tenant_id = @T;
DELETE FROM wujin_merchant_relation_submission WHERE tenant_id = @T;
DELETE FROM wujin_industry_template_item     WHERE tenant_id = @T;
DELETE FROM wujin_industry_template          WHERE tenant_id = @T;
DELETE FROM wujin_chain_entity_relation      WHERE tenant_id = @T;
DELETE FROM wujin_chain_entity               WHERE tenant_id = @T;
DELETE FROM wujin_category_mapping           WHERE tenant_id = @T;
DELETE FROM wujin_category                   WHERE tenant_id = @T;

-- =====================================================================
-- 1. 三泳道分类树 wujin_category  (status: 0=开启)
--    health_status: HEALTHY / NEEDS_SPLIT / UNBOUND
-- =====================================================================
INSERT INTO wujin_category
  (id, parent_id, lane, code, name, level, sort, status, display_depth, health_status, description, creator, create_time, updater, update_time, tenant_id) VALUES
-- 成品树 PRODUCT (1-8)
 (1, 0,'PRODUCT','P',                '五金成品',     1,1,0,3,'HEALTHY','成品泳道根类目',                @U,@NOW,@U,@NOW,@T),
 (2, 1,'PRODUCT','P.TIRE',           '轮胎',         2,1,0,3,'HEALTHY','乘用车/卡车轮胎成品',          @U,@NOW,@U,@NOW,@T),
 (3, 2,'PRODUCT','P.TIRE.CAR',       '乘用车轮胎',   3,1,0,3,'HEALTHY','规格级最细层展示',            @U,@NOW,@U,@NOW,@T),
 (4, 1,'PRODUCT','P.FASTENER',       '紧固件',       2,2,0,3,'HEALTHY','螺栓螺母等紧固件',            @U,@NOW,@U,@NOW,@T),
 (5, 4,'PRODUCT','P.FASTENER.BOLT',  '螺栓',         3,1,0,3,'HEALTHY','各类螺栓',                    @U,@NOW,@U,@NOW,@T),
 (6, 5,'PRODUCT','P.FASTENER.BOLT.HEX','外六角螺栓', 4,1,0,4,'HEALTHY','规格级:M6-M24',              @U,@NOW,@U,@NOW,@T),
 (7, 1,'PRODUCT','P.BEARING',        '轴承',         2,3,0,3,'NEEDS_SPLIT','类目过宽建议拆分',        @U,@NOW,@U,@NOW,@T),
 (8, 7,'PRODUCT','P.BEARING.DEEP',   '深沟球轴承',   3,1,0,3,'HEALTHY','规格级:6200系列',            @U,@NOW,@U,@NOW,@T),
-- 原材料树 MATERIAL (20-26)
 (20,0, 'MATERIAL','M',              '五金原材料',   1,1,0,1,'HEALTHY','原材料泳道根类目',            @U,@NOW,@U,@NOW,@T),
 (21,20,'MATERIAL','M.RUBBER',       '橡胶',         2,1,0,1,'HEALTHY','天然/合成橡胶',              @U,@NOW,@U,@NOW,@T),
 (22,21,'MATERIAL','M.RUBBER.NR',    '天然橡胶',     3,1,0,3,'HEALTHY','规格级:1#烟片/标准胶',       @U,@NOW,@U,@NOW,@T),
 (23,20,'MATERIAL','M.STEEL',        '钢材',         2,2,0,1,'HEALTHY','碳钢/合金钢',                @U,@NOW,@U,@NOW,@T),
 (24,23,'MATERIAL','M.STEEL.CARBON', '碳素钢',       3,1,0,2,'HEALTHY','45#/Q235',                  @U,@NOW,@U,@NOW,@T),
 (25,23,'MATERIAL','M.STEEL.BEARING','轴承钢',       3,2,0,2,'HEALTHY','GCr15',                     @U,@NOW,@U,@NOW,@T),
 (26,20,'MATERIAL','M.LATEX.MED',    '医用乳胶',     2,3,0,1,'UNBOUND','多行业适用,不可互换,含风险',  @U,@NOW,@U,@NOW,@T),
-- 加工工艺树 PROCESS (40-47)
 (40,0, 'PROCESS','W',               '五金加工工艺', 1,1,0,2,'HEALTHY','加工泳道根类目',              @U,@NOW,@U,@NOW,@T),
 (41,40,'PROCESS','W.HEAT',          '热处理',       2,1,0,2,'HEALTHY','淬火/回火/退火',             @U,@NOW,@U,@NOW,@T),
 (42,41,'PROCESS','W.HEAT.QUENCH',   '淬火',         3,1,0,2,'HEALTHY','提高硬度',                   @U,@NOW,@U,@NOW,@T),
 (43,40,'PROCESS','W.SURFACE',       '表面处理',     2,2,0,2,'HEALTHY','镀锌/发黑/钝化',             @U,@NOW,@U,@NOW,@T),
 (44,43,'PROCESS','W.SURFACE.ZINC',  '镀锌',         3,1,0,2,'HEALTHY','防腐蚀',                     @U,@NOW,@U,@NOW,@T),
 (45,40,'PROCESS','W.FORM',          '成型工艺',     2,3,0,2,'HEALTHY','硫化/锻造/铸造',             @U,@NOW,@U,@NOW,@T),
 (46,45,'PROCESS','W.FORM.VULCAN',   '硫化',         3,1,0,2,'HEALTHY','橡胶硫化成型',               @U,@NOW,@U,@NOW,@T),
 (47,45,'PROCESS','W.FORM.FORGE',    '锻造',         3,2,0,2,'HEALTHY','冷镦/热锻',                  @U,@NOW,@U,@NOW,@T);

START TRANSACTION;

-- =====================================================================
-- 2. 跨泳道分类映射 wujin_category_mapping
--    mapping_type: PRODUCT_TO_MATERIAL / PRODUCT_TO_PROCESS / MATERIAL_TO_PRODUCT
--    confidence 0-100; status 0=开启
-- =====================================================================
INSERT INTO wujin_category_mapping
  (source_category_id, source_lane, target_category_id, target_lane, mapping_type, confidence, status, risk_note, creator, create_time, updater, update_time, tenant_id) VALUES
 (2, 'PRODUCT', 21,'MATERIAL','PRODUCT_TO_MATERIAL',95,0,NULL,                       @U,@NOW,@U,@NOW,@T), -- 轮胎->橡胶
 (2, 'PRODUCT', 45,'PROCESS', 'PRODUCT_TO_PROCESS', 90,0,NULL,                       @U,@NOW,@U,@NOW,@T), -- 轮胎->成型(硫化)
 (4, 'PRODUCT', 23,'MATERIAL','PRODUCT_TO_MATERIAL',92,0,NULL,                       @U,@NOW,@U,@NOW,@T), -- 紧固件->钢材
 (4, 'PRODUCT', 41,'PROCESS', 'PRODUCT_TO_PROCESS', 88,0,NULL,                       @U,@NOW,@U,@NOW,@T), -- 紧固件->热处理
 (4, 'PRODUCT', 43,'PROCESS', 'PRODUCT_TO_PROCESS', 85,0,NULL,                       @U,@NOW,@U,@NOW,@T), -- 紧固件->表面处理
 (7, 'PRODUCT', 25,'MATERIAL','PRODUCT_TO_MATERIAL',96,0,NULL,                       @U,@NOW,@U,@NOW,@T), -- 轴承->轴承钢
 (26,'MATERIAL',2, 'PRODUCT', 'MATERIAL_TO_PRODUCT',60,0,'医用乳胶多行业适用,跨行业不可互换需提示',@U,@NOW,@U,@NOW,@T);

-- =====================================================================
-- 3. 产业链实体 wujin_chain_entity  (status 0=开启; junction_flag 交汇点)
--    显式 ID: 成品(100+)/材料(200+)/工艺(300+)
-- =====================================================================
INSERT INTO wujin_chain_entity
  (id, entity_code, name, lane, industries, junction_flag, risk_note, status, creator, create_time, updater, update_time, tenant_id) VALUES
 (100,'E.P.TIRE',     '轮胎',         'PRODUCT', '轮胎',           b'0',NULL,0,@U,@NOW,@U,@NOW,@T),
 (101,'E.P.BOLT',     '外六角螺栓',   'PRODUCT', '紧固件',         b'0',NULL,0,@U,@NOW,@U,@NOW,@T),
 (102,'E.P.BEARING',  '深沟球轴承',   'PRODUCT', '轴承',           b'0',NULL,0,@U,@NOW,@U,@NOW,@T),
 (200,'E.M.NR',       '天然橡胶',     'MATERIAL','轮胎,医疗',      b'1',NULL,0,@U,@NOW,@U,@NOW,@T), -- 交汇点:多行业
 (201,'E.M.CARBON',   '碳素钢45#',    'MATERIAL','紧固件,机械',    b'1',NULL,0,@U,@NOW,@U,@NOW,@T), -- 交汇点
 (202,'E.M.GCR15',    '轴承钢GCr15',  'MATERIAL','轴承',           b'0',NULL,0,@U,@NOW,@U,@NOW,@T),
 (203,'E.M.CARBONBLK','炭黑',         'MATERIAL','轮胎,涂料',      b'1',NULL,0,@U,@NOW,@U,@NOW,@T),
 (204,'E.M.LATEX.MED','医用乳胶',     'MATERIAL','医疗器械,轮胎',  b'1','医疗器械级,跨行业不可互换',0,@U,@NOW,@U,@NOW,@T),
 (300,'E.W.VULCAN',   '硫化',         'PROCESS', '轮胎',           b'0',NULL,0,@U,@NOW,@U,@NOW,@T),
 (301,'E.W.FORGE',    '冷镦锻造',     'PROCESS', '紧固件',         b'0',NULL,0,@U,@NOW,@U,@NOW,@T),
 (302,'E.W.HEAT',     '热处理淬火',   'PROCESS', '紧固件,轴承',    b'1',NULL,0,@U,@NOW,@U,@NOW,@T), -- 交汇点
 (303,'E.W.ZINC',     '镀锌',         'PROCESS', '紧固件',         b'0',NULL,0,@U,@NOW,@U,@NOW,@T);

-- =====================================================================
-- 4. 产业链上下游关系 wujin_chain_entity_relation
--    relation_type: REQUIRES_MATERIAL / REQUIRES_PROCESS
--    audit_status: 0=待审 1=通过 2=驳回 ; weight/cost_ratio 百分制
-- =====================================================================
INSERT INTO wujin_chain_entity_relation
  (source_entity_id, target_entity_id, relation_type, weight, cost_ratio, industry_context, audit_status, audit_remark, creator, create_time, updater, update_time, tenant_id) VALUES
 (100,200,'REQUIRES_MATERIAL',60,45,'轮胎',  1,'通过',     @U,@NOW,@U,@NOW,@T), -- 轮胎<-天然橡胶
 (100,203,'REQUIRES_MATERIAL',25,15,'轮胎',  1,'通过',     @U,@NOW,@U,@NOW,@T), -- 轮胎<-炭黑
 (100,300,'REQUIRES_PROCESS', 40,20,'轮胎',  1,'通过',     @U,@NOW,@U,@NOW,@T), -- 轮胎<-硫化
 (101,201,'REQUIRES_MATERIAL',70,50,'紧固件',1,'通过',     @U,@NOW,@U,@NOW,@T), -- 螺栓<-碳钢
 (101,301,'REQUIRES_PROCESS', 30,25,'紧固件',1,'通过',     @U,@NOW,@U,@NOW,@T), -- 螺栓<-锻造
 (101,302,'REQUIRES_PROCESS', 35,18,'紧固件',1,'通过',     @U,@NOW,@U,@NOW,@T), -- 螺栓<-热处理
 (101,303,'REQUIRES_PROCESS', 20,8, '紧固件',0,NULL,       @U,@NOW,@U,@NOW,@T), -- 螺栓<-镀锌(待审)
 (102,202,'REQUIRES_MATERIAL',75,55,'轴承',  1,'通过',     @U,@NOW,@U,@NOW,@T), -- 轴承<-轴承钢
 (102,302,'REQUIRES_PROCESS', 45,22,'轴承',  2,'权重待复核驳回',@U,@NOW,@U,@NOW,@T); -- 轴承<-热处理(驳回)

-- =====================================================================
-- 5. 行业模板 wujin_industry_template  (显式 ID 500+; status 0=开启)
-- =====================================================================
INSERT INTO wujin_industry_template
  (id, template_code, name, industry_code, product_lane, status, remark, creator, create_time, updater, update_time, tenant_id) VALUES
 (500,'TPL.TIRE',    '轮胎行业关系模板',   'TIRE',    'PRODUCT',0,'成品轮胎必绑橡胶+硫化',@U,@NOW,@U,@NOW,@T),
 (501,'TPL.FASTENER','紧固件行业关系模板', 'FASTENER','PRODUCT',0,'成品螺栓必绑钢材+锻造',@U,@NOW,@U,@NOW,@T),
 (502,'TPL.BEARING', '轴承行业关系模板',   'BEARING', 'PRODUCT',0,'成品轴承必绑轴承钢',  @U,@NOW,@U,@NOW,@T);

-- =====================================================================
-- 6. 行业模板项 wujin_industry_template_item  (required_flag 必填; weight 百分制)
-- =====================================================================
INSERT INTO wujin_industry_template_item
  (template_id, entity_id, relation_type, required_flag, sort, weight, remark, creator, create_time, updater, update_time, tenant_id) VALUES
 (500,200,'REQUIRES_MATERIAL',b'1',1,50,'必选:天然橡胶',  @U,@NOW,@U,@NOW,@T),
 (500,203,'REQUIRES_MATERIAL',b'0',2,20,'选填:炭黑',      @U,@NOW,@U,@NOW,@T),
 (500,300,'REQUIRES_PROCESS', b'1',3,30,'必选:硫化',      @U,@NOW,@U,@NOW,@T),
 (501,201,'REQUIRES_MATERIAL',b'1',1,55,'必选:碳钢',      @U,@NOW,@U,@NOW,@T),
 (501,301,'REQUIRES_PROCESS', b'1',2,25,'必选:锻造',      @U,@NOW,@U,@NOW,@T),
 (501,302,'REQUIRES_PROCESS', b'0',3,20,'选填:热处理',    @U,@NOW,@U,@NOW,@T),
 (502,202,'REQUIRES_MATERIAL',b'1',1,60,'必选:轴承钢',    @U,@NOW,@U,@NOW,@T),
 (502,302,'REQUIRES_PROCESS', b'0',2,40,'选填:热处理',    @U,@NOW,@U,@NOW,@T);

COMMIT;
-- (申报/供应/寻源/审核/搜索/监控 在下一段追加)

START TRANSACTION;

-- =====================================================================
-- 7. 商家关系申报 wujin_merchant_relation_submission (显式 ID 600+)
--    audit_status: 0=待审 1=通过 2=驳回 (设计doc含 20/30 业务态,此处用基础态)
--    audit_route: AUTO_APPROVE / MANUAL_REVIEW ; completeness_score 0-100
--    merchant_id 用 system_users 中租户1的用户; product_id 模拟
-- =====================================================================
INSERT INTO wujin_merchant_relation_submission
  (id, merchant_id, product_id, product_name, product_lane, product_category_id, template_id, audit_status, audit_route, completeness_score, remark, creator, create_time, updater, update_time, tenant_id) VALUES
 (600,100,9001,'米其林 205/55R16 乘用车轮胎','PRODUCT',3, 500,1,'AUTO_APPROVE', 95,'模板必选项完整,自动通过',      @U,@NOW,@U,@NOW,@T),
 (601,103,9002,'8.8级 M12 外六角螺栓',       'PRODUCT',6, 501,1,'AUTO_APPROVE', 90,'材料+工艺齐全',              @U,@NOW,@U,@NOW,@T),
 (602,112,9003,'6204 深沟球轴承',            'PRODUCT',8, 502,0,'MANUAL_REVIEW',55,'缺少必选轴承钢绑定,转人工',  @U,@NOW,@U,@NOW,@T),
 (603,100,9004,'卡车全钢子午线轮胎',         'PRODUCT',2, 500,2,'MANUAL_REVIEW',40,'移除模板必选硫化工艺被驳回',@U,@NOW,@U,@NOW,@T);

-- =====================================================================
-- 8. 商家关系申报项 wujin_merchant_relation_item (from_template/required_flag)
-- =====================================================================
INSERT INTO wujin_merchant_relation_item
  (submission_id, entity_id, relation_type, from_template, required_flag, remark, creator, create_time, updater, update_time, tenant_id) VALUES
 (600,200,'REQUIRES_MATERIAL',b'1',b'1','模板必选:天然橡胶',@U,@NOW,@U,@NOW,@T),
 (600,300,'REQUIRES_PROCESS', b'1',b'1','模板必选:硫化',    @U,@NOW,@U,@NOW,@T),
 (600,203,'REQUIRES_MATERIAL',b'1',b'0','模板选填:炭黑',    @U,@NOW,@U,@NOW,@T),
 (601,201,'REQUIRES_MATERIAL',b'1',b'1','模板必选:碳钢',    @U,@NOW,@U,@NOW,@T),
 (601,301,'REQUIRES_PROCESS', b'1',b'1','模板必选:锻造',    @U,@NOW,@U,@NOW,@T),
 (601,303,'REQUIRES_PROCESS', b'0',b'0','商家自定义:镀锌',  @U,@NOW,@U,@NOW,@T),
 (602,202,'REQUIRES_MATERIAL',b'1',b'1','模板必选:轴承钢(缺绑)',@U,@NOW,@U,@NOW,@T),
 (603,200,'REQUIRES_MATERIAL',b'1',b'1','模板必选:天然橡胶',@U,@NOW,@U,@NOW,@T);

-- =====================================================================
-- 9. 关系审核记录 wujin_relation_audit_record
--    action: BLOCK/SUGGEST_MERGE/MANUAL_REVIEW/APPROVE/REJECT
--    effective_flag 是否生效入网
-- =====================================================================
INSERT INTO wujin_relation_audit_record
  (submission_id, auditor_id, action, reason, comment, effective_flag, creator, create_time, updater, update_time, tenant_id) VALUES
 (600,1,'APPROVE',      'TEMPLATE_COMPLETE','模板必选材料/工艺完整,自动通过入网',b'1',@U,@NOW,@U,@NOW,@T),
 (601,1,'APPROVE',      'TEMPLATE_COMPLETE','材料工艺齐全,通过',                 b'1',@U,@NOW,@U,@NOW,@T),
 (602,1,'MANUAL_REVIEW','MISSING_REQUIRED', '缺少必选轴承钢绑定,转人工审核',     b'0',@U,@NOW,@U,@NOW,@T),
 (603,1,'REJECT',       'REMOVE_REQUIRED',  '移除模板必选硫化工艺,驳回',         b'0',@U,@NOW,@U,@NOW,@T);

-- =====================================================================
-- 10. 商家供应能力索引 wujin_merchant_supply_capability
--     supply_status: 0=启用 1=停供 (审核通过申报生成,幂等键 merchant+product+entity)
-- =====================================================================
INSERT INTO wujin_merchant_supply_capability
  (merchant_id, product_id, product_name, entity_id, lane, industry, supply_status, stock_count, min_order_quantity, delivery_days, service_area, remark, creator, create_time, updater, update_time, tenant_id) VALUES
 (100,9001,'米其林 205/55R16 乘用车轮胎',100,'PRODUCT', '轮胎',  0,1200,4,  7,'华东,华南','成品供应',          @U,@NOW,@U,@NOW,@T),
 (100,9001,'天然橡胶供应',                200,'MATERIAL','轮胎',  0,50000,1000,15,'全国',    '原料配套供应',      @U,@NOW,@U,@NOW,@T),
 (103,9002,'8.8级 M12 外六角螺栓',        101,'PRODUCT', '紧固件',0,98000,5000,5,'华东',     '成品供应',          @U,@NOW,@U,@NOW,@T),
 (103,9002,'碳钢45#线材供应',             201,'MATERIAL','紧固件',0,30000,2000,10,'华东,华北','原料配套供应',      @U,@NOW,@U,@NOW,@T),
 (112,9003,'6204 深沟球轴承',             102,'PRODUCT', '轴承',  1,0,   100, 12,'华南',     '暂时停供,缺料',     @U,@NOW,@U,@NOW,@T),
 (113,9005,'GCr15 轴承钢供应',            202,'MATERIAL','轴承',  0,8000, 500, 20,'华东',     '上游钢厂直供',      @U,@NOW,@U,@NOW,@T);

COMMIT;

START TRANSACTION;

-- =====================================================================
-- 11. 一键寻源线索 wujin_sourcing_lead
--     lead_status: SUBMITTED/ASSIGNED/CONTACTED/QUOTED/CONVERTED/LOST
--     dispatch_status: PENDING/DISPATCHED
-- =====================================================================
INSERT INTO wujin_sourcing_lead
  (user_id, keyword, lane, source_keyword, industry, supplier_id, supplier_name, merchant_id, contact_name, contact_phone, requirement, lead_status, dispatch_status, dispatch_remark, handle_remark, first_contact_time, quoted_time, converted_time, lost_time, process_duration_minutes, creator, create_time, updater, update_time, tenant_id) VALUES
 (110,'205/55R16轮胎','PRODUCT', '轮胎',    '轮胎',  100, '米其林华东仓',100, '张工',  '13800001111','需采购乘用车轮胎2000条,要求3C认证','CONVERTED','DISPATCHED','已分发至成品供应商','已成交,签订季度框架', DATE_SUB(@NOW,INTERVAL 5 DAY), DATE_SUB(@NOW,INTERVAL 4 DAY), DATE_SUB(@NOW,INTERVAL 2 DAY), NULL, 4320, @U,@NOW,@U,@NOW,@T),
 (111,'M12螺栓',      'PRODUCT', '紧固件',  '紧固件',103, '紧固件优选商',103, '李采购','13800002222','M12 8.8级螺栓5万件,镀锌','QUOTED','DISPATCHED','已分发','已报价待客户确认', DATE_SUB(@NOW,INTERVAL 3 DAY), DATE_SUB(@NOW,INTERVAL 1 DAY), NULL, NULL, 2880, @U,@NOW,@U,@NOW,@T),
 (104,'天然橡胶',    'MATERIAL','轮胎',    '轮胎',  NULL,'橡胶大宗供应',NULL,'王经理','13800003333','天然橡胶1#烟片50吨','CONTACTED','DISPATCHED','原材料泳道分发','已电话联系', DATE_SUB(@NOW,INTERVAL 1 DAY), NULL, NULL, NULL, 1440, @U,@NOW,@U,@NOW,@T),
 (NULL,'轴承钢GCr15','MATERIAL','轴承',    '轴承',  NULL,NULL,         NULL,'匿名访客','13800004444','GCr15轴承钢询价','SUBMITTED','PENDING',NULL,NULL, NULL,NULL,NULL,NULL,NULL, @U,@NOW,@U,@NOW,@T),
 (112,'深沟球轴承',  'PRODUCT', '轴承',    '轴承',  112, '轴承直营店', 112, '赵工',  '13800005555','6204轴承1万套','LOST','DISPATCHED','已分发','客户选择其他供应商', DATE_SUB(@NOW,INTERVAL 6 DAY), NULL, NULL, DATE_SUB(@NOW,INTERVAL 3 DAY), 4320, @U,@NOW,@U,@NOW,@T),
 (110,'医用乳胶',    'MATERIAL','医疗',    '医疗',  NULL,NULL,         NULL,'孙医采','13800006666','医用乳胶手套原料,需医疗器械资质','ASSIGNED','DISPATCHED','高风险:跨行业不可互换提示',NULL, NULL,NULL,NULL,NULL,NULL, @U,@NOW,@U,@NOW,@T);

-- =====================================================================
-- 12. 搜索行为日志 wujin_search_behavior_log
--     intent: DIRECT_SEARCH / UPSTREAM_JUMP / CHAIN_TRACE
--     result_lane: PRODUCT/PROCESS/MATERIAL ; satisfaction_score 1-5
-- =====================================================================
INSERT INTO wujin_search_behavior_log
  (user_id, keyword, intent, result_lane, industry_code, chain_viewed, classification_correct, high_risk_warning_triggered, satisfaction_score, response_time_millis, creator, create_time, updater, update_time, tenant_id) VALUES
 (110,'轮胎',     'DIRECT_SEARCH','PRODUCT', 'TIRE',    b'1',b'1',b'0',5,120, @U,DATE_SUB(@NOW,INTERVAL 6 DAY),@U,@NOW,@T), -- AC-01
 (110,'轮胎',     'UPSTREAM_JUMP','MATERIAL','TIRE',    b'1',b'1',b'0',4,150, @U,DATE_SUB(@NOW,INTERVAL 6 DAY),@U,@NOW,@T), -- AC-02
 (111,'天然橡胶', 'DIRECT_SEARCH','MATERIAL','TIRE',    b'0',b'1',b'0',5,90,  @U,DATE_SUB(@NOW,INTERVAL 5 DAY),@U,@NOW,@T), -- AC-03
 (104,'医用乳胶手套','DIRECT_SEARCH','MATERIAL','MEDICAL',b'1',b'1',b'1',4,200,@U,DATE_SUB(@NOW,INTERVAL 4 DAY),@U,@NOW,@T), -- AC-05 风险
 (112,'M12螺栓',  'DIRECT_SEARCH','PRODUCT', 'FASTENER',b'1',b'1',b'0',5,110, @U,DATE_SUB(@NOW,INTERVAL 3 DAY),@U,@NOW,@T),
 (112,'螺栓热处理','CHAIN_TRACE',  'PROCESS', 'FASTENER',b'1',b'1',b'0',4,180, @U,DATE_SUB(@NOW,INTERVAL 3 DAY),@U,@NOW,@T),
 (113,'轴承',     'DIRECT_SEARCH','PRODUCT', 'BEARING', b'0',b'0',b'0',2,260, @U,DATE_SUB(@NOW,INTERVAL 2 DAY),@U,@NOW,@T), -- 分类不准
 (113,'轴承钢',   'UPSTREAM_JUMP','MATERIAL','BEARING', b'1',b'1',b'0',4,140, @U,DATE_SUB(@NOW,INTERVAL 1 DAY),@U,@NOW,@T),
 (110,'镀锌螺栓', 'DIRECT_SEARCH','PRODUCT', 'FASTENER',b'0',b'1',b'0',3,130, @U,DATE_SUB(@NOW,INTERVAL 1 DAY),@U,@NOW,@T),
 (111,'橡胶硫化', 'CHAIN_TRACE',  'PROCESS', 'TIRE',    b'1',b'1',b'0',5,170, @U,@NOW,@U,@NOW,@T);

-- =====================================================================
-- 13. 搜索规则配置 wujin_search_rule_config
--     rule_type: GRANULARITY_LIMIT/INTENT_DICT/ENTITY_ALIAS/WEIGHT
-- =====================================================================
INSERT INTO wujin_search_rule_config
  (rule_type, lane, industry_code, rule_value, weight, status, remark, creator, create_time, updater, update_time, tenant_id) VALUES
 ('GRANULARITY_LIMIT','PRODUCT', NULL,      '3',                            NULL,0,'成品默认展示到规格级',      @U,@NOW,@U,@NOW,@T),
 ('GRANULARITY_LIMIT','MATERIAL',NULL,      '1',                            NULL,0,'原材料默认展示到大类',      @U,@NOW,@U,@NOW,@T),
 ('INTENT_DICT',      NULL,      'TIRE',    '轮胎,胎,tire=>PRODUCT',         NULL,0,'轮胎意图词典',              @U,@NOW,@U,@NOW,@T),
 ('INTENT_DICT',      NULL,      'FASTENER','螺栓,螺丝,螺钉=>PRODUCT',       NULL,0,'紧固件意图词典',            @U,@NOW,@U,@NOW,@T),
 ('ENTITY_ALIAS',     'MATERIAL','TIRE',    '天然橡胶=NR,生胶',              NULL,0,'天然橡胶别名',              @U,@NOW,@U,@NOW,@T),
 ('ENTITY_ALIAS',     'MATERIAL','BEARING', '轴承钢=GCr15,高碳铬轴承钢',     NULL,0,'轴承钢别名',                @U,@NOW,@U,@NOW,@T),
 ('WEIGHT',           'PRODUCT', NULL,      'STOCK_BOOST',                  30,  0,'有库存供应能力加权',        @U,@NOW,@U,@NOW,@T),
 ('WEIGHT',           NULL,      NULL,      'JUNCTION_BOOST',               20,  0,'交汇点实体兜底加权',        @U,@NOW,@U,@NOW,@T),
 ('GRANULARITY_LIMIT','MATERIAL','MEDICAL', '1;when=industry == 医疗器械',   NULL,1,'医疗乳胶高风险提示(停用态示例)',@U,@NOW,@U,@NOW,@T);

-- =====================================================================
-- 14. 监控指标快照 wujin_monitor_snapshot
--     metric: CHAIN_VIEW_RATE/CLASSIFY_ACCURACY/LEAD_CONVERT_RATE/AUTO_APPROVE_RATE...
--     alert_flag: 制造链查看率<30% 等触发告警
-- =====================================================================
INSERT INTO wujin_monitor_snapshot
  (metric, metric_value, threshold_value, audit_action, alert_flag, remark, creator, create_time, updater, update_time, tenant_id) VALUES
 ('CHAIN_VIEW_RATE',   70,30,NULL,        b'0','制造链查看率70%,高于阈值',      @U,DATE_SUB(@NOW,INTERVAL 2 DAY),@U,@NOW,@T),
 ('CHAIN_VIEW_RATE',   25,30,'ALERT',     b'1','制造链查看率25%,低于30%告警',   @U,DATE_SUB(@NOW,INTERVAL 1 DAY),@U,@NOW,@T),
 ('CLASSIFY_ACCURACY', 90,80,NULL,        b'0','分类准确率90%',                 @U,@NOW,@U,@NOW,@T),
 ('CLASSIFY_ACCURACY', 75,80,'ALERT',     b'1','分类准确率75%,低于阈值告警',     @U,DATE_SUB(@NOW,INTERVAL 1 DAY),@U,@NOW,@T),
 ('LEAD_CONVERT_RATE', 35,20,NULL,        b'0','寻源转化率35%',                 @U,@NOW,@U,@NOW,@T),
 ('AUTO_APPROVE_RATE', 50,40,NULL,        b'0','自动通过率50%',                 @U,@NOW,@U,@NOW,@T),
 ('PENDING_AUDIT_CNT', 2, 10,NULL,        b'0','待审申报2条',                   @U,@NOW,@U,@NOW,@T),
 ('HIGH_RISK_HIT_CNT', 1, 5, NULL,        b'0','高风险命中1次',                 @U,@NOW,@U,@NOW,@T);

COMMIT;

-- =====================================================================
-- 校验汇总
-- =====================================================================
SELECT 'wujin_category' t, COUNT(*) c FROM wujin_category WHERE tenant_id=@T
UNION ALL SELECT 'wujin_category_mapping',           COUNT(*) FROM wujin_category_mapping WHERE tenant_id=@T
UNION ALL SELECT 'wujin_chain_entity',               COUNT(*) FROM wujin_chain_entity WHERE tenant_id=@T
UNION ALL SELECT 'wujin_chain_entity_relation',      COUNT(*) FROM wujin_chain_entity_relation WHERE tenant_id=@T
UNION ALL SELECT 'wujin_industry_template',          COUNT(*) FROM wujin_industry_template WHERE tenant_id=@T
UNION ALL SELECT 'wujin_industry_template_item',     COUNT(*) FROM wujin_industry_template_item WHERE tenant_id=@T
UNION ALL SELECT 'wujin_merchant_relation_submission',COUNT(*) FROM wujin_merchant_relation_submission WHERE tenant_id=@T
UNION ALL SELECT 'wujin_merchant_relation_item',     COUNT(*) FROM wujin_merchant_relation_item WHERE tenant_id=@T
UNION ALL SELECT 'wujin_relation_audit_record',      COUNT(*) FROM wujin_relation_audit_record WHERE tenant_id=@T
UNION ALL SELECT 'wujin_merchant_supply_capability', COUNT(*) FROM wujin_merchant_supply_capability WHERE tenant_id=@T
UNION ALL SELECT 'wujin_sourcing_lead',              COUNT(*) FROM wujin_sourcing_lead WHERE tenant_id=@T
UNION ALL SELECT 'wujin_search_behavior_log',        COUNT(*) FROM wujin_search_behavior_log WHERE tenant_id=@T
UNION ALL SELECT 'wujin_search_rule_config',         COUNT(*) FROM wujin_search_rule_config WHERE tenant_id=@T
UNION ALL SELECT 'wujin_monitor_snapshot',           COUNT(*) FROM wujin_monitor_snapshot WHERE tenant_id=@T;
