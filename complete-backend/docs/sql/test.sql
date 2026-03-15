-- ==============================================================================
-- 测试数据汇总脚本
-- 毕业设计管理系统 - 测试数据初始化
-- 
-- 包含内容：
--   1. 初始化数据（角色、管理员账号）
--   2. 示例企业数据
--   3. 示例学校数据
--   4. 专业方向和专业测试数据
--   5. 测试用户账号
--   6. 审查教师测试账号
--   7. 系统阶段配置数据
-- 
-- @version 1.0
-- @since 2026-03-15
-- ==============================================================================

USE graduation_system;

-- ==============================================================================
-- 一、初始化基础数据
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1.1 初始化角色数据
-- ------------------------------------------------------------------------------
INSERT IGNORE INTO role_info (role_id, role_name, role_code, role_desc, sort_order) VALUES
('1', '系统管理员', 'SYSTEM_ADMIN', '系统配置、用户管理、交叉评阅分配', 1),
('2', '督导教师', 'SUPERVISOR_TEACHER', '毕业设计课题终审，课题审查最后环节', 2),
('3', '高校教师', 'UNIVERSITY_TEACHER', '课题预审、学生毕设指导、论文审查、成绩评语评定', 3),
('4', '企业负责人', 'ENTERPRISE_LEADER', '企业端毕设工作统筹管理', 4),
('5', '专业方向主管', 'MAJOR_DIRECTOR', '毕业设计课题初审，企业教师指导记录审查', 5),
('6', '企业教师', 'ENTERPRISE_TEACHER', '课题申报、学生选题确认、毕设全程指导', 6),
('7', '学生', 'STUDENT', '完成毕业设计全流程操作执行', 7);

