-- 目的：修复“我的文档”查询报错 1267 (Illegal mix of collations)
-- 根因：document_info 的 ID 列为 utf8mb4_general_ci，
--      但 user_info/topic_info 的关联列为 utf8mb4_unicode_ci，JOIN '=' 时报错。

USE graduation_system;

-- 推荐方案：统一 document_info 全表排序规则为 utf8mb4_unicode_ci
ALTER TABLE document_info
    CONVERT TO CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 校验关键列排序规则
SELECT TABLE_NAME,
       COLUMN_NAME,
       CHARACTER_SET_NAME,
       COLLATION_NAME
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'graduation_system'
  AND TABLE_NAME IN ('document_info', 'user_info', 'topic_info')
  AND COLUMN_NAME IN ('document_id', 'student_id', 'topic_id', 'uploader_id', 'user_id')
ORDER BY TABLE_NAME, COLUMN_NAME;
