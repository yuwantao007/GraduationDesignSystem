-- =============================================
-- 开题答辩管理模块数据库脚本
-- 创建时间: 2026-03-17
-- 功能描述: 创建答辩安排表、开题报告表、开题任务书表
-- 业务流程: 企业负责人设置答辩安排 → 企业教师编辑任务书 → 学生提交开题报告 → 企业教师审查
-- =============================================

-- =============================================
-- 表1：答辩安排表 defense_arrangement
-- 用途：企业负责人创建/管理各类型答辩（开题/中期/正式/二次）的时间、地点、答辩小组
-- =============================================
CREATE TABLE IF NOT EXISTS `defense_arrangement` (
    `arrangement_id`    VARCHAR(32)   NOT NULL COMMENT '安排ID',
    `defense_type`      TINYINT       NOT NULL COMMENT '答辩类型: 1=开题, 2=中期, 3=正式, 4=二次',
    `topic_category`    VARCHAR(20)   NOT NULL COMMENT '课题类别（高职升本/3+1/实验班）',
    `major_id`          VARCHAR(32)   NOT NULL COMMENT '专业ID',
    `defense_time`      DATETIME      NOT NULL COMMENT '答辩时间',
    `defense_location`  VARCHAR(200)  NOT NULL COMMENT '答辩地点',
    `panel_teachers`    JSON          NOT NULL COMMENT '答辩小组教师ID列表（JSON数组，固定3人：组长+2位答辩老师）',
    `deadline`          DATETIME      DEFAULT NULL COMMENT '报告提交截止时间（仅开题/中期使用）',
    `cohort`            VARCHAR(20)   NOT NULL COMMENT '毕业届别（如：2026届）',
    `enterprise_id`     VARCHAR(32)   NOT NULL COMMENT '所属企业ID',
    `creator_id`        VARCHAR(32)   NOT NULL COMMENT '创建人ID（企业负责人）',
    `status`            TINYINT       DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `remark`            VARCHAR(500)  DEFAULT NULL COMMENT '备注说明',
    `is_deleted`        TINYINT       DEFAULT 0 COMMENT '逻辑删除: 0=正常, 1=删除',
    `create_time`       DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`arrangement_id`),
    KEY `idx_type_category` (`defense_type`, `topic_category`),
    KEY `idx_major_id` (`major_id`),
    KEY `idx_enterprise` (`enterprise_id`),
    KEY `idx_cohort` (`cohort`),
    KEY `idx_defense_time` (`defense_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答辩安排表';

-- =============================================
-- 表2：开题报告表 opening_report
-- 用途：记录学生的开题报告提交与审查情况
-- 约束：一个学生只有一份开题报告（student_id唯一）
-- =============================================
CREATE TABLE IF NOT EXISTS `opening_report` (
    `report_id`         VARCHAR(32)   NOT NULL COMMENT '报告ID',
    `student_id`        VARCHAR(32)   NOT NULL COMMENT '学生ID（一个学生只有一份）',
    `topic_id`          VARCHAR(32)   NOT NULL COMMENT '课题ID',
    `arrangement_id`    VARCHAR(32)   DEFAULT NULL COMMENT '对应答辩安排ID',
    `document_id`       VARCHAR(32)   DEFAULT NULL COMMENT '报告文件ID（关联document_info）',
    `submit_time`       DATETIME      DEFAULT NULL COMMENT '提交时间',
    `review_status`     TINYINT       DEFAULT 0 COMMENT '审查状态: 0=未提交, 1=已提交待审, 2=通过, 3=不合格',
    `review_comment`    TEXT          DEFAULT NULL COMMENT '审查意见',
    `reviewer_id`       VARCHAR(32)   DEFAULT NULL COMMENT '审查人ID（企业教师）',
    `review_time`       DATETIME      DEFAULT NULL COMMENT '审查时间',
    `is_deleted`        TINYINT       DEFAULT 0 COMMENT '逻辑删除: 0=正常, 1=删除',
    `create_time`       DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`report_id`),
    UNIQUE KEY `uk_student_id` (`student_id`),
    KEY `idx_topic_id` (`topic_id`),
    KEY `idx_arrangement_id` (`arrangement_id`),
    KEY `idx_review_status` (`review_status`),
    KEY `idx_reviewer_id` (`reviewer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='开题报告表';

-- =============================================
-- 表3：开题任务书表 opening_task_book
-- 用途：企业教师为学生编写的开题任务书
-- 约束：一个学生只有一份任务书（student_id唯一）
-- =============================================
CREATE TABLE IF NOT EXISTS `opening_task_book` (
    `task_book_id`      VARCHAR(32)   NOT NULL COMMENT '任务书ID',
    `student_id`        VARCHAR(32)   NOT NULL COMMENT '学生ID（一个学生一份任务书）',
    `topic_id`          VARCHAR(32)   NOT NULL COMMENT '课题ID',
    `teacher_id`        VARCHAR(32)   NOT NULL COMMENT '编辑教师ID（企业教师）',
    `content`           LONGTEXT      DEFAULT NULL COMMENT '任务书正文（富文本HTML）',
    `document_id`       VARCHAR(32)   DEFAULT NULL COMMENT '任务书附件ID（关联document_info）',
    `is_deleted`        TINYINT       DEFAULT 0 COMMENT '逻辑删除: 0=正常, 1=删除',
    `create_time`       DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`task_book_id`),
    UNIQUE KEY `uk_student_id` (`student_id`),
    KEY `idx_topic_id` (`topic_id`),
    KEY `idx_teacher_id` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='开题任务书表';

-- =============================================
-- 说明：
-- 1. defense_arrangement 表支持所有类型答辩（开题/中期/正式/二次），通过 defense_type 区分
-- 2. panel_teachers 使用 JSON 数组存储答辩小组教师ID，支持灵活配置
-- 3. opening_report 的 document_id 关联 document_info 表，复用文档管理模块
-- 4. opening_task_book 的 content 字段支持富文本，也可选择上传附件（document_id）
-- 5. 审查状态流转：0(未提交) → 1(已提交待审) → 2(通过)/3(不合格)
-- 6. 不合格的学生直接进入二次答辩流程
-- =============================================

