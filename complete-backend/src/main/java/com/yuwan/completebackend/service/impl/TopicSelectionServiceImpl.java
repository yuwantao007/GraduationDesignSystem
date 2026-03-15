package com.yuwan.completebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.exception.BusinessException;
import com.yuwan.completebackend.mapper.EnterpriseMapper;
import com.yuwan.completebackend.mapper.MajorDirectionMapper;
import com.yuwan.completebackend.mapper.MajorMapper;
import com.yuwan.completebackend.mapper.MajorTeacherMapper;
import com.yuwan.completebackend.mapper.TeacherAssignmentMapper;
import com.yuwan.completebackend.mapper.TeacherRelationshipMapper;
import com.yuwan.completebackend.mapper.TopicMapper;
import com.yuwan.completebackend.mapper.TopicSelectionMapper;
import com.yuwan.completebackend.mapper.UserMapper;
import com.yuwan.completebackend.model.dto.ApplyTopicDTO;
import com.yuwan.completebackend.model.dto.AssignTeacherDTO;
import com.yuwan.completebackend.model.entity.Enterprise;
import com.yuwan.completebackend.model.entity.Major;
import com.yuwan.completebackend.model.entity.MajorDirection;
import com.yuwan.completebackend.model.entity.MajorTeacher;
import com.yuwan.completebackend.model.entity.TeacherAssignment;
import com.yuwan.completebackend.model.entity.Topic;
import com.yuwan.completebackend.model.entity.TopicSelection;
import com.yuwan.completebackend.model.entity.User;
import com.yuwan.completebackend.model.enums.TopicReviewStatus;
import com.yuwan.completebackend.model.vo.SelectionForTeacherVO;
import com.yuwan.completebackend.model.vo.SelectionForUnivTeacherVO;
import com.yuwan.completebackend.model.vo.SelectionOverviewVO;
import com.yuwan.completebackend.model.vo.UnivTeacherPairingVO;
import com.yuwan.completebackend.model.vo.TeacherAssignmentVO;
import com.yuwan.completebackend.model.vo.TopicForSelectionVO;
import com.yuwan.completebackend.model.vo.TopicSelectionVO;
import com.yuwan.completebackend.model.vo.UnselectedStudentVO;
import com.yuwan.completebackend.security.SecurityUtil;
import com.yuwan.completebackend.service.ITopicSelectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;

