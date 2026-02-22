package com.yuwan.completebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuwan.completebackend.mapper.RoleMapper;
import com.yuwan.completebackend.model.entity.Role;
import com.yuwan.completebackend.model.vo.RoleVO;
import com.yuwan.completebackend.service.IRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理服务实现类
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements IRoleService {

    private final RoleMapper roleMapper;

    @Override
    @Cacheable(value = "role:list", unless = "#result == null || #result.isEmpty()")
    public List<RoleVO> getAllRoles() {
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_status", 1);
        queryWrapper.orderByAsc("sort_order");

        List<Role> roles = roleMapper.selectList(queryWrapper);
        log.debug("查询所有启用角色，数量: {}", roles.size());
        
        return roles.stream().map(role -> {
            RoleVO roleVO = new RoleVO();
            BeanUtils.copyProperties(role, roleVO);
            return roleVO;
        }).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "user:roles", key = "#userId", unless = "#result == null || #result.isEmpty()")
    public List<RoleVO> getRolesByUserId(String userId) {
        List<Role> roles = roleMapper.selectRolesByUserId(userId);
        log.debug("查询用户角色，userId: {}, 角色数量: {}", userId, roles.size());
        
        return roles.stream().map(role -> {
            RoleVO roleVO = new RoleVO();
            BeanUtils.copyProperties(role, roleVO);
            return roleVO;
        }).collect(Collectors.toList());
    }
}
