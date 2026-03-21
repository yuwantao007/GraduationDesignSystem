-- =============================================
-- 开题报告表优化脚本（增量迁移）
-- 目标：
-- 1) 不保存最终上传文件ID
-- 2) 不启用审核流程
-- 3) 不拆“进度及预期结果”明细子表，改为单表文本字段
-- =============================================

USE graduation_system;

-- 0) 强烈建议先备份
-- CREATE TABLE opening_report_bak_20260321 AS SELECT * FROM opening_report;

-- 1) 删除旧流程字段（如果是 MySQL 8.0 可使用 IF EXISTS）
ALTER TABLE opening_report
    DROP COLUMN document_id,
    DROP COLUMN review_status,
    DROP COLUMN review_comment,
    DROP COLUMN reviewer_id,
    DROP COLUMN review_time;

-- 2) 新增结构化字段（为避免历史数据失败，先允许 NULL）
ALTER TABLE opening_report
    ADD COLUMN student_name      VARCHAR(50)   NULL COMMENT '姓名快照' AFTER arrangement_id,
    ADD COLUMN major_name        VARCHAR(100)  NULL COMMENT '专业快照' AFTER student_name,
    ADD COLUMN class_name        VARCHAR(100)  NULL COMMENT '班级快照' AFTER major_name,
    ADD COLUMN topic_title       VARCHAR(300)  NULL COMMENT '题目快照' AFTER class_name,
    ADD COLUMN advisor_names     VARCHAR(200)  NULL COMMENT '指导教师姓名（可多名，逗号分隔）' AFTER topic_title,
    ADD COLUMN report_date       DATE          NULL COMMENT '报告日期' AFTER advisor_names,

    ADD COLUMN research_status       TEXT      NULL COMMENT '一-1 国内外研究现状' AFTER report_date,
    ADD COLUMN purpose_significance  TEXT      NULL COMMENT '一-2 研究目的、意义' AFTER research_status,
    ADD COLUMN research_content      TEXT      NULL COMMENT '一-3 研究内容' AFTER purpose_significance,
    ADD COLUMN innovation_points     TEXT      NULL COMMENT '一-4 课题研究创新点' AFTER research_content,
    ADD COLUMN problems_to_solve     TEXT      NULL COMMENT '一-5 拟解决问题' AFTER innovation_points,

    ADD COLUMN progress_expectation  LONGTEXT  NULL COMMENT '二 进度及预期结果（单表文本）' AFTER problems_to_solve,
    ADD COLUMN current_conditions    LONGTEXT  NULL COMMENT '完成题目的现有条件' AFTER progress_expectation,
    ADD COLUMN advisor_opinion       TEXT      NULL COMMENT '审查意见（文本）' AFTER current_conditions,
    ADD COLUMN college_opinion       TEXT      NULL COMMENT '学院意见（文本）' AFTER advisor_opinion,

    ADD COLUMN status            TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0=草稿, 1=已定稿' AFTER college_opinion;

-- 3) 补充索引
ALTER TABLE opening_report
    ADD INDEX idx_status (status),
    ADD INDEX idx_submit_time (submit_time);

-- 4) 回填基础快照字段（按当前关联数据回填）
UPDATE opening_report r
LEFT JOIN user_info u ON u.user_id = r.student_id
LEFT JOIN topic_info t ON t.topic_id = r.topic_id
SET r.student_name = COALESCE(r.student_name, u.real_name),
    r.topic_title = COALESCE(r.topic_title, t.topic_title)
WHERE r.is_deleted = 0;

-- 5) 如确认历史数据完整，可再收紧非空约束（可选）
-- ALTER TABLE opening_report
--     MODIFY COLUMN student_name VARCHAR(50) NOT NULL,
--     MODIFY COLUMN topic_title  VARCHAR(300) NOT NULL;

-- 6) 校验
SELECT report_id, student_id, topic_id, student_name, topic_title, status, submit_time
FROM opening_report
ORDER BY update_time DESC
LIMIT 20;
