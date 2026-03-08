-- ============================================
-- 教师配对管理模块数据库设计
-- 功能：高校教师与企业教师/专业方向的配对关联
-- 创建日期：2026-03-08
-- 说明：
--   第一层（粗粒度）：university_teacher_major — 高校教师 → 专业方向
--   第二层（细粒度）：teacher_relationship    — 高校教师 ↔ 企业教师
--   查找优先级：teacher_relationship > university_teacher_major
-- ============================================

USE graduation_system;

-- ======================================================
-- 1. 高校教师 → 专业方向 分配表（粗粒度）
-- 说明：一个高校教师对接一个企业的某专业方向，该方向下
--       所有企业教师默认由该高校教师负责预审、论文指导等。
--       每届（cohort）独立配置，互不影响。
-- ======================================================
CREATE TABLE IF NOT EXISTS university_teacher_major (
    id              VARCHAR(32)  NOT NULL  COMMENT '主键',
    univ_teacher_id VARCHAR(32)  NOT NULL  COMMENT '高校教师 user_id',
    direction_id    VARCHAR(32)  NOT NULL  COMMENT '专业方向ID (major_direction_info)',
    enterprise_id   VARCHAR(32)  NOT NULL  COMMENT '所属企业ID（冗余加速查询）',
    cohort          VARCHAR(20)  NOT NULL  COMMENT '届别（如 2026届），来自 system_phase_record',
    is_enabled      TINYINT      DEFAULT 1 COMMENT '是否启用（1-启用 0-停用）',
    remark          VARCHAR(200) DEFAULT NULL COMMENT '备注',
    deleted         TINYINT      DEFAULT 0 COMMENT '逻辑删除（0-未删除 1-已删除）',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_teacher_dir_cohort (univ_teacher_id, direction_id, cohort),
    KEY idx_direction (direction_id),
    KEY idx_enterprise (enterprise_id),
    KEY idx_cohort (cohort),
    KEY idx_univ_teacher (univ_teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='高校教师-专业方向分配表（粗粒度）';

-- ======================================================
-- 2. 高校教师 ↔ 企业教师 精确配对表（细粒度）
-- 说明：用于特殊情况下的精确配对，优先级高于第一层。
--       例如某企业教师的课题方向跨专业，需由不同高校教师负责。
-- ======================================================
CREATE TABLE IF NOT EXISTS teacher_relationship (
    relation_id           VARCHAR(32)  NOT NULL  COMMENT '主键',
    univ_teacher_id       VARCHAR(32)  NOT NULL  COMMENT '高校教师 user_id',
    enterprise_teacher_id VARCHAR(32)  NOT NULL  COMMENT '企业教师 user_id',
    enterprise_id         VARCHAR(32)  NOT NULL  COMMENT '企业ID（冗余加速查询）',
    direction_id          VARCHAR(32)  DEFAULT NULL COMMENT '所属专业方向（辅助描述，可选）',
    cohort                VARCHAR(20)  NOT NULL  COMMENT '届别',
    relation_type         VARCHAR(20)  DEFAULT 'DIRECT' COMMENT '配对类型（DIRECT-直接配对 ASSIST-辅助支持）',
    is_enabled            TINYINT      DEFAULT 1 COMMENT '是否启用（1-启用 0-停用）',
    remark                VARCHAR(200) DEFAULT NULL COMMENT '备注',
    deleted               TINYINT      DEFAULT 0 COMMENT '逻辑删除（0-未删除 1-已删除）',
    create_time           DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time           DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (relation_id),
    UNIQUE KEY uk_pair_cohort (univ_teacher_id, enterprise_teacher_id, cohort),
    KEY idx_enterprise_teacher (enterprise_teacher_id),
    KEY idx_enterprise (enterprise_id),
    KEY idx_cohort (cohort),
    KEY idx_univ_teacher (univ_teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='高校教师-企业教师精确配对表（细粒度）';

-- ======================================================
-- 3. 验证表结构
-- ======================================================
SELECT '===== university_teacher_major 表结构 =====' AS '验证信息';
DESCRIBE university_teacher_major;

SELECT '===== teacher_relationship 表结构 =====' AS '验证信息';
DESCRIBE teacher_relationship;

SELECT '教师配对管理模块表结构创建完成' AS '执行结果';
