package com.yuwan.completebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.exception.BusinessException;
import com.yuwan.completebackend.mapper.EnterpriseMapper;
import com.yuwan.completebackend.model.dto.CreateEnterpriseDTO;
import com.yuwan.completebackend.model.dto.UpdateEnterpriseDTO;
import com.yuwan.completebackend.model.entity.Enterprise;
import com.yuwan.completebackend.model.vo.EnterpriseQueryVO;
import com.yuwan.completebackend.model.vo.EnterpriseVO;
import com.yuwan.completebackend.service.IEnterpriseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 企业管理服务实现类
 * 提供企业CRUD功能实现
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-21
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class EnterpriseServiceImpl implements IEnterpriseService {

    private final EnterpriseMapper enterpriseMapper;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public EnterpriseVO createEnterprise(CreateEnterpriseDTO createDTO) {
        // 检查企业名称是否已存在
        LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Enterprise::getEnterpriseName, createDTO.getEnterpriseName());
        if (enterpriseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("企业名称已存在");
        }

        // 检查企业编码是否已存在
        if (StringUtils.hasText(createDTO.getEnterpriseCode())) {
            LambdaQueryWrapper<Enterprise> codeWrapper = new LambdaQueryWrapper<>();
            codeWrapper.eq(Enterprise::getEnterpriseCode, createDTO.getEnterpriseCode());
            if (enterpriseMapper.selectCount(codeWrapper) > 0) {
                throw new BusinessException("企业编码已存在");
            }
        }

        // 创建企业实体
        Enterprise enterprise = new Enterprise();
        BeanUtils.copyProperties(createDTO, enterprise);
        enterprise.setEnterpriseStatus(1); // 默认正常状态

        // 保存到数据库
        enterpriseMapper.insert(enterprise);
        log.info("创建企业成功，企业ID: {}", enterprise.getEnterpriseId());

        return convertToVO(enterprise);
    }

    @Override
    public EnterpriseVO updateEnterprise(String enterpriseId, UpdateEnterpriseDTO updateDTO) {
        // 查询企业是否存在
        Enterprise enterprise = enterpriseMapper.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException("企业不存在");
        }

        // 检查企业名称是否重复
        if (StringUtils.hasText(updateDTO.getEnterpriseName()) 
                && !updateDTO.getEnterpriseName().equals(enterprise.getEnterpriseName())) {
            LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Enterprise::getEnterpriseName, updateDTO.getEnterpriseName())
                   .ne(Enterprise::getEnterpriseId, enterpriseId);
            if (enterpriseMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("企业名称已存在");
            }
        }

        // 检查企业编码是否重复
        if (StringUtils.hasText(updateDTO.getEnterpriseCode()) 
                && !updateDTO.getEnterpriseCode().equals(enterprise.getEnterpriseCode())) {
            LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Enterprise::getEnterpriseCode, updateDTO.getEnterpriseCode())
                   .ne(Enterprise::getEnterpriseId, enterpriseId);
            if (enterpriseMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("企业编码已存在");
            }
        }

        // 更新企业信息
        if (StringUtils.hasText(updateDTO.getEnterpriseName())) {
            enterprise.setEnterpriseName(updateDTO.getEnterpriseName());
        }
        if (StringUtils.hasText(updateDTO.getEnterpriseCode())) {
            enterprise.setEnterpriseCode(updateDTO.getEnterpriseCode());
        }
        if (StringUtils.hasText(updateDTO.getContactPerson())) {
            enterprise.setContactPerson(updateDTO.getContactPerson());
        }
        if (StringUtils.hasText(updateDTO.getContactPhone())) {
            enterprise.setContactPhone(updateDTO.getContactPhone());
        }
        if (StringUtils.hasText(updateDTO.getContactEmail())) {
            enterprise.setContactEmail(updateDTO.getContactEmail());
        }
        if (StringUtils.hasText(updateDTO.getAddress())) {
            enterprise.setAddress(updateDTO.getAddress());
        }
        if (StringUtils.hasText(updateDTO.getDescription())) {
            enterprise.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getEnterpriseStatus() != null) {
            enterprise.setEnterpriseStatus(updateDTO.getEnterpriseStatus());
        }

        enterpriseMapper.updateById(enterprise);
        log.info("更新企业成功，企业ID: {}", enterpriseId);

        return convertToVO(enterprise);
    }

    @Override
    public EnterpriseVO getEnterpriseDetail(String enterpriseId) {
        Enterprise enterprise = enterpriseMapper.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException("企业不存在");
        }
        return convertToVO(enterprise);
    }

    @Override
    public PageResult<EnterpriseVO> getEnterpriseList(EnterpriseQueryVO queryVO) {
        // 构建查询条件
        LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(queryVO.getEnterpriseName())) {
            wrapper.like(Enterprise::getEnterpriseName, queryVO.getEnterpriseName());
        }
        if (StringUtils.hasText(queryVO.getEnterpriseCode())) {
            wrapper.eq(Enterprise::getEnterpriseCode, queryVO.getEnterpriseCode());
        }
        if (queryVO.getEnterpriseStatus() != null) {
            wrapper.eq(Enterprise::getEnterpriseStatus, queryVO.getEnterpriseStatus());
        }
        wrapper.orderByDesc(Enterprise::getCreateTime);

        // 分页查询
        Page<Enterprise> page = new Page<>(queryVO.getPageNum(), queryVO.getPageSize());
        Page<Enterprise> result = enterpriseMapper.selectPage(page, wrapper);

        // 转换结果
        List<EnterpriseVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, result.getTotal(), queryVO.getPageNum().longValue(), queryVO.getPageSize().longValue());
    }

    @Override
    public List<EnterpriseVO> getAllEnterprises() {
        LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Enterprise::getEnterpriseStatus, 1)
               .orderByAsc(Enterprise::getEnterpriseName);
        
        List<Enterprise> enterprises = enterpriseMapper.selectList(wrapper);
        return enterprises.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteEnterprise(String enterpriseId) {
        Enterprise enterprise = enterpriseMapper.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException("企业不存在");
        }
        
        // TODO: 检查是否有关联的课题或用户

        enterpriseMapper.deleteById(enterpriseId);
        log.info("删除企业成功，企业ID: {}", enterpriseId);
    }

    @Override
    public void updateEnterpriseStatus(String enterpriseId, Integer status) {
        Enterprise enterprise = enterpriseMapper.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException("企业不存在");
        }

        enterprise.setEnterpriseStatus(status);
        enterpriseMapper.updateById(enterprise);
        log.info("更新企业状态成功，企业ID: {}, 状态: {}", enterpriseId, status);
    }

    /**
     * 将企业实体转换为VO
     *
     * @param enterprise 企业实体
     * @return 企业VO
     */
    private EnterpriseVO convertToVO(Enterprise enterprise) {
        EnterpriseVO vo = new EnterpriseVO();
        BeanUtils.copyProperties(enterprise, vo);
        
        // 状态描述
        vo.setEnterpriseStatusDesc(enterprise.getEnterpriseStatus() == 1 ? "正常" : "禁用");
        
        // 格式化时间
        if (enterprise.getCreateTime() != null) {
            vo.setCreateTime(DATE_FORMAT.format(enterprise.getCreateTime()));
        }
        if (enterprise.getUpdateTime() != null) {
            vo.setUpdateTime(DATE_FORMAT.format(enterprise.getUpdateTime()));
        }
        
        return vo;
    }
}
