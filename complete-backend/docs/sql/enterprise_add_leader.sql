-- =====================================================
-- 企业管理功能完善：添加负责人字段
-- 版本：V1.1  执行时间：2026-03-07
-- 说明：
--   1. enterprise_info 表新增 leader_id 字段，关联企业负责人用户
--   2. 旧数据 contactPerson 保留，不影响存量数据
-- =====================================================

-- 1. 新增 leader_id 字段
ALTER TABLE enterprise_info
    ADD COLUMN leader_id VARCHAR(32) NULL COMMENT '企业负责人用户ID（关联 user_info.user_id）'
        AFTER enterprise_code;

-- 2. （可选）为 leader_id 建立普通索引，加速按负责人查询
CREATE INDEX idx_enterprise_leader_id ON enterprise_info (leader_id);
