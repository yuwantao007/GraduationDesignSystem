-- ============================================
-- 学校管理模块数据库表结构
-- @author 系统架构师
-- @version 1.0
-- @since 2026-02-22
-- ============================================

USE graduation_system;

-- -------------------------------------------
-- 1. 学校信息表
-- -------------------------------------------
DROP TABLE IF EXISTS school_info;
CREATE TABLE school_info (
    school_id VARCHAR(32) NOT NULL COMMENT '学校ID',
    school_name VARCHAR(100) NOT NULL COMMENT '学校名称',
    school_code VARCHAR(50) DEFAULT NULL COMMENT '学校编码',
    address VARCHAR(200) DEFAULT NULL COMMENT '详细地址',
    contact_person VARCHAR(50) DEFAULT NULL COMMENT '联系人',
    contact_phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    school_email VARCHAR(100) DEFAULT NULL COMMENT '学校邮箱',
    description VARCHAR(500) DEFAULT NULL COMMENT '学校简介',
    school_status TINYINT DEFAULT 1 COMMENT '状态（0-禁用 1-正常）',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除 1-已删除）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (school_id),
    UNIQUE KEY uk_school_name (school_name),
    UNIQUE KEY uk_school_code (school_code),
    KEY idx_school_status (school_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学校信息表';

-- ============================================
-- 初始化测试数据
-- ============================================

-- 初始化示例学校
INSERT INTO school_info (school_id, school_name, school_code, contact_person, contact_phone, school_email, address, description) VALUES
('S001', '天津大学软件学院', 'TJU001', '王老师', '022-12345678', 'software@tju.edu.cn', '天津市南开区卫津路92号', '天津大学软件学院是一所培养高级软件人才的学院');
INSERT INTO school_info (school_id, school_name, school_code, contact_person, contact_phone, school_email, address, description) VALUES
('S002', '天津职业技术师范大学', 'TUTE001', '李老师', '022-88888888', 'info@tute.edu.cn', '天津市河西区大沽南路', '天津职业技术师范大学是一所培养职业教育师资的高校');
