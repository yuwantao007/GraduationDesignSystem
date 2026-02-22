-- ============================================
-- 修复管理员账号 - 添加工号字段
-- ============================================
-- 说明：由于登录逻辑改为使用学号/工号，需要为现有的 admin 账号设置 employee_no
-- 执行此脚本修复已存在的 admin 账号数据

-- 为 admin 账号设置 employee_no
UPDATE user_info 
SET employee_no = 'admin' 
WHERE username = 'admin' AND user_id = '1';

-- 验证修改结果
SELECT user_id, username, employee_no, student_no, real_name, user_email 
FROM user_info 
WHERE username = 'admin';
