-- =====================================================
-- 课题流程实例映射表
-- 将业务课题ID与 Flowable 流程实例ID关联
-- 创建时间：2026-03-15
-- =====================================================

CREATE TABLE IF NOT EXISTS topic_process_instance (
  id                  VARCHAR(32)  NOT NULL                    COMMENT '主键（雪花ID）',
  topic_id            VARCHAR(32)  NOT NULL                    COMMENT '课题ID，关联 topic_info.topic_id',
  process_instance_id VARCHAR(64)  NOT NULL                    COMMENT 'Flowable 流程实例ID（ACT_RU_EXECUTION.PROC_INST_ID_）',
  process_def_key     VARCHAR(64)  NOT NULL DEFAULT 'topic_review' COMMENT '流程定义Key',
  topic_category      TINYINT      NOT NULL DEFAULT 1          COMMENT '课题大类（1-高职升本 2-3+1 3-实验班），用于判断审查路径',
  process_status      TINYINT      NOT NULL DEFAULT 0          COMMENT '流程状态（0-运行中 1-已完成 2-已终止）',
  create_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted             TINYINT      NOT NULL DEFAULT 0          COMMENT '逻辑删除（0-未删除 1-已删除）',
  PRIMARY KEY (id),
  UNIQUE KEY uk_topic_id (topic_id),
  KEY idx_process_instance_id (process_instance_id),
  KEY idx_process_status (process_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='课题流程实例映射表（topic_info ↔ Flowable ACT_* 表的桥接）';
