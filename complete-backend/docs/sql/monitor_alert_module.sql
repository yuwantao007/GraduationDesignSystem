-- ============================================
-- 质量监控与智能预警模块数据库设计
-- 功能：系统预警记录管理、质量监控权限配置
-- 创建日期：2026-03-14
-- ============================================

USE graduation_system;

-- ============================================
-- 1. 系统预警记录表 (alert_info)
-- ============================================
CREATE TABLE IF NOT EXISTS alert_info (
    alert_id      VARCHAR(32)   NOT NULL                                          COMMENT '预警ID（雪花算法）',
    alert_type    VARCHAR(50)   NOT NULL                                          COMMENT '预警类型：STUDENT_NOT_SELECTED/TOPIC_NO_APPLICANT/REVIEW_BACKLOG/PHASE_DEADLINE_NEAR/SELECTION_RATE_LOW',
    alert_level   TINYINT       NOT NULL DEFAULT 1                                COMMENT '预警级别：1-提示 2-警告 3-严重',
    alert_title   VARCHAR(200)  NOT NULL                                          COMMENT '预警标题',
    alert_content VARCHAR(1000)                                                   COMMENT '预警详情',
    target_id     VARCHAR(32)                                                     COMMENT '关联对象ID（课题ID/学生ID/阶段代码）',
    target_type   VARCHAR(50)                                                     COMMENT '关联对象类型：TOPIC/STUDENT/PHASE',
    is_read       TINYINT       NOT NULL DEFAULT 0                                COMMENT '是否已读：0-未读 1-已读',
    is_resolved   TINYINT       NOT NULL DEFAULT 0                                COMMENT '是否已处理：0-未处理 1-已处理',
    resolved_at   DATETIME                                                        COMMENT '处理时间',
    deleted       TINYINT       NOT NULL DEFAULT 0                                COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP                COMMENT '创建时间',
    update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (alert_id),
    KEY idx_alert_type   (alert_type),
    KEY idx_alert_level  (alert_level),
    KEY idx_is_read      (is_read),
    KEY idx_is_resolved  (is_resolved),
    KEY idx_create_time  (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统预警记录表';

-- ============================================
-- 2. 权限配置：质量监控模块（ID 800-803）
-- ============================================

-- 2.1 添加监控权限节点（注意：必须设置 permission_status=1 才能被查询到）
INSERT IGNORE INTO permission_info (permission_id, parent_id, permission_name, permission_code, permission_type, path, icon, sort_order, permission_status, deleted) VALUES
    ('800', '0',   '质量监控',   'monitor:view',            1, '/monitor',         'BarChartOutlined', 10, 1, 0),
    ('801', '800', '监控仪表盘', 'monitor:dashboard:view',   2,  NULL,               NULL,               1, 1, 0),
    ('802', '800', '预警中心',   'monitor:alert:view',       2,  NULL,               NULL,               2, 1, 0),
    ('803', '800', '处理预警',   'monitor:alert:resolve',    2,  NULL,               NULL,               3, 1, 0);

-- 2.1.1 修复已插入但缺少 permission_status 的数据
UPDATE permission_info SET permission_status = 1, deleted = 0 
WHERE permission_id IN ('800', '801', '802', '803') AND permission_status IS NULL;

-- 2.2 为系统管理员（role_id=1）分配全部监控权限
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES
    ('rp_800', '1', '800'),
    ('rp_801', '1', '801'),
    ('rp_802', '1', '802'),
    ('rp_803', '1', '803');

-- 2.3 为企业负责人（role_id=4）分配查看权限（不含处理预警）
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES
    ('rp_810', '4', '800'),
    ('rp_811', '4', '801'),
    ('rp_812', '4', '802');

-- ============================================
-- 3. 验证脚本
-- ============================================

-- 查看监控权限配置
SELECT p.permission_id, p.permission_name, p.permission_code, r.role_name
FROM permission_info p
JOIN role_permission rp ON p.permission_id = rp.permission_id
JOIN role_info r ON rp.role_id = r.role_id
WHERE p.permission_id IN ('800','801','802','803')
ORDER BY p.permission_id, r.role_id;

SELECT '质量监控与智能预警模块数据库初始化完成！' AS '执行结果';
