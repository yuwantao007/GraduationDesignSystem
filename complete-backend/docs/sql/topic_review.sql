-- ============================================
-- 课题审查模块数据库表结构
-- @author 系统架构师
-- @version 1.0
-- @since 2026-02-23
-- ============================================

USE graduation_system;

-- -------------------------------------------
-- 1. 课题审查记录表
-- 记录每次审查操作的详细信息
-- -------------------------------------------
DROP TABLE IF EXISTS topic_review_record;
CREATE TABLE topic_review_record (
    review_id VARCHAR(32) NOT NULL COMMENT '审查记录ID',
    topic_id VARCHAR(32) NOT NULL COMMENT '课题ID',
    review_stage TINYINT NOT NULL COMMENT '审查阶段（1-预审 2-初审 3-终审）',
    reviewer_id VARCHAR(32) NOT NULL COMMENT '审查人ID',
    reviewer_role VARCHAR(50) NOT NULL COMMENT '审查人角色代码',
    reviewer_name VARCHAR(50) DEFAULT NULL COMMENT '审查人姓名',
    review_result TINYINT NOT NULL COMMENT '审查结果（1-通过 2-需修改 3-不通过）',
    review_opinion TEXT COMMENT '审查意见（针对单个课题）',
    is_batch_review TINYINT DEFAULT 0 COMMENT '是否批量审查（0-否 1-是）',
    batch_review_id VARCHAR(32) DEFAULT NULL COMMENT '批量审查批次ID',
    previous_status TINYINT COMMENT '审查前课题状态',
    new_status TINYINT COMMENT '审查后课题状态',
    is_modified TINYINT DEFAULT 0 COMMENT '是否被修改过（0-否 1-是）',
    modified_by VARCHAR(32) DEFAULT NULL COMMENT '修改人ID',
    modified_time DATETIME DEFAULT NULL COMMENT '修改时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除 1-已删除）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审查时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (review_id),
    KEY idx_topic_id (topic_id),
    KEY idx_reviewer_id (reviewer_id),
    KEY idx_review_stage (review_stage),
    KEY idx_review_result (review_result),
    KEY idx_batch_review_id (batch_review_id),
    KEY idx_create_time (create_time),
    CONSTRAINT fk_review_topic FOREIGN KEY (topic_id) REFERENCES topic_info (topic_id),
    CONSTRAINT fk_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES user_info (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课题审查记录表';

-- -------------------------------------------
-- 2. 综合审查意见表
-- 记录针对多个课题的综合修改意见
-- -------------------------------------------
DROP TABLE IF EXISTS topic_general_opinion;
CREATE TABLE topic_general_opinion (
    opinion_id VARCHAR(32) NOT NULL COMMENT '意见ID',
    reviewer_id VARCHAR(32) NOT NULL COMMENT '提交人ID',
    reviewer_role VARCHAR(50) NOT NULL COMMENT '提交人角色代码',
    reviewer_name VARCHAR(50) DEFAULT NULL COMMENT '提交人姓名',
    review_stage TINYINT NOT NULL COMMENT '审查阶段（1-预审 2-初审 3-终审）',
    guidance_direction VARCHAR(100) NOT NULL COMMENT '适用专业方向',
    opinion_content VARCHAR(200) NOT NULL COMMENT '综合意见内容（≤200字）',
    target_scope VARCHAR(50) DEFAULT 'DIRECTION' COMMENT '可见范围（DIRECTION-本专业方向）',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除 1-已删除）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (opinion_id),
    KEY idx_reviewer_id (reviewer_id),
    KEY idx_review_stage (review_stage),
    KEY idx_guidance_direction (guidance_direction),
    KEY idx_create_time (create_time),
    CONSTRAINT fk_opinion_reviewer FOREIGN KEY (reviewer_id) REFERENCES user_info (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='综合审查意见表';

-- -------------------------------------------
-- 3. 批量审查批次表
-- 记录批量审查的批次信息
-- -------------------------------------------
DROP TABLE IF EXISTS topic_batch_review;
CREATE TABLE topic_batch_review (
    batch_id VARCHAR(32) NOT NULL COMMENT '批次ID',
    reviewer_id VARCHAR(32) NOT NULL COMMENT '审查人ID',
    reviewer_role VARCHAR(50) NOT NULL COMMENT '审查人角色代码',
    review_stage TINYINT NOT NULL COMMENT '审查阶段（1-预审 2-初审 3-终审）',
    review_result TINYINT NOT NULL COMMENT '审查结果（1-通过 2-需修改 3-不通过）',
    review_opinion TEXT COMMENT '批量审查意见',
    topic_count INT DEFAULT 0 COMMENT '审查课题数量',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除 1-已删除）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (batch_id),
    KEY idx_reviewer_id (reviewer_id),
    KEY idx_review_stage (review_stage),
    KEY idx_create_time (create_time),
    CONSTRAINT fk_batch_reviewer FOREIGN KEY (reviewer_id) REFERENCES user_info (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='批量审查批次表';

-- ============================================
-- 审查状态说明（topic_info.review_status）
-- 0-草稿
-- 1-待预审（高职升本课题）/ 待初审（3+1/实验班课题）
-- 2-预审通过
-- 3-预审需修改
-- 4-初审通过
-- 5-初审需修改
-- 6-终审通过
-- 7-终审不通过
-- ============================================

-- ============================================
-- 审查阶段说明（review_stage）
-- 1-预审（高校教师，仅高职升本课题需要）
-- 2-初审（专业方向主管）
-- 3-终审（督导教师/高校负责人）
-- ============================================

-- ============================================
-- 审查结果说明（review_result）
-- 1-通过
-- 2-需修改
-- 3-不通过（仅终审可用）
-- ============================================
