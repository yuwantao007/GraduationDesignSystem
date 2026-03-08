package com.yuwan.completebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yuwan.completebackend.exception.BusinessException;
import com.yuwan.completebackend.mapper.EnterpriseMapper;
import com.yuwan.completebackend.mapper.MajorDirectionMapper;
import com.yuwan.completebackend.mapper.MajorMapper;
import com.yuwan.completebackend.mapper.MajorTeacherMapper;
import com.yuwan.completebackend.mapper.RoleMapper;
import com.yuwan.completebackend.mapper.UserMapper;
import com.yuwan.completebackend.mapper.UserRoleMapper;
import com.yuwan.completebackend.model.dto.MajorDTO;
import com.yuwan.completebackend.model.dto.MajorDirectionDTO;
import com.yuwan.completebackend.model.entity.Enterprise;
import com.yuwan.completebackend.model.entity.Major;
import com.yuwan.completebackend.model.entity.MajorDirection;
import com.yuwan.completebackend.model.entity.MajorTeacher;
import com.yuwan.completebackend.model.entity.Role;
import com.yuwan.completebackend.model.entity.User;
import com.yuwan.completebackend.model.entity.UserRole;
import com.yuwan.completebackend.model.vo.*;
import com.yuwan.completebackend.security.SecurityUtil;
import com.yuwan.completebackend.service.IMajorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    private final MajorTeacherMapper majorTeacherMapper;
    private final EnterpriseMapper enterpriseMapper;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;

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

        // 保存企业老师关联
        if (dto.getTeacherIds() != null && !dto.getTeacherIds().isEmpty()) {
            saveTeacherRelations(major.getMajorId(), dto.getTeacherIds());
        }

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

        // 更新企业老师关联（teacherIds 不为 null 时才更新）
        if (dto.getTeacherIds() != null) {
            // 删除旧关联并重新撸入
            majorTeacherMapper.delete(
                    new LambdaQueryWrapper<MajorTeacher>().eq(MajorTeacher::getMajorId, majorId));
            if (!dto.getTeacherIds().isEmpty()) {
                saveTeacherRelations(majorId, dto.getTeacherIds());
            }
        }

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
        // 同时删除老师关联
        majorTeacherMapper.delete(
                new LambdaQueryWrapper<MajorTeacher>().eq(MajorTeacher::getMajorId, majorId));
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

    @Override
    public List<MajorVO> getMajorList(String directionId) {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(directionId)) {
            wrapper.eq(Major::getDirectionId, directionId);
        }
        wrapper.eq(Major::getStatus, 1);
        wrapper.orderByAsc(Major::getSortOrder);
        List<Major> majors = majorMapper.selectList(wrapper);

        return majors.stream()
                .map(major -> {
                    String dName = "";
                    if (StringUtils.hasText(major.getDirectionId())) {
                        MajorDirection dir = majorDirectionMapper.selectById(major.getDirectionId());
                        if (dir != null) {
                            dName = dir.getDirectionName();
                        }
                    }
                    return convertToMajorVO(major, dName);
                })
                .collect(Collectors.toList());
    }

    // ==================== 専业老师搜索 ====================

    @Override
    public List<UserVO> searchMajorTeachers(String keyword, String enterpriseId) {
        // 找到 ENTERPRISE_TEACHER 角色
        Role teacherRole = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, "ENTERPRISE_TEACHER"));
        if (teacherRole == null) {
            return new ArrayList<>();
        }

        // 找到持有该角色的所有启用用户ID
        List<String> userIds = userRoleMapper.selectList(
                        new LambdaQueryWrapper<UserRole>()
                                .eq(UserRole::getRoleId, teacherRole.getRoleId()))
                .stream()
                .map(UserRole::getUserId)
                .collect(Collectors.toList());

        if (userIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 构建基础查询条件
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<User>()
                .in(User::getUserId, userIds)
                .eq(User::getUserStatus, 1)
                .orderByAsc(User::getRealName);

        if (StringUtils.hasText(keyword)) {
            userWrapper.and(w -> w.like(User::getRealName, keyword).or().like(User::getUsername, keyword));
        }

        // -------------------------------------------------------
        // 企业隔离过滤：基于 major_teacher → major.enterprise_id 关系
        //
        // 规则：教师归属于哪个企业，通过"该教师当前被哪个企业的专业所引用"来判断。
        //   - 未被任何专业引用的教师 → 对所有企业可见（可供任意企业选择）
        //   - 仅被企业X专业引用的教师 → 只对企业X可见
        //   - 被企业Y专业引用的教师   → 从企业X的下拉列表中排除（已被占用）
        //   - 从某企业专业中移除后    → major_teacher记录消失 → 重新对所有企业可见
        // -------------------------------------------------------
        if (StringUtils.hasText(enterpriseId)) {
            // 第一步：取得「其他企业」（非本企业）的所有专业ID
            List<String> otherEnterpriseMajorIds = majorMapper.selectList(
                            new LambdaQueryWrapper<Major>()
                                    .ne(Major::getEnterpriseId, enterpriseId)
                                    .select(Major::getMajorId))
                    .stream()
                    .map(Major::getMajorId)
                    .collect(Collectors.toList());

            if (!otherEnterpriseMajorIds.isEmpty()) {
                // 第二步：找出已分配给「其他企业」专业的教师ID
                List<String> teacherIdsOccupiedByOthers = majorTeacherMapper.selectList(
                                new LambdaQueryWrapper<MajorTeacher>()
                                        .in(MajorTeacher::getMajorId, otherEnterpriseMajorIds)
                                        .select(MajorTeacher::getUserId))
                        .stream()
                        .map(MajorTeacher::getUserId)
                        .distinct()
                        .collect(Collectors.toList());

                // 第三步：排除这些已被其他企业占用的教师
                if (!teacherIdsOccupiedByOthers.isEmpty()) {
                    userWrapper.notIn(User::getUserId, teacherIdsOccupiedByOthers);
                }
            }
        }

        return userMapper.selectList(userWrapper).stream().map(user -> {
            UserVO vo = new UserVO();
            vo.setUserId(user.getUserId());
            vo.setRealName(user.getRealName());
            vo.setUsername(user.getUsername());
            vo.setUserPhone(user.getUserPhone());
            vo.setUserEmail(user.getUserEmail());
            vo.setEmployeeNo(user.getEmployeeNo());
            return vo;
        }).collect(Collectors.toList());
    }

    // ==================== Excel 导入 ====================

    /**
     * Excel 列下标常量
     */
    private static final int COL_ENTERPRISE   = 0; // 企业名称*
    private static final int COL_DIR_NAME     = 1; // 专业方向名称*
    private static final int COL_DIR_CODE     = 2; // 专业方向代码
    private static final int COL_MAJOR_NAME   = 3; // 专业名称
    private static final int COL_MAJOR_CODE   = 4; // 专业代码
    private static final int COL_DEGREE_TYPE  = 5; // 学位类型（本科/专科）
    private static final int COL_EDU_YEARS    = 6; // 学制(年)
    private static final int COL_TEACHERS     = 7; // 企业教师账号（;分隔）

    @Override
    public ImportMajorResultVO importMajors(MultipartFile file, String enterpriseId) throws Exception {
        // 文件基础校验
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
            throw new BusinessException("请上传 Excel 文件（.xlsx 或 .xls）");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException("文件大小不能超过 5MB");
        }

        // 确定导入时使用的企业范围（非管理员强制用自己的企业）
        String fixedEnterpriseId = null;
        if (!SecurityUtil.hasRole("SYSTEM_ADMIN")) {
            fixedEnterpriseId = getCurrentUserEnterpriseId();
        } else if (StringUtils.hasText(enterpriseId)) {
            Enterprise ent = enterpriseMapper.selectById(enterpriseId);
            if (ent == null) {
                throw new BusinessException("指定的企业不存在");
            }
            fixedEnterpriseId = enterpriseId;
        }
        // fixedEnterpriseId == null 时系统管理员可通过 Excel 中的"企业名称"列选择企业

        ImportMajorResultVO result = new ImportMajorResultVO();
        List<ImportMajorResultVO.ImportRowErrorVO> errors = new ArrayList<>();
        int directionCreated = 0;
        int majorCreated = 0;
        int teacherLinked = 0;
        int skipCount = 0;
        int failureCount = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();

            // 至少要有表头行(0)和一行数据(1)
            if (lastRow < 1) {
                throw new BusinessException("Excel 中没有数据行，请参照模板填写后重新上传");
            }

            for (int rowIdx = 1; rowIdx <= lastRow; rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                int rowNum = rowIdx + 1; // 展示给用户的行号（含表头第1行）
                try {
                    String enterpriseName = getCellString(row, COL_ENTERPRISE);
                    String directionName  = getCellString(row, COL_DIR_NAME);
                    String directionCode  = getCellString(row, COL_DIR_CODE);
                    String majorName      = getCellString(row, COL_MAJOR_NAME);
                    String majorCode      = getCellString(row, COL_MAJOR_CODE);
                    String degreeType     = getCellString(row, COL_DEGREE_TYPE);
                    String eduYearsStr    = getCellString(row, COL_EDU_YEARS);
                    String teacherAccounts= getCellString(row, COL_TEACHERS);

                    // 必填字段校验
                    if (!StringUtils.hasText(enterpriseName)) {
                        throw new BusinessException("企业名称不能为空");
                    }
                    if (!StringUtils.hasText(directionName)) {
                        throw new BusinessException("专业方向名称不能为空");
                    }

                    // 确定企业
                    Enterprise enterprise;
                    if (StringUtils.hasText(fixedEnterpriseId)) {
                        enterprise = enterpriseMapper.selectById(fixedEnterpriseId);
                        if (enterprise == null) {
                            throw new BusinessException("企业不存在");
                        }
                    } else {
                        LambdaQueryWrapper<Enterprise> entWrapper = new LambdaQueryWrapper<>();
                        entWrapper.eq(Enterprise::getEnterpriseName, enterpriseName);
                        enterprise = enterpriseMapper.selectOne(entWrapper);
                        if (enterprise == null) {
                            throw new BusinessException("未找到企业「" + enterpriseName + "」，请确认企业名称正确");
                        }
                    }
                    String currentEnterpriseId2 = enterprise.getEnterpriseId();

                    // 获取或创建专业方向
                    LambdaQueryWrapper<MajorDirection> dirWrapper = new LambdaQueryWrapper<>();
                    dirWrapper.eq(MajorDirection::getEnterpriseId, currentEnterpriseId2)
                              .eq(MajorDirection::getDirectionName, directionName);
                    MajorDirection direction = majorDirectionMapper.selectOne(dirWrapper);

                    if (direction == null) {
                        // 检查方向代码唯一性
                        if (StringUtils.hasText(directionCode)) {
                            LambdaQueryWrapper<MajorDirection> codeCheck = new LambdaQueryWrapper<>();
                            codeCheck.eq(MajorDirection::getEnterpriseId, currentEnterpriseId2)
                                     .eq(MajorDirection::getDirectionCode, directionCode);
                            if (majorDirectionMapper.selectCount(codeCheck) > 0) {
                                throw new BusinessException("专业方向代码「" + directionCode + "」已存在");
                            }
                        }
                        direction = new MajorDirection();
                        direction.setEnterpriseId(currentEnterpriseId2);
                        direction.setDirectionName(directionName);
                        direction.setDirectionCode(directionCode);
                        direction.setSortOrder(0);
                        direction.setStatus(1);
                        majorDirectionMapper.insert(direction);
                        directionCreated++;
                        log.info("导入创建专业方向: {}", directionName);
                    }

                    // 如果没有专业名称，本行只处理方向，跳过专业
                    if (!StringUtils.hasText(majorName)) {
                        skipCount++;
                        continue;
                    }

                    // 获取或创建专业
                    LambdaQueryWrapper<Major> majWrapper = new LambdaQueryWrapper<>();
                    majWrapper.eq(Major::getEnterpriseId, currentEnterpriseId2)
                              .eq(Major::getMajorName, majorName);
                    Major major = majorMapper.selectOne(majWrapper);

                    boolean majorIsNew = false;
                    if (major == null) {
                        // 检查专业代码唯一性
                        if (StringUtils.hasText(majorCode)) {
                            LambdaQueryWrapper<Major> codeCheck = new LambdaQueryWrapper<>();
                            codeCheck.eq(Major::getEnterpriseId, currentEnterpriseId2)
                                     .eq(Major::getMajorCode, majorCode);
                            if (majorMapper.selectCount(codeCheck) > 0) {
                                throw new BusinessException("专业代码「" + majorCode + "」已存在");
                            }
                        }
                        major = new Major();
                        major.setDirectionId(direction.getDirectionId());
                        major.setEnterpriseId(currentEnterpriseId2);
                        major.setMajorName(majorName);
                        major.setMajorCode(majorCode);
                        major.setSortOrder(0);
                        major.setStatus(1);
                        major.setEducationYears(4);
                        if (StringUtils.hasText(degreeType)) {
                            major.setDegreeType(degreeType);
                        }
                        if (StringUtils.hasText(eduYearsStr)) {
                            try {
                                major.setEducationYears(Integer.parseInt(eduYearsStr.trim()));
                            } catch (NumberFormatException ignored) { /* 保持默认值 4 */ }
                        }
                        majorMapper.insert(major);
                        majorCreated++;
                        majorIsNew = true;
                        log.info("导入创建专业: {}", majorName);
                    } else {
                        skipCount++;
                    }

                    // 处理教师账号（仅新建的专业才绑定，已有专业不重复绑定）
                    if (majorIsNew && StringUtils.hasText(teacherAccounts)) {
                        String[] accounts = teacherAccounts.split("[;；,，]");
                        for (String account : accounts) {
                            String trimmed = account.trim();
                            if (!StringUtils.hasText(trimmed)) continue;
                            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
                            userWrapper.eq(User::getUsername, trimmed);
                            User teacher = userMapper.selectOne(userWrapper);
                            if (teacher != null) {
                                // 检查是否已关联
                                LambdaQueryWrapper<MajorTeacher> mtCheck = new LambdaQueryWrapper<>();
                                mtCheck.eq(MajorTeacher::getMajorId, major.getMajorId())
                                       .eq(MajorTeacher::getUserId, teacher.getUserId());
                                if (majorTeacherMapper.selectCount(mtCheck) == 0) {
                                    MajorTeacher mt = new MajorTeacher();
                                    mt.setMajorId(major.getMajorId());
                                    mt.setUserId(teacher.getUserId());
                                    majorTeacherMapper.insert(mt);
                                    teacherLinked++;
                                }
                            } else {
                                log.warn("导入时未找到账号为「{}」的教师，已跳过", trimmed);
                            }
                        }
                    }

                } catch (BusinessException be) {
                    failureCount++;
                    ImportMajorResultVO.ImportRowErrorVO err = new ImportMajorResultVO.ImportRowErrorVO();
                    err.setRowNum(rowNum);
                    err.setErrorMsg(be.getMessage());
                    errors.add(err);
                } catch (Exception e) {
                    failureCount++;
                    ImportMajorResultVO.ImportRowErrorVO err = new ImportMajorResultVO.ImportRowErrorVO();
                    err.setRowNum(rowNum);
                    err.setErrorMsg("解析异常：" + e.getMessage());
                    errors.add(err);
                    log.error("导入第 {} 行异常", rowNum, e);
                }
            }
        }

        result.setTotalCount(directionCreated + majorCreated + skipCount + failureCount);
        result.setDirectionCreatedCount(directionCreated);
        result.setMajorCreatedCount(majorCreated);
        result.setTeacherLinkedCount(teacherLinked);
        result.setSkipCount(skipCount);
        result.setFailureCount(failureCount);
        result.setErrors(errors);

        log.info("Excel导入完成：方向创建={}, 专业创建={}, 教师关联={}, 跳过={}, 失败={}",
                directionCreated, majorCreated, teacherLinked, skipCount, failureCount);
        return result;
    }

    @Override
    public byte[] downloadImportTemplate() throws Exception {
        // 使用 HSSFWorkbook 生成 .xls 格式模板
        // HSSFWorkbook 写入的是二进制 OLE2 格式，不依赖 commons-compress/ZIP，
        // 彻底规避与 MinIO 等依赖的 commons-compress 版本冲突
        try (HSSFWorkbook workbook = new HSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // ===== 创建样式 =====
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle requiredStyle = workbook.createCellStyle();
            requiredStyle.cloneStyleFrom(headerStyle);
            requiredStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());

            CellStyle exampleStyle = workbook.createCellStyle();
            Font exampleFont = workbook.createFont();
            exampleFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            exampleStyle.setFont(exampleFont);

            // ===== 数据填写Sheet =====
            Sheet dataSheet = workbook.createSheet("专业导入数据");

            // 列宽
            int[] colWidths = {5000, 5000, 4000, 5000, 4000, 3500, 3000, 8000};
            for (int i = 0; i < colWidths.length; i++) {
                dataSheet.setColumnWidth(i, colWidths[i]);
            }

            // 表头行
            String[] headers = {
                "企业名称*", "专业方向名称*", "专业方向代码", "专业名称",
                "专业代码", "学位类型", "学制(年)", "企业教师账号(多个用;分隔)"
            };
            Row headerRow = dataSheet.createRow(0);
            headerRow.setHeightInPoints(22);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(i < 2 ? requiredStyle : headerStyle);
            }

            // 示例行
            String[][] examples = {
                {"IBM", "计算机科学与技术", "CST", "", "", "", "", ""},
                {"IBM", "软件工程", "SE", "人工智能", "AI", "本科", "4", "zhangsan;lisi"},
                {"IBM", "软件工程", "SE", "移动应用开发", "MAD", "专科", "3", ""}
            };
            for (int rowIdx = 0; rowIdx < examples.length; rowIdx++) {
                Row row = dataSheet.createRow(rowIdx + 1);
                for (int colIdx = 0; colIdx < examples[rowIdx].length; colIdx++) {
                    Cell cell = row.createCell(colIdx);
                    cell.setCellValue(examples[rowIdx][colIdx]);
                    cell.setCellStyle(exampleStyle);
                }
            }

            // ===== 说明Sheet =====
            Sheet noteSheet = workbook.createSheet("填写说明");
            noteSheet.setColumnWidth(0, 10000);
            noteSheet.setColumnWidth(1, 20000);

            String[][] notes = {
                {"字段", "说明"},
                {"企业名称*", "必填。必须是系统中已存在的企业全称"},
                {"专业方向名称*", "必填。同一企业下方向名称不能重复；若已存在则复用"},
                {"专业方向代码", "可选。若填写则同企业内不能重复"},
                {"专业名称", "可选。为空时该行只处理专业方向；若填写则创建专业"},
                {"专业代码", "可选。若填写则同企业内不能重复"},
                {"学位类型", "可选。填写 本科 或 专科"},
                {"学制(年)", "可选。填写数字（如 2、3、4）"},
                {"企业教师账号", "可选。多个账号用半角分号或逗号分隔（如 zhangsan;lisi）；账号须在系统中存在"}
            };
            for (int i = 0; i < notes.length; i++) {
                Row noteRow = noteSheet.createRow(i);
                for (int j = 0; j < notes[i].length; j++) {
                    Cell cell = noteRow.createCell(j);
                    cell.setCellValue(notes[i][j]);
                    if (i == 0) cell.setCellStyle(headerStyle);
                }
            }

            workbook.write(out);
            return out.toByteArray();
        }
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
     * 转换专业为VO（含企业老师列表）
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

        // 填充关联的企业老师列表
        List<MajorTeacher> relations = majorTeacherMapper.selectList(
                new LambdaQueryWrapper<MajorTeacher>().eq(MajorTeacher::getMajorId, major.getMajorId()));
        if (!relations.isEmpty()) {
            List<String> teacherUserIds = relations.stream()
                    .map(MajorTeacher::getUserId)
                    .collect(Collectors.toList());
            List<UserVO> teachers = userMapper.selectList(
                    new LambdaQueryWrapper<User>().in(User::getUserId, teacherUserIds)
                            .orderByAsc(User::getRealName))
                    .stream().map(user -> {
                        UserVO uvo = new UserVO();
                        uvo.setUserId(user.getUserId());
                        uvo.setRealName(user.getRealName());
                        uvo.setUsername(user.getUsername());
                        uvo.setUserPhone(user.getUserPhone());
                        uvo.setUserEmail(user.getUserEmail());
                        uvo.setEmployeeNo(user.getEmployeeNo());
                        return uvo;
                    }).collect(Collectors.toList());
            vo.setTeachers(teachers);
        } else {
            vo.setTeachers(new ArrayList<>());
        }

        return vo;
    }

    /**
     * 批量保存专业-老师关联关系
     */
    private void saveTeacherRelations(String majorId, List<String> teacherIds) {
        for (String userId : teacherIds) {
            MajorTeacher relation = new MajorTeacher();
            relation.setMajorId(majorId);
            relation.setUserId(userId);
            relation.setCreateTime(new Date());
            majorTeacherMapper.insert(relation);
        }
    }

    /**
     * 读取 Excel 单元格的字符串值（统一处理数值型单元格）
     */
    private String getCellString(Row row, int colIdx) {
        Cell cell = row.getCell(colIdx);
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // 避免 4.0 这类显示，直接取整数字符串
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val)) {
                    return String.valueOf((long) val);
                }
                return String.valueOf(val);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    /**
     * 判断行是否全为空（跳过空行）
     */
    private boolean isRowEmpty(Row row) {
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK
                    && StringUtils.hasText(getCellString(row, i))) {
                return false;
            }
        }
        return true;
    }
}
