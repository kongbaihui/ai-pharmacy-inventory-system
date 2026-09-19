-- ----------------------------------------------------------------------------
-- AI 医院药品进销存信息管理系统 —— Web 菜单整合
-- 前提：已执行 ry_20260417.sql、01_med_schema.sql、03_stock_business.sql、02_med_menu.sql
-- 说明：可重复执行；隐藏若依演示菜单，接入真实库存业务页面。
-- ----------------------------------------------------------------------------
SET NAMES utf8mb4;

-- 隐藏与本项目日常业务无关的若依演示入口，系统管理保留给管理员。
UPDATE sys_menu SET visible = '1'
WHERE menu_name IN ('系统监控', '系统工具');

-- 清理上一版错误挂载的旧页面及其按钮权限（这些页面调用的后端接口不存在）。
DELETE FROM sys_menu
WHERE parent_id IN (
    SELECT menu_id FROM (
        SELECT menu_id FROM sys_menu
        WHERE component IN ('system/stock/index', 'system/flow/index', 'system/clean/index', 'system/AIChat/index')
    ) old_pages
);
DELETE FROM sys_menu
WHERE component IN ('system/stock/index', 'system/flow/index', 'system/clean/index', 'system/AIChat/index');

SET @medRootId = (
    SELECT menu_id FROM sys_menu
    WHERE menu_name = '药品进销存管理' AND menu_type = 'M'
    ORDER BY menu_id LIMIT 1
);

-- 将黄浩负责的盘点、预警、效期、过期管理统一归入“库存监管”。
UPDATE sys_menu
SET menu_name = '库存监管', order_num = 3, remark = '库存盘点、预警、有效期及过期药品管理'
WHERE parent_id = @medRootId AND path = 'stock' AND menu_type = 'M';

UPDATE sys_menu SET icon = 'clipboard'
WHERE parent_id IN (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE parent_id = @medRootId AND path = 'base') base_menu)
  AND component = 'system/info/index';

-- 胡振鹏负责的入库、出库、退库及库存流水目录。
INSERT INTO sys_menu
    (menu_name, parent_id, order_num, path, component, query, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon,
     create_by, create_time, update_by, update_time, remark)
SELECT '库存业务', @medRootId, 2, 'business', NULL, '', 'StockBusiness',
       1, 0, 'M', '0', '0', '', 'shopping',
       'admin', SYSDATE(), '', NULL, '入库、出库、退库及库存流水'
WHERE @medRootId IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu
      WHERE parent_id = @medRootId AND path = 'business' AND menu_type = 'M'
  );

SET @businessId = (
    SELECT menu_id FROM sys_menu
    WHERE parent_id = @medRootId AND path = 'business' AND menu_type = 'M'
    ORDER BY menu_id LIMIT 1
);

-- 入库管理
INSERT INTO sys_menu
    (menu_name, parent_id, order_num, path, component, query, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon,
     create_by, create_time, update_by, update_time, remark)
SELECT '入库管理', @businessId, 1, 'inbound', 'system/stockOrder/index', '{"orderType":"1"}', 'InboundOrder',
       1, 0, 'C', '0', '0', 'system:stockOrder:list', 'download',
       'admin', SYSDATE(), '', NULL, '药品入库单管理'
WHERE @businessId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @businessId AND path = 'inbound');

SET @inboundId = (SELECT menu_id FROM sys_menu WHERE parent_id = @businessId AND path = 'inbound' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu (menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark)
SELECT '入库单查询',@inboundId,1,'#','',1,0,'F','0','0','system:stockOrder:query','#','admin',SYSDATE(),'',NULL,'' WHERE @inboundId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@inboundId AND perms='system:stockOrder:query');
INSERT INTO sys_menu (menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark)
SELECT '入库单新增',@inboundId,2,'#','',1,0,'F','0','0','system:stockOrder:add','#','admin',SYSDATE(),'',NULL,'' WHERE @inboundId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@inboundId AND perms='system:stockOrder:add');
INSERT INTO sys_menu (menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark)
SELECT '入库单修改',@inboundId,3,'#','',1,0,'F','0','0','system:stockOrder:edit','#','admin',SYSDATE(),'',NULL,'' WHERE @inboundId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@inboundId AND perms='system:stockOrder:edit');
INSERT INTO sys_menu (menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark)
SELECT '入库单确认',@inboundId,4,'#','',1,0,'F','0','0','system:stockOrder:confirm','#','admin',SYSDATE(),'',NULL,'' WHERE @inboundId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@inboundId AND perms='system:stockOrder:confirm');
INSERT INTO sys_menu (menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark)
SELECT '入库单删除',@inboundId,5,'#','',1,0,'F','0','0','system:stockOrder:remove','#','admin',SYSDATE(),'',NULL,'' WHERE @inboundId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@inboundId AND perms='system:stockOrder:remove');

