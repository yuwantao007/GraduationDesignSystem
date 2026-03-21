-- 目的：确保“答辩安排”菜单权限仅授予企业负责人(ENTERPRISE_LEADER)
-- 涉及权限：
--   defense:arrangement:list (941)
--   defense:arrangement:menu (946)

USE graduation_system;

SET @arrangement_list_perm_id = (
    SELECT permission_id FROM permission_info
    WHERE permission_code = 'defense:arrangement:list' AND deleted = 0
    LIMIT 1
);

SET @arrangement_menu_perm_id = (
    SELECT permission_id FROM permission_info
    WHERE permission_code = 'defense:arrangement:menu' AND deleted = 0
    LIMIT 1
);

SET @enterprise_leader_role_id = (
    SELECT role_id FROM role_info
    WHERE role_code = 'ENTERPRISE_LEADER' AND deleted = 0
    LIMIT 1
);

-- 删除非企业负责人角色上的答辩安排菜单相关权限
DELETE rp
FROM role_permission rp
WHERE rp.permission_id IN (@arrangement_list_perm_id, @arrangement_menu_perm_id)
  AND rp.role_id <> @enterprise_leader_role_id;

-- 为企业负责人补齐权限（幂等）
INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @enterprise_leader_role_id, @arrangement_list_perm_id
FROM DUAL
WHERE @enterprise_leader_role_id IS NOT NULL
  AND @arrangement_list_perm_id IS NOT NULL;

INSERT IGNORE INTO role_permission (id, role_id, permission_id)
SELECT REPLACE(UUID(), '-', ''), @enterprise_leader_role_id, @arrangement_menu_perm_id
FROM DUAL
WHERE @enterprise_leader_role_id IS NOT NULL
  AND @arrangement_menu_perm_id IS NOT NULL;

-- 校验：仅企业负责人具备答辩安排菜单相关权限
SELECT r.role_code,
       p.permission_code
FROM role_permission rp
JOIN role_info r ON r.role_id = rp.role_id
JOIN permission_info p ON p.permission_id = rp.permission_id
WHERE p.permission_code IN ('defense:arrangement:list', 'defense:arrangement:menu')
  AND r.deleted = 0
  AND p.deleted = 0
ORDER BY p.permission_code, r.role_code;
