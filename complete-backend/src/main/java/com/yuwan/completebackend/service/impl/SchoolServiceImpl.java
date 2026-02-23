package com.yuwan.completebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.exception.BusinessException;
import com.yuwan.completebackend.mapper.SchoolMapper;
import com.yuwan.completebackend.model.dto.CreateSchoolDTO;
import com.yuwan.completebackend.model.dto.UpdateSchoolDTO;
import com.yuwan.completebackend.model.entity.School;
import com.yuwan.completebackend.model.vo.SchoolQueryVO;
import com.yuwan.completebackend.model.vo.SchoolVO;
import com.yuwan.completebackend.service.ISchoolService;
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
 * 学校管理服务实现类
 * 提供学校CRUD功能实现
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class SchoolServiceImpl implements ISchoolService {

    private final SchoolMapper schoolMapper;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public SchoolVO createSchool(CreateSchoolDTO createDTO) {
        // 检查学校名称是否已存在
        LambdaQueryWrapper<School> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(School::getSchoolName, createDTO.getSchoolName());
        if (schoolMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("学校名称已存在");
        }

        // 检查学校编码是否已存在
        if (StringUtils.hasText(createDTO.getSchoolCode())) {
            LambdaQueryWrapper<School> codeWrapper = new LambdaQueryWrapper<>();
            codeWrapper.eq(School::getSchoolCode, createDTO.getSchoolCode());
            if (schoolMapper.selectCount(codeWrapper) > 0) {
                throw new BusinessException("学校编码已存在");
            }
        }

        // 创建学校实体
        School school = new School();
        BeanUtils.copyProperties(createDTO, school);
        school.setSchoolStatus(1); // 默认正常状态

        // 保存到数据库
        schoolMapper.insert(school);
        log.info("创建学校成功，学校ID: {}", school.getSchoolId());

        return convertToVO(school);
    }

    @Override
    public SchoolVO updateSchool(String schoolId, UpdateSchoolDTO updateDTO) {
        // 查询学校是否存在
        School school = schoolMapper.selectById(schoolId);
        if (school == null) {
            throw new BusinessException("学校不存在");
        }

        // 检查学校名称是否重复
        if (StringUtils.hasText(updateDTO.getSchoolName()) 
                && !updateDTO.getSchoolName().equals(school.getSchoolName())) {
            LambdaQueryWrapper<School> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(School::getSchoolName, updateDTO.getSchoolName())
                   .ne(School::getSchoolId, schoolId);
            if (schoolMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("学校名称已存在");
            }
        }

        // 检查学校编码是否重复
        if (StringUtils.hasText(updateDTO.getSchoolCode()) 
                && !updateDTO.getSchoolCode().equals(school.getSchoolCode())) {
            LambdaQueryWrapper<School> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(School::getSchoolCode, updateDTO.getSchoolCode())
                   .ne(School::getSchoolId, schoolId);
            if (schoolMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("学校编码已存在");
            }
        }

        // 更新学校信息
        if (StringUtils.hasText(updateDTO.getSchoolName())) {
            school.setSchoolName(updateDTO.getSchoolName());
        }
        if (StringUtils.hasText(updateDTO.getSchoolCode())) {
            school.setSchoolCode(updateDTO.getSchoolCode());
        }
        if (StringUtils.hasText(updateDTO.getAddress())) {
            school.setAddress(updateDTO.getAddress());
        }
        if (StringUtils.hasText(updateDTO.getContactPerson())) {
            school.setContactPerson(updateDTO.getContactPerson());
        }
        if (StringUtils.hasText(updateDTO.getContactPhone())) {
            school.setContactPhone(updateDTO.getContactPhone());
        }
        if (StringUtils.hasText(updateDTO.getSchoolEmail())) {
            school.setSchoolEmail(updateDTO.getSchoolEmail());
        }
        if (StringUtils.hasText(updateDTO.getDescription())) {
            school.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getSchoolStatus() != null) {
            school.setSchoolStatus(updateDTO.getSchoolStatus());
        }

        schoolMapper.updateById(school);
        log.info("更新学校成功，学校ID: {}", schoolId);

        return convertToVO(school);
    }

    @Override
    public SchoolVO getSchoolDetail(String schoolId) {
        School school = schoolMapper.selectById(schoolId);
        if (school == null) {
            throw new BusinessException("学校不存在");
        }
        return convertToVO(school);
    }

    @Override
    public PageResult<SchoolVO> getSchoolList(SchoolQueryVO queryVO) {
        // 构建查询条件
        LambdaQueryWrapper<School> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(queryVO.getSchoolName())) {
            wrapper.like(School::getSchoolName, queryVO.getSchoolName());
        }
        if (StringUtils.hasText(queryVO.getSchoolCode())) {
            wrapper.eq(School::getSchoolCode, queryVO.getSchoolCode());
        }
        if (queryVO.getSchoolStatus() != null) {
            wrapper.eq(School::getSchoolStatus, queryVO.getSchoolStatus());
        }
        wrapper.orderByDesc(School::getCreateTime);

        // 分页查询
        Page<School> page = new Page<>(queryVO.getPageNum(), queryVO.getPageSize());
        Page<School> result = schoolMapper.selectPage(page, wrapper);

        // 转换结果
        List<SchoolVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, result.getTotal(), queryVO.getPageNum().longValue(), queryVO.getPageSize().longValue());
    }

    @Override
    public List<SchoolVO> getAllSchools() {
        LambdaQueryWrapper<School> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(School::getSchoolStatus, 1)
               .orderByAsc(School::getSchoolName);
        
        List<School> schools = schoolMapper.selectList(wrapper);
        return schools.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteSchool(String schoolId) {
        School school = schoolMapper.selectById(schoolId);
        if (school == null) {
            throw new BusinessException("学校不存在");
        }
        
        // TODO: 检查是否有关联的课题

        schoolMapper.deleteById(schoolId);
        log.info("删除学校成功，学校ID: {}", schoolId);
    }

    @Override
    public void updateSchoolStatus(String schoolId, Integer status) {
        School school = schoolMapper.selectById(schoolId);
        if (school == null) {
            throw new BusinessException("学校不存在");
        }
        
        school.setSchoolStatus(status);
        schoolMapper.updateById(school);
        log.info("更新学校状态成功，学校ID: {}, 新状态: {}", schoolId, status);
    }

    /**
     * 将实体转换为VO
     *
     * @param school 学校实体
     * @return 学校VO
     */
    private SchoolVO convertToVO(School school) {
        SchoolVO vo = new SchoolVO();
        BeanUtils.copyProperties(school, vo);
        
        // 设置状态描述
        vo.setSchoolStatusDesc(school.getSchoolStatus() == 1 ? "正常" : "禁用");
        
        // 格式化时间
        if (school.getCreateTime() != null) {
            vo.setCreateTime(DATE_FORMAT.format(school.getCreateTime()));
        }
        if (school.getUpdateTime() != null) {
            vo.setUpdateTime(DATE_FORMAT.format(school.getUpdateTime()));
        }
        
        return vo;
    }
}
