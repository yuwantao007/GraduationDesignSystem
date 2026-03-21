package com.yuwan.completebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.exception.BusinessException;
import com.yuwan.completebackend.mapper.GuidanceRecordMapper;
import com.yuwan.completebackend.mapper.TeacherRelationshipMapper;
import com.yuwan.completebackend.mapper.TopicMapper;
import com.yuwan.completebackend.mapper.TopicSelectionMapper;
import com.yuwan.completebackend.mapper.UserMapper;
import com.yuwan.completebackend.model.dto.CreateGuidanceDTO;
import com.yuwan.completebackend.model.entity.GuidanceRecord;
import com.yuwan.completebackend.model.entity.TeacherRelationship;
import com.yuwan.completebackend.model.entity.Topic;
import com.yuwan.completebackend.model.entity.TopicSelection;
import com.yuwan.completebackend.model.entity.User;
import com.yuwan.completebackend.model.enums.GuidanceType;
import com.yuwan.completebackend.model.vo.GuidanceListVO;
import com.yuwan.completebackend.model.vo.GuidanceQueryVO;
import com.yuwan.completebackend.model.vo.GuidanceRecordVO;
import com.yuwan.completebackend.model.vo.GuidanceStudentVO;
import com.yuwan.completebackend.security.SecurityUtil;
import com.yuwan.completebackend.service.IGuidanceRecordService;
import com.yuwan.completebackend.service.INotificationDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 指导记录服务实现类
 * 提供企业教师项目指导、高校教师论文指导的全流程业务功能
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class GuidanceRecordServiceImpl implements IGuidanceRecordService {

    private final GuidanceRecordMapper guidanceRecordMapper;
    private final TopicMapper topicMapper;
    private final TopicSelectionMapper topicSelectionMapper;
    private final UserMapper userMapper;
    private final TeacherRelationshipMapper teacherRelationshipMapper;
    private final INotificationDispatchService notificationDispatchService;

    @Override
    public GuidanceRecordVO createGuidanceRecord(CreateGuidanceDTO dto) {
        String currentUserId = SecurityUtil.getCurrentUserId();
        log.info("教师 {} 创建指导记录，学生: {}, 课题: {}", currentUserId, dto.getStudentId(), dto.getTopicId());

        // 校验指导类型
        GuidanceType guidanceType = GuidanceType.fromCode(dto.getGuidanceType());
        if (guidanceType == null) {
            throw new BusinessException("无效的指导类型");
        }

        // 校验课题是否存在
        Topic topic = topicMapper.selectById(dto.getTopicId());
        if (topic == null) {
            throw new BusinessException("课题不存在");
        }

        // 校验学生是否已中选该课题
        LambdaQueryWrapper<TopicSelection> selectionWrapper = new LambdaQueryWrapper<>();
        selectionWrapper.eq(TopicSelection::getStudentId, dto.getStudentId())
                .eq(TopicSelection::getTopicId, dto.getTopicId())
                .eq(TopicSelection::getSelectionStatus, 1)  // 中选状态
                .eq(TopicSelection::getDeleted, 0);
        TopicSelection selection = topicSelectionMapper.selectOne(selectionWrapper);
        if (selection == null) {
            throw new BusinessException("该学生未中选此课题，无法添加指导记录");
        }

        // 根据指导类型校验教师权限
        if (guidanceType == GuidanceType.PROJECT) {
            // 项目指导：企业教师只能为自己课题的学生添加记录
            if (!topic.getCreatorId().equals(currentUserId)) {
                throw new BusinessException("您只能为自己课题的学生添加项目指导记录");
            }
        } else {
            // 论文指导：高校教师只能为配对企业教师名下的学生添加记录
            LambdaQueryWrapper<TeacherRelationship> relationWrapper = new LambdaQueryWrapper<>();
            relationWrapper.eq(TeacherRelationship::getUnivTeacherId, currentUserId)
                    .eq(TeacherRelationship::getEnterpriseTeacherId, topic.getCreatorId())
                    .eq(TeacherRelationship::getIsEnabled, 1)
                    .eq(TeacherRelationship::getDeleted, 0);
            long count = teacherRelationshipMapper.selectCount(relationWrapper);
            if (count == 0) {
                throw new BusinessException("您未与该课题的企业教师建立配对关系，无法添加论文指导记录");
            }
        }

        // 创建指导记录
        GuidanceRecord record = new GuidanceRecord();
        record.setStudentId(dto.getStudentId());
        record.setTeacherId(currentUserId);
        record.setTopicId(dto.getTopicId());
        record.setGuidanceType(dto.getGuidanceType());
        record.setGuidanceDate(dto.getGuidanceDate());
        record.setGuidanceContent(dto.getGuidanceContent());
        record.setGuidanceMethod(dto.getGuidanceMethod());
        record.setDurationHours(dto.getDurationHours());

        guidanceRecordMapper.insert(record);
        log.info("指导记录创建成功，记录ID: {}", record.getRecordId());

        User teacher = userMapper.selectById(currentUserId);
        Map<String, String> variables = new HashMap<>();
        variables.put("studentId", dto.getStudentId());
        variables.put("teacherName", teacher != null ? teacher.getRealName() : "指导教师");
        variables.put("topicTitle", topic.getTopicTitle());
        variables.put("guidanceType", guidanceType.getDescription());

        notificationDispatchService.sendByTemplateAfterCommit(
            "GUIDANCE_RECORD_CREATED",
            dto.getStudentId(),
            "GUIDANCE_RECORD",
            record.getRecordId(),
            "/guidance/student",
            variables,
            "guidance:create:" + record.getRecordId() + ":" + dto.getStudentId(),
            null,
            null
        );

        return guidanceRecordMapper.selectRecordDetail(record.getRecordId());
    }

    @Override
    public void deleteGuidanceRecord(String recordId) {
        String currentUserId = SecurityUtil.getCurrentUserId();

        GuidanceRecord record = guidanceRecordMapper.selectById(recordId);
        if (record == null || record.getDeleted() == 1) {
            throw new BusinessException("指导记录不存在");
        }

        // 只能删除自己创建的记录
        if (!record.getTeacherId().equals(currentUserId)) {
            throw new BusinessException("只能删除自己创建的指导记录");
        }

        // 逻辑删除
        record.setDeleted(1);
        guidanceRecordMapper.updateById(record);
        log.info("教师 {} 删除指导记录: {}", currentUserId, recordId);
    }

    @Override
    public GuidanceRecordVO getGuidanceRecordDetail(String recordId) {
        GuidanceRecordVO detail = guidanceRecordMapper.selectRecordDetail(recordId);
        if (detail == null) {
            throw new BusinessException("指导记录不存在");
        }
        return detail;
    }

    @Override
    public List<GuidanceStudentVO> getMyStudents() {
        String currentUserId = SecurityUtil.getCurrentUserId();
        User currentUser = userMapper.selectById(currentUserId);

        if (currentUser == null) {
            throw new BusinessException("用户信息不存在");
        }

        // 根据用户角色决定指导类型
        // 企业教师：项目指导(1)，高校教师：论文指导(2)
        Integer guidanceType = hasRole("ENTERPRISE_TEACHER") ? 1 : 2;

        return guidanceRecordMapper.selectStudentsByTeacher(currentUserId, guidanceType);
    }

    @Override
    public List<GuidanceListVO> getStudentGuidanceRecords(String studentId) {
        return guidanceRecordMapper.selectByStudentId(studentId, null);
    }

    @Override
    public List<GuidanceListVO> getMyGuidanceRecords() {
        String currentUserId = SecurityUtil.getCurrentUserId();
        return guidanceRecordMapper.selectByStudentId(currentUserId, null);
    }

    @Override
    public PageResult<GuidanceListVO> getLeaderGuidanceOverview(GuidanceQueryVO queryVO) {
        String currentUserId = SecurityUtil.getCurrentUserId();
        User currentUser = userMapper.selectById(currentUserId);

        if (currentUser == null || currentUser.getEnterpriseId() == null) {
            throw new BusinessException("未找到您所属的企业信息");
        }

        String enterpriseId = currentUser.getEnterpriseId();

        // 查询全部数据后手动分页（复杂联表查询场景）
        List<GuidanceListVO> allList = guidanceRecordMapper.selectByEnterprise(
                enterpriseId,
                queryVO.getStudentName(),
                queryVO.getTeacherId(),
                queryVO.getGuidanceType(),
                queryVO.getStartDate(),
                queryVO.getEndDate()
        );

        // 手动分页
        int total = allList.size();
        int pageNum = queryVO.getPageNum();
        int pageSize = queryVO.getPageSize();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        
        List<GuidanceListVO> pageList = fromIndex < total ? allList.subList(fromIndex, toIndex) : Collections.emptyList();
        return new PageResult<>(pageList, (long) total);
    }

    @Override
    public byte[] exportGuidanceRecords(GuidanceQueryVO queryVO) {
        String currentUserId = SecurityUtil.getCurrentUserId();
        User currentUser = userMapper.selectById(currentUserId);

        if (currentUser == null || currentUser.getEnterpriseId() == null) {
            throw new BusinessException("未找到您所属的企业信息");
        }

        String enterpriseId = currentUser.getEnterpriseId();

        // 查询全部数据（不分页）
        List<GuidanceListVO> list = guidanceRecordMapper.selectByEnterprise(
                enterpriseId,
                queryVO.getStudentName(),
                queryVO.getTeacherId(),
                queryVO.getGuidanceType(),
                queryVO.getStartDate(),
                queryVO.getEndDate()
        );

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("指导记录");

            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 表头
            String[] headers = {"学生姓名", "学号", "课题名称", "指导教师", "指导类型",
                    "指导日期", "指导方式", "指导时长(小时)", "指导内容"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 数据行
            int rowNum = 1;
            for (GuidanceListVO vo : list) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(vo.getStudentName());
                row.createCell(1).setCellValue(vo.getStudentNo());
                row.createCell(2).setCellValue(vo.getTopicTitle());
                row.createCell(3).setCellValue(vo.getTeacherName());
                row.createCell(4).setCellValue(vo.getGuidanceTypeDesc());
                row.createCell(5).setCellValue(vo.getGuidanceDate());
                row.createCell(6).setCellValue(vo.getGuidanceMethod() != null ? vo.getGuidanceMethod() : "");
                row.createCell(7).setCellValue(vo.getDurationHours() != null ? vo.getDurationHours().toString() : "");
                row.createCell(8).setCellValue(vo.getContentSummary() != null ? vo.getContentSummary() : "");
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("导出指导记录失败", e);
            throw new BusinessException("导出指导记录失败");
        }
    }

    /**
     * 检查当前用户是否拥有指定角色
     */
    private boolean hasRole(String roleCode) {
        return SecurityUtil.getCurrentUserRoles().stream()
                .anyMatch(role -> role.equals(roleCode) || role.equals("ROLE_" + roleCode));
    }
}
