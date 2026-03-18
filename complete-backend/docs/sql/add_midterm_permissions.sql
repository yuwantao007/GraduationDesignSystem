-- =============================================
-- 中期检查权限配置
-- 创建时间: 2026-03-17
-- 权限ID范围: 960-979
-- =============================================
-- 角色ID已确认:
-- 1 = 系统管理员
-- 3 = 高校教师
-- 6 = 企业教师
-- 7 = 学生
-- =============================================

-- 1. 清理旧数据（如有）
DELETE FROM `role_permission` WHERE `permission_id` IN
  ('960','961','962','963','964','965','966','967','968','969','970','971','972','973','974');
DELETE FROM `permission_info` WHERE `permission_id` IN
  ('960','961','962','963','964','965','966','967','968','969','970','971','972','973','974');

-- 2. 插入权限数据
INSERT INTO `permission_info` (`permission_id`, `permission_name`, `permission_code`, `permission_type`, `parent_id`, `path`, `icon`, `sort_order`, `permission_status`) VALUES
-- 中期检查主菜单
('960', '中期检查管理', 'midterm:menu', 1, '0', '/midterm', 'FileTextOutlined', 40, 1),

-- 企业教师菜单和权限
('961', '中期检查填写', 'midterm:enterprise:menu', 1, '960', '/midterm/enterprise', NULL, 1, 1),
('962', '中期检查列表(企业)', 'midterm:enterprise:list', 2, '961', NULL, NULL, 2, 1),
('963', '中期检查编辑(企业)', 'midterm:enterprise:edit', 2, '961', NULL, NULL, 3, 1),
('964', '中期检查提交(企业)', 'midterm:enterprise:submit', 2, '961', NULL, NULL, 4, 1),
('965', '中期检查详情(企业)', 'midterm:enterprise:detail', 2, '961', NULL, NULL, 5, 1),

-- 高校教师菜单和权限
('966', '中期检查审查', 'midterm:univ:menu', 1, '960', '/midterm/univ', NULL, 6, 1),
('967', '中期检查列表(高校)', 'midterm:univ:list', 2, '966', NULL, NULL, 1, 1),
('968', '中期检查审查(高校)', 'midterm:univ:review', 2, '966', NULL, NULL, 2, 1),
('969', '中期检查详情(高校)', 'midterm:univ:detail', 2, '966', NULL, NULL, 3, 1),

-- 学生菜单和权限
('970', '我的中期检查', 'midterm:student:menu', 1, '960', '/midterm/student', NULL, 7, 1),
('971', '查看中期检查(学生)', 'midterm:student:view', 2, '970', NULL, NULL, 1, 1),

-- 管理员菜单和权限
('972', '中期检查管理(管理)', 'midterm:admin:menu', 1, '960', '/midterm/admin', NULL, 8, 1),
('973', '中期检查列表(管理)', 'midterm:admin:list', 2, '972', NULL, NULL, 1, 1),
('974', '中期检查详情(管理)', 'midterm:admin:detail', 2, '972', NULL, NULL, 2, 1);

-- 3. 分配角色权限

-- 企业教师(role_id=6): 中期检查填写相关权限
INSERT INTO `role_permission` (`id`, `role_id`, `permission_id`) VALUES
(REPLACE(UUID(),'-',''), '6', '960'),   -- 主菜单
(REPLACE(UUID(),'-',''), '6', '961'),   -- 企业教师菜单
(REPLACE(UUID(),'-',''), '6', '962'),   -- 列表
(REPLACE(UUID(),'-',''), '6', '963'),   -- 编辑
(REPLACE(UUID(),'-',''), '6', '964'),   -- 提交
(REPLACE(UUID(),'-',''), '6', '965');   -- 详情

-- 高校教师(role_id=3): 中期检查审查相关权限
INSERT INTO `role_permission` (`id`, `role_id`, `permission_id`) VALUES
(REPLACE(UUID(),'-',''), '3', '960'),   -- 主菜单
(REPLACE(UUID(),'-',''), '3', '966'),   -- 高校教师菜单
(REPLACE(UUID(),'-',''), '3', '967'),   -- 列表
(REPLACE(UUID(),'-',''), '3', '968'),   -- 审查
(REPLACE(UUID(),'-',''), '3', '969');   -- 详情

-- 学生(role_id=7): 查看自己的中期检查
INSERT INTO `role_permission` (`id`, `role_id`, `permission_id`) VALUES
(REPLACE(UUID(),'-',''), '7', '960'),   -- 主菜单
(REPLACE(UUID(),'-',''), '7', '970'),   -- 学生菜单
(REPLACE(UUID(),'-',''), '7', '971');   -- 查看

-- 系统管理员(role_id=1): 所有权限
INSERT INTO `role_permission` (`id`, `role_id`, `permission_id`) VALUES
(REPLACE(UUID(),'-',''), '1', '960'),   -- 主菜单
(REPLACE(UUID(),'-',''), '1', '961'),   -- 企业教师菜单
(REPLACE(UUID(),'-',''), '1', '962'),   -- 列表(企业)
(REPLACE(UUID(),'-',''), '1', '963'),   -- 编辑(企业)
(REPLACE(UUID(),'-',''), '1', '964'),   -- 提交(企业)
(REPLACE(UUID(),'-',''), '1', '965'),   -- 详情(企业)
(REPLACE(UUID(),'-',''), '1', '966'),   -- 高校教师菜单
(REPLACE(UUID(),'-',''), '1', '967'),   -- 列表(高校)
(REPLACE(UUID(),'-',''), '1', '968'),   -- 审查(高校)
(REPLACE(UUID(),'-',''), '1', '969'),   -- 详情(高校)
(REPLACE(UUID(),'-',''), '1', '970'),   -- 学生菜单
(REPLACE(UUID(),'-',''), '1', '971'),   -- 查看(学生)
(REPLACE(UUID(),'-',''), '1', '972'),   -- 管理菜单
(REPLACE(UUID(),'-',''), '1', '973'),   -- 列表(管理)
(REPLACE(UUID(),'-',''), '1', '974');   -- 详情(管理)
