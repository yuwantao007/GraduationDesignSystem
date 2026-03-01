package com.yuwan.completebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuwan.completebackend.exception.BusinessException;
import com.yuwan.completebackend.mapper.RoleMapper;
import com.yuwan.completebackend.mapper.RolePermissionMapper;
import com.yuwan.completebackend.mapper.UserRoleMapper;
import com.yuwan.completebackend.model.dto.CreateRoleDTO;
import com.yuwan.completebackend.model.dto.UpdateRoleDTO;
import com.yuwan.completebackend.model.entity.Role;
import com.yuwan.completebackend.model.entity.RolePermission;
import com.yuwan.completebackend.model.entity.UserRole;
import com.yuwan.completebackend.model.vo.RoleVO;
import com.yuwan.completebackend.service.IPermissionService;
import com.yuwan.completebackend.service.IRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理服务实现类
 *
 * @author 系统架构师
 * @version 1.1
 * @since 2026-02-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements IRoleService {

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserRoleMapper userRoleMapper;
    private final IPermissionService permissionService;

    @Override
    @Cacheable(value = "role:list", unless = "#result == null || #result.isEmpty()")
    public List<RoleVO> getAllRoles() {
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_status", 1);
        queryWrapper.orderByAsc("sort_order");

        List<Role> roles = roleMapper.selectList(queryWrapper);
        log.debug("查询所有启用角色，数量: {}", roles.size());
        
        return roles.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "user:roles", key = "#userId", unless = "#result == null || #result.isEmpty()")
    public List<RoleVO> getRolesByUserId(String userId) {
        List<Role> roles = roleMapper.selectRolesByUserId(userId);
        log.debug("查询用户角色，userId: {}, 角色数量: {}", userId, roles.size());
        
        return roles.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public RoleVO getRoleById(String roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null || role.getDeleted() == 1) {
            throw new BusinessException("角色不存在");
        }
        return convertToVO(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "role:list", allEntries = true)
    public RoleVO createRole(CreateRoleDTO createDTO) {
        // 检查角色代码是否已存在
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Role::getRoleCode, createDTO.getRoleCode());
        queryWrapper.eq(Role::getDeleted, 0);
        if (roleMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException("角色代码已存在");
        }

        // 创建角色
        Role role = new Role();
        role.setRoleName(createDTO.getRoleName());
        role.setRoleCode(createDTO.getRoleCode());
        role.setRoleDesc(createDTO.getDescription());
        role.setRoleStatus(1);
        role.setSortOrder(0);
        roleMapper.insert(role);

        // 分配权限
        if (createDTO.getPermissionIds() != null && !createDTO.getPermissionIds().isEmpty()) {
            permissionService.updateRolePermissions(role.getRoleId(), createDTO.getPermissionIds());
        }

        log.info("创建角色成功，角色ID: {}, 角色名称: {}", role.getRoleId(), role.getRoleName());
        return convertToVO(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "role:list", allEntries = true)
    public RoleVO updateRole(String roleId, UpdateRoleDTO updateDTO) {
        // 查询角色
        Role role = roleMapper.selectById(roleId);
        if (role == null || role.getDeleted() == 1) {
            throw new BusinessException("角色不存在");
        }

        // 更新基本信息
        if (StringUtils.hasText(updateDTO.getRoleName())) {
            role.setRoleName(updateDTO.getRoleName());
        }
        if (updateDTO.getDescription() != null) {
            role.setRoleDesc(updateDTO.getDescription());
        }
        role.setUpdateTime(new Date());
        roleMapper.updateById(role);

        // 更新权限
        if (updateDTO.getPermissionIds() != null) {
            permissionService.updateRolePermissions(roleId, updateDTO.getPermissionIds());
        }

        log.info("更新角色成功，角色ID: {}, 角色名称: {}", role.getRoleId(), role.getRoleName());
        return convertToVO(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "role:list", allEntries = true)
    public void deleteRole(String roleId) {
        // 查询角色
        Role role = roleMapper.selectById(roleId);
        if (role == null || role.getDeleted() == 1) {
            throw new BusinessException("角色不存在");
        }

        // 检查是否有用户使用该角色
        LambdaQueryWrapper<UserRole> userRoleQuery = new LambdaQueryWrapper<>();
        userRoleQuery.eq(UserRole::getRoleId, roleId);
        if (userRoleMapper.selectCount(userRoleQuery) > 0) {
            throw new BusinessException("该角色已分配给用户，无法删除");
        }

        // 删除角色权限关联
        LambdaQueryWrapper<RolePermission> permissionQuery = new LambdaQueryWrapper<>();
        permissionQuery.eq(RolePermission::getRoleId, roleId);
        rolePermissionMapper.delete(permissionQuery);

        // 逻辑删除角色
        roleMapper.deleteById(roleId);

        log.info("删除角色成功，角色ID: {}", roleId);
    }

    /**
     * 将Role实体转换为VO
     */
    private RoleVO convertToVO(Role role) {
        RoleVO roleVO = new RoleVO();
        BeanUtils.copyProperties(role, roleVO);
        return roleVO;
    }
}
