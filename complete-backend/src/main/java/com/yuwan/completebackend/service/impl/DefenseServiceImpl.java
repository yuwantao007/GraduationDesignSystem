package com.yuwan.completebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuwan.completebackend.common.enums.DefenseType;
import com.yuwan.completebackend.common.enums.OpeningReportStatus;
import com.yuwan.completebackend.exception.BusinessException;
import com.yuwan.completebackend.mapper.DefenseArrangementMapper;
import com.yuwan.completebackend.mapper.NotificationTargetMapper;
import com.yuwan.completebackend.mapper.OpeningReportMapper;
import com.yuwan.completebackend.mapper.OpeningTaskBookMapper;
import com.yuwan.completebackend.mapper.TopicMapper;
import com.yuwan.completebackend.mapper.TopicSelectionMapper;
import com.yuwan.completebackend.mapper.UserMapper;
import com.yuwan.completebackend.model.dto.defense.*;
import com.yuwan.completebackend.model.entity.DefenseArrangement;
import com.yuwan.completebackend.model.entity.OpeningReport;
import com.yuwan.completebackend.model.entity.OpeningTaskBook;
import com.yuwan.completebackend.model.entity.Topic;
import com.yuwan.completebackend.model.entity.TopicSelection;
import com.yuwan.completebackend.model.enums.TopicCategory;
import com.yuwan.completebackend.model.entity.User;
import com.yuwan.completebackend.model.vo.defense.DefenseArrangementVO;
import com.yuwan.completebackend.model.vo.defense.OpeningReportVO;
import com.yuwan.completebackend.model.vo.defense.OpeningTaskBookVO;
import com.yuwan.completebackend.service.IDefenseService;
import com.yuwan.completebackend.service.INotificationDispatchService;
import com.yuwan.completebackend.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

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
    private final TopicSelectionMapper topicSelectionMapper;
    private final TopicMapper topicMapper;
    private final UserMapper userMapper;
    private final NotificationTargetMapper notificationTargetMapper;
    private final INotificationDispatchService notificationDispatchService;

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
        validatePanelTeachers(dto.getPanelTeachers());

        // 创建答辩安排
        DefenseArrangement arrangement = new DefenseArrangement();
        BeanUtil.copyProperties(dto, arrangement);
        arrangement.setEnterpriseId(enterpriseId);
        arrangement.setCreatorId(currentUserId);
        arrangement.setStatus(1);

        this.save(arrangement);
        sendArrangementNotification(arrangement, false);
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
        validatePanelTeachers(dto.getPanelTeachers());

        BeanUtil.copyProperties(dto, arrangement, "arrangementId", "enterpriseId", "creatorId");
        boolean updated = this.updateById(arrangement);
        if (updated) {
            sendArrangementNotification(arrangement, true);
        }
        return updated;
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
            queryDTO.getMajorId(),
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

    /**
     * 校验答辩小组结构：1名组长 + 2名答辩老师（共3名且不重复）
     */
    private void validatePanelTeachers(List<String> panelTeachers) {
        if (CollUtil.isEmpty(panelTeachers) || panelTeachers.size() != 3) {
            throw new BusinessException("答辩小组必须由1名组长和2名答辩老师组成");
        }
        if (CollectionUtil.distinct(panelTeachers).size() != 3) {
            throw new BusinessException("答辩小组成员不能重复");
        }
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
        OpeningTaskBookVO vo = taskBookMapper.selectTaskBookByStudentId(studentId);
        if (vo != null) {
            return vo;
        }

        // 历史数据兜底：若学生已中选但尚未生成任务书，则按课题申报自动回填
        autoBackfillTaskBookBySelection(studentId);
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
        validateReportStatus(dto.getStatus());

        User student = userMapper.selectById(currentUserId);
        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }
        Topic topic = topicMapper.selectById(dto.getTopicId());
        if (topic == null) {
            throw new BusinessException("课题不存在");
        }

        // 查询是否已存在报告
        LambdaQueryWrapper<OpeningReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OpeningReport::getStudentId, currentUserId);
        OpeningReport existing = reportMapper.selectOne(wrapper);

        if (existing != null) {
            if (OpeningReportStatus.FINALIZED.getCode().equals(existing.getStatus())) {
                throw new BusinessException("开题报告已定稿，不能再次修改");
            }

            applyReportContent(existing, dto, student, topic);
            reportMapper.updateById(existing);
            return existing.getReportId();
        } else {
            OpeningReport report = new OpeningReport();
            report.setStudentId(currentUserId);
            applyReportContent(report, dto, student, topic);
            reportMapper.insert(report);
            return report.getReportId();
        }
    }

    @Override
    public OpeningReportVO getMyReport() {
        String currentUserId = SecurityUtil.getCurrentUserId();
        OpeningReportVO vo = reportMapper.selectReportByStudentId(currentUserId);
        if (vo != null) {
            vo.setStatusName(OpeningReportStatus.getDescByCode(vo.getStatus()));
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
                queryDTO.getStatus(),
                queryDTO.getArrangementId()
        );

        // 填充状态名称
        for (OpeningReportVO vo : result.getRecords()) {
            vo.setStatusName(OpeningReportStatus.getDescByCode(vo.getStatus()));
        }
        return result;
    }

    @Override
    public OpeningReportVO getReportDetail(String reportId) {
        OpeningReportVO vo = reportMapper.selectReportById(reportId);
        if (vo == null) {
            throw new BusinessException("开题报告不存在");
        }
        vo.setStatusName(OpeningReportStatus.getDescByCode(vo.getStatus()));
        return vo;
    }

    private void validateReportStatus(Integer status) {
        if (status == null || OpeningReportStatus.getByCode(status) == null) {
            throw new BusinessException("开题报告状态无效");
        }
    }

    private void applyReportContent(OpeningReport report, SubmitReportDTO dto, User student, Topic topic) {
        report.setTopicId(dto.getTopicId());
        report.setArrangementId(dto.getArrangementId());
        report.setStudentName(StrUtil.blankToDefault(dto.getStudentName(), student.getRealName()));
        report.setMajorName(StrUtil.blankToDefault(dto.getMajorName(), student.getMajor()));
        report.setClassName(dto.getClassName());
        report.setTopicTitle(StrUtil.blankToDefault(dto.getTopicTitle(), topic.getTopicTitle()));
        report.setAdvisorNames(StrUtil.blankToDefault(dto.getAdvisorNames(), getAdvisorNamesByTopic(topic)));
        report.setReportDate(dto.getReportDate());
        report.setResearchStatus(dto.getResearchStatus());
        report.setPurposeSignificance(dto.getPurposeSignificance());
        report.setResearchContent(dto.getResearchContent());
        report.setInnovationPoints(dto.getInnovationPoints());
        report.setProblemsToSolve(dto.getProblemsToSolve());
        report.setProgressExpectation(dto.getProgressExpectation());
        report.setCurrentConditions(dto.getCurrentConditions());
        report.setAdvisorOpinion(dto.getAdvisorOpinion());
        report.setCollegeOpinion(dto.getCollegeOpinion());
        report.setStatus(dto.getStatus());

        if (OpeningReportStatus.FINALIZED.getCode().equals(dto.getStatus())) {
            report.setSubmitTime(new Date());
        } else {
            report.setSubmitTime(null);
        }
    }

    private String getAdvisorNamesByTopic(Topic topic) {
        if (topic == null || StrUtil.isBlank(topic.getCreatorId())) {
            return null;
        }
        User advisor = userMapper.selectById(topic.getCreatorId());
        return advisor != null ? advisor.getRealName() : null;
    }

    private void autoBackfillTaskBookBySelection(String studentId) {
        LambdaQueryWrapper<TopicSelection> selectionWrapper = new LambdaQueryWrapper<>();
        selectionWrapper.eq(TopicSelection::getStudentId, studentId)
                .eq(TopicSelection::getSelectionStatus, 1)
                .orderByDesc(TopicSelection::getConfirmTime)
                .last("LIMIT 1");

        TopicSelection selected = topicSelectionMapper.selectOne(selectionWrapper);
        if (selected == null) {
            return;
        }

        Topic topic = topicMapper.selectById(selected.getTopicId());
        if (topic == null) {
            return;
        }

        LambdaQueryWrapper<OpeningTaskBook> taskBookWrapper = new LambdaQueryWrapper<>();
        taskBookWrapper.eq(OpeningTaskBook::getStudentId, studentId);
        OpeningTaskBook existing = taskBookMapper.selectOne(taskBookWrapper);
        if (existing != null) {
            return;
        }

        OpeningTaskBook taskBook = new OpeningTaskBook();
        taskBook.setStudentId(studentId);
        taskBook.setTopicId(topic.getTopicId());
        taskBook.setTeacherId(topic.getCreatorId());
        taskBook.setContent(buildTaskBookContentFromTopic(topic));
        taskBookMapper.insert(taskBook);
    }

    private String buildTaskBookContentFromTopic(Topic topic) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h2>毕业设计（论文）任务书</h2>");
        appendTaskBookSection(sb, "课题名称", topic.getTopicTitle());
        appendTaskBookSection(sb, "指导方向", topic.getGuidanceDirection());
        appendTaskBookSection(sb, "选题背景与意义", topic.getBackgroundSignificance());
        appendTaskBookSection(sb, "课题内容简述", topic.getContentSummary());
        appendTaskBookSection(sb, "专业知识综合训练情况", topic.getProfessionalTraining());
        if (topic.getWorkloadWeeks() != null) {
            appendTaskBookSection(sb, "工作量总周数", String.valueOf(topic.getWorkloadWeeks()));
        }
        appendTaskBookSection(sb, "备注", topic.getRemark());

        if (sb.length() <= "<h2>毕业设计（论文）任务书</h2>".length()) {
            sb.append("<p>该课题已确认，暂无可同步的任务书正文内容，请联系指导教师完善。</p>");
        }
        return sb.toString();
    }

    private void appendTaskBookSection(StringBuilder sb, String title, String value) {
        if (StrUtil.isBlank(value)) {
            return;
        }
        sb.append("<h3>").append(title).append("</h3>")
                .append("<p>")
                .append(value.replace("\n", "<br/>"))
                .append("</p>");
    }

    private void sendArrangementNotification(DefenseArrangement arrangement, boolean updated) {
        String arrangementType = DefenseType.getDescByCode(arrangement.getDefenseType());
        String route = "/defense/arrangement/detail/" + arrangement.getArrangementId();
        Integer topicCategoryCode = resolveTopicCategoryCode(arrangement.getTopicCategory());

        Map<String, String> variables = new HashMap<>();
        variables.put("defenseType", arrangementType);
        variables.put("defenseTime", arrangement.getDefenseTime() != null
            ? DateUtil.format(arrangement.getDefenseTime(), "yyyy-MM-dd HH:mm")
            : "待定");
        variables.put("defenseLocation", StrUtil.blankToDefault(arrangement.getDefenseLocation(), "待定"));
        variables.put("action", updated ? "已更新" : "已发布");

        String suffix = updated ? (":" + System.currentTimeMillis()) : "";
        String dedupPrefix = "defense:arrangement:" + arrangement.getArrangementId() + suffix;

        Set<String> receiverIds = new HashSet<>();
        List<String> panelTeacherIds = new ArrayList<>();
        List<String> enterpriseTeacherIds;
        List<String> studentIds;
        if (CollUtil.isNotEmpty(arrangement.getPanelTeachers())) {
            panelTeacherIds.addAll(arrangement.getPanelTeachers());
            receiverIds.addAll(panelTeacherIds);
        }
        enterpriseTeacherIds = queryEnterpriseTeachersWithFallback(
            arrangement.getEnterpriseId(), topicCategoryCode, arrangement.getMajorId(), arrangement.getArrangementId());
        studentIds = queryStudentsWithFallback(
            arrangement.getEnterpriseId(), topicCategoryCode, arrangement.getMajorId(), arrangement.getArrangementId());

        receiverIds.addAll(enterpriseTeacherIds);
        receiverIds.addAll(studentIds);

        receiverIds.removeIf(id -> !StrUtil.isNotBlank(id));

        log.info("答辩通知接收人统计，arrangementId={}, panelTeachers={}, enterpriseTeachers={}, students={}, total={}",
            arrangement.getArrangementId(), panelTeacherIds.size(), enterpriseTeacherIds.size(), studentIds.size(), receiverIds.size());
        if (CollUtil.isEmpty(studentIds)) {
            log.warn("答辩通知未命中任何学生，arrangementId={}, enterpriseId={}, topicCategory={}, majorId={}",
                arrangement.getArrangementId(), arrangement.getEnterpriseId(), arrangement.getTopicCategory(), arrangement.getMajorId());
        }

        notificationDispatchService.sendBatchByTemplateAfterCommit(
                "DEFENSE_ARRANGEMENT_NOTICE",
                new ArrayList<>(receiverIds),
                "DEFENSE_ARRANGEMENT",
                arrangement.getArrangementId(),
                route,
                variables,
                dedupPrefix,
                null,
                arrangement.getDeadline()
        );
    }

    private Integer resolveTopicCategoryCode(String topicCategory) {
        if (StrUtil.isBlank(topicCategory)) {
            return null;
        }
        if (StrUtil.isNumeric(topicCategory)) {
            return Integer.parseInt(topicCategory);
        }
        for (TopicCategory category : TopicCategory.values()) {
            if (StrUtil.equals(category.getDesc(), topicCategory)) {
                return category.getCode();
            }
        }
        log.warn("答辩安排课题类别无法映射为数值编码，topicCategory={}", topicCategory);
        return null;
    }

    private List<String> queryStudentsWithFallback(String enterpriseId,
                                                   Integer topicCategoryCode,
                                                   String majorId,
                                                   String arrangementId) {
        List<String> studentIds = notificationTargetMapper.selectStudentIdsForArrangement(enterpriseId, topicCategoryCode, majorId);
        if (CollUtil.isNotEmpty(studentIds)) {
            return studentIds;
        }

        if (StrUtil.isNotBlank(majorId)) {
            studentIds = notificationTargetMapper.selectStudentIdsForArrangement(enterpriseId, topicCategoryCode, null);
            if (CollUtil.isNotEmpty(studentIds)) {
                log.warn("答辩通知学生匹配回退生效（忽略majorId），arrangementId={}, enterpriseId={}, topicCategory={}, majorId={}",
                        arrangementId, enterpriseId, topicCategoryCode, majorId);
                return studentIds;
            }
        }

        if (topicCategoryCode != null) {
            studentIds = notificationTargetMapper.selectStudentIdsForArrangement(enterpriseId, null, majorId);
            if (CollUtil.isNotEmpty(studentIds)) {
                log.warn("答辩通知学生匹配回退生效（忽略topicCategory），arrangementId={}, enterpriseId={}, topicCategory={}, majorId={}",
                        arrangementId, enterpriseId, topicCategoryCode, majorId);
                return studentIds;
            }
        }

        studentIds = notificationTargetMapper.selectStudentIdsForArrangement(enterpriseId, null, null);
        if (CollUtil.isNotEmpty(studentIds)) {
            log.warn("答辩通知学生匹配回退生效（仅按enterpriseId），arrangementId={}, enterpriseId={}", arrangementId, enterpriseId);
        }
        return studentIds;
    }

    private List<String> queryEnterpriseTeachersWithFallback(String enterpriseId,
                                                             Integer topicCategoryCode,
                                                             String majorId,
                                                             String arrangementId) {
        List<String> teacherIds = notificationTargetMapper.selectEnterpriseTeacherIdsForArrangement(enterpriseId, topicCategoryCode, majorId);
        if (CollUtil.isNotEmpty(teacherIds)) {
            return teacherIds;
        }

        if (StrUtil.isNotBlank(majorId)) {
            teacherIds = notificationTargetMapper.selectEnterpriseTeacherIdsForArrangement(enterpriseId, topicCategoryCode, null);
            if (CollUtil.isNotEmpty(teacherIds)) {
                log.warn("答辩通知企业教师匹配回退生效（忽略majorId），arrangementId={}, enterpriseId={}, topicCategory={}, majorId={}",
                        arrangementId, enterpriseId, topicCategoryCode, majorId);
                return teacherIds;
            }
        }

        if (topicCategoryCode != null) {
            teacherIds = notificationTargetMapper.selectEnterpriseTeacherIdsForArrangement(enterpriseId, null, majorId);
            if (CollUtil.isNotEmpty(teacherIds)) {
                log.warn("答辩通知企业教师匹配回退生效（忽略topicCategory），arrangementId={}, enterpriseId={}, topicCategory={}, majorId={}",
                        arrangementId, enterpriseId, topicCategoryCode, majorId);
                return teacherIds;
            }
        }

        teacherIds = notificationTargetMapper.selectEnterpriseTeacherIdsForArrangement(enterpriseId, null, null);
        if (CollUtil.isNotEmpty(teacherIds)) {
            log.warn("答辩通知企业教师匹配回退生效（仅按enterpriseId），arrangementId={}, enterpriseId={}", arrangementId, enterpriseId);
        }
        return teacherIds;
    }
}
