-- =====================================================
-- 教师指派记录表
-- 用于企业负责人为"校外协同开发课题"中选学生指派企业指导教师
-- @author 系统架构师
-- @version 1.0
-- @since 2026-03-14
-- =====================================================

USE graduation_system;

DROP TABLE IF EXISTS teacher_assignment;
CREATE TABLE teacher_assignment (
    assignment_id       VARCHAR(32)   NOT NULL                              COMMENT '指派ID（雪花ID）',
    student_id          VARCHAR(32)   NOT NULL                              COMMENT '学生用户ID',
    topic_id            VARCHAR(32)   NOT NULL                              COMMENT '课题ID',
    selection_id        VARCHAR(32)   NOT NULL                              COMMENT '关联选报记录ID',
    assigned_teacher_id VARCHAR(32)   NOT NULL                              COMMENT '指派教师ID（企业教师）',
    assigned_by         VARCHAR(32)   NOT NULL                              COMMENT '指派人ID（企业负责人）',
    assign_time         DATETIME      DEFAULT CURRENT_TIMESTAMP             COMMENT '指派时间',
    cancel_time         DATETIME      DEFAULT NULL                          COMMENT '取消指派时间',
    assign_status       TINYINT       DEFAULT 1                             COMMENT '指派状态（1-已指派 0-已取消）',
    deleted             TINYINT       DEFAULT 0                             COMMENT '逻辑删除（0-未删除 1-已删除）',
    create_time         DATETIME      DEFAULT CURRENT_TIMESTAMP             COMMENT '创建时间',
    update_time         DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (assignment_id),
    KEY idx_student_id       (student_id),
    KEY idx_topic_id         (topic_id),
    KEY idx_selection_id     (selection_id),
    KEY idx_assigned_teacher (assigned_teacher_id),
    CONSTRAINT fk_assign_student  FOREIGN KEY (student_id)          REFERENCES user_info (user_id),
    CONSTRAINT fk_assign_topic    FOREIGN KEY (topic_id)            REFERENCES topic_info (topic_id),
    CONSTRAINT fk_assign_selection FOREIGN KEY (selection_id)       REFERENCES topic_selection (selection_id),
    CONSTRAINT fk_assign_teacher  FOREIGN KEY (assigned_teacher_id) REFERENCES user_info (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师指派记录表';
