-- ============================================
-- 毕业设计管理系统 - 权限配置汇总
-- 整合日期：2026-03-15
-- 说明：按功能模块分类整理所有权限配置SQL
-- 执行顺序：请在建表语句执行完成后执行本脚本
-- ============================================

USE graduation_system;

-- ============================================
-- 一、初始化角色数据
-- ============================================

INSERT INTO role_info (role_id, role_name, role_code, role_desc, sort_order) VALUES
('1', '系统管理员', 'SYSTEM_ADMIN', '系统配置、用户管理、交叉评阅分配', 1),
('2', '督导教师', 'SUPERVISOR_TEACHER', '毕业设计课题终审，课题审查最后环节', 2),
('3', '高校教师', 'UNIVERSITY_TEACHER', '课题预审、学生毕设指导、论文审查、成绩评语评定', 3),
('4', '企业负责人', 'ENTERPRISE_LEADER', '企业端毕设工作统筹管理', 4),
('5', '专业方向主管', 'MAJOR_DIRECTOR', '毕业设计课题初审，企业教师指导记录审查', 5),
('6', '企业教师', 'ENTERPRISE_TEACHER', '课题申报、学生选题确认、毕设全程指导', 6),
('7', '学生', 'STUDENT', '完成毕业设计全流程操作执行', 7);

-- ============================================
-- 二、基础权限配置（用户管理模块）
-- ============================================

INSERT INTO permission_info (permission_id, parent_id, permission_name, permission_code, permission_type, path, icon, sort_order) VALUES
-- 一级菜单
('100', '0', '仪表盘', 'dashboard:view', 1, '/dashboard', 'DashboardOutlined', 1),
('200', '0', '用户管理', 'user:manage', 1, '/user', 'UserOutlined', 2),
('300', '0', '个人中心', 'profile:view', 1, '/profile', 'UserOutlined', 3),
-- 用户管理子菜单
('201', '200', '用户列表', 'user:view', 1, '/user', 'TeamOutlined', 1),
('202', '200', '角色权限', 'role:view', 1, '/user/role', 'SafetyCertificateOutlined', 2),
-- 用户管理按钮权限
('2011', '201', '创建用户', 'user:create', 2, NULL, NULL, 1),
('2012', '201', '编辑用户', 'user:edit', 2, NULL, NULL, 2),
('2013', '201', '删除用户', 'user:delete', 2, NULL, NULL, 3),
('2014', '201', '重置密码', 'user:reset-password', 2, NULL, NULL, 4),
('2021', '202', '创建角色', 'role:create', 2, NULL, NULL, 1),
('2022', '202', '编辑角色', 'role:edit', 2, NULL, NULL, 2),
('2023', '202', '删除角色', 'role:delete', 2, NULL, NULL, 3),
('2024', '202', '分配权限', 'role:assign-permission', 2, NULL, NULL, 4);

-- 系统管理员基础权限
INSERT INTO role_permission (id, role_id, permission_id) VALUES
('1', '1', '100'),
('2', '1', '200'),
('3', '1', '300'),
('4', '1', '201'),
('5', '1', '202'),
('6', '1', '2011'),
('7', '1', '2012'),
('8', '1', '2013'),
('9', '1', '2014'),
('10', '1', '2021'),
('11', '1', '2022'),
('12', '1', '2023'),
('13', '1', '2024');

-- ============================================
-- 三、课题管理权限
-- ============================================

INSERT INTO permission_info (permission_id, parent_id, permission_name, permission_code, permission_type, path, icon, sort_order) VALUES
('400', '0', '课题管理', 'topic:manage', 1, '/topic', 'FileTextOutlined', 4),
('401', '400', '课题列表', 'topic:view', 1, '/topic/list', 'UnorderedListOutlined', 1),
('4011', '401', '创建课题', 'topic:create', 2, NULL, NULL, 1),
('4012', '401', '编辑课题', 'topic:edit', 2, NULL, NULL, 2),
('4013', '401', '删除课题', 'topic:delete', 2, NULL, NULL, 3),
('4014', '401', '提交课题', 'topic:submit', 2, NULL, NULL, 4),
('4015', '401', '撤回课题', 'topic:withdraw', 2, NULL, NULL, 5),
('4016', '401', '签名审核', 'topic:sign', 2, NULL, NULL, 6);

