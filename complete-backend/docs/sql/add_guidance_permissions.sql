-- =====================================================
-- 过程管理 - 指导记录模块 权限配置
-- 功能：配置指导记录相关权限和角色分配
-- @author 系统架构师
-- @version 1.0
-- @since 2026-03-16
-- =====================================================

USE graduation_system;

-- ============================================
-- 1. 指导记录权限配置（ID 范围 900-919）
-- ============================================

-- 一级菜单：过程管理
INSERT IGNORE INTO permission_info (permission_id, parent_id, permission_name, permission_code, permission_type, path, icon, sort_order) VALUES
('900', '0', '过程管理', 'guidance:manage', 1, '/guidance', 'FormOutlined', 50);

-- 二级菜单/权限
INSERT IGNORE INTO permission_info (permission_id, parent_id, permission_name, permission_code, permission_type, path, icon, sort_order) VALUES
-- 教师视角
('901', '900', '学生指导', 'guidance:teacher', 1, '/guidance/teacher', 'SolutionOutlined', 1),
('902', '900', '新增指导记录', 'guidance:create', 2, NULL, NULL, 2),
('903', '900', '删除指导记录', 'guidance:delete', 2, NULL, NULL, 3),
-- 学生视角
('904', '900', '我的指导记录', 'guidance:student', 1, '/guidance/student', 'ReadOutlined', 4),
('905', '900', '查看指导记录', 'guidance:view', 2, NULL, NULL, 5),
-- 企业负责人视角
('906', '900', '指导记录总览', 'guidance:leader', 1, '/guidance/leader', 'ProfileOutlined', 6),
('907', '900', '导出指导记录', 'guidance:export', 2, NULL, NULL, 7);

-- ============================================
-- 2. 角色权限分配
-- ============================================

-- 企业教师权限
SET @et_role_id = (SELECT role_id FROM role_info WHERE role_code = 'ENTERPRISE_TEACHER' AND deleted = 0 LIMIT 1);
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @et_role_id, '900' FROM DUAL WHERE @et_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @et_role_id, '901' FROM DUAL WHERE @et_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @et_role_id, '902' FROM DUAL WHERE @et_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @et_role_id, '903' FROM DUAL WHERE @et_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @et_role_id, '905' FROM DUAL WHERE @et_role_id IS NOT NULL;

-- 高校教师权限
SET @ut_role_id = (SELECT role_id FROM role_info WHERE role_code = 'UNIVERSITY_TEACHER' AND deleted = 0 LIMIT 1);
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @ut_role_id, '900' FROM DUAL WHERE @ut_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @ut_role_id, '901' FROM DUAL WHERE @ut_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @ut_role_id, '902' FROM DUAL WHERE @ut_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @ut_role_id, '903' FROM DUAL WHERE @ut_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @ut_role_id, '905' FROM DUAL WHERE @ut_role_id IS NOT NULL;

-- 学生权限
SET @student_role_id = (SELECT role_id FROM role_info WHERE role_code = 'STUDENT' AND deleted = 0 LIMIT 1);
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @student_role_id, '900' FROM DUAL WHERE @student_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @student_role_id, '904' FROM DUAL WHERE @student_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @student_role_id, '905' FROM DUAL WHERE @student_role_id IS NOT NULL;

-- 企业负责人权限
SET @el_role_id = (SELECT role_id FROM role_info WHERE role_code = 'ENTERPRISE_LEADER' AND deleted = 0 LIMIT 1);
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @el_role_id, '900' FROM DUAL WHERE @el_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @el_role_id, '905' FROM DUAL WHERE @el_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @el_role_id, '906' FROM DUAL WHERE @el_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @el_role_id, '907' FROM DUAL WHERE @el_role_id IS NOT NULL;

-- 系统管理员（全部权限）
SET @admin_role_id = (SELECT role_id FROM role_info WHERE role_code = 'SYSTEM_ADMIN' AND deleted = 0 LIMIT 1);
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @admin_role_id, '900' FROM DUAL WHERE @admin_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @admin_role_id, '901' FROM DUAL WHERE @admin_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @admin_role_id, '902' FROM DUAL WHERE @admin_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @admin_role_id, '903' FROM DUAL WHERE @admin_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @admin_role_id, '904' FROM DUAL WHERE @admin_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @admin_role_id, '905' FROM DUAL WHERE @admin_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @admin_role_id, '906' FROM DUAL WHERE @admin_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @admin_role_id, '907' FROM DUAL WHERE @admin_role_id IS NOT NULL;

-- ============================================
-- 3. 验证权限配置
-- ============================================
SELECT '===== 指导记录权限配置 =====' AS '验证信息';
SELECT permission_id, permission_name, permission_code FROM permission_info WHERE permission_id LIKE '90%' ORDER BY permission_id;

SELECT '===== 角色权限分配 =====' AS '验证信息';
SELECT r.role_name, p.permission_name 
FROM role_permission rp
JOIN role_info r ON rp.role_id = r.role_id
JOIN permission_info p ON rp.permission_id = p.permission_id
WHERE p.permission_id LIKE '90%'
ORDER BY r.role_id, p.permission_id;

SELECT '指导记录权限配置完成' AS '执行结果';
