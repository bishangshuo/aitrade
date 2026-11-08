-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('回测账户快照', '1137', '1', 'tfaccountdaily', 'stock/tfaccountdaily/index', 1, 0, 'C', '0', '0', 'stock:tfaccountdaily:list', '#', 'admin', sysdate(), '', null, '回测账户快照菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('回测账户快照查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'stock:tfaccountdaily:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('回测账户快照新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'stock:tfaccountdaily:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('回测账户快照修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'stock:tfaccountdaily:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('回测账户快照删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'stock:tfaccountdaily:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('回测账户快照导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'stock:tfaccountdaily:export',       '#', 'admin', sysdate(), '', null, '');