-- 系统管理员课题管理权限
INSERT INTO role_permission (id, role_id, permission_id) VALUES
('14', '1', '400'),
('15', '1', '401'),
('16', '1', '4011'),
('17', '1', '4012'),
('18', '1', '4013'),
('19', '1', '4014'),
('20', '1', '4015'),
('21', '1', '4016');

-- 企业教师课题管理权限
INSERT INTO role_permission (id, role_id, permission_id) VALUES
('100', '6', '100'),
('101', '6', '300'),
('102', '6', '400'),
('103', '6', '401'),
('104', '6', '4011'),
('105', '6', '4012'),
('106', '6', '4013'),
('107', '6', '4014'),
('108', '6', '4015');

-- 企业负责人课题管理权限
INSERT INTO role_permission (id, role_id, permission_id) VALUES
('200', '4', '100'),
('201', '4', '300'),
('202', '4', '400'),
('203', '4', '401'),
('204', '4', '4016');

-- 高校教师课题管理权限
INSERT INTO role_permission (id, role_id, permission_id) VALUES
('300', '3', '100'),
('301', '3', '300'),
('302', '3', '400'),
('303', '3', '401'),
('304', '3', '4016');

-- 专业方向主管课题管理权限
INSERT INTO role_permission (id, role_id, permission_id) VALUES
('400', '5', '100'),
('401', '5', '300'),
('402', '5', '400'),
('403', '5', '401'),
('404', '5', '4016');

-- 督导教师课题管理权限
INSERT INTO role_permission (id, role_id, permission_id) VALUES
('500', '2', '100'),
('501', '2', '300'),
('502', '2', '400'),
('503', '2', '401'),
('504', '2', '4016');

-- 学生课题管理权限
INSERT INTO role_permission (id, role_id, permission_id) VALUES
('600', '7', '100'),
('601', '7', '300'),
('602', '7', '400'),
('603', '7', '401');

-- ============================================
-- 四、企业管理权限
-- ============================================

INSERT IGNORE INTO permission_info (permission_id, parent_id, permission_name, permission_code, permission_type, path, icon, sort_order) VALUES 
('500', '0', '企业管理', 'enterprise', 1, '/enterprise', 'BankOutlined', 5),
('501', '500', '企业列表', 'enterprise:view', 1, '/enterprise', 'UnorderedListOutlined', 1),
('5011', '501', '创建企业', 'enterprise:create', 2, NULL, NULL, 1),
('5012', '501', '编辑企业', 'enterprise:edit', 2, NULL, NULL, 2),
('5013', '501', '删除企业', 'enterprise:delete', 2, NULL, NULL, 3),
('5014', '501', '管理企业状态', 'enterprise:status', 2, NULL, NULL, 4),
('5015', '501', '导出企业数据', 'enterprise:export', 2, NULL, NULL, 5);

-- 系统管理员企业管理权限（注：ID 500-506与双选管理冲突，使用别名）
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES 
('e500', '1', '500'),
('e501', '1', '501'),
('e502', '1', '5011'),
('e503', '1', '5012'),
('e504', '1', '5013'),
('e505', '1', '5014'),
('e506', '1', '5015');

-- ============================================
-- 五、学校管理权限
-- ============================================

INSERT IGNORE INTO permission_info (permission_id, parent_id, permission_name, permission_code, permission_type, path, icon, sort_order) VALUES 
('520', '0', '学校管理', 'school', 1, '/school', 'ReadOutlined', 6),
('521', '520', '学校列表', 'school:view', 1, '/school', 'UnorderedListOutlined', 1),
('5211', '521', '创建学校', 'school:create', 2, NULL, NULL, 1),
('5212', '521', '编辑学校', 'school:edit', 2, NULL, NULL, 2),
('5213', '521', '删除学校', 'school:delete', 2, NULL, NULL, 3),
('5214', '521', '管理学校状态', 'school:status', 2, NULL, NULL, 4),
('5215', '521', '导出学校数据', 'school:export', 2, NULL, NULL, 5);

INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES 
('520', '1', '520'),
('521', '1', '521'),
('522', '1', '5211'),
('523', '1', '5212'),
('524', '1', '5213'),
('525', '1', '5214'),
('526', '1', '5215');

-- ============================================
-- 六、双选管理权限
-- ============================================

-- 6.1 学生选报权限
INSERT IGNORE INTO permission_info (permission_id, parent_id, permission_name, permission_code, permission_type, path, icon, sort_order) VALUES
('1100', '0', '双选管理', 'selection:manage', 1, '/topic-selection', 'SelectOutlined', 50),
('1101', '1100', '可选课题', 'selection:available', 1, '/topic-selection/list', NULL, 1),
('1102', '1100', '选报课题', 'selection:apply', 2, NULL, NULL, 2),
('1103', '1100', '删除选报', 'selection:delete', 2, NULL, NULL, 3),
('1104', '1100', '我的选报', 'selection:my', 1, '/topic-selection/my', NULL, 4);

-- 6.2 企业教师确认权限（放在企业管理下）
INSERT IGNORE INTO permission_info (permission_id, parent_id, permission_name, permission_code, permission_type, path, sort_order) VALUES
('505', '500', '待确认选报', 'selection:teacher:list',    1, '/topic-selection/teacher', 5),
('506', '500', '确认人选',   'selection:teacher:confirm', 2, NULL,                       6),
('507', '500', '拒绝人选',   'selection:teacher:reject',  2, NULL,                       7),
('508', '500', '导出确认学生','selection:teacher:export', 2, NULL,                       8);

-- 6.3 企业负责人审核权限（放在企业管理下）
INSERT IGNORE INTO permission_info (permission_id, parent_id, permission_name, permission_code, permission_type, path, sort_order) VALUES
('510', '500', '双选审核概览', 'selection:leader:overview', 1, '/topic-selection/leader', 10),
('511', '500', '导出选题信息', 'selection:leader:export',   2, NULL,                      11),
('512', '500', '指派教师',     'selection:leader:assign',   2, NULL,                      12);

-- 6.4 高校教师查看选题权限（放在企业管理下）
INSERT IGNORE INTO permission_info (permission_id, parent_id, permission_name, permission_code, permission_type, path, sort_order) VALUES
('513', '500', '指导学生选题', 'selection:univ:view',   1, '/topic-selection/univ-teacher', 13),
('514', '500', '导出选题结果', 'selection:univ:export',  2, NULL,                            14);

-- 学生双选权限
SET @student_role_id = (SELECT role_id FROM role_info WHERE role_code = 'STUDENT' AND deleted = 0 LIMIT 1);
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @student_role_id, '1100' FROM DUAL WHERE @student_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @student_role_id, '1101' FROM DUAL WHERE @student_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @student_role_id, '1102' FROM DUAL WHERE @student_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @student_role_id, '1103' FROM DUAL WHERE @student_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @student_role_id, '1104' FROM DUAL WHERE @student_role_id IS NOT NULL;

-- 企业教师双选权限
SET @et_role_id = (SELECT role_id FROM role_info WHERE role_code = 'ENTERPRISE_TEACHER' AND deleted = 0 LIMIT 1);
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @et_role_id, '1100' FROM DUAL WHERE @et_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @et_role_id, '505' FROM DUAL WHERE @et_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @et_role_id, '506' FROM DUAL WHERE @et_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @et_role_id, '507' FROM DUAL WHERE @et_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @et_role_id, '508' FROM DUAL WHERE @et_role_id IS NOT NULL;

