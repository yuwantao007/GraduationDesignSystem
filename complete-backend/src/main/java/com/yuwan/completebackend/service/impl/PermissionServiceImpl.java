package com.yuwan.completebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuwan.completebackend.mapper.PermissionMapper;
import com.yuwan.completebackend.mapper.RolePermissionMapper;
import com.yuwan.completebackend.model.entity.Permission;
import com.yuwan.completebackend.model.entity.RolePermission;
import com.yuwan.completebackend.model.vo.PermissionVO;
import com.yuwan.completebackend.service.IPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 * 提供权限信息查询和角色权限管理功能
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements IPermissionService {

    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;

    @Override
    public List<PermissionVO> getPermissionTree() {
        // 查询所有启用的权限
        LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Permission::getDeleted, 0);
        queryWrapper.eq(Permission::getPermissionStatus, 1);
        queryWrapper.orderByAsc(Permission::getSortOrder);
        
        List<Permission> allPermissions = permissionMapper.selectList(queryWrapper);
        
        // 转换为VO
        List<PermissionVO> allVOs = allPermissions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        // 构建树形结构
        return buildTree(allVOs);
    }

    @Override
    public List<PermissionVO> getAllPermissions() {
        LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Permission::getDeleted, 0);
        queryWrapper.eq(Permission::getPermissionStatus, 1);
        queryWrapper.orderByAsc(Permission::getSortOrder);
        
        List<Permission> allPermissions = permissionMapper.selectList(queryWrapper);
        
        return allPermissions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PermissionVO> getPermissionsByRoleId(String roleId) {
        List<Permission> permissions = permissionMapper.selectPermissionsByRoleId(roleId);
        
        return permissions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getPermissionIdsByRoleId(String roleId) {
        LambdaQueryWrapper<RolePermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RolePermission::getRoleId, roleId);
        
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(queryWrapper);
        
        return rolePermissions.stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRolePermissions(String roleId, List<String> permissionIds) {
        log.info("更新角色权限，角色ID: {}, 权限数量: {}", roleId, permissionIds.size());
        
        // 删除角色原有权限
        LambdaQueryWrapper<RolePermission> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(RolePermission::getRoleId, roleId);
        rolePermissionMapper.delete(deleteWrapper);
        
        // 添加新权限
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (String permissionId : permissionIds) {
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId);
                rolePermission.setCreateTime(new Date());
                rolePermissionMapper.insert(rolePermission);
            }
        }
        
        log.info("角色权限更新完成，角色ID: {}, 新权限数量: {}", roleId, permissionIds.size());
    }

    /**
     * 将Permission实体转换为VO
     */
    private PermissionVO convertToVO(Permission permission) {
        PermissionVO vo = new PermissionVO();
        BeanUtils.copyProperties(permission, vo);
        return vo;
    }

    /**
     * 构建权限树形结构
     *
     * @param allVOs 所有权限VO列表
     * @return 树形结构的权限列表
     */
    private List<PermissionVO> buildTree(List<PermissionVO> allVOs) {
        // 按parentId分组
        Map<String, List<PermissionVO>> parentIdMap = allVOs.stream()
                .collect(Collectors.groupingBy(vo -> 
                        vo.getParentId() == null ? "0" : vo.getParentId()));
        
        // 为每个权限设置子权限
        for (PermissionVO vo : allVOs) {
            List<PermissionVO> children = parentIdMap.get(vo.getPermissionId());
            if (children != null) {
                vo.setChildren(children);
            }
        }
        
        // 返回顶级权限（parentId为0或null的）
        return allVOs.stream()
                .filter(vo -> "0".equals(vo.getParentId()) || vo.getParentId() == null)
                .collect(Collectors.toList());
    }
}
