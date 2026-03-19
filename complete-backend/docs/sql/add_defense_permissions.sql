-- =============================================
-- 开题答辩管理模块权限配置
-- 创建时间: 2026-03-17
-- 权限ID范围: 940-959
-- 角色ID参照 add_document_permissions.sql
-- =============================================

-- 插入权限数据
INSERT INTO `permission_info` (`permission_id`, `permission_name`, `permission_code`, `permission_type`, `parent_id`, `path`, `icon`, `sort_order`, `permission_status`) VALUES
('940', '开题答辩管理', 'defense:menu', 1, '0', '/defense', 'form', 940, 1),
('941', '查看答辩安排列表', 'defense:arrangement:list', 2, '940', NULL, NULL, 941, 1),
('942', '查看答辩安排详情', 'defense:arrangement:detail', 2, '940', NULL, NULL, 942, 1),
('943', '创建答辩安排', 'defense:arrangement:create', 2, '940', NULL, NULL, 943, 1),
('944', '更新答辩安排', 'defense:arrangement:update', 2, '940', NULL, NULL, 944, 1),
('945', '删除答辩安排', 'defense:arrangement:delete', 2, '940', NULL, NULL, 945, 1),
('946', '答辩安排菜单', 'defense:arrangement:menu', 1, '940', '/defense/arrangement', NULL, 946, 1),
('947', '任务书菜单', 'defense:taskbook:menu', 1, '940', '/defense/taskbook', NULL, 947, 1),
('948', '查看任务书详情', 'defense:taskbook:detail', 2, '940', NULL, NULL, 948, 1),
('949', '保存任务书', 'defense:taskbook:save', 2, '940', NULL, NULL, 949, 1),
('950', '开题报告菜单', 'defense:report:menu', 1, '940', '/defense/report', NULL, 950, 1),
('951', '查看开题报告列表', 'defense:report:list', 2, '940', NULL, NULL, 951, 1),
('952', '查看开题报告详情', 'defense:report:detail', 2, '940', NULL, NULL, 952, 1),
('953', '提交开题报告', 'defense:report:submit', 2, '940', NULL, NULL, 953, 1),
('954', '查看我的开题报告', 'defense:report:my', 2, '940', '/defense/my-report', NULL, 954, 1),
('955', '审查开题报告', 'defense:report:review', 2, '940', NULL, NULL, 955, 1),
('956', '我的任务书菜单', 'defense:taskbook:my', 1, '940', '/defense/my-taskbook', NULL, 956, 1);

-- 为企业负责人角色分配权限（role_id=4）
INSERT INTO `role_permission` (`id`, `role_id`, `permission_id`) VALUES
(REPLACE(UUID(),'-',''), '4', '940'),
(REPLACE(UUID(),'-',''), '4', '941'),
(REPLACE(UUID(),'-',''), '4', '942'),
(REPLACE(UUID(),'-',''), '4', '943'),
(REPLACE(UUID(),'-',''), '4', '944'),
(REPLACE(UUID(),'-',''), '4', '945'),
(REPLACE(UUID(),'-',''), '4', '946'),
(REPLACE(UUID(),'-',''), '4', '947'),
(REPLACE(UUID(),'-',''), '4', '948'),
(REPLACE(UUID(),'-',''), '4', '950'),
(REPLACE(UUID(),'-',''), '4', '951'),
(REPLACE(UUID(),'-',''), '4', '952'),
(REPLACE(UUID(),'-',''), '4', '955');

-- 为企业教师角色分配权限（role_id=6）
INSERT INTO `role_permission` (`id`, `role_id`, `permission_id`) VALUES
(REPLACE(UUID(),'-',''), '6', '940'),
(REPLACE(UUID(),'-',''), '6', '942'),
(REPLACE(UUID(),'-',''), '6', '947'),
(REPLACE(UUID(),'-',''), '6', '948'),
(REPLACE(UUID(),'-',''), '6', '949'),
(REPLACE(UUID(),'-',''), '6', '950'),
(REPLACE(UUID(),'-',''), '6', '951'),
(REPLACE(UUID(),'-',''), '6', '952'),
(REPLACE(UUID(),'-',''), '6', '955');

-- 为学生角色分配权限（role_id=7）
INSERT INTO `role_permission` (`id`, `role_id`, `permission_id`) VALUES
(REPLACE(UUID(),'-',''), '7', '940'),
(REPLACE(UUID(),'-',''), '7', '942'),
(REPLACE(UUID(),'-',''), '7', '947'),
(REPLACE(UUID(),'-',''), '7', '948'),
(REPLACE(UUID(),'-',''), '7', '956'),
(REPLACE(UUID(),'-',''), '7', '950'),
(REPLACE(UUID(),'-',''), '7', '952'),
(REPLACE(UUID(),'-',''), '7', '953'),
(REPLACE(UUID(),'-',''), '7', '954');

-- 为系统管理员角色分配权限（role_id=1）
INSERT INTO `role_permission` (`id`, `role_id`, `permission_id`) VALUES
(REPLACE(UUID(),'-',''), '1', '940'),
(REPLACE(UUID(),'-',''), '1', '942'),
(REPLACE(UUID(),'-',''), '1', '943'),
(REPLACE(UUID(),'-',''), '1', '944'),
(REPLACE(UUID(),'-',''), '1', '945'),
(REPLACE(UUID(),'-',''), '1', '947'),
(REPLACE(UUID(),'-',''), '1', '948'),
(REPLACE(UUID(),'-',''), '1', '949'),
(REPLACE(UUID(),'-',''), '1', '950'),
(REPLACE(UUID(),'-',''), '1', '951'),
(REPLACE(UUID(),'-',''), '1', '952'),
(REPLACE(UUID(),'-',''), '1', '953'),
(REPLACE(UUID(),'-',''), '1', '954'),
(REPLACE(UUID(),'-',''), '1', '955'),
(REPLACE(UUID(),'-',''), '1', '956');
