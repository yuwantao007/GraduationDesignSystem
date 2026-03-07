-- ============================================
-- 系统阶段管理模块权限配置脚本
-- 功能：阶段查看、初始化、切换、历史记录
-- 日期：2026-03-07
-- ============================================

USE graduation_system;

-- ============================================
-- 1. 添加系统阶段管理相关权限
-- ============================================
INSERT INTO permission_info (permission_id, parent_id, permission_name, permission_code, permission_type, path, icon, sort_order) VALUES
-- 一级菜单
('800', '0', '阶段管理', 'phase:manage', 1, '/system/phase', 'FieldTimeOutlined', 8),
-- 阶段管理子菜单
('801', '800', '阶段概览', 'phase:view', 1, '/system/phase/overview', 'DashboardOutlined', 1),
('802', '800', '切换记录', 'phase:records', 1, '/system/phase/records', 'HistoryOutlined', 2),
-- 阶段管理按钮权限
('8011', '801', '初始化阶段', 'phase:init', 2, NULL, NULL, 1),
('8012', '801', '切换阶段', 'phase:switch', 2, NULL, NULL, 2);

-- ============================================
-- 2. 为系统管理员分配完整阶段管理权限
-- 角色ID: 1 (SYSTEM_ADMIN)
-- 权限：阶段菜单、概览、初始化、切换、切换记录
-- ============================================
INSERT INTO role_permission (id, role_id, permission_id) VALUES
('800', '1', '800'),   -- 阶段管理菜单
('801', '1', '801'),   -- 阶段概览
('802', '1', '802'),   -- 切换记录
('803', '1', '8011'),  -- 初始化阶段
('804', '1', '8012');  -- 切换阶段

-- ============================================
-- 3. 为其他角色分配阶段查看权限
-- 所有角色均可查看当前阶段状态（只读）
-- ============================================

-- 督导教师（角色ID: 2）
INSERT INTO role_permission (id, role_id, permission_id) VALUES
('810', '2', '800'),  -- 阶段管理菜单
('811', '2', '801');  -- 阶段概览

-- 高校教师（角色ID: 3）
INSERT INTO role_permission (id, role_id, permission_id) VALUES
('820', '3', '800'),  -- 阶段管理菜单
('821', '3', '801');  -- 阶段概览

-- 企业负责人（角色ID: 4）
INSERT INTO role_permission (id, role_id, permission_id) VALUES
('830', '4', '800'),  -- 阶段管理菜单
('831', '4', '801');  -- 阶段概览

-- 专业方向主管（角色ID: 5）
INSERT INTO role_permission (id, role_id, permission_id) VALUES
('840', '5', '800'),  -- 阶段管理菜单
('841', '5', '801');  -- 阶段概览

-- 企业教师（角色ID: 6）
INSERT INTO role_permission (id, role_id, permission_id) VALUES
('850', '6', '800'),  -- 阶段管理菜单
('851', '6', '801');  -- 阶段概览

-- 学生（角色ID: 7）
INSERT INTO role_permission (id, role_id, permission_id) VALUES
('860', '7', '800'),  -- 阶段管理菜单
('861', '7', '801');  -- 阶段概览
