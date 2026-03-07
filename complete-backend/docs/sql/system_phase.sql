-- ===============================================
-- 系统阶段管理模块 - 数据库初始化脚本
-- 包含阶段配置表、阶段切换记录表及初始化数据
-- 
-- @author 系统架构师
-- @version 1.0
-- @since 2026-03-07
-- ===============================================

-- ----------------------------
-- 1. 阶段配置表（固定4条记录，只读配置）
-- ----------------------------
DROP TABLE IF EXISTS `system_phase_config`;
CREATE TABLE `system_phase_config` (
    `phase_id`          INT             NOT NULL AUTO_INCREMENT  COMMENT '阶段主键',
    `phase_code`        VARCHAR(50)     NOT NULL                 COMMENT '阶段代码',
    `phase_name`        VARCHAR(100)    NOT NULL                 COMMENT '阶段中文名',
    `phase_order`       INT             NOT NULL                 COMMENT '阶段序号（1/2/3/4）',
    `phase_description` VARCHAR(500)    DEFAULT NULL             COMMENT '阶段描述',
    `phase_icon`        VARCHAR(100)    DEFAULT NULL             COMMENT '前端展示图标',
    `phase_color`       VARCHAR(20)     DEFAULT NULL             COMMENT '前端展示颜色',
    `is_deleted`        TINYINT(1)      NOT NULL DEFAULT 0       COMMENT '逻辑删除（0-未删除 1-已删除）',
    `create_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`phase_id`),
    UNIQUE KEY `uk_phase_code` (`phase_code`),
    UNIQUE KEY `uk_phase_order` (`phase_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统阶段配置表';

-- ----------------------------
-- 2. 阶段切换记录表（审计追溯）
-- ----------------------------
DROP TABLE IF EXISTS `system_phase_record`;
CREATE TABLE `system_phase_record` (
    `record_id`           VARCHAR(32)   NOT NULL                 COMMENT '记录ID',
    `cohort`               VARCHAR(20)   NOT NULL                 COMMENT '毕业届别（如：2026届）',
    `phase_code`          VARCHAR(50)   NOT NULL                 COMMENT '当前阶段代码',
    `phase_order`         INT           NOT NULL                 COMMENT '当前阶段序号',
    `previous_phase_code` VARCHAR(50)   DEFAULT NULL             COMMENT '前一阶段代码（首个阶段为NULL）',
    `switch_time`         DATETIME      NOT NULL                 COMMENT '切换时间',
    `operator_id`         VARCHAR(32)   NOT NULL                 COMMENT '操作人ID（管理员）',
    `operator_name`       VARCHAR(100)  NOT NULL                 COMMENT '操作人姓名',
    `switch_reason`       VARCHAR(500)  DEFAULT NULL             COMMENT '切换原因/备注',
    `is_current`          TINYINT(1)    NOT NULL DEFAULT 1       COMMENT '是否为当前生效记录（仅一条为1）',
    `create_time`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`record_id`),
    KEY `idx_cohort_current` (`cohort`, `is_current`),
    KEY `idx_phase_code` (`phase_code`),
    KEY `idx_is_current` (`is_current`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统阶段切换记录表';

-- ----------------------------
-- 3. 已有旧表迁移：将 semester 列重命名为 cohort（学期→学届）
--    若表已按新结构创建则跳过此步骤
-- ----------------------------
ALTER TABLE `system_phase_record`
    CHANGE COLUMN `semester` `cohort` VARCHAR(20) NOT NULL COMMENT '毕业届别（如：2026届）';

DROP INDEX `idx_semester_current` ON `system_phase_record`;
ALTER TABLE `system_phase_record`
    ADD INDEX `idx_cohort_current` (`cohort`, `is_current`);

-- ----------------------------
-- 4. 初始化阶段配置数据（固定4条，不允许动态增删）
-- ----------------------------
INSERT INTO `system_phase_config` (`phase_id`, `phase_code`, `phase_name`, `phase_order`, `phase_description`, `phase_icon`, `phase_color`) VALUES
(1, 'TOPIC_DECLARATION',  '课题申报阶段', 1, '企业教师创建课题、三级审查流程（预审→初审→终审）', 'FormOutlined',      '#1890ff'),
(2, 'TOPIC_SELECTION',    '课题双选阶段', 2, '学生选报课题、教师确认人选、负责人审查双选结果',     'TeamOutlined',      '#52c41a'),
(3, 'TOPIC_GUIDANCE',     '课题指导阶段', 3, '项目指导、论文指导、文档提交、开题/中期答辩',       'EditOutlined',      '#faad14'),
(4, 'GRADUATION_DEFENSE', '毕设答辩阶段', 4, '答辩资格审查、正式/二次答辩、成绩评定、文档打印',   'TrophyOutlined',    '#f5222d');