-- 企业负责人双选权限
SET @el_role_id = (SELECT role_id FROM role_info WHERE role_code = 'ENTERPRISE_LEADER' AND deleted = 0 LIMIT 1);
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @el_role_id, '1100' FROM DUAL WHERE @el_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @el_role_id, '510' FROM DUAL WHERE @el_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @el_role_id, '511' FROM DUAL WHERE @el_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @el_role_id, '512' FROM DUAL WHERE @el_role_id IS NOT NULL;

-- 高校教师双选权限
SET @ut_role_id = (SELECT role_id FROM role_info WHERE role_code = 'UNIVERSITY_TEACHER' AND deleted = 0 LIMIT 1);
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @ut_role_id, '1100' FROM DUAL WHERE @ut_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @ut_role_id, '513' FROM DUAL WHERE @ut_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @ut_role_id, '514' FROM DUAL WHERE @ut_role_id IS NOT NULL;

-- 系统管理员双选权限
SET @admin_role_id = (SELECT role_id FROM role_info WHERE role_code = 'SYSTEM_ADMIN' AND deleted = 0 LIMIT 1);
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @admin_role_id, '1100' FROM DUAL WHERE @admin_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @admin_role_id, '1101' FROM DUAL WHERE @admin_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @admin_role_id, '1104' FROM DUAL WHERE @admin_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @admin_role_id, '505' FROM DUAL WHERE @admin_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @admin_role_id, '510' FROM DUAL WHERE @admin_role_id IS NOT NULL;
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @admin_role_id, '513' FROM DUAL WHERE @admin_role_id IS NOT NULL;

-- ============================================
-- 七、教师配对管理权限
-- ============================================

INSERT INTO permission_info (permission_id, parent_id, permission_name, permission_code, permission_type, path, icon, sort_order) VALUES
('540', '0', '教师配对管理', 'teacher_relation:manage', 1, '/system/teacher-relation', 'TeamOutlined', 9),
('541', '540', '方向分配', 'teacher_relation:major:view', 1, '/system/teacher-relation/major', 'ApartmentOutlined', 1),
('542', '540', '精确配对', 'teacher_relation:pair:view', 1, '/system/teacher-relation/pair', 'SwapOutlined', 2),
('543', '540', '覆盖检查', 'teacher_relation:coverage:view', 1, '/system/teacher-relation/coverage', 'CheckCircleOutlined', 3),
('5411', '541', '新增方向分配', 'teacher_relation:major:create', 2, NULL, NULL, 1),
('5412', '541', '编辑方向分配', 'teacher_relation:major:edit', 2, NULL, NULL, 2),
('5413', '541', '删除方向分配', 'teacher_relation:major:delete', 2, NULL, NULL, 3),
('5421', '542', '新增精确配对', 'teacher_relation:pair:create', 2, NULL, NULL, 1),
('5422', '542', '编辑精确配对', 'teacher_relation:pair:edit', 2, NULL, NULL, 2),
('5423', '542', '删除精确配对', 'teacher_relation:pair:delete', 2, NULL, NULL, 3);

-- 系统管理员教师配对权限
INSERT INTO role_permission (id, role_id, permission_id) VALUES
('900', '1', '540'),
('901', '1', '541'),
('902', '1', '542'),
('903', '1', '543'),
('904', '1', '5411'),
('905', '1', '5412'),
('906', '1', '5413'),
('907', '1', '5421'),
('908', '1', '5422'),
('909', '1', '5423');

-- 企业负责人教师配对权限（只读）
INSERT INTO role_permission (id, role_id, permission_id) VALUES
('910', '4', '540'),
('911', '4', '541'),
('912', '4', '542'),
('913', '4', '543');

-- 高校教师教师配对权限
INSERT INTO role_permission (id, role_id, permission_id) VALUES
('920', '3', '540'),
('921', '3', '543');

-- ============================================
-- 八、课题审查权限
-- ============================================

