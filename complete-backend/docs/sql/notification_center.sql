-- ============================================
-- 站内消息中心模块数据库脚本
-- 功能：模板管理、消息收件箱、消息操作日志（仅站内闭环）
-- 创建日期：2026-03-21
-- ============================================

USE graduation_system;

-- ============================================
-- 1. 消息模板表
-- ============================================
CREATE TABLE IF NOT EXISTS notification_template (
    template_id      VARCHAR(32)   NOT NULL COMMENT '模板ID',
    template_code    VARCHAR(100)  NOT NULL COMMENT '模板编码（唯一）',
    template_name    VARCHAR(100)  NOT NULL COMMENT '模板名称',
    category         VARCHAR(30)   NOT NULL COMMENT '消息分类：REVIEW/DEFENSE/GUIDANCE/SELECTION/MIDTERM/SYSTEM',
    default_level    TINYINT       NOT NULL DEFAULT 1 COMMENT '默认级别：1-普通 2-提醒 3-重要 4-紧急',
    title_template   VARCHAR(200)  NOT NULL COMMENT '标题模板',
    content_template VARCHAR(1000) NOT NULL COMMENT '内容模板',
    is_enabled       TINYINT       NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用 0-禁用',
    remark           VARCHAR(500)           DEFAULT NULL COMMENT '备注',
    is_deleted       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (template_id),
    UNIQUE KEY uk_template_code (template_code),
    KEY idx_category_enabled (category, is_enabled),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内消息模板表';

-- ============================================
-- 2. 站内消息表（每接收人一条）
-- ============================================
CREATE TABLE IF NOT EXISTS notification_message (
    message_id       VARCHAR(32)   NOT NULL COMMENT '消息ID',
    receiver_id      VARCHAR(32)   NOT NULL COMMENT '接收人ID',
    category         VARCHAR(30)   NOT NULL COMMENT '消息分类',
    level            TINYINT       NOT NULL DEFAULT 1 COMMENT '消息级别：1-普通 2-提醒 3-重要 4-紧急',
    title            VARCHAR(200)  NOT NULL COMMENT '消息标题',
    content          VARCHAR(1000) NOT NULL COMMENT '消息内容',
    business_type    VARCHAR(60)            DEFAULT NULL COMMENT '业务类型',
    business_id      VARCHAR(64)            DEFAULT NULL COMMENT '业务ID',
    business_route   VARCHAR(255)           DEFAULT NULL COMMENT '业务跳转路由',
    dedup_key        VARCHAR(255)           DEFAULT NULL COMMENT '去重键',
    message_status   TINYINT       NOT NULL DEFAULT 0 COMMENT '消息状态：0-未读 1-已读 2-已处理',
    read_time        DATETIME               DEFAULT NULL COMMENT '已读时间',
    processed_time   DATETIME               DEFAULT NULL COMMENT '处理时间',
    expire_time      DATETIME               DEFAULT NULL COMMENT '过期时间',
    is_deleted       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (message_id),
    UNIQUE KEY uk_receiver_dedup_deleted (receiver_id, dedup_key, is_deleted),
    KEY idx_receiver_status (receiver_id, message_status, is_deleted),
    KEY idx_category_level (category, level),
    KEY idx_expire_time (expire_time),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内消息表';

-- ============================================
-- 3. 消息操作日志表
-- ============================================
CREATE TABLE IF NOT EXISTS notification_action_log (
    log_id         VARCHAR(32)   NOT NULL COMMENT '日志ID',
    message_id     VARCHAR(32)   NOT NULL COMMENT '消息ID',
    operator_id    VARCHAR(32)   NOT NULL COMMENT '操作人ID',
    action_type    VARCHAR(30)   NOT NULL COMMENT '操作类型：READ/READ_ALL/PROCESS/DELETE',
    action_source  VARCHAR(20)   NOT NULL DEFAULT 'USER' COMMENT '操作来源：USER/SYSTEM',
    is_deleted     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (log_id),
    KEY idx_message_id (message_id),
    KEY idx_operator_id (operator_id),
    KEY idx_action_type (action_type),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内消息操作日志表';

-- ============================================
-- 4. 初始化模板数据
-- ============================================
INSERT INTO notification_template
(template_id, template_code, template_name, category, default_level, title_template, content_template, is_enabled, remark)
VALUES
(REPLACE(UUID(), '-', ''), 'TOPIC_REVIEW_RESULT', '课题审查结果通知', 'REVIEW', 3,
 '课题《{topicTitle}》审查结果更新',
 '审查阶段：{reviewStage}；审查结果：{reviewResult}；审查人：{reviewerName}。请及时查看并处理。',
 1, '课题审查意见触发'),

(REPLACE(UUID(), '-', ''), 'GUIDANCE_RECORD_CREATED', '指导记录新增通知', 'GUIDANCE', 2,
 '你收到一条新的{guidanceType}记录',
 '教师 {teacherName} 已为课题《{topicTitle}》新增指导记录，请及时查看并跟进。',
 1, '指导记录触发'),

(REPLACE(UUID(), '-', ''), 'TOPIC_SELECTION_RESULT', '双选结果通知', 'SELECTION', 3,
 '课题双选结果已更新',
 '课题《{topicTitle}》双选结果：{result}（确认教师：{teacherName}）。请及时查看。',
 1, '双选确认/拒绝触发'),

(REPLACE(UUID(), '-', ''), 'DEFENSE_ARRANGEMENT_NOTICE', '答辩安排通知', 'DEFENSE', 3,
 '{defenseType}安排{action}',
 '你的{defenseType}安排已{action}，时间：{defenseTime}，地点：{defenseLocation}。请按时参加。',
 1, '答辩安排创建/更新触发'),

(REPLACE(UUID(), '-', ''), 'MIDTERM_REVIEW_RESULT', '中期审查结果通知', 'MIDTERM', 3,
 '中期检查审查结果已更新',
 '你的中期检查审查结果：{reviewResult}。审查意见：{reviewComment}。请及时查看。',
 1, '中期审查触发')
ON DUPLICATE KEY UPDATE
    template_name = VALUES(template_name),
    category = VALUES(category),
    default_level = VALUES(default_level),
    title_template = VALUES(title_template),
    content_template = VALUES(content_template),
    is_enabled = VALUES(is_enabled),
    remark = VALUES(remark),
    is_deleted = 0;

SELECT 'notification_center.sql 执行完成' AS result;
