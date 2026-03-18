-- =============================================
-- 中期检查表
-- 创建时间: 2026-03-17
-- =============================================

CREATE TABLE `midterm_check` (
  `check_id`              VARCHAR(32)   NOT NULL COMMENT '检查表ID',
  `student_id`            VARCHAR(32)   NOT NULL COMMENT '学生ID（每个学生一份）',
  `topic_id`              VARCHAR(32)   NOT NULL COMMENT '课题ID',
  `arrangement_id`        VARCHAR(32)   DEFAULT NULL COMMENT '对应中期答辩安排ID（关联defense_arrangement）',
  `enterprise_teacher_id` VARCHAR(32)   NOT NULL COMMENT '填写企业教师ID',
  `completion_status`     TEXT          DEFAULT NULL COMMENT '完成情况（企业教师填写）',
  `existing_problems`     TEXT          DEFAULT NULL COMMENT '存在问题',
  `next_plan`             TEXT          DEFAULT NULL COMMENT '下一步计划',
  `document_id`           VARCHAR(32)   DEFAULT NULL COMMENT '中期检查表附件（关联document_info）',
  `submit_status`         TINYINT       DEFAULT 0 COMMENT '提交状态: 0=草稿, 1=已提交',
  `review_status`         TINYINT       DEFAULT 0 COMMENT '高校教师审查状态: 0=未审, 1=合格, 2=不合格',
  `review_comment`        TEXT          DEFAULT NULL COMMENT '高校教师审查意见',
  `reviewer_id`           VARCHAR(32)   DEFAULT NULL COMMENT '审查人（高校教师ID）',
  `review_time`           DATETIME      DEFAULT NULL COMMENT '审查时间',
  `is_deleted`            TINYINT       DEFAULT 0 COMMENT '逻辑删除: 0=正常, 1=删除',
  `create_time`           DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`           DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`check_id`),
  UNIQUE KEY `uk_student_id` (`student_id`),
  KEY `idx_topic_id` (`topic_id`),
  KEY `idx_enterprise_teacher` (`enterprise_teacher_id`),
  KEY `idx_reviewer` (`reviewer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='中期检查表';

