-- ============================================
-- 企业专业管理模块数据库设计
-- 功能：企业专业方向、专业的树型结构管理
-- 创建日期：2026-03-01
-- ============================================

USE graduation_system;

-- ============================================
-- 注意：先删除子表，再删除父表（避免外键约束错误）
-- ============================================

-- 删除专业表（子表）
DROP TABLE IF EXISTS major_info;

-- 删除专业方向表（父表）
DROP TABLE IF EXISTS major_direction_info;

-- ============================================
-- 1. 专业方向表 (major_direction_info)
-- 说明：一级分类，如"计算机科学与技术"、"软件工程"
-- ============================================
CREATE TABLE major_direction_info (
    direction_id VARCHAR(32) PRIMARY KEY COMMENT '专业方向ID',
    enterprise_id VARCHAR(32) NOT NULL COMMENT '所属企业ID',
    direction_name VARCHAR(100) NOT NULL COMMENT '专业方向名称',
    direction_code VARCHAR(50) COMMENT '专业方向代码',
    description VARCHAR(500) COMMENT '专业方向描述',
    leader_id VARCHAR(32) COMMENT '方向负责人ID（专业方向主管）',
    leader_name VARCHAR(50) COMMENT '方向负责人姓名',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态（1-启用 0-禁用）',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记（0-未删除 1-已删除）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_direction_enterprise FOREIGN KEY (enterprise_id) REFERENCES enterprise_info (enterprise_id),
    KEY idx_enterprise_id (enterprise_id),
    KEY idx_direction_name (direction_name),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专业方向信息表';

-- ============================================
-- 2. 专业表 (major_info)
-- 说明：二级分类，属于某个专业方向，如"人工智能"、"大数据技术"
-- ============================================
CREATE TABLE major_info (
    major_id VARCHAR(32) PRIMARY KEY COMMENT '专业ID',
    direction_id VARCHAR(32) NOT NULL COMMENT '所属专业方向ID',
    enterprise_id VARCHAR(32) NOT NULL COMMENT '所属企业ID',
    major_name VARCHAR(100) NOT NULL COMMENT '专业名称',
    major_code VARCHAR(50) COMMENT '专业代码',
    degree_type VARCHAR(20) COMMENT '学位类型（本科/专科）',
    education_years INT DEFAULT 4 COMMENT '学制（年）',
    description VARCHAR(500) COMMENT '专业描述',
    max_students INT DEFAULT 30 COMMENT '最大招生人数',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态（1-启用 0-禁用）',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记（0-未删除 1-已删除）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_major_direction FOREIGN KEY (direction_id) REFERENCES major_direction_info (direction_id),
    CONSTRAINT fk_major_enterprise FOREIGN KEY (enterprise_id) REFERENCES enterprise_info (enterprise_id),
    KEY idx_direction_id (direction_id),
    KEY idx_enterprise_id (enterprise_id),
    KEY idx_major_name (major_name),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专业信息表';

-- ============================================
-- 3. 修改课题表，添加专业关联
-- 注意：如果字段已存在，则跳过此步骤
-- ============================================
-- ALTER TABLE topic_info 
-- ADD COLUMN direction_id VARCHAR(32) COMMENT '专业方向ID' AFTER guidance_direction,
-- ADD COLUMN major_id VARCHAR(32) COMMENT '专业ID' AFTER direction_id,
-- ADD KEY idx_direction_id (direction_id),
-- ADD KEY idx_major_id (major_id);

-- 添加外键约束（可选，根据实际需要）
-- ALTER TABLE topic_info ADD CONSTRAINT fk_topic_direction FOREIGN KEY (direction_id) REFERENCES major_direction_info (direction_id);
-- ALTER TABLE topic_info ADD CONSTRAINT fk_topic_major FOREIGN KEY (major_id) REFERENCES major_info (major_id);

-- ============================================
-- 4. 修改用户表，添加专业方向关联（教师）
-- 注意：如果字段已存在，则跳过此步骤
-- ============================================
-- ALTER TABLE user_info 
-- ADD COLUMN direction_id VARCHAR(32) COMMENT '所属/指导专业方向ID' AFTER major,
-- ADD KEY idx_direction_id (direction_id);

-- ============================================
-- 5. 测试数据
-- ============================================

-- 5.1 IBM企业的专业方向
INSERT INTO major_direction_info (direction_id, enterprise_id, direction_name, direction_code, description, sort_order, status) VALUES
('DIR_001', 'E002', '计算机科学与技术', 'CS', '计算机科学与技术方向，包括计算机基础理论、算法、人工智能等', 1, 1),
('DIR_002', 'E002', '软件工程', 'SE', '软件工程方向，包括软件开发、软件测试、项目管理等', 2, 1),
('DIR_003', 'E002', '网络工程', 'NE', '网络工程方向，包括网络架构、网络安全、云计算等', 3, 1);

-- 5.2 计算机科学与技术方向下的专业
INSERT INTO major_info (major_id, direction_id, enterprise_id, major_name, major_code, degree_type, education_years, description, sort_order, status) VALUES
('MAJ_001', 'DIR_001', 'E002', '计算机科学与技术', 'CS01', '本科', 4, '培养计算机科学与技术领域的高级人才', 1, 1),
('MAJ_002', 'DIR_001', 'E002', '人工智能', 'CS02', '本科', 4, '培养人工智能领域的专业人才', 2, 1),
('MAJ_003', 'DIR_001', 'E002', '大数据技术', 'CS03', '本科', 4, '培养大数据分析与应用人才', 3, 1);

-- 5.3 软件工程方向下的专业
INSERT INTO major_info (major_id, direction_id, enterprise_id, major_name, major_code, degree_type, education_years, description, sort_order, status) VALUES
('MAJ_004', 'DIR_002', 'E002', '软件工程', 'SE01', '本科', 4, '培养软件开发、测试、维护的专业人才', 1, 1),
('MAJ_005', 'DIR_002', 'E002', '移动应用开发', 'SE02', '本科', 4, '培养移动端应用开发人才', 2, 1);

-- 5.4 网络工程方向下的专业
INSERT INTO major_info (major_id, direction_id, enterprise_id, major_name, major_code, degree_type, education_years, description, sort_order, status) VALUES
('MAJ_006', 'DIR_003', 'E002', '网络工程', 'NE01', '本科', 4, '培养网络设计、管理、安全的专业人才', 1, 1),
('MAJ_007', 'DIR_003', 'E002', '信息安全', 'NE02', '本科', 4, '培养信息安全领域的专业人才', 2, 1);

-- ============================================
-- 6. 权限配置
-- ============================================

-- 6.1 添加专业管理权限
INSERT IGNORE INTO permission_info (permission_id, parent_id, permission_name, permission_code, permission_type, path, icon, sort_order) VALUES 
    -- 专业管理模块
    ('700', '200', '专业管理', 'enterprise:major', 1, '/enterprise/major', 'BookOutlined', 2),
    
    -- 专业管理操作权限
    ('7001', '700', '查看专业', 'enterprise:major:view', 2, NULL, NULL, 1),
    ('7002', '700', '添加专业方向', 'enterprise:major:direction:add', 2, NULL, NULL, 2),
    ('7003', '700', '编辑专业方向', 'enterprise:major:direction:edit', 2, NULL, NULL, 3),
    ('7004', '700', '删除专业方向', 'enterprise:major:direction:delete', 2, NULL, NULL, 4),
    ('7005', '700', '添加专业', 'enterprise:major:add', 2, NULL, NULL, 5),
    ('7006', '700', '编辑专业', 'enterprise:major:edit', 2, NULL, NULL, 6),
    ('7007', '700', '删除专业', 'enterprise:major:delete', 2, NULL, NULL, 7),
    ('7008', '700', '启用/禁用', 'enterprise:major:status', 2, NULL, NULL, 8);

-- 6.2 为企业负责人分配专业管理权限
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES 
    ('700', '4', '700'),   -- 专业管理模块
    ('701', '4', '7001'),  -- 查看专业
    ('702', '4', '7002'),  -- 添加专业方向
    ('703', '4', '7003'),  -- 编辑专业方向
    ('704', '4', '7004'),  -- 删除专业方向
    ('705', '4', '7005'),  -- 添加专业
    ('706', '4', '7006'),  -- 编辑专业
    ('707', '4', '7007'),  -- 删除专业
    ('708', '4', '7008');  -- 启用/禁用

-- 6.3 为系统管理员分配专业管理权限
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES 
    ('710', '1', '700'),   -- 专业管理模块
    ('711', '1', '7001'),  -- 查看专业
    ('712', '1', '7002'),  -- 添加专业方向
    ('713', '1', '7003'),  -- 编辑专业方向
    ('714', '1', '7004'),  -- 删除专业方向
    ('715', '1', '7005'),  -- 添加专业
    ('716', '1', '7006'),  -- 编辑专业
    ('717', '1', '7007'),  -- 删除专业
    ('718', '1', '7008');  -- 启用/禁用

-- ============================================
-- 7. 验证脚本
-- ============================================

-- 查看专业树型结构
SELECT 
    d.direction_name AS '专业方向',
    m.major_name AS '专业',
    m.degree_type AS '学位',
    m.education_years AS '学制',
    d.status AS '方向状态',
    m.status AS '专业状态'
FROM major_direction_info d
LEFT JOIN major_info m ON d.direction_id = m.direction_id
WHERE d.enterprise_id = 'E002' AND d.deleted = 0
ORDER BY d.sort_order, m.sort_order;

-- ============================================
-- 执行完成提示
-- ============================================
SELECT '企业专业管理模块数据库创建完成！' AS '执行结果';
