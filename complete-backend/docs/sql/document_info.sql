-- =============================================
-- 文档管理模块数据库脚本
-- 创建时间: 2026-03-15
-- 功能描述: 创建文档信息表和文档访问日志表
-- =============================================

-- 1. 创建文档信息表
CREATE TABLE IF NOT EXISTS `document_info` (
    `document_id`     VARCHAR(32)   NOT NULL COMMENT '文档ID',
    `student_id`      VARCHAR(32)   NOT NULL COMMENT '所属学生ID',
    `topic_id`        VARCHAR(32)   NOT NULL COMMENT '关联课题ID',
    `document_type`   TINYINT       NOT NULL COMMENT '文档类型: 1=项目代码, 2=论文文档, 3=开题报告, 4=中期检查表, 5=教师批注文档',
    `file_name`       VARCHAR(200)  NOT NULL COMMENT '原始文件名',
    `file_path`       VARCHAR(500)  NOT NULL COMMENT 'MinIO存储路径',
    `file_size`       BIGINT        NOT NULL COMMENT '文件大小(字节)',
    `file_suffix`     VARCHAR(20)   NOT NULL COMMENT '文件后缀(pdf/doc/docx/zip等)',
    `uploader_id`     VARCHAR(32)   NOT NULL COMMENT '上传人ID',
    `version`         INT           DEFAULT 1 COMMENT '版本号（同类型文档递增）',
    `is_latest`       TINYINT       DEFAULT 1 COMMENT '是否最新版本: 1=是, 0=否',
    `remark`          VARCHAR(500)  DEFAULT NULL COMMENT '备注（如：根据第X次指导意见修改）',
    `is_deleted`      TINYINT       DEFAULT 0 COMMENT '逻辑删除: 0=正常, 1=删除',
    `create_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`document_id`),
    KEY `idx_student_topic` (`student_id`, `topic_id`),
    KEY `idx_doc_type` (`document_type`),
    KEY `idx_uploader` (`uploader_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文档信息表';

-- 2. 创建文档访问日志表
CREATE TABLE IF NOT EXISTS `document_access_log` (
    `log_id`          VARCHAR(32)   NOT NULL COMMENT '日志ID',
    `document_id`     VARCHAR(32)   NOT NULL COMMENT '文档ID',
    `accessor_id`     VARCHAR(32)   NOT NULL COMMENT '访问人ID',
    `access_type`     TINYINT       NOT NULL COMMENT '访问类型: 1=预览, 2=下载',
    `access_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
    `ip_address`      VARCHAR(50)   DEFAULT NULL COMMENT '访问IP地址',
    PRIMARY KEY (`log_id`),
    KEY `idx_doc_id` (`document_id`),
    KEY `idx_accessor_id` (`accessor_id`),
    KEY `idx_access_time` (`access_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文档访问日志表';