-- ------------------------------------------------------------------------------
-- 1.2 初始化系统管理员账号
-- 账号: admin / 密码: 123456（BCrypt加密）
-- ------------------------------------------------------------------------------
INSERT IGNORE INTO user_info (user_id, username, password, real_name, user_email, employee_no, user_status) VALUES
('1', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin@example.com', 'admin', 1);

-- 为管理员分配角色
INSERT IGNORE INTO user_role (id, user_id, role_id) VALUES
('1', '1', '1');


-- ==============================================================================
-- 二、示例企业数据
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 2.1 示例企业信息
-- ------------------------------------------------------------------------------
INSERT IGNORE INTO enterprise_info (enterprise_id, enterprise_name, enterprise_code, leader_id, contact_person, contact_phone, contact_email, address, description) VALUES
('E001', '京东', 'DEMO001', 'TEST_EL_001', '张三', '13800138000', 'zhangsan@demo.com', '北京市海淀区中关村大街1号', '专注于软件开发与技术培训的科技企业'),
('E002', 'IBM', 'IBM4075', 'TEST_EL_002', '李四', '13800138000', 'lisi@demo.com', '天津市大学软件学院', '专注于培训软件开发与技术培训人员的科技企业');


-- ==============================================================================
-- 三、示例学校数据
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 3.1 示例学校信息
-- ------------------------------------------------------------------------------
INSERT IGNORE INTO school_info (school_id, school_name, school_code, contact_person, contact_phone, school_email, address, description) VALUES
('S001', '天津大学软件学院', 'TJU001', '王老师', '022-12345678', 'software@tju.edu.cn', '天津市南开区卫津路92号', '天津大学软件学院是一所培养高级软件人才的学院'),
('S002', '天津职业技术师范大学', 'TUTE001', '李老师', '022-88888888', 'info@tute.edu.cn', '天津市河西区大沽南路', '天津职业技术师范大学是一所培养职业教育师资的高校'),
('S003', '天津大学化工学院', 'TJU002', '赵老师', '022-12345679', 'chemical@tju.edu.cn', '天津市南开区卫津路92号', '天津大学化工学院是一所培养化工人才的学院');


-- ==============================================================================
-- 四、专业方向和专业测试数据
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 4.1 京东企业（E001）专业方向数据
-- ------------------------------------------------------------------------------
INSERT IGNORE INTO major_direction_info (direction_id, enterprise_id, direction_name, direction_code, description, sort_order, status) VALUES
('2028057741800542209', 'E001', '软件工程', 'DEMO001D7937', '软件工程方向，包括软件开发、软件测试、项目管理等', 1, 1),
('2030292083893346305', 'E001', '大数据技术应用', 'JD3031', '大数据技术应用方向，包括数据分析、数据挖掘等', 2, 1);

-- ------------------------------------------------------------------------------
-- 4.2 京东企业（E001）专业数据
-- ------------------------------------------------------------------------------
INSERT IGNORE INTO major_info (major_id, direction_id, enterprise_id, major_name, major_code, degree_type, education_years, description, sort_order, status) VALUES
('2028060890951094273', '2028057741800542209', 'E001', 'Java后端', 'DEMO001D7937M8161', '本科', 4, '培养Java后端开发人才', 1, 1),
('2030292083968843777', '2030292083893346305', 'E001', '大数据', 'DSJ0001', '本科', 4, '培养大数据分析与应用人才', 1, 1);

-- ------------------------------------------------------------------------------
-- 4.3 IBM企业（E002）专业方向数据
-- ------------------------------------------------------------------------------
INSERT IGNORE INTO major_direction_info (direction_id, enterprise_id, direction_name, direction_code, description, sort_order, status) VALUES
('DIR_001', 'E002', '计算机科学与技术', 'CS', '计算机科学与技术方向，包括计算机基础理论、算法、人工智能等', 1, 1),
('DIR_002', 'E002', '软件工程', 'SE', '软件工程方向，包括软件开发、软件测试、项目管理等', 2, 1),
('DIR_003', 'E002', '网络工程', 'NE', '网络工程方向，包括网络架构、网络安全、云计算等', 3, 1);

-- ------------------------------------------------------------------------------
-- 4.4 IBM企业（E002）专业数据 - 计算机科学与技术方向
-- ------------------------------------------------------------------------------
INSERT IGNORE INTO major_info (major_id, direction_id, enterprise_id, major_name, major_code, degree_type, education_years, description, sort_order, status) VALUES
('MAJ_001', 'DIR_001', 'E002', '计算机科学与技术', 'CS01', '本科', 4, '培养计算机科学与技术领域的高级人才', 1, 1),
('MAJ_002', 'DIR_001', 'E002', '人工智能', 'CS02', '本科', 4, '培养人工智能领域的专业人才', 2, 1),
('MAJ_003', 'DIR_001', 'E002', '大数据技术', 'CS03', '本科', 4, '培养大数据分析与应用人才', 3, 1);

-- ------------------------------------------------------------------------------
-- 4.5 IBM企业（E002）专业数据 - 软件工程方向
-- ------------------------------------------------------------------------------
INSERT IGNORE INTO major_info (major_id, direction_id, enterprise_id, major_name, major_code, degree_type, education_years, description, sort_order, status) VALUES
('MAJ_004', 'DIR_002', 'E002', '软件工程', 'SE01', '本科', 4, '培养软件开发、测试、维护的专业人才', 1, 1),
('MAJ_005', 'DIR_002', 'E002', '移动应用开发', 'SE02', '本科', 4, '培养移动端应用开发人才', 2, 1);

-- ------------------------------------------------------------------------------
-- 4.6 IBM企业（E002）专业数据 - 网络工程方向
-- ------------------------------------------------------------------------------
INSERT IGNORE INTO major_info (major_id, direction_id, enterprise_id, major_name, major_code, degree_type, education_years, description, sort_order, status) VALUES
('MAJ_006', 'DIR_003', 'E002', '网络工程', 'NE01', '本科', 4, '培养网络设计、管理、安全的专业人才', 1, 1),
('MAJ_007', 'DIR_003', 'E002', '信息安全', 'NE02', '本科', 4, '培养信息安全领域的专业人才', 2, 1);


-- ==============================================================================
-- 五、测试用户账号
-- 所有测试账号密码统一为: 123456
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 5.1 企业教师测试账号
-- 账号：20001（工号）/ 密码：123456
-- ------------------------------------------------------------------------------
INSERT IGNORE INTO user_info (user_id, username, password, real_name, user_email, user_phone, employee_no, user_status) VALUES
('TEST_ET_001', '20001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王企业教师', 'enterprise.teacher@example.com', '13800138001', '20001', 1);

-- 为企业教师分配角色
INSERT IGNORE INTO user_role (id, user_id, role_id) VALUES
('TEST_UR_001', 'TEST_ET_001', '6');  -- ENTERPRISE_TEACHER

-- ------------------------------------------------------------------------------
-- 5.2 企业负责人测试账号
-- 账号：20002（工号）/ 密码：123456 - 京东企业负责人
-- 账号：20006（工号）/ 密码：123456 - IBM企业负责人
-- ------------------------------------------------------------------------------
INSERT IGNORE INTO user_info (user_id, username, password, real_name, user_email, user_phone, employee_no, user_status) VALUES
('TEST_EL_001', '20002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李企业负责人', 'enterprise.leader@example.com', '13800138002', '20002', 1),
('TEST_EL_002', '20006', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '叶企业负责人', 'enterprise.leader2@example.com', '13800138006', '20006', 1);

-- 为企业负责人分配角色
INSERT IGNORE INTO user_role (id, user_id, role_id) VALUES
('TEST_UR_002', 'TEST_EL_001', '4'),  -- ENTERPRISE_LEADER（京东）
('TEST_UR_007', 'TEST_EL_002', '4');  -- ENTERPRISE_LEADER（IBM）

-- ------------------------------------------------------------------------------
-- 5.3 高校教师测试账号
-- 账号：20003（工号）/ 密码：123456
-- ------------------------------------------------------------------------------
INSERT IGNORE INTO user_info (user_id, username, password, real_name, user_email, user_phone, employee_no, user_status) VALUES
('TEST_UT_001', '20003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张高校教师', 'university.teacher@example.com', '13800138003', '20003', 1);

-- 为高校教师分配角色
INSERT IGNORE INTO user_role (id, user_id, role_id) VALUES
('TEST_UR_003', 'TEST_UT_001', '3');  -- UNIVERSITY_TEACHER

-- ------------------------------------------------------------------------------
-- 5.4 学生测试账号
-- 账号：2024001（学号）/ 密码：123456
-- ------------------------------------------------------------------------------
INSERT IGNORE INTO user_info (user_id, username, password, real_name, user_email, user_phone, student_no, user_status) VALUES
('TEST_ST_001', '2024001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '赵学生', 'student@example.com', '13800138004', '2024001', 1);

-- 为学生分配角色
INSERT IGNORE INTO user_role (id, user_id, role_id) VALUES
('TEST_UR_004', 'TEST_ST_001', '7');  -- STUDENT


-- ==============================================================================
-- 六、审查教师测试账号
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 6.1 专业方向主管测试账号
-- 账号：20004（工号）/ 密码：123456
-- 角色：MAJOR_DIRECTOR / 职能：课题初审
-- ------------------------------------------------------------------------------
INSERT IGNORE INTO user_info (user_id, username, password, real_name, user_email, user_phone, employee_no, title, user_status, deleted) VALUES
('TEST_MD_001', '20004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '刘专业主管', 'major.director@example.com', '13800138004', '20004', '专业方向主管', 1, 0);

-- 为专业方向主管分配角色
INSERT IGNORE INTO user_role (id, user_id, role_id) VALUES
('TEST_UR_005', 'TEST_MD_001', '5');  -- MAJOR_DIRECTOR

-- ------------------------------------------------------------------------------
-- 6.2 督导教师测试账号
-- 账号：20005（工号）/ 密码：123456
-- 角色：SUPERVISOR_TEACHER / 职能：课题终审
-- ------------------------------------------------------------------------------
INSERT IGNORE INTO user_info (user_id, username, password, real_name, user_email, user_phone, employee_no, title, user_status, deleted) VALUES
('TEST_ST_002', '20005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '陈督导教师', 'supervisor.teacher@example.com', '13800138005', '20005', '督导教师', 1, 0);

-- 为督导教师分配角色
INSERT IGNORE INTO user_role (id, user_id, role_id) VALUES
('TEST_UR_006', 'TEST_ST_002', '2');  -- SUPERVISOR_TEACHER


-- ==============================================================================
-- 七、系统阶段配置数据
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 7.1 系统阶段配置（固定4条，不允许动态增删）
-- ------------------------------------------------------------------------------
INSERT IGNORE INTO `system_phase_config` (`phase_id`, `phase_code`, `phase_name`, `phase_order`, `phase_description`, `phase_icon`, `phase_color`) VALUES
(1, 'TOPIC_DECLARATION',  '课题申报阶段', 1, '企业教师创建课题、三级审查流程（预审→初审→终审）', 'FormOutlined',      '#1890ff'),
(2, 'TOPIC_SELECTION',    '课题双选阶段', 2, '学生选报课题、教师确认人选、负责人审查双选结果',     'TeamOutlined',      '#52c41a'),
(3, 'TOPIC_GUIDANCE',     '课题指导阶段', 3, '项目指导、论文指导、文档提交、开题/中期答辩',       'EditOutlined',      '#faad14'),
(4, 'GRADUATION_DEFENSE', '毕设答辩阶段', 4, '答辩资格审查、正式/二次答辩、成绩评定、文档打印',   'TrophyOutlined',    '#f5222d');


-- ==============================================================================
-- 八、验证脚本
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 8.1 查看所有测试用户
-- ------------------------------------------------------------------------------
SELECT 
    u.user_id, 
    u.username, 
    u.real_name, 
    u.employee_no, 
    u.student_no, 
    r.role_name, 
    r.role_code
FROM user_info u
INNER JOIN user_role ur ON u.user_id = ur.user_id
INNER JOIN role_info r ON ur.role_id = r.role_id
WHERE u.user_id LIKE 'TEST_%' OR u.username = 'admin'
ORDER BY r.sort_order;

-- ------------------------------------------------------------------------------
-- 8.2 查看企业专业树型结构
-- ------------------------------------------------------------------------------
SELECT 
    e.enterprise_name AS '企业',
    d.direction_name AS '专业方向',
    m.major_name AS '专业',
    m.degree_type AS '学位',
    m.education_years AS '学制'
FROM enterprise_info e
LEFT JOIN major_direction_info d ON e.enterprise_id = d.enterprise_id AND d.deleted = 0
LEFT JOIN major_info m ON d.direction_id = m.direction_id AND m.deleted = 0
WHERE e.deleted = 0
ORDER BY e.enterprise_name, d.sort_order, m.sort_order;

-- ------------------------------------------------------------------------------
-- 8.3 查看学校列表
-- ------------------------------------------------------------------------------
SELECT school_name, school_code, contact_person, contact_phone 
FROM school_info 
WHERE deleted = 0;

-- ------------------------------------------------------------------------------
-- 8.4 查看系统阶段配置
-- ------------------------------------------------------------------------------
SELECT phase_code, phase_name, phase_order, phase_description 
FROM system_phase_config 
WHERE is_deleted = 0 
ORDER BY phase_order;


-- ==============================================================================
-- 测试账号汇总表
-- ==============================================================================
-- +----------------+----------+----------+---------------------+
-- | 角色           | 账号     | 密码     | 说明                |
-- +----------------+----------+----------+---------------------+
-- | 系统管理员     | admin    | 123456   | 系统配置和管理      |
-- | 企业教师       | 20001    | 123456   | 课题申报指导        |
-- | 企业负责人     | 20002    | 123456   | 京东企业管理审批    |
-- | 企业负责人     | 20006    | 123456   | IBM企业管理审批     |
-- | 高校教师       | 20003    | 123456   | 课题预审指导        |
-- | 学生           | 2024001  | 123456   | 毕设全流程          |
-- | 专业方向主管   | 20004    | 123456   | 课题初审            |
-- | 督导教师       | 20005    | 123456   | 课题终审            |
-- +----------------+----------+----------+---------------------+

-- ==============================================================================
-- 执行完成提示
-- ==============================================================================
SELECT '测试数据初始化完成！' AS '执行结果';
