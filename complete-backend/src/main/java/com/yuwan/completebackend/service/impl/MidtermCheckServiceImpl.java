package com.yuwan.completebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.exception.BusinessException;
import com.yuwan.completebackend.mapper.MidtermCheckMapper;
import com.yuwan.completebackend.model.dto.midterm.CreateMidtermCheckDTO;
import com.yuwan.completebackend.model.dto.midterm.MidtermCheckQueryDTO;
import com.yuwan.completebackend.model.dto.midterm.ReviewMidtermCheckDTO;
import com.yuwan.completebackend.model.entity.MidtermCheck;
import com.yuwan.completebackend.model.enums.MidtermReviewStatus;
import com.yuwan.completebackend.model.enums.MidtermSubmitStatus;
import com.yuwan.completebackend.model.vo.MidtermCheckListVO;
import com.yuwan.completebackend.model.vo.MidtermCheckVO;
import com.yuwan.completebackend.service.IMidtermCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * 中期检查服务实现类
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MidtermCheckServiceImpl extends ServiceImpl<MidtermCheckMapper, MidtermCheck>
        implements IMidtermCheckService {

    private final MidtermCheckMapper midtermCheckMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrUpdateCheck(CreateMidtermCheckDTO dto, String teacherId) {
        MidtermCheck check;
        
        if (StringUtils.hasText(dto.getCheckId())) {
            // 编辑模式
            check = getById(dto.getCheckId());
            if (check == null) {
                throw new BusinessException("中期检查表不存在");
            }
            if (!check.getEnterpriseTeacherId().equals(teacherId)) {
                throw new BusinessException("无权限编辑此中期检查表");
            }
            if (check.getSubmitStatus() == MidtermSubmitStatus.SUBMITTED.getCode()) {
                throw new BusinessException("已提交的中期检查表无法编辑");
            }
            BeanUtils.copyProperties(dto, check);
        } else {
            // 创建模式 - 检查学生是否已有中期检查表
            LambdaQueryWrapper<MidtermCheck> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MidtermCheck::getStudentId, dto.getStudentId());
            wrapper.eq(MidtermCheck::getDeleted, 0);
            MidtermCheck existing = getOne(wrapper);
            
            if (existing != null) {
                throw new BusinessException("该学生已有中期检查表，请直接编辑");
            }
            
            check = new MidtermCheck();
            BeanUtils.copyProperties(dto, check);
            check.setEnterpriseTeacherId(teacherId);
            check.setSubmitStatus(MidtermSubmitStatus.DRAFT.getCode());
            check.setReviewStatus(MidtermReviewStatus.NOT_REVIEWED.getCode());
        }
        
        saveOrUpdate(check);
        log.info("企业教师[{}]{}中期检查表[{}]", teacherId, 
                StringUtils.hasText(dto.getCheckId()) ? "编辑" : "创建", check.getCheckId());
        return check.getCheckId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submitCheck(String checkId, String teacherId) {
        MidtermCheck check = getById(checkId);
        if (check == null) {
            throw new BusinessException("中期检查表不存在");
        }
        if (!check.getEnterpriseTeacherId().equals(teacherId)) {
            throw new BusinessException("无权限提交此中期检查表");
        }
        if (check.getSubmitStatus() == MidtermSubmitStatus.SUBMITTED.getCode()) {
            throw new BusinessException("该中期检查表已提交");
        }
        
        // 校验必填字段
        if (!StringUtils.hasText(check.getCompletionStatus())) {
            throw new BusinessException("请填写完成情况");
        }
        
        check.setSubmitStatus(MidtermSubmitStatus.SUBMITTED.getCode());
        updateById(check);
        log.info("企业教师[{}]提交中期检查表[{}]", teacherId, checkId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean reviewCheck(ReviewMidtermCheckDTO dto, String reviewerId) {
        MidtermCheck check = getById(dto.getCheckId());
        if (check == null) {
            throw new BusinessException("中期检查表不存在");
        }
        if (check.getSubmitStatus() != MidtermSubmitStatus.SUBMITTED.getCode()) {
            throw new BusinessException("该中期检查表未提交，无法审查");
        }
        if (check.getReviewStatus() != MidtermReviewStatus.NOT_REVIEWED.getCode()) {
            throw new BusinessException("该中期检查表已审查");
        }
        
        check.setReviewStatus(dto.getReviewStatus());
        check.setReviewComment(dto.getReviewComment());
        check.setReviewerId(reviewerId);
        check.setReviewTime(new Date());
        updateById(check);
        
        log.info("高校教师[{}]审查中期检查表[{}]，结果：{}", reviewerId, dto.getCheckId(), 
                MidtermReviewStatus.getDescByCode(dto.getReviewStatus()));
        return true;
    }

    @Override
    public MidtermCheckVO getCheckDetail(String checkId) {
        MidtermCheckVO vo = midtermCheckMapper.selectDetailById(checkId);
        if (vo == null) {
            throw new BusinessException("中期检查表不存在");
        }
        fillStatusDesc(vo);
        return vo;
    }

    @Override
    public MidtermCheckVO getByStudentId(String studentId) {
        MidtermCheckVO vo = midtermCheckMapper.selectByStudentId(studentId);
        if (vo != null) {
            fillStatusDesc(vo);
        }
        return vo;
    }

    @Override
    public PageResult<MidtermCheckListVO> getEnterpriseTeacherList(String teacherId, MidtermCheckQueryDTO queryDTO) {
        List<MidtermCheckListVO> list = midtermCheckMapper.selectByEnterpriseTeacher(teacherId, queryDTO);
        Long total = midtermCheckMapper.countByEnterpriseTeacher(teacherId, queryDTO);
        list.forEach(this::fillListStatusDesc);
        return new PageResult<>(list, total);
    }

    @Override
    public PageResult<MidtermCheckListVO> getUnivTeacherList(String teacherId, MidtermCheckQueryDTO queryDTO) {
        List<MidtermCheckListVO> list = midtermCheckMapper.selectByUnivTeacher(teacherId, queryDTO);
        Long total = midtermCheckMapper.countByUnivTeacher(teacherId, queryDTO);
        list.forEach(this::fillListStatusDesc);
        return new PageResult<>(list, total);
    }

    @Override
    public PageResult<MidtermCheckListVO> getAdminList(MidtermCheckQueryDTO queryDTO) {
        List<MidtermCheckListVO> list = midtermCheckMapper.selectListByCondition(queryDTO);
        Long total = midtermCheckMapper.countByCondition(queryDTO);
        list.forEach(this::fillListStatusDesc);
        return new PageResult<>(list, total);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String initCheckForStudent(String studentId, String topicId, String enterpriseTeacherId) {
        // 检查是否已存在
        LambdaQueryWrapper<MidtermCheck> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MidtermCheck::getStudentId, studentId);
        wrapper.eq(MidtermCheck::getDeleted, 0);
        MidtermCheck existing = getOne(wrapper);
        
        if (existing != null) {
            return existing.getCheckId();
        }
        
        MidtermCheck check = new MidtermCheck();
        check.setStudentId(studentId);
        check.setTopicId(topicId);
        check.setEnterpriseTeacherId(enterpriseTeacherId);
        check.setSubmitStatus(MidtermSubmitStatus.DRAFT.getCode());
        check.setReviewStatus(MidtermReviewStatus.NOT_REVIEWED.getCode());
        save(check);
        
        log.info("系统为学生[{}]初始化中期检查表[{}]", studentId, check.getCheckId());
        return check.getCheckId();
    }

    /**
     * 填充详情VO状态描述
     */
    private void fillStatusDesc(MidtermCheckVO vo) {
        vo.setSubmitStatusDesc(MidtermSubmitStatus.getDescByCode(vo.getSubmitStatus()));
        vo.setReviewStatusDesc(MidtermReviewStatus.getDescByCode(vo.getReviewStatus()));
    }

    /**
     * 填充列表VO状态描述
     */
    private void fillListStatusDesc(MidtermCheckListVO vo) {
        vo.setSubmitStatusDesc(MidtermSubmitStatus.getDescByCode(vo.getSubmitStatus()));
        vo.setReviewStatusDesc(MidtermReviewStatus.getDescByCode(vo.getReviewStatus()));
    }
}
