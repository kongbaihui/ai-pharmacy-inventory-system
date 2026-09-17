-- ----------------------------------------------------------------------------
-- 药品进销存模块菜单与按钮权限
-- 说明：菜单ID使用“当前最大菜单ID + 1”的方式自动生成，避免与现有菜单冲突
-- ----------------------------------------------------------------------------

-- ---------------- 一级目录：药品进销存管理 ----------------
SET @medRootId = (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu);
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values(@medRootId, '药品进销存管理', 0, 5, 'med', null, '', '', 1, 0, 'M', '0', '0', '', 'shopping', 'admin', sysdate(), '', null, '药品进销存业务菜单');

-- ---------------- 二级目录：基础资料 ----------------
SET @baseId = (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu);
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values(@baseId, '基础资料', @medRootId, 1, 'base', null, '', '', 1, 0, 'M', '0', '0', '', 'documentation', 'admin', sysdate(), '', null, '药品与供应商等基础数据');

-- 药品分类菜单
SET @menuId = (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu);
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values(@menuId, '药品分类', @baseId, 1, 'category', 'system/category/index', '', '', 1, 0, 'C', '0', '0', 'system:category:list', 'tree', 'admin', sysdate(), '', null, '药品分类菜单');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('药品分类查询', @menuId, 1, '#', '', 1, 0, 'F', '0', '0', 'system:category:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('药品分类新增', @menuId, 2, '#', '', 1, 0, 'F', '0', '0', 'system:category:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('药品分类修改', @menuId, 3, '#', '', 1, 0, 'F', '0', '0', 'system:category:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('药品分类删除', @menuId, 4, '#', '', 1, 0, 'F', '0', '0', 'system:category:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('药品分类导出', @menuId, 5, '#', '', 1, 0, 'F', '0', '0', 'system:category:export', '#', 'admin', sysdate(), '', null, '');

-- 药品信息菜单
SET @menuId = (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu);
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values(@menuId, '药品信息', @baseId, 2, 'info', 'system/info/index', '', '', 1, 0, 'C', '0', '0', 'system:info:list', 'medicine-box', 'admin', sysdate(), '', null, '药品信息菜单');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('药品信息查询', @menuId, 1, '#', '', 1, 0, 'F', '0', '0', 'system:info:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('药品信息新增', @menuId, 2, '#', '', 1, 0, 'F', '0', '0', 'system:info:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('药品信息修改', @menuId, 3, '#', '', 1, 0, 'F', '0', '0', 'system:info:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('药品信息删除', @menuId, 4, '#', '', 1, 0, 'F', '0', '0', 'system:info:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('药品信息导出', @menuId, 5, '#', '', 1, 0, 'F', '0', '0', 'system:info:export', '#', 'admin', sysdate(), '', null, '');

-- 供应商信息菜单
SET @menuId = (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu);
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values(@menuId, '供应商信息', @baseId, 3, 'supplier', 'system/supplier/index', '', '', 1, 0, 'C', '0', '0', 'system:supplier:list', 'peoples', 'admin', sysdate(), '', null, '供应商信息菜单');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('供应商查询', @menuId, 1, '#', '', 1, 0, 'F', '0', '0', 'system:supplier:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('供应商新增', @menuId, 2, '#', '', 1, 0, 'F', '0', '0', 'system:supplier:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('供应商修改', @menuId, 3, '#', '', 1, 0, 'F', '0', '0', 'system:supplier:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('供应商删除', @menuId, 4, '#', '', 1, 0, 'F', '0', '0', 'system:supplier:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('供应商导出', @menuId, 5, '#', '', 1, 0, 'F', '0', '0', 'system:supplier:export', '#', 'admin', sysdate(), '', null, '');

-- ---------------- 二级目录：库存管理 ----------------
SET @stockId = (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu);
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values(@stockId, '库存管理', @medRootId, 2, 'stock', null, '', '', 1, 0, 'M', '0', '0', '', 'shopping', 'admin', sysdate(), '', null, '库存盘点、预警与有效期管理');

-- 库存盘点菜单
SET @menuId = (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu);
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values(@menuId, '库存盘点', @stockId, 1, 'check', 'system/check/index', '', '', 1, 0, 'C', '0', '0', 'system:check:list', 'form', 'admin', sysdate(), '', null, '库存盘点菜单');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('盘点查询', @menuId, 1, '#', '', 1, 0, 'F', '0', '0', 'system:check:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('盘点新增', @menuId, 2, '#', '', 1, 0, 'F', '0', '0', 'system:check:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('盘点修改', @menuId, 3, '#', '', 1, 0, 'F', '0', '0', 'system:check:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('盘点删除', @menuId, 4, '#', '', 1, 0, 'F', '0', '0', 'system:check:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('盘点导出', @menuId, 5, '#', '', 1, 0, 'F', '0', '0', 'system:check:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('盘点审核', @menuId, 6, '#', '', 1, 0, 'F', '0', '0', 'system:check:audit',  '#', 'admin', sysdate(), '', null, '审核后按盈亏数量调整库存');

-- 库存预警菜单
SET @menuId = (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu);
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values(@menuId, '库存预警', @stockId, 2, 'warn', 'system/warn/index', '', '', 1, 0, 'C', '0', '0', 'system:warn:list', 'message', 'admin', sysdate(), '', null, '库存预警菜单');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('预警查询', @menuId, 1, '#', '', 1, 0, 'F', '0', '0', 'system:warn:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('预警新增', @menuId, 2, '#', '', 1, 0, 'F', '0', '0', 'system:warn:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('预警修改', @menuId, 3, '#', '', 1, 0, 'F', '0', '0', 'system:warn:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('预警删除', @menuId, 4, '#', '', 1, 0, 'F', '0', '0', 'system:warn:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('预警导出', @menuId, 5, '#', '', 1, 0, 'F', '0', '0', 'system:warn:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('预警扫描', @menuId, 6, '#', '', 1, 0, 'F', '0', '0', 'system:warn:scan',   '#', 'admin', sysdate(), '', null, '按库存上下限与有效期扫描生成预警');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('预警处理', @menuId, 7, '#', '', 1, 0, 'F', '0', '0', 'system:warn:handle', '#', 'admin', sysdate(), '', null, '处理或忽略预警');

-- 药品有效期（批次）菜单
SET @menuId = (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu);
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values(@menuId, '药品有效期', @stockId, 3, 'batch', 'system/batch/index', '', '', 1, 0, 'C', '0', '0', 'system:batch:list', 'time-range', 'admin', sysdate(), '', null, '药品批次有效期与临期查询菜单');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('批次查询', @menuId, 1, '#', '', 1, 0, 'F', '0', '0', 'system:batch:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('批次导出', @menuId, 2, '#', '', 1, 0, 'F', '0', '0', 'system:batch:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('批次新增', @menuId, 3, '#', '', 1, 0, 'F', '0', '0', 'system:batch:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('批次修改', @menuId, 4, '#', '', 1, 0, 'F', '0', '0', 'system:batch:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('批次删除', @menuId, 5, '#', '', 1, 0, 'F', '0', '0', 'system:batch:remove', '#', 'admin', sysdate(), '', null, '');

-- 过期药品清理菜单
SET @menuId = (SELECT IFNULL(MAX(menu_id), 0) + 1 FROM sys_menu);
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values(@menuId, '过期药品清理', @stockId, 4, 'expiredclean', 'system/expiredclean/index', '', '', 1, 0, 'C', '0', '0', 'system:expiredclean:list', 'delete', 'admin', sysdate(), '', null, '过期药品清理菜单');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('清理查询', @menuId, 1, '#', '', 1, 0, 'F', '0', '0', 'system:expiredclean:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('清理新增', @menuId, 2, '#', '', 1, 0, 'F', '0', '0', 'system:expiredclean:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('清理修改', @menuId, 3, '#', '', 1, 0, 'F', '0', '0', 'system:expiredclean:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('清理删除', @menuId, 4, '#', '', 1, 0, 'F', '0', '0', 'system:expiredclean:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('清理导出', @menuId, 5, '#', '', 1, 0, 'F', '0', '0', 'system:expiredclean:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) values('清理确认', @menuId, 6, '#', '', 1, 0, 'F', '0', '0', 'system:expiredclean:confirm', '#', 'admin', sysdate(), '', null, '确认清理并按数量扣减库存');
