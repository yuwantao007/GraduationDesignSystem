-- ========================================================================
-- 学校管理模块权限配置脚本
-- 功能说明：添加学校管理相关的权限定义和角色权限映射
-- 权限范围：学校管理功能仅对系统管理员(SYSTEM_ADMIN)开放
-- 执行时间：请在业务低峰期执行
-- 执行方式：在MySQL客户端或数据库管理工具中执行
-- 
-- 注意事项：
-- 1. 执行前请备份 permission_info 和 role_permission 表
-- 2. 权限ID范围：520-539（预留20个权限位）
-- 3. 本脚本使用 INSERT IGNORE 语句，重复执行不会报错
-- 4. 执行后建议重启应用或清除缓存，确保权限生效
-- ========================================================================

USE graduation_system;

-- ========================================================================
-- 第一部分：添加学校管理权限定义
-- ========================================================================

-- 学校管理 - 基础权限
INSERT IGNORE INTO permission_info (permission_id, parent_id, permission_name, permission_code, permission_type, path, icon, sort_order) VALUES 
    -- 学校模块父权限（一级菜单）
    ('520', '0', '学校管理', 'school', 1, '/school', 'ReadOutlined', 6),
    
    -- 学校查看权限（二级菜单）
    ('521', '520', '学校列表', 'school:view', 1, '/school', 'UnorderedListOutlined', 1),
    
    -- 学校操作按钮权限
    ('5211', '521', '创建学校', 'school:create', 2, NULL, NULL, 1),
    ('5212', '521', '编辑学校', 'school:edit', 2, NULL, NULL, 2),
    ('5213', '521', '删除学校', 'school:delete', 2, NULL, NULL, 3),
    ('5214', '521', '管理学校状态', 'school:status', 2, NULL, NULL, 4),
    ('5215', '521', '导出学校数据', 'school:export', 2, NULL, NULL, 5);

-- ========================================================================
-- 第二部分：为系统管理员分配学校管理权限
-- ========================================================================

-- 系统管理员 (SYSTEM_ADMIN, role_id='1') - 拥有所有学校管理权限
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES 
    ('520', '1', '520'),   -- 学校管理模块
    ('521', '1', '521'),   -- 学校列表
    ('522', '1', '5211'),  -- 创建学校
    ('523', '1', '5212'),  -- 编辑学校
    ('524', '1', '5213'),  -- 删除学校
    ('525', '1', '5214'),  -- 管理学校状态
    ('526', '1', '5215');  -- 导出学校数据（预留）

-- ========================================================================
-- 第三部分：数据验证查询
-- ========================================================================

-- 验证权限定义是否添加成功
SELECT 
    permission_id,
    parent_id,
    permission_name,
    permission_code,
    permission_type,
    path,
    icon,
    sort_order
FROM permission_info
WHERE permission_id >= '520' AND permission_id < '540'
ORDER BY permission_id;

-- 验证角色权限映射是否添加成功
SELECT 
    rp.id,
    rp.role_id,
    r.role_code,
    r.role_name,
    rp.permission_id,
    p.permission_code,
    p.permission_name
FROM role_permission rp
INNER JOIN role_info r ON rp.role_id = r.role_id
INNER JOIN permission_info p ON rp.permission_id = p.permission_id
WHERE rp.permission_id >= '520' AND rp.permission_id < '540'
ORDER BY rp.permission_id;

-- ========================================================================
-- 第四部分：回滚脚本（如需撤销，请取消下方注释执行）
-- ========================================================================

-- -- 删除角色权限映射
-- DELETE FROM role_permission WHERE permission_id >= '520' AND permission_id < '540';
-- 
-- -- 删除权限定义
-- DELETE FROM permission_info WHERE permission_id >= '520' AND permission_id < '540';