INSERT IGNORE INTO permission_info (permission_id, parent_id, permission_name, permission_code, permission_type, path, icon, sort_order) VALUES 
('600', '0', '课题审查', 'topic:review', 1, '/topic/review', 'AuditOutlined', 6),
('601', '600', '待审课题', 'topic:review:pending', 1, '/topic/review/pending', 'UnorderedListOutlined', 1),
('602', '600', '审查历史', 'topic:review:history', 1, '/topic/review/history', 'HistoryOutlined', 2),
('603', '600', '综合意见', 'topic:review:opinion', 1, '/topic/review/opinion', 'CommentOutlined', 3),
('604', '600', '审查统计', 'topic:review:stats', 1, '/topic/review/stats', 'BarChartOutlined', 4),
('6011', '601', '预审课题', 'topic:review:pre', 2, NULL, NULL, 1),
('6012', '601', '初审课题', 'topic:review:init', 2, NULL, NULL, 2),
('6013', '601', '终审课题', 'topic:review:final', 2, NULL, NULL, 3),
('6014', '601', '批量审批', 'topic:review:batch', 2, NULL, NULL, 4),
('6015', '601', '修改审批', 'topic:review:modify', 2, NULL, NULL, 5),
('6021', '602', '查看历史', 'topic:review:history:view', 2, NULL, NULL, 1),
('6031', '603', '提交综合意见', 'topic:review:opinion:submit', 2, NULL, NULL, 1),
('6032', '603', '查看综合意见', 'topic:review:opinion:view', 2, NULL, NULL, 2),
('6033', '603', '删除综合意见', 'topic:review:opinion:delete', 2, NULL, NULL, 3),
('6041', '604', '查看教师统计', 'topic:review:stats:teacher', 2, NULL, NULL, 1),
('6042', '604', '检查提交资格', 'topic:review:stats:check', 2, NULL, NULL, 2);

-- 系统管理员审查权限
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES 
('r600', '1', '600'),
('r601', '1', '601'),
('r602', '1', '602'),
('r603', '1', '603'),
('r604', '1', '604'),
('r605', '1', '6011'),
('r606', '1', '6012'),
('r607', '1', '6013'),
('r608', '1', '6014'),
('r609', '1', '6015'),
('r610', '1', '6021'),
('r611', '1', '6031'),
('r612', '1', '6032'),
('r613', '1', '6033'),
('r614', '1', '6041'),
('r615', '1', '6042');

-- 高校教师审查权限（预审 + 终审3+1/实验班）
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES 
('r620', '3', '600'),
('r621', '3', '601'),
('r622', '3', '602'),
('r623', '3', '6011'),
('r624', '3', '6013'),
('r625', '3', '6014'),
('r626', '3', '6015'),
('r627', '3', '6021'),
('r628', '3', '6032');

-- 专业方向主管审查权限（初审）
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES 
('r630', '5', '600'),
('r631', '5', '601'),
('r632', '5', '602'),
('r633', '5', '603'),
('r634', '5', '6012'),
('r635', '5', '6014'),
('r636', '5', '6015'),
('r637', '5', '6021'),
('r638', '5', '6031'),
('r639', '5', '6032'),
('r640', '5', '6033');

-- 督导教师审查权限（终审高职升本）
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES 
('r650', '2', '600'),
('r651', '2', '601'),
('r652', '2', '602'),
('r653', '2', '603'),
('r654', '2', '604'),
('r655', '2', '6013'),
('r656', '2', '6014'),
('r657', '2', '6015'),
('r658', '2', '6021'),
('r659', '2', '6031'),
('r660', '2', '6032'),
('r661', '2', '6033'),
('r662', '2', '6041'),
('r663', '2', '6042');

-- 企业教师审查权限（仅查看）
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES 
('r670', '6', '600'),
('r671', '6', '602'),
('r672', '6', '6021'),
('r673', '6', '6032'),
('r674', '6', '6042');

-- 企业负责人审查权限（查看统计）
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES 
('r680', '4', '600'),
('r681', '4', '604'),
('r682', '4', '6041');

-- ============================================
-- 九、专业管理权限
-- ============================================

