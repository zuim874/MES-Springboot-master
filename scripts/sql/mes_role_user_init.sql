-- ============================================================================
-- MES 系统角色与用户初始化脚本
-- 适用场景：唯实电子科技公司生产车间
-- 说明：本脚本用于创建角色、分配菜单权限、创建用户及分配角色
-- ============================================================================

-- ============================================================================
-- 第一部分：创建角色 (sp_sys_role)
-- ============================================================================
-- 角色名称与角色编码具有唯一性（数据库有 UNIQUE INDEX）
INSERT INTO `sp_sys_role` (`id`, `name`, `code`, `descr`, `is_deleted`, `create_time`, `create_username`, `update_time`, `update_username`)
VALUES
('role_data_clerk',      '数据员',       'data_clerk',       '该角色享有基础数据中心权限，负责基础数据维护与管理',  '0', NOW(), 'admin', NOW(), 'admin'),
('role_process_eng',     '工艺员',       'process_eng',      '负责工艺路线和BOM管理',                                  '0', NOW(), 'admin', NOW(), 'admin'),
('role_prod_planner',    '生产计划员',   'prod_planner',     '负责生产计划与工单下达',                                '0', NOW(), 'admin', NOW(), 'admin'),
('role_prod_supervisor', '生产主管',     'prod_supervisor',  '负责生产全面管理，包括计划、物料、工艺、在制品',        '0', NOW(), 'admin', NOW(), 'admin'),
('role_prod_operator',   '生产作业员',   'prod_operator',    '负责生产作业执行与在制品跟踪',                          '0', NOW(), 'admin', NOW(), 'admin'),
('role_warehouse_mgr',   '库房管理员',   'warehouse_mgr',    '负责物料管理与库存维护',                                '0', NOW(), 'admin', NOW(), 'admin'),
('role_quality_mgr',     '质量管理员',   'quality_mgr',      '负责质量检验与质量管理',                                '0', NOW(), 'admin', NOW(), 'admin');

-- ============================================================================
-- 第二部分：角色-菜单权限分配 (sp_sys_role_menu)
-- ============================================================================
-- 注意：需要同时分配父级菜单（导航菜单），以保证菜单树正常渲染

-- 1. 数据员 - 基础数据中心权限
--    菜单路径：常规管理(1) > 系统管理(10) > 基础数据配置平台(105)、基础数据维护(106)
INSERT INTO `sp_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_username`, `update_time`, `update_username`)
VALUES
('rm_dc_01', 'role_data_clerk', '1',   NOW(), 'admin', NOW(), 'admin'),
('rm_dc_02', 'role_data_clerk', '10',  NOW(), 'admin', NOW(), 'admin'),
('rm_dc_03', 'role_data_clerk', '105', NOW(), 'admin', NOW(), 'admin'),
('rm_dc_04', 'role_data_clerk', '106', NOW(), 'admin', NOW(), 'admin');

-- 2. 工艺员 - 工艺管理权限
--    菜单路径：常规管理(1) > 工艺管理(15) > 工艺路线管理(151)、工艺BOM管理(152)
INSERT INTO `sp_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_username`, `update_time`, `update_username`)
VALUES
('rm_pe_01', 'role_process_eng', '1',   NOW(), 'admin', NOW(), 'admin'),
('rm_pe_02', 'role_process_eng', '15',  NOW(), 'admin', NOW(), 'admin'),
('rm_pe_03', 'role_process_eng', '151', NOW(), 'admin', NOW(), 'admin'),
('rm_pe_04', 'role_process_eng', '152', NOW(), 'admin', NOW(), 'admin');

-- 3. 生产计划员 - 计划管理权限
--    菜单路径：常规管理(1) > 计划管理(12) > 工单下达(121)
INSERT INTO `sp_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_username`, `update_time`, `update_username`)
VALUES
('rm_pp_01', 'role_prod_planner', '1',   NOW(), 'admin', NOW(), 'admin'),
('rm_pp_02', 'role_prod_planner', '12',  NOW(), 'admin', NOW(), 'admin'),
('rm_pp_03', 'role_prod_planner', '121', NOW(), 'admin', NOW(), 'admin');

-- 4. 生产主管 - 生产全面管理
--    菜单路径：计划管理(12)>工单下达(121)、物料管理(13)>物料维护(131)、
--             工艺管理(15)>工艺路线管理(151)>工艺BOM管理(152)、在制品管理(16)>SN通用过程采集(161)
INSERT INTO `sp_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_username`, `update_time`, `update_username`)
VALUES
('rm_ps_01',  'role_prod_supervisor', '1',   NOW(), 'admin', NOW(), 'admin'),
('rm_ps_02',  'role_prod_supervisor', '12',  NOW(), 'admin', NOW(), 'admin'),
('rm_ps_03',  'role_prod_supervisor', '121', NOW(), 'admin', NOW(), 'admin'),
('rm_ps_04',  'role_prod_supervisor', '13',  NOW(), 'admin', NOW(), 'admin'),
('rm_ps_05',  'role_prod_supervisor', '131', NOW(), 'admin', NOW(), 'admin'),
('rm_ps_06',  'role_prod_supervisor', '15',  NOW(), 'admin', NOW(), 'admin'),
('rm_ps_07',  'role_prod_supervisor', '151', NOW(), 'admin', NOW(), 'admin'),
('rm_ps_08',  'role_prod_supervisor', '152', NOW(), 'admin', NOW(), 'admin'),
('rm_ps_09',  'role_prod_supervisor', '16',  NOW(), 'admin', NOW(), 'admin'),
('rm_ps_10',  'role_prod_supervisor', '161', NOW(), 'admin', NOW(), 'admin');

