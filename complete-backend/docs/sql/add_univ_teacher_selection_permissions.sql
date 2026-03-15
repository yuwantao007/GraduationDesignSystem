-- =====================================================
-- 双选管理 - 高校教师查看选题权限配置
-- 功能：为高校教师角色（UNIVERSITY_TEACHER）添加查看
--       指导学生选题结果的相关权限
-- @author 系统架构师
-- @version 1.0
-- @since 2026-03-14
-- =====================================================

USE graduation_system;

-- -------------------------------------------
-- 1. 添加权限定义（ID 513~514）
-- -------------------------------------------

-- 高校教师子权限
INSERT IGNORE INTO permission_info (permission_id, parent_id, permission_name, permission_code, permission_type, path, sort_order) VALUES
('513', '500', '指导学生选题', 'selection:univ:view',   1, '/topic-selection/univ-teacher', 13),
('514', '500', '导出选题结果', 'selection:univ:export',  2, NULL,                            14);

-- -------------------------------------------
-- 2. 为高校教师角色分配权限
-- -------------------------------------------

SET @ut_role_id = (SELECT role_id FROM role_info WHERE role_code = 'UNIVERSITY_TEACHER' AND deleted = 0 LIMIT 1);

-- 双选管理父菜单
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @ut_role_id, '500' FROM DUAL WHERE @ut_role_id IS NOT NULL;

-- 指导学生选题页权限
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @ut_role_id, '513' FROM DUAL WHERE @ut_role_id IS NOT NULL;

-- 导出权限
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @ut_role_id, '514' FROM DUAL WHERE @ut_role_id IS NOT NULL;

-- -------------------------------------------
-- 3. 为系统管理员补充权限（查看用）
-- -------------------------------------------

SET @admin_role_id = (SELECT role_id FROM role_info WHERE role_code = 'SYSTEM_ADMIN' AND deleted = 0 LIMIT 1);

INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @admin_role_id, '513' FROM DUAL WHERE @admin_role_id IS NOT NULL;
