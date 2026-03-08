-- =====================================================
-- 方案A：为 user_info 和 topic_info 表添加 major_id 字段
-- 功能：建立学生-专业、课题-专业的精确关联
-- 执行时间：2026-03-08
-- =====================================================

-- 1. 为 user_info 表添加 major_id 字段（幂等执行，字段不存在时才添加）
--    紧跟在 direction_id 之后，表示该用户（学生）所属的精确专业
ALTER TABLE user_info
    ADD COLUMN IF NOT EXISTS major_id VARCHAR(32) NULL COMMENT '所在专业ID（学生精确专业关联）'
    AFTER direction_id;

-- 2. 为 topic_info 表添加 major_id 字段（幂等执行，字段不存在时才添加）
--    紧跟在 enterprise_id 之后，表示该课题面向的专业
ALTER TABLE topic_info
    ADD COLUMN IF NOT EXISTS major_id VARCHAR(32) NULL COMMENT '课题所属专业ID（课题面向的专业）'
    AFTER enterprise_id;

-- 验证添加结果
-- SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT
-- FROM information_schema.COLUMNS
-- WHERE TABLE_SCHEMA = DATABASE()
--   AND TABLE_NAME IN ('user_info', 'topic_info')
--   AND COLUMN_NAME = 'major_id'
-- ORDER BY TABLE_NAME;
