-- =====================================================
-- 双选管理 - 学生选报权限配置
-- 功能：为学生角色添加课题选报相关权限
-- @author 系统架构师
-- @version 1.0
-- @since 2026-03-08
-- =====================================================

USE graduation_system;

-- -------------------------------------------
-- 1. 添加课题选报权限定义
-- -------------------------------------------

-- 双选管理菜单权限
INSERT IGNORE INTO permission_info (permission_id, parent_id, permission_name, permission_code, permission_type, path, icon, sort_order) VALUES
('500', '0', '双选管理', 'selection:manage', 1, '/topic-selection', 'SelectOutlined', 50);

-- 学生选报子权限
INSERT IGNORE INTO permission_info (permission_id, parent_id, permission_name, permission_code, permission_type, path, sort_order) VALUES
('501', '500', '可选课题', 'selection:available', 1, '/topic-selection/list', 1),
('502', '500', '选报课题', 'selection:apply', 2, NULL, 2),
('503', '500', '删除选报', 'selection:delete', 2, NULL, 3),
('504', '500', '我的选报', 'selection:my', 1, '/topic-selection/my', 4);

-- -------------------------------------------
-- 2. 为学生角色分配选报权限
-- -------------------------------------------

-- 查询学生角色ID（role_code = 'STUDENT'）
SET @student_role_id = (SELECT role_id FROM role_info WHERE role_code = 'STUDENT' AND deleted = 0 LIMIT 1);

-- 为学生分配权限（仅在角色存在时）
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @student_role_id, '500' FROM DUAL WHERE @student_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @student_role_id, '501' FROM DUAL WHERE @student_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @student_role_id, '502' FROM DUAL WHERE @student_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @student_role_id, '503' FROM DUAL WHERE @student_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @student_role_id, '504' FROM DUAL WHERE @student_role_id IS NOT NULL;

-- -------------------------------------------
-- 3. 为系统管理员也添加选报管理权限（查看用）
-- -------------------------------------------

SET @admin_role_id = (SELECT role_id FROM role_info WHERE role_code = 'SYSTEM_ADMIN' AND deleted = 0 LIMIT 1);

INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @admin_role_id, '500' FROM DUAL WHERE @admin_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @admin_role_id, '501' FROM DUAL WHERE @admin_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @admin_role_id, '504' FROM DUAL WHERE @admin_role_id IS NOT NULL;
