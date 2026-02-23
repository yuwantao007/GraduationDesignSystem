-- ========================================================================
-- 企业管理模块权限配置脚本
-- 功能说明：添加企业管理相关的权限定义和角色权限映射
-- 权限范围：企业管理功能仅对系统管理员(SYSTEM_ADMIN)开放
-- 执行时间：请在业务低峰期执行
-- 执行方式：在MySQL客户端或数据库管理工具中执行
-- 
-- 注意事项：
-- 1. 执行前请备份 permission_info 和 role_permission 表
-- 2. 权限ID范围：500-519（预留20个权限位）
-- 3. 本脚本使用 INSERT IGNORE 语句，重复执行不会报错
-- 4. 执行后建议重启应用或清除缓存，确保权限生效
-- ========================================================================

USE graduation_system;

-- ========================================================================
-- 第一部分：添加企业管理权限定义
-- ========================================================================

-- 企业管理 - 基础权限
INSERT IGNORE INTO permission_info (permission_id, parent_id, permission_name, permission_code, permission_type, path, icon, sort_order) VALUES 
    -- 企业模块父权限（一级菜单）
    ('500', '0', '企业管理', 'enterprise', 1, '/enterprise', 'BankOutlined', 5),
    
    -- 企业查看权限（二级菜单）
    ('501', '500', '企业列表', 'enterprise:view', 1, '/enterprise', 'UnorderedListOutlined', 1),
    
    -- 企业操作按钮权限
    ('5011', '501', '创建企业', 'enterprise:create', 2, NULL, NULL, 1),
    ('5012', '501', '编辑企业', 'enterprise:edit', 2, NULL, NULL, 2),
    ('5013', '501', '删除企业', 'enterprise:delete', 2, NULL, NULL, 3),
    ('5014', '501', '管理企业状态', 'enterprise:status', 2, NULL, NULL, 4),
    ('5015', '501', '导出企业数据', 'enterprise:export', 2, NULL, NULL, 5);

-- ========================================================================
-- 第二部分：为系统管理员分配企业管理权限
-- ========================================================================

-- 系统管理员 (SYSTEM_ADMIN, role_id='1') - 拥有所有企业管理权限
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES 
    ('500', '1', '500'),   -- 企业管理模块
    ('501', '1', '501'),   -- 企业列表
    ('502', '1', '5011'),  -- 创建企业
    ('503', '1', '5012'),  -- 编辑企业
    ('504', '1', '5013'),  -- 删除企业
    ('505', '1', '5014'),  -- 管理企业状态
    ('506', '1', '5015');  -- 导出企业数据（预留）

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
WHERE permission_id >= '500' AND permission_id < '520'
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
WHERE rp.permission_id >= '500' AND rp.permission_id < '520'
ORDER BY rp.id;

-- 查看系统管理员的所有权限（包括新增的企业管理权限）
SELECT 
    r.role_code,
    r.role_name,
    p.permission_code,
    p.permission_name,
    p.permission_type
FROM role_permission rp
INNER JOIN role_info r ON rp.role_id = r.role_id
INNER JOIN permission_info p ON rp.permission_id = p.permission_id
WHERE r.role_code = 'SYSTEM_ADMIN'
ORDER BY p.permission_id;

-- ========================================================================
-- 脚本执行完成
-- 
-- 执行结果说明：
-- 1. 权限定义表（permission_info）应新增 7 条记录
--    - 一级菜单：500（企业管理）
--    - 二级菜单：501（企业列表）
--    - 按钮权限：5011-5015（创建、编辑、删除、状态管理、导出）
-- 2. 角色权限映射表（role_permission）应新增 7 条记录（id: 500-506）
--    - 仅分配给系统管理员（role_id='1', SYSTEM_ADMIN）
-- 3. 系统管理员现在拥有企业管理的所有操作权限
-- 
-- 后续操作建议：
-- 1. 清除Redis缓存（如有）：redis-cli FLUSHDB
-- 2. 重启Spring Boot应用，确保权限配置生效
-- 3. 使用管理员账号登录系统，验证企业管理菜单是否可见
-- 4. 测试企业的增删改查功能是否正常
-- 5. 使用非管理员账号登录，确认无法访问企业管理功能（应显示403）
-- ========================================================================
