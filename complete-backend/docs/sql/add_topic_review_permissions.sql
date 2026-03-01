-- ========================================================================
-- 课题审查模块权限配置脚本
-- 功能说明：添加课题审查相关的权限定义和角色权限映射
-- 权限范围：
--   - 系统管理员(SYSTEM_ADMIN): 所有审查权限
--   - 高校教师(UNIVERSITY_TEACHER): 预审权限
--   - 专业方向主管(MAJOR_DIRECTOR): 初审权限、综合意见权限
--   - 督导教师(SUPERVISOR_TEACHER): 终审权限、综合意见权限
--   - 企业教师(ENTERPRISE_TEACHER): 查看审查历史权限
--   - 企业负责人(ENTERPRISE_LEADER): 统计查看权限
-- 执行时间：请在业务低峰期执行
-- 执行方式：在MySQL客户端或数据库管理工具中执行
-- 
-- 注意事项：
-- 1. 执行前请备份 permission_info 和 role_permission 表
-- 2. 权限ID范围：600-649（预留50个权限位）
-- 3. 本脚本使用 INSERT IGNORE 语句，重复执行不会报错
-- 4. 执行后建议重启应用或清除缓存，确保权限生效
-- ========================================================================

USE graduation_system;

-- ========================================================================
-- 第一部分：添加课题审查权限定义
-- ========================================================================

-- 课题审查 - 模块权限
INSERT IGNORE INTO permission_info (permission_id, parent_id, permission_name, permission_code, permission_type, path, icon, sort_order) VALUES 
    -- 课题审查模块父权限（一级菜单）
    ('600', '0', '课题审查', 'topic:review', 1, '/topic/review', 'AuditOutlined', 6),
    
    -- 待审查列表（二级菜单）
    ('601', '600', '待审课题', 'topic:review:pending', 1, '/topic/review/pending', 'UnorderedListOutlined', 1),
    
    -- 审查历史（二级菜单）
    ('602', '600', '审查历史', 'topic:review:history', 1, '/topic/review/history', 'HistoryOutlined', 2),
    
    -- 综合意见（二级菜单）
    ('603', '600', '综合意见', 'topic:review:opinion', 1, '/topic/review/opinion', 'CommentOutlined', 3),
    
    -- 审查统计（二级菜单）
    ('604', '600', '审查统计', 'topic:review:stats', 1, '/topic/review/stats', 'BarChartOutlined', 4);

-- 课题审查 - 操作按钮权限
INSERT IGNORE INTO permission_info (permission_id, parent_id, permission_name, permission_code, permission_type, path, icon, sort_order) VALUES 
    -- 预审操作权限
    ('6011', '601', '预审课题', 'topic:review:pre', 2, NULL, NULL, 1),
    
    -- 初审操作权限
    ('6012', '601', '初审课题', 'topic:review:init', 2, NULL, NULL, 2),
    
    -- 终审操作权限
    ('6013', '601', '终审课题', 'topic:review:final', 2, NULL, NULL, 3),
    
    -- 批量审批权限
    ('6014', '601', '批量审批', 'topic:review:batch', 2, NULL, NULL, 4),
    
    -- 修改审批权限
    ('6015', '601', '修改审批', 'topic:review:modify', 2, NULL, NULL, 5),
    
    -- 查看审查历史
    ('6021', '602', '查看历史', 'topic:review:history:view', 2, NULL, NULL, 1),
    
    -- 综合意见操作权限
    ('6031', '603', '提交综合意见', 'topic:review:opinion:submit', 2, NULL, NULL, 1),
    ('6032', '603', '查看综合意见', 'topic:review:opinion:view', 2, NULL, NULL, 2),
    ('6033', '603', '删除综合意见', 'topic:review:opinion:delete', 2, NULL, NULL, 3),
    
    -- 统计权限
    ('6041', '604', '查看教师统计', 'topic:review:stats:teacher', 2, NULL, NULL, 1),
    ('6042', '604', '检查提交资格', 'topic:review:stats:check', 2, NULL, NULL, 2);

-- ========================================================================
-- 第二部分：为各角色分配课题审查权限
-- ========================================================================

-- 系统管理员 (SYSTEM_ADMIN, role_id='1') - 拥有所有课题审查权限
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES 
    ('600', '1', '600'),   -- 课题审查模块
    ('601', '1', '601'),   -- 待审课题
    ('602', '1', '602'),   -- 审查历史
    ('603', '1', '603'),   -- 综合意见
    ('604', '1', '604'),   -- 审查统计
    ('605', '1', '6011'),  -- 预审课题
    ('606', '1', '6012'),  -- 初审课题
    ('607', '1', '6013'),  -- 终审课题
    ('608', '1', '6014'),  -- 批量审批
    ('609', '1', '6015'),  -- 修改审批
    ('610', '1', '6021'),  -- 查看历史
    ('611', '1', '6031'),  -- 提交综合意见
    ('612', '1', '6032'),  -- 查看综合意见
    ('613', '1', '6033'),  -- 删除综合意见
    ('614', '1', '6041'),  -- 查看教师统计
    ('615', '1', '6042');  -- 检查提交资格

