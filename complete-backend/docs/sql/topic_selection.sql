-- =====================================================
-- 双选管理 - 学生选报子模块 数据库表结构
-- 功能：学生选报课题、查看选报结果
-- @author 系统架构师
-- @version 1.0
-- @since 2026-03-08
-- =====================================================

USE graduation_system;

-- -------------------------------------------
-- 1. 课题选报记录表
-- -------------------------------------------
DROP TABLE IF EXISTS topic_selection;
CREATE TABLE topic_selection (
    selection_id     VARCHAR(32)   NOT NULL            COMMENT '选报ID（雪花ID）',
    student_id       VARCHAR(32)   NOT NULL            COMMENT '学生用户ID',
    topic_id         VARCHAR(32)   NOT NULL            COMMENT '课题ID',
    selection_reason TEXT          NOT NULL            COMMENT '选报理由（必填）',
    selection_status TINYINT       DEFAULT 0           COMMENT '选报状态（0-待确认 1-中选 2-落选）',
    apply_time       DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '选报时间',
    confirm_time     DATETIME      DEFAULT NULL        COMMENT '确认时间（企业教师确认）',
    confirmed_by     VARCHAR(32)   DEFAULT NULL        COMMENT '确认人ID（企业教师）',
    deleted          TINYINT       DEFAULT 0           COMMENT '逻辑删除（0-未删除 1-已删除）',
    create_time      DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (selection_id),
    KEY idx_student_id (student_id),
    KEY idx_topic_id (topic_id),
    KEY idx_selection_status (selection_status),
    CONSTRAINT fk_sel_student FOREIGN KEY (student_id) REFERENCES user_info (user_id),
    CONSTRAINT fk_sel_topic   FOREIGN KEY (topic_id)   REFERENCES topic_info (topic_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课题选报记录表';
