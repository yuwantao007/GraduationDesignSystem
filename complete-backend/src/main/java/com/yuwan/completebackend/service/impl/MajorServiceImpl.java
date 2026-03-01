package com.yuwan.completebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yuwan.completebackend.exception.BusinessException;
import com.yuwan.completebackend.mapper.EnterpriseMapper;
import com.yuwan.completebackend.mapper.MajorDirectionMapper;
import com.yuwan.completebackend.mapper.MajorMapper;
import com.yuwan.completebackend.mapper.UserMapper;
import com.yuwan.completebackend.model.dto.MajorDTO;
import com.yuwan.completebackend.model.dto.MajorDirectionDTO;
import com.yuwan.completebackend.model.entity.Enterprise;
import com.yuwan.completebackend.model.entity.Major;
import com.yuwan.completebackend.model.entity.MajorDirection;
import com.yuwan.completebackend.model.entity.User;
import com.yuwan.completebackend.model.vo.*;
import com.yuwan.completebackend.security.SecurityUtil;
import com.yuwan.completebackend.service.IMajorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 专业管理服务实现类
 * 提供专业方向和专业的CRUD功能实现
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class MajorServiceImpl implements IMajorService {

    private final MajorDirectionMapper majorDirectionMapper;
    private final MajorMapper majorMapper;
    private final EnterpriseMapper enterpriseMapper;
    private final UserMapper userMapper;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // ==================== 专业方向管理 ====================

    @Override
    public List<MajorTreeVO> getMajorTree(String enterpriseId, Integer status) {
        // 获取有效企业ID（系统管理员未指定时返回null表示查所有）
        String validEnterpriseId = getValidEnterpriseId(enterpriseId);

        // 查询企业列表
        List<Enterprise> enterprises = new ArrayList<>();
        if (StringUtils.hasText(validEnterpriseId)) {
            // 指定企业：查询单个企业
            Enterprise enterprise = enterpriseMapper.selectById(validEnterpriseId);
            if (enterprise != null) {
                enterprises.add(enterprise);
            }
        } else {
            // 未指定企业：查询所有启用的企业
            LambdaQueryWrapper<Enterprise> enterpriseWrapper = new LambdaQueryWrapper<>();
            enterpriseWrapper.eq(Enterprise::getEnterpriseStatus, 1);
            enterpriseWrapper.orderByAsc(Enterprise::getEnterpriseName);
            enterprises = enterpriseMapper.selectList(enterpriseWrapper);
        }

        // 查询所有相关的专业方向
        LambdaQueryWrapper<MajorDirection> directionWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(validEnterpriseId)) {
            directionWrapper.eq(MajorDirection::getEnterpriseId, validEnterpriseId);
        }
        if (status != null) {
            directionWrapper.eq(MajorDirection::getStatus, status);
        }
        directionWrapper.orderByAsc(MajorDirection::getSortOrder);
        List<MajorDirection> allDirections = majorDirectionMapper.selectList(directionWrapper);

        // 查询所有相关的专业
        LambdaQueryWrapper<Major> majorWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(validEnterpriseId)) {
            majorWrapper.eq(Major::getEnterpriseId, validEnterpriseId);
        }
        if (status != null) {
            majorWrapper.eq(Major::getStatus, status);
        }
        majorWrapper.orderByAsc(Major::getSortOrder);
        List<Major> allMajors = majorMapper.selectList(majorWrapper);

        // 构建三级树型结构：企业 -> 专业方向 -> 专业
        List<MajorTreeVO> tree = new ArrayList<>();
        for (Enterprise enterprise : enterprises) {
            MajorTreeVO enterpriseNode = convertToTreeVO(enterprise);
            
            // 查找该企业下的专业方向
            List<MajorDirection> enterpriseDirections = allDirections.stream()
                    .filter(d -> enterprise.getEnterpriseId().equals(d.getEnterpriseId()))
                    .collect(Collectors.toList());
            
            List<MajorTreeVO> directionNodes = new ArrayList<>();
            for (MajorDirection direction : enterpriseDirections) {
                MajorTreeVO directionNode = convertToTreeVO(direction);
                
                // 查找该方向下的专业
                List<MajorTreeVO> majorNodes = allMajors.stream()
                        .filter(m -> direction.getDirectionId().equals(m.getDirectionId()))
                        .map(this::convertToTreeVO)
                        .collect(Collectors.toList());
                
                directionNode.setChildren(majorNodes);
                directionNode.setIsLeaf(majorNodes.isEmpty());
                directionNodes.add(directionNode);
            }
            
            enterpriseNode.setChildren(directionNodes);
            enterpriseNode.setIsLeaf(directionNodes.isEmpty());
            tree.add(enterpriseNode);
        }

        return tree;
    }

    @Override
    public MajorDirectionVO addDirection(MajorDirectionDTO dto) {
        // 获取企业ID：优先使用DTO中的enterpriseId（系统管理员指定），否则从当前用户获取
        String enterpriseId = StringUtils.hasText(dto.getEnterpriseId()) 
                ? dto.getEnterpriseId() 
                : getCurrentUserEnterpriseId();

        // 检查专业方向名称是否重复
        if (isDirectionNameExists(enterpriseId, dto.getDirectionName(), null)) {
            throw new BusinessException("专业方向名称已存在");
        }

        // 检查专业方向代码是否重复
        if (StringUtils.hasText(dto.getDirectionCode()) 
                && isDirectionCodeExists(enterpriseId, dto.getDirectionCode(), null)) {
            throw new BusinessException("专业方向代码已存在");
        }

        // 创建专业方向实体
        MajorDirection direction = new MajorDirection();
        BeanUtils.copyProperties(dto, direction);
        direction.setEnterpriseId(enterpriseId);
        
        // 设置默认值
        if (direction.getSortOrder() == null) {
            direction.setSortOrder(0);
        }
        if (direction.getStatus() == null) {
            direction.setStatus(1);
        }

        // 如果有负责人ID，查询负责人姓名
        if (StringUtils.hasText(dto.getLeaderId())) {
            User leader = userMapper.selectById(dto.getLeaderId());
            if (leader != null) {
                direction.setLeaderName(leader.getRealName());
            }
        }

        // 保存到数据库
        majorDirectionMapper.insert(direction);
        log.info("创建专业方向成功，方向ID: {}, 企业ID: {}", direction.getDirectionId(), enterpriseId);

        return convertToDirectionVO(direction);
    }

    @Override
    public MajorDirectionVO updateDirection(String directionId, MajorDirectionDTO dto) {
        // 查询专业方向是否存在
        MajorDirection direction = majorDirectionMapper.selectById(directionId);
        if (direction == null) {
            throw new BusinessException("专业方向不存在");
        }

        // 验证权限：只能编辑自己企业的数据
        validateEnterprisePermission(direction.getEnterpriseId());

        // 检查名称是否重复
        if (StringUtils.hasText(dto.getDirectionName()) 
                && !dto.getDirectionName().equals(direction.getDirectionName())
                && isDirectionNameExists(direction.getEnterpriseId(), dto.getDirectionName(), directionId)) {
            throw new BusinessException("专业方向名称已存在");
        }

        // 检查代码是否重复
        if (StringUtils.hasText(dto.getDirectionCode()) 
                && !dto.getDirectionCode().equals(direction.getDirectionCode())
                && isDirectionCodeExists(direction.getEnterpriseId(), dto.getDirectionCode(), directionId)) {
            throw new BusinessException("专业方向代码已存在");
        }

        // 更新字段
        if (StringUtils.hasText(dto.getDirectionName())) {
            direction.setDirectionName(dto.getDirectionName());
        }
        if (dto.getDirectionCode() != null) {
            direction.setDirectionCode(dto.getDirectionCode());
        }
        if (dto.getDescription() != null) {
            direction.setDescription(dto.getDescription());
        }
        if (dto.getSortOrder() != null) {
            direction.setSortOrder(dto.getSortOrder());
        }
        if (dto.getStatus() != null) {
            direction.setStatus(dto.getStatus());
        }
        
        // 更新负责人
        if (dto.getLeaderId() != null) {
            direction.setLeaderId(dto.getLeaderId());
            if (StringUtils.hasText(dto.getLeaderId())) {
                User leader = userMapper.selectById(dto.getLeaderId());
                direction.setLeaderName(leader != null ? leader.getRealName() : null);
            } else {
                direction.setLeaderName(null);
            }
        }

        majorDirectionMapper.updateById(direction);
        log.info("更新专业方向成功，方向ID: {}", directionId);

        return convertToDirectionVO(direction);
    }

    @Override
    public void deleteDirection(String directionId) {
        // 查询专业方向是否存在
        MajorDirection direction = majorDirectionMapper.selectById(directionId);
        if (direction == null) {
            throw new BusinessException("专业方向不存在");
        }

        // 验证权限
        validateEnterprisePermission(direction.getEnterpriseId());

        // 检查是否有子专业
        Integer majorCount = majorMapper.countByDirectionId(directionId);
        if (majorCount > 0) {
            throw new BusinessException("该专业方向下存在专业，请先删除所有专业后再删除方向");
        }

        // 逻辑删除
        majorDirectionMapper.deleteById(directionId);
        log.info("删除专业方向成功，方向ID: {}", directionId);
    }

    @Override
    public MajorDirectionVO getDirectionDetail(String directionId) {
        MajorDirection direction = majorDirectionMapper.selectById(directionId);
        if (direction == null) {
            throw new BusinessException("专业方向不存在");
        }
        return convertToDirectionVO(direction);
    }

    // ==================== 专业管理 ====================

    @Override
    public MajorVO addMajor(MajorDTO dto) {
        // 验证专业方向是否存在
        MajorDirection direction = majorDirectionMapper.selectById(dto.getDirectionId());
        if (direction == null) {
            throw new BusinessException("专业方向不存在");
        }

        // 验证权限
        validateEnterprisePermission(direction.getEnterpriseId());

        // 检查专业名称是否重复
        if (isMajorNameExists(direction.getEnterpriseId(), dto.getMajorName(), null)) {
            throw new BusinessException("专业名称已存在");
        }

        // 检查专业代码是否重复
        if (StringUtils.hasText(dto.getMajorCode()) 
                && isMajorCodeExists(direction.getEnterpriseId(), dto.getMajorCode(), null)) {
            throw new BusinessException("专业代码已存在");
        }

        // 创建专业实体
        Major major = new Major();
        BeanUtils.copyProperties(dto, major);
        major.setEnterpriseId(direction.getEnterpriseId());
        
        // 设置默认值
        if (major.getSortOrder() == null) {
            major.setSortOrder(0);
        }
        if (major.getStatus() == null) {
            major.setStatus(1);
        }
        if (major.getEducationYears() == null) {
            major.setEducationYears(4);
        }

        // 保存到数据库
        majorMapper.insert(major);
        log.info("创建专业成功，专业ID: {}, 方向ID: {}", major.getMajorId(), dto.getDirectionId());

        return convertToMajorVO(major, direction.getDirectionName());
    }

    @Override
    public MajorVO updateMajor(String majorId, MajorDTO dto) {
        // 查询专业是否存在
        Major major = majorMapper.selectById(majorId);
        if (major == null) {
            throw new BusinessException("专业不存在");
        }

        // 验证权限
        validateEnterprisePermission(major.getEnterpriseId());

        // 如果更换了专业方向
        String directionName = null;
        if (StringUtils.hasText(dto.getDirectionId()) && !dto.getDirectionId().equals(major.getDirectionId())) {
            MajorDirection newDirection = majorDirectionMapper.selectById(dto.getDirectionId());
            if (newDirection == null) {
                throw new BusinessException("目标专业方向不存在");
            }
            // 确保是同一企业
            if (!newDirection.getEnterpriseId().equals(major.getEnterpriseId())) {
                throw new BusinessException("不能移动到其他企业的专业方向");
            }
            major.setDirectionId(dto.getDirectionId());
            directionName = newDirection.getDirectionName();
        }

        // 检查名称是否重复
        if (StringUtils.hasText(dto.getMajorName()) 
                && !dto.getMajorName().equals(major.getMajorName())
                && isMajorNameExists(major.getEnterpriseId(), dto.getMajorName(), majorId)) {
            throw new BusinessException("专业名称已存在");
        }

        // 检查代码是否重复
        if (StringUtils.hasText(dto.getMajorCode()) 
                && !dto.getMajorCode().equals(major.getMajorCode())
                && isMajorCodeExists(major.getEnterpriseId(), dto.getMajorCode(), majorId)) {
            throw new BusinessException("专业代码已存在");
        }

        // 更新字段
        if (StringUtils.hasText(dto.getMajorName())) {
            major.setMajorName(dto.getMajorName());
        }
        if (dto.getMajorCode() != null) {
            major.setMajorCode(dto.getMajorCode());
        }
        if (dto.getDegreeType() != null) {
            major.setDegreeType(dto.getDegreeType());
        }
        if (dto.getEducationYears() != null) {
            major.setEducationYears(dto.getEducationYears());
        }
        if (dto.getDescription() != null) {
            major.setDescription(dto.getDescription());
        }
        if (dto.getSortOrder() != null) {
            major.setSortOrder(dto.getSortOrder());
        }
        if (dto.getStatus() != null) {
            major.setStatus(dto.getStatus());
        }

        majorMapper.updateById(major);
        log.info("更新专业成功，专业ID: {}", majorId);

        // 获取方向名称
        if (directionName == null) {
            MajorDirection direction = majorDirectionMapper.selectById(major.getDirectionId());
            directionName = direction != null ? direction.getDirectionName() : null;
        }

        return convertToMajorVO(major, directionName);
    }

    @Override
    public void deleteMajor(String majorId) {
        // 查询专业是否存在
        Major major = majorMapper.selectById(majorId);
        if (major == null) {
            throw new BusinessException("专业不存在");
        }

        // 验证权限
        validateEnterprisePermission(major.getEnterpriseId());

        // TODO: 后续Topic表添加major_id字段后，需增加课题关联检查
        // 当前Topic表暂无major_id字段，跳过课题检查

        // 逻辑删除
        majorMapper.deleteById(majorId);
        log.info("删除专业成功，专业ID: {}", majorId);
    }

    @Override
    public MajorVO getMajorDetail(String majorId) {
        Major major = majorMapper.selectById(majorId);
        if (major == null) {
            throw new BusinessException("专业不存在");
        }

        MajorDirection direction = majorDirectionMapper.selectById(major.getDirectionId());
        String directionName = direction != null ? direction.getDirectionName() : null;

        return convertToMajorVO(major, directionName);
    }

    // ==================== 状态管理 ====================

    @Override
    public void updateDirectionStatus(String directionId, Integer status) {
        // 查询专业方向是否存在
        MajorDirection direction = majorDirectionMapper.selectById(directionId);
        if (direction == null) {
            throw new BusinessException("专业方向不存在");
        }

        // 验证权限
        validateEnterprisePermission(direction.getEnterpriseId());

        // 更新状态
        direction.setStatus(status);
        majorDirectionMapper.updateById(direction);

        // 如果禁用，级联禁用所有子专业
        if (status == 0) {
            LambdaUpdateWrapper<Major> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Major::getDirectionId, directionId);
            updateWrapper.set(Major::getStatus, 0);
            majorMapper.update(null, updateWrapper);
            log.info("禁用专业方向并级联禁用子专业，方向ID: {}", directionId);
        } else {
            log.info("启用专业方向，方向ID: {}", directionId);
        }
    }

    @Override
    public void updateMajorStatus(String majorId, Integer status) {
        // 查询专业是否存在
        Major major = majorMapper.selectById(majorId);
        if (major == null) {
            throw new BusinessException("专业不存在");
        }

        // 验证权限
        validateEnterprisePermission(major.getEnterpriseId());

        // 启用专业时，检查所属方向是否为禁用状态
        if (status == 1) {
            MajorDirection direction = majorDirectionMapper.selectById(major.getDirectionId());
            if (direction != null && direction.getStatus() == 0) {
                throw new BusinessException("所属专业方向已禁用，请先启用专业方向");
            }
        }

        // 更新状态
        major.setStatus(status);
        majorMapper.updateById(major);
        log.info("更新专业状态，专业ID: {}, 状态: {}", majorId, status);
    }

    // ==================== 级联选择器 ====================

    @Override
    public List<MajorCascadeVO> getCascadeData(String enterpriseId) {
        // 获取有效的企业ID
        String validEnterpriseId = getValidEnterpriseId(enterpriseId);

        // 查询启用的专业方向
        LambdaQueryWrapper<MajorDirection> directionWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(validEnterpriseId)) {
            directionWrapper.eq(MajorDirection::getEnterpriseId, validEnterpriseId);
        }
        directionWrapper.eq(MajorDirection::getStatus, 1);
        directionWrapper.orderByAsc(MajorDirection::getSortOrder);
        List<MajorDirection> directions = majorDirectionMapper.selectList(directionWrapper);

        // 查询启用的专业
        LambdaQueryWrapper<Major> majorWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(validEnterpriseId)) {
            majorWrapper.eq(Major::getEnterpriseId, validEnterpriseId);
        }
        majorWrapper.eq(Major::getStatus, 1);
        majorWrapper.orderByAsc(Major::getSortOrder);
        List<Major> majors = majorMapper.selectList(majorWrapper);

        // 构建级联数据
        List<MajorCascadeVO> cascade = new ArrayList<>();
        for (MajorDirection direction : directions) {
            MajorCascadeVO directionOption = new MajorCascadeVO();
            directionOption.setValue(direction.getDirectionId());
            directionOption.setLabel(direction.getDirectionName());
            directionOption.setDisabled(false);

            // 查找该方向下的专业
            List<MajorCascadeVO> majorOptions = majors.stream()
                    .filter(m -> direction.getDirectionId().equals(m.getDirectionId()))
                    .map(m -> {
                        MajorCascadeVO option = new MajorCascadeVO();
                        option.setValue(m.getMajorId());
                        option.setLabel(m.getMajorName());
                        option.setDisabled(false);
                        option.setChildren(null);
                        return option;
                    })
                    .collect(Collectors.toList());

            directionOption.setChildren(majorOptions.isEmpty() ? null : majorOptions);
            cascade.add(directionOption);
        }

        return cascade;
    }

    @Override
    public List<MajorDirectionVO> getDirectionList(String enterpriseId) {
        // 获取有效的企业ID
        String validEnterpriseId = getValidEnterpriseId(enterpriseId);

        // 查询启用的专业方向
        LambdaQueryWrapper<MajorDirection> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(validEnterpriseId)) {
            wrapper.eq(MajorDirection::getEnterpriseId, validEnterpriseId);
        }
        wrapper.eq(MajorDirection::getStatus, 1);
        wrapper.orderByAsc(MajorDirection::getSortOrder);
        List<MajorDirection> directions = majorDirectionMapper.selectList(wrapper);

        return directions.stream()
                .map(this::convertToDirectionVO)
                .collect(Collectors.toList());
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取有效的企业ID
     * 系统管理员可以指定企业ID或不指定（返回null表示查所有）
     * 非管理员使用当前用户企业
     */
    private String getValidEnterpriseId(String enterpriseId) {
        // 系统管理员处理
        if (SecurityUtil.hasRole("SYSTEM_ADMIN")) {
            if (StringUtils.hasText(enterpriseId)) {
                // 指定了企业ID，验证企业是否存在
                Enterprise enterprise = enterpriseMapper.selectById(enterpriseId);
                if (enterprise == null) {
                    throw new BusinessException("指定的企业不存在");
                }
                return enterpriseId;
            }
            // 未指定企业ID，返回null表示查询所有企业
            return null;
        }
        // 其他用户使用当前用户企业
        return getCurrentUserEnterpriseId();
    }

    /**
     * 获取当前用户所属企业ID
     * 从用户关联的方向或其他方式获取
     */
    private String getCurrentUserEnterpriseId() {
        String userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        // 系统管理员需要指定企业ID
        if (SecurityUtil.hasRole("SYSTEM_ADMIN")) {
            throw new BusinessException("系统管理员请指定企业ID");
        }

        // 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 如果用户有关联的专业方向，通过方向获取企业
        if (StringUtils.hasText(user.getDirectionId())) {
            MajorDirection direction = majorDirectionMapper.selectById(user.getDirectionId());
            if (direction != null) {
                return direction.getEnterpriseId();
            }
        }

        // 尝试通过用户department字段查找企业
        if (StringUtils.hasText(user.getDepartment())) {
            LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Enterprise::getEnterpriseName, user.getDepartment());
            Enterprise enterprise = enterpriseMapper.selectOne(wrapper);
            if (enterprise != null) {
                return enterprise.getEnterpriseId();
            }
        }

        throw new BusinessException("无法确定用户所属企业，请联系管理员配置");
    }

    /**
     * 验证企业权限
     * 确保只能操作自己企业的数据
     */
    private void validateEnterprisePermission(String enterpriseId) {
        // 系统管理员可以操作所有企业
        if (SecurityUtil.hasRole("SYSTEM_ADMIN")) {
            return;
        }

        try {
            String userEnterpriseId = getCurrentUserEnterpriseId();
            if (!enterpriseId.equals(userEnterpriseId)) {
                throw new BusinessException("无权操作其他企业的数据");
            }
        } catch (BusinessException e) {
            // 如果无法获取用户企业ID，也不允许操作
            throw new BusinessException("无法验证企业权限：" + e.getMessage());
        }
    }

    /**
     * 检查专业方向名称是否存在
     */
    private boolean isDirectionNameExists(String enterpriseId, String directionName, String excludeId) {
        LambdaQueryWrapper<MajorDirection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MajorDirection::getEnterpriseId, enterpriseId);
        wrapper.eq(MajorDirection::getDirectionName, directionName);
        if (StringUtils.hasText(excludeId)) {
            wrapper.ne(MajorDirection::getDirectionId, excludeId);
        }
        return majorDirectionMapper.selectCount(wrapper) > 0;
    }

    /**
     * 检查专业方向代码是否存在
     */
    private boolean isDirectionCodeExists(String enterpriseId, String directionCode, String excludeId) {
        LambdaQueryWrapper<MajorDirection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MajorDirection::getEnterpriseId, enterpriseId);
        wrapper.eq(MajorDirection::getDirectionCode, directionCode);
        if (StringUtils.hasText(excludeId)) {
            wrapper.ne(MajorDirection::getDirectionId, excludeId);
        }
        return majorDirectionMapper.selectCount(wrapper) > 0;
    }

    /**
     * 检查专业名称是否存在
     */
    private boolean isMajorNameExists(String enterpriseId, String majorName, String excludeId) {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Major::getEnterpriseId, enterpriseId);
        wrapper.eq(Major::getMajorName, majorName);
        if (StringUtils.hasText(excludeId)) {
            wrapper.ne(Major::getMajorId, excludeId);
        }
        return majorMapper.selectCount(wrapper) > 0;
    }

    /**
     * 检查专业代码是否存在
     */
    private boolean isMajorCodeExists(String enterpriseId, String majorCode, String excludeId) {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Major::getEnterpriseId, enterpriseId);
        wrapper.eq(Major::getMajorCode, majorCode);
        if (StringUtils.hasText(excludeId)) {
            wrapper.ne(Major::getMajorId, excludeId);
        }
        return majorMapper.selectCount(wrapper) > 0;
    }

    /**
     * 转换企业为树型节点VO
     */
    private MajorTreeVO convertToTreeVO(Enterprise enterprise) {
        MajorTreeVO vo = new MajorTreeVO();
        vo.setId(enterprise.getEnterpriseId());
        vo.setLabel(enterprise.getEnterpriseName());
        vo.setType("enterprise");
        vo.setCode(enterprise.getEnterpriseCode());
        vo.setStatus(enterprise.getEnterpriseStatus());
        vo.setStatusDesc(enterprise.getEnterpriseStatus() == 1 ? "启用" : "禁用");
        vo.setIsLeaf(false);
        return vo;
    }

    /**
     * 转换专业方向为树型节点VO
     */
    private MajorTreeVO convertToTreeVO(MajorDirection direction) {
        MajorTreeVO vo = new MajorTreeVO();
        vo.setId(direction.getDirectionId());
        vo.setLabel(direction.getDirectionName());
        vo.setType("direction");
        vo.setCode(direction.getDirectionCode());
        vo.setStatus(direction.getStatus());
        vo.setStatusDesc(direction.getStatus() == 1 ? "启用" : "禁用");
        vo.setSortOrder(direction.getSortOrder());
        vo.setDescription(direction.getDescription());
        vo.setLeaderName(direction.getLeaderName());
        vo.setIsLeaf(false);
        return vo;
    }

    /**
     * 转换专业为树型节点VO
     */
    private MajorTreeVO convertToTreeVO(Major major) {
        MajorTreeVO vo = new MajorTreeVO();
        vo.setId(major.getMajorId());
        vo.setLabel(major.getMajorName());
        vo.setType("major");
        vo.setCode(major.getMajorCode());
        vo.setStatus(major.getStatus());
        vo.setStatusDesc(major.getStatus() == 1 ? "启用" : "禁用");
        vo.setSortOrder(major.getSortOrder());
        vo.setDescription(major.getDescription());
        vo.setDegreeType(major.getDegreeType());
        vo.setEducationYears(major.getEducationYears());
        vo.setIsLeaf(true);
        vo.setChildren(null);
        return vo;
    }

    /**
     * 转换专业方向为VO
     */
    private MajorDirectionVO convertToDirectionVO(MajorDirection direction) {
        MajorDirectionVO vo = new MajorDirectionVO();
        BeanUtils.copyProperties(direction, vo);
        vo.setStatusDesc(direction.getStatus() == 1 ? "启用" : "禁用");
        
        // 获取企业名称
        if (StringUtils.hasText(direction.getEnterpriseId())) {
            Enterprise enterprise = enterpriseMapper.selectById(direction.getEnterpriseId());
            if (enterprise != null) {
                vo.setEnterpriseName(enterprise.getEnterpriseName());
            }
        }

        // 统计下属专业数量
        Integer majorCount = majorMapper.countByDirectionId(direction.getDirectionId());
        vo.setMajorCount(majorCount);

        // 格式化时间
        if (direction.getCreateTime() != null) {
            vo.setCreateTime(DATE_FORMAT.format(direction.getCreateTime()));
        }
        if (direction.getUpdateTime() != null) {
            vo.setUpdateTime(DATE_FORMAT.format(direction.getUpdateTime()));
        }

        return vo;
    }

    /**
     * 转换专业为VO
     */
    private MajorVO convertToMajorVO(Major major, String directionName) {
        MajorVO vo = new MajorVO();
        BeanUtils.copyProperties(major, vo);
        vo.setDirectionName(directionName);
        vo.setStatusDesc(major.getStatus() == 1 ? "启用" : "禁用");

        // 获取企业名称
        if (StringUtils.hasText(major.getEnterpriseId())) {
            Enterprise enterprise = enterpriseMapper.selectById(major.getEnterpriseId());
            if (enterprise != null) {
                vo.setEnterpriseName(enterprise.getEnterpriseName());
            }
        }

        // 格式化时间
        if (major.getCreateTime() != null) {
            vo.setCreateTime(DATE_FORMAT.format(major.getCreateTime()));
        }
        if (major.getUpdateTime() != null) {
            vo.setUpdateTime(DATE_FORMAT.format(major.getUpdateTime()));
        }

        return vo;
    }
}
