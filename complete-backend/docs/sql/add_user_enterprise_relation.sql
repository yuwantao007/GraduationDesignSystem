-- ============================================
-- 学生-企业关联功能 - 数据库迁移脚本
-- 日期：2026-03-14
-- 描述：为 user_info 表添加 enterprise_id 字段
--       实现学生与企业的直接关联，提高查询效率
-- ============================================

-- 1. 为 user_info 表添加 enterprise_id 字段
ALTER TABLE user_info 
ADD COLUMN enterprise_id VARCHAR(50) NULL COMMENT '所属企业ID' AFTER major_id;

-- 2. 创建索引，优化企业下学生查询性能
CREATE INDEX idx_user_enterprise ON user_info(enterprise_id);

-- 3. 数据迁移：根据已有的 major_id 回填 enterprise_id
-- 对于已关联专业的学生，自动设置其企业ID
UPDATE user_info u
INNER JOIN major_info m ON u.major_id = m.major_id
SET u.enterprise_id = m.enterprise_id
WHERE u.major_id IS NOT NULL 
  AND u.enterprise_id IS NULL;

-- 4. 验证迁移结果
-- SELECT 
--     COUNT(*) as total_students_with_major,
--     SUM(CASE WHEN enterprise_id IS NOT NULL THEN 1 ELSE 0 END) as students_with_enterprise
-- FROM user_info 
-- WHERE major_id IS NOT NULL;
