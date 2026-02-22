-- ============================================
-- 用户管理模块数据库表结构
-- @author 系统架构师
-- @version 1.0
-- @since 2026-02-20
-- ============================================

-- 创建数据库（如不存在）
CREATE DATABASE IF NOT EXISTS graduation_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE graduation_system;

-- -------------------------------------------
-- 1. 用户信息表
-- -------------------------------------------
DROP TABLE IF EXISTS user_info;
CREATE TABLE user_info (
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '登录账号',
    password VARCHAR(200) NOT NULL COMMENT '登录密码（BCrypt加密）',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    user_email VARCHAR(100) DEFAULT NULL COMMENT '用户邮箱',
    user_phone VARCHAR(20) DEFAULT NULL COMMENT '手机号码',
    avatar VARCHAR(500) DEFAULT NULL COMMENT '头像地址',
    gender TINYINT DEFAULT 0 COMMENT '性别（1-男 0-女）',
    department VARCHAR(100) DEFAULT NULL COMMENT '所属院系/企业',
    major VARCHAR(100) DEFAULT NULL COMMENT '专业方向',
    student_no VARCHAR(50) DEFAULT NULL COMMENT '学号（学生角色）',
    employee_no VARCHAR(50) DEFAULT NULL COMMENT '工号（教师角色）',
    title VARCHAR(50) DEFAULT NULL COMMENT '职称（教师角色）',
    user_status TINYINT DEFAULT 1 COMMENT '账号状态（0-禁用 1-正常 2-锁定）',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除 1-已删除）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_username (username),
    KEY idx_real_name (real_name),
    KEY idx_user_status (user_status),
    KEY idx_department (department)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- -------------------------------------------
-- 2. 角色信息表
-- -------------------------------------------
DROP TABLE IF EXISTS role_info;
CREATE TABLE role_info (
    role_id VARCHAR(32) NOT NULL COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    role_desc VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    role_status TINYINT DEFAULT 1 COMMENT '角色状态（0-禁用 1-正常）',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除 1-已删除）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (role_id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色信息表';

-- -------------------------------------------
-- 3. 权限信息表
-- -------------------------------------------
DROP TABLE IF EXISTS permission_info;
CREATE TABLE permission_info (
    permission_id VARCHAR(32) NOT NULL COMMENT '权限ID',
    parent_id VARCHAR(32) DEFAULT '0' COMMENT '父级权限ID',
    permission_name VARCHAR(50) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL COMMENT '权限编码',
    permission_type TINYINT DEFAULT 1 COMMENT '权限类型（1-菜单 2-按钮 3-接口）',
    path VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    icon VARCHAR(100) DEFAULT NULL COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    permission_status TINYINT DEFAULT 1 COMMENT '权限状态（0-禁用 1-正常）',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除 1-已删除）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (permission_id),
    UNIQUE KEY uk_permission_code (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限信息表';

-- -------------------------------------------
-- 4. 用户角色关联表
-- -------------------------------------------
DROP TABLE IF EXISTS user_role;
CREATE TABLE user_role (
    id VARCHAR(32) NOT NULL COMMENT '主键ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    role_id VARCHAR(32) NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- -------------------------------------------
-- 5. 角色权限关联表
-- -------------------------------------------
DROP TABLE IF EXISTS role_permission;
CREATE TABLE role_permission (
    id VARCHAR(32) NOT NULL COMMENT '主键ID',
    role_id VARCHAR(32) NOT NULL COMMENT '角色ID',
    permission_id VARCHAR(32) NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    KEY idx_role_id (role_id),
    KEY idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- ============================================
-- 初始化数据
-- ============================================

-- 初始化角色数据
INSERT INTO role_info (role_id, role_name, role_code, role_desc, sort_order) VALUES
('1', '系统管理员', 'SYSTEM_ADMIN', '系统配置、用户管理、交叉评阅分配', 1),
('2', '督导教师', 'SUPERVISOR_TEACHER', '毕业设计课题终审，课题审查最后环节', 2),
('3', '高校教师', 'UNIVERSITY_TEACHER', '课题预审、学生毕设指导、论文审查、成绩评语评定', 3),
('4', '企业负责人', 'ENTERPRISE_LEADER', '企业端毕设工作统筹管理', 4),
('5', '专业方向主管', 'MAJOR_DIRECTOR', '毕业设计课题初审，企业教师指导记录审查', 5),
('6', '企业教师', 'ENTERPRISE_TEACHER', '课题申报、学生选题确认、毕设全程指导', 6),
('7', '学生', 'STUDENT', '完成毕业设计全流程操作执行', 7);

-- 初始化系统管理员账号（密码: 123456，BCrypt加密）
-- 管理员账号可以使用 username='admin' 或 employeeNo='admin' 登录
INSERT INTO user_info (user_id, username, password, real_name, user_email, employee_no, user_status) VALUES
('1', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin@example.com', 'admin', 1);

-- 为管理员分配角色
INSERT INTO user_role (id, user_id, role_id) VALUES
('1', '1', '1');

-- ============================================
-- 初始化权限数据（菜单权限）
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

-- ============================================
-- 为系统管理员角色分配所有权限
-- ============================================
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
