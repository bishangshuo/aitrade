-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('回测持仓日历', '1068', '1', 'tfpositiondaily', 'stock/tfpositiondaily/index', 1, 0, 'C', '0', '0', 'stock:tfpositiondaily:list', '#', 'admin', sysdate(), '', null, '回测持仓日历菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('回测持仓日历查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'stock:tfpositiondaily:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('回测持仓日历新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'stock:tfpositiondaily:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('回测持仓日历修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'stock:tfpositiondaily:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('回测持仓日历删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'stock:tfpositiondaily:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('回测持仓日历导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'stock:tfpositiondaily:export',       '#', 'admin', sysdate(), '', null, '');