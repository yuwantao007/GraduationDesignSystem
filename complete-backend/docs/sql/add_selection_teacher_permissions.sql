-- =====================================================
-- 双选管理 - 教师确认 & 双选审核权限配置
-- 功能：为企业教师、企业负责人角色添加双选相关权限
-- @author 系统架构师
-- @version 1.0
-- @since 2026-03-14
-- =====================================================

USE graduation_system;

-- -------------------------------------------
-- 1. 添加权限定义（ID 505~512）
-- -------------------------------------------

-- 企业教师子权限
INSERT IGNORE INTO permission_info (permission_id, parent_id, permission_name, permission_code, permission_type, path, sort_order) VALUES
('505', '500', '待确认选报', 'selection:teacher:list',    1, '/topic-selection/teacher', 5),
('506', '500', '确认人选',   'selection:teacher:confirm', 2, NULL,                       6),
('507', '500', '拒绝人选',   'selection:teacher:reject',  2, NULL,                       7),
('508', '500', '导出确认学生','selection:teacher:export', 2, NULL,                       8);

-- 企业负责人子权限
INSERT IGNORE INTO permission_info (permission_id, parent_id, permission_name, permission_code, permission_type, path, sort_order) VALUES
('510', '500', '双选审核概览', 'selection:leader:overview', 1, '/topic-selection/leader', 10),
('511', '500', '导出选题信息', 'selection:leader:export',   2, NULL,                      11),
('512', '500', '指派教师',     'selection:leader:assign',   2, NULL,                      12);

-- -------------------------------------------
-- 2. 为企业教师角色分配权限
-- -------------------------------------------

SET @et_role_id = (SELECT role_id FROM role_info WHERE role_code = 'ENTERPRISE_TEACHER' AND deleted = 0 LIMIT 1);

INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @et_role_id, '500' FROM DUAL WHERE @et_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @et_role_id, '505' FROM DUAL WHERE @et_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @et_role_id, '506' FROM DUAL WHERE @et_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @et_role_id, '507' FROM DUAL WHERE @et_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @et_role_id, '508' FROM DUAL WHERE @et_role_id IS NOT NULL;

-- -------------------------------------------
-- 3. 为企业负责人角色分配权限
-- -------------------------------------------

SET @el_role_id = (SELECT role_id FROM role_info WHERE role_code = 'ENTERPRISE_LEADER' AND deleted = 0 LIMIT 1);

INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @el_role_id, '500' FROM DUAL WHERE @el_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @el_role_id, '510' FROM DUAL WHERE @el_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @el_role_id, '511' FROM DUAL WHERE @el_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @el_role_id, '512' FROM DUAL WHERE @el_role_id IS NOT NULL;

-- -------------------------------------------
-- 4. 为系统管理员补充权限（查看用）
-- -------------------------------------------

SET @admin_role_id = (SELECT role_id FROM role_info WHERE role_code = 'SYSTEM_ADMIN' AND deleted = 0 LIMIT 1);

INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @admin_role_id, '505' FROM DUAL WHERE @admin_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @admin_role_id, '510' FROM DUAL WHERE @admin_role_id IS NOT NULL;
