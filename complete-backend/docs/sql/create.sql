 -- ============================================
-- 毕业设计管理系统 - 建表语句汇总
-- 整合日期：2026-03-15
-- 说明：按功能模块分类整理所有建表语句
-- 执行顺序：请按文件顺序依次执行，确保外键约束正常
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS graduation_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE graduation_system;

-- ============================================
-- 一、用户管理模块
-- ============================================

-- 1.1 用户信息表
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
    direction_id VARCHAR(32) DEFAULT NULL COMMENT '所属/指导专业方向ID',
    major_id VARCHAR(32) DEFAULT NULL COMMENT '所在专业ID（学生精确专业关联）',
    enterprise_id VARCHAR(50) DEFAULT NULL COMMENT '所属企业ID',
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
    KEY idx_department (department),
    KEY idx_direction_id (direction_id),
    KEY idx_user_enterprise (enterprise_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- 1.2 角色信息表
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

-- 1.3 权限信息表
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

-- 1.4 用户角色关联表
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

-- 1.5 角色权限关联表
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
-- 二、企业管理模块
-- ============================================

-- 2.1 企业信息表
DROP TABLE IF EXISTS enterprise_info;
CREATE TABLE enterprise_info (
    enterprise_id VARCHAR(32) NOT NULL COMMENT '企业ID',
    enterprise_name VARCHAR(100) NOT NULL COMMENT '企业名称',
    enterprise_code VARCHAR(50) DEFAULT NULL COMMENT '企业编码',
    leader_id VARCHAR(32) DEFAULT NULL COMMENT '企业负责人用户ID',
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
    KEY idx_enterprise_status (enterprise_status),
    KEY idx_enterprise_leader_id (leader_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='企业信息表';

-- ============================================
-- 三、学校管理模块
-- ============================================

-- 3.1 学校信息表
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
-- 四、专业管理模块
-- ============================================

-- 4.1 专业方向表
DROP TABLE IF EXISTS major_direction_info;
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

-- 4.2 专业表
DROP TABLE IF EXISTS major_info;
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

-- 4.3 专业-企业老师关联表
CREATE TABLE IF NOT EXISTS major_teacher (
    id          VARCHAR(32)  NOT NULL COMMENT '主键ID',
    major_id    VARCHAR(32)  NOT NULL COMMENT '专业ID',
    user_id     VARCHAR(32)  NOT NULL COMMENT '企业老师用户ID',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_major_user (major_id, user_id),
    KEY idx_major_id (major_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专业-企业老师关联表';

-- ============================================
-- 五、课题管理模块
-- ============================================

-- 5.1 课题信息表
DROP TABLE IF EXISTS topic_info;
CREATE TABLE topic_info (
    topic_id VARCHAR(32) NOT NULL COMMENT '课题ID',
    topic_title VARCHAR(100) NOT NULL COMMENT '课题名称/题目',
    topic_category TINYINT NOT NULL COMMENT '课题大类（1-高职升本 2-3+1 3-实验班）',
    topic_type TINYINT NOT NULL COMMENT '课题类型（1-设计 2-论文）',
    topic_source TINYINT NOT NULL COMMENT '课题来源（1-校内 2-校外协同开发）',
    applicable_school VARCHAR(100) DEFAULT NULL COMMENT '适用学校（3+1/实验班必填）',
    enterprise_id VARCHAR(32) DEFAULT NULL COMMENT '归属企业ID（高职升本课题使用）',
    school_id VARCHAR(32) DEFAULT NULL COMMENT '关联学校ID（3+1/实验班课题使用）',
    guidance_direction VARCHAR(100) DEFAULT NULL COMMENT '指导方向/专业',
    direction_id VARCHAR(32) DEFAULT NULL COMMENT '专业方向ID',
    major_id VARCHAR(32) DEFAULT NULL COMMENT '专业ID',
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
    KEY idx_school_id (school_id),
    KEY idx_creator_id (creator_id),
    KEY idx_review_status (review_status),
    KEY idx_is_submitted (is_submitted),
    KEY idx_direction_id (direction_id),
    KEY idx_major_id (major_id),
    CONSTRAINT fk_topic_enterprise FOREIGN KEY (enterprise_id) REFERENCES enterprise_info (enterprise_id),
    CONSTRAINT fk_topic_creator FOREIGN KEY (creator_id) REFERENCES user_info (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课题信息表';

-- ============================================
-- 六、双选管理模块
-- ============================================

-- 6.1 课题选报记录表
DROP TABLE IF EXISTS topic_selection;
CREATE TABLE topic_selection (
    selection_id     VARCHAR(32)   NOT NULL            COMMENT '选报ID（雪花ID）',
    student_id       VARCHAR(32)   NOT NULL            COMMENT '学生用户ID',
    topic_id         VARCHAR(32)   NOT NULL            COMMENT '课题ID',
    selection_reason TEXT          NOT NULL            COMMENT '选报理由（必填）',
    selection_status TINYINT       DEFAULT 0           COMMENT '选报状态（0-待确认 1-中选 2-落选）',
    apply_time       DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '选报时间',
    confirm_time     DATETIME      DEFAULT NULL        COMMENT '确认时间（企业教师确认）',
    confirmed_by     VARCHAR(32)   DEFAULT NULL        COMMENT '确认人ID（企业教师）',
    deleted          TINYINT       DEFAULT 0           COMMENT '逻辑删除（0-未删除 1-已删除）',
    create_time      DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (selection_id),
    KEY idx_student_id (student_id),
    KEY idx_topic_id (topic_id),
    KEY idx_selection_status (selection_status),
    CONSTRAINT fk_sel_student FOREIGN KEY (student_id) REFERENCES user_info (user_id),
    CONSTRAINT fk_sel_topic   FOREIGN KEY (topic_id)   REFERENCES topic_info (topic_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课题选报记录表';

-- 6.2 教师指派记录表
DROP TABLE IF EXISTS teacher_assignment;
CREATE TABLE teacher_assignment (
    assignment_id       VARCHAR(32)   NOT NULL                              COMMENT '指派ID（雪花ID）',
    student_id          VARCHAR(32)   NOT NULL                              COMMENT '学生用户ID',
    topic_id            VARCHAR(32)   NOT NULL                              COMMENT '课题ID',
    selection_id        VARCHAR(32)   NOT NULL                              COMMENT '关联选报记录ID',
    assigned_teacher_id VARCHAR(32)   NOT NULL                              COMMENT '指派教师ID（企业教师）',
    assigned_by         VARCHAR(32)   NOT NULL                              COMMENT '指派人ID（企业负责人）',
    assign_time         DATETIME      DEFAULT CURRENT_TIMESTAMP             COMMENT '指派时间',
    cancel_time         DATETIME      DEFAULT NULL                          COMMENT '取消指派时间',
    assign_status       TINYINT       DEFAULT 1                             COMMENT '指派状态（1-已指派 0-已取消）',
    deleted             TINYINT       DEFAULT 0                             COMMENT '逻辑删除（0-未删除 1-已删除）',
    create_time         DATETIME      DEFAULT CURRENT_TIMESTAMP             COMMENT '创建时间',
    update_time         DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (assignment_id),
    KEY idx_student_id       (student_id),
    KEY idx_topic_id         (topic_id),
    KEY idx_selection_id     (selection_id),
    KEY idx_assigned_teacher (assigned_teacher_id),
    CONSTRAINT fk_assign_student  FOREIGN KEY (student_id)          REFERENCES user_info (user_id),
    CONSTRAINT fk_assign_topic    FOREIGN KEY (topic_id)            REFERENCES topic_info (topic_id),
    CONSTRAINT fk_assign_selection FOREIGN KEY (selection_id)       REFERENCES topic_selection (selection_id),
    CONSTRAINT fk_assign_teacher  FOREIGN KEY (assigned_teacher_id) REFERENCES user_info (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师指派记录表';

-- ============================================
-- 七、课题审查模块
-- ============================================

-- 7.1 课题审查记录表
DROP TABLE IF EXISTS topic_review_record;
CREATE TABLE topic_review_record (
    review_id VARCHAR(32) NOT NULL COMMENT '审查记录ID',
    topic_id VARCHAR(32) NOT NULL COMMENT '课题ID',
    review_stage TINYINT NOT NULL COMMENT '审查阶段（1-预审 2-初审 3-终审）',
    reviewer_id VARCHAR(32) NOT NULL COMMENT '审查人ID',
    reviewer_role VARCHAR(50) NOT NULL COMMENT '审查人角色代码',
    reviewer_name VARCHAR(50) DEFAULT NULL COMMENT '审查人姓名',
    review_result TINYINT NOT NULL COMMENT '审查结果（1-通过 2-需修改 3-不通过）',
    review_opinion TEXT COMMENT '审查意见（针对单个课题）',
    is_batch_review TINYINT DEFAULT 0 COMMENT '是否批量审查（0-否 1-是）',
    batch_review_id VARCHAR(32) DEFAULT NULL COMMENT '批量审查批次ID',
    previous_status TINYINT COMMENT '审查前课题状态',
    new_status TINYINT COMMENT '审查后课题状态',
    is_modified TINYINT DEFAULT 0 COMMENT '是否被修改过（0-否 1-是）',
    modified_by VARCHAR(32) DEFAULT NULL COMMENT '修改人ID',
    modified_time DATETIME DEFAULT NULL COMMENT '修改时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除 1-已删除）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审查时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (review_id),
    KEY idx_topic_id (topic_id),
    KEY idx_reviewer_id (reviewer_id),
    KEY idx_review_stage (review_stage),
    KEY idx_review_result (review_result),
    KEY idx_batch_review_id (batch_review_id),
    KEY idx_create_time (create_time),
    CONSTRAINT fk_review_topic FOREIGN KEY (topic_id) REFERENCES topic_info (topic_id),
    CONSTRAINT fk_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES user_info (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课题审查记录表';

-- 7.2 综合审查意见表
DROP TABLE IF EXISTS topic_general_opinion;
CREATE TABLE topic_general_opinion (
    opinion_id VARCHAR(32) NOT NULL COMMENT '意见ID',
    reviewer_id VARCHAR(32) NOT NULL COMMENT '提交人ID',
    reviewer_role VARCHAR(50) NOT NULL COMMENT '提交人角色代码',
    reviewer_name VARCHAR(50) DEFAULT NULL COMMENT '提交人姓名',
    review_stage TINYINT NOT NULL COMMENT '审查阶段（1-预审 2-初审 3-终审）',
    guidance_direction VARCHAR(100) NOT NULL COMMENT '适用专业方向',
    opinion_content VARCHAR(200) NOT NULL COMMENT '综合意见内容（≤200字）',
    target_scope VARCHAR(50) DEFAULT 'DIRECTION' COMMENT '可见范围（DIRECTION-本专业方向）',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除 1-已删除）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (opinion_id),
    KEY idx_reviewer_id (reviewer_id),
    KEY idx_review_stage (review_stage),
    KEY idx_guidance_direction (guidance_direction),
    KEY idx_create_time (create_time),
    CONSTRAINT fk_opinion_reviewer FOREIGN KEY (reviewer_id) REFERENCES user_info (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='综合审查意见表';

-- 7.3 批量审查批次表
DROP TABLE IF EXISTS topic_batch_review;
CREATE TABLE topic_batch_review (
    batch_id VARCHAR(32) NOT NULL COMMENT '批次ID',
    reviewer_id VARCHAR(32) NOT NULL COMMENT '审查人ID',
    reviewer_role VARCHAR(50) NOT NULL COMMENT '审查人角色代码',
    review_stage TINYINT NOT NULL COMMENT '审查阶段（1-预审 2-初审 3-终审）',
    review_result TINYINT NOT NULL COMMENT '审查结果（1-通过 2-需修改 3-不通过）',
    review_opinion TEXT COMMENT '批量审查意见',
    topic_count INT DEFAULT 0 COMMENT '审查课题数量',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除 1-已删除）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (batch_id),
    KEY idx_reviewer_id (reviewer_id),
    KEY idx_review_stage (review_stage),
    KEY idx_create_time (create_time),
    CONSTRAINT fk_batch_reviewer FOREIGN KEY (reviewer_id) REFERENCES user_info (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='批量审查批次表';

-- 7.4 课题流程实例映射表
CREATE TABLE IF NOT EXISTS topic_process_instance (
    id                  VARCHAR(32)  NOT NULL                    COMMENT '主键（雪花ID）',
    topic_id            VARCHAR(32)  NOT NULL                    COMMENT '课题ID',
    process_instance_id VARCHAR(64)  NOT NULL                    COMMENT 'Flowable流程实例ID',
    process_def_key     VARCHAR(64)  NOT NULL DEFAULT 'topic_review' COMMENT '流程定义Key',
    topic_category      TINYINT      NOT NULL DEFAULT 1          COMMENT '课题大类（1-高职升本 2-3+1 3-实验班）',
    process_status      TINYINT      NOT NULL DEFAULT 0          COMMENT '流程状态（0-运行中 1-已完成 2-已终止）',
    create_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT      NOT NULL DEFAULT 0          COMMENT '逻辑删除（0-未删除 1-已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_topic_id (topic_id),
    KEY idx_process_instance_id (process_instance_id),
    KEY idx_process_status (process_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课题流程实例映射表';

-- ============================================
-- 八、教师配对管理模块
-- ============================================

-- 8.1 高校教师-专业方向分配表（粗粒度）
CREATE TABLE IF NOT EXISTS university_teacher_major (
    id              VARCHAR(32)  NOT NULL  COMMENT '主键',
    univ_teacher_id VARCHAR(32)  NOT NULL  COMMENT '高校教师user_id',
    direction_id    VARCHAR(32)  NOT NULL  COMMENT '专业方向ID',
    enterprise_id   VARCHAR(32)  NOT NULL  COMMENT '所属企业ID',
    cohort          VARCHAR(20)  NOT NULL  COMMENT '届别（如2026届）',
    is_enabled      TINYINT      DEFAULT 1 COMMENT '是否启用（1-启用 0-停用）',
    remark          VARCHAR(200) DEFAULT NULL COMMENT '备注',
    deleted         TINYINT      DEFAULT 0 COMMENT '逻辑删除（0-未删除 1-已删除）',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_teacher_dir_cohort (univ_teacher_id, direction_id, cohort),
    KEY idx_direction (direction_id),
    KEY idx_enterprise (enterprise_id),
    KEY idx_cohort (cohort),
    KEY idx_univ_teacher (univ_teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='高校教师-专业方向分配表';

-- 8.2 高校教师-企业教师精确配对表（细粒度）
CREATE TABLE IF NOT EXISTS teacher_relationship (
    relation_id           VARCHAR(32)  NOT NULL  COMMENT '主键',
    univ_teacher_id       VARCHAR(32)  NOT NULL  COMMENT '高校教师user_id',
    enterprise_teacher_id VARCHAR(32)  NOT NULL  COMMENT '企业教师user_id',
    enterprise_id         VARCHAR(32)  NOT NULL  COMMENT '企业ID',
    direction_id          VARCHAR(32)  DEFAULT NULL COMMENT '所属专业方向（辅助描述，可选）',
    cohort                VARCHAR(20)  NOT NULL  COMMENT '届别',
    relation_type         VARCHAR(20)  DEFAULT 'DIRECT' COMMENT '配对类型（DIRECT-直接配对 ASSIST-辅助支持）',
    is_enabled            TINYINT      DEFAULT 1 COMMENT '是否启用（1-启用 0-停用）',
    remark                VARCHAR(200) DEFAULT NULL COMMENT '备注',
    deleted               TINYINT      DEFAULT 0 COMMENT '逻辑删除（0-未删除 1-已删除）',
    create_time           DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time           DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (relation_id),
    UNIQUE KEY uk_pair_cohort (univ_teacher_id, enterprise_teacher_id, cohort),
    KEY idx_enterprise_teacher (enterprise_teacher_id),
    KEY idx_enterprise (enterprise_id),
    KEY idx_cohort (cohort),
    KEY idx_univ_teacher (univ_teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='高校教师-企业教师精确配对表';

-- ============================================
-- 九、系统阶段管理模块
-- ============================================

-- 9.1 阶段配置表
DROP TABLE IF EXISTS system_phase_config;
CREATE TABLE system_phase_config (
    phase_id          INT             NOT NULL AUTO_INCREMENT  COMMENT '阶段主键',
    phase_code        VARCHAR(50)     NOT NULL                 COMMENT '阶段代码',
    phase_name        VARCHAR(100)    NOT NULL                 COMMENT '阶段中文名',
    phase_order       INT             NOT NULL                 COMMENT '阶段序号（1/2/3/4）',
    phase_description VARCHAR(500)    DEFAULT NULL             COMMENT '阶段描述',
    phase_icon        VARCHAR(100)    DEFAULT NULL             COMMENT '前端展示图标',
    phase_color       VARCHAR(20)     DEFAULT NULL             COMMENT '前端展示颜色',
    is_deleted        TINYINT(1)      NOT NULL DEFAULT 0       COMMENT '逻辑删除（0-未删除 1-已删除）',
    create_time       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (phase_id),
    UNIQUE KEY uk_phase_code (phase_code),
    UNIQUE KEY uk_phase_order (phase_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统阶段配置表';

-- 9.2 阶段切换记录表
DROP TABLE IF EXISTS system_phase_record;
CREATE TABLE system_phase_record (
    record_id           VARCHAR(32)   NOT NULL                 COMMENT '记录ID',
    cohort              VARCHAR(20)   NOT NULL                 COMMENT '毕业届别（如：2026届）',
    phase_code          VARCHAR(50)   NOT NULL                 COMMENT '当前阶段代码',
    phase_order         INT           NOT NULL                 COMMENT '当前阶段序号',
    previous_phase_code VARCHAR(50)   DEFAULT NULL             COMMENT '前一阶段代码（首个阶段为NULL）',
    switch_time         DATETIME      NOT NULL                 COMMENT '切换时间',
    operator_id         VARCHAR(32)   NOT NULL                 COMMENT '操作人ID（管理员）',
    operator_name       VARCHAR(100)  NOT NULL                 COMMENT '操作人姓名',
    switch_reason       VARCHAR(500)  DEFAULT NULL             COMMENT '切换原因/备注',
    is_current          TINYINT(1)    NOT NULL DEFAULT 1       COMMENT '是否为当前生效记录（仅一条为1）',
    create_time         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (record_id),
    KEY idx_cohort_current (cohort, is_current),
    KEY idx_phase_code (phase_code),
    KEY idx_is_current (is_current)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统阶段切换记录表';

-- ============================================
-- 十、质量监控与预警模块
-- ============================================

-- 10.1 系统预警记录表
CREATE TABLE IF NOT EXISTS alert_info (
    alert_id      VARCHAR(32)   NOT NULL                                          COMMENT '预警ID（雪花算法）',
    alert_type    VARCHAR(50)   NOT NULL                                          COMMENT '预警类型',
    alert_level   TINYINT       NOT NULL DEFAULT 1                                COMMENT '预警级别：1-提示 2-警告 3-严重',
    alert_title   VARCHAR(200)  NOT NULL                                          COMMENT '预警标题',
    alert_content VARCHAR(1000)                                                   COMMENT '预警详情',
    target_id     VARCHAR(32)                                                     COMMENT '关联对象ID',
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
-- 建表语句执行完成
-- ============================================
SELECT '毕业设计管理系统 - 建表语句执行完成！' AS '执行结果';
