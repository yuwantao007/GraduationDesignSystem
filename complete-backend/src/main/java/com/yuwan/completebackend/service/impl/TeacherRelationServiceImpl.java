package com.yuwan.completebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuwan.completebackend.exception.BusinessException;
import com.yuwan.completebackend.mapper.*;
import com.yuwan.completebackend.model.dto.TeacherRelationDTO;
import com.yuwan.completebackend.model.dto.UnivTeacherMajorDTO;
import com.yuwan.completebackend.model.entity.*;
import com.yuwan.completebackend.model.vo.TeacherCoverageVO;
import com.yuwan.completebackend.model.vo.TeacherRelationVO;
import com.yuwan.completebackend.model.vo.UnivTeacherMajorVO;
import com.yuwan.completebackend.service.ITeacherRelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 教师配对管理服务实现类
 * <p>
 * 提供高校教师与企业教师/专业方向的双层配对管理功能：
 * <ul>
 *   <li>第一层（粗粒度）：高校教师 → 专业方向</li>
 *   <li>第二层（细粒度）：高校教师 ↔ 企业教师</li>
 * </ul>
 * 查找时优先使用第二层精确配对，兜底使用第一层方向级分配。
 * </p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class TeacherRelationServiceImpl implements ITeacherRelationService {

    private final UniversityTeacherMajorMapper univTeacherMajorMapper;
    private final TeacherRelationshipMapper teacherRelationshipMapper;
    private final UserMapper userMapper;
    private final MajorDirectionMapper majorDirectionMapper;
    private final EnterpriseMapper enterpriseMapper;
    private final MajorTeacherMapper majorTeacherMapper;
    private final MajorMapper majorMapper;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // ==================== 第一层：方向级分配 ====================

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UnivTeacherMajorVO> listMajorAssignments(String enterpriseId, String cohort) {
        LambdaQueryWrapper<UniversityTeacherMajor> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(enterpriseId)) {
            wrapper.eq(UniversityTeacherMajor::getEnterpriseId, enterpriseId);
        }
        if (StringUtils.hasText(cohort)) {
            wrapper.eq(UniversityTeacherMajor::getCohort, cohort);
        }
        wrapper.orderByDesc(UniversityTeacherMajor::getCreateTime);
        List<UniversityTeacherMajor> list = univTeacherMajorMapper.selectList(wrapper);
        return list.stream().map(this::convertToMajorAssignmentVO).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UnivTeacherMajorVO addMajorAssignment(UnivTeacherMajorDTO dto) {
        // 唯一性校验：同一高校教师 + 同一方向 + 同一届别 不可重复
        LambdaQueryWrapper<UniversityTeacherMajor> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(UniversityTeacherMajor::getUnivTeacherId, dto.getUnivTeacherId())
                .eq(UniversityTeacherMajor::getDirectionId, dto.getDirectionId())
                .eq(UniversityTeacherMajor::getCohort, dto.getCohort());
        if (univTeacherMajorMapper.selectCount(checkWrapper) > 0) {
            throw new BusinessException("该高校教师在该届别已分配至该方向");
        }

        // 验证关联数据存在性
        validateUnivTeacher(dto.getUnivTeacherId());
        validateDirection(dto.getDirectionId());
        validateEnterprise(dto.getEnterpriseId());

        UniversityTeacherMajor entity = new UniversityTeacherMajor();
        BeanUtils.copyProperties(dto, entity);
        entity.setIsEnabled(1);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        univTeacherMajorMapper.insert(entity);

        log.info("新增方向级分配：高校教师={}, 方向={}, 届别={}", dto.getUnivTeacherId(), dto.getDirectionId(), dto.getCohort());
        return convertToMajorAssignmentVO(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UnivTeacherMajorVO updateMajorAssignment(String id, UnivTeacherMajorDTO dto) {
        UniversityTeacherMajor existing = univTeacherMajorMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("方向分配记录不存在");
        }

        // 如果修改了教师/方向/届别，需要重新校验唯一性
        boolean keyChanged = !existing.getUnivTeacherId().equals(dto.getUnivTeacherId())
                || !existing.getDirectionId().equals(dto.getDirectionId())
                || !existing.getCohort().equals(dto.getCohort());
        if (keyChanged) {
            LambdaQueryWrapper<UniversityTeacherMajor> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(UniversityTeacherMajor::getUnivTeacherId, dto.getUnivTeacherId())
                    .eq(UniversityTeacherMajor::getDirectionId, dto.getDirectionId())
                    .eq(UniversityTeacherMajor::getCohort, dto.getCohort())
                    .ne(UniversityTeacherMajor::getId, id);
            if (univTeacherMajorMapper.selectCount(checkWrapper) > 0) {
                throw new BusinessException("该高校教师在该届别已分配至该方向");
            }
        }

        validateUnivTeacher(dto.getUnivTeacherId());
        validateDirection(dto.getDirectionId());
        validateEnterprise(dto.getEnterpriseId());

        BeanUtils.copyProperties(dto, existing);
        existing.setUpdateTime(new Date());
        univTeacherMajorMapper.updateById(existing);

        log.info("编辑方向级分配：id={}", id);
        return convertToMajorAssignmentVO(existing);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteMajorAssignment(String id) {
        UniversityTeacherMajor existing = univTeacherMajorMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("方向分配记录不存在");
        }
        univTeacherMajorMapper.deleteById(id);
        log.info("删除方向级分配：id={}", id);
    }

    // ==================== 第二层：精确配对 ====================

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TeacherRelationVO> listTeacherPairs(String enterpriseId, String cohort) {
        LambdaQueryWrapper<TeacherRelationship> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(enterpriseId)) {
            wrapper.eq(TeacherRelationship::getEnterpriseId, enterpriseId);
        }
        if (StringUtils.hasText(cohort)) {
            wrapper.eq(TeacherRelationship::getCohort, cohort);
        }
        wrapper.orderByDesc(TeacherRelationship::getCreateTime);
        List<TeacherRelationship> list = teacherRelationshipMapper.selectList(wrapper);
        return list.stream().map(this::convertToTeacherRelationVO).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TeacherRelationVO addTeacherPair(TeacherRelationDTO dto) {
        // 唯一性校验：同一高校教师 + 同一企业教师 + 同一届别 不可重复
        LambdaQueryWrapper<TeacherRelationship> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(TeacherRelationship::getUnivTeacherId, dto.getUnivTeacherId())
                .eq(TeacherRelationship::getEnterpriseTeacherId, dto.getEnterpriseTeacherId())
                .eq(TeacherRelationship::getCohort, dto.getCohort());
        if (teacherRelationshipMapper.selectCount(checkWrapper) > 0) {
            throw new BusinessException("该高校教师与该企业教师在该届别已存在配对");
        }

        // 验证关联数据
        validateUnivTeacher(dto.getUnivTeacherId());
        validateEnterpriseTeacher(dto.getEnterpriseTeacherId());
        validateEnterprise(dto.getEnterpriseId());
        if (StringUtils.hasText(dto.getDirectionId())) {
            validateDirection(dto.getDirectionId());
        }

        TeacherRelationship entity = new TeacherRelationship();
        BeanUtils.copyProperties(dto, entity);
        entity.setRelationType(StringUtils.hasText(dto.getRelationType()) ? dto.getRelationType() : "DIRECT");
        entity.setIsEnabled(1);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        teacherRelationshipMapper.insert(entity);

        log.info("新增精确配对：高校教师={}, 企业教师={}, 届别={}", dto.getUnivTeacherId(), dto.getEnterpriseTeacherId(), dto.getCohort());
        return convertToTeacherRelationVO(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TeacherRelationVO updateTeacherPair(String relationId, TeacherRelationDTO dto) {
        TeacherRelationship existing = teacherRelationshipMapper.selectById(relationId);
        if (existing == null) {
            throw new BusinessException("精确配对记录不存在");
        }

        // 如果修改了关键字段，重新校验唯一性
        boolean keyChanged = !existing.getUnivTeacherId().equals(dto.getUnivTeacherId())
                || !existing.getEnterpriseTeacherId().equals(dto.getEnterpriseTeacherId())
                || !existing.getCohort().equals(dto.getCohort());
        if (keyChanged) {
            LambdaQueryWrapper<TeacherRelationship> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(TeacherRelationship::getUnivTeacherId, dto.getUnivTeacherId())
                    .eq(TeacherRelationship::getEnterpriseTeacherId, dto.getEnterpriseTeacherId())
                    .eq(TeacherRelationship::getCohort, dto.getCohort())
                    .ne(TeacherRelationship::getRelationId, relationId);
            if (teacherRelationshipMapper.selectCount(checkWrapper) > 0) {
                throw new BusinessException("该高校教师与该企业教师在该届别已存在配对");
            }
        }

        validateUnivTeacher(dto.getUnivTeacherId());
        validateEnterpriseTeacher(dto.getEnterpriseTeacherId());
        validateEnterprise(dto.getEnterpriseId());
        if (StringUtils.hasText(dto.getDirectionId())) {
            validateDirection(dto.getDirectionId());
        }

        BeanUtils.copyProperties(dto, existing);
        existing.setRelationType(StringUtils.hasText(dto.getRelationType()) ? dto.getRelationType() : "DIRECT");
        existing.setUpdateTime(new Date());
        teacherRelationshipMapper.updateById(existing);

        log.info("编辑精确配对：relationId={}", relationId);
        return convertToTeacherRelationVO(existing);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteTeacherPair(String relationId) {
        TeacherRelationship existing = teacherRelationshipMapper.selectById(relationId);
        if (existing == null) {
            throw new BusinessException("精确配对记录不存在");
        }
        teacherRelationshipMapper.deleteById(relationId);
        log.info("删除精确配对：relationId={}", relationId);
    }

    // ==================== 覆盖检查 ====================

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TeacherCoverageVO> getCoverageList(String enterpriseId, String cohort) {
        // 1. 获取企业教师列表（通过 major_teacher 关联获取）
        List<TeacherCoverageVO> resultList = new ArrayList<>();

        // 查询所有专业方向
        LambdaQueryWrapper<MajorDirection> dirWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(enterpriseId)) {
            dirWrapper.eq(MajorDirection::getEnterpriseId, enterpriseId);
        }
        dirWrapper.eq(MajorDirection::getStatus, 1);
        List<MajorDirection> directions = majorDirectionMapper.selectList(dirWrapper);

        for (MajorDirection direction : directions) {
            // 查询该方向下的所有专业
            List<Major> majors = majorMapper.selectList(
                    new LambdaQueryWrapper<Major>()
                            .eq(Major::getDirectionId, direction.getDirectionId())
                            .eq(Major::getStatus, 1));

            // 收集该方向下所有企业教师
            Set<String> teacherIds = new LinkedHashSet<>();
            for (Major major : majors) {
                List<MajorTeacher> relations = majorTeacherMapper.selectList(
                        new LambdaQueryWrapper<MajorTeacher>()
                                .eq(MajorTeacher::getMajorId, major.getMajorId()));
                relations.forEach(r -> teacherIds.add(r.getUserId()));
            }

            // 为每个企业教师计算覆盖情况
            for (String teacherId : teacherIds) {
                User teacher = userMapper.selectById(teacherId);
                if (teacher == null) {
                    continue;
                }

                TeacherCoverageVO coverageVO = new TeacherCoverageVO();
                coverageVO.setEnterpriseTeacherId(teacherId);
                coverageVO.setEnterpriseTeacherName(teacher.getRealName());
                coverageVO.setEnterpriseTeacherEmployeeNo(teacher.getEmployeeNo());
                coverageVO.setEnterpriseId(direction.getEnterpriseId());
                coverageVO.setDirectionId(direction.getDirectionId());
                coverageVO.setDirectionName(direction.getDirectionName());

                // 填充企业名称
                Enterprise enterprise = enterpriseMapper.selectById(direction.getEnterpriseId());
                if (enterprise != null) {
                    coverageVO.setEnterpriseName(enterprise.getEnterpriseName());
                }

                // 优先检查精确配对
                String univTeacherId = findUniversityTeacher(teacherId, direction.getDirectionId(), cohort);
                if (univTeacherId != null) {
                    User univTeacher = userMapper.selectById(univTeacherId);
                    if (univTeacher != null) {
                        coverageVO.setUnivTeacherId(univTeacherId);
                        coverageVO.setUnivTeacherName(univTeacher.getRealName());
                    }
                    // 判断来源：精确配对 or 方向级
                    TeacherRelationship directPair = findDirectPair(teacherId, cohort);
                    coverageVO.setCoverageSource(directPair != null ? "DIRECT" : "DIRECTION");
                    coverageVO.setCovered(true);
                } else {
                    coverageVO.setCovered(false);
                }

                resultList.add(coverageVO);
            }
        }

        return resultList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Integer> getCoverageStats(String cohort) {
        List<TeacherCoverageVO> coverageList = getCoverageList(null, cohort);
        int totalCount = coverageList.size();
        int coveredCount = (int) coverageList.stream().filter(TeacherCoverageVO::isCovered).count();
        int uncoveredCount = totalCount - coveredCount;

        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("totalCount", totalCount);
        stats.put("coveredCount", coveredCount);
        stats.put("uncoveredCount", uncoveredCount);
        return stats;
    }

    // ==================== 核心查找逻辑 ====================

    /**
     * {@inheritDoc}
     * <p>
     * 优先级：精确配对（teacher_relationship）> 方向级分配（university_teacher_major）
     * </p>
     */
    @Override
    public String findUniversityTeacher(String enterpriseTeacherId, String directionId, String cohort) {
        // 第一优先级：精确配对
        TeacherRelationship directPair = findDirectPair(enterpriseTeacherId, cohort);
        if (directPair != null) {
            return directPair.getUnivTeacherId();
        }

        // 第二优先级：方向级分配
        if (StringUtils.hasText(directionId) && StringUtils.hasText(cohort)) {
            LambdaQueryWrapper<UniversityTeacherMajor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UniversityTeacherMajor::getDirectionId, directionId)
                    .eq(UniversityTeacherMajor::getCohort, cohort)
                    .eq(UniversityTeacherMajor::getIsEnabled, 1);
            UniversityTeacherMajor assignment = univTeacherMajorMapper.selectOne(wrapper);
            if (assignment != null) {
                return assignment.getUnivTeacherId();
            }
        }

        return null;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 查找企业教师的精确配对记录
     *
     * @param enterpriseTeacherId 企业教师ID
     * @param cohort              届别
     * @return 精确配对记录（null 表示不存在）
     */
    private TeacherRelationship findDirectPair(String enterpriseTeacherId, String cohort) {
        LambdaQueryWrapper<TeacherRelationship> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeacherRelationship::getEnterpriseTeacherId, enterpriseTeacherId)
                .eq(StringUtils.hasText(cohort), TeacherRelationship::getCohort, cohort)
                .eq(TeacherRelationship::getRelationType, "DIRECT")
                .eq(TeacherRelationship::getIsEnabled, 1)
                .last("LIMIT 1");
        return teacherRelationshipMapper.selectOne(wrapper);
    }

    /**
     * 验证高校教师是否存在（角色为 UNIVERSITY_TEACHER 或 SUPERVISOR_TEACHER）
     *
     * @param userId 用户ID
     */
    private void validateUnivTeacher(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("高校教师不存在：" + userId);
        }
    }

    /**
     * 验证企业教师是否存在
     *
     * @param userId 用户ID
     */
    private void validateEnterpriseTeacher(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("企业教师不存在：" + userId);
        }
    }

    /**
     * 验证专业方向是否存在
     *
     * @param directionId 方向ID
     */
    private void validateDirection(String directionId) {
        MajorDirection direction = majorDirectionMapper.selectById(directionId);
        if (direction == null) {
            throw new BusinessException("专业方向不存在：" + directionId);
        }
    }

    /**
     * 验证企业是否存在
     *
     * @param enterpriseId 企业ID
     */
    private void validateEnterprise(String enterpriseId) {
        Enterprise enterprise = enterpriseMapper.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException("企业不存在：" + enterpriseId);
        }
    }

    /**
     * 将方向级分配实体转为VO
     *
     * @param entity 实体
     * @return VO
     */
    private UnivTeacherMajorVO convertToMajorAssignmentVO(UniversityTeacherMajor entity) {
        UnivTeacherMajorVO vo = new UnivTeacherMajorVO();
        BeanUtils.copyProperties(entity, vo);

        // 填充高校教师信息
        User univTeacher = userMapper.selectById(entity.getUnivTeacherId());
        if (univTeacher != null) {
            vo.setUnivTeacherName(univTeacher.getRealName());
            vo.setUnivTeacherEmployeeNo(univTeacher.getEmployeeNo());
        }

        // 填充方向名称
        MajorDirection direction = majorDirectionMapper.selectById(entity.getDirectionId());
        if (direction != null) {
            vo.setDirectionName(direction.getDirectionName());
        }

        // 填充企业名称
        Enterprise enterprise = enterpriseMapper.selectById(entity.getEnterpriseId());
        if (enterprise != null) {
            vo.setEnterpriseName(enterprise.getEnterpriseName());
        }

        // 格式化时间
        if (entity.getCreateTime() != null) {
            vo.setCreateTime(DATE_FORMAT.format(entity.getCreateTime()));
        }

        return vo;
    }

    /**
     * 将精确配对实体转为VO
     *
     * @param entity 实体
     * @return VO
     */
    private TeacherRelationVO convertToTeacherRelationVO(TeacherRelationship entity) {
        TeacherRelationVO vo = new TeacherRelationVO();
        BeanUtils.copyProperties(entity, vo);

        // 填充高校教师信息
        User univTeacher = userMapper.selectById(entity.getUnivTeacherId());
        if (univTeacher != null) {
            vo.setUnivTeacherName(univTeacher.getRealName());
            vo.setUnivTeacherEmployeeNo(univTeacher.getEmployeeNo());
        }

        // 填充企业教师信息
        User entTeacher = userMapper.selectById(entity.getEnterpriseTeacherId());
        if (entTeacher != null) {
            vo.setEnterpriseTeacherName(entTeacher.getRealName());
            vo.setEnterpriseTeacherEmployeeNo(entTeacher.getEmployeeNo());
        }

        // 填充企业名称
        Enterprise enterprise = enterpriseMapper.selectById(entity.getEnterpriseId());
        if (enterprise != null) {
            vo.setEnterpriseName(enterprise.getEnterpriseName());
        }

        // 填充方向名称
        if (StringUtils.hasText(entity.getDirectionId())) {
            MajorDirection direction = majorDirectionMapper.selectById(entity.getDirectionId());
            if (direction != null) {
                vo.setDirectionName(direction.getDirectionName());
            }
        }

        // 格式化时间
        if (entity.getCreateTime() != null) {
            vo.setCreateTime(DATE_FORMAT.format(entity.getCreateTime()));
        }

        return vo;
    }
}
