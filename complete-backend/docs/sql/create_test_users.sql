-- ============================================
-- 测试用户创建脚本
-- 用于测试企业教师和企业负责人的权限
-- 日期：2026-02-22
-- ============================================

USE graduation_system;

-- ============================================
-- 1. 创建企业教师测试账号
-- 账号：20001（工号）密码：123456
-- ============================================
INSERT INTO user_info (user_id, username, password, real_name, user_email, user_phone, employee_no, user_status) VALUES
('TEST_ET_001', '20001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王企业教师', 'enterprise.teacher@example.com', '13800138001', '20001', 1);

-- 为企业教师分配角色
INSERT INTO user_role (id, user_id, role_id) VALUES
('TEST_UR_001', 'TEST_ET_001', '6');  -- ENTERPRISE_TEACHER

-- ============================================
-- 2. 创建企业负责人测试账号
-- 账号：20002（工号）密码：123456
-- ============================================
INSERT INTO user_info (user_id, username, password, real_name, user_email, user_phone, employee_no, user_status) VALUES
('TEST_EL_001', '20002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李企业负责人', 'enterprise.leader@example.com', '13800138002', '20002', 1);

-- 为企业负责人分配角色
INSERT INTO user_role (id, user_id, role_id) VALUES
('TEST_UR_002', 'TEST_EL_001', '4');  -- ENTERPRISE_LEADER

-- ============================================
-- 3. 创建高校教师测试账号
-- 账号：20003（工号）密码：123456
-- ============================================
INSERT INTO user_info (user_id, username, password, real_name, user_email, user_phone, employee_no, user_status) VALUES
('TEST_UT_001', '20003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张高校教师', 'university.teacher@example.com', '13800138003', '20003', 1);

-- 为高校教师分配角色
INSERT INTO user_role (id, user_id, role_id) VALUES
('TEST_UR_003', 'TEST_UT_001', '3');  -- UNIVERSITY_TEACHER

-- ============================================
-- 4. 创建学生测试账号
-- 账号：2024001（学号）密码：123456
-- ============================================
INSERT INTO user_info (user_id, username, password, real_name, user_email, user_phone, student_no, user_status) VALUES
('TEST_ST_001', '2024001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '赵学生', 'student@example.com', '13800138004', '2024001', 1);

-- 为学生分配角色
INSERT INTO user_role (id, user_id, role_id) VALUES
('TEST_UR_004', 'TEST_ST_001', '7');  -- STUDENT

-- ============================================
-- 验证脚本
-- ============================================
-- 查看所有测试用户
-- SELECT u.user_id, u.username, u.real_name, u.employee_no, u.student_no, r.role_name, r.role_code
-- FROM user_info u
-- INNER JOIN user_role ur ON u.user_id = ur.user_id
-- INNER JOIN role_info r ON ur.role_id = r.role_id
-- WHERE u.user_id LIKE 'TEST_%';
