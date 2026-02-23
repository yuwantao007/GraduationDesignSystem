-- ========================================================================
-- 课题表添加学校关联字段
-- 功能说明：为 topic_info 表添加 school_id 字段，用于 3+1 和实验班类型课题关联学校
-- 执行时间：请在业务低峰期执行
-- 执行方式：在MySQL客户端或数据库管理工具中执行
-- 
-- 注意事项：
-- 1. 执行前请备份 topic_info 表
-- 2. 修改后 enterprise_id 字段改为可空（高职升本填企业，3+1/实验班填学校）
-- 3. 执行后建议重启应用，确保新字段生效
-- ========================================================================

USE graduation_system;

-- ========================================================================
-- 第一部分：添加 school_id 字段
-- ========================================================================

-- 添加学校ID字段
ALTER TABLE topic_info 
ADD COLUMN school_id VARCHAR(32) DEFAULT NULL COMMENT '关联学校ID（3+1/实验班课题使用）' 
AFTER enterprise_id;

-- 添加外键索引（不创建外键约束，避免级联删除问题）
ALTER TABLE topic_info 
ADD INDEX idx_school_id (school_id);

-- ========================================================================
-- 第二部分：修改 enterprise_id 字段为可空
-- ========================================================================

-- 将 enterprise_id 改为可空（高职升本填企业，3+1/实验班填学校）
ALTER TABLE topic_info 
MODIFY COLUMN enterprise_id VARCHAR(32) DEFAULT NULL COMMENT '归属企业ID（高职升本课题使用）';

-- ========================================================================
-- 第三部分：验证修改
-- ========================================================================

-- 查看表结构变更
DESC topic_info;

-- 查看涉及的字段
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'graduation_system'
  AND TABLE_NAME = 'topic_info'
  AND COLUMN_NAME IN ('enterprise_id', 'school_id', 'applicable_school', 'topic_category')
ORDER BY ORDINAL_POSITION;

-- ========================================================================
-- 第四部分：数据说明
-- ========================================================================

-- 字段使用规则：
-- 1. topic_category = 1 (高职升本)
--    - 必填：enterprise_id（归属企业）
--    - 不填：school_id、applicable_school
--
-- 2. topic_category = 2 (3+1) 或 3 (实验班)
--    - 必填：school_id（关联学校）
--    - 必填：applicable_school（适用学校名称，用于显示）
--    - 不填：enterprise_id

-- ========================================================================
-- 第五部分：回滚脚本（如需撤销，请取消下方注释执行）
-- ========================================================================

-- -- 删除学校ID字段
-- ALTER TABLE topic_info DROP COLUMN school_id;
-- 
-- -- 恢复 enterprise_id 为必填
-- ALTER TABLE topic_info 
-- MODIFY COLUMN enterprise_id VARCHAR(32) NOT NULL COMMENT '归属企业ID';
