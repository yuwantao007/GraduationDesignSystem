-- ============================================================
-- 专业-企业老师关联表迁移脚本
-- 功能：为专业管理添加"企业老师"多对多关联能力
-- 日期：2026-03-07
-- ============================================================

-- 创建专业-企业老师关联表
CREATE TABLE IF NOT EXISTS `major_teacher` (
    `id`          VARCHAR(32)  NOT NULL COMMENT '主键ID',
    `major_id`    VARCHAR(32)  NOT NULL COMMENT '专业ID',
    `user_id`     VARCHAR(32)  NOT NULL COMMENT '企业老师用户ID',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_major_user` (`major_id`, `user_id`),
    KEY `idx_major_id` (`major_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    COMMENT='专业-企业老师关联表';