-- 出库管理
INSERT INTO sys_menu
    (menu_name, parent_id, order_num, path, component, query, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon,
     create_by, create_time, update_by, update_time, remark)
SELECT '出库管理', @businessId, 2, 'outbound', 'system/stockOrder/index', '{"orderType":"2"}', 'OutboundOrder',
       1, 0, 'C', '0', '0', 'system:stockOrder:list', 'upload',
       'admin', SYSDATE(), '', NULL, '药品出库单管理'
WHERE @businessId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @businessId AND path = 'outbound');

SET @outboundId = (SELECT menu_id FROM sys_menu WHERE parent_id = @businessId AND path = 'outbound' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu (menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark)
SELECT '出库单查询',@outboundId,1,'#','',1,0,'F','0','0','system:stockOrder:query','#','admin',SYSDATE(),'',NULL,'' WHERE @outboundId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@outboundId AND perms='system:stockOrder:query');
INSERT INTO sys_menu (menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark)
SELECT '出库单新增',@outboundId,2,'#','',1,0,'F','0','0','system:stockOrder:add','#','admin',SYSDATE(),'',NULL,'' WHERE @outboundId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@outboundId AND perms='system:stockOrder:add');
INSERT INTO sys_menu (menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark)
SELECT '出库单修改',@outboundId,3,'#','',1,0,'F','0','0','system:stockOrder:edit','#','admin',SYSDATE(),'',NULL,'' WHERE @outboundId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@outboundId AND perms='system:stockOrder:edit');
INSERT INTO sys_menu (menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark)
SELECT '出库单确认',@outboundId,4,'#','',1,0,'F','0','0','system:stockOrder:confirm','#','admin',SYSDATE(),'',NULL,'' WHERE @outboundId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@outboundId AND perms='system:stockOrder:confirm');
INSERT INTO sys_menu (menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark)
SELECT '出库单删除',@outboundId,5,'#','',1,0,'F','0','0','system:stockOrder:remove','#','admin',SYSDATE(),'',NULL,'' WHERE @outboundId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@outboundId AND perms='system:stockOrder:remove');

-- 退库管理
INSERT INTO sys_menu
    (menu_name, parent_id, order_num, path, component, query, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon,
     create_by, create_time, update_by, update_time, remark)
SELECT '退库管理', @businessId, 3, 'return', 'system/stockOrder/index', '{"orderType":"3"}', 'ReturnOrder',
       1, 0, 'C', '0', '0', 'system:stockOrder:list', 'enter',
       'admin', SYSDATE(), '', NULL, '科室药品退库单管理'
WHERE @businessId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @businessId AND path = 'return');

SET @returnId = (SELECT menu_id FROM sys_menu WHERE parent_id = @businessId AND path = 'return' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu (menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark)
SELECT '退库单查询',@returnId,1,'#','',1,0,'F','0','0','system:stockOrder:query','#','admin',SYSDATE(),'',NULL,'' WHERE @returnId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@returnId AND perms='system:stockOrder:query');
INSERT INTO sys_menu (menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark)
SELECT '退库单新增',@returnId,2,'#','',1,0,'F','0','0','system:stockOrder:add','#','admin',SYSDATE(),'',NULL,'' WHERE @returnId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@returnId AND perms='system:stockOrder:add');
INSERT INTO sys_menu (menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark)
SELECT '退库单修改',@returnId,3,'#','',1,0,'F','0','0','system:stockOrder:edit','#','admin',SYSDATE(),'',NULL,'' WHERE @returnId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@returnId AND perms='system:stockOrder:edit');
INSERT INTO sys_menu (menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark)
SELECT '退库单确认',@returnId,4,'#','',1,0,'F','0','0','system:stockOrder:confirm','#','admin',SYSDATE(),'',NULL,'' WHERE @returnId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@returnId AND perms='system:stockOrder:confirm');
INSERT INTO sys_menu (menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark)
SELECT '退库单删除',@returnId,5,'#','',1,0,'F','0','0','system:stockOrder:remove','#','admin',SYSDATE(),'',NULL,'' WHERE @returnId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@returnId AND perms='system:stockOrder:remove');

-- 库存流水
INSERT INTO sys_menu
    (menu_name, parent_id, order_num, path, component, query, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon,
     create_by, create_time, update_by, update_time, remark)
SELECT '库存流水', @businessId, 4, 'flow', 'system/stockOrder/index', '{"mode":"flow"}', 'StockFlow',
       1, 0, 'C', '0', '0', 'system:stockOrder:flow', 'list',
       'admin', SYSDATE(), '', NULL, '库存变更流水查询'
WHERE @businessId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @businessId AND path = 'flow');
