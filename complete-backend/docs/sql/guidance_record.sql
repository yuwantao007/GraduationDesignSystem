-- =====================================================
-- 过程管理 - 指导记录模块 数据库表结构
-- 功能：企业教师项目指导记录、高校教师论文指导记录
-- @author 系统架构师
-- @version 1.0
-- @since 2026-03-16
-- =====================================================

USE graduation_system;

-- -------------------------------------------
-- 1. 指导记录表
-- -------------------------------------------
DROP TABLE IF EXISTS guidance_record;
CREATE TABLE guidance_record (
    record_id        VARCHAR(32)   NOT NULL                          COMMENT '指导记录ID（雪花ID）',
    student_id       VARCHAR(32)   NOT NULL                          COMMENT '学生用户ID',
    teacher_id       VARCHAR(32)   NOT NULL                          COMMENT '指导教师ID',
    topic_id         VARCHAR(32)   NOT NULL                          COMMENT '关联课题ID',
    guidance_type    TINYINT       NOT NULL                          COMMENT '指导类型（1-项目指导/企业教师  2-论文指导/高校教师）',
    guidance_date    DATE          NOT NULL                          COMMENT '指导日期',
    guidance_content TEXT          NOT NULL                          COMMENT '指导内容（必填）',
    guidance_method  VARCHAR(50)   DEFAULT NULL                      COMMENT '指导方式（线上/线下/邮件等）',
    duration_hours   DECIMAL(4,1)  DEFAULT NULL                      COMMENT '指导时长（小时，可选）',
    deleted          TINYINT       DEFAULT 0                         COMMENT '逻辑删除（0-未删除 1-已删除）',
    create_time      DATETIME      DEFAULT CURRENT_TIMESTAMP         COMMENT '创建时间',
    update_time      DATETIME      DEFAULT CURRENT_TIMESTAMP
                                   ON UPDATE CURRENT_TIMESTAMP       COMMENT '更新时间',
    PRIMARY KEY (record_id),
    KEY idx_student_id  (student_id),
    KEY idx_teacher_id  (teacher_id),
    KEY idx_topic_id    (topic_id),
    KEY idx_type_date   (guidance_type, guidance_date),
    CONSTRAINT fk_gr_student FOREIGN KEY (student_id) REFERENCES user_info (user_id),
    CONSTRAINT fk_gr_teacher FOREIGN KEY (teacher_id) REFERENCES user_info (user_id),
    CONSTRAINT fk_gr_topic   FOREIGN KEY (topic_id)   REFERENCES topic_info (topic_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='指导记录表';
