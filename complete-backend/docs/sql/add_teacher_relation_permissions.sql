-- ============================================
-- 教师配对管理模块权限配置脚本
-- 功能：教师配对的查看、分配、编辑、删除
-- 日期：2026-03-08
-- 权限ID范围：540-559
-- 角色权限关联ID范围：900-949
-- ============================================

USE graduation_system;

-- ============================================
-- 1. 添加教师配对管理相关权限
-- ============================================
INSERT INTO permission_info (permission_id, parent_id, permission_name, permission_code, permission_type, path, icon, sort_order) VALUES
-- 一级菜单
('540', '0', '教师配对管理', 'teacher_relation:manage', 1, '/system/teacher-relation', 'TeamOutlined', 9),
-- 子菜单
('541', '540', '方向分配', 'teacher_relation:major:view', 1, '/system/teacher-relation/major', 'ApartmentOutlined', 1),
('542', '540', '精确配对', 'teacher_relation:pair:view', 1, '/system/teacher-relation/pair', 'SwapOutlined', 2),
('543', '540', '覆盖检查', 'teacher_relation:coverage:view', 1, '/system/teacher-relation/coverage', 'CheckCircleOutlined', 3),
-- 按钮操作权限
('5411', '541', '新增方向分配', 'teacher_relation:major:create', 2, NULL, NULL, 1),
('5412', '541', '编辑方向分配', 'teacher_relation:major:edit', 2, NULL, NULL, 2),
('5413', '541', '删除方向分配', 'teacher_relation:major:delete', 2, NULL, NULL, 3),
('5421', '542', '新增精确配对', 'teacher_relation:pair:create', 2, NULL, NULL, 1),
('5422', '542', '编辑精确配对', 'teacher_relation:pair:edit', 2, NULL, NULL, 2),
('5423', '542', '删除精确配对', 'teacher_relation:pair:delete', 2, NULL, NULL, 3);

-- ============================================
-- 2. 为系统管理员分配完整权限
-- 角色ID: 1 (SYSTEM_ADMIN)
-- ============================================
INSERT INTO role_permission (id, role_id, permission_id) VALUES
('900', '1', '540'),    -- 教师配对管理菜单
('901', '1', '541'),    -- 方向分配
('902', '1', '542'),    -- 精确配对
('903', '1', '543'),    -- 覆盖检查
('904', '1', '5411'),   -- 新增方向分配
('905', '1', '5412'),   -- 编辑方向分配
('906', '1', '5413'),   -- 删除方向分配
('907', '1', '5421'),   -- 新增精确配对
('908', '1', '5422'),   -- 编辑精确配对
('909', '1', '5423');   -- 删除精确配对

-- ============================================
-- 3. 为企业负责人分配只读权限
-- 角色ID: 4 (ENTERPRISE_LEADER)
-- ============================================
INSERT INTO role_permission (id, role_id, permission_id) VALUES
('910', '4', '540'),    -- 教师配对管理菜单
('911', '4', '541'),    -- 方向分配（只读）
('912', '4', '542'),    -- 精确配对（只读）
('913', '4', '543');    -- 覆盖检查

-- ============================================
-- 4. 为高校教师分配覆盖检查查看权限
-- 角色ID: 3 (UNIVERSITY_TEACHER)
-- ============================================
INSERT INTO role_permission (id, role_id, permission_id) VALUES
('920', '3', '540'),    -- 教师配对管理菜单
('921', '3', '543');    -- 覆盖检查（只读，查看自己的配对）

-- ============================================
-- 5. 验证权限分配
-- ============================================
SELECT '===== 教师配对管理权限定义 =====' AS '验证信息';
SELECT permission_id, permission_name, permission_code, permission_type
FROM permission_info
WHERE permission_id >= '540' AND permission_id < '560'
ORDER BY permission_id;

SELECT '===== 教师配对管理角色权限分配 =====' AS '验证信息';
SELECT rp.id, r.role_name, p.permission_name, p.permission_code
FROM role_permission rp
JOIN role_info r ON rp.role_id = r.role_id
JOIN permission_info p ON rp.permission_id = p.permission_id
WHERE rp.permission_id >= '540' AND rp.permission_id < '560'
ORDER BY rp.role_id, rp.permission_id;

SELECT '教师配对管理权限配置完成' AS '执行结果';
