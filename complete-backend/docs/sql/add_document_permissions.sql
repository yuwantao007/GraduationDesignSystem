-- =============================================
-- 文档管理模块权限配置
-- 创建时间: 2026-03-15
-- 权限ID范围: 920-939
-- =============================================

-- 插入文档管理权限（父权限）
INSERT INTO `permission_info` (`permission_id`, `permission_name`, `permission_code`, `permission_type`, `parent_id`, `path`, `icon`, `sort_order`, `permission_status`) VALUES
('920', '文档管理', 'document:menu', 1, '0', '/document', 'file-text', 920, 1),
('921', '上传文档', 'document:upload', 2, '920', NULL, NULL, 921, 1),
('922', '查看文档', 'document:view', 2, '920', NULL, NULL, 922, 1),
('923', '下载文档', 'document:download', 2, '920', NULL, NULL, 923, 1),
('924', '删除文档', 'document:delete', 2, '920', NULL, NULL, 924, 1),
('925', '教师上传批注', 'document:upload:teacher', 2, '920', NULL, NULL, 925, 1),
('926', '学生文档中心', 'document:student', 2, '920', '/document/student', NULL, 926, 1),
('927', '教师文档查看', 'document:teacher', 2, '920', '/document/teacher', NULL, 927, 1);

-- 为学生角色分配权限（role_id=7）
INSERT INTO `role_permission` (`id`, `role_id`, `permission_id`) VALUES
(REPLACE(UUID(),'-',''), '7', '920'),
(REPLACE(UUID(),'-',''), '7', '921'),
(REPLACE(UUID(),'-',''), '7', '922'),
(REPLACE(UUID(),'-',''), '7', '923'),
(REPLACE(UUID(),'-',''), '7', '924'),
(REPLACE(UUID(),'-',''), '7', '926');

-- 为企业教师角色分配权限（role_id=6）
INSERT INTO `role_permission` (`id`, `role_id`, `permission_id`) VALUES
(REPLACE(UUID(),'-',''), '6', '920'),
(REPLACE(UUID(),'-',''), '6', '922'),
(REPLACE(UUID(),'-',''), '6', '923'),
(REPLACE(UUID(),'-',''), '6', '925'),
(REPLACE(UUID(),'-',''), '6', '927');

-- 为高校教师角色分配权限（role_id=3）
INSERT INTO `role_permission` (`id`, `role_id`, `permission_id`) VALUES
(REPLACE(UUID(),'-',''), '3', '920'),
(REPLACE(UUID(),'-',''), '3', '922'),
(REPLACE(UUID(),'-',''), '3', '923'),
(REPLACE(UUID(),'-',''), '3', '925'),
(REPLACE(UUID(),'-',''), '3', '927');

-- 为企业负责人角色分配权限（role_id=4）
INSERT INTO `role_permission` (`id`, `role_id`, `permission_id`) VALUES
(REPLACE(UUID(),'-',''), '4', '920'),
(REPLACE(UUID(),'-',''), '4', '922'),
(REPLACE(UUID(),'-',''), '4', '923'),
(REPLACE(UUID(),'-',''), '4', '927');

-- 为专业方向主管角色分配权限（role_id=5）
INSERT INTO `role_permission` (`id`, `role_id`, `permission_id`) VALUES
(REPLACE(UUID(),'-',''), '5', '920'),
(REPLACE(UUID(),'-',''), '5', '922'),
(REPLACE(UUID(),'-',''), '5', '923'),
(REPLACE(UUID(),'-',''), '5', '927');

-- 为督导教师角色分配权限（role_id=2）
INSERT INTO `role_permission` (`id`, `role_id`, `permission_id`) VALUES
(REPLACE(UUID(),'-',''), '2', '920'),
(REPLACE(UUID(),'-',''), '2', '922'),
(REPLACE(UUID(),'-',''), '2', '923'),
(REPLACE(UUID(),'-',''), '2', '927');

-- 为系统管理员角色分配权限（role_id=1）
INSERT INTO `role_permission` (`id`, `role_id`, `permission_id`) VALUES
(REPLACE(UUID(),'-',''), '1', '920'),
(REPLACE(UUID(),'-',''), '1', '921'),
(REPLACE(UUID(),'-',''), '1', '922'),
(REPLACE(UUID(),'-',''), '1', '923'),
(REPLACE(UUID(),'-',''), '1', '924'),
(REPLACE(UUID(),'-',''), '1', '925'),
(REPLACE(UUID(),'-',''), '1', '926'),
(REPLACE(UUID(),'-',''), '1', '927');
