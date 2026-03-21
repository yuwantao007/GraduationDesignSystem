-- ============================================
-- 站内消息中心权限脚本
-- 权限ID范围：960-969
-- 创建日期：2026-03-21
-- ============================================

USE graduation_system;

INSERT INTO permission_info
(permission_id, parent_id, permission_name, permission_code, permission_type, path, icon, sort_order, permission_status, deleted)
VALUES
('960', '0', '消息中心', 'notification:menu', 1, '/notification/center', 'BellOutlined', 960, 1, 0),
('961', '960', '查看消息', 'notification:view', 2, NULL, NULL, 961, 1, 0),
('962', '960', '标记已读', 'notification:read', 2, NULL, NULL, 962, 1, 0),
('963', '960', '标记处理', 'notification:process', 2, NULL, NULL, 963, 1, 0),
('964', '960', '删除消息', 'notification:delete', 2, NULL, NULL, 964, 1, 0)
ON DUPLICATE KEY UPDATE
    permission_name = VALUES(permission_name),
    permission_code = VALUES(permission_code),
    permission_type = VALUES(permission_type),
    path = VALUES(path),
    icon = VALUES(icon),
    sort_order = VALUES(sort_order),
    permission_status = VALUES(permission_status),
    deleted = VALUES(deleted);

-- 系统全角色赋予站内消息权限（1-系统管理员 2-督导教师 3-高校教师 4-企业负责人 5-专业方向主管 6-企业教师 7-学生）
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), role_id, '960'
FROM role_info
WHERE role_id IN ('1', '2', '3', '4', '5', '6', '7');

INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), role_id, '961'
FROM role_info
WHERE role_id IN ('1', '2', '3', '4', '5', '6', '7');

INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), role_id, '962'
FROM role_info
WHERE role_id IN ('1', '2', '3', '4', '5', '6', '7');

INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), role_id, '963'
FROM role_info
WHERE role_id IN ('1', '2', '3', '4', '5', '6', '7');

INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), role_id, '964'
FROM role_info
WHERE role_id IN ('1', '2', '3', '4', '5', '6', '7');

SELECT 'add_notification_permissions.sql 执行完成' AS result;