-- 5. 生产作业员 - 生产作业执行
--    菜单路径：常规管理(1) > 计划管理(12)>工单下达(121)、在制品管理(16)>SN通用过程采集(161)
INSERT INTO `sp_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_username`, `update_time`, `update_username`)
VALUES
('rm_po_01', 'role_prod_operator', '1',   NOW(), 'admin', NOW(), 'admin'),
('rm_po_02', 'role_prod_operator', '12',  NOW(), 'admin', NOW(), 'admin'),
('rm_po_03', 'role_prod_operator', '121', NOW(), 'admin', NOW(), 'admin'),
('rm_po_04', 'role_prod_operator', '16',  NOW(), 'admin', NOW(), 'admin'),
('rm_po_05', 'role_prod_operator', '161', NOW(), 'admin', NOW(), 'admin');

-- 6. 库房管理员 - 物料/库存管理
--    菜单路径：常规管理(1) > 物料管理(13) > 物料维护(131)
INSERT INTO `sp_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_username`, `update_time`, `update_username`)
VALUES
('rm_wm_01', 'role_warehouse_mgr', '1',   NOW(), 'admin', NOW(), 'admin'),
('rm_wm_02', 'role_warehouse_mgr', '13',  NOW(), 'admin', NOW(), 'admin'),
('rm_wm_03', 'role_warehouse_mgr', '131', NOW(), 'admin', NOW(), 'admin');

-- 7. 质量管理员 - 质量管理（基础数据查看与维护权限）
--    菜单路径：常规管理(1) > 系统管理(10) > 基础数据配置平台(105)、基础数据维护(106)
INSERT INTO `sp_sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `create_username`, `update_time`, `update_username`)
VALUES
('rm_qm_01', 'role_quality_mgr', '1',   NOW(), 'admin', NOW(), 'admin'),
('rm_qm_02', 'role_quality_mgr', '10',  NOW(), 'admin', NOW(), 'admin'),
('rm_qm_03', 'role_quality_mgr', '105', NOW(), 'admin', NOW(), 'admin'),
('rm_qm_04', 'role_quality_mgr', '106', NOW(), 'admin', NOW(), 'admin');

-- ============================================================================
-- 第三部分：创建用户 (sp_sys_user)
-- ============================================================================
-- 密码使用 MD5 加密，默认密码为 123456
-- 注意：用户名（username）和手机号（mobile）有唯一索引，不能重复
INSERT INTO `sp_sys_user` (`id`, `name`, `username`, `password`, `dept_id`, `email`, `mobile`, `tel`, `sex`, `descr`, `is_deleted`, `create_time`, `create_username`, `update_time`, `update_username`)
VALUES
('user_101', '工艺员1', 'user101',  'e10adc3949ba59abbe56e057f20f883e', 'dept_process', '', '13800000101', '', '1', '工艺部-工艺员', '0', NOW(), 'admin', NOW(), 'admin'),
('user_201', '计划员1', 'user201',  'e10adc3949ba59abbe56e057f20f883e', 'dept_plan',   '', '13800000201', '', '1', '计划部-计划员', '0', NOW(), 'admin', NOW(), 'admin'),
('user_301', '生产主管1', 'user301', 'e10adc3949ba59abbe56e057f20f883e', 'dept_prod',   '', '13800000301', '', '1', '生产部-生产主管', '0', NOW(), 'admin', NOW(), 'admin'),
('user_302', '作业员1', 'user302',  'e10adc3949ba59abbe56e057f20f883e', 'dept_prod',   '', '13800000302', '', '1', '生产部-生产作业员', '0', NOW(), 'admin', NOW(), 'admin'),
('user_401', '库管员1', 'user401',  'e10adc3949ba59abbe56e057f20f883e', 'dept_purchase', '', '13800000401', '', '1', '采购部-库房管理员', '0', NOW(), 'admin', NOW(), 'admin'),
('user_501', '检验员1', 'user501',  'e10adc3949ba59abbe56e057f20f883e', 'dept_quality', '', '13800000501', '', '1', '质量部-质量管理员', '0', NOW(), 'admin', NOW(), 'admin');

-- ============================================================================
-- 第四部分：用户-角色关联 (sp_sys_user_role)
-- ============================================================================
INSERT INTO `sp_sys_user_role` (`id`, `user_id`, `role_id`, `create_time`, `create_username`, `update_time`, `update_username`)
VALUES
-- 工艺部-工艺员1：数据员 + 工艺员
('ur_101_1', 'user_101', 'role_data_clerk',   NOW(), 'admin', NOW(), 'admin'),
('ur_101_2', 'user_101', 'role_process_eng',  NOW(), 'admin', NOW(), 'admin'),
-- 计划部-计划员1：生产计划员
('ur_201_1', 'user_201', 'role_prod_planner', NOW(), 'admin', NOW(), 'admin'),
-- 生产部-生产主管1：生产主管
('ur_301_1', 'user_301', 'role_prod_supervisor', NOW(), 'admin', NOW(), 'admin'),
-- 生产部-作业员1：生产作业员
('ur_302_1', 'user_302', 'role_prod_operator', NOW(), 'admin', NOW(), 'admin'),
-- 采购部-库管员1：库房管理员
('ur_401_1', 'user_401', 'role_warehouse_mgr', NOW(), 'admin', NOW(), 'admin'),
-- 质量部-检验员1：质量管理员
('ur_501_1', 'user_501', 'role_quality_mgr',   NOW(), 'admin', NOW(), 'admin');