/**
 * 课题选报服务实现类
 * 提供学生选报、企业教师确认人选、企业负责人双选审核的全链路业务功能
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class TopicSelectionServiceImpl implements ITopicSelectionService {

    private final TopicSelectionMapper topicSelectionMapper;
    private final TopicMapper topicMapper;
    private final UserMapper userMapper;
    private final EnterpriseMapper enterpriseMapper;
    private final MajorDirectionMapper majorDirectionMapper;
    private final MajorTeacherMapper majorTeacherMapper;
    private final MajorMapper majorMapper;
    private final TeacherAssignmentMapper teacherAssignmentMapper;
    private final TeacherRelationshipMapper teacherRelationshipMapper;

    /** 学生最多选报课题数 */
    private static final int MAX_SELECTION_COUNT = 3;

    /** 企业教师最多指导学生数 */
    private static final int MAX_CONFIRM_COUNT = 16;

    // ==================== 学生选报 ====================

    @Override
    public PageResult<TopicForSelectionVO> getAvailableTopics(
            Integer topicCategory,
            String guidanceDirection,
            String topicTitle,
            String majorId,
            int pageNum,
            int pageSize
    ) {
        String studentId = SecurityUtil.getCurrentUserId();
        if (studentId == null) {
            throw new BusinessException("用户未登录");
        }

        List<TopicForSelectionVO> allTopics = topicSelectionMapper.selectAvailableTopics(
                majorId, topicCategory, guidanceDirection, topicTitle, studentId
        );

        long total = allTopics.size();
        int fromIndex = (pageNum - 1) * pageSize;
        if (fromIndex >= total) {
            return new PageResult<>(List.of(), total, (long) pageNum, (long) pageSize);
        }
        int toIndex = Math.min(fromIndex + pageSize, (int) total);
        return new PageResult<>(allTopics.subList(fromIndex, toIndex), total, (long) pageNum, (long) pageSize);
    }

    @Override
    public TopicSelectionVO applyTopic(ApplyTopicDTO dto) {
        String studentId = SecurityUtil.getCurrentUserId();
        if (studentId == null) {
            throw new BusinessException("用户未登录");
        }

        User student = userMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("用户信息不存在");
        }
        if (!StringUtils.hasText(student.getUserPhone())) {
            throw new BusinessException("请先完善个人信息（手机号必填），再进行课题选报");
        }

        long selectedCount = topicSelectionMapper.selectCount(
                new LambdaQueryWrapper<TopicSelection>()
                        .eq(TopicSelection::getStudentId, studentId)
                        .eq(TopicSelection::getSelectionStatus, 1)
        );
        if (selectedCount > 0) {
            throw new BusinessException("您已中选课题，无需继续选报");
        }

        long activeCount = topicSelectionMapper.selectCount(
                new LambdaQueryWrapper<TopicSelection>()
                        .eq(TopicSelection::getStudentId, studentId)
                        .in(TopicSelection::getSelectionStatus, 0, 1)
        );
        if (activeCount >= MAX_SELECTION_COUNT) {
            throw new BusinessException("选报数量已达上限（最多" + MAX_SELECTION_COUNT + "个）");
        }

        long existCount = topicSelectionMapper.selectCount(
                new LambdaQueryWrapper<TopicSelection>()
                        .eq(TopicSelection::getStudentId, studentId)
                        .eq(TopicSelection::getTopicId, dto.getTopicId())
                        .in(TopicSelection::getSelectionStatus, 0, 1)
        );
        if (existCount > 0) {
            throw new BusinessException("您已选报过该课题，不能重复选报");
        }

        Topic topic = topicMapper.selectById(dto.getTopicId());
        if (topic == null || Integer.valueOf(1).equals(topic.getDeleted())) {
            throw new BusinessException("课题不存在");
        }
        if (topic.getReviewStatus() != TopicReviewStatus.FINAL_PASSED.getCode()) {
            throw new BusinessException("该课题尚未通过终审，无法选报");
        }

        TopicSelection selection = new TopicSelection();
        selection.setStudentId(studentId);
        selection.setTopicId(dto.getTopicId());
        selection.setSelectionReason(dto.getSelectionReason());
        selection.setSelectionStatus(0);
        selection.setApplyTime(new Date());
        topicSelectionMapper.insert(selection);

        log.info("学生 {} 选报课题 {} 成功，选报ID: {}", studentId, dto.getTopicId(), selection.getSelectionId());

        List<TopicSelectionVO> mySelections = topicSelectionMapper.selectMySelections(studentId);
        return mySelections.stream()
                .filter(s -> s.getSelectionId().equals(selection.getSelectionId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("选报记录创建异常"));
    }

    @Override
    public void deleteSelection(String selectionId) {
        String studentId = SecurityUtil.getCurrentUserId();
        if (studentId == null) {
            throw new BusinessException("用户未登录");
        }

        TopicSelection selection = topicSelectionMapper.selectById(selectionId);
        if (selection == null) {
            throw new BusinessException("选报记录不存在");
        }
        if (!selection.getStudentId().equals(studentId)) {
            throw new BusinessException("无权操作该选报记录");
        }
        if (selection.getSelectionStatus() != 2) {
            throw new BusinessException("仅落选的选报记录可以删除");
        }

        topicSelectionMapper.deleteById(selectionId);
        log.info("学生 {} 删除选报记录 {}", studentId, selectionId);
    }

    @Override
    public List<TopicSelectionVO> getMySelections() {
        String studentId = SecurityUtil.getCurrentUserId();
        if (studentId == null) {
            throw new BusinessException("用户未登录");
        }
        return topicSelectionMapper.selectMySelections(studentId);
    }

    // ==================== 教师确认子模块 ====================

    @Override
    public List<SelectionForTeacherVO> getSelectionsForTeacher(Integer selectionStatus) {
        String teacherId = SecurityUtil.getCurrentUserId();
        if (teacherId == null) {
            throw new BusinessException("用户未登录");
        }
        return topicSelectionMapper.selectByTeacher(teacherId, selectionStatus);
    }

    @Override
    public SelectionForTeacherVO confirmSelection(String selectionId) {
        String teacherId = SecurityUtil.getCurrentUserId();
        if (teacherId == null) {
            throw new BusinessException("用户未登录");
        }

        // 1. 查询选报记录
        TopicSelection selection = topicSelectionMapper.selectById(selectionId);
        if (selection == null) {
            throw new BusinessException("选报记录不存在");
        }

        // 2. 校验该选报对应的课题是否属于当前教师
        Topic topic = topicMapper.selectById(selection.getTopicId());
        if (topic == null || !teacherId.equals(topic.getCreatorId())) {
            throw new BusinessException("无权操作该选报记录");
        }

        // 3. 校验状态：只有待确认可以确认
        if (selection.getSelectionStatus() != 0) {
            throw new BusinessException("该选报记录当前状态不可确认");
        }

        // 4. 校验当前教师已确认的人数上限（≤16）
        long confirmedCount = topicSelectionMapper.selectCount(
                new LambdaQueryWrapper<TopicSelection>()
                        .eq(TopicSelection::getSelectionStatus, 1)
                        .inSql(TopicSelection::getTopicId,
                                "SELECT topic_id FROM topic_info WHERE creator_id = '" + teacherId + "' AND deleted = 0")
        );
        if (confirmedCount >= MAX_CONFIRM_COUNT) {
            throw new BusinessException("您的指导人数已达上限（最多" + MAX_CONFIRM_COUNT + "人），无法继续确认");
        }

        // 5. 将该学生在其他课题的待确认记录全部置为落选（联动）
        topicSelectionMapper.update(null,
                new LambdaUpdateWrapper<TopicSelection>()
                        .set(TopicSelection::getSelectionStatus, 2)
                        .eq(TopicSelection::getStudentId, selection.getStudentId())
                        .eq(TopicSelection::getSelectionStatus, 0)
                        .ne(TopicSelection::getSelectionId, selectionId)
        );

        // 6. 确认当前选报记录
        Date now = new Date();
        topicSelectionMapper.update(null,
                new LambdaUpdateWrapper<TopicSelection>()
                        .set(TopicSelection::getSelectionStatus, 1)
                        .set(TopicSelection::getConfirmTime, now)
                        .set(TopicSelection::getConfirmedBy, teacherId)
                        .eq(TopicSelection::getSelectionId, selectionId)
        );

        log.info("教师 {} 确认学生 {} 的课题选报 {}", teacherId, selection.getStudentId(), selectionId);

        // 7. 返回更新后的记录
        List<SelectionForTeacherVO> list = topicSelectionMapper.selectByTeacher(teacherId, null);
        return list.stream()
                .filter(v -> v.getSelectionId().equals(selectionId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public SelectionForTeacherVO rejectSelection(String selectionId) {
        String teacherId = SecurityUtil.getCurrentUserId();
        if (teacherId == null) {
            throw new BusinessException("用户未登录");
        }

        // 1. 查询选报记录
        TopicSelection selection = topicSelectionMapper.selectById(selectionId);
        if (selection == null) {
            throw new BusinessException("选报记录不存在");
        }

        // 2. 校验该课题是否属于当前教师
        Topic topic = topicMapper.selectById(selection.getTopicId());
        if (topic == null || !teacherId.equals(topic.getCreatorId())) {
            throw new BusinessException("无权操作该选报记录");
        }

        // 3. 只有待确认可以拒绝
        if (selection.getSelectionStatus() != 0) {
            throw new BusinessException("该选报记录当前状态不可拒绝");
        }

        // 4. 置为落选
        topicSelectionMapper.update(null,
                new LambdaUpdateWrapper<TopicSelection>()
                        .set(TopicSelection::getSelectionStatus, 2)
                        .set(TopicSelection::getConfirmTime, new Date())
                        .set(TopicSelection::getConfirmedBy, teacherId)
                        .eq(TopicSelection::getSelectionId, selectionId)
        );

        log.info("教师 {} 拒绝学生 {} 的课题选报 {}", teacherId, selection.getStudentId(), selectionId);

        List<SelectionForTeacherVO> list = topicSelectionMapper.selectByTeacher(teacherId, null);
        return list.stream()
                .filter(v -> v.getSelectionId().equals(selectionId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public byte[] exportConfirmedStudents() {
        String teacherId = SecurityUtil.getCurrentUserId();
        if (teacherId == null) {
            throw new BusinessException("用户未登录");
        }

        List<SelectionForTeacherVO> confirmedList = topicSelectionMapper.selectByTeacher(teacherId, 1);

        String[] headers = {"学生姓名", "学号", "手机号", "邮箱", "课题名称", "课题大类", "选报时间", "确认时间"};
        try (Workbook workbook = new HSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("已确认学生");
            CellStyle headerStyle = buildHeaderStyle(workbook);

            // 表头行
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            // 数据行
            for (int i = 0; i < confirmedList.size(); i++) {
                SelectionForTeacherVO vo = confirmedList.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(nullSafe(vo.getStudentName()));
                row.createCell(1).setCellValue(nullSafe(vo.getStudentNo()));
                row.createCell(2).setCellValue(nullSafe(vo.getStudentPhone()));
                row.createCell(3).setCellValue(nullSafe(vo.getStudentEmail()));
                row.createCell(4).setCellValue(nullSafe(vo.getTopicTitle()));
                row.createCell(5).setCellValue(nullSafe(vo.getTopicCategoryDesc()));
                row.createCell(6).setCellValue(nullSafe(vo.getApplyTime()));
                row.createCell(7).setCellValue(nullSafe(vo.getConfirmTime()));
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("导出已确认学生Excel失败", e);
            throw new BusinessException("导出失败：" + e.getMessage());
        }
    }

    // ==================== 双选审核子模块 ====================

    @Override
    public List<SelectionOverviewVO> getSelectionOverview() {
        String enterpriseId = resolveCurrentUserEnterpriseId();
        return topicSelectionMapper.selectOverviewByEnterprise(enterpriseId);
    }

    @Override
    public List<UnselectedStudentVO> getUnselectedStudents() {
        String enterpriseId = resolveCurrentUserEnterpriseId();
        return topicSelectionMapper.selectUnselectedStudents(enterpriseId);
    }

    @Override
    public byte[] exportSelectionInfo() {
        String enterpriseId = resolveCurrentUserEnterpriseId();
        List<SelectionOverviewVO> overviewList = topicSelectionMapper.selectOverviewByEnterprise(enterpriseId);

        String[] headers = {"课题名称", "课题大类", "课题来源", "企业教师", "指导方向", "选报总人数", "已确认人数", "待确认人数", "落选人数"};
        try (Workbook workbook = new HSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("双选结果概览");
            CellStyle headerStyle = buildHeaderStyle(workbook);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            for (int i = 0; i < overviewList.size(); i++) {
                SelectionOverviewVO vo = overviewList.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(nullSafe(vo.getTopicTitle()));
                row.createCell(1).setCellValue(nullSafe(vo.getTopicCategoryDesc()));
                row.createCell(2).setCellValue(nullSafe(vo.getTopicSourceDesc()));
                row.createCell(3).setCellValue(nullSafe(vo.getCreatorName()));
                row.createCell(4).setCellValue(nullSafe(vo.getGuidanceDirection()));
                row.createCell(5).setCellValue(vo.getTotalApplicants() != null ? vo.getTotalApplicants() : 0);
                row.createCell(6).setCellValue(vo.getConfirmedCount() != null ? vo.getConfirmedCount() : 0);
                row.createCell(7).setCellValue(vo.getPendingCount() != null ? vo.getPendingCount() : 0);
                row.createCell(8).setCellValue(vo.getRejectedCount() != null ? vo.getRejectedCount() : 0);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("导出选题信息Excel失败", e);
            throw new BusinessException("导出失败：" + e.getMessage());
        }
    }

    @Override
    public TeacherAssignmentVO assignTeacher(AssignTeacherDTO dto) {
        String leaderId = SecurityUtil.getCurrentUserId();
        if (leaderId == null) {
            throw new BusinessException("用户未登录");
        }

        // 1. 查询选报记录，推导 studentId 和 topicId
        TopicSelection selection = topicSelectionMapper.selectById(dto.getSelectionId());
        if (selection == null) {
            throw new BusinessException("选报记录不存在");
        }
        if (selection.getSelectionStatus() != 1) {
            throw new BusinessException("该学生尚未中选，无法指派教师");
        }
        // 优先使用 DTO 传入值，为空时从选报记录中推导
        String studentId = StringUtils.hasText(dto.getStudentId()) ? dto.getStudentId() : selection.getStudentId();
        String topicId = StringUtils.hasText(dto.getTopicId()) ? dto.getTopicId() : selection.getTopicId();

        // 2. 校验课题为校外协同开发来源
        Topic topic = topicMapper.selectById(topicId);
        if (topic == null) {
            throw new BusinessException("课题不存在");
        }
        if (!Integer.valueOf(2).equals(topic.getTopicSource())) {
            throw new BusinessException("仅校外协同开发课题的学生需要指派企业指导教师");
        }

        // 3. 校验被指派教师存在
        User teacher = userMapper.selectById(dto.getAssignedTeacherId());
        if (teacher == null) {
            throw new BusinessException("指派教师不存在");
        }

        // 4. 去重校验：同一学生对同一课题不能重复有效指派
        long existAssign = teacherAssignmentMapper.selectCount(
                new LambdaQueryWrapper<TeacherAssignment>()
                        .eq(TeacherAssignment::getStudentId, studentId)
                        .eq(TeacherAssignment::getTopicId, topicId)
                        .eq(TeacherAssignment::getAssignStatus, 1)
        );
        if (existAssign > 0) {
            throw new BusinessException("该学生已有有效的教师指派记录，请先取消再重新指派");
        }

        // 5. 创建指派记录
        TeacherAssignment assignment = new TeacherAssignment();
        assignment.setStudentId(studentId);
        assignment.setTopicId(topicId);
        assignment.setSelectionId(dto.getSelectionId());
        assignment.setAssignedTeacherId(dto.getAssignedTeacherId());
        assignment.setAssignedBy(leaderId);
        assignment.setAssignTime(new Date());
        assignment.setAssignStatus(1);
        teacherAssignmentMapper.insert(assignment);

        log.info("企业负责人 {} 为学生 {} 课题 {} 指派教师 {}", leaderId, studentId, topicId, dto.getAssignedTeacherId());

        String enterpriseId = resolveCurrentUserEnterpriseId();
        return teacherAssignmentMapper.selectAssignmentList(enterpriseId).stream()
                .filter(v -> v.getAssignmentId().equals(assignment.getAssignmentId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void cancelAssignment(String assignmentId) {
        String leaderId = SecurityUtil.getCurrentUserId();
        if (leaderId == null) {
            throw new BusinessException("用户未登录");
        }

        TeacherAssignment assignment = teacherAssignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            throw new BusinessException("指派记录不存在");
        }
        if (assignment.getAssignStatus() != 1) {
            throw new BusinessException("该指派记录已被取消");
        }

        teacherAssignmentMapper.update(null,
                new LambdaUpdateWrapper<TeacherAssignment>()
                        .set(TeacherAssignment::getAssignStatus, 0)
                        .set(TeacherAssignment::getCancelTime, new Date())
                        .eq(TeacherAssignment::getAssignmentId, assignmentId)
        );

        log.info("企业负责人 {} 取消指派记录 {}", leaderId, assignmentId);
    }

    @Override
    public List<TeacherAssignmentVO> getAssignmentList() {
        String enterpriseId = resolveCurrentUserEnterpriseId();
        return teacherAssignmentMapper.selectAssignmentList(enterpriseId);
    }

    // ==================== 高校教师查看选题 ====================

    @Override
    public List<SelectionForUnivTeacherVO> getSelectionsForUnivTeacher(Integer selectionStatus) {
        String univTeacherId = SecurityUtil.getCurrentUserId();
        if (univTeacherId == null) {
            throw new BusinessException("用户未登录");
        }
        return topicSelectionMapper.selectByUnivTeacher(univTeacherId, selectionStatus);
    }

    @Override
    public List<UnivTeacherPairingVO> getUnivTeacherPairings() {
        String univTeacherId = SecurityUtil.getCurrentUserId();
        if (univTeacherId == null) {
            throw new BusinessException("用户未登录");
        }
        return teacherRelationshipMapper.selectPairingsByUnivTeacher(univTeacherId);
    }

    @Override
    public byte[] exportSelectionsForUnivTeacher() {
        String univTeacherId = SecurityUtil.getCurrentUserId();
        if (univTeacherId == null) {
            throw new BusinessException("用户未登录");
        }

        List<SelectionForUnivTeacherVO> list = topicSelectionMapper.selectByUnivTeacher(univTeacherId, null);

        String[] headers = {"学生姓名", "学号", "手机号", "邮箱", "课题名称", "课题大类", "课题来源", "指导方向",
                "企业教师", "企业名称", "选报理由", "选报状态", "选报时间", "确认时间"};
        try (Workbook workbook = new HSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("指导学生选题结果");
            CellStyle headerStyle = buildHeaderStyle(workbook);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            for (int i = 0; i < list.size(); i++) {
                SelectionForUnivTeacherVO vo = list.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(nullSafe(vo.getStudentName()));
                row.createCell(1).setCellValue(nullSafe(vo.getStudentNo()));
                row.createCell(2).setCellValue(nullSafe(vo.getStudentPhone()));
                row.createCell(3).setCellValue(nullSafe(vo.getStudentEmail()));
                row.createCell(4).setCellValue(nullSafe(vo.getTopicTitle()));
                row.createCell(5).setCellValue(nullSafe(vo.getTopicCategoryDesc()));
                row.createCell(6).setCellValue(nullSafe(vo.getTopicSourceDesc()));
                row.createCell(7).setCellValue(nullSafe(vo.getGuidanceDirection()));
                row.createCell(8).setCellValue(nullSafe(vo.getEnterpriseTeacherName()));
                row.createCell(9).setCellValue(nullSafe(vo.getEnterpriseName()));
                row.createCell(10).setCellValue(nullSafe(vo.getSelectionReason()));
                row.createCell(11).setCellValue(nullSafe(vo.getSelectionStatusDesc()));
                row.createCell(12).setCellValue(nullSafe(vo.getApplyTime()));
                row.createCell(13).setCellValue(nullSafe(vo.getConfirmTime()));
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("导出指导学生选题结果Excel失败", e);
            throw new BusinessException("导出失败：" + e.getMessage());
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 解析当前用户所属企业ID
     * 支持：企业教师（方向/department/major_teacher）、企业负责人（enterprise_info.leader_id）
     * 路径1：user_info.direction_id → major_direction.enterprise_id
     * 路径2：user_info.department   → enterprise_info.enterprise_name
     * 路径3：major_teacher.user_id  → major.enterprise_id
     * 路径4：enterprise_info.leader_id = userId（企业负责人专用）
     */
    private String resolveCurrentUserEnterpriseId() {
        String userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 路径1：direction_id → 方向 → 企业
        if (StringUtils.hasText(user.getDirectionId())) {
            MajorDirection direction = majorDirectionMapper.selectById(user.getDirectionId());
            if (direction != null && StringUtils.hasText(direction.getEnterpriseId())) {
                return direction.getEnterpriseId();
            }
        }

        // 路径2：department 名称匹配企业
        if (StringUtils.hasText(user.getDepartment())) {
            Enterprise enterprise = enterpriseMapper.selectOne(
                    new LambdaQueryWrapper<Enterprise>()
                            .eq(Enterprise::getEnterpriseName, user.getDepartment())
            );
            if (enterprise != null) {
                return enterprise.getEnterpriseId();
            }
        }

        // 路径3：major_teacher 关联表
        MajorTeacher majorTeacher = majorTeacherMapper.selectOne(
                new LambdaQueryWrapper<MajorTeacher>()
                        .eq(MajorTeacher::getUserId, userId)
                        .last("LIMIT 1")
        );
        if (majorTeacher != null) {
            Major major = majorMapper.selectById(majorTeacher.getMajorId());
            if (major != null && StringUtils.hasText(major.getEnterpriseId())) {
                return major.getEnterpriseId();
            }
        }

        // 路径4：enterprise_info.leader_id（企业负责人专用）
        Enterprise enterprise = enterpriseMapper.selectOne(
                new LambdaQueryWrapper<Enterprise>()
                        .eq(Enterprise::getLeaderId, userId)
                        .last("LIMIT 1")
        );
        if (enterprise != null) {
            return enterprise.getEnterpriseId();
        }

        throw new BusinessException("无法确定用户所属企业，请联系管理员配置");
    }

    /**
     * 构建Excel表头样式（粗体）
     */
    private CellStyle buildHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    /**
     * 空值安全转换为字符串
     */
    private String nullSafe(Object value) {
        return value != null ? value.toString() : "";
    }
}
