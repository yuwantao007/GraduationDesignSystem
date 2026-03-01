-- ========================================================================
-- 课题审查模块权限修复脚本
-- 功能说明：修复课题审查权限配置，支持新的审核流程：
--   - 高职升本课题：高校教师预审 → 专业方向主管初审 → 督导教师终审
--   - 3+1/实验班课题：专业方向主管初审 → 高校教师终审
-- 
-- 主要变更：
--   - 高校教师(UNIVERSITY_TEACHER)新增终审权限(6013)，用于终审3+1/实验班课题
-- 
-- 执行时间：请在业务低峰期执行
-- 执行方式：在MySQL客户端或数据库管理工具中执行
-- 
-- 注意事项：
-- 1. 本脚本用于修复已执行 add_topic_review_permissions.sql 后的权限配置
-- 2. 脚本使用 INSERT IGNORE 语句，重复执行不会报错
-- 3. 执行后建议重启应用或清除缓存，确保权限生效
-- ========================================================================

USE graduation_system;

-- ========================================================================
-- 修复高校教师权限：新增终审权限(6013)
-- 高校教师现在需要终审3+1/实验班课题
-- ========================================================================

-- 添加高校教师的终审权限
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES 
    ('624', '2', '6013');  -- 终审课题（3+1/实验班）

-- ========================================================================
-- 验证修复结果
-- ========================================================================

-- 验证高校教师权限是否正确
SELECT '===== 高校教师(UNIVERSITY_TEACHER)权限列表 =====' AS '验证信息';
SELECT rp.id, rp.role_id, r.role_name, rp.permission_id, pi.permission_name, pi.permission_code
FROM role_permission rp
JOIN role_info r ON rp.role_id = r.role_id
JOIN permission_info pi ON rp.permission_id = pi.permission_id
WHERE rp.role_id = '2' 
  AND rp.permission_id LIKE '6%' 
  AND rp.permission_id >= '600' 
  AND rp.permission_id < '700'
ORDER BY pi.permission_id;

-- 验证高校教师是否同时拥有预审和终审权限
SELECT '===== 高校教师审核权限检查 =====' AS '验证信息';
SELECT 
    CASE WHEN COUNT(*) > 0 THEN '✅ 已拥有' ELSE '❌ 缺失' END AS '预审权限(6011)',
    (SELECT CASE WHEN COUNT(*) > 0 THEN '✅ 已拥有' ELSE '❌ 缺失' END 
     FROM role_permission WHERE role_id = '2' AND permission_id = '6013') AS '终审权限(6013)'
FROM role_permission 
WHERE role_id = '2' AND permission_id = '6011';

-- ========================================================================
-- 显示各角色的审核权限分配情况
-- ========================================================================

SELECT '===== 各角色审核权限分配 =====' AS '验证信息';
SELECT 
    r.role_name AS '角色名称',
    GROUP_CONCAT(
        CASE pi.permission_id
            WHEN '6011' THEN '预审'
            WHEN '6012' THEN '初审'
            WHEN '6013' THEN '终审'
        END
        ORDER BY pi.permission_id
        SEPARATOR ' → '
    ) AS '审核权限'
FROM role_info r
LEFT JOIN role_permission rp ON r.role_id = rp.role_id
LEFT JOIN permission_info pi ON rp.permission_id = pi.permission_id
WHERE pi.permission_id IN ('6011', '6012', '6013')
GROUP BY r.role_id, r.role_name
ORDER BY r.role_id;

-- ========================================================================
-- 执行完成提示
-- ========================================================================
SELECT '权限修复完成！' AS '执行结果';
SELECT '修改说明：高校教师现在可以终审3+1/实验班课题' AS '变更内容';
SELECT '请重启应用或清除缓存以使权限生效' AS '后续操作';
