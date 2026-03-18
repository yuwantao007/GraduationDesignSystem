package com.yuwan.completebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuwan.completebackend.common.enums.DefenseType;
import com.yuwan.completebackend.common.enums.OpeningReportStatus;
import com.yuwan.completebackend.exception.BusinessException;
import com.yuwan.completebackend.mapper.DefenseArrangementMapper;
import com.yuwan.completebackend.mapper.OpeningReportMapper;
import com.yuwan.completebackend.mapper.OpeningTaskBookMapper;
import com.yuwan.completebackend.mapper.UserMapper;
import com.yuwan.completebackend.model.dto.defense.*;
import com.yuwan.completebackend.model.entity.DefenseArrangement;
import com.yuwan.completebackend.model.entity.OpeningReport;
import com.yuwan.completebackend.model.entity.OpeningTaskBook;
import com.yuwan.completebackend.model.entity.User;
import com.yuwan.completebackend.model.vo.defense.DefenseArrangementVO;
import com.yuwan.completebackend.model.vo.defense.OpeningReportVO;
import com.yuwan.completebackend.model.vo.defense.OpeningTaskBookVO;
import com.yuwan.completebackend.service.IDefenseService;
import com.yuwan.completebackend.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 开题答辩管理Service实现类
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefenseServiceImpl extends ServiceImpl<DefenseArrangementMapper, DefenseArrangement>
        implements IDefenseService {

    private final DefenseArrangementMapper arrangementMapper;
    private final OpeningReportMapper reportMapper;
    private final OpeningTaskBookMapper taskBookMapper;
    private final UserMapper userMapper;

    // ==================== 答辩安排管理 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createArrangement(CreateArrangementDTO dto) {
        String currentUserId = SecurityUtil.getCurrentUserId();
        String enterpriseId = getEnterpriseIdByUserId(currentUserId);
        if (StrUtil.isBlank(enterpriseId)) {
            throw new BusinessException("当前用户未关联企业");
        }

        // 验证答辩类型
        DefenseType defenseType = DefenseType.getByCode(dto.getDefenseType());
        if (defenseType == null) {
            throw new BusinessException("无效的答辩类型");
        }

        // 创建答辩安排
        DefenseArrangement arrangement = new DefenseArrangement();
        BeanUtil.copyProperties(dto, arrangement);
        arrangement.setEnterpriseId(enterpriseId);
        arrangement.setCreatorId(currentUserId);
        arrangement.setStatus(1);

        this.save(arrangement);
        log.info("创建答辩安排成功，ID: {}", arrangement.getArrangementId());
        return arrangement.getArrangementId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateArrangement(UpdateArrangementDTO dto) {
        DefenseArrangement arrangement = this.getById(dto.getArrangementId());
        if (arrangement == null) {
            throw new BusinessException("答辩安排不存在");
        }

        // 验证权限：只能修改自己企业的安排
        String currentUserId = SecurityUtil.getCurrentUserId();
        String enterpriseId = getEnterpriseIdByUserId(currentUserId);
        if (!arrangement.getEnterpriseId().equals(enterpriseId)) {
            throw new BusinessException("无权修改此答辩安排");
        }

        BeanUtil.copyProperties(dto, arrangement, "arrangementId", "enterpriseId", "creatorId");
        return this.updateById(arrangement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteArrangement(String arrangementId) {
        DefenseArrangement arrangement = this.getById(arrangementId);
        if (arrangement == null) {
            throw new BusinessException("答辩安排不存在");
        }

        // 验证权限
        String currentUserId = SecurityUtil.getCurrentUserId();
        String enterpriseId = getEnterpriseIdByUserId(currentUserId);
        if (!arrangement.getEnterpriseId().equals(enterpriseId)) {
            throw new BusinessException("无权删除此答辩安排");
        }

        return this.removeById(arrangementId);
    }

    @Override
    public IPage<DefenseArrangementVO> pageArrangements(ArrangementQueryDTO queryDTO) {
        String currentUserId = SecurityUtil.getCurrentUserId();
        String enterpriseId = getEnterpriseIdByUserId(currentUserId);

        Page<DefenseArrangementVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<DefenseArrangementVO> result = arrangementMapper.selectArrangementPage(
                page,
                enterpriseId,
                queryDTO.getDefenseType(),
                queryDTO.getTopicCategory(),
                queryDTO.getCohort(),
                queryDTO.getStatus()
        );

        // 填充答辩类型名称和教师信息
        for (DefenseArrangementVO vo : result.getRecords()) {
            vo.setDefenseTypeName(DefenseType.getDescByCode(vo.getDefenseType()));
            fillPanelTeacherInfos(vo);
        }
        return result;
    }

    @Override
    public DefenseArrangementVO getArrangementDetail(String arrangementId) {
        DefenseArrangementVO vo = arrangementMapper.selectArrangementById(arrangementId);
        if (vo == null) {
            throw new BusinessException("答辩安排不存在");
        }
        vo.setDefenseTypeName(DefenseType.getDescByCode(vo.getDefenseType()));
        fillPanelTeacherInfos(vo);
        return vo;
    }

    /**
     * 填充答辩小组教师信息
     */
    private void fillPanelTeacherInfos(DefenseArrangementVO vo) {
        if (CollUtil.isEmpty(vo.getPanelTeachers())) {
            vo.setPanelTeacherInfos(new ArrayList<>());
            return;
        }
        List<DefenseArrangementVO.TeacherInfoVO> teacherInfos = new ArrayList<>();
        for (String teacherId : vo.getPanelTeachers()) {
            User user = userMapper.selectById(teacherId);
            if (user != null) {
                DefenseArrangementVO.TeacherInfoVO teacherInfo = new DefenseArrangementVO.TeacherInfoVO();
                teacherInfo.setUserId(user.getUserId());
                teacherInfo.setRealName(user.getRealName());
                teacherInfos.add(teacherInfo);
            }
        }
        vo.setPanelTeacherInfos(teacherInfos);
    }

    /**
     * 根据用户ID获取企业ID
     */
    private String getEnterpriseIdByUserId(String userId) {
        User user = userMapper.selectById(userId);
        return user != null ? user.getEnterpriseId() : null;
    }

    // ==================== 开题任务书管理 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveTaskBook(SaveTaskBookDTO dto) {
        String currentUserId = SecurityUtil.getCurrentUserId();

        // 查询是否已存在任务书
        LambdaQueryWrapper<OpeningTaskBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OpeningTaskBook::getStudentId, dto.getStudentId());
        OpeningTaskBook existing = taskBookMapper.selectOne(wrapper);

        if (existing != null) {
            // 更新
            existing.setTopicId(dto.getTopicId());
            existing.setContent(dto.getContent());
            existing.setDocumentId(dto.getDocumentId());
            existing.setTeacherId(currentUserId);
            taskBookMapper.updateById(existing);
            return existing.getTaskBookId();
        } else {
            // 新建
            OpeningTaskBook taskBook = new OpeningTaskBook();
            BeanUtil.copyProperties(dto, taskBook);
            taskBook.setTeacherId(currentUserId);
            taskBookMapper.insert(taskBook);
            return taskBook.getTaskBookId();
        }
    }

    @Override
    public OpeningTaskBookVO getTaskBookByStudent(String studentId) {
        return taskBookMapper.selectTaskBookByStudentId(studentId);
    }

    @Override
    public OpeningTaskBookVO getTaskBookDetail(String taskBookId) {
        return taskBookMapper.selectTaskBookById(taskBookId);
    }

    // ==================== 开题报告管理 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitReport(SubmitReportDTO dto) {
        String currentUserId = SecurityUtil.getCurrentUserId();

        // 查询是否已存在报告
        LambdaQueryWrapper<OpeningReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OpeningReport::getStudentId, currentUserId);
        OpeningReport existing = reportMapper.selectOne(wrapper);

        if (existing != null) {
            // 检查状态：已通过的不能重新提交
            if (OpeningReportStatus.PASSED.getCode().equals(existing.getReviewStatus())) {
                throw new BusinessException("开题报告已通过，不能重新提交");
            }
            // 更新
            existing.setTopicId(dto.getTopicId());
            existing.setArrangementId(dto.getArrangementId());
            existing.setDocumentId(dto.getDocumentId());
            existing.setSubmitTime(new Date());
            existing.setReviewStatus(OpeningReportStatus.SUBMITTED.getCode());
            existing.setReviewComment(null);
            existing.setReviewerId(null);
            existing.setReviewTime(null);
            reportMapper.updateById(existing);
            return existing.getReportId();
        } else {
            // 新建
            OpeningReport report = new OpeningReport();
            report.setStudentId(currentUserId);
            report.setTopicId(dto.getTopicId());
            report.setArrangementId(dto.getArrangementId());
            report.setDocumentId(dto.getDocumentId());
            report.setSubmitTime(new Date());
            report.setReviewStatus(OpeningReportStatus.SUBMITTED.getCode());
            reportMapper.insert(report);
            return report.getReportId();
        }
    }

    @Override
    public OpeningReportVO getMyReport() {
        String currentUserId = SecurityUtil.getCurrentUserId();
        OpeningReportVO vo = reportMapper.selectReportByStudentId(currentUserId);
        if (vo != null) {
            vo.setReviewStatusName(OpeningReportStatus.getDescByCode(vo.getReviewStatus()));
        }
        return vo;
    }

    @Override
    public IPage<OpeningReportVO> pageReports(ReportQueryDTO queryDTO) {
        String currentUserId = SecurityUtil.getCurrentUserId();
        String enterpriseId = getEnterpriseIdByUserId(currentUserId);

        Page<OpeningReportVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<OpeningReportVO> result = reportMapper.selectReportPage(
                page,
                enterpriseId,
                queryDTO.getStudentName(),
                queryDTO.getReviewStatus(),
                queryDTO.getArrangementId()
        );

        // 填充状态名称
        for (OpeningReportVO vo : result.getRecords()) {
            vo.setReviewStatusName(OpeningReportStatus.getDescByCode(vo.getReviewStatus()));
        }
        return result;
    }

    @Override
    public OpeningReportVO getReportDetail(String reportId) {
        OpeningReportVO vo = reportMapper.selectReportById(reportId);
        if (vo == null) {
            throw new BusinessException("开题报告不存在");
        }
        vo.setReviewStatusName(OpeningReportStatus.getDescByCode(vo.getReviewStatus()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewReport(ReviewReportDTO dto) {
        OpeningReport report = reportMapper.selectById(dto.getReportId());
        if (report == null) {
            throw new BusinessException("开题报告不存在");
        }

        // 验证状态：只能审查已提交待审的报告
        if (!OpeningReportStatus.SUBMITTED.getCode().equals(report.getReviewStatus())) {
            throw new BusinessException("当前状态不允许审查");
        }

        // 验证审查状态值
        if (!OpeningReportStatus.PASSED.getCode().equals(dto.getReviewStatus())
                && !OpeningReportStatus.FAILED.getCode().equals(dto.getReviewStatus())) {
            throw new BusinessException("审查状态无效");
        }

        String currentUserId = SecurityUtil.getCurrentUserId();
        report.setReviewStatus(dto.getReviewStatus());
        report.setReviewComment(dto.getReviewComment());
        report.setReviewerId(currentUserId);
        report.setReviewTime(new Date());

        return reportMapper.updateById(report) > 0;
    }
}
