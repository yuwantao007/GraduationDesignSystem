-- =============================================
-- 修复开题答辩相关表字符集排序规则不一致问题
-- 目的：避免与 user_info / enterprise_info 关联查询时出现
--      Illegal mix of collations (utf8mb4_general_ci vs utf8mb4_unicode_ci)
-- 执行时间：建议业务低峰期
-- =============================================

USE graduation_system;

-- 1) 统一 defense 模块三张表为 utf8mb4_unicode_ci
ALTER TABLE defense_arrangement CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE opening_report CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE opening_task_book CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2) 校验关键列排序规则
SELECT TABLE_NAME, COLUMN_NAME, CHARACTER_SET_NAME, COLLATION_NAME
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('defense_arrangement', 'opening_report', 'opening_task_book', 'user_info', 'enterprise_info')
  AND COLUMN_NAME IN ('arrangement_id', 'enterprise_id', 'creator_id', 'user_id')
ORDER BY TABLE_NAME, COLUMN_NAME;
