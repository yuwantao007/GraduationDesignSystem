-- ============================================
-- 课题管理模块数据库表结构
-- @author 系统架构师
-- @version 1.0
-- @since 2026-02-21
-- ============================================

USE graduation_system;

-- -------------------------------------------
-- 1. 企业信息表
-- -------------------------------------------
DROP TABLE IF EXISTS enterprise_info;
CREATE TABLE enterprise_info (
    enterprise_id VARCHAR(32) NOT NULL COMMENT '企业ID',
    enterprise_name VARCHAR(100) NOT NULL COMMENT '企业名称',
    enterprise_code VARCHAR(50) DEFAULT NULL COMMENT '企业编码',
    contact_person VARCHAR(50) DEFAULT NULL COMMENT '联系人',
    contact_phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    contact_email VARCHAR(100) DEFAULT NULL COMMENT '联系邮箱',
    address VARCHAR(200) DEFAULT NULL COMMENT '企业地址',
    description VARCHAR(500) DEFAULT NULL COMMENT '企业简介',
    enterprise_status TINYINT DEFAULT 1 COMMENT '状态（0-禁用 1-正常）',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除 1-已删除）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (enterprise_id),
    UNIQUE KEY uk_enterprise_name (enterprise_name),
    UNIQUE KEY uk_enterprise_code (enterprise_code),
    KEY idx_enterprise_status (enterprise_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='企业信息表';

-- -------------------------------------------
-- 2. 课题信息表
-- -------------------------------------------
DROP TABLE IF EXISTS topic_info;
CREATE TABLE topic_info (
    topic_id VARCHAR(32) NOT NULL COMMENT '课题ID',
    topic_title VARCHAR(100) NOT NULL COMMENT '课题名称/题目（不超过50字符，中文算2个）',
    topic_category TINYINT NOT NULL COMMENT '课题大类（1-高职升本 2-3+1 3-实验班）',
    topic_type TINYINT NOT NULL COMMENT '课题类型（1-设计 2-论文）',
    topic_source TINYINT NOT NULL COMMENT '课题来源（1-校内 2-校外协同开发）',
    applicable_school VARCHAR(100) DEFAULT NULL COMMENT '适用学校（3+1/实验班必填）',
    enterprise_id VARCHAR(32) NOT NULL COMMENT '归属企业ID',
    guidance_direction VARCHAR(100) DEFAULT NULL COMMENT '指导方向/专业',
    background_significance TEXT DEFAULT NULL COMMENT '选题背景与意义（≥150字）',
    content_summary TEXT DEFAULT NULL COMMENT '课题内容简述（≥150字）',
    professional_training TEXT DEFAULT NULL COMMENT '专业知识综合训练情况（≥100字）',
    development_environment JSON DEFAULT NULL COMMENT '开发环境(工具)，JSON格式',
    workload_weeks INT DEFAULT 17 COMMENT '工作量总周数',
    workload_detail JSON DEFAULT NULL COMMENT '工作量明细，JSON格式',
    schedule_requirements JSON DEFAULT NULL COMMENT '任务与进度要求，JSON格式',
    topic_references JSON DEFAULT NULL COMMENT '主要参考文献，JSON格式',
    start_date DATE DEFAULT NULL COMMENT '起止日期-开始',
    end_date DATE DEFAULT NULL COMMENT '起止日期-结束',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    creator_id VARCHAR(32) NOT NULL COMMENT '创建人(企业教师ID)',
    college_leader_sign VARCHAR(500) DEFAULT NULL COMMENT '学院负责人签名（图片URL）',
    college_leader_sign_time DATETIME DEFAULT NULL COMMENT '学院负责人签名时间',
    enterprise_leader_sign VARCHAR(500) DEFAULT NULL COMMENT '企业负责人签名（图片URL）',
    enterprise_leader_sign_time DATETIME DEFAULT NULL COMMENT '企业负责人签名时间',
    enterprise_teacher_sign VARCHAR(500) DEFAULT NULL COMMENT '企业指导教师签名（图片URL）',
    enterprise_teacher_sign_time DATETIME DEFAULT NULL COMMENT '企业指导教师签名时间',
    review_status TINYINT DEFAULT 0 COMMENT '审查状态（0-草稿 1-待预审 2-预审通过 3-预审需修改 4-初审通过 5-初审需修改 6-终审通过 7-终审不通过）',
    is_submitted TINYINT DEFAULT 0 COMMENT '是否已提交（0-未提交 1-已提交）',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除 1-已删除）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (topic_id),
    UNIQUE KEY uk_topic_title (topic_title),
    KEY idx_topic_category (topic_category),
    KEY idx_topic_type (topic_type),
    KEY idx_enterprise_id (enterprise_id),
    KEY idx_creator_id (creator_id),
    KEY idx_review_status (review_status),
    KEY idx_is_submitted (is_submitted),
    CONSTRAINT fk_topic_enterprise FOREIGN KEY (enterprise_id) REFERENCES enterprise_info (enterprise_id),
    CONSTRAINT fk_topic_creator FOREIGN KEY (creator_id) REFERENCES user_info (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课题信息表';

-- ============================================
-- 初始化测试数据
-- ============================================

-- 初始化示例企业
INSERT INTO enterprise_info (enterprise_id, enterprise_name, enterprise_code, contact_person, contact_phone, contact_email, address, description) VALUES
('E001', '示例科技有限公司', 'DEMO001', '张三', '13800138000', 'zhangsan@demo.com', '北京市海淀区中关村大街1号', '专注于软件开发与技术培训的科技企业');
INSERT INTO enterprise_info (enterprise_id, enterprise_name, enterprise_code, contact_person, contact_phone, contact_email, address, description) VALUES
    ('E002', 'IBM', 'DEMO002', '李四', '13800138000', 'lisi@demo.com', '天津市大学软件学院', '专注于培训软件开发与技术培训人员的科技企业');