INSERT IGNORE INTO permission_info (permission_id, parent_id, permission_name, permission_code, permission_type, path, icon, sort_order) VALUES 
('700', '200', '专业管理', 'enterprise:major', 1, '/enterprise/major', 'BookOutlined', 2),
('7001', '700', '查看专业', 'enterprise:major:view', 2, NULL, NULL, 1),
('7002', '700', '添加专业方向', 'enterprise:major:direction:add', 2, NULL, NULL, 2),
('7003', '700', '编辑专业方向', 'enterprise:major:direction:edit', 2, NULL, NULL, 3),
('7004', '700', '删除专业方向', 'enterprise:major:direction:delete', 2, NULL, NULL, 4),
('7005', '700', '添加专业', 'enterprise:major:add', 2, NULL, NULL, 5),
('7006', '700', '编辑专业', 'enterprise:major:edit', 2, NULL, NULL, 6),
('7007', '700', '删除专业', 'enterprise:major:delete', 2, NULL, NULL, 7),
('7008', '700', '启用/禁用', 'enterprise:major:status', 2, NULL, NULL, 8);

-- 企业负责人专业管理权限
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES 
('m700', '4', '700'),
('m701', '4', '7001'),
('m702', '4', '7002'),
('m703', '4', '7003'),
('m704', '4', '7004'),
('m705', '4', '7005'),
('m706', '4', '7006'),
('m707', '4', '7007'),
('m708', '4', '7008');

-- 系统管理员专业管理权限
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES 
('m710', '1', '700'),
('m711', '1', '7001'),
('m712', '1', '7002'),
('m713', '1', '7003'),
('m714', '1', '7004'),
('m715', '1', '7005'),
('m716', '1', '7006'),
('m717', '1', '7007'),
('m718', '1', '7008');

-- ============================================
-- 十、质量监控权限（含阶段管理）
-- ============================================

INSERT IGNORE INTO permission_info (permission_id, parent_id, permission_name, permission_code, permission_type, path, icon, sort_order, permission_status, deleted) VALUES
('800', '0',   '质量监控',   'monitor:view',            1, '/monitor',         'BarChartOutlined', 10, 1, 0),
('801', '800', '监控仪表盘', 'monitor:dashboard:view',   2,  NULL,               NULL,               1, 1, 0),
('8011', '801', '初始化阶段', 'phase:init',              2,  NULL,               NULL,               1, 1, 0),
('8012', '801', '切换阶段',   'phase:switch',            2,  NULL,               NULL,               2, 1, 0),
('802', '800', '预警中心',   'monitor:alert:view',       2,  NULL,               NULL,               2, 1, 0),
('803', '800', '处理预警',   'monitor:alert:resolve',    2,  NULL,               NULL,               3, 1, 0);

-- 系统管理员监控权限
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES
('mo800', '1', '800'),
('mo801', '1', '801'),
('mo8011', '1', '8011'),
('mo8012', '1', '8012'),
('mo802', '1', '802'),
('mo803', '1', '803');

-- 企业负责人监控权限（不含处理预警）
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES
('mo810', '4', '800'),
('mo811', '4', '801'),
('mo812', '4', '802');

-- 督导教师监控权限（只读）
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES
('mo820', '2', '800'),
('mo821', '2', '801');

-- 高校教师监控权限（只读）
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES
('mo830', '3', '800'),
('mo831', '3', '801');

-- 专业方向主管监控权限（只读）
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES
('mo840', '5', '800'),
('mo841', '5', '801');

-- 企业教师监控权限（只读）
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES
('mo850', '6', '800'),
('mo851', '6', '801');

-- 学生监控权限（只读）
INSERT IGNORE INTO role_permission (id, role_id, permission_id) VALUES
('mo860', '7', '800'),
('mo861', '7', '801');

-- ============================================
-- 权限配置执行完成
-- ============================================
SELECT '毕业设计管理系统 - 权限配置执行完成！' AS '执行结果';
