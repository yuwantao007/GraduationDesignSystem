-- ============================================
-- 课题审查教师测试账号创建脚本
-- 用于测试专业方向主管和督导教师的审查权限
-- 日期：2026-03-01
-- ============================================

USE graduation_system;

-- ============================================
-- 清理已存在的测试账号（如果存在）
-- ============================================
DELETE FROM user_role WHERE user_id IN ('TEST_MD_001', 'TEST_ST_002');
DELETE FROM user_info WHERE user_id IN ('TEST_MD_001', 'TEST_ST_002');

-- ============================================
-- 1. 创建专业方向主管测试账号
-- 账号：20004（工号）密码：123456
-- 角色：MAJOR_DIRECTOR (role_id=5)
-- 职能：课题初审
-- ============================================
INSERT INTO user_info (user_id, username, password, real_name, user_email, user_phone, employee_no, title, user_status, deleted) VALUES
('TEST_MD_001', '20004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '刘专业主管', 'major.director@example.com', '13800138004', '20004', '专业方向主管', 1, 0);

-- 为专业方向主管分配角色
INSERT INTO user_role (id, user_id, role_id) VALUES
('TEST_UR_005', 'TEST_MD_001', '5');  -- MAJOR_DIRECTOR

-- ============================================
-- 2. 创建督导教师测试账号
-- 账号：20005（工号）密码：123456
-- 角色：SUPERVISOR_TEACHER (role_id=2)
-- 职能：课题终审
-- ============================================
INSERT INTO user_info (user_id, username, password, real_name, user_email, user_phone, employee_no, title, user_status, deleted) VALUES
('TEST_ST_002', '20005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '陈督导教师', 'supervisor.teacher@example.com', '13800138005', '20005', '督导教师', 1, 0);

-- 为督导教师分配角色
INSERT INTO user_role (id, user_id, role_id) VALUES
('TEST_UR_006', 'TEST_ST_002', '2');  -- SUPERVISOR_TEACHER

-- ============================================
-- 验证脚本
-- ============================================
-- 查看新创建的测试用户
SELECT 
    u.user_id, 
    u.username, 
    u.real_name, 
    u.employee_no, 
    u.title,
    r.role_name, 
    r.role_code
FROM user_info u
INNER JOIN user_role ur ON u.user_id = ur.user_id
INNER JOIN role_info r ON ur.role_id = r.role_id
WHERE u.username IN ('20004', '20005')
ORDER BY r.role_code;

-- ============================================
-- 测试账号汇总
-- ============================================
-- 专业方向主管：账号 20004，密码 123456
-- 督导教师：账号 20005，密码 123456
-- ============================================
