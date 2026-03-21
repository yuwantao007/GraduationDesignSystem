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
-- 用途：学生结构化填写开题报告（单表存储，支持查看/修改/打印）
-- 约束：一个学生只有一份开题报告（student_id唯一）
-- 说明：
--   1) 不保存最终上传文件ID
--   2) 不走审核流程（无 review_* 字段）
--   3) “进度及预期结果”按单表文本字段存储（不拆明细子表）
-- =============================================
CREATE TABLE IF NOT EXISTS `opening_report` (
    `report_id`         VARCHAR(32)   NOT NULL COMMENT '报告ID',
    `student_id`        VARCHAR(32)   NOT NULL COMMENT '学生ID（唯一）',
    `topic_id`          VARCHAR(32)   NOT NULL COMMENT '课题ID',
    `arrangement_id`    VARCHAR(32)   DEFAULT NULL COMMENT '对应答辩安排ID（可选）',

    `student_name`      VARCHAR(50)   NOT NULL COMMENT '姓名快照',
    `major_name`        VARCHAR(100)  DEFAULT NULL COMMENT '专业快照',
    `class_name`        VARCHAR(100)  DEFAULT NULL COMMENT '班级快照',
    `topic_title`       VARCHAR(300)  NOT NULL COMMENT '题目快照',
    `advisor_names`     VARCHAR(200)  DEFAULT NULL COMMENT '指导教师姓名（可多名，逗号分隔）',
    `report_date`       DATE          DEFAULT NULL COMMENT '报告日期',

    `research_status`       TEXT      DEFAULT NULL COMMENT '一-1 国内外研究现状',
    `purpose_significance`  TEXT      DEFAULT NULL COMMENT '一-2 研究目的、意义',
    `research_content`      TEXT      DEFAULT NULL COMMENT '一-3 研究内容',
    `innovation_points`     TEXT      DEFAULT NULL COMMENT '一-4 课题研究创新点',
    `problems_to_solve`     TEXT      DEFAULT NULL COMMENT '一-5 拟解决问题',

    `progress_expectation`  LONGTEXT  DEFAULT NULL COMMENT '二 进度及预期结果（单表文本）',
    `current_conditions`    LONGTEXT  DEFAULT NULL COMMENT '完成题目的现有条件',
    `advisor_opinion`       TEXT      DEFAULT NULL COMMENT '审查意见（文本）',
    `college_opinion`       TEXT      DEFAULT NULL COMMENT '学院意见（文本）',

    `status`            TINYINT       NOT NULL DEFAULT 0 COMMENT '状态: 0=草稿, 1=已定稿',
    `submit_time`       DATETIME      DEFAULT NULL COMMENT '提交/定稿时间',

    `is_deleted`        TINYINT       DEFAULT 0 COMMENT '逻辑删除: 0=正常, 1=删除',
    `create_time`       DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`report_id`),
    UNIQUE KEY `uk_student_id` (`student_id`),
    KEY `idx_topic_id` (`topic_id`),
    KEY `idx_arrangement_id` (`arrangement_id`),
    KEY `idx_status` (`status`),
    KEY `idx_submit_time` (`submit_time`)
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
-- 3. opening_report 采用结构化单表字段存储，不依赖最终上传文件ID
-- 4. opening_report 不启用审核流，仅保留草稿/定稿状态
-- 5. opening_report “进度及预期结果”使用 progress_expectation 文本字段存储
-- 6. opening_task_book 的 content 字段支持富文本，也可选择上传附件（document_id）
-- =============================================