-- 高校教师 (UNIVERSITY_TEACHER, role_id='2') - 预审权限 + 终审3+1/实验班课题
-- 注意：高校教师可以预审高职升本课题，同时也可以终审3+1/实验班课题
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES 
    ('620', '2', '600'),   -- 课题审查模块
    ('621', '2', '601'),   -- 待审课题
    ('622', '2', '602'),   -- 审查历史
    ('623', '2', '6011'),  -- 预审课题
    ('624', '2', '6013'),  -- 终审课题（3+1/实验班）
    ('625', '2', '6014'),  -- 批量审批
    ('626', '2', '6015'),  -- 修改审批
    ('627', '2', '6021'),  -- 查看历史
    ('628', '2', '6032');  -- 查看综合意见

-- 专业方向主管 (MAJOR_DIRECTOR, role_id='3') - 初审权限 + 综合意见权限
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES 
    ('630', '3', '600'),   -- 课题审查模块
    ('631', '3', '601'),   -- 待审课题
    ('632', '3', '602'),   -- 审查历史
    ('633', '3', '603'),   -- 综合意见
    ('634', '3', '6012'),  -- 初审课题
    ('635', '3', '6014'),  -- 批量审批
    ('636', '3', '6015'),  -- 修改审批
    ('637', '3', '6021'),  -- 查看历史
    ('638', '3', '6031'),  -- 提交综合意见
    ('639', '3', '6032'),  -- 查看综合意见
    ('640', '3', '6033');  -- 删除综合意见

-- 督导教师 (SUPERVISOR_TEACHER, role_id='4') - 终审高职升本课题 + 综合意见权限 + 统计权限
-- 注意：督导教师仅终审高职升本课题，3+1/实验班课题由高校教师终审
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES 
    ('650', '4', '600'),   -- 课题审查模块
    ('651', '4', '601'),   -- 待审课题
    ('652', '4', '602'),   -- 审查历史
    ('653', '4', '603'),   -- 综合意见
    ('654', '4', '604'),   -- 审查统计
    ('655', '4', '6013'),  -- 终审课题
    ('656', '4', '6014'),  -- 批量审批
    ('657', '4', '6015'),  -- 修改审批
    ('658', '4', '6021'),  -- 查看历史
    ('659', '4', '6031'),  -- 提交综合意见
    ('660', '4', '6032'),  -- 查看综合意见
    ('661', '4', '6033'),  -- 删除综合意见
    ('662', '4', '6041'),  -- 查看教师统计
    ('663', '4', '6042');  -- 检查提交资格

-- 企业教师 (ENTERPRISE_TEACHER, role_id='5') - 仅查看审查历史和综合意见
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES 
    ('670', '5', '600'),   -- 课题审查模块
    ('671', '5', '602'),   -- 审查历史
    ('672', '5', '6021'),  -- 查看历史
    ('673', '5', '6032'),  -- 查看综合意见
    ('674', '5', '6042');  -- 检查提交资格

-- 企业负责人 (ENTERPRISE_LEADER, role_id='6') - 查看统计权限
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES 
    ('680', '6', '600'),   -- 课题审查模块
    ('681', '6', '604'),   -- 审查统计
    ('682', '6', '6041');  -- 查看教师统计

-- ========================================================================
-- 第三部分：验证语句（执行后可查看权限配置情况）
-- ========================================================================

-- 验证权限定义是否添加成功
SELECT '===== 课题审查权限定义 =====' AS '验证信息';
SELECT permission_id, permission_name, permission_code, permission_type 
FROM permission_info 
WHERE permission_id LIKE '6%' AND permission_id >= '600' AND permission_id < '700'
ORDER BY permission_id;

-- 验证角色权限映射是否添加成功
SELECT '===== 角色权限映射 =====' AS '验证信息';
SELECT rp.role_id, r.role_name, pi.permission_name 
FROM role_permission rp
JOIN role_info r ON rp.role_id = r.role_id
JOIN permission_info pi ON rp.permission_id = pi.permission_id
WHERE rp.permission_id LIKE '6%' AND rp.permission_id >= '600' AND rp.permission_id < '700'
ORDER BY rp.role_id, pi.permission_id;

-- ========================================================================
-- 执行完成提示
-- ========================================================================
SELECT '课题审查模块权限配置完成！' AS '执行结果';
SELECT '请重启应用或清除缓存以使权限生效' AS '后续操作';
